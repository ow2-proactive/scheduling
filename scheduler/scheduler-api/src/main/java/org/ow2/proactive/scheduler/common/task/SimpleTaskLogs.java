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

    // fix for #2456 : Credential Data and TaskLogs contain serialVersionUID based on scheduler server version
    private static final long serialVersionUID = 1L;

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

    @Override
    public String getAllLogs() {
        return getAllLogs(false);
    }

    @Override
    public String getStderrLogs() {
        return getStderrLogs(false);
    }

    @Override
    public String getStdoutLogs() {
        return getStdoutLogs(false);
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getAllLogs(boolean)
     */
    @Override
    public String getAllLogs(boolean timeStamp) {
        return this.standardLogs + this.errorlogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStderrLogs(boolean)
     */
    @Override
    public String getStderrLogs(boolean timeStamp) {
        return this.errorlogs;
    }

    /**
     * Timestamp parameter is not relevant for this TaskLogs implementation.
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStdoutLogs(boolean)
     */
    @Override
    public String getStdoutLogs(boolean timeStamp) {
        return this.standardLogs;
    }
}
