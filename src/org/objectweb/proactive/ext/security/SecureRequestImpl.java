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

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.UniqueID;

import org.objectweb.proactive.ext.security.crypto.SessionInitializer;

import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.ext.security.crypto.SessionException;

/**
 *  Description of the Class
 *
 *@author     Arnaud Contes
 *@created    27 juillet 2001
 */
public class SecureRequestImpl extends RequestImpl implements SecureRequest {

	private long sessionID;
	private Object cryptedObject;
	private Object signedObject;
	private Object signedEncryptedObject;
	private boolean signed = false;


	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	/**
	 *  Constructor for the SecureRequestImpl object
	 *
	 */
	public SecureRequestImpl(MethodCall methodCall, UniversalBody sender,boolean isOneWay, long sequenceID, long sessionID) {
		super(methodCall, sender, isOneWay, sequenceID);
		this.sessionID = sessionID;
	}


	/**
	 *  Sets the SessionID attribute of the SecureRequestImpl object
	 *
	 *@param  sessionID  The new SessionID value
	 */
	public void setSessionID(long sessionID) {
		this.sessionID = sessionID;
	}


	/**
	 *  Gets the SessionID attribute of the SecureRequestImpl object
	 *
	 *@return    The SessionID value
	 */
	public long getSessionID() {
		return sessionID;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public Object encrypt() {
		return null;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  sessionInitializer  Description of Parameter
	 */
	public void encrypt(SessionInitializer sessionInitializer) {
		try {
			cryptedObject = sessionInitializer.encrypt(methodCall);
			methodCall = null;
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
			methodCall = (MethodCall) body.decrypt(cryptedObject, sessionID);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  sessionInitializer  Description of Parameter
	 */
	public void sign(SessionInitializer sessionInitializer) {
		if (cryptedObject != null) {
			signedObject = sessionInitializer.signObject(methodCall);
		}
		else {
			signedEncryptedObject = sessionInitializer.signObject(methodCall);
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
		if (signedObject != null) {
			return body.checkSignature(signedObject, sessionID);
		}
		else {
			return body.checkSignature(signedEncryptedObject, sessionID);
		}
	}


	//
	// -- PROTECTED METHODS -----------------------------------------------
	//
	
	protected Object serveInternal(Body targetBody) throws ServeException {
    try {
      decrypt((SecureBody) targetBody);
      checkSignature((SecureBody) targetBody);
    } catch (Exception e) {
      throw new ServeException("SecureRequestReceiverImpl Exception while checking signature " + e);
    }
    return super.serveInternal(targetBody);
	}
	
	protected Reply createReply(Body targetBody, Object result) {
	  SecureReplyImpl reply =  new SecureReplyImpl(targetBody.getID(), sequenceNumber, methodName, result, sessionID);
    reply.encrypt((SecureBody) targetBody);
    return reply;
	}


}

