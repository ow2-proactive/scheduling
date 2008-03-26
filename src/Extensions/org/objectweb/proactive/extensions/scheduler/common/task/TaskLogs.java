/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.scheduler.common.task;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This interface must be implemented by log storage for scheduler jobs.
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 */
@PublicAPI
public interface TaskLogs extends java.io.Serializable {

    /**
     * Return the logs generated on standard output.
     * @return a String containing the logs generated on standard output.
     */
    public String getStdoutLogs(boolean timeStamp);

    /**
     * Return the logs generated on error output.
     * @return a String containing the logs generated on error output.
     */
    public String getStderrLogs(boolean timeStamp);

    /**
     * Return all the logs generated on standard and error output.
     * @return a String containing stored logs, or null if any.
     */
    public String getAllLogs(boolean timeStamp);
}
