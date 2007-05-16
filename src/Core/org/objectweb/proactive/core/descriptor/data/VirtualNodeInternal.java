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
package org.objectweb.proactive.core.descriptor.data;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Job;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.filetransfer.FileVector;


/**
 * <p> <code>VirtualNodeInternal</code> is the internal interface
 * to be used by ProActive developper to manipulate a VirtualNode.
 * Methods provided by this interface allow the initialization and modification
 * of this <code>VirtualNode</code></p>
 *
 * <p>This interface is not public and not supported.</p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see ProActiveDescriptor
 * @see VirtualMachine
 */
@PublicAPI
public interface VirtualNodeInternal extends VirtualNode {
    public final static Logger vnLogger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);

    /**
     * Returns the value of property attribute.
     * @return String
     */
    public String getProperty();

    /**
     * Adds a VirtualMachine entity to this VirtualNode
     * @param virtualMachine
     */
    public void addVirtualMachine(VirtualMachine virtualMachine);

    /**
     * Returns the virtualMachine entity linked to this VirtualNode or if cyclic, returns
     * one of the VirtualMachines linked to this VirtualNode with a cyclic manner(internal count incremented each time this method is called).
     * @return VirtualMachine
     */
    public VirtualMachine getVirtualMachine();

    /**
     * Returns the timeout of this VirtualNode
     * @return the timeout of this VirtualNode
     */
    public long getTimeout();

    /**
     * @deprecated use {@link #getNumberOfCurrentlyCreatedNodes()} or {@link #getNumberOfCreatedNodesAfterDeployment()} instead
     */
    @Deprecated
    public int createdNodeCount();

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

    /**
     * Creates a node with the given protocol(or null) on the current jvm, ie the jvm that originates the creation of this VirtualNode.
     * This newly created node will is mapped on this VirtualNode
     * @param protocol the protocol to create the node. If null protocol will be set to the system property: proactive.communication.protocol.
     */
    public void createNodeOnCurrentJvm(String protocol);

    /**
     * @return true if this VirtualNode is a VirtualNodeLookup, false if it is a VirtualNodeImpl
     */
    public boolean isLookup();

    /**
     * Allows to set runtime informations for this VirtualNode activation.
     * This method allows to give to this VirtualNode some informations retrieved at runtime and
     * not defined in the XML descriptor.
     * In the current release, this method can be called on a VirtualNode resulting from a lookup. The only
     * one information that can be set is LOOKUP_HOST. This information has a sense if in the XML descriptor
     * this VirtualNode is defined with the line:
     * <pre>
     * lookup virtualNode="vnName" host="*" protocol="rmi or jini", ie the name of the host where to perform the lookup
     * will be known at runtime.
     * </pre>
     * We expect to implement several runtime informations.
     * If this method fails, for instance, if the property does not exist or has already been set, or is performed on a VirtualNode not resulting
     * from a lookup, an exception will be thrown but the application will carry on.
     * @param information the information to be set at runtime
     * @param value the value of the information
     * @throws ProActiveException if the given information does not exist or has alredy been set
     */
    public void setRuntimeInformations(String information, String value)
        throws ProActiveException;

    /**
     * Returns the minimum number of nodes needed for this VirtualNode.
     * This number represents the minimum number of nodes,  this VirtualNode needs in order to be
     * suitable for the application. Default value is the total number of nodes requested in the
     * XML file
     */
    public int getMinNumberOfNodes();

    /**
     * checks the cardinality of the virtual node (i.e. whether the node is mapped to several nodes or not)
     * @return true if the virtual node is mapped to several nodes, false otherwise
     */
    public boolean isMultiple();

    /**
     * This methods triggers the remote retrieval of files, as specified
     * in the instantiated descriptor file for this VirtualNode.
     * To achieve this, the file transfer push and pull API is used on
     * all the nodes created from this VirtualNode.
     *
     * @return An array of FileWrapper (Futures) with the retrieved files.
     */
    public FileVector fileTransferRetrieve()
        throws ProActiveException, IOException;

    /**
     *
     * @return mpi process attached with the virtual node - null otherwise.
     */
    public ExternalProcess getMPIProcess();

    /**
     *
     * @return true if it exists an MPI process with this VN - false otherwise.
     */
    public boolean hasMPIProcess();

    /**
     * Set Filetransfer API parameters to use when Deploying and Retrieving files.
     * @param fileBlockSize The file will be splitted into blocks and transfered. This paramter
     * sets the size of the blocks in MB. The default value is 1MB. Increase this value to improove performance, decrease the value if the file transfer uses to much memory.
     * @param overlapping Says home many blocks to send in burst mode. Increase this value to improove performance,
     * decrase it to save memmory. The default value is 8.
     */
    public void setFileTransferParams(int fileBlockSize, int overlapping);
}
