/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.util.profiling;

import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;


/**
 * Timers indexes are typed with byte.
 * We suppose that the number of timers will not be more than 255
 */
public class TimerWarehouse {
    public static final byte TOTAL = 1;

    /** not used currently */
    public static final byte DEPLOYEMENT = 2;
    public static final byte SERVE = 3;
    public static final byte SEND_REQUEST = 4;
    public static final byte LOCAL_COPY = 5;
    public static final byte BEFORE_SERIALIZATION = 6;
    public static final byte SERIALIZATION = 7;
    public static final byte AFTER_SERIALIZATION = 8;
    public static final byte SEND_REPLY = 9;
    public static final byte WAIT_BY_NECESSITY = 10;
    public static final byte WAIT_FOR_REQUEST = 11;
    public static final byte GROUP_ONE_WAY_CALL = 12;
    public static final byte GROUP_ASYNC_CALL = 13;

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
        return TimerWarehouse.timerProvidableStore.get(ProActive.getBodyOnThis()
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
        final byte timerId) {
        startTimerWithInfos(uniqueID, timerId, null);
    }

    /**
     * Stops a timer on a TimerProvidable object specified by its unique id.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of the timer to stop
     */
    public static final void stopTimer(final UniqueID uniqueID,
        final byte timerId) {
        stopTimerWithInfos(uniqueID, timerId, null);
    }

    /**
     * Starts the timer specified by an id and provides some extra information.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of the timer to start
     * @param infos Some extra information
     */
    public static final void startTimerWithInfos(final UniqueID uniqueID,
        final byte timerId, String infos) {
        TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.startTimer(timerId, infos);
    }

    /**
     * Stops the timer specified by an id and provides some extra information.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of the timer to stop
     * @param infos Some extra information
     */
    public static final void stopTimerWithInfos(final UniqueID uniqueID,
        final byte timerId, String infos) {
        TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.stopTimer(timerId, infos);
    }

    /**
     * Starts the timer specified by an id, and disables the send request timers and its sons.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of timer to stop
     */
    public static final void startXAndSkipSendRequest(final UniqueID uniqueID,
        byte timerId) {
        TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.startXAndSkipSendRequest(timerId);
    }

    /**
     * Stops the timer specified by an id, and enables the send request timers and its sons.
     * @param uniqueID The unique id of the timer providable object
     * @param timerId The id of timer to stop
     */
    public static final void stopXAndUnskipSendRequest(
        final UniqueID uniqueID, byte timerId) {
        TimerProvidable timerProvidable = TimerWarehouse.timerProvidableStore.get(uniqueID);
        if (timerProvidable == null) {
            return;
        }
        timerProvidable.stopXAndUnskipSendRequest(timerId);
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
