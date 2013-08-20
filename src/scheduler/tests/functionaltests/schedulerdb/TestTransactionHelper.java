package functionaltests.schedulerdb;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.core.db.TransactionHelper;
import org.ow2.proactive.scheduler.core.db.TransactionHelper.SessionWork;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestTransactionHelper {

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

        transactionHelper = new TransactionHelper(sessionFactory);
        sessionWork = mock(SessionWork.class);
    }

    @Test
    public void testRunWithTransaction() {
        when(sessionWork.executeWork(session)).thenReturn(null);

        transactionHelper.runWithTransaction(sessionWork);

        verify(session).beginTransaction();
        verify(sessionWork).executeWork(session);
        verify(transaction).commit();
    }

    @Test
    public void testRunWithTransactionRetry() {
        when(sessionWork.executeWork(session)).thenThrow(LockAcquisitionException.class).thenReturn(null);

        transactionHelper.runWithTransaction(sessionWork);

        verify(session, times(2)).beginTransaction();
        verify(sessionWork, times(2)).executeWork(session);
        verify(transaction).rollback();
        verify(transaction).commit();
    }

    @Test
    public void testRunWithTransactionFail() {
        when(sessionWork.executeWork(session)).thenThrow(Throwable.class);

        try {
            transactionHelper.runWithTransaction(sessionWork);
            Assert.fail("Should throw an exception");
        } catch (DatabaseManagerException e) {
        }

        verify(session).beginTransaction();
        verify(sessionWork).executeWork(session);
        verify(transaction).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    public void testRunWithoutTransaction() {
        when(sessionWork.executeWork(session)).thenReturn(null);

        transactionHelper.runWithoutTransaction(sessionWork);

        verify(session, never()).beginTransaction();
        verify(sessionWork).executeWork(session);
    }
}
