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
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * This class is enabled when the body is a forwarder.  It
 * acts like  a forwarder for all security related messages
 *
 */
public class InternalBodySecurity implements SecurityEntity {
    protected SecurityEntity distantBody;

    public InternalBodySecurity(UniversalBody distantBody) {
        this.distantBody = distantBody;
    }

    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        distantBody.terminateSession(sessionID);
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return distantBody.getCertificate();
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return distantBody.startNewSession(policy);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return distantBody.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return distantBody.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        return distantBody.publicKeyExchange(sessionID, myPublicKey,
            myCertificate, signature);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return distantBody.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    public void setDistantBody(UniversalBody distantBody) {
        this.distantBody = distantBody;
    }

    public boolean isLocalBody() {
        return distantBody == null;
    }

    /**
     * @return distant Body Adapter
     */
    public UniversalBody getDistantBody() {
        return ((UniversalBody) distantBody).getRemoteAdapter();
    }

    /**
     * @return distant object's certificate as byte array
     */
    public byte[] getCertificatEncoded()
        throws SecurityNotAvailableException, IOException {
        return distantBody.getCertificateEncoded();
    }

    /**
     * @param securityContext
     * @return securityContext with distant object context
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return distantBody.getPolicy(securityContext);
    }

    public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, IOException {
        return distantBody.getEntities();
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return distantBody.getCertificateEncoded();
    }
}
