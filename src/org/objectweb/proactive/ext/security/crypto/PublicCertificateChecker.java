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
import java.security.*;
import java.util.*;

/**
 * The PublicCertificateCheckers are used to check the validity of the PublicCertificates.
 *
 * @author     Vincent RIBAILLIER
 * @created    July 19, 2001
 */
public class PublicCertificateChecker {

	/**
	 *  Constructor for the PublicCertificateChecker object
	 *
	 * @since
	 */
	public PublicCertificateChecker() {
	}

	/**
	 * Checks the validity of the PublicCertificate
	 *
	 * @param  publicCertificate  Description of Parameter
	 * @param  acPublicKey        The PublicKey of the Certification Authority
	 * @return                    true if valid, false otherwise
	 * @since
	 */
	public boolean checkValidity(PublicCertificate publicCertificate,
			PublicKey acPublicKey) {
		try {

			// We test that the CA public key of the certificate present in the certificate is the good one
			if (acPublicKey.toString().equals(publicCertificate.get_acPublicKey().toString())
					 == false) {
				return false;
				// We test the time validity of the certificate
			}

			if (publicCertificate.get_CertificateProperty().get_expirationDate().before(publicCertificate.get_CertificateProperty().get_deliveryDate())
					 == true) {
				return false;
			}

			if (publicCertificate.get_CertificateProperty().get_expirationDate().before(new Date(System.currentTimeMillis()))
					 == true) {
				return false;
			}

			Signature verificationEngine = Signature.getInstance("SHA-1/RSA",
					"BC");

			if (publicCertificate.get_signedCertificatePublicKey().verify(acPublicKey,
					verificationEngine)) {
				if (publicCertificate.get_signedID_bloc().verify(publicCertificate.get_certificatePublicKey(),
						verificationEngine)) {
					if (publicCertificate.get_signedCertificateProperty().verify(publicCertificate.get_certificatePublicKey(),
							verificationEngine)) {
						return true;
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("Exception during Certificate's signature check :"
					 + e);
		}

		return false;
	}

}

