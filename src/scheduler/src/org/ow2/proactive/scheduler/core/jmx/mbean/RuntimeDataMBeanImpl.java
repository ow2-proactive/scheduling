/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.jmx.mbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.utils.Tools;


/**
 * Implementation of the SchedulerRuntimeMBean interface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class RuntimeDataMBeanImpl extends StandardMBean implements RuntimeDataMBean {

    /** The Scheduler Started Time */
    private final long schedulerStartedTime;

    /** Current Scheduler status typed as scheduler event */
    protected SchedulerEvent schedulerStatus;

    /** The Scheduler clients */
    private final SchedulerUsers schedulerClients;

    /** Various counters changed on incoming events */
    protected int totalJobsCount;
    protected int pendingJobsCount;
    protected int runningJobsCount;
    protected int finishedJobsCount;
    protected int totalTasksCount;

    /** Maps for counting tasks per job */
    protected final Map<String, Integer> numberOfPendingTasks;
    protected final Map<String, Integer> numberOfRunningTasks;
    protected final Map<String, Integer> numberOfFinishedTasks;

    /** Variables representing the Key Performance Indicators for the SchedulerWrapper */
    private long meanJobPendingTime;
    private long meanJobExecutionTime;
    private long jobSubmittingPeriod;

    /** The counter fields for the KPI values */
    private long counterJobPendingTime;
    private long counterJobExecutionTime;
    private long counterJobArrivalTime;

    /** The cumulative Times */
    private long cumulativePendingTime;
    private long cumulativeExecutionTime;
    private long cumulativeArrivalTime;

    /** The previous submitted time, explained later */
    private long previousSubmittedTime;

    /**
     * Fields to keep the informations need for the Operations to get the Key Performance Indicator values
     * The first two are references to the Map of pending and running time for each job
     */
    private final Map<String, Long> jobPendingTimeMap;
    private final Map<String, Long> jobRunningTimeMap;

    /** The task timings list and the mean timings for the tasks of a given job */
    private final Map<String, List<Long>> taskPendingTimeMap;
    private final Map<String, List<Long>> taskRunningTimeMap;
    private final Map<String, Long> meanTaskPendingTimeMap;
    private final Map<String, Long> meanTaskRunningTimeMap;

    /** Map of the number of nodes used by the jobs */
    private final Map<String, Integer> nodesUsedByJobMap;
    /** List of execution host for each task of each job */
    private final Map<String, Set<String>> executionHostNames;

    /**
     * Empty constructor required by JMX
     */
    public RuntimeDataMBeanImpl() throws NotCompliantMBeanException {
        super(RuntimeDataMBean.class);

        // This MBean is instantiated by scheduler core
        this.schedulerStartedTime = System.currentTimeMillis();

        // The Scheduler clients
        this.schedulerClients = new SchedulerUsers();

        // Initialize count maps
        this.numberOfPendingTasks = new HashMap<String, Integer>();
        this.numberOfRunningTasks = new HashMap<String, Integer>();
        this.numberOfFinishedTasks = new HashMap<String, Integer>();

        // Initialize count maps
        this.jobPendingTimeMap = new HashMap<String, Long>();
        this.jobRunningTimeMap = new HashMap<String, Long>();
        this.taskPendingTimeMap = new HashMap<String, List<Long>>();
        this.taskRunningTimeMap = new HashMap<String, List<Long>>();
        this.meanTaskPendingTimeMap = new HashMap<String, Long>();
        this.nodesUsedByJobMap = new HashMap<String, Integer>();
        this.meanTaskRunningTimeMap = new HashMap<String, Long>();
        this.executionHostNames = new HashMap<String, Set<String>>();
    }

    /**
     * Recover this JMX Bean
     *
     * @param jobList the list of job to be recovered
     */
    public void recover(final Set<JobState> jobList) {
        if (jobList == null) {
            return;
        }
        for (final JobState js : jobList) {
            this.totalJobsCount++;
            this.totalTasksCount += js.getTotalNumberOfTasks();
            final String jobId = js.getId().value();
            this.numberOfRunningTasks.put(jobId, js.getNumberOfRunningTasks());
            this.numberOfFinishedTasks.put(jobId, js.getNumberOfFinishedTasks());
            switch (js.getStatus()) {
                case PENDING:
                case PAUSED:
                    numberOfPendingTasks.put(jobId, js.getTotalNumberOfTasks());
                    pendingJobsCount++;
                    break;
                case RUNNING:
                case STALLED:
                    numberOfPendingTasks.put(jobId, js.getNumberOfPendingTasks());
                    runningJobsCount++;
                    break;
                case CANCELED:
                case FAILED:
                case FINISHED:
                case KILLED:
                    numberOfPendingTasks.put(jobId, 0);
                    finishedJobsCount++;
                    break;
            }
        }
    }

    // EVENT MANAGEMENT

    /**
     * Methods for dispatching events
     *
     * Call the MBean event for the related Scheduler Updated event type
     *
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     * @param eventType the type of the received event
     */
    public void schedulerStateUpdatedEvent(final SchedulerEvent eventType) {
        this.schedulerStatus = eventType;
    }

    /**
     * Call the MBean event for the related Job Updated event type
     *
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     * @param notification data containing job info
     */
    public void jobStateUpdatedEvent(final NotificationData<JobInfo> notification) {
        final JobInfo jobInfo = notification.getData();
        switch (notification.getEventType()) {
            case JOB_PAUSED:
                this.runningJobsCount--;
                break;
            case JOB_RESUMED:
                this.runningJobsCount++;
                break;
            case JOB_PENDING_TO_RUNNING:
                jobPendingToRunningEvent(jobInfo);
                break;
            case JOB_RUNNING_TO_FINISHED:
                // Check for killed job (see SCHEDULING-776)            	
                if (jobInfo.getStartTime() == -1) {
                    // The call to jobPendingToFinishedEvent was added since 
                    jobPendingToFinishedEvent(jobInfo);
                } else {
                    jobRunningToFinishedEvent(jobInfo);
                }
                break;
            case JOB_REMOVE_FINISHED:
                jobRemoveFinishedEvent(jobInfo);
        }
    }

    /**
     * Call the MBean event for the related Task Updated event type
     *
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     * @param notification data containing task info
     */
    public void taskStateUpdatedEvent(final NotificationData<TaskInfo> notification) {
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
                taskPendingToRunningEvent(notification.getData());
                break;
            case TASK_RUNNING_TO_FINISHED:
                taskRunningToFinishedEvent(notification.getData());
        }
    }

    /**
     * Each time that there`s an event is done the related update
     *
     * This is a canonical event to calculate the meanJobPendingTime KPI
     *
     * @param job info
     */
    protected void jobPendingToRunningEvent(final JobInfo info) {
        // Update the status
        this.pendingJobsCount--;
        this.runningJobsCount++;

        // After a given number of Jobs we can have a good current estimation of the mean Job Pending Time
        // calculated each time dividing the accumulator time by the counter.        

        // Call the private method to calculate the mean pending time
        // Calculate the Pending time for this Job (startTime - submittedTime)
        final long jobPendingTime = (info.getStartTime() - info.getSubmittedTime());
        calculateMeanJobPendingTime(info.getJobId().value(), jobPendingTime);
    }

    protected void jobPendingToFinishedEvent(final JobInfo info) {
        final String jobId = info.getJobId().value();
        // Decrement pending job count
        this.pendingJobsCount--;
        // Call the private method to calculate the mean pending time
        // Calculate the Pending time for this Job (finishTime - submittedTime)
        final long jobPendingTime = (info.getFinishedTime() - info.getSubmittedTime());
        // Compute pending mean  
        calculateMeanJobPendingTime(jobId, jobPendingTime);
        // The job is finished
        this.finishedJobsCount++;
        // No more pending tasks
        this.numberOfPendingTasks.put(jobId, 0);
        // 
        // Add the meanTaskPendingTime for this job to the meanTaskPendingTimeMap in position [jobId]
        // long mean = this.computeMean(this.taskPendingTimeMap, jobId);
        // this.meanTaskPendingTimeMap.put(jobId, mean);
    }

    /**
     * the job is no more managed, it is removed from scheduler
     *
     * @param info the job's information
     */
    protected void jobRemoveFinishedEvent(JobInfo info) {
        this.finishedJobsCount--;
        this.totalJobsCount--;
        // For each task of the Job decrement the number of finished tasks and the total number of tasks
        this.totalTasksCount -= info.getTotalNumberOfTasks();
        String jobId = info.getJobId().value();
        this.numberOfPendingTasks.remove(jobId);
        this.numberOfRunningTasks.remove(jobId);
        this.numberOfFinishedTasks.remove(jobId);
    }

    /**
     * This is a canonical event to calculate the meanJobExecutionTime KPI
     *
     * @param info the job's information
     */
    protected void jobRunningToFinishedEvent(final JobInfo info) {
        this.runningJobsCount--;
        this.finishedJobsCount++;
        final String jobId = info.getJobId().value();
        this.numberOfPendingTasks.put(jobId, info.getNumberOfPendingTasks());
        this.numberOfRunningTasks.put(jobId, 0);
        this.numberOfFinishedTasks.put(jobId, info.getNumberOfFinishedTasks());
        // Call the private method to compute the mean execution time
        computeMeanJobExecutionTime(info);
        // Add the meanTaskPendingTime for this job to the meanTaskPendingTimeMap in position [jobId]
        long mean = this.computeMean(this.taskPendingTimeMap, jobId);
        this.meanTaskPendingTimeMap.put(jobId, mean);
        // Add the meanTaskRunningTime for this job to the meanTaskRunningTimeMap in position [jobId]
        mean = this.computeMean(this.taskRunningTimeMap, jobId);
        this.meanTaskRunningTimeMap.put(jobId, mean);
        // Put the number of nodes used by the Job and put it in the nodesUsedByJobMap in position [jobId]
        final Set<String> hostnames = this.executionHostNames.get(jobId);
        if (hostnames == null) {
            this.nodesUsedByJobMap.put(jobId, 0);
        } else {
            this.nodesUsedByJobMap.put(jobId, hostnames.size());
        }
    }

    /**
     * This is a canonical event to calculate the meanJobArrivalTime KPI
     *
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     * @param jobState the state of the job
     */
    public void jobSubmittedEvent(final JobState jobState) {
        this.totalJobsCount++;
        this.pendingJobsCount++;
        final int tasks = jobState.getTotalNumberOfTasks();
        // For each task of the Job increment the number of pending tasks and the total number of tasks
        this.totalTasksCount += tasks;
        final String jobId = jobState.getId().value();
        this.numberOfPendingTasks.put(jobId, tasks);
        this.numberOfRunningTasks.put(jobId, 0);
        this.numberOfFinishedTasks.put(jobId, 0);

        // Call the private method to calculate the mean arrival time
        calculateJobSubmittingPeriod(jobState.getSubmittedTime());
    }

    /**
     * Task pending to running event
     *
     * @param info task's information
     */
    protected void taskPendingToRunningEvent(TaskInfo info) {
        String jobId = info.getJobId().value();
        this.numberOfPendingTasks.put(jobId, this.numberOfPendingTasks.get(jobId) - 1);
        this.numberOfRunningTasks.put(jobId, this.numberOfRunningTasks.get(jobId) + 1);
        // Calculate the Pending time for this Task (taskStartTime - jobSubmittedTime)
        long taskPendingTime = (info.getStartTime() - info.getJobInfo().getSubmittedTime());
        // Add the taskPendingTime for this task to the taskPendingTimeMap in position [jobTaskId]
        if (this.taskPendingTimeMap.get(jobId) == null) {
            this.taskPendingTimeMap.put(jobId, new ArrayList<Long>());
        }
        this.taskPendingTimeMap.get(jobId).add(taskPendingTime);
    }

    /**
     * Task Running To Finished Event
     *
     * @param info task's information
     */
    protected void taskRunningToFinishedEvent(TaskInfo info) {
        String jobId = info.getJobId().value();
        this.numberOfRunningTasks.put(jobId, this.numberOfRunningTasks.get(jobId) - 1);
        this.numberOfFinishedTasks.put(jobId, this.numberOfFinishedTasks.get(jobId) + 1);
        // Calculate the Pending time for this Task (taskFinishedTime - taskStartTime)
        long taskRunningTime = (info.getFinishedTime() - info.getStartTime());
        // Add the taskRunningTime for this task to the taskRunningTimeMap in position [jobTaskId]
        if (this.taskRunningTimeMap.get(jobId) == null) {
            this.taskRunningTimeMap.put(jobId, new ArrayList<Long>());
        }
        this.taskRunningTimeMap.get(jobId).add(taskRunningTime);
        // Put the host name in the Map<jobTaskId,hostNames> of nodes used by the job
        if (this.executionHostNames.get(jobId) == null) {
            this.executionHostNames.put(jobId, new HashSet<String>());
        }
        this.executionHostNames.get(jobId).add(info.getExecutionHostName());
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(final NotificationData<UserIdentification> notificationData) {
        this.schedulerClients.update(notificationData.getData());
    }

    // ATTRIBUTES TO CONTROL

    /**
     * Returns the connected clients count
     * @return current number of connected users
     */
    public int getConnectedUsersCount() {
        return this.schedulerClients.getUsers().size();
    }

    /**
     * @return current number of finished jobs
     */
    public int getFinishedJobsCount() {
        return this.finishedJobsCount;
    }

    /**
     * @return current number of finished tasks
     */
    public int getFinishedTasksCount() {
        int total = 0;
        for (int noft : this.numberOfFinishedTasks.values()) {
            total += noft;
        }
        return total;
    }

    public int getPendingJobsCount() {
        return this.pendingJobsCount;
    }

    /**
     * @return current number of pending tasks
     */
    public int getPendingTasksCount() {
        int total = 0;
        for (int nopt : this.numberOfPendingTasks.values()) {
            total += nopt;
        }
        return total;
    }

    /**
     * @return current number of running jobs
     */
    public int getRunningJobsCount() {
        return this.runningJobsCount;
    }

    /**
     * @return current number of running tasks
     */
    public int getRunningTasksCount() {
        int total = 0;
        for (int nort : this.numberOfRunningTasks.values()) {
            total += nort;
        }
        return total;
    }

    /**
     * @return current status of the Scheduler as String
     */
    public String getStatus() {
        return this.schedulerStatus.toString();
    }

    /**
     * @return current number of jobs submitted to the Scheduler
     */
    public int getTotalJobsCount() {
        return this.totalJobsCount;
    }

    /**
     * @return current number of tasks submitted to the Scheduler
     */
    public int getTotalTasksCount() {
        return this.totalTasksCount;
    }

    // PRIVATE METHODS FOR CALCULATING KPIs

    /**

     * @param info the job information
     */
    private void calculateMeanJobPendingTime(final String jobId, final long jobPendingTime) {
        // Increment the cumulative pending time
        this.cumulativePendingTime += jobPendingTime;
        // Increment the related counter
        this.counterJobPendingTime++;
        // Update the mean pending time dividing the cumulative pending time by the related counter
        this.meanJobPendingTime = (this.cumulativePendingTime / this.counterJobPendingTime);
        // Add the jobPendingTime for this job to the jobPendingTimeMap in position [jobId]
        this.jobPendingTimeMap.put(jobId, jobPendingTime);
    }

    /**
     * After a given number of Jobs we can have a good current estimation of the mean Job Execution Time
     * calculated each time dividing the accumulator time by the counter.
     *
     * @param info the job information
     */
    private void computeMeanJobExecutionTime(final JobInfo info) {
        // Calculate the Running time for this Job (finishedTime - startTime)
        long jobRunningTime = (info.getFinishedTime() - info.getStartTime());
        // Increment the cumulative execution time
        this.cumulativeExecutionTime += jobRunningTime;
        // Increment the related counter
        this.counterJobExecutionTime++;
        // Update the mean execution time dividing the cumulative execution time by the related counter
        this.meanJobExecutionTime = (this.cumulativeExecutionTime / this.counterJobExecutionTime);
        // Add the jobRunningTime for this job to the jobRunningTimeMap in position [jobId]
        this.jobRunningTimeMap.put(info.getJobId().value(), jobRunningTime);
    }

    /**
     * After a given number of Jobs we can have a good current estimation of the mean Job Arrival Time
     * calculated each time dividing the accumulator time by the counter.
     *
     * @param info the job information
     */
    private void calculateJobSubmittingPeriod(final long jobSubmittedTime) {
        // Calculate the arrival time for this Job (currentSubmittedTime - previousSubmittedTime)
        // Only the first time we have this event, we set the cumulative arrival time with the scheduler started time
        // Otherwise we set it with the previous submitted time
        if (this.counterJobArrivalTime == 0) {
            this.cumulativeArrivalTime = (jobSubmittedTime - this.schedulerStartedTime);
        } else {
            this.cumulativeArrivalTime += (jobSubmittedTime - this.previousSubmittedTime);
        }
        // Increments the related counter
        this.counterJobArrivalTime++;
        // Update the mean arrival time dividing the cumulative arrival time by the related counter
        this.jobSubmittingPeriod = (this.cumulativeArrivalTime / this.counterJobArrivalTime);
        // This is the previous Submitted Time for the next time that happens this event
        this.previousSubmittedTime = jobSubmittedTime;

    }

    /**
     * Method to compute the mean of the values on a given map make of all the job and task keys
     * with key in the form <jobId taskId>
     *
     * @param map
     * @return a long representation of the mean
     */
    private long computeMean(final Map<String, List<Long>> map, final String jobId) {
        final int size = map.size();
        if (size == 0) {
            return 0;
        }
        // Initialize the mean for the given job
        long mean = 0;
        for (final Long value : map.get(jobId)) {
            mean += value;
        }
        return mean / size;
    }

    // ATTRIBUTES TO CONTROL

    /**
     * Getter methods for the KPI values
     *
     * @return current mean job pending time as integer
     */
    public int getMeanJobPendingTime() {
        return (int) this.meanJobPendingTime;
    }

    /**
     * @return current mean job execution time as integer
     */
    public int getMeanJobExecutionTime() {
        return (int) this.meanJobExecutionTime;
    }

    /**
     * @return current mean job submitting period as integer
     */
    public int getJobSubmittingPeriod() {
        return (int) this.jobSubmittingPeriod;
    }

    // UTILITY METHODS

    /**
     * Getter methods for KPI values as String
     *
     * @return current mean job pending time as formatted duration
     */
    public String getFormattedMeanJobPendingTime() {
        return Tools.getFormattedDuration(0, this.meanJobPendingTime);
    }

    /**
     * @return current mean job execution time as formatted duration
     */
    public String getFormattedMeanJobExecutionTime() {
        return Tools.getFormattedDuration(0, this.meanJobExecutionTime);
    }

    /**
     * @return current mean job submitting period as formatted duration
     */
    public String getFormattedJobSubmittingPeriod() {
        return Tools.getFormattedDuration(0, this.jobSubmittingPeriod);
    }

    // MBEAN OPERATIONS

    /**
     * This method represents a possible Operation to Invoke on the MBean.
     * It gives the pending time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the pending time for the given job
     */
    public long getJobPendingTime(final String jobId) {
        final Long res = this.jobPendingTimeMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return res;
    }

    /**
     * This method gives the running time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the running time for the given job
     */
    public long getJobRunningTime(final String jobId) {
        final Long res = this.jobRunningTimeMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return res;
    }

    /**
     * This method gives the mean task pending time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task pending time for the given job
     */
    public long getMeanTaskPendingTime(final String jobId) {
        final Long res = this.meanTaskPendingTimeMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return res;
    }

    /**
     * This method gives the mean task running time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task running time for the given job
     */
    public long getMeanTaskRunningTime(final String jobId) {
        final Long res = this.meanTaskRunningTimeMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return res;
    }

    /**
     * This method gives the total number of nodes used by a given Job
     *
     * @param jobId, the id of the Job to check
     * @return the total number of nodes used by the given job
     */
    public int getTotalNumberOfNodesUsed(final String jobId) {
        final Integer res = this.nodesUsedByJobMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return res;
    }

    // UTILITY OPERATIONS

    /**
     * This method represents a possible Operation to Invoke on the MBean.
     * It gives the pending time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a formatted representation of the duration of the pending time for the given job.
     */
    public String getFormattedJobPendingTime(final String jobId) {
        final Long res = this.jobPendingTimeMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return Tools.getFormattedDuration(0, res);
    }

    /**
     * This method gives the running time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a formatted representation of the duration of the running time for the given job
     */
    public String getFormattedJobRunningTime(final String jobId) {
        final Long res = this.jobRunningTimeMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return Tools.getFormattedDuration(0, res);
    }

    /**
     * This method gives the mean task pending time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a formatted representation of the duration of the mean task pending time for the given job
     */
    public String getFormattedMeanTaskPendingTime(final String jobId) {
        final Long res = this.meanTaskPendingTimeMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return Tools.getFormattedDuration(0, res);
    }

    /**
     * This method gives the mean task running time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a formatted representation as long of the duration of the mean task running time for the given job.
     */
    public String getFormattedMeanTaskRunningTime(final String jobId) {
        final Long res = this.meanTaskRunningTimeMap.get(jobId);
        if (res == null) {
            throw new RuntimeException("Unknown jobId: " + jobId);
        }
        return Tools.getFormattedDuration(0, res);
    }
}
