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

import org.objectweb.proactive.core.node.rmi.RemoteNodeAdapter;
import org.objectweb.proactive.core.node.NodeException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.rmi.RemoteNode;
import org.objectweb.proactive.core.node.rmi.RemoteNodeAdapter;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.mop.ConstructorCall;

import org.objectweb.proactive.ext.security.crypto.PublicCertificate;
import org.objectweb.proactive.ext.security.crypto.PrivateCertificate;
import java.security.PublicKey;

/**
 *  Description of the Class
 *
 *@author     Arnaud Contes
 *@created    27 juillet 2001
 */
public class SecureRemoteNodeAdapter extends RemoteNodeAdapter {

	/**
	 *  Constructor for the SecureRemoteNodeAdapter object
	 *
	 *@param  s                  Description of Parameter
	 *@param  policyFile         Description of Parameter
	 *@exception  NodeException  Description of Exception
	 */
	public SecureRemoteNodeAdapter(String s, boolean replacePreviousBinding, String policyFile, PublicCertificate publicCertificate,
				       PrivateCertificate privateCertificate, PublicKey publicKey) throws NodeException {
		try {
			this.remoteNode = new SecureRemoteNodeImpl(s, replacePreviousBinding, policyFile,publicCertificate,privateCertificate,publicKey);
		  this.nodeInformation = remoteNode.getNodeInformation();
		}
		catch (java.rmi.RemoteException e) {
			throw new NodeException("RemoteException while getting the name", e);
		}
		catch (java.rmi.AlreadyBoundException e) {
			throw new NodeException("Cannot bound the secure node to " + s, e);
		}
	}


	/**
	 *  Constructor for the SecureRemoteNodeAdapter object
	 *
	 *@param  r                  Description of Parameter
	 *@param  policyFile         Description of Parameter
	 *@exception  NodeException  Description of Exception
	 */
	public SecureRemoteNodeAdapter(SecureRemoteNode r, String policyFile) throws NodeException {
    super(r);
	}



	//
	// -- PUBLIC METHODS -----------------------------------------------
	//


}

