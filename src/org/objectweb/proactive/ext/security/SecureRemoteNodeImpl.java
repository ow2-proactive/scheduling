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

import org.objectweb.proactive.core.node.oldies.rmi.RemoteNodeImpl;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;

import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import org.objectweb.proactive.ext.security.crypto.PublicCertificate;
import org.objectweb.proactive.ext.security.crypto.PrivateCertificate;

import java.security.PublicKey;

/**
 *  Description of the Class
 *
 *@author     Arnaud Contes
 *<br>created    27 juillet 2001
 */
public class SecureRemoteNodeImpl extends RemoteNodeImpl implements SecureRemoteNode {

	/**
	 *  The fully-qualified Internet host name of the machine this Node is running
	 *  on.
	 */
  protected PublicCertificate publicCertificate;
  protected PrivateCertificate privateCertificate;
  protected PublicKey publicKey;

	// add here scanner & plop

	//
	// -- Constructors -----------------------------------------------
	//


	/**
	 *  Constructor for the SecureRemoteNodeImpl object
	 */
	public SecureRemoteNodeImpl() throws RemoteException {
	}

	public SecureRemoteNodeImpl(String url, String policyFile,PublicCertificate publicCertificate,
				    PrivateCertificate privateCertificate, PublicKey publicKey) throws RemoteException, AlreadyBoundException {
    this(url, false, policyFile, publicCertificate, privateCertificate, publicKey);
	}
  
  
	/**
	 *  Creates a Node object, then bounds it in registry
	 */
	public SecureRemoteNodeImpl(String url, boolean replacePreviousBinding, String policyFile,PublicCertificate publicCertificate,
				    PrivateCertificate privateCertificate, PublicKey publicKey) throws RemoteException, AlreadyBoundException {
		super(url, replacePreviousBinding);
		this.publicCertificate = publicCertificate;
		this.privateCertificate = privateCertificate;
		this.publicKey = publicKey;
		System.out.println("policy file is : " + policyFile);
	}



	//
	// -- PUBLIC METHODS -----------------------------------------------
	//


	//
	// -- Implements RemoteNode -----------------------------------------------
	//

	/**
	 *  Find which body class is to be instanciated, then instanciate it and feeds
	 *  it with the call representing the reified object to be created.
	 */
	public UniversalBody createBody(ConstructorCall c) throws ConstructorCallExecutionFailedException, java.lang.reflect.InvocationTargetException  {
		//System.out.println (this.nodeURL+" -> new "+c.getTargetClassName());
		System.out.println("checking right on create body");

		// waiting for indians parser ....
		
	 //System.out.println (this.nodeURL+" -> new "+c.getTargetClassName());
	 return super.createBody(c);  
	}


	/**
	 *  Receive the Agent from a Remote Node and make it run
	 */
	public UniversalBody receiveBody(Body b) {
		System.out.println("checking right in receiveAgent ");
		// waiting for indians parser ....
		return super.receiveBody(b);
	}

}

