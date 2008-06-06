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
package org.objectweb.proactive.extra.p2p.service.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.p2p.service.P2PService;
import org.objectweb.proactive.extra.p2p.service.messages.Message;
import org.objectweb.proactive.extra.p2p.service.messages.RequestNodesMessage;
import org.objectweb.proactive.extra.p2p.service.messages.RequestSingleNodeMessage;
import org.objectweb.proactive.extra.p2p.service.util.P2PConstants;
import org.objectweb.proactive.extra.p2p.service.util.UniversalUniqueID;


/**
 * @author The ProActive Team
 *
 * Created on Jan 18, 2005
 */
public class P2PNodeLookup implements InitActive, RunActive, EndActive, P2PConstants, Serializable,
        ProActiveInternalObject {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_NODES);
    private Vector<Node> waitingNodesList;
    private Vector<String> nodesToKillList;
    private long expirationTime;
    private static final long TIMEOUT = Long.parseLong(PAProperties.PA_P2P_NODES_ACQUISITION_T0.getValue());
    private static final long LOOKUP_FREQ = Long.parseLong(PAProperties.PA_P2P_LOOKUP_FREQ.getValue());
    private static final int TTL = Integer.parseInt(PAProperties.PA_P2P_TTL.getValue());
    private int numberOfAskedNodes;
    private int acquiredNodes = 0;
    private P2PService localP2pService_active;
    private String vnName;
    private String jobId;
    private P2PNodeLookup stub;
    private ProActiveRuntime paRuntime;
    private String parUrl;
    private HashMap<String, P2PNodeManager> nodeManagerMap = new HashMap<String, P2PNodeManager>();
    private boolean killAllFlag = false;
    private String nodeFamilyRegexp = null;

    public P2PNodeLookup() {
        // the empty constructor
    }

    public P2PNodeLookup(Integer numberOfAskedNodes, P2PService localP2pService, String vnName, String jobId,
            String nodeFamilyRegexp) {
        this.waitingNodesList = new Vector<Node>();
        this.nodesToKillList = new Vector<String>();
        this.expirationTime = System.currentTimeMillis() + TIMEOUT;
        this.numberOfAskedNodes = numberOfAskedNodes.intValue();
        assert (this.numberOfAskedNodes > 0) || (this.numberOfAskedNodes == MAX_NODE) : "None authorized value for asked nodes";
        // Use special case: do not check TO
        //        if (this.numberOfAskedNodes == MAX_NODE) {
        //            this.expirationTime = Long.MAX_VALUE;
        //        }
        this.localP2pService_active = localP2pService;
        this.vnName = vnName;
        this.jobId = jobId;
        this.nodeFamilyRegexp = nodeFamilyRegexp;
    }

    // -------------------------------------------------------------------------
    // Access methods
    // -------------------------------------------------------------------------

    /**
     * Check if all asked nodes are arrived. <code>false</code> for asking all
     * available nodes.
     * @return <code>true</code> if all asked nodes are arrived, <code>false</code>
     * else.
     */
    public boolean allArrived() {
        return (this.numberOfAskedNodes != MAX_NODE) ? (this.numberOfAskedNodes == this.acquiredNodes)
                : false;
    }

    /**
     * Check if n nodes are arrived.
     * @param n number of needed nodes.
     * @return <code>true</code> if n nodes are available, <code>false</code else.
     */
    public boolean nArrived(int n) {
        return n <= this.acquiredNodes;
    }

    /**
     * Kill the given node.
     * @param node the node url to kill.
     */
    public void killNode(String node) {
        try {
            logger.info("START KILLNODE");
            Node remoteNode = NodeFactory.getNode(node);
            ProActiveRuntime remoteRuntime = remoteNode.getProActiveRuntime();
            P2PNodeManager remoteNodeManager = this.nodeManagerMap.get(node);

            remoteRuntime.unregisterVirtualNode(vnName);
            remoteRuntime.rmAcquaintance(this.parUrl);
            this.paRuntime.rmAcquaintance(remoteRuntime.getURL());
            remoteNodeManager.leaveNode(remoteNode, this.vnName);

            logger.info("Node at " + node + " succefuly removed");

            // Unregister the remote runtime
            this.paRuntime.unregister(remoteRuntime, remoteRuntime.getURL(), "p2p",
                    PAProperties.PA_P2P_ACQUISITION.getValue() + ":", remoteRuntime.getVMInformation()
                            .getName());
        } catch (Exception e) {
            logger.info("Node @" + node + " already down");
        } finally {
            this.nodesToKillList.remove(node);
        }
    }

    /**
     * <p>Kill all received node.</p>
     * <p>Warning: if nodes are removed from the Collection, these nodes will be
     * not kill.</p>
     */
    public void killAllNodes() {
        logger.info("Size of nodes:" + nodesToKillList.size());
        this.killAllFlag = true;
        while (this.nodesToKillList.size() > 0) {
            String currentNode = this.nodesToKillList.get(0);
            this.killNode(currentNode);
        }
    }

    /**
     * Wake up the active object, to send asking node message.
     */
    public void wakeUp() {
        // nothing to do, just wake up the run activity
    }

    /**
     * Receipt a reference to a shared node.
     *
     * @param givenNode the shared node.
     * @param remoteNodeManager the remote node manager for the given node.
     * @return the total number of nodes still needed.
     */
    public P2PNodeAck giveNode(Node givenNode, P2PNodeManager remoteNodeManager) {
        if (logger.isDebugEnabled()) {
            logger.debug("Given node received from " + givenNode.getNodeInformation().getURL());
        }

        // Get currrent nodes accessor
        if (!this.allArrived()) {
            this.waitingNodesList.add(givenNode);
            String nodeUrl = givenNode.getNodeInformation().getURL();
            this.nodeManagerMap.put(nodeUrl, remoteNodeManager);
            this.acquiredNodes++;
            ProActiveRuntime remoteRt = givenNode.getProActiveRuntime();
            try {
                remoteRt.addAcquaintance(this.parUrl);
                this.paRuntime.addAcquaintance(remoteRt.getURL());
                this.paRuntime.register(remoteRt, remoteRt.getURL(), "p2p", PAProperties.PA_P2P_ACQUISITION
                        .getValue() +
                    ":", remoteRt.getVMInformation().getName());
            } catch (ProActiveException e) {
                logger.warn("Couldn't recgister the remote runtime", e);
            }
            logger.info("Node at " + nodeUrl + " succefuly added");
            logger.info("Lookup got " + this.acquiredNodes + " nodes");
            return new P2PNodeAck(true);
        }
        return new P2PNodeAck(false);
    }

    public void giveNodeForMax(Vector<Node> givenNodes, P2PNodeManager remoteNodeManager) {
        // Get currrent nodes accessor
        this.waitingNodesList.addAll(givenNodes);
        for (int i = 0; i < givenNodes.size(); i++) {
            Node current = givenNodes.get(i);
            String nodeUrl = current.getNodeInformation().getURL();
            this.nodeManagerMap.put(nodeUrl, remoteNodeManager);
            this.acquiredNodes++;
            ProActiveRuntime remoteRt = current.getProActiveRuntime();
            try {
                remoteRt.addAcquaintance(this.parUrl);
                this.paRuntime.addAcquaintance(remoteRt.getURL());
                this.paRuntime.register(remoteRt, remoteRt.getURL(), "p2p", PAProperties.PA_P2P_ACQUISITION
                        .getValue() +
                    ":", remoteRt.getVMInformation().getName());
            } catch (ProActiveException e) {
                logger.warn("Couldn't recgister the remote runtime", e);
            }
            logger.info("Node at " + nodeUrl + " succefuly added");
        }
        logger.info("Lookup MAX got " + this.acquiredNodes + " nodes");
    }

    /**
     * <p>Returns whether the node accessor is active or not.
     * The nodes accessor is active as long as it has an associated thread running
     * to serve the requests by calling methods on the active object and looking for
     * asked nodes.</p>
     * <p>If the nodes accessor is not active, looking for nodes is stopped.</p>
     * @return whether the nodes accessor is active or not.
     */
    public boolean isActive() {
        return PAActiveObject.getBodyOnThis().isActive();
    }

    /**
     * @return all available and node and remove them from the lookup.
     */
    public Vector getAndRemoveNodes() {
        Vector<Node> v = new Vector<Node>();
        while (this.waitingNodesList.size() != 0) {
            Object elem = this.waitingNodesList.get(0);
            v.add((Node) elem);
            this.waitingNodesList.remove(elem);
            this.nodesToKillList.add(((Node) elem).getNodeInformation().getURL());
        }
        return v;
    }

    /**
     * Reurn all nodes asked until the timeout.
     * @param timeout the timeout in milliseconds.
     * @return A Collection of nodes.
     */
    public Vector getNodes(long timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        Service service = new Service(PAActiveObject.getBodyOnThis());
        while (!this.allArrived() && (System.currentTimeMillis() < endTime)) {
            //            this.localP2pService.askingNode(TTL, null, this.localP2pService,
            //                this.numberOfAskedNodes - this.acquiredNodes, stub,
            //                this.vnName, this.jobId, this.nodeFamilyRegexp);
            this.localP2pService_active.message(new RequestNodesMessage(TTL, null,
                this.localP2pService_active, this.numberOfAskedNodes - this.acquiredNodes, stub, this.vnName,
                this.jobId, true, this.nodeFamilyRegexp));

            // Serving request
            if (timeout < LOOKUP_FREQ) {
                service.blockingServeOldest(500);
            } else {
                service.blockingServeOldest(LOOKUP_FREQ);
            }
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
        }
        return ((P2PNodeLookup) PAActiveObject.getStubOnThis()).getAndRemoveNodes();
    }

    /**
     * Migrate the P2P node lookup tyo a new node.
     *
     * @param nodeUrl the URL of the destination node.
     */
    public void moveTo(String nodeUrl) {
        try {
            PAMobileAgent.migrateTo(nodeUrl);
        } catch (Exception e) {
            logger.fatal("Couldn't migrate the node lookup to " + nodeUrl, e);
        }
    }

    // -------------------------------------------------------------------------
    // ProActive methods
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        this.stub = (P2PNodeLookup) PAActiveObject.getStubOnThis();
        try {
            this.paRuntime = RuntimeFactory.getDefaultRuntime();
            this.parUrl = this.paRuntime.getURL();
        } catch (ProActiveException e) {
            logger.fatal("Problem to get local runtime", e);
        }
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        logger.info("Looking for " +
            ((this.numberOfAskedNodes == MAX_NODE) ? "MAX" : (this.numberOfAskedNodes + "")) + " nodes");
        Service service = new Service(body);

        String reason = null;
        logger.debug("Asking nodes");

        Message m = null;
        UniversalUniqueID uuid = UniversalUniqueID.randomUUID();
        if (this.numberOfAskedNodes == 1) {
            m = new RequestSingleNodeMessage(TTL, uuid, this.localP2pService_active, this.stub, this.vnName,
                this.jobId);
        } else {
            //         //       if (onlyUnderloadedAnswer) {
            //                    m = new RequestNodesMessage(TTL, uuid, this.localP2pService,
            //                            this.numberOfAskedNodes - this.acquiredNodes, stub,
            //                            this.vnName, this.jobId, onlyUnderloadedAnswer, null);
            //                } else {
            m = new RequestNodesMessage(TTL, uuid, this.localP2pService_active, this.numberOfAskedNodes -
                this.acquiredNodes, stub, this.vnName, this.jobId, true, this.nodeFamilyRegexp);
        }
        //            }
        this.localP2pService_active.requestNodes(m);

        while (true) {
            // Send a message to everybody
            //            if (onlyUnderloadedAnswer) {
            ////                this.localP2pService.askingNode(1, null, this.localP2pService,
            ////                    this.numberOfAskedNodes - this.acquiredNodes, stub,
            ////                    this.vnName, this.jobId, onlyUnderloadedAnswer); // Load balancer question
            //                this.localP2pService.askingNode(new RequestNodesMessage(1,
            //                        null, this.localP2pService,
            //                        this.numberOfAskedNodes - this.acquiredNodes, stub,
            //                        this.vnName, this.jobId, onlyUnderloadedAnswer, null)); // Load balancer question
            //            } else {
            ////                this.localP2pService.askingNode(TTL, null,
            ////                    this.localP2pService,
            ////                    this.numberOfAskedNodes - this.acquiredNodes, stub,
            ////                    this.vnName, this.jobId, this.nodeFamilyRegexp);
            //                
            //                this.localP2pService.askingNode(new RequestNodesMessage(1,
            //                        null, this.localP2pService,
            //                        this.numberOfAskedNodes - this.acquiredNodes, stub,
            //                        this.vnName, this.jobId, true, this.nodeFamilyRegexp)); 
            //            }

            // Serving request
            logger.debug("Waiting for requests");
            service.blockingServeOldest(LOOKUP_FREQ);
            while (service.hasRequestToServe()) {
                if (logger.isDebugEnabled()) {
                    logger.info("Serving request: " + service.getOldest().getMethodName());
                }
                service.serveOldest();
            }

            // Test conditions to go out of the loop
            if (this.killAllFlag) {
                reason = "killing nodes request";
                break;
            } else if (this.numberOfAskedNodes == MAX_NODE) {
                reason = "Max node asked";
                continue;
            } else if (this.allArrived()) {
                reason = "all nodes are arrived";
                break;
            } else if ((System.currentTimeMillis() > this.expirationTime)) {
                reason = "timeout is expired";
                break;
            } else {
                reason = "Normal case continue";
                continue;
            }
        }

        if (reason == null) {
            reason = "Houston. We have a problem...";
        }
        logger.info("Ending loop activity because: " + reason);
    }

    /**
     * Remove this nodes accessor from the waiting node accessors list in the
     * local P2P service.
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
        Service service = new Service(body);
        logger.info("Nodes (" + this.acquiredNodes + ") arrived ending activity");
        this.localP2pService_active.removeWaitingAccessor(this.stub);
        while ((this.waitingNodesList.size() > 0) || (this.nodesToKillList.size() > 0)) {
            service.blockingServeOldest(LOOKUP_FREQ);
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
        }
        logger.debug(body);
        logger.info("This P2P nodes lookup is no more active, bye..");
    }
}
