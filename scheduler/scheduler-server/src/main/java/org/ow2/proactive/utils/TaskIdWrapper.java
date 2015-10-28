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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskIdWrapper that = (TaskIdWrapper) o;

        if (taskId.longValue() != that.taskId.longValue()) return false;
        return taskId.getJobId().equals(that.taskId.getJobId());

    }

    @Override
    public int hashCode() {
        int result = taskId.getJobId().hashCode();
        result = 31 * result + (int) (taskId.longValue() ^ (taskId.longValue() >>> 32));
        return result;
    }

}