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
package org.objectweb.proactive.core.body.http;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.RemoteBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPUnexpectedException;
import org.objectweb.proactive.core.body.http.util.messages.BodyRequest;
import org.objectweb.proactive.core.body.http.util.messages.HttpReply;
import org.objectweb.proactive.core.body.http.util.messages.HttpRequest;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 *   An adapter for a LocalBody to be able to receive remote calls using HTTP. This helps isolate HTTP-specific
 *   code into a small set of specific classes.
 * @author virginie
 */

/**
 * @author vlegrand
 *
 */
public class HttpRemoteBodyImpl implements RemoteBody {

    static {
        try {
            RuntimeFactory.getDefaultRuntime();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * The encapsulated local body
     * transient to deal with custom serialization of requests.
     */
    protected transient UniversalBody body;
    UniqueID bodyID;
    String jobID;
    String url;
    private boolean isLocal;

    //    int port; // port is no more necessary .. it is also included in the URL ...
    public HttpRemoteBodyImpl(UniversalBody body) {
        isLocal = true;
        this.body = body;
        this.bodyID = body.getID();
        this.url = ClassServer.getUrl();
        this.jobID = body.getJobID();
    }

    /**
     * @throws RenegotiateSessionException
     * @throws IOException
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveRequest(org.objectweb.proactive.core.body.request.Request)
     */
    public int receiveRequest(Request request)
        throws IOException, RenegotiateSessionException {
        if (isLocal) {
            return body.receiveRequest(request);
        } else {
            try {
                HttpRequest req = new HttpRequest(request, bodyID, this.url);
                req.send();
                return req.getReturnedObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveReply(org.objectweb.proactive.core.body.reply.Reply)
     */
    public int receiveReply(Reply reply) throws IOException {
        if (isLocal) {
            return body.receiveReply(reply);
        } else {
            try {
                HttpReply rep = new HttpReply(reply, bodyID, this.url);
                rep.send();

                return rep.getReturnedObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminate()
     */
    public void terminate() throws java.io.IOException {
        if (isLocal) {
            body.terminate();
        } else {
            (new BodyRequest("terminate", new ArrayList<Object>(), bodyID,
                this.url)).send();
        }
    }

    /**
     * @throws HTTPRemoteException
     * @see org.objectweb.proactive.core.body.UniversalBody#getNodeURL()
     */
    public String getNodeURL() throws HTTPRemoteException {
        if (isLocal) {
            return body.getNodeURL();
        } else {
            BodyRequest br = new BodyRequest("getNodeURL",
                    new ArrayList<Object>(), bodyID, this.url);
            br.send();
            try {
                return (String) br.getReturnedObject();
            } catch (Exception e) {
                throw new HTTPRemoteException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getID()
     */
    public UniqueID getID() {
        return bodyID;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#updateLocation(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
        if (isLocal) {
            body.updateLocation(id, body);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(id);
            paramsList.add(body);
            (new BodyRequest("updateLocation", paramsList, bodyID, this.url)).send();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#enableAC()
     */
    public void enableAC() throws IOException {
        if (isLocal) {
            body.enableAC();
        } else {
            (new BodyRequest("enableAC", new ArrayList<Object>(), bodyID,
                this.url)).send();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#disableAC()
     */
    public void disableAC() throws IOException {
        if (isLocal) {
            body.disableAC();
        } else {
            (new BodyRequest("disableAC", new ArrayList<Object>(), bodyID,
                this.url)).send();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#setImmediateService(java.lang.String)
     */
    public void setImmediateService(String methodName)
        throws IOException {
        if (isLocal) {
            body.setImmediateService(methodName);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(methodName);
            (new BodyRequest("setImmediateService", paramsList, bodyID, this.url)).send();
        }
    }

    public void setImmediateService(String methodName, Class[] parametersTypes)
        throws IOException {
        if (isLocal) {
            body.setImmediateService(methodName, parametersTypes);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(methodName);
            paramsList.add(parametersTypes);
            new BodyRequest("setImmediateService", paramsList, bodyID, this.url).send();
        }
    }

    public void removeImmediateService(String methodName,
        Class[] parametersTypes) throws IOException {
        if (isLocal) {
            body.removeImmediateService(methodName, parametersTypes);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(methodName);
            paramsList.add(parametersTypes);
            new BodyRequest("removeImmediateService", paramsList, bodyID,
                this.url).send();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        if (isLocal) {
            body.terminateSession(sessionID);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(new Long(sessionID));
            new BodyRequest("terminateSession", paramsList, bodyID, this.url).send();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificate()
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return body.getCertificate();
        } else {
            BodyRequest req = new BodyRequest("getCertificate",
                    new ArrayList<Object>(), bodyID, this.url);
            req.send();
            try {
                return (X509Certificate) req.getReturnedObject();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (Exception e) {
                throw new HTTPRemoteException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.core.security.Communication)
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException,
            RenegotiateSessionException {
        if (isLocal) {
            return body.startNewSession(policy);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(policy);
            BodyRequest req = new BodyRequest("startNewSession", paramsList,
                    bodyID, this.url);
            req.send();
            try {
                return ((Long) req.getReturnedObject()).longValue();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (RenegotiateSessionException ex1) {
                throw ex1;
            } catch (Exception e) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPublicKey()
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return body.getPublicKey();
        } else {
            BodyRequest req = new BodyRequest("getPublicKey",
                    new ArrayList<Object>(), bodyID, this.url);
            req.send();

            try {
                return (PublicKey) req.getReturnedObject();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (Exception e) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, IOException,
            RenegotiateSessionException {
        if (isLocal) {
            return body.randomValue(sessionID, cl_rand);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(new Long(sessionID));
            paramsList.add(cl_rand);

            BodyRequest req = new BodyRequest("randomValue", paramsList,
                    bodyID, this.url);
            req.send();
            try {
                return (byte[]) req.getReturnedObject();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (RenegotiateSessionException e) {
                throw e;
            } catch (Exception e) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.security.SecurityEntity#publicKeyExchange(long, byte[], byte[], byte[])
     */
    public byte[][] publicKeyExchange(long sessionID, byte[] my_pub,
        byte[] my_cert, byte[] sig_code)
        throws SecurityNotAvailableException, IOException, KeyExchangeException,
            RenegotiateSessionException {
        if (isLocal) {
            return body.publicKeyExchange(sessionID, my_pub, my_cert, sig_code);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(new Long(sessionID));
            paramsList.add(my_pub);
            paramsList.add(my_cert);
            paramsList.add(sig_code);

            BodyRequest req = new BodyRequest("publicKeyExchange", paramsList,
                    bodyID, this.url);
            req.send();

            try {
                return (byte[][]) req.getReturnedObject();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (RenegotiateSessionException e) {
                throw e;
            } catch (KeyExchangeException e) {
                throw e;
            } catch (Exception e) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @throws RenegotiateSessionException
     * @see org.objectweb.proactive.core.body.UniversalBody#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, IOException,
            RenegotiateSessionException {
        if (isLocal) {
            return body.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3, tmp4);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(new Long(sessionID));
            paramsList.add(tmp);
            paramsList.add(tmp1);
            paramsList.add(tmp2);
            paramsList.add(tmp3);
            paramsList.add(tmp4);

            BodyRequest req = new BodyRequest("secretKeyExchange", paramsList,
                    bodyID, this.url);
            req.send();
            try {
                return (byte[][]) req.getReturnedObject();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (RenegotiateSessionException e) {
                throw e;
            } catch (Exception e) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.core.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return body.getPolicy(securityContext);
        } else {
            ArrayList<Object> paramsList = new ArrayList<Object>();
            paramsList.add(securityContext);

            BodyRequest req = new BodyRequest("getPolicy", paramsList, bodyID,
                    this.url);
            req.send();

            try {
                return (SecurityContext) req.getReturnedObject();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (Exception e) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return body.getCertificateEncoded();
        } else {
            BodyRequest req = new BodyRequest("getCertificateEncoded",
                    new ArrayList<Object>(), bodyID, this.url);
            req.send();

            try {
                return (byte[]) req.getReturnedObject();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (Exception e) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getEntities()
     */
    @SuppressWarnings("unchecked")
	public ArrayList<Entity> getEntities()
        throws SecurityNotAvailableException, IOException {
        if (isLocal) {
            return body.getEntities();
        } else {
            BodyRequest req = new BodyRequest("getEntities",
                    new ArrayList<Object>(), bodyID, this.url);
            req.send();

            try {
                return (ArrayList<Entity>) req.getReturnedObject();
            } catch (SecurityNotAvailableException ex) {
                throw ex;
            } catch (Exception e) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return this.jobID;
    }

    /**
     * STILL NOT IMPLEMENTED
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(FTMessage)
     */
    public Object receiveFTMessage(FTMessage ev) throws IOException {
        return null;
    }

    public GCResponse receiveGCMessage(GCMessage msg) throws IOException {
        if (isLocal) {
            return body.receiveGCMessage(msg);
        } else {
            ArrayList<Object> paramList = new ArrayList<Object>();
            paramList.add(msg);

            BodyRequest br = new BodyRequest("receiveGCMessage", paramList,
                    bodyID, url);
            br.send();
            try {
                return (GCResponse) br.getReturnedObject();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void setRegistered(boolean registered) throws IOException {
        if (isLocal) {
            body.setRegistered(registered);
        } else {
            ArrayList<Object> paramList = new ArrayList<Object>();
            paramList.add(new Boolean(registered));

            BodyRequest br = new BodyRequest("setRegistered", paramList,
                    bodyID, url);
            br.send();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#createShortcut(org.objectweb.proactive.core.component.request.Shortcut)
     */
    public void createShortcut(Shortcut shortcut) throws IOException {
        if (bodyLogger.isDebugEnabled()) {
            bodyLogger.debug(
                "shortcuts are currently not implemented for http communications");
        }
    }

    //-------------------------------------
    //  NFEProducer implementation
    //-------------------------------------
    public void addNFEListener(NFEListener listener) throws HTTPRemoteException {
        if (isLocal) {
            body.addNFEListener(listener);
        } else {
            ArrayList<Object> paramList = new ArrayList<Object>();
            paramList.add(listener);

            new BodyRequest("addNFEListener", paramList, bodyID, url).send();
        }
    }

    public void removeNFEListener(NFEListener listener)
        throws HTTPRemoteException {
        if (isLocal) {
            body.removeNFEListener(listener);
        } else {
            ArrayList<Object> paramList = new ArrayList<Object>();
            paramList.add(listener);

            new BodyRequest("removeNFEListener", paramList, bodyID, url).send();
        }
    }

    public int fireNFE(NonFunctionalException e) throws HTTPRemoteException {
        if (isLocal) {
            return body.fireNFE(e);
        } else {
            ArrayList<Object> paramList = new ArrayList<Object>();
            paramList.add(e);
            BodyRequest br = new BodyRequest("fireNFE", paramList, bodyID, url);
            br.send();
            try {
                return ((Integer) br.getReturnedObject()).intValue();
            } catch (Exception e1) {
                throw new HTTPUnexpectedException("Unexpected exception", e);
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.RemoteBody#changeProxiedBody(org.objectweb.proactive.Body)
     */
    public void changeProxiedBody(Body newBody) throws IOException {
        if (isLocal) {
            this.body = newBody;
        } else {
            throw new IOException("Cannot change the body remotely");
        }
    }

    /*
       public boolean equals(Object o) {
               if (!(o instanceof HttpRemoteBodyImpl)) {
                       return false;
               }
               HttpRemoteBodyImpl rba = (HttpRemoteBodyImpl) o;
               return (remoteBodyAdapter.url.equals(rba.getURL()) && remoteBodyAdapter.bodyID.equals(rba.getBodyID())) &&
                   (remoteBodyAdapter.port == rba.getPort());
       }
     */

    //---------------------------------------------
    // Private methods
    //---------------------------------------------
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.isLocal = false;
    }
}
