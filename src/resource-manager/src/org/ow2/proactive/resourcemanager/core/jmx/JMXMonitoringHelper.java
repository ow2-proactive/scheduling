/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.jmx;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.jmx.connector.PAAuthenticationConnectorServer;
import org.ow2.proactive.jmx.naming.JMXProperties;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBeanImpl;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBeanImpl;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * This helper class represents the RMI based JMX monitoring infrastructure of the Resource Manager.
 * Two levels of JMX monitoring are possible using two separate MBeans, one for
 * anonymous users through the {@link RMAnonymMBean} interface and one for administrators
 * through the {@link RMAdminMBean} interface.
 * <p>
 * The service url of the connector server for anonymous access is built like
 * <code>service:jmx:rmi:///jndi/rmi://HOSTNAME_OR_IP:RM_JMX_PORT/RM_JMX_CONNECTOR_NAME</code> where
 * <ul>
 * <li><code>HOSTNAME_OR_IP</code> is specified by the ProActive network configuration.
 * <li><code>RM_JMX_PORT</code> is specified by the <code>pa.rm.jmx.port</code> property of the Resource Manager.
 * <li><code>RM_JMX_CONNECTOR_NAME</code> is specified by the <code>pa.rm.jmx.connectorname</code> property of the Resource Manager. 
 * </ul> 
 * <p>
 * The service url of the connector server for administrators is the same as precedent except that the
 * url must end with {@link JMXProperties.JMX_ADMIN}.
 * <p>  
 * Once booted with {@link #boot(AuthenticationImpl)} or with {@link #boot(AuthenticationImpl, RMAnonymMBean, RMAdminMBean)} the
 * infrastructure can be shutdown with the {@link #shutdown()} method.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public final class JMXMonitoringHelper {

    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.MONITORING);

    private static final JMXMonitoringHelper instance = new JMXMonitoringHelper();

    /** The name of the Resource Manager bean */
    public static final String RM_BEAN_NAME = "RMFrontend:name=RMBean";

    /**
     * Returns the single instance of this class.
     * 
     * @return the single instance of this class
     */
    public static JMXMonitoringHelper getInstance() {
        return JMXMonitoringHelper.instance;
    }

    /** The JMX connector server for {@link RMAnonymMBean} MBeans */
    private PAAuthenticationConnectorServer anonymConnectorServer;

    /** The JMX connector server for {@link RMAdminMBean} MBeans */
    private PAAuthenticationConnectorServer adminConnectorServer;

    /** The reason of the failure in case on unsuccessful boot sequence */
    private String failureReason;

    /**
     * Creates {@link RMAnonymMBeanImpl} and {@link RMAdminMBeanImpl} and 
     * starts the boot sequence by calling {@link #boot(AuthenticationImpl, RMAnonymMBean, RMAdminMBean)} method.
     * 
     * @param authentication the object responsible for authentication
     * @return <code>true</code> if the boot sequence was successful, otherwise returns <code>false</code> 
     */
    public boolean boot(final AuthenticationImpl authentication) {
        RMAnonymMBean anonymMBean = null;
        RMAdminMBean adminMBean = null;
        try {
            anonymMBean = new RMAnonymMBeanImpl(RMMonitoringImpl.rmStatistics);
            adminMBean = new RMAdminMBeanImpl(RMMonitoringImpl.rmStatistics);
        } catch (Throwable t) {
            logger.error(failureReason = "Unable to create the Resource Manager's JMX MBeans", t);
            return false;
        }
        return boot(authentication, anonymMBean, adminMBean);
    }

    /**
     * Starts the boot sequence of the Resource Manager's JMX monitoring infrastructure.
     * <p>
     * <ul>
     * <li>Creates a new RMI registry or reuses an existing one needed for the connector servers.
     * <li>Create two MBean servers, one for administrator users and one for anonymous users.
     * <li>Registers the MBeans given in parameters into their respective MBean servers.
     * <li>Creates and starts the connector servers.
     * </ul>
     *
     * @param authentication the object responsible for authentication
     * @param anonymMBean an MBean that provides information for anonymous users
     * @param adminMBean an MBean that provides information for administrator users
     * @return <code>true</code> if the boot sequence was successful, otherwise returns <code>false</code>
     */
    public boolean boot(final AuthenticationImpl authentication, final RMAnonymMBean anonymMBean,
            final RMAdminMBean adminMBean) {
        // Create or reuse an RMI registry needed for the connector servers
        try {
            LocateRegistry.createRegistry(PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt());
        } catch (ExportException ee) {
            // Reusing existing registry
            // do nothing and continue starting JMX
            if (logger.isDebugEnabled()) {
                logger.debug("Reusing existing RMI registry on port " +
                    PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt());
            }
        } catch (RemoteException e) {
            // This can occur if the port is already occupied
            logger.error(failureReason = "Unable to create an RMI registry on port " +
                PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt(), e);
            // do not start JMX service
            return false;
        }

        // Create two MBean servers, one for administrator users and one for
        // anonymous users
        MBeanServer mbsAnonym = null;
        MBeanServer mbsAdmin = null;
        try {
            mbsAnonym = MBeanServerFactory.createMBeanServer();
            mbsAdmin = MBeanServerFactory.createMBeanServer();
        } catch (Throwable t) {
            logger.error(failureReason = "Unable to create the Resource Manager's JMX MBean servers", t);
            return false;
        }

        // Register the two MBeans into their related server
        ObjectName rMNameAnonym = null;
        ObjectName rMNameAdmin = null;
        try {
            // Uniquely identify the MBeans and register them with the MBeanServer
            rMNameAnonym = new ObjectName(RM_BEAN_NAME);
            rMNameAdmin = new ObjectName(RM_BEAN_NAME + "_" + JMXProperties.JMX_ADMIN);
            mbsAnonym.registerMBean(anonymMBean, rMNameAnonym);
            mbsAdmin.registerMBean(adminMBean, rMNameAdmin);
        } catch (Throwable t) {
            logger.error(failureReason = "Unable to register the Resource Manager's JMX MBeans", t);
            return false;
        }

        // Use the same hostname as ProActive (follows properties defined by ProActive configuration)
        final String hostname = ProActiveInet.getInstance().getHostname();
        // The asked address of the new connector server. The actual address can be different due to 
        // JMX specification. See {@link JMXConnectorServerFactory} documentation.
        final String jmxConnectorURL = "service:jmx:rmi:///jndi/rmi://" + hostname + ":" +
            PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + "/";
        // The name of the connector as specified in the Resource Manager's configuration file.
        final String jmxConnectorName = PAResourceManagerProperties.RM_JMX_CONNECTOR_NAME.getValueAsString();

        // Create the connector servers 
        this.anonymConnectorServer = new PAAuthenticationConnectorServer(jmxConnectorURL, jmxConnectorName,
            mbsAnonym, authentication, true, logger);
        this.adminConnectorServer = new PAAuthenticationConnectorServer(jmxConnectorURL, jmxConnectorName +
            "_" + JMXProperties.JMX_ADMIN, mbsAdmin, authentication, false, logger);

        // Start the connector servers
        try {
            this.anonymConnectorServer.start();
            this.adminConnectorServer.start();
        } catch (Throwable t) {
            logger.error(failureReason = "Unable to start the Resource Manager's JMX connector servers", t);
            this.shutdown();
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Address of the Resource Manager's anonym JMX connector server: " +
                this.anonymConnectorServer.getAddress());
        }
        return true;
    }

    /**
     * Shutdown the Resource Manager's JMX monitoring infrastructure.
     */
    public void shutdown() {
        try {
            // Shutdown the connector server for anonym MBean
            if (this.anonymConnectorServer != null) {
                this.anonymConnectorServer.stop();
            }
        } catch (Throwable t) {
            // Log this only in debug
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to stop the Resource Manager's anonym JMX connector server", t);
            }
        }
        try {
            // Shutdown the connector server for admin MBean
            if (this.adminConnectorServer != null) {
                this.adminConnectorServer.stop();
            }
        } catch (Throwable t) {
            // Log this only in debug
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to stop the Resource Manager's admin JMX connector server", t);
            }
        }
    }

    /**
     * Returns the address of the anonymous connector server.
     * 
     * @return the address of the anonymous connector server
     * @throws JMException in case of boot sequence failure
     */
    public JMXServiceURL getAddress() throws JMException {
        if (this.failureReason == null) {
            this.failureReason = "Unknown failure. It is possible that the JMX monitoring infrastructure was not booted";
        }
        if (this.anonymConnectorServer == null) {
            throw new JMException(failureReason);
        }
        final JMXServiceURL address = this.anonymConnectorServer.getAddress();
        if (address == null) {
            throw new JMException(this.failureReason);
        }
        return address;
    }
}