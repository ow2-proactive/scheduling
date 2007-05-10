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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.message.MessageEventProducerImpl;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.RequestQueue;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.component.request.ComponentRequestImpl;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.event.MessageEvent;
import org.objectweb.proactive.core.event.MessageEventListener;
import org.objectweb.proactive.core.exceptions.body.BodyNonFunctionalException;
import org.objectweb.proactive.core.exceptions.body.SendReplyCommunicationException;
import org.objectweb.proactive.core.exceptions.body.ServiceFailedCalleeNFE;
import org.objectweb.proactive.core.exceptions.manager.NFEManager;
import org.objectweb.proactive.core.exceptions.proxy.ProxyNonFunctionalException;
import org.objectweb.proactive.core.exceptions.proxy.ServiceFailedCallerNFE;
import org.objectweb.proactive.core.gc.GarbageCollector;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.profiling.Profiling;
import org.objectweb.proactive.core.util.profiling.TimerWarehouse;


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
 * @see org.objectweb.proactive.Body
 * @see UniqueID
 *
 */
public abstract class BodyImpl extends AbstractBody implements java.io.Serializable,
    BodyImplMBean {
    //  
    // -- STATIC MEMBERS -----------------------------------------------
    //
    private static final String INACTIVE_BODY_EXCEPTION_MESSAGE = "Cannot perform this call because this body is inactive";

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /** The component in charge of receiving reply */
    protected ReplyReceiver replyReceiver;

    /** The component in charge of receiving request */
    protected RequestReceiver requestReceiver;
    protected MessageEventProducerImpl messageEventProducer;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody.
     * Used for serialization.
     */
    public BodyImpl() {
    }

    /**
     * Creates a new AbstractBody for an active object attached to a given node.
     * @param reifiedObject the active object that body is for
     * @param nodeURL the URL of the node that body is attached to
     * @param factory the factory able to construct new factories for each type of meta objects
     *                needed by this body
     */
    public BodyImpl(Object reifiedObject, String nodeURL,
        MetaObjectFactory factory, String jobId) {
        super(reifiedObject, nodeURL, factory, jobId);
        this.requestReceiver = factory.newRequestReceiverFactory()
                                      .newRequestReceiver();
        this.replyReceiver = factory.newReplyReceiverFactory().newReplyReceiver();
        this.messageEventProducer = new MessageEventProducerImpl();
        setLocalBodyImpl(new ActiveLocalBodyStrategy(reifiedObject,
                factory.newRequestQueueFactory().newRequestQueue(bodyID),
                factory.newRequestFactory()));
        this.localBodyStrategy.getFuturePool().setOwnerBody(this.getID());

        // FAULT TOLERANCE
        String ftstate = ProActiveConfiguration.getInstance().getFTState();
        if ("enable".equals(ftstate)) {
            // if the object is a ProActive internal object, FT is disabled
            if (!(this.localBodyStrategy.getReifiedObject() instanceof ProActiveInternalObject)) {
                // if the object is not serilizable, FT is disabled
                if (this.localBodyStrategy.getReifiedObject() instanceof Serializable) {
                    try {
                        // create the fault tolerance manager
                        int protocolSelector = FTManager.getProtoSelector(ProActiveConfiguration.getInstance()
                                                                                                .getFTProtocol());
                        this.ftmanager = factory.newFTManagerFactory()
                                                .newFTManager(protocolSelector);
                        this.ftmanager.init(this);
                        if (bodyLogger.isDebugEnabled()) {
                            bodyLogger.debug("Init FTManager on " +
                                this.getNodeURL());
                        }
                    } catch (ProActiveException e) {
                        bodyLogger.error(
                            "**ERROR** Unable to init FTManager. Fault-tolerance is disabled " +
                            e);
                        this.ftmanager = null;
                    }
                } else {
                    // target body is not serilizable
                    bodyLogger.error(
                        "**ERROR** Activated object is not serializable. Fault-tolerance is disabled");
                    this.ftmanager = null;
                }
            }
        } else {
            this.ftmanager = null;
        }
        this.gc = new GarbageCollector(this);

        // JMX registration 
        //        try {
        //            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        //            ObjectName name = new ObjectName(
        //                    "org.objectweb.proactive:type=oa,class=" + this.getName() +
        //                    ",name=" + this.getName() + "-" +
        //                    this.getID().toString().replace(':', '-'));
        //            mbs.registerMBean(this, name);
        //        } catch (MalformedObjectNameException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        } catch (NullPointerException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        } catch (InstanceAlreadyExistsException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        } catch (MBeanRegistrationException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        } catch (NotCompliantMBeanException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }

        // End JMX registration      
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements MessageEventProducer -----------------------------------------------
    //
    public void addMessageEventListener(MessageEventListener listener) {
        if (messageEventProducer != null) {
            messageEventProducer.addMessageEventListener(listener);
        }
    }

    public void removeMessageEventListener(MessageEventListener listener) {
        if (messageEventProducer != null) {
            messageEventProducer.removeMessageEventListener(listener);
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
    @Override
    protected int internalReceiveRequest(Request request)
        throws java.io.IOException, RenegotiateSessionException {
        if (messageEventProducer != null) {
            messageEventProducer.notifyListeners(request,
                MessageEvent.REQUEST_RECEIVED, bodyID,
                getRequestQueue().size() + 1);
        }

        // request queue length = number of requests in queue
        //							+ the one to add now 
        return requestReceiver.receiveRequest(request, this);
    }

    /**
     * Receives a reply in response to a former request.
     * @param reply the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     */
    @Override
    protected int internalReceiveReply(Reply reply) throws java.io.IOException {
        if (messageEventProducer != null) {
            messageEventProducer.notifyListeners(reply,
                MessageEvent.REPLY_RECEIVED, bodyID);
        }
        return replyReceiver.receiveReply(reply, this, getFuturePool());
    }

    /**
     * Signals that the activity of this body, managed by the active thread has just stopped.
     */
    @Override
    protected void activityStopped() {
        super.activityStopped();
        messageEventProducer = null;
        try {
            this.localBodyStrategy.getRequestQueue().destroy();
        } catch (ProActiveRuntimeException e) {
            bodyLogger.warn("Terminating already terminated body " +
                this.getID(), e);
        }
        setLocalBodyImpl(new InactiveLocalBodyStrategy());
    }

    public void setImmediateService(String methodName)
        throws java.io.IOException {
        this.requestReceiver.setImmediateService(methodName);
    }

    public void setImmediateService(String methodName, Class[] parametersTypes)
        throws IOException {
        this.requestReceiver.setImmediateService(methodName, parametersTypes);
    }

    public void removeImmediateService(String methodName,
        Class[] parametersTypes) throws IOException {
        this.requestReceiver.removeImmediateService(methodName, parametersTypes);
    }

    public void updateNodeURL(String newNodeURL) {
        this.nodeURL = newNodeURL;
    }

    @Override
    public boolean isInImmediateService() throws IOException {
        return this.requestReceiver.isInImmediateService();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    //
    // -- inner classes -----------------------------------------------
    //
    private class ActiveLocalBodyStrategy implements LocalBodyStrategy,
        java.io.Serializable {

        /** A pool future that contains the pending future objects */
        protected FuturePool futures;

        /** The reified object target of the request processed by this body */
        protected Object reifiedObject;
        protected BlockingRequestQueue requestQueue;
        protected RequestFactory internalRequestFactory;
        private long absoluteSequenceID;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public ActiveLocalBodyStrategy(Object reifiedObject,
            BlockingRequestQueue requestQueue, RequestFactory requestFactory) {
            this.reifiedObject = reifiedObject;
            this.futures = new FuturePool();
            this.requestQueue = requestQueue;
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
            return requestQueue;
        }

        public Object getReifiedObject() {
            return reifiedObject;
        }

        public String getName() {
            return reifiedObject.getClass().getName();
        }

        /** Serves the request. The request should be removed from the request queue
         * before serving, which is correctly done by all methods of the Service class.
         * However, this condition is not ensured for custom calls on serve.
         */
        public void serve(Request request) {
            if (Profiling.TIMERS_COMPILED) {
                TimerWarehouse.startTimer(bodyID, TimerWarehouse.SERVE);
                // TimerWarehouse.startTimerWithInfos(bodyID, TimerWarehouse.SERVE, request.getMethodName());        	
            }
            serveInternal(request);
            if (Profiling.TIMERS_COMPILED) {
                TimerWarehouse.stopTimer(bodyID, TimerWarehouse.SERVE);
                //TimerWarehouse.stopTimerWithInfos(bodyID, TimerWarehouse.SERVE, request.getMethodName());        		
            }
        }

        private void serveInternal(Request request) {
            if (request == null) {
                return;
            }
            try {
                messageEventProducer.notifyListeners(request,
                    MessageEvent.SERVING_STARTED, bodyID,
                    getRequestQueue().size());
                Reply reply = null;
                try {
                    //If the request is not a "terminate Active Object" request, 
                    //it is served normally.
                    if (!isTerminateAORequest(request)) {
                        reply = request.serve(BodyImpl.this);
                    }
                } catch (ServeException e) {
                    // Create a non functional exception encapsulating the service exception
                    BodyNonFunctionalException calleeNFE = new ServiceFailedCalleeNFE(
                            "Exception occured while serving pending request = " +
                            request.getMethodName(), e, this,
                            ProActive.getBodyOnThis());
                    NFEManager.fireNFE(calleeNFE, BodyImpl.this);

                    // Create a non functional exception encapsulating the service exception
                    ProxyNonFunctionalException callerNFE = new ServiceFailedCallerNFE(
                            "Exception occured while serving pending request = " +
                            request.getMethodName(), e);

                    // Create a new reply that contains this NFE instead of the result
                    Reply replyAlternate = null;
                    replyAlternate = request.serveAlternate(BodyImpl.this,
                            callerNFE);

                    // Send reply and stop local node if desired
                    if (replyAlternate == null) {
                        if (!isActive()) {
                            return; //test if active in case of terminate() method otherwise eventProducer would be null
                        }
                        messageEventProducer.notifyListeners(request,
                            MessageEvent.VOID_REQUEST_SERVED, bodyID,
                            getRequestQueue().size());
                        return;
                    }

                    if (Profiling.TIMERS_COMPILED) {
                        TimerWarehouse.startTimer(bodyID,
                            TimerWarehouse.SEND_REPLY);
                    }

                    UniqueID destinationBodyId = request.getSourceBodyID();
                    if ((destinationBodyId != null) &&
                            (messageEventProducer != null)) {
                        messageEventProducer.notifyListeners(reply,
                            MessageEvent.REPLY_SENT, destinationBodyId,
                            getRequestQueue().size());
                    }
                    ArrayList<UniversalBody> destinations = new ArrayList<UniversalBody>();
                    destinations.add(request.getSender());
                    this.getFuturePool().registerDestinations(destinations);

                    // FAULT-TOLERANCE
                    if (BodyImpl.this.ftmanager != null) {
                        BodyImpl.this.ftmanager.sendReply(replyAlternate,
                            request.getSender());
                    } else {
                        replyAlternate.send(request.getSender());
                    }

                    if (Profiling.TIMERS_COMPILED) {
                        TimerWarehouse.stopTimer(bodyID,
                            TimerWarehouse.SEND_REPLY);
                    }

                    this.getFuturePool().removeDestinations();
                    return;
                }

                if (reply == null) {
                    if (!isActive()) {
                        return; //test if active in case of terminate() method otherwise eventProducer would be null
                    }
                    if (messageEventProducer != null) {
                        messageEventProducer.notifyListeners(request,
                            MessageEvent.VOID_REQUEST_SERVED, bodyID,
                            getRequestQueue().size());
                    }
                    return;
                }

                if (Profiling.TIMERS_COMPILED) {
                    TimerWarehouse.startTimer(bodyID, TimerWarehouse.SEND_REPLY);
                }

                UniqueID destinationBodyId = request.getSourceBodyID();
                if ((destinationBodyId != null) &&
                        (messageEventProducer != null)) {
                    messageEventProducer.notifyListeners(reply,
                        MessageEvent.REPLY_SENT, destinationBodyId,
                        getRequestQueue().size());
                }
                ArrayList<UniversalBody> destinations = new ArrayList<UniversalBody>();
                destinations.add(request.getSender());
                this.getFuturePool().registerDestinations(destinations);

                // FAULT-TOLERANCE
                if (BodyImpl.this.ftmanager != null) {
                    BodyImpl.this.ftmanager.sendReply(reply, request.getSender());
                } else {
                    reply.send(request.getSender());
                }
                if (Profiling.TIMERS_COMPILED) {
                    TimerWarehouse.stopTimer(bodyID, TimerWarehouse.SEND_REPLY);
                }

                this.getFuturePool().removeDestinations();
            } catch (java.io.IOException e) {
                // Create a non functional exception encapsulating the network exception
                BodyNonFunctionalException nfe = new SendReplyCommunicationException(
                        "Exception occured in while sending reply to request = " +
                        request.getMethodName(), e, BodyImpl.this,
                        request.getSourceBodyID());

                NFEManager.fireNFE(nfe, BodyImpl.this);
            }
        }

        public void sendRequest(MethodCall methodCall, Future future,
            UniversalBody destinationBody)
            throws java.io.IOException, RenegotiateSessionException {
            long sequenceID = getNextSequenceID();
            Request request = internalRequestFactory.newRequest(methodCall,
                    BodyImpl.this, future == null, sequenceID);

            // COMPONENTS : generate ComponentRequest for component messages
            if (methodCall.getComponentMetadata() != null) {
                request = new ComponentRequestImpl((RequestImpl) request);
            }
            if (future != null) {
                future.setID(sequenceID);
                futures.receiveFuture(future);
            }
            messageEventProducer.notifyListeners(request,
                MessageEvent.REQUEST_SENT, destinationBody.getID());

            // FAULT TOLERANCE
            if (BodyImpl.this.ftmanager != null) {
                BodyImpl.this.ftmanager.sendRequest(request, destinationBody);
            } else {
                request.send(destinationBody);
            }
        }

        /**
         * Returns a unique identifier that can be used to tag a future, a request
         * @return a unique identifier that can be used to tag a future, a request.
         */
        public synchronized long getNextSequenceID() {
            return bodyID.toString().hashCode() + ++absoluteSequenceID;
        }

        //
        // -- PROTECTED METHODS -----------------------------------------------
        //

        /**
         * Test if the MethodName of the request is "terminateAO" or "terminateAOImmediately".
         * If true, AbstractBody.terminate() is called
         * @param request The request to serve
         * @return true if the name of the method is "terminateAO" or "terminateAOImmediately".
         */
        private boolean isTerminateAORequest(Request request) {
            boolean terminateRequest = (request.getMethodName()).startsWith(
                    "_terminateAO");
            if (terminateRequest) {
                terminate();
            }
            return terminateRequest;
        }
    }

    // end inner class LocalBodyImpl
    private class InactiveLocalBodyStrategy implements LocalBodyStrategy,
        java.io.Serializable {
        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public InactiveLocalBodyStrategy() {
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //
        //
        // -- implements LocalBody -----------------------------------------------
        //
        public FuturePool getFuturePool() {
            //throw new ProActiveRuntimeException(INACTIVE_BODY_EXCEPTION_MESSAGE);
            return null;
        }

        public BlockingRequestQueue getRequestQueue() {
            throw new ProActiveRuntimeException(INACTIVE_BODY_EXCEPTION_MESSAGE);
        }

        public RequestQueue getHighPriorityRequestQueue() {
            throw new ProActiveRuntimeException(INACTIVE_BODY_EXCEPTION_MESSAGE);
        }

        public Object getReifiedObject() {
            throw new ProActiveRuntimeException(INACTIVE_BODY_EXCEPTION_MESSAGE);
        }

        public String getName() {
            return "inactive body";
        }

        public void serve(Request request) {
            throw new ProActiveRuntimeException(INACTIVE_BODY_EXCEPTION_MESSAGE);
        }

        public void sendRequest(MethodCall methodCall, Future future,
            UniversalBody destinationBody) throws java.io.IOException {
            throw new ProActiveRuntimeException(INACTIVE_BODY_EXCEPTION_MESSAGE);
        }

        /*
         * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getNextSequenceID()
         */
        public long getNextSequenceID() {
            return 0;
        }
    }

    // end inner class LocalInactiveBody
}
