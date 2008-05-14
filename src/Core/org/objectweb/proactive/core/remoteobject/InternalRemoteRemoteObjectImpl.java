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
package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.AccessControlException;
import java.security.PublicKey;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * An InternalRemoteRemoteObject is a generic object owns by any remote remote object.
 * It seats between the protocol dependent part of the remote object on the server
 * side (the XXXRemoteObjectImpl) and the remote object implementation.
 * It handles all the requests related to the communication protocol of a remote remote object
 * that acts as a proxy for a remote object.
 * Whereas it can be seen as a protocol dependent object, the internal remote remote object behaviour is the same
 * for all the implementation of any protocol. This is why it is an internal remote remote
 * object, hidden to the level on the protocol dependent part of the remote object it represents
 * that only provides a transport layer
 */
public class InternalRemoteRemoteObjectImpl implements InternalRemoteRemoteObject {

    /**
     * the remote remote object of the internal remote remote object.
     * Remote method calls are received by this remote remote object,
     * go through the internal remote remote object.
     */
    private RemoteRemoteObject remoteRemoteObject;

    /**
     * the URI where the remote remote object is bound
     */
    private URI uri;

    /**
     * the remote object that contains the reified object
     */
    private transient RemoteObject remoteObject;

    public InternalRemoteRemoteObjectImpl() {
    }

    public InternalRemoteRemoteObjectImpl(RemoteObject ro) {
        this.remoteObject = ro;
    }

    public InternalRemoteRemoteObjectImpl(RemoteObject ro, URI uri) {
        this.remoteObject = ro;
        this.uri = uri;
    }

    /**
     * Constructor for an internal remote remote object
     * @param ro the remote object to represent
     * @param uri the uri where the remote remote object is bound
     * @param rro the remote remote object activated on a given URI for a given
     * protocol
     */
    public InternalRemoteRemoteObjectImpl(RemoteObject ro, URI uri, RemoteRemoteObject rro) {
        this.remoteObject = ro;
        this.uri = uri;
        this.remoteRemoteObject = rro;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#getURI()
     */
    public URI getURI() throws ProActiveException, IOException {
        return this.uri;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#setURI(java.net.URI)
     */
    public void setURI(URI uri) throws ProActiveException, IOException {
        this.uri = uri;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteRemoteObject#receiveMessage(org.objectweb.proactive.core.body.request.Request)
     */
    public Reply receiveMessage(Request message) throws ProActiveException, IOException,
            RenegotiateSessionException {
        if (message instanceof InternalRemoteRemoteObjectRequest) {
            Object o;
            try {
                o = message.getMethodCall().execute(this);
                return new SynchronousReplyImpl(new MethodCallResult(o, null));
            } catch (MethodCallExecutionFailedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return this.remoteObject.receiveMessage(message);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getCertificate()
     */
    public TypedCertificate getCertificate() throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getCertificate();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getEntities()
     */
    public Entities getEntities() throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getEntities();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getPolicy(org.objectweb.proactive.core.security.securityentity.Entities, org.objectweb.proactive.core.security.securityentity.Entities)
     */
    public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException,
            IOException {
        return this.remoteObject.getPolicy(local, distant);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getPublicKey()
     */
    public PublicKey getPublicKey() throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getPublicKey();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#publicKeyExchange(long, byte[])
     */
    public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException, IOException {
        return this.remoteObject.publicKeyExchange(sessionID, signature);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
            RenegotiateSessionException, IOException {
        return this.remoteObject.randomValue(sessionID, clientRandomValue);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
            byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
            throws SecurityNotAvailableException, RenegotiateSessionException, IOException {
        return this.remoteObject.secretKeyExchange(sessionID, encodedAESKey, encodedIVParameters,
                encodedClientMacKey, encodedLockData, parametersSignature);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#startNewSession(long, org.objectweb.proactive.core.security.SecurityContext, org.objectweb.proactive.core.security.TypedCertificate)
     */
    public long startNewSession(long distantSessionID, SecurityContext policy,
            TypedCertificate distantCertificate) throws SessionException, SecurityNotAvailableException,
            IOException {
        return this.remoteObject.startNewSession(distantSessionID, policy, distantCertificate);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#terminateSession(long)
     */
    public void terminateSession(long sessionID) throws SecurityNotAvailableException, IOException {
        this.remoteObject.terminateSession(sessionID);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#getRemoteObject()
     */
    public RemoteObject getRemoteObject() {
        return this.remoteObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#getRemoteRemoteObject()
     */
    public RemoteRemoteObject getRemoteRemoteObject() {
        return this.remoteRemoteObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#setRemoteRemoteObject(org.objectweb.proactive.core.remoteobject.RemoteRemoteObject)
     */
    public void setRemoteRemoteObject(RemoteRemoteObject remoteRemoteObject) {
        this.remoteRemoteObject = remoteRemoteObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#setRemoteObject(org.objectweb.proactive.core.remoteobject.RemoteObject)
     */
    public void setRemoteObject(RemoteObject remoteObject) {
        this.remoteObject = remoteObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#getObjectProxy()
     */
    public Object getObjectProxy() {
        try {
            return this.remoteObject.getObjectProxy(this.remoteRemoteObject);
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#getProActiveSecurityManager(org.objectweb.proactive.core.security.securityentity.Entity)
     */
    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
            throws SecurityNotAvailableException, AccessControlException, IOException {
        return this.remoteObject.getProActiveSecurityManager(user);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.security.SecurityEntity#setProActiveSecurityManager(org.objectweb.proactive.core.security.securityentity.Entity, org.objectweb.proactive.core.security.PolicyServer)
     */
    public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
            throws SecurityNotAvailableException, AccessControlException, IOException {
        this.remoteObject.setProActiveSecurityManager(user, policyServer);
    }
}
