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
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
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
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The Slave Manager Active Object is responsible for the deployment of Slaves :<br>
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

    /**
     * log4j logger for the slave manager
     */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE_SLAVEMANAGER);

    /**
     * stub on this active object
     */
    protected Object stubOnThis;

    /**
     * how many slaves have been created
     */
    protected long slaveCounter;

    /**
     * holds the virtual nodes, only used to kill the nodes when the slave manager is terminated
     */
    protected Vector<VirtualNode> vnlist;

    /**
     * a thread pool used for slave creation
     */
    protected ExecutorService threadPool;

    /**
     * true when the slave manager is terminated
     */
    protected boolean isTerminated;

    /**
     * the entity which will provide tasks to the slaves
     */
    protected TaskProvider provider;

    /**
     * Initial memory of the slaves
     */
    protected Map<String, Object> initialMemory;

    /**
     * ProActive no arg constructor
     */
    public AOSlaveManager() {
    }

    /**
     * Creates a task manager with the given task provider
     * @param provider the entity that will give tasks to the slaves created
     * @param initialMemory the initial memory of the slaves
     */
    public AOSlaveManager(final TaskProvider provider,
        final Map<String, Object> initialMemory) {
        this.provider = provider;
        this.initialMemory = initialMemory;
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final Collection<Node> nodes) {
        if (!isTerminated) {
            for (Node node : nodes) {
                threadPool.execute(new SlaveCreationHandler(node));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final URL descriptorURL) {
        if (!isTerminated) {
            try {
                ProActiveDescriptor pad = ProActive.getProactiveDescriptor(descriptorURL.toExternalForm());
                for (VirtualNode vn : pad.getVirtualNodes()) {
                    addResources(vn);
                }
            } catch (ProActiveException e) {
                logger.error("Couldn't add the specified resources.");
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final URL descriptorURL,
        final String virtualNodeName) {
        if (!isTerminated) {
            try {
                ProActiveDescriptor pad = ProActive.getProactiveDescriptor(descriptorURL.toExternalForm());
                addResources(pad.getVirtualNode(virtualNodeName));
            } catch (ProActiveException e) {
                logger.error("Couldn't add the specified resources.");
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(final VirtualNode virtualnode) {
        if (!isTerminated) {
            if (!virtualnode.isActivated()) {
                ((VirtualNodeImpl) virtualnode).addNodeCreationEventListener(this);
                virtualnode.activate();
            } else {
                try {
                    Node[] nodes = virtualnode.getNodes();
                    addResources(Arrays.asList(nodes));
                } catch (NodeException e) {
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
     * @param node the node on which a slave will be created
     */
    protected void createSlave(final Node node) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating slave on " +
                    node.getNodeInformation().getName());
            }
            String slavename = node.getNodeInformation().getHostName() + "_" +
                slaveCounter++;

            // Creates the slave which will automatically connect to the master
            ProActive.newActive(AOSlave.class.getName(),
                new Object[] { slavename, provider, initialMemory }, node);
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

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper freeSlave(final Slave slave) {
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

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper freeSlaves(final Collection<Slave> slavesToFree) {
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

    /**
     * {@inheritDoc}
     */
    public void initActivity(final Body body) {
        stubOnThis = ProActive.getStubOnThis();
        slaveCounter = 0;
        vnlist = new Vector<VirtualNode>();
        isTerminated = false;
        if (logger.isDebugEnabled()) {
            logger.debug("Resource Manager Initialized");
        }
        threadPool = Executors.newCachedThreadPool();
    }

    /**
     * {@inheritDoc}
     */
    public void nodeCreated(final NodeCreationEvent event) {
        // get the node
        Node node = event.getNode();
        threadPool.execute(new SlaveCreationHandler(node));
    }

    /**
     * {@inheritDoc}
     */
    public void setTaskProvider(final TaskProvider provider) {
        this.provider = provider;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper terminate(final boolean freeResources) {
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
            ProActive.terminateActiveObject(true);
            //sucess
            if (logger.isDebugEnabled()) {
                logger.debug("SlaveManager terminated...");
            }
            return new BooleanWrapper(true);
        } catch (Exception e) {
            logger.error("Couldnt Terminate the Resource manager");
            e.printStackTrace();

            return new BooleanWrapper(false);
        }
    }

    /**
     * Internal class which creates slaves on top of nodes
     * @author fviale
     *
     */
    protected class SlaveCreationHandler implements Runnable {

        /**
         * node on which slaves will be created
         */
        private Node node = null;

        /**
         * Creates a slave on a given node
         * @param node
         */
        public SlaveCreationHandler(final Node node) {
            this.node = node;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            createSlave(node);
        }
    }
}
