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

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.*;
import org.objectweb.proactive.core.event.*;
import org.objectweb.proactive.core.mop.MethodCall;

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
public abstract class AbstractBody extends AbstractEventProducer implements Body, java.io.Serializable {

    //
    // -- STATIC MEMBERS -----------------------------------------------
    //

    /**
     * This table maps all known active Bodies in this JVM with their UniqueID
     * From one UniqueID it is possible to get the corresponding body if
     * it belongs to this JVM
     */
    private static BodyMap localBodyMap = new BodyMap();

    /**
     * Static object that manages the registration of listeners and the sending of
     * events
     */
    private static BodyEventProducer bodyEventProducer = new BodyEventProducer();

    private static ThreadLocal bodyPerThread = new ThreadLocal();

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /** Unique ID of the body. */
    protected UniqueID bodyID;

    /** A pool future id/future couples that contains the pending future objects */
    protected FuturePool futures;


    /** The reified object target of the request processed by this body */
    protected Object reifiedObject;

    /** A table containing a mapping from a UniqueID toward a Body. The location table
     caches the location of all known bodies to whom this body communicated */
    protected BodyMap location;

    /** The URL of the node this body is attached to */
    protected String nodeURL;

    // WARNING: SOME ATTRIBS ARE TRANSIENT BECAUSE THEY ARE NOT SERIALIZABLES
    //Here we have a problem. If this.remoteBody serialized, RMI fails and we get a ClassCastException :-(
    //Since we don't really need it (we can get it again on arrival)
    //After a migration, we need to rebuild a new one
    /** A remote version of this body that is used to send to remote peer */
    protected transient UniversalBody remoteBody;

    /** The component in charge of receiving reply */
    protected ReplyReceiver replyReceiver;

    /** The component in charge of receiving request */
    protected RequestReceiver requestReceiver;

    protected BlockingRequestQueue requestQueue;

    protected RequestFactory internalRequestFactory;

    protected transient Barrier barrier;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //


    /** whether the body has an activity done with a active thread */
    private transient boolean isActive;

    /** whether the body has been killed. A killed body has no more activity although
     stopping the activity thread is not immediate */
    private transient boolean isDead;

    private long absoluteSequenceID;

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
     */
    public AbstractBody(Object reifiedObject, String nodeURL) {
        this.reifiedObject = reifiedObject;
        this.nodeURL = nodeURL;
        this.bodyID = new UniqueID();
        this.location = new BodyMap();
        this.futures = new FuturePool();
        this.requestQueue = createRequestQueue();
        this.requestReceiver = createRequestReceiver();
        this.replyReceiver = createReplyReceiver();
        this.remoteBody = createRemoteBody();
        this.internalRequestFactory = createRequestFactory();
        //we register in this JVM
        registerBody(this);
        //System.out.println(toString()+" created");
        this.barrier = new Barrier();
    }

    //
    // -- STATIC METHODS -----------------------------------------------
    //

    /**
     * Returns the body associated with the thread calling the method. If no body is associated with the
     * calling thread, an HalfBody is created to manage the futures.
     * @return the body associated to the active object whose active thread is calling this method.
     */
    public static Body getThreadAssociatedBody() {
        AbstractBody body = (AbstractBody) bodyPerThread.get();
        if (body == null) {
            // If we cannot find the body from the current thread we assume that the current thread
            // is not the one from an active object. Therefore in this case we create an HalfBody
            // that handle the futures
            body = HalfBody.getHalfBody();
            bodyPerThread.set(body);
            registerBody(body);
        }
        return body;
    }


    /**
     * Returns the body belonging to this JVM whose ID is the one specified.
     * Returns null if a body with such an id is not found in this jvm
     * @param bodyID the ID to look for
     * @return the body with matching id or null
     */
    public static Body getLocalBody(UniqueID bodyID) {
        return (Body) localBodyMap.getBody(bodyID);
    }


    /**
     * Returns the body belonging to this JVM whose ID is the one specified.
     * Returns null if a body with such an id is not found or if a body exists
     * but is not active
     * @param bodyID the ID to look for
     * @return the active body with matching id or null
     */
    public static Body getLocalActiveBody(UniqueID bodyID) {
        Body b = getLocalBody(bodyID);
        if (b == null) return null;
        return b.isActive() ? b : null;
    }


