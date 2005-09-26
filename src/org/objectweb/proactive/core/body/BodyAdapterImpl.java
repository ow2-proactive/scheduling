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
import java.io.ObjectStreamException;
import java.io.Serializable;
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


/**
 * An adapter for a RemoteBody. The Adpater is the generic entry point for remote calls
 * to a RemoteBody using different protocols such as RMI, RMISSH, IBIS, HTTP, JINI.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 * @author ProActiveTeam
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 */
public abstract class BodyAdapterImpl extends BodyAdapter implements Cloneable,
    Serializable {

    /** The encapsulated RmiRemoteBody */
    protected RemoteBody proxiedRemoteBody;

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

        BodyAdapterImpl rba = (BodyAdapterImpl) o;

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

    //--------------------------------------------
    // implements SecurityEntity
    //--------------------------------------------

    /**
     * entity certificate
     * @return returns entity certificate
     * @throws SecurityNotAvailableException if security is not available
     * @throws java.io.IOException if communication fails
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getCertificate();
    }

    /**
     * start an unvalidated empty session
     * @param policy policy associated to the session
     * @return session ID
     * @throws SecurityNotAvailableException if security is not available
     * @throws RenegotiateSessionException if the session immediatly expires
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proxiedRemoteBody.startNewSession(policy);
    }

    /**
     * entity public key
     * @return returns entity public key
     * @throws SecurityNotAvailableException
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPublicKey();
    }

    /**
     * Exchange random value between client and server entity
     * @param sessionID the session ID
     * @param clientRandomValue client random value
     * @return server random value
     * @throws SecurityNotAvailableException if the security is not available
     * @throws RenegotiateSessionException if the session has expired
     */
    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proxiedRemoteBody.randomValue(sessionID, clientRandomValue);
    }

    /**
     * exchange entity certificate and/or public key if certificate are not available
     * @param sessionID the session ID
     * @param myPublicKey encoded public key
     * @param myCertificate encoded certificate
     * @param signature encoded signature of previous paramaters
     * @return an array containing :
     *           - server certificate and/or server public key
     *           - encoded signature of these parameters
     * @throws SecurityNotAvailableException if the security is not available
     * @throws RenegotiateSessionException if the session has expired
     * @throws KeyExchangeException if a key data/length/algorithm is not supported
     */
    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, IOException {
        return proxiedRemoteBody.publicKeyExchange(sessionID, myPublicKey,
            myCertificate, signature);
    }

    /**
     * this method sends encoded secret parameters to the target entity
     * @param sessionID session ID
     * @param encodedAESKey the AES key use to exchange secret message
     * @param encodedIVParameters Initilization parameters for the AES key
     * @param encodedClientMacKey MAC key for checking signature of future messages
     * @param encodedLockData random value to prevent message replays by an external attacker
     * @param parametersSignature encoded signature of the previous parameters
     * @return an array containing  :
     *             - encoded server AES key
     *             - encoded IV parameters
     *             - encoded server MAC key
     *             - encoded lock data to prevent message replays
     *             - encoded signature of previous parameters
     * @throws SecurityNotAvailableException if this entity does not support security
     * @throws RenegotiateSessionException if the session has expired or has been cancelled during this exchange
     * @throws java.io.IOException if communication fails
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proxiedRemoteBody.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    /**
     * Ask the entity to fill the securityContext parameters with its own policy
     * according to the communication details contained in the given securityContext
     * @param securityContext communication details allowing the entity to
     * look for a matching policy
     * @return securityContext filled with this entity's policy
     * @throws SecurityNotAvailableException thrown the entity doest not support the security
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicy(securityContext);
    }

    /**
     * Entity's X509Certificate as byte array
     * @return entity's X509Certificate as byte array
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getCertificateEncoded();
    }

    /**
     * Retrieves all the entity's ID which contain this entity plus this entity ID.
     * @return returns all the entity's ID which contain this entity plus this entity ID.
     * @throws SecurityNotAvailableException if the target entity does not support security
     */
    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getEntities();
    }

    /**
     * terminate a given session
     * @param sessionID
     * @throws SecurityNotAvailableException id security is not available
     */
    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        proxiedRemoteBody.terminateSession(sessionID);
    }

    /* (non-Javadoc)
      * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
      */
    public Object receiveFTMessage(FTMessage ev) throws IOException {
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

    protected Object readResolve() throws ObjectStreamException {
        String prop = System.getProperty("proactive.hierarchicalRuntime");

        if ((prop != null) && prop.equals("true")) {
            ProActiveRuntimeForwarderImpl partf = (ProActiveRuntimeForwarderImpl) ProActiveRuntimeImpl.getProActiveRuntime();

            try {
                partf.getBodyForwarder().add((BodyAdapterImpl) this.clone());

                return new BodyAdapterForwarder(partf.getBodyAdapterForwarder(),
                    this, bodyID);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        return this;
    }
}
