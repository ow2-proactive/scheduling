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
package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.core.Topology;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;


/**
 * A GCM Application Descriptor
 *
 * TODO cmathieu write documentation here
 */
@PublicAPI
public interface GCMApplicationDescriptor {

    /**
     * Starts the deployment process
     *
     * Creates remote ProActive Runtimes and Nodes.
     *
     * Applications should subscribe to event notification before calling
     * this method.
     *
     *  @see GCMVirtualNode
     */
    public void startDeployment();

    /**
     * Indicates if the deployment has been started
     * @return true if startDeployment has already been called, false otherwise
     */
    public boolean isStarted();

    /**
     * Returns the Virtual Node associated to this name
     *
     * @param vnName a Virtual Node name declared inside the GCM Application Descriptor
     * @return the GCMVirtualNode associated to vnName or null if the Virtual Node does not exist
     */
    public GCMVirtualNode getVirtualNode(String vnName);

    /**
     * Returns all the Virtual Nodes declared inside a GCM Application Descriptor
     *
     * Keys are the Virtual Node names and values the Virtual Nodes.
     *
     * @return All the Virtual Nodes declared inside the GCM Application Descriptor.
     */
    public Map<String, ? extends GCMVirtualNode> getVirtualNodes();

    /**
     * Terminates all the ProActive Runtime started by this Application
     */
    public void kill();

    /**
     * Returns all the Nodes currently available
     *
     * This method should not be used. Usage of the Virtual Node abstraction
     * is strongly advised. You should not use it unless you have to getCurrentTopology()
     *
     * @return all currently available Nodes
     */
    public Set<Node> getCurrentNodes();

    /**
     * Returns all non attached Nodes
     *
     * Nodes are attached to Virtual Node by the Node Allocator. The Node Allocator
     * follows the rules described inside the GCM Application Descriptor. This method
     * returns all the Nodes started by this application but that have not been attached
     * to a Virtual Node.
     *
     * @return all non attached Nodes
     */
    public Set<Node> getCurrentUnusedNodes();

    /**
     * Returns the topology of all the Nodes currently available
     *
     * This method should not be used. Usage of the Virtual Node abstraction
     * is strongly advised.
     *
     * This method only exists to allow application to perform smarter deployment than
     * the Node Allocator and Virtual Nodes can do. If your application needs a fine
     * control on where active objects are created (advanced coallocation for example),
     * then you probably have to forget about Virtual Node.
     *
     * @return the current topology of all the nodes inside the application
     */
    public Topology getCurrentTopology();

    /**
     * Updates the Topology passed in parameter
     *
     * Nodes present in the Application but not in the Topology are added to it.
     *
     * @param topology the topology to be updated
     */
    public void updateTopology(Topology topology);

    public long getDeploymentId();
}
