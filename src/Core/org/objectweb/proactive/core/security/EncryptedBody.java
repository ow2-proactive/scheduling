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
import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.event.MessageEventListener;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * @author Arnaud Contes
 * Wrap an cyphered body as a normal body
 * used for transparency call.
 */
public class EncryptedBody implements Body, Serializable {
    // specify if this body is encrypted or not
    protected boolean isEncrypted = false;

    // session ID
    protected long sessionID;

    // serialized and encrypted body
    protected byte[] encryptedBody;

    public EncryptedBody() {
    }

    /**
     *
     */
    public EncryptedBody(byte[] encryptedBody, long sessionID) {
        this.encryptedBody = encryptedBody;
        this.sessionID = sessionID;
        isEncrypted = (encryptedBody != null);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#terminate()
     */
    public void terminate() {
    }

    /*
     * (non-Javadoc)
     * @see org.objectweb.proactive.Body#terminate(boolean)
     */
    public void terminate(boolean completeACs) {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#isAlive()
     */
    public boolean isAlive() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#isActive()
     */
    public boolean isActive() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#blockCommunication()
     */
    public void blockCommunication() {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#acceptCommunication()
     */
    public void acceptCommunication() {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#enterInThreadStore()
     */
    public void enterInThreadStore() {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#exitFromThreadStore()
     */
    public void exitFromThreadStore() {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#checkNewLocation(org.objectweb.proactive.core.UniqueID)
     */
    public UniversalBody checkNewLocation(UniqueID uniqueID) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#setPolicyServer(org.objectweb.proactive.ext.security.PolicyServer)
     */
    public void setPolicyServer(PolicyServer server) {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getFuturePool()
     */
    public FuturePool getFuturePool() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getRequestQueue()
     */
    public BlockingRequestQueue getRequestQueue() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getReifiedObject()
     */
    public Object getReifiedObject() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getName()
     */
    public String getName() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#sendRequest(org.objectweb.proactive.core.mop.MethodCall, org.objectweb.proactive.core.body.future.Future, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void sendRequest(MethodCall methodCall, Future future,
        UniversalBody destinationBody)
        throws IOException, RenegotiateSessionException {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#serve(org.objectweb.proactive.core.body.request.Request)
     */
    public void serve(Request request) {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveRequest(org.objectweb.proactive.core.body.request.Request)
     */
    public int receiveRequest(Request request)
        throws IOException, RenegotiateSessionException {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    public int receiveReply(Reply r) throws IOException {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getNodeURL()
     */
    public String getNodeURL() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getID()
     */
    public UniqueID getID() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#updateLocation(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#enableAC()
     */
    public void enableAC() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#disableAC()
     */
    public void disableAC() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#setImmediateService(java.lang.String)
     */
    public void setImmediateService(String methodName) {
    }

    public void removeImmediateService(String methodName) {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificate()
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getProActiveSecurityManager()
     */
    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.ext.security.Communication)
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException,
            RenegotiateSessionException {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#negociateKeyReceiverSide(org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket, long)
     */
    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPublicKey()
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] clientRandomValue) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#publicKeyExchange(long, org.objectweb.proactive.core.body.UniversalBody, byte[], byte[], byte[])
     */
    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, IOException,
            RenegotiateSessionException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyTo(java.lang.String, java.lang.String, java.lang.String)
     */
    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getVNName()
     */
    public String getVNName() throws SecurityNotAvailableException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getEntities()
     */
    public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.message.MessageEventProducer#addMessageEventListener(org.objectweb.proactive.core.event.MessageEventListener)
     */
    public void addMessageEventListener(MessageEventListener listener) {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.message.MessageEventProducer#removeMessageEventListener(org.objectweb.proactive.core.event.MessageEventListener)
     */
    public void removeMessageEventListener(MessageEventListener listener) {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.Body#updateNodeURL(java.lang.String)
     */
    public void updateNodeURL(String newNodeURL) {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public Object receiveFTMessage(FTMessage ev) throws IOException {
        return null;
    }

    public GCResponse receiveGCMessage(GCMessage msg) throws IOException {
        return null;
    }

    public void setRegistered(boolean registered) {
    }

    public UniversalBody getShortcutTargetBody(ItfID functionalItfID) {
        return null;
    }

    public void createShortcut(Shortcut shortcut) throws IOException {
    }

    public void setImmediateService(String methodName,
        Class<?>[] parametersTypes) {
    }

    public void removeImmediateService(String methodName,
        Class<?>[] parametersTypes) {
    }

    public UniversalBody getRemoteAdapter() {
        return null;
    }

    public String getReifiedClassName() {
        return null;
    }

    /*
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getNextSequenceID()
     */
    public long getNextSequenceID() {
        return 0;
    }

    public BodyWrapperMBean getMBean() {
        return null;
    }

    public boolean checkMethod(String methodName, Class<?>[] parametersTypes) {
        return false;
    }

    public boolean checkMethod(String methodName) {
        return false;
    }

    public void register(String url)
        throws IOException, UnknownProtocolException {
        // TODO Auto-generated method stub
    }

    public void registerIncomingFutures() {
        // TODO Auto-generated method stub
    }
}
