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

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.event.RequestQueueEvent;
import org.objectweb.proactive.core.group.spmd.BarrierState;
import org.objectweb.proactive.core.group.spmd.MethodCallBarrier;

public class BlockingRequestQueueImpl extends RequestQueueImpl implements java.io.Serializable,BlockingRequestQueue {

	public static Logger logger = Logger.getLogger(BlockingRequestQueueImpl.class.getName());

  //
  // -- PROTECTED MEMBERS -----------------------------------------------
  //
  
  protected boolean shouldWait;
  
  private transient UniversalBody body = null;
  private boolean firstBarrierCallEncountered = false;
  private boolean suspended = false;
  private int awaitedBarrierCall = 0;
  private HashMap currentBarriers = new HashMap();
  
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
  
  public synchronized boolean isDestroyed() {
    return ! shouldWait;
  }

//  public synchronized void add(Request r) {
//    super.add(r);
//    if (logger.isDebugEnabled()) {
//    	logger.debug("Adding request " + r.getMethodName());
//    }
//	if (r.getMethodCall() instanceof MethodCallBarrier) {
//		System.out.println("Je recois un appel barrier !");
//		if (!this.suspended) {
//			System.out.println("Premiere barriere");
//			this.firstBarrierCallEncountered = true;
//			this.suspended = true;
//			this.awaitedBarrierCall = ((AbstractBody) this.body).getSPMDGroupSize();
//			System.out.println("   this.firstBarrierCallEncountered = " + this.firstBarrierCallEncountered);
//			System.out.println("   this.suspended = " + this.suspended);
//			System.out.println("   this.awaitedBarrierCall = " + this.awaitedBarrierCall);
//		}
//		else {
//			this.awaitedBarrierCall--;
//			System.out.println("Nouvelle barriere");
//			System.out.println("   this.firstBarrierCallEncountered = " + this.firstBarrierCallEncountered);
//			System.out.println("   this.suspended = " + this.suspended);
//			System.out.println("   this.awaitedBarrierCall = " + this.awaitedBarrierCall);
//			if (this.awaitedBarrierCall == 0) {
//				this.firstBarrierCallEncountered = false;
//				this.suspended = false;
//				System.out.println("Fin de la barriere");
//			}
//		}
//	}
//    this.notifyAll();
//  }

//	public synchronized void add(Request r) {
//	  super.add(r);
//	  logger.debug("Adding request " + r.getMethodName());
//
//	  // if barrier call => special treatement
//	  if (r.getMethodCall() instanceof MethodCallBarrier) {
//
//		System.out.println("    BARRIER CALL\n          Source: " + r.getSourceBodyID() + "\n          Body:   " + this.body.getID());
//
//
//	  	  MethodCallBarrier mcb = (MethodCallBarrier)r.getMethodCall();
//		  //System.out.println("Je recois un appel barrier : \"" + mcb.getIDName() + "\" !");
//		  // search the state of the barrier for the barrier ID name
//		  BarrierState bs = (BarrierState) this.currentBarriers.get(mcb.getIDName());
//		  // bs == null  =>  state not found  =>  first barrier encountered for ID name
//		  if (bs == null) { 
//			  System.out.println("Premiere barriere : \"" + mcb.getIDName() + "\" !");
//		  	  // build and add infos about new barrier
//		  	  bs = new BarrierState(((AbstractBody) this.body).getSPMDGroupSize()-1);
//		  	  this.currentBarriers.put(mcb.getIDName(), bs);
//			  // if this object is the sender of the barrier : tag and suspend
//			  if (r.getSourceBodyID().equals(this.body.getID())) {
//				System.out.println("C'est MON appel barrier \"" + mcb.getIDName() + "\" : je bloque !");
//				bs.tagLocalyCalled();
//				this.suspended = true;
//			  }
//		  }
//		  // bs != null  =>  state found  =>  this is not the first call to this barrier
//		  else {
//				// check if local call
//				if (r.getSourceBodyID().equals(this.body.getID())) {
//					bs.tagLocalyCalled();
//				}
//		  		int calls = bs.getAwaitedCalls() -1;
//				// if there is others waiting calls, decrement
//		  		if (calls != 0) {
//					System.out.println("Barriere \"" + mcb.getIDName() + "\"  (" + (bs.getAwaitedCalls()-1) + ") toujours attendues");
//			  		bs.decrementAwaitedCalls();
//		  		}
//		  		// calls == 0  =>  this is the last awaited call to this barrier 
//		  		else {
//		  			// if there is no other barrier in action  =>  resume
//					boolean resume = true;
//		  			Iterator it = this.currentBarriers.values().iterator();
//		  			while (it.hasNext() && resume) {
//		  				resume = ! ((BarrierState) it.next()).isLocalyCalled();
//		  			}
//		  			if (resume) {
//						System.out.println("Fin de la barriere \"" + mcb.getIDName() + "\"");
//			  			this.suspended = false;
//					}
//		  		}
//		  }
//	  }
//	  this.notifyAll();
//	}

