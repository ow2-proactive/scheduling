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
package org.ow2.proactive.scheduler.common.job;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Scheduling state of a job.
 * The different job states are best described below.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public enum JobState implements java.io.Serializable {
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
     * This state runs when a user exception occurs in a task
     * and when the user has asked to cancel On exception.
     */
    CANCELLED("Canceled"),
    /**
     * The job has failed. One or more tasks have failed (due to resources failure).
     * There is no more executionOnFailure left for a task.
     */
    FAILED("Failed");

    /** The textual definition of the state */
    private String definition;

    /**
     * Default constructor.
     * @param def the textual definition of the state.
     */
    JobState(String def) {
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
