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
package org.objectweb.proactive.core.util.profiling;

import java.lang.reflect.Method;

import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.core.UniqueID;


/**
 * An instance of a class that implements this interface can
 * provide the minimal operations on timers.
 * @see TimerWarehouse
 * @author vbodnart
 */
public interface TimerProvidable {

    /**
     * Starts a timer identified by an id.
     * @param timerId The id of the timer defined in <code>TimerWarehouse</code> class
     */
    public void startTimer(int timerId);

    /**
     * Stops a timer identified by an id.
     * @param timerId The id of the timer defined in <code>TimerWarehouse</code> class
     */
    public void stopTimer(int timerId);

    /**
     * Starts the Serve timer and the timer associated to the currently served Method.
     * @param timerId The id of the timer defined in <code>TimerWarehouse</code> class
     */
    public void startServeTimer(Method method);

    /**
     * Starts the Serve timer and the timer associated to the currently served Method.
     * @param timerId The id of the timer defined in <code>TimerWarehouse</code> class
     */
    public void stopServeTimer();

    /**
     * Starts the timer identified by an id and disables the provider.
     * Such operation can be usefull in case of a communication with a group of active objects.
     * @param timerId The id of the timer defined in <code>TimerWarehouse</code> class
     */
    public void startXAndDisable(int timerId);

    /**
     * Enables the provider and stops the timer identified by an id.
     * @param timerId The id of the timer defined in <code>TimerWarehouse</code> class
     */
    public void enableAndStopX(int timerId);

    /**
     * Returns the unique id of the provider.
     * @return The unique id of the provider
     */
    public UniqueID getTimerProvidableID();

    /**
     * Sends the results to the reductor.
     * @param className The name of the class of the reified object
     * @param shortUniqueID The short version of the associated body id
     */
    public void sendResults(String className, String shortUniqueID);

    /**
     * Stops all timers in a hierarchical manner.
     */
    public void stopAll();

    /**
     * Returns a snapshot of all timers. Note that these timers are not stopped.
     * @return An array of non-stopped timers
     */
    public BasicTimer[] getSnapshot();

    /**
     * Creates, attaches and returns a new named timer named Starts the Serve timer and the timer associated to the currently served Method.
     * @param timerName The id of the timer defined in <code>TimerWarehouse</code> class
     */
    public BasicTimer attachTimer(String timerName);
}
