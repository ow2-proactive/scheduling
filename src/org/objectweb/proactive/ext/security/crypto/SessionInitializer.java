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
import java.lang.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 *  The SessionInitializer is located in the SecureProxy of an active object and initiates the authentication and the key
 *  negociation protocols. It communicates with the SessionsManager located in the SecureBody. It provides also the methods
 *  that ensure confidentiality and integrity.
 *
 * @author     Vincent RIBAILLIER
 * @created    July 19, 2001
 */
public class SessionInitializer {
  public boolean sessionInitiated = false;
  public boolean authentication = false;
  public boolean keyNegociation = false;
  private long sessionID;
  private EncryptionEngine encryptionEngine;
  private SigningEngine signingEngine;
  private SessionKeyFactory sessionKeyfactory;
  private transient PublicCertificate publicCertificate;
  private transient PrivateCertificate privateCertificate;
  private PublicCertificateChecker certificateChecker;
  private PrivateKey privateKey;
  private PublicKey publicKey;
  private PublicCertificate otherPublicCertificate;
  private RandomLongGenerator randomLongGenerator;
  private PublicKey acPublicKey;
  private Key sessionKey;

  /**
   *  Constructor for the SessionInitializer object
   *
   * @param  publicCertificate   The PublicCertificate of the domain
   * @param  privateCertificate  The PrivateCertificate of the domain
   * @param  acPublicKey         The PublicKey of the Certification Authority
   * @since
   */
  public SessionInitializer(PublicCertificate publicCertificate, PrivateCertificate privateCertificate, PublicKey acPublicKey) {
    this.acPublicKey = acPublicKey;
    this.set_publicCertificate(publicCertificate);
    this.set_privateCertificate(privateCertificate);
    //System.out.println("SessionInitializer : SessionKeyFactory instanciation...");

    sessionKeyfactory = new SessionKeyFactory();
    //System.out.println("SessionInitializer : EncryptionEngine instanciation...");

    encryptionEngine = new EncryptionEngine();
    //System.out.println("SessionInitializer : SigningEngine instanciation...");

    signingEngine = new SigningEngine();
    //System.out.println("SessionInitializer : Session instanciation...");
    //System.out.println("SessionInitializer : PublicCertificateChecker instanciation ...");

    certificateChecker = new PublicCertificateChecker();
    //System.out.println("SessionInitializer : RandomLongGenerator instanciation...");

    randomLongGenerator = new RandomLongGenerator();
    sessionID = randomLongGenerator.generateLong(4);
  }

  /**
   * @param  publicCertificate  The publicCertificate of the current domain
   * @since
   */
  public void set_publicCertificate(PublicCertificate publicCertificate) {
    this.publicCertificate = publicCertificate;
    this.publicKey = publicCertificate.get_certificatePublicKey();
  }

  /**
   * @param  privateCertificate  The PrivateCertificate of the current domain
   * @since
   */
  public void set_privateCertificate(PrivateCertificate privateCertificate) {
    this.privateCertificate = privateCertificate;
    this.privateKey = privateCertificate.get_PrivateKey();
  }

  /**
   * @return    The PublicCertificate of the domain
   * @since
   */
  public PublicCertificate getPublicCertificate() {
    return publicCertificate;
  }

  /**
   *  
   *
   * @return    The ID of the Session
   * @since
   */
  public long get_sessionID() {
    return this.sessionID;
  }

  /**
   *  Initiates the communication session
   *
   * @param  manager  The SessionsManager of the other communicating entity
   * @return          true if the initialization succeeds, false otherwise
   * @since
   */
  public boolean initiateSession(SessionsManagerInt manager) {
    if (sessionID == 0) {
      sessionID = randomLongGenerator.generateLong(4);
    }
    try {
      manager.initiateSession(sessionID);
    } catch (SessionException e) {
      return false;
    } catch (java.rmi.RemoteException e) {
      System.out.println("SessionInitializer :Remote Exception : " + e);
    }
    this.sessionInitiated = true;
    return true;
  }

  /**
   *  Kills the current session and set the SessionsInitializer ready to initialize a new Session
   *
   * @param  manager  The SessionsManager of the other communicating entity
   * @return          true if the operation succeeds, false otherwise
   * @since
   */
  public boolean killSession(SessionsManagerInt manager) {
    try {
      manager.killSession(sessionID);
    } catch (SessionException e) {
      return false;
    } catch (java.rmi.RemoteException e) {
      System.out.println("SessionInitializer :Remote Exception : " + e);
    }
    this.sessionID = 0;
    this.sessionKey = null;
    this.sessionInitiated = false;
    this.authentication = false;
    this.keyNegociation = false;
    return true;
  }

