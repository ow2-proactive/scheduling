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
package org.objectweb.proactive.extra.infrastructuremanager.dataresource;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript;


/**
 * The {@link IMDataResource} class will handle the resource queries :
 * return nodes that verify the given script, get back the nodes.
 * It's a kind of resource managment policy : you can write your own policy provided the nodes.
 * @author proactive team
 *
 */
public interface IMDataResource {
    //----------------------------------------------------------------------//
    // INIT
    /**
     * Initialize the {@link IMDataResource}.
     */
    public void init();

    //----------------------------------------------------------------------//
    // FREE
    /**
     * The scheduler or other application that use the Infrastructure Manager
     * return the node to free.
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

    //----------------------------------------------------------------------//
    // GET NODE 
    /**
     * Return a maximum of nb Nodes in a {@link NodeSet},
     * that verify the {@link VerifyingScript} if given.
     * If no node is available, an empty NodeSet is returned.
     */
    public NodeSet getAtMostNodes(IntWrapper nb, VerifyingScript verifyingScript);

    /**
     * Return nb Nodes in a {@link NodeSet},
     * that verify the {@link VerifyingScript} if given.
     * If no node is available, or if there is not enough nodes,
     * an empty NodeSet is returned.
     */
    public NodeSet getExactlyNodes(IntWrapper nb,
        VerifyingScript verifyingScript);

    /**
     * Notify that the given {@link IMNode} is down.
     * @param imNode
     */
    public void nodeIsDown(IMNode imNode);
}
