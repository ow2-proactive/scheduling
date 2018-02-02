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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;


/**
 * This class tracks the information related to node deployment on a host.
 * It is used by {@link HostsFileBasedInfrastructureManager} and HostTracker
 * instances are persisted along with the node source data.
 *
 * All non-final fields of this class should be thread-safe.
 */
public class HostTracker implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static final Logger logger = Logger.getLogger(HostTracker.class);

    private final String configuredAddress;

    private final int configuredNodeNumber;

    private final InetAddress resolvedAddress;

    private Map<String, NodeStatus> nodeStatusPerNodeUrl;

    private AtomicInteger aliveNodesCounter;

    private AtomicInteger downNodesCounter;

    private AtomicInteger removedNodesCounter;

    private AtomicBoolean needsNodesFlag;

    protected HostTracker(String configuredAddress, int configuredNodeNumber, InetAddress resolvedAddress) {
        this.configuredAddress = configuredAddress;
        this.configuredNodeNumber = configuredNodeNumber;
        this.resolvedAddress = resolvedAddress;
        nodeStatusPerNodeUrl = new HashMap<>();
        aliveNodesCounter = new AtomicInteger(0);
        downNodesCounter = new AtomicInteger(0);
        removedNodesCounter = new AtomicInteger(0);
        // a newly created host tracker requires nodes because it has not
        // been deployed yet
        needsNodesFlag = new AtomicBoolean(true);
    }

    /**
     * @return the host as it is written in the hosts file supplied by the user
     */
    protected String getConfiguredAddress() {
        return configuredAddress;
    }

    /**
     * @return the {@link InetAddress} representation of the host
     */
    protected InetAddress getResolvedAddress() {
        return resolvedAddress;
    }

    /**
     * @return whether this host requires nodes
     */
    protected boolean needsNodes() {
        return needsNodesFlag.get();
    }

    /**
     * Say whether this host requires nodes.
     */
    protected void setNeedsNodes(boolean needNodesFlag) {
        this.needsNodesFlag.set(needNodesFlag);
    }

    /**
     * Compute the number of nodes that are needed by this host at that
     * moment. The needed number of nodes is defined by the initially
     * configured number of hosts (written in the host file), minus the number
     * of alive nodes, and minus the number of removed nodes (since removed
     * nodes are the result of a remove action by the user, we should not
     * redeploy removed node).
     */
    protected int getNeededNodesNumber() {
        int neededNodesNumber = 0;
        if (needsNodesFlag.get()) {
            int computedNeededNodeNumber = configuredNodeNumber - aliveNodesCounter.get() - removedNodesCounter.get();
            if (computedNeededNodeNumber > configuredNodeNumber) {
                neededNodesNumber = configuredNodeNumber;
                logger.warn("Computed needed node number " + computedNeededNodeNumber +
                            " is bigger than the configured number of nodes " + configuredNodeNumber + ". Requiring " +
                            neededNodesNumber + " nodes.");
            } else {
                if (computedNeededNodeNumber < 0) {
                    neededNodesNumber = 0;
                    logger.warn("Computed needed node number " + computedNeededNodeNumber +
                                " is negative. Requiring no nodes.");
                } else {
                    neededNodesNumber = computedNeededNodeNumber;
                }
            }
        }
        return neededNodesNumber;
    }

    /**
     * @return whether the host still has nodes alive
     */
    protected boolean hasAliveNodes() {
        return aliveNodesCounter.get() > 0;
    }

    /**
     * Registers the URL of an alive node and increment the alive node counter.
     */
    protected void putAliveNodeUrl(String aliveNodeUrl) {
        decrementPreviousNodesCounter(aliveNodeUrl);
        nodeStatusPerNodeUrl.put(aliveNodeUrl, NodeStatus.ALIVE);
        aliveNodesCounter.incrementAndGet();
    }

    /**
     * Registers the URL of a down node and increment the down node counter.
     */
    protected void putDownNodeUrl(String downNodeUrl) {
        decrementPreviousNodesCounter(downNodeUrl);
        nodeStatusPerNodeUrl.put(downNodeUrl, NodeStatus.DOWN);
        downNodesCounter.incrementAndGet();
    }

    /**
     * Registers the URL of a removed node and increment the removed node
     * counter.
     */
    protected void putRemovedNodeUrl(String removedNodeUrl) {
        decrementPreviousNodesCounter(removedNodeUrl);
        nodeStatusPerNodeUrl.put(removedNodeUrl, NodeStatus.REMOVED);
        removedNodesCounter.incrementAndGet();
    }

    private void decrementPreviousNodesCounter(String nodeUrl) {
        NodeStatus previousStatus = nodeStatusPerNodeUrl.get(nodeUrl);
        if (previousStatus != null) {
            findNodesCounter(previousStatus).decrementAndGet();
        }
    }

    private AtomicInteger findNodesCounter(NodeStatus nodeStatus) {
        switch (nodeStatus) {
            case ALIVE:
                return aliveNodesCounter;
            case DOWN:
                return downNodesCounter;
            case REMOVED:
                return removedNodesCounter;
            default:
                throw new IllegalStateException("Searched node status " + nodeStatus + " is none of the following: " +
                                                Arrays.toString(NodeStatus.values()));
        }
    }

    @Override
    public String toString() {
        String minimalInfo = "Host " + configuredAddress + ". Configured with " + configuredNodeNumber +
                             " nodes. Current state: [alive node number=" + aliveNodesCounter.get() +
                             ", down node number=" + downNodesCounter.get() + ", removed node number=" +
                             removedNodesCounter.get() + "].";
        if (logger.isDebugEnabled()) {
            List<String> allAliveNodeUrls = listNodesUrlWithStatus(NodeStatus.ALIVE);
            List<String> allDownNodeUrls = listNodesUrlWithStatus(NodeStatus.DOWN);
            List<String> allRemovedNodeUrls = listNodesUrlWithStatus(NodeStatus.REMOVED);
            return minimalInfo + " Alive node URLs (" + allAliveNodeUrls.size() + "): " +
                   Arrays.toString(allAliveNodeUrls.toArray()) + ". Down node URLs (" + allDownNodeUrls.size() + "): " +
                   Arrays.toString(allDownNodeUrls.toArray()) + ". Removed node URLs (" + allRemovedNodeUrls.size() +
                   "): " + Arrays.toString(allRemovedNodeUrls.toArray()) + ".";
        } else {
            return minimalInfo;
        }
    }

    private List<String> listNodesUrlWithStatus(NodeStatus status) {
        List<String> nodeUrlsWithStatus = new LinkedList<>();
        for (Map.Entry<String, NodeStatus> entry : nodeStatusPerNodeUrl.entrySet()) {
            if (entry.getValue().equals(status)) {
                nodeUrlsWithStatus.add(entry.getKey());
            }
        }
        return nodeUrlsWithStatus;
    }

    private enum NodeStatus {

        ALIVE,

        DOWN,

        REMOVED;

    }

}
