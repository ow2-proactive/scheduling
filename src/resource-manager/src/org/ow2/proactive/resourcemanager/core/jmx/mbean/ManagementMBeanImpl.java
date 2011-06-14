/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

import java.net.URL;
import java.security.Policy;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.ow2.proactive.resourcemanager.core.account.RMAccountsManager;


/**
 * Implementation of the ManagementMBean interface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class ManagementMBeanImpl extends StandardMBean implements ManagementMBean {

    private final RMAccountsManager accountsManager;

    public ManagementMBeanImpl(final RMAccountsManager accountsManager) throws NotCompliantMBeanException {
        super(ManagementMBean.class);
        this.accountsManager = accountsManager;
    }

    public int getAccountingCacheValidityTimeInSeconds() {
        return this.accountsManager.getCacheValidityTimeInSeconds();
    }

    public void setAccountingCacheValidityTimeInSeconds(int refreshRateInSeconds) {
        this.accountsManager.setCacheValidityTimeInSeconds(refreshRateInSeconds);
    }

    public void setDefaultAccountingCacheValidityTimeInSeconds() {
        final int defaultValue = this.accountsManager.getDefaultCacheValidityTimeInSeconds();
        this.accountsManager.setCacheValidityTimeInSeconds(defaultValue);
    }

    public void clearAccoutingCache() {
        this.accountsManager.clearCache();
    }

    public long getLastRefreshDurationInMilliseconds() {
        return this.accountsManager.getLastRefreshDurationInMilliseconds();
    }

    public void refreshConfiguration() {
        try {
            // reloading permissions
            Policy.getPolicy().refresh();

            // reloading log4j configuration
            String configFilename = System.getProperty("log4j.configuration");
            if (configFilename != null) {
                LogManager.resetConfiguration();
                PropertyConfigurator.configure(new URL(configFilename));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}