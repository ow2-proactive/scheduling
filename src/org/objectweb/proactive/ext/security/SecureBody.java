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
import java.io.Serializable;
import java.security.PublicKey;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.AuthenticationTicket;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.crypto.PrivateCertificate;
import org.objectweb.proactive.ext.security.crypto.PublicCertificate;
import org.objectweb.proactive.ext.security.crypto.Session;
import org.objectweb.proactive.ext.security.crypto.SessionException;
import org.objectweb.proactive.ext.security.crypto.SessionsManager;
import org.objectweb.proactive.ext.security.crypto.SessionsManagerInt;


/**
 *  An implementation of Body interface,wich allows security
 *
 *@author     Arnaud Contes
 *<br>created    27 juillet 2001
 */
public class SecureBody extends BodyImpl implements SessionsManagerInt {
  /*
   * implements SecureBodyInterface
   */
  /**
   *  The sessionsManager 
   */
  public SessionsManager sessionsManager;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  /**
   *  Constructor for the SecureBody object
   */
  public SecureBody() {}

  /**
   *  Build the body object, then fires its service thread
   *
   *@param  c Description of Parameter
   *@param  nodeURL Description of Parameter
   *@exception  java.lang.reflect.InvocationTargetException Description of Exception
   *@exception  org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException Description of Exception
   */
  public SecureBody(
    ConstructorCall c,
    String nodeURL,
    Active activity,
    MetaObjectFactory factory,
    PublicCertificate publicCertificate,
    PrivateCertificate privateCertificate,
    PublicKey publicKey)
    throws java.lang.reflect.InvocationTargetException, org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException {
    super(c, nodeURL, activity, factory);
    setCertificate(publicCertificate, privateCertificate, publicKey);
  }

  public SecureBody(ConstructorCall c, String nodeURL, Active activity, MetaObjectFactory factory)
    throws java.lang.reflect.InvocationTargetException, org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException {
    super(c, nodeURL, activity, factory);
    setCertificate();
  }

