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

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.event.RequestQueueEvent;

public class BlockingRequestQueueImpl extends RequestQueueImpl implements java.io.Serializable,BlockingRequestQueue {

  //
  // -- PROTECTED MEMBERS -----------------------------------------------
  //
  
  protected boolean shouldWait;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public BlockingRequestQueueImpl(UniqueID ownerID) {
    super(ownerID);
    shouldWait = true;
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
 
  public synchronized void destroy() {
    super.clear();
    shouldWait = false;
    notifyAll();
  }

  public synchronized void add(Request r) {
    super.add(r);
    this.notifyAll();
  }

  public synchronized void addToFront(Request r) {
    super.addToFront(r);
    this.notifyAll();
  }

  public synchronized Request blockingRemoveOldest(RequestFilter requestFilter) {
    return blockingRemove(requestFilter, true);
  }

  public synchronized Request blockingRemoveOldest(String methodName) {
    return blockingRemove(methodName, true);  }

  public synchronized Request blockingRemoveOldest() {
    return blockingRemove(true);
  }

  public synchronized Request blockingRemoveOldest(long timeout) {
    return blockingRemove(timeout, true);
  }

  public synchronized Request blockingRemoveYoungest(RequestFilter requestFilter) {
    return blockingRemove(requestFilter, false);
  }

  public synchronized Request blockingRemoveYoungest(String methodName) {
    return blockingRemove(methodName, false);
  }

  public synchronized Request blockingRemoveYoungest() {
    return blockingRemove(false);
  }

  public synchronized Request blockingRemoveYoungest(long timeout) {
    return blockingRemove(timeout, false);
  }

  public synchronized void waitForRequest() {
    while (isEmpty() && shouldWait) {
      if (hasListeners()) {
        notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.WAIT_FOR_REQUEST));
      }
      try {
        this.wait();
      } catch (InterruptedException e) {}
    }
  }



  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  protected Request blockingRemove(RequestFilter requestFilter, boolean oldest) {
    Request r = oldest ? removeOldest(requestFilter) : removeYoungest(requestFilter);
    while (r == null && shouldWait) {
      if (hasListeners()) 
        notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.WAIT_FOR_REQUEST));
      try {
        this.wait();
      } catch (InterruptedException e) {}
      r = oldest ? removeOldest(requestFilter) : removeYoungest(requestFilter);
    }
    return r;
  }


  /**
   * Blocks the calling thread until there is a request of name methodName
   * Returns immediately if there is already one. The request returned is non 
   * null unless the thread has been asked not to wait anymore.
   * @param methodName the name of the method to wait for
   * @param oldest true if the request to remove is the oldest, false for the youngest
   * @return the request of name methodName found in the queue.
   */
  protected Request blockingRemove(String methodName, boolean oldest) {
    Request r = oldest ? removeOldest(methodName) : removeYoungest(methodName);
    while (r == null && shouldWait) {
      if (hasListeners()) 
        notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.WAIT_FOR_REQUEST));
      try {
        this.wait();
      } catch (InterruptedException e) {}
      r = oldest ? removeOldest(methodName) : removeYoungest(methodName);
    }
    return r;
  }


  /**
   * Blocks the calling thread until there is a request available
   * Returns immediately if there is already one. The request returned is non 
   * null unless the thread has been asked not to wait anymore.
   * @param oldest true if the request to remove is the oldest, false for the youngest
   * @return the request found in the queue.
   */
  protected Request blockingRemove(boolean oldest) {
    while (isEmpty() && shouldWait) {
      if (hasListeners()) {
        notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.WAIT_FOR_REQUEST));
      }
      try {
        this.wait();
      } catch (InterruptedException e) {}
    }
    return oldest ? removeOldest() : removeYoungest();
  }


  /**
   * Blocks the calling thread until there is a request available but try 
   * to limit the time the thread is blocked to timeout.
   * Returns immediately if there is already one. The request returned is non 
   * null if a request has been found during the given time.
   * @param timeout the maximum time to wait
   * @param oldest true if the request to remove is the oldest, false for the youngest
   * @return the request found in the queue or null.
   */
  protected Request blockingRemove(long timeout, boolean oldest) {
    long timeStartWaiting = System.currentTimeMillis();
    while (isEmpty() && shouldWait) {
      if (hasListeners()) {
        notifyAllListeners(new RequestQueueEvent(ownerID, RequestQueueEvent.WAIT_FOR_REQUEST));
      }
      try {
        this.wait(timeout);
      } catch (InterruptedException e) {}
      if (System.currentTimeMillis() - timeStartWaiting > timeout) {
        return oldest ? removeOldest() : removeYoungest();
      }
    }
    return oldest ? removeOldest() : removeYoungest();
  }


}
