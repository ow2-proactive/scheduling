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

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.RequestQueue;
import org.objectweb.proactive.core.component.request.ComponentRequestImpl;
import org.objectweb.proactive.core.event.MessageEventListener;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.ext.security.CommunicationForbiddenException;
import org.objectweb.proactive.ext.security.InternalBodySecurity;
import org.objectweb.proactive.ext.security.ProActiveSecurity;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

import java.security.cert.X509Certificate;


public class HalfBody extends AbstractBody {
    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    private static final String HALF_BODY_EXCEPTION_MESSAGE = "This method is not implemented in class HalfBody.";
    private static final String NAME = "Other thread";

    /** The component in charge of receiving reply */
    private ReplyReceiver replyReceiver;

    public synchronized static HalfBody getHalfBody(MetaObjectFactory factory) {
        return new HalfBody(factory);
    }

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    private HalfBody(MetaObjectFactory factory) {
        //SECURITY 
        super(new Object(), "LOCAL", factory, getRuntimeJobID());
        //super(new Object(),
        //	 NodeFactory.getDefaultNode().getNodeInformation().getURL(), factory);
        // creating a default psm for HalfBody
        // TODO get application certificate instead of generated one
        //Object[] o = ProActiveSecurity.generateGenericCertificate();
        //psm = new ProActiveSecurityManager((X509Certificate) o[0], (PrivateKey) o[1], null);
        //psm = new ProActiveSecurityManager();
        //isSecurityOn = true;
        //psm.setBody(this);
        
       
   	 this.psm = factory.getProActiveSecurityManager();
   	   if (psm != null) {
   		   //  startDefaultProActiveSecurityManager();
   		   isSecurityOn = (psm != null);
   		   logger.debug("HalfBody Security is " + isSecurityOn);
   		   psm.setBody(this);
   		   internalBodySecurity = new InternalBodySecurity(null);  

   	   }
   	   
        
       // internalBodySecurity = new InternalBodySecurity(null);

        this.replyReceiver = factory.newReplyReceiverFactory().newReplyReceiver();
        setLocalBodyImpl(new HalfLocalBodyStrategy(factory.newRequestFactory()));
        this.localBodyStrategy.getFuturePool().setOwnerBody(this.getID());
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements MessageEventProducer -----------------------------------------------
    //
    public void addMessageEventListener(MessageEventListener listener) {
    }

    public void removeMessageEventListener(MessageEventListener listener) {
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
    protected void internalReceiveRequest(Request request)
        throws java.io.IOException {
        throw new ProActiveRuntimeException(
            "The method 'receiveRequest' is not implemented in class HalfBody.");
    }

    /**
     * Receives a reply in response to a former request.
     * @param reply the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     */
    protected void internalReceiveReply(Reply reply) throws java.io.IOException {
    	//System.out.print("Half-Body receives Reply -> ");
        try {
            if (reply.isCiphered()) {
                reply.decrypt(psm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*if (reply.getResult() != null) {
        	System.out.println("Result contains in Reply is : " + reply.getResult().getClass());
        } else {
        	System.out.println("Reply is : " + reply);
        }*/
        replyReceiver.receiveReply(reply, this, getFuturePool());
    }

    public void setImmediateService(String methodName) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

     /**
     *  @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return getRuntimeJobID();
    }

    private static String getRuntimeJobID() {
    	return ProActiveRuntimeImpl.getProActiveRuntime().getJobID();
    }
    
    //
    // -- inner classes -----------------------------------------------
    //
    private class HalfLocalBodyStrategy implements LocalBodyStrategy,
        java.io.Serializable {

        /** A pool future that contains the pending future objects */
        protected FuturePool futures;
        protected RequestFactory internalRequestFactory;
        private long absoluteSequenceID;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public HalfLocalBodyStrategy(RequestFactory requestFactory) {
            this.futures = new FuturePool();
            this.internalRequestFactory = requestFactory;
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //
        //
        // -- implements LocalBody -----------------------------------------------
        //
        public FuturePool getFuturePool() {
            return futures;
        }

        public BlockingRequestQueue getRequestQueue() {
            throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
        }

        public RequestQueue getHighPriorityRequestQueue() {
            throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
        }

        public Object getReifiedObject() {
            throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
        }

        public String getName() {
            return NAME;
        }

        public void serve(Request request) {
            throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
        }

        public void sendRequest(MethodCall methodCall, Future future,
            UniversalBody destinationBody)
            throws java.io.IOException, RenegotiateSessionException {
            long sequenceID = getNextSequenceID();
            Request request = internalRequestFactory.newRequest(methodCall,
                    HalfBody.this, future == null, sequenceID);

            // COMPONENTS : generate ComponentRequest for component messages
            if (methodCall.getTag() != null) {
                if (methodCall.getTag().equals(MethodCall.COMPONENT_TAG)) {
                    request = new ComponentRequestImpl((RequestImpl) request);
                }
            }
            if (future != null) {
                future.setID(sequenceID);
                futures.receiveFuture(future);
            }

            // SECURITY 
            long sessionID = 0;

            //	logger.debug("send Request Body" + destinationBody);
            //   logger.debug(" halfbla" + destinationBody.getRemoteAdapter());
            try {
                try {
                    if (!isSecurityOn) {
                        logger.debug("security is off");
                        throw new SecurityNotAvailableException();
                    }
                    if (internalBodySecurity.isLocalBody()) {
                        byte[] certE = destinationBody.getRemoteAdapter()
                                                      .getCertificateEncoded();
                        X509Certificate cert = ProActiveSecurity.decodeCertificate(certE);
                        if ((sessionID = psm.getSessionIDTo(cert)) == 0) {
                            psm.initiateSession(SecurityContext.COMMUNICATION_SEND_REPLY_TO,
                                destinationBody.getRemoteAdapter());
                            sessionID = psm.getSessionIDTo(cert);
                        }
                    }
                } catch (SecurityNotAvailableException e) {
                    // do nothing 
                    logger.debug("communication without security");
                    //e.printStackTrace();
                }
                request.send(destinationBody);
            } catch (RenegotiateSessionException e) {
                //e.printStackTrace();
                updateLocation(destinationBody.getID(), e.getUniversalBody());
                psm.terminateSession(sessionID);
                logger.debug("renegotiate session");
                sendRequest(methodCall, future, e.getUniversalBody());
            } catch (CommunicationForbiddenException e) {
                logger.warn(e);
                //e.printStackTrace();
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        }

        //
        // -- PROTECTED METHODS -----------------------------------------------
        //

        /**
         * Returns a unique identifier that can be used to tag a future, a request
         * @return a unique identifier that can be used to tag a future, a request.
         */
        private synchronized long getNextSequenceID() {
            return ++absoluteSequenceID;
        }
    }

    // end inner class LocalHalfBody
}
