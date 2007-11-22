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


public class InternalRemoteRemoteObjectImpl
    implements InternalRemoteRemoteObject {
    private URI uri;
    private transient RemoteObject remoteObject;
    private RemoteRemoteObject remoteRemoteObject;

    public InternalRemoteRemoteObjectImpl() {
    }

    public InternalRemoteRemoteObjectImpl(RemoteObject ro) {
        this.remoteObject = ro;
    }

    public InternalRemoteRemoteObjectImpl(RemoteObject ro, URI uri) {
        this.remoteObject = ro;
        this.uri = uri;
    }

    public InternalRemoteRemoteObjectImpl(RemoteObject ro, URI uri,
        RemoteRemoteObject rro) {
        this.remoteObject = ro;
        this.uri = uri;
        this.remoteRemoteObject = rro;
    }

    public URI getURI() throws ProActiveException, IOException {
        return this.uri;
    }

    public void setURI(URI uri) throws ProActiveException, IOException {
        this.uri = uri;
    }

    public Reply receiveMessage(Request message)
        throws ProActiveException, IOException, RenegotiateSessionException {
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

    public TypedCertificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getCertificate();
    }

    public Entities getEntities()
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getEntities();
    }

    public SecurityContext getPolicy(Entities local, Entities distant)
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getPolicy(local, distant);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getPublicKey();
    }

    public byte[] publicKeyExchange(long sessionID, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        return this.remoteObject.publicKeyExchange(sessionID, signature);
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return this.remoteObject.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return this.remoteObject.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    public long startNewSession(long distantSessionID, SecurityContext policy,
        TypedCertificate distantCertificate)
        throws SessionException, SecurityNotAvailableException, IOException {
        return this.remoteObject.startNewSession(distantSessionID, policy,
            distantCertificate);
    }

    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        this.remoteObject.terminateSession(sessionID);
    }

    public RemoteObject getRemoteObject() {
        return this.remoteObject;
    }

    public RemoteRemoteObject getRemoteRemoteObject() {
        return this.remoteRemoteObject;
    }

    public void setRemoteRemoteObject(RemoteRemoteObject remoteRemoteObject) {
        this.remoteRemoteObject = remoteRemoteObject;
    }

    public void setRemoteObject(RemoteObject remoteObject) {
        this.remoteObject = remoteObject;
    }

    public Object getObjectProxy() {
        try {
            return this.remoteObject.getObjectProxy(this.remoteRemoteObject);
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
        throws SecurityNotAvailableException, AccessControlException,
            IOException {
        return this.remoteObject.getProActiveSecurityManager(user);
    }

    public void setProActiveSecurityManager(Entity user,
        PolicyServer policyServer)
        throws SecurityNotAvailableException, AccessControlException,
            IOException {
        this.remoteObject.setProActiveSecurityManager(user, policyServer);
    }
}