    /**
     * Returns all local Bodies in a new BodyMap
     * @return all local Bodies in a new BodyMap
     */
    public static BodyMap getLocalBodies() {
        return (BodyMap) localBodyMap.clone();
    }


    /**
     * Adds a listener of body events. The listener is notified every time a body
     * (active or not) is registered or unregistered in this JVM.
     * @param listener the listener of body events to add
     */
    public static void addBodyEventListener(BodyEventListener listener) {
        bodyEventProducer.addBodyEventListener(listener);
    }


    /**
     * Removes a listener of body events.
     * @param listener the listener of body events to remove
     */
    public static void removeBodyEventListener(BodyEventListener listener) {
        bodyEventProducer.removeBodyEventListener(listener);
    }


    private static void registerBody(AbstractBody body) {
        localBodyMap.putBody(body.bodyID, body);
        bodyEventProducer.fireBodyCreated(body);
    }


    private static void unregisterBody(AbstractBody body) {
        localBodyMap.removeBody(body.bodyID);
        bodyEventProducer.fireBodyRemoved(body);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

   /**
   * Returns the future pool of this body
   * @return the future pool of this body
   */
  public FuturePool getFuturePool()  {
    return futures;
  }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    public String toString() {
        return reifiedObject.getClass().getName() + " Body | " + nodeURL + " | " + bodyID;
    }



    //
    // -- implements MessageEventProducer -----------------------------------------------
    //

    public void addMessageEventListener(MessageEventListener listener) {
        addListener(listener);
    }


    public void removeMessageEventListener(MessageEventListener listener) {
        removeListener(listener);
    }


    //
    // -- implements UniversalBody -----------------------------------------------
    //

    /**
     * Receives a request for later processing. The call to this method is non blocking
     * unless the body cannot temporary receive the request.
     * @param request the request to process
     * @exception java.io.IOException if the request cannot be accepted
     */
    public void receiveRequest(Request request) throws java.io.IOException {
        if (isDead) throw new java.io.IOException("The body has been Terminated");
        this.barrier.enter();
        if (hasListeners()) notifyAllListeners(new MessageEvent(request, MessageEvent.REQUEST_RECEIVED, bodyID));
        requestReceiver.receiveRequest(request, this);
        this.barrier.exit();
    }


    /**
     * Receives a reply in response to a former request.
     * @param reply the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     */
    public void receiveReply(Reply reply) throws java.io.IOException {
        if (isDead) throw new java.io.IOException("The body has been Terminated");
        this.barrier.enter();
        if (hasListeners()) notifyAllListeners(new MessageEvent(reply, MessageEvent.REPLY_RECEIVED, bodyID));
        replyReceiver.receiveReply(reply, this, futures);
        this.barrier.exit();
    }


    /**
     * Returns the url of the node this body is associated to
     * The url of the node can change if the active object migrates
     * @return the url of the node this body is associated to
     */
    public String getNodeURL() {
        return nodeURL;
    }


    /**
     * Returns the remote version of this body
     * @return the remote version of this body
     */
    public UniversalBody getRemoteAdapter() {
        return remoteBody;
    }


    /**
     * Returns the UniqueID of this body
     * This identifier is unique accross all JVMs
     * @return the UniqueID of this body
     */
    public UniqueID getID() {
        return this.bodyID;
    }


    /**
     * Signals to this body that the body identified by id is now to a new
     * remote location. The body given in parameter is a new stub pointing
     * to this new location. This call is a way for a body to signal to his
     * peer that it has migrated to a new location
     * @param id the id of the body
     * @param body the stub to the new location
     */
    public void updateLocation(UniqueID bodyID, UniversalBody body) {
        location.putBody(bodyID, body);
    }



    //
    // -- implements Body -----------------------------------------------
    //

    public void setRequestFactory(RequestFactory requestFactory) {
        if (requestFactory == null) throw new IllegalArgumentException("requestFactory cannot be null");
        this.internalRequestFactory = requestFactory;
    }


    public void serve(Request request) {
        if (request == null) return;
        try {
            Reply reply = request.serve(this);
            if (reply == null) return;
            UniqueID destinationBodyId = request.getSourceBodyID();
            if (destinationBodyId != null && hasListeners()) {
                notifyAllListeners(new MessageEvent(reply, MessageEvent.REPLY_SENT, destinationBodyId));
            }
            reply.send(request.getSender());
        } catch (ServeException e) {
            // handle error here
            throw new ProActiveRuntimeException("Exception in serve (Still not handled) : throws killer RuntimeException", e);
        } catch (java.io.IOException e) {
            // handle error here
            throw new ProActiveRuntimeException("Exception in sending reply (Still not handled) : throws killer RuntimeException", e);
        }
    }


    public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody,
                            RequestFactory requestFactory) throws java.io.IOException {
        if (isDead) throw new java.io.IOException("The body has been Terminated");
        long sequenceID = getNextSequenceID();
        Request request = null;
        if (requestFactory == null) {
            request = internalRequestFactory.createRequest(methodCall, this, future == null, sequenceID);
        } else {
            request = requestFactory.createRequest(methodCall, this, future == null, sequenceID);
        }
        if (future != null) futures.put(sequenceID, future);
        if (hasListeners()) notifyAllListeners(new MessageEvent(request, MessageEvent.REQUEST_SENT, destinationBody.getID()));
        request.send(destinationBody);
    }


