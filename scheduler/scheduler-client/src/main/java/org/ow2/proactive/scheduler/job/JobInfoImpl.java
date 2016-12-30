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
package org.ow2.proactive.scheduler.job;

import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.ClientTaskState;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * JobInfo provides some information about the Job it is linked with.
 * <br>
 * These information and only them are able to change inside the job,
 * and that's what the scheduler will send to each listener.
 * <br>
 * To have a job up to date, you must use {@code org.ow2.proactive.scheduler.job.InternalJob#setJobInfo(JobInfo)}.
 * This will automatically put the job up to date.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobInfoImpl implements JobInfo {

    /** job id: must be initialized to a value in order to create temp taskId */
    private JobId jobId = JobIdImpl.makeJobId("0");

    private String owner;

    /** job submitted time */
    private long submittedTime = -1;

    /** job started time*/
    //DEFAULT MUST BE -1
    private long startTime = -1;

    /** job inError time*/
    //DEFAULT MUST BE -1
    private long inErrorTime = -1;

    /** job finished time*/
    //DEFAULT MUST BE -1
    private long finishedTime = -1;

    /** job removed time (it means the user got back the result of the job)*/
    //DEFAULT MUST BE -1
    private long removedTime = -1;

    /** last updated time on the job*/
    private long lastUpdatedTime = -1;

    /** total number of tasks */
    private int totalNumberOfTasks = 0;

    /** number of pending tasks */
    private int numberOfPendingTasks = 0;

    /** number of running tasks */
    private int numberOfRunningTasks = 0;

    /** number of finished tasks */
    private int numberOfFinishedTasks = 0;

    /** number of failed tasks */
    private int numberOfFailedTasks = 0;

    /** number of faulty tasks */
    private int numberOfFaultyTasks = 0;

    /** number of in-error tasks */
    private int numberOfInErrorTasks = 0;

    /** job priority */
    private JobPriority priority = JobPriority.NORMAL;

    /** status of the job */
    private JobStatus status = JobStatus.PENDING;

    /** to know if the job has to be removed after the fixed admin delay or not */
    private boolean toBeRemoved;

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
        this.inErrorTime = jobInfo.getInErrorTime();
        this.finishedTime = jobInfo.getFinishedTime();
        this.removedTime = jobInfo.getRemovedTime();
        this.totalNumberOfTasks = jobInfo.getTotalNumberOfTasks();
        this.numberOfPendingTasks = jobInfo.getNumberOfPendingTasks();
        this.numberOfRunningTasks = jobInfo.getNumberOfRunningTasks();
        this.numberOfFinishedTasks = jobInfo.getNumberOfFinishedTasks();
        this.numberOfFailedTasks = jobInfo.getNumberOfFailedTasks();
        this.numberOfFaultyTasks = jobInfo.getNumberOfFaultyTasks();
        this.numberOfInErrorTasks = jobInfo.getNumberOfInErrorTasks();
        this.priority = jobInfo.getPriority();
        this.status = jobInfo.getStatus();
        this.toBeRemoved = jobInfo.toBeRemoved;

        if (jobInfo.getTasksSkipped() != null) {
            this.tasksSkipped = new HashSet<>(jobInfo.getTasksSkipped());
        }
        if (jobInfo.getModifiedTasks() != null) {
            this.modifiedTasks = new ArrayList<>(jobInfo.getModifiedTasks());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJobOwner() {
        return owner;
    }

    public void setJobOwner(String owner) {
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobId getJobId() {
        return jobId;
    }

    public void setJobId(JobId jobId) {
        this.jobId = jobId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRemovedTime() {
        return removedTime;
    }

    public void setRemovedTime(long removedTime) {
        this.removedTime = removedTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getInErrorTime() {
        return inErrorTime;
    }

    public void setInErrorTime(long inErrorTime) {
        this.inErrorTime = inErrorTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(long submittedTime) {
        this.submittedTime = submittedTime;
    }

    @Override
    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalNumberOfTasks() {
        return totalNumberOfTasks;
    }

    public void setTotalNumberOfTasks(int totalNumberOfTasks) {
        this.totalNumberOfTasks = totalNumberOfTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFinishedTasks() {
        return numberOfFinishedTasks;
    }

    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        this.numberOfFinishedTasks = numberOfFinishedTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfPendingTasks() {
        return numberOfPendingTasks;
    }

    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        this.numberOfPendingTasks = numberOfPendingTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfRunningTasks() {
        return numberOfRunningTasks;
    }

    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        this.numberOfRunningTasks = numberOfRunningTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFailedTasks() {
        return numberOfFailedTasks;
    }

    public void setNumberOfFailedTasks(int numberOfFailedTasks) {
        this.numberOfFailedTasks = numberOfFailedTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFaultyTasks() {
        return numberOfFaultyTasks;
    }

    public void setNumberOfFaultyTasks(int numberOfFaultyTasks) {
        this.numberOfFaultyTasks = numberOfFaultyTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfInErrorTasks() {
        return numberOfInErrorTasks;
    }

    public void setNumberOfInErrorTasks(int numberOfInErrorTasks) {
        this.numberOfInErrorTasks = numberOfInErrorTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobPriority getPriority() {
        return priority;
    }

    public void setPriority(JobPriority priority) {
        this.priority = priority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

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

    public void setTasksChanges(ChangedTasksInfo changesInfo, JobState job) {
        this.modifiedTasks =
                new ArrayList<>(
                        changesInfo.getNewTasks().size() + changesInfo.getUpdatedTasks().size());

        for (TaskId id : changesInfo.getNewTasks()) {
            modifiedTasks.add(new ClientTaskState(job.getHMTasks().get(id)));
        }

        for (TaskId id : changesInfo.getUpdatedTasks()) {
            modifiedTasks.add(new ClientTaskState(job.getHMTasks().get(id)));
        }

        this.tasksSkipped = new HashSet<>(changesInfo.getSkippedTasks());
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
