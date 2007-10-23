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
package org.objectweb.proactive.extra.scheduler.common.job;

import java.io.Serializable;
import java.util.HashMap;

import org.objectweb.proactive.extra.scheduler.common.task.Status;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;


/**
 * JobEvent provides some informations about a job.
 * These informations and only them are able to change,
 * that's what the scheduler will send to each listener.event.getJobEvent();
        tasks.get(event.getTaskID()).update(event);
 * To have the jog up to date, user must use Job.setJobInfo(JobEvent); .
 * This will automatically put the job up to date.
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jun 25, 2007
 * @since ProActive 3.2
 */
public class JobEvent implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = -7426315610231893158L;

    /** job id  : must be initialize to a value in order to create temp taskId */
    private JobId jobId = JobId.makeJobId("0");

    /** job submitted time */
    private long submittedTime = -1;

    /** job started time */
    private long startTime = -1;

    /** job finished time */
    private long finishedTime = -1;

    /** job removed time (it means the user got back the result of the job) */
    private long removedTime = -1;

    /** total number of tasks */
    private int totalNumberOfTasks = 0;

    /** number of pending tasks */
    private int numberOfPendingTasks = 0;

    /** number of running tasks */
    private int numberOfRunningTasks = 0;

    /** number of finished tasks */
    private int numberOfFinishedTasks = 0;

    /** job priority */
    private JobPriority priority = JobPriority.NORMAL;

    /** state of the job */
    private JobState state = JobState.PENDING;

    /** If this status is not null, it means the tasks have to change their status */
    private HashMap<TaskId, Status> taskStatusModify = null;

    /** If this finished time is not null, it means the tasks have to change their finished time */
    private HashMap<TaskId, Long> taskFinishedTimeModify = null;

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
    public void setTaskStatusModify(HashMap<TaskId, Status> taskStatusModify) {
        this.taskStatusModify = taskStatusModify;
    }

    /**
     * To get the taskStatusModify
     *
     * @return the taskStatusModify
     */
    public HashMap<TaskId, Status> getTaskStatusModify() {
        return taskStatusModify;
    }

    /**
     * To set the taskFinishedTimeModify
     *
     * @param taskFinishedTimeModify the taskStatusModify to set
     */
    public void setTaskFinishedTimeModify(
        HashMap<TaskId, Long> taskFinishedTimeModify) {
        this.taskFinishedTimeModify = taskFinishedTimeModify;
    }

    /**
     * To get the taskFinishedTimeModify
     *
     * @return the taskFinishedTimeModify
     */
    public HashMap<TaskId, Long> getTaskFinishedTimeModify() {
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
}
