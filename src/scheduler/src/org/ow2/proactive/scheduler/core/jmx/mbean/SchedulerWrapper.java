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
package org.ow2.proactive.scheduler.core.jmx.mbean;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.util.Tools;

/**
 * This class represents a Managed Bean to allow the management of the ProActive Scheduler 
 * following the JMX standard for management.
 * It provides some attributes and some statistics indicators.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class SchedulerWrapper implements SchedulerWrapperMBean {
	 /** Scheduler current state */
    private SchedulerStatus schedulerState = SchedulerStatus.STOPPED;
    
    /** 
     * Variables representing the attributes of the SchedulerMBean 
     */
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
    
    /**
     * Variables representing the Key Performance Indicators for the SchedulerWrapper
     */
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
     * Empty constructor required by JMX
     */
    public SchedulerWrapper() {
        /* Empty Constructor required by JMX */
    }
   
    // EVENT MANAGEMENT
	
	/**
	 * Methods for dispatching events
	 *  
     * Call the MBean event for the related Scheduler Updated event type
     *
     * @param eventType
     */
    public void schedulerStateUpdated(SchedulerEvent eventType) {
    	switch (eventType) {
	    	case STARTED: schedulerStartedEvent(); break;
	    	case STOPPED: this.schedulerState = SchedulerStatus.STOPPED; break;
	    	case PAUSED: this.schedulerState = SchedulerStatus.PAUSED; break;
	    	case FROZEN: this.schedulerState = SchedulerStatus.FROZEN; break;
	    	case RESUMED: this.schedulerState = SchedulerStatus.STARTED; break;
	    	case SHUTTING_DOWN: this.schedulerState = SchedulerStatus.SHUTTING_DOWN; break;
	    	case SHUTDOWN: this.schedulerState = SchedulerStatus.STOPPED; break;
	    	case KILLED: this.schedulerState = SchedulerStatus.KILLED; break;
    	}
    }
    
    /**
     * Call the MBean event for the related Job Updated event type
     *
     * @param notification data containing job info
     */
    public void jobStateUpdated(NotificationData<JobInfo> notification) {
    	switch (notification.getEventType()) {
	    	case JOB_PAUSED: this.numberOfRunningJobs--; break;
	    	case JOB_RESUMED: this.numberOfRunningJobs++; break;
	    	case JOB_PENDING_TO_RUNNING: jobPendingToRunningEvent(notification.getData()); break;
        }
    }
    
    /**
     * Call the MBean event for the related Task Updated event type
     *
     * @param notification data containing task info
     */
    public void taskStateUpdated(NotificationData<TaskInfo> notification) {
    	switch (notification.getEventType()) {
	    	case TASK_PENDING_TO_RUNNING: taskPendingToRunningEvent(notification.getData()); break;
	    	case TASK_RUNNING_TO_FINISHED: taskRunningToFinishedEvent(notification.getData()); break;
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
	 * @param job info
	 */
	public void jobRemoveFinishedEvent(JobInfo info) {
		this.numberOfFinishedJobs--;
		this.totalNumberOfJobs--;
		// For each task of the Job decrement the number of finished tasks and the total number of tasks
		for(int i=0; i<info.getTotalNumberOfTasks(); i++) {
			this.totalNumberOfTasks--;
			this.numberOfFinishedTasks--;
		}
	}

	/**
	 * This is a canonical event to calculate the meanJobExecutionTime KPI
	 * 
	 * @param job info
	 */
	public void jobRunningToFinishedEvent(JobInfo info) {
		this.numberOfRunningJobs--;
		this.numberOfFinishedJobs++;
		// Call the private method to calculate the mean execution time
		calculateMeanJobExecutionTime(info);
	}

	/**
	 * This is a canonical event to calculate the meanJobArrivalTime KPI
	 * @param job state
	 */
	public void jobSubmittedEvent(JobState job) {
		this.totalNumberOfJobs++;
		this.numberOfPendingJobs++;
		// For each task of the Job increment the number of pending tasks and the total number of tasks
		for(int i=0; i<job.getTotalNumberOfTasks(); i++) {
			this.totalNumberOfTasks++;
			this.numberOfPendingTasks++;
		}
		// Call the private method to calculate the mean arrival time
		calculateJobSubmittingPeriod(job.getJobInfo());
	}

	/**
	 * This is a canonical event to calculate the meanJobArrivalTime KPI
	 */
	private void schedulerStartedEvent() {
		this.schedulerState = SchedulerStatus.STARTED;
		// Set the scheduler started time
    	setSchedulerStartedTime(System.currentTimeMillis());
	}
	
	/**
	 * @param task info
	 */
	private void taskPendingToRunningEvent(TaskInfo info) {
		this.numberOfPendingTasks--;
		this.numberOfRunningTasks++;
	}

	/**
	 * @param task info
	 */
	private void taskRunningToFinishedEvent(TaskInfo info) {
		this.numberOfRunningTasks--;
		this.numberOfFinishedTasks++;
	}

	/**
	 * @param user identification
	 */
	public void usersUpdate(UserIdentification userIdentification) {
		// It can be an update to remove or to add a User
		if(userIdentification.isToRemove()) {
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
    	// Increment the cumulative pending time
    	this.cumulativePendingTime += (info.getStartTime()-info.getSubmittedTime());
    	// Increment the related counter
        this.counterJobPendingTime++;
        // Update the mean pending time dividing the cumulative pending time by the related counter
    	this.meanJobPendingTime = (this.cumulativePendingTime/this.counterJobPendingTime);
    }
    
    /**
     * After a given number of Jobs we can have a good current estimation of the mean Job Execution Time
     * calculated each time dividing the accumulator time by the counter.
     * 
     * @param job info
     */
    private void calculateMeanJobExecutionTime(JobInfo info) {
    	// Calculate the Running time for this Job (finishedTime - startTime)
    	// Increment the cumulative execution time
    	this.cumulativeExecutionTime += (info.getFinishedTime()-info.getStartTime());
    	// Increment the related counter
        this.counterJobExecutionTime++;
        // Update the mean execution time dividing the cumulative execution time by the related counter
    	this.meanJobExecutionTime = (this.cumulativeExecutionTime/this.counterJobExecutionTime);
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
    	if(this.counterJobArrivalTime == 0) {
    		this.cumulativeArrivalTime = (info.getSubmittedTime()-this.schedulerStartedTime);
    	} else {
    		this.cumulativeArrivalTime += (info.getSubmittedTime()-this.previousSubmittedTime);
    	}
    	// Increments the related counter
    	this.counterJobArrivalTime++;
    	// Update the mean arrival time dividing the cumulative arrival time by the related counter
    	this.jobSubmittingPeriod = (this.cumulativeArrivalTime/this.counterJobArrivalTime);
    	// This is the previous Submitted Time for the next time that happens this event
    	this.previousSubmittedTime = info.getSubmittedTime();
    	
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
	public String getSchedulerState() {
		return this.schedulerState.toString();
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
     * Getter methods for KPI values
     * 
     * @return current mean job pending time as formatted duration
     */
	public String getMeanJobPendingTime() {
		return Tools.getFormattedDuration(0, this.meanJobPendingTime);
	}

	/**
	 * @return current mean job execution time as formatted duration
	 */
	public String getMeanJobExecutionTime() {
		return Tools.getFormattedDuration(0, this.meanJobExecutionTime);
	}

	/**
	 * @return current mean job submitting period as formatted duration
	 */
	public String getJobSubmittingPeriod() {
		return Tools.getFormattedDuration(0,this.jobSubmittingPeriod);
	}
	
	// UTILITY METHODS
	
	/**
	 * Getter methods for the KPI values as integer
	 * 
	 * @return current mean job pending time as integer
	 */
	public int getMeanJobPendingTimeAsInt() {
		return (int)this.meanJobPendingTime;
	}

	/**
	 * @return current mean job execution time as integer
	 */
	public int getMeanJobExecutionTimeAsInt() {
		return (int)this.meanJobExecutionTime;
	}

	/**
	 * @return current mean job submitting period as integer
	 */
	public int getJobSubmittingPeriodAsInt() {
		return (int)this.jobSubmittingPeriod;
	}
}
