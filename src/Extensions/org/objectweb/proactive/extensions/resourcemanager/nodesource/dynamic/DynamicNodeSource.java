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
package org.objectweb.proactive.extensions.resourcemanager.nodesource.dynamic;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCoreSourceInterface;
import org.objectweb.proactive.extensions.resourcemanager.exception.AddingNodesException;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.DynamicNodeSourceInterface;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extensions.resourcemanager.utils.Heap;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


/**
 * Abstract class that provides a simply way to create a dynamicNodeSource active object, i.e node source which
 * handle a set of {@link Node} objects which are only available for a specific time.
 * so it provides mechanism to acquire and give back (release) nodes.
 *
 * A DynamicNodeSource object has a number max of Node to acquire : nbMax.<BR>
 * A DynamicNodeSource object has a time during a node acquired will be kept : ttr.<BR>
 * After a node release,a DynamicNodeSource object has a time to wait before acquiring a new Node :
 * nice.<BR>
 *
 * <BR>You have to implement abstract methods getNode() and releaseNode(Node)
 * methods to acquire and release a Node object (and all other abstract method inherited from {@link NodeSource}).<BR><BR>
 *
 * WARNING : The {@link DynamicNodeSource} you will write must be an Active Object !
 *
 * @see org.objectweb.proactive.extensions.resourcemanager.core.RMCoreSourceInterface
 * @see org.objectweb.proactive.extensions.resourcemanager.core.RMCore
 * @author The ProActive Team
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 *
 */
