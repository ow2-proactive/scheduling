/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.exception.RMException;


/**
 * An interface Front-End for the {@link RMAdminImpl} active object.
 * this Resource Manager object is designed to receive and perform
 * administrator commands :<BR>
 * -initiate creation and removal of {@link org.ow2.proactive.resourcemanager.nodesource.frontend#NodeSource} active objects.<BR>
 * -add nodes to a static node source ({@link org.ow2.proactive.resourcemanager.nodesource.gcm.GCMNodeSource}), by
 * a ProActive descriptor.<BR>
 * -remove nodes from the RM.<BR>
 * -shutdown the RM.<BR>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
@PublicAPI
public interface RMAdmin extends Serializable {

    /**
     * Set the ping frequency to the default node source
     * @param frequency the frequency to set to the node source in ms.
     */
    public void setDefaultNodeSourcePingFrequency(int frequency);

    /**
     * Set the ping frequency to a node source
     * @param frequency the frequency to set to the node source in ms.
     * @param sourceName name of the node source to set the frequency
     * @throws RMException if the node source doesn't exist
     */
    public void setNodeSourcePingFrequency(int frequency, String sourceName) throws RMException;

    /**
     * Set the ping frequency to all nodes sources.
     * @param frequency the frequency to set to the node sources in ms.
     */
    public void setAllNodeSourcesPingFrequency(int frequency);

    /**
     * Return the Ping frequency of a node source
     * @param sourceName name of the node source
     * @return the ping frequency
     * @throws RMException if the node source doesn't exist
     */
    public IntWrapper getNodeSourcePingFrequency(String sourceName) throws RMException;

    /**
     * Creates a static Node source and deploy nodes specified in GCM deployment data.
     * GCMDeployment data is an array representing a deployment descriptor.
     * This GCM deployment descriptor will be combined with GCM by default GCM application template
     * used by resource manager.
     * A byte array is used to transfer a GCMDeployment descriptor from a remote RM management
     * application and a Resource Manager, an RCP plugin for example. GCMDeployment is stored in a byte array
     * because GCMDeployment object isn't serializable. 
     * Before using this function You can use 
     * {@link org.ow2.proactive.resourcemanager.common.FileToBytesConverter#convertFileToByteArray}
     * to transform your GCMDeployment file to a byte array before calling this method.
     * @param gcmDeploymentData byte array containing GCM deployment XML descriptor, or null
     * 			if you want to create an empty Node Source
     * @param sourceName Name of the node source to create.
     * @throws RMException if the creation fails, notably if a node source with
     * the same name is already existing.
     */
    public void createGCMNodesource(byte[] gcmDeploymentData, String sourceName) throws RMException;

    /**
     * Creates a Dynamic Node source Active Object.
     * Creates a new dynamic node source which is a {@link org.ow2.proactive.resourcemanager.nodesource.p2p.P2PNodeSource} active object.
     * Other dynamic node source (PBS, OAR) are not yet implemented
     * @param id name of the dynamic node source to create
     * @param nbMaxNodes max number of nodes the NodeSource has to provide.
     * @param nice nice time in ms, time to wait between a node remove and a new node acquisition
     * @param ttr Time to release in ms, time during the node will be kept by the nodes source and the Core.
     * @param peerUrls vector of ProActive P2P living peer and able to provide nodes.
     * @throws RMException if the creation fails notably if a node source with
     * the same name is already existing. 
     */
    public void createDynamicNodeSource(String id, int nbMaxNodes, int nice, int ttr, Vector<String> peerUrls)
            throws RMException;

    /**
     * deploy nodes specified in GCM deployment to the default node source.
     * GCMDeployment data is an array representing a deployment descriptor.
     * This GCM deployment descriptor will be combined with GCM by default GCM application template
     * used by resource manager. 
     * A byte array is used to transfer a GCMDeployment descriptor from a remote RM management
     * application and a Resource Manager, an RCP plugin for example. GCMDeployment is stored in a byte array
     * because GCMDeployment object isn't serializable.
     * Before using this function You can use 
     * {@link rg.ow2.proactive.resourcemanager.utils.FileToBytesConverter#convertFileToByteArray}
     * to transform your GCMDeployment file to a byte array before calling this method.
     * @param gcmDeploymentData byte array containing GCM deployment XML description
     * @throws RMException if an error occurs during the deployment of GCM deployment 
     * descriptor object.
     */
    public void addNodes(byte[] gcmDeploymentData) throws RMException;

    /**
     * Deploy nodes specified in GCM deployment using an already created GCMNodeSource.
     * GCMDeployment data is an array representing a deployment descriptor.
     * This GCM deployment descriptor will be combined with GCM by default GCM application template
     * used by resource manager. 
     * A byte array is used to transfer a GCMDeployment descriptor from a remote RM management
     * application and a Resource Manager, an RCP plugin for example. GCMDeployment is stored in a byte array
     * because GCMDeployment object isn't serializable.
     * Before using this function You can use 
     * {@link rg.ow2.proactive.resourcemanager.utils.FileToBytesConverter#convertFileToByteArray}
     * to transform your GCMDeployment file to a byte array before calling this method.
     * @param gcmDeploymentData byte array containing GCM deployment xml description
     * @param sourceName Name of the node source already created that will handle new nodes.
     * @throws RMException if the node source doesn't exist 
     */
    public void addNodes(byte[] gcmDeploymentData, String sourceName) throws RMException;

    /**
     * Add an already deployed node to the default static nodes source of the RM
     * @param nodeUrl URL of the node to add.
     * @throws RMException if a exception occurs during the node registration.
     */
    public void addNode(String nodeUrl) throws RMException;

    /**
     * Add nodes to a StaticNodeSource represented by sourceName.
     * SourceName must exist and must be a static source
     * @param nodeUrl URL of the node to add.
     * @param sourceName name of the static node source that will handle the node
     * @throws RMException if a exception occurs during the node registration,
     * notably if the node source doesn't exist
     */
    public void addNode(String nodeUrl, String sourceName) throws RMException;

    /**
     * Removes a node from the RM.
     * Performs the removing request of a node.
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void removeNode(String nodeUrl, boolean preempt);

    /**
     * Remove a node source from the RM.
     * Performs the removing of a node source.
     * All nodes handled by the node source are removed.
     * @param sourceName name (id) of the source to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     * @throws RMException if the node source doesn't exists
     */
    public void removeSource(String sourceName, boolean preempt) throws RMException;

    /**
     * Kills Resource Manager
     * @param preempt if true, resource manager is killed without waiting current tasks ending,
     * otherwise Resource manager wait its RM user give back busy nodes before performing the shutdown.
     * @exception ProActiveException
     *
     */
    public void shutdown(boolean preempt) throws ProActiveException;
}
