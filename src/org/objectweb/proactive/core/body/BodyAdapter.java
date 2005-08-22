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
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.CommunicationForbiddenException;
import org.objectweb.proactive.ext.security.Policy;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 * An adapter for a RemoteBody. The Adpater is the generic entry point for remote calls
 * to a RemoteBody using different protocols such as RMI, RMISSH, IBIS, HTTP, JINI.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 * @author ProActiveTeam
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */
public abstract class BodyAdapter implements UniversalBody {

    /**
     * The encapsulated RmiRemoteBody
     */
    protected RemoteBody proxiedRemoteBody;

    /**
     * Cache the ID of the Body locally for speed
     */
    protected UniqueID bodyID;

    /**
     * Cache the jobID locally for speed
     */
    protected String jobID;

    //------------------------------------------
    // Adpater Methods
    //------------------------------------------

    /**
     * Change the body referenced by this adapter.
     * @param newBody the body referenced after the call
     * @exception java.io.IOException if a pb occurs during this method call
     */
    public void changeProxiedBody(Body newBody) throws IOException {
        this.proxiedRemoteBody.changeProxiedBody(newBody);
    }

    /**
     * Looks-up an active object previously registered in a registry. In fact it is the
     * remote version of the body of an active object that can be registered into the
     * Registry under a given URL.
     * @param url the url the remote Body is registered to
     * @return a UniversalBody
     * @exception java.io.IOException if the remote body cannot be found under the given url
     *      or if the object found is not of type RemoteBody
     */
    public abstract UniversalBody lookup(String url) throws java.io.IOException;

    /**
     * Registers an active object into protocol-specific registry. In fact it is the
     * remote version of the body of the active object that is registered into the
     * Registry under the given URL.
     * @param url the url under which the remote body is registered.
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public abstract void register(String url) throws java.io.IOException;

    /**
     * Unregisters an active object previously registered into a registry.
     * @param url the url under which the active object is registered.
     * @exception java.io.IOException if the remote object cannot be removed from the registry
     */
    public abstract void unregister(String url) throws java.io.IOException;

    public boolean equals(Object o) {
        if (!(o instanceof BodyAdapter)) {
            return false;
        }
        BodyAdapter rba = (BodyAdapter) o;
        return proxiedRemoteBody.equals(rba.proxiedRemoteBody);
    }

    public int hashCode() {
        return proxiedRemoteBody.hashCode();
    }

    protected void construct(RemoteBody remoteBody) throws ProActiveException {
        this.proxiedRemoteBody = remoteBody;
        if (bodyLogger.isDebugEnabled()) {
            bodyLogger.debug(proxiedRemoteBody.getClass());
        }
        try {
            this.bodyID = remoteBody.getID();
            this.jobID = remoteBody.getJobID();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    //--------------------------------------------
    // implements Job
    //--------------------------------------------

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return jobID;
    }

    //--------------------------------------------
    // implements UniversalBody
    //--------------------------------------------

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveRequest(org.objectweb.proactive.core.body.request.Request)
     */
    public int receiveRequest(Request request)
        throws IOException, RenegotiateSessionException {
        return proxiedRemoteBody.receiveRequest(request);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    public int receiveReply(Reply r) throws IOException {
        return proxiedRemoteBody.receiveReply(r);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getNodeURL()
     */
    public String getNodeURL() {
        try {
            return proxiedRemoteBody.getNodeURL();
        } catch (IOException e) {
            return "cannot contact the body to get the nodeURL";
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getID()
     */
    public UniqueID getID() {
        return bodyID;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#updateLocation(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
        proxiedRemoteBody.updateLocation(id, body);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#createShortcut(org.objectweb.proactive.core.component.request.Shortcut)
     */
    public void createShortcut(Shortcut shortcut) throws IOException {
        //      TODO implement
        throw new ProActiveRuntimeException(
            "create shortcut method not implemented yet");
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getRemoteAdapter()
     */
    public BodyAdapter getRemoteAdapter() {
        return this;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminate()
     */
    public void terminate() throws IOException {
        proxiedRemoteBody.terminate();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#enableAC()
     */
    public void enableAC() throws IOException {
        proxiedRemoteBody.enableAC();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#disableAC()
     */
    public void disableAC() throws IOException {
        proxiedRemoteBody.disableAC();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#setImmediateService(java.lang.String)
     */
    public void setImmediateService(String methodName)
        throws IOException {
        proxiedRemoteBody.setImmediateService(methodName);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#setImmediateService(java.lang.String, java.lang.Class[])
     */
    public void setImmediateService(String methodName, Class[] parametersTypes)
        throws IOException {
        proxiedRemoteBody.setImmediateService(methodName, parametersTypes);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#removeImmediateService(java.lang.String, java.lang.Class[])
     */
    public void removeImmediateService(String methodName,
        Class[] parametersTypes) throws IOException {
        proxiedRemoteBody.removeImmediateService(methodName, parametersTypes);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#initiateSession(int, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void initiateSession(int type, UniversalBody body)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        proxiedRemoteBody.initiateSession(type, body);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        proxiedRemoteBody.terminateSession(sessionID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificate()
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getCertificate();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getProActiveSecurityManager()
     */
    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getProActiveSecurityManager();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyFrom(java.security.cert.X509Certificate)
     */
    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicyFrom(certificate);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.ext.security.Communication)
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        return proxiedRemoteBody.startNewSession(policy);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#negociateKeyReceiverSide(org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket, long)
     */
    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
        return proxiedRemoteBody.negociateKeyReceiverSide(confidentialityTicket,
            sessionID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPublicKey()
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPublicKey();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, Exception {
        return proxiedRemoteBody.randomValue(sessionID, cl_rand);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#publicKeyExchange(long, org.objectweb.proactive.core.body.UniversalBody, byte[], byte[], byte[])
     */
    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code)
        throws SecurityNotAvailableException, Exception, 
            RenegotiateSessionException {
        return proxiedRemoteBody.publicKeyExchange(sessionID, distantBody,
            my_pub, my_cert, sig_code);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, Exception, 
            RenegotiateSessionException {
        return proxiedRemoteBody.secretKeyExchange(sessionID, tmp, tmp1, tmp2,
            tmp3, tmp4);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyTo(java.lang.String, java.lang.String, java.lang.String)
     */
    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicyTo(type, from, to);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicy(securityContext);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getVNName()
     */
    public String getVNName() throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getVNName();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getCertificateEncoded();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getEntities()
     */
    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getEntities();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public int receiveFTMessage(FTMessage ev) throws IOException {
        return this.proxiedRemoteBody.receiveFTMessage(ev);
    }

    //--------------------------------
    //  NFEProducer implementation
    //--------------------------------
    public void addNFEListener(NFEListener listener) {
        try {
            proxiedRemoteBody.addNFEListener(listener);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void removeNFEListener(NFEListener listener) {
        try {
            proxiedRemoteBody.removeNFEListener(listener);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int fireNFE(NonFunctionalException e) {
        try {
            return proxiedRemoteBody.fireNFE(e);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return 0;
        }
    }
}
