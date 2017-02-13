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

/**
 * MBean interface for the management of the Resource Manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public interface ManagementMBean {

    /**
     * Returns the accounting cache valid time in seconds.
     * @return refresh rate in seconds 
     */
    int getAccountingCacheValidityTimeInSeconds();

    /**
     * Sets a new accounting cache valid time in seconds.  
     * @param timeInSeconds the new cache refresh rate in seconds
     */
    void setAccountingCacheValidityTimeInSeconds(int timeInSeconds);

    /**
     * Sets the cache refresh rate to the one defined in the configuration file.
     */
    void setDefaultAccountingCacheValidityTimeInSeconds();

    /**
     * Clears the cache (all precomputed accounts)
     */
    void clearAccoutingCache();

    /**
     * Returns the last duration in milliseconds of account refresh.
     * @return the last refresh duration in milliseconds
     */
    long getLastRefreshDurationInMilliseconds();

    /**
     * Reload the resource manager configuration, permissions, and log4j config.
     */
    void refreshConfiguration();
}
