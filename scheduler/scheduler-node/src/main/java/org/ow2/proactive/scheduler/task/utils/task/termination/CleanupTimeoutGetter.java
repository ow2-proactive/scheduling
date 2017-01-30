/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2017 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 *  * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.utils.task.termination;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;

public class CleanupTimeoutGetter {

    private static final Logger LOGGER = Logger.getLogger(CleanupTimeoutGetter.class);

    private static final long CLEANUP_TIME_DEFAULT_SECONDS = 10;

    public long getCleanupTimeSeconds() {
        try {
            String cleanupTimeString = System.getProperty(RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME);
            writePropertyDebugMessageIfEnabled(cleanupTimeString);
            if (cleanupTimeString != null) {
                System.setProperty(RMNodeStarter.
                                SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME_PROACTIVE_PROGRAMMING,
                        cleanupTimeString);
                return Long.parseLong(cleanupTimeString);
            } else {
                writeDefaultPropertyUsedDebugMessageIfEnabled();
                return CLEANUP_TIME_DEFAULT_SECONDS;
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("proactive.task.cleanup.time: "
                    + System.getProperty(RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME)
                    + " is not parsable to long, fallback to default value. Error : "
                    + e.getMessage());
            return CLEANUP_TIME_DEFAULT_SECONDS;
        }
    }

    private void writeDefaultPropertyUsedDebugMessageIfEnabled() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cleanup timeout default value used: " + CLEANUP_TIME_DEFAULT_SECONDS);
        }
    }

    private void writePropertyDebugMessageIfEnabled(String cleanupTimeString) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cleanup timeout retrieved from property: " + cleanupTimeString);
        }
    }
}
