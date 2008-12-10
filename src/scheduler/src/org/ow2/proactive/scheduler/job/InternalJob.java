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
package org.ow2.proactive.scheduler.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * Abstract class job.
 * This class represents a job with no specification.
 * Specific jobs may extend this class.
 * It provides method to order the job and to set and get every needed properties.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public abstract class InternalJob extends Job implements Comparable<InternalJob> {
    /** Used to sort by id */
    public static final int SORT_BY_ID = 1;
    /** Used to sort by name */
    public static final int SORT_BY_NAME = 2;
    /** Used to sort by priority */
    public static final int SORT_BY_PRIORITY = 3;
    /** Used to sort by type */
    public static final int SORT_BY_TYPE = 4;
    /** Used to sort by description */
    public static final int SORT_BY_DESCRIPTION = 5;
    /** Used to sort by owner */
    public static final int SORT_BY_OWNER = 6;
    /** Used to sort by state */
    public static final int SORT_BY_STATE = 7;
    /** Used to sort by project name */
    public static final int SORT_BY_PROJECT = 8;
    /** Used to sort according to ascendant order */
    public static final int ASC_ORDER = 1;
    /** Used to sort according to descendant order */
    public static final int DESC_ORDER = 2;
    private static int currentSort = SORT_BY_ID;
    private static int currentOrder = ASC_ORDER;

    /** Owner of the job */
    private String owner = "";

    /** List of every tasks in this job. */
    protected HashMap<TaskId, InternalTask> tasks = new HashMap<TaskId, InternalTask>();

    /** Instances of the precious task results, important to know which results the user wants */
    protected Vector<InternalTask> preciousResults = new Vector<InternalTask>();

    /** Informations (that can be modified) about job execution */
    protected JobEvent jobInfo = new JobEvent();

    /** Job descriptor for dependences management */
    private JobDescriptor jobDescriptor;

    /** Job result */
    private JobResult jobResult;

    /** Initial waiting time for a task before restarting in millisecond */
    private long restartWaitingTimer = PASchedulerProperties.REEXECUTION_INITIAL_WAITING_TIME.getValueAsInt();

    /**
     * ProActive empty constructor.
     */
    public InternalJob() {
    }

    /**
     * Create a new Job with the given parameters. It provides methods to add or
     * remove tasks.
     *
     * @param name the current job name.
     * @param priority the priority of this job between 1 and 5.
     * @param cancelJobOnError true if the job has to run until its end or an user intervention.
     * @param description a short description of the job and what it will do.
     */

    public InternalJob(String name, JobPriority priority, boolean cancelJobOnError, String description) {
        this.name = name;
        this.jobInfo.setPriority(priority);
        this.setCancelJobOnError(cancelJobOnError);
        this.description = description;
    }

    /**
     * To get the type
     *
     * @return the type
     */
    @Override
    public abstract JobType getType();

    /**
     * Set the jobEvent contained in the TaskEvent to this job.
     *
     * @param event a taskEvent containing a job event.
     */
    public synchronized void update(TaskEvent event) {
        jobInfo = event.getJobEvent();
        tasks.get(event.getTaskId()).update(event);
    }

    /**
     * To update the content of this job with a jobInfo.
     *
     * @param jobInfo the jobInfo to set
     */
    public synchronized void update(JobEvent jobInfo) {
        this.jobInfo = jobInfo;

        if (jobInfo.getTaskStatusModify() != null) {
            for (TaskId id : tasks.keySet()) {
                tasks.get(id).setStatus(jobInfo.getTaskStatusModify().get(id));
            }
        }

        if (jobInfo.getTaskFinishedTimeModify() != null) {
            for (TaskId id : tasks.keySet()) {
                if (jobInfo.getTaskFinishedTimeModify().containsKey(id)) {
                    //a null send to a long setter throws a NullPointerException so, here is the fix
                    tasks.get(id).setFinishedTime(jobInfo.getTaskFinishedTimeModify().get(id));
                }
            }
        }
    }

    /**
     * To get the jobInfo
     *
     * @return the jobInfo
     */
    public JobEvent getJobInfo() {
        return jobInfo;
    }

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
     * @param job The internal job to be compared.
     * @return  a negative integer, zero, or a positive integer as this job
     *		is less than, equal to, or greater than the specified job.
     * 
     */
    public int compareTo(InternalJob job) {
        switch (currentSort) {
            case SORT_BY_DESCRIPTION:
                return (currentOrder == ASC_ORDER) ? (description.compareTo(job.description))
                        : (job.description.compareTo(description));
            case SORT_BY_NAME:
                return (currentOrder == ASC_ORDER) ? (name.compareTo(job.name)) : (job.name.compareTo(name));
            case SORT_BY_PRIORITY:
                return (currentOrder == ASC_ORDER) ? (jobInfo.getPriority().getPriority() - job.jobInfo
                        .getPriority().getPriority()) : (job.jobInfo.getPriority().getPriority() - jobInfo
                        .getPriority().getPriority());
            case SORT_BY_TYPE:
                return (currentOrder == ASC_ORDER) ? (getType().compareTo(job.getType())) : (job.getType()
                        .compareTo(getType()));
            case SORT_BY_OWNER:
                return (currentOrder == ASC_ORDER) ? (owner.compareTo(job.owner)) : (job.owner
                        .compareTo(owner));
            case SORT_BY_STATE:
                return (currentOrder == ASC_ORDER) ? (jobInfo.getState().compareTo(job.jobInfo.getState()))
                        : (job.jobInfo.getState().compareTo(jobInfo.getState()));
            case SORT_BY_PROJECT:
                return (currentOrder == ASC_ORDER) ? (getProjectName().compareTo(job.getProjectName()))
                        : (job.getProjectName().compareTo(getProjectName()));
            default:
                return (currentOrder == ASC_ORDER) ? (getId().compareTo(job.getId())) : (job.getId()
                        .compareTo(getId()));
        }
    }

    /**
     * Append a task to this job.
     *
     * @param task
     *            the task to add.
     * @return true if the task has been correctly added to the job, false if
     *         not.
     */
    public boolean addTask(InternalTask task) {
        task.setJobId(getId());

        if (task.isPreciousResult()) {
            preciousResults.add(task);
        }

        task.setId(TaskId.nextId(getId(), task.getName()));

        boolean result = (tasks.put(task.getId(), task) == null);

        if (result) {
            jobInfo.setTotalNumberOfTasks(jobInfo.getTotalNumberOfTasks() + 1);
        }

        return result;
    }

    /**
     * Start a new task will set some count and update dependencies if necessary.
     *
     * @param td the task which has just been started.
     */
    public void startTask(InternalTask td) {
        setNumberOfPendingTasks(getNumberOfPendingTask() - 1);
        setNumberOfRunningTasks(getNumberOfRunningTask() + 1);

        if (getState() == JobState.STALLED) {
            setState(JobState.RUNNING);
        }

        jobDescriptor.start(td.getId());
        td.setStatus(TaskState.RUNNING);
        td.setStartTime(System.currentTimeMillis());
        td.setExecutionHostName(td.getExecuterInformations().getHostName() + " (" +
            td.getExecuterInformations().getNodeName() + ")");
    }

    public void newWaitingTask() {
        setNumberOfPendingTasks(getNumberOfPendingTask() + 1);
        setNumberOfRunningTasks(getNumberOfRunningTask() - 1);
    }

    /**
     * Set this task in restart mode, it will set the task to pending state and change task count.
     *
     * @param task the task which has to be restarted.
     */
    public void reStartTask(InternalTask task) {

        jobDescriptor.reStart(task.getId());

        if (getState() == JobState.PAUSED) {
            task.setStatus(TaskState.PAUSED);
            HashMap<TaskId, TaskState> hts = new HashMap<TaskId, TaskState>();
            hts.put(task.getId(), task.getStatus());
            jobDescriptor.update(hts);
        } else {
            task.setStatus(TaskState.PENDING);
        }
    }

    /**
     * Terminate a task, change status, managing dependences
     *
     * @param errorOccured has an error occurred for this termination
     * @param taskId the task to terminate.
     * @return the taskDescriptor that has just been terminated.
     */
    public InternalTask terminateTask(boolean errorOccurred, TaskId taskId) {
        InternalTask descriptor = tasks.get(taskId);
        descriptor.setFinishedTime(System.currentTimeMillis());
        descriptor.setStatus(errorOccurred ? TaskState.FAULTY : TaskState.FINISHED);
        setNumberOfRunningTasks(getNumberOfRunningTask() - 1);
        setNumberOfFinishedTasks(getNumberOfFinishedTask() + 1);

        if ((getState() == JobState.RUNNING) && (getNumberOfRunningTask() == 0)) {
            setState(JobState.STALLED);
        }

        //terminate this task
        jobDescriptor.terminate(taskId);

        //creating list of status for the jobDescriptor
        HashMap<TaskId, TaskState> hts = new HashMap<TaskId, TaskState>();

        for (InternalTask td : tasks.values()) {
            hts.put(td.getId(), td.getStatus());
        }

        //updating job descriptor for eligible task
        jobDescriptor.update(hts);

        return descriptor;
    }

    /**
     * Simulate that a task have been started and terminated.
     * Used only by the recovery method in scheduler core.
     *
     * @param id the id of the task to start and terminate.
     */
    public void simulateStartAndTerminate(TaskId id) {
        jobDescriptor.start(id);
        jobDescriptor.terminate(id);
    }

    /**
     * Failed this job due to the given task failure.
     *
     * @param taskId the task that has been the cause to failure.
     * @param jobState type of the failure on this job. (failed/canceled)
     */
    public void failed(TaskId taskId, JobState jobState) {
        InternalTask descriptor = tasks.get(taskId);
        descriptor.setFinishedTime(System.currentTimeMillis());
        setFinishedTime(System.currentTimeMillis());
        setNumberOfPendingTasks(0);
        setNumberOfRunningTasks(0);
        descriptor.setStatus((jobState == JobState.FAILED) ? TaskState.FAILED : TaskState.FAULTY);
        setState(jobState);
        //terminate this job descriptor
        jobDescriptor.failed();

        //creating list of status
        HashMap<TaskId, TaskState> hts = new HashMap<TaskId, TaskState>();
        HashMap<TaskId, Long> htl = new HashMap<TaskId, Long>();

        for (InternalTask td : tasks.values()) {
            if (!td.getId().equals(taskId)) {
                if (td.getStatus() == TaskState.RUNNING) {
                    td.setStatus(TaskState.ABORTED);
                    td.setFinishedTime(System.currentTimeMillis());
                } else if (td.getStatus() != TaskState.FINISHED) {
                    td.setStatus(TaskState.NOT_STARTED);
                }
            }

            htl.put(td.getId(), td.getFinishedTime());
            hts.put(td.getId(), td.getStatus());
        }

        setTaskStatusModify(hts);
        setTaskFinishedTimeModify(htl);
    }

    /**
     * Get a task descriptor that is in the running task queue.
     * 
     * @param id the id of the task descriptor to retrieve.
     * @return the task descriptor associated to this id, or null if not running.
     */
    public TaskDescriptor getRunningTaskDescriptor(TaskId id) {
        return jobDescriptor.GetRunningTaskDescriptor(id);
    }

    /**
     * Set all properties following a job submitting.
     */
    public void submit() {
        setSubmittedTime(System.currentTimeMillis());
        setState(JobState.PENDING);
    }

    /**
     * Set all properties in order to start the job.
     * After this method and for better performances you may have to
     * set the taskStatusModify to "null" : setTaskStatusModify(null);
     */
    public void start() {
        setStartTime(System.currentTimeMillis());
        setNumberOfPendingTasks(getTotalNumberOfTasks());
        setNumberOfRunningTasks(0);
        setState(JobState.RUNNING);

        HashMap<TaskId, TaskState> taskState = new HashMap<TaskId, TaskState>();

        for (InternalTask td : getTasks()) {
            td.setStatus(TaskState.PENDING);
            taskState.put(td.getId(), TaskState.PENDING);
        }

        setTaskStatusModify(taskState);
    }

    /**
     * Set all properties in order to terminate the job.
     */
    public void terminate() {
        setState(JobState.FINISHED);
        setFinishedTime(System.currentTimeMillis());
    }

    /**
     * Paused every running and submitted tasks in this pending job.
     * After this method and for better performances you may have to
     * set the taskStatusModify to "null" : setTaskStatusModify(null);
     * 
     * @return true if the job has correctly been paused, false if not.
     */
    public boolean setPaused() {
        if (jobInfo.getState() == JobState.PAUSED) {
            return false;
        }

        jobInfo.setState(JobState.PAUSED);

        HashMap<TaskId, TaskState> hts = new HashMap<TaskId, TaskState>();

        for (InternalTask td : tasks.values()) {
            if ((td.getStatus() != TaskState.FINISHED) && (td.getStatus() != TaskState.RUNNING)) {
                td.setStatus(TaskState.PAUSED);
            }

            hts.put(td.getId(), td.getStatus());
        }

        jobDescriptor.update(hts);
        setTaskStatusModify(hts);

        return true;
    }

    /**
     * State of every paused tasks becomes pending or submitted in this pending job.
     * After this method and for better performances you may have to
     * set the taskStatusModify to "null" : setTaskStatusModify(null);
     * 
     * @return true if the job has correctly been unpaused, false if not.
     */
    public boolean setUnPause() {
        if (jobInfo.getState() != JobState.PAUSED) {
            return false;
        }

        if ((getNumberOfPendingTask() + getNumberOfRunningTask() + getNumberOfFinishedTask()) == 0) {
            jobInfo.setState(JobState.PENDING);
        } else if (getNumberOfRunningTask() == 0) {
            jobInfo.setState(JobState.STALLED);
        } else {
            jobInfo.setState(JobState.RUNNING);
        }

        HashMap<TaskId, TaskState> hts = new HashMap<TaskId, TaskState>();

        for (InternalTask td : tasks.values()) {
            if (jobInfo.getState() == JobState.PENDING) {
                td.setStatus(TaskState.SUBMITTED);
            } else if ((jobInfo.getState() == JobState.RUNNING) || (jobInfo.getState() == JobState.STALLED)) {
                if ((td.getStatus() != TaskState.FINISHED) && (td.getStatus() != TaskState.RUNNING)) {
                    td.setStatus(TaskState.PENDING);
                }
            }

            hts.put(td.getId(), td.getStatus());
        }

        jobDescriptor.update(hts);
        setTaskStatusModify(hts);

        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getId()
     */
    @Override
    public JobId getId() {
        return jobInfo.getJobId();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getPriority()
     */
    @Override
    public JobPriority getPriority() {
        return jobInfo.getPriority();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#setPriority(org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    @Override
    public void setPriority(JobPriority priority) {
        jobInfo.setPriority(priority);

        if (jobDescriptor != null) {
            jobDescriptor.setPriority(priority);
        }
    }

    /**
     * To get the tasks as an array list.
     *
     * @return the tasks
     */
    public ArrayList<InternalTask> getTasks() {
        return new ArrayList<InternalTask>(tasks.values());
    }

    /**
     * To get the tasks as a hash map.
     *
     * @return the tasks
     */
    public HashMap<TaskId, InternalTask> getHMTasks() {
        return tasks;
    }

    /**
     * To set the taskStatusModify
     *
     * @param taskStatusModify the taskStatusModify to set
     */
    public void setTaskStatusModify(HashMap<TaskId, TaskState> taskStatusModify) {
        jobInfo.setTaskStatusModify(taskStatusModify);
    }

    /**
     * To set the taskFinishedTimeModify
     *
     * @param taskFinishedTimeModify the taskFinishedTimeModify to set
     */
    public void setTaskFinishedTimeModify(HashMap<TaskId, Long> taskFinishedTimeModify) {
        jobInfo.setTaskFinishedTimeModify(taskFinishedTimeModify);
    }

    /**
     * To get the precious results of this job
     *
     * @return the precious results of this job
     */
    public Vector<InternalTask> getPreciousResults() {
        return preciousResults;
    }

    /**
     * To set the id
     *
     * @param id
     *            the id to set
     */
    public void setId(JobId id) {
        jobInfo.setJobId(id);
    }

    /**
     * To get the numberOfFinishedTask
     *
     * @return the numberOfFinishedTask
     */
    public int getNumberOfFinishedTask() {
        return jobInfo.getNumberOfFinishedTasks();
    }

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
     */
    public long getFinishedTime() {
        return jobInfo.getFinishedTime();
    }

    /**
     * To set the finishedTime
     *
     * @param finishedTime
     *            the finishedTime to set
     */
    public void setFinishedTime(long finishedTime) {
        jobInfo.setFinishedTime(finishedTime);
    }

    /**
     * To get the numberOfPendingTask
     *
     * @return the numberOfPendingTask
     */
    public int getNumberOfPendingTask() {
        return jobInfo.getNumberOfPendingTasks();
    }

    /**
     * To get the numberOfRunningTask
     *
     * @return the numberOfRunningTask
     */
    public int getNumberOfRunningTask() {
        return jobInfo.getNumberOfRunningTasks();
    }

    /**
     * To get the startTime
     *
     * @return the startTime
     */
    public long getStartTime() {
        return jobInfo.getStartTime();
    }

    /**
     * To set the startTime
     *
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(long startTime) {
        jobInfo.setStartTime(startTime);
    }

    /**
     * To get the totalNumberOfTasks
     *
     * @return the totalNumberOfTasks
     */
    public int getTotalNumberOfTasks() {
        return jobInfo.getTotalNumberOfTasks();
    }

    /**
     * Prepare tasks in order to be ready to be scheduled.
     * The task may have a consistent id and job event.
     */
    public synchronized void prepareTasks() {
        //get tasks
        ArrayList<InternalTask> sorted = getTasks();
        //re-init taskId count
        TaskId.initialize();
        //sort task according to the ID
        Collections.sort(sorted);
        tasks.clear();
        for (InternalTask td : sorted) {
            TaskId newId = TaskId.nextId(getId(), td.getName());
            td.setId(newId);
            td.setJobInfo(getJobInfo());
            tasks.put(newId, td);
        }
    }

    /**
     * To get the removedTime
     *
     * @return the removedTime
     */
    public long getRemovedTime() {
        return jobInfo.getRemovedTime();
    }

    /**
     * To get the submittedTime
     *
     * @return the submittedTime
     */
    public long getSubmittedTime() {
        return jobInfo.getSubmittedTime();
    }

    /**
     * To set the submittedTime
     *
     * @param submittedTime
     *            the submittedTime to set
     */
    public void setSubmittedTime(long submittedTime) {
        jobInfo.setSubmittedTime(submittedTime);
    }

    /**
     * To set the removedTime
     *
     * @param removedTime
     *            the removedTime to set
     */
    public void setRemovedTime(long removedTime) {
        jobInfo.setRemovedTime(removedTime);
    }

    /**
     * To set the numberOfFinishedTasks
     *
     * @param numberOfFinishedTasks the numberOfFinishedTasks to set
     */
    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        jobInfo.setNumberOfFinishedTasks(numberOfFinishedTasks);
    }

    /**
     * To set the numberOfPendingTasks
     *
     * @param numberOfPendingTasks the numberOfPendingTasks to set
     */
    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        jobInfo.setNumberOfPendingTasks(numberOfPendingTasks);
    }

    /**
     * To set the numberOfRunningTasks
     *
     * @param numberOfRunningTasks the numberOfRunningTasks to set
     */
    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        jobInfo.setNumberOfRunningTasks(numberOfRunningTasks);
    }

    /**
     * To get the jobDescriptor
     *
     * @return the jobDescriptor
     */
    public JobDescriptor getJobDescriptor() {
        return jobDescriptor;
    }

    /**
     * To set the jobDescriptor
     *
     * @param jobDescriptor the jobDescriptor to set
     */
    public void setJobDescriptor(JobDescriptor jobDescriptor) {
        this.jobDescriptor = jobDescriptor;
    }

    /**
     * To get the state of the job.
     *
     * @return the state of the job.
     */
    public JobState getState() {
        return jobInfo.getState();
    }

    /**
     * @param state the state to set
     */
    public void setState(JobState state) {
        jobInfo.setState(state);
    }

    /**
     * To get the owner of the job.
     *
     * @return the owner of the job.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * To set the owner of this job.
     *
     * @param owner the owner to set.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Returns the jobResult.
     *
     * @return the jobResult.
     */
    public JobResult getJobResult() {
        return jobResult;
    }

    /**
     * Sets the jobResult to the given jobResult value.
     *
     * @param jobResult the jobResult to set.
     */
    public void setJobResult(JobResult jobResult) {
        this.jobResult = jobResult;
    }

    /**
     * Get the next restart waiting time in millis.
     * 
     * @return the next restart waiting time in millis.
     */
    public long getNextWaitingTime(int executionNumber) {
        if (executionNumber <= 0) {
            //execution number is 0 or less, restart with the minimal amount of time
            return restartWaitingTimer;
        } else if (executionNumber > 10) {
            //execution timer exceed 10, restart after 60 seconds
            return 60 * 1000;
        } else {
            //else restart according to this function
            return (getNextWaitingTime(executionNumber - 1) + executionNumber * 1000);
        }
    }

    /**
     * Get the toBeRemoved property.
     *
     * @return the toBeRemoved property.
     */
    public boolean isToBeRemoved() {
        return jobInfo.isToBeRemoved();
    }

    /**
     * Set this job to the state toBeRemoved.
     */
    public void setToBeRemoved() {
        jobInfo.setToBeRemoved();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getName()
     */
    @Override
    public String getName() {
        if (getId() == null || getId().getReadableName().equals(JobId.DEFAULT_JOB_NAME)) {
            return super.getName();
        } else {
            return getId().getReadableName();
        }
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
    public boolean equals(Object o) {
        if (o instanceof InternalJob) {
            return getId().equals(((InternalJob) o).getId());
        }

        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getId() + "]";
    }
}
