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
import java.io.Serializable;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;

/**
 * This class is used to provide confidentiality. 
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class EncryptionEngine {
	private SecureRandom rand = new FixedSecureRandom();
	private Cipher symmetricCipher;
	private Cipher asymmetricCipher;


	/**
	 *  Constructor for the EncryptionEngine object
	 *
	 * @since
	 */
	public EncryptionEngine() {
		try {
			symmetricCipher = Cipher.getInstance("Rijndael/ECB/WithCTS");
			asymmetricCipher = Cipher.getInstance("RSA");
		}
		catch (Exception e) {
			System.out.println("Exception in cipher creation : " + e);
		}
	}

	/**
	 *  Encrypt any serializable object using conventional (symmetric) cryptography
	 *
	 * @param  object      The serializable object to encrypt
	 * @param  sessionKey  The sessionKey to used for encryption
	 * @return             The encrypted object
	 * @since
	 */
	public Object encrypt(Serializable object, Key sessionKey) {
		try {
			symmetricCipher.init(Cipher.ENCRYPT_MODE, sessionKey, rand);

			return new SealedObject(object, symmetricCipher);
		}
		catch (Exception e) {
			System.out.println("Exception in encryption :" + e);
		}

		return null;
	}


	/**
	 * Decrypt an encrypted object using conventional (symmetric) cryptography
	 *
	 * @param  object      The object to decrypt
	 * @param  sessionKey  The sessionKey to use for decryption
	 * @return             The decrypted object
	 * @since
	 */
	public Object decrypt(Object object, Key sessionKey) {
		try {
			symmetricCipher.init(Cipher.DECRYPT_MODE, sessionKey, rand);

			return ((SealedObject) object).getObject(symmetricCipher);
		}
		catch (Exception e) {
			System.out.println("Exception in decryption :" + e);
		}

		return null;
	}

	/**
	 *  Encrypt any serializable object using asymmetric cryptography
	 *
	 * @param  object  The serializable object to encrypt
	 * @param  key     The PublicKey to use for encryption
	 * @return         The encrypted object
	 * @since
	 */
	public Object asymmetric_encrypt(Serializable object, PublicKey key) {
		try {
			asymmetricCipher.init(Cipher.ENCRYPT_MODE, key, rand);

			return new SealedObject(object, asymmetricCipher);
		}
		catch (Exception e) {
			System.out.println("Exception in encryption :" + e);
		}

		return null;
	}
		
	/**
	 *  Decrypt an encrypted object using asymmetric cryptography
	 *
	 * @param  object  The object to decrypt
	 * @param  key     The privateKey to use for decryption
	 * @return         The decrypted object
	 * @since
	 */
	public Object asymmetric_decrypt(Object object, PrivateKey key) {
		try {
			asymmetricCipher.init(Cipher.DECRYPT_MODE, key, rand);

			return ((SealedObject) object).getObject(asymmetricCipher);
		}
		catch (Exception e) {
			System.out.println("Exception in decryption :" + e);
		}

		return null;
	}

}

