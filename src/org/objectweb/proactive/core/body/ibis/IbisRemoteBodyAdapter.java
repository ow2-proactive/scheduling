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
package org.objectweb.proactive.core.body.ibis;

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.BodyAdapter;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.handler.Handler;
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

/**
 *   An adapter for a IbisRemoteBody to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to another remote objects library.
 */
import java.io.IOException;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.HashMap;


public class IbisRemoteBodyAdapter implements BodyAdapter, java.io.Serializable {
    protected static Logger logger = Logger.getLogger(IbisRemoteBodyAdapter.class.getName());

    /**
     * The encapsulated IbisRemoteBody
     */
    protected IbisRemoteBody proxiedRemoteBody;

    /**
     * Cache the ID of the Body locally for speed
     */
    protected UniqueID bodyID;

    /**
     * Cache the jobID locally for speed
     */
    protected String jobID;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public IbisRemoteBodyAdapter() {
    }

    public IbisRemoteBodyAdapter(IbisRemoteBody remoteBody)
        throws ProActiveException {
        this.proxiedRemoteBody = remoteBody;
        if (logger.isDebugEnabled()) {
            logger.debug(" remote body = " + proxiedRemoteBody.getClass());
        }
        try {
            this.bodyID = remoteBody.getID();
        } catch (ibis.rmi.RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    public IbisRemoteBodyAdapter(UniversalBody body) throws ProActiveException {
        try {
            this.proxiedRemoteBody = new IbisRemoteBodyImpl(body);
            if (logger.isDebugEnabled()) {
                logger.debug("proxiedRemoteBody = " +
                    proxiedRemoteBody.getClass());
            }
        } catch (ibis.rmi.RemoteException e) {
            throw new ProActiveException(e);
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
     * @param bodyAdapter the bodyadapter of the active object to register.
     * @param url the url under which the remote body is registered.
     * @exception java.io.IOException if the remote body cannot be registered
     */
    public static void register(IbisRemoteBodyAdapter bodyAdapter, String url)
        throws java.io.IOException {
        ibis.rmi.Naming.rebind(url, bodyAdapter.proxiedRemoteBody);
    }

    /**
     * Unregisters an active object previously registered into a RMI registry.
     * @param url the url under which the active object is registered.
     * @exception java.io.IOException if the remote object cannot be removed from the registry
     */
    public static void unregister(String url) throws java.io.IOException {
        try {
            ibis.rmi.Naming.unbind(url);
        } catch (ibis.rmi.NotBoundException e) {
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
     *      or if the object found is not of type IbisRemoteBody
     */
    public static UniversalBody lookup(String url) throws java.io.IOException {
        Object o = null;

        // Try if URL is the address of a IbisRemoteBody
        try {
            o = ibis.rmi.Naming.lookup(UrlBuilder.removeProtocol(url,"ibis:"));
        } catch (ibis.rmi.NotBoundException e) {
            throw new java.io.IOException("The url " + url +
                " is not bound to any known object");
        }
        if (o instanceof IbisRemoteBody) {
            try {
                return new IbisRemoteBodyAdapter((IbisRemoteBody) o);
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
        if (!(o instanceof IbisRemoteBodyAdapter)) {
            return false;
        }
        IbisRemoteBodyAdapter rba = (IbisRemoteBodyAdapter) o;
        return proxiedRemoteBody.equals(rba.proxiedRemoteBody);
    }

    public int hashCode() {
        return proxiedRemoteBody.hashCode();
    }

    //
    // -- implements UniversalBody -----------------------------------------------
    //
    public String getJobID() {
        if (jobID == null) {
            try {
                jobID = proxiedRemoteBody.getJobID();
            } catch (ibis.rmi.RemoteException e) {
                e.printStackTrace();
                return "";
            }
        }

        return jobID;
    }

    public int receiveRequest(Request r)
        throws java.io.IOException, RenegotiateSessionException {
        return proxiedRemoteBody.receiveRequest(r);
    }

    public int receiveReply(Reply r) throws java.io.IOException {
        return proxiedRemoteBody.receiveReply(r);
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#terminate()
     */
    public void terminate() throws IOException {
        proxiedRemoteBody.terminate();
    }

    public String getNodeURL() {
        try {
            return proxiedRemoteBody.getNodeURL();
        } catch (ibis.rmi.RemoteException e) {
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

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#initiateSession(org.objectweb.proactive.core.body.UniversalBody)
     */
    public void initiateSession(int type, UniversalBody body)
        throws IOException, CommunicationForbiddenException, 
            AuthenticationException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        proxiedRemoteBody.initiateSession(type, body);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        proxiedRemoteBody.terminateSession(sessionID);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificate()
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getCertificate();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getProActiveSecurityManager()
     */
    public ProActiveSecurityManager getProActiveSecurityManager()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getProActiveSecurityManager();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyFrom(java.security.cert.X509Certificate)
     */
    public Policy getPolicyFrom(X509Certificate certificate)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicyFrom(certificate);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#startNewSession(org.objectweb.proactive.ext.security.Policy)
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        return proxiedRemoteBody.startNewSession(policy);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#negociateKeyReceiverSide(org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket, long)
     */
    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws SecurityNotAvailableException, KeyExchangeException, IOException {
        return proxiedRemoteBody.negociateKeyReceiverSide(confidentialityTicket,
            sessionID);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPublicKey()
     */
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPublicKey();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, Exception {
        return proxiedRemoteBody.randomValue(sessionID, cl_rand);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#publicKeyExchange(long, org.objectweb.proactive.core.body.UniversalBody, byte[], byte[], byte[])
     */
    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code)
        throws SecurityNotAvailableException, Exception, 
            RenegotiateSessionException {
        return proxiedRemoteBody.publicKeyExchange(sessionID, distantBody,
            my_pub, my_cert, sig_code);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, Exception, 
            RenegotiateSessionException {
        return proxiedRemoteBody.secretKeyExchange(sessionID, tmp, tmp1, tmp2,
            tmp3, tmp4);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicyTo(java.lang.String, java.lang.String, java.lang.String)
     */
    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicyTo(type, from, to);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getPolicy(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getPolicy(securityContext);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getVNName()
     */
    public String getVNName() throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getVNName();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getCertificateEncoded();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.body.UniversalBody#getEntities()
     */
    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return proxiedRemoteBody.getEntities();
    }

    /**
     * Get information about the handlerizable object
     * @return information about the handlerizable object
     */
    public String getHandlerizableInfo() throws java.io.IOException {
        return "BODY of CLASS [" + this.getClass() + "]";
    }

    /** Give a reference to a local map of handlers
     * @return A reference to a map of handlers
     */
    public HashMap getHandlersLevel() throws java.io.IOException {
        return null;
    }

    /**
     * Clear the local map of handlers
     */
    public void clearHandlersLevel() throws java.io.IOException {
    }

    /** Set a new handler within the table of the Handlerizable Object
     * @param handler A handler associated with a class of non functional exception.
     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
     */
    public void setExceptionHandler(Handler handler, Class exception)
        throws java.io.IOException {
    }

    /** Remove a handler from the table of the Handlerizable Object
     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
     * @return The removed handler or null
     */
    public Handler unsetExceptionHandler(Class exception)
        throws java.io.IOException {
        return null;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveFTMessage(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public int receiveFTMessage(FTMessage ev) throws IOException {
        return this.proxiedRemoteBody.receiveFTMessage(ev);
    }

    //
    // Implements Adapter
    //
    public void changeProxiedBody(Body newBody) {
    	try {
        this.proxiedRemoteBody.changeProxiedBody(newBody);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
