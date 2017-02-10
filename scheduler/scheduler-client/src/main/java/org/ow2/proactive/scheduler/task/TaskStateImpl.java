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
package org.ow2.proactive.scheduler.task;

import java.util.List;

import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class TaskStateImpl extends TaskState {

    private TaskInfo taskInfo;

    private int maxNumberOfExecutionOnFailure = PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE.getValueAsInt();

    private int iterationIndex;

    private int replicationIndex;

    @Override
    public void update(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    @Override
    public List<TaskState> getDependences() {
        return null;
    }

    @Override
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setMaxNumberOfExecutionOnFailure(int maxNumberOfExecutionOnFailure) {
        this.maxNumberOfExecutionOnFailure = maxNumberOfExecutionOnFailure;
    }

    @Override
    public int getMaxNumberOfExecutionOnFailure() {
        return maxNumberOfExecutionOnFailure;
    }

    @Override
    public TaskState replicate() throws Exception {
        TaskStateImpl impl = new TaskStateImpl();
        impl.update(taskInfo);
        impl.setName(name);
        impl.setDescription(getDescription());
        impl.setTag(getTag());
        impl.setIterationIndex(getIterationIndex());
        impl.setReplicationIndex(getReplicationIndex());
        impl.setMaxNumberOfExecution(getMaxNumberOfExecution());
        impl.setParallelEnvironment(getParallelEnvironment());
        impl.setNumberOfNeededNodes(getNumberOfNodesNeeded());
        return impl;
    }

    public void setIterationIndex(int iterationIndex) {
        this.iterationIndex = iterationIndex;
    }

    @Override
    public int getIterationIndex() {
        return iterationIndex;
    }

    public void setReplicationIndex(int replicationIndex) {
        this.replicationIndex = replicationIndex;
    }

    @Override
    public int getReplicationIndex() {
        return replicationIndex;
    }

}
