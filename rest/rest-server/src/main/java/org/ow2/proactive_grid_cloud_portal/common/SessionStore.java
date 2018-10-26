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
import org.jose4j.jwt.MalformedClaimException;
import org.ow2.proactive.boot.microservices.iam.exceptions.IAMException;
import org.ow2.proactive.boot.microservices.iam.util.IAMSessionUtil;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;


public class SessionStore {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private SchedulerRMProxyFactory schedulerRMProxyFactory = new SchedulerRMProxyFactory();

    private Clock clock = new Clock();

    private IAMSessionUtil iamSessionUtil = new IAMSessionUtil();

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

    // Create an IAMSession instead of the legacy Session
    public Session createIAMSession(String username, char[] password) {

        // Call IAM and create a new JWT
        AbstractMap.SimpleEntry<String, JwtClaims> token = iamSessionUtil.createNewSessionToken(username, password);

        // Create an IAMSession using the generated JWT
        IAMSession iamSession = new IAMSession(token.getKey(), token.getValue(), schedulerRMProxyFactory, clock);
        iamSession.setUserName(username);

        // Add IAMSession to SessionStore
        sessions.put(iamSession.getSessionId(), iamSession);

        return iamSession;
    }

    // Check that an IAMSession is still valid
    public void renewIAMSession(String sessionId) {

        IAMSession iamSession = (IAMSession) get(sessionId);

        try {

            String ssoTicket;

            // SSO ticket is a JWT
            if (iamSessionUtil.isJWTSession()) {
                JwtClaims jwtClaims = iamSession.getJwtClaims();
                ssoTicket = jwtClaims.getJwtId();
            }
            // SSO ticket is a ST
            else {
                ssoTicket = sessionId;
            }

            if (!iamSessionUtil.tokenIsValid(ssoTicket)) {
                throw new IAMException("SSO ticket [" + ssoTicket + "] is no longer valid.");
            }

            iamSessionUtil.createServiceToken(ssoTicket);

        } catch (MalformedClaimException e) {
            throw new IAMException("SSO token contains malformed session id (jti)");
        }
    }

    // Delete IAMSession (from IAM ticket registry)
    public void destroyIAMSession(String sessionId) throws NotConnectedException {

        IAMSession iamSession = (IAMSession) get(sessionId);

        if (iamSession != null) {
            try {

                String ssoTicket;

                // SSO ticket is a JWT
                if (iamSessionUtil.isJWTSession()) {
                    JwtClaims jwtClaims = iamSession.getJwtClaims();
                    ssoTicket = jwtClaims.getJwtId();
                }
                // SSO ticket is a ST
                else {
                    ssoTicket = sessionId;
                }

                if (!iamSessionUtil.deleteToken(ssoTicket)) {
                    throw new IAMException("Error occurred while destroying SSO ticket [" + ssoTicket +
                                           "] from IAM ticket registry.");
                }

            } catch (MalformedClaimException e) {
                throw new IAMException("SSO token contains malformed session id (jti)");
            }

        } else {
            throw new NotConnectedException(SchedulerStateRest.YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST);
        }
    }
}
