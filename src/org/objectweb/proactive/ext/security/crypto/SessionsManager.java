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

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignedObject;
import java.util.Hashtable;

/**
 *  The SessionsManger is located in the SecureBody of an active object and manages the different communications sessions
 *  of the active objects.
 *  It communicates with the SessionInitializer located in the SecureProxy of others actives objects.
 *  It provides also the methods that ensure confidentiality and integrity.
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class SessionsManager implements SessionsManagerInt {

	private Hashtable ids = new Hashtable(MAX);
	private int nbSessions = -1;
	private Session[] session = new Session[MAX];
	private EncryptionEngine encryptionEngine;
	private SigningEngine signingEngine;
	private SessionKeyFactory sessionKeyfactory;
	private transient PublicCertificate publicCertificate;
	private transient PrivateCertificate privateCertificate;
	private PublicCertificateChecker certificateChecker;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private RandomLongGenerator randomLongGenerator;
	private PublicKey acPublicKey;
	static int MAX = 100;


	/**
	 *  Constructor for the SessionsManager object
	 *
	 * @param  publicCertificate   The PublicCertificate of the current domain
	 * @param  privateCertificate  The PrivateCertificate of the current domain
	 * @param  acPublicKey         The PublicKey of the CertificationAuthority
	 * @since
	 */
	public SessionsManager(PublicCertificate publicCertificate,	PrivateCertificate privateCertificate, PublicKey acPublicKey) {
		this.set_publicCertificate(publicCertificate);
		this.set_privateCertificate(privateCertificate);
		this.acPublicKey = acPublicKey;
		sessionKeyfactory = new SessionKeyFactory();
		encryptionEngine = new EncryptionEngine();
		signingEngine = new SigningEngine();
		certificateChecker = new PublicCertificateChecker();
		randomLongGenerator = new RandomLongGenerator();
	}


	/**
	 *  
	 *
	 * @param  publicCertificate  The PublicCertificate of the current domain.
	 * @since
	 */
	public void set_publicCertificate(PublicCertificate publicCertificate) {
		this.publicCertificate = publicCertificate;
		this.publicKey = publicCertificate.get_certificatePublicKey();
	}


	/**
	 *  
	 *
	 * @param  privateCertificate  The PrivateCertificate of the current domain
	 * @since
	 */
	public void set_privateCertificate(PrivateCertificate privateCertificate) {
		this.privateCertificate = privateCertificate;
		this.privateKey = privateCertificate.get_PrivateKey();
	}


	/**
	 * @return                               The name of the current domain
	 * @since
	 */
	public String getDomainName() {
		return publicCertificate.get_CertificateIdentity().getDomainName();
	}


	/**
	 * @return                               The PublicCertificate of the current domain
	 * @since
	 */
	public PublicCertificate getPublicCertificate() {
		return publicCertificate;
	}


	/**
	 *  Initiates a new communication session. This method must be call by the SessionInitializer just
	 *  after the constructor call.
	 *
	 * @param  randomID                      The ID of the session.
	 * @exception  SessionException
	 * @since
	 */
	public void initiateSession(long randomID) throws SessionException {
		if (ids.containsKey(new Long(randomID)) == true) {
			throw new SessionException("SessionsManager : Session ID already taken");
		}
		nbSessions++;
		if (nbSessions >= MAX) {
			throw new SessionException("SessionsManager : Too many sessions are openened");
		}
		ids.put(new Long(randomID), new Integer(nbSessions));
		session[nbSessions] = new Session(randomID);
	}


	/**
	 *  Adds an already existing communication session to the SessionsManager.
	 *
	 * @param  sessionID                     The sessionID of the session to be added
	 * @param  newSession                    The session to be added
	 * @exception  SessionException
	 * @since
	 */
	public void addSession(long sessionID, Session newSession) throws SessionException {
		if (!newSession.isID(sessionID)) {
			throw new SessionException("SessionsManager :wrong SessionID");
		}

		if (ids.containsKey(new Long(sessionID)) == true) {
			throw new SessionException("SessionsManager : Session ID already taken");
		}

		nbSessions++;

		if (nbSessions >= MAX) {
			throw new SessionException("SessionsManager : Too many sessions are openened");
		}

		ids.put(new Long(sessionID), new Integer(nbSessions));
		session[nbSessions] = new Session(newSession);
	}


	/**
	 *  Kills a session
	 *
	 * @param  randomID                      The ID of the Session to kill
	 * @exception  SessionException          
	 * @since
	 */
	public void killSession(long randomID) throws SessionException {
		if (ids.containsKey(new Long(randomID)) == true) {
			throw new SessionException("SessionsManager : Session ID already taken");
		}

		this.session[nbSessions] = null;
		ids.remove(new Long(randomID));
		nbSessions--;
	}


	/**
	 *  Generates a random long.
	 *
	 * @param  randomID                      the ID of the session asking for the random.
	 * @return                               the random long
	 * @exception  AuthenticationException
	 * @since
	 */
	public long give_me_a_random(long randomID) throws AuthenticationException {
		Integer ind = (Integer) ids.get(new Long(randomID));

		if (ind == null) {
			throw new AuthenticationException("SessionsManager : wrong sessionID");
		}
		int indice = ind.intValue();
		session[indice].set_lastrandom(randomLongGenerator.generateLong(4));
		return session[indice].get_lastrandom(randomID);
	}


	/**
	 *  Engages the mutual authentication process on the SessionsManager side.
	 *
	 * @param  authenticationTicket          The AuthenticationTicket of the sessionInitializer
	 * @param  randomID                      The ID of the session asking for mutual authentication
	 * @return                               A new AuthenticationTicket.
	 * @exception  AuthenticationException
	 * @since
	 */
	public AuthenticationTicket authenticate_mutual(AuthenticationTicket authenticationTicket, long randomID) throws AuthenticationException {
		Integer ind = (Integer) ids.get(new Long(randomID));

		if (ind == null) {
			throw new AuthenticationException("SessionsManager : wrong sessionID");
		}

		int indice = ind.intValue();

		long rb = session[indice].get_lastrandom(randomID);
		long ra = authenticationTicket.random;
		String addresse = authenticationTicket.identity;
		String B = getDomainName();

		if (addresse.equals(B) == false) {
			throw new AuthenticationException("SessionsManager : WRONG IDENTITY");
		}

		// Emitter Certificate Checking
		PublicCertificate emitterCertificate =
				authenticationTicket.certificate;
		String A = emitterCertificate.get_CertificateIdentity().getDomainName();
		boolean certificateValidity =
				certificateChecker.checkValidity(emitterCertificate, acPublicKey);

		if (certificateValidity == false) {
			throw new AuthenticationException("SessionsManager : Certificate is not valid");
		}

		boolean signatureValidity =
				signingEngine.checkSignature(authenticationTicket.signedAuthenticationTicketProperty,
				emitterCertificate.get_certificatePublicKey());

		if (signatureValidity == false) {
			throw new AuthenticationException("SessionsManager : AuthenticationTicketProperty signature is not valid");
		}

		AuthenticationTicketProperty properties = new AuthenticationTicketProperty();

		try {
			properties =
					(AuthenticationTicketProperty) ((SignedObject) authenticationTicket.signedAuthenticationTicketProperty).getObject();
		}
		catch (Exception e) {
			System.out.println("Exception in AuthenticationTicketProperty extraction : " + e);
		}

		if (properties.random1 != ra) {
			throw new AuthenticationException("SessionsManager : wrong ra");
		}

		if (properties.random2 != rb) {
			throw new AuthenticationException("SessionsManager : wrong rb");
		}

		if (properties.identity.equals(B) == false) {
			throw new AuthenticationException("SessionsManager : wrong B");
		}

		session[indice].set_otherPublicCertificate(emitterCertificate);

		// AutenticationTicket generation
		AuthenticationTicketProperty properties2 = new AuthenticationTicketProperty();

		properties2.random1 = rb;
		properties2.random2 = ra;
		properties2.identity = A;

		Object signedProperties2 = signingEngine.signObject(properties2, privateKey);
		AuthenticationTicket authenticationTicket2 = new AuthenticationTicket();

		authenticationTicket2.certificate = publicCertificate;
		authenticationTicket2.identity = A;
		authenticationTicket2.random = 0;
		authenticationTicket2.signedAuthenticationTicketProperty = signedProperties2;

		return authenticationTicket2;
	}


	/**
	 *  Engages the unilateral authentication on the SessionsManager side.
	 *
	 * @param  randomID                      The ID of the session asking for unilateral authentication.
	 * @param  rb                            A random long
	 * @param  emittor
	 * @return                               The AuthenticationTicket required by the sessionInitializer. 
	 * @exception  AuthenticationException   
	 * @since
	 */
	public AuthenticationTicket authenticate_unilateral(long randomID, long rb, String emittor) throws AuthenticationException {
		
		Integer ind = (Integer) ids.get(new Long(randomID));

		if (ind == null) {
			throw new AuthenticationException("SessionsManager : wrong sessionID");
		}

		int indice = ind.intValue();

		long ra = randomLongGenerator.generateLong(4);
		String B = emittor;

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

		return authenticationTicket;
	}


	/**
	 * Engages the session key negociation protocol on the SessionsManager Side
	 *
	 * @param  confidentialityTicket         The ConfidentialityTicket of the SessionInitializer
	 * @param  randomID                      The ID of the session askking for the key negociation protocol
	 * @return                               The ConfidentialityTicket required by the SessionInitializer 
	 * @exception  KeyExchangeException      
	 * @since
	 */
	public ConfidentialityTicket negociate_key(ConfidentialityTicket confidentialityTicket,	long randomID) throws KeyExchangeException {
		
		Integer ind = (Integer) ids.get(new Long(randomID));

		if (ind == null) {
			throw new KeyExchangeException("SessionsManager : wrong sessionID");
		}

		int indice = ind.intValue();
		long timestamp = confidentialityTicket.timestamp;
		long ra = confidentialityTicket.random1;
		long rb = randomLongGenerator.generateLong(4);
		String A = session[indice].get_otherPublicCertificate(randomID).get_CertificateIdentity().getDomainName();
		String B = getDomainName();
		String addresse = confidentialityTicket.identity;

		if (addresse.equals(B) == false) {
			throw new KeyExchangeException("SessionManager : WRONG IDENTITY");
		}

		// AuthenticationTicketProperty extraction
		ConfidentialityTicketProperty properties = new ConfidentialityTicketProperty();
		try {
			properties =
					(ConfidentialityTicketProperty) ((SignedObject) confidentialityTicket.signedConfidentialityTicketProperty).getObject();
		}
		catch (Exception e) {
			System.out.println("Exception in ConfidentialityTicketProperty extraction : "+ e);
		}
		if (properties.timestamp != timestamp) {
			throw new KeyExchangeException("SessionsManager : wrong timestamp");
		}
		if (properties.random1 != ra) {
			throw new KeyExchangeException("SessionsManager : wrong ra");
		}
		if (properties.random2 != 0) {
			throw new KeyExchangeException("SessionsManager : wrong rb");
		}
		if (properties.identity.equals(B) == false) {
			throw new KeyExchangeException("SessionsManager : wrong B");
		}
		// Reconstitution of the encrypted key
		byte[] newKey = new byte[146];
		byte[] part1 =
			(byte[]) encryptionEngine.asymmetric_decrypt(confidentialityTicket.encryptedSessionKeyPart1,privateKey);
		byte[] part2 =
			(byte[]) encryptionEngine.asymmetric_decrypt(confidentialityTicket.encryptedSessionKeyPart2,privateKey);
		int length = newKey.length;
		int middle = Math.abs(length / 2);
		// Key joining
		for (int i = 0; i < middle; i++) {
			newKey[i] = part1[i];
		}
		int jj = -1;
		for (int i = middle; i < length; i++) {
			jj++;
			newKey[i] = part2[jj];
		}
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(newKey);
			ObjectInputStream in = new ObjectInputStream(bin);
			session[indice].set_sessionKey((Key) in.readObject());
			in.close();
		}
		catch (Exception e) {
			System.out.println("SessionsManager : Exception 1 in sessionKey reconstitution :"
					 + e);
		}
		// decryption and reencryption of the sessionKey
		// sessionKey = (Key) encryptionEngine.asymmetric_decrypt(confidentialityTicket.encryptedSessionKey,privateKey);
		// Object encryptedSessionKey = encryptionEngine.asymmetric_encrypt(sessionKey,otherPublicCertificate.get_certificatePublicKey());
		Object encryptedSessionKeyPart1 = encryptionEngine.asymmetric_encrypt(part1,
				session[indice].get_otherPublicCertificate(randomID).get_certificatePublicKey());
		Object encryptedSessionKeyPart2 = encryptionEngine.asymmetric_encrypt(part2,
				session[indice].get_otherPublicCertificate(randomID).get_certificatePublicKey());
		// ConfidentialityTicket generation
		ConfidentialityTicketProperty returnProperties = new ConfidentialityTicketProperty();

		returnProperties.timestamp = timestamp;
		returnProperties.random1 = ra;
		returnProperties.random2 = rb;
		returnProperties.identity = A;
		properties.encryptedSessionKeyPart1 = encryptedSessionKeyPart1;
		properties.encryptedSessionKeyPart2 = encryptedSessionKeyPart2;

		Object signedProperties = signingEngine.signObject(returnProperties, privateKey);
		ConfidentialityTicket returnConfidentialityTicket = new ConfidentialityTicket();

		returnConfidentialityTicket.timestamp = timestamp;
		returnConfidentialityTicket.random1 = ra;
		returnConfidentialityTicket.random2 = rb;
		returnConfidentialityTicket.identity = A;
		returnConfidentialityTicket.encryptedSessionKeyPart1 = encryptedSessionKeyPart1;
		returnConfidentialityTicket.encryptedSessionKeyPart2 = encryptedSessionKeyPart2;
		returnConfidentialityTicket.signedConfidentialityTicketProperty = signedProperties;

		return returnConfidentialityTicket;
	}

	/**
	 *  Encrypts a serializable object with the corrrect session key
	 *
	 * @param  object                        The object to encrypt
	 * @param  ID                            The ID of the session containing the sessionkey
	 * @return                               The encrypted object
	 * @exception  SessionException          
	 * @since
	 */
	public Object encrypt(Serializable object,long ID) throws SessionException {
		
		Integer ind = (Integer) ids.get(new Long(ID));

		if (ind == null) {
			throw new SessionException("SessionsManager : wrong sessionID");
		}
		int indice = ind.intValue();
		
		return this.encryptionEngine.encrypt(object,session[indice].get_sessionKey(ID));
	}


	/**
	 *  Decrypts an object with the correct sessionkey
	 *
	 * @param  object                        The encrytpted object
	 * @param  ID                            The ID of the session containing the sessionKey
	 * @return                               The decrypted object
	 * @exception  SessionException          
	 * @since
	 */
	public Object decrypt(Object object, long ID) throws SessionException {
		Integer ind = (Integer) ids.get(new Long(ID));
		if (ind == null) {
			throw new SessionException("SessionsManager : wrong sessionID");
		}
		int indice = ind.intValue();

		return this.encryptionEngine.decrypt(object,session[indice].get_sessionKey(ID));
	}

	/**
	 * Signs an object with the current domain PrivateCertificate
	 *
	 * @param  object                        The object to sign
	 * @return                               The signed object
	 * @exception  SessionException          
	 * @since
	 */
	public Object signObject(Serializable object) throws SessionException {
		return this.signingEngine.signObject(object, privateKey);
	}


	/**
	 *  Verify the signature of a signed object with the PublicCertificate of the SessionInitializer domain
	 *
	 * @param  signedObject                  The signed object
	 * @param  ID                            The ID of the SessionInitializer 
	 * @return                               true if the signature is valid, false otherwise
	 * @exception  SessionException          
	 * @since
	 */
	public boolean checkSignature(Object signedObject, long ID) throws SessionException {
		Integer ind = (Integer) ids.get(new Long(ID));
		if (ind == null) {
			throw new SessionException("SessionsManager : wrong sessionID");
		}
		int indice = ind.intValue();
		return this.signingEngine.checkSignature(signedObject, session[indice].get_otherPublicCertificate(ID).get_certificatePublicKey());
	}
}

