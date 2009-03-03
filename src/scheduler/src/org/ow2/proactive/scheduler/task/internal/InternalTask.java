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
import org.ow2.proactive.scheduler.common.db.annotation.Unloadable;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutable;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.utils.NodeSet;


/**
 * Internal and global description of a task.
 * This class contains all informations about the task to launch.
 * It also provides methods to create its own launcher and manage the content regarding the scheduling order.<br/>
 * Specific internal task may extend this abstract class.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@MappedSuperclass
@Table(name = "INTERNAL_TASK")
@AccessType("field")
@Proxy(lazy = false)
public abstract class InternalTask extends TaskState {

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
    @OneToOne(fetch = FetchType.EAGER, targetEntity = TaskInfoImpl.class)
    private TaskInfoImpl taskInfo = new TaskInfoImpl();

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
     * @see org.ow2.proactive.scheduler.common.task.TaskState#hasDependences()
     */
    public boolean hasDependences() {
        return (idependences != null && idependences.size() > 0);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskState#getTaskInfo()
     */
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskState#update(org.ow2.proactive.scheduler.common.task.TaskInfo)
     */
    public synchronized void update(TaskInfo taskInfo) {
        if (!getId().equals(taskInfo.getTaskId())) {
            throw new IllegalArgumentException(
                "This task info is not applicable for this task. (expected id is '" + getId() +
                    "' but was '" + taskInfo.getTaskId() + "'");
        }
        this.taskInfo = (TaskInfoImpl) taskInfo;
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
     * To set the finishedTime
     *
     * @param finishedTime the finishedTime to set
     */
    public void setFinishedTime(long finishedTime) {
        taskInfo.setFinishedTime(finishedTime);
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
     * To set the startTime
     *
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        taskInfo.setStartTime(startTime);
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
    public void setJobInfo(JobInfo jobInfo) {
        taskInfo.setJobInfo(jobInfo);
    }

    /**
     * To set the status
     *
     * @param taskStatus the status to set
     */
    public void setStatus(TaskStatus taskStatus) {
        taskInfo.setStatus(taskStatus);
    }

    /**
     * To get the dependences of this task as internal tasks.
     * Return null if this task has no dependence.
     *
     * @return the dependences
     */
    public List<InternalTask> getIDependences() {
        //set to null if needed
        if (idependences != null && idependences.size() == 0) {
            idependences = null;
        }
        return idependences;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskState#getDependences()
     */
    public List<TaskState> getDependences() {
        //set to null if needed
        if (idependences == null || idependences.size() == 0) {
            idependences = null;
            return null;
        }
        List<TaskState> tmp = new ArrayList<TaskState>(idependences.size());
        for (TaskState ts : idependences) {
            tmp.add(ts);
        }
        return tmp;
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
     * Decrease the number of re-run left.
     */
    public void decreaseNumberOfExecutionLeft() {
        taskInfo.decreaseNumberOfExecutionLeft();
    }

    /**
     * Decrease the number of execution on failure left.
     */
    public void decreaseNumberOfExecutionOnFailureLeft() {
        taskInfo.decreaseNumberOfExecutionOnFailureLeft();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskState#getMaxNumberOfExecutionOnFailure()
     */
    public int getMaxNumberOfExecutionOnFailure() {
        return maxNumberOfExecutionOnFailure;
    }

    /**
     * Set the given launcher the local wallTime
     *
     * @param launcher the launcher on which to set the walltime
     */
    protected void setKillTaskTimer(TaskLauncher launcher) {
        if (isWallTime()) {
            launcher.setWallTime(wallTime);
        }
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

}
