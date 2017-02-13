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
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

import java.net.MalformedURLException;
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
        // reloading permissions
        Policy.getPolicy().refresh();

        // reloading log4j configuration
        String configFilename = System.getProperty("log4j.configuration");
        if (configFilename != null) {
            LogManager.resetConfiguration();
            try {
                PropertyConfigurator.configure(new URL(configFilename));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
