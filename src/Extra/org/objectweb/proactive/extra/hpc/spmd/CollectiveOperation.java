package org.objectweb.proactive.extra.hpc.spmd;

import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.extra.hpc.exchange.Exchanger;


/**
 * This class provides a set of <i>collective operations</i> to use among the members of a SPMD
 * program. These operations were designed to take advantage of the underlying topology.<br/>
 * 
 * Here is an example of usage : <code><pre>
 * public class MySPMDClass {
 *   (...)
 *   public void foo() {
 *     CollectiveOperation co = new CollectiveOperation(PASPMD.getMySPMDGroup());
 *     double myValue = Math.random();
 *     double maxValue = co.max(myValue);
 *     // maxValue will contains the maximal value returned by Math.random() on
 *     // each member of the SPMD group
 *   }
 * }
 * </pre></code>
 */
public class CollectiveOperation {
    private Group<Object> group;
    private int myRank;

    //
    // --- CONSTRUCTORS --------------------------------------------------------
    // 
    /**
     * Construct a {@link CollectiveOperation} for a given {@link Group}
     */
    public CollectiveOperation(Group<Object> g) {
        this.group = g;
        this.myRank = PASPMD.getMyRank();
    }

    //
    // --- PUBLIC METHODS ------------------------------------------------------
    // 
    /**
     * Performs a collective operation to compute a sum.
     * 
     * @param val
     *            the value to sum with others
     * @return the sum of all specified values
     */
    public double sum(double val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] srcArray = new double[1];
        double[] dstArray = new double[1];
        Exchanger exchanger = Exchanger.getExchanger();
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("sumD" + step, destRank, srcArray, 0, dstArray, 0, 1);
            srcArray[0] += dstArray[0];
        } while (step < nbSteps);
        return srcArray[0];
    }

    public int sum(int val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] srcArray = new int[1];
        int[] dstArray = new int[1];
        Exchanger exchanger = Exchanger.getExchanger();
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("sumI" + step, destRank, srcArray, 0, dstArray, 0, 1);
            srcArray[0] += dstArray[0];
        } while (step < nbSteps);
        return srcArray[0];
    }

    /**
     * Performs a collective operation to compute a sum on an array. Each element of<tt>valArray</tt>
     * will be replaced by the sum of the elements at the same position on other SPMD members. It is
     * equivalent to call <tt>sum(double)</tt> on each element of <tt>valArray</tt>.
     * 
     * @param valArray
     *            the array involved in the sum
     */
    public void sum(double[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] dstArray = new double[srcArray.length];
        Exchanger exchanger = Exchanger.getExchanger();
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("sumDArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                srcArray[i] += dstArray[i];
            }
        } while (step < nbSteps);
    }

    public void sum(int[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] dstArray = new int[srcArray.length];
        Exchanger exchanger = Exchanger.getExchanger();
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("sumIArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                srcArray[i] += dstArray[i];
            }
        } while (step < nbSteps);
    }

    /**
     * Performs a collective operation to find a minimum.
     * 
     * @param val
     *            the value to compare.
     * @return the minimum <tt>val</tt> between all SPMD members
     */
    public double min(double val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] srcArray = new double[1];
        double[] dstArray = new double[1];
        Exchanger exchanger = Exchanger.getExchanger();
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("minD" + step, destRank, srcArray, 0, dstArray, 0, 1);
            if (dstArray[0] < srcArray[0]) { // Look for the min value...
                srcArray[0] = dstArray[0];
            }
        } while (step < nbSteps);
        return srcArray[0];
    }

    public int min(int val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] srcArray = new int[1];
        int[] dstArray = new int[1];
        Exchanger exchanger = Exchanger.getExchanger();
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("minI" + step, destRank, srcArray, 0, dstArray, 0, 1);
            if (dstArray[0] < srcArray[0]) { // Look for the min value...
                srcArray[0] = dstArray[0];
            }
        } while (step < nbSteps);
        return srcArray[0];
    }

    /**
     * Performs a collective operation to get an array of minimums.
     * 
     * @param valArray
     *            the values to compare. It is equivalent to call <tt>min(double)</tt> on each
     *            element of <tt>srcArray</tt>.
     */
    public void min(double[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] dstArray = new double[srcArray.length];
        Exchanger exchanger = Exchanger.getExchanger();
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("minDArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                if (dstArray[i] < srcArray[i]) { // Look for the min value
                    srcArray[i] = dstArray[i];
                }
            }
        } while (step < nbSteps);
    }

    public void min(int[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] dstArray = new int[srcArray.length];
        Exchanger exchanger = Exchanger.getExchanger();
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("minIArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                if (dstArray[i] < srcArray[i]) { // Look for the min value
                    srcArray[i] = dstArray[i];
                }
            }
        } while (step < nbSteps);
    }

    /**
     * Performs a collective operation to find a maximum.
     * 
     * @param val
     *            the value to compare.
     * @return the maximum <tt>val</tt> between all SPMD members
     */
    public double max(double val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] srcArray = new double[1];
        double[] dstArray = new double[1];
        Exchanger exchanger = Exchanger.getExchanger();
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("maxD" + step, destRank, srcArray, 0, dstArray, 0, 1);
            if (dstArray[0] > srcArray[0]) { // Look for the max value...
                srcArray[0] = dstArray[0];
            }
        } while (step < nbSteps);
        return srcArray[0];
    }

    public int max(int val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] srcArray = new int[1];
        int[] dstArray = new int[1];
        Exchanger exchanger = Exchanger.getExchanger();
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("maxI" + step, destRank, srcArray, 0, dstArray, 0, 1);
            if (dstArray[0] > srcArray[0]) { // Look for the max value...
                srcArray[0] = dstArray[0];
            }
        } while (step < nbSteps);
        return srcArray[0];
    }

    /**
     * Performs a collective operation to get an array of maximum.
     * 
     * @param valArray
     *            the values to compare. It is equivalent to call <tt>max(double)</tt> on each
     *            element of <tt>srcArray</tt>.
     */
    public void max(double[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] dstArray = new double[srcArray.length];
        Exchanger exchanger = Exchanger.getExchanger();
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("maxDArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                if (dstArray[i] > srcArray[i]) { // Look for the max value
                    srcArray[i] = dstArray[i];
                }
            }
        } while (step < nbSteps);
    }

    public void max(int[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] dstArray = new int[srcArray.length];
        Exchanger exchanger = Exchanger.getExchanger();
        do {
            int destRank = myRank ^ ipow2(step++);
            exchanger.exchange("maxIArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                if (dstArray[i] > srcArray[i]) { // Look for the max value
                    srcArray[i] = dstArray[i];
                }
            }
        } while (step < nbSteps);
    }

    //
    // --- PRIVATE METHODS -----------------------------------------------------
    //  
    private static int ilog2(int n) {
        int nn, lg;

        if (n == 1)
            return 0;

        lg = 1;
        nn = 2;
        while (nn < n) {
            nn *= 2;
            lg += 1;
        }
        return lg;
    }

    private static final int ipow2(int n) {
        return (1 << n);
    }
}
