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
    /**
     *
     */
    public SecurityContext() {
       
    }

    public SecurityContext(int type,ArrayList entitiesFrom,ArrayList entitiesTo) {
        this.type = type;
		this.entitiesFrom =  entitiesFrom;
		this.entitiesTo =  entitiesTo;
       
    }

	public void addEntityFrom(Entity entity) {
		entitiesFrom.add(entity);
	}
	
	public void addEntityTo(Entity entity) {
			entitiesTo.add(entity);
		}
   
	/**
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param i
	 */
	public void setType(int i) {
		type = i;
	}

	/**
	 * @return
	 */
	public boolean isCreationActiveObjectFrom() {
		return creationActiveObjectFrom;
	}

	/**
	 * @return
	 */
	public boolean isCreationActiveObjectTo() {
		return creationActiveObjectTo;
	}

	/**
	 * @return
	 */
	public ArrayList getEntitiesFrom() {
		return entitiesFrom;
	}

	/**
	 * @return
	 */
	public ArrayList getEntitiesTo() {
		return entitiesTo;
	}


	/**
	 * @return
	 */
	public boolean isMigration() {
		return migration;
	}

	/**
	 * @return
	 */
	public Communication getReceiveReply() {
		return receiveReply;
	}

	/**
	 * @return
	 */
	public Communication getReceiveRequest() {
		return receiveRequest;
	}

	/**
	 * @return
	 */
	public Communication getSendReply() {
		return sendReply;
	}

	/**
	 * @return
	 */
	public Communication getSendRequest() {
		return sendRequest;
	}

	/**
	 * @param b
	 */
	public void setCreationActiveObjectFrom(boolean b) {
		creationActiveObjectFrom = b;
	}

	/**
	 * @param b
	 */
	public void setCreationActiveObjectTo(boolean b) {
		creationActiveObjectTo = b;
	}

	/**
	 * @param list
	 */
	public void setEntitiesFrom(ArrayList list) {
		entitiesFrom = list;
	}

	/**
	 * @param list
	 */
	public void setEntitiesTo(ArrayList list) {
		entitiesTo = list;
	}

	/**
	 * @param b
	 */
	public void setMigration(boolean b) {
		migration = b;
	}

	/**
	 * @param b
	 */
	public void setMigrationTo(boolean b) {
		migrationTo = b;
	}

	/**
	 * @param communication
	 */
	public void setReceiveReply(Communication communication) {
		receiveReply = communication;
	}

	/**
	 * @param communication
	 */
	public void setReceiveRequest(Communication communication) {
		receiveRequest = communication;
	}

	/**
	 * @param communication
	 */
	public void setSendReply(Communication communication) {
		sendReply = communication;
	}

	/**
	 * @param communication
	 */
	public void setSendRequest(Communication communication) {
		sendRequest = communication;
	}



}
