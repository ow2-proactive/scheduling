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
package org.objectweb.proactive.p2p.service;

import org.apache.log4j.Logger;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.group.ExceptionInGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.service.util.P2PConstants;

import java.io.Serializable;

import java.util.Iterator;


/**
 * Updating the group of exportAcquaintances of the P2P service.
 *
 * @author Alexandre di Costanzo
 *
 */
public class P2PAcquaintanceManager implements InitActive, RunActive,
    Serializable, P2PConstants {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_ACQUAINTANCES);
    private P2PService localService = null;
    private P2PService acquaintances = null;
    private P2PService acquaintancesActived = null;
    private Group groupOfAcquaintances = null;
    private static final long TTU = Long.parseLong(System.getProperty(
                P2PConstants.PROPERTY_TTU));
    private static final int NOA = Integer.parseInt(System.getProperty(
                P2PConstants.PROPERTY_NOA));
    private static final int TTL = Integer.parseInt(System.getProperty(
                P2PConstants.PROPERTY_TTL));

    /**
     * The empty constructor for activating.
     */
    public P2PAcquaintanceManager() {
        // empty constructor
    }

    /**
     * Construct a new <code>P2PAcquaintanceManager</code>.
     * @param localService a reference to the local P2P service.
     */
    public P2PAcquaintanceManager(P2PService localService) {
        this.localService = localService;
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        String nodeUrl = body.getNodeURL();

        // Create exportAcquaintances group
        try {
            this.acquaintances = (P2PService) ProActiveGroup.newGroup(P2PService.class.getName());
            this.groupOfAcquaintances = ProActiveGroup.getGroup(acquaintances);
            this.acquaintancesActived = (P2PService) ProActiveGroup.turnActiveGroup(acquaintances,
                    nodeUrl);
        } catch (ClassNotReifiableException e) {
            logger.fatal("Couldn't create the group of exportAcquaintances", e);
        } catch (ClassNotFoundException e) {
            logger.fatal("Couldn't create the group of exportAcquaintances", e);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't create the group of exportAcquaintances", e);
        } catch (NodeException e) {
            logger.fatal("Couldn't create the group of exportAcquaintances", e);
        }

        logger.debug("Group of exportAcquaintances succefuly created");
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (body.isActive()) {
            try {
                if (this.groupOfAcquaintances.size() > 0) {
                    // Register the local P2P service in all exportAcquaintances
                    logger.debug("Sending heart-beat");
                    this.acquaintances.heartBeat();
                    logger.debug("Heart-beat sent");

                    // How many peers ?
                    if (this.groupOfAcquaintances.size() < NOA) {
                        // Looking for new peers
                        logger.debug("NOA is " + NOA +
                            " - Size of P2PAcquaintanceManager is " +
                            this.groupOfAcquaintances.size());

                        // Sending exploring message
                        this.acquaintances.exploring(TTL, null,
                            this.localService);
                        logger.debug("Explorating message sent");
                    }
                }

                // Waiting TTU & serving requests
                logger.debug("Waiting for " + TTU + "ms");
                long endTime = System.currentTimeMillis() + TTU;
                service.blockingServeOldest(TTU);
                while (System.currentTimeMillis() < endTime) {
                    try {
                        service.blockingServeOldest(endTime -
                            System.currentTimeMillis());
                    } catch (ProActiveRuntimeException e) {
                        logger.debug("Certainly because the body is not active",
                            e);
                    }
                }
                logger.debug("End waiting");
            } catch (ExceptionListException e) {
                // Removing bad peers
                logger.debug("Some peers to remove from group");
                Iterator it = e.iterator();
                while (it.hasNext()) {
                    // Remove bad peers
                    ExceptionInGroup ex = (ExceptionInGroup) it.next();
                    Object peer = ex.getObject();
                    this.groupOfAcquaintances.remove(peer);
                    logger.debug("Peer removed");
                }
            }
        }
    }

    /**
     * @return An active object to make group method call.
     */
    public P2PService getActiveGroup() {
        return this.acquaintancesActived;
    }

    /**
     * Add a peer in the group of acquaintancesActived.
     * @param peer the peer to add.
     */
    public void add(P2PService peer) {
        if (!this.groupOfAcquaintances.contains(peer)) {
            boolean result = this.groupOfAcquaintances.add(peer);
            String peerUrl = ProActive.getActiveObjectNodeUrl(peer);
            logger.info("Acquaintance " + peerUrl + " " + result + " added");
        }
    }

    public void remove(P2PService peer) {
        boolean result = this.groupOfAcquaintances.remove(peer);
        if (result) {
            logger.debug("Peer successfully removed");
        } else {
            logger.debug("Peer not removed");
        }
    }

    /**
     * Returns the number of elements in this group.
     *
     * @return the number of elements in this group.
     */
    public int size() {
        return this.groupOfAcquaintances.size();
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified
     * element.  More formally, returns <tt>true</tt> if and only if this
     * collection contains at least one element <tt>e</tt> such that
     * <tt>(o==null ? e==null : o.equals(e))</tt>.
     *
     * @param service element whose presence in this collection is to be tested.
     * @return <tt>true</tt> if this collection contains the specified
     *         element.
     */
    public boolean contains(P2PService service) {
        return this.groupOfAcquaintances.contains(service);
    }

	/**
	 * @param n number of neighbors which receive the balance request
	 */
	public void chooseNneighborsAndSendTheBalanceRequest(int n, P2PService sender, double ranking) {
		int size = this.size();
		if (size <= 0) return;
		if (n > size) n = size; 
		
		int candidate = (int) (Math.random()*size);
		int low = (candidate - 1 < 0)? 0:candidate-1;
		int high = (candidate+1 >= size)? size-1:candidate+1;
		Group subGroupofAcquaintances = this.groupOfAcquaintances.range(low,high); 
		
		((P2PService) subGroupofAcquaintances.getGroupByType()).balanceWithMe(sender, ranking);
	}
}
