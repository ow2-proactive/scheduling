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

/**
 * Kernel IS
 * 
 * A large integer sort. This kernel performs a sorting operation that is
 * important in "particle method" codes. It tests both integer computation
 * speed and communication performance.
 */
public interface ISClasses {

    /**
     * Benchmark name
     */
    public static String KERNEL_NAME = "IS";
    public String OPERATION_TYPE = "keys ranked";

    /**
     * Common defs
     */
    public static final int MAX_ITERATIONS = 10;
    public static final int TEST_ARRAY_SIZE = 5;

    /*************/

    /*  CLASS S  */

    /*************/
    public static final char S_CLASS_NAME = 'S';
    public static final int S_TOTAL_KEYS_LOG_2 = 16;
    public static final int S_MAX_KEY_LOG_2 = 11;
    public static final int S_NUM_BUCKETS_LOG_2 = 9;
    public static int[] S_test_index_array = { 48427, 17148, 23627, 62548, 4431 };
    public static int[] S_test_rank_array = { 0, 18, 346, 64917, 65463 };

    /*************/

    /*  CLASS W  */

    /*************/
    public static final char W_CLASS_NAME = 'W';
    public static final int W_TOTAL_KEYS_LOG_2 = 20;
    public static final int W_MAX_KEY_LOG_2 = 16;
    public static final int W_NUM_BUCKETS_LOG_2 = 10;
    public static int[] W_test_index_array = { 357773, 934767, 875723, 898999, 404505 };
    public static int[] W_test_rank_array = { 1249, 11698, 1039987, 1043896, 1048018 };

    /*************/

    /*  CLASS A  */

    /*************/
    public static final char A_CLASS_NAME = 'A';
    public static final int A_TOTAL_KEYS_LOG_2 = 23;
    public static final int A_MAX_KEY_LOG_2 = 19;
    public static final int A_NUM_BUCKETS_LOG_2 = 10;
    public static int[] A_test_index_array = { 2112377, 662041, 5336171, 3642833, 4250760 };
    public static int[] A_test_rank_array = { 104, 17523, 123928, 8288932, 8388264 };

    /*************/

    /*  CLASS B  */

    /*************/
    public static final char B_CLASS_NAME = 'B';
    public static final int B_TOTAL_KEYS_LOG_2 = 25;
    public static final int B_MAX_KEY_LOG_2 = 21;
    public static final int B_NUM_BUCKETS_LOG_2 = 10;
    public static int[] B_test_index_array = { 41869, 812306, 5102857, 18232239, 26860214 };
    public static int[] B_test_rank_array = { 33422937, 10244, 59149, 33135281, 99 };

    /*************/

    /*  CLASS C  */

    /*************/
    public static final char C_CLASS_NAME = 'C';
    public static final int C_TOTAL_KEYS_LOG_2 = 27;
    public static final int C_MAX_KEY_LOG_2 = 23;
    public static final int C_NUM_BUCKETS_LOG_2 = 10;
    public static int[] C_test_index_array = { 44172927, 72999161, 74326391, 129606274, 21736814 };
    public static int[] C_test_rank_array = { 61147, 882988, 266290, 133997595, 133525895 };

    /*************/

    /*  CLASS D  */

    /*************/
    public static final char D_CLASS_NAME = 'D';
    public static final int D_TOTAL_KEYS_LOG_2 = 31;
    public static final int D_MAX_KEY_LOG_2 = 27;
    public static final int D_NUM_BUCKETS_LOG_2 = 10;
    public static int[] D_test_index_array = { 44172927, 72999161, 74326391, 129606274, 21736814 };
    public static int[] D_test_rank_array = { 974930, 14139196, 4271338, 133997595, 133525895 };
}
