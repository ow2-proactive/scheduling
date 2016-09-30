package org.ow2.proactive.scheduler.core.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.db.TransactionHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;


public class TransactionHelperTest {

    private TransactionHelper transactionHelper;
    private SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;
    private SessionWork sessionWork;

    @Before
    public void setUp() {
        sessionFactory = mock(SessionFactory.class);
        session = mock(Session.class);
        transaction = mock(Transaction.class);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.getTransaction()).thenReturn(transaction);

        transactionHelper = new TransactionHelper(sessionFactory);
        sessionWork = mock(SessionWork.class);
    }

    @Test
    public void testExecuteReadOnlyTransaction() {
        when(sessionWork.doInTransaction(session)).thenReturn(null);

        transactionHelper.executeReadOnlyTransaction(sessionWork);

        verify(session).beginTransaction();
        verify(sessionWork).doInTransaction(session);
        verify(transaction, never()).rollback();
        verify(transaction).commit();
    }

    @Test
    public void testExecuteReadOnlyTransactionNoRetry() {
        PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES.updateProperty("0");

        when(sessionWork.doInTransaction(session)).thenThrow(Throwable.class).thenReturn(null);

        try {
            transactionHelper.executeReadOnlyTransaction(sessionWork);
            fail("Exception expected but no one raised");
        } catch (DatabaseManagerException t) {
            verify(session).beginTransaction();
            verify(sessionWork).doInTransaction(session);
            verify(transaction, never()).commit();
            verify(transaction, never()).rollback();
        }
    }

    @Test
    public void testExecuteReadOnlyTransactionRetry() {
        PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES.updateProperty("1");

        when(sessionWork.doInTransaction(session)).thenThrow(Throwable.class).thenReturn(null);

        transactionHelper.executeReadOnlyTransaction(sessionWork);
        verify(session, times(2)).beginTransaction();
        verify(sessionWork, times(2)).doInTransaction(session);
        verify(transaction).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    public void testExecuteReadOnlyTransactionFail() {
        PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES.updateProperty("0");

        when(sessionWork.doInTransaction(session)).thenThrow(Throwable.class);

        try {
            transactionHelper.executeReadOnlyTransaction(sessionWork);
            fail("Exception expected but no one raised");
        } catch (DatabaseManagerException e) {
            verify(session).beginTransaction();
            verify(sessionWork).doInTransaction(session);
            verify(transaction, never()).rollback();
            verify(transaction, never()).commit();
        }
    }

    @Test
    public void testExecuteReadWriteTransaction() {
        when(sessionWork.doInTransaction(session)).thenReturn(null);

        transactionHelper.executeReadWriteTransaction(sessionWork);

        verify(session).beginTransaction();
        verify(sessionWork).doInTransaction(session);
        verify(transaction).commit();
    }

    @Test
    public void testExecuteReadWriteTransactionNoRetry() {
        PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES.updateProperty("0");

        when(sessionWork.doInTransaction(session)).thenThrow(Throwable.class);

        try {
            transactionHelper.executeReadWriteTransaction(sessionWork);
            fail("Exception expected but no one raised");
        } catch (DatabaseManagerException t) {
            verify(session).beginTransaction();
            verify(sessionWork).doInTransaction(session);
            verify(transaction).rollback();
            verify(transaction, never()).commit();
        }
    }

    @Test
    public void testExecuteReadWriteTransactionRetry() {
        PASchedulerProperties.SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES.updateProperty("5");

        when(sessionWork.doInTransaction(session))
                .thenThrow(LockAcquisitionException.class)
                .thenThrow(Throwable.class)
                .thenReturn(null);

        transactionHelper.executeReadWriteTransaction(sessionWork);

        verify(session, times(3)).beginTransaction();
        verify(sessionWork, times(3)).doInTransaction(session);
        verify(transaction, times(2)).rollback();
        verify(transaction).commit();
    }

    @Test
    public void testExecuteReadWriteTransactionFail() {
        when(sessionWork.doInTransaction(session)).thenThrow(Throwable.class);

        try {
            transactionHelper.executeReadWriteTransaction(sessionWork);
            fail("Exception expected but no one raised");
        } catch (DatabaseManagerException e) {
            verify(session).beginTransaction();
            verify(sessionWork).doInTransaction(session);
            verify(transaction).rollback();
            verify(transaction, never()).commit();
        }
    }

}
