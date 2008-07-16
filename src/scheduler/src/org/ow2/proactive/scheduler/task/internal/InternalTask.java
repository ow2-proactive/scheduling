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
package org.ow2.proactive.scheduler.task.internal;

import java.util.ArrayList;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskLauncher;


/**
 * Internal and global description of a task.
 * This class contains all informations about the task to launch.
 * It also provides a method to create its own launcher.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 */
public abstract class InternalTask extends Task implements Comparable<InternalTask> {

    /** Sorting constant, this will allow the user to sort the descriptor. */
    public static final int SORT_BY_ID = 1;
    public static final int SORT_BY_NAME = 2;
    public static final int SORT_BY_STATUS = 3;
    public static final int SORT_BY_DESCRIPTION = 4;
    public static final int SORT_BY_RUN_TIME_LIMIT = 5;
    public static final int SORT_BY_RERUNNABLE = 6;
    public static final int SORT_BY_SUBMITTED_TIME = 7;
    public static final int SORT_BY_STARTED_TIME = 8;
    public static final int SORT_BY_FINISHED_TIME = 9;
    public static final int SORT_BY_HOST_NAME = 10;
    public static final int ASC_ORDER = 1;
    public static final int DESC_ORDER = 2;
    private static int currentSort = SORT_BY_ID;
    private static int currentOrder = ASC_ORDER;

    /** Parents list : null if no dependences */
    private ArrayList<InternalTask> dependences = null;

    /** Informations about the launcher and node */
    private ExecuterInformations executerInformations;

    /** Task information : this is the informations that can change during process. */
    private TaskEvent taskInfo = new TaskEvent();

    /** Node exclusion for this task if desired */
    private transient NodeSet nodeExclusion = null;

    /** Contains the user executable */
    protected ExecutableContainer executableContainer = null;

    /**
     * ProActive Empty constructor
     */
    public InternalTask() {
    }

    /**
     * Create the launcher for this taskDescriptor.
     *
     * @param node the node on which to create the launcher.
     * @return the created launcher as an activeObject.
     */
    public abstract TaskLauncher createLauncher(Node node) throws ActiveObjectCreationException,
            NodeException;

    /**
     * Set the field to sort on.
     *
     * @param sortBy
     *            the field on which the sort will be made.
     */
    public static void setSortingBy(int sortBy) {
        currentSort = sortBy;
    }

    /**
     * Set the order for the next sort.
     *
     * @param order
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
    public int compareTo(InternalTask task) {
        switch (currentSort) {
            case SORT_BY_DESCRIPTION:
                return (currentOrder == ASC_ORDER) ? (description.compareTo(task.description))
                        : (task.description.compareTo(description));
            case SORT_BY_NAME:
                return (currentOrder == ASC_ORDER) ? (name.compareTo(task.name))
                        : (task.name.compareTo(name));
            case SORT_BY_STATUS:
                return (currentOrder == ASC_ORDER) ? (getStatus().compareTo(task.getStatus())) : (task
                        .getStatus().compareTo(getStatus()));
            case SORT_BY_SUBMITTED_TIME:
                return (currentOrder == ASC_ORDER) ? ((int) (getSubmitTime() - task.getSubmitTime()))
                        : ((int) (task.getSubmitTime() - getSubmitTime()));
            case SORT_BY_STARTED_TIME:
                return (currentOrder == ASC_ORDER) ? ((int) (getStartTime() - task.getStartTime()))
                        : ((int) (task.getStartTime() - getStartTime()));
            case SORT_BY_FINISHED_TIME:
                return (currentOrder == ASC_ORDER) ? ((int) (getFinishedTime() - task.getFinishedTime()))
                        : ((int) (task.getFinishedTime() - getFinishedTime()));
            case SORT_BY_RERUNNABLE:
                return (currentOrder == ASC_ORDER) ? (Integer.valueOf(getRerunnable()).compareTo(Integer
                        .valueOf(task.getRerunnable()))) : (Integer.valueOf(task.getRerunnable())
                        .compareTo(Integer.valueOf(getRerunnable())));
            case SORT_BY_HOST_NAME:
                return (currentOrder == ASC_ORDER) ? (getExecutionHostName().compareTo(task
                        .getExecutionHostName())) : (task.getExecutionHostName()
                        .compareTo(getExecutionHostName()));
            default:
                return (currentOrder == ASC_ORDER) ? (getId().compareTo(task.getId())) : (task.getId()
                        .compareTo(getId()));
        }
    }

    /**
     * Return a container for the user executable represented by this task descriptor.
     * @return the user executable represented by this task descriptor.
     */
    public ExecutableContainer getExecutableContainer() {
        return this.executableContainer;
    }

    /**
     * Add a dependence to the list of dependences for this taskDescriptor.
     * The tasks in this list represents the tasks that the current task have to wait for before starting.
     *
     * @param task a super task of this task.
     */
    public void addDependence(InternalTask task) {
        if (dependences == null) {
            dependences = new ArrayList<InternalTask>();
        }

        dependences.add(task);
    }

    /**
     * Return true if this task has dependencies.
     * It means the first eligible tasks in case of TASK_FLOW job type.
     *
     * @return true if this task has dependencies, false otherwise.
     */
    public boolean hasDependences() {
        return dependences != null;
    }

