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
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.Loggers;
import org.objectweb.proactive.p2p.service.P2PAcquaintanceManager;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.util.P2PConstants;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Vector;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Jan 18, 2005
 */
public class P2PNodesLookup implements InitActive, RunActive, EndActive,
    P2PConstants, Serializable {
    private static final Logger logger = Logger.getLogger(Loggers.P2P_NODES);
    private Vector nodesList;
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
    private P2PAcquaintanceManager acquaintances;
    private String vnName;
    private VirtualNode vn;
    private String jobId;
    private P2PNodesLookup stub;
    private ProActiveRuntime paRuntime;
    private String parUrl;

    public P2PNodesLookup() {
        // the empty constructor
    }

    public P2PNodesLookup(Integer numberOfAskedNodes,
        P2PService localP2pService, P2PAcquaintanceManager acquaintances,
        String vnName, String jobId) {
        this.nodesList = new Vector();
        this.expirationTime = System.currentTimeMillis() + TIMEOUT;
        this.numberOfAskedNodes = numberOfAskedNodes.intValue();
        assert (this.numberOfAskedNodes > 0) ||
        (this.numberOfAskedNodes == MAX_NODE) : "None authorized value for asked nodes";
        if (this.numberOfAskedNodes == MAX_NODE) {
            this.expirationTime = Long.MAX_VALUE;
        }
        this.localP2pService = localP2pService;
        this.acquaintances = acquaintances;
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
     * @param node the node to kill.
     */
    public void killNode(Node node) {
        String nodeUrl = node.getNodeInformation().getURL();

        // Get remote reference to the node manager
        ProActiveRuntime remoteRuntime = node.getProActiveRuntime();
        ArrayList remoteAO = null;
        try {
            remoteAO = remoteRuntime.getActiveObjects(P2P_NODE_NAME,
                    P2PNodeManager.class.getName());
        } catch (ProActiveException e) {
            logger.warn("Couldn't get reference to remote node manager at " +
                nodeUrl, e);
        }
        if (remoteAO.size() == 1) {
            P2PNodeManager remoteNodeManager = (P2PNodeManager) remoteAO.get(0);
            remoteNodeManager.leaveNode(node);
            if (logger.isInfoEnabled()) {
                logger.info("Node at " + nodeUrl + " succefuly killed");
            }
        } else {
            logger.warn(
                "Couldn't kill remote node: no remote node manager found at " +
                nodeUrl);
        }

        // TODO Adding unregister method in PA Runtime
    }

    /**
     * <p>Kill all received node.</p>
     * <p>Warning: if nodes are removed from the Collection, these nodes will be
     * not kill.</p>
     */
    public void killAllNodes() {
        for (int index = 0; index < this.nodesList.size(); index++) {
            Node currentNode = (Node) this.nodesList.get(index);
            this.killNode(currentNode);
        }
    }

    /**
     * Asynchronous adding node.
     * @param node the node to add.
     */
    public void addNode(Node node) {
        boolean r = this.nodesList.add(node);
        if (r) {
            this.acquiredNodes++;
            if (logger.isInfoEnabled()) {
                logger.info("Lookup got " + this.acquiredNodes + " nodes");
            }
            ProActiveRuntime remoteRt = node.getProActiveRuntime();
            remoteRt.addAcquaintance(this.parUrl);
            try {
                this.paRuntime.register(remoteRt, remoteRt.getURL(), "p2p",
                    System.getProperty(PROPERTY_ACQUISITION) + ":",
                    this.paRuntime.getVMInformation().getName());
            } catch (ProActiveException e) {
                logger.warn("Couldn't recgister the remote runtime", e);
            }
        }
        if (logger.isDebugEnabled()) {
            if (r) {
                logger.debug("Node at " + node.getNodeInformation().getURL() +
                    " succefuly added");
            } else {
                logger.debug("Node at " + node.getNodeInformation().getURL() +
                    " NOT added");
            }
        }
    }

    /**
     * Wake up the active object, to send asking node message.
     */
    public void wakeUp() {
        // nothing to do, just wake up the run activity
    }

    // -------------------------------------------------------------------------
    // ProActive methods
    // -------------------------------------------------------------------------

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        this.stub = (P2PNodesLookup) ProActive.getStubOnThis();
        try {
            this.paRuntime = RuntimeFactory.getDefaultRuntime();
            this.parUrl = this.paRuntime.getURL();
        } catch (ProActiveException e) {
            logger.fatal("Problem to get local runtime", e);
        }
    }

    private int currentTtl = 0;

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        if (logger.isInfoEnabled()) {
            logger.info("Looking for " + this.numberOfAskedNodes + " nodes");
        }
        Service service = new Service(body);
        while (!this.allArrived() &&
                (System.currentTimeMillis() < this.expirationTime)) {
            if (this.numberOfAskedNodes == MAX_NODE) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Aksing MAX number of nodes");
                }

                // Send a message to everybody
                this.localP2pService.askingNode(TTL, null,
                    this.localP2pService, this.vnName, this.jobId);
            } else {
                // incremental TTL
                if ((currentTtl < TTL) &&
                        (this.numberOfAskedNodes <= this.acquaintances.size())) {
                    currentTtl++;
                } else {
                    currentTtl = TTL;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Asking nodes with TTL = " + currentTtl);
                }
                this.localP2pService.askingNode(currentTtl, null,
                    this.localP2pService, this.vnName, this.jobId);
            }

            // Serving request
            service.blockingServeOldest(LOOKUP_FREQ);
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
        }
    }

    /**
     * Remove this nodes accessor from the waiting node accessors list in the
     * local P2P service.
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
        Service service = new Service(body);
        if (logger.isInfoEnabled()) {
            logger.info("All nodes (" + this.acquiredNodes +
                ") arrived ending activity");
        }
        this.localP2pService.removeWaitingAccessor(this.stub);
        while (this.nodesList.size() > 0) {
            service.blockingServeOldest(LOOKUP_FREQ);
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("This P2P nodes lookup is no more active, bye..");
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
        while (this.nodesList.size() != 0) {
            Object elem = this.nodesList.get(0);
            v.add(elem);
            this.nodesList.remove(elem);
        }
        return v;
    }
}
