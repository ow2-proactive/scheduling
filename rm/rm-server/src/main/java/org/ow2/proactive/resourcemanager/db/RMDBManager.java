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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.db.TransactionHelper;
import org.ow2.proactive.resourcemanager.core.history.Alive;
import org.ow2.proactive.resourcemanager.core.history.LockHistory;
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.core.history.UserHistory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;

import com.google.common.collect.Maps;

import it.sauronsoftware.cron4j.Scheduler;


public class RMDBManager {

    private static final String RM_DATABASE_IN_MEMORY = "rm.database.nodb";

    private static final Logger logger = ProActiveLogger.getLogger(RMDBManager.class);

    private static final String REQUEST_BUFFER_STRING = "Request " + RMDBManagerBuffer.class.getSimpleName() + " to ";

    private static final String IN_DATABASE_STRING = " in database";

    private final SessionFactory sessionFactory;

    private final TransactionHelper transactionHelper;

    private final RMDBManagerBuffer rmdbManagerBuffer;

    private Scheduler houseKeepingScheduler;

    private static final class LazyHolder {

        private static final RMDBManager INSTANCE = createUsingProperties();

    }

    private Timer timer = null;

    public static RMDBManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static RMDBManager createUsingProperties() {
        if (System.getProperty(RM_DATABASE_IN_MEMORY) != null) {
            return createInMemoryRMDBManager();
        } else {
            File configFile = new File(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getValueAsString()));

            boolean drop = PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getValueAsBoolean();
            boolean dropNS = PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB_NODESOURCES.getValueAsBoolean();

            if (logger.isInfoEnabled()) {
                logger.info("Starting RM DB Manager " + "with drop DB = " + drop + " and drop nodesources = " + dropNS +
                            " and configuration file = " + configFile.getAbsolutePath());
            }

            Configuration configuration = new Configuration();

            if (configFile.getName().endsWith(".xml")) {
                configuration.configure(configFile);
            } else {
                try {
                    Properties properties = new Properties();
                    properties.load(Files.newBufferedReader(configFile.toPath(), Charset.defaultCharset()));
                    configuration.addProperties(properties);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }

            return new RMDBManager(configuration, drop, dropNS);
        }
    }

    public static RMDBManager createInMemoryRMDBManager() {
        Configuration config = new Configuration();
        config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbc.JDBCDriver");
        config.setProperty("hibernate.connection.url",
                           "jdbc:hsqldb:mem:" + System.currentTimeMillis() + ";hsqldb.tx=mvcc");
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        return new RMDBManager(config, true, true);
    }

    public void startHouseKeeping() {
        houseKeepingScheduler = new Scheduler();
        if (PAResourceManagerProperties.RM_HISTORY_MAX_PERIOD.isSet() &&
            PAResourceManagerProperties.RM_HISTORY_MAX_PERIOD.getValueAsLong() > 0 &&
            PAResourceManagerProperties.RM_HISTORY_REMOVAL_CRONPERIOD.isSet()) {
            String cronExpr = PAResourceManagerProperties.RM_HISTORY_REMOVAL_CRONPERIOD.getValueAsString();
            houseKeepingScheduler.schedule(cronExpr, new HousekeepingRunner());
            houseKeepingScheduler.start();
        }

    }

