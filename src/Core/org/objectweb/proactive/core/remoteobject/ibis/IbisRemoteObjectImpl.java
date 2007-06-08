package org.objectweb.proactive.core.remoteobject.ibis;

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
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousProxy;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class IbisRemoteObjectImpl extends ibis.rmi.server.UnicastRemoteObject
    implements IbisRemoteObject {
    protected RemoteObject remoteObject;
    protected URI uri;
    protected Object stub;

    public IbisRemoteObjectImpl() throws ibis.rmi.RemoteException {
    }

    public IbisRemoteObjectImpl(RemoteObject target)
        throws ibis.rmi.RemoteException {
        this.remoteObject = target;
    }

    public Reply receiveMessage(Request message)
        throws RemoteException, RenegotiateSessionException, ProActiveException {
        return remoteObject.receiveMessage(message);
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return remoteObject.getCertificate();
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return remoteObject.getCertificateEncoded();
    }

    public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, IOException {
        return remoteObject.getEntities();
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return remoteObject.getPolicy(securityContext);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return remoteObject.getPublicKey();
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        return remoteObject.publicKeyExchange(sessionID, myPublicKey,
            myCertificate, signature);
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return remoteObject.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return remoteObject.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return remoteObject.startNewSession(policy);
    }

    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        remoteObject.terminateSession(sessionID);
    }

    public Object getObjectProxy() throws ProActiveException, IOException {
        if (this.stub == null) {
            this.stub = this.remoteObject.getObjectProxy();
            ((SynchronousProxy) ((StubObject) this.stub).getProxy()).setRemoteObject(this);
        }

        return this.stub;
    }

    public void setObjectProxy(Object stub)
        throws ProActiveException, IOException {
        this.stub = stub;
    }

    public RemoteObject getRemoteObject() throws ProActiveException {
        return this.remoteObject;
    }

    public URI getURI() throws ProActiveException, IOException {
        return this.uri;
    }

    public void setURI(URI uri) throws ProActiveException, IOException {
        this.uri = uri;
    }
}
