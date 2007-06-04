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
package org.objectweb.proactive.core.body.future;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.event.FutureEvent;
import org.objectweb.proactive.core.exceptions.manager.ExceptionHandler;
import org.objectweb.proactive.core.exceptions.manager.ExceptionMaskLevel;
import org.objectweb.proactive.core.exceptions.manager.NFEManager;
import org.objectweb.proactive.core.exceptions.proxy.FutureTimeoutException;
import org.objectweb.proactive.core.exceptions.proxy.ProxyNonFunctionalException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.profiling.Profiling;
import org.objectweb.proactive.core.util.profiling.TimerWarehouse;


/**
 * This proxy class manages the semantic of future objects
 *
 * @author Julien Vayssi?re - INRIA
 * @see org.objectweb.proactive.core.mop.Proxy
 *
 */
public class FutureProxy implements Future, Proxy, java.io.Serializable {
    //
    // -- STATIC MEMBERS -----------------------------------------------
    //

    /**
     *  The size of the pool we use for recycling FutureProxy objects.
     */
    public static final int RECYCLE_POOL_SIZE = 1000;
    private static FutureProxy[] recyclePool;

    /**
     *  Indicates if the recycling of FutureProxy objects is on.
     */
    private static boolean shouldPoolFutureProxyObjects;
    private static int index;

    /** Static point for management of events related to futures.
     * This FutureEventProducer is responsible for all FutureProxys of this VM */
    private static FutureEventProducerImpl futureEventProducer;

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /**
     *        The object the proxy sends calls to
     */
    protected FutureResult target;

    /**
     * True if this proxy has to be copied for migration or local copie.
     * If true, the serialization of this future does not register an automatic continuation.
     */
    protected transient boolean copyMode;

    /**
     * UniqueID of the body which create this future
     */
    protected UniqueID creatorID;

    /**
     * ID of the future
     * In fact, the sequence number of the request that generate this future
     */
    protected long ID;

    /**
     * Unique ID of the sender (in case of automatic continuation).
     */
    protected UniqueID senderID;

    /**
     * The exception level in the stack in which this future is
     * registered
     */
    private ExceptionMaskLevel exceptionLevel;

    /**
     * The proxy that created this future. Set as transient to avoid
     * adding remote references when sending the future. Migration is
     * thus not supported.
     */
    private transient AbstractProxy originatingProxy;

    /**
     * Max timeout when waiting for a future
     * Can be set with the property proactive.future.maxdelay
     */
    protected static long futureMaxDelay = -1;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * As this proxy does not create a reified object (as opposed to
     * BodyProxy for example), it is the noargs constructor that
     * is usually called.
     */
    public FutureProxy() throws ConstructionOfReifiedObjectFailedException {
    }

    /**
     * This constructor is provided for compatibility with other proxies.
     * More precisely, this permits proxy instanciation via the Meta.newMeta
     * method.
     */
    public FutureProxy(ConstructorCall c, Object[] p)
        throws ConstructionOfReifiedObjectFailedException {
        // we don't care what the arguments are
        this();
    }

    //
    // -- PUBLIC STATIC METHODS -----------------------------------------------
    //

    /**
     * Tests if the object <code>obj</code> is awaited or not. Always returns
     * <code>false</code> if <code>obj</code> is not a future object.
     */
    public static boolean isAwaited(Object obj) {
        return ProActive.isAwaited(obj);
    }

    public synchronized static FutureProxy getFutureProxy() {
        FutureProxy result;
        if (shouldPoolFutureProxyObjects && (index > 0)) {
            // gets the object from the pool
            index--;
            result = recyclePool[index];
            recyclePool[index] = null;
        } else {
            try {
                result = new FutureProxy();
            } catch (ConstructionOfReifiedObjectFailedException e) {
                result = null;
            }
        }
        return result;
    }

