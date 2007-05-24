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
package org.objectweb.proactive.core.body.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.BodyAdapter;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.UniversalBodyForwarder;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;


/**
 *   An adapter for a LocalBody to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 */
public class RmiRemoteBodyForwarderImpl extends java.rmi.server.UnicastRemoteObject
    implements RmiRemoteBodyForwarder {

    /**
     * A custom socket Factory
     */

    //    protected static RandomPortSocketFactory factory = new RandomPortSocketFactory(37002,
    //            5000);
    //  protected static BenchSocketFactory factory = new BenchSocketFactory();
    protected transient UniversalBodyForwarder body;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RmiRemoteBodyForwarderImpl() throws RemoteException {
    }

    public RmiRemoteBodyForwarderImpl(UniversalBodyForwarder body)
        throws RemoteException {
        //      super(0, factory, factory);
        this.body = body;
    }

    public RmiRemoteBodyForwarderImpl(UniversalBodyForwarder body,
        RMIServerSocketFactory sf, RMIClientSocketFactory cf)
        throws RemoteException {
        super(0, cf, sf);
        this.body = body;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements RmiRemoteBodyForwarder -----------------------------------------
    //
    public int receiveRequest(UniqueID id, Request r)
        throws java.io.IOException, RenegotiateSessionException {
        return body.receiveRequest(id, r);
    }

    public int receiveReply(UniqueID id, Reply r) throws java.io.IOException {
        return body.receiveReply(id, r);
    }

    public String getNodeURL(UniqueID id) {
        return body.getNodeURL(id);
    }

    public UniqueID getID(UniqueID id) {
        return body.getID(id);
    }

    public void updateLocation(UniqueID id, UniqueID uid,
        UniversalBody remoteBody) throws java.io.IOException {
        body.updateLocation(id, uid, remoteBody);
    }

    public void unreferenced(UniqueID id) {
        // System.out.println("RmiRemoteBodyImpl: unreferenced()");      
        // System.gc();
    }

    public void enableAC(UniqueID id) throws java.io.IOException {
        body.enableAC(id);
    }

    public void disableAC(UniqueID id) throws java.io.IOException {
        body.disableAC(id);
    }

    // SECURITY
    public void terminateSession(UniqueID id, long sessionID)
        throws IOException, SecurityNotAvailableException {
        body.terminateSession(id, sessionID);
    }

    public X509Certificate getCertificate(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        X509Certificate cert = body.getCertificate(id);
        return cert;
    }

    public long startNewSession(UniqueID id, Communication policy)
        throws SecurityNotAvailableException, IOException,
            RenegotiateSessionException {
        return body.startNewSession(id, policy);
    }

    public PublicKey getPublicKey(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        return body.getPublicKey(id);
    }

    public byte[] randomValue(UniqueID id, long sessionID, byte[] cl_rand)
        throws RenegotiateSessionException, IOException,
            SecurityNotAvailableException {
        return body.randomValue(id, sessionID, cl_rand);
    }

    public byte[][] publicKeyExchange(UniqueID id, long sessionID,
        byte[] my_pub, byte[] my_cert, byte[] sig_code)
        throws IOException, RenegotiateSessionException,
            SecurityNotAvailableException, KeyExchangeException {
        return body.publicKeyExchange(id, sessionID, my_pub, my_cert, sig_code);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.rmi.RmiRemoteBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded(UniqueID id)
        throws IOException, SecurityNotAvailableException {
        return body.getCertificateEncoded(id);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.rmi.RmiRemoteBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(UniqueID id,
        SecurityContext securityContext)
        throws IOException, SecurityNotAvailableException {
        return body.getPolicy(id, securityContext);
    }

    public ArrayList getEntities(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        return body.getEntities(id);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.rmi.RmiRemoteBody#receiveFTEvent(org.objectweb.proactive.core.body.ft.events.FTEvent)
     */
    public Object receiveFTMessage(UniqueID id, FTMessage fte)
        throws IOException {
        return this.body.receiveFTMessage(id, fte);
    }

    public GCResponse receiveGCMessage(UniqueID id, GCMessage msg)
        throws IOException {
        return body.receiveGCMessage(id, msg);
    }

    public void setRegistered(UniqueID id, boolean registered)
        throws IOException {
        body.setRegistered(id, registered);
    }

    public void changeProxiedBody(UniqueID id, Body newBody)
        throws IOException {
        body.changeProxiedBody(id, newBody);
    }

    //-------------------------------
    //  NFEProducer implementation
    //-------------------------------
    public void addNFEListener(UniqueID id, NFEListener listener)
        throws IOException {
        body.addNFEListener(id, listener);
    }

    public void removeNFEListener(UniqueID id, NFEListener listener)
        throws IOException {
        body.removeNFEListener(id, listener);
    }

    public int fireNFE(UniqueID id, NonFunctionalException e)
        throws IOException {
        return body.fireNFE(id, e);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    //
    // -- SERIALIZATION -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        // long startTime=System.currentTimeMillis();
        //System.out.println("i am in serialization");
        out.defaultWriteObject();
        //System.out.println("i am in serialization");
        //long endTime=System.currentTimeMillis();
        //System.out.println(" SERIALIZATION OF REMOTEBODYIMPL lasted " + (endTime - startTime));
    }

    public String getJobID(UniqueID id) throws IOException {
        return body.getJobID(id);
    }

    public UniversalBody lookup(UniqueID id, String url)
        throws IOException {
        return body.lookup(id, url);
    }

    public void register(UniqueID id, String url) throws IOException {
        body.register(id, url);
    }

    public void unregister(UniqueID id, String url) throws IOException {
        body.unregister(id, url);
    }

    public BodyAdapter getRemoteAdapter(UniqueID id) {
        return body.getRemoteAdapter(id);
    }

    public byte[][] secretKeyExchange(UniqueID id, long sessionID,
        byte[] encodedAESKey, byte[] encodedIVParameters,
        byte[] encodedClientMacKey, byte[] encodedLockData,
        byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return body.secretKeyExchange(id, sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }
}
