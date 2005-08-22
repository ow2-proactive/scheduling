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
package org.objectweb.proactive.core.node;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;


/**
 * <p>
 * A <code>Node</code> offers a set of services needed by ProActive to work with
 * remote JVM. Each JVM that is aimed to hold active objects should contains at least
 * one instance of the node class. That instance, when created, will be registered
 * to some registry where it is possible to perform a lookup (such as the RMI registry).
 * </p><p>
 * When ProActive needs to interact with a remote JVM, it will lookup for one node associated
 * with that JVM (using typically the RMI Registry) and use this node to perform the interaction.
 * </p><p>
 * We expect several concrete implementations of the Node to be wrtten such as a RMI node, a JINI node ...
 * </p>
 *
 * @author  ProActive Team
 * @version 1.1,  2002/08/28
 * @since   ProActive 0.9
 *
 */
public interface Node {

    /**
     * Returns the node information as one object. This method allows to
     * retrieve all node information in one call to optimize performance.
     * @return the node information as one object
     */
    public NodeInformation getNodeInformation();

    /**
     * Returns a reference to the <code>ProActiveRuntime</code> where the node has been created
     * @return ProActiveRuntime. The reference to the <code>ProActiveRuntime</code> where the node has been created
     */
    public ProActiveRuntime getProActiveRuntime();

    /**
     * Returns all activeObjects deployed on this Node
     * @return Object[] contains all activeObjects deployed on this Node
     */
    public Object[] getActiveObjects()
        throws NodeException, ActiveObjectCreationException;

    /**
     * Returns all activeObjects with the given name deployed on this Node
     * or null if such objects do not exist
     * @param className the class of the Active Objects
     * @return Object[].The set of activeObjects deployed on this node of the given class
     */
    public Object[] getActiveObjects(String className)
        throws NodeException, ActiveObjectCreationException;

    /**
     * @return The number of active objects deployed in this Node.
     * @throws NodeException Cannot get Active Objects registered on this node.
     */
    public int getNumberOfActiveObjects() throws NodeException;

    /**
     * Returns the name of the VirtualNode this Node belongs to
     * @return the name of the VirtualNode this Node belongs to
     */
    public String getVnName();

    /**
     * Sets the name of the VirtualNode this Node belongs to
     * @param virtualNodeName
     */
    public void setVnName(String virtualNodeName);

    /**
     * Terminate body of all node's active objects.
     * @throws NodeException
     * @throws IOException
     */
    public void killAllActiveObjects() throws NodeException, IOException;
}
