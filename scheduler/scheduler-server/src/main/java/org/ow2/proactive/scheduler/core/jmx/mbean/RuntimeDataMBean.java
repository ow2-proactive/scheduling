/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
     * @return The state of the scheduler.
     */
    String getStatus();

    /**
     * @return the number of users connected to the scheduler.
     */
    int getConnectedUsersCount();

    /**
     * @return the total number of jobs.
     */
    int getTotalJobsCount();

    /**
     * @return The number of pending jobs of the scheduler.
     */
    int getPendingJobsCount();

    /**
     * @return The number of running jobs of the scheduler.
     */
    int getRunningJobsCount();

    /**
     * @return The number of finished jobs of the scheduler.
     */
    int getFinishedJobsCount();

    int getStalledJobsCount();

    int getPausedJobsCount();

    int getInErrorJobsCount();

    int getKilledJobsCount();

    int getCancelledJobsCount();

    int getFailedJobsCount();

    /**
     * @return the total number of Tasks.
     */
    int getTotalTasksCount();

    /**
     * @return The number of pending Tasks of the scheduler.
     */
    int getPendingTasksCount();

    /**
     * @return The number of running Tasks of the scheduler.
     */
    int getRunningTasksCount();

    /**
     * @return The number of finished Tasks of the scheduler.
     */
    int getFinishedTasksCount();

    /**
     * @return current mean job pending time as integer
     */
    int getMeanJobPendingTime();

    /**
     * @return current mean job execution time as integer
     */
    int getMeanJobExecutionTime();

    /**
     * @return current mean job submitting period as integer
     */
    int getJobSubmittingPeriod();

    /**
     * Returns the Key Performance Indicator related to the average of pending
     * time for a job.
     *
     * @return A string representing the average pending time for a job.
     */
    String getFormattedMeanJobPendingTime();

    /**
     * Returns the Key Performance Indicator related to the average of executing
     * time for a job.
     *
     * @return A string representing the average executing time for a job.
     */
    String getFormattedMeanJobExecutionTime();

    /**
     * Returns the Key Performance Indicator related to the average period of
     * arrival time for a job.
     *
     * @return A string representing the average submitting period for a job.
     */
    String getFormattedJobSubmittingPeriod();

    /**
     * This method represents a possible Operation to Invoke on the MBean. It
     * gives the pending time for a given Job
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the pending time for
     *         the given job.
     */
    long getJobPendingTime(String jobId);

    /**
     * This method gives the running time for a given Job.
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the running time for
     *         the given job.
     */
    long getJobRunningTime(String jobId);

    /**
     * This method gives the mean task pending time for a given Job
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the mean task pending
     *         time for the given job.
     */
    long getMeanTaskPendingTime(String jobId);

    /**
     * This method gives the mean task running time for a given Job
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the mean task running
     *         time for the given job.
     */
    long getMeanTaskRunningTime(String jobId);

    /**
     * This method gives the total number of nodes used by a given Job
     *
     * @param jobId
     *            the id of the Job to check
     * @return the total number of nodes used by the given job.
     */
    int getTotalNumberOfNodesUsed(String jobId);

    /**
     * This method represents a possible Operation to Invoke on the MBean. It
     * gives the pending time for a given Job as String
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the pending time for
     *         the given job.
     */
    String getFormattedJobPendingTime(String jobId);

    /**
     * This method gives the running time for a given Job as String
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the running time for
     *         the given job.
     */
    String getFormattedJobRunningTime(String jobId);

    /**
     * This method gives the mean task pending time for a given Job as String
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the mean task pending
     *         time for the given job.
     */
    String getFormattedMeanTaskPendingTime(String jobId);

    /**
     * This method gives the mean task running time for a given Job as String
     *
     * @param jobId
     *            the id of the Job to check
     * @return a representation as long of the duration of the mean task running
     *         time for the given job.
     */
    String getFormattedMeanTaskRunningTime(String jobId);

    /**
     * Sends the statistics accumulated in the RRD data base
     *
     * @return data base file converted to bytes
     * @throws IOException when data base cannot be read
     */
    byte[] getStatisticHistory() throws IOException;

    int getNeededNodes();

}
