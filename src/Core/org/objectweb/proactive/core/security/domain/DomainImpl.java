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
package org.objectweb.proactive.core.security.domain;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.InvalidPolicyFile;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class DomainImpl implements SecurityDomain {
    private PolicyServer policyServer;

    // empty constructor
    public DomainImpl() {
    }

    // create policy Server
    public DomainImpl(String securityFile) {
        try {
            this.policyServer = ProActiveSecurityDescriptorHandler.createPolicyServer(securityFile);
        } catch (InvalidPolicyFile e) {
            e.printStackTrace();
        }
    }

    public SecurityContext getPolicy(SecurityContext securityContext) {
        try {
            return policyServer.getPolicy(securityContext);
        } catch (SecurityNotAvailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.ext.security.domain.Domain#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded() throws SecurityNotAvailableException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.ext.security.domain.Domain#getEntities()
     */
    public ArrayList<Entity> getEntities() throws SecurityNotAvailableException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.ext.security.domain.Domain#getName()
     */
    public String getName() {
        return null;
    }

    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException {
        return null;
    }

    public ProActiveSecurityManager getProActiveSecurityManager() {
        return null;
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return 0;
    }

    public PublicKey getPublicKey() throws SecurityNotAvailableException {
        return null;
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return null;
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException {
        return null;
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return null;
    }

    public String getVNName() throws SecurityNotAvailableException {
        return null;
    }
}
