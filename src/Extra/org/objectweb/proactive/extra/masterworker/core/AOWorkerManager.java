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
package org.objectweb.proactive.extra.masterworker.core;

import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.exceptions.SendRequestCommunicationException;
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
import org.objectweb.proactive.extra.masterworker.interfaces.internal.TaskProvider;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.Worker;
import org.objectweb.proactive.extra.masterworker.interfaces.internal.WorkerManager;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The Worker Manager Active Object is responsible for the deployment of Slaves :<br>
 * <ul>
 * <li> Through a ProActive deployment descriptor</li>
 * <li> Using an existing VirtualNode object</li>
 * <li> Using a collection of Nodes</li>
 * </ul>
 *
 * @author fviale
 *
 */
public class AOWorkerManager implements WorkerManager,
    NodeCreationEventListener, InitActive, Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -1488970573456417472L;

    /**
    * log4j logger for the slave manager
    */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE_SLAVEMANAGER);

    /**
     * stub on this active object
     */
    protected Object stubOnThis;

    /**
     * how many workers have been created
     */
    protected long slaveNameCounter;

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
     * the entity which will provide tasks to the workers
     */
    protected TaskProvider<Serializable> provider;

    /**
     * Initial memory of the workers
     */
    protected Map<String, Object> initialMemory;

    /**
     * workers deployed so far
     */
    protected Map<String, Worker> workers;

    /**
     * ProActive no arg constructor
     */
    public AOWorkerManager() {
    }

    /**
     * Creates a task manager with the given task provider
     * @param provider the entity that will give tasks to the workers created
     * @param initialMemory the initial memory of the workers
     */
    public AOWorkerManager(final TaskProvider<Serializable> provider,
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
                ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(descriptorURL.toExternalForm());
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
                ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(descriptorURL.toExternalForm());
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
                logger.warn("vn is not activated");
                ((VirtualNodeImpl) virtualnode).addNodeCreationEventListener(this);
                virtualnode.activate();
            } else {
                logger.warn("vn is activated");
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
        if (!isTerminated) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating slave on " +
                        node.getNodeInformation().getName());
                }

                String slavename = node.getVMInformation().getHostName() + "_" +
                    slaveNameCounter++;

                // Creates the slave which will automatically connect to the master
                workers.put(slavename,
                    (Worker) ProActiveObject.newActive(
                        AOWorker.class.getName(),
                        new Object[] { slavename, provider, initialMemory },
                        node));
                if (logger.isDebugEnabled()) {
                    logger.debug("Worker " + slavename + " created on " +
                        node.getNodeInformation().getName());
                }
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace(); // bad node
            } catch (NodeException e) {
                e.printStackTrace(); // bad node
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initActivity(final Body body) {
        stubOnThis = ProActiveObject.getStubOnThis();
        slaveNameCounter = 0;
        workers = new HashMap<String, Worker>();
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
        logger.warn("nodeCreated " + event.getNode());
        Node node = event.getNode();
        try {
            threadPool.execute(new SlaveCreationHandler(node));
        } catch (java.util.concurrent.RejectedExecutionException e) {
        }
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper terminate(final boolean freeResources) {
        isTerminated = true;

        if (logger.isDebugEnabled()) {
            logger.debug("Terminating WorkerManager...");
        }

        try {
            // we shutdown the thread pool, no new thread will be accepted
            threadPool.shutdown();

            for (int i = 0; i < vnlist.size(); i++) {
                // we wait for every node creation, in case some nodes were not already deployed
                try {
                    if (vnlist.get(i).getNbMappedNodes() > vnlist.get(i)
                                                                     .getNumberOfCurrentlyCreatedNodes()) {
                        // implicit wait of created nodes
                        vnlist.get(i).getNodes();
                    }
                } catch (org.objectweb.proactive.core.node.NodeException e) {
                    // do nothing, we ignore node creation exceptions
                }
            }

            // we wait that all threads creating active objects finish
            threadPool.awaitTermination(120, TimeUnit.SECONDS);

            // we send the terminate message to every thread
            for (Entry<String, Worker> worker : workers.entrySet()) {
                try {
                    BooleanWrapper term = worker.getValue().terminate();
                    ProFuture.waitFor(term);
                    if (logger.isDebugEnabled()) {
                        logger.debug(worker.getKey() + " freed.");
                    }
                } catch (SendRequestCommunicationException exp) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(worker.getKey() + " is already freed.");
                    }
                }
            }

            for (int i = 0; i < vnlist.size(); i++) {
                // if the user asked it, we also release the resources, by killing all JVMs
                if (freeResources) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Killing all active objects...");
                    }

                    ((VirtualNodeImpl) vnlist.get(i)).killAll(false);
                }
            }

            // finally we terminate this active object
            ProActiveObject.terminateActiveObject(true);
            // success
            if (logger.isDebugEnabled()) {
                logger.debug("WorkerManager terminated...");
            }

            return new BooleanWrapper(true);
        } catch (Exception e) {
            logger.error("Couldn't Terminate the Resource manager");
            e.printStackTrace();
            return new BooleanWrapper(false);
        }
    }

    /**
     * Internal class which creates workers on top of nodes
     * @author fviale
     *
     */
    protected class SlaveCreationHandler implements Runnable {

        /**
         * node on which workers will be created
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
