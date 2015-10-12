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
package org.ow2.proactive.scheduler.core.jmx.mbean;

/**
 * MBean interface for the management of the Scheduler.
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
