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
package org.objectweb.proactive;

import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.body.request.RequestProcessor;


/**
 * <P>
 * Service is a utility class that provides many useful methods to serve requests.
 * It is usually instantiated once at the begining of the runActivity() method of 
 * an active object in order to be used for serving requests. For instance :
 * </P><P>
 * <pre>
 * public void runActivity(org.objectweb.proactive.Body body) {
 *   org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
 * ...
 * }
 * </pre>
 * For intance the live method of the bounded buffer example :
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
public class Service {
  
  //
  // -- PROTECTED MEMBERS -----------------------------------------------
  //
  
  protected Body body;
  protected BlockingRequestQueue requestQueue;


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  /**
   * Creates a new intance of Service based on the given body.
   * @param body the body that helper service is for.
   */
  public Service(Body body) {
    this.body = body;
    this.requestQueue = body.getRequestQueue();
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //


  // -- Serve Oldest ---------------------------------------------------

  /**
   * Serves the oldest request in the request queue. 
   * The method blocks if there is no request until one request is 
   * received or until the body terminates.
   */
  public void blockingServeOldest() {
    body.serve(requestQueue.blockingRemoveOldest());
  }


  /**
   * Serves the oldest request in request queue. 
   * The method blocks if there is no request until one request is 
   * received or until the body terminates. The method does not block
   * more than the given timeout.
   * @param timeout how long the thread can be blocked for.
   */
  public void blockingServeOldest(long timeout) {
    body.serve(requestQueue.blockingRemoveOldest(timeout));
  }


  /**
   * Serves the oldest request for a method of name <code>methodName</code>. 
   * The method blocks if there is no matching request until one
   * matching request is received or until the body terminates.
   * @param methodName The name of the request to serve
   */
  public void blockingServeOldest(String methodName) {
    body.serve(requestQueue.blockingRemoveOldest(methodName));
  }


  /**
   * Serves the oldest request matching the criteria given be the filter.
   * The method blocks if there is no matching request until one
   * matching request is received or until the body terminates.
   * @param requestFilter The request filter accepting the request
   */
  public void blockingServeOldest(RequestFilter requestFilter) {
    body.serve(requestQueue.blockingRemoveOldest(requestFilter));
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
   */
  public void serveOldest(String methodName) {
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
    body.serve(requestQueue.blockingRemoveYoungest());
  }


  /**
   * Serves the youngest request in request queue. 
   * The method blocks if there is no request until one request is 
   * received or until the body terminates. The method does not block
   * more than the given timeout.
   * @param timeout how long the thread can be blocked for.
   */
  public void blockingServeYoungest(long timeout) {
    body.serve(requestQueue.blockingRemoveYoungest(timeout));
  }


  /**
   * Serves the youngest request for a method of name <code>methodName</code>. 
   * The method blocks if there is no matching request until one
   * matching request is received or until the body terminates.
   * @param methodName The name of the request to serve
   */
  public void blockingServeYoungest(String methodName) {
    body.serve(requestQueue.blockingRemoveYoungest(methodName));
  }


  /**
   * Serves the youngest request matching the criteria given be the filter.
   * The method blocks if there is no matching request until one
   * matching request is received or until the body terminates.
   * @param requestFilter The request filter accepting the request
   */
  public void blockingServeYoungest(RequestFilter requestFilter) {
    body.serve(requestQueue.blockingRemoveYoungest(requestFilter));
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
   */
  public void serveYoungest(String methodName) {
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
   */
  public void serveAll(String methodName) {
    serveAll(new RequestFilterOnMethodName(methodName));
  }


  /**
   * Serves all requests accepted by the given filter. 
   * All served requests are removed from the RequestQueue.
   * @param requestFilter The request filter accepting the request
   */
  public void serveAll(RequestFilter requestFilter) {
    requestQueue.processRequests(new ServingRequestProcessor(requestFilter));
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
   */
  public void flushingServeYoungest(String methodName) {
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
    requestQueue.processRequests(new FlushingServeYoungestRequestProcessor(requestFilter));
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
   */
  public void flushingServeOldest(String methodName) {
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
    requestQueue.processRequests(new FlushingServeOldestRequestProcessor(requestFilter));
  }



  // -- Other helpers methods ---------------------------------------------------

  /**
   * blocks until a request is available or until the body terminate
   */
  public void waitForRequest() {
    requestQueue.waitForRequest();
  }

  
  /**
   * true if and only if at least one request is available
   * @return true if a request is available, false else.
   */
  public boolean hasRequestToServe() {
    return ! requestQueue.isEmpty();
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
  


  // -- getOldests ---------------------------------------------------

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
   */
  public Request getOldest(String methodName) {
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



  // -- getYoungests ---------------------------------------------------

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
   */
  public Request getYoungest(String methodName) {
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
    public boolean processRequest(Request request) {
      if (selectorRequestFilter.acceptRequest(request)) {
        body.serve(request);
        return true;  
      } else {
        return false;
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
    public boolean processRequest(Request request) {
      if (counter == 0) {
        // first call
        numberOfRequests = requestQueue.size();
      }
      counter++;
      boolean shouldRemove;
      if (selectorRequestFilter.acceptRequest(request)) {
        requestToServe = request;
        shouldRemove = true;
      } else {
        shouldRemove = false;
      }
      if (counter == numberOfRequests && requestToServe != null) {
        body.serve(requestToServe);
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
    public boolean processRequest(Request request) {
      if (selectorRequestFilter.acceptRequest(request)) {
        if (! hasServed) {
          body.serve(request);
          hasServed = true;
        }
        return true;
      } else {
        return false;
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
  protected class RequestFilterOnMethodName implements RequestFilter {
    private String methodName;
    public RequestFilterOnMethodName(String methodName) {
      this.methodName = methodName;
    }
    /**
     * Returns true if and only if the given request can be accepted.
     * @param request the request to test
     * @return true if the request can be accepted, false else.
     */
    public boolean acceptRequest(Request request) {
      return methodName.equals(request.getMethodName());
    }
  } // end inner class RequestFilterOnMethodName



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
