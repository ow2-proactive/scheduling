/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.runtime.http.RuntimeReply;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.CommunicationForbiddenException;
import org.objectweb.proactive.ext.security.Policy;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.ext.webservices.utils.HTTPUnexpectedException;
import org.objectweb.proactive.ext.webservices.utils.ProActiveXMLUtils;

import java.io.IOException;
import java.io.Serializable;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *   An adapter for a LocalBody to be able to receive remote calls using HTTP. This helps isolate HTTP-specific
 *   code into a small set of specific classes.
 * @author virginie
 */
public class HttpRemoteBodyImpl implements UniversalBody, Serializable {
    private static Logger logger = Logger.getLogger("XML_HTTP");
    UniqueID bodyID;
    String jobID;
    String url;
    int port;

    /**
     *
     * @param bodyID
     * @param url
     */
    public HttpRemoteBodyImpl(UniqueID bodyID, String url, String jobID) {
        this.bodyID = bodyID;
        this.url = url;
        this.port = UrlBuilder.getPortFromUrl(url);
        this.jobID = jobID;
    }

    public int receiveRequest(Request request)
        throws IOException, RenegotiateSessionException {
        try {
            //logger.debug("Receive Request " + request.getMethodName());
            HttpRequest xmlReq = new HttpRequest(request, bodyID);

            String rep = (String) ProActiveXMLUtils.sendMessage(url, port,
                    xmlReq, ProActiveXMLUtils.MESSAGE);
        } catch (RenegotiateSessionException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }

        return FTManager.NON_FT;
    }

