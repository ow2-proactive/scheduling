/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.p2p;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.extra.p2p.service.P2PService;
import org.objectweb.proactive.extra.p2p.service.StartP2PService;
import org.objectweb.proactive.extra.p2p.service.messages.Message;
import org.objectweb.proactive.extra.p2p.service.messages.RequestNodesMessage;
import org.objectweb.proactive.extra.p2p.service.messages.RequestSingleNodeMessage;
import org.objectweb.proactive.extra.p2p.service.node.P2PLookupInt;
import org.objectweb.proactive.extra.p2p.service.node.P2PNodeAck;
import org.objectweb.proactive.extra.p2p.service.node.P2PNodeLookup;
import org.objectweb.proactive.extra.p2p.service.node.P2PNodeManager;
import org.objectweb.proactive.extra.p2p.service.util.P2PConstants;
import org.objectweb.proactive.extra.p2p.service.util.UniversalUniqueID;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.RMCoreSourceInterface;
import org.ow2.proactive.resourcemanager.nodesource.dynamic.DynamicNodeSource;


/**
 * Implementation of a ProActive Peer to Peer dynamic node source. A peer to
 * peer DynamicNodeSource object acquire nodes from a ProActive P2P
 * infrastructure. So a ProActive peer to peer network is considered as a
 * dynamic NodeSource, on which nodes can be acquired to submit jobs.<BR>
 * <BR>
 * 
 * WARNING : you must instantiate this class as an Active Object !
 * 
 * @author The ProActive Team
 * 
 */
public class P2PNodeSource extends DynamicNodeSource implements InitActive, P2PLookupInt {

    /**
     * A HashMap associating a Node with its remote NodeManager
     * used for a node release
     */
    private HashMap<String, P2PNodeManager> nodeManagerMap = new HashMap<String, P2PNodeManager>();

    /** Vector of known peers used at startup */
    private Vector<String> peerUrls;

    /** Peer to peer Service object which is interface to peer to peer network */
    private P2PService p2pService;

    /**
     * Stub of this active object, used to put in NodeRequestMessage objects).
     */
    private P2PLookupInt myStub;

    /**
     * TTL of nodes request through the P2P network
     */
    private static final int TTL = Integer.parseInt(PAProperties.PA_P2P_TTL.getValue());

    /**
     * The lookup frequency in ms of the P2P network, here this value
     * is used as a retry frequency, in case of previous not successful nodes
     * requests.
     * between two NodeRequests sent to P2P network
     */
    private long lookup_freq = Integer.parseInt(PAProperties.PA_P2P_LOOKUP_FREQ.getValue());;

    /**
     * ProActive runtime of this active object.
     */
    private ProActiveRuntime paRuntime;

    /**
     * ProActive runtime's URL of this active object.
     */
    private String parUrl;

    /**
     * Timestamp of the last node Request sen to P2P network
     */
    private long lastNodeRequestTimeStamp;

    /**
     * ProActive empty constructor
     */
    public P2PNodeSource() {
    }

    /**
     * Active object constructor.
     * 
     * @param id
     *            Name of node source.
     * @param rmCore
     *            Stub of Active object {@link RMCore}.
     * @param nbMaxNodes
     *            Max number of nodes that the source has to provide.
     * @param nice
     *            Time to wait before acquire a new node just after a node
     *            release.
     * @param ttr
     *            Node keeping duration before releasing it.
     * @param peerUrls
     *            Vector containing known peers .
     * 
     */
    public P2PNodeSource(String id, RMCoreSourceInterface rmCore, int nbMaxNodes, int nice, int ttr,
            Vector<String> peerUrls) {
        super(id, rmCore, nbMaxNodes, nice, ttr);
        this.peerUrls = peerUrls;
    }