    /**
     * To get the taskInfo
     *
     * @return the taskInfo
     */
    public TaskEvent getTaskInfo() {
        return taskInfo;
    }

    /**
     * To set the taskInfo
     *
     * @param taskInfo the taskInfo to set
     */
    public void update(TaskEvent taskInfo) {
        this.taskInfo = taskInfo;
    }

    /**
     * Set the number of possible rerun for this task.
     *
     * @param rerunnable the number of rerun possible for this task.
     */
    @Override
    public void setRerunnable(int rerunnable) {
        this.rerunnable = rerunnable;
        this.taskInfo.setRerunnableLeft(rerunnable);
    }

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
     */
    public long getFinishedTime() {
        return taskInfo.getFinishedTime();
    }

    /**
     * To set the finishedTime
     *
     * @param finishedTime the finishedTime to set
     */
    public void setFinishedTime(long finishedTime) {
        taskInfo.setFinishedTime(finishedTime);
    }

    /**
     * To get the jobID
     *
     * @return the jobID
     */
    public JobId getJobId() {
        return taskInfo.getJobId();
    }

    /**
     * To set the jobId
     *
     * @param id the jobId to set
     */
    public void setJobId(JobId id) {
        taskInfo.setJobId(id);
    }

    /**
     * To get the startTime
     *
     * @return the startTime
     */
    public long getStartTime() {
        return taskInfo.getStartTime();
    }

    /**
     * To set the startTime
     *
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        taskInfo.setStartTime(startTime);
    }

    /**
     * To get the submitTime
     *
     * @return the submitTime
     */
    public long getSubmitTime() {
        return taskInfo.getSubmitTime();
    }

    /**
     * To set the submitTime
     *
     * @param submitTime the submitTime to set
     */
    public void setSubmitTime(long submitTime) {
        taskInfo.setSubmitTime(submitTime);
    }

    /**
     * To get the taskId
     *
     * @return the taskID
     */
    public TaskId getId() {
        return taskInfo.getTaskId();
    }

    /**
     * To set the taskId
     *
     * @param taskId the taskId to set
     */
    public void setId(TaskId taskId) {
        taskInfo.setTaskId(taskId);
    }

    /**
     * Set the job info to this task.
     *
     * @param jobInfo a job info containing job id and others informations
     */
    public void setJobInfo(JobEvent jobInfo) {
        taskInfo.setJobEvent(jobInfo);
    }

    /**
     * To get the status
     *
     * @return the status
     */
    public TaskState getStatus() {
        return taskInfo.getStatus();
    }

    /**
     * To set the status
     *
     * @param taskState the status to set
     */
    public void setStatus(TaskState taskState) {
        taskInfo.setStatus(taskState);
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
        if (InternalTask.class.isAssignableFrom(obj.getClass())) {
            return ((InternalTask) obj).getId().equals(getId());
        }

        return false;
    }

    /**
     * To get the dependences
     *
     * @return the dependences
     */
    public ArrayList<InternalTask> getDependences() {
        return dependences;
    }

    /**
     * To get the executionHostName
     *
     * @return the executionHostName
     */
    public String getExecutionHostName() {
        return taskInfo.getExecutionHostName();
    }

    /**
     * To set the executionHostName
     *
     * @param executionHostName the executionHostName to set
     */
    public void setExecutionHostName(String executionHostName) {
        taskInfo.setExecutionHostName(executionHostName);
    }

    /**
     * To get the executer informations
     *
     * @return the executerInformations
     */
    public ExecuterInformations getExecuterInformations() {
        return executerInformations;
    }

    /**
     * To set the executer informations.
     *
     * @param executerInformations the executerInformations to set
     */
    public void setExecuterInformations(ExecuterInformations executerInformations) {
        this.executerInformations = executerInformations;
    }

    /**
     * Returns the node Exclusion group.
     * 
     * @return the node Exclusion group.
     */
    public NodeSet getNodeExclusion() {
        return nodeExclusion;
    }

    /**
     * Sets the nodes Exclusion to the given nodeExclusion value.
     *
     * @param nodes Exclusion the nodeExclusion to set.
     */
    public void setNodeExclusion(NodeSet nodeExclusion) {
        this.nodeExclusion = nodeExclusion;
    }

    /**
     * Get the number of rerun left.
     *
     * @return the rerunnableLeft
     */
    public int getRerunnableLeft() {
        return taskInfo.getRerunnableLeft();
    }

    /**
     * Set the number of re-run left.
     *
     * @param rerunnableLeft the rerunnableLeft to set
     */
    public void setRerunnableLeft(int rerunnableLeft) {
        taskInfo.setRerunnableLeft(rerunnableLeft);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.Task#getName()
     */
    @Override
    public String getName() {
        if (getId() == null || getId().getReadableName().equals(TaskId.DEFAULT_TASK_NAME)) {
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
        return "TaskDescriptor(" + getId() + ")";
    }

    protected void setKillTaskTimer(TaskLauncher launcher) {
        if (isWallTime()) {
            launcher.setWallTime(wallTime);
        }
    }
}