	public synchronized void add(Request r) {
	  super.add(r);
	  logger.debug("Adding request " + r.getMethodName());

	  // if barrier call => special treatement
	  if (r.getMethodCall() instanceof MethodCallBarrier) {
		  //System.out.println("    BARRIER CALL\n          Source: " + r.getSourceBodyID() + "\n          Body:   " + this.body.getID());
		  MethodCallBarrier mcb = (MethodCallBarrier)r.getMethodCall();
		  //System.out.println("Je recois un appel barrier : \"" + mcb.getIDName() + "\" !");
		  // search the state of the barrier for the barrier ID name
		  BarrierState bs = (BarrierState) this.currentBarriers.get(mcb.getIDName());
		  // bs == null  =>  state not found  =>  first barrier encountered for ID name
		  if (bs == null) { 
			  //System.out.println("First barrier \"" + mcb.getIDName() + "\" encountered !");
			  // build and add infos about new barrier
			  bs = new BarrierState(((AbstractBody) this.body).getSPMDGroupSize());
			  this.currentBarriers.put(mcb.getIDName(), bs);
		  }
		  // if this object is the sender of the barrier : tag and suspend
		  if (r.getSourceBodyID().equals(this.body.getID())) {
				//System.out.println("This is MY call to barrier \"" + mcb.getIDName() + "\" : I stop !");
				bs.tagLocalyCalled();
				this.suspended = true;
		  }
		  int calls = bs.getAwaitedCalls() -1;
		  // if there is others waiting calls, decrement
		  if (calls != 0) {
			  //System.out.println("Barrier \"" + mcb.getIDName() + "\"  (" + (bs.getAwaitedCalls()-1) + ") still awaited");
			  bs.decrementAwaitedCalls();
		  }
		  // calls == 0  =>  this is the last awaited call to this barrier 
		  else {
		  	  this.currentBarriers.remove(mcb.getIDName());
			  // if there is no other barrier in action  =>  resume
			  //System.out.println("End of  barrier \"" + mcb.getIDName() + "\"");
			  this.suspended = false;
		  }
	  }
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
	if (this.body == null) {
		this.body = ProActive.getBodyOnThis();
	}
	return this.barrierBlockingRemove();
	//return blockingRemove(true);
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



  /**
   * Blocks the calling thread until there is a request available
   * Returns immediately if there is already one. The request returned is non
   * null unless the thread has been asked not to wait anymore.
   * @return the request found in the queue.
   */
  protected Request barrierBlockingRemove() {
	  while ((this.isEmpty() && this.shouldWait) || this.suspended) {
		  if (this.hasListeners()) {
			  this.notifyAllListeners(new RequestQueueEvent(this.ownerID, RequestQueueEvent.WAIT_FOR_REQUEST));
		  }
		  try {
			  this.wait(); }
		  catch (InterruptedException e) { }
	  }
	  return this.barrierRemoveOldest();
  }


	/**
	 * 
	 * @return
	 */
  public synchronized Request barrierRemoveOldest() {
	  if (this.requestQueue.isEmpty()) {
	  	return null;
	  } 
	  Request r = (Request) requestQueue.remove(0);
//	  if (r.getMethodCall() instanceof MethodCallBarrier) {
//		  r = (Request) requestQueue.remove(0);
//	  }
	  if (SEND_ADD_REMOVE_EVENT && hasListeners()) {
		  this.notifyAllListeners(new RequestQueueEvent(this.ownerID, RequestQueueEvent.REMOVE_REQUEST));
	  }
	  return r;
  }



}
