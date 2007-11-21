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
import java.security.AccessControlException;
import java.security.PublicKey;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * This class is enabled when the body is a forwarder.  It
 * acts like  a forwarder for all security related messages
 *
 */
public class InternalBodySecurity implements SecurityEntity {

    /**
         *
         */
    private static final long serialVersionUID = -2486146998877256304L;
    protected SecurityEntity distantBody;

    public InternalBodySecurity(UniversalBody distantBody) {
        this.distantBody = distantBody;
    }

    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        this.distantBody.terminateSession(sessionID);
    }

    public TypedCertificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return this.distantBody.getCertificate();
    }

    public long startNewSession(long distantSessionID, SecurityContext policy,
        TypedCertificate distantCertificate)
        throws IOException, SessionException, SecurityNotAvailableException {
        return this.distantBody.startNewSession(distantSessionID, policy,
            distantCertificate);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return this.distantBody.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return this.distantBody.randomValue(sessionID, clientRandomValue);
    }

    public byte[] publicKeyExchange(long sessionID, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        return this.distantBody.publicKeyExchange(sessionID, signature);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return this.distantBody.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    public void setDistantBody(UniversalBody distantBody) {
        this.distantBody = distantBody;
    }

    public boolean isLocalBody() {
        return this.distantBody == null;
    }

    /**
     * @return distant Body Adapter
     */
    public UniversalBody getDistantBody() {
        return ((UniversalBody) this.distantBody).getRemoteAdapter();
    }

    //    /**
    //     * @return distant object's certificate as byte array
    //     */
    //    public byte[] getCertificatEncoded()
    //        throws SecurityNotAvailableException, IOException {
    //        return this.distantBody.getCertificateEncoded();
    //    }

    /**
     * @param securityContext
     * @return securityContext with distant object context
     */
    public SecurityContext getPolicy(Entities local, Entities distant)
        throws SecurityNotAvailableException, IOException {
        return this.distantBody.getPolicy(local, distant);
    }

    public Entities getEntities()
        throws SecurityNotAvailableException, IOException {
        return this.distantBody.getEntities();
    }

    //    public byte[] getCertificateEncoded()
    //        throws SecurityNotAvailableException, IOException {
    //        return this.distantBody.getCertificateEncoded();
    //    }
    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
        throws SecurityNotAvailableException, AccessControlException,
            IOException {
        return this.distantBody.getProActiveSecurityManager(user);
    }

    public void setProActiveSecurityManager(Entity user,
        PolicyServer policyServer)
        throws SecurityNotAvailableException, AccessControlException,
            IOException {
        this.distantBody.setProActiveSecurityManager(user, policyServer);
    }
}
