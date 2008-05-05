/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.benchmarks.NAS.util;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;


/**
 * A simple active object useful to make some reduce operations
 *
 */
public class Communicator implements Serializable {

    private double total, max, min;
    private int[] maxArray;

    private int received;
    private int groupSize;

    private double sumResult, maxResult, minResult;
    private int[] maxIResultArray;

    private Body body;

    /* ProActive singleton */
    public Communicator() {
    }

    public Communicator(Integer groupSize) {
        this.groupSize = groupSize;
        this.init();
    }

    public void setup() {
        this.body = PAActiveObject.getBodyOnThis();
    }

    public void init() {
        this.total = 0d;
        this.received = 0;
        this.max = Double.MIN_VALUE;
        this.min = Double.MAX_VALUE;
    }

    /**
     * Summing double values
     * @param value
     * @return the sum
     */
    public double sum(double value) {
        this.sumResult = Double.NEGATIVE_INFINITY;
        received++;
        total += value;
        while (received < this.groupSize && this.sumResult == Double.NEGATIVE_INFINITY) {
            this.blockingServe();
        }
        if (this.sumResult == Double.NEGATIVE_INFINITY) {
            this.sumResult = this.total;
            this.init();
        }
        return sumResult;
    }

    /**
     * Sum and Max
     * @param valueToSum
     * @param valueToMax
     * @return
     */
    public double[] sumAndMax(double valueToSum, double valueToMax) {
        this.sumResult = Double.NEGATIVE_INFINITY;
        received++;
        this.total += valueToSum;

        if (this.max < valueToMax) {
            this.max = valueToMax;
        }

        while (received < this.groupSize && this.sumResult == Double.NEGATIVE_INFINITY) {
            this.blockingServe();
        }

        if (this.sumResult == Double.NEGATIVE_INFINITY) {
            this.sumResult = this.total;
            this.maxResult = this.max;
            this.init();
        }
        return new double[] { this.sumResult, this.maxResult };
    }

    /**
     * Sum and Min
     * @param valueToSum
     * @param valueToMin
     * @return
     */
    public double[] sumAndMin(double valueToSum, double valueToMin) {
        this.sumResult = Double.POSITIVE_INFINITY;
        received++;
        this.total += valueToSum;

        // Handle max
        if (this.min > valueToMin) {
            this.min = valueToMin;
        }

        while (received < this.groupSize && this.sumResult == Double.POSITIVE_INFINITY) {
            this.blockingServe();
        }

        if (this.sumResult == Double.POSITIVE_INFINITY) {
            this.sumResult = this.total;
            this.minResult = this.min;
            this.init();
        }
        return new double[] { this.sumResult, this.minResult };
    }

    /**
     * Min double values
     * @param value
     * @return the sum
     */
    public double min(double value) {
        this.minResult = Double.POSITIVE_INFINITY;
        received++;
        if (this.min > value) {
            this.min = value;
        }
        while (received < this.groupSize && this.minResult == Double.POSITIVE_INFINITY) {
            this.blockingServe();
        }
        if (this.minResult == Double.POSITIVE_INFINITY) {
            this.minResult = this.min;
            this.init();
        }
        return minResult;
    }

    /**
     * Max double values
     * @param value
     * @return the sum
     */
    public double max(double value) {
        this.maxResult = Double.NEGATIVE_INFINITY;
        received++;
        if (this.max < value) {
            this.max = value;
        }
        while (received < this.groupSize && this.maxResult == Double.NEGATIVE_INFINITY) {
            this.blockingServe();
        }
        if (this.maxResult == Double.NEGATIVE_INFINITY) {
            this.maxResult = this.max;
            this.init();
        }
        return maxResult;
    }

    /**
     * Max int[] values
     * @param value
     * @return the sum
     */
    public int[] max(int[] value) {
        this.maxIResultArray = null;
        if (received == 0) {
            this.maxArray = new int[value.length];
            for (int i = 0; i < value.length; i++) {
                this.maxArray[i] = Integer.MIN_VALUE;
            }
        }
        received++;
        for (int i = 0; i < value.length; i++) {
            if (this.maxArray[i] < value[i]) {
                this.maxArray[i] = value[i];
            }
        }
        while (received < this.groupSize && this.maxIResultArray == null) {
            this.blockingServe();
        }
        if (this.maxIResultArray == null) {
            this.maxIResultArray = this.maxArray;
            this.init();
        }
        return maxIResultArray;
    }

    /**
     * Forces the associated worker Used to block the treatment of the requestQueue.
     */
    private final void blockingServe() {
        body.serve(body.getRequestQueue().blockingRemoveOldest());
    }
}
