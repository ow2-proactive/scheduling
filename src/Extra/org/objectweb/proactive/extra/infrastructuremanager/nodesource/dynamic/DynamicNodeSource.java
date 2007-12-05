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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt;
import org.objectweb.proactive.extra.infrastructuremanager.exception.AddingNodesException;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.DynamicNSInterface;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.utils.Heap;


/**
 * Abstract class that provides a simply way to create a dynamicNodeSource active object, i.e node source which
 * handle a set of {@link Node} objects which are only available for a specific time.
 * so it provides mechanism to acquire and give back (release) nodes.
 *
 * A DynamicNodeSource object has a number max of Node to acquire : {@link DynamicNodeSource#nbMax}.<BR>
 * A DynamicNodeSource object has a time during a node acquired will be kept : {@link DynamicNodeSource#ttr}.<BR>
 * After a node release,a DynamicNodeSource object has a time to wait before acquiring a new Node : {@link DynamicNodeSource#nice}.<BR>
 *
 * <BR>You have to write the {@link DynamicNodeSource#getNode()} and {@link DynamicNodeSource#releaseNode(Node)}
 * methods to acquire and release a Node object (and all other abstract method inherited from {@link NodeSource}).<BR><BR>
 *
 * WARNING : The {@link DynamicNodeSource} you will write must be an Active Object !
 *
 * @see org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt
 * @see org.objectweb.proactive.extra.infrastructuremanager.core.IMCore
 * @author ProActive team
 *
 */
