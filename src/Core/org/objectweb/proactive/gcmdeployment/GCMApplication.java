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
package org.objectweb.proactive.gcmdeployment;

import java.util.List;
import java.util.Map;
import java.net.URL;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractImpl;


/**
 * A GCM Application
 * 
 * A GCM Application is described by a GCM Application Descriptor XML file. This interface offers
 * some services to manipulate a GCM Application:
 * <ul>
 * <li>Application life cycle management: start or stop application deployment</li>
 * <li>Virtual Node abstraction</li>
 * <li>Node {@link Topology} of the application </li>
 * <li>application's statistics/metrics</li>
 * </ul>
 * 
 * @see GCMVirtualNode
 * @see Topology
 */
@PublicAPI
public interface GCMApplication {

    /**
     * Starts the deployment process
     * 
     * Creates remote ProActive Runtimes and Nodes described in all the GCM Deployment Descriptors
     * referenced by the GCM Application Descriptor.
     */
    public void startDeployment();

    /**
     * Terminates all the ProActive Runtimes started by this Application
     */
    public void kill();

    /**
     * Indicates if the deployment is already started
     * 
     * @return true if startDeployment has already been called, false otherwise
     */
    public boolean isStarted();

    public void waitReady();

    /**
     * Returns the Virtual Node associated to this name
     * 
     * @param vnName
     *            a Virtual Node name declared inside the GCM Application Descriptor
     * @return the GCMVirtualNode associated to vnName or null if the Virtual Node does not exist
     */
    public GCMVirtualNode getVirtualNode(String vnName);

    /**
     * Returns all the Virtual Nodes known by this application
     * 
     * Keys are Virtual Node
     * 
     * @return All the Virtual Nodes known by this application
     */
    public Map<String, GCMVirtualNode> getVirtualNodes();

    /**
     * Returns all the Nodes created by this application
     * 
     * Typical applications should not use this method but the Virtual Node abstraction.
     * 
     * @return All the Nodes created by this application
     */
    public List<Node> getAllCurrentNodes();

    /**
     * Returns the topology of all the Nodes created by this application
     * 
     * Typical applications should not use this method but the Virtual Node abstraction.
     * 
     * 
     * This method should not be used. Usage of the Virtual Node abstraction is strongly advised.
     * 
     * This method only exists to allow application to perform smarter deployment than the Node
     * Allocator and Virtual Nodes can do. If your application needs a fine control on where active
     * objects are created (advanced coallocation for example), then you probably have to forget
     * about Virtual Node.
     * 
     * @return the current topology of all the nodes inside the application
     */
    public Topology getAllCurrentNodesTopology();

    /**
     * Returns all non attached Nodes
     * 
     * Nodes are attached to Virtual Node by the Node Allocator. The Node Allocator follows the
     * rules described inside the GCM Application Descriptor. This method returns all the Nodes
     * started by this application but that have not been attached to a Virtual Node.
     * 
     * @return all non attached Nodes
     */
    public List<Node> getCurrentUnmappedNodes();

    public String debugUnmappedNodes();

    public long getNbUnmappedNodes();

    /**
     * Updates the Topology passed in parameter
     * 
     * Nodes present in the Application but not in the Topology are added to it.
     * 
     * @param topology
     *            the topology to be updated
     */
    public void updateTopology(Topology topology);

    /**
     * Returns the variable contract associated to this application
     * 
     * @return The variable contract associated to this application
     */
    public VariableContractImpl getVariableContract();

    /**
     * Returns the descriptor url associated to this Application
     * @return descriptor url
     */
    public URL getDescriptorURL();
}
