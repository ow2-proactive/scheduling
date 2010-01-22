/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.jmx.mbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.utils.Tools;


/**
 * This class represents a Managed Bean to allow the management of the ProActive Scheduler 
 * following the JMX standard for management.
 * It provides some attributes and some statistics indicators.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SchedulerWrapperAdmin extends SchedulerWrapperAnonym implements SchedulerWrapperAdminMBean {
    /**  */
    private static final long serialVersionUID = 200;

    /** Scheduler logger device */
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerLoggers.FRONTEND);

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

    /** 
     * Fields to keep the informations need for the Operations to get the Key Performance Indicator values 
     * The first two are references to the Map of pending and running time for each job
     */
    private Map<String, Long> jobPendingTimeMap = new HashMap<String, Long>();
    private Map<String, Long> jobRunningTimeMap = new HashMap<String, Long>();
    /** The task timings list and the mean timings for the tasks of a given job */
    private Map<String, List<Long>> taskPendingTimeMap = new HashMap<String, List<Long>>();
    private Map<String, List<Long>> taskRunningTimeMap = new HashMap<String, List<Long>>();
    private Map<String, Long> meanTaskPendingTimeMap = new HashMap<String, Long>();
    private Map<String, Long> meanTaskRunningTimeMap = new HashMap<String, Long>();
    /** Map of the number of nodes used by the jobs */
    private Map<String, Integer> nodesUsedByJobMap = new HashMap<String, Integer>();
    /** List of execution host for each task of each job */
    private Map<String, Set<String>> executionHostNames = new HashMap<String, Set<String>>();

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
    @Override
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
     * Each time that there`s an event is done the related update
     * 
     * This is a canonical event to calculate the meanJobPendingTime KPI
     * 
     * @param job info
     */
    @Override
    protected void jobPendingToRunningEvent(JobInfo info) {
        super.jobPendingToRunningEvent(info);
        // Call the private method to calculate the mean pending time
        calculateMeanJobPendingTime(info);
    }

    /**
     * This is a canonical event to calculate the meanJobExecutionTime KPI
     * 
     * @param info the job's information
     */
    @Override
    protected void jobRunningToFinishedEvent(JobInfo info) {
        super.jobRunningToFinishedEvent(info);
        // Call the private method to compute the mean execution time
        computeMeanJobExecutionTime(info);
        // Add the meanTaskPendingTime for this job to the meanTaskPendingTimeMap in position [jobId]
        String jobId = info.getJobId().value();
        long mean = this.computeMean(this.taskPendingTimeMap, jobId);
        this.meanTaskPendingTimeMap.put(jobId, mean);
        // Add the meanTaskRunningTime for this job to the meanTaskRunningTimeMap in position [jobId]
        mean = this.computeMean(this.taskRunningTimeMap, jobId);
        this.meanTaskRunningTimeMap.put(jobId, mean);
        /* put the number of nodes used by the Job and put it in the nodesUsedByJobMap in position [jobId] */
        try {
            this.nodesUsedByJobMap.put(jobId, this.executionHostNames.get(info.getJobId().value()).size());
        } catch (NullPointerException npe) {
            this.nodesUsedByJobMap.put(jobId, 0);
        }
    }

    /**
     * This is a canonical event to calculate the meanJobArrivalTime KPI
     * 
     * @param job the state of the job
     */
    @Override
    public void jobSubmittedEvent(JobState job) {
        super.jobSubmittedEvent(job);
        // Call the private method to calculate the mean arrival time
        calculateJobSubmittingPeriod(job.getJobInfo());
    }

    /**
     * Task pending to running event
     * 
     * @param info task's information
     */
    @Override
    protected void taskPendingToRunningEvent(TaskInfo info) {
        super.taskPendingToRunningEvent(info);
        // Calculate the Pending time for this Task (taskStartTime - jobSubmittedTime)
        long taskPendingTime = (info.getStartTime() - info.getJobInfo().getSubmittedTime());
        // Add the taskPendingTime for this task to the taskPendingTimeMap in position [jobTaskId]
        String jobId = info.getJobId().value();
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
    @Override
    protected void taskRunningToFinishedEvent(TaskInfo info) {
        super.taskRunningToFinishedEvent(info);
        // Calculate the Pending time for this Task (taskFinishedTime - taskStartTime)
        long taskRunningTime = (info.getFinishedTime() - info.getStartTime());
        // Add the taskRunningTime for this task to the taskRunningTimeMap in position [jobTaskId]
        String jobId = info.getJobId().value();
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
    private void computeMeanJobExecutionTime(JobInfo info) {
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
     * Method to compute the mean of the values on a given map make of all the job and task keys
     * with key in the form <jobId taskId>
     * 
     * @param map
     * @return a long representation of the mean
     */
    private long computeMean(Map<String, List<Long>> map, String jobId) {
        List<Long> list = map.get(jobId);
        if (list == null || list.size() == 0) {
            return 0;
        }
        // Initialize the mean for the given job
        long mean = 0;
        int counter = 0;
        for (Long l : list) {
            mean = mean + l;
            counter++;
        }
        return mean / counter;
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
            logger_dev.error("Job '" + jobId + "' is not present in the job pending time map", e);
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
            logger_dev.error("Job '" + jobId + "' is not present in the job running time map", e);
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
            logger_dev.error("Job '" + jobId + "' is not present in the task pending time map", e);
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
            logger_dev.error("Job '" + jobId + "' is not present in the task running time map", e);
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
            logger_dev.error("Job '" + jobId + "' is not present in the nodes used by job map", e);
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