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
package org.objectweb.proactive.benchmarks.NAS.IS;

import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;


/**
 * Kernel IS
 * 
 * A large integer sort. This kernel performs a sorting operation that is
 * important in "particle method" codes. It tests both integer computation
 * speed and communication performance.
 */
public class AllBucketSize implements Serializable {

    private WorkerIS typedGroup;
    private int groupSize;
    private int received;
    private int[] table;

    //
    //---------- CONSTRUCTORS -------------------------------------------------
    //
    public AllBucketSize() {
    }

    public AllBucketSize(WorkerIS workersGroup, Integer groupSize, Integer arraySize) {
        this.table = new int[arraySize.intValue()];
        this.typedGroup = workersGroup;
        this.groupSize = groupSize;
        this.received = 0;
    }

    //
    //----------- PUBLIC METHODS ----------------------------------------------
    //
    /**
     * Setting the group of workers
     */
    public void setWorkers(WorkerIS workersGroup) {
        this.typedGroup = workersGroup;
    }

    /**
     * Compute the sum of n operands and send it to the SPMD group
     *
     * @param tab one operand of the sum operation
     */
    public void sum(int[] tab) {
        for (int i = 0; i < tab.length; i++) {
            this.table[i] += tab[i];
        }

        this.received++;

        if (this.groupSize == this.received) {
            this.typedGroup.receiveBucketSizeTotal(this.table);
            java.util.Arrays.fill(this.table, 0);
            this.received = 0;
        }
    }

    /**
     * Kill this active object
     */
    public void destroy() {
        PAActiveObject.getBodyOnThis().terminate();
    }
}
