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
package org.ow2.proactive.resourcemanager.core.recovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.pamr.client.Agent;
import org.objectweb.proactive.extensions.pamr.remoteobject.PAMRRemoteObjectFactory;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMNodeData;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManagerFactory;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.utils.PAExecutors;

import com.google.common.util.concurrent.Uninterruptibles;


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

    public NodesRecoveryManager(RMCore rmCore) {
        this.rmCore = rmCore;
    }

    public void initialize() {
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

    public void restoreLock(RMNode rmNode, Client provider) {
        this.nodesLockRestorationManager.handle(rmNode, provider);
    }

    protected Function<RMCore, NodesLockRestorationManager> getNodesLockRestorationManagerBuilder() {
        return NodesLockRestorationManager::new;
    }

    public void recoverNodeSourcesAndNodes() {
        Collection<NodeSourceData> nodeSources = this.rmCore.getDbManager().getNodeSources();
        this.logPersistedNodeSourceInfo(nodeSources);

        for (NodeSourceData nodeSourceData : nodeSources) {
            String nodeSourceName = nodeSourceData.getName();
            if (RMConstants.DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME.equals(nodeSourceName)) {
                // will be recreated by SchedulerStarter
                this.rmCore.getDbManager().removeNodeSource(nodeSourceName);
            } else {
                this.recoverNodeSourceSuccessfullyOrRemove(nodeSourceData, nodeSourceName);
            }
        }
    }

    public boolean recoverFullyDeployedInfrastructureOrReset(String nodeSourceName, NodeSource nodeSourceToDeploy,
            NodeSourceDescriptor descriptor) {
        boolean recoverNodes = false;
        boolean existPersistedNodes = this.existPersistedNodes(nodeSourceName);

        if (existPersistedNodes) {
            InfrastructureManager im = InfrastructureManagerFactory.recover(descriptor);
            if (!im.getDeployingAndLostNodes().isEmpty()) {
                logRecoveryAbortedReason(nodeSourceName, "There were deploying or lost nodes");
                this.rmCore.getDbManager().removeAllNodesFromNodeSource(nodeSourceName);
            } else {
                recoverNodes = true;
                nodeSourceToDeploy.setInfrastructureManager(im);
            }
        } else {
            logRecoveryAbortedReason(nodeSourceName, "This node source has no associated nodes in database");
        }
        return recoverNodes;
    }

    public void recoverNodes(NodeSource nodeSource) {
        logger.info(START_TO_RECOVER_NODES); // this log line is important for performance tests
        String nodeSourceName = nodeSource.getName();
        Collection<RMNodeData> nodesData = this.rmCore.getDbManager().getNodesByNodeSource(nodeSourceName);
        logger.info("Number of nodes found in database for node source " + nodeSourceName + ": " + nodesData.size());

        List<RMNode> recoveredEligibleNodes = Collections.synchronizedList(new ArrayList<>());
        Map<NodeState, Integer> recoveredNodeStatesCounter = new HashMap<>();
        // for each node found in database, try to lookup node or recover it
        // as down node
        ExecutorService nodeRecoveryThreadPool = PAExecutors.newCachedBoundedThreadPool(1,
                                                                                        PAResourceManagerProperties.RM_NODESOURCE_MAX_THREAD_NUMBER.getValueAsInt(),
                                                                                        120L,
                                                                                        TimeUnit.SECONDS,
                                                                                        new NamedThreadFactory("NodeRecoveryThreadPool"));
        List<Future<RMNode>> nodesFutures = new ArrayList<>(nodesData.size());
        for (RMNodeData rmNodeData : nodesData) {
            nodesFutures.add(nodeRecoveryThreadPool.submit(() -> this.recoverNode(rmNodeData,
                                                                                  nodeSource,
                                                                                  recoveredNodeStatesCounter)));
        }
        for (Future<RMNode> rmNodeFuture : nodesFutures) {
            RMNode node = null;
            try {
                node = rmNodeFuture.get();
            } catch (Exception e) {
                logger.error("Unexpected error occurred while recovering node source " + nodeSource.getName(), e);
                nodeRecoveryThreadPool.shutdownNow();
                return;
            }
            if (this.isEligible(node)) {
                recoveredEligibleNodes.add(node);
            }
            if (node != null) {
                final RMNodeEvent event = node.createNodeEvent(RMEventType.NODE_ADDED,
                                                               null,
                                                               node.getProvider().getName());
                this.rmCore.registerAndEmitNodeEvent(event);
            }
        }
        nodeRecoveryThreadPool.shutdownNow();
        this.rmCore.addEligibleNodesToRecover(recoveredEligibleNodes);
        this.logNodeRecoverySummary(nodeSourceName, recoveredNodeStatesCounter, recoveredEligibleNodes.size());
    }

    public void logRecoveryAbortedReason(String nodeSourceName, String reason) {
        logger.info("Not recovering node source " + nodeSourceName + ": " + reason);
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
            this.rmCore.recoverNodeSource(nodeSourceData.toNodeSourceDescriptor());
        } catch (Throwable t) {
            logger.fatal("Failed to recover node source " + nodeSourceName, t);
            System.err.println("Failed to recover node source " + nodeSourceName + ", see logs for more details.");
            try {
                this.rmCore.removeNodeSource(nodeSourceName, true);
            } catch (Exception e) {
                // exception can occur if the node was not at all registered in the resource manager, print the exception as debug only
                logger.debug("Could not remove recovered node source", e);
            }
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

    private synchronized RMNode addRMNodeToCoreAndSource(NodeSource nodeSource, Map<NodeState, Integer> nodeStates,
            RMNodeData rmNodeData, String nodeUrl, Node node, NodeState previousState) {
        RMNode rmNode = nodeSource.internalAddNodeAfterRecovery(node, rmNodeData);
        boolean tokenInNodeSource = nodeSource.getNodeUserAccessType().getTokens() != null &&
                                    nodeSource.getNodeUserAccessType().getTokens().length > 0;
        boolean tokenInNode = false;
        this.rmCore.registerAvailableNode(rmNode);
        if (!(node instanceof FakeDownNodeForRecovery)) {
            try {
                String nodeAccessToken = node.getProperty(RMNodeStarter.NODE_ACCESS_TOKEN);
                tokenInNode = nodeAccessToken != null && nodeAccessToken.length() > 0;

                if (tokenInNode) {
                    logger.debug("Node " + node.getNodeInformation().getURL() + " is protected by access token " +
                                 nodeAccessToken);
                }
            } catch (Exception e) {
                throw new AddingNodesException(e);
            }
            try {
                RMCore.topologyManager.addNode(rmNode.getNode());
            } catch (Exception e) {
                logger.error("Error occurred when adding recovered node to the topology", e);
            }
            this.nodesLockRestorationManager.handle(rmNode, rmNodeData.getProvider());
        } else {
            this.nodesLockRestorationManager.handle(rmNode, rmNodeData.getProvider());
            logger.info("Triggering down node notification for " + nodeUrl);
            this.triggerDownNodeHookIfNecessary(nodeSource, rmNodeData, nodeUrl, previousState);
        }
        rmNode.setProtectedByToken(tokenInNode || tokenInNodeSource);
        this.updateRecoveredNodeStateCounter(nodeStates, rmNode.getState());
        return rmNode;
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

    private void triggerDownNodeHookIfNecessary(NodeSource nodeSource, RMNodeData rmNodeData, String nodeUrl,
            NodeState previousState) {
        // if the node to recover was in deploying state then we have
        // nothing to do as it is going to be redeployed
        if (!rmNodeData.getState().equals(NodeState.DEPLOYING)
        // if previous state was down, we don't need to trigger the down hook
        // once again
            && !previousState.equals(NodeState.DOWN)) {
            // inform the node source that this recreated node is down
            nodeSource.detectedPingedDownNode(rmNodeData.getName(), nodeUrl);
        }
    }

    private RMNode recoverNode(RMNodeData rmNodeData, NodeSource nodeSource,
            Map<NodeState, Integer> recoveredNodeStatesCounter) {
        Node node = null;
        String nodeUrl = rmNodeData.getNodeUrl();
        NodeState previousState = rmNodeData.getState();
        boolean isPAMR = nodeUrl.startsWith(PAMRRemoteObjectFactory.PROTOCOL_ID + "://");
        boolean connected = false;
        long initialTime = System.currentTimeMillis();
        do {
            try {
                node = nodeSource.lookupNode(nodeUrl,
                                             PAResourceManagerProperties.RM_NODELOOKUP_TIMEOUT.getValueAsInt());
                logger.info("Node " + nodeUrl + " was looked up successfully");
                connected = true;
            } catch (Exception e) {
                if (isPAMR) {
                    logger.debug("Node " + nodeUrl +
                                 " could not be looked up. Wait for PAMR agent reconnection delay.");
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                } else {
                    logger.error("Node " + nodeUrl + " could not be looked up.");
                }

            }
        } while (!connected && isPAMR && (System.currentTimeMillis() - initialTime) < Agent.MAXIMUM_RETRY_DELAY_MS);

        if (!connected) {
            node = new FakeDownNodeForRecovery(rmNodeData.getName(), rmNodeData.getNodeUrl(), rmNodeData.getHostname());
            rmNodeData.setState(NodeState.DOWN);
            if (isPAMR) {
                logger.error("Node " + nodeUrl + " could not be looked up.");
            }
        }

        return this.addRMNodeToCoreAndSource(nodeSource,
                                             recoveredNodeStatesCounter,
                                             rmNodeData,
                                             nodeUrl,
                                             node,
                                             previousState);
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
