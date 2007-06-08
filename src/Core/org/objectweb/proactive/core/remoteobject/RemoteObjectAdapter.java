package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.net.URI;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.exception.RemoteObjectConnection;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RemoteObjectAdapter implements RemoteObject {
    protected RemoteRemoteObject remoteObject;
    protected Object stub;
    protected URI uri;

    public RemoteObjectAdapter() {
    }

    public RemoteObjectAdapter(RemoteRemoteObject ro) throws ProActiveException{
        this.remoteObject = ro;
        try {
            this.uri = ro.getURI();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    public Reply receiveMessage(Request message)
        throws ProActiveException, RenegotiateSessionException {
        try {
            //            System.out.println(" calling " + message.getMethodName() + " on " +
            //                uri.toString());
            if (message.isOneWay()) {
                try {
                    this.remoteObject.receiveMessage(message);
                    return new SynchronousReplyImpl();
                } catch (RemoteObjectConnection roc) {
                    // unmarchalling exception could occurs
                    // means that remote object has been killed
                    return new SynchronousReplyImpl();
                }
            }

            return this.remoteObject.receiveMessage(message);
        } catch (IOException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .warn("unable to contact remote object at " +
                this.uri.toString() + " when calling " + message.getMethodName());
            //e.printStackTrace();
            //            throw new ProActiveException(e.getMessage());
            return new SynchronousReplyImpl();
        }
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

    public Object getObjectProxy() throws ProActiveException {
        try {
            if (this.stub == null) {
                this.stub = this.remoteObject.getObjectProxy();
                ((SynchronousProxy) ((StubObject) this.stub).getProxy()).setRemoteObject(this);
            }
            return this.stub;
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }
}
