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
package org.objectweb.proactive.core.node;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.TechnicalServicesProperties;


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
 * We expect several concrete implementations of the Node to be wrtten such as a RMI node, a HTTP node ...
 * </p>
 *
 * @author  ProActive Team
 * @version 1.1,  2002/08/28
 * @since   ProActive 0.9
 *
 */
@PublicAPI
public interface Node {

    /**
     * Returns the node information as one object. This method allows to
     * retrieve all node information in one call to optimize performance.
     * @return the node information as one object
     */
    public NodeInformation getNodeInformation();

    /**
     * Returns the information about the <code>ProActiveRuntime</code> where the node has been created
     * @return the runtime information as one object
     */
    public VMInformation getVMInformation();

    /**
     * Returns a reference to the <code>ProActiveRuntime</code> where the node has been created
     * @return ProActiveRuntime. The reference to the <code>ProActiveRuntime</code> where the node has been created
     */
    public ProActiveRuntime getProActiveRuntime();

    /**
     * Returns all activeObjects deployed on this Node
     * @return Object[] contains all activeObjects deployed on this Node
     */
    public Object[] getActiveObjects() throws NodeException, ActiveObjectCreationException;

    /**
     * Returns all activeObjects with the given name deployed on this Node
     * or null if such objects do not exist
     * @param className the class of the Active Objects
     * @return Object[].The set of activeObjects deployed on this node of the given class
     */
    public Object[] getActiveObjects(String className) throws NodeException, ActiveObjectCreationException;

    /**
     * @return The number of active objects deployed in this Node.
     * @throws NodeException Cannot get Active Objects registered on this node.
     */
    public int getNumberOfActiveObjects() throws NodeException;

    /**
     * Terminate body of all node's active objects.
     * @throws NodeException
     * @throws IOException
     */
    public void killAllActiveObjects() throws NodeException, IOException;

    /**
     * Put the specified key value in this node property list.
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to key.
     * @return the previous value of the specified key in this property list,
     * or <code>null</code> if it did not have one.
     * @throws ProActiveException
     */
    public Object setProperty(String key, String value) throws ProActiveException;

    /**
     * Searches for the property with the specified key in this node property
     * list.
     * The method returns <code>null</code> if the property is not found.
     * @param key the hashtable key.
     * @return the value in this property list with the specified key value.
     * @throws ProActiveException
     */
    public String getProperty(String key) throws ProActiveException;

    /**
     * 
     * @param ts
     */
    public void setTechnicalServices(TechnicalServicesProperties ts);
}
