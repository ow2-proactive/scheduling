/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.p2p.service.node;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.Loggers;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Jan 12, 2005
 */
public class P2PNodeManager implements Serializable, InitActive, RunActive,
    EndActive, P2PConstants {
    private static final Logger logger = Logger.getLogger(Loggers.P2P_NODES);
    private static final int PROC = Runtime.getRuntime().availableProcessors();
    private Object localService = null;
    private Node p2pServiceNode = null;
    private ProActiveRuntime proactiveRuntime = null;
    private Vector availbaleNodes = new Vector();
    private Vector bookedNodes = new Vector();
    private Vector usingNodes = new Vector();
    private int nodeCounter = 0;

    //--------------------------------------------------------------------------
    // Class Constructors
    //--------------------------------------------------------------------------

    /**
     * Empty constructor for new active.
     */
    public P2PNodeManager() {
        // The empty constructor
    }

    /**
     * Construct a new <code>P2PNodeManager</code>.
     * @param localService a reference to the local P2P service.
     */
    public P2PNodeManager(P2PService localService) {
        this.localService = localService;
    }

    //--------------------------------------------------------------------------
    // Public Class methods
    // -------------------------------------------------------------------------

    /**
     * Asking a shared node.
     * @return a <code>P2PNode</code> which contains a node or <code>null</code>
     * if no shared nodes are available.
     */
    public P2PNode askingNode() {
        if (logger.isDebugEnabled()) {
            logger.debug("Asking a node to the nodes manager");
        }
        if ((this.availbaleNodes.size() == 0) &&
                (this.bookedNodes.size() == 0) &&
                (this.usingNodes.size() == 0)) {
            this.deployingDefaultSharedNodes();
        }
        if (this.availbaleNodes.size() > 0) {
            Node node = (Node) this.availbaleNodes.remove(0);
            this.bookedNodes.add(new Booking(node));
            if (logger.isDebugEnabled()) {
                logger.debug("Yes the manager has a node");
            }
            return new P2PNode(node);
        } else {
            // All nodes is already assigned
            if (logger.isDebugEnabled()) {
                logger.debug("Sorry no availbale node for the moment");
            }
            return new P2PNode(null);
        }
    }

    /**
     * Leave the specified node. The node is killed and new one is created and
     * ready for sharing.
     * @param nodeToFree the node to kill.
     */
    public void leaveNode(Node nodeToFree) {
        this.usingNodes.remove(nodeToFree);
        try {
            // Terminating all body
            Object[] activeObjects = nodeToFree.getActiveObjects();
            for (int index = 0; index < activeObjects.length; index++) {
                BodyProxy proxy = (BodyProxy) ((StubObject) activeObjects[index]).getProxy();
                ((Body) proxy.getBody()).terminate();
            }

            // Kill the node
            this.proactiveRuntime.killNode(nodeToFree.getNodeInformation()
                                                     .getURL());
            // Creating a new node
            this.createNewNode();
        } catch (ProActiveException e) {
            logger.fatal("Coudln't delete or create a shared node", e);
        }
    }

    //--------------------------------------------------------------------------
    // Active Object methods
    //--------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entering initActivity");
        }

        try {
            // Getting reference to the P2P node
            this.p2pServiceNode = NodeFactory.getNode(body.getNodeURL());
            // Getting ProActive runtime
            this.proactiveRuntime = this.p2pServiceNode.getProActiveRuntime();
        } catch (NodeException e) {
            logger.fatal("Couldn't get reference to the local p2pServiceNode", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("P2P node manager is running at " +
                this.p2pServiceNode.getNodeInformation().getURL());
            try {
                logger.debug("ProActiveRuntime at " +
                    this.proactiveRuntime.getURL());
            } catch (ProActiveException e1) {
                logger.debug("Coudln't get the ProActiveRuntime URL", e1);
            }
        }

        // Creating default shared node
        this.deployingDefaultSharedNodes();

        if (logger.isDebugEnabled()) {
            logger.debug("Exiting initActivity");
        }
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            long currentTime = System.currentTimeMillis();
            int index = 0;
            while (index < this.bookedNodes.size()) {
                Booking bookedNode = (Booking) this.bookedNodes.get(index);
                try {
                    // Searching expired booking
                    if ((bookedNode.getNode().getActiveObjects().length == 0) &&
                            (currentTime > bookedNode.getExpirationTime())) {
                        // Cancel booking
                        this.bookedNodes.remove(bookedNode);
                        String nodeUrl = bookedNode.getNode()
                                                   .getNodeInformation().getURL();
                        this.proactiveRuntime.killNode(nodeUrl);
                        this.createNewNode();
                        if (logger.isInfoEnabled()) {
                            logger.info("Node at " + nodeUrl + " killed");
                        }
                    } else if (bookedNode.getNode().getActiveObjects().length != 0) {
                        // Move booked nodes to used nodes
                        this.bookedNodes.remove(bookedNode);
                        this.usingNodes.add(bookedNode);
                    } else {
                        index++;
                    }
                } catch (NodeException e) {
                    logger.warn("Problem with a shared node", e);
                } catch (ActiveObjectCreationException e) {
                    logger.warn("Problem with a shared node", e);
                } catch (ProActiveException e) {
                    logger.warn("Problem with a shared node", e);
                }
            }

            // Serving request
            service.blockingServeOldest(MAX_TIME);
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
        }
    }

    /**
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
        this.killAllSharedNodes();
    }

    // -------------------------------------------------------------------------
    // Private class method
    // -------------------------------------------------------------------------

    /**
     * @return a new shared node.
     * @throws NodeException
     * @throws ProActiveException
     */
    private Node createNewNode() throws NodeException, ProActiveException {
        Node newNode = NodeFactory.createNode(P2PConstants.SHARED_NODE_NAME +
                "_" + this.nodeCounter++, true,
                this.proactiveRuntime.getPolicyServer(), P2PConstants.VN_NAME);
        this.availbaleNodes.add(newNode);
        return newNode;
    }

    /**
     * Starting default shared nodes. One by processor.
     */
    private void deployingDefaultSharedNodes() {
        assert PROC > 0 : "Processor count = 0";
        if (logger.isInfoEnabled()) {
            logger.info("Number of available processors for this JVM: " + PROC);
        }

        // Starting default shared nodes
        for (int procNumber = 0; procNumber < PROC; procNumber++) {
            try {
                Node node = NodeFactory.createNode(P2PConstants.SHARED_NODE_NAME +
                        "_" + procNumber);
                if (logger.isDebugEnabled()) {
                    logger.debug("Default shared node succefuly created at: " +
                        node.getNodeInformation().getURL());
                }
                this.availbaleNodes.add(node);
                this.nodeCounter++;
            } catch (NodeException e) {
                logger.warn("Couldn't create default shared node", e);
            }
        }
        this.nodeCounter++;
    }

    /**
     * Kill all shared nodes.
     */
    private void killAllSharedNodes() {
        this.availbaleNodes = new Vector();
        this.bookedNodes = new Vector();
        this.usingNodes = new Vector();
        for (int procNumber = 0; procNumber < PROC; procNumber++) {
            try {
                this.proactiveRuntime.killNode(P2PConstants.SHARED_NODE_NAME +
                    "_" + procNumber);
            } catch (ProActiveException e) {
                logger.warn("Couldn't delete the previous shared node", e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Inner class
    // -------------------------------------------------------------------------
    private static final long MAX_TIME = Long.parseLong(System.getProperty(
                P2PConstants.PROPERTY_BOOKING_MAX));

    /**
     *
     * Representing a booking node.
     *
     * @author Alexandre di Costanzo
     *
     * Created on Jan 14, 2005
     */
    private class Booking {
        private Node node = null;
        private long bookingTime = 0;
        private long expirationTime = 0;

        /**
         * Construct a new <code>Booking</code> for a node.
         * @param node the node to book.
         */
        public Booking(Node node) {
            this.node = node;
            this.bookingTime = System.currentTimeMillis();
            this.expirationTime = this.bookingTime + MAX_TIME;
        }

        /**
         * @return Returns the expiration time for this booking.
         */
        public long getExpirationTime() {
            return this.expirationTime;
        }

        /**
         * @return Returns the booked node.
         */
        public Node getNode() {
            return this.node;
        }
    }
}
