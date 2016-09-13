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
 */
public class RamSchedulingPolicy extends ExtendedSchedulerPolicy {

    private static final Logger logger = Logger.getLogger(RamSchedulingPolicy.class);

    public static final String RAM_VARIABLE_NAME = "ALLOC_RAM_GIGABYTES";

    @Override
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {

        logger.info("Selected Nodes: " + selectedNodes);

        logger.info("Analysing task: " + task.getInternal().getName());

        String allocRam = task.getInternal().getGenericInformation().get(RAM_VARIABLE_NAME);

        if (allocRam != null) {
            return canRunTaskOnNode(selectedNodes, task, Integer.parseInt(allocRam));
        } else {
            return true;
        }

    }

    private boolean canRunTaskOnNode(NodeSet selectedNodes, EligibleTaskDescriptor task, int neededRam) {
        Node n = selectedNodes.get(0);
        try {
            long freeRam = getFreeRamFromNode(n);
            logger.info("Free Ram for node (" + n.getNodeInformation().getName() + ") : " + freeRam +
                " , neededRam : " + neededRam);
            if (freeRam >= neededRam) {
                logger.info("Task " + task.getInternal().getName() + " can execute on " + n);
                n.setProperty(RAM_VARIABLE_NAME, "" + neededRam);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Error while setting the property " + RAM_VARIABLE_NAME, e);
        }
        return false;
    }

    private long getFreeRamFromNode(Node n) throws ActiveObjectCreationException, NodeException {
        RamCompute ramCompute = PAActiveObject.newActive(RamCompute.class, new Object[] {}, n);
        long freeRam = ramCompute.getAvailableRAM();
        try {
            PAActiveObject.terminateActiveObject(ramCompute, true);
        } catch (Exception e) {
            logger.warn("Error while terminating Active Object", e);
        }
        return freeRam;
    }

}