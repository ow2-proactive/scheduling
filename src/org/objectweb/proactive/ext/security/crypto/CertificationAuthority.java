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
import org.bouncycastle.jce.provider.*;
import java.security.*;
import java.util.*;
import java.io.*;


/**
 * The role of the CertificationAuthority is to generate the public/private keypair reference.
 *
 * @author     Vincent RIBAILLIER
 * @created    July 19, 2001
 */
public class CertificationAuthority {
	private static PrivateKey privateKey;
	private static PublicKey publicKey;

	/**
	 *  Constructor for the CertificationAuthority object
	 *
	 * @since
	 */
	public CertificationAuthority() {
		generateKeyPair();
	}

	/**
	 * 
	 *
	 * @return CertificationAuthority PublicKey
	 * @since
	 */
	public static PublicKey get_PublicKey() {
		return publicKey;
	}

	/**
	 * 
	 *
	 * @return CertificationAuthority PrivateKey
	 * @since
	 */
	public static PrivateKey get_PrivateKey() {
		return privateKey;
	}


	/**
	 * 
	 *
	 * @since
	 */
	public static void writeKeys() {
		try {

			System.out.println("Generating AC publicKey...");
			FileOutputStream fout = new FileOutputStream("acPublicKey");
			ObjectOutputStream out = new ObjectOutputStream(fout);

			out.writeObject(publicKey);
			out.flush();
			out.close();

			System.out.println("Generating AC privateKey...");
			fout = new FileOutputStream("acPrivateKey");
			out = new ObjectOutputStream(fout);

			out.writeObject(privateKey);
			out.flush();
			out.close();

			System.out.println("The KeyPair has been correctly generated.");
			System.out.println("The AC publicKey  is saved in : acPublicKey");
			System.out.println("The AC privateKey is saved in : acPrivateKey");

		}
		catch (Exception e) {
			System.out.println("Exception in AC key serialization :" + e);
		}
	}


	/**
	 *  The main program for the CertificationAuthority class
	 *
	 * @param  args 
	 * @since
	 */
	public static void main(String[] args) {
		new CertificationAuthority();
		writeKeys();
	}


	/**
	 * 
	 *
	 * @since
	 */
	private static void generateKeyPair() {
		Provider myProvider =
				new org.bouncycastle.jce.provider.BouncyCastleProvider();

		// Tester ici si ca n'a pas ete deja fait : cf mail...
		Security.addProvider(myProvider);

		// Key Pair Generation...
		SecureRandom rand = new SecureRandom();
		JDKKeyPairGenerator.RSA keyPairGen = new JDKKeyPairGenerator.RSA();

		keyPairGen.initialize(512, rand);

		KeyPair keyPair = keyPairGen.generateKeyPair();

		privateKey = keyPair.getPrivate();
		publicKey = keyPair.getPublic();
	}

}

