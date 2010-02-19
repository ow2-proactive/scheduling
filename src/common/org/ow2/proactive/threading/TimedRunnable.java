/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.threading;

/**
 * Generic TimedRunnable interface can be used to execute a task in the {@link ThreadPoolController} thread pool.
 * This interface provides methods to :<br>
 * <ul>
 * <li>Run the piece of code that should be threaded</li>
 * <li>Retrieve the result at the end of the execution</li>
 * <li>do a particular action when timeout has expired for this execution</li>
 * </ul> 
 * <br>
 * The {@link #isDone()} method must return true as soon as the execution is considered as finished, false otherwise.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public interface TimedRunnable extends Runnable {

    /**
     * Return the status of the task, terminated or not.
     * 
     * @return true if the task is finished, false otherwise.
     */
    public boolean isDone();

    /**
     * Executed if the timeout for this task is expired.
     * <b>Warning</b> : This method must be non-blocking.
     */
    public void timeoutAction();
}
