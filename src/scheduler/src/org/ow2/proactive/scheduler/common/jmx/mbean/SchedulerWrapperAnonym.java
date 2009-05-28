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

import java.util.HashSet;
import java.util.Set;

import javax.management.NotificationBroadcasterSupport;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * This class represents a Managed Bean to allow the management of the ProActive Scheduler 
 * following the JMX standard for management.
 * It provides some attributes and some statistics indicators.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SchedulerWrapperAnonym extends NotificationBroadcasterSupport implements
        SchedulerWrapperAnonymMBean, SchedulerEventListener {
    /** Scheduler current state */
    protected SchedulerStatus schedulerStatus = SchedulerStatus.STOPPED;

    /** Variables representing the attributes of the SchedulerWrapperMBean */
    protected int totalNumberOfJobs = 0;

    protected int totalNumberOfTasks = 0;

    protected int numberOfPendingJobs = 0;

    protected int numberOfRunningJobs = 0;

    protected int numberOfFinishedJobs = 0;

    protected int numberOfPendingTasks = 0;

    protected int numberOfRunningTasks = 0;

    protected int numberOfFinishedTasks = 0;

    /** Number of Connected Users */
    protected Set<UserIdentification> users = new HashSet<UserIdentification>();

    /**
     * Empty constructor required by JMX
     */
    public SchedulerWrapperAnonym() {
        /* Empty Constructor required by JMX */
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
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        switch (eventType) {
            case STARTED:
                this.schedulerStatus = SchedulerStatus.STARTED;
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
        }
    }

    /**
     * Each time that there`s an event is done the related update
     * 
     * This is a canonical event to calculate the meanJobPendingTime KPI
     * 
     * @param job info
     */
    protected void jobPendingToRunningEvent(JobInfo info) {
        // Update the status
        this.numberOfPendingJobs--;
        this.numberOfRunningJobs++;
    }

    /**
     * the job is no more managed, it is removed from scheduler
     * 
     * @param info the job's information
     */
    protected void jobRemoveFinishedEvent(JobInfo info) {
        this.numberOfFinishedJobs--;
        this.totalNumberOfJobs--;
        // For each task of the Job decrement the number of finished tasks and the total number of tasks
        this.totalNumberOfTasks -= info.getTotalNumberOfTasks();
        this.numberOfFinishedTasks -= info.getTotalNumberOfTasks();
    }

    /**
     * This is a canonical event to calculate the meanJobExecutionTime KPI
     * 
     * @param info the job's information
     */
    protected void jobRunningToFinishedEvent(JobInfo info) {
        this.numberOfRunningJobs--;
        this.numberOfFinishedJobs++;
    }

    /**
     * This is a canonical event to calculate the meanJobArrivalTime KPI
     * 
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     * @param job the state of the job
     */
    public void jobSubmittedEvent(JobState job) {
        this.totalNumberOfJobs++;
        this.numberOfPendingJobs++;
        // For each task of the Job increment the number of pending tasks and the total number of tasks
        this.totalNumberOfTasks += job.getTotalNumberOfTasks();
        this.numberOfPendingTasks += job.getTotalNumberOfTasks();
    }

    /**
     * Task pending to running event
     * 
     * @param info task's information
     */
    protected void taskPendingToRunningEvent(TaskInfo info) {
        this.numberOfPendingTasks--;
        this.numberOfRunningTasks++;
    }

    /**
     * Task Running To Finished Event
     * 
     * @param info task's information
     */
    protected void taskRunningToFinishedEvent(TaskInfo info) {
        this.numberOfRunningTasks--;
        this.numberOfFinishedTasks++;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notificationData) {
        // It can be an update to remove or to add a User
        if (notificationData.getData().isToRemove()) {
            users.remove(notificationData.getData());
        } else {
            users.add(notificationData.getData());
        }
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
        return this.users.size();
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
     * @return current status of the Scheduler as String
     */
    public String getSchedulerStatus() {
        return this.schedulerStatus.toString();
    }

    /**
     * @return current status of the Scheduler
     */
    public SchedulerStatus getSchedulerStatus_() {
        return this.schedulerStatus;
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
}