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

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author acontes
 * RemoteObjectAdapter are used to hide the protocol specific part of a remote object ie the RemoteRemoteObject
 */
public class RemoteObjectAdapter implements RemoteObject {
    protected RemoteRemoteObject remoteObject;
    protected Object stub;
    protected URI uri;
    protected static Method[] methods;
    protected static Method[] securityMethods;
    protected static Method[] internalRROMethods;

    static {
        try {
            methods = new Method[20];
            methods[0] = RemoteObject.class.getDeclaredMethod("getObjectProxy",
                    new Class<?>[0]);
            methods[1] = RemoteObject.class.getDeclaredMethod("getObjectProxy",
                    new Class<?>[] { RemoteRemoteObject.class });
            methods[2] = RemoteObject.class.getDeclaredMethod("getClassName",
                    new Class<?>[0]);
            methods[3] = RemoteObject.class.getDeclaredMethod("getTargetClass",
                    new Class<?>[0]);
            methods[4] = RemoteObject.class.getDeclaredMethod("getProxyName",
                    new Class<?>[0]);
            methods[5] = RemoteObject.class.getDeclaredMethod("getAdapterClass",
                    new Class<?>[0]);

            securityMethods = new Method[20];
            internalRROMethods = new Method[20];
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public RemoteObjectAdapter() {
    }

    public RemoteObjectAdapter(RemoteRemoteObject ro) throws ProActiveException {
        this.remoteObject = ro;
        this.uri = getURI();
    }

    public Reply receiveMessage(Request message)
        throws ProActiveException, RenegotiateSessionException, IOException {
        try {
            return this.remoteObject.receiveMessage(message);
        } catch (EOFException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .debug("EOFException while calling method " +
                message.getMethodName());
            return new SynchronousReplyImpl();
        } catch (ProActiveException e) {
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .warn("unable to contact remote object at " +
                this.uri + " when calling " + message.getMethodName());
            return new SynchronousReplyImpl(e);
        }

        //        return new SynchronousReplyImpl();
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
        if (this.stub == null) {
            //                this.stub = this.remoteObject.getObjectProxy();
            try {
                Method m = InternalRemoteRemoteObject.class.getDeclaredMethod("getObjectProxy",
                        new Class<?>[] {  });
                MethodCall mc = MethodCall.getMethodCall(m, new Object[0],
                        new HashMap());
                Request r = new InternalRemoteRemoteObjectRequest(mc);

                SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

                this.stub = (Object) reply.getSynchResult();
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ProActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RenegotiateSessionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //                ((SynchronousProxy) ((StubObject) this.stub).getProxy()).setRemoteObject(this);
        }
        return this.stub;
    }

    public Object getObjectProxy(RemoteRemoteObject rmo)
        throws ProActiveException {
        if (this.stub == null) {
            try {
                Method m = InternalRemoteRemoteObject.class.getDeclaredMethod("getObjectProxy",
                        new Class<?>[] {  });
                MethodCall mc = MethodCall.getMethodCall(m,
                        new Object[] { rmo }, new HashMap());
                Request r = new InternalRemoteRemoteObjectRequest(mc);

                SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

                this.stub = (Object) reply.getSynchResult();
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ProActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RenegotiateSessionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return this.stub;
    }

    public String getClassName() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[2], new Object[0],
                    new HashMap());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

            return (String) reply.getSynchResult();
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RenegotiateSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public String getProxyName() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[4], new Object[0],
                    new HashMap());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

            return (String) reply.getSynchResult();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RenegotiateSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RemoteObjectAdapter) {
            return remoteObject.equals(((RemoteObjectAdapter) o).remoteObject);
        }
        return false;
    }

    public Class<?> getTargetClass() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[3], new Object[0],
                    new HashMap());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

            return (Class<?>) reply.getSynchResult();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RenegotiateSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public Class<?> getAdapterClass() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[5], new Object[0],
                    new HashMap());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

            return (Class<?>) reply.getSynchResult();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RenegotiateSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public URI getURI() {
        try {
            Method m = InternalRemoteRemoteObject.class.getDeclaredMethod("getURI",
                    new Class<?>[0]);
            MethodCall mc = MethodCall.getMethodCall(m, new Object[0],
                    new HashMap());

            Request r = new InternalRemoteRemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

            return (URI) reply.getSynchResult();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RenegotiateSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
