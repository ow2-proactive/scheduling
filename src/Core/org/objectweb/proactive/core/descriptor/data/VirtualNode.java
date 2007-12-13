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
package org.objectweb.proactive.core.descriptor.data;

import java.io.Serializable;

import org.objectweb.proactive.Job;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.LocalNode;


/**
 * A <code>VirtualNode</code> is a conceptual entity that represents one or several nodes. After activation
 * a <code>VirtualNode</code> represents one or several nodes.
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see ProActiveDescriptor
 * @see VirtualMachine
 */
@PublicAPI
public interface VirtualNode extends Job, Serializable {

    /**
     * Name of the default Virtual Node.
     *
     * A Node belongs to the default Virtual Node until
     * setVirtualNodeName is called on it.
     *
     * @See {@link LocalNode}
     */
    static final public String DEFAULT_VN = "DEFAULT_VN";

    /**
     * This method can be used to access to the internal view of this
     * VirtualNode.
     *
     * If you are not a ProActive developper you shouldn't use this
     * method. VirtualNodeInternal is not a public interface
     * and is not supported.
     *
     * @return the internal view of this VirtualNode
     */
    public VirtualNodeInternal getVirtualNodeInternal();

    /**
     * Activates all the Nodes mapped to this VirtualNode in the XML Descriptor
     */
    public void activate();

    /**
     * Returns the name of this VirtualNode
     * @return String
     */
    public String getName();

    /**
     * Returns the first Node created among Nodes mapped to this VirtualNode in the XML Descriptor
     * Another call to this method will return the following created node if any. Note that the order
     * in which Nodes are created has nothing to do with the order defined in the XML descriptor.
     * @return Node
     */
    public Node getNode() throws NodeException;

    /**
     * Returns the Node mapped to this VirtualNode with the specified index. There is no relationship between,
     * the order in the xml descriptor and the order in the array.
     * @param index
     * @return Node the node at the specified index in the array of nodes mapped to this VirtualNode
     * @deprecated use {@link #getNode()} or {@link #getNodes()} instead
     */
    @Deprecated
    public Node getNode(int index) throws NodeException;

    /**
     * Returns all nodes url mapped to this VirualNode
     * @return String[]. An array of string containing the url of all nodes mapped to
     * this VirtualNode in the XML descriptor.
     */
    public String[] getNodesURL() throws NodeException;

    /**
     * Returns all nodes mapped to this VirtualNode
     * @return Node[] An array of Node conataining all the nodes mapped to this
     * VirtualNode in the XML descriptor
     */
    public Node[] getNodes() throws NodeException;

    /**
     * Returns the node of the given url among nodes mapped to this VirtualNode in the xml
     * descriptor or null if such node does not exist.
     * @param url
     * @return Node the node of the given url or null if such node does not exist
     */
    public Node getNode(String url) throws NodeException;

    /**
     * Returns the unique active object created on the unique node mapped to this VirtualNode.
     * This method should be called on a virtualNode, with unique_singleAO property defined in the XML descriptor. If more than one active object are found, a
     * warning is generated, and the first active object found is returned
     * @return Object the unique active object created on the unique node mapped to this VirtualNode. If many active objects are found, the first one is returned
     * @throws ProActiveException if no active objects are created on this VirtualNode.
     */
    public Object getUniqueAO() throws ProActiveException;

    /**
     * Returns true is this VirtualNode is already activated, false otherwise
     */
    public boolean isActivated();

    /**
     * Kills all nodes mapped to this VirtualNode.
     * It is in fact the runtime(so the jvm) on which the node is running that is killed.
     * Nodes are previously unregistered from any registry.
     * @param softly if false, all jvms created when activating this VirtualNode are killed abruptely
     * if true a jvm that originates the creation of  a rmi registry waits until registry is empty before
     * dying. To be more precise a thraed is created to ask periodically the registry if objects are still
     * registered.
     */
    public void killAll(boolean softly);

    /**
     * Returns the number of Nodes mapped to this VirtualNode in the XML Descriptor
     * @return int number of mapped nodes, {@link org.objectweb.proactive.core.process.UniversalProcess#UNKNOWN_NODE_NUMBER} if
     * the number of mapped nodes cannot be determined (when this method is invoked).
     * This method returns the exact number of mapped nodes once the virtual node has been activated.
     */
    public int getNbMappedNodes();

    /**
     * Returns the minimum number of nodes needed for this VirtualNode.
     * This number represents the minimum number of nodes,  this VirtualNode needs in order to be
     * suitable for the application. Default value is the total number of nodes requested in the
     * XML file
     */
    public int getMinNumberOfNodes();

    /**
     * Returns the number of Nodes already created (at the time of the reception of method call) among the Nodes mapped to this VirtualNode
     * in the XML Descriptor
     * @return int the number of nodes created at  the time of the method call
     */
    public int getNumberOfCurrentlyCreatedNodes();

    /**
     * Returns the number of Nodes that could successfully be created at the end of the deployment
     *  @return int the number of nodes effectively created
     */
    public int getNumberOfCreatedNodesAfterDeployment();
}
