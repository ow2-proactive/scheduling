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
package org.objectweb.proactive.extra.resourcemanager.core;

import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.resourcemanager.common.event.RMInitialState;
import org.objectweb.proactive.extra.resourcemanager.core.RMCore;
import org.objectweb.proactive.extra.resourcemanager.exception.AddingNodesException;
import org.objectweb.proactive.extra.resourcemanager.frontend.NodeSet;
import org.objectweb.proactive.extra.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extra.resourcemanager.frontend.RMMonitoring;
import org.objectweb.proactive.extra.resourcemanager.frontend.RMUser;
import org.objectweb.proactive.extra.resourcemanager.nodesource.dynamic.DynamicNodeSource;
import org.objectweb.proactive.extra.resourcemanager.nodesource.dynamic.P2PNodeSource;
import org.objectweb.proactive.extra.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.resourcemanager.nodesource.pad.PADNodeSource;
import org.objectweb.proactive.extra.resourcemanager.rmnode.RMNode;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


/**
 * Interface of the RMCore Active object for {@link RMAdmin},
 * {@link RMUser}, {@link RMMonitoring} active objects.
 *
 * @see RMCore
 *
 * @author proactive team
 *
 */
public interface RMCoreInterface {

    /**
     * Gives a String representation of the RMCore's ID.
     * @return String representation of the RMCore's ID.
     */
    public String getId();

    /**
     * Creates a static node source Active Object.
     * Creates a new static node source which is a {@link PADNodeSource} active object.
     * @param pad a ProActiveDescriptor object to deploy at the node source creation.
     * @param sourceName name given to the static node source.
     */
    public void createStaticNodesource(ProActiveDescriptor pad,
        String sourceName);

    /**
     * Creates a Dynamic Node source Active Object.
     * Creates a new dynamic node source which is a {@link P2PNodeSource} active object.
     * Other dynamic node source (PBS, OAR) are not yet implemented
     * @param id name of the dynamic node source to create
     * @param nbMaxNodes max number of nodes the NodeSource has to provide.
     * @param nice nice time in ms, time to wait between a node remove and a new node acquisition
     * @param ttr Time to release in ms, time during the node will be kept by the nodes source and the Core.
     * @param peerUrls vector of ProActive P2P living peer and able to provide nodes.
     */
    public void createDynamicNodeSource(String id, int nbMaxNodes, int nice,
        int ttr, Vector<String> peerUrls);

    /**
     * Creates a dummy node source to test a {@link DynamicNodeSource} active object
     * @param id name of the dynamic node source to create
     * @param nbMaxNodes max number of number the NodeSource has to provide.
     * @param nice nice time in ms, time to wait between a node remove and a new node acquisition
     * @param ttr Time to release in ms, time during the node will be keeped by the Nodesource and the Core.
     */
    public void createDummyNodeSource(String id, int nbMaxNodes, int nice,
        int ttr);

    /**
     * Add nodes to a static Node Source.
     * Ask to a static Node source to deploy a ProActiveDescriptor.
     * nodes deployed will be added after to RMCore, by the NodeSource itself.
     * @param pad ProActiveDescriptor to deploy
     * @param sourceName name of an existing PADNodesource
     * @throws AddingNodesException if the NodeSource
     */
    public void addNodes(ProActiveDescriptor pad, String sourceName);

    /**
     * Add nodes to the default static Node Source.
     * Ask to the default static Node source to deploy a ProActiveDescriptor.
     * nodes deployed will be added after to RMCore, by the NodeSource itself.
     * @param pad ProActiveDescriptor to deploy
     */
    public void addNodes(ProActiveDescriptor pad);

    /**
     * Remove a node from the Core and from its node source.
     * perform the removing request of a node
     * asked by {@link RMAdmin} active object.<BR><BR>
     *
     * If the node is down, node is just removed from the Core, and nothing is asked to its related NodeSource,
     * because the node source has already detected the node down (it is its function), informed the RMCore,
     * and removed the node from its list.<BR>
     * Else the removing request is just forwarded to the corresponding NodeSource of the node.<BR><BR>
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     *
     */
    public void removeNode(String nodeUrl, boolean preempt);

    /**
     * Stops the RMCore.
     * Stops all {@link NodeSource} active objects
     * Stops {@link RMAdmin}, {@link RMUser}, {@link RMMonitoring} active objects.
     */
    public void shutdown(boolean preempt);

