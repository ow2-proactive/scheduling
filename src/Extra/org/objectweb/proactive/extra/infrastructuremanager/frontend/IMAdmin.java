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
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMInitialState;


/**
 * An interface Front-End for the Admin to communicate with
 * the Infrastructure Manager
 */
public interface IMAdmin extends Serializable {
    public StringWrapper echo();

    //Germs old functions kept for the moment, must disappear
    public void createP2PNodeSource(String id, int nbMaxNodes, int nice,
        int ttr, Vector<String> peerUrls);

    public void createDummyNodeSource(String id, int nbMaxNodes, int nice,
        int ttr);

    /**
     * Create a static Node source and deploy nodes specified in the PAD
     */
    public void createStaticNodesource(String sourceName,
        ProActiveDescriptor pad);

    /**
     * add nodes to the default StaticNode source of the scheduler
     * @param pad
     * @param PADName
     */
    public void addNodes(ProActiveDescriptor pad);

    /**
     * add nodes to a StaticNodeSource represented by sourceName
     * this sourceName must exist and must be a static source
     * @param pad
     * @param PADName
     */
    public void addNodes(ProActiveDescriptor pad, String sourceName);

    /**
     * Remove a node from the node InfrastructureManager
     * @param nodeUrl
     * @param killNode
     */
    public void removeNode(String nodeUrl, boolean killNode);

    /**
     *
     * @param sourceName
     * @param killNodes
     */
    public void removeSource(String sourceName, boolean killNodes);

    //---------------------------------------------------------------------//
    // old functions what to do with this ?
    //---------------------------------------------------------------------//

    /**
     * This method serve to get all deployed virtualnodes by proactive
     * descriptor.<BR/>
     * It's used before calling methods <I>kill</I> or <I>redeploy</I> for
     * getting the name of the pad and the names of deployed virtualnodes.
     * @return hashmap < String padName, ArrayList<VirtualNode> list of deployed virtualnodes >
     */
    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad();

    //----------------------------------------------------------------------//
    // SHUTDOWN

    /**
     * Kill all ProActiveDescriptor and Infrastructure Manager
     * @exception ProActiveException
     */
    public void shutdown() throws ProActiveException;

    /**
     *
     * @param listener
     * @param events
     * @return
     */
    public IMInitialState addIMEventListener(IMEventListener listener,
        IMEvent... events);
}
