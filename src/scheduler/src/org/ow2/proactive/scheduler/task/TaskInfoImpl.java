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
package org.ow2.proactive.scheduler.task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;
import org.ow2.proactive.scheduler.common.db.annotation.Alterable;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.JobInfoImpl;


/**
 * Informations about the task that is able to change.<br>
 * These informations are not in the {@link Task} class in order to permit
 * the scheduler listener to send this class as event.
 * To keep an internalTask up to date, just use the {@link InternalTask.update(TaskInfo)} method.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@Entity
@Table(name = "TASK_INFO")
@AccessType("field")
@Proxy(lazy = false)
public class TaskInfoImpl implements TaskInfo {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    /** id of the task */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = TaskIdImpl.class)
    private TaskId taskId = null;

    /** informations about the job */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobInfoImpl.class)
    private JobInfo jobInfo = null;

    /** task started time */
    @Alterable
    @Column(name = "START_TIME")
    private long startTime = -1;

    /** task finished time : DEFAULT HAS TO BE SET TO -1 */
    @Alterable
    @Column(name = "FINISHED_TIME")
    private long finishedTime = -1;

    /** Current taskStatus of the task */
    @Alterable
    @Column(name = "TASK_STATE")
    private TaskStatus taskStatus = TaskStatus.SUBMITTED;

    /** name of the host where the task is executed */
    @Alterable
    @Column(name = "EXEC_HOSTNAME")
    private String executionHostName;

    /** Number of executions left */
    @Alterable
    @Column(name = "NB_EXEC_LEFT")
    private int numberOfExecutionLeft = 1;

    /** Number of execution left for this task in case of failure (node down) */
    @Alterable
    @Column(name = "NB_EXEC_ON_FAILURE_LEFT")
    private int numberOfExecutionOnFailureLeft = 1;

    /** Hibernate default constructor */
    public TaskInfoImpl() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getJobInfo()
     */
    public JobInfo getJobInfo() {
        return jobInfo;
    }

    /**
     * To set the jobInfo
     *
     * @param jobInfo the jobInfo to set
     */
    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getFinishedTime()
     */
    public long getFinishedTime() {
        return finishedTime;
    }

    /**
     * To set the finishedTime
     *
     * @param finishedTime the finishedTime to set
     */
    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getJobId()
     */
    public JobId getJobId() {
        if (jobInfo != null) {
            return jobInfo.getJobId();
        }

        return null;
    }

    /**
     * To set the jobId
     *
     * @param jobId the jobId to set
     */
    public void setJobId(JobId jobId) {
        if (jobInfo != null) {
            ((JobInfoImpl) jobInfo).setJobId(jobId);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getStartTime()
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * To set the startTime
     *
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getTaskId()
     */
    public TaskId getTaskId() {
        return taskId;
    }

    /**
     * To set the taskId
     *
     * @param taskId The taskId to be set.
     *
     */
    public void setTaskId(TaskId taskId) {
        this.taskId = taskId;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getStatus()
     */
    public TaskStatus getStatus() {
        return taskStatus;
    }

    /**
     * To set the taskStatus
     *
     * @param taskStatus the taskStatus to set
     */
    public void setStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getExecutionHostName()
     */
    public String getExecutionHostName() {
        return executionHostName;
    }

    /**
     * To set the executionHostName
     *
     * @param executionHostName the executionHostName to set
     */
    public void setExecutionHostName(String executionHostName) {
        this.executionHostName = executionHostName;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getNumberOfExecutionLeft()
     */
    public int getNumberOfExecutionLeft() {
        return numberOfExecutionLeft;
    }

    /**
     * Set the number of execution left.
     *
     * @param numberOfExecutionLeft the number of execution left to set.
     */
    public void setNumberOfExecutionLeft(int numberOfExecutionLeft) {
        this.numberOfExecutionLeft = numberOfExecutionLeft;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getNumberOfExecutionOnFailureLeft()
     */
    public int getNumberOfExecutionOnFailureLeft() {
        return numberOfExecutionOnFailureLeft;
    }

    /**
     * Decrease the number of execution left.
     */
    public void decreaseNumberOfExecutionLeft() {
        numberOfExecutionLeft--;
    }

    /**
     * Set the initial number of execution on failure left.
     *
     * @param numberOfExecutionOnFailureLeft the new number of execution to be set.
     */
    public void setNumberOfExecutionOnFailureLeft(int numberOfExecutionOnFailureLeft) {
        this.numberOfExecutionOnFailureLeft = numberOfExecutionOnFailureLeft;
    }

    /**
     * Decrease the number of execution on failure left.
     */
    public void decreaseNumberOfExecutionOnFailureLeft() {
        numberOfExecutionOnFailureLeft--;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + taskId + "]";
    }

}
