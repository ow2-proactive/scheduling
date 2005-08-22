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
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
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
 *   An adapter for a LocalBody to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 */
public class RmiRemoteBodyImpl extends java.rmi.server.UnicastRemoteObject
    implements RmiRemoteBody, java.rmi.server.Unreferenced {

    /**
     * A custom socket Factory
     */

    //    protected static RandomPortSocketFactory factory = new RandomPortSocketFactory(37002,
    //            5000);
    //  protected static BenchSocketFactory factory = new BenchSocketFactory();

    /**
     * The encapsulated local body
     * transient to deal with custom serialization of requests.
     */
    protected transient UniversalBody body;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RmiRemoteBodyImpl() throws RemoteException {
    }

    public RmiRemoteBodyImpl(UniversalBody body) throws RemoteException {
        //      super(0, factory, factory);
        this.body = body;
    }

    public RmiRemoteBodyImpl(UniversalBody body, RMIServerSocketFactory sf,
        RMIClientSocketFactory cf) throws RemoteException {
        super(0, cf, sf);
        this.body = body;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements RmiRemoteBody -----------------------------------------------
    //
    public String getJobID() {
        return body.getJobID();
    }

    public int receiveRequest(Request r)
        throws java.io.IOException, RenegotiateSessionException {
        return body.receiveRequest(r);
    }

    public int receiveReply(Reply r) throws java.io.IOException {
        return body.receiveReply(r);
    }

    /**
     * @see org.objectweb.proactive.core.body.rmi.RmiRemoteBody#terminate()
     */
    public void terminate() throws IOException {
        body.terminate();
    }

    public String getNodeURL() {
        return body.getNodeURL();
    }

    public UniqueID getID() {
        return body.getID();
    }

    public void updateLocation(UniqueID id, UniversalBody remoteBody)
        throws java.io.IOException {
        body.updateLocation(id, remoteBody);
    }

    public void unreferenced() {
        // System.out.println("RmiRemoteBodyImpl: unreferenced()");      
        // System.gc();
    }

    public void enableAC() throws java.io.IOException {
        body.enableAC();
    }

    public void disableAC() throws java.io.IOException {
        body.disableAC();
    }

    public void setImmediateService(String methodName)
        throws java.io.IOException {
        body.setImmediateService(methodName);
    }

    public void removeImmediateService(String methodName,
        Class[] parametersTypes) throws IOException {
        body.removeImmediateService(methodName, parametersTypes);
    }

    public void setImmediateService(String methodName, Class[] parametersTypes)
        throws IOException {
        body.setImmediateService(methodName, parametersTypes);
    }

    // SECURITY
    public void initiateSession(int type, UniversalBody rbody)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        body.initiateSession(type, rbody);
    }

    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        body.terminateSession(sessionID);
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        X509Certificate cert = body.getCertificate();
        return cert;
    }

    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        return body.getProActiveSecurityManager();
    }

    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, IOException {
        return body.getPolicyFrom(certificate);
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        return body.startNewSession(policy);
    }

    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
        return body.negociateKeyReceiverSide(confidentialityTicket, sessionID);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return body.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws Exception {
        return body.randomValue(sessionID, cl_rand);
    }

    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code) throws Exception {
        return body.publicKeyExchange(sessionID, distantBody, my_pub, my_cert,
            sig_code);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4) throws Exception {
        return body.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3, tmp4);
    }

    public Communication getPolicyTo(String type, String from, String to)
        throws java.io.IOException, SecurityNotAvailableException {
        return body.getPolicyTo(type, from, to);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.rmi.RmiRemoteBody#getVNName()
     */
    public String getVNName() throws IOException, SecurityNotAvailableException {
        return body.getVNName();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.rmi.RmiRemoteBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws IOException, SecurityNotAvailableException {
        return body.getCertificateEncoded();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.rmi.RmiRemoteBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws IOException, SecurityNotAvailableException {
        return body.getPolicy(securityContext);
    }

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return body.getEntities();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.rmi.RmiRemoteBody#receiveFTEvent(org.objectweb.proactive.core.body.ft.events.FTEvent)
     */
    public int receiveFTMessage(FTMessage fte) throws IOException {
        return this.body.receiveFTMessage(fte);
    }

    public void changeProxiedBody(Body newBody) {
        this.body = newBody;
    }

    //-------------------------------
    //  NFEProducer implementation
    //-------------------------------
    public void addNFEListener(NFEListener listener) {
        body.addNFEListener(listener);
    }

    public void removeNFEListener(NFEListener listener) {
        body.removeNFEListener(listener);
    }

    public int fireNFE(NonFunctionalException e) {
        return body.fireNFE(e);
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
}