    /**
     * Initialization part of P2PNodeSource Active Object. call the init part of
     * super class {@link DynamicNodeSource} Launching
     * {@link StartP2PService P2P Service}, which connect to an existing P2P
     * infrastructure.
     */
    @Override
    public void initActivity(Body body) {
        super.initActivity(body);

        // set this method in Immediate service in order to avoid avoid
        // waiting time for a monitor, if the P2PNode is blocked by
        // the acquisition delay of a peer to peer node.
        PAActiveObject.setImmediateService("getSourceEvent");

        //initiate this timeStamp with current time
        lastNodeRequestTimeStamp = System.currentTimeMillis();

        try {
            this.paRuntime = RuntimeFactory.getDefaultRuntime();
            this.parUrl = this.paRuntime.getURL();
            myStub = (P2PLookupInt) PAActiveObject.getStubOnThis();

            //we don't share the RM's JVM
            PAProperties.PA_P2P_NO_SHARING.setValue(true);

            StartP2PService startServiceP2P = new StartP2PService(this.peerUrls);
            startServiceP2P.start();
            this.p2pService = startServiceP2P.getP2PService();

            try {
                //long wait time before passing to run activity
                //it's better that P2Pservice get several acquaintances
                //before sending the first nodes request 
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * Terminates activity of P2PNodeSource Active Object.
     */
    @Override
    public void endActivity(Body body) {
        super.endActivity(body);
    }

    /**
     * release the nodes which have reached their TTR, Get back nodes if Nice Time is elapsed.
     * <BR>This method is called periodically.<BR>
     * First Method verify if acquired node have reached there TTR, if yes,
     * dynamicNodeSource ask to {@link org.ow2.proactive.resourcemanager.core.RMCore} to release the node (by a softly way, i.e waiting the job's end if the node is busy).<BR>
     * Then if {@link DynamicNodeSource#nbMax} number is not reached, it will try to acquire new nodes, according to this max number.
     */
    protected void cleanAndGet() {
        assert this.niceTimes.size() <= this.nbMax;
        assert this.nodes_ttr.size() <= this.nbMax;

        long time = System.currentTimeMillis();

        //add nice times to the niceTimes heap in case of previous 
        //nodes request  launched didn't had (all)responses 
        //or nodes could have fallen or nbMAx value has been changed
        //but we wait a lookup frequency period after the last node request
        while (!this.toShutdown && (nodes.size() + niceTimes.size()) < this.nbMax &&
            this.lastNodeRequestTimeStamp + this.lookup_freq < time) {
            newNiceTime(0);
        }

        // cleaning part
        Iterator<Entry<String, Long>> iter = this.nodes_ttr.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Long> entry = iter.next();
            if (time > entry.getValue()) {
                iter.remove();
                //call to RMCore to release the node
                this.rmCore.nodeRemovalNodeSourceRequest(entry.getKey(), false);
            }
        }

        time = System.currentTimeMillis();

        // Getting part
        //count number of nodes to ask (try to group nodes requests)
        int nodesToAskNumber = 0;
        while (!this.toShutdown && (nodes.size() < nbMax) && (niceTimes.peek() != null) &&
            (niceTimes.peek() < time)) {
            niceTimes.extract();
            nodesToAskNumber++;
        }

        //launch nodes request through P2P network
        if (nodesToAskNumber > 0) {
            launchNodesRequest(nodesToAskNumber);
        }
    }

    /**
     * Launch a nodes request through P2P network.
     * Build the RequestNodeMessage object, and ask
     * to P2PService to execute the request. 
     * 
     * @param nodesNumber
     */
    public void launchNodesRequest(int nodesNumber) {
        logger.info("[" + this.SourceId + "] Asking " + nodesNumber + " nodes");
        Message m = null;
        UniversalUniqueID uuid = UniversalUniqueID.randomUUID();
        if (nodesNumber == 1) {
            m = new RequestSingleNodeMessage(TTL, uuid, this.p2pService, this.myStub, this.SourceId,
                this.SourceId);
        } else {
            m = new RequestNodesMessage(TTL, uuid, this.p2pService, nodesNumber, this.myStub, this.SourceId,
                this.SourceId, true, ".*");
        }

        lastNodeRequestTimeStamp = System.currentTimeMillis();
        this.p2pService.requestNodes(m);
    }

    // ----------------------------------------------------------------------//
    // definition of abstract methods inherited from dynamicNodeSource
    // ----------------------------------------------------------------------//

    /**
     * Gives back a node to P2P infrastructure. Internal method, node is given
     * back to the source Kill the node given by the source and its active
     * objects. Kill the {@link P2PNodeLookup} object, remove the node from
     * internal node list and create a new Nice Time
     * 
     * @param node
     *            Node object to release
     * @Override releaseNode from DynamicNodeSource
     */
    @Override
    protected void releaseNode(Node node) {

        String nodeUrl = node.getNodeInformation().getURL();
        logger.info("[" + this.SourceId + "] release node " + nodeUrl);

        this.nodes_ttr.remove(nodeUrl);

        //unregistering the node in P2P's part
        P2PNodeManager remoteNodeManager = this.nodeManagerMap.remove(nodeUrl);
        remoteNodeManager.leaveNode(node, this.SourceId);

        ProActiveRuntime remoteRuntime = node.getProActiveRuntime();
        unregisterRemoteRuntime(remoteRuntime);

        newNiceTime(this.nice);
    }

    /** unregister a remote of P2P node to give back
     * @param remoteRuntime remoteRuntime to unregister
     */
    public void unregisterRemoteRuntime(ProActiveRuntime remoteRuntime) {
        try {
            remoteRuntime.unregisterVirtualNode(this.SourceId);
            remoteRuntime.rmAcquaintance(this.parUrl);
            paRuntime.rmAcquaintance(remoteRuntime.getURL());

            // Unregister the remote runtime
            this.paRuntime.unregister(remoteRuntime, remoteRuntime.getURL(), "p2p",
                    PAProperties.PA_P2P_ACQUISITION.getValue() + ":", remoteRuntime.getVMInformation()
                            .getName());
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Not used in this implementation
     * @see org.ow2.proactive.resourcemanager.nodesource.dynamic.DynamicNodeSource#getNode()
     */
    @Override
    protected Node getNode() {
        return null;
    }

    /**
     * Get the RMNodeSourceEvent object of the source. Create the
     * {@link RMNodeSourceEvent} object related to the P2PNodeSource
     * 
     * @return event representing the source.
     */
    @Override
    public RMNodeSourceEvent getSourceEvent() {
        return new RMNodeSourceEvent(this.getSourceId(), RMConstants.P2P_NODE_SOURCE_TYPE);
    }

    // ----------------------------------------------------------------------//
    // method called by the intern class Pinger
    // ----------------------------------------------------------------------//

    /**
     * Manages a down node. A down node has been detected remove the broken node
     * from the list this.nodes remove node from the TTR list Inform the RMNode
     * Manager about the broken node, Create a new nice time.
     */
    @Override
    public void detectedPingedDownNode(String nodeUrl) {
        Node node = getNodebyUrl(nodeUrl);
        if (node != null) {
            // remove node from the list
            removeFromList(node);
            // remove the node from the node_ttr HashMap
            this.getNodesTtr_List().remove(nodeUrl);
            // remove the node from the nodeManager-nodeUrl HashMap
            this.nodeManagerMap.remove(nodeUrl);
            // informing RMNode Manager about the broken node
            this.rmCore.setDownNode(nodeUrl);

            //unregister its P2PService's runtime
            ProActiveRuntime remoteRuntime = node.getProActiveRuntime();
            unregisterRemoteRuntime(remoteRuntime);
        }
    }

    /**
     * Shutdown the node source All nodes are removed from node source and from
     * RMCore
     * 
     * @param preempt
     *            true Node source doesn't wait tasks end on its handled nodes,
     *            false node source wait end of tasks on its nodes before
     *            shutting down
     */
    @Override
    public void shutdown(boolean preempt) {
        super.shutdown(preempt);
    }

    /**
     * Call when all nodes of this NodeSource have been unregistered, 
     * so we can perform the shutdown. 
     * Kill the ProActive node containing all P2P active object
     * 
     * @see org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource#terminateNodeSourceShutdown()
     */
    @Override
    protected void terminateNodeSourceShutdown() {
        super.terminateNodeSourceShutdown();
        //kill the P2P node, containing all 
        //P2P active objects
        try {

            String uri = URIBuilder.buildURIFromProperties(
                    PAActiveObject.getNode().getVMInformation().getHostName(), P2PConstants.P2P_NODE_NAME)
                    .toString();

            String P2PNodeUrl = URIBuilder.getNameFromURI(uri);
            this.paRuntime.killNode(P2PNodeUrl);

        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------//
    // methods called by remote NodeManager, when a remote peer want to supply
    // P2PNodeSource in nodes. Override P2PLookupInt
    // ----------------------------------------------------------------------//

    /**
     * Receipt a shared node; This method is called by a NodeRequest
     * message, when this is message is arrived on a peer that has nodes available
     * a want to provide some.
     * 
     * check that the good amount of nodes isn't already reached.
     * if node register the node to the waiting list. The node is ready to be used 
     * by the P2PNodeSource
     * Register the node to the waiting list
     *
     * 
     * @see org.objectweb.proactive.extra.p2p.service.node.P2PLookupInt#giveNode(org.objectweb.proactive.core.node.Node, org.objectweb.proactive.extra.p2p.service.node.P2PNodeManager)
     *
     * 
     * @param givenNode the shared node.
     * @param remoteNodeManager the remote node manager for the given node.
     * @return the total number of nodes still needed.
     */
    public P2PNodeAck giveNode(Node givenNode, P2PNodeManager remoteNodeManager) {
        logger.info("[" + this.SourceId + "] node received from " + givenNode.getNodeInformation().getURL());

        // Check that we don't have already reached the desired amount of booked nodes
        // or we are in a shutdown case, in that case we don't accept nodes anymore.
        if (nodes.size() < this.nbMax && !this.toShutdown) {

            //register the new node in RM's part
            String nodeUrl = givenNode.getNodeInformation().getURL();
            this.nodeManagerMap.put(nodeUrl, remoteNodeManager);
            long currentTime = System.currentTimeMillis();
            this.nodes_ttr.put(nodeUrl, currentTime + ttr);
            addNewAvailableNode(givenNode, this.SourceId, this.SourceId);

            //register node in P2P part
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
            return new P2PNodeAck(true);

        } else {
            //full house or shutdown , no need to accept this node.
            if (this.toShutdown) {
                logger.info("[" + this.SourceId + "] shutting down, no need of node :" +
                    givenNode.getNodeInformation().getURL());
            } else {
                logger.info("[" + this.SourceId + "] Full house, no need of node :" +
                    givenNode.getNodeInformation().getURL());
            }
            return new P2PNodeAck(false);
        }
    }

    /**
     * @see org.objectweb.proactive.extra.p2p.service.node.P2PLookupInt#giveNodeForMax(java.util.Vector, org.objectweb.proactive.extra.p2p.service.node.P2PNodeManager)
     */
    public void giveNodeForMax(Vector<Node> givenNodes, P2PNodeManager remoteNodeManager) {
        //this method is a hack in P2PNodeLookup upper class. there is no way to No-Ack received nodes 
        // (no callback awaited by peers that call this method) 
        //and peers call this method only if they received a node request with a node number equals to MAX_NODE=-1.... 
        // choose not use this hack, and not ask -1 nodes in this lookup.
    }

    @Override
    protected void killNodeRT(Node node) {
        try {
            node.getProActiveRuntime().killRT(false);
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
