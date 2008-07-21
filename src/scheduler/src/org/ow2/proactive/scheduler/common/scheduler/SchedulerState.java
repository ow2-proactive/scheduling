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
package org.ow2.proactive.scheduler.common.scheduler;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * State of the scheduler.
 * The state and what you can do with the scheduler according to the current state
 * are best described below.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 */
@PublicAPI
public enum SchedulerState implements java.io.Serializable {

    /**
     * The scheduler is running. Jobs can be submitted.
     * Get the jobs results is possible.
     * It can be paused, stopped or shutdown.
     */
    STARTED("Started"),
    /**
     * The scheduler is stopped. Jobs cannot be submitted anymore.
     * It will terminate every submitted jobs.
     * Get the jobs results is possible.
     * It can be started or shutdown.
     */
    STOPPED("Stopped"),
    /**
     * The scheduler is in freeze mode.
     * It means that every running tasks will be terminated,
     * but the running jobs will wait for the scheduler to resume.
     * It can be resumed, stopped, paused or shutdown.
     */
    FROZEN("Frozen"),
    /**
     * The scheduler is paused.
     * It means that every running jobs will be terminated.
     * It can be resumed, stopped, frozen or shutdown.
     */
    PAUSED("Paused"),
    /**
     * The scheduler is shutting down,
     * It will terminate all running jobs (during this time, get jobs results is possible),
     * then it will serialize every remaining jobs results that still are in the finished queue.
     * Finally, it will shutdown the scheduler.
     */
    SHUTTING_DOWN("Shutting down"),
    /**
     * The scheduler is unlinked with RM,
     * This can be due to the crash of the resource manager.
     * This state will block every called to the scheduler except the terminate one
     * and the call to reconnect to a new Resource Manager.
     */
    UNLINKED("Unlinked from RM"),
    /**
     * The scheduler has been killed, nothing can be done anymore.
     * (Similar to Ctrl-C)
     */
    KILLED("Killed");
    /** The textual definition of the state */
    private String definition;

    /**
     * Default constructor.
     * @param def the textual definition of the state.
     */
    SchedulerState(String def) {
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
