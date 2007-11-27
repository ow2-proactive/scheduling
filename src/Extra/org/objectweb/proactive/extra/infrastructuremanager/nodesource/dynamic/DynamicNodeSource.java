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
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreSourceInt;
import org.objectweb.proactive.extra.infrastructuremanager.exception.AddingNodesException;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.DynamicNSInterface;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.utils.Heap;


/**
 * Abstract class that provide a way to simply create {@link DynamicNodeSource}
 * You just have to write the {@link #getNode()} and {@link #releaseNode()}
 * methods to create a dynamic node source.
 * WARNING : The {@link DynamicNodeSource} you will write must be an Active Object !
 * @author ProActive team
 *
 */
public abstract class DynamicNodeSource extends NodeSource
    implements DynamicNSInterface, Serializable, InitActive, RunActive,
        EndActive {
    // nodes URL and when they must be released
    private HashMap<String, Long> nodes_ttr;

    // Heap of the times to get a node.
    private Heap<Long> niceTimes;

    //save nodeUrl that have to be killed on remove confirm
    private ArrayList<String> nodesToKillOnConfirm;

    // 3 parameters, used by the DynamicNSInterface 
    private int nbMax;
    private int nice;
    private int ttr;
    private boolean running;
    private int delay = 20000;
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);

    public DynamicNodeSource() {
    }

    public DynamicNodeSource(String id, IMCoreSourceInt nodeManager,
        int nbMaxNodes, int nice, int ttr) {
        super(id, nodeManager);
        this.nbMax = nbMaxNodes;
        this.nice = nice;
        this.ttr = ttr;
        this.nodesToKillOnConfirm = new ArrayList<String>();
    }

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

    /**
     * Periodically update the internal state of the dynamic
     * node source.
     * TODO The Time To Update (here 3000) should be parametrable.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (running) {
            service.blockingServeOldest(3000);
            cleanAndGet();
        }
    }

    /**
     * If not shutdown, do it.
     */
    public void endActivity(Body body) {
        if (!running) {
            shutdown();
        }
    }

    public int getNbMaxNodes() {
        return nbMax;
    }

    public int getNiceTime() {
        return nice;
    }

    public int getTimeToRelease() {
        return ttr;
    }

    public void setNbMaxNodes(int nb) {
        this.nbMax = nb;
    }

    public void setNiceTime(int nice) {
        this.nice = nice;
    }

    public void setTimeToRelease(int ttr) {
        this.ttr = ttr;
    }

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
     * True if the node the ttr is reached for this node.
     * @param node
     * @return
     */
    protected boolean isNodeToRelease(Node node) {
        Long stamp = this.nodes_ttr.get(node.getNodeInformation().getURL());
        if (stamp == null) {
            return false;
        } else {
            return System.currentTimeMillis() > stamp;
        }
    }

    protected HashMap<String, Long> getNodesTtr_List() {
        return this.nodes_ttr;
    }

    /**
     * release the nodes which have reached their TTR ;
     * Get back nodes if Nice Time is elapsed.
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
     * Create a new Nice time in the heap of nice times
     */
    protected void newNiceTime() {
        long currentTime = System.currentTimeMillis();
        niceTimes.insert(currentTime + nice);
    }

    // ----------------------------------------------------------------------//
    // definitions of abstract methods inherited from NodeSource, 
    // called by IMNodeManager
    // ----------------------------------------------------------------------//    
    @Override
    public void addNodes(ProActiveDescriptor pad, String padName)
        throws AddingNodesException {
        throw new AddingNodesException("Node source : " + this.SourceId +
            " Node cannot be added to a dynamic source");
    }

    @Override
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

    @Override
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
     *  way to get a new dynamic node
     */
    protected abstract Node getNode();

    /**
     * way to give back a node which has reached his TTR
     * and IMNodeManager has confirm the release
     * @param node
     */
    protected abstract void releaseNode(Node node);
}
