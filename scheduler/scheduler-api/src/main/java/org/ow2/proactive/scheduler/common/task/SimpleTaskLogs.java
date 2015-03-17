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
package org.ow2.proactive.scheduler.common.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A simple String based implementation of TaskLogs.
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleTaskLogs implements TaskLogs {

    private static final long serialVersionUID = 61L;

    // logs on standard output
    private String standardLogs;

    // logs on error output
    private String errorlogs;

    /**
     * Create a new SimpleTaskLogs.
     * @param stdLogs the standard output.
     * @param errLogs the error output.
     */
    public SimpleTaskLogs(String stdLogs, String errLogs) {
        this.standardLogs = stdLogs;
        this.errorlogs = errLogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getAllLogs(boolean)
     */
    public String getAllLogs(boolean timeStamp) {
        return this.standardLogs + this.errorlogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStderrLogs(boolean)
     */
    public String getStderrLogs(boolean timeStamp) {
        return this.errorlogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStdoutLogs(boolean)
     */
    public String getStdoutLogs(boolean timeStamp) {
        return this.standardLogs;
    }
}
