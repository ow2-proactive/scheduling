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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
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

    private static final int BATCH_TIME_MILLISECONDS = 500;

    private static final Logger logger = ProActiveLogger.getLogger(RMDBManagerBuffer.class);

    private RMDBManager rmdbManager;

    /**
     * We need a single executor with a single thread because the operation on
     * nodes and node sources need ordering.
     */
    private ScheduledExecutorService databaseCommitter;

    private ScheduledFuture<?> scheduledNodeSourceTransaction;

    /**
     * The set of node sources to persist in database. This represents one
     * update to do per node source (i.e. the latest state of the node source).
     */
    private Set<NodeSourceData> pendingNodeSourcesUpdate;

    private ScheduledFuture<?> scheduledNodeTransaction;

    private final Lock lock = new ReentrantLock();

    private final Condition pendingModificationsCondition = lock.newCondition();

    /**
     * The set of nodes to persist in database. One entry of this map
     * corresponds to the successive updates to apply for one node.
     */
    private Map<RMNodeData, Map<DatabaseOperationType, RMNodeData>> pendingNodesOperations;

    public RMDBManagerBuffer(RMDBManager rmdbManager) {
        this.rmdbManager = rmdbManager;
        databaseCommitter = Executors.newSingleThreadScheduledExecutor();
        pendingNodeSourcesUpdate = new HashSet<>();
        pendingNodesOperations = new HashMap<>();
    }

    public void addPendingUpdateOperationAndRescheduleTransaction(final NodeSourceData nodeSourceData) {
        pendingNodeSourcesUpdate.add(nodeSourceData);
        if (scheduledNodeSourceTransaction != null && !scheduledNodeSourceTransaction.isDone()) {
            scheduledNodeSourceTransaction.cancel(false);
        }
        scheduledNodeSourceTransaction = databaseCommitter.schedule(new Runnable() {
            @Override
            public void run() {
                applyNodeSourceTransactionAndCommit();
            }
        }, BATCH_TIME_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    private void applyNodeSourceTransactionAndCommit() {
        try {
            rmdbManager.executeReadWriteTransaction(new SessionWork<Void>() {
                @Override
                public Void doInTransaction(Session session) {
                    for (NodeSourceData nodeSource : pendingNodeSourcesUpdate) {
                        session.update(nodeSource);
                    }
                    pendingNodeSourcesUpdate.clear();
                    return null;
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while updating node sources", e);
        }
    }

    public void addPendingOperations(DatabaseOperationType databaseOperationType,
            final Collection<RMNodeData> rmNodesData) {

        cancelScheduledTransaction();
        updatePendingNodeOperations(databaseOperationType, rmNodesData);
        scheduleTransaction();
    }

    public void executeOrAddPendingOperation(DatabaseOperationType databaseOperationType, final RMNodeData rmNodeData) {

        if (!executeUpdateSynchronouslyIfPossible(databaseOperationType, rmNodeData)) {

            cancelScheduledTransaction();
            updatePendingNodeOperations(databaseOperationType, rmNodeData);
            scheduleTransaction();
        }
    }

    private void scheduleTransaction() {
        scheduledNodeTransaction = databaseCommitter.schedule(new Runnable() {
            @Override
            public void run() {
                applyNodesTransactionAndCommit();
            }
        }, BATCH_TIME_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    private boolean executeUpdateSynchronouslyIfPossible(DatabaseOperationType databaseOperationType,
            final RMNodeData nodeData) {
        boolean updated = false;
        if (databaseOperationType.equals(DatabaseOperationType.UPDATE) &&
            !pendingNodesOperations.containsKey(nodeData)) {
            try {
                rmdbManager.executeReadWriteTransaction(new SessionWork<Void>() {
                    @Override
                    public Void doInTransaction(Session session) {
                        session.update(nodeData);
                        return null;
                    }
                });
                updated = true;
            } catch (RuntimeException e) {
                throw new RuntimeException("Exception occurred while updating node " + nodeData.getName(), e);
            }
        }
        return updated;
    }

    private void cancelScheduledTransaction() {
        if (scheduledNodeTransaction != null && !scheduledNodeTransaction.isDone()) {
            scheduledNodeTransaction.cancel(false);
        }
    }

    private void updatePendingNodeOperations(DatabaseOperationType databaseOperationType, RMNodeData rmNodeData) {
        if (pendingNodesOperations.containsKey(rmNodeData)) {
            pendingNodesOperations.get(rmNodeData).put(databaseOperationType, rmNodeData);
        } else {
            Map<DatabaseOperationType, RMNodeData> rmNodeDataSnapshots = new HashMap<>();
            rmNodeDataSnapshots.put(databaseOperationType, rmNodeData);
            pendingNodesOperations.put(rmNodeData, rmNodeDataSnapshots);
        }
    }

    private void updatePendingNodeOperations(DatabaseOperationType databaseOperationType,
            Collection<RMNodeData> rmNodesData) {
        for (RMNodeData rmNodeData : rmNodesData) {
            updatePendingNodeOperations(databaseOperationType, rmNodeData);
        }
    }

    private void applyNodesTransactionAndCommit() {
        lock.lock();
        try {
            rmdbManager.executeReadWriteTransaction(new SessionWork<Void>() {
                @Override
                public Void doInTransaction(Session session) {
                    for (Map.Entry<RMNodeData, Map<DatabaseOperationType, RMNodeData>> rmNodeDataEntry : pendingNodesOperations.entrySet()) {

                        Map<DatabaseOperationType, RMNodeData> rmNodeDataOperations = rmNodeDataEntry.getValue();

                        // apply CREATE, UPDATE, DELETE *in order*
                        RMNodeData createdRmNodeData = rmNodeDataOperations.get(DatabaseOperationType.CREATE);
                        if (createdRmNodeData != null) {
                            session.save(createdRmNodeData);
                        }
                        RMNodeData updatedRmNodeData = rmNodeDataOperations.get(DatabaseOperationType.UPDATE);
                        if (updatedRmNodeData != null) {
                            session.update(updatedRmNodeData);
                        }
                        RMNodeData deletedRmNodeData = rmNodeDataOperations.get(DatabaseOperationType.DELETE);
                        if (deletedRmNodeData != null) {
                            session.delete(deletedRmNodeData);
                        }
                    }
                    return null;
                }
            });
            pendingNodesOperations.clear();
            pendingModificationsCondition.signal();
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while adding new node ", e);
        } finally {
            lock.unlock();
        }
    }

    public void debounceNodeUpdatesIfNeeded() {
        lock.lock();
        try {
            if (!pendingNodesOperations.isEmpty()) {
                try {
                    pendingModificationsCondition.await();
                } catch (InterruptedException e) {
                    logger.warn("Waiting of debouncing of database operations has been interrupted.");
                }
            }
        } finally {
            lock.unlock();
        }
    }

    enum DatabaseOperationType {
        CREATE,
        UPDATE,
        DELETE
    }

}
