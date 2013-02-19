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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private String owner;

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
    private boolean toBeRemoved;

    /** If this status is not null, it means the tasks have to change their status */
    //not Hibernate informations
    private Map<TaskId, TaskStatus> taskStatusModify;

    /** If this finished time is not null, it means the tasks have to change their finished time */
    //not Hibernate informations
    private Map<TaskId, Long> taskFinishedTimeModify;

    /** Tasks skipped by a Control Flow Action */
    private Set<TaskId> tasksSkipped;

    private List<ClientTaskState> modifiedTasks;

    public JobInfoImpl() {
    }

    /*
     * Copy constructor is used to pass job information to the event listener 
     * (SchedulerStateUpdate)
     */
    public JobInfoImpl(JobInfoImpl jobInfo) {
        this.jobId = jobInfo.getJobId();
        this.owner = jobInfo.owner;
        this.submittedTime = jobInfo.getSubmittedTime();
        this.startTime = jobInfo.getStartTime();
        this.finishedTime = jobInfo.getFinishedTime();
        this.removedTime = jobInfo.getRemovedTime();
        this.totalNumberOfTasks = jobInfo.getTotalNumberOfTasks();
        this.numberOfPendingTasks = jobInfo.getNumberOfPendingTasks();
        this.numberOfRunningTasks = jobInfo.getNumberOfRunningTasks();
        this.numberOfFinishedTasks = jobInfo.getNumberOfFinishedTasks();
        this.priority = jobInfo.getPriority();
        this.status = jobInfo.getStatus();
        this.toBeRemoved = jobInfo.toBeRemoved;
        if (jobInfo.taskStatusModify != null) {
            this.taskStatusModify = new HashMap<TaskId, TaskStatus>(jobInfo.taskStatusModify);
        }
        if (jobInfo.getTaskFinishedTimeModify() != null) {
            this.taskFinishedTimeModify = new HashMap<TaskId, Long>(jobInfo.getTaskFinishedTimeModify());
        }
        if (jobInfo.getTasksSkipped() != null) {
            this.tasksSkipped = new HashSet<TaskId>(jobInfo.getTasksSkipped());
        }
        if (jobInfo.getModifiedTasks() != null) {
            this.modifiedTasks = new ArrayList<ClientTaskState>(jobInfo.getModifiedTasks());
        }
    }

    @Override
    public String getJobOwner() {
        return owner;
    }

    public void setJobOwner(String owner) {
        this.owner = owner;
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

    public void setTasksChanges(ChangedTasksInfo changesInfo, InternalJob job) {
        this.modifiedTasks = new ArrayList<ClientTaskState>(changesInfo.getNewTasks().size() +
            changesInfo.getUpdatedTasks().size());
        for (TaskId id : changesInfo.getNewTasks()) {
            modifiedTasks.add(new ClientTaskState(job.getIHMTasks().get(id)));
        }
        for (TaskId id : changesInfo.getUpdatedTasks()) {
            modifiedTasks.add(new ClientTaskState(job.getIHMTasks().get(id)));
        }
        this.tasksSkipped = new HashSet<TaskId>(changesInfo.getSkippedTasks());
    }

    public void clearTasksChanges() {
        modifiedTasks = null;
        tasksSkipped = null;
    }

    public List<ClientTaskState> getModifiedTasks() {
        return this.modifiedTasks;
    }

    /**
     * Used as an argument for {@link SchedulerEvent#TASK_SKIPPED} to
     * specify which tasks were skipped
     * 
     * @return a set of the skipped tasks
     */
    public Set<TaskId> getTasksSkipped() {
        return this.tasksSkipped;
    }

}
