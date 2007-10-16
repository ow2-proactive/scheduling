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
package org.objectweb.proactive.loadbalancing.metrics.CPURanking;

import java.io.*;

import org.objectweb.proactive.loadbalancing.LoadBalancer;
import org.objectweb.proactive.loadbalancing.LoadBalancingConstants;
import org.objectweb.proactive.loadbalancing.metrics.Metric;


public class LinuxCPURanking implements Metric {
    private double ranking = 1;
    private double normaLoad;
    private RandomAccessFile statfile;

    /**
     * This method returns sets the machine ranking, in our implementation is the CPU clock
     */
    public LinuxCPURanking() {
        BufferedReader br;
        String line = null;
        try {
            br = new BufferedReader(new FileReader("/proc/cpuinfo"));

            while ((line = br.readLine()) != null) {
                if (line.startsWith("cpu MHz")) {
                    String[] splited = line.split(":");
                    double cpuClock = Double.parseDouble(splited[1]); // obtaining the cpu clock
                    ranking = cpuClock / 1000;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.normaLoad = 1.0;
        //load = 0;
        int nProcessors = 0;
        try {
            nProcessors = Runtime.getRuntime().availableProcessors();
            if (nProcessors > 1) {
                this.normaLoad = 1 / (1.0 * nProcessors);
            }
            statfile = new RandomAccessFile("/proc/loadavg", "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void setRanking(double x) {
        ranking = x;
    }

    public void addToRanking(double x) {
        ranking += x;
    }

    /* -- IMPLEMENTS METRIC -- */

    /**
     * This method returns if the machine ranking
     */
    public double getRanking() {
        return ranking;
    }

    public void takeDecision(LoadBalancer lb) {
        double load = getLoad();
        if (load > LoadBalancingConstants.OVERLOADED_THREASHOLD) {
            lb.startBalancing();
        } else if (load < LoadBalancingConstants.UNDERLOADED_THREASHOLD) {
            lb.stealWork();
        }
    }

    public double getLoad() {
        String cpuLine = null;
        try {
            statfile.seek(0);
            cpuLine = statfile.readLine();
        } catch (IOException e) {
            return 1;
        }

        double min1;

        java.util.StringTokenizer st = new java.util.StringTokenizer(cpuLine,
                " ");
        min1 = Double.parseDouble(st.nextToken());

        return min1 * normaLoad;
    }
}
