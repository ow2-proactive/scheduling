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
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SessionStore;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;


public final class PerUserConnectionRMProxiesManager extends RMProxiesManager {

    private final Map<URI, RMAuthentication> rmAuthentifications = new HashMap<>(2);

    private final Map<String, RMProxy> userProxiesMap = new HashMap<>();

    private final Map<String, Session> userSessions = new HashMap<>();

    private final RMProxy schedulerRMProxy;

    private Connection currentRMConnection;

    private RMProxyActiveObject rmProxyActiveObject;

    private final Object connectionStateLock = new Object();

    private SessionStore sessionStore;

    private URI rmURI;

    public PerUserConnectionRMProxiesManager(URI rmURI, Credentials schedulerProxyCredentials)
            throws RMException, RMProxyCreationException, URISyntaxException {
        super(schedulerProxyCredentials);
        this.rmURI = rmURI;
        schedulerRMProxy = new RMProxy(rmURI, schedulerProxyCredentials);
        sessionStore = SharedSessionStore.getInstance();
    }

    @Override
    public URI getRmUrl() {
        synchronized (connectionStateLock) {
            return rmURI;
        }
    }

    @Override
    public void rebindRMProxiesManager(URI rmURI) throws RMException, RMProxyCreationException {
        synchronized (connectionStateLock) {
            synchronized (userProxiesMap) {
                for (RMProxy userProxy : userProxiesMap.values()) {
                    userProxy.rebind(rmURI);
                }
            }
            schedulerRMProxy.rebind(rmURI);
        }
    }

    @Override
    public RMProxy getUserRMProxy(String user, Credentials credentials) throws RMProxyCreationException {
        synchronized (userProxiesMap) {
            RMProxy proxy = userProxiesMap.get(user);
            if (proxy == null) {
                try {
                    proxy = new RMProxy(rmURI, credentials);
                    userProxiesMap.put(user, proxy);
                } catch (RMException e) {
                    throw new RMProxyCreationException(e);
                }
                createUserSession(user, credentials, proxy);
            } else {
                if (!"true".equals(System.getProperty(SchedulerStarter.REST_DISABLED_PROPERTY))) {
                    // Rest session management works only if the rest server is launched in the same JVM as the scheduler server
                    try {
                        userSessions.get(user).renewSession();
                    } catch (Exception e) {
                        createUserSession(user, credentials, proxy);
                    }
                }
            }
            return proxy;
        }
    }

    private void createUserSession(String user, Credentials credentials, RMProxy proxy)
            throws RMProxyCreationException {
        try {
            if (!"true".equals(System.getProperty(SchedulerStarter.REST_DISABLED_PROPERTY))) {
                // Rest session management works only if the rest server is launched in the same JVM as the scheduler server
                Session session = sessionStore.create(user);
                session.connectToRM(credentials);
                proxy.setSessionid(session.getSessionId());
                userSessions.put(user, session);
            }
        } catch (Exception e) {
            throw new RMProxyCreationException(e);
        }
    }

    @Override
    public RMProxy getRmProxy() {
        return schedulerRMProxy;
    }

    @Override
    public void terminateRMProxy(String user) {
        synchronized (userProxiesMap) {
            RMProxy proxy = userProxiesMap.remove(user);
            if (proxy != null) {
                proxy.terminate();
            }
        }
    }

    @Override
    public void terminateAllProxies() {
        schedulerRMProxy.terminate();
        synchronized (connectionStateLock) {
            synchronized (userProxiesMap) {
                for (RMProxy userProxy : userProxiesMap.values()) {
                    userProxy.terminate();
                }
                userProxiesMap.clear();
            }
        }

    }
}
