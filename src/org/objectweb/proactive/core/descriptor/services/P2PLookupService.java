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
package org.objectweb.proactive.core.descriptor.services;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.p2p.core.service.P2PService;
import org.objectweb.proactive.p2p.core.service.StartP2PService;

import java.util.Vector;


/**
 * This class represents a service to acquire ProActiveRuntime(JVMs) with the ProActive P2P infrastructure
 * This service can be defined and used transparently when using XML Deployment descriptor
 * @author  ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class P2PLookupService implements UniversalService {
    protected static String serviceName = "P2PLookup";
    protected int MAX = 10000;
    protected int nodeNumber = 0;
    protected int nodeCount;
    protected long timeout = 70000;
    protected int lookupFrequence = 900000;
    protected int TTL = P2PService.TTL;
    protected P2PService serviceP2P;
    protected Vector peersList;

    public P2PLookupService() {
        peersList = new Vector();
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#startService()
     */
    public ProActiveRuntime[] startService() throws ProActiveException {
        if (serviceP2P == null) {
            nodeCount = nodeNumber;
            String protocol = System.getProperty(
                    "proactive.communication.protocol");
            String port = System.getProperty("proactive.rmi.port");
            StartP2PService startServiceP2P = new StartP2PService(protocol,
                    port, peersList);
            startServiceP2P.start();
            this.serviceP2P = startServiceP2P.getP2PService();
        }
        ProActiveRuntime[] part = serviceP2P.getProActiveJVMs(nodeNumber, TTL);
        nodeCount = nodeCount - part.length;
        return part;
    }

    /**
     * @return Returns the nodeNumber.
     */
    public int getNodeNumber() {
        return nodeNumber;
    }

    /**
     * Sets the number of nodes to be acquired with this P2P service
     * @param nodeNumber The nodeNumber to set.
     */
    public void setNodeNumber(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     * Sets the number of nodes to be acquired to 10000(Max Value)
     * This method is usefull to acquire an undefined number of nodes. Indeed the number
     * of nodes expected is never reached, the operation will be done again after lookupFrequence ms
     * and if the timout has not expired
     */
    public void setNodeNumberToMAX() {
        this.nodeNumber = MAX;
    }

    /**
     * @return Returns the timeout.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * @return Returns the lookupFrequence.
     */
    public int getLookupFrequence() {
        return lookupFrequence;
    }

    /**
     * Sets the period of lookup. It means that every lookupFrequence(in ms) a P2P lookup will occur
     * @param lookupFrequence The lookupFrequence to set.
     */
    public void setLookupFrequence(int lookupFrequence) {
        this.lookupFrequence = lookupFrequence;
    }

    /**
     * @return Returns the TTL.
     */
    public int getTTL() {
        return TTL;
    }

    /**
     * @param ttl The TTL to set.
     */
    public void setTTL(int ttl) {
        TTL = ttl;
    }

    public void setPeerList(String[] peerList) {
        for (int i = 0; i < peerList.length; i++) {
            peersList.add(peerList[i]);
        }
    }

    /**
     * @return the value of MAX attributes
     */
    public int getMAX() {
        return MAX;
    }

    /**
     * Sets the Maximum number of nodes. Once this max number is reached the service provide access
     * to acuired ProActiveRuntimes
     * @param max
     */
    public void setMAX(int max) {
        MAX = max;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#getServiceName()
     */
    public String getServiceName() {
        return serviceName;
    }
}
