/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.resourcemanager.core.history.Alive;
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.core.history.UserHistory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;


public class RMDBManager {

    private static final String JAVA_PROPERTYNAME_NODB = "rm.database.nodb";
    private static final Logger logger = ProActiveLogger.getLogger(RMDBManager.class);

    private final SessionFactory sessionFactory;

    private static RMDBManager instance = null;

    private Timer timer = null;

    /**
    * An internal class to encapsulate the semantic of your data base query.
    * It allows to avoid try/catch in every method. 
    */
    private static abstract class SessionWork<T> {
        abstract T executeWork(Session session);
    }

    public synchronized static RMDBManager getInstance() {
        if (instance == null) {
            instance = createUsingProperties();
        }

        return instance;
    }

    private static RMDBManager createUsingProperties() {
        if (System.getProperty(JAVA_PROPERTYNAME_NODB) != null) {
            return createInMemoryRMDBManager();
        } else {
            File configFile = new File(PAResourceManagerProperties
                    .getAbsolutePath(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getValueAsString()));

            boolean drop = PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getValueAsBoolean();
            boolean dropNS = PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB_NODESOURCES
                    .getValueAsBoolean();

            logger.info("Initializing RM DB using Hibernate config " + configFile.getAbsolutePath());

            return new RMDBManager(new Configuration().configure(configFile), drop, dropNS);
        }
    }

    private static RMDBManager createInMemoryRMDBManager() {
        Configuration config = new Configuration();
        config.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        config.setProperty("hibernate.connection.url", "jdbc:h2:mem:scheduler");
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        return new RMDBManager(config, true, true);
    }

    /**
     * Used only for testing purposes of the hibernate config needs to be changed.
     * RMDBManager.getInstance() should be used in most of cases.
     */
    public RMDBManager(Configuration configuration, boolean drop, boolean dropNS) {
        logger.info("Starting Hibernate...");
        logger.info("Drop DB : " + drop);
        logger.info("Drop Nodesources : " + dropNS);
        try {
            configuration.addAnnotatedClass(NodeSourceData.class);
            configuration.addAnnotatedClass(NodeHistory.class);
            configuration.addAnnotatedClass(UserHistory.class);
            configuration.addAnnotatedClass(Alive.class);
            if (drop) {
                configuration.setProperty("hibernate.hbm2ddl.auto", "create");

                // dropping RRD data base as well
                File ddrDB = new File(PAResourceManagerProperties.RM_HOME.getValueAsString() +
                    System.getProperty("file.separator") +
                    PAResourceManagerProperties.RM_RRD_DATABASE_NAME.getValueAsString());
                if (ddrDB.exists()) {
                    ddrDB.delete();
                }
            }

            configuration.setProperty("hibernate.id.new_generator_mappings", "true");
            configuration.setProperty("hibernate.jdbc.use_streams_for_binary", "true");

            sessionFactory = configuration.buildSessionFactory();

            List<?> lastAliveTime = sqlQuery("from Alive");
            if (lastAliveTime == null || lastAliveTime.size() == 0) {
                createRmAliveTime();
            } else if (!drop) {
                if (dropNS) {
                    removeNodeSources();
                }

                recover(((Alive) lastAliveTime.get(0)).getTime());
            }

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

    private void recover(final long lastAliveTime) {

        // updating node events with uncompleted end time        
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                int updated = session.createSQLQuery(
                        "update NodeHistory set endTime = :endTime where endTime = 0").setParameter(
                        "endTime", lastAliveTime).executeUpdate();
                updated = +session.createSQLQuery(
                        "update NodeHistory set endTime = startTime where endTime < startTime")
                        .executeUpdate();
                logger.debug("Restoring the node history: " + updated + " raws updated");
                return null;
            }
        });

