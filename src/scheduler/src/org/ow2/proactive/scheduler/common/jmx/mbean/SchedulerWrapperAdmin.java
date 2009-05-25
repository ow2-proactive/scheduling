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
package org.ow2.proactive.scheduler.common.jmx.mbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.Tools;


/**
 * This class represents a Managed Bean to allow the management of the ProActive Scheduler 
 * following the JMX standard for management.
 * It provides some attributes and some statistics indicators.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SchedulerWrapperAdmin extends SchedulerWrapperAnonym implements SchedulerWrapperAdminMBean,
        SchedulerEventListener {
    /** Scheduler logger device */
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerLoggers.FRONTEND);

    /** Scheduler current state */
    private SchedulerStatus schedulerStatus = SchedulerStatus.STOPPED;

    /** Variables representing the attributes of the SchedulerWrapperMBean */
    private int totalNumberOfJobs = 0;

    private int totalNumberOfTasks = 0;

    private int numberOfPendingJobs = 0;

    private int numberOfRunningJobs = 0;

    private int numberOfFinishedJobs = 0;

    private int numberOfPendingTasks = 0;

    private int numberOfRunningTasks = 0;

    private int numberOfFinishedTasks = 0;

    /** Number of Connected Users */
    private int numberOfConnectedUsers = 0;

    /** Variables representing the Key Performance Indicators for the SchedulerWrapper */
    private long meanJobPendingTime = 0;

    private long meanJobExecutionTime = 0;

    private long jobSubmittingPeriod = 0;

    /** The counter fields for the KPI values */
    private long counterJobPendingTime = 0;

    private long counterJobExecutionTime = 0;

    private long counterJobArrivalTime = 0;

    /** The cumulative Times */
    private long cumulativePendingTime = 0;

    private long cumulativeExecutionTime = 0;

    private long cumulativeArrivalTime = 0;

    /** The previous submitted time, explained later */
    private long previousSubmittedTime = 0;

    /** The Scheduler Started Time */
    private long schedulerStartedTime = 0;

    /** Sequence number for Notifications */
    //private long sequenceNumber = 1;
    /** 
     * Fields to keep the informations need for the Operations to get the Key Performance Indicator values 
     * The first two are references to the Map of pending and running time for each job
     */
    private HashMap<String, Long> jobPendingTimeMap = new HashMap<String, Long>();
    private HashMap<String, Long> jobRunningTimeMap = new HashMap<String, Long>();
    /** The task timings list and the mean timings for the tasks of a given job */
    private HashMap<String, Long> taskPendingTimeMap = new HashMap<String, Long>();
    private HashMap<String, Long> taskRunningTimeMap = new HashMap<String, Long>();
    private HashMap<String, Long> meanTaskPendingTimeMap = new HashMap<String, Long>();
    private HashMap<String, Long> meanTaskRunningTimeMap = new HashMap<String, Long>();
    /** Map of the number of nodes used by the jobs */
    private HashMap<String, Integer> nodesUsedByJobMap = new HashMap<String, Integer>();
    /** List of execution host for each task of each job */
    private HashMap<String, String> executionHostNames = new HashMap<String, String>();

    /**
     * Empty constructor required by JMX
     */
    public SchedulerWrapperAdmin() {
        /* Empty Constructor required by JMX */
    }

    // ---------------------- EVENT MANAGEMENT ----------------------------

    /**
     * Methods for dispatching events
     *  
     * Call the MBean event for the related Scheduler Updated event type
     *
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     * @param eventType the type of the received event 
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        switch (eventType) {
            case STARTED:
                this.schedulerStatus = SchedulerStatus.STARTED;
                // Set the scheduler started time
                setSchedulerStartedTime(System.currentTimeMillis());
                break;
            case STOPPED:
                this.schedulerStatus = SchedulerStatus.STOPPED;
                break;
            case PAUSED:
                this.schedulerStatus = SchedulerStatus.PAUSED;
                break;
            case FROZEN:
                this.schedulerStatus = SchedulerStatus.FROZEN;
                break;
            case RESUMED:
                this.schedulerStatus = SchedulerStatus.STARTED;
                break;
            case SHUTTING_DOWN:
                this.schedulerStatus = SchedulerStatus.SHUTTING_DOWN;
                break;
            case SHUTDOWN:
                this.schedulerStatus = SchedulerStatus.STOPPED;
                break;
            case KILLED:
                this.schedulerStatus = SchedulerStatus.KILLED;
                break;
            case RM_DOWN:
            case RM_UP:
            case POLICY_CHANGED:
                break;
        }
    }

    /**
     * Call the MBean event for the related Job Updated event type
     *
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     * @param notification data containing job info
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_PAUSED:
                this.numberOfRunningJobs--;
                break;
            case JOB_RESUMED:
                this.numberOfRunningJobs++;
                break;
            case JOB_PENDING_TO_RUNNING:
                jobPendingToRunningEvent(notification.getData());
                break;
            case JOB_RUNNING_TO_FINISHED:
                jobRunningToFinishedEvent(notification.getData());
                break;
            case JOB_REMOVE_FINISHED:
                jobRemoveFinishedEvent(notification.getData());
                break;
            case JOB_CHANGE_PRIORITY:
                break;
        }
    }

    /**
     * Call the MBean event for the related Task Updated event type
     * 
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     * @param notification data containing task info
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
                taskPendingToRunningEvent(notification.getData());
                break;
            case TASK_RUNNING_TO_FINISHED:
                taskRunningToFinishedEvent(notification.getData());
                break;
            case TASK_WAITING_FOR_RESTART:
                break;
        }
    }

    /**
     * Each time that there`s an event is done the related update
     * 
     * This is a canonical event to calculate the meanJobPendingTime KPI
     * 
     * @param job info
     */
    private void jobPendingToRunningEvent(JobInfo info) {
        // Update the status
        this.numberOfPendingJobs--;
        this.numberOfRunningJobs++;
        // Call the private method to calculate the mean pending time
        calculateMeanJobPendingTime(info);
    }

    /**
     * the job is no more managed, it is removed from scheduler
     * 
     * @param info the job's information
     */
    private void jobRemoveFinishedEvent(JobInfo info) {
        this.numberOfFinishedJobs--;
        this.totalNumberOfJobs--;
        // For each task of the Job decrement the number of finished tasks and the total number of tasks
        for (int i = 0; i < info.getTotalNumberOfTasks(); i++) {
            this.totalNumberOfTasks--;
            this.numberOfFinishedTasks--;
        }
    }

    /**
     * This is a canonical event to calculate the meanJobExecutionTime KPI
     * 
     * @param info the job's information
     */
    private void jobRunningToFinishedEvent(JobInfo info) {
        this.numberOfRunningJobs--;
        this.numberOfFinishedJobs++;
        // Call the private method to calculate the mean execution time
        calculateMeanJobExecutionTime(info);
        // Add the meanTaskPendingTime for this job to the meanTaskPendingTimeMap in position [jobId]
        long mean = this.calculateMean(this.taskPendingTimeMap, info.getJobId().value());
        this.meanTaskPendingTimeMap.put(info.getJobId().value(), mean);
        // Add the meanTaskRunningTime for this job to the meanTaskRunningTimeMap in position [jobId]
        mean = this.calculateMean(this.taskRunningTimeMap, info.getJobId().value());
        this.meanTaskRunningTimeMap.put(info.getJobId().value(), mean);
        /*
         *  Calculate the number of nodes used by the Job and put it in the nodesUsedByJobMap in position [jobId]
         */
        Iterator<String> valuesIterator = this.executionHostNames.values().iterator();
        Iterator<String> keyIterator = this.executionHostNames.keySet().iterator();
        // Initialize the hostNames Array for the given job 
        ArrayList<String> hostNames = new ArrayList<String>();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            String value = valuesIterator.next();
            // If is the given job, add the host name to the list of nodes used by the job
            if (key.startsWith(info.getJobId().value())) {
                // Add the node name only if this node has not been used already by the given job
                if (!hostNames.contains(value)) {
                    hostNames.add(value);
                }
            }
        }
        this.nodesUsedByJobMap.put(info.getJobId().value(), hostNames.size());
    }

    /**
     * This is a canonical event to calculate the meanJobArrivalTime KPI
     * 
     * @param job the state of the job
     */
    public void jobSubmittedEvent(JobState job) {
        this.totalNumberOfJobs++;
        this.numberOfPendingJobs++;
        // For each task of the Job increment the number of pending tasks and the total number of tasks
        for (int i = 0; i < job.getTotalNumberOfTasks(); i++) {
            this.totalNumberOfTasks++;
            this.numberOfPendingTasks++;
        }
        // Call the private method to calculate the mean arrival time
        calculateJobSubmittingPeriod(job.getJobInfo());
    }

    /**
     * Task pending to running event
     * 
     * @param info task's information
     */
    private void taskPendingToRunningEvent(TaskInfo info) {
        this.numberOfPendingTasks--;
        this.numberOfRunningTasks++;
        // Calculate the Pending time for this Task (taskStartTime - jobSubmittedTime)
        long taskPendingTime = (info.getStartTime() - info.getJobInfo().getSubmittedTime());
        // Add the taskPendingTime for this task to the taskPendingTimeMap in position [jobTaskId]
        this.taskPendingTimeMap.put(info.getJobId() + " " + info.getTaskId().toString(), taskPendingTime);
    }

    /**
     * Task Running To Finished Event
     * 
     * @param info task's information
     */
    private void taskRunningToFinishedEvent(TaskInfo info) {
        this.numberOfRunningTasks--;
        this.numberOfFinishedTasks++;
        // Calculate the Pending time for this Task (taskFinishedTime - taskStartTime)
        long taskRunningTime = (info.getFinishedTime() - info.getStartTime());
        // Add the taskRunningTime for this task to the taskRunningTimeMap in position [jobTaskId]
        this.taskRunningTimeMap.put(info.getJobId() + " " + info.getTaskId().toString(), taskRunningTime);
        /*
         *  Put the host name in the Map<jobTaskId,hostName> of nodes used by the job
         *  jobTaskId is a String made like (jobId taskId)
         */
        this.executionHostNames.put(info.getJobId() + " " + info.getTaskId().toString(), info
                .getExecutionHostName());
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notificationData) {
        // It can be an update to remove or to add a User
        if (notificationData.getData().isToRemove()) {
            this.numberOfConnectedUsers--;
        } else {
            this.numberOfConnectedUsers++;
        }
    }

    // PRIVATE METHODS FOR CALCULATING KPIs

    /**  
     * After a given number of Jobs we can have a good current estimation of the mean Job Pending Time
     * calculated each time dividing the accumulator time by the counter.
     * 
     * @param job info
     */
    private void calculateMeanJobPendingTime(JobInfo info) {
        // Calculate the Pending time for this Job (startTime - submittedTime)
        long jobPendingTime = (info.getStartTime() - info.getSubmittedTime());
        // Increment the cumulative pending time
        this.cumulativePendingTime += jobPendingTime;
        // Increment the related counter
        this.counterJobPendingTime++;
        // Update the mean pending time dividing the cumulative pending time by the related counter
        this.meanJobPendingTime = (this.cumulativePendingTime / this.counterJobPendingTime);
        // Add the jobPendingTime for this job to the jobPendingTimeMap in position [jobId]
        this.jobPendingTimeMap.put(info.getJobId().value(), jobPendingTime);
    }

    /**
     * After a given number of Jobs we can have a good current estimation of the mean Job Execution Time
     * calculated each time dividing the accumulator time by the counter.
     * 
     * @param job info
     */
    private void calculateMeanJobExecutionTime(JobInfo info) {
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
     * @param job info
     */
    private void calculateJobSubmittingPeriod(JobInfo info) {
        // Calculate the arrival time for this Job (currentSubmittedTime - previousSubmittedTime)
        // Only the first time we have this event, we set the cumulative arrival time with the scheduler started time
        // Otherwise we set it with the previous submitted time
        if (this.counterJobArrivalTime == 0) {
            this.cumulativeArrivalTime = (info.getSubmittedTime() - this.schedulerStartedTime);
        } else {
            this.cumulativeArrivalTime += (info.getSubmittedTime() - this.previousSubmittedTime);
        }
        // Increments the related counter
        this.counterJobArrivalTime++;
        // Update the mean arrival time dividing the cumulative arrival time by the related counter
        this.jobSubmittingPeriod = (this.cumulativeArrivalTime / this.counterJobArrivalTime);
        // This is the previous Submitted Time for the next time that happens this event
        this.previousSubmittedTime = info.getSubmittedTime();

    }

    /**
     * Method to calculate the mean of the values on a given map make of all the job and task keys
     * with key in the form <jobId taskId>
     * 
     * @param an HashMap<String,Long>
     * @return a long representation of the mean
     */
    private long calculateMean(HashMap<String, Long> map, String jobId) {
        Iterator<Long> valuesIterator = map.values().iterator();
        Iterator<String> keyIterator = map.keySet().iterator();
        // Initialize the mean for the given job 
        long mean = 0;
        int counter = 0;
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            long value = valuesIterator.next();
            // If is the given job sum the value to the mean and increment the number of the tasks
            // of the given job 
            if (key.startsWith(jobId)) {
                // Remove the key for the tasks of the given job cause we don`t need it anymore
                //this.taskPendingTimeMap.remove(key);
                //this.taskRunningTimeMap.remove(key);
                mean = mean + value;
                counter++;
            }
        }
        // The final mean
        mean = mean / counter;
        return mean;
    }

    /**
     * Set the Scheduler Started Time
     *
     * @param time represented as a long
     */
    private void setSchedulerStartedTime(long time) {
        this.schedulerStartedTime = time;
    }

    // ATTRIBUTES TO CONTROL

    /**
     * The following methods represent the Attributes that is possible to monitor from the MBean
     * 
     * Methods to get the values of the attributes of the MBean
     * 
     * @return current number of connected users
     */
    public int getNumberOfConnectedUsers() {
        return this.numberOfConnectedUsers;
    }

    /**
     * @return current number of finished jobs
     */
    public int getNumberOfFinishedJobs() {
        return this.numberOfFinishedJobs;
    }

    /**
     * @return current number of finished tasks
     */
    public int getNumberOfFinishedTasks() {
        return this.numberOfFinishedTasks;
    }

    public int getNumberOfPendingJobs() {
        return this.numberOfPendingJobs;
    }

    /**
     * @return current number of pending tasks
     */
    public int getNumberOfPendingTasks() {
        return this.numberOfPendingTasks;
    }

    /**
     * @return current number of running jobs
     */
    public int getNumberOfRunningJobs() {
        return this.numberOfRunningJobs;
    }

    /**
     * @return current number of running tasks
     */
    public int getNumberOfRunningTasks() {
        return this.numberOfRunningTasks;
    }

    /**
     * @return current state of the Scheduler
     */
    public String getSchedulerStatus() {
        return this.schedulerStatus.toString();
    }

    /**
     * @return current number of jobs submitted to the Scheduler
     */
    public int getTotalNumberOfJobs() {
        return this.totalNumberOfJobs;
    }

    /**
     * @return current number of tasks submitted to the Scheduler
     */
    public int getTotalNumberOfTasks() {
        return this.totalNumberOfTasks;
    }

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
     * @return a representation as long of the duration of the pending time for the given job. 
     * @throws SchedulerException 
     */
    public long getJobPendingTime(String jobId) {
        // Check if the jobId inserted is not present in the map
        long result = 0;
        try {
            result = this.jobPendingTimeMap.get(jobId);
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the job pending time map";
            logger_dev.error(msg, e);
        }
        return result;
    }

    /**
     * This method gives the running time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the running time for the given job. 
     * @throws SchedulerException 
     */
    public long getJobRunningTime(String jobId) {
        // Check if the jobId inserted is not present in the map
        long result = 0;
        try {
            result = this.jobRunningTimeMap.get(jobId);
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the job running time map";
            logger_dev.error(msg, e);
        }
        return result;
    }

    /**
     * This method gives the mean task pending time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task pending time for the given job. 
     * @throws SchedulerException 
     */
    public long getMeanTaskPendingTime(String jobId) {
        // Check if the jobId inserted is not present in the map
        long result = 0;
        try {
            result = this.meanTaskPendingTimeMap.get(jobId);
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the task pending time map";
            logger_dev.error(msg, e);
        }
        return result;
    }

    /**
     * This method gives the mean task running time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task running time for the given job. 
     */
    public long getMeanTaskRunningTime(String jobId) {
        // Check if the jobId inserted is not present in the map
        long result = 0;
        try {
            result = this.meanTaskRunningTimeMap.get(jobId);
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the task running time map";
            logger_dev.error(msg, e);
        }
        return result;
    }

    /**
     * This method gives the total number of nodes used by a given Job
     *
     * @param jobId, the id of the Job to check
     * @return the total number of nodes used by the given job. 
     */
    public int getTotalNumberOfNodesUsed(String jobId) {
        // Check if the jobId inserted is not present in the map
        int result = 0;
        try {
            result = this.nodesUsedByJobMap.get(jobId);
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the nodes used by job map";
            logger_dev.error(msg, e);
        }
        return result;
    }

    // UTILITY OPERATIONS

    /**
     * This method represents a possible Operation to Invoke on the MBean.
     * It gives the pending time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the pending time for the given job. 
     */
    public String getFormattedJobPendingTime(String jobId) {
        // Check if the jobId inserted is not present in the map
        String result = "";
        try {
            result = Tools.getFormattedDuration(0, this.jobPendingTimeMap.get(jobId));
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the job pending time map";
            logger_dev.error(msg, e);
            result = msg;
        }
        return result;
    }

    /**
     * This method gives the running time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the running time for the given job. 
     */
    public String getFormattedJobRunningTime(String jobId) {
        // Check if the jobId inserted is not present in the map
        String result = "";
        try {
            result = Tools.getFormattedDuration(0, this.jobRunningTimeMap.get(jobId));
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the job running time map";
            logger_dev.error(msg, e);
            result = msg;
        }
        return result;
    }

    /**
     * This method gives the mean task pending time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task pending time for the given job. 
     */
    public String getFormattedMeanTaskPendingTime(String jobId) {
        // Check if the jobId inserted is not present in the map
        String result = "";
        try {
            result = Tools.getFormattedDuration(0, this.meanTaskPendingTimeMap.get(jobId));
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the task pending time map";
            logger_dev.error(msg, e);
            result = msg;
        }
        return result;
    }

    /**
     * This method gives the mean task running time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task running time for the given job. 
     */
    public String getFormattedMeanTaskRunningTime(String jobId) {
        // Check if the jobId inserted is not present in the map
        String result = "";
        try {
            result = Tools.getFormattedDuration(0, this.meanTaskRunningTimeMap.get(jobId));
        } catch (Exception e) {
            String msg = "Job '" + jobId + "' is not present in the task running time map";
            logger_dev.error(msg, e);
            result = msg;
        }
        return result;
    }

}