    public String getName() {
        return reifiedObject.getClass().getName();
    }


    public void terminate() {
        if (isDead) return;
        isDead = true;
        activityStopped();
        // we are no longer an existing body
        //unregisterBody(this);
        // unblock is thread was block
        acceptCommunication();
    }


    public BlockingRequestQueue getRequestQueue() {
        return requestQueue;
    }


    public UniversalBody checkNewLocation(UniqueID bodyID) {
        //we look in the location table of the current JVM
        Body body = (Body) localBodyMap.getBody(bodyID);
        if (body != null) {
            // we update our table to say that this body is local
            location.putBody(bodyID, body);
            return body;
        } else {
            //it was not found in this vm let's try the location table
            return location.getBody(bodyID);
        }
    }


    public Object getReifiedObject() {
        return reifiedObject;
    }


    public void blockCommunication() {
        barrier.close();
    }


    public void acceptCommunication() {
        barrier.open();
    }


    public boolean isAlive() {
        return !isDead;
    }


    public boolean isActive() {
        return isActive;
    }



    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Creates the component in charge of storing incoming requests.
     * @return the component in charge of storing incoming requests.
     */
    protected abstract BlockingRequestQueue createRequestQueue();


    /**
     * Creates the component in charge of receiving incoming requests.
     * @return the component in charge of receiving incoming requests.
     */
    protected abstract RequestReceiver createRequestReceiver();


    /**
     * Creates the component in charge of receiving incoming replies.
     * @return the component in charge of receiving incoming replies.
     */
    protected abstract ReplyReceiver createReplyReceiver();


    /**
     * Creates the factory in charge of constructing the requests.
     * @return the factory in charge of constructing the requests.
     */
    protected RequestFactory createRequestFactory() {
        return new DefaultRequestFactory();
    }


    /**
     * Creates the remote version of this body.
     * @return the remote version of this body.
     */
    protected UniversalBody createRemoteBody() {
        try {
            return new org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter(this);
        } catch (ProActiveException e) {
            throw new ProActiveRuntimeException("Cannot create Remote body adapter ", e);
        }
    }


    /**
     * Returns a unique identifier that can be used to tag a future, a request
     * @return a unique identifier that can be used to tag a future, a request.
     */
    protected synchronized long getNextSequenceID() {
        return ++absoluteSequenceID;
    }


    /**
     * Signals that the activity of this body, managed by the active thread has just stopped.
     */
    protected void activityStopped() {
        isActive = false;
        //We are no longer an active body
        unregisterBody(this);
        //bodyEventProducer.fireBodyChanged(this);
        requestQueue.destroy();
        //System.out.println("Body:activityStopped localBodyMap.removeBody(bodyID)");
    }


    /**
     * Signals that the activity of this body, managed by the active thread has just started.
     */
    protected void activityStarted() {
        isActive = true;
        // we associated this body to the thread running it
        bodyPerThread.set(this);
        //We are now an active body
        bodyEventProducer.fireBodyChanged(this);
        //System.out.println("Body:activityStarted localBodyMap.putBody(bodyID)");
    }


