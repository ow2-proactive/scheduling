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

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.rmi.ClassServer;
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
import java.util.Hashtable;


public class RemoteBodyAdapter implements UniversalBody, Serializable {
    /**
    * an Hashtable containing all the http  adapters registered. They can be retrieved
    * thanks to the ProActive.lookupActive method
    */
    protected static transient Hashtable urnBodys = new Hashtable();
    private static Logger logger = Logger.getLogger("XML_HTTP");

    /**
    * Cache the jobID locally for speed
    */
    protected String jobID;
    private transient UniversalBody remoteBodyStrategy;

    /**
     * The unique  ID of the body
     */
    protected UniqueID bodyID;

    /**
     * The url of the Runtime where the body is located
     */
    protected String url;

    /**
     * The port of the Runntime where the body is located
     */
    protected int port;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RemoteBodyAdapter() {
    }

    public RemoteBodyAdapter(UniversalBody body) throws ProActiveException {
        //distant
        this.bodyID = body.getID();
        this.url = ClassServer.getUrl();
        this.port = ClassServer.getServerSocketPort();
        
        remoteBodyStrategy = body;
        jobID = remoteBodyStrategy.getJobID();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Registers an active object into the table of body.
     * @param obj the active object to register.
     * @param urn The urn of the body (in fact his url + his name)
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public static void register(RemoteBodyAdapter paBody, String urn)
        throws java.io.IOException {
        urn = urn.substring(urn.lastIndexOf('/') + 1);
        urnBodys.put(urn, paBody);

        if (logger.isInfoEnabled()) {
            logger.info("register object  at " + urn);
        }
    }

    /**
     * Unregisters an active object previously registered into the bodys table
     * @param url the urn under which the active object has been registered
     */
    public static void unregister(String urn) throws java.io.IOException {
        urnBodys.put(urn, null);
    }

    /**
     * Looks-up an active object previously registered in the bodys table .
     * @param urn the urn (in fact its url + name)  the remote Body is registered to
     * @return a UniversalBody
     */
    public static UniversalBody lookup(String urn) throws java.io.IOException {
        try {
            String url;
            int port = ClassServer.DEFAULT_SERVER_BASE_PORT;
            url = urn;

            if (urn.lastIndexOf(":") > 4) {
                port = UrlBuilder.getPortFromUrl(urn);

                //				port = Integer.parseInt(urn.substring(urn.lastIndexOf(':'),
                //                          urn.lastIndexOf(':') + 5));
            }

            urn = urn.substring(urn.lastIndexOf('/') + 1);

            HttpLookupMessage message = new HttpLookupMessage(urn);
            message = (HttpLookupMessage) ProActiveXMLUtils.sendMessage(url,
                    port, message, ProActiveXMLUtils.MESSAGE);

            UniversalBody result = (UniversalBody) message.processMessage();  
            if (result == null)
            	throw new java.io.IOException("The url " + url +
       	 	 		" is not bound to any known object");
            else
            	return result;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    // ------------------------------------------
    public boolean equals(Object o) {
        if (!(o instanceof RemoteBodyAdapter)) {
            return false;
        }
        RemoteBodyAdapter rba = (RemoteBodyAdapter) o;
        return remoteBodyStrategy.equals(rba.remoteBodyStrategy);
       //return (url.equals(rba.getURL()) && bodyID.equals(rba.getBodyID())) &&
		//(port == rba.getPort());       
       
    }

    public void receiveRequest(Request request)
        throws IOException, RenegotiateSessionException {
        remoteBodyStrategy.receiveRequest(request);
    }

    public void receiveReply(Reply reply) throws IOException {
        remoteBodyStrategy.receiveReply(reply);
    }

    public String getURL() {
        return this.url;
    }

    public static synchronized UniversalBody getBodyFromUrn(String urn) {
        return (UniversalBody) urnBodys.get(urn);
    }

    public String getNodeURL() {
        return remoteBodyStrategy.getNodeURL();
    }

    public UniqueID getID() {
        return remoteBodyStrategy.getID();
    }

    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
        remoteBodyStrategy.updateLocation(id, body);
    }

    public UniversalBody getRemoteAdapter() {
        return this;
    }

    public void enableAC() throws IOException {
        remoteBodyStrategy.enableAC();
    }

    public void disableAC() throws IOException {
        remoteBodyStrategy.disableAC();
    }

    public void setImmediateService(String methodName)
        throws IOException {
        remoteBodyStrategy.setImmediateService(methodName);
    }

    public void initiateSession(int type, UniversalBody body)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        remoteBodyStrategy.initiateSession(type, body);
    }

    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        remoteBodyStrategy.terminateSession(sessionID);
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getCertificate();
    }

    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getPolicyFrom(certificate);
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        return remoteBodyStrategy.startNewSession(policy);
    }

    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
        return remoteBodyStrategy.negociateKeyReceiverSide(confidentialityTicket,
            sessionID);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, Exception {
        return remoteBodyStrategy.randomValue(sessionID, cl_rand);
    }

    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code) throws SecurityNotAvailableException, Exception {
        return remoteBodyStrategy.publicKeyExchange(sessionID, distantBody,
            my_pub, my_cert, sig_code);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, Exception {
        return remoteBodyStrategy.secretKeyExchange(sessionID, tmp, tmp1, tmp2,
            tmp3, tmp4);
    }

    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getPolicyTo(type, from, to);
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getPolicy(securityContext);
    }

    public String getVNName() throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getVNName();
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getCertificateEncoded();
    }

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getEntities();
    }

    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getProActiveSecurityManager();
    }

    public HashMap getHandlersLevel() throws java.io.IOException {
        return remoteBodyStrategy.getHandlersLevel();
    }

    /*
        public void setExceptionHandler(Class handler, Class exception)
            throws IOException {

                logger.info("\n\n---------------------------------------\n-------- Comprend pas\n----------------------\n\n");
                //        remoteBodyStrategy.setExceptionHandler(handler, exception);
        }
    */
    public Handler unsetExceptionHandler(Class exception)
        throws IOException {
        return remoteBodyStrategy.unsetExceptionHandler(exception);
    }

    public void setExceptionHandler(Handler handler, Class exception)
        throws IOException {
        remoteBodyStrategy.setExceptionHandler(handler, exception);
    }

    public String getJobID() {
        return jobID;
    }

    /**
     * Clear the local map of handlers
     */
    public void clearHandlersLevel() throws java.io.IOException {
        remoteBodyStrategy.clearHandlersLevel();
    }

    /**
     * Get information about the handlerizable object
     * @return
     */
    public String getHandlerizableInfo() throws java.io.IOException {
        return remoteBodyStrategy.getHandlerizableInfo();
    }

    public UniqueID getBodyID() {
    	return this.bodyID;
    }
      

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.remoteBodyStrategy = new HttpRemoteBodyImpl(this.bodyID, this.url);
    }
     
    
    public int hashCode() {
        //return bodyID.hashCode();//jobID
        return remoteBodyStrategy.hashCode();
        
    }
    
    
}
