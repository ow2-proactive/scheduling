/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.db.RMNodeData;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * This classe handles the recovery of node sources and of nodes when the RM is restarted.
 *
 * @author ActiveEon Team
 * @since 26/06/17
 */
public class RMRecoverer {

    private static final Logger logger = Logger.getLogger(RMRecoverer.class);

    public static final String START_TO_RECOVER_NODES = "Start to recover nodes";

    public static final String END_OF_NODES_RECOVERY = "Total number of nodes recovered: ";

    private RMCore resourceManager;

    private RMDBManager dbManager;

    public RMRecoverer(RMCore resourceManager, RMDBManager dbManager) {
        this.resourceManager = resourceManager;
        this.dbManager = dbManager;
    }

    protected void initiateRecoveryIfRequired() {
        if (nodesRecoveryEnabled()) {
            logger.info("Starting Nodes Recovery");
        } else {
            logger.info("Nodes Recovery is disabled. Removing all nodes from database");
            dbManager.removeAllNodes();
        }
        recoverNodeSourcesAndNodes();
    }

    /**
     * @return whether there are nodes in database for the given node source
     *
     * In case of a recovery, we need to check whether there are nodes in
     * database for this node source, otherwise, we will do a redeployment
     * from scratch. The reason is that when the RM shuts down correctly,
     * it removes all its nodes. Thus if we restart and recover the RM
     * afterwards there will be no nodes in the database.
     */
    public boolean existNodesToRecover(String nodeSourceName, boolean nodesRecoverable) {
        boolean recoverNodes = false;
        if (nodesRecoveryEnabled() && nodesRecoverable) {
            Collection<RMNodeData> nodesData = dbManager.getNodesByNodeSource(nodeSourceName);
            if (nodesData.isEmpty()) {
                logger.info("No node found in database for node source: " + nodeSourceName);
            } else {
                recoverNodes = true;
            }
        }
        return recoverNodes;
    }

    private void recoverNodeSourcesAndNodes() {
        Collection<NodeSourceData> nodeSources = dbManager.getNodeSources();
        logPersistedNodeSourceInfo(nodeSources);

        for (NodeSourceData nodeSourceData : nodeSources) {
            String nodeSourceDataName = nodeSourceData.getName();
            try {
                logger.info("Recovering node source " + nodeSourceDataName);
                resourceManager.createNodeSource(nodeSourceData, nodeSourceData.getNodesRecoverable());
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
                resourceManager.addBrokenNodeSource(nodeSourceDataName);
            }
        }
    }

    private void logPersistedNodeSourceInfo(Collection<NodeSourceData> nodeSources) {
        if (nodeSources.isEmpty()) {
            logger.info("No node source found in database");
        } else {
            logger.info("Number of node sources found in database: " + nodeSources.size());
        }
    }

    public void recoverNodes(NodeSource nodeSource) {
        logger.info(START_TO_RECOVER_NODES); // this log line is important for performance tests
        int lookUpTimeout = PAResourceManagerProperties.RM_NODELOOKUP_TIMEOUT.getValueAsInt();
        String nodeSourceName = nodeSource.getName();

        makeSureNodeSourceHasNoNode(nodeSource, nodeSourceName);

        Collection<RMNodeData> nodesData = dbManager.getNodesByNodeSource(nodeSourceName);
        logger.info("Number of nodes found in database for node source " + nodeSourceName + ": " + nodesData.size());

        Map<NodeState, Integer> nodeStates = new HashMap<>();
        int totalEligibleRecoveredNodes = 0;

        // for each node found in database, try to lookup node or recover it
        // as down node
        for (RMNodeData rmNodeData : nodesData) {
            String nodeUrl = rmNodeData.getNodeUrl();
            RMNode rmnode = null;

            if (rmNodeData.getState().equals(NodeState.DEPLOYING) || rmNodeData.getState().equals(NodeState.LOST)) {
                rmnode = recoverDeployingNodeInternally(nodeSource, rmNodeData, nodeUrl);
            } else {

                Node node = tryToLookupNode(nodeSource, lookUpTimeout, nodeUrl);

                if (node != null) {
                    rmnode = recoverNodeInternally(nodeSource, rmNodeData, nodeUrl, node);

                } else {
                    // the node is not recoverable and does not appear in any data
                    // structures: we can remove it safely from database
                    dbManager.removeNode(rmNodeData);
                    triggerDownNodeHandling(nodeSource, rmNodeData, nodeUrl);
                }
            }

            increaseNbNodesInState(nodeStates, rmNodeData);

            // we must add the recreated node to the eligible data
            // structure if we want it to be usable by a task
            if (isEligible(rmnode)) {
                resourceManager.addEligibleNode(rmnode);
                totalEligibleRecoveredNodes++;
            }

        }

        int totalRecoveredNodes = 0;

        logNodeRecoverySummary(nodeSourceName, nodeStates, totalRecoveredNodes, totalEligibleRecoveredNodes);
    }

