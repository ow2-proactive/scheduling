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
