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
package org.objectweb.proactive.loadbalancing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.loadbalancing.metrics.Metric;
import org.objectweb.proactive.loadbalancing.metrics.MetricFactory;


/**
 * This is the main class for load balancing algorithms, all implementations should inherite
 * from this one.  It provides the methods for register the load (used by the load monitor)
 * and to send active objects to another node, choosing the one with the shortest queue.
 *
 * The load balance for Active Objects is server initiated: overloaded machines has to begin
 * the balance process. Using this paradigm, particular implementations of Load Balancing algorithms
 * have to implement only the method startBalancing.
 *
 * Also, this class  provides a method to know if this CPU is in underloaded state
 * (usefull in server oriented load balancing algorithms).
 *
 *
 * @author Javier.Bustos@sophia.inria.fr
 *
 */
public class LoadBalancer implements ProActiveInternalObject {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.LOAD_BALANCING);
    protected LoadMonitor lm;
    protected Metric metric;
    protected Node myNode;
    protected ArrayList<LoadBalancer> loadBalancers;
    private static final int STEAL = 1;
    private static final int BALANCE = 2;
    protected String balancerName;
    protected Random randomizer;
    protected LoadBalancer myThis;
    protected InformationRecover informationRecover;

    public LoadBalancer() {
    }

    public LoadBalancer(MetricFactory mf) {
        this.randomizer = new Random();
        this.metric = mf.getNewMetric();
    }

    public void startBalancing() {
        internalAction(BALANCE);
    }

    public void stealWork() {
        internalAction(STEAL);
    }

    public void sendActiveObjectsTo(Node remoteNode, double remoteRanking) {
        if (this.metric == null) {
            return;
        }
        if ((this.metric.getRanking() * LoadBalancingConstants.STEAL_FACTOR) < remoteRanking) { // it's better than me!
            sendActiveObjectsTo(remoteNode);
        }
    }

    protected void getActiveObjectsFrom(LoadBalancer remoteBalancer, double remoteRanking) {
        if (this.metric == null) {
            return;
        }
        if (remoteRanking < (this.metric.getRanking() * LoadBalancingConstants.BALANCE_FACTOR)) { // I'm better than him!
            remoteBalancer.sendActiveObjectsTo(myNode);
        }
    }

    /**
     * This method sends an active object to a destiny, choosing the active objects
     * which don't implement <code>ProActiveInternalObject</code> and having the shortest queue.
     * @param <code>destNode</code> Node destiny to send the active abject.
     * If this node is local, this method does nothing.
     * @return none
     */
    public void sendActiveObjectsTo(Node destNode) {
        if (NodeFactory.isNodeLocal(destNode)) {
            return;
        }

        try {
            BodyMap knownBodies = LocalBodyStore.getInstance().getLocalBodies();

            if (knownBodies.size() < 1) {
                return;
            }

            java.util.Iterator bodiesIterator = knownBodies.bodiesIterator();

            /** ******** Choosing the shortest service queue ******** */
            int minLength = Integer.MAX_VALUE;
            Body minBody = null;
            while (bodiesIterator.hasNext()) {
                Body activeObjectBody = (Body) bodiesIterator.next();
                Object testObject = activeObjectBody.getReifiedObject();

                /********** Only some Active Objects can migrate *************/
                boolean testSerialization = !(testObject instanceof ProActiveInternalObject) &&
                    !(testObject instanceof NotLoadBalanceableObject);

                if (activeObjectBody.isAlive()) {
                    if (activeObjectBody.isActive() && testSerialization) {
                        int aoQueueLenght = activeObjectBody.getRequestQueue().size();
                        if (aoQueueLenght < minLength) {
                            minLength = aoQueueLenght;
                            minBody = activeObjectBody;
                        }
                    }
                }
            }

            /***********  we have the Active Object with shortest queue, so we send the migration call ********/
            if ((minBody != null) && minBody.isActive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[Loadbalancer] Migrating (" +
                        minBody.getReifiedObject().getClass().getName() + ") from " +
                        myNode.getNodeInformation().getURL() + " to " +
                        destNode.getNodeInformation().getURL());
                }

                PAMobileAgent.migrateTo(minBody, destNode, false);
                informationRecover.register(this.getName(), this.metric.getLoad(), destNode
                        .getNodeInformation().getURL(), minBody.getReifiedObject().getClass().getName());
            }
        } catch (IllegalArgumentException e) {
            logger.error("[LoadBalancer] " + e.getLocalizedMessage());
        } catch (SecurityException e) {
            logger.error("[LoadBalancer] Object doesn't have migrateTo method");
        } catch (MigrationException e) {
            logger.error("[LoadBalancer] Object can't migrate (?)");

            /** ****** if you cannot migrate, is not my business ********** */
        }
    }

    /*
    /**
     * This method returns if this machine is in an underloaded state
     * @param none
     * @return none
     */

    //    public boolean AreYouUnderloaded() {
    //        return underloaded;
    //    }
    public String getName() {
        return balancerName;
    }

    public void init(ArrayList<LoadBalancer> loadBalancers, InformationRecover ir) {
        try {
            this.myNode = PAActiveObject.getNode();
            this.informationRecover = ir;
        } catch (NodeException e) {
            e.printStackTrace();
        }
        this.loadBalancers = loadBalancers;
        this.myThis = (LoadBalancer) PAActiveObject.getStubOnThis();
        this.balancerName = myNode.getNodeInformation().getURL();

        // by now we use only Linux
        lm = new LoadMonitor(myThis, metric);
        new Thread(lm).start();
    }

    private void internalAction(int action) {
        int size = loadBalancers.size();
        if (size < 1) {
            return;
        }

        int first = randomizer.nextInt(size);
        for (int i = 0; (i < LoadBalancingConstants.SUBSET_SIZE) && (i < size); i++) {
            LoadBalancer remoteLb = loadBalancers.get((first + i) % size);
            try {
                switch (action) {
                    case STEAL:
                        remoteLb.sendActiveObjectsTo(myNode, this.metric.getRanking());
                        break;
                    case BALANCE:
                        remoteLb.getActiveObjectsFrom(myThis, this.metric.getRanking());
                        break;
                }
            } catch (ProActiveRuntimeException e) {
                loadBalancers.remove((first + i) % size);
                size--;
            }
        }
    }

    public void notifyLoadBalancers() {
        LoadBalancer lb;
        LoadBalancer myThis = (LoadBalancer) PAActiveObject.getStubOnThis();
        Iterator<LoadBalancer> it = loadBalancers.iterator();
        while (it.hasNext()) {
            lb = it.next();
            if (!lb.equals(this)) {
                lb.addNewBalancer(myThis);
            }
            lb = null;
        }
    }

    public void addNewBalancer(LoadBalancer lb) {
        loadBalancers.add(lb);
    }
}
