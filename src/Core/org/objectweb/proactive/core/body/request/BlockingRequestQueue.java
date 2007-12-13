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

public interface BlockingRequestQueue extends RequestQueue {
    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Destroys this BlockingQueue by removing all its content, unblocking all thread
     * waiting for a request and making sure that no thread will block again.
     * After this call, any call to a blocking method won't block and return null.
     */
    public void destroy();

    /**
     * Returns if this BlockingQueue is destroyed
     */
    public boolean isDestroyed();

    /**
     * Blocks the calling thread until there is a request that can be accepted
     * be the given RequestFilter.
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param requestFilter the request filter that select the request to be returned
     * @return the oldest request found in the queue that is accepted by the filter.
     */
    public Request blockingRemoveOldest(RequestFilter requestFilter);

    /**
     * Blocks the calling thread until there is a request of name methodName
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param methodName the name of the method to wait for
     * @return the oldest request of name methodName found in the queue.
     */
    public Request blockingRemoveOldest(String methodName);

    /**
     * Blocks the calling thread until there is a request available
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @return the oldest request found in the queue.
     */
    public Request blockingRemoveOldest();

    /**
     * Blocks the calling thread until there is a request available but try
     * to limit the time the thread is blocked to timeout.
     * Returns immediately if there is already one. The request returned is non
     * null if a request has been found during the given time.
     * @return the oldest request found in the queue or null.
     */
    public Request blockingRemoveOldest(long timeout);

    /**
     * Blocks the calling thread until there is a request that can be accepted
     * be the given RequestFilter but tries
     * to limit the time the thread is blocked to timeout.
     * Returns immediately if there is already one. The request returned is non
     * null if a request has been found during the given time.
     * @return the oldest request found in the queue or null.
     */
    public Request blockingRemoveOldest(RequestFilter requestFilter, long timeout);

    /**
     * Blocks the calling thread until there is a request that can be accepted
     * be the given RequestFilter.
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param requestFilter the request filter that select the request to be returned
     * @return the youngest request found in the queue that is accepted by the filter.
     */
    public Request blockingRemoveYoungest(RequestFilter requestFilter);

    /**
     * Blocks the calling thread until there is a request that can be accepted
     * be the given RequestFilter, but tries
     * to limit the time the thread is blocked to timeout.
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param requestFilter the request filter that select the request to be returned
     * @return the youngest request found in the queue that is accepted by the filter.
     */
    public Request blockingRemoveYoungest(RequestFilter requestFilter, long timeout);

    /**
     * Blocks the calling thread until there is a request of name methodName
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @param methodName the name of the method to wait for
     * @return the youngest request of name methodName found in the queue.
     */
    public Request blockingRemoveYoungest(String methodName);

    /**
     * Blocks the calling thread until there is a request available
     * Returns immediately if there is already one. The request returned is non
     * null unless the thread has been asked not to wait anymore.
     * @return the youngest request found in the queue.
     */
    public Request blockingRemoveYoungest();

    /**
     * Blocks the calling thread until there is a request available but try
     * to limit the time the thread is blocked to timeout.
     * Returns immediately if there is already one. The request returned is non
     * null if a request has been found during the given time.
     * @return the youngest request found in the queue or null.
     */
    public Request blockingRemoveYoungest(long timeout);

    /**
     * Check if the thread is currently waiting for a request
     * @return true iff the thread is waiting for a request
     */
    public boolean isWaitingForRequest();

    /**
     * Blocks the calling thread until there is a request available. The request
     * is not removed from the queue.
     * Returns immediately if there is already one.
     */
    public void waitForRequest(long timeout);

    /**
     * Blocks the service of requests.
     * Incoming requests are still added in queue.
     */
    public void suspend();

    /**
     * Resumes the service of requests.
     */
    public void resume();
}
