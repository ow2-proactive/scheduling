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
 * This class provides a command-line tool to display the properties of a public or private certificate.
 *
 * @author     Vincent RIBAILLIER
 * @created    July 19, 2001
 */
public class CertificateReader {

	/**
	 *  Constructor for the CertificateReader object
	 *
	 * @since
	 */
	public CertificateReader() {
	}

	/**
	 *  The main program for the CertificateReader class
	 *
	 * @param  args 
	 * @since
	 */
	public static void main(String[] args) {
		Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(myProvider);
		String file_name = "";
		try {
			file_name = args[0];
		}
		catch (Exception e) {
			System.out.println("Usage : java CertificateReader mycertificate");
		}
		PrivateCertificate privateCertificate;
		PublicCertificate publicCertificate;
		CertificateIdentity certificateIdentity;
		try {
			FileInputStream fin = new FileInputStream(file_name);
			ObjectInputStream in = new ObjectInputStream(fin);
			Object object = in.readObject();
			in.close();
			System.out.println(object.toString());
		} catch (Exception e) {
			System.out.println("Exception : " + e);
		}
	}

}

