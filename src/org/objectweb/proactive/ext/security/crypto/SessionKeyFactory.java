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
import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.KeyGenerator;

/**
 *  This class generates the symmetric keys (SessionKey) required for the confidentiality.
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class SessionKeyFactory {
	private Provider myProvider;

	// private SecureRandom      rand = new FixedSecureRandom();
	private KeyGenerator keyGen;


	/**
	 *  Constructor for the SessionKeyFactory object
	 *
	 * @since
	 */
	public SessionKeyFactory() {
		myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();

		// Tester ici si ca n'a pas ete deja fait : cf mail...
		Security.addProvider(myProvider);

		try {

			// "BC" is the name of the BouncyCastle provider
			keyGen = KeyGenerator.getInstance("Rijndael", "BC");

			keyGen.init(128, new SecureRandom());

			// keyGen.init(128,rand);
		}
		catch (Exception e) {
			System.out.println("Exception in the Session Key Generation : "
					 + e);
		}
	}


	/**
	 *  Generates a sessionKey
	 *
	 * @return    The sessionKey generated
	 * @since
	 */
	public Key generateSessionKey() {
		return keyGen.generateKey();
	}

}

