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

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;

import java.io.IOException;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import java.util.ArrayList;


public class InternalBodySecurity {
    protected UniversalBody distantBody;

    public InternalBodySecurity(UniversalBody distantBody) {
        this.distantBody = distantBody;
    }

    public void initiateSession(int type, UniversalBody body)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        distantBody.initiateSession(type, body);
    }

    public void terminateSession(long sessionID)
        throws java.io.IOException, SecurityNotAvailableException {
        distantBody.terminateSession(sessionID);
    }

    public X509Certificate getCertificate()
        throws java.io.IOException, SecurityNotAvailableException {
        return distantBody.getCertificate();
    }

    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        return distantBody.getProActiveSecurityManager();
    }

    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, IOException {
        return distantBody.getPolicyFrom(certificate);
    }

    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, IOException {
        return distantBody.getPolicyTo(type, from, to);
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        return distantBody.startNewSession(policy);
    }

    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
        return distantBody.negociateKeyReceiverSide(confidentialityTicket,
            sessionID);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return distantBody.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws Exception {
        return distantBody.randomValue(sessionID, cl_rand);
    }

    public byte[][] publicKeyExchange(long sessionID, UniversalBody dBody,
        byte[] my_pub, byte[] my_cert, byte[] sig_code)
        throws Exception {
        return distantBody.publicKeyExchange(sessionID, dBody, my_pub, my_cert,
            sig_code);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4) throws Exception {
        return distantBody.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3,
            tmp4);
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
        return distantBody.getRemoteAdapter();
    }

    public String getVNName() throws IOException, SecurityNotAvailableException {
        return distantBody.getVNName();
    }

    /**
     * @return distant object's certificate as byte array
     */
    public byte[] getCertificatEncoded()
        throws IOException, SecurityNotAvailableException {
        return distantBody.getCertificateEncoded();
    }

    /**
     * @param securityContext
     * @return securityContext with distant object context
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws IOException, SecurityNotAvailableException {
        return distantBody.getPolicy(securityContext);
    }

    public ArrayList getEntities()
        throws IOException, SecurityNotAvailableException {
        return distantBody.getEntities();
    }
}
