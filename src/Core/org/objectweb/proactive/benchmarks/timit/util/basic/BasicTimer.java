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
package org.objectweb.proactive.benchmarks.timit.util.basic;


/**
 * A simple timer in nano seconds.
 *
 * @author vbodnart
 */
public final class BasicTimer implements java.io.Serializable {

    /** A static variable to switch to debug mode */
    public static final boolean DEBUG = false; //ProActiveLogger.getLogger(Loggers.BODY).isDebugEnabled();    

    /** The name of the timer */
    private String name;

    /** The start time */
    private long startTime;

    /** The total time */
    private long totalTime;

    /** The reference to the parent timer */
    private BasicTimer parent;

    /** The number of performed start stop couples is increased by 1 at each stop call */
    private int startStopCoupleCount;

    /** A boolean to know if the timer is started (true) or not (false) */
    private boolean isStarted;

    /**
     * Creates an instance of a SimpleTimer.
     * @param name The name of the timer
     * @param parent A reference to the parent timer
     */
    public BasicTimer(String name, BasicTimer parent) {
        this.name = name;
        this.startTime = 0L;
        this.totalTime = 0L;
        this.parent = parent;
        this.startStopCoupleCount = 0;
        this.isStarted = false;
    }

    /**
     * Starts the current timer.
     * In DEBUG mode this method checks if the parent timer is not started if not
     * it throws a RuntimeException. If the current timer is already started
     * it throws also a RuntimeException.
     */
    public void start() {
        if (DEBUG) {
            // Check if the parent timer is started		
            if ((this.parent != null) && !this.parent.isStarted) {
                throw new RuntimeException(
                    "** _____ TimIt [SimpleTimer.start()] : The parent timer of " +
                    this.name + " timer is not started. Hierarchy is broken.");
            }

            // Check if this timer is not started twice
            if (isStarted) {
                throw new RuntimeException(
                    "** _____ TimIt [SimpleTimer.start()] : The timer " +
                    this.name +
                    " is already started. You cannot start it twice.");
            }
        }
        this.isStarted = true;
        this.startTime = System.nanoTime();
    }

    /**
     * Stop the current timer.
     * In DEBUG mode this method checks if the parent timer is not started if not
     * it throws a RuntimeException. If the current timer is not started
     * it throws also a RuntimeException.
     */
    public void stop() {
        if (DEBUG) {
            if ((this.parent != null) && !this.parent.isStarted) {
                throw new RuntimeException(
                    "** _____ TimIt [SimpleTimer.stop()] : The parent timer of " +
                    this.name + " timer is not started. Hierarchy is broken");
            }

            // Check if this timer is started
            if (!isStarted) {
                throw new RuntimeException(
                    "** _____ TimIt [SimpleTimer.stop()] : The timer " +
                    this.name +
                    " is not started. You cannot stop an unstarted timer.");
            }
        }
        this.isStarted = false;
        // Count the number of start stop couples
        this.startStopCoupleCount++;
        this.totalTime += (System.nanoTime() - this.startTime);
    }

    /**
     * Returns the total time of this timer.
     * Checks if the timer was stopped, if not throws a RuntimeException.
     * @return The total time in nanoseconds.
     */
    public long getTotalTime() {
        if (DEBUG) {
            if (isStarted) {
                throw new RuntimeException(
                    "** _____ TimIt [SimpleTimer.stop()] : The timer " +
                    this.name + " was not stopped.");
            }
        }
        return this.totalTime;
    }

    /**
     * Checks if the current timer is started of not.
     * @return True if this timer is started.
     */
    public boolean isStarted() {
        return this.isStarted;
    }

    /**
     * Cancels the last start performed on this timer (erases the start time only).
     */
    public void undoStart() {
        if (DEBUG) {
            if (isStarted) {
                throw new RuntimeException(
                    "** _____ TimIt [SimpleTimer.undoStart()] : The timer " +
                    this.name + " was not stopped.");
            }
        }
        this.startTime = 0L;
        this.isStarted = false;
    }

    /**
     * Completely resets the timer. (erases the total time and the start stop couple count)
     */
    public void reset() {
        if (DEBUG) {
            if (isStarted) {
                throw new RuntimeException(
                    "** _____ TimIt [SimpleTimer.reset()] : The timer " +
                    this.name + " was not stopped.");
            }
        }
        this.totalTime = 0L;
        this.startStopCoupleCount = 0;
    }

    /**
     * Sets the total time for this timer.
     * @param totalTime The new total time value
     */
    public void setTotal(long totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * Sets a new parent to the current timer.
     * @param parentTimer
     */
    public void setParent(BasicTimer parentTimer) {
        this.parent = parentTimer;
    }

    /**
     * Method to know the parent of the current timer.
     * @return The parent timer of this timer.
     */
    public BasicTimer getParent() {
        return this.parent;
    }

    /**
     * The name of the current timer.
     * @return The name of the current timer
     */
    public String getName() {
        return this.name;
    }

    /**
     * The toString method of this timer.
     * @return A description of the current timer.
     */
    public String toString() {
        return "" + this.name + " " + "\t totalTime in milliseconds : " +
        (this.totalTime / 1000000L) + " " + "\t in nanoseconds : " +
        this.totalTime + " " +
        ((this.parent == null) ? "" : ("\t parent : " + this.parent.getName()));
    }

    /**
     * Returns the number of start stop couples of this timer.
     * @return The number of start stop
     */
    public int getStartStopCoupleCount() {
        return startStopCoupleCount;
    }

    /**
     * Sets the number of start stop couples of this timer.
     * @param startStopCoupleCount
     */
    public void setStartStopCoupleCount(int startStopCoupleCount) {
        this.startStopCoupleCount = startStopCoupleCount;
    }
}
