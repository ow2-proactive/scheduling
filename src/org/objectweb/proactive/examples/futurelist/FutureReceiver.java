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
package org.objectweb.proactive.examples.futurelist;

import org.objectweb.proactive.core.body.future.FutureList;
import org.objectweb.proactive.core.body.future.FuturePool;

public class FutureReceiver implements java.io.Serializable {

  int etape = 0; // this is to count the jumps we have made so far
  BlockedObject blocked;
  java.util.Vector waitingFutures = new java.util.Vector();
  FutureList futureList;


  public FutureReceiver() {
  }


  public void getFuture() {
    //System.out.println("FutureReceiver: Now processing getFuture()");
    //we  create a future objet through this call since the callee is blocked
    waitingFutures.add(blocked.createFuture());
  }


  public void getFutureAndAddToFutureList() {
    //System.out.println("FutureReceiver: Now processing getFutureAndAddToFutureList()");
    EmptyFuture f = blocked.createFuture();
    waitingFutures.add(f);
    this.addToFutureList(f);
  }


  public void displayAllFutures() {
    EmptyFuture temp;

    for (java.util.Enumeration e = waitingFutures.elements(); e.hasMoreElements();) {
      temp = (EmptyFuture)e.nextElement();
      System.out.println("Result: " + temp.getName());
    }
  }

  //call a method on the blockedObject to unblock it
  public void unblockOtherObject() {
    blocked.go();
  }


  public void setBlockedObject(BlockedObject t) {
    blocked = t;
  }


  /**
   * Request its body futurePool and create an empty future list
   */
  public void createFutureList() {
    org.objectweb.proactive.core.body.ActiveBody b = (org.objectweb.proactive.core.body.ActiveBody) org.objectweb.proactive.ProActive.getBodyOnThis();
    FuturePool futurePool = b.getFuturePool();
    this.futureList = new FutureList(futurePool);
  }


  public void addToFutureList(Object o) {
    this.futureList.add(o);
  }


  public void displayAwaited() {
    if (futureList != null) {
      System.out.println("FutureReceiver: I am still waiting " + futureList.countAwaited() + " futures");
    }
  }


  public void displayAllAwaited() {
    if (futureList != null) {
      System.out.println("FutureReceiver: I am waiting for all my futures:  " + futureList.allAwaited());
    }
  }


  public void displayNoneAwaited() {
    if (futureList != null) {
      System.out.println("FutureReceiver: I don't have any pending future:  " + futureList.noneAwaited());
    }
  }


  public void waitAllFuture() {
    if (futureList != null) {
      System.out.println("FutureReceiver: waiting all futures  ");
      futureList.waitAll();
    }
  }


  public void waitOneFuture() {
    if (futureList != null) {
      System.out.println("FutureReceiver: waiting one future  ");
      futureList.waitOne();
    }
  }


  public void waitAndDisplayOneFuture() {
    Object tmp;
    if (futureList != null) {
      tmp = futureList.waitAndRemoveOne();
      System.out.println("I got this future: " + tmp);
    }
  }


  public void waitAndDisplayAllFuture() {
    if (futureList != null) {
      this.waitAllFuture();
      this.displayAllFutures();
      //	System.out.println("I got this future: "+tmp);
    }
  }

  //call a method on the other agent to unblock it and wait for its replies
  public void unblockOtherObjectAndWaitAll() {
    blocked.go();
    this.waitAllFuture();
  }


  public void unblockOtherObjectAndWaitOne() {
    blocked.go();
    this.waitOneFuture();
  }
}
