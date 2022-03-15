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
package org.ow2.proactive.scheduler.core.rmproxies;

import java.net.URI;
import java.net.URISyntaxException;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SessionStore;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;


public final class SingleConnectionRMProxiesManager extends RMProxiesManager {

    private final RMProxy rmProxy;

    private URI rmURI;

    private SessionStore sessionStore;

    private Session currentSession;

    public SingleConnectionRMProxiesManager(URI rmURI, Credentials schedulerProxyCredentials)
            throws RMException, RMProxyCreationException, URISyntaxException {
        super(schedulerProxyCredentials);
        this.rmURI = rmURI;

        rmProxy = new RMProxy(rmURI, schedulerProxyCredentials);
        sessionStore = SharedSessionStore.getInstance();
    }

    @Override
    synchronized public URI getRmUrl() {
        return rmURI;
    }

    @Override
    synchronized public void rebindRMProxiesManager(URI rmURI) throws RMException, RMProxyCreationException {
        this.rmURI = rmURI;
        rmProxy.rebind(rmURI);
    }

    @Override
    public RMProxy getUserRMProxy(String user, Credentials credentials) throws RMProxyCreationException {
        synchronized (rmProxy) {
            if ("true".equals(System.getProperty(SchedulerStarter.REST_DISABLED_PROPERTY))) {
                return rmProxy;
            }
            if (currentSession == null) {
                currentSession = createUserSession(user, credentials, rmProxy);
            } else {
                if (sessionStore.exists(currentSession.getSessionId())) {
                    // current session still referenced in SessionStore, renewing it
                    try {
                        currentSession.renewSession();
                    } catch (NotConnectedException e) {
                        // current session is not connected to the scheduler, creating a new one
                        currentSession = createUserSession(user, credentials, rmProxy);
                    }
                } else {
                    // current session has been removed from SessionStore due to inactivity, creating a new one
                    currentSession = createUserSession(user, credentials, rmProxy);
                }
            }

            return rmProxy;
        }
    }

    @Override
    public void terminateRMProxy(String user) {
        // nothing to do
    }

    @Override
    public void terminateAllProxies() {
        rmProxy.terminate();
    }

    @Override
    public RMProxy getRmProxy() {
        return rmProxy;
    }

    private Session createUserSession(String user, Credentials credentials, RMProxy proxy)
            throws RMProxyCreationException {
        try {
            if (!"true".equals(System.getProperty(SchedulerStarter.REST_DISABLED_PROPERTY))) {
                // Rest session management works only if the rest server is launched in the same JVM as the scheduler server
                Session session = sessionStore.create(user);
                session.connectToRM(credentials);
                proxy.setSessionid(session.getSessionId());
                proxy.setUser(user);
                return session;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RMProxyCreationException(e);
        }
    }

}
