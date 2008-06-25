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
package org.ow2.proactive.resourcemanager.core;

import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.pad.PADNodeSource;
import org.ow2.proactive.resourcemanager.common.scripting.SelectionScript;


/**
 * Interface of the RMCore Active object for {@link RMAdmin},
 * {@link RMUser}, {@link RMMonitoring} active objects.
 *
 * @see RMCore
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
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
     * @param padList a list of ProActiveDescriptor objects to deploy at the node source creation.
     * @param sourceName name given to the static node source.
     * @throws RMException if an errors occurs during the creation of the static node source.  
     */
    public void createStaticNodesource(List<ProActiveDescriptor> padList, String sourceName)
            throws RMException;

    /**
     * Creates a GCM node source Active Object, which is a static node source,
     * and deploys the GCMApllication descriptor
     * @param GCMApp A GCMApplication GCMAppliication object containing virtual nodes to deploy,
     * this parameter can be set to null.
     * @param sourceName the name of the node source to create
     * @throws RMException if the creation fails, notably if a node source with the same 
     * source name is already existing. 
     */
    public void createGCMNodesource(GCMApplication GCMApp, String sourceName) throws RMException;

    /**
     * Creates a Dynamic Node source Active Object.
     * Creates a new dynamic node source which is a 
     * {@link org.objectweb.proactive.extra.p2p.scheduler.P2PNodeSource} active object.
     * Other dynamic node source (PBS, OAR) are not yet implemented
     * @param id name of the dynamic node source to create
     * @param nbMaxNodes max number of nodes the NodeSource has to provide.
     * @param nice nice time in ms, time to wait between a node remove and a new node acquisition
     * @param ttr Time to release in ms, time during the node will be kept by the nodes source and the Core.
     * @param peerUrls vector of ProActive P2P living peer and able to provide nodes.
     * @throws RMException if the creation of the node fails.
     */
    public void createDynamicNodeSource(String id, int nbMaxNodes, int nice, int ttr, Vector<String> peerUrls)
            throws RMException;

    /**
     * Add nodes to a static Node Source.
     * Ask to a static Node source to deploy a GCMApplication descriptor.
     * nodes deployed will be added after to RMCore, by the NodeSource itself.
     * @param GCMApp a GCMAppliication object containing virtual nodes to deploy
     * @param sourceName name of an existing GCMNodesource
     * @throws RMException if the new node cannot be added, notably if the adding node.
     * attempt has been requested on a node source. 
     */
    public void addingNodesAdminRequest(GCMApplication GCMApp, String sourceName) throws RMException;

    /**
     * Add nodes to the default static Node Source.
     * Ask to the default static Node source to deploy a GCMApplication descriptor.
     * nodes deployed will be added after to RMCore, by the NodeSource itself.
     * @param GCMApp GCMApplication object containing virtual nodes to deploy.
     */
    public void addingNodesAdminRequest(GCMApplication GCMApp);

    /**
     * Add a deployed node to the default static nodes source of the RM
     * @param nodeUrl URL of the node.
     * @throws RMException if the new node cannot be added, notably if the adding node
     * is requested on a dynamic node source.
     */
    public void addingNodeAdminRequest(String nodeUrl) throws RMException;;

    /**
     * Add nodes to a StaticNodeSource represented by sourceName.
     * SourceName must exist and must be a static source
     * @param nodeUrl URL of an existing node to add.
     * @param sourceName name of the static node source that perform the deployment.
     * @throws RMException if the new node cannot be added, notably if the adding node
     * is requested on a dynamic node source. 
     */
    public void addingNodeAdminRequest(String nodeUrl, String sourceName) throws RMException;

    /**
     * Remove a node from the Core and from its node source.
     * perform the removing request of a node
     * asked by {@link RMAdmin} active object.<BR><BR>
     *
     * If the node is down, node is just removed from the Core, and nothing is asked to its related NodeSource,
     * because the node source has already detected the node down (it is its function), informed the RMCore,
     * and removed the node from its list.<BR>
     * Else the node is removed from the Core and the removing request is forwarded to the corresponding NodeSource of the node.
     * If the node is busy and the removal request is non preemptive, the node is just put in 'to release' state
     * <BR><BR>
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     *
     */
    public void nodeRemovalAdminRequest(String nodeUrl, boolean preempt);

    /**
     * Stops the RMCore.
     * Stops all {@link NodeSource} active objects
     * Stops {@link RMAdmin}, {@link RMUser}, {@link RMMonitoring} active objects.
     * @param preempt if set to true, Resource manager wait its RM User give back all the busy
     * nodes before performing the shutdown 
     */
    public void shutdown(boolean preempt);

    /**
     * Stops and removes a NodeSource active object with their nodes from the Resource Manager
     * @param sourceName name of the NodeSource object to remove
     * @param preempt true all the nodes must be removed immediately, without waiting job ending if nodes are busy,
     * false nodes are removed just after the job ending if busy.
     * @throws RMException if the node source cannot be removed, notably if the name of the node source is unknown.
     */
    public void nodeSourceRemovalAdminRequest(String sourceName, boolean preempt) throws RMException;

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
     * - This will not returns node that are specified in the exclusion list.
     *
     * @param nb number of node to provide
     * @param selectionScript that nodes must verify
     * @param exclusion the exclusion nodes that cannot be returned
     * @return an array list of nodes.
     */
    public NodeSet getAtMostNodes(IntWrapper nb, SelectionScript selectionScript, NodeSet exclusion);

    /**
     * Returns an exactly number of nodes
     * not yet implemented.
     * @param nb exactly number of nodes to provide.
     * @param selectionScript  that nodes must verify.
     * @return an array list of nodes.
     */
    public NodeSet getExactlyNodes(IntWrapper nb, SelectionScript selectionScript);

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
     * Gives number of free nodes handled by the Core.
     * @return IntWrapper number of free nodes in the RMCore.
     */
    public IntWrapper getSizeListFreeRMNodes();

    /**
     * Gives number of all nodes handled by the Core.
     * @return IntWrapper number of nodes in the RMCore.
     */
    public IntWrapper getNbAllRMNodes();

    /**
     * Set the ping frequency to the default node source
     * @param frequency the frequency to set to the node source in ms.
     */
    public void setPingFrequency(int frequency);

    /**
     * Set the ping frequency to a node source
     * @param frequency the frequency to set to the node source in ms.
     * @param sourceName name of the node source to set the frequency
     * @throws RMException if node source name is unknown.
     */
    public void setPingFrequency(int frequency, String sourceName) throws RMException;

    /**
     * Return the Ping frequency of a node source
     * @param sourceName name of the node source
     * @return the ping frequency
     * @throws RMException if the node source doesn't exist
     */
    public IntWrapper getPingFrequency(String sourceName) throws RMException;

    /**
     * Set the ping frequency to all nodes sources.
     * @param frequency the frequency to set to the node source in ms.
     */
    public void setAllPingFrequency(int frequency);

    /**
     * Builds and returns a snapshot of RMCore's current state.
     * Initial state must be understood as a new Monitor point of view.
     * A new monitor start to receive RMCore events, so must be informed of the current
     * state of the Core at the beginning of monitoring.
     * @return RMInitialState containing nodes and nodeSources of the RMCore.
     */
    public RMInitialState getRMInitialState();

    /**
     * Returns the stub of RMAdmin Active object.
     * @return the RMAdmin Active object.
     */
    public RMAdmin getAdmin();

    /**
     * Returns the stub of RMMonitoring Active object.
     * @return the RMMonitoring Active object.
     */
    public RMMonitoring getMonitoring();

    /**
     * Returns the stub of RMUser Active object.
     * @return the RMUser Active object.
     */
    public RMUser getUser();

}
