/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job;

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
    PENDING("Pending"),
    /**
     * The job is running. Actually at least one of its task has been scheduled.
     */
    RUNNING("Running"),
    /**
     * The job has been launched but no task are currently running.
     */
    STALLED("Stalled"),
    /**
     * The job is finished. Every tasks are finished.
     */
    FINISHED("Finished"),
    /**
     * The job is paused waiting for user to resume it.
     */
    PAUSED("Paused"),
    /**
     * The job has been canceled due to user exception and order.
     * This status runs when a user exception occurs in a task
     * and when the user has asked to cancel On exception.
     */
    CANCELED("Canceled"),
    /**
     * The job has failed. One or more tasks have failed (due to resources failure).
     * There is no more executionOnFailure left for a task.
     */
    FAILED("Failed"),
    /**
     * The job has been killed by a user..
     * Nothing can be done anymore on this job expect read execution informations
     * such as output, time, etc...
     */
    KILLED("Killed");

    /** The textual definition of the status */
    private String definition;

    /**
     * Default constructor.
     * @param def the textual definition of the status.
     */
    JobStatus(String def) {
        definition = def;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return definition;
    }
}
