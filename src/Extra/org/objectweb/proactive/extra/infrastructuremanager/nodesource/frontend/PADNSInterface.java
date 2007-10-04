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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.PADNodeSource;


public interface PADNSInterface extends NodeSourceInterface {
    // DEPLOY	

    /**
     * Deploy all vnode of the proactive descriptor giving by the paramter
     *         <I>xmlDescriptor</I>.<BR/>
     * This function need a node in your local machine for deploying the file.
     * You can create the node with this instruction :<BR/>
     * <code>Node node = NodeFactory.createNode("nodeName");</code>
     * @param xmlDescriptor : the file proactive descriptor
     * @param remoteNode    : the node in your local host for transfering
     *         the file descriptor
     * @exception Exception : Cannot pull the remote file
     */
    public void deployAllVirtualNodes(File xmlDescriptor, Node remoteNode)
        throws Exception;

    /**
     * Deploy the virtual node <I>vnName</I> of the proactive descriptor giving by the paramter
     *         <I>xmlDescriptor</I>.<BR/>
     * This function need a node in your local machine for deploying the file.
     * You can create the node with this instruction :<BR/>
     * <code>Node node = NodeFactory.createNode("nodeName");</code>
     * @param xmlDescriptor : the file proactive descriptor
     * @param remoteNode    : the node in your local host for transfering
     * @param vnName                 : the name of virtual node that you want to deploy
     *         the file descriptor
     * @exception Exception : Cannot pull the remote file
     */
    public void deployVirtualNode(File xmlDescriptor, Node remoteNode,
        String vnName) throws Exception;

    /**
     * Deploy the virtual nodes giving by hte table<I>vnNames</I> of the proactive
     * descriptor giving by the paramter <I>xmlDescriptor</I>.<BR/>
     * This function need a node in your local machine for deploying the file.
     * You can create the node with this instruction :<BR/>
     * <code>Node node = NodeFactory.createNode("nodeName");</code>
     * @param xmlDescriptor : the file proactive descriptor
     * @param remoteNode    : the node in your local host for transfering
     * @param vnNames                 : a table of the name of virtual nodes that you want to deploy
     *         the file descriptor
     * @exception Exception : Cannot pull the remote file
     */
    public void deployVirtualNodes(File xmlDescriptor, Node remoteNode,
        String[] vnNames) throws Exception;

    /**
    * Add the new deployed node in the dataresource
    * @param node    : the new deployed node
    * @param vnName  : the name of the virtual node
    * @param padName : the name of the proactive descriptor
    */
    public void addNode(Node node, String vnName, String padName);

    /**
     * Add the new proactive descriptor in the dataresource
     * @param padName : the name of the proactive descriptor
     * @param pad     : the proactive descriptor
     */
    public void addPAD(String padName, ProActiveDescriptor pad);

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName);

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor
     * @param vnName  : the name of the virtual node of this pad to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName, String vnName);

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor
     * @param vnNames : the name of the virtual nodes of this pad to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName, String[] vnNames);

    // ----------------------------------------------------------------------//	
    // KILL

    /**
     * Kill all virtual nodes of them proactive descriptors
     * TODO delete the pad file
     * @param padName :  the name of the proactive descriptor
     * @exception ProActiveException
     */
    public void killPAD(String padName) throws ProActiveException;

    /**
    * Kill the virtual nodes of the proactive descriptors <I>padName>/I>
    * @param padName : the name of the Proactive Descriptor
    * @exception ProActiveException
    */
    public void killPAD(String padName, String vnName);

    /**
    * Kill the virtual node <I>vnName</I> of the proactive descriptor <I>padName</I>
    * @param padName : the name of the Proactive Descriptor
    * @param vnName  : the name of the virtual node for killing
    * @see  killPAD(String padName)
    * @exception ProActiveException
    */
    public void killPAD(String padName, String[] vnNames);

    /**
    * Kill the virtual nodes <I>vnNames</I>
    * of the proactive descriptor <I>padName</I>
    * @param padName : the name of the Proactive Descriptor
    * @param vnNames : the name of the virtual nodes for killing
    * @see  killPAD(String padName)
    * @exception ProActiveException
    */
    public void killAll() throws ProActiveException;

    /**
     * @return the number of PADs handled by this {@link PADNodeSource}
     */
    public IntWrapper getSizeListPad();

    /**
     * @return the list of PADs handled by this {@link PADNodeSource}
     */
    public HashMap<String, ProActiveDescriptor> getListPAD();

    /**
     * @return The virtual nodes handled, ordered by PAD.
     */
    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad();
}
