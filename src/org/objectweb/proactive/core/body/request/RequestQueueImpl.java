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
package org.objectweb.proactive.core.body.request;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.event.*;
import org.objectweb.proactive.core.util.CircularArrayList;

public class RequestQueueImpl extends AbstractEventProducer implements java.io.Serializable, RequestQueue {

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    protected CircularArrayList requestQueue;
    protected UniqueID ownerID;
    private RequestFilterOnMethodName requestFilterOnMethodName;

    protected static final boolean SEND_ADD_REMOVE_EVENT = false;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    public RequestQueueImpl(UniqueID ownerID) {
        this.requestQueue = new CircularArrayList(20);
        this.ownerID = ownerID;
        this.requestFilterOnMethodName = new RequestFilterOnMethodName();
    }


    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    public java.util.Iterator iterator() {
        return requestQueue.iterator();
    }


    public synchronized boolean isEmpty() {
        return requestQueue.isEmpty();
    }


    public synchronized int size() {
        return requestQueue.size();
    }


    public boolean hasRequest(String s) {
        return getOldest(s) != null;
    }


    public synchronized void clear() {
        requestQueue.clear();
    }

    public synchronized Request getOldest() {
        if (requestQueue.isEmpty()) return null;
        return (Request) requestQueue.get(0);
    }


    public synchronized Request getOldest(String methodName) {
        requestFilterOnMethodName.setMethodName(methodName);
        return findOldest(requestFilterOnMethodName, false);
    }

    public synchronized Request getOldest(RequestFilter requestFilter) {
        return findOldest(requestFilter, false);
    }

    public synchronized Request removeOldest() {
        if (requestQueue.isEmpty()) return null;
        Request r = (Request) requestQueue.remove(0);
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.REMOVE_REQUEST));
        }
        return r;
    }

    public synchronized Request removeOldest(String methodName) {
        requestFilterOnMethodName.setMethodName(methodName);
        return findOldest(requestFilterOnMethodName, true);
    }

    public synchronized Request removeOldest(RequestFilter requestFilter) {
        return findOldest(requestFilter, true);
    }

    public synchronized Request getYoungest() {
        if (requestQueue.isEmpty()) return null;
        return (Request) requestQueue.get(requestQueue.size() - 1);
    }

    public synchronized Request getYoungest(String methodName) {
        requestFilterOnMethodName.setMethodName(methodName);
        return findYoungest(requestFilterOnMethodName, false);
    }

    public synchronized Request getYoungest(RequestFilter requestFilter) {
        return findYoungest(requestFilter, false);
    }

    public synchronized Request removeYoungest() {
        if (requestQueue.isEmpty()) return null;
        Request r = (Request) requestQueue.remove(requestQueue.size() - 1);
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.REMOVE_REQUEST));
        }
        return r;
    }

    public synchronized Request removeYoungest(String methodName) {
        requestFilterOnMethodName.setMethodName(methodName);
        return findYoungest(requestFilterOnMethodName, true);
    }

    public synchronized Request removeYoungest(RequestFilter requestFilter) {
        return findYoungest(requestFilter, true);
    }

    public synchronized void add(Request request) {
      //System.out.println("  --> RequestQueue.add m="+request.getMethodName());
      requestQueue.add(request);
      if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
          notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.ADD_REQUEST));
      }
    }

    public synchronized void addToFront(Request request) {
        requestQueue.add(0, request);
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.ADD_REQUEST));
        }
    } 

    public synchronized void processRequests(RequestProcessor processor, Body body) {
	    for (int i = 0; i < requestQueue.size(); i++) {
            Request r = (Request) requestQueue.get(i);
            int result = processor.processRequest(r);
            switch (result) {
            	case RequestProcessor.REMOVE_AND_SERVE :
                    requestQueue.remove(i);
                    i --;
	                if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
	                    notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.REMOVE_REQUEST));
	                }
	   	            body.serve(r);
             	  break;
            	case RequestProcessor.REMOVE :
                    requestQueue.remove(i);
                    i --;
	                if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
	                    notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.REMOVE_REQUEST));
	                }
            	  break;
            	case RequestProcessor.KEEP :
            	  break;
            }
        }
    }

    public synchronized String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--- RequestQueueImpl n=").append(requestQueue.size()).append("   requests --- ->\n");
        int count = 0;
        java.util.Iterator iterator = requestQueue.iterator();
        while (iterator.hasNext()) {
            Request currentrequest = (Request) iterator.next();
            sb.append(count).append("--> ").append(currentrequest.getMethodName()).append("\n");
            count++;
        }
        sb.append("--- End RequestQueueImpl ---");
        return sb.toString();
    }


    public void addRequestQueueEventListener(RequestQueueEventListener listener) {
        addListener(listener);
    }


    public void removeRequestQueueEventListener(RequestQueueEventListener listener) {
        removeListener(listener);
    }


    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    protected void notifyOneListener(ProActiveListener listener, ProActiveEvent event) {
        ((RequestQueueEventListener) listener).requestQueueModified((RequestQueueEvent) event);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    /**
     * Return the oldest fullfilling the criteria defined by the
     * given filter or null if no match
     * The request is removed only if shouldRemove is set to true
     * @param methodName the name of the method to look for
     * @param shouldRemove whether to remove the request found or not
     * @return the oldest matching request or null
     */
    private Request findOldest(RequestFilter requestFilter, boolean shouldRemove) {
        java.util.Iterator iterator = requestQueue.iterator();
        while (iterator.hasNext()) {
            Request r = (Request) iterator.next();
            if (requestFilter.acceptRequest(r)) {
                if (shouldRemove) {
                    iterator.remove();
                    if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
                        notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.REMOVE_REQUEST));
                    }
                }
                return r;
            }
        }
        return null;
    }

    /**
     * Return the youngest request fullfilling the criteria defined by the
     * given filter or null if no match
     * The request is removed only if shouldRemove is set to true
     * @param methodName the name of the method to look for
     * @param shouldRemove whether to remove the request found or not
     * @return the youngest matching request or null
     */
    private Request findYoungest(RequestFilter requestFilter, boolean shouldRemove) {
        java.util.ListIterator iterator = requestQueue.listIterator(requestQueue.size());
        while (iterator.hasPrevious()) {
            Request r = (Request) iterator.previous();
            if (requestFilter.acceptRequest(r)) {
                if (shouldRemove) {
                    iterator.remove();
                    if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
                        notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.REMOVE_REQUEST));
                    }
                }
                return r;
            }
        }
        return null;
    }


	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		// we must set migration tag because requests could contain awaited future (parameters)
		org.objectweb.proactive.Body owner = LocalBodyStore.getInstance().getLocalBody(ownerID);
		owner.getFuturePool().setMigrationTag();
		out.defaultWriteObject();
	}


    //
    // -- INNER CLASSES -----------------------------------------------
    //

    private class RequestFilterOnMethodName implements RequestFilter, java.io.Serializable {

        private String methodName;

        public RequestFilterOnMethodName() {
        }

        public boolean acceptRequest(Request request) {
            return methodName.equals(request.getMethodName());
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
    }
}
