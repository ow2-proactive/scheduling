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
package org.objectweb.proactive.extra.scheduler.resourcemanager;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class SimpleResourceManager implements GenericResourceManager,
    NodeCreationEventListener, InitActive {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.RESOURCE_MANAGER);
    //holds the nodes
    Vector<Node> freeNodes;
    //holds the virutal nodes, only used to kill the nodes when the active object is closed
    Vector<VirtualNode> vn;

    public SimpleResourceManager() {
    } //proactive no arg constructor

    public void initActivity(Body body) {
        freeNodes = new Vector<Node>();
        vn = new Vector<VirtualNode>();

        if (logger.isDebugEnabled()) {
            logger.debug("Resource Manager Initialized");
        }
    }

    public BooleanWrapper stopRM() {
        try {
            for (int i = 0; i < vn.size(); i++) {
                ((VirtualNodeImpl) vn.get(i)).waitForAllNodesCreation();
                ((VirtualNodeImpl) vn.get(i)).removeNodeCreationEventListener(this);
                ((VirtualNodeImpl) vn.get(i)).killAll(false);
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "finished deactivating nodes, will terminate Resource Manager");
            }

            ProActiveObject.terminateActiveObject(true);

            //sucess
            return new BooleanWrapper(true);
        } catch (Exception e) {
            logger.error("Couldnt Terminate the Resource manager" +
                e.toString());

            return new BooleanWrapper(false);
        }
    }

    //adds the virtual nodes and create listeners for the virtual nodes to add nodes whenever created
    public void addNodes(String xmlURL) {
        try {
            ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(xmlURL);
            VirtualNode[] virtualNodes = pad.getVirtualNodes();

            for (int i = 0; i < virtualNodes.length; i++) {
                ((VirtualNodeImpl) virtualNodes[i]).addNodeCreationEventListener(this);
                virtualNodes[i].activate();
                vn.add(virtualNodes[i]);

                if (logger.isDebugEnabled()) {
                    logger.debug("Virtual Node " + virtualNodes[i].getName() +
                        " added to resource manager");
                }
            }
        } catch (Exception e) {
            logger.error("Couldnt add the specified resources" + e.toString());
        }
    }

    public Vector<Node> getAtMostNNodes(IntWrapper maxNodeNb) {
        Vector<Node> nodesToSend = new Vector<Node>();

        //exits the loop when there are no more free 
        while (!freeNodes.isEmpty() &&
                (nodesToSend.size() < maxNodeNb.intValue())) {
            nodesToSend.add(freeNodes.remove(0));
        }

        if (logger.isDebugEnabled() && (nodesToSend.size() > 0)) {
            logger.debug(nodesToSend.size() + " Nodes Reserved");
        }

        return nodesToSend;
    }

    public void freeNodes(Vector<Node> nodesToFree) {
        int nodesFreed = 0;

        while (!nodesToFree.isEmpty()) {
            try {
                nodesToFree.get(0).killAllActiveObjects();
                freeNodes.add(nodesToFree.remove(0));
                nodesFreed++;
            } catch (Exception e) {
                nodesToFree.remove(0);
                logger.error("Node has died " + e.toString());
            }
        }

        if (logger.isDebugEnabled() & (nodesFreed > 0)) {
            logger.debug(nodesFreed + " nodes Freed.");
        }
    }

    public void nodeCreated(NodeCreationEvent event) {
        // get the node
        freeNodes.add(event.getNode());

        if (logger.isDebugEnabled()) {
            logger.debug("Node at " +
                event.getNode().getNodeInformation().getURL() +
                " added to Resource Manager");
        }
    }
}
