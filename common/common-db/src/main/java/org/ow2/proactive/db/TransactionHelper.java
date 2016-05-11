package org.ow2.proactive.db;

import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.LockAcquisitionException;


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
     *
     * @param sessionWork the action to perform.
     * @param <T>         the result type of the session work.
     * @return session work result based on the generic type.
     */
    public <T> T executeReadOnlyTransaction(SessionWork<T> sessionWork) {
        return tryExecuteTransaction(sessionWork, false, true);
    }

    /**
     * Execute the specified {@code sessionWork} in a session configured
     * for read/write interactions with the database. This method should be
     * used for insert, update, and delete queries. If select only is required,
     * then {@link #executeReadOnlyTransaction(SessionWork)} could be used.
     * <p>
     * In case of database error while executing the query, a rollback is
     * performed.
     *
     * @param sessionWork the action to perform.
     * @param <T>         the result type of the session work.
     * @return session work result based on the generic type.
     */
    public <T> T executeReadWriteTransaction(SessionWork<T> sessionWork) {
        return executeReadWriteTransaction(sessionWork, true);
    }

    public <T> T executeReadWriteTransaction(SessionWork<T> sessionWork, boolean readOnlyEntities) {
        Exception lastException = null;

        int dampingFactor =
                PASchedulerProperties.SCHEDULER_DB_TRANSACTION_DAMPING_FACTOR.getValueAsInt();
        int delay =
                PASchedulerProperties.SCHEDULER_DB_TRANSACTION_SLEEP_DELAY.getValueAsInt();
        int maximumNumberOfRetries =
                Math.max(PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES.getValueAsInt(), 0);

        for (int i = 0; i <= maximumNumberOfRetries; i++) {
            try {
                return tryExecuteTransaction(sessionWork, true, readOnlyEntities);
            } catch (LockAcquisitionException exception) {
                lastException = exception;

                logger.warn(
                        String.format(
                                "Database operation failed. Automatic retry in %d ms (attempt %d)",
                                delay, i), exception);

                new Sleeper(delay, logger).sleep();

                delay *= dampingFactor;
            } catch (Throwable throwable) {
                logger.warn("Database operation failed", throwable);
                throw new DatabaseManagerException(throwable);
            }
        }

        logger.warn(
                "Maximum number of transaction retries exceeded, giving up. Last exception is: ",
                lastException);

        return null;
    }

    private <T> T tryExecuteTransaction(SessionWork<T> sessionWork,
            boolean readWriteTransaction, boolean readOnlyEntities) {
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