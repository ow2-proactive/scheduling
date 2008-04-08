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
package org.objectweb.proactive.examples.pi;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplication;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNode;


/**
 *
 * This program evaluates the PI number using the Bailey-Borwein-Plouffe
 * algorithm. This is the main class, which we have to execute in order to run the pi computation
 *
 * @author The ProActive Team
 *
 */
public class PiBBP implements Serializable {
    private final static int SIMPLE = 1;
    private final static int PARALLEL = 2;
    private final static int PARALLEL_DISTRIBUTED = 3;
    private final static int COMPONENT = 4;
    private int run_ = SIMPLE;
    protected int nbDecimals_;
    private String deploymentDescriptorLocation_;
    private GCMApplication deploymentDescriptor_;
    private boolean ws_ = false;
    protected PiComp piComputer;

    /**
     * Empty constructor
     *
     */
    public PiBBP() {
    }

    /**
     * Constructor
     * @param args the string array containing the arguments which will initalize the computation
     */
    public PiBBP(String[] args) {
        parseProgramArguments(args);
    }

    /**
     * Sets the  number of decimals to compute
     * @param nbDecimals the number of decimals
     */
    public void setNbDecimals(int nbDecimals) {
        this.nbDecimals_ = nbDecimals;
    }

    /**
     *  Computes the value of PI on a local node, deployed on a local JVM
     * @return The value of PI
     */
    public String runSimple() {
        System.out.println("No deployment : computation will take place on the current node.");
        System.out.println("Starting computation ...");
        long timeAtBeginningOfComputation = System.currentTimeMillis();

        // create a PiComputer instance
        piComputer = new PiComputer(new Integer(nbDecimals_));

        // define the interval to calculate
        Interval interval = new Interval(0, nbDecimals_);

        // compute
        Result result = piComputer.compute(interval);
        long timeAtEndOfComputation = System.currentTimeMillis();

        // display results
        System.out.println("Computation finished ...");
        System.out.println("Computed PI value is : " + result.getNumericalResult().toString());

        System.out.println("Time waiting for result : " +
            (timeAtEndOfComputation - timeAtBeginningOfComputation) + " ms");
        return result.getNumericalResult().toString();
    }

