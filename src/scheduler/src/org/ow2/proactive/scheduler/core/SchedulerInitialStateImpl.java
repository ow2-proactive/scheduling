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
package org.ow2.proactive.scheduler.core;

import java.util.Vector;

import org.ow2.proactive.scheduler.common.SchedulerInitialState;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.job.Job;


/**
 * This class is a representation of the whole scheduler initial jobs list state.<br>
 * It is basically represented by 3 lists of jobs, and its scheduling state.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public final class SchedulerInitialStateImpl implements SchedulerInitialState {

    /** Pending jobs */
    private Vector<Job> pendingJobs = new Vector<Job>();

    /** Running jobs */
    private Vector<Job> runningJobs = new Vector<Job>();

    /** Finished jobs */
    private Vector<Job> finishedJobs = new Vector<Job>();

    /** Scheduler state */
    private SchedulerState state = SchedulerState.STOPPED;

    /** List of connected user. */
    private SchedulerUsers sUsers;

    /**
     * ProActive Empty constructor.
     */
    public SchedulerInitialStateImpl() {
    }

    /**
     * To get the finishedJobs
     *
     * @return the finishedJobs
     */
    public Vector<Job> getFinishedJobs() {
        return finishedJobs;
    }

    /**
     * To set the finishedJobs
     *
     * @param finishedJobs the finishedJobs to set
     */
    public void setFinishedJobs(Vector<Job> finishedJobs) {
        this.finishedJobs = finishedJobs;
    }

    /**
     * To get the pendingJobs
     *
     * @return the pendingJobs
     */
    public Vector<Job> getPendingJobs() {
        return pendingJobs;
    }

    /**
     * To set the pendingJobs
     *
     * @param pendingJobs the pendingJobs to set
     */
    public void setPendingJobs(Vector<Job> pendingJobs) {
        this.pendingJobs = pendingJobs;
    }

    /**
     * To get the runningJobs
     *
     * @return the runningJobs
     */
    public Vector<Job> getRunningJobs() {
        return runningJobs;
    }

    /**
     * To set the runningJobs
     *
     * @param runningJobs the runningJobs to set
     */
    public void setRunningJobs(Vector<Job> runningJobs) {
        this.runningJobs = runningJobs;
    }

    /**
     * @return the state
     */
    public SchedulerState getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(SchedulerState state) {
        this.state = state;
    }

    /**
     * Returns the list of connected users.
     *
     * @return the list of connected users.
     */
    public SchedulerUsers getUsers() {
        return sUsers;
    }

    /**
     * Sets the list of connected users to the given users value.
     *
     * @param users the list of connected users to set.
     */
    public void setUsers(SchedulerUsers users) {
        sUsers = users;
    }
}
