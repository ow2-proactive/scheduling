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

import org.objectweb.proactive.loadbalancing.*;
import org.objectweb.proactive.p2p.service.P2PService;


public class P2PLoadBalancer extends LoadBalancer {
    private P2PService p2pSer;

    public P2PLoadBalancer(P2PService p2pSer) {
        this.p2pSer = p2pSer;
        underloaded = false;
        // by now we use only P2P over Linux
        lm = new LoadMonitorLinux(this);
        new Thread(lm).start();

        /* And we update the ranking */
        CPURanking thisCPURanking = new LinuxCPURanking();
        ranking = thisCPURanking.getRanking();
    }

    /**
     * This method use the P2P infrastructure to search nodes which can
     * receive its active objects.
     * @param none
     * @return none
     */
    public void startBalancing() {
        p2pSer.tellToMyNeighborsThatIWantToShareActiveObjects(ranking);
    }

    /**
     * This method is to know if this machine is underloaded and "better" than
     * who want to send active objects
     * receive its active objects.
     * @param <code>remoteRanking</code>: ranking of the overloaded machine
     * @return <code>true</code> if this machine is underloaded in relation to the overloaded one.
     * Else <code>false</code>.
     */
    public boolean AreYouUnderloaded(double remoteRanking) {
        if (myLoad >= LoadBalancingConstants.OVERLOADED_THREASHOLD) {
            return false;
        }

        return myLoad < (LoadBalancingConstants.UNDERLOADED_THREASHOLD * (ranking / remoteRanking));
    }
}
