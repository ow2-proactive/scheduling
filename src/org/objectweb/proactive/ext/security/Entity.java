/*
 * Created on 11 sept. 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.objectweb.proactive.ext.security;

import java.io.IOException;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class Entity implements Serializable {
	protected static Logger logger = Logger.getLogger(Entity.class.getName());
	protected X509Certificate applicationCertificate;
	protected X509Certificate certificate;

	public X509Certificate getApplicationCertificate() {
	 return applicationCertificate;
	}
	
	public X509Certificate getCertificate(){
		return certificate;
	}
	public abstract String getName();
	public abstract boolean  equals(Entity e);
	
	// implements Serializable
	  private void writeObject(java.io.ObjectOutputStream out)
		  throws IOException {
		  out.defaultWriteObject();

		if (applicationCertificate != null ) {
		
		  try {
			  byte[] certE = applicationCertificate.getEncoded();
			  out.writeInt(certE.length);
			  out.write(certE);
		  } catch (CertificateEncodingException e) {
			  e.printStackTrace();
		  } catch (IOException e) {
			  e.printStackTrace();
		  }
	  } else {
		out.writeInt(0);
	  }
		  }
	  private void readObject(java.io.ObjectInputStream in)
		  throws IOException, ClassNotFoundException {
		  in.defaultReadObject();

		  int i = in.readInt();
		  if ( i != 0 ) {
		  
		  byte[] certEncoded = new byte[i];
		  in.read(certEncoded);

		  applicationCertificate = ProActiveSecurity.decodeCertificate(certEncoded);
		  }
	  }
	
}
