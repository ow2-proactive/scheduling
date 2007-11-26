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
package org.objectweb.proactive.extra.infrastructuremanager.core;

import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMInitialState;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMDataResource;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


/**
 * The {@link IMDataResource} class will handle the resource queries :
 * return nodes that verify the given script, get back the nodes.
 * It's a kind of resource managment policy : you can write your own policy provided the nodes.
 * @author proactive team
 *
 */
public interface IMCoreInterface {
    public String getId();

    //Germs old methods to garbage
    public void createP2PNodeSource(String id, int nbMaxNodes, int nice,
        int ttr, Vector<String> peerUrls);

    public void createDummyNodeSource(String id, int nbMaxNodes, int nice,
        int ttr);

    public BooleanWrapper shutdown();

    public void createStaticNodesource(ProActiveDescriptor pad,
        String sourceName);

    public void addNodes(ProActiveDescriptor pad, String sourceName);

    public void addNodes(ProActiveDescriptor pad);

    public void removeNode(String nodeUrl, boolean killNode);

    public void removeSource(String ns, boolean killNodes);

    public ArrayList<NodeSource> getNodeSources();

    public IntWrapper getSizeListFreeIMNode();

    public IntWrapper getSizeListBusyIMNode();

    public IntWrapper getSizeListDownIMNode();

    public IntWrapper getSizeListToReleaseIMNode();

    public IntWrapper getNbAllIMNode();

    public ArrayList<IMNode> getListFreeIMNode();

    public ArrayList<IMNode> getListBusyIMNode();

    public ArrayList<IMNode> getListAllNodes();

    public ArrayList<IMNode> getListToReleasedIMNodes();

    public IMInitialState getIMInitialState();

    // ----------------------------------------------------------------------//
    // ACCESSORS
    public Node getNodeIM();

    public IMAdmin getAdmin();

    public IMMonitoring getMonitoring();

    public IMUser getUser();

    //----------------------------------------------------------------------//
    //User
    //----------------------------------------------------------------------//

    // GET NODE 
    /**
     * Return a maximum of nb Nodes in a {@link NodeSet},
     * that verify the {@link VerifyingScript} if given.
     * If no node is available, an empty NodeSet is returned.
     */
    public NodeSet getAtMostNodes(IntWrapper nb, SelectionScript selectionScript);

    /**
     * Return nb Nodes in a {@link NodeSet},
     * that verify the {@link VerifyingScript} if given.
     * If no node is available, or if there is not enough nodes,
     * an empty NodeSet is returned.
     */
    public NodeSet getExactlyNodes(IntWrapper nb,
        SelectionScript selectionScript);

    // FREE NODE
    /**
     * The scheduler or other application that use the Infrastructure Manager
     * ask to free a node.
     */
    public void freeNode(Node node);

    /**
     * This method provide a way to free a set of nodes in one call.
     * @param nodes
     */
    public void freeNodes(NodeSet nodes);

    /**
     * You can free nodes giving their {@link VirtualNode}.
     * @param vnode
     */
    @Deprecated
    public void freeNodes(VirtualNode vnode);
}
