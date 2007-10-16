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
package org.objectweb.proactive.core.remoteobject.http;

import java.io.IOException;
import java.net.URI;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousProxy;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.remoteobject.http.message.RemoteObjectRequest;
import org.objectweb.proactive.core.remoteobject.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.remoteobject.http.util.messages.HttpRemoteObjectRequest;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class HttpRemoteObjectImpl implements RemoteRemoteObject {
    private boolean isLocal;

    //    private String servletURL;
    private URI remoteObjectURL;
    protected Object stub;
    protected transient RemoteObject remoteObject;

    public HttpRemoteObjectImpl(RemoteObject remoteObject, URI remoteObjectURL) {
        //    	Thread.dumpStack();

        //        if (ProActiveConfiguration.getInstance().osgiServletEnabled()) {
        //            this.servletURL = ClassServerServlet.getUrl();
        //        } else {
        //            this.servletURL = ClassServer.getUrl();
        //        }
        //
        //        System.out.println("HttpRemoteObjectImpl.HttpRemoteObjectImpl() -------------- servlet "  + this.servletURL);
        //
        this.remoteObjectURL = remoteObjectURL;

        //        try {
        //            this.stub = remoteObject.getObjectProxy(this);
        //        } catch (ProActiveException e) {
        //            e.printStackTrace();
        //        }
    }

    public Reply receiveMessage(Request message)
        throws IOException, RenegotiateSessionException, ProActiveException {
        ArrayList<Object> paramsList = new ArrayList<Object>();
        paramsList.add(message);

        RemoteObjectRequest req = new RemoteObjectRequest(message,
                this.remoteObjectURL.toString());

        req.send();

        SynchronousReplyImpl rep = (SynchronousReplyImpl) req.getReturnedObject();
        return rep;
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return remoteObject.getCertificate();
        } else {
            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("getCertificate",
                    new ArrayList<Object>(), this.remoteObjectURL.toString());
            br.send();
            try {
                return (X509Certificate) br.getReturnedObject();
            } catch (Exception e) {
                throw new HTTPRemoteException("Unexpected exception", e);
            }
        }
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return this.remoteObject.getCertificateEncoded();
        } else {
            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("getCertificateEncoded",
                    new ArrayList<Object>(), this.remoteObjectURL.toString());
            br.send();
            try {
                return (byte[]) br.getReturnedObject();
            } catch (Exception e) {
                throw new HTTPRemoteException("Unexpected exception", e);
            }
        }
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
        ((StubObject) stub).setProxy(new SynchronousProxy(null,
                new Object[] { this }));

        return stub;
    }

    public void setObjectProxy(Object stub)
        throws ProActiveException, IOException {
        this.stub = stub;
    }

    public void setURI(URI url) {
        this.remoteObjectURL = url;
    }

    public URI getURI() {
        return this.remoteObjectURL;
    }

    public RemoteObject getRemoteObject() throws ProActiveException {
        return new RemoteObjectAdapter(this);
    }

    public String getClassName() throws ProActiveException, IOException {
        if (isLocal) {
            return this.remoteObject.getClassName();
        } else {
            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("getClassName",
                    new ArrayList<Object>(), this.remoteObjectURL.toString());
            br.send();
            try {
                return (String) br.getReturnedObject();
            } catch (Exception e) {
                throw new HTTPRemoteException("Unexpected exception", e);
            }
        }
    }

    public String getProxyName() throws ProActiveException, IOException {
        if (isLocal) {
            return this.remoteObject.getProxyName();
        } else {
            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("getProxyName",
                    new ArrayList<Object>(), this.remoteObjectURL.toString());
            br.send();
            try {
                return (String) br.getReturnedObject();
            } catch (Exception e) {
                throw new HTTPRemoteException("Unexpected exception", e);
            }
        }
    }

    public Class<?> getTargetClass() throws ProActiveException, IOException {
        if (isLocal) {
            return this.remoteObject.getTargetClass();
        } else {
            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("getTargetClass",
                    new ArrayList<Object>(), this.remoteObjectURL.toString());
            br.send();
            try {
                return (Class<?>) br.getReturnedObject();
            } catch (Exception e) {
                throw new HTTPRemoteException("Unexpected exception", e);
            }
        }
    }

    public Class<?> getAdapterClass() throws ProActiveException, IOException {
        if (isLocal) {
            return this.remoteObject.getAdapterClass();
        } else {
            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("getAdapterClass",
                    new ArrayList<Object>(), this.remoteObjectURL.toString());
            br.send();
            try {
                return (Class<?>) br.getReturnedObject();
            } catch (Exception e) {
                throw new HTTPRemoteException("Unexpected exception", e);
            }
        }
    }
}
