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

import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.RM_NODES_LOCK_RESTORATION;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMNodeData;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceStatus;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;

import com.google.common.base.Function;


/**
 * This classe handles the recovery of node sources and of nodes when the RM is restarted.
 *
 * @author ActiveEon Team
 * @since 26/06/17
 */
public class NodesRecoveryManager {

    private static final Logger logger = Logger.getLogger(NodesRecoveryManager.class);

    public static final String START_TO_RECOVER_NODES = "Start to recover nodes";

    public static final String END_OF_NODES_RECOVERY = "Total number of nodes recovered: ";

    private RMCore rmCore;

    private NodesLockRestorationManager nodesLockRestorationManager;

    public NodesRecoveryManager(RMCore rmCore) {
        this.rmCore = rmCore;
    }

    public void initialize() {
        initNodesRestorationManager();
    }

    void initNodesRestorationManager() {
        nodesLockRestorationManager = getNodesLockRestorationManagerBuilder().apply(rmCore);

        if (RM_NODES_LOCK_RESTORATION.getValueAsBoolean()) {
            nodesLockRestorationManager.initialize();
        } else {
            logger.info("Nodes lock restoration is disabled");
        }
    }

    public void restoreLock(RMNode rmNode, Client provider) {
        nodesLockRestorationManager.handle(rmNode, provider);
    }

    Function<RMCore, NodesLockRestorationManager> getNodesLockRestorationManagerBuilder() {
        return new Function<RMCore, NodesLockRestorationManager>() {
            @Override
            public NodesLockRestorationManager apply(RMCore rmCore) {
                return new NodesLockRestorationManager(rmCore);
            }
        };
    }

    public void recoverNodeSourcesAndNodes() {
        Collection<NodeSourceData> nodeSources = rmCore.getDbManager().getNodeSources();
        logPersistedNodeSourceInfo(nodeSources);
        recoverDefinedNodeSources(nodeSources);

        for (NodeSourceDescriptor nodeSourceDescriptor : rmCore.getDefinedNodeSourceDescriptors()) {
            String nodeSourceName = nodeSourceDescriptor.getName();
            if (NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME.equals(nodeSourceName)) {
                // will be recreated by SchedulerStarter
                rmCore.removeDefinedNodeSource(nodeSourceName);
                rmCore.getDbManager().removeNodeSource(nodeSourceName);
            } else {
                recoverDeployedNodeSourceIfNeeded(nodeSourceDescriptor, nodeSourceName);
            }
        }
    }

    private void recoverDeployedNodeSourceIfNeeded(NodeSourceDescriptor nodeSourceDescriptor, String nodeSourceName) {
        try {
            logger.info("Recover node source " + nodeSourceName);
            if (nodeSourceDescriptor.getStatus().equals(NodeSourceStatus.DEPLOYED)) {
                rmCore.deployNodeSource(nodeSourceName);
            }
        } catch (Throwable t) {
            logger.error("Failed to recover node source " + nodeSourceName, t);
            rmCore.removeDefinedNodeSource(nodeSourceName);
            rmCore.getDbManager().removeNodeSource(nodeSourceName);
        }
    }

    private void recoverDefinedNodeSources(Collection<NodeSourceData> persistedNodeSources) {
        for (NodeSourceData persistedNodeSource : persistedNodeSources) {
            rmCore.addDefinedNodeSource(persistedNodeSource.toNodeSourceDescriptor());
        }
    }

    private void logPersistedNodeSourceInfo(Collection<NodeSourceData> nodeSources) {
        if (nodeSources.isEmpty()) {
            logger.info("No node source found in database");
        } else {
            if (nodeSources.size() < 10) {
                logger.info("Node sources found in database: " + Arrays.toString(nodeSources.toArray()));
            } else {
                logger.info("Number of node sources found in database: " + nodeSources.size());
            }
        }
    }

    public void recoverNodes(NodeSource nodeSource) {
        logger.info(START_TO_RECOVER_NODES); // this log line is important for performance tests
        int lookUpTimeout = PAResourceManagerProperties.RM_NODELOOKUP_TIMEOUT.getValueAsInt();
        String nodeSourceName = nodeSource.getName();

        makeSureNodeSourceHasNoNode(nodeSource, nodeSourceName);

        Collection<RMNodeData> nodesData = rmCore.getDbManager().getNodesByNodeSource(nodeSourceName);
        logger.info("Number of nodes found in database for node source " + nodeSourceName + ": " + nodesData.size());

        Map<NodeState, Integer> nodeStates = new HashMap<>();
        int totalEligibleRecoveredNodes = 0;

        // for each node found in database, try to lookup node or recover it
        // as down node
        for (RMNodeData rmNodeData : nodesData) {
            String nodeUrl = rmNodeData.getNodeUrl();
            RMNode rmnode = null;

            Node node = tryToLookupNode(nodeSource, lookUpTimeout, nodeUrl);

            if (node != null) {
                rmnode = recoverNodeInternally(nodeSource, rmNodeData, nodeUrl, node);
                nodesLockRestorationManager.handle(rmnode, rmNodeData.getProvider());
                Integer nbNodesInState = nodeStates.get(rmnode.getState());
                int newNbNodesInState = nbNodesInState == null ? 1 : nbNodesInState + 1;
                nodeStates.put(rmnode.getState(), newNbNodesInState);
            } else {
                // the node is not recoverable and does not appear in any data
                // structures: we can remove it safely from database
                rmCore.getDbManager().removeNode(rmNodeData);
                triggerDownNodeHandling(nodeSource, rmNodeData, nodeUrl);
            }
            // we must add the recreated node to the eligible data
            // structure if we want it to be usable by a task
            if (isEligible(rmnode)) {
                rmCore.addEligibleNode(rmnode);
                totalEligibleRecoveredNodes++;
            }
        }

        int totalRecoveredNodes = 0;

        logNodeRecoverySummary(nodeSourceName, nodeStates, totalRecoveredNodes, totalEligibleRecoveredNodes);
    }

    private RMNode recoverNodeInternally(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl, Node node) {
        RMNode rmNode = null;
        // the node has been successfully looked up, we compare its
        // information to the node data retrieved in database.
        if (rmNodeData.equalsToNode(node)) {
            logger.info("Node to recover could successfully be looked up at URL: " + nodeUrl);
            rmNode = nodeSource.internalAddNodeAfterRecovery(node, rmNodeData);
            rmCore.registerAvailableNode(rmNode);
        } else {
            logger.error("The node that has been looked up does not have the same information as the node to recover: " +
                         node.getNodeInformation().getName() + " is not equal to " + rmNodeData.getName() + " or " +
                         node.getNodeInformation().getURL() + " is not equal to " + rmNodeData.getNodeUrl());
        }
        return rmNode;
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

    private boolean isEligible(RMNode node) {
        return node != null && node.isFree() && !node.isLocked();
    }

}
