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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.body.request.RequestProcessor;
import org.objectweb.proactive.core.component.body.ComponentBody;


/**
 * 
 * <P>
 * Service is a utility class that provides many useful methods to serve requests.
 * It is usually instantiated once at the beginning of the runActivity() method of
 * an active object in order to be used for serving requests. For instance :
 * </P><P>
 * <pre>
 * public void runActivity(org.objectweb.proactive.Body body) {
 *   org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
 * ...
 * }
 * </pre>
 * For instance the live method of the bounded buffer example :
 * <pre>
 *  public void runActivity(org.objectweb.proactive.Body body) {
 *    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
 *    while (body.isActive()) {
 *      if (count == 0) {
 *        // if the buffer is empty
 *        service.blockingServeOldest("put"); // Serve the first buffer.put call
 *      } else if (count == size) {
 *        // if the buffer is full
 *        service.blockingServeOldest("get"); // Serve the first buffer.get call
 *      } else {
 *        // if the buffer is neither empty nor full
 *        service.blockingServeOldest(); // Serve the first buffer.xxx call
 *      }
 *    }
 *  }
 * </pre>
 *
 * </P>
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see RunActive
 *
 */
@PublicAPI
public class Service {
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected Body body;
    protected BlockingRequestQueue requestQueue;
    protected LifeCycleController lifeCycleController = null;