    /**
     * Computes the value of PI with a group of "pi computers", deployed on a local node
     * @return the value of PI
     */
    public String runParallel() {
        try {
            // create a group of computers on the current host
            piComputer = (PiComputer) PAGroup.newGroup(PiComputer.class.getName(), new Object[][] {
                    new Object[] { new Integer(nbDecimals_) }, new Object[] { new Integer(nbDecimals_) } });

            return computeOnGroup(piComputer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Computes the value of PI with a group of "pi computers", deployed on remote nodes
     * @return the value of PI
     */
    public String runParallelDistributed() {
        try {
            //*************************************************************
            // * creation of remote nodes
            // *************************************************************/
            System.out.println("\nStarting deployment of virtual nodes");
            // parse the descriptor file
            deploymentDescriptor_ = PAGCMDeployment.loadApplicationDescriptor(new File("../descriptors/" +
                deploymentDescriptorLocation_));
            deploymentDescriptor_.startDeployment();
            GCMVirtualNode computersVN = deploymentDescriptor_.getVirtualNode("computers-vn");
            computersVN.waitReady();

            //            // create the remote nodes for the virtual node computersVN
            //           computersVN.activate();
            //*************************************************************
            // * creation of active objects on the remote nodes
            // *************************************************************/
            System.out.println("\nCreating a group of computers on the given virtual node ...");

            // create a group of computers on the virtual node computersVN
            Set<Node> nodes = computersVN.getCurrentNodes();
            piComputer = (PiComputer) PAGroup.newGroupInParallel(PiComputer.class.getName(),
                    new Object[] { new Integer(nbDecimals_) }, nodes.toArray(new Node[0]));

            return computeOnGroup(piComputer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            deploymentDescriptor_.kill();
        }
        return "";
    }

    /**
     * Computes PI value with the component version of the application
     *
     */
    public void runComponent() {
        try {
            String arg0 = "-fractal"; // using the fractal component model
            String arg1 = "org.objectweb.proactive.examples.pi.fractal.PiBBPWrapper";
            String arg2 = "r";

            String arg3 = "../descriptors/" + deploymentDescriptorLocation_; // the deployment descriptor

            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map context = new HashMap();

            /* Deploying runtimes */
            GCMApplication deploymentDescriptor = PAGCMDeployment.loadApplicationDescriptor(new File(arg3));
            context.put("deployment-descriptor", deploymentDescriptor);
            deploymentDescriptor.startDeployment();
            GCMVirtualNode virtualNode = deploymentDescriptor.getVirtualNode("computers-vn");
            virtualNode.waitReady();
            long nbNodes = virtualNode.getNbCurrentNodes();

            /* Determing intervals to send for computation */
            List<Interval> intervals = PiUtil.dividePIList(nbNodes, nbDecimals_);

            /* Master component creation */
            Component master = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.pi.fractal.PiBBPWrapper", null);

            /* Creation of worker components, depending on the number of deployed nodes */
            Component worker;
            List<Component> workers = new ArrayList<Component>();
            for (int i = 0; i < nbNodes; i++) {
                worker = (Component) f.newComponent("org.objectweb.proactive.examples.pi.fractal.PiComputer",
                        context);
                Fractal.getBindingController(master).bindFc("multicastDispatcher",
                        worker.getFcInterface("computation")); /*
                 * Master component is bound to each
                 * worker, with its client multicast
                 * interface
                 */
                workers.add(worker);
            }

            Component w;

            //Starting all the workers
            PiComp picomp;
            for (int j = 0; j < workers.size(); j++) {
                w = workers.get(j);
                Fractal.getLifeCycleController(w).startFc();
                picomp = (PiComp) w.getFcInterface("computation");
                picomp.setScale(nbDecimals_); /*
                 * Normally, this is made when instanciating
                 * PiComputers, but with ADL instanciation, we have
                 * to make an explicit call to setScale
                 */
            }

            Fractal.getLifeCycleController(master).startFc();
            MasterComputation m = (MasterComputation) master.getFcInterface("s");
            m.computePi(intervals); /*
             * Computing and displaying the value of PI(the call is
             * synchronous)
             */

            /* Stopping all the components */
            /* Stopping master component */
            Fractal.getLifeCycleController(master).stopFc();

            /* Stopping workers components */
            for (int j = 0; j < workers.size(); j++) {
                w = workers.get(j);
                Fractal.getLifeCycleController(w).stopFc();
            }

            deploymentDescriptor.kill(); /* Killing deployed runtimes */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called when the value of pi has to be computed with a group of "pi computers"
     * @param piComputers the group of "pi computers" that will perform the computation
     * @return the value of PI
     */
    public String computeOnGroup(PiComp piComputers) {
        int nbNodes = PAGroup.getGroup(piComputers).size();
        System.out.println("\nUsing " + nbNodes + " PiComputers for the computation\n");

        // distribution of the intervals to the computers is handled in
        // a private method
        Interval intervals = null;
        try {
            intervals = PiUtil.dividePI(nbNodes, nbDecimals_);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        // scatter group data, so that independent intervals are sent as
        // parameters to each PiComputer instance
        PAGroup.setScatterGroup(intervals);

        //*************************************************************
        // * computation
        // *************************************************************/
        System.out.println("Starting computation ...\n");
        long timeAtBeginningOfComputation = System.currentTimeMillis();

        // invocation on group, parameters are scattered, result is a
        // group
        Result results = piComputers.compute(intervals);
        Group resultsGroup = PAGroup.getGroup(results);

        // the following is displayed because the "compute" operation is
        // asynchronous (non-blocking)
        System.out.println("Intervals sent to the computers...\n");

        Result total = PiUtil.conquerPI(results);

        long timeAtEndOfComputation = System.currentTimeMillis();

        //*************************************************************
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
        System.out.println("Evaluation of Pi will be performed with " + nbDecimals_ + " decimals");

        switch (run_) {
            case (SIMPLE):
                runSimple();
                break;
            case (PARALLEL):
                runParallel();
                break;
            case (PARALLEL_DISTRIBUTED):
                runParallelDistributed();
                try {
                    deploymentDescriptor_.kill();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case (COMPONENT):
                runComponent();
                break;
            default:
                runSimple();
        }
    }

    public static void main(String[] args) {
        try {
            //            PiBBP piApplication = new PiBBP(args);
            //            piApplication.start();
            PiBBP piApplication = (PiBBP) PAActiveObject.newActive(PiBBP.class.getName(),
                    new Object[] { args });

            if (piApplication.isWebService()) {
                WebServices
                        .exposeAsWebService(piApplication, "http://localhost:8080/", "piComputation",
                                new String[] { "runSimple", "runParallel", "runParallelDistributed",
                                        "setNbDecimals" });
            } else {
                piApplication.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests if the computation of PI has to de deployed as a web service
     * @return true if the application has to be exposed as a web service, false if not
     */
    public boolean isWebService() {
        return ws_;
    }

    /**
     * Initializes the computation with the arguments found in the args array
     * @param args The initialization arguments
     */
    private void parseProgramArguments(String[] args) {
        if (args.length == 0) {
            ws_ = true;
            deploymentDescriptorLocation_ = "LAN.xml";
        } else {
            nbDecimals_ = new Integer(args[0]).intValue();
            run_ = new Integer(args[1]).intValue();
            int deployment = new Integer(args[2]).intValue();
            switch (deployment) {
                case 1:
                    deploymentDescriptorLocation_ = "localhost.xml";
                    break;
                case 2:
                    deploymentDescriptorLocation_ = "LAN.xml";
                    break;
                case 3:
                    deploymentDescriptorLocation_ = "sophia-infra-p2p.xml";
                    break;
                case 4:
                    deploymentDescriptorLocation_ = "sophia-cluster.xml";
                    break;
                case 5:
                    deploymentDescriptorLocation_ = "custom-descriptor.xml";
                    break;
                default:
                    deploymentDescriptorLocation_ = "localhost.xml";
            }
        }
    }
}
