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
package org.objectweb.proactive.core.body.rmi;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.handler.Handler;
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

import java.io.IOException;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.HashMap;


public class RemoteBodyAdapter implements UniversalBody, java.io.Serializable {
    protected static Logger logger = Logger.getLogger(RemoteBodyAdapter.class.getName());

    /**
     * The encapsulated RemoteBody
     */
    protected RemoteBody proxiedRemoteBody;

    /**
     * Cache the ID of the Body locally for speed
     */
    protected UniqueID bodyID;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RemoteBodyAdapter() {
    }

    public RemoteBodyAdapter(RemoteBody remoteBody) throws ProActiveException {
        //	Thread.dumpStack();
        //	ProActiveConfiguration.getConfiguration().dumpLoadedProperties();
        this.proxiedRemoteBody = remoteBody;
        if (logger.isDebugEnabled()) {
            logger.debug(proxiedRemoteBody.getClass());
        }
        try {
            this.bodyID = remoteBody.getID();
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    public RemoteBodyAdapter(UniversalBody body) throws ProActiveException {
        try {
            this.proxiedRemoteBody = new RemoteBodyImpl(body);
        } catch (java.rmi.RemoteException e) {
            throw new ProActiveException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(proxiedRemoteBody.getClass());
        }
        this.bodyID = body.getID();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Registers an active object into a RMI registry. In fact it is the
     * remote version of the body of the active object that is registered into the
     * RMI Registry under the given URL.
     * @param obj the active object to register.
     * @param url the url under which the remote body is registered.
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public static void register(RemoteBodyAdapter bodyAdapter, String url)
        throws java.io.IOException {
        java.rmi.Naming.rebind(url, bodyAdapter.proxiedRemoteBody);
    }

    /**
     * Unregisters an active object previously registered into a RMI registry.
     * @param url the url under which the active object is registered.
     * @exception java.io.IOException if the remote object cannot be removed from the registry
     */
    public static void unregister(String url) throws java.io.IOException {
        try {
            java.rmi.Naming.unbind(url);
        } catch (java.rmi.NotBoundException e) {
            throw new java.io.IOException(
                "No object is bound to the given url : " + url);
        }
    }

    /**
     * Looks-up an active object previously registered in a RMI registry. In fact it is the
     * remote version of the body of an active object that can be registered into the
     * RMI Registry under a given URL.
     * @param url the url the remote Body is registered to
     * @return a UniversalBody
     * @exception java.io.IOException if the remote body cannot be found under the given url
     *      or if the object found is not of type RemoteBody
     */
    public static UniversalBody lookup(String url) throws java.io.IOException {
        Object o = null;

        // Try if URL is the address of a RemoteBody
        try {
            o = java.rmi.Naming.lookup(url);
        } catch (java.rmi.NotBoundException e) {
            throw new java.io.IOException("The url " + url +
                " is not bound to any known object");
        }
        if (o instanceof RemoteBody) {
            try {
                return new RemoteBodyAdapter((RemoteBody) o);
            } catch (ProActiveException e) {
                throw new java.io.IOException("Cannot build a Remote Adapter" +
                    e.toString());
            }
        } else {
            throw new java.io.IOException(
                "The given url does exist but doesn't point to a remote body  url=" +
                url + " class found is " + o.getClass().getName());
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof RemoteBodyAdapter)) {
            return false;
        }
        RemoteBodyAdapter rba = (RemoteBodyAdapter) o;
        return proxiedRemoteBody.equals(rba.proxiedRemoteBody);
    }

    public int hashCode() {
        return proxiedRemoteBody.hashCode();
    }

    //
    // -- implements UniversalBody -----------------------------------------------
    //
    public void receiveRequest(Request r)
        throws java.io.IOException, RenegotiateSessionException {
        proxiedRemoteBody.receiveRequest(r);
    }

    public void receiveReply(Reply r) throws java.io.IOException {
        proxiedRemoteBody.receiveReply(r);
    }

    public String getNodeURL() {
        try {
            return proxiedRemoteBody.getNodeURL();
        } catch (java.rmi.RemoteException e) {
            return "cannot contact the body to get the nodeURL";
        }
    }

    public UniqueID getID() {
        return bodyID;
    }

    public void updateLocation(UniqueID id, UniversalBody remoteBody)
        throws java.io.IOException {
        proxiedRemoteBody.updateLocation(id, remoteBody);
    }

    public UniversalBody getRemoteAdapter() {
        return this;
    }

    public void enableAC() throws java.io.IOException {
        proxiedRemoteBody.enableAC();
    }

    public void disableAC() throws java.io.IOException {
        proxiedRemoteBody.disableAC();
    }

    public void setImmediateService(String methodName)
        throws java.io.IOException {
        proxiedRemoteBody.setImmediateService(methodName);
    }

    // SECURITY
    public void initiateSession(int type, UniversalBody body)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        proxiedRemoteBody.initiateSession(type, body);
    }

    public void terminateSession(long sessionID)
        throws java.io.IOException, SecurityNotAvailableException {
        proxiedRemoteBody.terminateSession(sessionID);
    }

    public X509Certificate getCertificate()
        throws java.io.IOException, SecurityNotAvailableException {
        return proxiedRemoteBody.getCertificate();
    }

    public ProActiveSecurityManager getProActiveSecurityManager()
        throws java.io.IOException, SecurityNotAvailableException {
        return proxiedRemoteBody.getProActiveSecurityManager();
    }

    public Policy getPolicyFrom(X509Certificate certificate)
        throws java.io.IOException, SecurityNotAvailableException {
        return proxiedRemoteBody.getPolicyFrom(certificate);
    }

    public long startNewSession(Communication policy)
        throws IOException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        return proxiedRemoteBody.startNewSession(policy);
    }

    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws java.io.IOException, KeyExchangeException, 
            SecurityNotAvailableException {
        return proxiedRemoteBody.negociateKeyReceiverSide(confidentialityTicket,
            sessionID);
    }

