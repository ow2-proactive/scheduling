/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.ow2.proactive.scheduler.common.scheduler;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.Job;


/**
 * This class is a representation of the whole scheduler initial jobs list state.<br>
 * It is basically represented by 3 lists of jobs, and its scheduling state.
 *
 * @author The ProActive Team
 * @version 3.9, Jun 12, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public final class SchedulerInitialState<E extends Job> implements Serializable {

    /** Pending jobs */
    private Vector<E> pendingJobs = new Vector<E>();

    /** Running jobs */
    private Vector<E> runningJobs = new Vector<E>();

    /** Finished jobs */
    private Vector<E> finishedJobs = new Vector<E>();

    /** Scheduler state */
    private SchedulerState state = SchedulerState.STOPPED;

    /** List of connected user. */
    private SchedulerUsers sUsers;

    /**
     * ProActive Empty constructor.
     */
    public SchedulerInitialState() {
    }

    /**
     * To get the finishedJobs
     *
     * @return the finishedJobs
     */
    public Vector<E> getFinishedJobs() {
        return finishedJobs;
    }

    /**
     * To set the finishedJobs
     *
     * @param finishedJobs the finishedJobs to set
     */
    public void setFinishedJobs(Vector<E> finishedJobs) {
        this.finishedJobs = finishedJobs;
    }

    /**
     * To get the pendingJobs
     *
     * @return the pendingJobs
     */
    public Vector<E> getPendingJobs() {
        return pendingJobs;
    }

    /**
     * To set the pendingJobs
     *
     * @param pendingJobs the pendingJobs to set
     */
    public void setPendingJobs(Vector<E> pendingJobs) {
        this.pendingJobs = pendingJobs;
    }

    /**
     * To get the runningJobs
     *
     * @return the runningJobs
     */
    public Vector<E> getRunningJobs() {
        return runningJobs;
    }

    /**
     * To set the runningJobs
     *
     * @param runningJobs the runningJobs to set
     */
    public void setRunningJobs(Vector<E> runningJobs) {
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
