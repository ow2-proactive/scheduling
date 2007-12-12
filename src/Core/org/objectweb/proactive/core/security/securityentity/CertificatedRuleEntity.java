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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.objectweb.proactive.core.security.KeyStoreTools;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.TypedCertificate;


public class CertificatedRuleEntity extends RuleEntity {

    /**
     *
     */
    protected final TypedCertificate certificate;

    public CertificatedRuleEntity(EntityType type, KeyStore keystore,
        String name)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        super(type,
            KeyStoreTools.getLevel(keystore,
                KeyStoreTools.getCertificate(keystore, type, name)));
        this.certificate = KeyStoreTools.getCertificate(keystore, type, name);
    }

    @Override
    protected Match match(Entity e) {
        for (TypedCertificate cert : e.getCertificateChain()) {
            if (this.certificate.equals(cert)) {
                return Match.OK;
            }
        }
        return Match.FAILED;
    }

    @Override
    public String toString() {
        return super.toString() + "\n\tCertificate : " +
        this.certificate.toString();
    }

    @Override
    public String getName() {
        return this.certificate.toString();
    }
}