    public int receiveReply(Reply reply) throws IOException {
        try {
            logger.debug("Receive Reply " + reply.getResult());

            HttpReply xmlReply = new HttpReply(reply, bodyID);
            String rep = (String) ProActiveXMLUtils.sendMessage(url, port,
                    xmlReply, ProActiveXMLUtils.MESSAGE);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }

        return FTManager.NON_FT;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminate()
     */
    public void terminate() throws java.io.IOException {
        try {
            sendRequest(new BodyRequest("terminate", new ArrayList(), bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public String getNodeURL() {
        try {
            return (String) sendRequest(new BodyRequest("getNodeURL",
                    new ArrayList(), bodyID));
        } catch (Exception e) {
            return "cannot contact the body to get the nodeURL";
        }
    }

    public UniqueID getID() {
        return bodyID;
    }

    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(id);
            paramsList.add(body);
            sendRequest(new BodyRequest("updateLocation", paramsList, bodyID));

            //System.out.println("end update Location");
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public void enableAC() throws IOException {
        try {
            sendRequest(new BodyRequest("enableAC", new ArrayList(), bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public void disableAC() throws IOException {
        try {
            sendRequest(new BodyRequest("disableAC", new ArrayList(), bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public void setImmediateService(String methodName)
        throws IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(methodName);
            sendRequest(new BodyRequest("setImmediateService", paramsList,
                    bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public void initiateSession(int type, UniversalBody body)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(new Integer(type));
            paramsList.add(body);
            sendRequest(new BodyRequest("initiateSession", paramsList, bodyID));
        } catch (CommunicationForbiddenException e) {
            throw e;
        } catch (AuthenticationException e) {
            throw e;
        } catch (RenegotiateSessionException e) {
            throw e;
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(new Long(sessionID));
            sendRequest(new BodyRequest("terminateSession", paramsList, bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        try {
            return (X509Certificate) sendRequest(new BodyRequest(
                    "getCertificate", new ArrayList(), bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(certificate);

            return (Policy) sendRequest(new BodyRequest("getPolicyFrom",
                    paramsList, bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(policy);

            return ((Long) sendRequest(new BodyRequest("startNewSession",
                    paramsList, bodyID))).longValue();
        } catch (RenegotiateSessionException e) {
            throw e;
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(confidentialityTicket);
            paramsList.add(new Long(sessionID));

            return (ConfidentialityTicket) sendRequest(new BodyRequest(
                    "negociateKeyReceiverSide", paramsList, bodyID));
        } catch (KeyExchangeException e) {
            throw e;
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        try {
            return (PublicKey) sendRequest(new BodyRequest("getPublicKey",
                    new ArrayList(), bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, Exception {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(new Long(sessionID));
            paramsList.add(cl_rand);

            return (byte[]) sendRequest(new BodyRequest("randomValue",
                    paramsList, bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code) throws SecurityNotAvailableException, Exception {
        ArrayList paramsList = new ArrayList();
        paramsList.add(new Long(sessionID));
        paramsList.add(distantBody);
        paramsList.add(my_pub);
        paramsList.add(my_cert);
        paramsList.add(sig_code);

        return (byte[][]) sendRequest(new BodyRequest("publicKeyExchange",
                paramsList, bodyID));
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, Exception {
        ArrayList paramsList = new ArrayList();
        paramsList.add(new Long(sessionID));
        paramsList.add(tmp);
        paramsList.add(tmp1);
        paramsList.add(tmp2);
        paramsList.add(tmp3);
        paramsList.add(tmp4);

        return (byte[][]) sendRequest(new BodyRequest("secretKeyExchange",
                paramsList, bodyID));
    }

    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(type);
            paramsList.add(from);
            paramsList.add(to);

            return (Communication) sendRequest(new BodyRequest("getPolicyTo",
                    paramsList, bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(securityContext);

            return (SecurityContext) sendRequest(new BodyRequest("getPolicy",
                    paramsList, bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public String getVNName() throws SecurityNotAvailableException, IOException {
        try {
            return (String) sendRequest(new BodyRequest("getVNName",
                    new ArrayList(), bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        try {
            return (byte[]) sendRequest(new BodyRequest(
                    "getCertificateEncoded", new ArrayList(), bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        try {
            return (ArrayList) sendRequest(new BodyRequest("getEntities",
                    new ArrayList(), bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        try {
            return (ProActiveSecurityManager) sendRequest(new BodyRequest(
                    "getProActiveSecurityManager", new ArrayList(), bodyID));
        } catch (SecurityNotAvailableException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    private Object sendRequest(BodyRequest req) throws Exception {
        RuntimeReply reply = (RuntimeReply) ProActiveXMLUtils.sendMessage(url,
                port, req, ProActiveXMLUtils.RUNTIME_REQUEST);

        return reply.getReturnedObject();
    }

    public HashMap getHandlersLevel() throws java.io.IOException {
        try {
            return (HashMap) sendRequest(new BodyRequest("getHandlersLevel",
                    new ArrayList(), bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public void setExceptionHandler(Class handler, Class exception)
        throws IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(handler);
            paramsList.add(exception);
            sendRequest(new BodyRequest("setExceptionHandler", paramsList,
                    bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public Handler unsetExceptionHandler(Class exception)
        throws IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(exception);

            return (Handler) sendRequest(new BodyRequest(
                    "unsetExceptionHandler", paramsList, bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public void setExceptionHandler(Handler handler, Class exception)
        throws IOException {
        try {
            ArrayList paramsList = new ArrayList();
            paramsList.add(handler);
            paramsList.add(exception);

            sendRequest(new BodyRequest("setExceptionHandler", paramsList,
                    bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    public String getJobID() {
        return this.jobID;
    }

    /**
     * Clear the local map of handlers
     */
    public void clearHandlersLevel() throws java.io.IOException {
        try {
            sendRequest(new BodyRequest("clearHandlersLevel", new ArrayList(),
                    bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    /**
     * Get information about the handlerizable object
     */
    public String getHandlerizableInfo() throws java.io.IOException {
        try {
            return (String) sendRequest(new BodyRequest(
                    "getHandlerizableInfo", new ArrayList(), bodyID));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
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
    public UniversalBody getRemoteAdapter() {
        return this;
    }

    /*
     * STILL NOT IMPLEMENTED
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTEvent(org.objectweb.proactive.core.body.ft.events.FTEvent)
     */
    public int receiveFTMessage(FTMessage ev) throws IOException {
        return FTManager.NON_FT;
    }
}
