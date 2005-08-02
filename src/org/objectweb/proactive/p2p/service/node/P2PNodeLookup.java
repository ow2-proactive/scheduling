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

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.util.P2PConstants;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Vector;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Jan 18, 2005
 */
public class P2PNodeLookup implements InitActive, RunActive, EndActive,
    P2PConstants, Serializable {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_NODES);
    private Vector waitingNodesList;
    private Vector nodesToKillList;
    private long expirationTime;
    private static final long TIMEOUT = Long.parseLong(System.getProperty(
                PROPERTY_NODES_ACQUISITION_T0));
    private static final long LOOKUP_FREQ = Long.parseLong(System.getProperty(
                PROPERTY_LOOKUP_FREQ));
    private static final int TTL = Integer.parseInt(System.getProperty(
                PROPERTY_TTL));
    private int numberOfAskedNodes;
    private int acquiredNodes = 0;
    private P2PService localP2pService;
    private String vnName;
    private VirtualNode vn;
    private String jobId;
    private P2PNodeLookup stub;
    private ProActiveRuntime paRuntime;
    private String parUrl;
    private HashMap nodeManagerMap = new HashMap();
    private boolean killAllFlag = false;
    private boolean onlyUnderloadedAnswer = false;

    public P2PNodeLookup() {
        // the empty constructor
    }

    public P2PNodeLookup(Integer numberOfAskedNodes,
        P2PService localP2pService, String vnName, String jobId) {
        this.waitingNodesList = new Vector();
        this.nodesToKillList = new Vector();
        this.expirationTime = System.currentTimeMillis() + TIMEOUT;
        this.numberOfAskedNodes = numberOfAskedNodes.intValue();
        assert (this.numberOfAskedNodes > 0) ||
        (this.numberOfAskedNodes == MAX_NODE) : "None authorized value for asked nodes";
        // Use special case: do not check TO
        //        if (this.numberOfAskedNodes == MAX_NODE) {
        //            this.expirationTime = Long.MAX_VALUE;
        //        }
        this.localP2pService = localP2pService;
        this.vnName = vnName;
        this.jobId = jobId;
    }

    // new constructor, for a load balanced environment
    public P2PNodeLookup(Integer numberOfAskedNodes,
        P2PService localP2pService, String vnName, String jobId,
        String onlyUnderloadedAnswer) {
        this.onlyUnderloadedAnswer = Boolean.getBoolean(onlyUnderloadedAnswer);
        this.waitingNodesList = new Vector();
        this.nodesToKillList = new Vector();
        this.expirationTime = System.currentTimeMillis() + TIMEOUT;
        this.numberOfAskedNodes = numberOfAskedNodes.intValue();
        assert (this.numberOfAskedNodes > 0) ||
        (this.numberOfAskedNodes == MAX_NODE) : "None authorized value for asked nodes";
        if (this.numberOfAskedNodes == MAX_NODE) {
            this.expirationTime = Long.MAX_VALUE;
        }
        this.localP2pService = localP2pService;
        this.vnName = vnName;
        this.jobId = jobId;
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
        return (this.numberOfAskedNodes != MAX_NODE)
        ? (this.numberOfAskedNodes == this.acquiredNodes) : false;
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
            Node remoteNode = NodeFactory.getNode(node);
            ProActiveRuntime remoteRuntime = remoteNode.getProActiveRuntime();
            P2PNodeManager remoteNodeManager = (P2PNodeManager) this.nodeManagerMap.get(node);

            remoteRuntime.unregisterVirtualNode(vnName);
            remoteRuntime.rmAcquaintance(this.parUrl);
            this.paRuntime.rmAcquaintance(remoteRuntime.getURL());
            remoteNodeManager.leaveNode(remoteNode, this.vnName);

            logger.info("Node at " + node + " succefuly removed");

            // Unregister the remote runtime
            this.paRuntime.unregister(remoteRuntime, remoteRuntime.getURL(),
                "p2p", System.getProperty(PROPERTY_ACQUISITION) + ":",
                remoteRuntime.getVMInformation().getName());
        } catch (Exception e) {
            logger.warn("Couldn't leave node @" + node, e);
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
        this.killAllFlag = true;
        while (this.nodesToKillList.size() > 0) {
            String currentNode = (String) this.nodesToKillList.get(0);
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
     * @return true if the given node is accepted by the P2PNodeLookup, false
     * else.
     */
    public P2PNodeAck giveNode(Node givenNode, P2PNodeManager remoteNodeManager) {
        if (logger.isDebugEnabled()) {
            logger.debug("Given node received from " +
                givenNode.getNodeInformation().getURL());
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
                this.paRuntime.register(remoteRt, remoteRt.getURL(), "p2p",
                    System.getProperty(PROPERTY_ACQUISITION) + ":",
                    remoteRt.getVMInformation().getName());
            } catch (ProActiveException e) {
                logger.warn("Couldn't recgister the remote runtime", e);
            }
            logger.info("Node at " + nodeUrl + " succefuly added");
            logger.info("Lookup got " + this.acquiredNodes + " nodes");
            return new P2PNodeAck(true);
        } else {
            return new P2PNodeAck(false);
        }
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
        return ProActive.getBodyOnThis().isActive();
    }

    /**
     * @return all available and node and remove them from the lookup.
     */
    public Vector getAndRemoveNodes() {
        Vector v = new Vector();
        while (this.waitingNodesList.size() != 0) {
            Object elem = this.waitingNodesList.get(0);
            v.add(elem);
            this.waitingNodesList.remove(elem);
            this.nodesToKillList.add(((Node) elem).getNodeInformation().getURL());
        }
        return v;
    }

    /**
     * Migrate the P2P node lookup tyo a new node.
     *
     * @param nodeUrl the URL of the destination node.
     */
    public void moveTo(String nodeUrl) {
        try {
            ProActive.migrateTo(nodeUrl);
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
        this.stub = (P2PNodeLookup) ProActive.getStubOnThis();
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
            ((this.numberOfAskedNodes == MAX_NODE) ? "MAX"
                                                   : (this.numberOfAskedNodes +
            "")) + " nodes");
        Service service = new Service(body);

        String reason = null;
        while (true) {
            logger.debug("Aksing nodes");

            // Send a message to everybody
            if (onlyUnderloadedAnswer) {
                this.localP2pService.askingNode(1, null, this.localP2pService,
                    this.numberOfAskedNodes - this.acquiredNodes, stub,
                    this.vnName, this.jobId, onlyUnderloadedAnswer); // Load balancer question
            } else {
                this.localP2pService.askingNode(TTL, null,
                    this.localP2pService,
                    this.numberOfAskedNodes - this.acquiredNodes, stub,
                    this.vnName, this.jobId);
            }

            // Serving request
            service.blockingServeOldest(LOOKUP_FREQ);
            while (service.hasRequestToServe()) {
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
        logger.info("Nodes (" + this.acquiredNodes +
            ") arrived ending activity");
        this.localP2pService.removeWaitingAccessor(this.stub);
        while ((this.waitingNodesList.size() > 0) ||
                (this.nodesToKillList.size() > 0)) {
            service.blockingServeOldest(LOOKUP_FREQ);
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
        }
        logger.info("This P2P nodes lookup is no more active, bye..");
    }
}
