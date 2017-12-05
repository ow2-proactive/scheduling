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
package org.ow2.proactive.resourcemanager.db;

import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.RM_NODES_DB_OPERATIONS_DELAY;
import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.RM_NODES_DB_SYNCHRONOUS_UPDATES;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.db.SessionWork;


/**
 * Methods of this class can be used to delay database operations in order to
 * batch them and reduce database hit overhead.
 */
public class RMDBManagerBuffer {

    private static final Logger logger = ProActiveLogger.getLogger(RMDBManagerBuffer.class);

    private RMDBManager rmdbManager;

    /**
     * This flag defines whether the database operations will be synchronous
     * or asynchronous.
     */
    private boolean delayEqualsToZero;

    /**
     * We need a single executor with a single thread because the operation on
     * nodes and node sources need ordering.
     */
    private ScheduledExecutorService databaseTransactionExecutor;

    /**
     * The set of node source names that have been added and not removed.
     */
    private Set<String> knownNodeSources;

    /**
     * The map of node sources to persist in database per node source name.
     * This represents one update to do per node source (i.e. the latest
     * state of the node source).
     */
    private Map<String, NodeSourceData> pendingNodeSourceUpdates;

    /**
     * The database transaction regarding node sources that is currently
     * scheduled for later.
     */
    private ScheduledFuture<?> scheduledNodeSourceTransaction;

    private final Lock pendingNodeSourceUpdatesLock = new ReentrantLock();

    /**
     * The set of nodes to persist in database. One entry of this map
     * corresponds to the successive updates to apply for one node.
     */
    private List<NodeOperation> pendingNodesOperations;

    /**
     * The database transaction regarding nodes that is currently
     * scheduled for later.
     */
    private ScheduledFuture<?> scheduledNodeTransaction;

    private final Lock pendingNodeOperationsLock = new ReentrantLock();

    private final Condition pendingNodeOperationsCondition = pendingNodeOperationsLock.newCondition();

    protected RMDBManagerBuffer(RMDBManager rmdbManager) {
        this.rmdbManager = rmdbManager;
        delayEqualsToZero = RM_NODES_DB_OPERATIONS_DELAY.getValueAsInt() == 0;
        databaseTransactionExecutor = Executors.newSingleThreadScheduledExecutor();
        pendingNodeSourceUpdates = new HashMap<>();
        pendingNodesOperations = new LinkedList<>();
        knownNodeSources = new HashSet<>();

        // populate the set of node source names that were existing in the
        // previous execution of the RM
        Collection<NodeSourceData> nodeSources = rmdbManager.getNodeSources();
        for (NodeSourceData nodeSource : nodeSources) {
            knownNodeSources.add(nodeSource.getName());
        }
    }

    ////// Node Source Database Operations //////

    protected void addKnownNodeSource(final String nodeSourceName) {
        pendingNodeSourceUpdatesLock.lock();
        try {
            knownNodeSources.add(nodeSourceName);
        } finally {
            pendingNodeSourceUpdatesLock.unlock();
        }
    }

    protected void removeKnownNodeSourceAndPendingUpdates(final String nodeSourceName) {
        pendingNodeSourceUpdatesLock.lock();
        try {
            knownNodeSources.remove(nodeSourceName);
            pendingNodeSourceUpdates.remove(nodeSourceName);
        } finally {
            pendingNodeSourceUpdatesLock.unlock();
        }
    }

    protected void addUpdateNodeSourceToPendingDatabaseOperations(final NodeSourceData nodeSource) {
        pendingNodeSourceUpdatesLock.lock();
        try {
            String nodeSourceName = nodeSource.getName();
            if (knownNodeSources.contains(nodeSourceName)) {
                cancelScheduledNodeSourceTransaction();
                pendingNodeSourceUpdates.put(nodeSourceName, nodeSource);
                if (delayEqualsToZero) {
                    buildNodeSourceTransactionAndCommit();
                } else {
                    scheduleNodeSourceTransaction();
                }
            }
        } finally {
            pendingNodeSourceUpdatesLock.unlock();
        }
    }

    private void cancelScheduledNodeSourceTransaction() {
        if (scheduledNodeSourceTransaction != null && !scheduledNodeSourceTransaction.isDone()) {
            scheduledNodeSourceTransaction.cancel(false);
        }
    }

