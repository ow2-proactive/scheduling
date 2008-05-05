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
package org.objectweb.proactive.benchmarks.NAS.FT;

/**
 * Kernel FT
 * 
 * A 3-D partial differential equation solution using FFTs. This kernel performs the essence of many
 * "spectral" codes. It is a rigorous test of long-distance communication performance.
 */
public interface FTClasses {

    /**
     * Benchmark name
     */
    public static String KERNEL_NAME = "FT";
    public String OPERATION_TYPE = "floating point";

    /**
     * Common defs
     */
    public static int LAYOUT_0D = 0;
    public static int LAYOUT_1D = 1;
    public static int LAYOUT_2D = 2;

    /** ********** */

    /* CLASS S */

    /** ********** */
    public static final char S_CLASS_NAME = 'S';
    public static final int S_NX = 64;
    public static final int S_NY = 64;
    public static final int S_NZ = 64;
    public static final int S_NITER = 6;
    public static final double[] vdata_real_s = new double[] { 0., 554.6087004964, 554.6385409189,
            554.6148406171, 554.5423607415, 554.4255039624, 554.2683411902 };
    public static final double[] vdata_imag_s = new double[] { 0., 484.5363331978, 486.5304269511,
            488.3910722336, 490.1273169046, 491.7475857993, 493.2597244941 };

    /** ********** */

    /* CLASS W */

    /** ********** */
    public static final char W_CLASS_NAME = 'W';
    public static final int W_NX = 128;
    public static final int W_NY = 128;
    public static final int W_NZ = 32;
    public static final int W_NITER = 6;
    public static final double[] vdata_real_w = new double[] { 0., 567.3612178944, 563.1436885271,
            559.4024089970, 556.0698047020, 553.0898991250, 550.4159734538 };
    public static final double[] vdata_imag_w = new double[] { 0., 529.3246849175, 528.2149986629,
            527.0996558037, 526.0027904925, 524.9400845633, 523.9212247086 };

    /** ********** */

    /* CLASS A */

    /** ********** */
    public static final char A_CLASS_NAME = 'A';
    public static final int A_NX = 256;
    public static final int A_NY = 256;
    public static final int A_NZ = 128;
    public static final int A_NITER = 6;
    public static final double[] vdata_real_a = new double[] { 0., 504.6735008193, 505.9412319734,
            506.9376896287, 507.7892868474, 508.5233095391, 509.1487099959 };
    public static final double[] vdata_imag_a = new double[] { 0., 511.4047905510, 509.8809666433,
            509.8144042213, 510.1336130759, 510.4914655194, 510.7917842803 };

    /** ********** */

    /* CLASS B */

    /** ********** */
    public static final char B_CLASS_NAME = 'B';
    public static final int B_NX = 512;
    public static final int B_NY = 256;
    public static final int B_NZ = 256;
    public static final int B_NITER = 20;
    public static final double[] vdata_real_b = new double[] { 0., 517.7643571579, 515.4521291263,
            514.6409228649, 514.2378756213, 513.9626667737, 513.7423460082, 513.5547056878, 513.3910925466,
            513.2470705390, 513.1197729984, 513.0070319283, 512.9070537032, 512.8182883502, 512.7393733383,
            512.6691062020, 512.6064276004, 512.5504076570, 512.5002331720, 512.4551951846, 512.4146770029 };
    public static final double[] vdata_imag_b = new double[] { 0., 507.7803458597, 508.8249431599,
            509.6208912659, 510.1023387619, 510.3976610617, 510.5948019802, 510.7404165783, 510.8576573661,
            510.9577278523, 511.0460304483, 511.1252433800, 511.1968077718, 511.2616233064, 511.3203605551,
            511.3735928093, 511.4218460548, 511.4656139760, 511.5053595966, 511.5415130407, 511.5744692211 };

    /** ********** */

    /* CLASS C */

    /** ********** */
    public static final char C_CLASS_NAME = 'C';
    public static final int C_NX = 512;
    public static final int C_NY = 512;
    public static final int C_NZ = 512;
    public static final int C_NITER = 20;
    public static final double[] vdata_real_c = new double[] { 0., 519.5078707457, 515.5422171134,
            514.4678022222, 514.0150594328, 513.7550426810, 513.5811056728, 513.4569343165, 513.3651975661,
            513.2955192805, 513.2410471738, 513.1971141679, 513.1605205716, 513.1290734194, 513.1012720314,
            513.0760908195, 513.0528295923, 513.0310107773, 513.0103090133, 512.9905029333, 512.9714421109 };
    public static final double[] vdata_imag_c = new double[] { 0., 514.9019699238, 512.7578201997,
            512.2251847514, 512.1090289018, 512.1143685824, 512.1496764568, 512.1870921893, 512.2193250322,
            512.2454735794, 512.2663649603, 512.2830879827, 512.2965869718, 512.3075927445, 512.3166486553,
            512.3241541685, 512.3304037599, 512.3356167976, 512.3399592211, 512.3435588985, 512.3465164008 };

    /** ********** */

    /* CLASS D */

    /** ********** */
    public static final char D_CLASS_NAME = 'D';
    public static final int D_NX = 2048;
    public static final int D_NY = 1024;
    public static final int D_NZ = 1024;
    public static final int D_NITER = 25;
    public static final double[] vdata_real_d = new double[] { 0., 512.2230065252, 512.0463975765,
            511.9865766760, 511.9518799488, 511.9269088223, 511.9082416858, 511.8943814638, 511.8842385057,
            511.8769435632, 511.8718203448, 511.8683569061, 511.8661708593, 511.8649768950, 511.8645605626,
            511.8647586618, 511.8654451572, 511.8665212451, 511.8679083821, 511.8695433664, 511.8713748264,
            511.8733606701, 511.8754661974, 511.8776626738, 511.8799262314, 511.8822370068 };
    public static final double[] vdata_imag_d = new double[] { 0., 511.8534037109, 511.7061181082,
            511.7096364601, 511.7373863950, 511.7680347632, 511.7967875532, 511.8225281841, 511.8451629348,
            511.8649119387, 511.8820803844, 511.8969781011, 511.9098918835, 511.9210777066, 511.9307604484,
            511.9391362671, 511.9463757241, 511.9526269238, 511.9580184108, 511.9626617538, 511.9666538138,
            511.9700787219, 511.9730095953, 511.9755100241, 511.9776353561, 511.9794338060 };
}
