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
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;


/**
 * Informations about the task that is able to change.<br>
 * These informations are not in the {@link Task} class in order to permit
 * the scheduler listener to send this class as event.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface TaskInfo extends Serializable {

    /**
     * To get the jobInfo
     *
     * @return the jobInfo
     */
    JobInfo getJobInfo();

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
     */
    long getFinishedTime();

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    JobId getJobId();

    /**
     * Get the name of the task
     *
     * @return the name of the task
     */
    String getName();

    /**
     * To get the startTime
     *
     * @return the startTime
     */
    long getStartTime();

    /**
     * To get the time at which the Task has been terminated for the last time (last attempt).
     *
     * @return the time at which the Task has been terminated for the last time (last attempt).
     */
    long getInErrorTime();

    /**
     * To get the taskId
     *
     * @return the taskId
     */
    TaskId getTaskId();

    /**
     * To get the taskStatus
     *
     * @return the taskStatus
     */
    TaskStatus getStatus();

    /*
     * <B>This method always returns 0 in Scheduling 2.2.0.</B>
     */
    /**
     * Return the latest progress value for this task.
     * Progress value is ranged between 0 and 100.
     *
     * @return the latest progress value for this task.
     */
    int getProgress();

    /**
     * Get the last execution HostName of the task.
     *
     * @return the last execution HostName.
     */
    String getExecutionHostName();

    /**
     * To get the list of execution hosts name.
     * The first element of the returned array is the most recent used host.
     *
     * @return the execution Host Name list.
     */
    String[] getExecutionHostNameList();

    /**
     * Get the number of execution left.
     *
     * @return the number of execution left.
     */
    int getNumberOfExecutionLeft();

    /**
     * Get the numberOfExecutionOnFailureLeft value.
     * 
     * @return the numberOfExecutionOnFailureLeft value.
     */
    int getNumberOfExecutionOnFailureLeft();

    /**
     * Get the execution duration of the task. It is the real execution time, CPU usage.
     *
     * @return the execution duration in millis
     */
    long getExecutionDuration();

    /**
     * Get the scheduled time.
     * @return the scheduled time value in millis
     */
    long getScheduledTime();

    /**
     * If the visualization is activated on the task
     * @return visualization status
     */
    boolean isVisualizationActivated();

    /**
     * If the visualization is activated, return the connection string
     * @return visualization connection string
     */
    String getVisualizationConnectionString();

}
