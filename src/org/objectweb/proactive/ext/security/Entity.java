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

    public X509Certificate getCertificate() {
        return certificate;
    }

    public abstract String getName();

    public abstract boolean equals(Entity e);

    // implements Serializable
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.defaultWriteObject();

        if (applicationCertificate != null) {
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
        if (i != 0) {
            byte[] certEncoded = new byte[i];
            in.read(certEncoded);

            applicationCertificate = ProActiveSecurity.decodeCertificate(certEncoded);
        }
    }
}