        // updating user events with uncompleted end time        
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                int updated = session.createSQLQuery(
                        "update UserHistory set endTime = :endTime where endTime = 0").setParameter(
                        "endTime", lastAliveTime).executeUpdate();
                updated = +session.createSQLQuery(
                        "update UserHistory set endTime = startTime where endTime < startTime")
                        .executeUpdate();
                logger.debug("Restoring the user history: " + updated + " raws updated");
                return null;
            }
        });
    }

    /**
     * Should be used for insert/update/delete queries
     */
    private <T> T runWithTransaction(SessionWork<T> sessionWork) {
        return runWithTransaction(sessionWork, true);
    }

    /**
     * Should be used for insert/update/delete queries
     */
    private <T> T runWithTransaction(SessionWork<T> sessionWork, boolean readonly) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            session.setDefaultReadOnly(readonly);
            tx = session.beginTransaction();
            T result = sessionWork.executeWork(session);
            tx.commit();
            return result;
        } catch (Throwable e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Throwable rollbackError) {
                    logger.warn("Failed to rollback transaction", rollbackError);
                }
            }
            logger.warn("DB operation failed", e);
            return null;
        } finally {
            try {
                session.close();
            } catch (Throwable e) {
                logger.warn("Failed to close session", e);
            }
        }
    }

    /**
     * Should be used for select queries
     */
    private <T> T runWithoutTransaction(SessionWork<T> sessionWork) {
        Session session = sessionFactory.openSession();
        try {
            session.setDefaultReadOnly(true);
            T result = sessionWork.executeWork(session);
            return result;
        } catch (Throwable e) {
            logger.warn("DB operation failed", e);
            return null;
        } finally {
            try {
                session.close();
            } catch (Throwable e) {
                logger.warn("Failed to close session", e);
            }
        }
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
            return runWithTransaction(new SessionWork<Boolean>() {
                @Override
                Boolean executeWork(Session session) {
                    logger.info("Adding a new node source " + nodeSourceData.getName() + " to the data base");
                    session.save(nodeSourceData);
                    return true;
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Node source '" + nodeSourceData.getName() + "' already exists");
        }
    }

    public void removeNodeSource(final String sourceName) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                logger.info("Removing the node source " + sourceName + " from the data base");
                session.createQuery("delete from NodeSourceData where name=:name").setParameter("name",
                        sourceName).executeUpdate();
                return null;
            }
        });
    }

    private void removeNodeSources() {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                logger.info("Removing all node sources from the data base");
                session.createQuery("delete from NodeSourceData").executeUpdate();
                return null;
            }
        });
    }

    public Collection<NodeSourceData> getNodeSources() {
        return runWithoutTransaction(new SessionWork<Collection<NodeSourceData>>() {
            @Override
            @SuppressWarnings("unchecked")
            Collection<NodeSourceData> executeWork(Session session) {
                Query query = session.createQuery("from NodeSourceData");
                return (Collection<NodeSourceData>) query.list();
            }
        });
    }

    public void saveUserHistory(final UserHistory history) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                session.save(history);
                return null;
            }
        });
    }

    public void updateUserHistory(final UserHistory history) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                session.update(history);
                return null;
            }
        });
    }

    public void saveNodeHistory(final NodeHistory nodeHistory) {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                session.createSQLQuery(
                        "update NodeHistory set endTime=:endTime where nodeUrl=:nodeUrl and endTime=0")
                        .setParameter("endTime", nodeHistory.getStartTime()).setParameter("nodeUrl",
                                nodeHistory.getNodeUrl()).executeUpdate();

                if (nodeHistory.isStoreInDataBase()) {
                    session.save(nodeHistory);
                }
                return null;
            }
        });
    }

    /**
     * @return prev alive time
     */
    protected void updateRmAliveTime() {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                long curMilliseconds = System.currentTimeMillis();
                session.createSQLQuery("update Alive set time = :time").setParameter("time", curMilliseconds)
                        .executeUpdate();
                return null;
            }
        });
    }

    private void createRmAliveTime() {
        runWithTransaction(new SessionWork<Void>() {
            @Override
            Void executeWork(Session session) {
                Alive alive = new Alive();
                alive.setTime(System.currentTimeMillis());
                session.save(alive);
                return null;
            }
        });
    }

    public List<?> sqlQuery(final String queryStr) {
        return runWithoutTransaction(new SessionWork<List<?>>() {
            @Override
            @SuppressWarnings("unchecked")
            List<?> executeWork(Session session) {
                Query query = session.createQuery(queryStr);
                return query.list();
            }
        });
    }

}