    private void increaseNbNodesInState(Map<NodeState, Integer> nodeStates, RMNodeData rmNodeData) {
        Integer nbNodesInState = nodeStates.get(rmNodeData.getState());
        int newNbNodesInState = nbNodesInState == null ? 1 : nbNodesInState + 1;
        nodeStates.put(rmNodeData.getState(), newNbNodesInState);
    }

    private RMNode recoverNodeInternally(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl, Node node) {
        RMNode rmNode = null;
        // the node has been successfully looked up, we compare its
        // information to the node data retrieved in database.
        if (rmNodeData.equalsToNode(node)) {
            logger.info("Node to recover could successfully be looked up at URL: " + nodeUrl);
            rmNode = nodeSource.internalAddNodeAfterRecovery(node, rmNodeData);
            resourceManager.registerAvailableNode(rmNode);
        } else {
            logger.error("The node that has been looked up does not have the same information as the node to recover: " +
                         node.getNodeInformation().getName() + " is not equal to " + rmNodeData.getName() + " or " +
                         node.getNodeInformation().getURL() + " is not equal to " + rmNodeData.getNodeUrl());
        }
        return rmNode;
    }

    private RMNode recoverDeployingNodeInternally(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl) {
        logger.info("Recover " + rmNodeData.getState().toString() + " node at URL: " + nodeUrl);
        return nodeSource.internalAddDeployingNodeAfterRecovery(rmNodeData);
    }

    private void triggerDownNodeHandling(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl) {
        // if the node to recover was in deploying state then we have
        // nothing to do as it is going to be redeployed
        if (!rmNodeData.getState().equals(NodeState.DEPLOYING)) {
            // inform the node source that this recreated node is down
            nodeSource.detectedPingedDownNode(rmNodeData.getName(), nodeUrl);
        }
    }

    private Node tryToLookupNode(NodeSource nodeSource, int lookUpTimeout, String nodeUrl) {
        Node node = null;
        try {
            logger.info("Trying to lookup node to recover: " + nodeUrl);
            node = nodeSource.lookupNode(nodeUrl, lookUpTimeout);
        } catch (Exception e) {
            // do not log exception message here: not being able to look up a
            // node to recover is not an exceptional behavior
            logger.warn("Node to recover could not be looked up");
        }
        return node;
    }

    private void makeSureNodeSourceHasNoNode(NodeSource nodeSource, String nodeSourceName) {
        int nodesCount = nodeSource.getNodesCount();
        if (nodesCount != 0) {
            logger.warn("Recovered node source " + nodeSourceName + " unexpectedly already manages nodes");
        }
    }

    private void logNodeRecoverySummary(String nodeSourceName, Map<NodeState, Integer> nodeStates,
            int totalRecoveredNodes, int totalEligibleRecoveredNodes) {
        logger.info("Recovered nodes summary for node source " + nodeSourceName + ":");
        for (Map.Entry<NodeState, Integer> nodeStateIntEntry : nodeStates.entrySet()) {
            logger.info("- Nodes in " + nodeStateIntEntry.getKey() + " state: " + nodeStateIntEntry.getValue());
            totalRecoveredNodes += nodeStateIntEntry.getValue();
        }
        logger.info(END_OF_NODES_RECOVERY + totalRecoveredNodes + ", including eligible nodes: " +
                    totalEligibleRecoveredNodes); // this line is important for performance tests
    }

    /**
     * Add the information of the given node to the database.
     *
     * @param rmNode the node to add to the database
     */
    public void persistNewRMNodeIfRecoveryEnabled(RMNode rmNode) {
        if (nodesRecoveryEnabledForNode(rmNode)) {
            RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
            NodeSourceData nodeSourceData = dbManager.getNodeSource(rmNode.getNodeSourceName());
            rmNodeData.setNodeSource(nodeSourceData);
            dbManager.addNode(rmNodeData);
        }
    }

    /**
     * Update the information of the given node in database.
     *
     * @param rmNode the node to update in database
     */
    public void persistUpdatedRMNodeIfRecoveryEnabled(RMNode rmNode) {
        if (nodesRecoveryEnabledForNode(rmNode)) {
            RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
            dbManager.updateNode(rmNodeData);
        }
    }

    /**
     * Delete a given node in database.
     *
     * @param rmNode the node to update in database
     */
    public void persistDeletedRMNodeIfRecoveryEnabled(RMNode rmNode) {
        if (nodesRecoveryEnabledForNode(rmNode)) {
            RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
            dbManager.removeNode(rmNodeData);
        }
    }

    private boolean nodesRecoveryEnabled() {
        return PAResourceManagerProperties.RM_NODES_RECOVERY.getValueAsBoolean();
    }

    private boolean nodesRecoveryEnabledForNode(RMNode rmNode) {
        return nodesRecoveryEnabled() && rmNode.getNodeSource().nodesRecoverable();
    }

    private boolean isEligible(RMNode node) {
        // an eligible node should be in free state and should not be locked
        return node != null && node.isFree() && !node.isLocked();
    }

}
