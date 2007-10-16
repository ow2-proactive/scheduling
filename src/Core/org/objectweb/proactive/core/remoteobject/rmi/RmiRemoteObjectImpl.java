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
package org.objectweb.proactive.core.remoteobject.rmi;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 * RMI implementation of the remote remote object interface
 *
 *
 */
public class RmiRemoteObjectImpl extends UnicastRemoteObject
    implements RmiRemoteObject {

    /**
    *
    */
    protected RemoteObject remoteObject;
    protected Object stub;
    protected URI uri;

    public RmiRemoteObjectImpl() throws java.rmi.RemoteException {
    }

    public RmiRemoteObjectImpl(RemoteObject target)
        throws java.rmi.RemoteException {
        this.remoteObject = target;
    }

    public RmiRemoteObjectImpl(RemoteObject target, RMIServerSocketFactory sf,
        RMIClientSocketFactory cf) throws java.rmi.RemoteException {
        super(0, cf, sf);
        this.remoteObject = target;
    }

    public Reply receiveMessage(Request message)
        throws RemoteException, RenegotiateSessionException, ProActiveException,
            IOException {
        if (message.isOneWay()) {
            this.remoteObject.receiveMessage(message);
            return null;
        }

        return this.remoteObject.receiveMessage(message);
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getCertificate();
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getCertificateEncoded();
    }

    public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getEntities();
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getPolicy(securityContext);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return this.remoteObject.getPublicKey();
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        return this.remoteObject.publicKeyExchange(sessionID, myPublicKey,
            myCertificate, signature);
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

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return this.remoteObject.startNewSession(policy);
    }

    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        this.remoteObject.terminateSession(sessionID);
    }

    public Object getObjectProxy() throws ProActiveException, IOException {
        if (this.stub == null) {
            this.stub = this.remoteObject.getObjectProxy(this);

            //            if (stub instanceof Adapter) {
            //            	 ((StubObject) ((Adapter)this.stub).getAdapter()).setProxy(new SynchronousProxy(null, new Object[] { this } ));
            //            } else {
            //            ((StubObject) this.stub).setProxy(new SynchronousProxy(null, new Object[] { this } ));
            //            }
        }
        return this.stub;
    }

    public void setObjectProxy(Object stub)
        throws ProActiveException, IOException {
        this.stub = stub;
    }

    public RemoteObject getRemoteObject()
        throws ProActiveException, IOException {
        return this.remoteObject;
    }

    public java.net.URI getURI() throws ProActiveException, IOException {
        return this.uri;
    }

    public void setURI(java.net.URI uri) throws ProActiveException, IOException {
        this.uri = uri;
    }

    public String getClassName() throws ProActiveException, IOException {
        return this.remoteObject.getClassName();
    }

    public String getProxyName() throws ProActiveException, IOException {
        return this.remoteObject.getProxyName();
    }

    public Class<?> getTargetClass() throws ProActiveException, IOException {
        return this.remoteObject.getTargetClass();
    }

    public Class<?> getAdapterClass() throws ProActiveException, IOException {
        return this.remoteObject.getAdapterClass();
    }
}