public abstract class DynamicNodeSource extends NodeSource implements DynamicNodeSourceInterface,
        Serializable, RunActive {

    /** nodes URL and when they must be released */
    private HashMap<String, Long> nodes_ttr;

    /** Heap of the times to get a node.*/
    private Heap<Long> niceTimes;

    /** Max number of nodes that the source has to provide */
    private int nbMax;

    /** Time to wait before acquire a new node just after a node release */
    private int nice;

    /** Node keeping duration before releasing it */
    private int ttr;

    /** Indicate the DynamicNodeSource running state */
    private boolean running = true;

    /** Used to calculate
     * the delay between two nodes acquisitions
     */
    private int delay = 10000;

    /** Logger name */
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.RM_CORE);

    /**
     * ProActive empty constructor.
     */
    public DynamicNodeSource() {
    }

    /**
     * Creates the DynamicNodeSource object.
     * @param id name of the NodeSource.
     * @param rmCore Stub of Active object {@link RMCore}.
     * @param nbMaxNodes Max number of nodes that the source has to provide.
     * @param nice Time to wait before acquire a new node just after a node release.
     * @param ttr Node keeping duration before releasing it.
     */
    public DynamicNodeSource(String id, RMCoreSourceInterface rmCore, int nbMaxNodes, int nice, int ttr) {
        super(id, rmCore);
        this.nbMax = nbMaxNodes;
        this.nice = nice;
        this.ttr = ttr;
    }

    /**
     * Initialization part of NodeSource Active Object.
     * Creates objects member of the object.
     */
    @Override
    public void initActivity(Body body) {
        super.initActivity(body);
        niceTimes = new Heap<Long>(nbMax);
        nodes_ttr = new HashMap<String, Long>();
        long currentTime = System.currentTimeMillis();

        // delaying the node adding.
        for (int i = 0; i < nbMax; i++) {
            niceTimes.add(currentTime + ((i * delay) / nbMax));
        }
    }

    /**
     * Periodically updates the internal state of the DynamicNodeSource.
     * Verify if there are nodes to release and nodes to acquire, by calling cleanAndGet().
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (running) {
            service.blockingServeOldest(3000);
            if (!this.toShutdown) {
                cleanAndGet();
            }
        }
    }

    /**
     * Terminates activity of DynamicNodeSource Active Object.
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
    }

    /**
     * Shutdown the dynamic node source.
     * call to shutdown function of the super class.
     * clearing the heap of nice times, no nodes are got anymore
     * ask to the Core to remove all the nodes
     * DynamicNodesource will Shutdown after the core confirms all nodes removal.
     * @param preempt true Node source doesn't wait tasks end on its handled nodes,
     * false node source wait end of tasks on its nodes before shutting down
     */
    @Override
    public void shutdown(boolean preempt) {
        super.shutdown(preempt);
        this.niceTimes.clear();
        running = false;

        if (this.nodes.size() > 0) {
            for (Entry<String, Node> entry : this.nodes.entrySet()) {
                this.rmCore.nodeRemovalNodeSourceRequest(entry.getKey(), preempt);
            }
            //preemptive shutdown, no need to wait preemptive removals
            //shutdown immediately
            if (preempt) {
                terminateNodeSourceShutdown();
            }
        } else {
            //no nodes handled by the node source, 
            //so node source can be stopped and removed immediately
            //(preemptive shutdown or not) 
            terminateNodeSourceShutdown();
        }
    }

    /**
     * Returns the max number of nodes to acquire.
     * @return int max number of nodes to acquire;
     */
    public int getNbMaxNodes() {
        return nbMax;
    }

    /**
     * Returns the time to wait before acquire a new node just after a node release.
     * @return time to wait before acquire a new node just after a node release.
     */
    public int getNiceTime() {
        return nice;
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.DynamicNodeSourceInterface#getTimeToRelease()
     */
    public int getTimeToRelease() {
        return ttr;
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.DynamicNodeSourceInterface#setNbMaxNodes(int)
     */
    public void setNbMaxNodes(int nb) {
        this.nbMax = nb;
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.DynamicNodeSourceInterface#setNiceTime(int)
     */
    public void setNiceTime(int nice) {
        this.nice = nice;
    }

    /**
     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.DynamicNodeSourceInterface#setTimeToRelease(int)
     */
    public void setTimeToRelease(int ttr) {
        this.ttr = ttr;
    }

    /**
     * True if the node {@link DynamicNodeSource#nodes_ttr} is reached for this node.
     * @param node the node object to test.
     * @return true if the node must be released, false otherwise.
     */
    protected boolean isNodeToRelease(Node node) {
        Long stamp = this.nodes_ttr.get(node.getNodeInformation().getURL());
        if (stamp == null) {
            return false;
        } else {
            return System.currentTimeMillis() > stamp;
        }
    }

    /**
     * Gives the Hashmap associate an acquired node with its releasing deadline.
     * @return an Hashmap containning Node - releasing deadline.
     */
    protected HashMap<String, Long> getNodesTtr_List() {
        return this.nodes_ttr;
    }

    /**
     * release the nodes which have reached their TTR, Get back nodes if Nice Time is elapsed.
     * <BR>This method is called periodically.<BR>
     * First Method verify if acquired node have reached there TTR, if yes,
     * dynamicNodeSource ask to {@link RMCore} to release the node (by a softly way, i.e waiting the job's end if the node is busy).<BR>
     * Then if {@link DynamicNodeSource#nbMax} number is not reached, it will try to acquire new nodes, according to this max number.
     *
     */
    private void cleanAndGet() {
        assert this.niceTimes.size() <= this.nbMax;
        assert this.nodes_ttr.size() <= this.nbMax;
        long currentTime = System.currentTimeMillis();

        // cleaning part
        Iterator<Entry<String, Long>> iter = this.nodes_ttr.entrySet().iterator();
        long time = System.currentTimeMillis();
        while (iter.hasNext()) {
            Entry<String, Long> entry = iter.next();
            if (time > entry.getValue()) {
                iter.remove();
                this.nodes_ttr.remove(entry.getKey());
                this.rmCore.nodeRemovalNodeSourceRequest(entry.getKey(), false);
            }
        }

        // Getting part
        while ((nodes.size() < nbMax) && (niceTimes.peek() != null) && (niceTimes.peek() < currentTime)) {
            Node node = getNode();
            if (node == null) {
                niceTimes.extract();
                break;
            } else {
                currentTime = System.currentTimeMillis();
                nodes_ttr.put(node.getNodeInformation().getURL(), currentTime + ttr);
                this.addNewAvailableNode(node, this.SourceId, this.SourceId);
                niceTimes.extract();
            }
        }

        //add nice times to the niceTimes heap if more nodes have to be acquired
        //(nodes have fallen or nbMAx value has been changed)
        int localDelay = 0;
        while ((this.nodes_ttr.size() + this.niceTimes.size()) < this.nbMax) {
            newNiceTime(localDelay);
            localDelay += this.delay;
        }
    }

    /**
     * Performs operation to remove a node by an explicit Admin request,
     * on the contrary of a normal get-and-release node cycle
     * performed by a dynamic source.
     * (RMAdmin has asked to remove a node or remove the dynamic source).
     * Node to be removed must be handled by source (verify before).
     * @param nodeUrl
     * @param killNode if the node's runtime has to be killed after the removal 
     */
    public void nodeRemovalCoreRequest(String nodeUrl, boolean killNode) {
        Node node = this.getNodebyUrl(nodeUrl);
        if (this.nodes_ttr.containsKey(nodeUrl)) {
            this.nodes_ttr.remove(nodeUrl);
        }
        this.removeFromList(node);

        //node removal asked by RMCore with killing node action, 
        //so this node has been already removed from Core.
        //just remove the node from node Source
        if (killNode) {
            //a node is killed by a preemptive removal request 
            try {
                node.getProActiveRuntime().killRT(false);
            } catch (IOException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //if softly removal, just release the node
            releaseNode(node);
        }

        //all nodes has been removed and NodeSource has been asked to shutdown:
        //shutdown the Node source
        if (this.toShutdown && (this.nodes.size() == 0)) {
            terminateNodeSourceShutdown();
        }
    }

    /**
     * Create a new Nice time in the heap of nice times.
     */
    protected void newNiceTime(int shift) {
        long currentTime = System.currentTimeMillis();
        niceTimes.insert(currentTime + nice + shift);
    }

    // ----------------------------------------------------------------------//
    // definitions of abstract methods inherited from NodeSource, 
    // called by RMCore
    // ----------------------------------------------------------------------//

    //    /**
    //     * Confirms a remove request asked previously by the DynamicNodeSource object.
    //     * <BR>Verify if the node is already handled by the NodeSource (node could have been detected down).
    //     * Verify if the node has to be killed after the remove confirmation, and kill it if it has to.
    //     * @param nodeUrl url of the node.
    //     * @see org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource#confirmRemoveNode(String)
    //     */
    //    @Override
    //    public void nodeRemovalCoreRequest(String nodeUrl, boolean preempt) {
    //        //verifying if node is already in the list,
    //        //node could have fallen between remove request and the confirm
    //        if (this.nodes.containsKey(nodeUrl)) {
    //            if (this.nodesToKillOnConfirm.contains(nodeUrl)) {
    //                this.nodesToKillOnConfirm.remove(nodeUrl);
    //                this.killNodeRT(this.getNodebyUrl(nodeUrl));
    //            } else {
    //                releaseNode(this.getNodebyUrl(nodeUrl));
    //            }
    //            //remove node from the main list
    //            removeFromList(this.getNodebyUrl(nodeUrl));
    //
    //            //shutdown nodesource part
    //            if (this.toShutdown) {
    //                if (this.nodes.size() == 0) {
    //                    //Node source is to shutdown and all nodes have been removed :
    //                    //finish the shutdown
    //                    this.rmCore.internalRemoveSource(this.SourceId, this.getSourceEvent());
    //                    //terminates runActivty's infinite loop.
    //                    running = false;
    //                }
    //            } else {
    //                newNiceTime();
    //            }
    //        }
    //    }

    /**
     * Manages an explicit adding nodes request asked by RMAdmin object.
     * <BR>Called by {@link RMCore}.<BR>
     * Ask to a DynamicNodesource object to add static nodes is prohibited.
     * So this method just return an addingNodesException.
     * @param app GCM Application descriptor containing virtual nodes to deploy.
     * @throws AddingNodesException always.
     */
    @Override
    public void nodesAddingCoreRequest(GCMApplication app) throws AddingNodesException {
        throw new AddingNodesException("Node source : " + this.SourceId +
            " Node cannot be added to a dynamic source");
    }

    /**
     * Adds an already deployed node to the Node source.
     * Operation impossible on a dynamic node source
     * AddingnodesException always launched.
     * @param nodeUrl
     * @throws AddingNodesException if lookup has failed
     * or asked to a dynamicnodeSource object.
     */
    public void nodeAddingCoreRequest(String nodeUrl) throws AddingNodesException {
        throw new AddingNodesException("Node source : " + this.SourceId +
            " Node cannot be added to a dynamic source");
    }

    // ----------------------------------------------------------------------//
    // intern methods called by the dynamicNodesource
    // to implements by dynamic source type
    // ----------------------------------------------------------------------//    

    /**
     * Way to get a new dynamic node.
     * <BR>Abstract method to implement according to the specific dynamicNodeSource.
     * @return the new node got.
     */
    protected abstract Node getNode();

    /**
     * Way to give back a node which has reached his TTR.
     * <BR>Abstract method to implement according to the specific dynamicNodeSource.
     * @param node node to release.
     */
    protected abstract void releaseNode(Node node);

    /**
     * Way to give Kill a node's RT.
     * RMAdmin has asked to kill the node, runtime is killed on after Core's confirm.
     * <BR>Abstract method to implement according to the specific dynamicNodeSource.
     * @param node node to kill.
     */
    protected abstract void killNodeRT(Node node);
}
