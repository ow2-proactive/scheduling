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
package org.objectweb.proactive.ext.security.crypto;

/**
 *  This is the Rmi interface of the SessionsManager.
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 20, 2001
 */
public interface SessionsManagerInt extends java.rmi.Remote {

	/**
	 *  Description of the Method
	 *
	 * @return                               Description of the Returned Value
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public String getDomainName() throws java.rmi.RemoteException;

	
	/**
	 *  Description of the Method
	 *
	 * @return                               Description of the Returned Value
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public PublicCertificate getPublicCertificate() throws java.rmi.RemoteException;

	
	/**
	 *  Description of the Method
	 *
	 * @param  randomID                      Description of Parameter
	 * @exception  SessionException          Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public void initiateSession(long randomID) throws SessionException, java.rmi.RemoteException;


	/**
	 *  Adds a feature to the Session attribute of the SessionsManagerInt object
	 *
	 * @param  sessionID                     The feature to be added to the Session attribute
	 * @param  newSession                    The feature to be added to the Session attribute
	 * @exception  SessionException          Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public void addSession(long sessionID, Session newSession) throws SessionException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  randomID                      Description of Parameter
	 * @exception  SessionException          Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public void killSession(long randomID) throws SessionException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  randomID                      Description of Parameter
	 * @return                               Description of the Returned Value
	 * @exception  AuthenticationException   Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public long give_me_a_random(long randomID) throws AuthenticationException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  authenticationTicket          Description of Parameter
	 * @param  randomID                      Description of Parameter
	 * @return                               Description of the Returned Value
	 * @exception  AuthenticationException   Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public AuthenticationTicket authenticate_mutual(AuthenticationTicket authenticationTicket, long randomID)
			 throws AuthenticationException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  randomID                      Description of Parameter
	 * @param  rb                            Description of Parameter
	 * @param  emittor                       Description of Parameter
	 * @return                               Description of the Returned Value
	 * @exception  AuthenticationException   Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public AuthenticationTicket authenticate_unilateral(long randomID, long rb, String emittor) throws
			AuthenticationException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  confidentialityTicket         Description of Parameter
	 * @param  randomID                      Description of Parameter
	 * @return                               Description of the Returned Value
	 * @exception  KeyExchangeException      Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public ConfidentialityTicket negociate_key(ConfidentialityTicket confidentialityTicket,
			long randomID) throws KeyExchangeException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  object                        Description of Parameter
	 * @param  ID                            Description of Parameter
	 * @return                               Description of the Returned Value
	 * @exception  SessionException          Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public Object encrypt(java.io.Serializable object, long ID) throws SessionException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  object                        Description of Parameter
	 * @param  ID                            Description of Parameter
	 * @return                               Description of the Returned Value
	 * @exception  SessionException          Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public Object decrypt(Object object, long ID) throws SessionException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  object                        Description of Parameter
	 * @return                               Description of the Returned Value
	 * @exception  SessionException          Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public Object signObject(java.io.Serializable object) throws SessionException, java.rmi.RemoteException;


	/**
	 *  Description of the Method
	 *
	 * @param  signedObject                  Description of Parameter
	 * @param  ID                            Description of Parameter
	 * @return                               Description of the Returned Value
	 * @exception  SessionException          Description of Exception
	 * @exception  java.rmi.RemoteException  Description of Exception
	 * @since
	 */
	public boolean checkSignature(Object signedObject, long ID) throws SessionException, java.rmi.RemoteException;
  
}

