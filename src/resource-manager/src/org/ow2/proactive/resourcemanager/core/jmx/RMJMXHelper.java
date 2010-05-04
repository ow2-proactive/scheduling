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
import org.ow2.proactive.jmx.AbstractJMXHelper;
import org.ow2.proactive.resourcemanager.core.account.RMAccountsManager;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.AllAccountsMBeanImpl;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.ManagementMBeanImpl;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.MyAccountMBeanImpl;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBeanImpl;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * This helper class represents the RMI and RO based JMX monitoring infrastructure of the Resource Manager.
 * @see org.ow2.proactive.jmx.AbstractJMXHelper
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public final class RMJMXHelper extends AbstractJMXHelper {

    private static final Logger LOGGER = ProActiveLogger.getLogger(RMLoggers.MONITORING);

    /** The name of the Resource Manager bean */
    public static final String RUNTIMEDATA_MBEAN_NAME = "ProActiveResourceManager:name=RuntimeDataMBean";
    public static final String MYACCOUNT_MBEAN_NAME = "ProActiveResourceManager:name=MyAccount";
    public static final String ALLACCOUNTS_MBEAN_NAME = "ProActiveResourceManager:name=AllAccounts";
    public static final String MANAGEMENT_MBEAN_NAME = "ProActiveResourceManager:name=Management";

    /** The single instance of this class */
    private static RMJMXHelper instance;

    /** The accounts manager */
    private final RMAccountsManager accountsManager;

    /**
     * Creates a new instance of this class.
     * @param accountsManager the accounts manager
     */
    public RMJMXHelper(final RMAccountsManager accountsManager) {
        super(RMJMXHelper.LOGGER);
        this.accountsManager = accountsManager;
    }

    /**
     * Returns the single instance of this class.
     *
     * @return the single instance of this class
     */
    public static RMJMXHelper getInstance() {
        if (RMJMXHelper.instance == null) {
            throw new IllegalStateException("Not ready, it seems that the RMCore is not instantiated yet");
        }
        return RMJMXHelper.instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerMBeans(final MBeanServer mbs) {
        // Register all mbeans into the server
        try {
            final RuntimeDataMBean anonymMBean = new RuntimeDataMBeanImpl(RMMonitoringImpl.rmStatistics);
            // Uniquely identify the MBean and register it to the MBeanServer
            final ObjectName name = new ObjectName(RUNTIMEDATA_MBEAN_NAME);
            mbs.registerMBean(anonymMBean, name);
        } catch (Exception e) {
            LOGGER.error("Unable to register the ResourceManagerRuntimeMBean", e);
        }

        // Register the MyAccount MBean into the MBean server
        try {
            final MyAccountMBeanImpl myAccountMBean = new MyAccountMBeanImpl(this.accountsManager);
            final ObjectName name = new ObjectName(MYACCOUNT_MBEAN_NAME);
            mbs.registerMBean(myAccountMBean, name);
        } catch (Exception e) {
            LOGGER.error("Unable to register the MyAccountMBean", e);
        }

        // Register the ViewAccount MBean into the MBean server
        try {
            final AllAccountsMBeanImpl viewAccountMBean = new AllAccountsMBeanImpl(this.accountsManager);
            final ObjectName name = new ObjectName(ALLACCOUNTS_MBEAN_NAME);
            mbs.registerMBean(viewAccountMBean, name);
        } catch (Exception e) {
            LOGGER.error("Unable to register the AllAccountsMBean", e);
        }

        // Register the Management MBean into the MBean server
        try {
            final ManagementMBeanImpl managementMBean = new ManagementMBeanImpl(this.accountsManager);
            final ObjectName name = new ObjectName(MANAGEMENT_MBEAN_NAME);
            mbs.registerMBean(managementMBean, name);
        } catch (Exception e) {
            LOGGER.error("Unable to register the ManagementMBean", e);
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