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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

public enum JobStatusData {

    /**
     * The job is waiting to be scheduled.
     */
    PENDING,
    /**
     * The job is running. Actually at least one of its task has been scheduled.
     */
    RUNNING,
    /**
     * The job has been launched but no task are currently running.
     */
    STALLED,
    /**
     * The job is finished. Every tasks are finished.
     */
    FINISHED,
    /**
     * The job is paused waiting for user to resume it.
     */
    PAUSED,
    /**
     * The job has been canceled due to user exception and order.
     * This status runs when a user exception occurs in a task
     * and when the user has asked to cancel On exception.
     */
    CANCELED,
    /**
     * The job has failed. One or more tasks have failed (due to resources failure).
     * There is no more executionOnFailure left for a task.
     */
    FAILED,
    /**
     * The job has been killed by a user..
     * Nothing can be done anymore on this job expect read execution informations
     * such as output, time, etc...
     */
    KILLED,
    /**
     * @see org.ow2.proactive.scheduler.common.job.JobStatus#IN_ERROR
     */
    IN_ERROR;

}