    /**
     * Stops and removes a NodeSource active object with their nodes from the Resource Manager
     * @param sourceName name of the NodeSource object to remove
     * @param preempt true all the nodes must be removed immediately, without waiting job ending if nodes are busy,
     * false nodes are removed just after the job ending if busy.
     */
    public void removeSource(String sourceName, boolean preempt);

    /**
    * Get a set of nodes that verify a selection script.
    * This method has three way to handle the request :<BR>
    *  - if there is no script, it returns at most the
    * first nb free nodes asked.<BR>
    * - If the script is a dynamic script, the method will
    * test the resources, until nb nodes verify the script or if there is no
    * node left.<BR>
    * - If the script is a static script, it will return in priority the
    * nodes on which the given script has already been verified.<BR>
    *
    * @param nb number of node to provide
    * @param selectionScript that nodes must verify
    */
    public NodeSet getAtMostNodes(IntWrapper nb, SelectionScript selectionScript);

    /**
     * Returns an exactly number of nodes
     * not yet implemented.
     * @param nb exactly number of nodes to provide.
     * @param selectionScript  that nodes must verify.
     */
    public NodeSet getExactlyNodes(IntWrapper nb,
        SelectionScript selectionScript);

    /**
     * Free a node after a work.
     * RMUser active object wants to free a node that has ended a task.
     * If the node is 'to be released', perform the removing mechanism with
     * the {@link NodeSource} object corresponding to the node,
     * otherwise just set the node to free.
     * @param node node that has terminated a task and must be freed.
     */
    public void freeNode(Node node);

    /**
     * Free a set of nodes.
     * @param nodes a set of nodes to set free.
     */
    public void freeNodes(NodeSet nodes);

    /**
     * Gives an array list of NodeSource object
     * @return list of NodeSource objects of the RM.
     */
    public ArrayList<NodeSource> getNodeSources();

    /**
     * Gives number of free nodes handled by the Core.
     * @return IntWrapper number of free nodes in the RMCore.
     */
    public IntWrapper getSizeListFreeRMNode();

    /**
     * Gives number of busy nodes handled by the Core.
     * @return IntWrapper number of busy nodes in the RMCore.
     */
    public IntWrapper getSizeListBusyRMNode();

    /**
     * Gives number of down nodes handled by the Core.
     * @return IntWrapper number of down nodes in the IMCore.
     */
    public IntWrapper getSizeListDownRMNode();

    /**
     * Gives number of 'to be released' nodes handled by the Core.
     * @return IntWrapper number of 'to be released' nodes in the RMCore.
     */
    public IntWrapper getSizeListToReleaseRMNode();

    /**
     * Gives number of all nodes handled by the Core.
     * @return IntWrapper number of nodes in the RMCore.
     */
    public IntWrapper getNbAllRMNode();

    /**
     * Gives the free nodes list
     * @return free nodes of the RMCore.
     */
    public ArrayList<RMNode> getListFreeRMNode();

    /**
     * Gives the busy nodes list
     * @return busy nodes of the RMCore.
     */
    public ArrayList<RMNode> getListBusyRMNode();

    /**
     * Gives the 'to be released' nodes list
     * @return 'to be released' nodes of the RMCore.
     */
    public ArrayList<RMNode> getListToReleasedRMNodes();

    /**
     * Gives the list of all nodes handled by th IMCore
     * @return 'to be released' nodes of the IMCore.
     */
    public ArrayList<RMNode> getListAllNodes();

    /**
     * Builds and returns a snapshot of RMCore's current state.
     * Initial state must be understood as a new Monitor point of view.
     * A new monitor start to receive RMCore events, so must be informed of the current
     * state of the Core at the beginning of monitoring.
            * @return RMInitialState containing nodes and nodeSources of the IMCore.
     */
    public RMInitialState getRMInitialState();

    /**
     * Returns the ProActive Node containing the RMCore active object.
     * @return the ProActive Node containing the RMCore active object.
     */
    public Node getNodeRM();

    /**
     * Returns the stub of RMAdmin ProActive object.
     * @return the RMAdmin ProActive object.
     */
    public RMAdmin getAdmin();

    /**
     * Returns the stub of RMMonitoring ProActive object.
     * @return the RMMonitoring ProActive object.
     */
    public RMMonitoring getMonitoring();

    /**
     * Returns the stub of RMUser ProActive object.
     * @return the RMUser ProActive object.
     */
    public RMUser getUser();
}
