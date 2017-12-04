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
 * It is used by {@link HostsFileBasedInfrastructureManager} and objects of
 * this class are persisted along with the node source data.
 *
 * All non-final fields of this class should be thread-safe.
 */
public class HostTracker implements Serializable {

    protected static final Logger logger = Logger.getLogger(HostTracker.class);

    private final String hostInFile;

    private final int configuredNodeNumber;

    private final InetAddress host;

    private Map<String, NodeStatus> nodeStatusPerNodeUrl;

    private AtomicInteger aliveNodeCounter;

    private AtomicInteger downNodeCounter;

    private AtomicInteger removedNodeCounter;

    private AtomicBoolean needNodesFlag;

    protected HostTracker(String hostInFile, int configuredNodeNumber, InetAddress host) {
        this.hostInFile = hostInFile;
        this.configuredNodeNumber = configuredNodeNumber;
        this.host = host;
        nodeStatusPerNodeUrl = new HashMap<>();
        aliveNodeCounter = new AtomicInteger(0);
        downNodeCounter = new AtomicInteger(0);
        removedNodeCounter = new AtomicInteger(0);
        // a newly created host tracker requires nodes because it has not
        // been deployed yet
        needNodesFlag = new AtomicBoolean(true);
    }

    protected String getHostInFile() {
        return hostInFile;
    }

    protected InetAddress getHost() {
        return host;
    }

    protected boolean getNeedNodesFlag() {
        return needNodesFlag.get();
    }

    protected void setNeedNodesFlag(boolean needNodesFlag) {
        this.needNodesFlag.set(needNodesFlag);
    }

    /**
     * Compute the number of nodes that are needed by this host at that
     * moment. The needed number of nodes is defined by the initially
     * configured number of hosts (written in the host file), minus the number
     * of alive nodes, and minus the number of removed nodes (since removed
     * nodes are the result of a remove action by the user, we should not
     * redeploy removed node).
     */
    protected int getNeededNodeNumber() {
        int neededNodeNumber = 0;
        if (needNodesFlag.get()) {
            int computedNeededNodeNumber = configuredNodeNumber - aliveNodeCounter.get() - removedNodeCounter.get();
            if (computedNeededNodeNumber > configuredNodeNumber) {
                neededNodeNumber = configuredNodeNumber;
                logger.warn("Computed needed node number " + computedNeededNodeNumber +
                            " is bigger than the configured number of nodes " + configuredNodeNumber + ". Requiring " +
                            neededNodeNumber + " nodes.");
            }
            if (computedNeededNodeNumber < 0) {
                neededNodeNumber = 0;
                logger.warn("Computed needed node number " + computedNeededNodeNumber +
                            " is negative. Requiring no nodes.");
            }
            if (computedNeededNodeNumber <= configuredNodeNumber && computedNeededNodeNumber >= 0) {
                neededNodeNumber = computedNeededNodeNumber;
            }
        }
        return neededNodeNumber;
    }

    protected boolean hasAliveNodes() {
        return aliveNodeCounter.get() > 0;
    }

    protected void putAliveNodeUrl(String aliveNodeUrl) {
        decrementCounterForPreviousStatus(aliveNodeUrl);
        nodeStatusPerNodeUrl.put(aliveNodeUrl, NodeStatus.ALIVE);
        aliveNodeCounter.incrementAndGet();
    }

    protected void putDownNodeUrl(String downNodeUrl) {
        decrementCounterForPreviousStatus(downNodeUrl);
        nodeStatusPerNodeUrl.put(downNodeUrl, NodeStatus.DOWN);
        downNodeCounter.incrementAndGet();
    }

    protected void putRemovedNodeUrl(String removedNodeUrl) {
        decrementCounterForPreviousStatus(removedNodeUrl);
        nodeStatusPerNodeUrl.put(removedNodeUrl, NodeStatus.REMOVED);
        removedNodeCounter.incrementAndGet();
    }

    private void decrementCounterForPreviousStatus(String aliveNodeUrl) {
        NodeStatus previousStatus = nodeStatusPerNodeUrl.get(aliveNodeUrl);
        if (previousStatus != null) {
            findCounterForStatus(previousStatus).decrementAndGet();
        }
    }

    private AtomicInteger findCounterForStatus(NodeStatus nodeStatus) {
        switch (nodeStatus) {
            case ALIVE:
                return aliveNodeCounter;
            case DOWN:
                return downNodeCounter;
            case REMOVED:
                return removedNodeCounter;
            default:
                throw new IllegalStateException("Searched node status " + nodeStatus + " is none of the following: " +
                                                Arrays.toString(NodeStatus.values()));
        }
    }

    @Override
    public String toString() {
        String minimalInfo = "Host " + hostInFile + ". Configured with " + configuredNodeNumber +
                             " nodes. Current state: [alive node number=" + aliveNodeCounter.get() +
                             ", down node number=" + downNodeCounter.get() + ", removed node number=" +
                             removedNodeCounter.get() + "].";
        if (logger.isDebugEnabled()) {
            List<String> allAliveNodeUrls = findAllNodeUrlsWithStatus(NodeStatus.ALIVE);
            List<String> allDownNodeUrls = findAllNodeUrlsWithStatus(NodeStatus.DOWN);
            List<String> allRemovedNodeUrls = findAllNodeUrlsWithStatus(NodeStatus.REMOVED);
            return minimalInfo + " Alive node URLs (" + allAliveNodeUrls.size() + "): " +
                   Arrays.toString(allAliveNodeUrls.toArray()) + ". Down node URLs (" + allDownNodeUrls.size() + "): " +
                   Arrays.toString(allDownNodeUrls.toArray()) + ". Removed node URLs (" + allRemovedNodeUrls.size() +
                   "): " + Arrays.toString(allRemovedNodeUrls.toArray()) + ".";
        } else {
            return minimalInfo;
        }
    }

    private List<String> findAllNodeUrlsWithStatus(NodeStatus status) {
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

        REMOVED

    }

}
