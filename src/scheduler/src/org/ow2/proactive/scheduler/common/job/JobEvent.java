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
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.db.annotation.Alterable;


/**
 * JobEvent provides some informations about a job.<br>
 * These informations and only them are able to change,
 * that's what the scheduler will send to each listener.<br>
 * To have the job up to date, you must use Job.setJobInfo(JobEvent);<br>.
 * This will automatically put the job up to date.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@Entity
@Table(name = "JOB_EVENT")
@AccessType("field")
@Proxy(lazy = false)
public class JobEvent implements Serializable {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    /** job id  : must be initialize to a value in order to create temp taskId */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobId.class)
    private JobId jobId = JobId.makeJobId("0");

    /** job submitted time */
    @Alterable
    @Column(name = "SUBMIT_TIME")
    private long submittedTime = -1;

    /** job started time*/
    //DEFAULT MUST BE -1
    @Alterable
    @Column(name = "START_TIME")
    private long startTime = -1;

    /** job finished time*/
    //DEFAULT MUST BE -1
    @Alterable
    @Column(name = "FINISHED_TIME")
    private long finishedTime = -1;

    /** job removed time (it means the user got back the result of the job)*/
    //DEFAULT MUST BE -1
    @Alterable
    @Column(name = "REMOVED_TIME")
    private long removedTime = -1;

    /** total number of tasks */
    @Column(name = "NB_TASKS")
    @Alterable
    private int totalNumberOfTasks = 0;

    /** number of pending tasks */
    @Column(name = "NB_PENDING_TASKS")
    @Alterable
    private int numberOfPendingTasks = 0;

    /** number of running tasks */
    @Column(name = "NB_RUNNING_TASKS")
    @Alterable
    private int numberOfRunningTasks = 0;

    /** number of finished tasks */
    @Column(name = "NB_FINISHED_TASKS")
    @Alterable
    private int numberOfFinishedTasks = 0;

    /** job priority */
    @Column(name = "PRIORITY")
    @Alterable
    private JobPriority priority = JobPriority.NORMAL;

    /** state of the job */
    @Column(name = "STATE")
    @Alterable
    private JobState state = JobState.PENDING;

    /** to know if the job has to be removed after the fixed admin delay or not */
    @Column(name = "TO_REMOVE")
    @Alterable
    private boolean toBeRemoved = false;

    /** If this status is not null, it means the tasks have to change their status */
    //not Hibernate informations
    @Transient
    private Map<TaskId, TaskState> taskStatusModify = null;

    /** If this finished time is not null, it means the tasks have to change their finished time */
    //not Hibernate informations
    @Transient
    private Map<TaskId, Long> taskFinishedTimeModify = null;

    /** Hibernate default constructor */
    public JobEvent() {
    }

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    public JobId getJobId() {
        return jobId;
    }

    /**
     * To set the jobId
     *
     * @param jobId the jobId to set
     */
    public void setJobId(JobId jobId) {
        this.jobId = jobId;
    }

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
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
     * To get the removedTime
     *
     * @return the removedTime
     */
    public long getRemovedTime() {
        return removedTime;
    }

    /**
     * To set the removedTime
     *
     * @param removedTime the removedTime to set
     */
    public void setRemovedTime(long removedTime) {
        this.removedTime = removedTime;
    }

    /**
     * To get the startTime
     *
     * @return the startTime
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
     * To get the submittedTime
     *
     * @return the submittedTime
     */
    public long getSubmittedTime() {
        return submittedTime;
    }

    /**
     * To set the submittedTime
     *
     * @param submittedTime the submittedTime to set
     */
    public void setSubmittedTime(long submittedTime) {
        this.submittedTime = submittedTime;
    }

    /**
     * To get the totalNumberOfTasks
     *
     * @return the totalNumberOfTasks
     */
    public int getTotalNumberOfTasks() {
        return totalNumberOfTasks;
    }

    /**
     * To set the taskStatusModify
     *
     * @param taskStatusModify the taskStatusModify to set
     */
    public void setTaskStatusModify(Map<TaskId, TaskState> taskStatusModify) {
        this.taskStatusModify = taskStatusModify;
    }

    /**
     * To get the taskStatusModify
     *
     * @return the taskStatusModify
     */
    public Map<TaskId, TaskState> getTaskStatusModify() {
        return taskStatusModify;
    }

    /**
     * To set the taskFinishedTimeModify
     *
     * @param taskFinishedTimeModify the taskStatusModify to set
     */
    public void setTaskFinishedTimeModify(Map<TaskId, Long> taskFinishedTimeModify) {
        this.taskFinishedTimeModify = taskFinishedTimeModify;
    }

    /**
     * To get the taskFinishedTimeModify
     *
     * @return the taskFinishedTimeModify
     */
    public Map<TaskId, Long> getTaskFinishedTimeModify() {
        return taskFinishedTimeModify;
    }

    /**
     * To get the numberOfFinishedTasks
     *
     * @return the numberOfFinishedTasks
     */
    public int getNumberOfFinishedTasks() {
        return numberOfFinishedTasks;
    }

    /**
     * To set the numberOfFinishedTasks
     *
     * @param numberOfFinishedTasks the numberOfFinishedTasks to set
     */
    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        this.numberOfFinishedTasks = numberOfFinishedTasks;
    }

    /**
     * To get the numberOfPendingTasks
     *
     * @return the numberOfPendingTasks
     */
    public int getNumberOfPendingTasks() {
        return numberOfPendingTasks;
    }

    /**
     * To set the numberOfPendingTasks
     *
     * @param numberOfPendingTasks the numberOfPendingTasks to set
     */
    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        this.numberOfPendingTasks = numberOfPendingTasks;
    }

    /**
     * To get the numberOfRunningTasks
     *
     * @return the numberOfRunningTasks
     */
    public int getNumberOfRunningTasks() {
        return numberOfRunningTasks;
    }

    /**
     * To set the numberOfRunningTasks
     *
     * @param numberOfRunningTasks the numberOfRunningTasks to set
     */
    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        this.numberOfRunningTasks = numberOfRunningTasks;
    }

    /**
     * To set the totalNumberOfTasks
     *
     * @param totalNumberOfTasks the totalNumberOfTasks to set
     */
    public void setTotalNumberOfTasks(int totalNumberOfTasks) {
        this.totalNumberOfTasks = totalNumberOfTasks;
    }

    /**
     * To get the priority.
     *
     * @return the priority.
     */
    public JobPriority getPriority() {
        return priority;
    }

    /**
     * To set The priority.
     *
     * @param priority the priority to set
     */
    public void setPriority(JobPriority priority) {
        this.priority = priority;
    }

    /**
     * Return the state of the job.
     *
     * @return the state of the job.
     */
    public JobState getState() {
        return state;
    }

    /**
     * To set the state of the job.
     *
     * @param state the state to set.
     */
    public void setState(JobState state) {
        this.state = state;
    }

    /**
     * Get the toBeRemoved property.
     *
     * @return the toBeRemoved property.
     */
    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    /**
     * Set this job to the state toBeRemoved.
     */
    public void setToBeRemoved() {
        this.toBeRemoved = true;
    }

}
