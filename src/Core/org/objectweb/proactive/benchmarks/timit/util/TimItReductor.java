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
import java.util.ArrayList;

import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventData;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventDataBag;


/**
 * This class is an active object used by TimItManager to retrieve timers from
 * all workers. It localized on Startable instance node
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class TimItReductor implements ProActiveInternalObject, Serializable {

    /**
     *
     */

    /** timeout (in seconds) when gatehering statistics from Timeds */
    public static final int TIMEOUT = 5000;
    private transient Service service;
    private int nbReceived;
    private HierarchicalTimer timer;
    private String information;
    private int groupSize;
    private EventDataBag eventDataBag;
    private BenchmarkStatistics bstats;

    /**
     * List of vectors of StatDatas (each subject can handle several observers
     * thus StatDatas)
     */
    private ArrayList<EventDataBag> bagList = new ArrayList<EventDataBag>();
    private static boolean error = false;

    /**
     * Singleton pattern
     */
    public TimItReductor() {
        this.bstats = null;
    }

    /**
     * Used by MigratableCounter as a network clock
     *
     * @return
     */
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Invoked by Timed instances when finalizing
     *
     * @param receivedTimer
     */
    public void receiveTimer(HierarchicalTimer receivedTimer) {
        if (this.nbReceived == 0) {
            this.timer = receivedTimer;
        }
        this.timer.addInstance(receivedTimer);
    }

    private void handleInformation(String receivedInformation) {
        if ((this.information == null) || (this.information.length() == 0)) {
            this.information = receivedInformation;
        } else {
            this.information += ("\n" + receivedInformation);
        }
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    /**
     * Invoked by TimIt beteween each run
     */
    public void clean() {
        this.bstats = null;
        this.groupSize = 0;
        this.nbReceived = 0;
        this.eventDataBag = null;
        this.information = null;
        this.timer = null;
        this.bagList = new ArrayList<EventDataBag>();
    }

    /**
     * Invoked by TimItManager when Startable call finalizeStats()
     *
     * @param groupSize
     *            the size of the Timed group
     * @return a BenchmarkStatistics
     */
    public BenchmarkStatistics getStatistics() {
        // Check if already computed
        if (this.bstats != null) {
            return this.bstats;
        }
        this.service = new Service(PAActiveObject.getBodyOnThis());

        // Wait for the groupSize
        while (this.groupSize == 0) {
            this.service.blockingServeOldest();
        }

        HierarchicalTimerStatistics hts;
        EventStatistics events;

        while ((this.nbReceived < this.groupSize) && !TimItReductor.error) {
            this.service.blockingServeOldest(TimItReductor.TIMEOUT);
        }

        // Timer counters statistics
        if (this.timer == null) {
            hts = new HierarchicalTimerStatistics();
        } else {
            hts = this.timer.getStats();
        }

        // Event observers statisitics
        EventDataBag sdb = this.collapseEventData();
        this.eventDataBag = sdb;
        if (sdb == null) {
            events = new EventStatistics();
        } else {
            events = sdb.getStats();
        }

        this.bstats = new BenchmarkStatistics(hts, events, this.information);
        return this.bstats;
    }

    /**
     * Invoked by TimIt if an application timeout occur
     */
    public static void stop() {
        TimItReductor.error = true;
    }

    /**
     * Invoked by TimIt if an application timeout occur
     */
    public static void ready() {
        TimItReductor.error = false;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Specialized Observer/Observable pattern handling
    // //////////////////////////////////////////////////////////////////////////

    /**
     * This method will be called by a worker ie an observable object to reduce
     * its vector of EventDatas. Important note : The order of each
     * eventDataVector is preserved between workers.
     */
    public void receiveAll(EventDataBag eventDataBag, HierarchicalTimer receivedTimer,
            String receivedInformation) {
        if (eventDataBag != null) {
            this.bagList.add(eventDataBag);
        }

        if (receivedTimer != null) {
            this.receiveTimer(receivedTimer);
        }

        if (receivedInformation != null) {
            this.handleInformation(receivedInformation);
        }

        this.nbReceived++;
    }

    /**
     * Collapse all EventData results from all Timeds
     *
     * @return an EventDataBag containing collapsed results
     */
    private EventDataBag collapseEventData() {
        if (this.bagList.size() == 0) {
            return null;
        }

        if ((this.nbReceived < this.groupSize) && !TimItReductor.error) {
            this.service.blockingServeOldest(TimItReductor.TIMEOUT);
        }
        int i;
        int j;

        // This first bag will contain all collapsed StatDatas
        EventDataBag firstBag = this.bagList.get(0);

        if (firstBag == null) {
            System.out.println("The first bag is null inside method " + "TimItReductor.collapseStatData !");
            return null;
        }

        // The EventData to collapse
        EventData data;

        if (this.bagList.size() == 1) {
            for (i = 0; i < firstBag.size(); i++) {
                data = firstBag.getEventData(i);
                data.collapseWith(data, firstBag.getSubjectRank());
            }
            return firstBag;
        }

        EventDataBag anotherBag;

        // Iterate through elements of the firstBag
        for (i = 0; i < firstBag.size(); i++) {
            // Get the data to collapse with
            data = firstBag.getEventData(i);
            // Iterate through other bags
            for (j = 1; j < this.bagList.size(); j++) {
                anotherBag = this.bagList.get(j);
                data.collapseWith(anotherBag.getEventData(i), anotherBag.getSubjectRank());
            }
        }
        return firstBag;
    }

    /**
     *
     * @return the EventDataBag of collapsed results
     */
    public EventDataBag getEventDataBag() {
        return this.eventDataBag;
    }

    public void terminate() {
        PAActiveObject.terminateActiveObject(true);
    }
}
