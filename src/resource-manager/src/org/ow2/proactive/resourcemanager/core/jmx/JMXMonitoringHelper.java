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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.jmx.AbstractJMXMonitoringHelper;
import org.ow2.proactive.jmx.naming.JMXProperties;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBeanImpl;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBeanImpl;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * This helper class represents the RMI and RO based JMX monitoring infrastructure of the Resource Manager.
 * @see org.ow2.proactive.jmx.AbstractJMXMonitoringHelper
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public final class JMXMonitoringHelper extends AbstractJMXMonitoringHelper {

    private static final Logger LOGGER = ProActiveLogger.getLogger(RMLoggers.MONITORING);

    private static final JMXMonitoringHelper instance = new JMXMonitoringHelper();

    /** The name of the Resource Manager bean */
    public static final String RM_BEAN_NAME = "RMFrontend:name=RMBean";

    private JMXMonitoringHelper() {
        super(JMXMonitoringHelper.LOGGER);
    }

    /**
     * Returns the single instance of this class.
     * 
     * @return the single instance of this class
     */
    public static JMXMonitoringHelper getInstance() {
        return JMXMonitoringHelper.instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectName registerAnonymMBean(final MBeanServer mbs) {
        // Register the anonym MBean into the MBean server      
        try {
            final RMAnonymMBean anonymMBean = new RMAnonymMBeanImpl(RMMonitoringImpl.rmStatistics);
            // Uniquely identify the MBean and register it to the MBeanServer
            final ObjectName anonymMBeanName = new ObjectName(RM_BEAN_NAME);
            mbs.registerMBean(anonymMBean, anonymMBeanName);
            return anonymMBeanName;
        } catch (Exception e) {
            LOGGER.error("Unable to register the RMAnonymMBean", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectName registerAdminMBean(final MBeanServer mbs) {
        // Register the anonym MBean into the MBean server
        try {
            final RMAdminMBean adminMBean = new RMAdminMBeanImpl(RMMonitoringImpl.rmStatistics);
            // Uniquely identify the MBean and register it to the MBeanServer
            final ObjectName adminMBeanName = new ObjectName(RM_BEAN_NAME + "_" + JMXProperties.JMX_ADMIN);
            mbs.registerMBean(adminMBean, adminMBeanName);
            return adminMBeanName;
        } catch (Exception e) {
            LOGGER.error("Unable to register the RMAdminMBean", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectorServerName() {
        return PAResourceManagerProperties.RM_JMX_CONNECTOR_NAME.getValueAsString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMXRMIConnectorServerPort() {
        return PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt();
    }
}