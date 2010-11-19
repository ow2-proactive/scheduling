/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core.rmproxies;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * RMProxiesManager is in charge to manage objects communicating with RM.
 * It can contains either SchedulerRMProxy or UserRMProxy interface to RM.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class RMProxiesManager {

    private static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.RMPROXY);

    private static final Set<String> usersRMProxiesFilter = null;
    private static final Set<String> schedulerRMProxyFilter = null;
    private static final Map<URI, RMProxiesManager> proxiesByURL;

    static {
        proxiesByURL = new HashMap<URI, RMProxiesManager>();
        //usersRMProxiesFilter = new HashSet<String>();
        //usersRMProxiesFilter.add("releaseNode");
        //usersRMProxiesFilter.add("releaseNodes");
        //schedulerRMProxyFilter = new HashSet<String>();
        //usersRMProxiesFilter.add("shutdown");
    }

    /**
     * Get a RMProxiesManager using its URI (example : "rmi://localhost:1099/" ).
     *
     * @param uriIM The URI of a started Resource Manager
     * @return an instance of RMProxiesManager joined to the Resource Manager at the given URI
     */
    public static RMProxiesManager getRMProxiesManager(final URI uriRM) throws RMException {
        String url = uriRM.toString();
        RMAuthentication auth = RMConnection.join(url);
        //check if proxy exists
        RMProxiesManager proxy = proxiesByURL.get(uriRM);
        if (proxy == null) {
            proxy = new RMProxiesManager(auth);
            proxiesByURL.put(uriRM, proxy);
        }
        return proxy;
    }

    /** List of proxies */
    private Map<String, UserRMProxy> usersRMProxies;

    /** Scheduler proxies */
    private SchedulerRMProxy schedulerRMProxy;

    /** RM authentication interface */
    private RMAuthentication RMAuth;

    /**
     * Create a new instance of RMProxiesManager
     *
     * @param auth an RM authentication interface
     */
    public RMProxiesManager(RMAuthentication auth) {
        this.RMAuth = auth;
        this.usersRMProxies = new HashMap<String, UserRMProxy>();
    }

    /**
     * Return the Scheduler RM proxy.
     * This metod will create the Scheduler proxy if it does not exist
     *
     * @return the Scheduler RM proxy.
     */
    public synchronized ResourceManager getSchedulerRMProxy() throws RMProxyCreationException {
        if (schedulerRMProxy == null) {
            if (this.RMAuth != null) {
                try {
                    Credentials creds = Credentials
                            .getCredentials(PASchedulerProperties
                                    .getAbsolutePath(PASchedulerProperties.RESOURCE_MANAGER_CREDS
                                            .getValueAsString()));
                    schedulerRMProxy = createAOProxy(SchedulerRMProxy.class, creds, schedulerRMProxyFilter);
                } catch (Exception e) {
                    logger_dev.error("", e);
                    throw new RMProxyCreationException(e);
                }
            } else {
                throw new RMProxyCreationException(
                    "Cannot create Scheduler RM proxy because RM authentication is null");
            }
            logger_dev.info("RM Proxies Manager for Scheduler successfully started");
        }
        return schedulerRMProxy;
    }

    /**
     * Return the user RM proxy associated with the given owner.
     * If the proxy does not exist, a new one will be created.
     *
     * @param owner the user that owns or want a connection to RM
     * @return the user RM proxy associated with the given owner.
     */
    public synchronized ResourceManager getUserRMProxy(final InternalJob job) throws RMProxyCreationException {
        UserRMProxy urmp = usersRMProxies.get(job.getOwner());
        if (urmp == null) {
            if (this.RMAuth != null) {
                try {
                    urmp = createAOProxy(UserRMProxy.class, job.getCredentials(), usersRMProxiesFilter);
                    usersRMProxies.put(job.getOwner(), urmp);
                } catch (Exception e) {
                    logger_dev.error("", e);
                    throw new RMProxyCreationException(e);
                }
            } else {
                throw new RMProxyCreationException(
                    "Cannot create Scheduler RM proxy because RM authentication is null");
            }
            logger_dev.info("RM Proxies Manager for user '" + job.getOwner() + "' successfully started");
        }
        return urmp;
    }

    /**
     * Create a new proxy of the given class T.
     *
     * @param <T> must be a type inherited from {@link ResourceManager}
     * @param clazz the class from which to create the proxy
     * @param creds credentials to connect the ResourceManager
     * @param filters a list of methods that can be executed in the created object.
     * 			Other methods will throw an exception for each calls.
     * 			This argument can be null if no filter is needed.
     *
     * @return the new created RMProxy active object.
     * @throws RMProxyCreationException if there is a problem while creating the proxy
     */
    private <T extends ResourceManager> T createAOProxy(Class<T> clazz, Credentials creds, Set<String> filters)
            throws RMProxyCreationException {
        try {
            ResourceManager rm = this.RMAuth.login(creds);
            Object[] args = null;
            if (filters == null) {
                args = new Object[] { rm };
            } else {
                args = new Object[] { rm, filters };
            }
            return PAActiveObject.newActive(clazz, args);
        } catch (Exception e) {
            throw new RMProxyCreationException(e);
        }
    }

    /**
     * Terminate the Scheduler RM proxy
     */
    public synchronized void terminateSchedulerRMProxy() {
        if (schedulerRMProxy != null) {
            schedulerRMProxy.terminateProxy();
            schedulerRMProxy = null;
            logger_dev.info("RM Proxies Manager for Scheduler successfully terminated");
        }
    }

    /**
     * Terminate the user RM proxy associated with the given job owner
     *
     * @param job the job on which owner proxy will be terminated
     */
    public void terminateUserRMProxy(final InternalJob job) {
        terminateUserRMProxy(job.getOwner());
    }

    /**
     * Terminate the user RM proxy associated with the given owner argument
     *
     * @param owner the owner of the proxy to be terminated
     */
    public synchronized void terminateUserRMProxy(final String owner) {
        UserRMProxy urmp = usersRMProxies.remove(owner);
        if (urmp != null) {
            urmp.terminateProxy();
            logger_dev.info("RM Proxies Manager for user '" + owner + "' successfully terminated");
        }
    }

}
