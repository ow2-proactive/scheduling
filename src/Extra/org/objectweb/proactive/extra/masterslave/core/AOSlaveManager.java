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
package org.objectweb.proactive.extra.masterslave.core;

import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManager;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManagerAdmin;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskProvider;


/**
 * The Slave Manager Active Object is responsible for the deployment of Slaves :<br/>
 * <ul>
 * <li> Through a ProActive deployment descriptor</li>
 * <li> Using an existing VirtualNode objectcollection of Nodes</li>
 * <li> Using a collection of Nodes</li>
 * </ul>
 *
 * @author fviale
 *
 */
public class AOSlaveManager implements SlaveManager, SlaveManagerAdmin,
    NodeCreationEventListener, InitActive, Serializable {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE_SLAVEMANAGER);

    //	stub on this active object
    protected Object stubOnThis;
    protected long slaveCounter;
    //	holds the virtual nodes, only used to kill the nodes when the active object is closed
    Vector<VirtualNode> vnlist;

    // a thread pool used for slave creation
    private ExecutorService threadPool;
    private boolean isTerminated;
    private TaskProvider provider;

    public AOSlaveManager() {
    } //proactive no arg constructor

    /**
     *
     * @param provider
     */
    public AOSlaveManager(TaskProvider provider) {
        this.provider = provider;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManagerAdmin#addResources(java.util.Collection)
     */
    public void addResources(Collection<Node> nodes) {
        if (!isTerminated) {
            for (Node node : nodes) {
                threadPool.execute(new SlaveCreationHandler(node));
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManagerAdmin#addResources(java.net.URL, java.lang.String[])
     */
    public void addResources(URL descriptorURL, String virtualNodeName) {
        if (!isTerminated) {
            try {
                ProActiveDescriptor pad = ProActive.getProactiveDescriptor(descriptorURL.toExternalForm());
                addResources(pad.getVirtualNode(virtualNodeName));
            } catch (Exception e) {
                logger.error("Couldnt add the specified resources " +
                    e.toString());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManagerAdmin#addResources(org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    public void addResources(VirtualNode virtualnode) {
        if (!isTerminated) {
            if (!virtualnode.isActivated()) {
                ((VirtualNodeImpl) virtualnode).addNodeCreationEventListener(this);
                virtualnode.activate();
            } else {
                try {
                    Node[] nodes = virtualnode.getNodes();
                    addResources(Arrays.asList(nodes));
                } catch (NodeException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            vnlist.add(virtualnode);
            if (logger.isDebugEnabled()) {
                logger.debug("Virtual Node " + virtualnode.getName() +
                    " added to slave manager");
            }
        }
    }

    /**
     * Creates a slave object inside the given node
     * @param node
     */
    private void createSlave(Node node) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating slave on " +
                    node.getNodeInformation().getName());
            }
            String slavename = "Slave" + slaveCounter++;

            // Creates the slave which will automatically connect to the master
            ProActive.newActive(AOSlave.class.getName(),
                new Object[] { slavename, provider }, node);
            if (logger.isDebugEnabled()) {
                logger.debug("Slave " + slavename + " created on " +
                    node.getNodeInformation().getName());
            }
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace(); // bad node
        } catch (NodeException e) {
            e.printStackTrace(); // bad node
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManager#freeSlave(org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public BooleanWrapper freeSlave(Slave slave) {
        if (!isTerminated) {
            if (logger.isDebugEnabled()) {
                logger.debug(slave.getName() + " freed.");
            }
            BooleanWrapper term = slave.terminate();
            ProActive.waitFor(term);
            return new BooleanWrapper(true);
        }
        return new BooleanWrapper(false);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManager#freeSlaves(java.util.Collection)
     */
    public BooleanWrapper freeSlaves(Collection<Slave> slavesToFree) {
        if (!isTerminated) {
            for (Slave slave : slavesToFree) {
                freeSlave(slave);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(slavesToFree.size() + " slaves Freed.");
            }
            return new BooleanWrapper(true);
        }
        return new BooleanWrapper(false);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        stubOnThis = ProActive.getStubOnThis();
        slaveCounter = 0;
        vnlist = new Vector<VirtualNode>();
        isTerminated = false;
        if (logger.isDebugEnabled()) {
            logger.debug("Resource Manager Initialized");
        }
        threadPool = Executors.newCachedThreadPool();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.event.NodeCreationEventListener#nodeCreated(org.objectweb.proactive.core.event.NodeCreationEvent)
     */
    public void nodeCreated(NodeCreationEvent event) {
        // get the node
        Node node = event.getNode();
        threadPool.execute(new SlaveCreationHandler(node));
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManagerAdmin#setConsumer(org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveConsumer)
     */
    public void setTaskProvider(TaskProvider provider) {
        this.provider = provider;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManagerAdmin#terminate(boolean)
     */
    public BooleanWrapper terminate(boolean freeResources) {
        isTerminated = true;
        if (logger.isDebugEnabled()) {
            logger.debug("Terminating SlaveManager...");
        }
        try {
            for (int i = 0; i < vnlist.size(); i++) {
                // ((VirtualNodeImpl) vnlist.get(i)).waitForAllNodesCreation();
                ((VirtualNodeImpl) vnlist.get(i)).removeNodeCreationEventListener(this);
                if (freeResources) {
                    ((VirtualNodeImpl) vnlist.get(i)).killAll(false);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "finished deactivating nodes, will terminate Resource Manager");
            }
            ProActive.getBodyOnThis().terminate();
            //sucess
            if (logger.isDebugEnabled()) {
                logger.debug("SlaveManager terminated...");
            }
            return new BooleanWrapper(true);
        } catch (Exception e) {
            logger.error("Couldnt Terminate the Resource manager" +
                e.toString());

            return new BooleanWrapper(false);
        }
    }

    /**
     * Internal class which creates slaves on top of nodes
     * @author fviale
     *
     */
    private class SlaveCreationHandler implements Runnable {
        private Node node = null;

        public SlaveCreationHandler(Node _node) {
            this.node = _node;
        }

        public void run() {
            createSlave(node);
        }
    }
}
