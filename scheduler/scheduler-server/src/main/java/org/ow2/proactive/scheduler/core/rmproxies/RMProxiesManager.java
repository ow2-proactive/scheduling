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
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * RMProxiesManager is in charge to manage objects communicating with RM.
 * It can contains either RMProxy or RMProxy interface to RM.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public abstract class RMProxiesManager {

    static class Connection {

        private final URI rmURI;

        private final RMAuthentication rmAuthentication;

        public Connection(URI rmURI, RMAuthentication rmAuthentication) {
            this.rmURI = rmURI;
            this.rmAuthentication = rmAuthentication;
        }

        public URI getRmURI() {
            return rmURI;
        }

        public RMAuthentication getRmAuthentication() {
            return rmAuthentication;
        }

    }

    /**
     * Create a RMProxiesManager using RM's URI (example : "rmi://localhost:1099/" ).
     *
     * @param rmURI The URI of a started Resource Manager
     * @return an instance of RMProxiesManager joined to the Resource Manager at the given URI
     */
    public static RMProxiesManager createRMProxiesManager(final URI rmURI)
            throws RMException, RMProxyCreationException, URISyntaxException {
        Credentials schedulerProxyCredentials;
        try {
            schedulerProxyCredentials = Credentials.getCredentials(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.RESOURCE_MANAGER_CREDS.getValueAsString()));

        } catch (Exception e) {
            throw new RMProxyCreationException(e);
        }

        boolean singleConnection = PASchedulerProperties.RESOURCE_MANAGER_SINGLE_CONNECTION.getValueAsBoolean();

        if (singleConnection) {
            return new SingleConnectionRMProxiesManager(rmURI, schedulerProxyCredentials);
        } else {
            return new PerUserConnectionRMProxiesManager(rmURI, schedulerProxyCredentials);
        }
    }

    protected final Credentials schedulerProxyCredentials;

    public RMProxiesManager(Credentials schedulerProxyCredentials) throws RMException, RMProxyCreationException {
        this.schedulerProxyCredentials = schedulerProxyCredentials;
    }

    public abstract URI getRmUrl();

    /**
     * Rebind a RMProxiesManager to another RM using its URI (example : "rmi://localhost:1099/" ).
     *
     * @param rmURI The URI of a started Resource Manager
     */
    public abstract void rebindRMProxiesManager(final URI rmURI) throws RMException, RMProxyCreationException;

    /**
     * Return the Scheduler RM proxy.
     * This metod will create the Scheduler proxy if it does not exist
     *
     * @return the Scheduler RM proxy.
     */
    public abstract RMProxy getRmProxy();

    /**
     * Return the user RM proxy associated with the given owner.
     * If the proxy does not exist, a new one will be created.
     *
     * @return the user RM proxy associated with the given owner.
     */
    public abstract RMProxy getUserRMProxy(String user, Credentials credentials) throws RMProxyCreationException;

    /**
     * Terminate the user RM proxy associated with the given user
     *
     * @param user the owner of the proxy to be terminated
     */
    public abstract void terminateRMProxy(final String user);

    public abstract void terminateAllProxies();

}
