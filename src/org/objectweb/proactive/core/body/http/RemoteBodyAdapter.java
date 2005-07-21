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

import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.body.BodyAdapter;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.http.util.exceptions.HTTPUnexpectedException;
import org.objectweb.proactive.core.body.http.util.messages.HttpLookupMessage;
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


public class RemoteBodyAdapter implements BodyAdapter, Serializable {

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

//    /**
//     * The port of the Runtime where the body is located
//     */
//    protected int port;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RemoteBodyAdapter() {
    }

    public RemoteBodyAdapter(UniversalBody body) throws ProActiveException {
        //distant
        this.bodyID = body.getID();
        this.url = ClassServer.getUrl();        
//        this.port = ClassServer.getServerSocketPort();
        remoteBodyStrategy = body;
        jobID = remoteBodyStrategy.getJobID();        
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Registers an active object into the table of body.
     * @param paBody the body of the active object to register.
     * @param urn The urn of the body (in fact his url + his name)
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public static void register(RemoteBodyAdapter paBody, String urn)
        throws java.io.IOException {
        
        int port = UrlBuilder.getPortFromUrl(urn);
//        System.out.println("port = " + port);
//        System.out.println("port config = " + ClassServer.getServerSocketPort());
        if (port != ClassServer.getServerSocketPort()) {
            throw new IOException(
                "Bad registering port. You have to register on the same port as the runtime");
        }

       
        
        urn = urn.substring(urn.lastIndexOf('/') + 1);

        urnBodys.put(urn, paBody);

        if (logger.isInfoEnabled()) {
            logger.info("register object  at " + urn);
            logger.info(urnBodys);
        }
    }

    /**
     * Unregisters an active object previously registered into the bodys table
     * @param urn the urn under which the active object has been registered
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
            int port = ClassServer.getServerSocketPort();    
            url = urn;
            if (urn.lastIndexOf(":") > 4) {
                port = UrlBuilder.getPortFromUrl(urn);

                port = Integer.parseInt(urn.substring(urn.lastIndexOf(':') +1,
                                         urn.lastIndexOf(':') + 5));
            } 

            urn = urn.substring(urn.lastIndexOf('/') + 1);


            HttpLookupMessage message = new HttpLookupMessage(urn, url, port);
            message.send();
//            message = (HttpLookupMessage) ProActiveXMLUtils.sendMessage(url,
//                    port, message, ProActiveXMLUtils.MESSAGE);
            //UniversalBody result = (UniversalBody) message.processMessage();
            UniversalBody result = message.getReturnedObject();
            //System.out.println("result = " + result );
            if (result == null) {
                throw new java.io.IOException("The url " + url +
                    " is not bound to any known object");
            } else {
                

                return result;
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HTTPUnexpectedException("Unexpected exception", e);
        }
    }

    // ------------------------------------------
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof RemoteBodyAdapter)) {
            return false;
        }
        RemoteBodyAdapter rba = (RemoteBodyAdapter) o;
        return remoteBodyStrategy.equals(rba.remoteBodyStrategy);
        //return (url.equals(rba.getURL()) && bodyID.equals(rba.getBodyID())) &&
        //(port == rba.getPort());       
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveRequest(org.objectweb.proactive.core.body.request.Request)
     */
    public int receiveRequest(Request request)
        throws IOException, RenegotiateSessionException {
        return remoteBodyStrategy.receiveRequest(request);
    }

    public int receiveReply(Reply reply) throws IOException {
        return remoteBodyStrategy.receiveReply(reply);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminate()
     */
    public void terminate() throws IOException {
        remoteBodyStrategy.terminate();
    }

    /**
     * Gets a body from an urn in the table that mps urns and bodies
     * @param urn The urn of the body 
     * @return the body mapping the urn
     */
     public static synchronized UniversalBody getBodyFromUrn(String urn) {
        return (UniversalBody) urnBodys.get(urn);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getNodeURL()
     */
    public String getNodeURL() {
        return remoteBodyStrategy.getNodeURL();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getID()
     */
    public UniqueID getID() {
        return remoteBodyStrategy.getID();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#updateLocation(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void updateLocation(UniqueID id, UniversalBody body)
        throws IOException {
        remoteBodyStrategy.updateLocation(id, body);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getRemoteAdapter()
     */
    public UniversalBody getRemoteAdapter() {        
        return this;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#enableAC()
     */
    public void enableAC() throws IOException {
        remoteBodyStrategy.enableAC();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#disableAC()
     */
    public void disableAC() throws IOException {
        remoteBodyStrategy.disableAC();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#setImmediateService(java.lang.String)
     */
    public void setImmediateService(String methodName)
        throws IOException {
        remoteBodyStrategy.setImmediateService(methodName);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#initiateSession(int, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void initiateSession(int type, UniversalBody body)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        remoteBodyStrategy.initiateSession(type, body);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        remoteBodyStrategy.terminateSession(sessionID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificate()
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {

        return  remoteBodyStrategy.getCertificate();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyFrom(java.security.cert.X509Certificate)
     */
    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getPolicyFrom(certificate);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.ext.security.Communication)
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        return remoteBodyStrategy.startNewSession(policy);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#negociateKeyReceiverSide(org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket, long)
     */
    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
        return remoteBodyStrategy.negociateKeyReceiverSide(confidentialityTicket,
            sessionID);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPublicKey()
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getPublicKey();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, Exception {
        return remoteBodyStrategy.randomValue(sessionID, cl_rand);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#publicKeyExchange(long, org.objectweb.proactive.core.body.UniversalBody, byte[], byte[], byte[])
     */
    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code) throws SecurityNotAvailableException, Exception {
        return remoteBodyStrategy.publicKeyExchange(sessionID, distantBody,
            my_pub, my_cert, sig_code);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, Exception {
        return remoteBodyStrategy.secretKeyExchange(sessionID, tmp, tmp1, tmp2,
            tmp3, tmp4);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyTo(java.lang.String, java.lang.String, java.lang.String)
     */
    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getPolicyTo(type, from, to);
    }

    /**
     *      * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getPolicy(securityContext);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getVNName()
     */
    public String getVNName() throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getVNName();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getCertificateEncoded();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getEntities()
     */
    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getEntities();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getProActiveSecurityManager()
     */
    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        return remoteBodyStrategy.getProActiveSecurityManager();
    }

    /**
     * @see org.objectweb.proactive.core.exceptions.Handlerizable#getHandlersLevel()
     */
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
    
    /**
     * @see org.objectweb.proactive.core.exceptions.Handlerizable#unsetExceptionHandler(java.lang.Class)
     */
    public Handler unsetExceptionHandler(Class exception)
        throws IOException {
        return remoteBodyStrategy.unsetExceptionHandler(exception);
    }

    /**
     * @see org.objectweb.proactive.core.exceptions.Handlerizable#setExceptionHandler(org.objectweb.proactive.core.exceptions.handler.Handler, java.lang.Class)
     */
    public void setExceptionHandler(Handler handler, Class exception)
        throws IOException {
        remoteBodyStrategy.setExceptionHandler(handler, exception);
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return jobID;
    }

    /**
     * Clear the local map of handlers
     * @see org.objectweb.proactive.core.exceptions.Handlerizable#clearHandlersLevel()
     */
    public void clearHandlersLevel() throws java.io.IOException {
        remoteBodyStrategy.clearHandlersLevel();
    }

    /**
     * Get information about the handlerizable object
     */
    public String getHandlerizableInfo() throws java.io.IOException {
        return remoteBodyStrategy.getHandlerizableInfo();
    }

   
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.remoteBodyStrategy = new HttpRemoteBodyImpl(this.bodyID, this.url,
                this.jobID);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        //return bodyID.hashCode();//jobID
        return remoteBodyStrategy.hashCode();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public int receiveFTMessage(FTMessage ev) throws IOException {
        return this.remoteBodyStrategy.receiveFTMessage(ev);
    }

    //
    // Implements Adapter
    //

    /**
     * This method must be called only locally.
    * @see org.objectweb.proactive.core.body.BodyAdapter#changeProxiedBody(org.objectweb.proactive.Body)
     */
    public void changeProxiedBody(Body newBody) {
        this.remoteBodyStrategy = newBody;
    }


    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#createShortcut(org.objectweb.proactive.core.component.request.Shortcut)
     */
    public void createShortcut(Shortcut shortcut) throws IOException {
        // TODO implement
        throw new ProActiveRuntimeException("create shortcut method not implemented yet");        
    }
}
