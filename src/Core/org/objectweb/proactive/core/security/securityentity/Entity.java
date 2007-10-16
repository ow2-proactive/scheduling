/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.security.securityentity;

import java.io.IOException;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.security.ProActiveSecurity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class Entity implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SECURITY);
    protected X509Certificate applicationCertificate;
    protected byte[] encodedApplicationCertificate;
    protected X509Certificate certificate;

    public X509Certificate getApplicationCertificate() {
        return applicationCertificate;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public abstract String getName();

    public abstract boolean equals(Entity e);

    // implements Serializable
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        if (applicationCertificate != null) {
            try {
                encodedApplicationCertificate = applicationCertificate.getEncoded();
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
        }
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (encodedApplicationCertificate != null) {
            applicationCertificate = ProActiveSecurity.decodeCertificate(encodedApplicationCertificate);
        }
    }
}
