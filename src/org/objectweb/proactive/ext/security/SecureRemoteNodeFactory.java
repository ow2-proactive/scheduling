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

import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.rmi.RemoteNodeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.rmi.RemoteNode;
import org.objectweb.proactive.core.node.rmi.RemoteNodeAdapter;

import org.objectweb.proactive.ext.security.crypto.*;
import java.security.PublicKey;

import java.io.*;

/**
 *  Description of the Class
 *
 *@author     Arnaud Contes
 *@created    27 juillet 2001
 */
public class SecureRemoteNodeFactory extends RemoteNodeFactory {

	private String policyFile;
	private PublicCertificate publicCertificate;
	private PrivateCertificate privateCertificate;
	private PublicKey acPublicKey;


	/**
	 *  Constructor for the SecureRemoteNodeFactory object
	 */
	public SecureRemoteNodeFactory() throws java.io.IOException {
	}


	/**
	 *  Constructor for the SecureRemoteNodeFactory object
	 *
	 *@param  publicCertificate   Description of Parameter
	 *@param  privateCertificate  Description of Parameter
	 *@param  acPublicKey         Description of Parameter
	 */
	public SecureRemoteNodeFactory(PublicCertificate publicCertificate, PrivateCertificate privateCertificate, PublicKey acPublicKey)  throws java.io.IOException {
		this.publicCertificate = publicCertificate;
		this.privateCertificate = privateCertificate;
		this.acPublicKey = acPublicKey;
	}


	/**
	 *  Constructor for the SecureRemoteNodeFactory object
	 *
	 *@param  public_certifFile   Description of Parameter
	 *@param  private_certifFile  Description of Parameter
	 *@param  acPublicKeyFile     Description of Parameter
	 */
	public SecureRemoteNodeFactory(String public_certifFile, String private_certifFile, String acPublicKeyFile)  throws java.io.IOException {
    ObjectInputStream in = null;
    try {
		  FileInputStream fin = new FileInputStream(public_certifFile);
		  in = new ObjectInputStream(fin);
		  this.publicCertificate = (PublicCertificate) in.readObject();
		  in.close();

		  fin = new FileInputStream(private_certifFile);
		  in = new ObjectInputStream(fin);
		  this.privateCertificate = (PrivateCertificate) in.readObject();
		  in.close();

		  fin = new FileInputStream(acPublicKeyFile);
		  in = new ObjectInputStream(fin);
		  this.acPublicKey = (PublicKey) in.readObject();
		  in.close();
    } catch (ClassNotFoundException e) {
      throw new java.io.IOException("Cannot find the class definition of the object read in the file");
    } finally {
      if (in != null) in.close();
    }
	}


	/**
	 *  Set the value of policyFile.
	 *
	 *@param  v  Value to assign to policyFile.
	 */
	public void setPolicyFile(String v) {
		this.policyFile = v;
	}


	/**
	 *  Set the value of publicCertificate.
	 *
	 *@param  v  Value to assign to publicCertificate.
	 */
	public void setPublicCertificate(PublicCertificate v) {
		this.publicCertificate = v;
	}


	/**
	 *  Set the value of privateCertificate.
	 *
	 *@param  v  Value to assign to privateCertificate.
	 */
	public void setPrivateCertificate(PrivateCertificate v) {
		this.privateCertificate = v;
	}


	/**
	 *  Get the value of policyFile.
	 *
	 *@return    Value of policyFile.
	 */
	public String getPolicyFile() {
		return policyFile;
	}


	/**
	 *  Get the value of publicCertificate.
	 *
	 *@return    Value of publicCertificate.
	 */
	public PublicCertificate getPublicCertificate() {
		return publicCertificate;
	}



	/**
	 *  Get the value of privateCertificate.
	 *
	 *@return    Value of privateCertificate.
	 */
	public PrivateCertificate getPrivateCertificate() {
		return privateCertificate;
	}


	protected RemoteNodeAdapter createNodeAdapter(RemoteNode remoteNode) throws NodeException {
    if (remoteNode instanceof SecureRemoteNode)
      return new SecureRemoteNodeAdapter((SecureRemoteNode)remoteNode, this.policyFile);
	  return new RemoteNodeAdapter(remoteNode);
	}
	
	protected RemoteNodeAdapter createNodeAdapter(String remoteNodeName, boolean replacePreviousBinding) throws NodeException {
	  return new SecureRemoteNodeAdapter(remoteNodeName, replacePreviousBinding, this.policyFile,publicCertificate,privateCertificate,acPublicKey);
	}

}

