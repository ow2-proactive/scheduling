/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */

package org.ow2.proactive.resourcemanager.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.core.history.UserHistory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


public class RMDBManager {

    private static final String RM_DATABASE_IN_MEMORY = "rm.database.nodb";

    private static final Logger logger = ProActiveLogger.getLogger(RMDBManager.class);

    private final SessionFactory sessionFactory;

    private final TransactionHelper transactionHelper;

    private long rmLastStartupTime;

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

    /**
     * Used only for testing purposes of the hibernate config needs to be changed.
     * RMDBManager.getInstance() should be used in most of cases.
     */
    public RMDBManager(Configuration configuration, boolean drop, boolean dropNS) {
        try {
            configuration.addAnnotatedClass(NodeSourceData.class);
            configuration.addAnnotatedClass(NodeHistory.class);
            configuration.addAnnotatedClass(UserHistory.class);
            configuration.addAnnotatedClass(Alive.class);
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

            Alive lastAliveTimeResult = findRmLastAliveEntry();

            if (lastAliveTimeResult == null) {
                createRmAliveEntry();
            } else if (!drop) {
                if (dropNS) {
                    removeNodeSources();
                }

                rmLastStartupTime = lastAliveTimeResult.getLastStartupTime();

                recover(rmLastStartupTime);
            }

            updateRmLastStartupTime();

            int periodInMilliseconds = PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.getValueAsInt();

            timer = new Timer("Periodic RM live event saver");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateRmAliveTime();
                }
            }, periodInMilliseconds, periodInMilliseconds);

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
    private <T> T executeReadWriteTransaction(SessionWork<T> sessionWork) {
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
            return executeReadWriteTransaction(new SessionWork<Boolean>() {
                @Override
                public Boolean doInTransaction(Session session) {
                    logger.info("Adding a new node source " + nodeSourceData.getName() + " to the database");
                    session.save(nodeSourceData);
                    return true;
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception occured while adding new node source '" + nodeSourceData.getName() +
                                       "'");
        }
    }

    public void removeNodeSource(final String sourceName) {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                logger.info("Removing the node source " + sourceName + " from the database");
                session.getNamedQuery("deleteNodeSourceDataByName").setParameter("name", sourceName).executeUpdate();
                return null;
            }
        });
    }

    private void removeNodeSources() {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                logger.info("Removing all node sources from the database");
                session.getNamedQuery("deleteAllNodeSourceData").executeUpdate();
                return null;
            }
        });
    }

    public Collection<NodeSourceData> getNodeSources() {
        return executeReadTransaction(new SessionWork<Collection<NodeSourceData>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Collection<NodeSourceData> doInTransaction(Session session) {
                Query query = session.getNamedQuery("getNodeSourceData");
                return (Collection<NodeSourceData>) query.list();
            }
        });
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

    /**
     * Returns information about the nodes which have been locked on previous RM run.
     * <p>
     * The purpose of this method is to fetch the number of nodes locked per node source
     * and host pair on the previous RM run.
     *
     * @return the number of nodes locked per node source and host pair on the previous RM run.
     */
    public Table<String, String, MutableInteger> getNodesLockedOnPreviousRun() {
        return getNodesLockedOnPreviousRun(getRmLastStartupTime());
    }

    Table<String, String, MutableInteger> getNodesLockedOnPreviousRun(final long rmLastStartupTime) {

        List<Object[]> lockingInformation = findNodesLockedOnPreviousRun(rmLastStartupTime);

        return groupNodeUrlsByHostAndNodeSource(lockingInformation);
    }

    public List<Object[]> findNodesLockedOnPreviousRun(final long rmLastStartupTime) {
        return executeReadTransaction(new SessionWork<List<Object[]>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Object[]> doInTransaction(Session session) {
                return session.getNamedQuery("getNodesLockedOnPreviousRun")
                              .setLong("endTime", rmLastStartupTime)
                              .list();
            }
        });
    }

    protected Table<String, String, MutableInteger>
            groupNodeUrlsByHostAndNodeSource(List<Object[]> lockingInformation) {

        Table<String, String, MutableInteger> table = HashBasedTable.create(lockingInformation.size(), 3);

        // Used to remove duplicate node URLs
        // The JPQL query already applies GROUP BY but DISTINCT is not possible on a specific column
        // As a consequence, filtering is done at this level
        Set<String> nodeUrls = new HashSet<>(lockingInformation.size(), 1f);

        for (Object[] row : lockingInformation) {

            String nodeSource = (String) row[0];
            String host = (String) row[1];
            String nodeUrl = (String) row[2];

            int increment = 0;
            if (nodeUrls.add(nodeUrl)) {
                increment = 1;
            }

            MutableInteger nodeCount = table.get(nodeSource, host);

            if (nodeCount == null) {
                nodeCount = new MutableInteger(increment);
            } else {
                nodeCount.add(increment);
            }

            table.put(nodeSource, host, nodeCount);
        }

        return table;
    }

    protected void updateRmAliveTime() {
        updateAliveTable("time", System.currentTimeMillis());
    }

    protected void updateRmLastStartupTime() {
        updateAliveTable("lastStartupTime", System.currentTimeMillis());
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

    private void createRmAliveEntry() {
        executeReadWriteTransaction(new SessionWork<Void>() {
            @Override
            public Void doInTransaction(Session session) {
                long now = System.currentTimeMillis();

                Alive alive = new Alive();
                alive.setLastStartupTime(now);
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

    /**
     * Returns the timestamp at which the Resource Manager
     * was started up the last time.
     *
     * @return a Unix epoch time denoting the last timestamp at which
     * the Resource Manager was restarted for the last time. A return
     * value of {@code 0} means the RM is starting for the first time.
     */
    public long getRmLastStartupTime() {
        return rmLastStartupTime;
    }

}
