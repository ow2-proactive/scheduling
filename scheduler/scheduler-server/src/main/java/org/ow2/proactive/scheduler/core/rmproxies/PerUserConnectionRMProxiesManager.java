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


public final class PerUserConnectionRMProxiesManager extends RMProxiesManager {

    private final Map<URI, RMAuthentication> rmAuthentifications = new HashMap<>(2);

    private final Map<String, RMProxy> userProxiesMap = new HashMap<>();

    private final RMProxy schedulerRMProxy;

    private Connection currentRMConnection;

    private RMProxyActiveObject rmProxyActiveObject;

    private final Object connectionStateLock = new Object();

    private URI rmURI;

    public PerUserConnectionRMProxiesManager(URI rmURI, Credentials schedulerProxyCredentials)
            throws RMException, RMProxyCreationException, URISyntaxException {
        super(schedulerProxyCredentials);
        this.rmURI = rmURI;
        schedulerRMProxy = new RMProxy(rmURI, schedulerProxyCredentials);
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
            }
            return proxy;
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
