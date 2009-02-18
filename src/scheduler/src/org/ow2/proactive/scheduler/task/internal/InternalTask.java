/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.internal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.db.annotation.Unloadable;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutable;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskEventImpl;
import org.ow2.proactive.scheduler.task.TaskLauncher;


/**
 * Internal and global description of a task.
 * This class contains all informations about the task to launch.
 * It also provides a method to create its own launcher.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@MappedSuperclass
@Table(name = "INTERNAL_TASK")
@AccessType("field")
@Proxy(lazy = false)
public abstract class InternalTask extends Task implements Comparable<InternalTask> {

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
    public static final int ASC_ORDER = 1;
    public static final int DESC_ORDER = 2;
    private static int currentSort = SORT_BY_ID;
    private static int currentOrder = ASC_ORDER;

    /** Parents list : null if no dependences */
    @ManyToAny(metaColumn = @Column(name = "ITASK_TYPE", length = 5))
    @AnyMetaDef(idType = "long", metaType = "string", metaValues = {
            @MetaValue(targetEntity = InternalJavaTask.class, value = "IJT"),
            @MetaValue(targetEntity = InternalNativeTask.class, value = "INT"),
            @MetaValue(targetEntity = InternalProActiveTask.class, value = "IPT") })
    @JoinTable(joinColumns = @JoinColumn(name = "ITASK_ID"), inverseJoinColumns = @JoinColumn(name = "DEPEND_ID"))
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @Cascade(CascadeType.ALL)
    private List<InternalTask> idependences = null;

    /** Informations about the launcher and node */
    //These informations are not required during task process
    @Transient
    private ExecuterInformations executerInformations;

    /** Task information : this is the informations that can change during process. */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = TaskEventImpl.class)
    private TaskEventImpl taskInfo = new TaskEventImpl();

    /** Node exclusion for this task if desired */
    @Transient
    private transient NodeSet nodeExclusion = null;

    /** Contains the user executable */
    @Unloadable
    @Any(fetch = FetchType.EAGER, metaColumn = @Column(name = "EXEC_CONTAINER_TYPE", updatable = false, length = 5))
    @AnyMetaDef(idType = "long", metaType = "string", metaValues = {
            @MetaValue(targetEntity = JavaExecutableContainer.class, value = "JEC"),
            @MetaValue(targetEntity = NativeExecutableContainer.class, value = "NEC"),
            @MetaValue(targetEntity = ForkedJavaExecutable.class, value = "FJE") })
    @JoinColumn(name = "EXEC_CONTAINER_ID", updatable = false)
    @Cascade(CascadeType.ALL)
    protected ExecutableContainer executableContainer = null;

    /** Maximum number of execution for this task in case of failure (node down) */
    @Column(name = "MAX_EXEC_ON_FAILURE")
    private int maxNumberOfExecutionOnFailure = PASchedulerProperties.NUMBER_OF_EXECUTION_ON_FAILURE
            .getValueAsInt();

    /** Hibernate default constructor */
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
            case SORT_BY_STARTED_TIME:
                return (currentOrder == ASC_ORDER) ? ((int) (getStartTime() - task.getStartTime()))
                        : ((int) (task.getStartTime() - getStartTime()));
            case SORT_BY_FINISHED_TIME:
                return (currentOrder == ASC_ORDER) ? ((int) (getFinishedTime() - task.getFinishedTime()))
                        : ((int) (task.getFinishedTime() - getFinishedTime()));
            case SORT_BY_EXECUTIONLEFT:
                return (currentOrder == ASC_ORDER) ? (Integer.valueOf(getNumberOfExecutionLeft())
                        .compareTo(Integer.valueOf(task.getNumberOfExecutionLeft()))) : (Integer.valueOf(task
                        .getNumberOfExecutionLeft()).compareTo(Integer.valueOf(getNumberOfExecutionLeft())));
            case SORT_BY_EXECUTIONONFAILURELEFT:
                return (currentOrder == ASC_ORDER) ? (Integer.valueOf(getNumberOfExecutionOnFailureLeft())
                        .compareTo(Integer.valueOf(task.getNumberOfExecutionOnFailureLeft()))) : (Integer
                        .valueOf(task.getNumberOfExecutionOnFailureLeft()).compareTo(Integer
                        .valueOf(getNumberOfExecutionOnFailureLeft())));
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
     * 
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
        if (idependences == null) {
            idependences = new ArrayList<InternalTask>();
        }

        idependences.add(task);
    }

    /**
     * Return true if this task has dependencies.
     * It means the first eligible tasks in case of TASK_FLOW job type.
     *
     * @return true if this task has dependencies, false otherwise.
     */
    public boolean hasDependences() {
        return (idependences != null && idependences.size() > 0);
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
        this.taskInfo = (TaskEventImpl) taskInfo;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.CommonAttribute#setNumberOfExecution(int)
     */
    @Override
    public void setMaxNumberOfExecution(int numberOfExecution) {
        super.setMaxNumberOfExecution(numberOfExecution);
        this.taskInfo.setNumberOfExecutionLeft(numberOfExecution);
        this.taskInfo.setNumberOfExecutionOnFailureLeft(maxNumberOfExecutionOnFailure);
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
     * To get the dependences of this task.
     * Return null if this task has no dependence.
     *
     * @return the dependences
     */
    public List<InternalTask> getDependences() {
        //set to null if needed
        if (idependences != null && idependences.size() == 0) {
            idependences = null;
        }
        return idependences;
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
     * Get the number of execution left.
     *
     * @return the number of execution left.
     */
    public int getNumberOfExecutionLeft() {
        return taskInfo.getNumberOfExecutionLeft();
    }

    /**
     * Decrease the number of re-run left.
     */
    public void decreaseNumberOfExecutionLeft() {
        taskInfo.decreaseNumberOfExecutionLeft();
    }

    /**
     * Get the numberOfExecutionOnFailureLeft value.
     * 
     * @return the numberOfExecutionOnFailureLeft value.
     */
    public int getNumberOfExecutionOnFailureLeft() {
        return taskInfo.getNumberOfExecutionOnFailureLeft();
    }

    /**
     * Decrease the number of execution on failure left.
     */
    public void decreaseNumberOfExecutionOnFailureLeft() {
        taskInfo.decreaseNumberOfExecutionOnFailureLeft();
    }

    /**
     * Get the number of execution on failure allowed by the task.
     * 
     * @return the number of execution on failure allowed by the task
     */
    public int getMaxNumberOfExecutionOnFailure() {
        return maxNumberOfExecutionOnFailure;
    }

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

    protected void setKillTaskTimer(TaskLauncher launcher) {
        if (isWallTime()) {
            launcher.setWallTime(wallTime);
        }
    }
}
