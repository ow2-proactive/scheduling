/*
 * Created on 23 juil. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.descriptor.services;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.p2p.core.service.P2PService;
import org.objectweb.proactive.p2p.core.service.StartP2PService;

import java.util.Vector;


/**
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class P2PLookupService implements UniversalService {
    protected static String serviceName = "P2PLookup";
    protected int MAX = 10000;
    protected int nodeNumber = 0;
    protected int nodeCount;

    //protected int minNodeNumber;
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
     * @param nodeNumber The nodeNumber to set.
     */
    public void setNodeNumber(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public void setNodeNumberToMAX() {
        this.nodeNumber = MAX;
    }

    /**
     * @return Returns the minNodeNumber.
     */

    //    public int getMinNodeNumber() {
    //        return minNodeNumber;
    //    }

    /**
     * @param minNodeNumber The minNodeNumber to set.
     */

    //    public void setMinNodeNumber(int minNodeNumber) {
    //        this.minNodeNumber = minNodeNumber;
    //    }

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
     * Sets the Maximum number of nodes
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
