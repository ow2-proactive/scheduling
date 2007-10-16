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
package org.objectweb.proactive.core.body.request;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.event.*;
import org.objectweb.proactive.core.util.CircularArrayList;


public class RequestQueueImpl extends AbstractEventProducer implements java.io.Serializable,
    RequestQueue {
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected CircularArrayList requestQueue;
    protected UniqueID ownerID;
    protected RequestFilterOnMethodName requestFilterOnMethodName;
    protected static final boolean SEND_ADD_REMOVE_EVENT = false;
    protected NonFunctionalRequestsProcessor nfRequestsProcessor;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RequestQueueImpl(UniqueID ownerID) {
        this.requestQueue = new CircularArrayList(20);
        this.ownerID = ownerID;
        this.requestFilterOnMethodName = new RequestFilterOnMethodName();
        this.nfRequestsProcessor = new NonFunctionalRequestsProcessor();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public java.util.Iterator iterator() {
        return requestQueue.iterator();
    }

    public CircularArrayList getInternalQueue() {
        return this.requestQueue;
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
        if (requestQueue.isEmpty()) {
            return null;
            //serves the non functional requests first.
        } else if (!nfRequestsProcessor.isEmpty()) {
            return nfRequestsProcessor.getOldestPriorityNFRequest(false);
        }
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
        if (requestQueue.isEmpty()) {
            return null;
        } else if (!nfRequestsProcessor.isEmpty()) {
            Request r = nfRequestsProcessor.getOldestPriorityNFRequest(true);
            requestQueue.remove(r);
            return r;
        }

        Request r = (Request) requestQueue.remove(0);

        // ProActiveEvent
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID,
                    RequestQueueEvent.REMOVE_REQUEST));
        }

        // END ProActiveEvent
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
        if (requestQueue.isEmpty()) {
            return null;
        } else if (!nfRequestsProcessor.isEmpty()) {
            return nfRequestsProcessor.getYoungestPriorityNFRequest(false);
        }
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
        if (requestQueue.isEmpty()) {
            return null;
        } else if (!nfRequestsProcessor.isEmpty()) {
            Request r = nfRequestsProcessor.getYoungestPriorityNFRequest(true);
            requestQueue.remove(r);
            return r;
        }
        Request r = (Request) requestQueue.remove(requestQueue.size() - 1);

        // ProActiveEvent
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID,
                    RequestQueueEvent.REMOVE_REQUEST));
        }

        // END ProActiveEvent
        return r;
    }

    public synchronized Request removeYoungest(String methodName) {
        requestFilterOnMethodName.setMethodName(methodName);
        return findYoungest(requestFilterOnMethodName, true);
    }

    public synchronized Request removeYoungest(RequestFilter requestFilter) {
        return findYoungest(requestFilter, true);
    }

    public synchronized int add(Request request) {
        //System.out.println("  --> RequestQueue.add m="+request.getMethodName());
        // FAULT-TOLERANCE  
        int ftres = FTManager.NON_FT;
        FTManager ftm = request.getFTManager();
        if (ftm != null) {
            // null if FT is disable OR if request is an awaited request         
            ftres = ftm.onDeliverRequest(request);
            if (request.ignoreIt()) {
                return ftres;
            }
        }

        //if the request is non functional and priority, a reference on it is added in a nonFunctionalRequestsQueue.
        int priority = request.getNFRequestPriority();
        if ((priority == Request.NFREQUEST_IMMEDIATE_PRIORITY) ||
                (priority == Request.NFREQUEST_PRIORITY)) {
            nfRequestsProcessor.addToNFRequestsQueue(request);
        }

        requestQueue.add(request);

        // ProActiveEvent
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID,
                    RequestQueueEvent.ADD_REQUEST));
        }

        // END ProActiveEvent
        return ftres;
    }

    public synchronized int addToFront(Request request) {
        int ftres = 0;

        //if the request is non functional and priority, a reference on it is added in a nonFunctionalRequestsQueue.
        int priority = request.getNFRequestPriority();
        if ((priority == Request.NFREQUEST_IMMEDIATE_PRIORITY) ||
                (priority == Request.NFREQUEST_PRIORITY)) {
            nfRequestsProcessor.addToNFRequestsQueue(request);
        }

        requestQueue.add(0, request);

        // ProActiveEvent
        if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
            notifyAllListeners(new RequestQueueEvent(ownerID,
                    RequestQueueEvent.ADD_REQUEST));
        }

        // END ProActiveEvent
        return ftres;
    }

    public synchronized void processRequests(RequestProcessor processor,
        Body body) {
        for (int i = 0; i < requestQueue.size(); i++) {
            Request r;

            // First, we deal with priotity non functional requests
            while (!nfRequestsProcessor.isEmpty()) {
                r = nfRequestsProcessor.getOldestPriorityNFRequest(true);
                LocalBodyStore.getInstance().getLocalBody(ownerID).serve(r);
                requestQueue.remove(r);
            }
            if (requestQueue.isEmpty()) {
                return;
            }

            r = (Request) requestQueue.get(i);
            int result = processor.processRequest(r);
            switch (result) {
            case RequestProcessor.REMOVE_AND_SERVE:
                requestQueue.remove(i);
                i--;

                // ProActiveEvent
                if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
                    notifyAllListeners(new RequestQueueEvent(ownerID,
                            RequestQueueEvent.REMOVE_REQUEST));
                }
                // END ProActiveEvent
                body.serve(r);
                break;
            case RequestProcessor.REMOVE:
                requestQueue.remove(i);
                i--;
                if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
                    notifyAllListeners(new RequestQueueEvent(ownerID,
                            RequestQueueEvent.REMOVE_REQUEST));
                }
                break;
            case RequestProcessor.KEEP:
                break;
            }
        }
    }

    @Override
    public synchronized String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--- RequestQueueImpl n=").append(requestQueue.size())
          .append("   requests --- ->\n");
        int count = 0;
        java.util.Iterator iterator = requestQueue.iterator();
        while (iterator.hasNext()) {
            Request currentrequest = (Request) iterator.next();
            sb.append(count).append("--> ")
              .append(currentrequest.getMethodName()).append("\n");
            count++;
        }
        sb.append("--- End RequestQueueImpl ---");
        sb.append("\n" + nfRequestsProcessor.toString());
        return sb.toString();
    }

    public void addRequestQueueEventListener(RequestQueueEventListener listener) {
        addListener(listener);
    }

    public void removeRequestQueueEventListener(
        RequestQueueEventListener listener) {
        removeListener(listener);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected void notifyOneListener(ProActiveListener listener,
        ProActiveEvent event) {
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
        java.util.Iterator iterator;
        Request r;

        //First, we deal with priority non functional requests
        if (shouldRemove) {
            while (!nfRequestsProcessor.isEmpty()) {
                r = nfRequestsProcessor.getOldestPriorityNFRequest(true);
                LocalBodyStore.getInstance().getLocalBody(ownerID).serve(r);
                requestQueue.remove(r);
            }
        }

        iterator = requestQueue.iterator();
        //then we look for the oldest request fullfilling the criteria defined by the given filter
        while (iterator.hasNext()) {
            r = (Request) iterator.next();
            if (requestFilter.acceptRequest(r)) {
                if (shouldRemove) {
                    iterator.remove();

                    // ProActiveEvent
                    if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
                        notifyAllListeners(new RequestQueueEvent(ownerID,
                                RequestQueueEvent.REMOVE_REQUEST));
                    }

                    // END ProActiveEvent
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
    private Request findYoungest(RequestFilter requestFilter,
        boolean shouldRemove) {
        Request r;

        //First, we deal with priotity non functional requests
        while (!nfRequestsProcessor.isEmpty()) {
            r = nfRequestsProcessor.getYoungestPriorityNFRequest(true);
            LocalBodyStore.getInstance().getLocalBody(ownerID).serve(r);
            requestQueue.remove(r);
        }

        java.util.ListIterator iterator = requestQueue.listIterator(requestQueue.size());
        while (iterator.hasPrevious()) {
            r = (Request) iterator.previous();
            if (requestFilter.acceptRequest(r)) {
                if (shouldRemove) {
                    iterator.remove();

                    // ProActiveEvent
                    if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
                        notifyAllListeners(new RequestQueueEvent(ownerID,
                                RequestQueueEvent.REMOVE_REQUEST));
                    }

                    // END ProActiveEvent
                }
                return r;
            }
        }
        return null;
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    protected class RequestFilterOnMethodName implements RequestFilter,
        java.io.Serializable {
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
