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
package functionalTests.hpc.exchange;

import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.extra.hpc.exchange.Exchanger;


public class A {
    public static int QUARTER_SIZE = 10000;
    private double[] array;
    private Exchanger exchanger;

    public A() {
    }

    public void doubleExchange() {
        try {
            Thread.sleep((long) Math.random() * 5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int startIndice, destIndice, destRank;
        this.exchanger = Exchanger.getExchanger();
        startIndice = PASPMD.getMyRank() * QUARTER_SIZE;

        // Fill in my array
        array = new double[4 * QUARTER_SIZE];
        for (int i = startIndice; i < startIndice + QUARTER_SIZE; i++) {
            array[i] = Math.random();
        }

        // Step #1 : Exchange with my local neighbor
        destRank = PASPMD.getMyRank() ^ (1 << 0);
        destIndice = destRank * QUARTER_SIZE;
        exchanger.exchange("step1", destRank, array, startIndice, array, destIndice, QUARTER_SIZE);

        // Step #2 : Exchange with my distant neighbor
        destRank = PASPMD.getMyRank() ^ (1 << 1);
        startIndice = PASPMD.getMyRank() < 2 ? 0 : 2 * QUARTER_SIZE;
        destIndice = PASPMD.getMyRank() >= 2 ? 0 : 2 * QUARTER_SIZE;
        exchanger.exchange("step2", destRank, array, startIndice, array, destIndice, 2 * QUARTER_SIZE);
    }

    public double[] getArray() {
        return array;
    }
}
