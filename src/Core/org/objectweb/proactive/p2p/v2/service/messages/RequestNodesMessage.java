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
package org.objectweb.proactive.p2p.v2.service.messages;

import java.util.Vector;

import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.node.P2PNode;
import org.objectweb.proactive.p2p.v2.service.node.P2PNodeAck;
import org.objectweb.proactive.p2p.v2.service.node.P2PNodeLookup;
import org.objectweb.proactive.p2p.v2.service.util.P2PConstants;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;


public class RequestNodesMessage extends BreadthFirstMessage {
    protected int numberOfNodes;
    protected P2PNodeLookup lookup;
    protected String vnName;
    protected String jobId;
    protected boolean underloadedOnly;
    protected String nodeFamilyRegexp;
    protected Boolean active;

    /**
     * @param ttl Time to live of the message, in number of hops.
     * @param uuid UUID of the message.
     * @param remoteService The original sender.
     * @param numberOfNodes Number of asked nodes.
     * @param lookup The P2P nodes lookup.
     * @param vnName Virtual node name.
     * @param jobId
     * @param underloadedOnly determines if it replies with normal "askingNode" method or discard the call
     */
    public RequestNodesMessage(int ttl, UniversalUniqueID uuid,
        P2PService remoteService, int numberOfNodes, P2PNodeLookup lookup,
        String vnName, String jobId, boolean underloadedOnly,
        String nodeFamilyRegexp) {
        super(ttl, uuid, remoteService);
        this.numberOfNodes = numberOfNodes;
        this.lookup = lookup;
        this.vnName = vnName;
        this.jobId = jobId;
        this.underloadedOnly = underloadedOnly;
        this.nodeFamilyRegexp = nodeFamilyRegexp;
    }

    @Override
    public void execute(P2PService target) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting execution for message  " + uuid);
        }
        if (!underloadedOnly || !target.amIUnderloaded(0)) {
            return;
        }

        // Do not give a local node to a local request
        if ((uuid != null) || (numberOfNodes == P2PConstants.MAX_NODE)) {
            // Asking a node to the node manager
            if (numberOfNodes == P2PConstants.MAX_NODE) {
                Vector<Node> nodes = target.nodeManager.askingAllNodes(nodeFamilyRegexp);
                for (int i = 0; i < nodes.size(); i++) {
                    Node current = nodes.get(i);
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
                    lookup.giveNodeForMax(nodes, target.nodeManager);
                    numberOfNodes -= nodes.size();
                    if (numberOfNodes <= 0) {
                        this.active = false;
                    }
                    target.acquaintanceManager_active.setMaxNOA(target.acquaintanceManager_active.getMaxNOA() -
                        1);
                }
            } else {
                P2PNode askedNode = target.nodeManager.askingNode(nodeFamilyRegexp);

                // Asking node available?
                Node nodeAvailable = askedNode.getNode();
                if (nodeAvailable != null) {
                    P2PNodeAck nodeAck = null;

                    try {
                        nodeAck = lookup.giveNode(nodeAvailable,
                                askedNode.getNodeManager());
                    } catch (Exception lookupExcption) {
                        logger.info("Cannot contact the remote lookup",
                            lookupExcption);
                        target.nodeManager.noMoreNodeNeeded(nodeAvailable);
                        return;
                    }
                    if (nodeAck != null) {
                        // Waitng the ACK
                        long endTime = System.currentTimeMillis() +
                            P2PService.ACQ_TO;
                        while ((System.currentTimeMillis() < endTime) &&
                                ProFuture.isAwaited(nodeAck)) {
                            if (target.service.hasRequestToServe()) {
                                target.service.serveAll(target.filter);
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
                            // Prevent  deadlock
                            target.nodeManager.noMoreNodeNeeded(nodeAvailable);
                            return;
                        }
                    }

                    // Waiting ACK or NACK
                    if (nodeAck.ackValue()) {
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
                        numberOfNodes = (numberOfNodes == P2PConstants.MAX_NODE)
                            ? P2PConstants.MAX_NODE : (numberOfNodes - 1);
                        logger.info("Giving 1 node to vn: " + vnName);
                    } else {
                        // It's a NACK node
                        target.nodeManager.noMoreNodeNeeded(nodeAvailable);
                        logger.debug("NACK node received");
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
    }

    @Override
    public boolean shouldExecute() {
        return active;
    }

    @Override
    public boolean shouldTransmit() {
        return active;
    }
}
