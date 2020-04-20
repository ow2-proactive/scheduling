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
package org.ow2.proactive.resourcemanager.core.jmx;

import java.io.File;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.jmx.AbstractJMXHelper;
import org.ow2.proactive.jmx.RRDDataStore;
import org.ow2.proactive.resourcemanager.core.account.RMAccountsManager;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.AllAccountsMBeanImpl;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.ManagementMBeanImpl;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.MyAccountMBeanImpl;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBeanImpl;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;


/**
 * This helper class represents the RMI and RO based JMX monitoring infrastructure of the Resource Manager.
 * @see org.ow2.proactive.jmx.AbstractJMXHelper
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public final class RMJMXHelper extends AbstractJMXHelper {

    private static final Logger LOGGER = Logger.getLogger(RMJMXHelper.class);

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
        RMJMXHelper.instance = this;
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
            final ObjectName name = new ObjectName(RMJMXBeans.RUNTIMEDATA_MBEAN_NAME);
            mbs.registerMBean(anonymMBean, name);

            String dataBaseName = PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_RRD_DATABASE_NAME.getValueAsString());
            FileUtils.forceMkdir(new File(dataBaseName).getParentFile());

            setDataStore(new RRDDataStore((StandardMBean) anonymMBean,
                                          dataBaseName,
                                          PAResourceManagerProperties.RM_RRD_STEP.getValueAsInt(),
                                          Logger.getLogger(RMJMXHelper.class)));
        } catch (Exception e) {
            LOGGER.error("Unable to register the ResourceManagerRuntimeMBean", e);
        }

        // Register the MyAccount MBean into the MBean server
        try {
            final MyAccountMBeanImpl myAccountMBean = new MyAccountMBeanImpl(this.accountsManager);
            final ObjectName name = new ObjectName(RMJMXBeans.MYACCOUNT_MBEAN_NAME);
            mbs.registerMBean(myAccountMBean, name);
        } catch (Exception e) {
            LOGGER.error("Unable to register the MyAccountMBean", e);
        }

        // Register the ViewAccount MBean into the MBean server
        try {
            final AllAccountsMBeanImpl viewAccountMBean = new AllAccountsMBeanImpl(this.accountsManager);
            final ObjectName name = new ObjectName(RMJMXBeans.ALLACCOUNTS_MBEAN_NAME);
            mbs.registerMBean(viewAccountMBean, name);
        } catch (Exception e) {
            LOGGER.error("Unable to register the AllAccountsMBean", e);
        }

        // Register the Management MBean into the MBean server
        try {
            final ManagementMBeanImpl managementMBean = new ManagementMBeanImpl(this.accountsManager);
            final ObjectName name = new ObjectName(RMJMXBeans.MANAGEMENT_MBEAN_NAME);
            mbs.registerMBean(managementMBean, name);
        } catch (Exception e) {
            LOGGER.error("Unable to register the ManagementMBean", e);
        }
    }

    /**
     * Return a list of registered MBeanServers. 
     *
     * @param agentId the agentId of the MBeanServer to find.
     */
    public static ArrayList<MBeanServer> findMBeanServer(String agentId) {
        return MBeanServerFactory.findMBeanServer(agentId);
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
