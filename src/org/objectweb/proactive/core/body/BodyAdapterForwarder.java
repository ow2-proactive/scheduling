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
import org.objectweb.proactive.core.runtime.ProActiveRuntimeForwarderImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


public class BodyAdapterForwarder extends BodyAdapter implements Cloneable,
    RemoteBody {

    /** The encapsulated RmiRemoteBody */
    protected RemoteBodyForwarder proxiedRemoteBody;

    public BodyAdapterForwarder(RemoteBodyForwarder remoteBodyForwarder) {
        this.proxiedRemoteBody = remoteBodyForwarder;
        bodyID = null; // never used for the local adapter
    }

    public BodyAdapterForwarder(RemoteBodyForwarder remoteBodyForwarder,
        UniqueID id) {
        this.proxiedRemoteBody = remoteBodyForwarder;

        try {
            this.bodyID = remoteBodyForwarder.getID(id);
            this.jobID = remoteBodyForwarder.getJobID(id);
        } catch (IOException e) {
            // XXX cmathieu
            System.err.println(
                "Woops cannot retrieve bodyID/jobID from the RemoteBodyForwarder");
            System.err.println("All will probably go bad");
        }
    }

    public BodyAdapterForwarder(BodyAdapterForwarder defaultAdapterForwarder,
        BodyAdapter remoteAdapter, UniqueID id) {
        this.proxiedRemoteBody = defaultAdapterForwarder.proxiedRemoteBody;
        this.bodyID = remoteAdapter.bodyID;
        this.jobID = remoteAdapter.jobID;
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();

        String prop = System.getProperty("proactive.hierarchicalRuntime");
        if ((prop != null) && prop.equals("true")) {
            ProActiveRuntimeForwarderImpl partf = (ProActiveRuntimeForwarderImpl) ProActiveRuntimeImpl.getProActiveRuntime();

            try {
                partf.getBodyForwarder().add((BodyAdapterForwarder) this.clone());
                this.proxiedRemoteBody = partf.getBodyAdapterForwarder().proxiedRemoteBody;
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                UniversalBody.bodyLogger.warn(
                    "Cannot create a forwarder for this RemoteObject.");
            }
        }
    }

    //------------------------------------------
    // Adpater Methods
    //------------------------------------------
    public void changeProxiedBody(Body newBody) throws IOException {
        this.proxiedRemoteBody.changeProxiedBody(bodyID, newBody);
    }

    public UniversalBody lookup(String url) throws java.io.IOException {
        return this.proxiedRemoteBody.lookup(bodyID, url);
    }

    public void register(String url) throws java.io.IOException {
        this.proxiedRemoteBody.register(bodyID, url);
    }

    public void unregister(String url) throws java.io.IOException {
        this.proxiedRemoteBody.unregister(bodyID, url);
    }

    public boolean equals(Object o) {
        if (!(o instanceof BodyAdapterForwarder)) {
            return false;
        }

        BodyAdapterForwarder rba = (BodyAdapterForwarder) o;

        return rba.bodyID == this.bodyID;
    }

    public int hashCode() {
        return bodyID.hashCode();
    }

    protected void construct(BodyAdapterForwarder localBody,
        BodyAdapter remoteBody, UniqueID id) throws ProActiveException {
        this.bodyID = id;
        this.proxiedRemoteBody = localBody.proxiedRemoteBody;

        if (super.bodyLogger.isDebugEnabled()) {
            super.bodyLogger.debug(proxiedRemoteBody.getClass());
        }

        this.bodyID = remoteBody.getID();
        this.jobID = remoteBody.getJobID();
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
        return proxiedRemoteBody.receiveRequest(bodyID, request);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    public int receiveReply(Reply r) throws IOException {
        return proxiedRemoteBody.receiveReply(bodyID, r);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getNodeURL()
     */
    public String getNodeURL() {
        try {
            return proxiedRemoteBody.getNodeURL(bodyID);
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
    public void updateLocation(UniqueID uid, UniversalBody body)
        throws IOException {
        proxiedRemoteBody.updateLocation(bodyID, uid, body);
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
        try {
            return proxiedRemoteBody.getRemoteAdapter(bodyID);
        } catch (IOException e) {
            e.printStackTrace();

            return null; // XXX
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminate()
     */
    public void terminate() throws IOException {
        proxiedRemoteBody.terminate(bodyID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#enableAC()
     */
    public void enableAC() throws IOException {
        proxiedRemoteBody.enableAC(bodyID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#disableAC()
     */
    public void disableAC() throws IOException {
        proxiedRemoteBody.disableAC(bodyID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#setImmediateService(java.lang.String)
     */
    public void setImmediateService(String methodName)
        throws IOException {
        proxiedRemoteBody.setImmediateService(bodyID, methodName);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#setImmediateService(java.lang.String, java.lang.Class[])
     */
    public void setImmediateService(String methodName, Class[] parametersTypes)
        throws IOException {
        proxiedRemoteBody.setImmediateService(bodyID, methodName,
            parametersTypes);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#removeImmediateService(java.lang.String, java.lang.Class[])
     */
    public void removeImmediateService(String methodName,
        Class[] parametersTypes) throws IOException {
        proxiedRemoteBody.removeImmediateService(bodyID, methodName,
            parametersTypes);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        proxiedRemoteBody.terminateSession(bodyID, sessionID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificate()
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getCertificate(bodyID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.ext.security.Communication)
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        return proxiedRemoteBody.startNewSession(bodyID, policy);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPublicKey()
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPublicKey(bodyID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proxiedRemoteBody.randomValue(bodyID, sessionID, cl_rand);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proxiedRemoteBody.secretKeyExchange(bodyID, sessionID,
            encodedAESKey, encodedIVParameters, encodedClientMacKey,
            encodedLockData, parametersSignature);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicy(bodyID, securityContext);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getCertificateEncoded(bodyID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getEntities()
     */
    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getEntities(bodyID);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public int receiveFTMessage(FTMessage ev) throws IOException {
        return this.proxiedRemoteBody.receiveFTMessage(bodyID, ev);
    }

    //--------------------------------
    //  NFEProducer implementation
    //--------------------------------
    public void addNFEListener(NFEListener listener) {
        try {
            proxiedRemoteBody.addNFEListener(bodyID, listener);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void removeNFEListener(NFEListener listener) {
        try {
            proxiedRemoteBody.removeNFEListener(bodyID, listener);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int fireNFE(NonFunctionalException e) {
        try {
            return proxiedRemoteBody.fireNFE(bodyID, e);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();

            return 0;
        }
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, IOException {
        try {
            return proxiedRemoteBody.publicKeyExchange(bodyID, sessionID,
                myPublicKey, myCertificate, signature);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();

            return null;
        }
    }
}
