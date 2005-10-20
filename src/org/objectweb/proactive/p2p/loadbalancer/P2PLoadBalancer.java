/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html
 *
 * Contributor(s):                                 Javier Bustos
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.loadbalancer;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.loadbalancing.Balanceable;
import org.objectweb.proactive.loadbalancing.CPURanking;
import org.objectweb.proactive.loadbalancing.LinuxCPURanking;
import org.objectweb.proactive.loadbalancing.LoadBalancer;
import org.objectweb.proactive.loadbalancing.LoadBalancingConstants;
import org.objectweb.proactive.loadbalancing.LoadMonitorLinux;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


public class P2PLoadBalancer extends LoadBalancer {
    private P2PService p2pSer;
    private String myNodeAddress;

    public P2PLoadBalancer(P2PService p2pSer) {
        this.p2pSer = p2pSer;
        underloaded = false;
        // by now we use only P2P over Linux
        lm = new LoadMonitorLinux(this);
        new Thread(lm).start();

        /* And we update the ranking */
        CPURanking thisCPURanking = new LinuxCPURanking();
        ranking = thisCPURanking.getRanking();
        String address = ProActive.getActiveObjectNodeUrl(p2pSer);
        address = address.substring(0, address.indexOf('/', 3));
        myNodeAddress = address + "/" + P2PConstants.SHARED_NODE_NAME + "_0";
    }

    /**
     * This method use the P2P infrastructure to search nodes which can
     * receive its active objects.
     */
    public void startBalancing() {
        p2pSer.tellToMyNeighborsThatIWantToShareActiveObjects(ranking);
    }

    /**
     * This method use the P2P infrastructure to search nodes which I
     * can steal work.
     */
    public void stealWork() {
        p2pSer.startStealingNeighbors(ranking, myNodeAddress);
    }

    /**
     * This method is to know if this machine is underloaded and "better" than
     * who want to send active objects
     * receive its active objects.
     * @param remoteRanking ranking of the overloaded machine
     * @return <code>true</code> if this machine is underloaded in relation to the overloaded one.
     * Else <code>false</code>.
     */
    public boolean AreYouUnderloaded(double remoteRanking) {
        if (myLoad >= LoadBalancingConstants.OVERLOADED_THREASHOLD) {
            return false;
        }

        return myLoad < (LoadBalancingConstants.UNDERLOADED_THREASHOLD * (ranking / remoteRanking));
    }

    public void ImStealingYou(double remoteRanking, String remoteNodeAddress) {
        // Is the remote better than me (really better)?
        if (remoteRanking < (ranking * LoadBalancingConstants.STEAL_PONDERATION)) {
            return;
        }

        // have I somebody to send?
        BodyMap knownBodies = LocalBodyStore.getInstance().getLocalBodies();
        Body candidateBody = null;

        if (knownBodies.size() < 1) {
            return;
        }

        java.util.Iterator bodiesIterator = knownBodies.bodiesIterator();

        /** ******** Choosing a candidate ******** */
        while (bodiesIterator.hasNext()) {
            Body activeObjectBody = (Body) bodiesIterator.next();
            Object testObject = activeObjectBody.getReifiedObject();

            /********** Only some Active Objects can migrate *************/
            boolean testSerialization = testObject instanceof Balanceable;

            if (activeObjectBody.isAlive()) {
                if (activeObjectBody.isActive() && testSerialization) {
                    candidateBody = activeObjectBody;
                    break;
                }
            }
        }

        /******* somebody? *********/
        if (candidateBody == null) {
            return;
        }

        if (candidateBody.isActive()) {
            logger.debug("[Loadbalancer] Migrating from " +
                candidateBody.getNodeURL() + " to " + remoteNodeAddress);

            try {
                ProActive.migrateTo(candidateBody, remoteNodeAddress, true);
            } catch (MigrationException e) {

                /** ****** if you cannot migrate, is not my business ********** */
            }
        }
    }
}
