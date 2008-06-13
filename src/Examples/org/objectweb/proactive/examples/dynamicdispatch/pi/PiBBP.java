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
package org.objectweb.proactive.examples.dynamicdispatch.pi;

import java.io.Serializable;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PAGroup;


/**
 * 
 * This program evaluates the PI number using the Bailey-Borwein-Plouffe
 * algorithm. This is the main class, which we have to execute in order to run
 * the pi computation
 * 
 * @author The ProActive Team
 * 
 */
public class PiBBP implements Serializable {
    protected int nbDecimals;
    private String deploymentDescriptorLocation_;
    private ProActiveDescriptor deploymentDescriptor_;
    private boolean ws_ = false;
    protected PiComputer piComputer;
    int nbIntervals;

    /**
     * Empty constructor
     * 
     */
    public PiBBP() {
    }

    /**
     * Constructor
     * 
     * @param args
     *            the string array containing the arguments which will initalize
     *            the computation
     */
    public PiBBP(String[] args) {
        parseProgramArguments(args);
    }

    /**
     * Sets the number of decimals to compute
     * 
     * @param nbDecimals
     *            the number of decimals
     */
    public void setNbDecimals(int nbDecimals) {
        this.nbDecimals = nbDecimals;
    }

    public String launchComputation() {
        try {
            // *************************************************************
            // * creation of remote nodes
            // *************************************************************/
            System.out.println("\nStarting deployment of virtual nodes");
            // parse the descriptor file
            deploymentDescriptor_ = PADeployment.getProactiveDescriptor(deploymentDescriptorLocation_);
            deploymentDescriptor_.activateMappings();
            VirtualNode computersVN = deploymentDescriptor_.getVirtualNode("workers");

            // // create the remote nodes for the virtual node computersVN
            // computersVN.activate();
            // *************************************************************
            // * creation of active objects on the remote nodes
            // *************************************************************/
            System.out.println("\nCreating a group of computers on the given virtual node ...");

            // create a group of computers on the virtual node computersVN
            piComputer = (PiComputer) PAGroup.newGroupInParallel(PiComputer.class.getName(),
                    new Object[] { Integer.valueOf(nbDecimals) }, computersVN.getNodes());

            return computeOnGroup(piComputer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                deploymentDescriptor_.killall(true);
            } catch (ProActiveException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * Method called when the value of pi has to be computed with a group of "pi
     * computers"
     * 
     * @param piComputers
     *            the group of "pi computers" that will perform the computation
     * @return the value of PI
     */
    public String computeOnGroup(PiComputer piComputers) {
        int nbNodes = PAGroup.getGroup(piComputers).size();
        System.out.println("\nUsing " + nbNodes + " PiComputers for the computation\n");

        // distribution of the intervals to the computers is handled in
        // a private method
        Interval intervals = null;
        try {
            intervals = PiUtil.dividePIByIntervalSize(nbIntervals, nbDecimals);
            System.out.println("we have: " + PAGroup.getGroup(intervals).size() + " intervals and " +
                nbNodes + " workers");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        // scatter group data, so that independent intervals are sent as
        // parameters to each PiComputer instance
        PAGroup.setScatterGroup(intervals);

        PAGroup.setDispatchMode(piComputers, DispatchMode.DYNAMIC, 1);

        // *************************************************************
        // * computation
        // *************************************************************/
        System.out.println("Starting computation ...\n");
        long timeAtBeginningOfComputation = System.currentTimeMillis();

        // invocation on group, parameters are scattered, result is a
        // group
        Result results = piComputers.compute(intervals);
        Group<Result> resultsGroup = PAGroup.getGroup(results);

        // the following is displayed because the "compute" operation is
        // asynchronous (non-blocking)
        System.out.println("Intervals sent to the computers...\n");

        Result total = PiUtil.conquerPI(results);

        long timeAtEndOfComputation = System.currentTimeMillis();

        // *************************************************************
        // * results
        // *************************************************************/
        System.out.println("\nComputation finished ...");
        System.out.println("Computed PI value is : " + total.getNumericalResult().toString());
        System.out.println("Time waiting for result : " +
            (timeAtEndOfComputation - timeAtBeginningOfComputation) + " ms");
        System.out.println("Cumulated time from all computers is : " + total.getComputationTime() + " ms");
        System.out
                .println("Ratio for " +
                    resultsGroup.size() +
                    " processors is : " +
                    (((double) total.getComputationTime() / ((double) (timeAtEndOfComputation - timeAtBeginningOfComputation))) * 100) +
                    " %");
        return total.getNumericalResult().toString();
    }

    /**
     * This method decides which version of pi application has to be launched
     * 
     */
    public void start() {
        System.out.println("Evaluation of Pi will be performed with " + nbDecimals + " decimals");

        launchComputation();

    }

    public static void main(String[] args) {
        try {
            // PiBBP piApplication = new PiBBP(args);
            // piApplication.start();
            PiBBP piApplication = (PiBBP) PAActiveObject.newActive(PiBBP.class.getName(),
                    new Object[] { args });

            piApplication.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the computation with the arguments found in the args array
     * 
     * @param args
     *            The initialization arguments
     */
    private void parseProgramArguments(String[] args) {
        nbDecimals = new Integer(args[0]).intValue();
        nbIntervals = new Integer(args[1]).intValue();
        deploymentDescriptorLocation_ = args[2];
    }
}
