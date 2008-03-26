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
package org.objectweb.proactive.core.body.http;

/**
 *   An adapter for a LocalBody to be able to receive remote calls using HTTP. This helps isolate HTTP-specific
 *   code into a small set of specific classes.
 * @author The ProActive Team
 */

/**
 * @author The ProActive Team
 *
 */
public class HttpRemoteBodyImpl {
    //    static {
    //        try {
    //            RuntimeFactory.getDefaultRuntime();
    //        } catch (ProActiveException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //
    //    /**
    //     * The encapsulated local body
    //     * transient to deal with custom serialization of requests.
    //     */
    //    protected transient UniversalBody body;
    //    UniqueID bodyID;
    //    String jobID;
    //    String url;
    //    private boolean isLocal;
    //
    //    //    int port; // port is no more necessary .. it is also included in the URL ...
    //    public HttpRemoteBodyImpl(UniversalBody body) {
    //        isLocal = true;
    //        this.body = body;
    //        this.bodyID = body.getID();
    //        this.url = ClassServer.getUrl();
    //        this.jobID = body.getJobID();
    //    }
    //
    //    /**
    //     * @throws RenegotiateSessionException
    //     * @throws IOException
    //     * @see org.objectweb.proactive.core.body.UniversalBody#receiveRequest(org.objectweb.proactive.core.body.request.Request)
    //     */
    //    public int receiveRequest(Request request)
    //        throws IOException, RenegotiateSessionException {
    //        if (isLocal) {
    //            return body.receiveRequest(request);
    //        } else {
    //            try {
    //                HttpRequest req = new HttpRequest(request, bodyID, this.url);
    //                req.send();
    //                return req.getReturnedObject();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //            return 0;
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#receiveReply(org.objectweb.proactive.core.body.reply.Reply)
    //     */
    //    public int receiveReply(Reply reply) throws IOException {
    //        if (isLocal) {
    //            return body.receiveReply(reply);
    //        } else {
    //            try {
    //                HttpReply rep = new HttpReply(reply, bodyID, this.url);
    //                rep.send();
    //
    //                return rep.getReturnedObject();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //            return 0;
    //        }
    //    }
    //
    //    /**
    //     * @throws HTTPRemoteException
    //     * @see org.objectweb.proactive.core.body.UniversalBody#getNodeURL()
    //     */
    //    public String getNodeURL() throws HTTPRemoteException {
    //        if (isLocal) {
    //            return body.getNodeURL();
    //        } else {
    //            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("getNodeURL",
    //                    new ArrayList<Object>(), bodyID, this.url);
    //            br.send();
    //            try {
    //                return (String) br.getReturnedObject();
    //            } catch (Exception e) {
    //                throw new HTTPRemoteException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#getID()
    //     */
    //    public UniqueID getID() {
    //        return bodyID;
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#updateLocation(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody)
    //     */
    //    public void updateLocation(UniqueID id, UniversalBody body)
    //        throws IOException {
    //        if (isLocal) {
    //            body.updateLocation(id, body);
    //        } else {
    //            ArrayList<Object> paramsList = new ArrayList<Object>();
    //            paramsList.add(id);
    //            paramsList.add(body);
    //            (new HttpRemoteObjectRequest("updateLocation", paramsList, bodyID, this.url)).send();
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#enableAC()
    //     */
    //    public void enableAC() throws IOException {
    //        if (isLocal) {
    //            body.enableAC();
    //        } else {
    //            (new HttpRemoteObjectRequest("enableAC", new ArrayList<Object>(), bodyID,
    //                this.url)).send();
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#disableAC()
    //     */
    //    public void disableAC() throws IOException {
    //        if (isLocal) {
    //            body.disableAC();
    //        } else {
    //            (new HttpRemoteObjectRequest("disableAC", new ArrayList<Object>(), bodyID,
    //                this.url)).send();
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#terminateSession(long)
    //     */
    //    public void terminateSession(long sessionID)
    //        throws IOException, SecurityNotAvailableException {
    //        if (isLocal) {
    //            body.terminateSession(sessionID);
    //        } else {
    //            ArrayList<Object> paramsList = new ArrayList<Object>();
    //            paramsList.add(new Long(sessionID));
    //            new HttpRemoteObjectRequest("terminateSession", paramsList, bodyID, this.url).send();
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificate()
    //     */
    //    public X509Certificate getCertificate()
    //        throws SecurityNotAvailableException, IOException {
    //        if (isLocal) {
    //            return body.getCertificate();
    //        } else {
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("getCertificate",
    //                    new ArrayList<Object>(), bodyID, this.url);
    //            req.send();
    //            try {
    //                return (X509Certificate) req.getReturnedObject();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (Exception e) {
    //                throw new HTTPRemoteException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.core.security.Communication)
    //     */
    //    public long startNewSession(Communication policy)
    //        throws SecurityNotAvailableException, IOException,
    //            RenegotiateSessionException {
    //        if (isLocal) {
    //            return body.startNewSession(policy);
    //        } else {
    //            ArrayList<Object> paramsList = new ArrayList<Object>();
    //            paramsList.add(policy);
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("startNewSession", paramsList,
    //                    bodyID, this.url);
    //            req.send();
    //            try {
    //                return ((Long) req.getReturnedObject()).longValue();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (RenegotiateSessionException ex1) {
    //                throw ex1;
    //            } catch (Exception e) {
    //                throw new HTTPUnexpectedException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#getPublicKey()
    //     */
    //    public PublicKey getPublicKey()
    //        throws SecurityNotAvailableException, IOException {
    //        if (isLocal) {
    //            return body.getPublicKey();
    //        } else {
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("getPublicKey",
    //                    new ArrayList<Object>(), bodyID, this.url);
    //            req.send();
    //
    //            try {
    //                return (PublicKey) req.getReturnedObject();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (Exception e) {
    //                throw new HTTPUnexpectedException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#randomValue(long, byte[])
    //     */
    //    public byte[] randomValue(long sessionID, byte[] cl_rand)
    //        throws SecurityNotAvailableException, IOException,
    //            RenegotiateSessionException {
    //        if (isLocal) {
    //            return body.randomValue(sessionID, cl_rand);
    //        } else {
    //            ArrayList<Object> paramsList = new ArrayList<Object>();
    //            paramsList.add(new Long(sessionID));
    //            paramsList.add(cl_rand);
    //
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("randomValue", paramsList,
    //                    bodyID, this.url);
    //            req.send();
    //            try {
    //                return (byte[]) req.getReturnedObject();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (RenegotiateSessionException e) {
    //                throw e;
    //            } catch (Exception e) {
    //                throw new HTTPUnexpectedException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.security.SecurityEntity#publicKeyExchange(long, byte[], byte[], byte[])
    //     */
    //    public byte[][] publicKeyExchange(long sessionID, byte[] my_pub,
    //        byte[] my_cert, byte[] sig_code)
    //        throws SecurityNotAvailableException, IOException, KeyExchangeException,
    //            RenegotiateSessionException {
    //        if (isLocal) {
    //            return body.publicKeyExchange(sessionID, my_pub, my_cert, sig_code);
    //        } else {
    //            ArrayList<Object> paramsList = new ArrayList<Object>();
    //            paramsList.add(new Long(sessionID));
    //            paramsList.add(my_pub);
    //            paramsList.add(my_cert);
    //            paramsList.add(sig_code);
    //
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("publicKeyExchange", paramsList,
    //                    bodyID, this.url);
    //            req.send();
    //
    //            try {
    //                return (byte[][]) req.getReturnedObject();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (RenegotiateSessionException e) {
    //                throw e;
    //            } catch (KeyExchangeException e) {
    //                throw e;
    //            } catch (Exception e) {
    //                throw new HTTPUnexpectedException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @throws RenegotiateSessionException
    //     * @see org.objectweb.proactive.core.body.UniversalBody#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
    //     */
    //    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
    //        byte[] tmp2, byte[] tmp3, byte[] tmp4)
    //        throws SecurityNotAvailableException, IOException,
    //            RenegotiateSessionException {
    //        if (isLocal) {
    //            return body.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3, tmp4);
    //        } else {
    //            ArrayList<Object> paramsList = new ArrayList<Object>();
    //            paramsList.add(new Long(sessionID));
    //            paramsList.add(tmp);
    //            paramsList.add(tmp1);
    //            paramsList.add(tmp2);
    //            paramsList.add(tmp3);
    //            paramsList.add(tmp4);
    //
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("secretKeyExchange", paramsList,
    //                    bodyID, this.url);
    //            req.send();
    //            try {
    //                return (byte[][]) req.getReturnedObject();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (RenegotiateSessionException e) {
    //                throw e;
    //            } catch (Exception e) {
    //                throw new HTTPUnexpectedException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.core.security.SecurityContext)
    //     */
    //    public SecurityContext getPolicy(SecurityContext securityContext)
    //        throws SecurityNotAvailableException, IOException {
    //        if (isLocal) {
    //            return body.getPolicy(securityContext);
    //        } else {
    //            ArrayList<Object> paramsList = new ArrayList<Object>();
    //            paramsList.add(securityContext);
    //
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("getPolicy", paramsList, bodyID,
    //                    this.url);
    //            req.send();
    //
    //            try {
    //                return (SecurityContext) req.getReturnedObject();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (Exception e) {
    //                throw new HTTPUnexpectedException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
    //     */
    //    public byte[] getCertificateEncoded()
    //        throws SecurityNotAvailableException, IOException {
    //        if (isLocal) {
    //            return body.getCertificateEncoded();
    //        } else {
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("getCertificateEncoded",
    //                    new ArrayList<Object>(), bodyID, this.url);
    //            req.send();
    //
    //            try {
    //                return (byte[]) req.getReturnedObject();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (Exception e) {
    //                throw new HTTPUnexpectedException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#getEntities()
    //     */
    //    @SuppressWarnings("unchecked")
    //    public ArrayList<Entity> getEntities()
    //        throws SecurityNotAvailableException, IOException {
    //        if (isLocal) {
    //            return body.getEntities();
    //        } else {
    //            HttpRemoteObjectRequest req = new HttpRemoteObjectRequest("getEntities",
    //                    new ArrayList<Object>(), bodyID, this.url);
    //            req.send();
    //
    //            try {
    //                return (ArrayList<Entity>) req.getReturnedObject();
    //            } catch (SecurityNotAvailableException ex) {
    //                throw ex;
    //            } catch (Exception e) {
    //                throw new HTTPUnexpectedException("Unexpected exception", e);
    //            }
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.Job#getJobID()
    //     */
    //    public String getJobID() {
    //        return this.jobID;
    //    }
    //
    //    /**
    //     * STILL NOT IMPLEMENTED
    //     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(FTMessage)
    //     */
    //    public Object receiveFTMessage(FTMessage ev) throws IOException {
    //        return null;
    //    }
    //
    //    public GCResponse receiveGCMessage(GCMessage msg) throws IOException {
    //        if (isLocal) {
    //            return body.receiveGCMessage(msg);
    //        } else {
    //            ArrayList<Object> paramList = new ArrayList<Object>();
    //            paramList.add(msg);
    //
    //            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("receiveGCMessage", paramList,
    //                    bodyID, url);
    //            br.send();
    //            try {
    //                return (GCResponse) br.getReturnedObject();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //                return null;
    //            }
    //        }
    //    }
    //
    //    public void setRegistered(boolean registered) throws IOException {
    //        if (isLocal) {
    //            body.setRegistered(registered);
    //        } else {
    //            ArrayList<Object> paramList = new ArrayList<Object>();
    //            paramList.add(new Boolean(registered));
    //
    //            HttpRemoteObjectRequest br = new HttpRemoteObjectRequest("setRegistered", paramList,
    //                    bodyID, url);
    //            br.send();
    //        }
    //    }
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.UniversalBody#createShortcut(org.objectweb.proactive.core.component.request.Shortcut)
    //     */
    //    public void createShortcut(Shortcut shortcut) throws IOException {
    //        if (bodyLogger.isDebugEnabled()) {
    //            bodyLogger.debug(
    //                "shortcuts are currently not implemented for http communications");
    //        }
    //    }
    //
    //
    //    /**
    //     * @see org.objectweb.proactive.core.body.RemoteBody#changeProxiedBody(org.objectweb.proactive.Body)
    //     */
    //    public void changeProxiedBody(Body newBody) throws IOException {
    //        if (isLocal) {
    //            this.body = newBody;
    //        } else {
    //            throw new IOException("Cannot change the body remotely");
    //        }
    //    }
    //
    //    /*
    //       public boolean equals(Object o) {
    //               if (!(o instanceof HttpRemoteBodyImpl)) {
    //                       return false;
    //               }
    //               HttpRemoteBodyImpl rba = (HttpRemoteBodyImpl) o;
    //               return (remoteBodyAdapter.url.equals(rba.getURL()) && remoteBodyAdapter.bodyID.equals(rba.getBodyID())) &&
    //                   (remoteBodyAdapter.port == rba.getPort());
    //       }
    //     */
    //
    //    //---------------------------------------------
    //    // Private methods
    //    //---------------------------------------------
    //    private void readObject(java.io.ObjectInputStream in)
    //        throws IOException, ClassNotFoundException {
    //        in.defaultReadObject();
    //        this.isLocal = false;
    //    }
}
