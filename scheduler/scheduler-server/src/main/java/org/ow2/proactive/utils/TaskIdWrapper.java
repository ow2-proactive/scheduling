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
package org.ow2.proactive.utils;

import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * TaskId wrapper mainly used to return an implementation that
 * takes into account job id + task id for hashcode ans equals.
 */
public final class TaskIdWrapper {

    // TODO: this class could probably be removed once next issue is fixed
    // https://github.com/ow2-proactive/scheduling/issues/2306

    private final TaskId taskId;

    private TaskIdWrapper(TaskId taskId) {
        this.taskId = taskId;
    }

    public static TaskIdWrapper wrap(TaskId taskId) {
        return new TaskIdWrapper(taskId);
    }

    public TaskId getTaskId() {
        return taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TaskIdWrapper that = (TaskIdWrapper) o;

        if (taskId.longValue() != that.taskId.longValue())
            return false;
        return taskId.getJobId().equals(that.taskId.getJobId());

    }

    @Override
    public int hashCode() {
        int result = taskId.getJobId().hashCode();
        result = 31 * result + (int) (taskId.longValue() ^ (taskId.longValue() >>> 32));
        return result;
    }

}
