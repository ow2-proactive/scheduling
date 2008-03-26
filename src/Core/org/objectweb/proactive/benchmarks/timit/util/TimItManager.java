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

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventDataBag;


/**
 * TimItManager is used to manage timers between Timed instances TimItManager
 * instance should be created in application main class
 *
 * @author The ProActive Team
 */
public class TimItManager implements Serializable {

    /**
     *
     */
    private static TimItManager instance = new TimItManager();
    private BenchmarkStatistics benchStats;
    private TimItReductor timitReductor;
    private int groupSize;

    /**
     * Singleton pattern
     */
    private TimItManager() {
    }

    public static TimItManager getInstance() {
        return TimItManager.instance;
    }

    public void setTimitReductor(TimItReductor reductor) {
        this.timitReductor = reductor;
    }

    /**
     * Construct a TimItManager from a single Timed object (active or not), or a
     * typed group of Timeds
     *
     * @param timed
     *            a standard or an active Timed object or a typed group of
     *            Timeds
     */
    public void setTimedObjects(Timed timed) {
        if (this.timitReductor == null) {
            return;
        }
        if (PAGroup.isGroup(timed)) {
            this.groupSize = PAGroup.size(timed);
        } else {
            this.groupSize = 1;
        }
        timed.setTimerReduction(this.timitReductor);
        this.timitReductor.setGroupSize(this.groupSize);
    }

    /**
     * Construct a TimItManager from an array of Timed
     *
     * @param timed
     *            a Timed array
     */
    public void setTimedObjects(Timed[] timed) {
        if (this.timitReductor == null) {
            return;
        }
        this.groupSize = timed.length;
        for (int i = 0; i < this.groupSize; i++) {
            timed[i].setTimerReduction(this.timitReductor);
        }
        this.timitReductor.setGroupSize(this.groupSize);
    }

    /**
     * You may call this method at the end your launcher to perform timers
     * analysis and charts generation.<br>
     * This call is synchronous (it waiting for the workers results) Use
     * getBenchmarkStatistics() indeed
     *
     * @deprecated
     */
    @Deprecated
    public void finalizeStats() {
        if (this.timitReductor == null) {
            return;
        }
        this.benchStats = this.timitReductor.getStatistics();
        PAFuture.waitFor(this.benchStats);
    }

    /**
     * Useful if you want to retrieve collapsed data from an Event observer
     *
     * @return an EventDataBag
     */
    public EventDataBag getEventCollapsedBag() {
        if (this.timitReductor == null) {
            return null;
        }
        return this.timitReductor.getEventDataBag();
    }

    /**
     * Wait for the result of all Timed object
     *
     * @return all benchmark statistics (timers and events statistics)
     */
    public BenchmarkStatistics getBenchmarkStatistics() {
        if (this.timitReductor == null) {
            return null;
        }
        this.benchStats = this.timitReductor.getStatistics();
        PAFuture.waitFor(this.benchStats);
        return this.benchStats;
    }
}
