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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeComparator;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.IMNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.DynamicNSInterface;
import org.objectweb.proactive.extra.infrastructuremanager.utils.Heap;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


/**
 * Abstract class that provide a way to simply create {@link DynamicNodeSource}
 * You just have to write the {@link #getNode()} and {@link #releaseNode(IMNode)}
 * methods to create a dynamic node source.
 *
 * WARNING : The {@link DynamicNodeSource} you will write must be an Active Object !
 * @author proactive team
 *
 */
public abstract class DynamicNodeSource extends IMNodeSource
    implements DynamicNSInterface, Serializable, InitActive, RunActive,
        EndActive {
    // nodes and when they must be released
    private Map<IMNode, Long> nodes;

    // Heap of the times to get a node.
    private Heap<Long> niceTimes;
    private ArrayList<IMNode> freeNodes;
    private ArrayList<IMNode> busyNodes;
    private ArrayList<IMNode> downNodes;

    // Id of the DNS
    private String stringId;

    // 3 parameters, used by the DynamicNSInterface 
    private int nbMax;
    private int nice;
    private int ttr;
    private boolean running;
    private int delay = 20000;
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);

    public DynamicNodeSource(String id, int nbMaxNodes, int nice, int ttr) {
        this.stringId = id;
        this.nbMax = nbMaxNodes;
        this.nice = nice;
        this.ttr = ttr;
    }

    public DynamicNodeSource() {
    }

    public void initActivity(Body body) {
        freeNodes = new ArrayList<IMNode>();
        busyNodes = new ArrayList<IMNode>();
        downNodes = new ArrayList<IMNode>();
        niceTimes = new Heap<Long>(nbMax);
        nodes = new HashMap<IMNode, Long>();
        running = true;
        long currentTime = System.currentTimeMillis();

        // delaying the node adding.
        for (int i = 0; i < nbMax; i++) {
            niceTimes.add(currentTime + ((i * delay) / nbMax));
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
        if (running) {
            shutdown();
        }
    }

    @Override
    public String getSourceId() {
        return stringId;
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

    public ArrayList<IMNode> getAllNodes() {
        ArrayList<IMNode> res = new ArrayList<IMNode>();
        res.addAll(freeNodes);
        res.addAll(busyNodes);
        res.addAll(downNodes);
        return res;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<IMNode> getBusyNodes() {
        return (ArrayList<IMNode>) busyNodes.clone();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<IMNode> getDownNodes() {
        return (ArrayList<IMNode>) downNodes.clone();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<IMNode> getFreeNodes() {
        return (ArrayList<IMNode>) freeNodes.clone();
    }

    public IntWrapper getNbAllNodes() {
        return new IntWrapper(freeNodes.size() + busyNodes.size() +
            downNodes.size());
    }

    public IntWrapper getNbBusyNodes() {
        return new IntWrapper(busyNodes.size());
    }

    public IntWrapper getNbDownNodes() {
        return new IntWrapper(downNodes.size());
    }

    public IntWrapper getNbFreeNodes() {
        return new IntWrapper(freeNodes.size());
    }

    @Override
    public ArrayList<IMNode> getNodesByScript(SelectionScript script,
        boolean ordered) {
        ArrayList<IMNode> result = getFreeNodes();
        if ((script != null) && ordered) {
            Collections.sort(result, new IMNodeComparator(script));
        }
        return result;
    }

    @Override
    public void setBusy(IMNode imnode) {
        removeFromAllLists(imnode);
        if (!nodes.containsKey(imnode)) {
            throw new RuntimeException("Setting busy a removed node : " +
                imnode.getNodeURL());
        }
        busyNodes.add(imnode);
        try {
            imnode.setBusy();
        } catch (NodeException e1) {
            // A down node shouldn't by busied...
            e1.printStackTrace();
        }
    }

    @Override
    public void setDown(IMNode imnode) {
        //TODO 
        // peut etre rendre directement le noeud a la source dynamique ?
        removeFromAllLists(imnode);
        downNodes.add(imnode);
        imnode.setDown(true);
    }

    @Override
    public void setFree(IMNode imnode) {
        removeFromAllLists(imnode);
        if (isNodeToRelease(imnode)) {
            nodes.remove(imnode);
            niceTimes.insert(System.currentTimeMillis() + nice);
            releaseNode(imnode);
        } else {
            if (!nodes.containsKey(imnode)) {
                throw new RuntimeException("Freeing a removed node : " +
                    imnode.getNodeURL());
            }
            assert (!nodes.containsKey(imnode));
            freeNodes.add(imnode);
            try {
                imnode.setFree();
            } catch (NodeException e1) {
                // A down node shouldn't by busied...
                e1.printStackTrace();
            }
        }
    }

    @Override
    public BooleanWrapper shutdown() {
        logger.info("Shutting down Node Source : " + getSourceId());
        running = false;
        try {
            for (IMNode node : nodes.keySet())
                releaseNode(node);
            return new BooleanWrapper(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new BooleanWrapper(false);
        }
    }

    /**
     * Remove the imnode from all the lists it can appears.
     * @param imnode
     * @return
     */
    protected boolean removeFromAllLists(IMNode imnode) {
        // Free
        boolean free = freeNodes.remove(imnode);

        // Busy
        boolean busy = busyNodes.remove(imnode);

        // Down
        boolean down = downNodes.remove(imnode);

        return free || busy || down;
    }

    /**
     * True if the node the ttr is reached for this node.
     * @param node
     * @return
     */
    protected boolean isNodeToRelease(IMNode node) {
        Long stamp = nodes.get(node);
        if (stamp == null) {
            return false;
        } else {
            return System.currentTimeMillis() > stamp;
        }
    }

    /**
     * release the nodes which have reached their TTR ;
     * Get back nodes if Nice Time is elapsed.
     *
     */
    private void cleanAndGet() {
        long currentTime = System.currentTimeMillis();

        // cleaning part
        Iterator<Entry<IMNode, Long>> iter = nodes.entrySet().iterator();
        long time = System.currentTimeMillis();
        while (iter.hasNext()) {
            Entry<IMNode, Long> entry = iter.next();
            try {
                // release only free nodes !!
                if ((time > entry.getValue()) &&
                        (entry.getKey().isDown() || entry.getKey().isFree())) {
                    iter.remove();
                    removeFromAllLists(entry.getKey());
                    releaseNode(entry.getKey());
                    niceTimes.insert(currentTime + nice);
                }
            } catch (NodeException e) {
                logger.warn("Exception occured", e);
                e.printStackTrace();
            }
        }

        // Getting part
        while ((nodes.size() <= nbMax) && (niceTimes.peek() != null) &&
                (niceTimes.peek() < currentTime)) {
            IMNode node = getNode();
            if (node == null) {
                niceTimes.extract();
                niceTimes.add(System.currentTimeMillis() + nice);
                break;
            }
            nodes.put(node, currentTime + ttr);
            freeNodes.add(node);
            niceTimes.extract();
            // log
            try {
                logger.info("[DYNAMIC SOURCE] get new node : " +
                    node.getNodeInformation().getURL() + " (total = " +
                    nodes.size() + ") while MAX is " + nbMax);
            } catch (NodeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    protected abstract IMNode getNode();

    protected abstract void releaseNode(IMNode node);
}
