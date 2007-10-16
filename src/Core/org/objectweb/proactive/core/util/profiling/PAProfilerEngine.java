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

import java.util.ArrayList;
import java.util.Iterator;

import timer.AverageMicroTimer;


/**
 * The Engine used for profiling code
 * It creates profilers on request and keep them on a list
 * It also registers itself to run when the JVM shutdowns and dump the results of the profilers
 */
public class PAProfilerEngine implements Runnable {
    ArrayList<Timer> profilerList = new ArrayList<Timer>();
    private static PAProfilerEngine engine;

    static {
        engine = new PAProfilerEngine();
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(engine));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a profiler of default type
     * @return an AverageTimeProfiler
     */
    public static Timer createTimer() {
        Timer tmp = new AverageMicroTimer();
        registerTimer(tmp);
        return tmp;
    }

    /**
     * Add profilers to be managed by this profiler engine
     * @param papr
     */
    public static void registerTimer(Timer papr) {
        synchronized (engine.profilerList) {
            engine.profilerList.add(papr);
        }
    }

    /**
     * Remove a profiler from this engine
     * It's dump() method will thus never be called
     * @param papr
     */
    public static boolean removeTimer(Timer papr) {
        synchronized (engine.profilerList) {
            return engine.profilerList.remove(papr);
        }
    }

    private PAProfilerEngine() {
    }

    /**
     * This method starts when a shutdown of the VM is initiated
     */
    public void run() {
        dump();
    }

    /**
     * Call dump on all profilers registered in this engine
     */
    public void dump() {
        Iterator<Timer> it = profilerList.iterator();
        while (it.hasNext()) {
            it.next().dump();
        }
    }

    public static void main(String[] args) {
        System.out.println("Creating a profiler and registering it");
        PAProfilerEngine.createTimer();
        System.out.println("Creating an AverageTimeProfiler and registering it");
        Timer avg = new AverageMicroTimer("Test ");
        PAProfilerEngine.registerTimer(avg);

        for (int i = 0; i < 10; i++) {
            avg.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            avg.stop();
        }
        System.out.println("Now dying");
    }
}
