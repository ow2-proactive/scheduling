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
package org.objectweb.proactive.extra.scheduler.gui.data;

public interface EventSchedulerListener {

    /**
     * Invoked when the scheduler has just been started.
     */
    public void startedEvent();

    /**
     * Invoked when the scheduler has just been stopped.
     */
    public void stoppedEvent();

    /**
     * Invoked when the scheduler has just been paused.
     *
     * @param event the scheduler informations about the status of every tasks.
     *            use <code>SchedulerEvent.update(Vector<<Job>>)</code> to
     *            update your job.
     */
    public void pausedEvent();

    /**
     * Invoked when the scheduler has received a paused immediate signal.
     */
    public void freezeEvent();

    /**
     * Invoked when the scheduler has just been resumed.
     */
    public void resumedEvent();

    /**
     * Invoked when the scheduler shutdown sequence is initialised.
     */
    public void shuttingDownEvent();

    /**
     * Invoked when the scheduler has just been shutdown.
     *
     * @param job the new scheduled job.
     */
    public void shutDownEvent();

    /**
     * Invoked when the scheduler has just been killed. Scheduler is not
     * reachable anymore.
     */
    public void killedEvent();
}
