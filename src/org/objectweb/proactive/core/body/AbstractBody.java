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
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.group.MethodCallControlForGroup;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.ProActiveGroupManager;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.util.ThreadStore;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.CommunicationForbiddenException;
import org.objectweb.proactive.ext.security.DefaultProActiveSecurityManager;
import org.objectweb.proactive.ext.security.InternalBodySecurity;
import org.objectweb.proactive.ext.security.Policy;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurity;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.Secure;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class gives a common implementation of the Body interface. It provides all
 * the non specific behavior allowing sub-class to write the detail implementation.
 * </p><p>
 * Each body is identify by an unique identifier.
 * </p><p>
 * All active bodies that get created in one JVM register themselves into a table that allows
 * to tack them done. The registering and deregistering is done by the AbstractBody and
 * the table is managed here as well using some static methods.
 * </p><p>
 * In order to let somebody customize the body of an active object without subclassing it,
 * AbstractBody delegates lot of tasks to satellite objects that implements a given
 * interface. Abstract protected methods instantiate those objects allowing subclasses
 * to create them as they want (using customizable factories or instance).
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see Body
 * @see UniqueID
 *
 */
public abstract class AbstractBody extends AbstractUniversalBody implements Body,
    java.io.Serializable {
    //
    // -- STATIC MEMBERS -----------------------------------------------
    //
    private static final String TERMINATED_BODY_EXCEPTION_MESSAGE = "The body has been Terminated";

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected ThreadStore threadStore;

    // the current implementation of the local view of this body
    protected LocalBodyStrategy localBodyStrategy;

    // SECURITY
    protected ProActiveSecurityManager psm;
    protected boolean isSecurityOn = false;
    protected transient InternalBodySecurity internalBodySecurity;
    protected Hashtable openedSessions;
    protected static Logger logger = Logger.getLogger(AbstractBody.class.getName());
    protected boolean isInterfaceSecureImplemented = false;

    // GROUP
    protected ProActiveGroupManager pgm;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //

    /** whether the body has an activity done with a active thread */
    private transient boolean isActive;

    /** whether the body has been killed. A killed body has no more activity although
       stopping the activity thread is not immediate */
    private transient boolean isDead;

    /** table of handlers associated to the body */
    private HashMap bodyLevel;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody.
     * Used for serialization.
     */
    public AbstractBody() {
    }

    /**
     * Creates a new AbstractBody for an active object attached to a given node.
     * @param reifiedObject the active object that body is for
     * @param nodeURL the URL of the node that body is attached to
     * @param factory the factory able to construct new factories for each type of meta objects
     *                needed by this body
     */
    public AbstractBody(Object reifiedObject, String nodeURL,
        MetaObjectFactory factory, String jobId) {
        super(nodeURL, factory.newRemoteBodyFactory(), jobId);
        this.threadStore = factory.newThreadStoreFactory().newThreadStore();

        // GROUP
        this.pgm = factory.newProActiveGroupManagerFactory()
                          .newProActiveGroupManager();

        Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(myProvider);

        // SECURITY
        if (reifiedObject instanceof Secure) {
            isInterfaceSecureImplemented = true;
        }
        psm = new ProActiveSecurityManager();
        internalBodySecurity = new InternalBodySecurity(null); // SECURITY

        /*
           this.psm = factory.getProActiveSecurityManager();
             if (psm != null) {
                     //  startDefaultProActiveSecurityManager();
                     isSecurityOn = (psm != null);
                     logger.debug("Security is " + isSecurityOn);
                     psm.setBody(this);
                     internalBodySecurity = new InternalBodySecurity(null);
             }
         */
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    public String toString() {
        return "Body for " + localBodyStrategy.getName() + " node=" + nodeURL +
        " id=" + bodyID;
    }

    //
    // -- implements UniversalBody -----------------------------------------------
    //
    public void receiveRequest(Request request)
        throws java.io.IOException, RenegotiateSessionException {
        //System.out.println("  --> receiveRequest m="+request.getMethodName());
        try {
            this.enterInThreadStore();
            if (this.isDead) {
                throw new java.io.IOException(TERMINATED_BODY_EXCEPTION_MESSAGE);
            }
            if (this.isSecurityOn) {

                /*
                   if (isInterfaceSecureImplemented) {
                   Session session = psm.getSession(request.getSessionId());
                   ((Secure) getReifiedObject()).receiveRequest(session.getSecurityContext());
                   }
                 */
                try {
                    this.renegociateSessionIfNeeded(request.getSessionId());
                    if ((this.internalBodySecurity.isLocalBody()) &&
                            request.isCiphered()) {
                        request.decrypt(psm);
                    }
                } catch (SecurityNotAvailableException e) {
                    // do nothing
                }
            }
            this.registerIncomingFutures();
            this.internalReceiveRequest(request);
        } finally {
            this.exitFromThreadStore();
        }
    }

    public void receiveReply(Reply reply) throws java.io.IOException {
        //System.out.println("  --> receiveReply m="+reply.getMethodName());
       try {
    	//System.out.println("Body receives Reply on NODE : " + this.nodeURL); 
            enterInThreadStore();
            if (isDead) {
                throw new java.io.IOException(TERMINATED_BODY_EXCEPTION_MESSAGE);
            }
            if (isSecurityOn) {
                try {
                    if ((internalBodySecurity.isLocalBody()) &&
                            reply.isCiphered()) {
                        reply.decrypt(psm);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.registerIncomingFutures();
            internalReceiveReply(reply);
        } finally {
        	exitFromThreadStore();
        }
    }

    /**
     * This method effectively register futures (ie in the futurePool) that arrive in this active
     * object (by parameter or by result). Incoming futures have been registered in the static table
     * FuturePool.incomingFutures during their deserialization. This effective registration must be perform
     * AFTER entering in the ThreadStore.
     */
    private void registerIncomingFutures() {
        // get list of futures that should be deserialized and registred "behind the ThreadStore"
        java.util.ArrayList incomingFutures = FuturePool.getIncomingFutures();

        if (incomingFutures != null) {
            // if futurePool is not null, we are in an Active Body
            if (getFuturePool() != null) {
                // some futures have to be registred in the local futurePool
                java.util.Iterator it = incomingFutures.iterator();
                while (it.hasNext()) {
                    Future current = (Future) (it.next());
                    getFuturePool().receiveFuture(current);
                }
                FuturePool.removeIncomingFutures();
            } else {
                // we are in a forwarder
                // some futures have to set their continuation tag
                java.util.Iterator it = incomingFutures.iterator();
                while (it.hasNext()) {
                    FutureProxy current = (FutureProxy) (it.next());
                    current.setContinuationTag();
                }
                FuturePool.removeIncomingFutures();
            }
        }
    }

    public void enableAC() {
        localBodyStrategy.getFuturePool().enableAC();
    }

    public void disableAC() {
        localBodyStrategy.getFuturePool().disableAC();
    }

    public void renegociateSessionIfNeeded(long sID)
        throws IOException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (!internalBodySecurity.isLocalBody() &&
                    (openedSessions != null)) {
                // inside a forwarder
                Long sessionID;

                //long sID = request.getSessionId();
                if (sID != 0) {
                    sessionID = new Long(sID);
                    if (openedSessions.containsKey(sessionID)) {
                        openedSessions.remove(sessionID);
                        internalBodySecurity.terminateSession(sID);
                        //System.out.println("Object has migrated : Renegotiate Session");
                        throw new RenegotiateSessionException(internalBodySecurity.getDistantBody());
                    }
                }
            }
        } finally {
            exitFromThreadStore();
        }
    }

    public String getVNName()
        throws java.io.IOException, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                if (internalBodySecurity.isLocalBody()) {
                    return psm.getVNName();
                } else {
                    return internalBodySecurity.getVNName();
                }
            }
        } finally {
            exitFromThreadStore();
        }
        return null;
    }

    public void initiateSession(int type, UniversalBody body)
        throws java.io.IOException, CommunicationForbiddenException, 
            org.objectweb.proactive.ext.security.crypto.AuthenticationException, 
            RenegotiateSessionException, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                if (internalBodySecurity.isLocalBody()) {
                    psm.initiateSession(type, body);
                } else {
                    internalBodySecurity.initiateSession(type, body);
                }
            }
        } finally {
            exitFromThreadStore();
        }
        throw new SecurityNotAvailableException();
    }

    public void terminateSession(long sessionID)
        throws java.io.IOException, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                if (internalBodySecurity.isLocalBody()) {
                    psm.terminateSession(sessionID);
                } else {
                    internalBodySecurity.terminateSession(sessionID);
                }
            }
            throw new SecurityNotAvailableException();
        } finally {
            exitFromThreadStore();
        }
    }

    public X509Certificate getCertificate()
        throws java.io.IOException, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                if (internalBodySecurity.isLocalBody()) {
                    //	System.out.println(" getCertificate on demande un security manager a " + ProActive.getBodyOnThis());
                    //  if (psm == null) {
                    //  startDefaultProActiveSecurityManager();
                    //}
                    return psm.getCertificate();
                } else {
                    return internalBodySecurity.getCertificate();
                }
            }
            throw new SecurityNotAvailableException();
        } finally {
            exitFromThreadStore();
        }
    }

    public ProActiveSecurityManager getProActiveSecurityManager()
        throws java.io.IOException, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                if (internalBodySecurity.isLocalBody()) {
                    //	System.out.println("getProActiveSecurityManager on demande un security manager a " + ProActive.getBodyOnThis());
                    // if (psm == null) {
                    //    startDefaultProActiveSecurityManager();
                    // }
                    return psm;
                } else {
                    ProActiveSecurityManager plop = internalBodySecurity.getProActiveSecurityManager();
                    return plop;
                }
            } else {
                throw new SecurityNotAvailableException();
            }
        } finally {
            exitFromThreadStore();
        }
    }

    public Policy getPolicyFrom(X509Certificate certificate)
        throws java.io.IOException, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                Policy pol;

                if (internalBodySecurity.isLocalBody()) {
                    // if (psm == null) {
                    //  startDefaultProActiveSecurityManager();
                    // }
                    pol = psm.getPolicyTo(certificate);

                    return pol;
                } else {
                    pol = internalBodySecurity.getPolicyFrom(certificate);

                    return pol;
                }
            }
            throw new SecurityNotAvailableException();
        } finally {
            exitFromThreadStore();
        }
    }

    public long startNewSession(Communication policy)
        throws java.io.IOException, RenegotiateSessionException, 
            SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                long sessionID;

                if (internalBodySecurity.isLocalBody()) {
                    //System.out.println("startNewSession on demande un security manager a " + ProActive.getBodyOnThis());
                    //if (psm == null) {
                    //startDefaultProActiveSecurityManager();
                    //}
                    sessionID = psm.startNewSession(policy);

                    return sessionID;
                } else {
                    sessionID = internalBodySecurity.startNewSession(policy);

                    return sessionID;
                }
            }
            throw new SecurityNotAvailableException();
        } finally {
            exitFromThreadStore();
        }
    }

    public ConfidentialityTicket negociateKeyReceiverSide(
        ConfidentialityTicket confidentialityTicket, long sessionID)
        throws java.io.IOException, KeyExchangeException, 
            SecurityNotAvailableException {
        try {
            enterInThreadStore();
            ConfidentialityTicket tick;

            if (internalBodySecurity.isLocalBody()) {
                //System.out.println("negociateKeyReceiverSide on demande un security manager a " + ProActive.getBodyOnThis());
                //if (psm == null) {
                //   startDefaultProActiveSecurityManager();       
                // }
                tick = psm.keyNegociationReceiverSide(confidentialityTicket,
                        sessionID);

                return tick;
            } else {
                tick = internalBodySecurity.negociateKeyReceiverSide(confidentialityTicket,
                        sessionID);

                return tick;
            }
        } finally {
            exitFromThreadStore();
        }
    }

    public PublicKey getPublicKey()
        throws java.io.IOException, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                PublicKey pk;

                if (internalBodySecurity.isLocalBody()) {
                    //	System.out.println("getPublicKey on demande un security manager a " + ProActive.getBodyOnThis());
                    //if (psm == null) {
                    //         startDefaultProActiveSecurityManager();
                    //        }
                    pk = psm.getPublicKey();

                    return pk;
                } else {
                    pk = internalBodySecurity.getPublicKey();

                    return pk;
                }
            }
            throw new SecurityNotAvailableException();
        } finally {
            exitFromThreadStore();
        }
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws java.io.IOException, SecurityNotAvailableException, Exception {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                byte[] plop;

                if (internalBodySecurity.isLocalBody()) {
                    //	System.out.println("randomValue on demande un security manager a " + ProActive.getBodyOnThis());
                    plop = psm.randomValue(sessionID, cl_rand);

                    return plop;
                } else {
                    plop = internalBodySecurity.randomValue(sessionID, cl_rand);

                    return plop;
                }
            }
            throw new SecurityNotAvailableException();
        } finally {
            exitFromThreadStore();
        }
    }

    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] my_pub, byte[] my_cert,
        byte[] sig_code)
        throws java.io.IOException, SecurityNotAvailableException, Exception {
        try {
            enterInThreadStore();
            if (isSecurityOn) {
                renegociateSessionIfNeeded(sessionID);

                byte[][] pke;

                if (internalBodySecurity.isLocalBody()) {
                    pke = psm.publicKeyExchange(sessionID, distantBody, my_pub,
                            my_cert, sig_code);

                    return pke;
                } else {
                    pke = internalBodySecurity.publicKeyExchange(sessionID,
                            distantBody, my_pub, my_cert, sig_code);

                    return pke;
                }
            }
            throw new SecurityNotAvailableException();
        } finally {
            exitFromThreadStore();
        }
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] tmp, byte[] tmp1,
        byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws java.io.IOException, Exception, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (!isSecurityOn) {
                throw new SecurityNotAvailableException();
            }

            renegociateSessionIfNeeded(sessionID);

            byte[][] ske;

            renegociateSessionIfNeeded(sessionID);
            if (internalBodySecurity.isLocalBody()) {
                //	System.out.println("secretKeyExchange demande un security manager a " + ProActive.getBodyOnThis());
                ske = psm.secretKeyExchange(sessionID, tmp, tmp1, tmp2, tmp3,
                        tmp4);

                return ske;
            } else {
                ske = internalBodySecurity.secretKeyExchange(sessionID, tmp,
                        tmp1, tmp2, tmp3, tmp4);

                return ske;
            }
        } finally {
            threadStore.exit();
        }
    }

    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException, java.io.IOException {
        try {
            enterInThreadStore();
            if (!isSecurityOn) {
                throw new SecurityNotAvailableException();
            }

            if (internalBodySecurity.isLocalBody()) {
                return psm.getPolicyTo(type, from, to);
            } else {
                return internalBodySecurity.getPolicyTo(type, from, to);
            }
        } finally {
            exitFromThreadStore();
        }
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws java.io.IOException, SecurityNotAvailableException {
        try {
            enterInThreadStore();
            if (!isSecurityOn) {
                throw new SecurityNotAvailableException();
            }

            if (internalBodySecurity.isLocalBody()) {
                return psm.getPolicy(securityContext);
            } else {
                return internalBodySecurity.getPolicy(securityContext);
            }
        } finally {
            exitFromThreadStore();
        }
    }

    public byte[] getCertificateEncoded()
        throws IOException, SecurityNotAvailableException {
        try {
            enterInThreadStore();

            //if (psm == null) {
            //	  startDefaultProActiveSecurityManager();
            // }
            if (!isSecurityOn || (psm == null)) {
                throw new SecurityNotAvailableException();
            }

            if (internalBodySecurity.isLocalBody()) {
                return psm.getCertificate().getEncoded();
            } else {
                return internalBodySecurity.getCertificatEncoded();
            }
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } finally {
            exitFromThreadStore();
        }
        return null;
    }

    protected void startDefaultProActiveSecurityManager() {
        try {
            //     logger.info("starting a new psm ");
            this.psm = new DefaultProActiveSecurityManager("vide ");
            isSecurityOn = true;
            psm.setBody(this);
            internalBodySecurity = new InternalBodySecurity(null);
        } catch (Exception e) {
            System.out.println(
                "Error when contructing a DefaultProActiveManager");
            e.printStackTrace();
        }
    }

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        try {
            enterInThreadStore();
            if (!isSecurityOn) {
                throw new SecurityNotAvailableException();
            }

            if (internalBodySecurity.isLocalBody()) {
                return psm.getEntities();
            } else {
                return internalBodySecurity.getEntities();
            }
        } finally {
            exitFromThreadStore();
        }
    }

    //
    // -- implements Body -----------------------------------------------
    //
    public void terminate() {
        if (isDead) {
            return;
        }
        isDead = true;
        activityStopped();
        // unblock is thread was block
        acceptCommunication();
    }

    public void blockCommunication() {
        threadStore.close();
    }

    public void acceptCommunication() {
        threadStore.open();
    }

    public void enterInThreadStore() {
        threadStore.enter();
    }

    public void exitFromThreadStore() {
        threadStore.exit();
    }

    public boolean isAlive() {
        return !isDead;
    }

    public boolean isActive() {
        return isActive;
    }

    public UniversalBody checkNewLocation(UniqueID bodyID) {
        //we look in the location table of the current JVM
        Body body = LocalBodyStore.getInstance().getLocalBody(bodyID);
        if (body != null) {
            // we update our table to say that this body is local
            location.updateBody(bodyID, body);
            return body;
        } else {
            //it was not found in this vm let's try the location table
            return location.getBody(bodyID);
        }
    }

    public void setPolicyServer(PolicyServer server) {
        if (server != null) {
            if ((psm != null) && (psm.getPolicyServer() == null)) {
                psm = new ProActiveSecurityManager(server);
                isSecurityOn = true;
                System.out.println("Security is on " + isSecurityOn);
                psm.setBody(this);
            }
        }
    }

    //
    // -- implements LocalBody -----------------------------------------------
    //
    public FuturePool getFuturePool() {
        return localBodyStrategy.getFuturePool();
    }

    public BlockingRequestQueue getRequestQueue() {
        return localBodyStrategy.getRequestQueue();
    }

    public Object getReifiedObject() {
        return localBodyStrategy.getReifiedObject();
    }

    public String getName() {
        return localBodyStrategy.getName();
    }

    /** Serves the request. The request should be removed from the request queue
     * before serving, which is correctly done by all methods of the Service class.
     * However, this condition is not ensured for custom calls on serve. */
    public void serve(Request request) {
        localBodyStrategy.serve(request);
    }

    public void sendRequest(MethodCall methodCall, Future future,
        UniversalBody destinationBody)
        throws java.io.IOException, RenegotiateSessionException {
        long sessionID = 0;

        //	logger.debug("send Request Body" + destinationBody);
        //logger.debug(" bla" + destinationBody.getRemoteAdapter());
   //     try {
//            try {
//                if (!isSecurityOn) {
//                    logger.debug("security is off");
//                    throw new SecurityNotAvailableException();
//                }
//                if (internalBodySecurity.isLocalBody()) {
//                    byte[] certE = destinationBody.getRemoteAdapter()
//                                                  .getCertificateEncoded();
//                    X509Certificate cert = ProActiveSecurity.decodeCertificate(certE);
//                    System.out.println("send Request AbstractBody, method " +
//                        methodCall.getName() + " cert " +
//                        cert.getSubjectDN().getName());
//                    if ((sessionID = psm.getSessionIDTo(cert)) == 0) {
//                        psm.initiateSession(SecurityContext.COMMUNICATION_SEND_REQUEST_TO,
//                            destinationBody.getRemoteAdapter());
//                        sessionID = psm.getSessionIDTo(cert);
//                    }
//                }
//            } catch (SecurityNotAvailableException e) {
//                // do nothing 
//                logger.debug("communication without security");
//                //e.printStackTrace();
//            }
            localBodyStrategy.sendRequest(methodCall, future, destinationBody);
//        } catch (RenegotiateSessionException e) {
//            if (e.getUniversalBody() != null) {
//                updateLocation(destinationBody.getID(), e.getUniversalBody());
//            }
//            psm.terminateSession(sessionID);
//            sendRequest(methodCall, future, e.getUniversalBody());
//        } catch (CommunicationForbiddenException e) {
//            logger.warn(e);
//            //e.printStackTrace();
//        } catch (AuthenticationException e) {
//            e.printStackTrace();
//        }
    }

	/**
	 * Get information about the handlerizable object
	 * @return information about the handlerizable object
	 */
	public String getHandlerizableInfo() throws java.io.IOException {
		return "BODY (URL=" + this.nodeURL + ") of CLASS ["+ this.getClass()  +"]";
	}
	
    /** Give a reference to a local map of handlers
               * @return A reference to a map of handlers
               */
    public HashMap getHandlersLevel() throws java.io.IOException {
        return bodyLevel;
    }

	/** 
	 * Clear the local map of handlers
	 */
	public void clearHandlersLevel() throws java.io.IOException {
		 bodyLevel.clear();
	}

    /** Set a new handler within the table of the Handlerizable Object
     * @param handler A handler associated with a class of non functional exception.
     * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
     */
    public void setExceptionHandler(Handler handler, Class exception)
        throws java.io.IOException {
        // add handler to body level
        if (bodyLevel == null) {
            bodyLevel = new HashMap();
        }
        bodyLevel.put(exception, handler);
    }

    /** Remove a handler from the table of the Handlerizable Object
             * @param exception A class of non functional exception. It is a subclass of <code>NonFunctionalException</code>.
             * @return The removed handler or null
             */
    public Handler unsetExceptionHandler(Class exception)
        throws java.io.IOException {
        // remove handler from body level
        if (bodyLevel != null) {
            Handler handler = (Handler) bodyLevel.remove(exception);
            return handler;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("[NFE_WARNING] No handler for [" +
                    exception.getName() + "] can be removed from BODY level");
            }
            return null;
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Receives a request for later processing. The call to this method is non blocking
     * unless the body cannot temporary receive the request.
     * @param request the request to process
     * @exception java.io.IOException if the request cannot be accepted
     */
    protected abstract void internalReceiveRequest(Request request)
        throws java.io.IOException, RenegotiateSessionException;

    /**
     * Receives a reply in response to a former request.
     * @param reply the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     */
    protected abstract void internalReceiveReply(Reply reply)
        throws java.io.IOException;   

    protected void setLocalBodyImpl(LocalBodyStrategy localBody) {
        localBodyStrategy = localBody;
    }

    /**
     * Signals that the activity of this body, managed by the active thread has just stopped.
     */
    protected void activityStopped() {
        if (!isActive) {
            return;
        }
        isActive = false;
        //We are no longer an active body
        LocalBodyStore.getInstance().unregisterBody(this);
    }

    //protected void activityStopped2(){
    //	LocalBodyStore.getInstance().unregisterBody(this);
    //}

    /**
     * Signals that the activity of this body, managed by the active thread has just started.
     */
    protected void activityStarted() {
        if (isActive) {
            return;
        }
        isActive = true;
        // we associated this body to the thread running it
        LocalBodyStore.getInstance().setCurrentThreadBody(this);
        // we register in this JVM
        LocalBodyStore.getInstance().registerBody(this);
    }

    /**
     * Set the SPMD group for the active object
     * @param o - the new SPMD group
     */
    public void setSPMDGroup(Object o) {
        this.pgm.setSPMDGroup(o);
    }

    /**
     * Returns the SPMD group of the active object
     * @return the SPMD group of the active object
     */
    public Object getSPMDGroup() {
        return this.pgm.getSPMDGroup();
    }

    /**
     * Returns the size of of the SPMD group
     * @return the size of of the SPMD group
     */
    public int getSPMDGroupSize() {
        return ProActiveGroup.size(this.getSPMDGroup());
    }

    /**
     * Send a call to all member of the SPMD group
     * @param gmc the control method call for group
     */
    public void sendSPMDGroupCall(MethodCallControlForGroup gmc) {
        try {
            ((ProxyForGroup) ProActiveGroup.getGroup(this.pgm.getSPMDGroup())).reify(gmc);
        } catch (InvocationTargetException e) {
            System.err.println(
                "Unable to invoke a method call to control groups");
            e.printStackTrace();
        }
    }

    //
    // -- SERIALIZATION METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        logger = Logger.getLogger("AbstractBody");
    }
}
