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


import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.PublicKey;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.crypto.PrivateCertificate;
import org.objectweb.proactive.ext.security.crypto.PublicCertificate;
import org.objectweb.proactive.ext.security.crypto.SessionInitializer;
import org.objectweb.proactive.ext.security.crypto.SessionsManagerInt;


/**
 *  Description of the Class
 *
 *@author     Arnaud Contes
 *@created    27 juillet 2001
 */
public class SecureProxy extends UniversalBodyProxy {

	/**
	 *  Description of the Field
	 */
	 public transient SessionInitializer sessionInitializer = null;
   
	 public transient RequestFactory secureRequestFactory;


	/**
	 *  Constructor for the SecureProxy object
	 *
	 *@param  c                                               Description of
	 *      Parameter
	 *@param  p                                               Description of
	 *      Parameter
	 *@exception  ConstructionOfReifiedObjectFailedException  Description of
	 *      Exception
	 */
	public SecureProxy(ConstructorCall c, Object[] p) throws ProActiveException {
		super(c, p);

		try {

			FileInputStream fin = new FileInputStream("/net/home/acontes/dev/ProActive/classes/certif_public1");
			ObjectInputStream in = new ObjectInputStream(fin);

			PublicCertificate publicCertificate = (PublicCertificate) in.readObject();
			in.close();

			fin = new FileInputStream("/net/home/acontes/dev/ProActive/classes/certif_private1");
			in = new ObjectInputStream(fin);
			PrivateCertificate privateCertificate = (PrivateCertificate) in.readObject();

			in.close();

			fin = new FileInputStream("/net/home/acontes/dev/ProActive/classes/acPublicKey");
			in = new ObjectInputStream(fin);
			PublicKey acPublicKey = (PublicKey) in.readObject();

			in.close();
			System.out.println("SessionInitializer instantiation... in SecureProxy");

			this.sessionInitializer = new SessionInitializer(publicCertificate, privateCertificate, acPublicKey);

		} catch (Exception e) {
			System.out.println("Exception in setting certificate " + e);
		}

    secureRequestFactory = new SecureRequestFactory();
		System.out.println("this.sessionInitializer " + this.sessionInitializer);
	}


	/**
	 *  Constructor for the SecureProxy object
	 */
	public SecureProxy(ConstructorCall c, Object[] p, PublicCertificate publicCertificate, PrivateCertificate privateCertificate, PublicKey acPublicKey) throws ProActiveException {
		super(c, p);
		this.sessionInitializer = new SessionInitializer(publicCertificate, privateCertificate, acPublicKey);
	}


	//
	// -- PROTECTED METHODS -----------------------------------------------
	//

	/**
	 *  Description of the Method
	 *
	 *@param  r  Description of Parameter
	 */
	private void policy(SecureRequest r) {
	}


	protected void sendRequestInternal(MethodCall methodCall, Future future, Body sourceBody) throws java.io.IOException {
	  if (isLocal) {
	  
	    System.out.println("SecureProxy : SecureLocalBodyProxy : sendRequest  " + methodCall.getName() + " to local");
	    sourceBody.sendRequest(methodCall, future, universalBody, secureRequestFactory);
	    
	  } else {
	  
	    System.out.println("SecureProxy : SecureRemoteBodyProxy : sendRequest  " + methodCall.getName() + " to remote");
	    //	System.out.println(sessionInitializer);

	    // Test if a session exists
	    if (!sessionInitializer.sessionInitiated) {
	  	  sessionInitializer.initiateSession((SessionsManagerInt) ((SecureRemoteBodyAdapter) this.universalBody));
	  	  System.out.println("Session initiated");

	  	  // get policy
	  	  try {
	  		  sessionInitializer.authenticate_mutual((SessionsManagerInt) ((SecureRemoteBodyAdapter) this.universalBody));
	  	  } catch (Exception e) {
	  		  System.out.println("Exception " + e);
	  	  }
	  	  if (sessionInitializer.authentication) {
	  		  System.out.println("Authentication mutual succeeded");
	  	  }
	    }

	    if (!sessionInitializer.keyNegociation) {
	  	  try {
	  		  sessionInitializer.negociate_key((SessionsManagerInt) ((SecureRemoteBodyAdapter) this.universalBody));
	  	  }
	  	  catch (KeyExchangeException e) {
	  		  System.out.println(e);
	  	  }
	  	  System.out.println("key negociated");
	    }

	    sourceBody.sendRequest(methodCall, future, universalBody, secureRequestFactory);
	  }
	}



  private class SecureRequestFactory implements RequestFactory {
  
    public Request newRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
      SecureRequestImpl request = new SecureRequestImpl(methodCall, sourceBody, isOneWay, sequenceID, sessionInitializer.get_sessionID());
      request.encrypt(sessionInitializer);
      if (! isLocal) request.sign(sessionInitializer);
      return request;
    }
  }
}

