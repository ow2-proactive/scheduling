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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.MutableInteger;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;


/**
 * Encapsulates the logic about Nodes lock restoration upon Resource Manager restart.
 * <p>
 * It was decided to keep the mechanism simple by considering the node source name only
 * when the lock restoration is made. The hostname does not enter into account.
 * <p>
 * Considering the hostname is non-trivial since it is not stable. It may change on
 * machine restart or due to node state change. Besides, some infrastructures are
 * dynamics and the number of nodes deployed from a restart to another may differ.
 * <p>
 * This class is not thread-safe.
 *
 * @author ActiveEon Team
 * @since 01/02/17
 */
public class NodesLockRestorationManager {

    private static final Logger log = Logger.getLogger(NodesLockRestorationManager.class);

    private final RMCore rmCore;

    protected Map<String, MutableInteger> nodeLockedOnPreviousRun;

    protected boolean initialized;

    NodesLockRestorationManager(RMCore rmCore) {
        this.nodeLockedOnPreviousRun = Maps.newHashMap();
        this.rmCore = rmCore;
    }

    protected void initialize() {
        Stopwatch stopwatch = null;

        if (log.isInfoEnabled()) {
            stopwatch = Stopwatch.createStarted();
        }

        nodeLockedOnPreviousRun = findNodesLockedOnPreviousRun();

        if (log.isInfoEnabled()) {
            stopwatch.stop();
            log.info("Identifying nodes locked on the previous run required " +
                     stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
        }

        if (nodeLockedOnPreviousRun.isEmpty()) {
            log.info("There is no locks to restore");
        } else {
            log.info("Here is the number of nodes to lock per node source:");

            for (Map.Entry<String, MutableInteger> entry : nodeLockedOnPreviousRun.entrySet()) {
                log.info("  - nodeSource=" + entry.getKey() + ", host=" + entry.getKey() + ", count=" +
                         entry.getValue().getValue());
            }
        }

        initialized = true;
    }

    Map<String, MutableInteger> findNodesLockedOnPreviousRun() {
        RMDBManager dbManager = rmCore.getDbManager();
        Map<String, MutableInteger> nodesLockedOnPreviousRun = dbManager.findNodesLockedOnPreviousRun();
        dbManager.clearLockHistory();
        return nodesLockedOnPreviousRun;
    }

    Map<String, MutableInteger> getNodeLockedOnPreviousRun() {
        return nodeLockedOnPreviousRun;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Handle the specified node for lock restoration.
     *
     * @param node the node to consider.
     */
    public void handle(RMNode node) {

        if (!isNodeValidToBeRestored(node)) {
            if (log.isDebugEnabled()) {
                String nodeUrl = node.getNodeURL();

                if (!initialized) {
                    logSkipReason(nodeUrl, "manager is not yet initialized");
                } else if (node.isLocked()) {
                    logSkipReason(nodeUrl, "it is locked");
                } else {
                    logSkipReason(nodeUrl, "restoration is complete");
                }
            }

            return;
        }

        String nodeSource = node.getNodeSourceName();

        MutableInteger nodeCount = nodeLockedOnPreviousRun.get(nodeSource);

        if (nodeCount != null) {

            lockNode(node);

            int newNodeCount = nodeCount.add(-1);

            if (newNodeCount == 0) {
                nodeLockedOnPreviousRun.remove(nodeSource);
            }
        }
    }

    private boolean isNodeValidToBeRestored(RMNode node) {
        return initialized && !node.isLocked() && !isRestorationCompleted();
    }

    private void logSkipReason(String nodeUrl, String reason) {
        log.info("Node '" + nodeUrl + "' skipped because " + reason);
    }

    boolean isRestorationCompleted() {
        return initialized && nodeLockedOnPreviousRun.isEmpty();
    }

    boolean lockNode(RMNode node) {
        String nodeUrl = node.getNodeURL();

        if (rmCore.lockNodes(ImmutableSet.of(nodeUrl)).getBooleanValue()) {
            log.info("Node '" + nodeUrl + "' has been locked with success");
            return true;
        } else {
            log.info("Locking '" + nodeUrl + "' has failed");
            return false;
        }
    }

}