    /**
     * Used only for testing purposes of the hibernate config needs to be changed.
     * RMDBManager.getInstance() should be used in most of cases.
     */
    public RMDBManager(Configuration configuration, boolean drop, boolean dropNS) {
        try {
            configuration.addAnnotatedClass(Alive.class);
            configuration.addAnnotatedClass(LockHistory.class);
            configuration.addAnnotatedClass(NodeHistory.class);
            configuration.addAnnotatedClass(NodeSourceData.class);
            configuration.addAnnotatedClass(UserHistory.class);
            configuration.addAnnotatedClass(RMNodeData.class);
            if (drop) {
                configuration.setProperty("hibernate.hbm2ddl.auto", "create");

                // dropping RRD database as well
                File ddrDB = new File(PAResourceManagerProperties.RM_HOME.getValueAsString(),
                                      PAResourceManagerProperties.RM_RRD_DATABASE_NAME.getValueAsString());

                if (ddrDB.exists() && !ddrDB.delete()) {
                    logger.error("Dropping RRD database has failed: " + ddrDB);
                }
            }

            configuration.setProperty("hibernate.id.new_generator_mappings", "true");
            configuration.setProperty("hibernate.jdbc.use_streams_for_binary", "true");

            sessionFactory = configuration.buildSessionFactory();
            transactionHelper = new TransactionHelper(sessionFactory);
            rmdbManagerBuffer = new RMDBManagerBuffer(this);

            Alive lastAliveTimeResult = findRmLastAliveEntry();

            if (lastAliveTimeResult == null) {
                createRmAliveEntry();
            } else if (!drop) {
                if (dropNS) {
                    removeNodeSources();
                }

                recover(lastAliveTimeResult.getTime());
            }

            long periodInMilliseconds = PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.getValueAsLong();

            timer = new Timer("Periodic RM live event saver");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateRmAliveTime();
                }
            }, periodInMilliseconds, periodInMilliseconds);

            // Start house keeping of node history
            startHouseKeeping();

        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed", ex);
            throw new DatabaseManagerException("Initial SessionFactory creation failed", ex);
        }
    }

    public Alive findRmLastAliveEntry() {

        List<?> lastAliveTimeResult = executeSqlQuery("from Alive");

        if (lastAliveTimeResult == null || lastAliveTimeResult.isEmpty()) {
            return null;
        }

        return (Alive) lastAliveTimeResult.get(0);
    }

    private void recover(final long lastAliveTime) {

        // updating node events with uncompleted end time
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                int updated = session.createSQLQuery("update NodeHistory set endTime = :endTime where endTime = 0")
                                     .setParameter("endTime", lastAliveTime)
                                     .executeUpdate();
                updated = +session.createSQLQuery("update NodeHistory set endTime = startTime where endTime < startTime")
                                  .executeUpdate();

                if (logger.isDebugEnabled()) {
                    logger.debug("Restoring the node history: " + updated + " raws updated");
                }

                return null;
            }
        });

        // updating user events with uncompleted end time
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                int updated = session.createSQLQuery("update UserHistory set endTime = :endTime where endTime = 0")
                                     .setParameter("endTime", lastAliveTime)
                                     .executeUpdate();
                updated = +session.createSQLQuery("update UserHistory set endTime = startTime where endTime < startTime")
                                  .executeUpdate();

                if (logger.isDebugEnabled()) {
                    logger.debug("Restoring the user history: " + updated + " raws updated");
                }

                return null;
            }
        });
    }

    /**
     * Should be used for insert/update/delete queries
     */
    protected <T> T executeReadWriteTransaction(SessionWork<T> sessionWork) {
        return executeReadWriteTransaction(sessionWork, true);
    }

    /**
     * Should be used for insert/update/delete queries
     */
    private <T> T executeReadWriteTransaction(SessionWork<T> sessionWork, boolean readOnlyEntities) {
        return transactionHelper.executeReadWriteTransaction(sessionWork, readOnlyEntities);
    }

    /**
     * Should be used for select queries only.
     * In case of failure no rollback is performed.
     */
    public <T> T executeReadTransaction(SessionWork<T> sessionWork) {
        return transactionHelper.executeReadOnlyTransaction(sessionWork);
    }

    public void close() {
        try {
            if (sessionFactory != null) {
                logger.info("Closing session factory");
                sessionFactory.close();
            }
        } catch (Exception e) {
            logger.error("Error while closing database", e);
        }
    }

    //======================================================================================

    public boolean addNodeSource(final NodeSourceData nodeSourceData) {
        try {
            logger.info("Add node source " + nodeSourceData.getName() + IN_DATABASE_STRING);
            boolean persisted = executeReadWriteTransaction(new SessionWork<Boolean>() {
                @Override
                public Boolean doInTransaction(Session session) {
                    boolean persisted = false;
                    NodeSourceData retrievedNodeSource = session.get(NodeSourceData.class, nodeSourceData.getName());
                    if (retrievedNodeSource == null) {
                        session.save(nodeSourceData);
                        rmdbManagerBuffer.addKnownNodeSource(nodeSourceData.getName());
                        persisted = true;
                    }
                    return persisted;
                }
            });
            if (!persisted) {
                persisted = executeReadWriteTransaction(new SessionWork<Boolean>() {
                    @Override
                    public Boolean doInTransaction(Session session) {
                        session.update(nodeSourceData);
                        return true;
                    }
                });
            }
            return persisted;
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while adding new node source " + nodeSourceData.getName(),
                                       e);
        }
    }

    public NodeSourceData getNodeSource(final String sourceName) {
        try {
            logger.debug("Retrieve node source " + sourceName + IN_DATABASE_STRING);
            return executeReadTransaction(new SessionWork<NodeSourceData>() {
                @Override
                @SuppressWarnings("unchecked")
                public NodeSourceData doInTransaction(Session session) {
                    Query query = session.getNamedQuery("getNodeSourceDataByName").setParameter("name", sourceName);
                    return (NodeSourceData) query.uniqueResult();
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while getting a node source from name " + sourceName, e);
        }
    }

    public void updateNodeSource(final NodeSourceData nodeSourceData) {
        logger.debug(REQUEST_BUFFER_STRING + "update node source " + nodeSourceData.getName() + IN_DATABASE_STRING);
        rmdbManagerBuffer.addUpdateNodeSourceToPendingDatabaseOperations(nodeSourceData);
    }

    public void removeNodeSource(final String sourceName) {
        rmdbManagerBuffer.removeKnownNodeSourceAndPendingUpdates(sourceName);
        final Collection<RMNodeData> relatedNodes = getNodesByNodeSource(sourceName);
        logger.info("Remove nodes linked to the node source " + sourceName + IN_DATABASE_STRING);
        removeNodes(relatedNodes, sourceName);
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                logger.info("Remove node source " + sourceName + IN_DATABASE_STRING);
                session.getNamedQuery("deleteNodeSourceDataByName").setParameter("name", sourceName).executeUpdate();
                return null;
            }
        });
    }

    private void removeNodeSources() {
        logger.info("Remove all node sources" + IN_DATABASE_STRING);
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                session.getNamedQuery("deleteAllNodeSourceData").executeUpdate();
                return null;
            }
        });
    }

    public Collection<NodeSourceData> getNodeSources() {
        logger.debug("Retrieve all node sources" + IN_DATABASE_STRING);
        return executeReadTransaction(new SessionWork<Collection<NodeSourceData>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Collection<NodeSourceData> doInTransaction(Session session) {
                Query query = session.getNamedQuery("getNodeSourceData");
                return (Collection<NodeSourceData>) query.list();
            }
        });
    }

    //======================================================================================

    private boolean nodeRecoveryDisabled() {
        return !PAResourceManagerProperties.RM_NODES_RECOVERY.getValueAsBoolean();
    }

    public void addNode(RMNodeData rmNodeData, String nodeSourceName) {
        if (nodeRecoveryDisabled()) {
            return;
        }

        logger.debug(REQUEST_BUFFER_STRING + "create node " + rmNodeData.getName() + IN_DATABASE_STRING);
        rmdbManagerBuffer.addCreateNodeToPendingDatabaseOperations(rmNodeData, nodeSourceName);
    }

    public void updateNode(final RMNodeData rmNodeData, final String nodeSourceName) {
        if (nodeRecoveryDisabled()) {
            return;
        }

        if (rmdbManagerBuffer.canOperateDatabaseSynchronouslyWithNode(rmNodeData)) {
            try {
                logger.debug("Update node " + rmNodeData.getName() + IN_DATABASE_STRING);
                executeReadWriteTransaction(new SessionWork<Void>() {
                    @Override
                    public Void doInTransaction(Session session) {
                        NodeSourceData nodeSourceData = session.load(NodeSourceData.class, nodeSourceName);
                        rmNodeData.setNodeSource(nodeSourceData);
                        session.update(rmNodeData);
                        return null;
                    }
                });
            } catch (RuntimeException e) {
                throw new RuntimeException("Exception occurred while updating node " + rmNodeData.getName(), e);
            }
        } else {
            logger.debug(REQUEST_BUFFER_STRING + "update node " + rmNodeData.getName() + IN_DATABASE_STRING);
            rmdbManagerBuffer.addUpdateNodeToPendingDatabaseOperations(rmNodeData, nodeSourceName);
        }
    }

    public void removeNode(RMNode rmNode) {
        if (nodeRecoveryDisabled()) {
            return;
        }

        RMNodeData rmNodeData = RMNodeData.createRMNodeData(rmNode);
        removeNode(rmNodeData, rmNode.getNodeSourceName());
    }

    public void removeNode(final RMNodeData rmNodeData, final String nodeSourceName) {
        if (nodeRecoveryDisabled()) {
            return;
        }

        if (rmdbManagerBuffer.canOperateDatabaseSynchronouslyWithNode(rmNodeData)) {
            try {
                logger.info("Remove node " + rmNodeData.getName() + IN_DATABASE_STRING);
                executeReadWriteTransaction(new SessionWork<Void>() {
                    @Override
                    public Void doInTransaction(Session session) {
                        NodeSourceData nodeSourceData = session.load(NodeSourceData.class, nodeSourceName);
                        rmNodeData.setNodeSource(nodeSourceData);
                        session.delete(rmNodeData);
                        return null;
                    }
                });
            } catch (RuntimeException e) {
                throw new RuntimeException("Exception occurred while removing node " + rmNodeData.getName(), e);
            }
        } else {
            logger.debug(REQUEST_BUFFER_STRING + "remove node " + rmNodeData.getName() + IN_DATABASE_STRING);
            rmdbManagerBuffer.addRemoveNodeToPendingDatabaseOperations(rmNodeData, nodeSourceName);
        }
    }

    private void removeNodes(final Collection<RMNodeData> nodes, final String nodeSourceName) {
        if (nodeRecoveryDisabled()) {
            return;
        }

        if (rmdbManagerBuffer.canOperateDatabaseSynchronouslyWithNodes(nodes)) {
            try {
                logger.info("Remove " + nodes.size() + " nodes" + IN_DATABASE_STRING);
                if (logger.isDebugEnabled()) {
                    logger.debug("Nodes removed: " + Arrays.toString(nodes.toArray()));
                }
                executeReadWriteTransaction(new SessionWork<Void>() {
                    @Override
                    public Void doInTransaction(Session session) {
                        for (RMNodeData rmNodeData : nodes) {
                            NodeSourceData nodeSourceData = session.load(NodeSourceData.class, nodeSourceName);
                            rmNodeData.setNodeSource(nodeSourceData);
                            session.delete(rmNodeData);
                        }
                        return null;
                    }
                });
            } catch (RuntimeException e) {
                throw new RuntimeException("Exception occurred while removing nodes", e);
            }
        } else {
            logger.debug(REQUEST_BUFFER_STRING + "remove " + nodes.size() + " nodes" + IN_DATABASE_STRING);
            rmdbManagerBuffer.addRemoveNodesToPendingDatabaseOperations(nodes, nodeSourceName);
        }
    }

    public void removeAllNodes() {
        try {
            logger.info("Remove all nodes" + IN_DATABASE_STRING);
            executeReadWriteTransaction(new SessionWork<Void>() {
                @Override
                public Void doInTransaction(Session session) {
                    session.getNamedQuery("deleteAllRMNodeData").executeUpdate();
                    return null;
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while removing all nodes in the database", e);
        }
    }

    public void removeAllNodesFromNodeSource(final String nodeSourceName) {
        try {
            logger.info("Remove all nodes from node source " + nodeSourceName + IN_DATABASE_STRING);
            executeReadWriteTransaction(new SessionWork<Void>() {
                @Override
                public Void doInTransaction(Session session) {
                    session.getNamedQuery("deleteAllRMNodeDataFromNodeSource")
                           .setParameter("name", nodeSourceName)
                           .executeUpdate();
                    return null;
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while removing all nodes from node source " +
                                       nodeSourceName + " in the database", e);
        }
    }

    public RMNodeData getNodeByNameAndUrl(final String nodeName, final String nodeUrl) {
        logger.debug(REQUEST_BUFFER_STRING + "retrieve node with node name " + nodeName + IN_DATABASE_STRING);
        rmdbManagerBuffer.debounceNodeUpdatesIfNeeded();
        try {
            return executeReadTransaction(new SessionWork<RMNodeData>() {
                @Override
                public RMNodeData doInTransaction(Session session) {
                    logger.debug("Retrieve node " + nodeName + IN_DATABASE_STRING);
                    Query query = session.getNamedQuery("getRMNodeDataByNameAndUrl")
                                         .setParameter("name", nodeName)
                                         .setParameter("url", nodeUrl);
                    return (RMNodeData) query.uniqueResult();
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while retrieving node " + nodeName, e);
        }
    }

    public Collection<RMNodeData> getNodesByNodeSource(final String nodeSourceName) {
        logger.debug(REQUEST_BUFFER_STRING + "retrieve node with node source name " + nodeSourceName +
                     IN_DATABASE_STRING);
        rmdbManagerBuffer.debounceNodeUpdatesIfNeeded();
        try {
            logger.debug("Retrieve nodes from node source " + nodeSourceName + IN_DATABASE_STRING);
            return executeReadTransaction(new SessionWork<Collection<RMNodeData>>() {
                @Override
                @SuppressWarnings("unchecked")
                public Collection<RMNodeData> doInTransaction(Session session) {
                    Query query = session.getNamedQuery("getRMNodeDataByNodeSource").setParameter("name",
                                                                                                  nodeSourceName);
                    return (Collection<RMNodeData>) query.list();
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while getting node by node source name " + nodeSourceName,
                                       e);
        }
    }

    public Collection<RMNodeData> getAllNodes() {
        logger.debug(REQUEST_BUFFER_STRING + "retrieve all nodes" + IN_DATABASE_STRING);
        rmdbManagerBuffer.debounceNodeUpdatesIfNeeded();
        try {
            logger.debug("Retrieve all nodes" + IN_DATABASE_STRING);
            return executeReadTransaction(new SessionWork<Collection<RMNodeData>>() {
                @Override
                @SuppressWarnings("unchecked")
                public Collection<RMNodeData> doInTransaction(Session session) {
                    Query query = session.getNamedQuery("getAllRMNodeData");
                    return (Collection<RMNodeData>) query.list();
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occurred while getting all nodes from database", e);
        }
    }

    public void saveUserHistory(final UserHistory history) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                session.save(history);
                return null;
            }
        });
    }

    public void updateUserHistory(final UserHistory history) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                session.update(history);
                return null;
            }
        });
    }

    public void saveNodeHistory(final NodeHistory nodeHistory) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                session.createSQLQuery("update NodeHistory set endTime=:endTime where nodeUrl=:nodeUrl and endTime=0")
                       .setParameter("endTime", nodeHistory.getStartTime())
                       .setParameter("nodeUrl", nodeHistory.getNodeUrl())
                       .executeUpdate();

                if (nodeHistory.isStoreInDataBase()) {
                    session.save(nodeHistory);
                }
                return null;
            }
        });
    }

    public void deleteOldNodeHistory() {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                if (PAResourceManagerProperties.RM_HISTORY_MAX_PERIOD.isSet() &&
                    PAResourceManagerProperties.RM_HISTORY_MAX_PERIOD.getValueAsLong() > 0) {
                    long oldestTime = System.currentTimeMillis() -
                                      (PAResourceManagerProperties.RM_HISTORY_MAX_PERIOD.getValueAsLong() * 1000);

                    int nbEntriesDeleted = session.createSQLQuery("delete from NodeHistory where startTime<:minTime")
                                                  .setParameter("minTime", oldestTime)
                                                  .executeUpdate();
                    if (nbEntriesDeleted > 0) {
                        logger.info("HOUSEKEEPING of NodeHistory performed, deleted " + nbEntriesDeleted + " entries");
                    }
                }
                return null;
            }
        });
    }

    public void deleteOldUserHistory() {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                if (PAResourceManagerProperties.RM_HISTORY_MAX_PERIOD.isSet() &&
                    PAResourceManagerProperties.RM_HISTORY_MAX_PERIOD.getValueAsLong() > 0) {
                    long oldestTime = System.currentTimeMillis() -
                                      (PAResourceManagerProperties.RM_HISTORY_MAX_PERIOD.getValueAsLong() * 1000);

                    int nbEntriesDeleted = session.createSQLQuery("delete from UserHistory where startTime<:minTime")
                                                  .setParameter("minTime", oldestTime)
                                                  .executeUpdate();
                    if (nbEntriesDeleted > 0) {
                        logger.info("HOUSEKEEPING of UserHistory performed, deleted " + nbEntriesDeleted + " entries");
                    }
                }
                return null;
            }
        });
    }

    /**
     * Removes all entries from LockHistory table.
     */
    public void clearLockHistory() {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                int nbDeletes = session.createSQLQuery("delete from LockHistory").executeUpdate();

                if (logger.isDebugEnabled()) {
                    logger.debug(nbDeletes + " delete(s) performed with success on LockHistory table.");
                }

                return null;
            }
        });
    }

    /**
     * Returns information about the nodes which have been locked on previous RM run.
     * <p>
     * The purpose of this method is to fetch the number of nodes locked per node source
     * on the previous RM run.
     *
     * @return the number of nodes locked, per node source, on the previous RM run.
     */
    public Map<String, MutableInteger> findNodesLockedOnPreviousRun() {
        List<LockHistory> lockHistoryResult = getLockHistories();
        return entityToMap(lockHistoryResult);
    }

    public List<LockHistory> getLockHistories() {
        return (List<LockHistory>) executeSqlQuery("from LockHistory");
    }

    public Map<String, MutableInteger> entityToMap(List<LockHistory> lockHistoryResult) {
        if (lockHistoryResult == null || lockHistoryResult.isEmpty()) {
            return Maps.newHashMap();
        }

        Map<String, MutableInteger> result = new HashMap<>(lockHistoryResult.size(), 1f);

        for (Object entry : lockHistoryResult) {
            LockHistory lockHistory = (LockHistory) entry;

            int lockCount = lockHistory.getLockCount();
            if (lockCount > 0) {
                result.put(lockHistory.getNodeSource(), new MutableInteger(lockCount));
            }
        }

        return result;
    }

    protected void updateRmAliveTime() {
        updateAliveTable("time", System.currentTimeMillis());
    }

    private void updateAliveTable(final String columnName, final Object value) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                session.createSQLQuery("update Alive set " + columnName + " = :time")
                       .setParameter("time", value)
                       .executeUpdate();
                return null;
            }
        });
    }

    public void createLockEntryOrUpdate(final String nodeSource, final NodeLockUpdateAction actionOnUpdate) {

        if (!PAResourceManagerProperties.RM_NODES_LOCK_RESTORATION.getValueAsBoolean()) {
            return;
        }

        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                LockHistory lockHistory = session.get(LockHistory.class, nodeSource);

                if (lockHistory == null) {
                    lockHistory = new LockHistory(nodeSource, 1);
                    session.save(lockHistory);
                } else {
                    switch (actionOnUpdate) {
                        case DECREMENT:
                            lockHistory.decrementLockCount();
                            break;
                        case INCREMENT:
                            lockHistory.incrementLockCount();
                            break;
                        default:
                            break;
                    }

                    int nbUpdates = session.createSQLQuery("update LockHistory set lockCount = :lockCount where nodeSource = :nodeSource")
                                           .setParameter("lockCount", lockHistory.getLockCount())
                                           .setParameter("nodeSource", lockHistory.getNodeSource())
                                           .executeUpdate();

                    if (nbUpdates <= 0) {
                        logger.warn("Lock history update has failed for a node that belongs to Node source " +
                                    nodeSource);
                    }
                }

                return null;
            }
        });
    }

    public enum NodeLockUpdateAction {
        DECREMENT,
        INCREMENT
    }

    private void createRmAliveEntry() {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long now = System.currentTimeMillis();

                Alive alive = new Alive();
                alive.setTime(now);
                session.save(alive);

                return null;
            }
        });
    }

    public List<?> executeSqlQuery(final String queryStr) {
        return executeReadTransaction(new SessionWork<List<?>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<?> doInTransaction(Session session) {
                Query query = session.createQuery(queryStr);
                return query.list();
            }
        });
    }

    private class HousekeepingRunner implements Runnable {

        @Override
        public void run() {
            getInstance().deleteOldNodeHistory();
            getInstance().deleteOldUserHistory();
        }
    }

    public RMDBManagerBuffer getBuffer() {
        return rmdbManagerBuffer;
    }

    public List<NodeHistory> getNodesHistory(long windowStart, long windowEnd) {
        return executeReadTransaction(session -> {
            //            this expression is built as negation to expression that
            //            retrieves all items that we do not want to have in the response.
            //            just to clarify the events we include
            //            we discard only events :
            //            - ending before the windows start
            //            - starting after the window start
            //            we include everything else
            //            when an event as no endingtime the  value of the endingtime will be 0
            Query query = session.createQuery("FROM NodeHistory " + "WHERE (endTime = 0 OR :windowStart <= endTime) " +
                                              "AND ( startTime <= :windowEnd )");
            query.setParameter("windowStart", windowStart);
            query.setParameter("windowEnd", windowEnd);
            return (List<NodeHistory>) query.list();
        });
    }
}
