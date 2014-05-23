/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.common;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SessionStore {

    private final Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();
    private SchedulerRMProxyFactory schedulerRMProxyFactory = new SchedulerRMProxyFactory();
    private Clock clock = new Clock();

    public Session create() {
        String sessionId = SessionIdGenerator.newSessionId();
        Session session = new Session(sessionId, schedulerRMProxyFactory, clock);
        sessions.put(sessionId, session);
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

    public void terminate(String sessionId) {
        Session sessionToRemove = sessions.remove(sessionId);
        if (sessionToRemove != null) {
            sessionToRemove.terminate();
        }
    }

    public void terminateAll() {
        synchronized (sessions) {
            for (Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator(); iterator
                    .hasNext();) {
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
            for (Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator(); iterator
                    .hasNext();) {
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
}
