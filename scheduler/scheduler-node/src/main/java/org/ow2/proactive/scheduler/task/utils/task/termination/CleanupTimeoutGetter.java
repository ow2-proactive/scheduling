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
                System.setProperty(RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME_PROACTIVE_PROGRAMMING,
                                   cleanupTimeString);
                return Long.parseLong(cleanupTimeString);
            } else {
                writeDefaultPropertyUsedDebugMessageIfEnabled();
                return CLEANUP_TIME_DEFAULT_SECONDS;
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("proactive.task.cleanup.time: " +
                        System.getProperty(RMNodeStarter.SECONDS_TASK_CLEANUP_TIMEOUT_PROP_NAME) +
                        " is not parsable to long, fallback to default value. Error : " + e.getMessage());
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
