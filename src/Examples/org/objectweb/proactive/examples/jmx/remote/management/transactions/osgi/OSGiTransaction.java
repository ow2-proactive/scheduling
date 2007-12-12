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
package org.objectweb.proactive.examples.jmx.remote.management.transactions.osgi;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.objectweb.proactive.examples.jmx.remote.management.command.CommandMBean;
import org.objectweb.proactive.examples.jmx.remote.management.command.osgi.OSGiCommand;
import org.objectweb.proactive.examples.jmx.remote.management.exceptions.JMXException;
import org.objectweb.proactive.examples.jmx.remote.management.exceptions.TransactionNotActiveException;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.IJmx;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.TransactionCancelledNotification;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.TransactionCommandNotification;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.notifications.TransactionCommitedNotification;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.TransactionsManager;


public class OSGiTransaction extends Transaction implements OSGiTransactionMBean,
    IJmx, Serializable {

    /**
     *
     */
    private Stack<CommandMBean> commands = new Stack<CommandMBean>();
    private ArrayList<CommandMBean> compensation = new ArrayList<CommandMBean>();

    public OSGiTransaction() {
        SecureRandom sr = new SecureRandom();
        this.idTransaction = sr.nextLong();

        this.state = Transaction.ACTIVE;
        try {
            register();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        } catch (JMXException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Status executeCommand(CommandMBean c) {
        if (!c.check()) {
            try {
                ((OSGiCommand) c).register();
                Notification notification = new TransactionCommandNotification(this.getClass()
                                                                                   .getName(),
                        c.getOperation(), seqNumber++,
                        "New command in transaction", this.idTransaction,
                        c.getDate());
                sendNotification(notification);
            } catch (InstanceAlreadyExistsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MBeanRegistrationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NotCompliantMBeanException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JMXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Status s = c.do_();
            this.commands.push(c);
            return s;
        } else {
            this.compensation.add(c);
            return new Status(Status.OK, c.getOperation(),
                "CommandMBean already done, put in compensation list",
                OSGiStore.getInstance().getUrl());
        }
    }

    @Override
    public long getId() {
        return this.idTransaction;
    }

    @Override
    public void compensate() {
        for (CommandMBean c : this.compensation) {
            c.do_();
            this.commands.push(c);
        }
        this.compensation = new ArrayList<CommandMBean>();
    }

    private static int seqNumber;

    @Override
    public Status commit() throws TransactionNotActiveException {
        this.state = Transaction.COMMITED;
        TransactionsManager.getInstance().update(this);
        Notification notification = new TransactionCommitedNotification(OSGiTransaction.class.getName(),
                this, seqNumber++, "Transaction Commited", this.idTransaction,
                null);
        sendNotification(notification);
        return new Status(Status.OK, "commit " + this.idTransaction,
            "Transaction commited", OSGiStore.getInstance().getUrl());
    }

    @Override
    public Status rollback() throws TransactionNotActiveException {
        this.state = Transaction.DEAD;
        while (!commands.empty()) {
            CommandMBean c = commands.pop();
            c.undo_();
        }

        TransactionsManager.getInstance().update(this);
        Notification notification = new TransactionCancelledNotification(OSGiTransaction.class.getName(),
                this, seqNumber++, "Transaction cancelled", this.idTransaction,
                null);
        sendNotification(notification);
        return new Status(Status.OK, "rollback " + this.idTransaction,
            "Transaction cancelled", OSGiStore.getInstance().getUrl());
    }

    public Status rollback(int step) throws TransactionNotActiveException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * IJmx implementation
     * @throws JMXException
     */
    public void register()
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, JMXException {
        ObjectName on;
        try {
            on = new ObjectName("Transactions:id=" + this.idTransaction);
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, on);
        } catch (MalformedObjectNameException e) {
            throw new JMXException(e);
        } catch (NullPointerException e) {
            throw new JMXException(e);
        }
    }

    public void unregister()
        throws InstanceNotFoundException, MBeanRegistrationException,
            JMXException {
        ObjectName on;

        try {
            on = new ObjectName("Transactions:id" + this.idTransaction);
        } catch (MalformedObjectNameException e) {
            throw new JMXException(e);
        } catch (NullPointerException e) {
            throw new JMXException(e);
        }
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(on);
    }

    @Override
    public Vector<CommandMBean> getCommands() {
        return this.commands;
    }

    @Override
    public void removeInCompensation(Vector<CommandMBean> commandsToRemove) {
        this.compensation.removeAll(commandsToRemove);
        if (this.compensation.size() == 0) {
            TransactionsManager.getInstance().deleteTransaction(this);
        }
    }

    @Override
    public ArrayList<CommandMBean> getCompensation() {
        return this.compensation;
    }
}
