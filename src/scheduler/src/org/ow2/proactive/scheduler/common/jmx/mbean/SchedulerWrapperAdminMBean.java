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

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;


/**
 * MBean interface representing the attributes and the KPI values to monitor the ProActive Scheduler
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public interface SchedulerWrapperAdminMBean {

    /**
     * Returns the state of the scheduler.
     *
     * @return The state of the scheduler.
     */
    public String getSchedulerStatus();

    /**
     * Returns the number of users connected to the scheduler.
     * 
     * @return the number of users connected to the scheduler.
     */
    public int getNumberOfConnectedUsers();

    /**
     * Returns the total number of jobs.
     * 
     * @return the total number of jobs.
     */
    public int getTotalNumberOfJobs();

    /**
     * Returns the number of pending jobs of the scheduler.
     *
     * @return The number of pending jobs of the scheduler.
     */
    public int getNumberOfPendingJobs();

    /**
     * Returns the number of running jobs of the scheduler.
     *
     * @return The number of running jobs of the scheduler.
     */
    public int getNumberOfRunningJobs();

    /**
     * Returns the number of finished jobs of the scheduler.
     *
     * @return The number of finished jobs of the scheduler.
     */
    public int getNumberOfFinishedJobs();

    /**
     * Returns the total number of Tasks.
     * 
     * @return the total number of Tasks.
     */
    public int getTotalNumberOfTasks();

    /**
     * Returns the number of pending Tasks of the scheduler.
     *
     * @return The number of pending Tasks of the scheduler.
     */
    public int getNumberOfPendingTasks();

    /**
     * Returns the number of running Tasks of the scheduler.
     *
     * @return The number of running Tasks of the scheduler.
     */
    public int getNumberOfRunningTasks();

    /**
     * Returns the number of finished Tasks of the scheduler.
     *
     * @return The number of finished Tasks of the scheduler.
     */
    public int getNumberOfFinishedTasks();

    /**
     * @return current mean job pending time as integer
     */
    public int getMeanJobPendingTime();

    /**
     * @return current mean job execution time as integer
     */
    public int getMeanJobExecutionTime();

    /**
     * @return current mean job submitting period as integer
     */
    public int getJobSubmittingPeriod();

    /**
     * Returns the Key Performance Indicator related to the average of pending time for a job.
     *
     * @return A string representing the average pending time for a job.
     */
    public String getFormattedMeanJobPendingTime();

    /**
     * Returns the Key Performance Indicator related to the average of executing time for a job.
     *
     * @return A string representing the average executing time for a job.
     */
    public String getFormattedMeanJobExecutionTime();

    /**
     * Returns the Key Performance Indicator related to the average period of arrival time for a job.
     *
     * @return A string representing the average submitting period for a job.
     */
    public String getFormattedJobSubmittingPeriod();

    /**
     * This method represents a possible Operation to Invoke on the MBean.
     * It gives the pending time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the pending time for the given job. 
     * @throws SchedulerException 
     */
    public long getJobPendingTime(String jobId);

    /**
     * This method gives the running time for a given Job.
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the running time for the given job. 
     * @throws SchedulerException 
     */
    public long getJobRunningTime(String jobId);

    /**
     * This method gives the mean task pending time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task pending time for the given job. 
    * @throws SchedulerException 
     */
    public long getMeanTaskPendingTime(String jobId);

    /**
     * This method gives the mean task running time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task running time for the given job. 
     */
    public long getMeanTaskRunningTime(String jobId);

    /**
     * This method gives the total number of nodes used by a given Job
     *
     * @param jobId, the id of the Job to check
     * @return the total number of nodes used by the given job. 
     */
    public int getTotalNumberOfNodesUsed(String jobId);

    /**
     * This method represents a possible Operation to Invoke on the MBean.
     * It gives the pending time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the pending time for the given job. 
     */
    public String getFormattedJobPendingTime(String jobId);

    /**
     * This method gives the running time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the running time for the given job. 
     */
    public String getFormattedJobRunningTime(String jobId);

    /**
     * This method gives the mean task pending time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task pending time for the given job. 
     */
    public String getFormattedMeanTaskPendingTime(String jobId);

    /**
     * This method gives the mean task running time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task running time for the given job. 
     */
    public String getFormattedMeanTaskRunningTime(String jobId);
}