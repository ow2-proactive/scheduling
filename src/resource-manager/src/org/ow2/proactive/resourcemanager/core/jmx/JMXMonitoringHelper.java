/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.jmx;

import java.io.Serializable;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.jmx.connector.PAAuthenticationConnectorServer;
import org.ow2.proactive.jmx.naming.JMXProperties;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * JMX Helper Class for the RM to create the MBeanServer Views and the Connectors
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JMXMonitoringHelper implements Serializable {

    /** logger device */
    public static final Logger logger = ProActiveLogger.getLogger(RMLoggers.MONITORING);
    private static final String RM_BEAN_NAME = "RMFrontend:name=RMBean";

    private static final String JMX_CONNECTOR_NAME = PAResourceManagerProperties.RM_JMX_CONNECTOR_NAME
            .getValueAsString();

    /**
     * The default jmx Connector Server url for the RM
     */
    private static String DEFAULT_JMX_CONNECTOR_URL;

    public static String getDefaultJmxConnectorUrl() {
	if (DEFAULT_JMX_CONNECTOR_URL == null){
		String hostname = "localhost";
		try {
			hostname = PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()).getVMInformation().getHostName();
		} catch (Throwable t){
			logger.warn("Cannot set host name in JMX default connector URL",t);
		}
		DEFAULT_JMX_CONNECTOR_URL =
			"service:jmx:rmi:///jndi/rmi://" +
			hostname + ":" +
			PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + "/";
	}
	return DEFAULT_JMX_CONNECTOR_URL;
    }

    /** RM`s MBeanServer */
    private MBeanServer mbsAnonym;
    private MBeanServer mbsAdmin;

    /**
     * Create the MBean Objects at the starting of the RM
     * and register them on the related MBeanServer based on the View
     */
    public void createMBeanServers() {
        // Create one MBeanServer for each View
        this.mbsAnonym = MBeanServerFactory.createMBeanServer();
        this.mbsAdmin = MBeanServerFactory.createMBeanServer();
        // Get the Resource Manager MBeans and register them in the related RM MBeanServer
        ObjectName rMNameAnonym = null;
        ObjectName rMNameAdmin = null;
        try {
            // Uniquely identify the MBeans and register them with the MBeanServer 
            rMNameAnonym = new ObjectName(RM_BEAN_NAME);
            rMNameAdmin = new ObjectName(RM_BEAN_NAME + "_" + JMXProperties.JMX_ADMIN);
            // Get the MBean Objects for the Resource Manager from the ResourceManager Frontend
            // Register the MBean Views for the Resource Manager
            mbsAnonym.registerMBean(RMMonitoringImpl.rMBeanAnonym, rMNameAnonym);
            mbsAdmin.registerMBean(RMMonitoringImpl.rMBeanAdmin, rMNameAdmin);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /**
     * method to create the MBeanServer Connectors for the Scheduler and to start them
     */
    public void createConnectors(AuthenticationImpl authentication) {
        PAAuthenticationConnectorServer rmiConnectorAnonym = new PAAuthenticationConnectorServer(
            getDefaultJmxConnectorUrl(), JMX_CONNECTOR_NAME, this.mbsAnonym, authentication, true, logger);
        PAAuthenticationConnectorServer rmiConnectorAdmin = new PAAuthenticationConnectorServer(
            getDefaultJmxConnectorUrl(), JMX_CONNECTOR_NAME + "_" + JMXProperties.JMX_ADMIN, this.mbsAdmin,
            authentication, false, logger);
        // Start the Connectors
        rmiConnectorAnonym.start();
        rmiConnectorAdmin.start();
    }

}
