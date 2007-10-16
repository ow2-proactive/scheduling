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
package org.objectweb.proactive.p2p.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.p2p.service.exception.P2POldMessageException;
import org.objectweb.proactive.p2p.service.node.P2PNode;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;
import org.objectweb.proactive.p2p.service.node.P2PNodeManager;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * <p>ProActive Peer-to-Peer Service.</p>
 * <p>This class is made to be actived.</p>
 *
 * @author Alexandre di Costanzo
 *
 */
public class P2PService implements InitActive, P2PConstants, Serializable,
    ProActiveInternalObject {

    /** Logger. */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SERVICE);

    /** ProActive Group of acquaintances. **/
    private P2PService acquaintances;

    /**
     * ProActive Group representing <code>acquaintances</code>.
     */
    private P2PAcquaintanceManager acquaintanceManager;

    /**
     * Reference to the current Node.
     */
    private Node p2pServiceNode = null;
    private static final int MSG_MEMORY = Integer.parseInt(PAProperties.PA_P2P_MSG_MEMORY.getValue());
    private static final int NOA = Integer.parseInt(PAProperties.PA_P2P_NOA.getValue());
    private static final int EXPL_MSG = Integer.parseInt(PAProperties.PA_P2P_EXPLORING_MSG.getValue()) -
        1;
    private static final long ACQ_TO = Long.parseLong(PAProperties.PA_P2P_NODES_ACQUISITION_T0.getValue());

    /**
     * Randomizer uses in <code>shouldBeAcquaintance</code> method.
     */
    private static final Random randomizer = new Random();

    /**
     * Sequence number list of received messages.
     */
    private Vector<UUID> oldMessageList = new Vector<UUID>(MSG_MEMORY);
    private P2PNodeManager nodeManager = null;

    /**
     * A collection of not full <code>P2PNodeLookup</code>.
     */
    private Vector<P2PNodeLookup> waitingNodesLookup = new Vector<P2PNodeLookup>();
    private Vector<P2PNodeLookup> waitingMaximunNodesLookup = new Vector<P2PNodeLookup>();
    private P2PService stubOnThis = null;

    // For asking nodes
    private Service service = null;
    private RequestFilter filter = new RequestFilter() {

            /**
             * @see org.objectweb.proactive.core.body.request.RequestFilter#acceptRequest(org.objectweb.proactive.core.body.request.Request)
             */
            public boolean acceptRequest(Request request) {
                String requestName = request.getMethodName();
                return requestName.compareToIgnoreCase("askingNode") != 0;
            }
        };

    //--------------------------------------------------------------------------
    // Class Constructors
    //--------------------------------------------------------------------------

    /**
     * The empty constructor.
     *
     * @see org.objectweb.proactive.api.ProActiveObject
     */
    public P2PService() {
        // empty
    }

    //--------------------------------------------------------------------------
    // Public Class methods
    // -------------------------------------------------------------------------

    /**
     * Contact all specified peers to enter in the existing P2P network.
     * @param peers a list of peers URL.
     */
    public void firstContact(Vector peers) {
        // Creating an active P2PFirstContact
        Object[] params = new Object[3];
        params[0] = peers;
        params[1] = this.acquaintanceManager;
        params[2] = this.stubOnThis;
        try {
            System.out.println("<<<<<<<<<<<<< p2pServiceMode " +
                this.p2pServiceNode.getNodeInformation().getURL());

            ProActiveObject.newActive(P2PFirstContact.class.getName(), params,
                this.p2pServiceNode);
        } catch (ActiveObjectCreationException e) {
            logger.warn("Couldn't active P2PFirstContact", e);
        } catch (NodeException e) {
            logger.warn("Couldn't active P2PFirstContact", e);
        }
    }

    /**
     * Add the remote P2P service in the local acquaintances group if NOA is
     * not yet reached.
     * @param service the remote P2P service.
     */
    public void register(P2PService service) {
        try {
            if (!this.stubOnThis.equals(service)) {
                this.acquaintanceManager.add(service);
                if (logger.isDebugEnabled()) {
                    logger.debug("Remote peer localy registered: " +
                        ProActiveObject.getActiveObjectNodeUrl(service));
                }

                // Wake up all node accessor, because new peers are know
                this.wakeUpEveryBody();
            }
        } catch (Exception e) {
            logger.debug("The remote P2P service is certainly down", e);
        }
    }

    /**
     * Just to test if the peer is alive.
     */
    public void heartBeat() {
        logger.debug("Heart-beat message received");
    }

    /**
     * <b>Method automaticly forwarded by run activity if needed.</b>
     * <p>Using a random fonction to choose if this peer should be know by the
     * remote peer or not.</p>
     * @param ttl Time to live of the message, in number of hops.
     * @param uuid UUID of the message.
     * @param remoteService The original sender.
     */
    public void exploring(int ttl, UUID uuid, P2PService remoteService) {
        if (uuid != null) {
            logger.debug("Exploring message received with #" + uuid);
            ttl--;
        }

        boolean broadcast;
        try {
            broadcast = broadcaster(ttl, uuid, remoteService);
        } catch (P2POldMessageException e) {
            return;
        }

        // This should be register
        if (this.shouldBeAcquaintance(remoteService)) {
            this.register(remoteService);
            try {
                remoteService.register(this.stubOnThis);
            } catch (Exception e) {
                logger.debug("Trouble with registering remote peer", e);
                this.acquaintanceManager.remove(remoteService);
            }
        } else if (broadcast) {
            // Forwarding the message
            if (uuid == null) {
                logger.debug("Generating uuid for exploring message");
                uuid = UUID.randomUUID();
            }
            this.acquaintances.exploring(ttl, uuid, remoteService);
            logger.debug("Broadcast exploring message with #" + uuid);
        }
    }

    /**
     * <b>Method automaticly forwarded by run activity if needed.</b>
     * <p>Booking a free node.</p>
     * @param ttl Time to live of the message, in number of hops.
     * @param uuid UUID of the message.
     * @param remoteService The original sender.
     * @param numberOfNodes Number of asked nodes.
     * @param lookup The P2P nodes lookup.
     * @param vnName Virtual node name.
     * @param jobId  Job ID
     */
    public void askingNode(int ttl, UUID uuid, P2PService remoteService,
        int numberOfNodes, P2PNodeLookup lookup, String vnName, String jobId,
        String nodeFamilyRegexp) {
        boolean broadcast;
        if (uuid != null) {
            logger.debug("AskingNode message received with #" + uuid);
            ttl--;
            try {
                broadcast = broadcaster(ttl, uuid, remoteService);
            } catch (P2POldMessageException e) {
                return;
            }
        } else {
            broadcast = true;
        }

        // Do not give a local node to a local request
        if ((uuid != null) || (numberOfNodes == MAX_NODE)) {
            if (numberOfNodes == MAX_NODE) {
                Vector nodes = this.nodeManager.askingAllNodes(nodeFamilyRegexp);
                for (int i = 0; i < nodes.size(); i++) {
                    Node current = (Node) nodes.get(i);
                    if (vnName != null) {
                        try {
                            current.getProActiveRuntime()
                                   .registerVirtualNode(vnName, true);
                        } catch (Exception e) {
                            logger.warn("Couldn't register " + vnName +
                                " in the PAR", e);
                        }
                    }
                    if (jobId != null) {
                        current.getNodeInformation().setJobID(jobId);
                    }
                }
                if (nodes.size() > 0) {
                    lookup.giveNodeForMax(nodes, this.nodeManager);
                }
            } else {
                P2PNode askedNode = this.nodeManager.askingNode(nodeFamilyRegexp);

                // Asking node available?
                Node nodeAvailable = askedNode.getNode();
                if (nodeAvailable != null) {
                    IntWrapper nodeAck;

                    try {
                        nodeAck = lookup.giveNode(nodeAvailable,
                                askedNode.getNodeManager());
                    } catch (Exception lookupExcption) {
                        logger.info("Cannot contact the remote lookup",
                            lookupExcption);
                        this.nodeManager.noMoreNodeNeeded(nodeAvailable);
                        return;
                    }
                    if (nodeAck != null) {
                        // Waitng the ACK
                        long endTime = System.currentTimeMillis() + ACQ_TO;
                        while ((System.currentTimeMillis() < endTime) &&
                                ProFuture.isAwaited(nodeAck)) {
                            if (this.service.hasRequestToServe()) {
                                service.serveAll(this.filter);
                            } else {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    logger.debug(e);
                                }
                            }
                        }

                        // Testing future is here or timeout is expired??
                        if (ProFuture.isAwaited(nodeAck)) {
                            // Do not forward the message
                            // Prevent from deadlock
                            logger.debug("Ack timeout expired");
                            this.nodeManager.noMoreNodeNeeded(nodeAvailable);
                            return;
                        }
                    }

                    if (nodeAck.intValue() != -1) {
                        // Setting vnInformation and JobId
                        if (vnName != null) {
                            try {
                                nodeAvailable.getProActiveRuntime()
                                             .registerVirtualNode(vnName, true);
                            } catch (Exception e) {
                                logger.warn("Couldn't register " + vnName +
                                    " in the PAR", e);
                            }
                        }
                        if (jobId != null) {
                            nodeAvailable.getNodeInformation().setJobID(jobId);
                        }
                        numberOfNodes = (numberOfNodes == MAX_NODE) ? MAX_NODE
                                                                    : nodeAck.intValue();
                        logger.info("Giving 1 node to vn: " + vnName);
                    } else {
                        // It's a NACK node
                        logger.debug("NACK node received");
                        this.nodeManager.noMoreNodeNeeded(nodeAvailable);
                        // No more nodes needed
                        return;
                    }
                }

                // Do we need more nodes?
                if (numberOfNodes == 0) {
                    logger.debug("No more nodes are needed");
                    return;
                }
            }
        }

        // My friend needs more nodes, so I'm broadcasting his request to my own
        // friends
        // Forwarding the message
        if (broadcast) {
            if (uuid == null) {
                logger.debug("Generating uuid for askingNode message");
                uuid = UUID.randomUUID();
            }
            this.acquaintances.askingNode(ttl, uuid, remoteService,
                numberOfNodes, lookup, vnName, jobId, nodeFamilyRegexp);
            logger.debug("Broadcast askingNode message with #" + uuid);
        }
    }

    /** Put in a <code>P2PNodeLookup</code>, the number of asked nodes.
     * @param numberOfNodes the number of asked nodes.
     * @param nodeFamilyRegexp the regexp for the famili, null or empty String for all.
     * @param vnName Virtual node name, cannot be null.
     * @param jobId of the vn, cannot be null.
     * @return the number of asked nodes.
     */
    public P2PNodeLookup getNodes(int numberOfNodes, String nodeFamilyRegexp,
        String vnName, String jobId) {
        assert vnName != null : vnName;
        assert jobId != null : jobId;
        Object[] params = new Object[5];
        params[0] = new Integer(numberOfNodes);
        params[1] = this.stubOnThis;
        params[2] = vnName;
        params[3] = jobId;
        params[4] = nodeFamilyRegexp;

        P2PNodeLookup lookup = null;
        try {
            lookup = (P2PNodeLookup) ProActiveObject.newActive(P2PNodeLookup.class.getName(),
                    params, this.p2pServiceNode);
            ProActiveObject.enableAC(lookup);
            if (numberOfNodes == MAX_NODE) {
                this.waitingMaximunNodesLookup.add(lookup);
            } else {
                this.waitingNodesLookup.add(lookup);
            }
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't create an active lookup", e);
            return null;
        } catch (NodeException e) {
            logger.fatal("Couldn't connect node to creat", e);
            return null;
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Couldn't enable AC for a nodes lookup", e);
            }
        }

        if (logger.isInfoEnabled()) {
            if (numberOfNodes != MAX_NODE) {
                logger.info("Asking for " + numberOfNodes + " nodes");
            } else {
                logger.info("Asking for maxinum nodes");
            }
        }
        return lookup;
    }

    /** Put in a <code>P2PNodeLookup</code>, the number of asked nodes.
     * @param numberOfNodes the number of asked nodes.
     * @param vnName Virtual node name, cannot be null.
     * @param jobId of the vn, cannot be null.
     * @return the number of asked nodes.
     */
    public P2PNodeLookup getNodes(int numberOfNodes, String vnName, String jobId) {
        return this.getNodes(numberOfNodes, ".*", vnName, jobId);
    }

    /**
     * For asking a single node to the p2p infrastructure.
     * There no warranties that a node will be returned.
     * @param vnName the virtual node name, cannot be null.
     * @param jobId the job ID, cannot be null.
     * @return a free node.
     */
    public Node getANode(String vnName, String jobId) {
        return this.stubOnThis.getANode(vnName, jobId, this.stubOnThis);
    }

    /**
     * <b>***For internal use only***
     * @param vnName the virtual node name, cannot be null.
     * @param jobId the job ID., cannot be null
     * @param service a stub on the requester
     * @return a free node.
     */
    public Node getANode(String vnName, String jobId, P2PService service) {
        if (service.equals(this.stubOnThis)) {
            P2PService newPeer = this.acquaintanceManager.randomPeer();
            if (newPeer != null) {
                return newPeer.getANode(vnName, jobId, service);
            }
        }
        P2PNode askedNode = this.nodeManager.askingNode(null);
        Node nodeAvailable = askedNode.getNode();

        if (nodeAvailable != null) {
            if (vnName != null) {
                try {
                    nodeAvailable.getProActiveRuntime()
                                 .registerVirtualNode(vnName, true);
                } catch (Exception e) {
                    logger.warn("Couldn't register " + vnName + " in the PAR", e);
                }
            }
            if (jobId != null) {
                nodeAvailable.getNodeInformation().setJobID(jobId);
            }
            return nodeAvailable;
        }
        return this.acquaintanceManager.randomPeer()
                                       .getANode(vnName, jobId, service);
    }

    /**
    * Kill the given node.
    * @param node the node url to kill.
    */
    public void killNode(String node) {
        try {
            ProActiveRuntime paRuntime = RuntimeFactory.getDefaultRuntime();
            String parUrl = paRuntime.getURL();

            Node remoteNode = NodeFactory.getNode(node);
            ProActiveRuntime remoteRuntime = remoteNode.getProActiveRuntime();
            String remoteRuntimeUrl = remoteRuntime.getURL();
            P2PNodeManager remoteNodeManager = (P2PNodeManager) ProActiveObject.lookupActive(P2PNodeManager.class.getName(),
                    URIBuilder.buildURI(URIBuilder.getHostNameFromUrl(
                            remoteRuntimeUrl), "P2PNodeManager",
                        URIBuilder.getProtocol(remoteRuntimeUrl),
                        URIBuilder.getPortNumber(remoteRuntimeUrl)).toString());

            remoteNodeManager.leaveNode(remoteNode);
            remoteRuntime.rmAcquaintance(parUrl);
            paRuntime.rmAcquaintance(remoteRuntime.getURL());

            logger.info("Node at " + node + " succefuly removed");

            // Unregister the remote runtime
            paRuntime.unregister(remoteRuntime, remoteRuntime.getURL(), "p2p",
                PAProperties.PA_P2P_ACQUISITION.getValue() + ":",
                remoteRuntime.getVMInformation().getName());
        } catch (Exception e) {
            logger.info("Node @" + node + " already down", e);
        }
    }

    /**
    * Put in a <code>P2PNodeLookup</code> all available nodes during all the
    * time where it is actived.
    * @param vnName Virtual node name, cannot be null.
    * @param jobId cannot be null
    * @return an active object where nodes are received.
    */
    public P2PNodeLookup getMaximunNodes(String vnName, String jobId) {
        return this.getNodes(P2PConstants.MAX_NODE, vnName, jobId);
    }

    /**
     * For load balancing.
     * @return URL of the node where the P2P service is running.
     */
    public StringWrapper getAddress() {
        return new StringWrapper(this.p2pServiceNode.getNodeInformation()
                                                    .getURL());
    }

    /**
       /**
     * Remove a no more waiting nodes accessor.
     * @param accessorToRemove the accessor to remove.
     */
    public void removeWaitingAccessor(P2PNodeLookup accessorToRemove) {
        this.waitingNodesLookup.remove(accessorToRemove);
        logger.debug("Accessor succefuly removed");
    }

    /**
     * @return the list of current acquaintances.
     */
    public Vector getAcquaintanceList() {
        return this.acquaintanceManager.getAcquaintanceList();
    }

    // -------------------------------------------------------------------------
    // Private class method
    // -------------------------------------------------------------------------

    /**
     * If not an old message and ttl > 1 return true else false.
     * @param ttl TTL of the message.
     * @param uuid UUID of the message.
     * @param remoteService P2PService of the first service.
     * @return tur if you should broadcats, false else.
     * @throws org.objectweb.proactive.p2p.service.exception.P2POldMessageException it is an old message
     */
    private boolean broadcaster(int ttl, UUID uuid, P2PService remoteService)
        throws P2POldMessageException {
        // is it an old message?
        boolean isAnOldMessage = this.isAnOldMessage(uuid);

        String remoteNodeUrl = null;
        try {
            remoteNodeUrl = ProActiveObject.getActiveObjectNodeUrl(remoteService);
        } catch (Exception e) {
            isAnOldMessage = true;
        }

        String thisNodeUrl = this.p2pServiceNode.getNodeInformation().getURL();

        if (!isAnOldMessage && !remoteNodeUrl.equals(thisNodeUrl)) {
            if (ttl > 0) {
                logger.debug("Forwarding message request");
                return true;
            }
            return false;
        }

        // it is an old message: nothing to do
        // NO REMOVE the isDebugEnabled message
        if (logger.isDebugEnabled()) {
            if (isAnOldMessage) {
                logger.debug("Old message request with #" + uuid);
            } else {
                logger.debug("The peer is me: " + remoteNodeUrl);
            }
        }

        throw new P2POldMessageException();
    }

    /**
     * If number of acquaintances is less than NOA return <code>true</code>, else
     * use random factor.
     * @param remoteService the remote service which is asking acquaintance.
     * @return <code>true</code> if this peer should be an acquaintance, else
     * <code>false</code>.
     */
    private boolean shouldBeAcquaintance(P2PService remoteService) {
        if (this.acquaintanceManager.contains(remoteService).booleanValue()) {
            logger.debug("The remote peer is already known");
            return false;
        }
        if (this.acquaintanceManager.size().intValue() < NOA) {
            logger.debug("NOA not reached: I should be an acquaintance");
            return true;
        }
        int random = randomizer.nextInt(100);
        if (random < EXPL_MSG) {
            logger.debug("Random said: I should be an acquaintance");
            return true;
        }
        logger.debug("Random said: I should not be an acquaintance");
        return false;
    }

    /**
     * If ti's not an old message add teh sequence number in the list.
     * @param uuid the uuid of the message.
     * @return <code>true</code> if it was an old message, <code>false</code> else.
     */
    private boolean isAnOldMessage(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        if (oldMessageList.contains(uuid)) {
            return true;
        }
        if (oldMessageList.size() == MSG_MEMORY) {
            oldMessageList.remove(0);
        }
        oldMessageList.add(uuid);
        return false;
    }

    /**
     * Wake up all node lookups.
     */
    private void wakeUpEveryBody() {
        for (int i = 0; i < this.waitingNodesLookup.size(); i++) {
            (this.waitingNodesLookup.get(i)).wakeUp();
        }
    }

    //--------------------------------------------------------------------------
    // Active Object methods
    //--------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        logger.debug("Entering initActivity");

        this.service = new Service(body);

        try {
            // Reference to my current p2pServiceNode
            this.p2pServiceNode = NodeFactory.getNode(body.getNodeURL());
        } catch (NodeException e) {
            logger.fatal("Couldn't get reference to the local p2pServiceNode", e);
        }

        logger.debug("P2P Service running in p2pServiceNode: " +
            this.p2pServiceNode.getNodeInformation().getURL());

        this.stubOnThis = (P2PService) ProActiveObject.getStubOnThis();

        Object[] params = new Object[1];
        params[0] = this.stubOnThis;
        try {
            // Active acquaintances
            this.acquaintanceManager = (P2PAcquaintanceManager) ProActiveObject.newActive(P2PAcquaintanceManager.class.getName(),
                    params, this.p2pServiceNode);
            logger.debug("P2P acquaintance manager activated");

            // Get active group
            this.acquaintances = this.acquaintanceManager.getActiveGroup();
            logger.debug("Got active group reference");

            // Active Node Manager
            this.nodeManager = (P2PNodeManager) ProActiveObject.newActive(P2PNodeManager.class.getName(),
                    null, this.p2pServiceNode);
            logger.debug("P2P node manager activated");
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't create one of managers", e);
        } catch (NodeException e) {
            logger.fatal("Couldn't create one the managers", e);
        }
        logger.debug("Exiting initActivity");
    }

    /**
     * @return the P2PService of this local JVM.
     * @throws Exception no P2PService in this local JVM.
     */
    public static P2PService getLocalP2PService() throws Exception {
        UniversalBody body = ProActiveRuntimeImpl.getProActiveRuntime()
                                                 .getActiveObjects(P2P_NODE_NAME,
                P2PService.class.getName()).get(0);
        return (P2PService) MOP.newInstance(P2PService.class.getName(), null,
            (Object[]) null, Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
            new Object[] { body });
    }
}
