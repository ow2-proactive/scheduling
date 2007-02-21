/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.body.ibis;

import ibis.rmi.RemoteException;

import java.io.IOException;
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
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.rmi.RandomPortSocketFactory;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


public class IbisRemoteBodyImpl extends ibis.rmi.server.UnicastRemoteObject
    implements IbisRemoteBody, java.rmi.server.Unreferenced {

    /**
     * A custom socket Factory
     */
    protected static RandomPortSocketFactory factory = new RandomPortSocketFactory(37002,
            5000);

    /**
     * The encapsulated local body
     * transient to deal with custom serialization of requests.
     */
    protected transient UniversalBody body;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public IbisRemoteBodyImpl() throws RemoteException {
    }

    public IbisRemoteBodyImpl(UniversalBody body) throws RemoteException {
        //   super(0, factory, factory);
        if (bodyLogger.isDebugEnabled()) {
            bodyLogger.debug(" IbisRemoteBodyImpl<init> ");
        }
        this.body = body;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements IbisRemoteBody -----------------------------------------------
    //
    public int receiveRequest(Request r)
        throws java.io.IOException, RenegotiateSessionException {
        if (bodyLogger.isDebugEnabled()) {
            bodyLogger.debug("body  = " + body);
            bodyLogger.debug("request =  " + r.getMethodName());
        }
        return body.receiveRequest(r);
    }

    public int receiveReply(Reply r) throws java.io.IOException {
        return body.receiveReply(r);
    }

    /**
     * @see org.objectweb.proactive.core.body.ibis.IbisRemoteBody#terminate()
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

    public String getJobID() {
        return body.getJobID();
    }

    public void updateLocation(UniqueID id, UniversalBody remoteBody)
        throws java.io.IOException {
        body.updateLocation(id, remoteBody);
    }

    public void unreferenced() {
        if (bodyLogger.isDebugEnabled()) {
            // logger.debug("IbisRemoteBodyImpl: unreferenced()");      
        }

        //  System.gc();
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
    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        body.terminateSession(sessionID);
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        X509Certificate cert = body.getCertificate();
        return cert;
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        return body.startNewSession(policy);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return body.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws IOException, SecurityNotAvailableException, 
            RenegotiateSessionException {
        return body.randomValue(sessionID, cl_rand);
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] my_pub,
        byte[] my_cert, byte[] sig_code)
        throws IOException, SecurityNotAvailableException, 
            RenegotiateSessionException, KeyExchangeException {
        return body.publicKeyExchange(sessionID, my_pub, my_cert, sig_code);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws IOException, SecurityNotAvailableException, 
            RenegotiateSessionException {
        return body.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3, tmp4);
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

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.ibis.IbisRemoteBody#getEntities()
     */
    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return body.getEntities();
    }

    public void changeProxiedBody(Body newBody) {
        this.body = newBody;
    }

    //-------------------------------------
    //  NFEProducer implementation
    //-------------------------------------
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
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        //System.out.println("----- IbisRemoteBodyImpl.readObject() ");
        in.defaultReadObject();
    }

    /**
     * @see org.objectweb.proactive.core.body.ibis.IbisRemoteBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public Object receiveFTMessage(FTMessage fte) throws IOException {
        return this.body.receiveFTMessage(fte);
    }

    public GCResponse receiveGCMessage(GCMessage msg) throws IOException {
    	return body.receiveGCMessage(msg);
    }
    
    public void setRegistered(boolean registered) throws IOException {
    	body.setRegistered(registered);
    }
    
    /*
       private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
       long startTime=System.currentTimeMillis();
       out.defaultWriteObject();
       long endTime=System.currentTimeMillis();
       if (logger.isDebugEnabled()) {
       logger.debug(" SERIALIZATION OF REMOTEBODYIMPL lasted " + (endTime - startTime));
       }
       }
       private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
       in.defaultReadObject();
       }
     */
}
