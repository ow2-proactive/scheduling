/*
 * Created on 27 janv. 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.objectweb.proactive.ext.security;

import java.io.Serializable;
import java.security.cert.X509Certificate;


/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EntityCertificate extends Entity implements Serializable {

	/**
	 * 
	 */
	public EntityCertificate(X509Certificate applicationCertificate, X509Certificate certificate) {
		super();
		this.applicationCertificate = applicationCertificate;
		this.certificate = certificate;
	}

	public String getName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.proactive.ext.security.Entity#equals(org.objectweb.proactive.ext.security.Entity)
	 */
	public boolean equals(Entity e) {
			return e.getCertificate().equals(certificate);
	}

}
