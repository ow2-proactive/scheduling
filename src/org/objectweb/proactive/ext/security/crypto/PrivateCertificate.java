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
import java.io.*;

/**
 * PrivateCertificates are required for the authentication and the key_negociation protocols
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class PrivateCertificate implements Serializable {
	private PublicCertificate publicCertificate;
	private PrivateKey privateKey;
 
	/**
	 *  Constructor for the PrivateCertificate object
	 *
	 * @param  publicCertificate  The PublicCertificate to store in the PrivateCertificate
	 * @param  privateKey  The privateKey to store in the Certificate        
	 * @since
	 */
	public PrivateCertificate(PublicCertificate publicCertificate,
			PrivateKey privateKey) {
		this.publicCertificate = publicCertificate;
		this.privateKey = privateKey;
	}

	/**
	 * 
	 *
	 * @return   The PublicCertificate stored in the PrivateCertificate
	 * @since
	 */
	public PublicCertificate get_PublicCertificate() {
		return publicCertificate;
	}


	/**
	 * 
	 *
	 * @return   The PrivateKey stored in the PrivateCertificate
	 * @since
	 */
	public PrivateKey get_PrivateKey() {
		return privateKey;
	}


	/**
	 * 
	 *
	 * @return  The desciption of the Certificate
	 * @since
	 */
	public String toString() {
		String me;

		me =
				"Type\t\t: PrivateCertificate" + "\nDomain \t\t: "
				 + publicCertificate.get_CertificateIdentity().getDomainName()
				 + "\nDelivered \t: "
				 + publicCertificate.get_CertificateProperty().get_deliveryDate()
				 + "\nExpires \t: "
				 + publicCertificate.get_CertificateProperty().get_expirationDate();

		return me;
	}

}