    public PublicKey getPublicKey()
        throws java.io.IOException, SecurityNotAvailableException {
        return proxiedRemoteBody.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws Exception, SecurityNotAvailableException {
        return proxiedRemoteBody.randomValue(sessionID, cl_rand);
    }

    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code) throws Exception, SecurityNotAvailableException {
        return proxiedRemoteBody.publicKeyExchange(sessionID, distantBody,
            my_pub, my_cert, sig_code);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws Exception, SecurityNotAvailableException {
        return proxiedRemoteBody.secretKeyExchange(sessionID, tmp, tmp1, tmp2,
            tmp3, tmp4);
    }

    public Communication getPolicyTo(String type, String from, String to)
        throws java.io.IOException, SecurityNotAvailableException {
        return proxiedRemoteBody.getPolicyTo(type, from, to);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getVNName()
     */
    public String getVNName()
        throws java.io.IOException, SecurityNotAvailableException {
        return proxiedRemoteBody.getVNName();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws java.io.IOException, SecurityNotAvailableException {
        return proxiedRemoteBody.getCertificateEncoded();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicy(securityContext);
    }

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getEntities();
    }

    // Implements Handlerizable

    /** Give a reference to a local map of handlers
     * @return A reference to a map of handlers
     */
    public HashMap getHandlersLevel() throws ProActiveException {
        try {
            HashMap map = proxiedRemoteBody.getHandlersLevel();
            return map;
        } catch (java.io.IOException e) {
            throw new ProActiveException(e.getMessage(), e);
        }
    }

    /** Set a new handler within the table of the Handlerizable Object
     * @param handler A class of handler associated with a class of non functional exception.
     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
     */
    public void setExceptionHandler(Class handler, Class exception)
        throws ProActiveException {
        try {
            proxiedRemoteBody.setExceptionHandler(handler, exception);
        } catch (java.io.IOException e) {
            System.out.println("ERROR : " + e.getMessage());
            throw new ProActiveException(e.getMessage(), e);
        }
    }

    /** Remove a handler from the table of the Handlerizable Object
     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
     * @return The removed handler or null
     */
    public Handler unsetExceptionHandler(Class exception)
        throws ProActiveException {
        try {
            Handler handler = proxiedRemoteBody.unsetExceptionHandler(exception);
            return handler;
        } catch (java.io.IOException e) {
            throw new ProActiveException(e.getMessage(), e);
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
