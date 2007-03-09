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
package org.objectweb.proactive.core.body;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
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
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.event.MessageEventListener;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.exceptions.manager.NFEListenerList;
import org.objectweb.proactive.core.gc.HalfBodies;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.InternalBodySecurity;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;


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
        super(new Object(), "LOCAL", factory, getRuntimeJobID());

        //SECURITY 
        if (securityManager == null) {
            securityManager = factory.getProActiveSecurityManager();
        }

        if (securityManager != null) {
            securityManager = securityManager.generateSiblingCertificate(
                    "HalfBody");
            securityManager.setBody(this);
            isSecurityOn = securityManager.getCertificate() != null;
            internalBodySecurity = new InternalBodySecurity(null); // SECURITY
            ProActiveLogger.getLogger(Loggers.SECURITY_MANAGER)
                           .debug("  ------> HalfBody Security is " +
                isSecurityOn);
        }

        this.replyReceiver = factory.newReplyReceiverFactory().newReplyReceiver();
        setLocalBodyImpl(new HalfLocalBodyStrategy(factory.newRequestFactory()));
        this.localBodyStrategy.getFuturePool().setOwnerBody(this.getID());

        // FAULT TOLERANCE
        String ftstate = ProActiveConfiguration.getFTState();
        if ("enable".equals(ftstate)) {
            try {
                // create the fault-tolerance manager
                int protocolSelector = FTManager.getProtoSelector(ProActiveConfiguration.getFTProtocol());
                this.ftmanager = factory.newFTManagerFactory()
                                        .newHalfFTManager(protocolSelector);
                this.ftmanager.init(this);
                if (bodyLogger.isDebugEnabled()) {
                    bodyLogger.debug("Init FTManager on " + this.getNodeURL());
                }
            } catch (ProActiveException e) {
                bodyLogger.error(
                    "**ERROR** Unable to init FTManager. Fault-tolerance is disabled " +
                    e);
                this.ftmanager = null;
            }
        } else {
            this.ftmanager = null;
        }
        this.gc = HalfBodies.getInstance();
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
    @Override
    protected int internalReceiveRequest(Request request)
        throws java.io.IOException {
        throw new ProActiveRuntimeException(
            "The method 'receiveRequest' is not implemented in class HalfBody.");
    }

    /**
     * Receives a reply in response to a former request.
     * @param reply the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     */
    @Override
    protected int internalReceiveReply(Reply reply) throws java.io.IOException {
        try {
            if (reply.isCiphered()) {
                reply.decrypt(securityManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return replyReceiver.receiveReply(reply, this, getFuturePool());
    }

    public void setImmediateService(String methodName) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public void setImmediateService(String methodName, Class[] parametersTypes) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    public void removeImmediateService(String methodName,
        Class[] parametersTypes) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    @Override
    public boolean isInImmediateService() {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
    }

    /**
     *  @see org.objectweb.proactive.Job#getJobID()
     */
    @Override
    public String getJobID() {
        return getRuntimeJobID();
    }

    private static String getRuntimeJobID() {
        return ProActiveRuntimeImpl.getProActiveRuntime().getJobID();
    }

    public void updateNodeURL(String newNodeURL) {
        throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
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
            if (methodCall.getComponentMetadata() != null) {
                request = new ComponentRequestImpl((RequestImpl) request);
            }
            if (future != null) {
                future.setID(sequenceID);
                futures.receiveFuture(future);
            }

            // FAULT TOLERANCE
            // System.out.println("a half body send a request: " + request.getMethodName());
            if (HalfBody.this.ftmanager != null) {
                HalfBody.this.ftmanager.sendRequest(request, destinationBody);
            } else {
                request.send(destinationBody);
            }
        }

        //
        // -- PROTECTED METHODS -----------------------------------------------
        //

        /**
         * Returns a unique identifier that can be used to tag a future, a request
         * @return a unique identifier that can be used to tag a future, a request.
         */
        public synchronized long getNextSequenceID() {
            return bodyID.toString().hashCode() + ++absoluteSequenceID;
        }
    }

    // end inner class LocalHalfBody
    // NFEProducer implementation
    private NFEListenerList nfeListeners = null;

    @Override
    public void addNFEListener(NFEListener listener) {
        if (nfeListeners == null) {
            nfeListeners = new NFEListenerList();
        }
        nfeListeners.addNFEListener(listener);
    }

    @Override
    public void removeNFEListener(NFEListener listener) {
        if (nfeListeners != null) {
            nfeListeners.removeNFEListener(listener);
        }
    }

    @Override
    public int fireNFE(NonFunctionalException e) {
        if (nfeListeners != null) {
            return nfeListeners.fireNFE(e);
        }
        return 0;
    }

    public long getNextSequenceID() {
        return localBodyStrategy.getNextSequenceID();
    }
}
