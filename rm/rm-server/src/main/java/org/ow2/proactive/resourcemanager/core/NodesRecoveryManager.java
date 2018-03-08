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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManagerFactory;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;

import com.google.common.base.Function;


/**
 * This class handles the recovery of node sources and of nodes when the RM is restarted.
 *
 * @author ActiveEon Team
 * @since 26/06/17
 */
public class NodesRecoveryManager {

    public static final String START_TO_RECOVER_NODES = "Start to recover nodes";

    public static final String END_OF_NODES_RECOVERY = "Total number of nodes recovered: ";

    private static final Logger logger = Logger.getLogger(NodesRecoveryManager.class);

    private RMCore rmCore;

    private NodesLockRestorationManager nodesLockRestorationManager;

    protected NodesRecoveryManager(RMCore rmCore) {
        this.rmCore = rmCore;
    }

    protected void initialize() {
        this.initNodesRestorationManager();
    }

    protected void initNodesRestorationManager() {
        this.nodesLockRestorationManager = this.getNodesLockRestorationManagerBuilder().apply(this.rmCore);

        if (PAResourceManagerProperties.RM_NODES_LOCK_RESTORATION.getValueAsBoolean()) {
            this.nodesLockRestorationManager.initialize();
        } else {
            logger.info("Nodes lock restoration is disabled");
        }
    }

    protected void restoreLock(RMNode rmNode, Client provider) {
        this.nodesLockRestorationManager.handle(rmNode, provider);
    }

    protected Function<RMCore, NodesLockRestorationManager> getNodesLockRestorationManagerBuilder() {
        return new Function<RMCore, NodesLockRestorationManager>() {
            @Override
            public NodesLockRestorationManager apply(RMCore rmCore) {
                return new NodesLockRestorationManager(rmCore);
            }
        };
    }

    protected void recoverNodeSourcesAndNodes() {
        Collection<NodeSourceData> nodeSources = this.rmCore.getDbManager().getNodeSources();
        this.logPersistedNodeSourceInfo(nodeSources);

        for (NodeSourceData nodeSourceData : nodeSources) {
            String nodeSourceName = nodeSourceData.getName();
            if (NodeSource.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME.equals(nodeSourceName)) {
                // will be recreated by SchedulerStarter
                this.rmCore.getDbManager().removeNodeSource(nodeSourceName);
            } else {
                this.recoverNodeSourceSuccessfullyOrRemove(nodeSourceData, nodeSourceName);
            }
        }
    }

    protected boolean recoverFullyDeployedInfrastructureOrReset(String nodeSourceName, NodeSource nodeSourceToDeploy,
            NodeSourceDescriptor descriptor) {
        boolean recoverNodes = false;
        boolean existPersistedNodes = this.existPersistedNodes(nodeSourceName);

        if (existPersistedNodes) {
            InfrastructureManager im = InfrastructureManagerFactory.recover(descriptor);
            if (!im.getDeployingAndLostNodes().isEmpty()) {
                // if there are deploying nodes, we will not recover
                this.rmCore.getDbManager().removeAllNodesFromNodeSource(nodeSourceName);
            } else {
                recoverNodes = true;
                nodeSourceToDeploy.setInfrastructureManager(im);
            }
        }
        return recoverNodes;
    }

    protected void recoverNodes(NodeSource nodeSource) {
        logger.info(START_TO_RECOVER_NODES); // this log line is important for performance tests
        int lookUpTimeout = PAResourceManagerProperties.RM_NODELOOKUP_TIMEOUT.getValueAsInt();
        String nodeSourceName = nodeSource.getName();
        this.logWarnIfNodeSourceHasNoNode(nodeSource, nodeSourceName);

        Collection<RMNodeData> nodesData = this.rmCore.getDbManager().getNodesByNodeSource(nodeSourceName);
        logger.info("Number of nodes found in database for node source " + nodeSourceName + ": " + nodesData.size());

        List<RMNode> recoveredEligibleNodes = Collections.synchronizedList(new ArrayList<RMNode>());
        Map<NodeState, Integer> recoveredNodeStatesCounter = new HashMap<>();

        // for each node found in database, try to lookup node or recover it
        // as down node
        for (RMNodeData rmNodeData : nodesData) {
            String nodeUrl = rmNodeData.getNodeUrl();

            Node node = this.tryToLookupNode(nodeSource, lookUpTimeout, nodeUrl);
            RMNode rmnode = this.recoverRMNode(nodeSource, recoveredNodeStatesCounter, rmNodeData, nodeUrl, node);

            if (this.isEligible(rmnode)) {
                recoveredEligibleNodes.add(rmnode);
            }
        }

        this.rmCore.setEligibleNodesToRecover(recoveredEligibleNodes);

        this.logNodeRecoverySummary(nodeSourceName, recoveredNodeStatesCounter, recoveredEligibleNodes.size());
    }

