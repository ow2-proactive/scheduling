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
package org.ow2.proactive.scheduler.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.JobInfoImpl;


/**
 * Information about the task that is able to change.<br>
 * These information are not in the {@link Task} class in order to permit
 * the scheduler listener to send this class as event.
 * To keep an internalTask up to date, just use the {@code org.ow2.proactive.scheduler.task.internal.InternalTask#update(TaskInfo)} method.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskInfoImpl implements TaskInfo {

    private static final String HOSTNAME_SEPARATOR = ",";

    /** id of the task */
    private TaskId taskId = null;

    /** informations about the job */
    @XmlTransient
    private JobInfo jobInfo = null;

    /** task started time */
    private long startTime = -1;

    /** task finished time : DEFAULT HAS TO BE SET TO -1 */
    private long finishedTime = -1;

    /** task scheduled time : DEFAULT HAS TO BE SET TO -1 */
    private long scheduledTime = -1;

    /** task real execution time : DEFAULT HAS TO BE SET TO -1 */
    private long executionDuration = -1;

    /** Current taskStatus of the task */
    private TaskStatus taskStatus = TaskStatus.SUBMITTED;

    /** Current progress value of the task */
    private int progress = 0;

    /** name of the host where the task is executed */
    private String executionHostName;

    /** Number of executions left */
    private int numberOfExecutionLeft = 1;

    /** Number of execution left for this task in case of failure (node down) */
    private int numberOfExecutionOnFailureLeft = 1;

    public TaskInfoImpl() {
    }

    /*
     * Copy constructor is used to pass task information to the event listener 
     * (SchedulerStateUpdate)
     */
    public TaskInfoImpl(TaskInfoImpl taskInfo) {
        this.taskId = taskInfo.getTaskId();
        this.jobInfo = new JobInfoImpl((JobInfoImpl) taskInfo.getJobInfo());
        this.startTime = taskInfo.getStartTime();
        this.finishedTime = taskInfo.getFinishedTime();
        this.scheduledTime = taskInfo.getScheduledTime();
        this.executionDuration = taskInfo.getExecutionDuration();
        this.taskStatus = taskInfo.getStatus();
        this.progress = taskInfo.getProgress();
        this.executionHostName = taskInfo.getExecutionHostName();
        this.numberOfExecutionLeft = taskInfo.getNumberOfExecutionLeft();
        this.numberOfExecutionOnFailureLeft = taskInfo.getNumberOfExecutionOnFailureLeft();
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
     * To set the scheduledTime
     *
     * @param scheduledTime the scheduledTime to set
     */
    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
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
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getName()
     */
    public String getName() {
        return this.taskId.getReadableName();
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
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getProgress()
     */
    public int getProgress() {
        return this.progress;
    }

    /**
     * To set the progress value
     *
     * @throws IllegalArgumentException if the new value is not ranged between 0 and 100.
     */
    public void setProgress(int newValue) {
        if (newValue < 0 || newValue > 100) {
            throw new IllegalArgumentException("Progress value must be ranged between 0 and 100");
        }
        this.progress = newValue;
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
        if (this.executionHostName == null || "".equals(this.executionHostName)) {
            return null;
        } else {
            return getExecutionHostNameList()[0];
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getExecutionHostNameList()
     */
    public String[] getExecutionHostNameList() {
        if (this.executionHostName == null || "".equals(this.executionHostName)) {
            return null;
        } else {
            return this.executionHostName.split(HOSTNAME_SEPARATOR);
        }
    }

    /**
     * Set a new execution HostName.
     * If there was already a hostname, the new one is prepended to the old one.
     *
     * @param executionHostName the execution HostName to set
     */
    public void setExecutionHostName(String executionHostName) {
        if (this.executionHostName == null || "".equals(this.executionHostName)) {
            this.executionHostName = executionHostName;
        } else {
            this.executionHostName = executionHostName + HOSTNAME_SEPARATOR + this.executionHostName;
        }
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
     * {@inheritDoc}
     */
    public long getExecutionDuration() {
        return executionDuration;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskInfo#getScheduledTime()
     */
    public long getScheduledTime() {
        return this.scheduledTime;
    }


    /**
     * Set the execution duration value to the given executionDuration value
     * 
     * @param executionDuration the executionDuration to be set
     */
    public void setExecutionDuration(long executionDuration) {
        this.executionDuration = executionDuration;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Job=" + taskId.getJobId().value() + ", TaskId=" + taskId.value() + "]";
    }
}