    /** Returns the <code>FutureEventProducer</code> that is responsible for the
     * FutureProxys of this VM. Listeners can register themselves here. */
    public static FutureEventProducer getFutureEventProducer() {
        if (futureEventProducer == null) {
            futureEventProducer = new FutureEventProducerImpl();
        }
        return futureEventProducer;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public boolean equals(Object obj) {
        //we test if we have a future object
        if (isFutureObject(obj)) {
            return (((StubObject) obj).getProxy().hashCode() == this.hashCode());
        }
        return false;
    }

    //
    // -- Implements Future -----------------------------------------------
    //

    /**
     * Invoked by a thread of the skeleton that performed the service in order
     * to tie the result object to the proxy.
     *
     * If the execution of the call raised an exception, this exception is put
     * into an object of class InvocationTargetException and returned, just like
     * for any returned object
     */
    public synchronized void receiveReply(FutureResult obj)
        throws java.io.IOException {
        if (isAvailable()) {
            throw new java.io.IOException(
                "FutureProxy receives a reply and this target field is not null");
        }
        target = obj;
        ExceptionHandler.addResult(this);
        ProxyNonFunctionalException nfe = target.getNFE();
        if (nfe != null) {
            NFEManager.fireNFE(nfe, originatingProxy);
        }

        originatingProxy = null;
        this.notifyAll();
    }

    /**
     * Returns the result this future is for as an exception if an exception has been raised
     * or null if the result is not an exception. The method blocks until the result is available.
     * @return the exception raised once available or null if no exception.
     */
    public synchronized Throwable getRaisedException() {
        waitFor();
        return target.getExceptionToRaise();
    }

    /**
     * @return true iff the future has arrived.
     */
    public boolean isAvailable() {
        return target != null;
    }

    /**
     * Returns a FutureResult containing the awaited result, or the exception that occured if any.
     * The method blocks until the future is available
     * @return the result of this future object once available.
     */
    public synchronized FutureResult getFutureResult() {
        waitFor();
        return target;
    }

    /**
     * Returns the result this future is for. The method blocks until the future is available
     * @return the result of this future object once available.
     */
    public synchronized Object getResult() {
        waitFor();
        return target.getResult();
    }

    /**
     * Tests the status of the returned object
     * @return <code>true</code> if the future object is NOT yet available, <code>false</code> if it is.
     */
    public synchronized boolean isAwaited() {
        return !isAvailable();
    }

    /**
     * Blocks the calling thread until the future object is available.
     */
    public synchronized void waitFor() {
        if (futureMaxDelay == -1) {

            /* First time, hopefully the configuration file has been read */
            try {
                futureMaxDelay = Long.parseLong(ProActiveConfiguration.getInstance()
                                                                      .getProperty("proactive.future.maxdelay"));
            } catch (IllegalArgumentException iea) {
                /* The property is not set, that's not a problem */
                futureMaxDelay = 0;
            }
        }

        try {
            waitFor(futureMaxDelay);
        } catch (ProActiveException e) {
            ProxyNonFunctionalException nfe = new FutureTimeoutException(
                    "Exception after waiting for " + futureMaxDelay + "ms", e);

            target = new FutureResult(null, null, nfe);
            notifyAll();
        }
    }

    /**
     * Blocks the calling thread until the future object is available or the timeout expires
     * @param timeout
     * @throws ProActiveException if the timeout expires
     */
    public synchronized void waitFor(long timeout) throws ProActiveException {
        if (isAvailable()) {
            return;
        }

        if (Profiling.TIMERS_COMPILED) {
            TimerWarehouse.startTimer(ProActive.getBodyOnThis().getID(),
                TimerWarehouse.WAIT_BY_NECESSITY);
        }

        UniqueID id = null;

        // send WAIT_BY_NECESSITY event to listeners if there are any
        if (futureEventProducer != null) {
            id = ProActive.getBodyOnThis().getID();
            if (LocalBodyStore.getInstance().getLocalBody(id) != null) {
                // send event only if ActiveObject, not for HalfBodies
                futureEventProducer.notifyListeners(id, getCreatorID(),
                    FutureEvent.WAIT_BY_NECESSITY);
            } else {
                id = null;
            }
        }
        int timeoutCounter = 1;
        while (!isAvailable()) {
            timeoutCounter--;
            // counter < 0 means that it is the second time we enter in the loop
            // while still not available, i.e timeout has expired
            if (timeoutCounter < 0) {
                throw new ProActiveException(
                    "Timeout expired while waiting for the future update");
            }
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // send RECEIVED_FUTURE_RESULT event to listeners if there are any
        if (id != null) {
            futureEventProducer.notifyListeners(id, getCreatorID(),
                FutureEvent.RECEIVED_FUTURE_RESULT);
        }

        if (Profiling.TIMERS_COMPILED) {
            TimerWarehouse.stopTimer(ProActive.getBodyOnThis().getID(),
                TimerWarehouse.WAIT_BY_NECESSITY);
        }
    }

    public long getID() {
        return ID;
    }

    public void setID(long l) {
        ID = l;
    }

    public void setCreatorID(UniqueID i) {
        creatorID = i;
    }

    public UniqueID getCreatorID() {
        return creatorID;
    }

    public void setSenderID(UniqueID i) {
        senderID = i;
    }

    public void setOriginatingProxy(AbstractProxy p) {
        originatingProxy = p;
    }

    //
    // -- Implements Proxy -----------------------------------------------
    //

    /**
     * Blocks until the future object is available, then executes Call <code>c</code> on the now-available object.
     *
     *  As future and process behaviors are mutually exclusive, we know that
     * the invocation of a method on a future objects cannot lead to wait-by
     * necessity. Thus, we can propagate all exceptions raised by this invocation
     *
     * @exception InvocationTargetException If the invokation of the method represented by the
     * <code>Call</code> object <code>c</code> on the reified object
     * throws an exception, this exception is thrown as-is here. The stub then
     * throws this exception to the calling thread after checking that it is
     * declared in the throws clause of the reified method. Otherwise, the stub
     * does nothing except print a message on System.err (or out ?).
     */
    public Object reify(MethodCall c) throws InvocationTargetException {
        Object result = null;
        waitFor();

        // Now that the object is available, execute the call
        Object resultObject = target.getResult();
        try {
            result = c.execute(resultObject);
        } catch (MethodCallExecutionFailedException e) {
            throw new ProActiveRuntimeException(
                "FutureProxy: Illegal arguments in call " + c.getName());
        }

        // If target of this future is another future, make a shortcut !
        if (resultObject instanceof StubObject) {
            Proxy p = ((StubObject) resultObject).getProxy();
            if (p instanceof FutureProxy) {
                target = ((FutureProxy) p).target;
            }
        }

        return result;
    }

    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected void finalize() {
        returnFutureProxy(this);
    }

    public void setCopyMode(boolean mode) {
        copyMode = mode;
    }

    //
    // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
    //
    private synchronized void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        if (!FuturePool.isInsideABodyForwarder()) {
            // if copy mode, no need for registering AC
            if (this.isAwaited() && !this.copyMode) {
                boolean continuation = (FuturePool.getBodiesDestination() != null);

                // if continuation=false, no destination is registred:
                // - either ac are disabled,
                // - or this future is serialized in a migration forwarder.

                // identify the sender for regsitering continuation and determine if we are in a migration formwarder
                Body sender = LocalBodyStore.getInstance().getLocalBody(senderID);

                // it's a halfbody...
                if (sender == null) {
                    sender = LocalBodyStore.getInstance()
                                           .getLocalHalfBody(senderID);
                }
                if (sender != null) { // else we are in a migration forwarder
                    if (continuation) {
                        for (UniversalBody dest : FuturePool.getBodiesDestination()) {
                            sender.getFuturePool()
                                  .addAutomaticContinuation(ID, creatorID, dest);
                        }
                    } else {
                        // its not a copy and not a continuation: wait for the result
                        this.waitFor();
                    }
                }
            }
        } else {
            // Maybe this FutureProxy has been added into FuturePool by readObject
            // Remove it and restore continuation
            ArrayList<Future> futures = FuturePool.getIncomingFutures();
            if (futures != null) {
                for (int i = 0; i < futures.size(); i++) {
                    Future fp = futures.get(i);
                    if (fp.getCreatorID().equals(creatorID) &&
                            (fp.getID() == ID)) {
                        FuturePool.removeIncomingFutures();
                    }
                }
            }
        }

        // for future that are deepcopied then not registred in any futurepool
        out.writeObject(senderID);
        // Pass the result
        out.writeObject(target);
        // Pass the id
        out.writeLong(ID);
        //Pass the creatorID
        out.writeObject(creatorID);
    }

    private synchronized void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        senderID = (UniqueID) in.readObject();
        target = (FutureResult) in.readObject();
        ID = (long) in.readLong();
        creatorID = (UniqueID) in.readObject();
        // register all incoming futures, even for migration or checkpoiting
        if (this.isAwaited()) {
            FuturePool.registerIncomingFuture(this);
        }
        copyMode = false;
    }

