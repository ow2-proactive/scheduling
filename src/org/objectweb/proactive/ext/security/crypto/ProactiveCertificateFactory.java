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
 *  This class  generates the Certificates required for the domains.
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class ProactiveCertificateFactory {
	private PublicKey acPublicKey;
	private PrivateKey acPrivateKey;
	private PrivateKey privateKey;
	private PublicKey publicKey;


	/**
	 *  Constructor for the ProactiveCertificateFactory object
	 *
	 * @param  acPublicKey   Description of Parameter
	 * @param  acPrivateKey  Description of Parameter
	 * @since
	 */
	public ProactiveCertificateFactory(PublicKey acPublicKey,
			PrivateKey acPrivateKey) {
		this.acPublicKey = acPublicKey;
		this.acPrivateKey = acPrivateKey;
	}


	/**
	 *  Generates a PrivateCertificate
	 *
	 * @param  domainName  The name of the domain
	 * @return             The generated PrivateCertificate for the domain
	 * @since
	 */
	public PrivateCertificate generatePrivateCertificate(String domainName) {
		generateKeyPair();

		CertificateIdentity cert_id = new CertificateIdentity(domainName);
		CertificateProperty cert_property =
				new CertificateProperty(new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis()
				 + 365 * 24 * 60 * 60 * 1000));
		PublicCertificate certif_public = new PublicCertificate(cert_id,
				cert_property, publicKey, acPublicKey, privateKey,
				acPrivateKey);
		PrivateCertificate certif_private =
				new PrivateCertificate(certif_public, privateKey);

		return certif_private;
	}


	/**
	 *  Generates a PrivateCertificate
	 *
	 * @param  domainName  The name of the domain
	 * @param  days        The time validity of the PrivateCertificate (in days)
	 * @return             The generated PrivateCertificate for the domain
	 * @since
	 */
	public PrivateCertificate generatePrivateCertificate(String domainName, int days) {
		generateKeyPair();

		CertificateIdentity cert_id = new CertificateIdentity(domainName);
		long cur_date=System.currentTimeMillis();
		long exp_date=cur_date + new Long(days).longValue() * new Long(24 * 60 * 60 * 1000).longValue();
		CertificateProperty cert_property = new CertificateProperty(new Date(cur_date),new Date(exp_date));
		PublicCertificate certif_public = new PublicCertificate(cert_id, cert_property, publicKey, acPublicKey, privateKey, acPrivateKey);
		PrivateCertificate certif_private = new PrivateCertificate(certif_public, privateKey);

		return certif_private;
	}


	/**
	 *  Description of the Method
	 *
	 * @since
	 */
	private void generateKeyPair() {
		Provider myProvider =
				new org.bouncycastle.jce.provider.BouncyCastleProvider();

		Security.addProvider(myProvider);

		// Key Pair Generation...
		SecureRandom rand = new SecureRandom();
		JDKKeyPairGenerator.RSA keyPairGen = new JDKKeyPairGenerator.RSA();

		keyPairGen.initialize(1024, rand);

		KeyPair keyPair = keyPairGen.generateKeyPair();

		privateKey = keyPair.getPrivate();
		publicKey = keyPair.getPublic();
	}


}