    private void buildNodeSourceTransactionAndCommit() {
        rmdbManager.executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                for (NodeSourceData nodeSource : pendingNodeSourceUpdates.values()) {
                    if (knownNodeSources.contains(nodeSource.getName())) {
                        session.update(nodeSource);
                    }
                }
                pendingNodeSourceUpdates.clear();
                return null;
            }
        });
    }

    private void scheduleNodeSourceTransaction() {
        scheduledNodeSourceTransaction = databaseTransactionExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                pendingNodeSourceUpdatesLock.lock();
                try {
                    buildNodeSourceTransactionAndCommit();
                } finally {
                    pendingNodeSourceUpdatesLock.unlock();
                }
            }
        }, RM_NODES_DB_OPERATIONS_DELAY.getValueAsInt(), TimeUnit.MILLISECONDS);
    }

    ////// Node Database Operations //////

    protected boolean canOperateDatabaseSynchronouslyWithNode(RMNodeData rmNodeData) {
        boolean canOperateNodeSynchronously = true;
        if (!delayEqualsToZero) {
            pendingNodeOperationsLock.lock();
            try {
                canOperateNodeSynchronously = synchronousOperationsRequired() && !nodeHasPendingOperations(rmNodeData);
            } finally {
                pendingNodeOperationsLock.unlock();
            }
        }
        return canOperateNodeSynchronously;
    }

    protected boolean canOperateDatabaseSynchronouslyWithNodes(Collection<RMNodeData> nodes) {
        boolean canOperateAllNodesSynchronously = true;
        if (!delayEqualsToZero) {
            for (RMNodeData rmNodeData : nodes) {
                if (!canOperateDatabaseSynchronouslyWithNode(rmNodeData)) {
                    canOperateAllNodesSynchronously = false;
                    break;
                }
            }
        }
        return canOperateAllNodesSynchronously;
    }

    private boolean synchronousOperationsRequired() {
        return RM_NODES_DB_SYNCHRONOUS_UPDATES.getValueAsBoolean();
    }

    protected void addCreateNodeToPendingDatabaseOperations(RMNodeData rmNodeData) {
        cancelScheduledNodeTransaction();
        registerPendingNodeOperations(DatabaseOperation.CREATE, rmNodeData);
        if (delayEqualsToZero) {
            buildNodesTransactionAndCommit();
        } else {
            scheduleNodeTransaction();
        }
    }

    protected void addUpdateNodeToPendingDatabaseOperations(RMNodeData rmNodeData) {
        cancelScheduledNodeTransaction();
        registerPendingNodeOperations(DatabaseOperation.UPDATE, rmNodeData);
        if (delayEqualsToZero) {
            buildNodesTransactionAndCommit();
        } else {
            scheduleNodeTransaction();
        }
    }

    protected void addRemoveNodeToPendingDatabaseOperations(RMNodeData rmNodeData) {
        cancelScheduledNodeTransaction();
        registerPendingNodeOperations(DatabaseOperation.DELETE, rmNodeData);
        if (delayEqualsToZero) {
            buildNodesTransactionAndCommit();
        } else {
            scheduleNodeTransaction();
        }
    }

    protected void addRemoveNodesToPendingDatabaseOperations(Collection<RMNodeData> nodes) {
        cancelScheduledNodeTransaction();
        for (RMNodeData rmNodeData : nodes) {
            registerPendingNodeOperations(DatabaseOperation.DELETE, rmNodeData);
        }
        if (delayEqualsToZero) {
            buildNodesTransactionAndCommit();
        } else {
            scheduleNodeTransaction();
        }
    }

    protected void debounceNodeUpdatesIfNeeded() {
        if (!delayEqualsToZero) {
            pendingNodeOperationsLock.lock();
            try {
                while (!pendingNodesOperations.isEmpty()) {
                    try {
                        pendingNodeOperationsCondition.await();
                    } catch (InterruptedException e) {
                        logger.warn("Waiting of debouncing of database operations has been interrupted.");
                    }
                }
            } finally {
                pendingNodeOperationsLock.unlock();
            }
        }
    }

    public List<NodeOperation> listPendingNodeOperations() {
        List<NodeOperation> listCopy;
        pendingNodeOperationsLock.lock();
        try {
            listCopy = new LinkedList<>(pendingNodesOperations);
        } finally {
            pendingNodeOperationsLock.unlock();
        }
        return listCopy;
    }

    private void cancelScheduledNodeTransaction() {
        if (scheduledNodeTransaction != null && !scheduledNodeTransaction.isDone()) {
            scheduledNodeTransaction.cancel(false);
        }
    }

    private void registerPendingNodeOperations(DatabaseOperation databaseOperation, RMNodeData rmNodeData) {
        pendingNodeOperationsLock.lock();
        try {
            pendingNodesOperations.add(new NodeOperation(rmNodeData, databaseOperation));
        } finally {
            pendingNodeOperationsLock.unlock();
        }
    }

    private void scheduleNodeTransaction() {
        scheduledNodeTransaction = databaseTransactionExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                buildNodesTransactionAndCommit();
            }
        }, RM_NODES_DB_OPERATIONS_DELAY.getValueAsInt(), TimeUnit.MILLISECONDS);
    }

    private void buildNodesTransactionAndCommit() {
        pendingNodeOperationsLock.lock();
        try {
            List<NodeOperation> effectiveNodesOperations = extractOperationsOfNextTransaction();
            while (!effectiveNodesOperations.isEmpty()) {
                final List<NodeOperation> currentNodesOperations = effectiveNodesOperations;
                rmdbManager.executeReadWriteTransaction(new SessionWork<Void>() {
                    @Override
                    public Void doInTransaction(Session session) {
                        logger.debug("Executing database transaction with " + currentNodesOperations.size() +
                                     " operations");
                        try {
                            for (NodeOperation nodeOperation : currentNodesOperations) {
                                RMNodeData rmNodeData = nodeOperation.node;
                                DatabaseOperation databaseOperation = nodeOperation.operation;
                                switch (databaseOperation) {
                                    case CREATE:
                                        session.save(rmNodeData);
                                        logger.info("Adding a new node " + rmNodeData.getName() + " to the database");
                                        break;
                                    case UPDATE:
                                        session.update(rmNodeData);
                                        break;
                                    case DELETE:
                                        session.delete(rmNodeData);
                                        logger.info("Removing a node " + rmNodeData.getName() + " from the database");
                                        break;
                                    case RETRIEVE:
                                        // currently retrieval are not enqueued
                                        // and are executed after the barrier
                                        // set by debounceNodeUpdatesIfNeeded
                                    default:
                                        logger.warn("Database operation not supported");
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("Database operations could not be applied", e);
                        }
                        return null;
                    }
                });
                effectiveNodesOperations = extractOperationsOfNextTransaction();
            }
            // Pending node operations are cleared, retrieval can be enabled
            pendingNodeOperationsCondition.signalAll();
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while adding new node ", e);
        } finally {
            pendingNodeOperationsLock.unlock();
        }
    }

    private List<NodeOperation> extractOperationsOfNextTransaction() {
        Set<RMNodeData> nodesOfNextTransaction = new HashSet<>();
        List<NodeOperation> operationsOfNextTransaction = new LinkedList<>();
        Iterator<NodeOperation> pendingOperationsIterator = pendingNodesOperations.iterator();

        boolean canContinueAddingOperationsToTransaction = true;

        while (pendingOperationsIterator.hasNext() && canContinueAddingOperationsToTransaction) {
            NodeOperation nodeOperation = pendingOperationsIterator.next();
            canContinueAddingOperationsToTransaction = addOperationToNextTransactionIfPossible(nodesOfNextTransaction,
                                                                                               operationsOfNextTransaction,
                                                                                               pendingOperationsIterator,
                                                                                               nodeOperation);
        }
        return operationsOfNextTransaction;
    }

    private boolean addOperationToNextTransactionIfPossible(Set<RMNodeData> nodesOfNextTransaction,
            List<NodeOperation> operationsOfNextTransaction, Iterator<NodeOperation> pendingOperationsIterator,
            NodeOperation nodeOperation) {

        boolean operationAddedToNextTransaction = false;
        RMNodeData rmNodeData = nodeOperation.node;
        DatabaseOperation databaseOperation = nodeOperation.operation;

        if (!nodesOfNextTransaction.contains(rmNodeData)) {
            nodesOfNextTransaction.add(rmNodeData);
            operationsOfNextTransaction.add(new NodeOperation(rmNodeData, databaseOperation));
            pendingOperationsIterator.remove();
            operationAddedToNextTransaction = true;
        }
        return operationAddedToNextTransaction;
    }

    private boolean nodeHasPendingOperations(RMNodeData searchedNode) {
        for (NodeOperation nodeOperation : pendingNodesOperations) {
            if (searchedNode.equals(nodeOperation.node)) {
                return true;
            }
        }
        return false;
    }

    public enum DatabaseOperation {
        CREATE,
        RETRIEVE,
        UPDATE,
        DELETE
    }

    public static class NodeOperation {

        protected final RMNodeData node;

        protected final DatabaseOperation operation;

        protected NodeOperation(RMNodeData node, DatabaseOperation operation) {
            this.node = node;
            this.operation = operation;
        }
    }

}
