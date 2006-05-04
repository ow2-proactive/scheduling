/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.loadbalancing;

import java.io.*;


/**
 * @author Javier.Bustos@sophia.inria.fr
 *
 */
public class LinuxCPURanking implements CPURanking {
    private double ranking = 1;

    /**
     * This method returns if the machine ranking
     */
    public double getRanking() {
        return ranking;
    }

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
    }

    public void setRanking(double x) {
        ranking = x;
    }

    public void addToRanking(double x) {
        ranking += x;
    }
}
