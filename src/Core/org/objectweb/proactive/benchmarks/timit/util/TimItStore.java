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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.core.config.PAProperties;


/**
 * This class is useful to share TimerCounter and EventObserver instances
 * between classes on the same Body (or VM if app is not in a ProActive context)
 *
 * @author The ProActive Team
 */
public class TimItStore {
    private static TimItStore vmInstance;
    private static HashMap<Body, TimItStore> timerStoreMap = new HashMap<Body, TimItStore>();
    private String[] activation;
    private boolean allActivated;
    private Timed timed;
    private ArrayList<TimerCounter> tcList;

    /**
     * Used by getInstance to create an unique instance per Body
     */
    private TimItStore(Timed timed) {
        String prop = PAProperties.PA_TIMIT_ACTIVATION.getValue();
        if (prop == null) {
            this.activation = new String[0];
        } else if (prop.equals("all")) {
            this.allActivated = true;
        } else {
            this.activation = prop.split(",");
        }
        Arrays.sort(this.activation);
        //        System.err.println("proactive.timit.activation = " + Arrays.toString(this.activation));
        this.tcList = new ArrayList<TimerCounter>();
        this.timed = timed;
    }

    /**
     * Get a TimerStore instance for the current Body, or the VM if we are
     * not on an active object (ProActive context)
     *
     * @return an instance of TimerStore
     */
    synchronized public static TimItStore getInstance(Timed timed) {
        Body body = PAActiveObject.getBodyOnThis();

        if (body == null) {
            if (vmInstance == null) {
                vmInstance = new TimItStore(timed);
            }
            return vmInstance;
        }
        TimItStore ts = TimItStore.timerStoreMap.get(body);
        if (ts == null) {
            ts = new TimItStore(timed);
            TimItStore.timerStoreMap.put(body, ts);
        }
        return ts;
    }

    public TimerCounter addTimerCounter(TimerCounter tc) {
        if (this.allActivated || Arrays.binarySearch(this.activation, tc.getName()) >= 0) {
            this.tcList.add(tc);
        }
        return tc;
    }

    public EventObserver addEventObserver(EventObserver eo) {
        if (this.allActivated || Arrays.binarySearch(this.activation, eo.getName()) > 0) {
            this.timed.getEventObservable().addObserver(eo);
        }
        return eo;
    }

    public void activation() {
        TimerCounter[] tcActivate = this.tcList.toArray(new TimerCounter[0]);
        this.timed.activate(tcActivate);
    }
}
