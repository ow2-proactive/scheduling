/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.loadbalancing;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


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
public class LoadBalancer {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.LOAD_BALANCING);
    protected boolean underloaded = false;
    protected LoadMonitor lm;
    protected double normalization = 1;
    protected double myLoad = 0;
    protected double ranking = LoadBalancingConstants.RANKING_NORMALIZATION;

    /**
     * This method is called by the LoadMonitor, it updates the load state
     * @param <code>load</code> is the load value, using the load index from the load monitor.
     * @return none
     */
    public void register(double load) {
        myLoad = load;
        if (load > LoadBalancingConstants.OVERLOADED_THREASHOLD) {
            if (underloaded) {
                underloaded = false;
            }
            startBalancing();
        } else if (load >= (LoadBalancingConstants.UNDERLOADED_THREASHOLD * normalization)) {
            if (underloaded) {
                underloaded = false;
            }
        } else {
            if (!underloaded) {
                underloaded = true;
            }
            stealWork();
        }
    }

    /**
     * This method has to be implemented for load balancing algorithms,
     * it starts the load balance process
     * @param none
     * @return none
     */
    public void startBalancing() {
    }
    ;

    /**
     * This method has to be implemented for load balancing algorithms,
     * it starts the work stealing process (underloaded processores which "steal"
     * work from others
     * @param none
     * @return none
     */
    public void stealWork() {
    }

    /**
     * This method sends an active object to a destiny, choosing the active objects
     * which implements <code>Balanceable</code> having the shortest queue.
     * @param <code>destNode</code> Node destiny to send the active abject.
     * If this node is local, this method does nothing.
     * @return none
     */
    public void sendActiveObjectsTo(Node destNode) throws RemoteException {
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
                boolean testSerialization = !(testObject instanceof LoadBalancer);

                if (activeObjectBody.isAlive()) {
                    if (activeObjectBody.isActive() && testSerialization) {
                        int aoQueueLenght = activeObjectBody.getRequestQueue()
                                                            .size();
                        if (aoQueueLenght < minLength) {
                            minLength = aoQueueLenght;
                            minBody = activeObjectBody;
                        }
                    }
                }
            }

            /***********  we have the Active Object with shortest queue, so we send the migration call ********/
            if ((minBody != null) && minBody.isActive()) {
                logger.debug("[Loadbalancer] Migrating from " +
                    minBody.getNodeURL() + " to " +
                    destNode.getNodeInformation().getURL());
                ProActive.migrateTo(minBody, destNode, true);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (MigrationException e) {

            /** ****** if you cannot migrate, is not my business ********** */
        }
    }

    /**
     * This method returns if this machine is in an underloaded state
     * @param none
     * @return none
     */
    public boolean AreYouUnderloaded() {
        return underloaded;
    }
}
