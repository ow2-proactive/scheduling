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

import static org.ow2.proactive.scheduler.common.job.JobState.*;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Scheduling status of a job.
 * The different job status are best described below.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public enum JobStatus implements java.io.Serializable {

    /**
     * The job is waiting to be scheduled.
     */
    PENDING("Pending", true, PENDING_RANK),
    /**
     * The job is running. Actually at least one of its task has been scheduled.
     */
    RUNNING("Running", true, RUNNING_RANK),
    /**
     * The job has been launched but no task are currently running.
     */
    STALLED("Stalled", true, RUNNING_RANK),
    /**
     * The job is finished. Every tasks are finished.
     */
    FINISHED("Finished", false, FINISHED_RANK),
    /**
     * The job is paused waiting for user to resume it.
     */
    PAUSED("Paused", true, RUNNING_RANK),
    /**
     * The job has been canceled due to user exception and order.
     * This status runs when a user exception occurs in a task
     * and when the user has asked to cancel On exception.
     */
    CANCELED("Canceled", false, FINISHED_RANK),
    /**
     * The job has failed. One or more tasks have failed (due to resources failure).
     * There is no more executionOnFailure left for a task.
     */
    FAILED("Failed", false, FINISHED_RANK),
    /**
     * The job has been killed by a user..
     * Nothing can be done anymore on this job expect read execution informations
     * such as output, time, etc...
     */
    KILLED("Killed", false, FINISHED_RANK),
    /**
     * The job has at least one in-error task and in-error tasks are the last, among others,
     * which have changed their state (i.e. Job status is depicted by the last action).
     */
    IN_ERROR("In-Error", true, RUNNING_RANK);

    /** The textual definition of the status */
    private final String definition;

    private final boolean jobAlive;

    private final int rank;

    /**
     * Default constructor.
     * @param def the textual definition of the status.
     */
    JobStatus(String def, boolean jobAlive, int rank) {
        definition = def;
        this.jobAlive = jobAlive;
        this.rank = rank;
    }

    public static JobStatus findStatus(String name) {

        for (JobStatus jobStatus : JobStatus.values()) {
            if (name.equalsIgnoreCase(jobStatus.toString()))
                return jobStatus;
        }

        // default case
        return KILLED;

    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return definition;
    }

    public boolean isJobAlive() {
        return jobAlive;
    }

    public int getRank() {
        return rank;
    }
}
