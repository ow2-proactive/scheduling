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
package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.P2PNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.PADNodeSource;


/**
 * An interface Front-End for the {@link IMAdminImpl} active object.
 * this Infrastructure Manager object is designed to receive and perform
 * administrator commands :<BR>
 * -initiate creation and removal of {@link NodeSource} active objects.<BR>
 * -add nodes to a static node source ({@link PADNodeSource}), by
 * a ProActive descriptor.<BR>
 * -remove nodes from the IM.<BR>
 * -shutdown the IM.<BR>
 *
 * @author ProActive team
 *
 */
@PublicAPI
public interface IMAdmin extends Serializable {

    /**
     * Creates a static Node source and deploy nodes specified in the PAD.
     * @param sourceName name of the source to create.
     * @param pad ProActive deployment descriptor to deploy.
     */
    public void createStaticNodesource(String sourceName,
        ProActiveDescriptor pad);

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
     * Add nodes to the default static nodes source of the scheduler
     * @param pad ProActive deployment descriptor to deploy.
     */
    public void addNodes(ProActiveDescriptor pad);

    /**
     * Add nodes to a StaticNodeSource represented by sourceName.
     * SourceName must exist and must be a static source
     * @param pad ProActive deployment descriptor to deploy.
     * @param sourceName name of the static node source that perform the deployment.
     */
    public void addNodes(ProActiveDescriptor pad, String sourceName);

    /**
     * Removes a node from the IM.
     * Performs the removing request of a node.
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void removeNode(String nodeUrl, boolean preempt);

    /**
     * Remove a node source from the IM.
     * Performs the removing of a node source.
     * All nodes handled by the node source are removed.
     * @param sourceName name (id) of the source to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void removeSource(String sourceName, boolean preempt);

    /**
     * Kills IMAdin object.
     * @exception ProActiveException
     */
    public void shutdown() throws ProActiveException;
}