    //
    // -- PRIVATE STATIC METHODS -----------------------------------------------
    //
    private static boolean isFutureObject(Object obj) {
        // If obj is not reified, it cannot be a future
        if (!(MOP.isReifiedObject(obj))) {
            return false;
        }

        // Being a future object is equivalent to have a stub/proxy pair
        // where the proxy object implements the interface FUTURE_PROXY_INTERFACE
        // if the proxy does not inherit from FUTURE_PROXY_ROOT_CLASS
        // it is not a future
        Class proxyclass = ((StubObject) obj).getProxy().getClass();
        Class[] ints = proxyclass.getInterfaces();
        for (int i = 0; i < ints.length; i++) {
            if (Constants.FUTURE_PROXY_INTERFACE.isAssignableFrom(ints[i])) {
                return true;
            }
        }
        return false;
    }

    private static synchronized void setShouldPoolFutureProxyObjects(
        boolean value) {
        if (shouldPoolFutureProxyObjects == value) {
            return;
        }
        shouldPoolFutureProxyObjects = value;
        if (shouldPoolFutureProxyObjects) {
            // Creates the recycle poll for FutureProxy objects
            recyclePool = new FutureProxy[RECYCLE_POOL_SIZE];
            index = 0;
        } else {
            // If we do not want to recycle FutureProxy objects anymore,
            // let's free some memory by permitting the reyclePool to be
            // garbage-collecting
            recyclePool = null;
        }
    }

