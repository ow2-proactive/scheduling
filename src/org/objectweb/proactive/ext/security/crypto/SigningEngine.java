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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignedObject;

/**
 *  A SigningEngine is used to sign object and to check their signature
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class SigningEngine {
	private Signature signingEngine;

	/**
	 *  Constructor for the SigningEngine object
	 *
	 * @since
	 */
	public SigningEngine() {
		try {
			signingEngine = Signature.getInstance("SHA-1/RSA", "BC");
		}
		catch (Exception e) {
			System.out.println("Exception in SigningEngine instanciation : "
					 + e);
		}
	}

	/**
	 * Signs a serializable object
	 *
	 * @param  object      The object to sign
	 * @param  privateKey  The private key to use for the signature
	 * @return             The signed object
	 * @since
	 */
	public Object signObject(Serializable object, PrivateKey privateKey) {
		try {
			return new SignedObject(object, privateKey, signingEngine);
		}
		catch (Exception e) {
			System.out.println("Exception in object signature : " + e);
		}

		return null;
	}

	/**
	 *  Checks the signature of a signed object
	 *
	 * @param  signedObject  The signed object
	 * @param  publicKey     The public key to use to check the signature
	 * @return               true if the signature is valid, false otherwise
	 * @since
	 */
	public boolean checkSignature(Object signedObject, PublicKey publicKey) {
		try {
			if (((SignedObject) signedObject).verify(publicKey,
					signingEngine)) {
				return true;
			}
		}
		catch (Exception e) {
			System.out.println("Exception object signature checking :" + e);
		}

		return false;
	}
}

