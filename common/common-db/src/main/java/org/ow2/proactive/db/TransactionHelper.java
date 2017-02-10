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
package org.ow2.proactive.db;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.LockAcquisitionException;
import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class TransactionHelper {

    private static final Logger logger = Logger.getLogger(TransactionHelper.class);

    private final SessionFactory sessionFactory;

    public TransactionHelper(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Execute the specified {@code sessionWork} in a session configured
     * for read-only interactions with the database. This method should be
     * used for select queries.
     * <p>
     * In case of a transaction failure a retry is performed or not based on
     * the value defined by {@code PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES}.
     *
     * @param sessionWork the action to perform.
     * @param <T>         the result type of the session work.
     * @return session work result based on the generic type.
     */
    public <T> T executeReadOnlyTransaction(SessionWork<T> sessionWork) {
        return tryExecuteTransactionLoop(sessionWork, false, true);
    }

    /**
     * Execute the specified {@code sessionWork} in a session configured
     * for read/write interactions with the database. This method should be
     * used for insert, update, and delete queries. If select only is required,
     * then {@link #executeReadOnlyTransaction(SessionWork)} could be used.
     * <p>
     * In case of database error while executing the query, a rollback is
     * performed and a retry executed depending of the value associated to
     * the property {@code PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES}.
     *
     * @param sessionWork the action to perform.
     * @param <T>         the result type of the session work.
     * @return session work result based on the generic type.
     */
    public <T> T executeReadWriteTransaction(SessionWork<T> sessionWork) {
        return tryExecuteTransactionLoop(sessionWork, true, true);
    }

    public <T> T executeReadWriteTransaction(SessionWork<T> sessionWork, boolean readOnlyEntities) {
        return tryExecuteTransactionLoop(sessionWork, true, readOnlyEntities);
    }

    private <T> T tryExecuteTransactionLoop(SessionWork<T> sessionWork, boolean readWriteTransaction,
            boolean readOnlyEntities) {
        Throwable lastException = null;

        int dampingFactor = PASchedulerProperties.SCHEDULER_DB_TRANSACTION_DAMPING_FACTOR.getValueAsInt();
        int delay = PASchedulerProperties.SCHEDULER_DB_TRANSACTION_SLEEP_DELAY.getValueAsInt();
        int maximumNumberOfRetries = Math.max(PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES.getValueAsInt(),
                                              0);

        for (int i = 0; i <= maximumNumberOfRetries; i++) {
            try {
                return tryExecuteTransaction(sessionWork, readWriteTransaction, readOnlyEntities);
            } catch (Throwable exception) {
                lastException = exception;

                logger.warn(String.format("Database operation failed. Automatic retry in %d ms (attempt %d)", delay, i),
                            exception);

                new Sleeper(delay, logger).sleep();

                delay *= dampingFactor;
            }
        }

        logger.warn("Maximum number of transaction retries exceeded, giving up. Last exception is: ", lastException);

        throw new DatabaseManagerException(lastException);
    }

    private <T> T tryExecuteTransaction(SessionWork<T> sessionWork, boolean readWriteTransaction,
            boolean readOnlyEntities) {
        Session session = sessionFactory.openSession();
        session.setDefaultReadOnly(readOnlyEntities);

        try {
            session.beginTransaction();

            T result = sessionWork.doInTransaction(session);

            session.getTransaction().commit();

            return result;
        } catch (Throwable e) {
            logger.warn("Database operation failed", e);

            if (readWriteTransaction) {
                try {
                    session.getTransaction().rollback();
                } catch (Throwable rollbackError) {
                    logger.warn("Failed to rollback transaction", rollbackError);
                }
            }

            throw e;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                logger.warn("Failed to close session", e);
            }
        }
    }

}
