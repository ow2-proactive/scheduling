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
package org.ow2.proactive.scheduler.common.usage;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Task information for accounting / usage purpose.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
@PublicAPI
public class TaskUsage implements Serializable {

    private final String taskId;

    private final String taskName;

    private final long taskStartTime;

    private final long taskFinishedTime;

    private final long taskExecutionDuration;

    private final int taskNodeNumber;

    public TaskUsage(String taskId, String taskName, long taskStartTime, long taskFinishedTime,
            long taskExecutionDuration, int taskNodeNumber) {

        this.taskId = taskId;
        this.taskName = taskName;
        this.taskStartTime = taskStartTime;
        this.taskFinishedTime = taskFinishedTime;
        this.taskExecutionDuration = taskExecutionDuration;
        this.taskNodeNumber = taskNodeNumber;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }

    public long getTaskFinishedTime() {
        return taskFinishedTime;
    }

    public long getTaskExecutionDuration() {
        return taskExecutionDuration;
    }

    public int getTaskNodeNumber() {
        return taskNodeNumber;
    }
}
