/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
 *                                                                        Javier Bustos
 * ################################################################
 */
package org.objectweb.proactive.loadbalancing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class LoadMonitorLinux extends LoadMonitor {
    private double oldTotal;
    private double oldUsed;
    ;
    private int[] secLoad;
    private RandomAccessFile statfile;

    public LoadMonitorLinux(LoadBalancer lb) {
        this.lb = lb;
        load = 0;

        String line = null;
        try {
            statfile = new RandomAccessFile("/proc/stat", "r");
            statfile.seek(5);
            line = statfile.readLine();

            long user;
            long nice;
            long system;
            long idle;
            java.util.StringTokenizer st = new java.util.StringTokenizer(line,
                    " ");
            user = Long.parseLong(st.nextToken());
            nice = Long.parseLong(st.nextToken());
            system = Long.parseLong(st.nextToken());
            idle = Long.parseLong(st.nextToken());

            oldTotal = user + nice + system + idle;
            oldUsed = user + nice + system;

            load = 1;
            calculateLoad();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized double getLoad() {
        long user;
        long nice;
        long system;
        long idle;
        double totalTime;
        double realUsedTime;
        String cpuLine = null;
        try {
            // skip "cpu"
            statfile.seek(5);
            cpuLine = statfile.readLine();
        } catch (IOException e) {
            return 1;
        }

        // read "cpu x x x x"           
        java.util.StringTokenizer st = new java.util.StringTokenizer(cpuLine,
                " ");
        user = Long.parseLong(st.nextToken());
        nice = Long.parseLong(st.nextToken());
        system = Long.parseLong(st.nextToken());
        idle = Long.parseLong(st.nextToken());

        // compute load
        totalTime = (user + nice + system + idle) - oldTotal;
        realUsedTime = (user + nice + system) - oldUsed;
        if (totalTime < 1) {
            totalTime = realUsedTime;
        }
        double thisLoad = realUsedTime / (totalTime + 1);
        oldTotal = user + nice + system + idle;
        oldUsed = user + nice + system;
        return thisLoad;
    }

    protected synchronized void calculateLoad() {
        double newload = getLoad();

        newload = ((0.7 * newload) + (0.3 * load));
        load = newload;
    }
}
