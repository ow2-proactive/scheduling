/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.common;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jose4j.jwt.JwtClaims;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;


public class SessionStore {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private SchedulerRMProxyFactory schedulerRMProxyFactory = new SchedulerRMProxyFactory();

    private IAMSessionUtil iamSessionUtil = new IAMSessionUtil();

    private Clock clock = new Clock();

    public Session createUnnamedSession() {
        String sessionId = SessionIdGenerator.newSessionId();
        Session session = new Session(sessionId, schedulerRMProxyFactory, clock);
        sessions.put(sessionId, session);
        return session;
    }

    public Session create(String username) {
        Session session = createUnnamedSession();
        session.setUserName(username);
        return session;
    }

    // Create an IAMSession instead of the legacy Session
    public Session create(String username, char[] password) {

        // Call IAM and create a new JWT
        AbstractMap.SimpleEntry<String, JwtClaims> jwt = iamSessionUtil.createNewSessionToken(username, password);

        // Create an IAMSession using the generated JWT
        IAMSession iamSession = new IAMSession(jwt.getKey(), jwt.getValue(), schedulerRMProxyFactory, clock);
        iamSession.setUserName(username);

        // Add IAMSession to SessionStore
        sessions.put(iamSession.getSessionId(), iamSession);

        return iamSession;
    }

    public boolean exists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public Session get(String sessionId) {
        return sessions.get(sessionId);
    }

    /** For testing only */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /** For testing only */
    public void setSchedulerRMProxyFactory(SchedulerRMProxyFactory schedulerRMProxyFactory) {
        this.schedulerRMProxyFactory = schedulerRMProxyFactory;
    }

    /** For testing only */
    public void setIamSessionUtil(IAMSessionUtil iamSessionUtil) {
        this.iamSessionUtil = iamSessionUtil;
    }

    public void terminate(String sessionId) {
        Session sessionToRemove = sessions.remove(sessionId);
        if (sessionToRemove != null) {
            sessionToRemove.terminate();
        }
    }

    public void terminateAll() {
        synchronized (sessions) {
            for (Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, Session> sessionEntry = iterator.next();
                Session session = sessionEntry.getValue();
                iterator.remove();
                session.terminate();
            }
        }
    }

    public int terminateExpiredSessions(long timeoutDelay) {
        synchronized (sessions) {
            int terminatedSessionCounter = 0;
            for (Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, Session> sessionEntry = iterator.next();
                Session session = sessionEntry.getValue();
                if (session.isExpired(timeoutDelay)) {
                    session.terminate();
                    iterator.remove();
                    terminatedSessionCounter++;
                }
            }
            return terminatedSessionCounter;
        }
    }

    public int size() {
        return sessions.size();
    }

    public void renewSession(String sessionId) throws NotConnectedException {
        Session session = sessions.get(sessionId);

        if (session != null) {
            session.renewSession();
        } else {
            throw new NotConnectedException(SchedulerStateRest.YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST);
        }
    }

    public void renewIAMSession(String sessionId) throws NotConnectedException {

        IAMSession iamSession = (IAMSession) get(sessionId);

        if (iamSession != null) {
            JwtClaims jwtClaims = iamSession.getJwtClaims();

            // If IAMSession expires create a new IAMSession and remove the old one
            if (!iamSessionUtil.tokenIsValid(jwtClaims)) {
                char[] password = iamSessionUtil.getTokenPassword(jwtClaims);

                // Create a new IAMSession using user credentials
                create(iamSession.getUserName(), password);
                sessionId = iamSession.getSessionId();

                // Remove the old IAMSession
                terminate(sessionId);
            }

            renewSession(sessionId);

        } else {
            throw new NotConnectedException(SchedulerStateRest.YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST);
        }
    }

}