public abstract class DynamicNodeSource extends NodeSource
    implements DynamicNSInterface, Serializable, InitActive, RunActive,
        EndActive {

    /** nodes URL and when they must be released */
    private HashMap<String, Long> nodes_ttr;

    /** Heap of the times to get a node.*/
    private Heap<Long> niceTimes;

    /** Save nodeUrl that have to be killed on remove confirm */
    private ArrayList<String> nodesToKillOnConfirm;

    /** Max number of nodes that the source has to provide */
    private int nbMax;

    /** Time to wait before acquire a new node just after a node release */
    private int nice;

    /** Node keeping duration before releasing it */
    private int ttr;

    /** Indicate the DynamicNodeSource running state */
    private boolean running;

    /** At the DynamicNodeSource startup, used to calculate
     * the delay between two nodes acquisitions
     */
    private int delay = 20000;

    /** Logger name */
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);

    /**
     * ProActive empty constructor.
     */
    public DynamicNodeSource() {
    }

    /**
     * Creates the DynamicNodeSource object.
     * @param id name of the NodeSource.
     * @param imcore Stub of Active object {@link IMCore}.
     * @param nbMaxNodes Max number of nodes that the source has to provide.
     * @param nice Time to wait before acquire a new node just after a node release.
     * @param ttr Node keeping duration before releasing it.
     */
    public DynamicNodeSource(String id, IMCoreSourceInt imcore, int nbMaxNodes,
        int nice, int ttr) {
        super(id, imcore);
        this.nbMax = nbMaxNodes;
        this.nice = nice;
        this.ttr = ttr;
        this.nodesToKillOnConfirm = new ArrayList<String>();
    }

    /**
     * Initialization part of NodeSource Active Object.
     * Creates objects member of the object.
     */
    public void initActivity(Body body) {
        super.initActivity(body);
        niceTimes = new Heap<Long>(nbMax);
        nodes_ttr = new HashMap<String, Long>();
        running = true;
        long currentTime = System.currentTimeMillis();

        // delaying the node adding.
        for (int i = 0; i < nbMax; i++) {
            niceTimes.add((long) currentTime + ((i * delay) / nbMax));
        }
    }

    //TODO The Time To Update (here 3000) should be parameterable.
    /**
     * Periodically updates the internal state of the DynamicNodeSource.
     * Verify if there are nodes to release and nodes to acquire, by calling {@link DynamicNodeSource#cleanAndGet()}
     *
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (running) {
            service.blockingServeOldest(3000);
            cleanAndGet();
        }
    }

    //TODO gsigety refactor the shutdown mechanism 
    /**
     * Terminate activity of DynamicNodeSource Active Object.
     * Stop the Pinger thread of the upper class
     */
    public void endActivity(Body body) {
        super.endActivity(body);
        if (!running) {
            shutdown();
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
     * Returns the node keeping duration.
     */
    public int getTimeToRelease() {
        return ttr;
    }

    /**
     * Set the max number of nodes to acquire.
     */
    public void setNbMaxNodes(int nb) {
        this.nbMax = nb;
    }

    /**
     * Set the time to wait before acquiring a new node just after a node release.
     */
    public void setNiceTime(int nice) {
        this.nice = nice;
    }

    /**
     * Set the node keeping duration before releasing it.
     */
    public void setTimeToRelease(int ttr) {
        this.ttr = ttr;
    }

    /**
     * initiate the shutdown of the DynamicNodeSource ActiveObject.
     */
    public void shutdown() {
        logger.info("Shutting down Node Source : " + getSourceId());
        running = false;
        try {
            for (Node node : this.getNodes())
                releaseNode(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * dynamicNodeSource ask to {@link IMCore} to release the node (by a softly way, i.e waiting the job's end if the node is busy).<BR>
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

            // try soft release only free nodes !!
            if (time > entry.getValue()) {
                iter.remove();
                this.imCore.internalRemoveNode(entry.getKey(), false);
            }
        }

        // Getting part
        while ((nodes.size() <= nbMax) && (niceTimes.peek() != null) &&
                (niceTimes.peek() < currentTime)) {
            Node node = getNode();
            if (node == null) {
                niceTimes.extract();
                newNiceTime();
                break;
            } else {
                currentTime = System.currentTimeMillis();
                nodes_ttr.put(node.getNodeInformation().getURL(),
                    currentTime + ttr);
                this.addNewAvailableNode(node, this.SourceId, this.SourceId);
                niceTimes.extract();
            }
        }
    }

    /**
     * Create a new Nice time in the heap of nice times.
     */
    protected void newNiceTime() {
        long currentTime = System.currentTimeMillis();
        niceTimes.insert(currentTime + nice);
    }

    // ----------------------------------------------------------------------//
    // definitions of abstract methods inherited from NodeSource, 
    // called by IMNCore
    // ----------------------------------------------------------------------//

    /**
     * Confirms a remove request asked previously by the DynamicNodeSource object.
     * <BR>Verify if the node is already handled by the NodeSource (node could have been detected down).
     * Verify if the node has to be killed after the remove confirmation, and kill it if it has to.
     * @param nodeUrl url of the node.
     * @see org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource#confirmRemoveNode(String)
     */
    public void confirmRemoveNode(String nodeUrl) {
        //verifying if node is already in the list,
        //node could have fallen between remove request and the confirm
        if (this.nodes.containsKey(nodeUrl)) {
            if (this.nodesToKillOnConfirm.contains(nodeUrl)) {
                this.nodesToKillOnConfirm.remove(nodeUrl);
                //killing a dynamic node, what to do ?
                releaseNode(this.getNodebyUrl(nodeUrl));
            } else {
                releaseNode(this.getNodebyUrl(nodeUrl));
            }
        }
    }

    /**
     * Manages an explicit adding nodes request asked by IMAdmin object.
     * <BR>Called by {@link IMCore}.<BR>
     * Ask to a DynamicNodesource object to add static nodes is prohibited.
     * So this method just return an addingNodesException.
     * @param pad ProActive Deployment descriptor representing nodes to deploy.
     * @throws AddingNodesException always.
     */
    public void addNodes(ProActiveDescriptor pad) throws AddingNodesException {
        throw new AddingNodesException("Node source : " + this.SourceId +
            " Node cannot be added to a dynamic source");
    }

    /**
     * Removes a specific Node asked by Admin.
     * DynamicNodeSource object has received a node removing request asked by the {@link IMAdmin}.
     * <BR>If the removing request is in a softly way and a softly removing request has been already made,
     * the DynamicNodeSource object has nothing to do, otherwise perform the release.
     * @param nodeUrl URL of the node.
     * @param preempt true if the node must be removed immediately, without waiting job ending if the node is busy (softly way),
     * false the node is removed just after the job ending if the node is busy.<BR><BR>
     * @see org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource#forwardRemoveNode(String, boolean)
     */
    public void forwardRemoveNode(String nodeUrl, boolean preempt) {
        //verifying that node is already in the list,
        //node could have been already released or
        if (this.nodes.containsKey(nodeUrl)) {
            //node in nodes List and node not in node_ttr list == node already in releasing state
            if (!this.nodes_ttr.containsKey(nodeUrl)) {
                if (preempt) {
                    this.imCore.internalRemoveNode(nodeUrl, preempt);
                }

                //else nothing, softly remove request has already been send 
            } else {
                //node not in TTR list
                this.nodes_ttr.remove(nodeUrl);
                this.imCore.internalRemoveNode(nodeUrl, preempt);
            }
            if (preempt && !nodesToKillOnConfirm.contains(nodeUrl)) {
                this.nodesToKillOnConfirm.add(nodeUrl);
            }
        }
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
}