  /**
   *  each active objects belongs to a Domain
   *
   *@return    Return the name of the Domain
   */
  public String getDomainName() {
    try {
      return sessionsManager.getDomainName();
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return null;
  }

  /**
   *  
   *
   *@return    Description of the Returned Value
   */
  public PublicCertificate getPublicCertificate() {
    try {
      return sessionsManager.getPublicCertificate();
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return null;
  }

  //
  // -- implements SessionsManagerInt  -----------------------------------------------
  //

  /**
   *  Implementation of SessionsManagerInt
   *
   *@param  randomID              Description of Parameter
   *@exception  SessionException  Description of Exception
   */
  public void initiateSession(long randomID) throws SessionException {
    try {
      sessionsManager.initiateSession(randomID);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
  }

  /**
   *  Implementation of SessionsManagerInt
   *
   *@param  randomID                     Description of Parameter
   *@return                              Description of the Returned Value
   *@exception  AuthenticationException  Description of Exception
   */
  public long give_me_a_random(long randomID) throws AuthenticationException {
    try {
      return sessionsManager.give_me_a_random(randomID);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return 0;
  }

  /**
   *  Implementation of SessionsManagerInt
   *
   *@param  authenticationTicket         Description of Parameter
   *@param  randomID                     Description of Parameter
   *@return                              Description of the Returned Value
   *@exception  AuthenticationException  Description of Exception
   */
  public AuthenticationTicket authenticate_mutual(AuthenticationTicket authenticationTicket, long randomID) throws AuthenticationException {
    try {
      return sessionsManager.authenticate_mutual(authenticationTicket, randomID);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return null;
  }

  /**
   *  Description of the Method
   *
   *@param  randomID                     Description of Parameter
   *@param  rb                           Description of Parameter
   *@param  emittor                      Description of Parameter
   *@return                              Description of the Returned Value
   *@exception  AuthenticationException  Description of Exception
   */
  public AuthenticationTicket authenticate_unilateral(long randomID, long rb, String emittor) throws AuthenticationException {
    try {
      return sessionsManager.authenticate_unilateral(randomID, rb, emittor);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return null;
  }

  /**
   *  Description of the Method
   *
   *@param  confidentialityTicket     Description of Parameter
   *@param  randomID                  Description of Parameter
   *@return                           Description of the Returned Value
   *@exception  KeyExchangeException  Description of Exception
   */
  public ConfidentialityTicket negociate_key(ConfidentialityTicket confidentialityTicket, long randomID) throws KeyExchangeException {
    try {
      return sessionsManager.negociate_key(confidentialityTicket, randomID);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return null;
  }

  //   public void set_publicCertificate(PublicCertificate publicCertificate) {

  // try {
  // 	sessionsManager.set_publicCertificate(publicCertificate);
  // 	} catch (Exception e) {
  // 	    System.out.println("Exception in SecureBody " +e);
  //     }

  //    }

  //     public void set_privateCertificate(PrivateCertificate privateCertificate) {
  // try {
  // 	sessionsManager.set_privateCertificate(privateCertificate);
  // 	} catch (Exception e) {
  // 	    System.out.println("Exception in SecureBody " +e);
  //     }

  //     }

  /**
   *  Adds a feature to the Session attribute of the SecureBody object
   *
   *@param  sessionID                     The feature to be added to the Session
   *      attribute
   *@param  newSession                    The feature to be added to the Session
   *      attribute
   *@exception  SessionException          Description of Exception
   */
  public void addSession(long sessionID, Session newSession) throws SessionException {
    try {
      sessionsManager.addSession(sessionID, newSession);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }

  }

  /**
   *  Description of the Method
   *
   *@param  randomID                      Description of Parameter
   *@exception  SessionException          Description of Exception
   */
  public void killSession(long randomID) throws SessionException {
    try {
      sessionsManager.killSession(randomID);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }

  }

  /**
   *  Description of the Method
   *
   *@param  object                        Description of Parameter
   *@param  ID                            Description of Parameter
   *@return                               Description of the Returned Value
   *@exception  SessionException          Description of Exception
   */
  public Object encrypt(Serializable object, long ID) throws SessionException {

    Object o = null;

    try {
      o = sessionsManager.encrypt(object, ID);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return o;
  }

  /**
   *  Description of the Method
   *
   *@param  object                        Description of Parameter
   *@param  ID                            Description of Parameter
   *@return                               Description of the Returned Value
   *@exception  SessionException          Description of Exception
   */
  public Object decrypt(Object object, long ID) throws SessionException {
    Object o = null;
    try {
      o = sessionsManager.decrypt(object, ID);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return o;
  }

  /**
   *  Description of the Method
   *
   *@param  object                        Description of Parameter
   *@return                               Description of the Returned Value
   *@exception  SessionException          Description of Exception
   */
  public Object signObject(Serializable object) throws SessionException {
    Object o = null;
    try {
      o = sessionsManager.signObject(object);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return o;
  }

  /**
   *  Description of the Method
   *
   *@param  signedObject                  Description of Parameter
   *@param  ID                            Description of Parameter
   *@return                               Description of the Returned Value
   *@exception  SessionException          Description of Exception
   */
  public boolean checkSignature(Object signedObject, long ID) throws SessionException {
    boolean b = false;
    try {
      b = sessionsManager.checkSignature(signedObject, ID);
    } catch (Exception e) {
      System.out.println("Exception in SecureBody " + e);
    }
    return b;
  }

  //
  // -- PROTECTED METHODS ----------------------------------------------------------------------------
  //

  protected ReplyReceiver createReplyReceiver() {
    return new SecureReplyReceiverImpl();
  }

  protected UniversalBody createRemoteBody() {
    try {
      return new SecureRemoteBodyAdapter(this);
    } catch (ProActiveException e) {
      throw new ProActiveRuntimeException("Cannot create Remote body adapter ", e);
    }
  }

  //
  // -- PRIVATE METHODS ----------------------------------------------------------------------------
  //

  /**
   *  Sets the Certificate attribute of the SecureBody object
   */
  private void setCertificate() {

    try {
      FileInputStream fin = new FileInputStream("/net/home/acontes/dev/ProActive/classes/certif_public2");
      ObjectInputStream in = new ObjectInputStream(fin);

      PublicCertificate publicCertificate = (PublicCertificate) in.readObject();
      in.close();

      fin = new FileInputStream("/net/home/acontes/dev/ProActive/classes/certif_private2");
      in = new ObjectInputStream(fin);
      PrivateCertificate privateCertificate = (PrivateCertificate) in.readObject();

      in.close();

      System.out.println("SessionsManager instanciation...");

      //creation du serveur
      //	SessionsManagerImpl sessionsManagerImpl;
      // sessionsManagerImpl = new SessionsManagerImpl();

      //	sessionsManager.set_publicCertificate(publicCertificate);
      //	sessionsManager.set_privateCertificate(privateCertificate);

      fin = new FileInputStream("/net/home/acontes/dev/ProActive/classes/acPublicKey");
      in = new ObjectInputStream(fin);
      PublicKey acPublicKey = (PublicKey) in.readObject();

      in.close();

      this.sessionsManager = new SessionsManager(publicCertificate, privateCertificate, acPublicKey);

    } catch (Exception e) {
      System.out.println("Exception in setting certificates" + e);
    }

  }

  private void setCertificate(PublicCertificate publicCertificate, PrivateCertificate privateCertificate, PublicKey publicKey) {
    this.sessionsManager = new SessionsManager(publicCertificate, privateCertificate, publicKey);
  }

}