  /**
   *  Engages the mutual authentication protocol
   *
   * @param  sessionsManager              The SessionsManager of the other communicating entity
   * @return                              true if the Authentication suceeds, false otherwise
   * @exception  AuthenticationException
   * @since
   */
  public boolean authenticate_mutual(SessionsManagerInt sessionsManager) throws AuthenticationException {
    long ra = randomLongGenerator.generateLong(4);
    long rb = 0;

    try {
      rb = sessionsManager.give_me_a_random(sessionID);
    } catch (java.rmi.RemoteException e) {
      System.out.println("SessionInitializer :Remote Exception : " + e);
    } catch (Exception e) {
      System.out.println("SessionInitializer : Exception in random generation :" + e);
    }
    String A = "";
    String B = "";
    try {
      A = publicCertificate.get_CertificateIdentity().getDomainName();
      B = sessionsManager.getDomainName();
    } catch (java.rmi.RemoteException e) {
      System.out.println("SessionInitializer :Remote Exception : " + e);
    }

    // AutenticationTicket generation
    AuthenticationTicketProperty properties = new AuthenticationTicketProperty();
    properties.random1 = ra;
    properties.random2 = rb;
    properties.identity = B;

    Object signedProperties = signingEngine.signObject(properties, privateKey);
    AuthenticationTicket authenticationTicket = new AuthenticationTicket();

    authenticationTicket.certificate = publicCertificate;
    authenticationTicket.identity = B;
    authenticationTicket.random = ra;
    authenticationTicket.signedAuthenticationTicketProperty = signedProperties;

    // sessionsManager authentication method invocation;
    AuthenticationTicket return_ticket = new AuthenticationTicket();

    try {
      return_ticket = sessionsManager.authenticate_mutual(authenticationTicket, sessionID);
    } catch (AuthenticationException e) {
      System.out.println("SessionInitializer : AuthenticationException : " + e);
      return false;
    } catch (java.rmi.RemoteException e) {
      System.out.println("SessionInitializer :Remote Exception : " + e);
    }

    // We need now to authenticate the sessionsManager
    String me = return_ticket.identity;

    if (me.equals(A) == false) {
      throw new AuthenticationException("SessionInitializer : WRONG IDENTITY");
    }

    PublicCertificate emitterCertificate = return_ticket.certificate;
    String B2 = emitterCertificate.get_CertificateIdentity().getDomainName();

    if (B2.equals(B) == false) {
      throw new AuthenticationException("SessionInitializer : wrong certificate");
    }

    boolean certificateValidity = certificateChecker.checkValidity(emitterCertificate,
    /* emitterCertificate.get_acPublicKey() */
    acPublicKey);

    if (certificateValidity == false) {
      throw new AuthenticationException("SessionInitializer : emitter Certificate Validity");
    }

    boolean signatureValidity =
      signingEngine.checkSignature(return_ticket.signedAuthenticationTicketProperty, emitterCertificate.get_certificatePublicKey());

    if (signatureValidity == false) {
      throw new AuthenticationException("SessionInitializer : AuthenticationTicketProperty signature not valid");
    }

    AuthenticationTicketProperty properties2 = new AuthenticationTicketProperty();

    try {
      properties2 = (AuthenticationTicketProperty) ((SignedObject) return_ticket.signedAuthenticationTicketProperty).getObject();
    } catch (Exception e) {
      System.out.println("SessionInitializer : Exception in AuthenticationTicketProperty extraction : " + e);
    }

    if (properties2.random1 != rb) {
      throw new AuthenticationException("SessionInitializer : wrong ra");
    }

    if (properties2.random2 != ra) {
      throw new AuthenticationException("SessionInitializer : wrong rb");
    }

    if (properties2.identity.equals(A) == false) {
      throw new AuthenticationException("SessionInitializer : wrong A");
    }

    otherPublicCertificate = emitterCertificate;

    //System.out.println("SessionInitializer : AUTHENTICATION SUCCEEDS");
    this.authentication = true;
    return true;
  }

