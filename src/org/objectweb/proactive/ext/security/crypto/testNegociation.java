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
 *  This class is the simple class to test the security protocols. To start the test just type :
 * java org.objectweb.proactive.ext.security.crypto.testNegociation
 * You must first generate two sets of Public/Private Certificates
 * The first set will be saved in "certif_public1" and "certif_private1", the second set in certif_public2" and "certif_private2"
 * 
 * @author     Vincent RIBAILLIER
 * @created    July 19, 2001
 */
public class testNegociation {

	/**
	 *  Constructor for the testNegociation object
	 *
	 * @since
	 */
	public testNegociation() {
	}


	/**
	 *  The main program for the testNegociation class
	 *
	 * @param  args  
	 * @since
	 */
	public static void main(String[] args) {
		Provider myProvider =
				new org.bouncycastle.jce.provider.BouncyCastleProvider();

		Security.addProvider(myProvider);

		PrivateCertificate privateCertificate1;
		PrivateCertificate privateCertificate2;
		PublicCertificate publicCertificate1;
		PublicCertificate publicCertificate2;
		PublicKey acPublicKey;

		try {
			FileInputStream fin = new FileInputStream("/0/oasis/vribaill/dev/certif_public1");
			ObjectInputStream in = new ObjectInputStream(fin);

			publicCertificate1 = (PublicCertificate) in.readObject();

			in.close();

			fin = new FileInputStream("/0/oasis/vribaill/dev/certif_public2");
			in = new ObjectInputStream(fin);
			publicCertificate2 = (PublicCertificate) in.readObject();

			in.close();

			fin = new FileInputStream("/0/oasis/vribaill/dev/certif_private1");
			in = new ObjectInputStream(fin);
			privateCertificate1 = (PrivateCertificate) in.readObject();

			in.close();

			fin = new FileInputStream("/0/oasis/vribaill/dev/certif_private2");
			in = new ObjectInputStream(fin);
			privateCertificate2 = (PrivateCertificate) in.readObject();

			in.close();
			System.out.println("SessionInitalizers instanciation...");

			fin = new FileInputStream("/0/oasis/vribaill/dev/acPublicKey");
			in = new ObjectInputStream(fin);
			acPublicKey = (PublicKey) in.readObject();

			in.close();

			SessionInitializer sessionInitializer =
					new SessionInitializer(publicCertificate1,
					privateCertificate1, acPublicKey);
			SessionInitializer sessionInitializer2 =
					new SessionInitializer(publicCertificate1,
					privateCertificate1, acPublicKey);

			System.out.println("SessionsManager instanciation...");

			SessionsManager sessionsManager =
					new SessionsManager(publicCertificate2, privateCertificate2,
					acPublicKey);

			sessionInitializer.initiateSession(sessionsManager);
			sessionInitializer2.initiateSession(sessionsManager);

			// sessionInitializer authenticates itself if the sessionsMagaer
			// authentication is here mutual
			try {
				long t1;
				long t2;

				t1 = System.currentTimeMillis();

				boolean result =
						sessionInitializer.authenticate_mutual(sessionsManager);

				t2 = System.currentTimeMillis();
				t2 = t2 - t1;

				System.out.println("MUTUAL AUTHENTICATION RESULT = " + result
						 + "  Time (ms) :" + t2);
			}
			catch (AuthenticationException e) {
				System.out.println("Authentication exception...");
			}

			try {
				long t1;
				long t2;

				t1 = System.currentTimeMillis();

				boolean result =
						sessionInitializer2.authenticate_mutual(sessionsManager);

				t2 = System.currentTimeMillis();
				t2 = t2 - t1;

				System.out.println("MUTUAL AUTHENTICATION RESULT 2 = "
						 + result + "  Time (ms) :" + t2);
			}
			catch (AuthenticationException e) {
				System.out.println("Authentication exception...");
			}

			// sessionInitializer authenticates itself if the sessionsMagaer
			// authentication is here unilateral
			try {
				long t1;
				long t2;

				t1 = System.currentTimeMillis();

				boolean result =
						sessionInitializer.authenticate_unilateral(sessionsManager);

				t2 = System.currentTimeMillis();
				t2 = t2 - t1;

				System.out.println("UNILATERAL AUTHENTICATION RESULT = "
						 + result + "  Time (ms) :" + t2);
			}
			catch (AuthenticationException e) {
				System.out.println("Authentication exception...");
			}

			try {
				long t1;
				long t2;

				t1 = System.currentTimeMillis();

				boolean result =
						sessionInitializer2.authenticate_unilateral(sessionsManager);

				t2 = System.currentTimeMillis();
				t2 = t2 - t1;

				System.out.println("UNILATERAL AUTHENTICATION RESULT 2 = "
						 + result + "  Time (ms) :" + t2);
			}
			catch (AuthenticationException e) {
				System.out.println("Authentication exception...");
			}

			// sessionInitializer asks for a key negociation
			// There must be a mutual authentication before the keyExchange protocol
			try {
				long t1;
				long t2;

				t1 = System.currentTimeMillis();

				boolean result =
						sessionInitializer.negociate_key(sessionsManager);

				t2 = System.currentTimeMillis();
				t2 = t2 - t1;

				System.out.println("KEY NEGOCIATION RESULT = " + result
						 + "  Time (ms) :" + t2);
			}
			catch (KeyExchangeException e) {
				System.out.println("Key Negociation exception...");
			}

			try {
				long t1;
				long t2;

				t1 = System.currentTimeMillis();

				boolean result =
						sessionInitializer2.negociate_key(sessionsManager);

				t2 = System.currentTimeMillis();
				t2 = t2 - t1;

				System.out.println("KEY NEGOCIATION RESULT 2 = " + result
						 + "  Time (ms) :" + t2);
			}
			catch (KeyExchangeException e) {
				System.out.println("Key Negociation exception...");
			}
		}
		catch (Exception e) {
			System.out.println("Exception  :" + e);
		}
	}

}

