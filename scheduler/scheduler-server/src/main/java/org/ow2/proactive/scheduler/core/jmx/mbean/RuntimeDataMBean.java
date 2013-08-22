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
package org.ow2.proactive.scheduler.core.jmx.mbean;

import java.io.IOException;


/**
 * MBean interface representing the attributes of the ProActive Scheduling Runtime
 * and some Key Performance Indicators values to monitor the ProActive Scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public interface RuntimeDataMBean {

    /**
     * Returns the state of the scheduler.
     *
     * @return The state of the scheduler.
     */
    public String getStatus();

    /**
     * Returns the number of users connected to the scheduler.
     *
     * @return the number of users connected to the scheduler.
     */
    public int getConnectedUsersCount();

    /**
     * Returns the total number of jobs.
     *
     * @return the total number of jobs.
     */
    public int getTotalJobsCount();

    /**
     * Returns the number of pending jobs of the scheduler.
     *
     * @return The number of pending jobs of the scheduler.
     */
    public int getPendingJobsCount();

    /**
     * Returns the number of running jobs of the scheduler.
     *
     * @return The number of running jobs of the scheduler.
     */
    public int getRunningJobsCount();

    /**
     * Returns the number of finished jobs of the scheduler.
     *
     * @return The number of finished jobs of the scheduler.
     */
    public int getFinishedJobsCount();

    /**
     * Returns the total number of Tasks.
     *
     * @return the total number of Tasks.
     */
    public int getTotalTasksCount();

    /**
     * Returns the number of pending Tasks of the scheduler.
     *
     * @return The number of pending Tasks of the scheduler.
     */
    public int getPendingTasksCount();

    /**
     * Returns the number of running Tasks of the scheduler.
     *
     * @return The number of running Tasks of the scheduler.
     */
    public int getRunningTasksCount();

    /**
     * Returns the number of finished Tasks of the scheduler.
     *
     * @return The number of finished Tasks of the scheduler.
     */
    public int getFinishedTasksCount();

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
     * Returns the Key Performance Indicator related to the average of pending
     * time for a job.
     *
     * @return A string representing the average pending time for a job.
     */
    public String getFormattedMeanJobPendingTime();

    /**
     * Returns the Key Performance Indicator related to the average of executing
     * time for a job.
     *
     * @return A string representing the average executing time for a job.
     */
    public String getFormattedMeanJobExecutionTime();

    /**
     * Returns the Key Performance Indicator related to the average period of
     * arrival time for a job.
     *
     * @return A string representing the average submitting period for a job.
     */
    public String getFormattedJobSubmittingPeriod();

    /**
     * This method represents a possible Operation to Invoke on the MBean. It
     * gives the pending time for a given Job
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the pending time for
     *         the given job.
     */
    public long getJobPendingTime(String jobId);

    /**
     * This method gives the running time for a given Job.
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the running time for
     *         the given job.
     */
    public long getJobRunningTime(String jobId);

    /**
     * This method gives the mean task pending time for a given Job
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the mean task pending
     *         time for the given job.
     */
    public long getMeanTaskPendingTime(String jobId);

    /**
     * This method gives the mean task running time for a given Job
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the mean task running
     *         time for the given job.
     */
    public long getMeanTaskRunningTime(String jobId);

    /**
     * This method gives the total number of nodes used by a given Job
     *
     * @param jobId
     *            the id of the Job to check
     * @return the total number of nodes used by the given job.
     */
    public int getTotalNumberOfNodesUsed(String jobId);

    /**
     * This method represents a possible Operation to Invoke on the MBean. It
     * gives the pending time for a given Job as String
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the pending time for
     *         the given job.
     */
    public String getFormattedJobPendingTime(String jobId);

    /**
     * This method gives the running time for a given Job as String
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the running time for
     *         the given job.
     */
    public String getFormattedJobRunningTime(String jobId);

    /**
     * This method gives the mean task pending time for a given Job as String
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the mean task pending
     *         time for the given job.
     */
    public String getFormattedMeanTaskPendingTime(String jobId);

    /**
     * This method gives the mean task running time for a given Job as String
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the mean task running
     *         time for the given job.
     */
    public String getFormattedMeanTaskRunningTime(String jobId);

    /**
     * Sends the statistics accumulated in the RRD data base
     *
     * @return data base file converted to bytes
     * @throws IOException when data base cannot be read
     */
    public byte[] getStatisticHistory() throws IOException;
}
