/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.scheduler.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.task.ClientTaskState;


/**
 * JobInfo provides some informations about a job.<br>
 * These informations and only them are able to change inside the job,
 * that's what the scheduler will send to each listener.<br>
 * To have a job up to date, you must use InternalJob.setJobInfo(JobInfo);<br>.
 * This will automatically put the job up to date.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobInfoImpl implements JobInfo {

    /** job id  : must be initialize to a value in order to create temp taskId */
    private JobId jobId = JobIdImpl.makeJobId("0");

    /** job submitted time */
    private long submittedTime = -1;

    /** job started time*/
    //DEFAULT MUST BE -1
    private long startTime = -1;

    /** job finished time*/
    //DEFAULT MUST BE -1
    private long finishedTime = -1;

    /** job removed time (it means the user got back the result of the job)*/
    //DEFAULT MUST BE -1
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

    /** status of the job */
    private JobStatus status = JobStatus.PENDING;

    /** to know if the job has to be removed after the fixed admin delay or not */
    private boolean toBeRemoved = false;

    /** If this status is not null, it means the tasks have to change their status */
    //not Hibernate informations
    private Map<TaskId, TaskStatus> taskStatusModify = null;

    /** If this finished time is not null, it means the tasks have to change their finished time */
    //not Hibernate informations
    private Map<TaskId, Long> taskFinishedTimeModify = null;

    /**
     * contains the ids of the original and the replicated task,
     * as well as ids of the dependencies of the replicated task
     */
    public static class ReplicatedTask implements Serializable {
        public TaskId originalId = null;
        public TaskId replicatedId = null;
        public List<TaskId> deps = null;

        public ReplicatedTask(TaskId original, TaskId replicated) {
            this.originalId = original;
            this.replicatedId = replicated;
            this.deps = new ArrayList<TaskId>();
        }
    }

    /** Tasks replicated by a Control Flow Action */
    private List<ReplicatedTask> tasksReplicated = null;

    /** Tasks loop by a Control Flow Action */
    private List<ReplicatedTask> tasksLooped = null;

    /** Tasks skipped by a Control Flow Action */
    private List<TaskId> tasksSkipped = null;

    private List<ClientTaskState> modifiedTasks;

    /** Hibernate default constructor */
    public JobInfoImpl() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getJobId()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getFinishedTime()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getRemovedTime()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getStartTime()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getSubmittedTime()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getTotalNumberOfTasks()
     */
    public int getTotalNumberOfTasks() {
        return totalNumberOfTasks;
    }

    /**
     * To set the taskStatusModify
     *
     * @param taskStatusModify the taskStatusModify to set
     */
    public void setTaskStatusModify(Map<TaskId, TaskStatus> taskStatusModify) {
        this.taskStatusModify = taskStatusModify;
    }

    /**
     * To get the taskStatusModify
     *
     * @return the taskStatusModify
     */
    public Map<TaskId, TaskStatus> getTaskStatusModify() {
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
     * Used as an argument for {@link SchedulerEvent#TASK_REPLICATED} to
     * specify which tasks were replicated
     * 
     * @param m list of the replicated tasks
     */
    public void setTasksReplicated(List<ReplicatedTask> m) {
        this.tasksReplicated = m;
    }

    /**
     * Used as an argument for {@link SchedulerEvent#TASK_REPLICATED} to
     * specify which tasks were replicated
     * 
     * @return a list of the replicated tasks
     */
    public List<ReplicatedTask> getTasksReplicated() {
        return this.tasksReplicated;
    }

    /**
     * Used as an argument for {@link SchedulerEvent#TASK_REPLICATED} to
     * specify which tasks were replicated
     * 
     * @param m list of the replicated tasks
     */
    public void setTasksLooped(List<ReplicatedTask> m) {
        this.tasksLooped = m;
    }

    /**
     * Used as an argument for {@link SchedulerEvent#TASK_REPLICATED} to
     * specify which tasks were replicated
     * 
     * @return a list of the replicated tasks
     */
    public List<ReplicatedTask> getTasksLooped() {
        return this.tasksLooped;
    }

    /**
     * Used as an argument for {@link SchedulerEvent#TASK_SKIPPED} to
     * specify which tasks were skipped
     * 
     * @param m list of the skipped tasks
     */
    public void setTasksSkipped(List<TaskId> m) {
        this.tasksSkipped = m;
    }

    /**
     * Used as an argument for {@link SchedulerEvent#TASK_SKIPPED} to
     * specify which tasks were skipped
     * 
     * @return a list of the skipped tasks
     */
    public List<TaskId> getTasksSkipped() {
        return this.tasksSkipped;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getNumberOfFinishedTasks()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getNumberOfPendingTasks()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getNumberOfRunningTasks()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getPriority()
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
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#getStatus()
     */
    public JobStatus getStatus() {
        return status;
    }

    /**
     * To set the status of the job.
     *
     * @param status the status to set.
     */
    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobInfo#isToBeRemoved()
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

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + jobId + "]";
    }

    public void setModifiedTasks(List<ClientTaskState> tasks) {
        this.modifiedTasks = tasks;
    }

    public List<ClientTaskState> getModifiedTasks() {
        return this.modifiedTasks;
    }

}
