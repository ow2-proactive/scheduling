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
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * JobInfo provides information about the Job it is linked with.
 * <br>
 * These information and only them are able to change inside the Job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface JobInfo extends Serializable {

    /**
     * Returns an identifier that uniquely identifies the Job
     * within a Scheduler instance.
     *
     * @return an identifier that uniquely identifies the Job
     * within a Scheduler instance.
     */
    JobId getJobId();

    /**
     * Returns the name of the Job owner.
     *
     * @return the name of the Job owner (the one who submitted the Job).
     */
    String getJobOwner();

    /**
     * Returns the time at which the Job has finished.
     *
     * @return the time at which the Job has finished
     * (i.e. all tasks have finished because of a normal or abnormal termination).
     */
    long getFinishedTime();

    /**
     * Returns the time at which the Job has been marked as removed.
     *
     * @return the time at which the Job has been marked as removed.
     */
    long getRemovedTime();

    /**
     * Returns the time at which the Job has started.
     *
     * @return the time at which the Job has started.
     */
    long getStartTime();

    /**
     * Returns the time at which a Job was seen as in-error
     * for the last time.
     *
     * @return the time at which a Job was seen as in-error
     * for the last time. The default value is {@code -1}.
     */
    long getInErrorTime();

    /**
     * Returns the time at which the Job was submitted.
     *
     * @return the time at which the Job was submitted.
     */
    long getSubmittedTime();

    /**
     * Returns the time at which the last updated happened on the Job.
     *
     * @return the time in long format.
     */
    long getLastUpdatedTime();

    /**
     * Returns the number of tasks managed by the Job.
     *
     * @return the number of tasks managed by the Job.
     * The number should correspond to the sum of tasks which
     * are grouped in pending, running and finished.
     */
    int getTotalNumberOfTasks();

    /**
     * Returns the number of tasks managed by the Job
     * which are finished.
     *
     * @return the number of tasks managed by the Job
     * which are finished.
     */
    int getNumberOfFinishedTasks();

    /**
     * Returns the number of tasks managed by the Job
     * which are pending.
     *
     * @return the number of tasks managed by the Job
     * which are pending.
     */
    int getNumberOfPendingTasks();

    /**
     * Returns the number of tasks managed by the Job
     * which are running.
     *
     * @return the number of tasks managed by the Job
     * which are running.
     */
    int getNumberOfRunningTasks();

    /**
     * Returns the number of tasks managed by the Job
     * that are in a failed state due to a resource failure.
     *
     * @return the number of tasks managed by the Job
     * that are in a failed state due to a resource failure.
     */
    int getNumberOfFailedTasks();

    /**
     * Returns the number of tasks managed by the Job
     * that are in a faulty state.
     *
     * @return the number of tasks managed by the Job
     * that are in a faulty state due to a task fault
     * (exception or return code).
     */
    int getNumberOfFaultyTasks();

    /**
     * Returns the number of tasks managed by the Job
     * that are in an in-error state.
     *
     * @return the number of tasks managed by the Job
     * that are in an in-error state because of faulty
     * tasks that were configured to suspend on error.
     */
    int getNumberOfInErrorTasks();

    /**
     * Returns the priority of the Job.
     *
     * @return the priority of the Job.
     */
    JobPriority getPriority();

    /**
     * Return the status of the job.
     *
     * @return the status of the job.
     */
    JobStatus getStatus();

    /**
     * Returns a boolean that indicates whether the job
     * is marked for removal or not.
     *
     * @return a boolean that indicates whether the job
     * is marked for removal or not.
     */
    boolean isToBeRemoved();

    /**
     * Returns the scheduled time for removal.
     * If none is set, returns 0.
     *
     * @return the time at which the Job is to be removed.
     */
    long getScheduledTimeForRemoval();

    /**
     * Returns the generic information Map
     * @return generic information Map
     */
    Map<String, String> getGenericInformation();

    /**
     * Returns the variables Map
     * @return variables Map
     */
    Map<String, String> getVariables();
}
