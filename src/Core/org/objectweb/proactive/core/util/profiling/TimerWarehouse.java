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
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;


/**
 * Timers indexes are typed with int.
 * This class handles all operations relative to the internal ProActive timing system.
 */
@PublicAPI
public class TimerWarehouse {

    /** The root Total timer */
    public static final int TOTAL = 0;

    /** The Serve timer represents the service time */
    public static final int SERVE = 1;

    /** The WaitForRequest timer represents time that the active object has spent on waiting for requests */
    public static final int WAIT_FOR_REQUEST = 2;

    /** The UserComputation timer correponds to the computation time for a single method of an active object */
    public static final int USER_COMPUTATION = 1;

    /** The SendReques timer represents the time that the active object has spent on sending requests */
    public static final int SEND_REQUEST = 2;

    /** The SendReply timer represents the time that the active object has spent on sending replies */
    public static final int SEND_REPLY = 3;

    /** The LocalCopy timer represents the time that the active object has spent on copying something during a request send */
    public static final int LOCAL_COPY = 4;

    /** The BeforeSerialization timer represents the time that the active object has spent between the remote call and a the serialization of the request */
    public static final int BEFORE_SERIALIZATION = 5;

    /** The Serialization timer represents the time that the active object has spent on serialization of requests */
    public static final int SERIALIZATION = 6;

    /** The AfterSerialization timer represents the time that the active object has spent after the end of the request serialization and the end of the remote call */
    public static final int AFTER_SERIALIZATION = 7;

    /** The WaitByNecessity timer represents the time that the active object has spent on waiting by necessity */
    public static final int WAIT_BY_NECESSITY = 8;

    /** The GroupOneWayCall timer represents the time that the active object has spent on performing group one way calls */
    public static final int GROUP_ONE_WAY_CALL = 9;

    /** The GroupAsyncCall timer represents the time that the active object has spent on performing asynchronous calls on a group */
    public static final int GROUP_ASYNC_CALL = 10;

    /** HashMap to store TimerProvidable objects */
    public static final ConcurrentHashMap<UniqueID, TimerProvidable> timerProvidableStore =
        new ConcurrentHashMap<UniqueID, TimerProvidable>();

    /**
     * Adds an instance of a timers provider to the local store.
     * @param timerProvidable The provider of timers. This object must implement TimerProvidable interface.
     */
    public static final void addTimerProvidable(
        final TimerProvidable timerProvidable) {
        TimerWarehouse.timerProvidableStore.put(timerProvidable.getTimerProvidableID(),
            timerProvidable);
    }

    /**
     * Returns a TimerProvidable object instance identified by its body.
     * Must be called inside ProActive.
     * @return An instance of timer provider object.
     */
    public static final TimerProvidable getTimerProvidable() {
        return TimerWarehouse.timerProvidableStore.get(PAActiveObject.getBodyOnThis()
                                                                     .getID());
    }

    /**
     * Returns the timer providable object associated to the specified unique id.
     * @param uniqueID The unique id of the timer providable object
     * @return The instance of the TimerProvidable object associated to the specified id
     */
    public static final TimerProvidable getTimerProvidable(
        final UniqueID uniqueID) {
        return TimerWarehouse.timerProvidableStore.get(uniqueID);
    }

    /**
     * Starts a timer on a TimerProvidable object specified by its unique id.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of the timer to start
     */
    public static final void startTimer(final UniqueID uniqueID,
        final int timerId) {
        final TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.startTimer(timerId);
    }

    /**
     * Stops a timer on a TimerProvidable object specified by its unique id.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of the timer to stop
     */
    public static final void stopTimer(final UniqueID uniqueID,
        final int timerId) {
        final TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.stopTimer(timerId);
    }

    /**
     * Starts the timer specified by an id, and disables the timer container.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of timer to stop
     */
    public static final void startXAndDisable(final UniqueID uniqueID,
        int timerId) {
        final TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.startXAndDisable(timerId);
    }

    /**
     * Enables the timer container and stops the timer specified by an id.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of timer to stop
     */
    public static final void enableAndStopX(final UniqueID uniqueID, int timerId) {
        final TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.enableAndStopX(timerId);
    }

    /**
     * Starts the serve timer then starts the associated method timer given by the methodName.
     * @param uniqueID The unique id of the timer providable object
     * @param method The name of the timer associated to this method
     */
    public static final void startServeTimer(final UniqueID uniqueID,
        final Method method) {
        final TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.startServeTimer(method);
    }

    /**
     * Stops the current started method timer and then stops the serve timer.
     * @param uniqueID The unique id of the timer providable object
     */
    public static final void stopServeTimer(final UniqueID uniqueID) {
        final TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.stopServeTimer();
    }

    /**
     * Enables timers on this VM.
     */
    public static final void enableTimers() {
        Profiling.TIMERS_COMPILED = true;
    }

    /**
     * Disable timers on this VM.
     */
    public static final void disableTimers() {
        Profiling.TIMERS_COMPILED = false;
    }

    /**
     * Use this method to know if timers a re compiled or not
     * @return The boolean value of the Profiling.TIMERS_COMPILED variable.
     */
    public static final boolean areTimersCompiled() {
        return Profiling.TIMERS_COMPILED;
    }
}
