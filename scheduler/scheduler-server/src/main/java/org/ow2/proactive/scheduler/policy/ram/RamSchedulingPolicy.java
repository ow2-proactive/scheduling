package org.ow2.proactive.scheduler.policy.ram;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.utils.NodeSet;


/**
 * 
 * This Policy is designed to handle preallocation of RAM into a node machine. 
 * When the task contains the generic information ALLOC_RAM_GIGABYTES, 
 * this policy will return false if there is not enough RAM available 
 * or true if there is RAM available (in this last case it will set the ALLOC_RAM_GIGABYTES property at node level to book the RAM in the node machine)
 * 
 * It's important to set the ALLOC_RAM_GIGABYTES task property to 0 in the clean script to release the preallocation. For example for a groovy clean script :
 * org.objectweb.proactive.api.PAActiveObject.getNode().setProperty("ALLOC_RAM_GIGABYTES","0");
 *
 */
public class RamSchedulingPolicy extends ExtendedSchedulerPolicy {

    private static final Logger logger = Logger.getLogger(RamSchedulingPolicy.class);

    public static final String RAM_VARIABLE_NAME = "ALLOC_RAM_GIGABYTES";

    @Override
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {

        logger.debug("Selected Nodes: " + selectedNodes);

        logger.debug("Analysing task: " + task.getInternal().getName());

        String allocRam = task.getInternal().getRuntimeGenericInformation().get(RAM_VARIABLE_NAME);

        if (allocRam != null) {
            return canRunTaskOnNode(selectedNodes, task, Double.parseDouble(allocRam));
        } else {
            return true;
        }

    }

    private boolean canRunTaskOnNode(NodeSet selectedNodes, EligibleTaskDescriptor task, double neededRam) {
        Node n = selectedNodes.get(0);
        try {
            double freeRam = getFreeRamFromNode(n);
            logger.debug("Free Ram for node (" + n.getNodeInformation().getName() + ") : " + freeRam +
                " , neededRam : " + neededRam);
            if (freeRam >= neededRam) {
                logger.debug("Task " + task.getInternal().getName() + " can execute on " + n);
                n.setProperty(RAM_VARIABLE_NAME, "" + neededRam);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Error while setting the property " + RAM_VARIABLE_NAME, e);
        }
        return false;
    }

    private double getFreeRamFromNode(Node n) throws ActiveObjectCreationException, NodeException {
        RamCompute ramCompute = PAActiveObject.newActive(RamCompute.class, new Object[] {}, n);
        double freeRam = ramCompute.getAvailableRAMInGB();
        try {
            PAActiveObject.terminateActiveObject(ramCompute, true);
        } catch (Exception e) {
            logger.warn("Error while terminating Active Object", e);
        }
        return freeRam;
    }

}