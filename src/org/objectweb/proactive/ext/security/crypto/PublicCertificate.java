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
import java.io.*;
 
/**
 * PublicCertificates are required for the authentification and the key-negociation protocols.
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class PublicCertificate implements Serializable {
	private CertificateIdentity ID_bloc;
	private CertificateProperty property;
	private PublicKey certificatePublicKey;
	private PublicKey acPublicKey;
	private SignedObject signedCertificatePublicKey;
	private SignedObject signedCertificateProperty;
	private SignedObject signedID_bloc;

	/**
	 *  Constructor for the PublicCertificate object
	 *
	 * @param  ID_bloc                The certificate ID_Block
	 * @param  property               The certificate property
	 * @param  certificatePublicKey   The certificate PublicKey
	 * @param  acPublicKey            The PublicKey of the CertificationAuthority
	 * @param  certificatePrivateKey  The PrivateKey needed to sign the Certificate
	 * @param  acPrivateKey           The PrivateKey of the CertificationAuthority
	 * @since
	 */
	public PublicCertificate(CertificateIdentity ID_bloc,
			CertificateProperty property,
			PublicKey certificatePublicKey,
			PublicKey acPublicKey,
			PrivateKey certificatePrivateKey,
			PrivateKey acPrivateKey) {
		this.ID_bloc = ID_bloc;
		this.property = property;
		this.certificatePublicKey = certificatePublicKey;
		this.acPublicKey = acPublicKey;

		try {
			Signature signingEngine = Signature.getInstance("SHA-1/RSA", "BC");
			signedCertificatePublicKey = new SignedObject(certificatePublicKey, acPrivateKey, signingEngine);
			signedID_bloc = new SignedObject(ID_bloc, certificatePrivateKey, signingEngine);
			signedCertificateProperty = new SignedObject(property, certificatePrivateKey, signingEngine);
		} catch (Exception e) {
			System.out.println("Exception in certificate Signature : " + e);
		}
	}


	/**
	 * 
	 *
	 * @return   The CertificateIdentity
	 * @since
	 */
	public CertificateIdentity get_CertificateIdentity() {
		return ID_bloc;
	}

	/**
	 * 
	 *
	 * @return  The CertificateProperty
	 * @since
	 */
	public CertificateProperty get_CertificateProperty() {
		return property;
	}


	/**
	 * 
	 *
	 * @return  The PublicKey of the Certificate
	 * @since
	 */
	public PublicKey get_certificatePublicKey() {
		return certificatePublicKey;
	}

	/**
	 * 
	 *
	 * @return  The CertificationAuthority PublicKey
	 * @since
	 */
	public PublicKey get_acPublicKey() {
		return acPublicKey;
	}


	/**
	 * 
	 *
	 * @return The PublicKey signed by the CertificationAuthority
	 * @since
	 */
	public SignedObject get_signedCertificatePublicKey() {
		return signedCertificatePublicKey;
	}

	/**
	 * 
	 *
	 * @return The signed ID_bloc of the Certificate
	 * @since
	 */
	public SignedObject get_signedID_bloc() {
		return signedID_bloc;
	}


	/**
	 * 
	 *
	 * @return   The signed CertificateProperty of the Certificate
	 * @since
	 */
	public SignedObject get_signedCertificateProperty() {
		return signedCertificateProperty;
	}

	/**
	 * 
	 *
	 * @return   The PublicCertificate description
	 * @since
	 */
	public String toString() {
		return "Type\t\t: PublicCertificate" + "\nDomain \t\t: "
				 + ID_bloc.getDomainName() + "\nDelivered \t: "
				 + property.get_deliveryDate() + "\nExpires \t: "
				 + property.get_expirationDate();
	}

}

