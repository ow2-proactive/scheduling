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

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.Request;

import org.objectweb.proactive.core.UniqueID;

import org.objectweb.proactive.ext.security.crypto.SessionException;

import java.io.*;
import java.security.SignedObject;

/**
 *  Description of the Class
 *
 *@author     Arnaud Contes
 *<br>created    27 juillet 2001
 */
public class SecureReplyImpl extends ReplyImpl implements SecureReply {

	private long sessionID;
	private Object cryptedObject;
	private Object signedEncryptedObject;
	private Object signedObject;


	//
	// -- CONSTRUCTORS -----------------------------------------------
	//


	public SecureReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, Object result, long sessionID) {
	  super(senderID, sequenceNumber, methodName, result);
	  this.sessionID = sessionID;
	}

	/**
	 *  Sets the SessionID attribute of the SecureReplyImpl object
	 *
	 *@param  sessionID  The new SessionID value
	 */
	public void setSessionID(long sessionID) {
		this.sessionID = sessionID;
	}


	/**
	 *  Gets the SessionID attribute of the SecureReplyImpl object
	 *
	 *@return    The SessionID value
	 */
	public long getSessionID() {
		return sessionID;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  secureBody  Description of Parameter
	 */
	public void encrypt(SecureBody secureBody) {
		try {
			this.cryptedObject = secureBody.encrypt((Serializable) this.result, sessionID);
			this.result = null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  body  Description of Parameter
	 */
	public void decrypt(SecureBody body) {
		try {
			this.result = (Object) body.decrypt(cryptedObject, sessionID);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  secureBody  Description of Parameter
	 */
	public void sign(SecureBody secureBody) {
		try {
			if (cryptedObject != null) {
				signedObject = secureBody.signObject((Serializable) this.result);
			}
			else {
				signedEncryptedObject = secureBody.signObject((Serializable) this.cryptedObject);
			}
		}
		catch (Exception e) {
			System.out.println("SecureReplyImpl Exception while signing " + e);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  body                          Description of Parameter
	 *@return                               Description of the Returned Value
	 *@exception  SessionException          Description of Exception
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public boolean checkSignature(SecureBody body) throws SessionException, java.rmi.RemoteException {
		try {
			if (signedObject != null) {
				if (body.checkSignature(signedObject, sessionID)) {
					this.result = ((SignedObject) signedObject).getObject();
					return true;
				}
			}
			else {
				if (body.checkSignature(signedEncryptedObject, sessionID)) {
					this.cryptedObject = ((SignedObject) signedEncryptedObject).getObject();
					return true;
				}
			}
		}
		catch (Exception e) {
			System.out.println("SecureReplyImpl Exception while checking signature " + e);
		}
		return false;
	}
}

