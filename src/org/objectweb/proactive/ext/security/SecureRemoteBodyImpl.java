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

import org.objectweb.proactive.core.body.rmi.RemoteBodyImpl;
import org.objectweb.proactive.ext.security.crypto.SessionsManagerInt;

import org.objectweb.proactive.ext.security.crypto.SessionsManagerInt;
import org.objectweb.proactive.ext.security.crypto.SessionsManager;
import org.objectweb.proactive.ext.security.crypto.Session;
import org.objectweb.proactive.ext.security.crypto.SessionException;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.crypto.AuthenticationTicket;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.PublicCertificate;
import org.objectweb.proactive.ext.security.crypto.PrivateCertificate;

import java.io.Serializable;

/**
 *  Description of the Class
 *
 *@author     Arnaud Contes
 *@created    27 juillet 2001
 */
public class SecureRemoteBodyImpl extends RemoteBodyImpl implements SecureRemoteBody {

	private SecureBody secureBody;


	/**
	 *  Constructor for the SecureRemoteBodyImpl object
	 *
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public SecureRemoteBodyImpl() throws java.rmi.RemoteException {
	}


	/**
	 *  Constructor for the SecureRemoteBodyImpl object
	 *
	 *@param  secureBody                    Description of Parameter
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public SecureRemoteBodyImpl(SecureBody secureBody) throws java.rmi.RemoteException {
		super(secureBody);
		this.secureBody = secureBody;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  publicCertificate             Description of Parameter
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
// 	public void set_publicCertificate(PublicCertificate publicCertificate) throws java.rmi.RemoteException {
// 		secureBody.set_publicCertificate(publicCertificate);
// 	}


	/**
	 *  Description of the Method
	 *
	 *@param  privateCertificate            Description of Parameter
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public void set_privateCertificate(PrivateCertificate privateCertificate) throws java.rmi.RemoteException {
		secureBody.sessionsManager.set_privateCertificate(privateCertificate);
	}


	/**
	 *  Description of the Method
	 *
	 *@return                               Description of the Returned Value
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public String getDomainName() throws java.rmi.RemoteException {
		return secureBody.sessionsManager.getDomainName();
	}


	/**
	 *  Description of the Method
	 *
	 *@return                               Description of the Returned Value
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public PublicCertificate getPublicCertificate() throws java.rmi.RemoteException {
		return secureBody.sessionsManager.getPublicCertificate();
	}


	//
	// -- implements SessionsManagerInt  -----------------------------------------------
	//


	/**
	 *  Description of the Method
	 *
	 *@param  randomID                      Description of Parameter
	 *@exception  SessionException          Description of Exception
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public void initiateSession(long randomID) throws SessionException, java.rmi.RemoteException {
		secureBody.sessionsManager.initiateSession(randomID);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  randomID                      Description of Parameter
	 *@return                               Description of the Returned Value
	 *@exception  AuthenticationException   Description of Exception
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public long give_me_a_random(long randomID) throws AuthenticationException, java.rmi.RemoteException {
		return secureBody.sessionsManager.give_me_a_random(randomID);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  authenticationTicket          Description of Parameter
	 *@param  randomID                      Description of Parameter
	 *@return                               Description of the Returned Value
	 *@exception  AuthenticationException   Description of Exception
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public AuthenticationTicket authenticate_mutual(AuthenticationTicket authenticationTicket, long randomID) throws AuthenticationException, java.rmi.RemoteException {
		return secureBody.sessionsManager.authenticate_mutual(authenticationTicket, randomID);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  randomID                      Description of Parameter
	 *@param  rb                            Description of Parameter
	 *@param  emittor                       Description of Parameter
	 *@return                               Description of the Returned Value
	 *@exception  AuthenticationException   Description of Exception
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public AuthenticationTicket authenticate_unilateral(long randomID, long rb, String emittor) throws AuthenticationException, java.rmi.RemoteException {
		return secureBody.sessionsManager.authenticate_unilateral(randomID, rb, emittor);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  confidentialityTicket         Description of Parameter
	 *@param  randomID                      Description of Parameter
	 *@return                               Description of the Returned Value
	 *@exception  KeyExchangeException      Description of Exception
	 *@exception  java.rmi.RemoteException  Description of Exception
	 */
	public ConfidentialityTicket negociate_key(ConfidentialityTicket confidentialityTicket, long randomID) throws KeyExchangeException, java.rmi.RemoteException {
		return secureBody.sessionsManager.negociate_key(confidentialityTicket, randomID);
	}
    
    public void addSession(long sessionID, Session newSession) throws SessionException, java.rmi.RemoteException {
	secureBody.sessionsManager.addSession(sessionID,newSession);
    }

    public void killSession(long sessionID) throws SessionException, java.rmi.RemoteException {
	secureBody.sessionsManager.killSession(sessionID);
    }

    public Object encrypt(Serializable object,long sessionID) throws SessionException, java.rmi.RemoteException  {
	return secureBody.sessionsManager.encrypt(object,sessionID);
    }

    public Object decrypt(Object object, long sessionID) throws SessionException, java.rmi.RemoteException { 
	return secureBody.sessionsManager.decrypt(object,sessionID);
    }


    
    public Object signObject(Serializable object) throws SessionException, java.rmi.RemoteException {
	return secureBody.sessionsManager.signObject(object);
    }
    
    
    public boolean checkSignature(Object signedObject, long ID) throws SessionException, java.rmi.RemoteException {
	return secureBody.sessionsManager.checkSignature(signedObject,ID);
    }
    


}

