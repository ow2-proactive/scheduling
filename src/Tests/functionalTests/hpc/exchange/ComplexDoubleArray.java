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

import org.objectweb.proactive.extra.hpc.exchange.ExchangeableDouble;


public class ComplexDoubleArray implements ExchangeableDouble {
    private double[] array;
    private int getPos, putPos;

    public ComplexDoubleArray(int size, boolean odd) {
        this.array = new double[size];
        this.getPos = odd ? 1 : 0;
        this.putPos = odd ? 0 : 1;
        for (int i = getPos; i < array.length; i += 2) {
            array[i] = Math.random();
        }
    }

    public double getChecksum() {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    public String toString() {
        return java.util.Arrays.toString(array);
    }

    @Override
    public double get() {
        double res = array[getPos];
        getPos += 2;
        return res;
    }

    @Override
    public boolean hasNextGet() {
        return getPos < array.length;
    }

    @Override
    public boolean hasNextPut() {
        return putPos < array.length;
    }

    @Override
    public void put(double value) {
        array[putPos] = value;
        putPos += 2;
    }
}
