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
package org.ow2.proactive.scheduler.rest.data;

import static org.ow2.proactive.scheduler.rest.data.DataUtility.taskInfo;
import static org.ow2.proactive.scheduler.task.TaskIdImpl.createTaskId;

import java.util.List;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;


public class TaskStateImpl extends TaskState {
    private static final long serialVersionUID = 1L;

    private TaskStateData d;

    public TaskStateImpl(TaskStateData d) {
        this.d = d;
    }

    @Override
    public TaskId getId() {
        TaskIdData idData = d.getTaskInfo().getTaskId();
        return createTaskId(null, idData.getReadableName(), idData.getId());
    }

    @Override
    public List<TaskState> getDependences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIterationIndex() {
        return d.getIterationIndex();
    }

    @Override
    public int getMaxNumberOfExecutionOnFailure() {
        return d.getMaxNumberOfExecutionOnFailure();
    }

    @Override
    public int getReplicationIndex() {
        return d.getReplicationIndex();
    }

    @Override
    public TaskInfo getTaskInfo() {
        return taskInfo(d.getTaskInfo());
    }

    @Override
    public TaskState replicate() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(TaskInfo arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getName() {
        return d.getName();
    }

    @Override
    public String getDescription() {
        return d.getDescription();
    }

}
