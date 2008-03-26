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
package org.objectweb.proactive.benchmarks.timit.util;

import java.io.Serializable;

import org.objectweb.proactive.benchmarks.timit.util.observing.EventDataBag;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObservable;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.FakeEventObservable;
import org.objectweb.proactive.benchmarks.timit.util.observing.RealEventObservable;


/**
 * All timed objects (like workers) must extends this class. It provide some
 * useful methods to reduce timers between workers and generate statistics.
 *
 * @author The ProActive Team
 */
public class Timed implements Serializable {

    /**
     *
     */
    private HierarchicalTimer timer;
    private EventObservable delegatedObservable;
    private TimItReductor timitReductor;

    /**
     * Singleton pattern
     */
    public Timed() {
        if (true) { // Here we can switch to a fake observable
            this.delegatedObservable = new RealEventObservable();
        } else {
            this.delegatedObservable = new FakeEventObservable();
        }
    }

    /**
     * Do not activate any counter
     */
    public void activate() {
        this.activate(null, null);
    }

    public void activate(TimerCounter[] counters) {
        this.activate(counters, null);
    }

    public void activate(EventObserver[] events) {
        this.activate(null, events);
    }

    /**
     * Activate only some TimerCounters and EventObservers
     *
     * @param counters
     *            counters to activate
     * @param events
     *            event to activate
     */
    public void activate(TimerCounter[] counters, EventObserver[] events) {
        if (counters != null) {
            this.timer = new HierarchicalTimer();
            this.timer.activateCounters(counters, this.timitReductor);
        }
        if (events != null) {
            for (EventObserver element : events) {
                this.delegatedObservable.addObserver(element);
            }
        }
    }

    public void activateDebug(TimerCounter[] counters) {
        this.activateDebug(counters, null);
    }

    public void activateDebug(EventObserver[] events) {
        this.activateDebug(null, events);
    }

    /**
     * Active only some counters. Debug version used to detect misplaced
     * start/stop/reset. For real test, use activateCounters()
     *
     * @param counters
     *            the array of counters you want to use
     * @param events
     *            the array of events you want to use
     */
    public void activateDebug(TimerCounter[] counters, EventObserver[] events) {
        System.out.println("\n\n\t !! BE CARREFUL : "
            + "Counters are activated with debug mode (slower) !! \n\n");

        if (counters != null) {
            this.timer = new SecuredHierarchicalTimer();
            this.timer.activateCounters(counters, this.timitReductor);
        }
        if (events != null) {
            for (EventObserver element : events) {
                this.delegatedObservable.addObserver(element);
            }
        }
    }

    public EventObservable getEventObservable() {
        return this.delegatedObservable;
    }

    /**
     * Reset all the value of the timer
     */
    public void resetTimer() {
        this.timer.resetTimer();
    }

    /**
     * Invoked by TimItReductor to inform Timed object that he is the
     * finalization reductor
     */
    public void setTimerReduction(TimItReductor red) {
        this.timitReductor = red;
    }

    /**
     * This method performs the EventData and the Timer reduction.
     *
     * @param rank
     *            an identification number for this timed object
     * @param information
     *            this message will be transmitted into results files
     */
    public void finalizeTimed(int rank, String information) {
        if (this.timitReductor == null) {
            return;
        }
        EventDataBag eventDataBag = this.delegatedObservable.getEventDataBag(rank);
        this.timitReductor.receiveAll(eventDataBag, this.timer, information);
    }
}
