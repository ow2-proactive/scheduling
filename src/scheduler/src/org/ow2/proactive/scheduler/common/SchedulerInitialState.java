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
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.Job;


/**
 * This interface is a representation of the whole scheduler initial jobs list state.<br>
 * It is basically represented by 3 lists of jobs, and its scheduling state.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface SchedulerInitialState extends Serializable {

    /**
     * Get the finished Jobs list
     *
     * @return the finished Jobs list
     */
    public Vector<Job> getFinishedJobs();

    /**
     * Get the pending Jobs list
     *
     * @return the pending Jobs list
     */
    public Vector<Job> getPendingJobs();

    /**
     * Get the running Jobs list
     *
     * @return the running Jobs list
     */
    public Vector<Job> getRunningJobs();

    /**
     * Get the state of the scheduler
     *
     * @return the state of the scheduler
     */
    public SchedulerState getState();

    /**
     * Returns the list of connected users.
     *
     * @return the list of connected users.
     */
    public SchedulerUsers getUsers();

}
