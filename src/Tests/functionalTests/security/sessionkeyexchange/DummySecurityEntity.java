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
package functionalTests.security.sessionkeyexchange;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.SecurityEntity;
import org.objectweb.proactive.core.security.crypto.AuthenticationException;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class DummySecurityEntity implements SecurityEntity {
    private ProActiveSecurityManager securityManager;

    public DummySecurityEntity(ProActiveSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public void initiateSession(int type, SecurityEntity body)
        throws IOException, CommunicationForbiddenException,
            AuthenticationException, RenegotiateSessionException,
            SecurityNotAvailableException {
        securityManager.initiateSession(type, body);
    }

    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException {
        securityManager.terminateSession(sessionID);
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException {
        return securityManager.getCertificate();
    }

    public ProActiveSecurityManager getProActiveSecurityManager() {
        return securityManager.getProActiveSecurityManager();
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return securityManager.startNewSession(policy);
    }

    public PublicKey getPublicKey() throws SecurityNotAvailableException {
        return securityManager.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return securityManager.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException {
        return securityManager.publicKeyExchange(sessionID, myPublicKey,
            myCertificate, signature);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return securityManager.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException {
        return securityManager.getPolicy(securityContext);
    }

    public String getVNName() throws SecurityNotAvailableException {
        return securityManager.getVNName();
    }

    public byte[] getCertificateEncoded() throws SecurityNotAvailableException {
        return securityManager.getCertificateEncoded();
    }

    public ArrayList<Entity> getEntities() throws SecurityNotAvailableException {
        return securityManager.getEntities();
    }
}
