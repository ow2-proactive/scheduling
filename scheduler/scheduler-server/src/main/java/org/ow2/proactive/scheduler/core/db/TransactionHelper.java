package org.ow2.proactive.scheduler.core.db;

import org.objectweb.proactive.utils.Sleeper;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.DatabaseManagerExceptionHandler;
import org.ow2.proactive.db.FilteredExceptionCallback;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class TransactionHelper implements FilteredExceptionCallback {

    private static final Logger debugLogger = Logger.getLogger(TransactionHelper.class);
    private final DatabaseManagerExceptionHandler exceptionHandler;
    private SessionFactory sessionFactory;

    ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Lock readLock;
    private Lock writeLock;

    public TransactionHelper(SessionFactory sessionFactory) {
        this.rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();
        this.sessionFactory = sessionFactory;
        this.exceptionHandler = new DatabaseManagerExceptionHandler(
            new Class[] { JDBCConnectionException.class },
            DatabaseManagerExceptionHandler.DBMEHandler.FILTER_ALL, this);
    }

    public <T> T runWithTransaction(SessionWork<T> sessionWork) {
        return runWithTransaction(sessionWork, true);
    }

    public <T> T runWithTransaction(SessionWork<T> sessionWork, boolean readonly) {

        writeLock.lock();
        try {
            final int RETRIES = 5;
            final int DAMPING_FACTOR = 2;
            int delay = 1;
            Exception lastException = null;

            for (int i = 1; i <= RETRIES; i++) {
                try {

                    return tryRunWithTransaction(sessionWork, readonly);

                } catch (LockAcquisitionException exception) {
                    lastException = exception;
                    debugLogger.warn(String.format(
                            "DB operation failed. Will retry in %d seconds (attempt %d). %s", delay, i, exception
                                    .getMessage()));
                    new Sleeper(1000 * delay, debugLogger).sleep();
                    delay *= DAMPING_FACTOR;
                } catch (Throwable throwable) {
                    debugLogger.warn("DB operation failed", throwable);
                    exceptionHandler.handle("DB operation failed", throwable);
                    return null;
                }
            }

            debugLogger.warn("Maximum number of transaction retries exceeded, giving up. Last exception: ",
                    lastException);
            exceptionHandler.handle("DB operation failed", lastException);
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    private <T> T tryRunWithTransaction(SessionWork<T> sessionWork, boolean readonly) throws Throwable {
        writeLock.lock();
        try {
            Session session = sessionFactory.openSession();
            Transaction tx = null;
            try {
                session.setDefaultReadOnly(readonly);
                tx = session.beginTransaction();
                T result = sessionWork.executeWork(session);
                tx.commit();
                debugLogger.trace("Transaction committed");
                return result;
            } catch (Throwable e) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Throwable rollbackError) {
                        debugLogger.warn("Failed to rollback transaction", rollbackError);
                    }
                }
                throw e;
            } finally {
                try {
                    session.close();
                } catch (Throwable e) {
                    debugLogger.warn("Failed to close session", e);
                }

            }
        } finally {
            writeLock.unlock();
        }
    }

    public <T> T runWithoutTransaction(SessionWork<T> sessionWork) {
        readLock.lock();
        try {
            Session session = sessionFactory.openSession();
            try {
                session.setDefaultReadOnly(true);
                T result = sessionWork.executeWork(session);
                return result;
            } catch (Throwable e) {
                debugLogger.warn("DB operation failed", e);
                exceptionHandler.handle("DB operation failed", e);
                return null;
            } finally {
                try {
                    session.close();
                } catch (Throwable e) {
                    debugLogger.warn("Failed to close session", e);
                }

            }
        } finally {
            readLock.unlock();
        }
    }

    public static interface SessionWork<T> {

        T executeWork(Session session);

    }

    @Override
    public void notify(DatabaseManagerException dme) {
        if (this.callback != null) {
            this.callback.notify(dme);
        }
        throw dme;
    }

    private FilteredExceptionCallback callback;

    public void setCallback(FilteredExceptionCallback callback) {
        this.callback = callback;
    }

}