    private static synchronized void returnFutureProxy(FutureProxy futureProxy) {
        if (!shouldPoolFutureProxyObjects) {
            return;
        }

        // If there's still one slot left in the pool
        if (recyclePool[index] == null) {
            // Cleans up a FutureProxy object
            // It is prefereable to do it here rather than at the moment
            // the object is picked out of the pool, because it allows
            // garbage-collecting the objects referenced in here
            futureProxy.target = null;
            futureProxy.exceptionLevel = null;

            // Inserts the object in the pool
            recyclePool[index] = futureProxy;
            index++;
            if (index == RECYCLE_POOL_SIZE) {
                index = RECYCLE_POOL_SIZE - 1;
            }
        }
    }

    /**
     * @return Returns the exceptionLevel.
     */
    public ExceptionMaskLevel getExceptionLevel() {
        return exceptionLevel;
    }

    /**
     * @param exceptionLevel The exceptionLevel to set.
     */
    public void setExceptionLevel(ExceptionMaskLevel exceptionLevel) {
        this.exceptionLevel = exceptionLevel;
    }

    //////////////////////////
    //////////////////////////
    ////FOR DEBUG PURPOSE/////
    //////////////////////////
    //////////////////////////
    public synchronized static int futureLength(Object future) {
        int res = 0;
        if ((MOP.isReifiedObject(future)) &&
                ((((StubObject) future).getProxy()) instanceof Future)) {
            res++;
            Future f = (Future) (((StubObject) future).getProxy());
            Object gna = f.getResult();
            while ((MOP.isReifiedObject(gna)) &&
                    ((((StubObject) gna).getProxy()) instanceof Future)) {
                f = (Future) (((StubObject) gna).getProxy());
                gna = f.getResult();
                res++;
            }
        }
        return res;
    }
}
