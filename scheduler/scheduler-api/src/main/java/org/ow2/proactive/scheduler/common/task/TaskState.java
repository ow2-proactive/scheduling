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

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.task.SchedulerVars;


/**
 * This class contains all informations about the state of the task.
 * It also provides methods and static fields to order the tasks if you hold them in a list.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TaskState extends Task implements Comparable<TaskState> {

    /** Sorting constant, this will allow the user to sort the descriptor. */
    public static final int SORT_BY_ID = 1;

    public static final int SORT_BY_NAME = 2;

    public static final int SORT_BY_STATUS = 3;

    public static final int SORT_BY_DESCRIPTION = 4;

    public static final int SORT_BY_EXECUTIONLEFT = 5;

    public static final int SORT_BY_EXECUTIONONFAILURELEFT = 6;

    public static final int SORT_BY_STARTED_TIME = 8;

    public static final int SORT_BY_FINISHED_TIME = 9;

    public static final int SORT_BY_HOST_NAME = 10;

    public static final int SORT_BY_EXEC_DURATION = 11;

    public static final int ASC_ORDER = 1;

    public static final int DESC_ORDER = 2;

    protected static int currentSort = SORT_BY_ID;

    protected static int currentOrder = ASC_ORDER;

    public static final Comparator<TaskState> COMPARE_BY_FINISHED_TIME_ASC = new Comparator<TaskState>() {
        @Override
        public int compare(TaskState task1, TaskState task2) {
            return Long.compare(task1.getFinishedTime(), task2.getFinishedTime());
        }
    };

    /** ProActive default constructor */
    public TaskState() {
    }

    /**
     * To update this taskState using a taskInfo
     *
     * @param taskInfo the taskInfo to set
     */
    public abstract void update(TaskInfo taskInfo);

    /**
     * To get the dependences of this task.
     * Return null if this task has no dependence.
     *
     * @return the dependences of this task
     */
    @XmlTransient
    public abstract List<TaskState> getDependences();

    /**
     * If the Task was submitted, a call to this method will throw a
     * RuntimeException. The Dependence list should then
     * be accessed through {@link #getDependences()}
     *
     * @return the the list of dependences of the task, or null if this task
     *      has been submitted
     * @throws IllegalStateException if this task was already submitted to the scheduler
     */
    @Override
    @XmlTransient
    public List<Task> getDependencesList() {
        throw new IllegalStateException("This method cannot be used on a submitted task;" + "use " +
                                        this.getClass().getCanonicalName() + "#getDependences()");
    }

    /**
     * If the Task was submitted, a call to this method will throw a
     * RuntimeException. Dependences cannot be added to a task at runtime.
     *
     * @param task the parent task to add to this task.
     * @throws IllegalStateException if this task was already submitted to the scheduler
     */
    @Override
    public void addDependence(Task task) {
        throw new IllegalStateException("This method cannot be used on a submitted task");
    }

    /**
     * If the Task was submitted, a call to this method will throw a
     * RuntimeException. Dependences cannot be added to a task at runtime.
     *
     * @param tasks the parent tasks to add to this task.
     * @throws IllegalStateException if this task was already submitted to the scheduler
     */
    @Override
    public void addDependences(List<Task> tasks) {
        throw new IllegalStateException("This method cannot be used on a submitted task");
    }

    /**
     * Set the field to sort on.
     *
     * @param sortBy the field on which the sort will be made.
     */
    public static void setSortingBy(int sortBy) {
        currentSort = sortBy;
    }

    /**
     * Set the order for the next sort.
     *
     * @param order ASC_ORDER or DESC_ORDER
     */
    public static void setSortingOrder(int order) {
        if ((order == ASC_ORDER) || (order == DESC_ORDER)) {
            currentOrder = order;
        } else {
            currentOrder = ASC_ORDER;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TaskState task) {
        switch (currentSort) {
            case SORT_BY_DESCRIPTION:
                return (currentOrder == ASC_ORDER) ? (description.compareTo(task.description))
                                                   : (task.description.compareTo(description));
            case SORT_BY_NAME:
                return (currentOrder == ASC_ORDER) ? (name.compareTo(task.name)) : (task.name.compareTo(name));
            case SORT_BY_STATUS:
                return (currentOrder == ASC_ORDER) ? (getStatus().compareTo(task.getStatus()))
                                                   : (task.getStatus().compareTo(getStatus()));
            case SORT_BY_STARTED_TIME:
                return (currentOrder == ASC_ORDER) ? ((int) (getStartTime() - task.getStartTime()))
                                                   : ((int) (task.getStartTime() - getStartTime()));
            case SORT_BY_FINISHED_TIME:
                return (currentOrder == ASC_ORDER) ? ((int) (getFinishedTime() - task.getFinishedTime()))
                                                   : ((int) (task.getFinishedTime() - getFinishedTime()));
            case SORT_BY_EXECUTIONLEFT:
                return (currentOrder == ASC_ORDER) ? (Integer.valueOf(getNumberOfExecutionLeft())
                                                             .compareTo(Integer.valueOf(task.getNumberOfExecutionLeft())))
                                                   : (Integer.valueOf(task.getNumberOfExecutionLeft())
                                                             .compareTo(Integer.valueOf(getNumberOfExecutionLeft())));
            case SORT_BY_EXECUTIONONFAILURELEFT:
                return (currentOrder == ASC_ORDER) ? (Integer.valueOf(getNumberOfExecutionOnFailureLeft())
                                                             .compareTo(Integer.valueOf(task.getNumberOfExecutionOnFailureLeft())))
                                                   : (Integer.valueOf(task.getNumberOfExecutionOnFailureLeft())
                                                             .compareTo(Integer.valueOf(getNumberOfExecutionOnFailureLeft())));
            case SORT_BY_HOST_NAME:
                return (currentOrder == ASC_ORDER) ? (getExecutionHostName().compareTo(task.getExecutionHostName()))
                                                   : (task.getExecutionHostName().compareTo(getExecutionHostName()));
            case SORT_BY_EXEC_DURATION:
                return (currentOrder == ASC_ORDER) ? (int) (getExecutionDuration() - task.getExecutionDuration())
                                                   : (int) (task.getExecutionDuration() - getExecutionDuration());
            default:
                return (currentOrder == ASC_ORDER) ? (getId().compareTo(task.getId()))
                                                   : (task.getId().compareTo(getId()));
        }
    }

    /**
     * To get the taskInfo
     *
     * @return the taskInfo
     */
    public abstract TaskInfo getTaskInfo();

    /**
     * Returns true if the task is not alive any more (a task is alive when it's waiting to be executed or being executed
     * @return true if the task is alive
     */
    public boolean isTaskAlive() {
        return getTaskInfo().getStatus().isTaskAlive();
    }

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
     */
    public long getFinishedTime() {
        return getTaskInfo().getFinishedTime();
    }

    /**
     * To get the jobID
     *
     * @return the jobID
     */
    public JobId getJobId() {
        return getTaskInfo().getJobId();
    }

    /**
     * To get the startTime
     *
     * @return the startTime
     */
    public long getStartTime() {
        return getTaskInfo().getStartTime();
    }

    /**
     * To get the scheduledTime
     *
     * @return the scheduledTime
     */
    public long getScheduledTime() {
        return getTaskInfo().getScheduledTime();
    }

    /**
     * To get the taskId
     *
     * @return the taskID
     */
    public TaskId getId() {
        return getTaskInfo().getTaskId();
    }

    /**
     * To get the status of this task
     *
     * @return the status of this task
     */
    public TaskStatus getStatus() {
        return getTaskInfo().getStatus();
    }

    /*
     * <B>This method always returns 0 in Scheduling 2.2.0.</B>
     */
    /**
     * Return the latest progress value for this task.
     * Progress value is ranged between 0 and 100.
     *
     * @return the latest progress value for this task.
     */
    public int getProgress() {
        return this.getTaskInfo().getProgress();
    }

    /**
     * Get the last execution HostName of the task.
     *
     * @return the last execution HostName.
     */
    public String getExecutionHostName() {
        return getTaskInfo().getExecutionHostName();
    }

    /**
     * To get the list of execution hosts name.
     * The first element of the returned array is the most recent used host.
     *
     * @return the execution Host Name list.
     */
    public String[] getExecutionHostNameList() {
        return getTaskInfo().getExecutionHostNameList();
    }

    /**
     * Get the number of execution left.
     *
     * @return the number of execution left.
     */
    public int getNumberOfExecutionLeft() {
        return getTaskInfo().getNumberOfExecutionLeft();
    }

    /**
     * Get the numberOfExecutionOnFailureLeft value.
     *
     * @return the numberOfExecutionOnFailureLeft value.
     */
    public int getNumberOfExecutionOnFailureLeft() {
        return getTaskInfo().getNumberOfExecutionOnFailureLeft();
    }

    /**
     * Get the real task duration in millis. It is the CPU time used by the task not including
     * communication and initialization.
     * It also include time spent in Pre and Post scripts.
     *
     * @return the real task duration in millis
     */
    public long getExecutionDuration() {
        return getTaskInfo().getExecutionDuration();
    }

    /**
     * Get the number of execution on failure allowed by the task.
     *
     * @return the number of execution on failure allowed by the task
     */
    public abstract int getMaxNumberOfExecutionOnFailure();

    /**
     * @see org.ow2.proactive.scheduler.common.task.Task#getName()
     */
    @Override
    public String getName() {
        if (getId() == null || getId().getReadableName().equals(SchedulerConstants.TASK_DEFAULT_NAME)) {
            return super.getName();
        } else {
            return getId().getReadableName();
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getId() + ")";
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (TaskState.class.isAssignableFrom(obj.getClass())) {
            return ((TaskState) obj).getId().equals(getId());
        }

        return false;
    }

    /**
     * Replicates a task
     * <p>
     * Deep copies all fields, does not share any reference
     * 
     * @return the newly created task, cast as a TaskState
     * @throws Exception
     */
    public abstract TaskState replicate() throws Exception;

    /**
     * When Control Flow actions are performed (see {@link #getFlowScript()}),
     * some tasks are replicated. 
     * A task replicated by a {@link FlowActionType#LOOP} action
     * is differentiated from the original by an incremented Iteration Index.
     * 
     * @return the iteration number of this task if it was replicated by a LOOP flow operation ({@code >= 0})
     */
    public abstract int getIterationIndex();

    /**
     * When Control Flow actions are performed (see {@link #getFlowScript()}),
     * some tasks are replicated. 
     * A task replicated by a {@link FlowActionType#REPLICATE} action
     * is differentiated from the original by an incremented Replication Index.
     
     * @return the replication number of this task if it was replicated by a REPLICATE flow operations ({@code >= 0})
     */
    public abstract int getReplicationIndex();

    /**
     * Returns a map containing all scope variables defined in this Task (i.e. all task variables not inherited)
     *
     * @return map of variables
     */
    public Map<String, Serializable> getScopeVariables() {
        Map<String, Serializable> scopeVariables = new HashMap<>();
        Map<String, String> jobVariables = getTaskInfo().getJobInfo().getVariables();
        for (TaskVariable variable : variables.values()) {
            if (!variable.isJobInherited() ||
                (variable.isJobInherited() &&
                 (jobVariables == null || !jobVariables.containsKey(variable.getName())))) {
                scopeVariables.put(variable.getName(), variable.getValue());
            }
        }
        return scopeVariables;
    }

    /**
     * Returns a map containing all variables defined by the system for this task, such as PA_JOB_ID, PA_USER, etc.
     * @return map of variables
     */
    public Map<String, Serializable> getSystemVariables() {
        Map<String, Serializable> systemVariables = new HashMap<>();
        if (getTaskInfo() != null) {
            systemVariables.put(SchedulerVars.PA_JOB_ID.toString(), getTaskInfo().getJobId().value());
            systemVariables.put(SchedulerVars.PA_JOB_NAME.toString(), getTaskInfo().getJobId().getReadableName());
            if (getId() != null) {
                systemVariables.put(SchedulerVars.PA_TASK_ID.toString(), getId().toString());
                systemVariables.put(SchedulerVars.PA_TASK_NAME.toString(), getName());
            }
            systemVariables.put(SchedulerVars.PA_USER.toString(), getTaskInfo().getJobInfo().getJobOwner());

            systemVariables.put(SchedulerVars.PA_TASK_ITERATION.toString(), getIterationIndex());
            systemVariables.put(SchedulerVars.PA_TASK_REPLICATION.toString(), getReplicationIndex());
        }
        return systemVariables;
    }

    /**
     * Returns a map containing both scope variables and system variables
     * @return map of variables
     */
    public Map<String, Serializable> getRuntimeVariables() {
        Map<String, Serializable> runtimeVariables = new HashMap<>();
        runtimeVariables.putAll(getScopeVariables());
        runtimeVariables.putAll(getSystemVariables());
        return runtimeVariables;
    }

    /**
     * Returns a map of generic information, with replacement done from the runtime variables map
     */
    public Map<String, String> getRuntimeGenericInformation() {

        if (getTaskInfo() == null) {
            // task is not yet properly initialized
            return new HashMap<>();
        }

        HashMap<String, String> gInfo = new HashMap<>();

        if (genericInformation != null) {
            Map<String, String> updatedTaskGenericInfo = applyReplacementsOnGenericInformation(genericInformation,
                                                                                               getRuntimeVariables());
            gInfo.putAll(updatedTaskGenericInfo);
        }

        return gInfo;
    }
}
