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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad;

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
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.PadDeployInterface;


public class IMDeploy implements NodeCreationEventListener, Runnable {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_DEPLOY);

    // Attributes
    private String padName = null;
    private ProActiveDescriptor pad = null;
    private String[] vnNames = null;
    private PadDeployInterface nodeSource = null;

    //----------------------------------------------------------------------//
    // Construtors

    /**
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the proactive descriptor
     */
    public IMDeploy(PadDeployInterface nodeSource, String padName,
        ProActiveDescriptor pad) {
        this.nodeSource = nodeSource;
        this.padName = padName;
        this.pad = pad;
    }

    /**
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the proactive descriptor
     * @param vnNames : the name of the virtual nodes of this pad to deploy
     */
    public IMDeploy(PadDeployInterface nodeSource, String padName,
        ProActiveDescriptor pad, String[] vnNames) {
        this.nodeSource = nodeSource;
        this.padName = padName;
        this.pad = pad;
        this.vnNames = vnNames;
    }

    //----------------------------------------------------------------------//

    /**
     * Implementation of the method run to interface Runnable
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
     * When a node is activated this method is call for saving
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
