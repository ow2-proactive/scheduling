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
package org.objectweb.proactive.extra.scheduler.core;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;


/**
 * Scheduler interface.
 * This interface represents what the AdminScheduler and the SchedulerFrontend should do.
 *
 * @author ProActive Team
 * @version 1.0, Jun 29, 2007
 * @since ProActive 3.2
 */
public interface AdminSchedulerInterface extends UserSchedulerInterface {

    /**
     * Start the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper start() throws SchedulerException;

    /**
     * Stop the scheduler.
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
     * Shutdown the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper shutdown() throws SchedulerException;

    /**
     * kill the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper kill() throws SchedulerException;
}
