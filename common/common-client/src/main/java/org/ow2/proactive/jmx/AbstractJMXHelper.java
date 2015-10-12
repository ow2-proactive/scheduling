/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.jmx;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.util.HashMap;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.authentication.Authentication;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.jmx.provider.JMXProviderUtils;


/**
 * This helper class represents the common JMX monitoring infrastructure.
 * Two levels of JMX monitoring are possible using two separate MBeans, one for
 * anonymous users and one for administrators.
 * <p>
 * Sub-classes must register these MBeans into the MBean server then provide an {@link ObjectName} for each MBean.
 * <p>
 * This helper exposes two connector servers, one over RMI and one over RO (Remote Objects). Each
 * connector server exposes the anonymous MBean for anonymous users and same for admin users.
 * <p>
 * The service url of the connector server over RMI for anonymous access is built like
 * <code>service:jmx:rmi:///jndi/rmi://HOSTNAME_OR_IP:PORT/NAME</code> where
 * <ul>
 * <li><code>HOSTNAME_OR_IP</code> is specified by the ProActive network configuration.
 * <li><code>PORT</code> is provided by sub-classes by the {@link #getJMXRMIConnectorServerPort()} method.
 * <li><code>NAME</code> is provided by sub-classes by the {@link #getConnectorServerName()} method.
 * </ul>
 * <p>
 * Once booted with {@link #boot(AuthenticationImpl)} infrastructure can be shutdown with the {@link #shutdown()} method.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public abstract class AbstractJMXHelper {
    /** The logger provided by sub-classes */
    private final Logger logger;
    /** The standard JMX over RMI connector server */
    private JMXConnectorServer rmiCs;
    /** The custom JMX over RO connector server */
    private JMXConnectorServer roCs;
    /** The reason of the failure in case on unsuccessful boot sequence of the JMX RMI */
    private String jmxRmiFailureReason;
    /** The reason of the failure in case on unsuccessful boot sequence of the JMX RO */
    private String jmxRoFailureReason;
    /** RRD data base dumper */
    private RRDDataStore dataStore;

    /** Can be called only by sub-classes */
    protected AbstractJMXHelper() {
        this.logger = Logger.getLogger(AbstractJMXHelper.class);
    }

    /** Can be called only by sub-classes */
    protected AbstractJMXHelper(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Starts the boot sequence of of the JMX monitoring infrastructure with a new JMX server.
     * <p>
     * <ul>
     * <li>Creates a new RMI registry or reuses an existing one needed for the JMX RMI connector server.
     * <li>Create a a single MBean server for both administrator users and for anonymous users.
     * <li>Registers the MBeans into the MBean server.
     * <li>Creates and starts the connector servers, one over RMI and one over RO.
     * </ul>
     *
     * @param auth the object responsible for authentication
     * @return <code>true</code> if the boot sequence was successful (at least one of two connector servers were started), otherwise returns <code>false</code>
     */
    public final boolean boot(final Authentication auth) {
        return boot(auth, true, null);
    }

    /**
     * Starts the boot sequence of of the JMX monitoring infrastructure.
     * <p>
     * <ul>
     * <li>Creates a new RMI registry or reuses an existing one needed for the JMX RMI connector server.
     * <li>Create a a single MBean server for both administrator users and for anonymous users.
     * <li>Registers the MBeans into the MBean server.
     * <li>Creates and starts the connector servers, one over RMI and one over RO.
     * </ul>
     *
     * @param auth the object responsible for authentication
     * @param createNewServer defined is new JMX server will be created or default will be used. 
     * @param permissionChecker additional permission checker
     * @return <code>true</code> if the boot sequence was successful (at least one of two connector servers were started), otherwise returns <code>false</code>
     */
    public final boolean boot(final Authentication auth, boolean createNewServer,
            PermissionChecker permissionChecker) {
        // Create a single MBean server
        MBeanServer mbs = null;
        try {
            if (createNewServer) {
                mbs = MBeanServerFactory.createMBeanServer();
            } else {
                mbs = ManagementFactory.getPlatformMBeanServer();
            }
        } catch (Exception e) {
            logger.error(jmxRmiFailureReason = jmxRoFailureReason = "Unable to create the JMX MBean server",
                    e);
            return false;
        }
        // Create an authenticator that will be used by the connectors
        final JMXAuthenticator authenticator = new JMXAuthenticatorImpl(auth, permissionChecker);

        // Let sub-classes create a the MBean server forwarder
        this.registerMBeans(mbs);

        // Sub-classes provides the name of the connector server and the port
        final String serverName = this.getConnectorServerName();
        final int port = this.getJMXRMIConnectorServerPort();

        // Boot the JMX RMI connector server
        final boolean isJMXRMIbooted = this.createJMXRMIConnectorServer(mbs, serverName, port, authenticator);
        if (isJMXRMIbooted) {
            try {
                this.rmiCs.start();
                if (logger.isInfoEnabled()) {
                    logger.info("Started JMX RMI connector server at " + this.rmiCs.getAddress());
                }
            } catch (Exception e) {
                logger.error(this.jmxRoFailureReason = "Unable to start the JMX RMI connector server", e);
            }
        }
        // Boot the JMX RO connector server
        final boolean isJMXRObooted = this.createJMXROConnectorServer(mbs, serverName, authenticator);
        if (isJMXRObooted) {
            try {
                this.roCs.start();
                if (logger.isInfoEnabled()) {
                    logger.info("Started JMX RO connector server at " + this.roCs.getAddress());
                }
            } catch (Exception e) {
                logger.error(this.jmxRoFailureReason = "Unable to start the JMX RO connector server", e);
            }
        }
        return isJMXRMIbooted || isJMXRObooted;
    }

    /**
     * Sub-class must register the MBeans into the MBean server.
     *
     * @param mbs the MBean server
     */
    public abstract void registerMBeans(final MBeanServer mbs);

    /**
     * Sub-classes must provide the name of the connector server.
     *
     * @return the name of the connector server
     */
    public abstract String getConnectorServerName();

    /**
     * Sub-classes must provide the port to be used by the JMX RMI connector server.
     * with correct values.
     *
     * @return the JMX RMI connector server port
     */
    public abstract int getJMXRMIConnectorServerPort();

    private boolean createJMXRMIConnectorServer(final MBeanServer mbs, final String connectorServerName,
            final int port, final JMXAuthenticator authenticator) {
        // Create or reuse an RMI registry needed for the connector server
        try {
            LocateRegistry.createRegistry(port);
        } catch (ExportException ee) {
            // Reusing existing registry
            // do nothing and continue starting JMX
            if (logger.isDebugEnabled()) {
                logger.debug("Reusing existing RMI registry on port " + port);
            }
        } catch (RemoteException e) {
            // This can occur if the port is already occupied
            logger.error(jmxRmiFailureReason = "Unable to create an RMI registry on port " + port, e);
            // do not start JMX service
            return false;
        }
        // Use the same hostname as ProActive (follows properties defined by ProActive configuration)
        final String hostname = ProActiveInet.getInstance().getHostname();
        // The asked address of the new connector server. The actual address can be different due to
        // JMX specification. See {@link JMXConnectorServerFactory} documentation.
        final String jmxConnectorServerURL = "service:jmx:rmi:///jndi/rmi://" + hostname + ":" + port + "/" +
            connectorServerName;
        JMXServiceURL jmxUrl = null;
        try {
            jmxUrl = new JMXServiceURL(jmxConnectorServerURL);
        } catch (MalformedURLException e) {
            logger.error(jmxRmiFailureReason = "Unable to create the JMXServiceURL from " +
                jmxConnectorServerURL, e);
            return false;
        }
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
        // Create the connector server
        try {
            this.rmiCs = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, env, mbs);
        } catch (Exception e) {
            logger.error(jmxRmiFailureReason = "Unable to create the JMXConnectorServer at " + jmxUrl, e);
            return false;
        }
        return true;
    }

    private boolean createJMXROConnectorServer(final MBeanServer mbs, final String connectorServerName,
            final JMXAuthenticator authenticator) {

        // Get the base uri (seems to always end with '/')
        URI baseURI = null;
        try {
            baseURI = JMXProviderUtils.getBaseURI();
        } catch (Exception e) {
            logger.error(jmxRoFailureReason = "Unable to get the base uri", e);
            return false;
        }
        // The asked address of the new connector server. The actual address can be different due to
        // JMX specification. See {@link JMXConnectorServerFactory} documentation.
        final String jmxConnectorServerURL = "service:jmx:" + JMXProviderUtils.RO_PROTOCOL + ":///jndi/" +
            baseURI + connectorServerName;
        JMXServiceURL jmxUrl = null;
        try {
            jmxUrl = new JMXServiceURL(jmxConnectorServerURL);
        } catch (MalformedURLException e) {
            logger.error(jmxRoFailureReason = "Unable to create the JMXServiceURL from " +
                jmxConnectorServerURL, e);
            return false;
        }
        final HashMap<String, Object> env = new HashMap<>(2);
        env.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
        env.put(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES, JMXProviderUtils.RO_PROVIDER_PKGS);
        // Create the connector server
        try {
            this.roCs = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, env, mbs);
        } catch (Exception e) {
            logger.error(jmxRoFailureReason = "Unable to create the JMXConnectorServer at " + jmxUrl, e);
            return false;
        }
        return true;
    }

    /**
     * Shutdown the JMX monitoring infrastructure.
     */
    public void shutdown() {
        try {
            // Shutdown the JMX RMI connector server
            if (this.rmiCs != null) {
                this.rmiCs.stop();
            }
        } catch (Exception t) {
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to stop the JMX-RMI connector server", t);
            }
        }
        try {
            // Shutdown the JMX RO connector server
            if (this.roCs != null) {
                this.roCs.stop();
            }
        } catch (Exception t) {
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to stop the JMX-RO connector server", t);
            }
        }

        if (dataStore != null) {
            dataStore.terminate();
        }
    }

    /**
     * Returns the address of the JMX connector server depending on the specified protocol.
     *
     * @param protocol the JMX transport protocol
     * @return the address of the connector server
     * @throws JMException in case of boot sequence failure
     */
    public JMXServiceURL getAddress(final JMXTransportProtocol protocol) throws JMException {
        JMXServiceURL address;
        switch (protocol) {
            case RMI:
                if (this.jmxRmiFailureReason == null) {
                    this.jmxRmiFailureReason = "Unknown failure. It is possible that the JMX-RMI monitoring infrastructure was not booted";
                }
                if (this.rmiCs == null) {
                    throw new JMException(this.jmxRmiFailureReason);
                }
                address = this.rmiCs.getAddress();
                if (address == null) {
                    throw new JMException(this.jmxRmiFailureReason);
                }
                return address;
            case RO:
                if (this.jmxRoFailureReason == null) {
                    this.jmxRoFailureReason = "Unknown failure. It is possible that the JMX-RO monitoring infrastructure was not booted";
                }
                if (this.roCs == null) {
                    throw new JMException(this.jmxRoFailureReason);
                }
                address = this.roCs.getAddress();
                if (address == null) {
                    throw new JMException(this.jmxRoFailureReason);
                }
                return address;
            default:
                throw new JMException("Uknown JMX transport protocol: " + protocol);
        }
    }

    /**
     * Sets the rrd data store.
     * @param dataStore
     */
    public void setDataStore(RRDDataStore rrdDataStore) {
        this.dataStore = rrdDataStore;
    }

    /**
     * Gets the RRD data base with statistics.
     * @return RRD data base with statistics.
     */
    public RRDDataStore getDataStore() {
        return dataStore;
    }
}
