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

import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;

import java.io.Serializable;


/**
 * Kernel IS
 * 
 * A large integer sort. This kernel performs a sorting operation that is
 * important in "particle method" codes. It tests both integer computation
 * speed and communication performance.
 */
public class ISProblemClass extends NASProblemClass implements Serializable {

    public int[] test_index_array;
    public int[] test_rank_array;

    /**
     * Class instance of kernel IS
     */
    public int NUM_KEYS;

    /**
     * Number of iteration
     */
    public int MAX_ITERATIONS;

    /**
     * Array's size for partial verification
     */
    public int TEST_ARRAY_SIZE;
    public int TOTAL_KEYS_LOG_2;
    public int MAX_KEY_LOG_2;
    public int NUM_BUCKETS_LOG_2;
    public long TOTAL_KEYS;
    public int MAX_KEY;
    public int NUM_BUCKETS;
    public int SIZE_OF_BUFFERS;
}
