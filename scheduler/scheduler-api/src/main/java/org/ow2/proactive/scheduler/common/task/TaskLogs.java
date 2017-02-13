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
package org.ow2.proactive.scheduler.common.task;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This interface must be implemented by log storage for scheduler jobs.
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface TaskLogs extends java.io.Serializable {

    /**
     * Return the logs generated on standard output. No timestamp
     *
     * @return a String containing the logs generated on standard output.
     */
    String getStdoutLogs();

    /**
     * Return the logs generated on error output. No timestamp
     *
     * @return a String containing the logs generated on error output.
     */
    String getStderrLogs();

    /**
     * Return all the logs generated on standard and error output. No timestamp
     *
     * @return a String containing stored logs, or null if any.
     */
    String getAllLogs();

    /**
     * Return the logs generated on standard output.
     * @param timeStamp get the logs with or without time tags.
     * 
     * @return a String containing the logs generated on standard output.
     */
    String getStdoutLogs(boolean timeStamp);

    /**
     * Return the logs generated on error output.
     * @param timeStamp get the logs with or without time tags.
     * 
     * @return a String containing the logs generated on error output.
     */
    String getStderrLogs(boolean timeStamp);

    /**
     * Return all the logs generated on standard and error output.
     * @param timeStamp get the logs with or without time tags.
     * 
     * @return a String containing stored logs, or null if any.
     */
    String getAllLogs(boolean timeStamp);
}
