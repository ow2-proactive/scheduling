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
package org.objectweb.proactive.p2p.core.service;

import java.util.Collection;
import java.util.LinkedList;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.p2p.core.info.Info;
import org.objectweb.proactive.p2p.core.load.Load;


/**
 * Interface to specify a ProActive Peer-to-Peer Service.
 *
 * @author Alexandre di Costanzo
 *
 */
public interface P2PService extends Load {

    /**
     * <code>TTL</code>: Time To Live to get ProActive JVMs.
     */
    public static final int TTL = 10;

    /**
     * <code>P2P_NODE_NAME</code>: name of the node where the P2P Service is
     * running.
     */
    public static final String P2P_NODE_NAME = "P2PNode";

    /**
     * <code>TTU</code>: Time To Update the known JVM table.
     */
    public static final long TTU = 600000; // TTU in milliseconds. By default 10 minuts

    /**
     * <p>
     * <code>NOA</code>: minimal number of known peers.
     * </p>
     * <p>
     * Only if it's possible, enougth peers in the networks.
     * </p>
     * <p>
     * For a single server use, change it in 0 Friends.
     * </p>
     */
    public static final int NOA = 0;

    public static final int MAX_LOAD = 1;
    
    /**
     * <p>
     * Allows this P2P Service on this VM to register in a list of other P2P
     * Services.
     * </p>
     * <p>
     * Also, register other Services in this one.
     * </p>
     *
     * @param servers
     *            List of P2P Services.
     */
    public abstract void registerP2PServices(Collection servers);

    /**
     * Allow in this current P2P Service a remote P2P Service with its url.
     *
     * @param url
     *            like //hostname.domain.com
     */
    public abstract void registerP2PService(String url);

    /**
     * <p>
     * Allows this P2P Service on this VM to register in the remote P2P Service.
     * </p>
     * <p>
     * Also, register the other Service in this one.
     * </p>
     *
     * @param serviceName
     * @param service
     *            A remote P2P Service.
     * @param serviceLoad
     * @param remoteRecord
     *            <code>false</code> for don't register the local Service in
     *            the remote Service.
     */
    /*
    public abstract void registerP2PService(String serviceName,
        P2PService service, int serviceLoad, boolean remoteRecord);
        */

    /**
     * Unregister a remote P2P Service form this local Service.
     *
     * @param url
     *            like //hostname.domain.com
     */
    public void unregisterP2PService(String url);

    /**
     * Unregister a remote P2P Service form this local Service.
     *
     * @param service
     *            the remote P2P Service.
     */
    public void unregisterP2PService(P2PService service);

    /**
     * <p>
     * Returns n ProActive JVMs registered from this P2P Service. This method is
     * recursive on the redorded P2P Services.
     * </p>
     * <p>
     * This method returns n >= ProActive JVMs (this one is exclude).
     * </p>
     *
     * @param n
     *            number of ProActive JVMs asked.
     * @param TTL
     *            Maxi hop.
     * @return n >= ProActive JVMs.
     * @throws ProActiveException
     *             if a problem occurs due to the remote nature of this
     *             ProActiveRuntime.
     */
    public abstract ProActiveRuntime[] getProActiveJVMs(int n, int TTL)
        throws ProActiveException;

    /**
     * For internal use only.
     *
     * @param n
     * @param TTL
     * @param parent
     * @return @throws
     *         ProActiveException
     */
    public ProActiveRuntime[] getProActiveJVMs(int n, int TTL, LinkedList parent)
        throws ProActiveException;

    /**
     * <p>
     * Wiht default value of TTL at 10.
     * </p>
     *
     * @see org.objectweb.proactive.p2p.core.service.P2PService#getProActiveJVMs(int,
     *      int)
     * @param n
     *            number of ProActive JVMs asked.
     * @return n >= ProActive JVMs.
     * @throws ProActiveException
     *             if a problem occurs due to the remote nature of this
     *             ProActiveRuntime.
     */
    public abstract ProActiveRuntime[] getProActiveJVMs(int n)
        throws ProActiveException;

    /**
     * @return All JVMS who are registered in this Service.
     * @throws ProActiveException
     *             if a problem occurs due to the remote nature of this
     *             ProActiveRuntime.
     */
    public abstract ProActiveRuntime[] getProActiveJVMs()
        throws ProActiveException;
    
    /**
     * Ask to this peer to create a node. It's no garantee to create a node
     * @param name the name of the node.
     * @param vnName name of the Virtual Node.
     * @param jobId your jobId.
     * @return a new node or whatever.
     */
    public abstract Node getComputationalNode(String name, String vnName, String jobId);
    
    /**
     * Ask to kill the specified node in this peer.
     * @param name the name of the node.
     */
    public abstract void killComputationalNode(String name);

    /**
     * Get the name of this P2P Service.
     *
     * @return the name of this P2P Service.
     */
    public abstract String getServiceName();

    /**
     * Get the ProActive Runtime include in this P2P Service.
     *
     * @return
     */
    public abstract Object getProActiveRuntime();

    /**
     * @return the acquisition method of its ProActive Runtime.
     */
    public abstract String getProtocol();

    /**
     * @return the URL of this service
     */
    public abstract String getURL();
    
    
    // MODIFICATION ...............................
    public abstract Info getInfo();
    
    public abstract void registerP2PService(String name, Info distInfo, boolean remoteRecord);
    
    public void printKnownPeer();
       
    
}
