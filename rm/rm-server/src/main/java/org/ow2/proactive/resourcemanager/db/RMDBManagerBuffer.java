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

import java.util.*;
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
     * The set of node sources to persist in database. This represents one
     * update to do per node source (i.e. the latest state of the node source).
     */
    private Set<NodeSourceData> pendingNodeSourceUpdates;

    private ScheduledFuture<?> scheduledNodeSourceTransaction;

    private final Lock pendingNodeSourceUpdatesLock = new ReentrantLock();

    /**
     * The set of nodes to persist in database. One entry of this map
     * corresponds to the successive updates to apply for one node.
     */
    private List<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> pendingNodesOperations;

    private Map<RMNodeData, Integer> nbPendingOperationsPerNode = new HashMap<>();

    private ScheduledFuture<?> scheduledNodeTransaction;

    private final Lock pendingNodeOperationsLock = new ReentrantLock();

    private final Condition pendingNodeOperationsCondition = pendingNodeOperationsLock.newCondition();

    public RMDBManagerBuffer(RMDBManager rmdbManager) {
        this.rmdbManager = rmdbManager;
        delayEqualsToZero = RM_NODES_DB_OPERATIONS_DELAY.getValueAsInt() == 0;
        databaseTransactionExecutor = Executors.newSingleThreadScheduledExecutor();
        pendingNodeSourceUpdates = new HashSet<>();
        pendingNodesOperations = new LinkedList<>();
    }

    ////// Node Source Database Operations //////

    public void addUpdateNodeSourceToPendingDatabaseOperations(final NodeSourceData nodeSourceData) {
        cancelScheduledNodeSourceTransaction();
        registerPendingNodeSourceUpdate(nodeSourceData);
        if (delayEqualsToZero) {
            buildNodeSourceTransactionAndCommit();
        } else {
            scheduleNodeSourceTransaction();
        }
    }

    private void cancelScheduledNodeSourceTransaction() {
        if (scheduledNodeSourceTransaction != null && !scheduledNodeSourceTransaction.isDone()) {
            scheduledNodeSourceTransaction.cancel(false);
        }
    }

    private void registerPendingNodeSourceUpdate(NodeSourceData nodeSourceData) {
        pendingNodeSourceUpdatesLock.lock();
        try {
            pendingNodeSourceUpdates.add(nodeSourceData);
        } finally {
            pendingNodeSourceUpdatesLock.unlock();
        }
    }

    private void buildNodeSourceTransactionAndCommit() {
        try {
            rmdbManager.executeReadWriteTransaction(new SessionWork<Void>() {
                @Override
                public Void doInTransaction(Session session) {
                    pendingNodeSourceUpdatesLock.lock();
                    try {
                        for (NodeSourceData nodeSource : pendingNodeSourceUpdates) {
                            session.update(nodeSource);
                        }
                        pendingNodeSourceUpdates.clear();
                    } finally {
                        pendingNodeSourceUpdatesLock.unlock();
                    }
                    return null;
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while updating node sources", e);
        }
    }

    private void scheduleNodeSourceTransaction() {
        scheduledNodeSourceTransaction = databaseTransactionExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                buildNodeSourceTransactionAndCommit();
            }
        }, RM_NODES_DB_OPERATIONS_DELAY.getValueAsInt(), TimeUnit.MILLISECONDS);
    }

    ////// Node Database Operations //////

    public boolean canOperateDatabaseSynchronouslyWithNode(RMNodeData rmNodeData) {
        boolean canOperateNodeSynchronously = true;
        if (!delayEqualsToZero) {
            pendingNodeOperationsLock.lock();
            try {
                canOperateNodeSynchronously = synchronousOperationsRequired() &&
                                              !nbPendingOperationsPerNode.containsKey(rmNodeData);
            } finally {
                pendingNodeOperationsLock.unlock();
            }
        }
        return canOperateNodeSynchronously;
    }

    public boolean canOperateDatabaseSynchronouslyWithNodes(Collection<RMNodeData> nodes) {
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

    public void addCreateNodeToPendingDatabaseOperations(RMNodeData rmNodeData) {
        cancelScheduledNodeTransaction();
        registerPendingNodeOperations(DatabaseOperation.CREATE, rmNodeData);
        if (delayEqualsToZero) {
            buildNodesTransactionAndCommit();
        } else {
            scheduleNodeTransaction();
        }
    }

    public void addUpdateNodeToPendingDatabaseOperations(RMNodeData rmNodeData) {
        cancelScheduledNodeTransaction();
        registerPendingNodeOperations(DatabaseOperation.UPDATE, rmNodeData);
        if (delayEqualsToZero) {
            buildNodesTransactionAndCommit();
        } else {
            scheduleNodeTransaction();
        }
    }

    public void addRemoveNodeToPendingDatabaseOperations(RMNodeData rmNodeData) {
        cancelScheduledNodeTransaction();
        registerPendingNodeOperations(DatabaseOperation.DELETE, rmNodeData);
        if (delayEqualsToZero) {
            buildNodesTransactionAndCommit();
        } else {
            scheduleNodeTransaction();
        }
    }

    public void addRemoveNodesToPendingDatabaseOperations(Collection<RMNodeData> nodes) {
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

    public void debounceNodeUpdatesIfNeeded() {
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

    public List<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> listPendingNodeOperations() {
        List<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> listCopy;
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
            pendingNodesOperations.add(new AbstractMap.SimpleImmutableEntry<>(rmNodeData, databaseOperation));
            if (!nbPendingOperationsPerNode.containsKey(rmNodeData)) {
                nbPendingOperationsPerNode.put(rmNodeData, 0);
            }
            nbPendingOperationsPerNode.put(rmNodeData, nbPendingOperationsPerNode.get(rmNodeData) + 1);
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
            List<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> effectiveNodesOperations = extractOperationsOfNextTransaction();
            while (!effectiveNodesOperations.isEmpty()) {
                final List<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> currentNodesOperations = effectiveNodesOperations;
                rmdbManager.executeReadWriteTransaction(new SessionWork<Void>() {
                    @Override
                    public Void doInTransaction(Session session) {
                        logger.debug("Executing database transaction with " + currentNodesOperations.size() +
                                     " operations");
                        try {
                            for (Map.Entry<RMNodeData, DatabaseOperation> operationEntry : currentNodesOperations) {
                                RMNodeData rmNodeData = operationEntry.getKey();
                                DatabaseOperation databaseOperation = operationEntry.getValue();
                                switch (databaseOperation) {
                                    case CREATE:
                                        session.save(rmNodeData);
                                        break;
                                    case UPDATE:
                                        session.update(rmNodeData);
                                        break;
                                    case DELETE:
                                        session.delete(rmNodeData);
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

    private List<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> extractOperationsOfNextTransaction() {
        Set<RMNodeData> nodesOfNextTransaction = new HashSet<>();
        List<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> operationsOfNextTransaction = new LinkedList<>();
        Iterator<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> pendingOperationsIterator = pendingNodesOperations.iterator();

        boolean canContinueAddingOperationsToTransaction = true;

        while (pendingOperationsIterator.hasNext() && canContinueAddingOperationsToTransaction) {
            AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation> operation = pendingOperationsIterator.next();
            canContinueAddingOperationsToTransaction = addOperationToNextTransactionIfPossible(nodesOfNextTransaction,
                                                                                               operationsOfNextTransaction,
                                                                                               pendingOperationsIterator,
                                                                                               operation);
        }
        return operationsOfNextTransaction;
    }

    private boolean addOperationToNextTransactionIfPossible(Set<RMNodeData> nodesOfNextTransaction,
            List<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> operationsOfNextTransaction,
            Iterator<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> pendingOperationsIterator,
            AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation> operation) {

        boolean operationAddedToNextTransaction = false;
        RMNodeData rmNodeData = operation.getKey();
        DatabaseOperation databaseOperation = operation.getValue();

        if (!nodesOfNextTransaction.contains(rmNodeData)) {
            nodesOfNextTransaction.add(rmNodeData);
            operationsOfNextTransaction.add(new AbstractMap.SimpleImmutableEntry<>(rmNodeData, databaseOperation));
            removeOperationFromPendingOperations(pendingOperationsIterator, rmNodeData);
            operationAddedToNextTransaction = true;
        }
        return operationAddedToNextTransaction;
    }

    private void removeOperationFromPendingOperations(
            Iterator<AbstractMap.SimpleImmutableEntry<RMNodeData, DatabaseOperation>> pendingOperationsIterator,
            RMNodeData rmNodeData) {

        pendingOperationsIterator.remove();
        int updatedNbOperationsForNode = nbPendingOperationsPerNode.get(rmNodeData) - 1;
        if (updatedNbOperationsForNode == 0) {
            nbPendingOperationsPerNode.remove(rmNodeData);
        }
    }

    public enum DatabaseOperation {
        CREATE,
        RETRIEVE,
        UPDATE,
        DELETE
    }

}
