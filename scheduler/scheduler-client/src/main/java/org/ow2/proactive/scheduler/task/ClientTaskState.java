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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * 
 * This class is a client view of a {@link TaskState}. A client may access
 * instances of this class when connecting to the scheduler front-end and ask
 * for a JobState (for instance by using {@link Scheduler#getJobState(String)}).
 * 
 * The value of some attributes will not be available in this view of the
 * TaskState. Therefore, calling the respective getters will throw a
 * RuntimeException. See the public method's javadoc for more details.
 * 
 * @author esalagea
 * 
 */
public final class ClientTaskState extends TaskState {

    private TaskInfo taskInfo;

    private int maxNumberOfExecutionOnFailure;

    private List<TaskId> dependenceIds = new ArrayList<>();

    transient private List<TaskState> dependences = new ArrayList<>();

    private int maxNumberOfExecution;

    public ClientTaskState(TaskState taskState) {
        // copy information from the TaskStae passed as an argument
        taskInfo = taskState.getTaskInfo();
        maxNumberOfExecutionOnFailure = taskState.getMaxNumberOfExecutionOnFailure();
        this.setName(taskState.getName());

        this.setDescription(taskState.getDescription());
        this.setTag(taskState.getTag());
        this.setRunAsMe(taskState.isRunAsMe());

        this.setWallTime(taskState.getWallTime());
        this.setPreciousResult(taskState.isPreciousResult());
        this.setPreciousLogs(taskState.isPreciousLogs());
        this.setRunAsMe(taskState.isRunAsMe());

        this.maxNumberOfExecution = taskState.getMaxNumberOfExecution();

        this.setParallelEnvironment(taskState.getParallelEnvironment());
        this.setGenericInformation(taskState.getGenericInformation());

        this.setOnTaskError(taskState.getOnTaskErrorProperty().getValue());

        // Store only task IDs here; #restoreDependences is later called by
        // ClientJobState in order for this instance to store references to the
        // same ClientTaskState instances as the ones held in the
        // ClientJobState#tasks field.
        if (taskState.getDependences() != null) {
            for (TaskState dep : taskState.getDependences()) {
                dependenceIds.add(dep.getId());
            }
        }
    }

    @Override
    public void update(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    @Override
    public List<TaskState> getDependences() {
        return dependences;
    }

    @Override
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
    public int getMaxNumberOfExecutionOnFailure() {
        return maxNumberOfExecutionOnFailure;
    }

    @Override
    public TaskState replicate() throws Exception {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public int getIterationIndex() {
        return taskInfo.getTaskId().getIterationIndex();
    }

    @Override
    public int getReplicationIndex() {
        return taskInfo.getTaskId().getReplicationIndex();
    }

    public void restoreDependences(Map<TaskId, TaskState> tasksMap) {
        dependences.clear();
        for (TaskId id : dependenceIds) {
            dependences.add(tasksMap.get(id));
        }
    }

    @Override
    public int getMaxNumberOfExecution() {
        return this.maxNumberOfExecution;
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public List<InputSelector> getInputFilesList() {
        throw new RuntimeException("Not implemented: the input files atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public List<OutputSelector> getOutputFilesList() {
        throw new RuntimeException("Not implemented: the output files atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public Script<?> getPreScript() {
        throw new RuntimeException("Not implemented: the PreScript atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public Script<?> getPostScript() {
        throw new RuntimeException("Not implemented: the PostScript atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public List<SelectionScript> getSelectionScripts() {
        throw new RuntimeException("Not implemented: the SelectionScript atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public FlowScript getFlowScript() {
        throw new RuntimeException("Not implemented: the FlowScript atttribute is not available on client side.");
    }

    /**
     * This property is not available for this implementation. Calling this
     * method will throw a RuntimeException
     */
    @Override
    public RestartMode getRestartTaskOnError() {
        throw new RuntimeException("Not implemented: the restart task on error property is not available on client side.");
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.dependences = new ArrayList<>();
    }
}
