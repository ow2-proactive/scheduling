/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.jmx.remote.management.transactions;

import java.lang.management.ManagementFactory;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.objectweb.proactive.examples.jmx.remote.management.exceptions.InvalidTransactionException;
import org.objectweb.proactive.examples.jmx.remote.management.exceptions.JMXException;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.IJmx;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.osgi.OSGiTransaction;
import org.objectweb.proactive.examples.jmx.remote.management.utils.Constants;


public class TransactionsManager implements TransactionsManagerMBean, IJmx {
    private HashMap<Long, Transaction> transactionsMap;
    private HashMap<Transaction, Vector<Transaction>> concurrentTransactions;
    private static TransactionsManager instance;
    private ObjectName on;
    private String url;

    private TransactionsManager(String url) throws JMXException {
        try {
            this.url = url;
            this.on = new ObjectName(Constants.ON_TRANSACTION_MANAGER);

            this.transactionsMap = new HashMap<Long, Transaction>();
            this.concurrentTransactions = new HashMap<Transaction, Vector<Transaction>>();
        } catch (MalformedObjectNameException e) {
            throw new JMXException(e);
        } catch (NullPointerException e) {
            throw new JMXException(e);
        }
    }

    public static TransactionsManager getInstance() {
        return instance;
    }

    public static TransactionsManager getInstance(String url) throws JMXException {
        if (instance == null) {
            instance = new TransactionsManager(url);
            try {
                instance.register();
            } catch (InstanceAlreadyExistsException e) {
                throw new JMXException(e);
            } catch (MBeanRegistrationException e) {
                throw new JMXException(e);
            } catch (NotCompliantMBeanException e) {
                throw new JMXException(e);
            }
        }
        return instance;
    }

    public Long openTransaction() {
        Transaction t = new OSGiTransaction();

        this.transactionsMap.put(t.getId(), t);
        this.concurrentTransactions.put(t, new Vector<Transaction>());
        update(t);
        return new Long(t.getId());
    }

    public Transaction getTransaction(long id) throws InvalidTransactionException {
        Transaction t = this.transactionsMap.get(id);

        if ((t != null) && (t.getState() != Transaction.COMMITED) && (t.getState() != Transaction.DEAD)) {
            return t;
        }
        throw new InvalidTransactionException(id);
    }

    public void update(Transaction transaction) {
        if (transaction.getState() == Transaction.ACTIVE) {
            Iterator<Transaction> i = this.concurrentTransactions.keySet().iterator();
            while (i.hasNext()) {
                Transaction t = i.next();
                if (t.getState() == Transaction.ACTIVE) {
                    Vector<Transaction> v = this.concurrentTransactions.get(t);
                    v.addElement(transaction);
                }
            }
        } else if (transaction.getState() == Transaction.COMMITED) {
            if (transaction.getCompensation().size() == 0) {
                deleteTransaction(transaction);
                return;
            }
            Vector<Transaction> ct = this.concurrentTransactions.get(transaction);
            Enumeration<Transaction> e = ct.elements();
            while (e.hasMoreElements()) {
                Transaction t = e.nextElement();
                t.removeInCompensation(transaction.getCommands());
            }
        } else if (transaction.getState() == Transaction.DEAD) {
            Vector<Transaction> v = this.concurrentTransactions.get(transaction);
            for (Transaction t : v) {
                t.compensate();
            }
            deleteTransaction(transaction);
        }
    }

    public void deleteTransaction(Transaction transaction) {
        Iterator<Transaction> i = this.concurrentTransactions.keySet().iterator();
        while (i.hasNext()) {
            Transaction t = i.next();
            Vector<Transaction> v = this.concurrentTransactions.get(t);
            v.remove(transaction);
        }
        concurrentTransactions.remove(transaction);
    }

    /**
     * IJMx implementation
     */
    public void register() throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, on);
    }

    public void unregister() throws InstanceNotFoundException, MBeanRegistrationException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(on);
    }
}
