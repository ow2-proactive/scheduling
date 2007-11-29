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
package org.objectweb.proactive.extra.scheduler.common.scheduler;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.policy.PolicyInterface;


/**
 * Scheduler interface for someone connected to the scheduler as administrator.<br>
 * This interface represents what a scheduler administrator should do.
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jun 29, 2007
 * @since ProActive 3.2
 * @publicAPI
 */
public interface AdminSchedulerInterface extends UserSchedulerInterface {

    /**
    * Change the policy of the scheduler.<br>
    * This method will immediately change the policy and so the whole scheduling process.
    *
    * @param newPolicyFile the new policy file as a string.
    * @return true if the policy has been correctly change, false if not.
    * @throws SchedulerException (can be due to insufficient permission)
    */
    public BooleanWrapper changePolicy(
        Class<?extends PolicyInterface> newPolicyFile)
        throws SchedulerException;

    /**
     * Start the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper start() throws SchedulerException;

    /**
     * Stop the scheduler.<br>
     * Once done, you won't be able to submit job, and the scheduling will be stopped.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper stop() throws SchedulerException;

    /**
     * Pause the scheduler by terminating running jobs.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper pause() throws SchedulerException;

    /**
     * Pause the scheduler by terminating running tasks.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper pauseImmediate() throws SchedulerException;

    /**
     * Resume the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper resume() throws SchedulerException;

    /**
     * Shutdown the scheduler.<br>
     * It will terminate every submitted jobs but won't accept new submit.<br>
     * Use {@link #kill()} if you want to stop the scheduling and exit the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper shutdown() throws SchedulerException;

    /**
     * kill the scheduler.<br>
     * Will stop the scheduling, and shutdown the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper kill() throws SchedulerException;
}