  /**
   *  Engages the unilateral authentication protocol
   *
   * @param  sessionsManager              The SessionsManager of the other communicating entity
   * @return                              true if the Authentication suceeds, false otherwise
   * @exception  AuthenticationException
   * @since
   */
  public boolean authenticate_unilateral(SessionsManagerInt sessionsManager) throws AuthenticationException {
    long rb = randomLongGenerator.generateLong(4);
    AuthenticationTicket authenticationTicket = new AuthenticationTicket();
    String B = publicCertificate.get_CertificateIdentity().getDomainName();

    try {
      authenticationTicket = sessionsManager.authenticate_unilateral(sessionID, rb, B);
    } catch (AuthenticationException e) {
      System.out.println("SessionInitializer : AuthenticationException : " + e);
    } catch (java.rmi.RemoteException e) {
      System.out.println("SessionInitializer :Remote Exception : " + e);
    }

    long ra = authenticationTicket.random;
    String addresse = authenticationTicket.identity;

    if (addresse.equals(B) == false) {
      throw new AuthenticationException("SessionInitializer : WRONG IDENTITY");
    }

    // Emitter Certificate Checking
    PublicCertificate emitterCertificate = authenticationTicket.certificate;
    String A = emitterCertificate.get_CertificateIdentity().getDomainName();
    // A is the sessionInitializer
    boolean certificateValidity = certificateChecker.checkValidity(emitterCertificate, /* emitterCertificate.get_acPublicKey() */
    acPublicKey);

    if (certificateValidity == false) {
      throw new AuthenticationException("SessionInitializer : Certificate is not valid");
    }

    boolean signatureValidity =
      signingEngine.checkSignature(authenticationTicket.signedAuthenticationTicketProperty, emitterCertificate.get_certificatePublicKey());

    if (signatureValidity == false) {
      throw new AuthenticationException("SessionInitializer : AuthenticationTicketProperty signature is not valid");
    }

    AuthenticationTicketProperty properties = new AuthenticationTicketProperty();

    try {
      properties = (AuthenticationTicketProperty) ((SignedObject) authenticationTicket.signedAuthenticationTicketProperty).getObject();
    } catch (Exception e) {
      System.out.println("SessionInitializer : Exception in AuthenticationTicketProperty extraction : " + e);
    }

    if (properties.random1 != ra) {
      throw new AuthenticationException("SessionInitializer : wrong ra");
    }

    if (properties.random2 != rb) {
      throw new AuthenticationException("SessionInitializer : wrong rb");
    }

    if (properties.identity.equals(B) == false) {
      throw new AuthenticationException("SessionInitializer : wrong B");
    }

    //System.out.println("SessionInitializer : AUTHENTICATION SUCCEEDS");

    this.authentication = true;

    return true;
  }

