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
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeForwarderImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * An adapter for a RemoteBodyForwarder. The Adpater is the generic entry point for remote calls
 * to a RemoteBodyForwarder using different protocols such as RMI, RMISSH, IBIS, HTTP.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 *
 * A BodyAdapterForwarder has also an UniqueID which permit to associate an adapter to a body
 * on the forwarder since forwarder basically works like a multiplexer.
 *
 * @author ProActiveTeam
 * @see org.objectweb.proactive.core.body.BodyForwarderImpl
 */
public class BodyAdapterForwarder extends BodyAdapter implements Cloneable,
    RemoteBody {

    /** The encapsulated RmiRemoteBody
     * Points to the BodyForwarder running on the forwarder.
     */
    protected RemoteBodyForwarder proxiedRemoteBody;

    /**
     * Create an adapter on the local RemoteBodyForwarder.
     * An adapter created by this constructor must not be send over the network
     */
    public BodyAdapterForwarder(RemoteBodyForwarder remoteBodyForwarder) {
        this.proxiedRemoteBody = remoteBodyForwarder;
        bodyID = null; // never used for the local adapter
    }

    /**
     * Create an adapter pointing on remoteBodyForwarder and associated to the id.
     * @param remoteBodyForwarder the bodyForwarder on the forwarder
     * @param id the ID of the associated BodyAdapter
     */
    public BodyAdapterForwarder(RemoteBodyForwarder remoteBodyForwarder,
        UniqueID id) {
        this.proxiedRemoteBody = remoteBodyForwarder;

        try {
            this.bodyID = id;
            this.jobID = remoteBodyForwarder.getJobID(id);
        } catch (IOException e) {
            jobID = null;
            System.err.println("Connexion to RemoteBodyForwarder(id=" + id +
                "failled, this BodyAdapterForwarder will be unusable");
            System.err.println(
                "You probably need to check your hierarchical deployment configuration file.");
        }
    }

    /**
     * Create an adpater pointing on the remoteBody pointed by defaultAdapterForwarder
     * and associated to the ID contained in remoteAdapter.
     * @param defaultAdapterForwarder the original adapter
     * @param remoteAdapter
     */
    public BodyAdapterForwarder(BodyAdapterForwarder defaultAdapterForwarder,
        BodyAdapter remoteAdapter) {
        this.proxiedRemoteBody = defaultAdapterForwarder.proxiedRemoteBody;
        this.bodyID = remoteAdapter.bodyID;
        this.jobID = remoteAdapter.jobID;
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();

        /*
         * This adapter forwarder is being deserialized on a forwarder.
         * This can only happen when a forwarder talks to another forwarder.
         *
         * We put this adapter into our cache (multiplexer), and create a new
         * adapter with the same ID but pointing on the current forwarder.
         *
         *  All calls doing on the adapter will be send to the forwarder which
         *  will forward the call.
         */
        if (ProActiveConfiguration.getInstance().isForwarder()) {
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
    @Override
    public void changeProxiedBody(Body newBody) throws IOException {
        this.proxiedRemoteBody.changeProxiedBody(bodyID, newBody);
    }

    @Override
    public UniversalBody lookup(String url) throws java.io.IOException {
        return this.proxiedRemoteBody.lookup(bodyID, url);
    }

    @Override
    public void register(String url) throws java.io.IOException {
        this.proxiedRemoteBody.register(bodyID, url);
    }

    @Override
    public void unregister(String url) throws java.io.IOException {
        this.proxiedRemoteBody.unregister(bodyID, url);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BodyAdapterForwarder)) {
            return false;
        }

        BodyAdapterForwarder rba = (BodyAdapterForwarder) o;

        return rba.bodyID == this.bodyID;
    }

    @Override
    public int hashCode() {
        return bodyID.hashCode();
    }

    protected void construct(BodyAdapterForwarder localBody,
        BodyAdapter remoteBody, UniqueID id) throws ProActiveException {
        this.bodyID = id;
        this.proxiedRemoteBody = localBody.proxiedRemoteBody;

        if (UniversalBody.bodyLogger.isDebugEnabled()) {
            UniversalBody.bodyLogger.debug(proxiedRemoteBody.getClass());
        }

        this.bodyID = remoteBody.getID();
        this.jobID = remoteBody.getJobID();
    }

    //--------------------------------------------
    // implements Job
    //--------------------------------------------
    public String getJobID() {
        return jobID;
    }

    //--------------------------------------------
    // implements UniversalBody
    //--------------------------------------------
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
     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.core.security.Communication)
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
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.core.security.SecurityContext)
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
    public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getEntities(bodyID);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public Object receiveFTMessage(FTMessage ev) throws IOException {
        return this.proxiedRemoteBody.receiveFTMessage(bodyID, ev);
    }

    public GCResponse receiveGCMessage(GCMessage msg) throws IOException {
        return proxiedRemoteBody.receiveGCMessage(bodyID, msg);
    }

    public void setRegistered(boolean registered) throws IOException {
        proxiedRemoteBody.setRegistered(bodyID, registered);
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

    /**
     * List all the existing objects registered in a registry.
     */
    @Override
    public String[] list(String url) throws IOException {
        throw new IOException("Lookup is not implemented for this Adapter");
    }
}
