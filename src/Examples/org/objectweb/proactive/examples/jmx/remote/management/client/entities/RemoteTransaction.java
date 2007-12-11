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
package org.objectweb.proactive.examples.jmx.remote.management.client.entities;

import java.io.Serializable;
import java.util.ArrayList;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.examples.jmx.remote.management.events.EntitiesEventManager;


public class RemoteTransaction extends ManageableEntity implements Serializable {
    public static final long ACTIVE = 0;
    public static final long COMMITED = 1;
    public static final long CANCELLED = 2;
    private long id;
    private ObjectName transactionON;
    private ManageableEntity parent;
    private String url;
    private ArrayList<RemoteCommand> commandList = new ArrayList<RemoteCommand>();
    private long state;
    private RemoteGateway gateway;

    public RemoteTransaction(long id, String url, RemoteGateway gateway) {
        this.id = id;
        this.url = url;
        RemoteTransactionManager.getInstance().addTransaction(this);
        this.parent = RemoteTransactionManager.getInstance()
                                              .getTransactionsGroup();
        this.state = ACTIVE;
        this.gateway = gateway;
        try {
            this.transactionON = new ObjectName("Transactions:id=" + id);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        EntitiesEventManager.getInstance()
                            .newEvent(this,
            EntitiesEventManager.TRANSACTION_OPENED);
        EntitiesEventManager.getInstance().listenTo(this);
    }

    public void setState(long state) {
        this.state = state;
        EntitiesEventManager.getInstance()
                            .newEvent(this,
            "Transaction " + this.id + " changed state.");
    }

    public long getState() {
        return this.state;
    }

    @Override
    public void addEntity(ManageableEntity entity) {
        this.commandList.add((RemoteCommand) entity);
    }

    @Override
    public Object[] getChildren() {
        return this.commandList.toArray();
    }

    @Override
    public String getName() {
        return this.url + '[' + this.id + ']';
    }

    @Override
    public ManageableEntity getParent() {
        return this.parent;
    }

    @Override
    public boolean hasChildren() {
        return this.commandList.size() > 0;
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeEntity(ManageableEntity entity) {
        this.commandList.remove(entity);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public ProActiveConnection getConnection() {
        return this.gateway.getConnection();
    }

    @Override
    public ObjectName getObjectName() {
        return this.transactionON;
    }

    @Override
    public String getUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getId() {
        return this.id;
    }
}
