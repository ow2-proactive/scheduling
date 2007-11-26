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

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMInitialState;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;


/**
 * An interface Front-End for the Monitoring to communicate with
 * the Infrastructure Manager
 */
public interface IMMonitoring {
    // pour tester
    public StringWrapper echo();

    /**
     * This method serve to know all the deployed proactive descriptor,
     * and get the virtual nodes for redeploying or killing nodes.<BR/>
     * For getting the virtual nodes of a pad call this method :<BR/>
     * VirtualNode[] vnodes = pad.getVirtualNodes();
     * @return hashmap < String padName, ProActiveDescriptor pad >
     */
    public HashMap<String, ProActiveDescriptor> getListDescriptor();

    /**
     * This method serve to get all deployed virtualnodes by proactive descriptor.
     * @return hashmap < String padName, ArrayList<VirtualNode> list of deployed virtualnodes >
     */
    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad();

    /**
     * @return the ArrayList of IMNode know by the IM, it's a set of
     *  free IMNodes and busy IMNodes.
     *  @see org.objectweb.proactive.infrastructuremanager.dataresource.IMNode
     */
    public ArrayList<IMNode> getListAllIMNodes();

    /**
     * @return the ArrayList of free Node
     * @see org.objectweb.proactive.infrastructuremanager.dataresource.IMNode
     */
    public ArrayList<IMNode> getListFreeIMNode();

    /**
     * @return the ArrayList of used Node
     * @see org.objectweb.proactive.infrastructuremanager.dataresource.IMNode
     */
    public ArrayList<IMNode> getListBusyIMNode();

    /**
     * @return the number of free resource IMNode
     */
    public IntWrapper getNumberOfFreeResource();

    /**
     * @return the number of used resource IMNode
     */
    public IntWrapper getNumberOfBusyResource();

    /**
     * @return the number of down resource IMNode
     */
    public IntWrapper getNumberOfDownResource();

    /**
     * @return the number of all resource IMNode
     */
    public IntWrapper getNumberOfAllResources();

    public IMInitialState addIMEventListener(IMEventListener listener,
        IMEvent... events);
}
