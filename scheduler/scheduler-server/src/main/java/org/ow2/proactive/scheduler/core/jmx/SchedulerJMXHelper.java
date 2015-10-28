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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.jmx;

import java.io.File;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.ow2.proactive.jmx.AbstractJMXHelper;
import org.ow2.proactive.jmx.RRDDataStore;
import org.ow2.proactive.scheduler.core.account.SchedulerAccountsManager;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.jmx.mbean.AllAccountsMBeanImpl;
import org.ow2.proactive.scheduler.core.jmx.mbean.ManagementMBeanImpl;
import org.ow2.proactive.scheduler.core.jmx.mbean.MyAccountMBeanImpl;
import org.ow2.proactive.scheduler.core.jmx.mbean.RuntimeDataMBeanImpl;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


/**
 * This helper class represents the RMI and RO based JMX monitoring infrastructure of the Scheduler.
 * @see org.ow2.proactive.jmx.AbstractJMXHelper
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SchedulerJMXHelper extends AbstractJMXHelper {
    private static final Logger LOGGER = Logger.getLogger(SchedulerJMXHelper.class);

    public static final String RUNTIMEDATA_MBEAN_NAME = "ProActiveScheduler:name=RuntimeData";
    public static final String MYACCOUNT_MBEAN_NAME = "ProActiveScheduler:name=MyAccount";
    public static final String ALLACCOUNTS_MBEAN_NAME = "ProActiveScheduler:name=AllAccounts";
    public static final String MANAGEMENT_MBEAN_NAME = "ProActiveScheduler:name=Management";

    /** The single instance of this class */
    private static SchedulerJMXHelper instance;

    /** The accounts manager used by the MBeans */
    private final SchedulerAccountsManager accountsManager;

    /** The Scheduler Runtime MBean */
    private RuntimeDataMBeanImpl schedulerRuntimeMBean;

    private final SchedulerDBManager dbManager;

    /**
     * Creates a new instance of this class.
     * @param accountsManager the accounts manager
     */
    public SchedulerJMXHelper(SchedulerAccountsManager accountsManager, SchedulerDBManager dbManager) {
        super(LOGGER);
        this.accountsManager = accountsManager;
        this.dbManager = dbManager;
        SchedulerJMXHelper.instance = this;
    }

    /**
     * Returns the single instance of this class.
     *
     * @return the single instance of this class
     */
    public static SchedulerJMXHelper getInstance() {
        if (SchedulerJMXHelper.instance == null) {
            throw new IllegalStateException(
                "Not ready, it seems that the SchedulerFrontend is not instantiated yet");
        }
        return SchedulerJMXHelper.instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerMBeans(final MBeanServer mbs) {
        // Register the Scheduler runtime MBean into the MBean server
        try {
            this.schedulerRuntimeMBean = new RuntimeDataMBeanImpl(dbManager);
            final ObjectName name = new ObjectName(RUNTIMEDATA_MBEAN_NAME);
            mbs.registerMBean(this.schedulerRuntimeMBean, name);

            String dataBaseName = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
                System.getProperty("file.separator") +
                PASchedulerProperties.SCHEDULER_RRD_DATABASE_NAME.getValueAsString();

            FileUtils.forceMkdir(new File(dataBaseName).getParentFile());
            if (PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getValueAsBoolean()) {
                // dropping the RDD data base
                File rrdDataBase = new File(dataBaseName);
                if (rrdDataBase.exists()) {
                    rrdDataBase.delete();
                }
            }

            setDataStore(new RRDDataStore((StandardMBean) schedulerRuntimeMBean, dataBaseName,
                PASchedulerProperties.SCHEDULER_RRD_STEP.getValueAsInt(), Logger
                        .getLogger(SchedulerJMXHelper.class)));

        } catch (Exception e) {
            LOGGER.error("Unable to register the RuntimeDataMBean", e);
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
        return PASchedulerProperties.SCHEDULER_JMX_CONNECTOR_NAME.getValueAsString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMXRMIConnectorServerPort() {
        return PASchedulerProperties.SCHEDULER_JMX_PORT.getValueAsInt();
    }

    public RuntimeDataMBeanImpl getSchedulerRuntimeMBean() {
        return this.schedulerRuntimeMBean;
    }
}