  /**
   *  Engages the key negociation protocol
   *
   * @param  sessionsManager           The SessionsManager of the other communicating entity
   * @return                           true if the protocol suceeds, false otherwise
   * @exception  KeyExchangeException
   * @since
   */
  public boolean negociate_key(SessionsManagerInt sessionsManager) throws KeyExchangeException {
    long rb;
    long t1;
    long t2;
    long t3;
    long ra = randomLongGenerator.generateLong(4);

    sessionKey = sessionKeyfactory.generateSessionKey();

    // Key Splitting...
    byte[] myKey = new byte[200];
    byte[] newKey = new byte[200];
    byte[] part1 = new byte[100];
    byte[] part2 = new byte[100];

    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bout);
      out.writeObject(sessionKey);
      out.flush();
      myKey = bout.toByteArray();
      out.close();
    } catch (Exception e) {
      System.out.println("SessionInitializer : Exception 1 in sessionKey reconstitution :" + e);
    }

    int length = myKey.length;
    int middle = Math.abs(length / 2);
    for (int i = 0; i < middle; i++) {
      part1[i] = myKey[i];
    }

    int jj = -1;
    for (int i = middle; i < length; i++) {
      jj++;
      part2[jj] = myKey[i];
    }

    String A = "";
    String B = "";
    try {
      A = publicCertificate.get_CertificateIdentity().getDomainName();
      B = sessionsManager.getDomainName();
    } catch (java.rmi.RemoteException e) {
      System.out.println("SessionInitializer :Remote Exception : " + e);
    }

    // ConfidentialityTicket generation
    ConfidentialityTicketProperty properties = new ConfidentialityTicketProperty();

    properties.timestamp = 1000 * 60 * 60;
    properties.random1 = ra;
    properties.random2 = 0;
    properties.identity = B;

    Object encryptedSessionKeyPart1 = encryptionEngine.asymmetric_encrypt(part1, otherPublicCertificate.get_certificatePublicKey());
    Object encryptedSessionKeyPart2 = encryptionEngine.asymmetric_encrypt(part2, otherPublicCertificate.get_certificatePublicKey());

    properties.encryptedSessionKeyPart1 = encryptedSessionKeyPart1;
    properties.encryptedSessionKeyPart2 = encryptedSessionKeyPart2;

    Object signedProperties = signingEngine.signObject(properties, privateKey);
    ConfidentialityTicket confidentialityTicket = new ConfidentialityTicket();

    confidentialityTicket.timestamp = 1000 * 60 * 60;
    confidentialityTicket.random1 = ra;
    confidentialityTicket.random2 = 0;
    confidentialityTicket.identity = B;
    confidentialityTicket.encryptedSessionKeyPart1 = encryptedSessionKeyPart1;
    confidentialityTicket.encryptedSessionKeyPart2 = encryptedSessionKeyPart2;
    confidentialityTicket.signedConfidentialityTicketProperty = signedProperties;

    ConfidentialityTicket return_ticket = new ConfidentialityTicket();

    try {
      return_ticket = sessionsManager.negociate_key(confidentialityTicket, sessionID);
    } catch (KeyExchangeException e) {
      System.out.println("SessionInitializer : KeyNegociationException : " + e);
      return false;
    } catch (java.rmi.RemoteException e) {
      System.out.println("SessionInitializer :Remote Exception : " + e);
    }

    String me = return_ticket.identity;

    if (me.equals(A) == false) {
      throw new KeyExchangeException("SessionInitializer : WRONG IDENTITY");
    }

    boolean signatureValidity =
      signingEngine.checkSignature(return_ticket.signedConfidentialityTicketProperty, otherPublicCertificate.get_certificatePublicKey());

    if (signatureValidity == false) {
      throw new KeyExchangeException("SessionInitializer : ConfidentialityTicketProperty signature not valid");
    }

    ConfidentialityTicketProperty properties2 = new ConfidentialityTicketProperty();

    try {
      properties2 = (ConfidentialityTicketProperty) ((SignedObject) return_ticket.signedConfidentialityTicketProperty).getObject();
    } catch (Exception e) {
      System.out.println("Exception in AuthenticationTicketProperty extraction : " + e);
    }

    if (properties2.random1 != ra) {
      throw new KeyExchangeException("SessionInitializer : wrong ra");
    }

    if (properties2.random2 != return_ticket.random2) {
      throw new KeyExchangeException("SessionInitializer : wrong ra");
    }

    if (properties2.identity.equals(A) == false) {
      throw new KeyExchangeException("SessionInitializer : wrong A");
    }

    // Key decryption and reconstitution
    part1 = (byte[]) encryptionEngine.asymmetric_decrypt(return_ticket.encryptedSessionKeyPart1, privateKey);
    part2 = (byte[]) encryptionEngine.asymmetric_decrypt(return_ticket.encryptedSessionKeyPart2, privateKey);

    for (int i = 0; i < middle; i++) {
      newKey[i] = part1[i];
    }

    jj = -1;

    for (int i = middle; i < length; i++) {
      jj++;
      newKey[i] = part2[jj];
    }

    try {
      ByteArrayInputStream bin = new ByteArrayInputStream(myKey);
      ObjectInputStream in = new ObjectInputStream(bin);

      sessionKey = (Key) in.readObject();

      in.close();
    } catch (Exception e) {
      System.out.println("SessionInitializer : Exception 2 in sessionKey reconstitution :" + e);
    }

    // Session key decryption
    // Key returnedSessionKey = (Key) encryptionEngine.asymmetric_decrypt(return_ticket.encryptedSessionKey,privateKey);
    for (int i = 0; i < length; i++) {
      if (myKey[i] != newKey[i]) {
        throw new KeyExchangeException("SessionInitializer : wrong sesionKey");
      }
    }

    //System.out.println("SessionInitializer : KEY NEGOCIATION SUCCEEDS");

    keyNegociation = true;

    return true;
  }

  /**
   *  Encrypt an object with the sessionKey
   *
   * @param  object  The object to encrypt
   * @return         The encrypted object
   * @since
   */
  public Object encrypt(Serializable object) {
    return this.encryptionEngine.encrypt(object, this.sessionKey);
  }

  /**
   *  Decrypts an encrypted object with the sessionkey
   *
   * @param  object  The object to decrypt
   * @return         The decrypted object
   * @since
   */
  public Object decrypt(Object object) {
    return this.encryptionEngine.decrypt(object, this.sessionKey);
  }

  /**
   *  Signs the object
   *
   * @param  object  The object to sign
   * @return         The signed object
   * @since
   */
  public Object signObject(Serializable object) {
    return this.signingEngine.signObject(object, privateKey);
  }

  /**
   *  Checks the signature of an object
   *
   * @param  signedObject  The signed object
   * @return               true if signature is valid, false otherwise
   * @since
   */
  public boolean checkSignature(Object signedObject) {
    return this.signingEngine.checkSignature(signedObject, otherPublicCertificate.get_certificatePublicKey());
  }

}