    protected void notifyOneListener(ProActiveListener listener, ProActiveEvent event) {
        MessageEvent messageEvent = (MessageEvent) event;
        MessageEventListener messageEventListener = (MessageEventListener) listener;
        switch (messageEvent.getType()) {
            case MessageEvent.REQUEST_SENT:
                // WARNING: we don't generate an event in the following case:
                //   - The listener is an active object  AND
                //   - The destination of the request is the listener
                // This is done to avoid recursive generation of events
                if (listener instanceof org.objectweb.proactive.core.mop.StubObject) {
                    org.objectweb.proactive.core.mop.StubObject so = (org.objectweb.proactive.core.mop.StubObject) listener;
                    UniqueID id = ((org.objectweb.proactive.core.body.proxy.BodyProxy) so.getProxy()).getBodyID();
                    if (id.equals(messageEvent.getDestinationBodyID())) {
                        break;
                    }
                }
                messageEventListener.requestSent(messageEvent);
                break;

            case MessageEvent.REQUEST_RECEIVED:
                messageEventListener.requestReceived(messageEvent);
                break;

            case MessageEvent.REPLY_SENT:
                messageEventListener.replySent(messageEvent);
                break;

            case MessageEvent.REPLY_RECEIVED:
                messageEventListener.replyReceived(messageEvent);
                break;
        }
    }



    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }


    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // remoteBody is transient so we recreate it here
        this.remoteBody = createRemoteBody();
        // barrier is transient so we recreate it here
        this.barrier = new Barrier();
        if (bodyID == null) {
            // it may append that the bodyID is set to null before serialization if we want to
            // create a copy of the Body that is distinct from the original
            bodyID = new UniqueID();
        }
        //we register in this JVM
        registerBody(this);
    }




    //
    // -- inner classes -----------------------------------------------
    //


    private static class BodyEventProducer extends AbstractEventProducer {

        BodyEventProducer() {
        }

        void fireBodyCreated(AbstractBody b) {
            if (hasListeners())
                notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_CREATED));
        }

        void fireBodyRemoved(AbstractBody b) {
            if (hasListeners())
                notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_DESTROYED));
        }

        void fireBodyChanged(AbstractBody b) {
            //System.out.println("fireBodyChanged from AbstractBody active="+b.isActive());
            if (hasListeners())
                notifyAllListeners(new BodyEvent(b, BodyEvent.BODY_CHANGED));
        }
        //
        // -- implements BodyEventProducer -----------------------------------------------
        //

        public void addBodyEventListener(BodyEventListener listener) {
            addListener(listener);
        }


        public void removeBodyEventListener(BodyEventListener listener) {
            removeListener(listener);
        }

        //
        // -- PROTECTED METHODS -----------------------------------------------
        //

        protected void notifyOneListener(ProActiveListener listener, ProActiveEvent event) {
            BodyEvent bodyEvent = (BodyEvent) event;
            switch (bodyEvent.getType()) {
                case BodyEvent.BODY_CREATED:
                    ((BodyEventListener) listener).bodyCreated(bodyEvent);
                    break;

                case BodyEvent.BODY_DESTROYED:
                    ((BodyEventListener) listener).bodyDestroyed(bodyEvent);
                    break;

                case BodyEvent.BODY_CHANGED:
                    ((BodyEventListener) listener).bodyChanged(bodyEvent);
                    break;
            }
        }
    } // end inner class BodyEventProducer


    protected class DefaultRequestFactory implements RequestFactory, java.io.Serializable {

        /**
         * Creates a request object based on the given parameter
         * @return a Request object.
         */
        public Request createRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
            return new RequestImpl(methodCall, sourceBody, isOneWay, sequenceID);
        }

    } // end inner class DefaultRequestFactory


    protected class Barrier implements java.io.Serializable {

        protected int counter;
        protected boolean open = true;

        public synchronized void enter() {
            while (!open) {
                try {
                   wait();
                } catch (InterruptedException e) {}
            }
          counter++;
        }

        public synchronized void exit() {
            counter--;
            notifyAll();
        }

        /**
         * Close the barrier, block until all threads
         * currently in the body have finished
         */
        public synchronized void close() {
            open = false;
            while (counter != 0 && !open) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }

        public synchronized void open() {
            open = true;
            notifyAll();
        }
    }

}
