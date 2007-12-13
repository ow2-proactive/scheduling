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
package org.objectweb.proactive.extensions.calcium.monitor;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.Calcium;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobal;


/**
 * This class provides a simple monitor that periodically queries the Calcium
 * framework and prints the results.
 *
 *  * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class SimpleLogMonitor implements Monitor {
    Calcium calcium;
    int frequency;
    MonitoringThread thread;

    /**
     * The constructor.
     *
     * @param calcium The framework to query.
     * @param frequency The number of seconds to wait between each query.
     */
    public SimpleLogMonitor(Calcium calcium, int frequency) {
        this.calcium = calcium;
        this.frequency = frequency;
        thread = null;
    }

    /**
     * @see {@link Monitor#stop()}
     */
    public void stop() {
        thread.myStop();
        thread.interrupt();
        thread = null;
    }

    /**
     * @see {@link Monitor#start()}
     */
    public void start() {
        if (thread != null) {
            return;
        }

        thread = new MonitoringThread();
        thread.start();
    }

    class MonitoringThread extends Thread {
        boolean stopped;

        public MonitoringThread() {
            super();
            stopped = false;
        }

        public void myStop() {
            stopped = true;
        }

        @Override
        public void run() {
            while (!stopped) {
                try {
                    Thread.sleep(frequency * 1000);
                } catch (InterruptedException e) {
                    return;
                }

                StatsGlobal stats = calcium.getStatsGlobal();
                System.out.println("[" + System.currentTimeMillis() + "]" + stats);
            }
        }
    }
}
