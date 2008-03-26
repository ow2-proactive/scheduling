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

import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.TypedCertificateList;


/**
 * @author The ProActive Team
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Entity implements Serializable {

    /**
     *
     */

    //	private static final Logger logger = ProActiveLogger.getLogger(Loggers.SECURITY);
    private final TypedCertificateList certChain;

    //	public Entity() {
    //		// needed for serializable ?
    //	}
    public Entity(TypedCertificateList certChain) {
        this.certChain = certChain;
    }

    public EntityType getType() {
        return this.certChain.get(0).getType();
    }

    public String getName() {
        return this.certChain.get(0).getCert().getSubjectX500Principal().getName();
    }

    public TypedCertificateList getCertificateChain() {
        return this.certChain;
    }

    public TypedCertificate getCertificate() {
        if (this.certChain == null) {
            return null;
        }
        return this.certChain.get(0);
    }

    @Override
    public String toString() {
        X509Certificate certificate = getCertificate().getCert();
        String string = new String();
        string = "\nType : " + getCertificate().getType();
        string += "\nCertificate : ";

        if (certificate != null) {
            string += certificate.toString();
        } else {
            string += "*";
        }
        return string;
    }
}