    //protected RequestFilterOnMethodName requestFilterOnMethodName = null;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new instance of Service based on the given body.
     * @param body the body that helper service is for.
     */
    public Service(Body body) {
        this.body = body;
        this.requestQueue = body.getRequestQueue();
        if (((ComponentBody) body).isComponent()) {
            try {
                lifeCycleController = Fractal.getLifeCycleController(((ComponentBody) body)
                        .getProActiveComponentImpl());
            } catch (NoSuchInterfaceException e) {
                throw new ProActiveRuntimeException(
                    "could not find the life cycle controller for this component");
            }
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public String toString() {
        return "Service\n  Body=" + body.toString() + "\n  RequestQueue=" + requestQueue.toString();
    }

    /**
     * Serves the request given in parameter
     * @param request the request to be served
     */
    public void serve(Request request) {
        body.serve(request);
    }

    /**
     * Invoke the default FIFO policy to pick up the requests from the request queue.
     * This does not return until the body terminate, as the active thread enters in
     * an infinite loop for processing the request in the FIFO order.
     */
    public void fifoServing() {
        if (((ComponentBody) body).isComponent()) {
            while (LifeCycleController.STARTED.equals(lifeCycleController.getFcState())) {
                blockingServeOldest();
            }
        } else {
            while (body.isActive()) {
                blockingServeOldest();
            }
        }
    }

    /**
     * Invoke the LIFO  policy to pick up the requests from the request queue.
     * This does not return until the body terminate, as the active thread enters in
     * an infinite loop for processing the request in the LIFO order.
     */
    public void lifoServing() {
        while (body.isActive()) {
            blockingServeYoungest();
        }
    }

    // -- Serve Oldest ---------------------------------------------------

    /**
     * Serves the oldest request in the request queue.
     * The method blocks if there is no request until one request is
     * received or until the body terminates.
     */
    public void blockingServeOldest() {
        blockingServeOldest(null, 0);
    }

    /**
     * Serves the oldest request in request queue.
     * The method blocks if there is no request until one request is
     * received or until the body terminates. The method does not block
     * more than the given timeout.
     * @param timeout how long the thread can be blocked for.
     */
    public void blockingServeOldest(long timeout) {
        blockingServeOldest(null, timeout);
    }

    /**
     * Serves the oldest request matching the criteria given be the filter.
     * The method blocks if there is no matching request until one
     * matching request is received or until the body terminates. The method does not block
     * more than the given timeout.
     * @param requestFilter The request filter accepting the request
     * @param timeout the timeout in ms
     */
    public void blockingServeOldest(RequestFilter requestFilter, long timeout) {
        Request r = requestQueue.blockingRemoveOldest(requestFilter, timeout);
        if (r != null) {
            body.serve(r);
        }
    }

    /**
     * Serves the oldest request for a method of name <code>methodName</code>.
     * The method blocks if there is no matching request until one
     * matching request is received or until the body terminates.
     * @param methodName The name of the request to serve
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public void blockingServeOldest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        blockingServeOldest(new RequestFilterOnMethodName(methodName));
    }

    /**
     * Serves the oldest request matching the criteria given be the filter.
     * The method blocks if there is no matching request until one
     * matching request is received or until the body terminates.
     * @param requestFilter The request filter accepting the request
     */
    public void blockingServeOldest(RequestFilter requestFilter) {
        blockingServeOldest(requestFilter, 0);
    }

    /**
     * Serves the oldest request in the request queue. If there is no
     * request, the method returns with no effect.
     */
    public void serveOldest() {
        body.serve(requestQueue.removeOldest());
    }

    /**
     * Serves the oldest request for a method of name methodName.
     * If no matching request is found, the method returns with no effect.
     * @param methodName The name of the request to serve
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public void serveOldest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        body.serve(requestQueue.removeOldest(methodName));
    }

    /**
     * Serves the oldest request matching the criteria given be the filter.
     * If no matching request is found, the method returns with no effect.
     * @param requestFilter The request filter accepting the request
     */
    public void serveOldest(RequestFilter requestFilter) {
        body.serve(requestQueue.removeOldest(requestFilter));
    }

    // -- Serve Youngest ---------------------------------------------------

    /**
     * Serves the youngest request in the request queue.
     * The method blocks if there is no request until one request is
     * received or until the body terminates.
     */
    public void blockingServeYoungest() {
        blockingServeYoungest(null, 0);
    }

    /**
     * Serves the youngest request in request queue.
     * The method blocks if there is no request until one request is
     * received or until the body terminates. The method does not block
     * more than the given timeout.
     * @param timeout : for how long the thread can be blocked.
     */
    public void blockingServeYoungest(long timeout) {
        blockingServeYoungest(null, timeout);
    }

    /**
     * Serves the youngest request for a method of name <code>methodName</code>.
     * The method blocks if there is no matching request until one
     * matching request is received or until the body terminates.
     * @param methodName The name of the request to serve
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public void blockingServeYoungest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        blockingServeYoungest(new RequestFilterOnMethodName(methodName));
    }

    /**
     * Serves the youngest request matching the criteria given be the filter.
     * The method blocks if there is no matching request until one
     * matching request is received or until the body terminates.
     * @param requestFilter The request filter accepting the request
     */
    public void blockingServeYoungest(RequestFilter requestFilter) {
        blockingServeYoungest(requestFilter, 0);
    }

    /**
     * Serves the youngest request matching the criteria given be the filter.
     * The method blocks if there is no matching request until one
     * matching request is received or until the body terminates. The method does not block
     * more than the given timeout.
     * @param requestFilter The request filter accepting the request
     * @param timeout : for how long the thread can be blocked.
     */
    public void blockingServeYoungest(RequestFilter requestFilter, long timeout) {
        body.serve(requestQueue.blockingRemoveYoungest(requestFilter, timeout));
    }

    /**
     * Serves the youngest request in the request queue. If there is no
     * request, the method returns with no effect.
     */
    public void serveYoungest() {
        body.serve(requestQueue.removeYoungest());
    }

    /**
     * Serves the youngest request for a method of name methodName.
     * If no matching request is found, the method returns with no effect.
     * @param methodName The name of the request to serve
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public void serveYoungest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        body.serve(requestQueue.removeYoungest(methodName));
    }

    /**
     * Serves the youngest request matching the criteria given be the filter.
     * If no matching request is found, the method returns with no effect.
     * @param requestFilter The request filter accepting the request
     */
    public void serveYoungest(RequestFilter requestFilter) {
        body.serve(requestQueue.removeYoungest(requestFilter));
    }

    // -- Serve All ---------------------------------------------------

    /**
     * Serves all requests for the method named <code>methodName</code>.
     * If there is no request matching the method name,
     * no request is served.
     * All served requests are removed from the RequestQueue.
     * @param methodName The name of the request to serve
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public void serveAll(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        serveAll(new RequestFilterOnMethodName(methodName));
    }

    /**
     * Serves all requests accepted by the given filter.
     * All served requests are removed from the RequestQueue.
     * @param requestFilter The request filter accepting the request
     */
    public void serveAll(RequestFilter requestFilter) {
        requestQueue.processRequests(new ServingRequestProcessor(requestFilter), body);
    }

    // -- Serve And Flush Youngest ---------------------------------------------------

    /**
     * Serves the youngest request and discard all other requests.
     * After the call the youngest request is served and the request
     * queue is empty.
     * If the request queue is already empty before the call the method
     * has no effect
     */
    public void flushingServeYoungest() {
        flushingServeYoungest(new AcceptAllRequestFilter());
    }

    /**
     * Serves the most recent request (youngest) for the method named <code>methodName</code>
     * and discards all the other requests of the same name. The most recent
     * request is the one served.
     * If there is no match, no request is served or removed.
     * All requests of method name <code>methodName</code> are removed from the
     * request queue.
     * @param methodName The name of the request to serve and flush
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public void flushingServeYoungest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        flushingServeYoungest(new RequestFilterOnMethodName(methodName));
    }

    /**
     * Serves the most recent request (youngest) accepted by the given filter
     * and discards all the other requests also accepted by this sasme filter.
     * The most recent request is the one served.
     * If there is no match, no request is served or removed.
     * All requests accepted by the filter are removed from the request queue.
     * @param requestFilter The request filter accepting requests
     */
    public void flushingServeYoungest(RequestFilter requestFilter) {
        requestQueue.processRequests(new FlushingServeYoungestRequestProcessor(requestFilter), body);
    }

    // -- Serve And Flush Oldest ---------------------------------------------------

    /**
     * Serves the oldest request and discard all other requests.
     * After the call the oldest request is served and the request
     * queue is empty.
     * If the request queue is already empty before the call the method
     * has no effect
     */
    public void flushingServeOldest() {
        flushingServeOldest(new AcceptAllRequestFilter());
    }

    /**
     * Serves the oldest request for the method named <code>methodName</code>
     * and discards all the other requests of the same name. The oldest
     * request is the one served.
     * If there is no match, no request is served or removed.
     * All requests of method name <code>methodName</code> are removed from the
     * request queue.
     * @param methodName The name of the request to serve and flush
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public void flushingServeOldest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        flushingServeOldest(new RequestFilterOnMethodName(methodName));
    }

    /**
     * Serves the oldest request accepted by the given filter
     * and discards all the other requests also accepted by this sasme filter.
     * The oldest request is the one served.
     * If there is no match, no request is served or removed.
     * All requests accepted by the filter are removed from the request queue.
     * @param requestFilter The request filter accepting requests
     */
    public void flushingServeOldest(RequestFilter requestFilter) {
        requestQueue.processRequests(new FlushingServeOldestRequestProcessor(requestFilter), body);
    }

    // -- Other helpers methods ---------------------------------------------------

    /**
     * blocks until a request is available or until the body terminate
     */
    public void waitForRequest() {
        requestQueue.waitForRequest(0);
    }

    /**
     * true if and only if at least one request is available
     * @return true if a request is available, false else.
     */
    public boolean hasRequestToServe() {
        return !requestQueue.isEmpty();
    }

    /**
     * true if and only if at least one request with the given name is available
     * @return true if a request with the given name is available, false else.
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public boolean hasRequestToServe(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        return requestQueue.hasRequest(methodName);
    }

    /**
     * Returns the number of request(s) in the queue
     * @return the number of request(s) in the queue.
     */
    public int getRequestCount() {
        return requestQueue.size();
    }

    /**
     * Removes all request from the queue. No request is served.
     */
    public void flushAll() {
        requestQueue.clear();
    }

    //
    // -- getOldests ---------------------------------------------------
    //

    /**
     * Returns the oldest request from the queue or null if the queue is empty
     * The request queue is unchanged by this call
     * @return the oldest request or null
     */
    public Request getOldest() {
        return requestQueue.getOldest();
    }

    /**
     * Returns the oldest request whose method name is s or null if no match
     * The request queue is unchanged by this call
     * @param methodName the name of the method to look for
     * @return the oldest matching request or null
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public Request getOldest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        return requestQueue.getOldest(methodName);
    }

    /**
     * Returns the oldest request that matches the criteria defined by the given filter
     * The request queue is unchanged by this call
     * @param requestFilter the filter accepting request on a given criteria
     * @return the oldest matching request or null
     */
    public Request getOldest(RequestFilter requestFilter) {
        return requestQueue.getOldest(requestFilter);
    }

    /**
     * Returns the oldest request from the queue
     * If no request is available the method block until one request can be returned
     * The request queue is unchanged by this call
     * @return the oldest request or null
     */
    public Request blockingGetOldest() {
        Request request = null;
        while ((request == null) && !requestQueue.isDestroyed()) {
            waitForRequest();
            request = requestQueue.getOldest();
        }
        return request;
    }

    //
    // -- getYoungests ---------------------------------------------------
    //

    /**
     * Returns the youngest request from the queue or null if the queue is empty
     * The request queue is unchanged by this call
     * @return the youngest request or null
     */
    public Request getYoungest() {
        return requestQueue.getYoungest();
    }

    /**
     * Returns the youngest request whose method name is s or null if no match
     * The request queue is unchanged by this call
     * @param methodName the name of the method to look for
     * @return the youngest matching request or null
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public Request getYoungest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        return requestQueue.getYoungest(methodName);
    }

    /**
     * Returns the youngest request that matches the criteria defined by the given filter
     * The request queue is unchanged by this call
     * @param requestFilter the filter accepting request on a given criteria
     * @return the youngest matching request or null
     */
    public Request getYoungest(RequestFilter requestFilter) {
        return requestQueue.getYoungest(requestFilter);
    }

    /**
     * Returns the youngest request from the queue
     * If no request is available the method block until one request can be returned
     * The request queue is unchanged by this call
     * @return the oldest request or null
     */
    public Request blockingGetYoungest() {
        Request request = null;
        while ((request == null) && !requestQueue.isDestroyed()) {
            waitForRequest();
            request = requestQueue.getYoungest();
        }
        return request;
    }

    //
    // -- blockingRemoveOldests ---------------------------------------------------
    //

    /**
     * Blocks the calling thread until there is a request that can be accepted
     * be the given RequestFilter.
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param requestFilter the request filter that select the request to be returned
     * @return the oldest request found in the queue that is accepted by the filter.
     */
    public Request blockingRemoveOldest(RequestFilter requestFilter) {
        return blockingRemoveOldest(requestFilter, 0);
    }

    /**
     * Blocks the calling thread until there is a request that can be accepted
     * be the given RequestFilter, but tries to limit the time the thread is blocked to timeout.
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param requestFilter the request filter that select the request to be returned
     * @param timeout : for how long the thread can be blocked.
     * @return the oldest request found in the queue that is accepted by the filter.
     */
    public Request blockingRemoveOldest(RequestFilter requestFilter, long timeout) {
        return requestQueue.blockingRemoveOldest(requestFilter, timeout);
    }

    /**
     * Blocks the calling thread until there is a request of name methodName
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param methodName the name of the method to wait for
     * @return the oldest request of name methodName found in the queue.
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public Request blockingRemoveOldest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        return blockingRemoveOldest(new RequestFilterOnMethodName(methodName), 0);
    }

    /**
     * Blocks the calling thread until there is a request available
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @return the oldest request found in the queue.
     */
    public Request blockingRemoveOldest() {
        return blockingRemoveOldest(null, 0);
    }

    /**
     * Blocks the calling thread until there is a request available but try
     * to limit the time the thread is blocked to timeout.
     * Returns immediately if there is already one. The request returned is non
     * null if a request has been found during the given time.
     * @param timeout : for how long the thread can be blocked.
     * @return the oldest request found in the queue or null.
     */
    public Request blockingRemoveOldest(long timeout) {
        return blockingRemoveOldest(null, timeout);
    }

    //
    // -- blockingRemoveYoungests ---------------------------------------------------
    //

    /**
     * Blocks the calling thread until there is a request that can be accepted
     * be the given RequestFilter.
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param requestFilter the request filter that select the request to be returned
     * @return the youngest request found in the queue that is accepted by the filter.
     */
    public Request blockingRemoveYoungest(RequestFilter requestFilter) {
        return blockingRemoveYoungest(requestFilter, 0);
    }

    /**
     * Blocks the calling thread until there is a request that can be accepted
     * be the given RequestFilter, but tries
     * to limit the time the thread is blocked to the timeout.
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param requestFilter the request filter that select the request to be returned
     * @param timeout : for how long the thread can be blocked.
     * @return the youngest request found in the queue that is accepted by the filter.
     */
    public Request blockingRemoveYoungest(RequestFilter requestFilter, long timeout) {
        return requestQueue.blockingRemoveYoungest(requestFilter, timeout);
    }

    /**
     * Blocks the calling thread until there is a request of name methodName
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param methodName the name of the method to wait for
     * @return the youngest request of name methodName found in the queue.
     * @throws NoSuchMethodError if methodName is not a public method of the reified object.
     */
    public Request blockingRemoveYoungest(String methodName) {
        if (!this.body.checkMethod(methodName)) {
            throw new NoSuchMethodError(methodName + " is not defined in " +
                this.body.getReifiedObject().getClass().getName());
        }
        return blockingRemoveYoungest(new RequestFilterOnMethodName(methodName), 0);
    }

    /**
     * Blocks the calling thread until there is a request available
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @return the youngest request found in the queue.
     */
    public Request blockingRemoveYoungest() {
        return blockingRemoveYoungest(null, 0);
    }

    /**
     * Blocks the calling thread until there is a request available but try
     * to limit the time the thread is blocked to timeout.
     * Returns immediately if there is already one. The request returned is non
     * null if a request has been found during the given time.
     * @param timeout : for how long the thread can be blocked.
     * @return the youngest request found in the queue or null.
     */
    public Request blockingRemoveYoungest(long timeout) {
        return blockingRemoveYoungest(null, timeout);
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //

    /**
     * ServingRequestProcessor is a simple RequestProcessor that serves and removes
     * all requests accepted by a given RequestFilter
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     * @see RequestProcessor
     *
     */
    protected class ServingRequestProcessor implements RequestProcessor {

        /** the filter*/
        private RequestFilter selectorRequestFilter;

        public ServingRequestProcessor(RequestFilter selectorRequestFilter) {
            this.selectorRequestFilter = selectorRequestFilter;
        }

        /**
         * Processes the request and returns true if the request can be discarded
         * after processing.
         * @param request the request to process
         * @return true if the request can be discarded (removed from the
         * container it is stored), false if it has to be kept.
         */
        public int processRequest(Request request) {
            if (selectorRequestFilter.acceptRequest(request)) {
                return REMOVE_AND_SERVE;
            } else {
                return KEEP;
            }
        }
    } // end inner class ServingRequestProcessor

    /**
     * FlushingServeYoungestRequestProcessor is a RequestProcessor that serves
     * only the youngest request accepted by the given RequestFilter and removes
     * all other requests accepted by that same Filter
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     * @see RequestProcessor
     *
     */
    protected class FlushingServeYoungestRequestProcessor implements RequestProcessor {
        private RequestFilter selectorRequestFilter;
        private Request requestToServe;
        private int counter;
        private int numberOfRequests;

        public FlushingServeYoungestRequestProcessor(RequestFilter selectorRequestFilter) {
            this.selectorRequestFilter = selectorRequestFilter;
        }

        /**
         * Processes the request and returns true if the request can be discarded
         * after processing.
         * @param request the request to process
         * @return true if the request can be discarded (removed from the
         * container it is stored), false if it has to be kept.
         */
        public int processRequest(Request request) {
            if (counter == 0) {
                // first call
                numberOfRequests = requestQueue.size();
            }
            counter++;
            int shouldRemove;
            if (selectorRequestFilter.acceptRequest(request)) {
                requestToServe = request;
                shouldRemove = REMOVE;
            } else {
                shouldRemove = KEEP;
            }
            if ((counter == numberOfRequests) && (requestToServe != null)) {
                if (request == requestToServe) {
                    return REMOVE_AND_SERVE; // serve current request
                } else {
                    body.serve(requestToServe); // serve an already removed request
                }
            }
            return shouldRemove;
        }
    } // end inner class FlushingServeYoungestRequestProcessor

    /**
     * FlushingServeOldestRequestProcessor is a RequestProcessor that serves
     * only the oldest request accepted by the given RequestFilter and removes
     * all other requests accepted by that same Filter
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     * @see RequestProcessor
     *
     */
    protected class FlushingServeOldestRequestProcessor implements RequestProcessor {
        private RequestFilter selectorRequestFilter;
        private boolean hasServed;

        public FlushingServeOldestRequestProcessor(RequestFilter selectorRequestFilter) {
            this.selectorRequestFilter = selectorRequestFilter;
        }

        /**
         * Processes the request and returns true if the request can be discarded
         * after processing.
         * @param request the request to process
         * @return true if the request can be discarded (removed from the
         * container it is stored), false if it has to be kept.
         */
        public int processRequest(Request request) {
            if (selectorRequestFilter.acceptRequest(request)) {
                if (!hasServed) {
                    hasServed = true;
                    return REMOVE_AND_SERVE;
                }
                return REMOVE;
            } else {
                return KEEP;
            }
        }
    } // end inner class FlushingServeYoungestRequestProcessor

    /**
     * RequestFilterOnMethodName is a RequestFilter that matches
     * only request of a given method name.
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     * @see RequestFilter
     *
     */
    protected class RequestFilterOnMethodName implements RequestFilter, java.io.Serializable {
        private String methodName;

        public RequestFilterOnMethodName(String methodName) {
            this.methodName = methodName;
        }

        public boolean acceptRequest(Request request) {
            return methodName.equals(request.getMethodName());
        }
    }

    /**
     * AcceptAllRequestFilter is a RequestFilter that matches any request
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     * @see RequestFilter
     *
     */
    protected class AcceptAllRequestFilter implements RequestFilter {

        /**
         * Returns true if and only if the given request can be accepted.
         * @param request the request to test
         * @return true if the request can be accepted, false else.
         */
        public boolean acceptRequest(Request request) {
            return true;
        }
    } // end inner class AcceptAllRequestFilter
}