    private boolean existPersistedNodes(String nodeSourceName) {
        boolean existPersistedNodes = false;

        Collection<RMNodeData> nodesData = this.rmCore.getDbManager().getNodesByNodeSource(nodeSourceName);
        if (nodesData.isEmpty()) {
            logger.info("No node found in database for node source: " + nodeSourceName);
        } else {
            existPersistedNodes = true;
        }

        return existPersistedNodes;
    }

    private void recoverNodeSourceSuccessfullyOrRemove(NodeSourceData nodeSourceData, String nodeSourceName) {
        try {
            logger.info("Recover node source " + nodeSourceName);
            // retrieve node source status
            boolean deployNodeSource = false;
            if (nodeSourceData.getStatus().equals(NodeSourceStatus.NODES_DEPLOYED)) {
                // reset node source status to be able to deploy again
                nodeSourceData.setStatus(NodeSourceStatus.NODES_UNDEPLOYED);
                deployNodeSource = true;
            }
            this.rmCore.prepareNodeSource(nodeSourceData);
            if (deployNodeSource) {
                this.rmCore.deployNodeSource(nodeSourceName);
            }
        } catch (Throwable t) {
            logger.error("Failed to recover node source " + nodeSourceName, t);
            this.rmCore.removeNodeSource(nodeSourceName);
            this.rmCore.getDbManager().removeNodeSource(nodeSourceName);
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

    private RMNode recoverRMNode(NodeSource nodeSource, Map<NodeState, Integer> nodeStates, RMNodeData rmNodeData,
            String nodeUrl, Node node) {
        RMNode rmnode = null;
        if (node != null) {
            rmnode = this.recoverNodeInternally(nodeSource, rmNodeData, nodeUrl, node);
            this.nodesLockRestorationManager.handle(rmnode, rmNodeData.getProvider());
            this.updateRecoveredNodeStateCounter(nodeStates, rmnode.getState());
        } else {
            // the node is not recoverable and does not appear in any data
            // structures: we can remove it safely from database
            this.rmCore.getDbManager().removeNode(rmNodeData, rmNodeData.getNodeSource().getName());
            this.markNodesNotInDeployingStateAsDown(nodeSource, rmNodeData, nodeUrl);
            this.updateRecoveredNodeStateCounter(nodeStates, NodeState.DOWN);
        }
        return rmnode;
    }

    private void updateRecoveredNodeStateCounter(Map<NodeState, Integer> nodeStates, NodeState nodeState) {
        Integer previousCounter = nodeStates.get(nodeState);
        int updatedCounter;
        if (previousCounter == null) {
            updatedCounter = 1;
        } else {
            updatedCounter = previousCounter + 1;
        }
        nodeStates.put(nodeState, updatedCounter);
    }

    private RMNode recoverNodeInternally(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl, Node node) {
        RMNode rmNode = null;
        // the node has been successfully looked up, we compare its
        // information to the node data retrieved in database.
        if (rmNodeData.equalsToNode(node)) {
            logger.info("Node to recover could successfully be looked up at URL: " + nodeUrl);
            rmNode = nodeSource.internalAddNodeAfterRecovery(node, rmNodeData);
            this.rmCore.registerAvailableNode(rmNode);
        } else {
            logger.error("The node that has been looked up does not have the same information as the node to recover: " +
                         node.getNodeInformation().getName() + " is not equal to " + rmNodeData.getName() + " or " +
                         node.getNodeInformation().getURL() + " is not equal to " + rmNodeData.getNodeUrl());
        }
        return rmNode;
    }

    private void markNodesNotInDeployingStateAsDown(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl) {
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

    private void logWarnIfNodeSourceHasNoNode(NodeSource nodeSource, String nodeSourceName) {
        int nodesCount = nodeSource.getNodesCount();
        if (nodesCount != 0) {
            logger.warn("Recovered node source " + nodeSourceName + " unexpectedly already manages nodes");
        }
    }

    private void logNodeRecoverySummary(String nodeSourceName, Map<NodeState, Integer> nodeStates,
            int totalEligibleRecoveredNodes) {
        int totalRecoveredNodes = 0;
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
