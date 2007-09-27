package org.objectweb.proactive.examples.fastdeployment;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 * Activate a set of ProActive descriptor
 */
public class VNActivator implements Serializable, RunActive,
    NodeCreationEventListener, InitActive {
    final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** Created slave are returned to this manager */
    private Manager manager;

    /** Descriptors to be activated*/
    private Set<String> descriptors;

    /** Virtual Nodes to be activated
     *
     * If this set is empty then all the Virtual Nodes are activated
     */
    private Set<String> virtualNodeNames;

    /** Wait this amount of time between each activation to trashing */
    private int pause;

    /** Number of concurrent threads to perform AO Creation */
    private int concurrency;
    private Executor AOCreators;

    /** The number of slave already created */
    int slaveID = 0;
    Object slaveIDLock;

    public VNActivator() {
        // No-args empty descriptor
    }

    public VNActivator(Manager manager, Set<String> descriptors,
        Set<String> virtualNodes, int concurrency, int pause) {
        this.manager = manager;
        this.descriptors = descriptors;
        this.virtualNodeNames = virtualNodes;
        this.concurrency = concurrency;
        this.pause = pause;
    }

    public void initActivity(Body body) {
        ProActiveObject.setImmediateService("nodeCreated");

        slaveIDLock = new Object();
        AOCreators = Executors.newFixedThreadPool(concurrency);
    }

    public void runActivity(Body body) {

        /*
         * Until the end of the deployment only immediateService are served
         *
         *  It's OK since VNActivator does not serve any method,
         *  nodeCreated excepted
         */
        for (String descriptor : descriptors) {
            ProActiveDescriptor pad;

            try {
                pad = ProDeployment.getProactiveDescriptor(descriptor);
                logger.debug("Loaded Descriptor: " +
                    pad.getProActiveDescriptorURL());

                Set<VirtualNode> virtualNodes = new HashSet<VirtualNode>();

                // No VN specified, activate all !
                if (virtualNodeNames.isEmpty()) {
                    for (VirtualNode vn : pad.getVirtualNodes()) {
                        virtualNodes.add(vn);
                    }
                } else {
                    for (String vnName : virtualNodeNames) {
                        VirtualNode vn = pad.getVirtualNode(vnName);
                        if (vn != null) {
                            virtualNodes.add(vn);
                        } else {
                            logger.warn("Virtual Node " + vnName +
                                " not found in " +
                                pad.getProActiveDescriptorURL());
                        }
                    }
                }

                for (VirtualNode vn : virtualNodes) {
                    logger.info("Activating Virtual Node " + vn.getName() +
                        " from " + pad.getProActiveDescriptorURL());
                    ((VirtualNodeImpl) vn).addNodeCreationEventListener((NodeCreationEventListener) ProActiveObject.getStubOnThis());
                    vn.activate();

                    try {
                        Thread.sleep(pause);
                    } catch (InterruptedException e) {
                        logger.info(e);
                    }
                }
            } catch (ProActiveException e) {
                logger.warn("Descriptor " + descriptor + " does not exist");
            }
        }

        Service service = new Service(body);
        while (body.isActive()) {
            service.blockingServeOldest();
        }
    }

    public void nodeCreated(NodeCreationEvent event) {

        /*
         * A threadpool is used to release the pressure on a internal ProActive lock
         * (deployment event/listener). Since we try to be as fast as possible, threading
         * is using to perform active object creation in parallel.
         *
         * Expect gain factor is 2+
         */
        synchronized (slaveIDLock) {
            AOCreators.execute(new AOCreator(slaveID, event.getNode()));
            slaveID++;
        }
    }

    class AOCreator implements Runnable {
        int slaveID;
        Node node;

        public AOCreator(int slaveID, Node node) {
            this.slaveID = slaveID;
            this.node = node;
        }

        public void run() {
            try {
                String nodeUrl = node.getNodeInformation().getURL();

                logger.info("Creating Active Object on " + nodeUrl);

                // CHANGEME: Create your active object here !
                CPUBurner ao = (CPUBurner) ProActiveObject.newActive(CPUBurner.class.getName(),
                        new Object[] { new IntWrapper(slaveID), manager }, node);

                logger.info("Created Active Object on " + nodeUrl);

                logger.info("The " + slaveID +
                    "th slave is ready, sending it to the manager");

                // The slave is ready, send it to the manager
                manager.nodeAvailable(new IntWrapper(slaveID), ao);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
