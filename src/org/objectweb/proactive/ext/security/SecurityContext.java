/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ext.security;

import java.io.Serializable;

import java.util.ArrayList;


/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SecurityContext implements Serializable {
    public static int COMMUNICATION_SEND_REQUEST_TO = 0;
    public static int COMMUNICATION_RECEIVE_REQUEST_FROM = 1;
    public static int COMMUNICATION_SEND_REPLY_TO = 2;
    public static int COMMUNICATION_RECEIVE_REPLY_FROM = 3;
    public static int MIGRATION_TO = 4;
    public static int MIGRATION_FROM = 5;
    protected ArrayList entitiesFrom;
    protected ArrayList entitiesTo;
    protected Communication sendRequest;
    protected Communication receiveRequest;
    protected Communication sendReply;
    protected Communication receiveReply;
    protected boolean migration;
    protected boolean migrationTo;
    protected boolean creationActiveObjectFrom;
    protected boolean creationActiveObjectTo;
    protected int type;

    public SecurityContext() {
    }

    public SecurityContext(int type, ArrayList entitiesFrom,
        ArrayList entitiesTo) {
        this.type = type;
        this.entitiesFrom = entitiesFrom;
        this.entitiesTo = entitiesTo;
    }

    public void addEntityFrom(Entity entity) {
        entitiesFrom.add(entity);
    }

    public void addEntityTo(Entity entity) {
        entitiesTo.add(entity);
    }

    /**
     * @return type of the interaction (migration, request, reply)
     */
    public int getType() {
        return type;
    }

    /**
     * @param set the type of the interaction (migration, request, reply)
     */
    public void setType(int i) {
        type = i;
    }

    /**
     * @return true if creation of an active object is authorized by the from entities
     */
    public boolean isCreationActiveObjectFrom() {
        return creationActiveObjectFrom;
    }

    /**
     * @return true if creation of an active object is authorized to the 'to' entities
     */
    public boolean isCreationActiveObjectTo() {
        return creationActiveObjectTo;
    }

    /**
     * @return entities of the 'from' objects
     */
    public ArrayList getEntitiesFrom() {
        return entitiesFrom;
    }

    /**
     * @return entities of the 'to' objects
     */
    public ArrayList getEntitiesTo() {
        return entitiesTo;
    }

    /**
     * @return true if migration is granted
     */
    public boolean isMigration() {
        return migration;
    }

    /**
     * @return true if object can receive replies
     */
    public Communication getReceiveReply() {
        return receiveReply;
    }

    /**
     * @return true if object can receive requests
     */
    public Communication getReceiveRequest() {
        return receiveRequest;
    }

    /**
     * @return true if object can send replies
     */
    public Communication getSendReply() {
        return sendReply;
    }

    /**
     * @return true if object can send requests
     */
    public Communication getSendRequest() {
        return sendRequest;
    }

    /**
     * @param true if object on 'from' can create object
     */
    public void setCreationActiveObjectFrom(boolean b) {
        creationActiveObjectFrom = b;
    }

    /**
     * @param true if object is authorized to create onject on 'to'
     */
    public void setCreationActiveObjectTo(boolean b) {
        creationActiveObjectTo = b;
    }

    /**
     * @param lists all entities from 'from'
     */
    public void setEntitiesFrom(ArrayList list) {
        entitiesFrom = list;
    }

    /**
     * @param lists all entities from 'to'
     */
    public void setEntitiesTo(ArrayList list) {
        entitiesTo = list;
    }

    /**
     * @param true if migration is granted
     */
    public void setMigration(boolean b) {
        migration = b;
    }

    /**
     * @param true if migration is granted to
     */
    public void setMigrationTo(boolean b) {
        migrationTo = b;
    }

    /**
     * @param communication attributes for receiving a reply
     */
    public void setReceiveReply(Communication communication) {
        receiveReply = communication;
    }

    /**
     * @param communication attributes for receiving a request
     */
    public void setReceiveRequest(Communication communication) {
        receiveRequest = communication;
    }

    /**
     * @param communication attributes for send a reply
     */
    public void setSendReply(Communication communication) {
        sendReply = communication;
    }

    /**
     * @param communication attributes for send a request
     */
    public void setSendRequest(Communication communication) {
        sendRequest = communication;
    }
}
