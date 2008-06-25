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
package org.ow2.proactive.resourcemanager.nodesource.pad;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.nodesource.frontend.PadDeployInterface;


/**
 * Provides a ProActive descriptor (PAD)
 * deployment mechanism for Resource Manager.
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 *
 */
public class RMDeploy implements NodeCreationEventListener, Runnable {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.RM_DEPLOY);

    /** name of the Pad to deploy */
    private String padName = null;

    /** pad object to deploy */
    private ProActiveDescriptor pad = null;

    /** Array of virtual nodes names to deploy */
    private String[] vnNames = null;

    /** stub of {@link PADNodeSource} active object that initiated the deployment */
    private PadDeployInterface nodeSource = null;

    /**
     * Creates an RMDeploy object.
     * @param nodeSource Stub of NodeSource object that initiated the deployment.
     * @param padName descriptor name to deploy.
     * @param pad ProActive descriptor to deploy.
     */
    public RMDeploy(PadDeployInterface nodeSource, ProActiveDescriptor pad) {
        this.nodeSource = nodeSource;
        this.padName = pad.getUrl();
        this.pad = pad;
    }

    /**
     * Creates an RMDeploy object.
     * @param nodeSource Stub of NodeSource object that initiated the deployment.
     * @param padName descriptor name to deploy.
     * @param pad ProActive descriptor to deploy.
     * @param vnNames virtual nodes to deploy
     */
    public RMDeploy(PadDeployInterface nodeSource, ProActiveDescriptor pad, String[] vnNames) {
        this.nodeSource = nodeSource;
        this.padName = pad.getUrl();
        this.pad = pad;
        this.vnNames = vnNames;
    }

    /**
     * Implementation of the method run to interface Runnable
     * Performs the deployment of the PAD.
     */
    public void run() {
        VirtualNode[] vns;
        if (vnNames == null) {
            vns = pad.getVirtualNodes();
        } else {
            vns = new VirtualNode[vnNames.length];
            for (int i = 0; i < this.vnNames.length; i++) {
                vns[i] = pad.getVirtualNode(this.vnNames[i]);
            }
        }
        for (VirtualNode vn : vns) {
            ((VirtualNodeImpl) vn).addNodeCreationEventListener(this);
            vn.activate();
        }

        for (VirtualNode vn : vns) {
            try {
                ((VirtualNodeImpl) vn).waitForAllNodesCreation();
            } catch (NodeException e) {
                logger.warn("NodeException : " + e, e);
            }
        }
    }

    /**
     * Called when new node is deployed and available.
     * When a node is activated this method is called for adding
     * the new activated nodes in the NodeSource.
     * @param event
     */
    public void nodeCreated(NodeCreationEvent event) {
        Node node = event.getNode();
        ProActiveRuntime par = node.getProActiveRuntime();
        try {
            String vnName = par.getVNName(node.getNodeInformation().getName());
            this.nodeSource.receiveDeployedNode(node, vnName, padName);
        } catch (ProActiveException e) {
            logger.warn("ProActiveException : " + e, e);
        }
    }
}
