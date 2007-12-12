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
package org.objectweb.proactive.core.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.objectweb.proactive.core.security.SecurityConstants.EntityType;


public class TypedCertificate implements Serializable {

    /**
     *
     */
    private transient X509Certificate cert;
    private final EntityType type;
    private final PrivateKey privateKey;
    private byte[] encodedCert;

    public TypedCertificate(X509Certificate cert, EntityType type,
        PrivateKey privateKey) {
        this.cert = cert;
        this.type = type;
        this.privateKey = privateKey;
        this.encodedCert = null;
    }

    public X509Certificate getCert() {
        return this.cert;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public EntityType getType() {
        return this.type;
    }

    //	public void setType(EntityType type) {
    //		this.type = type;
    //	}
    public TypedCertificate noPrivateKey() {
        return new TypedCertificate(this.cert, this.type, null);
    }

    @Override
    public String toString() {
        return getType() + ":" + getCert().toString();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (getCert() != null) {
            try {
                this.encodedCert = this.cert.getEncoded();
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
        }

        out.defaultWriteObject();
        this.encodedCert = null;
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.encodedCert != null) {
            this.cert = ProActiveSecurity.decodeCertificate(this.encodedCert);
        }

        this.encodedCert = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypedCertificate)) {
            return false;
        }
        TypedCertificate otherCert = (TypedCertificate) obj;

        if (!otherCert.getType().match(this.getType())) {
            return false;
        }
        if (!otherCert.getCert().equals(this.getCert())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.cert.hashCode();
    }
}
