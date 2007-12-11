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
package org.objectweb.proactive.extensions.resourcemanager.frontend;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCore;
import org.objectweb.proactive.extensions.resourcemanager.core.RMCoreInterface;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.dynamic.P2PNodeSource;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend.NodeSource;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.pad.PADNodeSource;


/**
 * Implementation of the {@link RMAdmin} active object.
 * the RMAdmin active object object is designed to receive and perform
 * administrator commands :<BR>
 * -initiate creation and removal of {@link NodeSource} active objects.<BR>
 * -add nodes to static nodes sources ({@link PADNodeSource}), by
 * a ProActive descriptor.<BR>
 * -remove nodes from the RM.<BR>
 * -shutdown the RM.<BR>
 *
 * @author ProActive team
 *
 */
public class RMAdminImpl implements RMAdmin, Serializable, InitActive {

    /** serial version UID */
    private static final long serialVersionUID = 320085562179242055L;

    /** Log4J logger name for RMAdmin */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.RM_ADMIN);

    /** IMCore active object of the RM */
    private RMCoreInterface rmcore;

    /**
     * ProActive Empty constructor
     */
    public RMAdminImpl() {
    }

    /**
     * Creates the RMAdmin object
     * @param rmcore Stub of the {@link RMCore} active object of the RM.
     */
    public RMAdminImpl(RMCoreInterface rmcore) {
        this.rmcore = rmcore;
    }

    /**
     * Initialization part of the RMAdmin active object.
     * Register in RMI register the RMAdmin active object.
     */
    public void initActivity(Body body) {
        try {
            PAActiveObject.register(PAActiveObject.getStubOnThis(),
                "//localhost/" + RMConstants.NAME_ACTIVE_OBJECT_RMADMIN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a static Node source and deploy nodes specified in the PAD.
     * @param sourceName name of the source to create.
     * @param pad ProActive deployment descriptor to deploy.
     */
    public void createStaticNodesource(String sourceName,
        ProActiveDescriptor pad) {
        this.rmcore.createStaticNodesource(pad, sourceName);
    }

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
        int ttr, Vector<String> peerUrls) {
        this.rmcore.createDynamicNodeSource(id, nbMaxNodes, nice, ttr, peerUrls);
    }

    /**
     * Add nodes to the default static nodes source of the scheduler
     * @param pad ProActive deployment descriptor to deploy.
     */
    public void addNodes(ProActiveDescriptor pad) {
        this.rmcore.addNodes(pad);
    }

    /**
     * Add nodes to a StaticNodeSource represented by sourceName
     * SourceName must exist and must be a static source
     * @param pad ProActive deployment descriptor to deploy.
     * @param sourceName name of the static node source that perform the deployment.
     */
    public void addNodes(ProActiveDescriptor pad, String sourceName) {
        this.rmcore.addNodes(pad, sourceName);
    }

    /**
     * Removes a node from the RM.
     * perform the removing request of a node.
     * @param nodeUrl URL of the node to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void removeNode(String nodeUrl, boolean preempt) {
        this.rmcore.removeNode(nodeUrl, preempt);
    }

    /**
     * Removes a node source from the RM.
     * perform the removing of a node source.
     * All nodes handled by the node source are removed.
     * @param sourceName name (id) of the source to remove.
     * @param preempt true the node must be removed immediately, without waiting job ending if the node is busy,
     * false the node is removed just after the job ending if the node is busy.
     */
    public void removeSource(String sourceName, boolean preempt) {
        this.rmcore.removeSource(sourceName, preempt);
    }

    /**
     * Kills RMAdin object.
     * @exception ProActiveException
     */
    public void shutdown(boolean preempt) throws ProActiveException {
        this.rmcore.shutdown(preempt);
        PAActiveObject.terminateActiveObject(false);
    }
}
