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
package org.objectweb.proactive.core.body.future;

/**
 * <p>
 * <code>FutureList</code> is an object used to monitor a subset of all the
 * futures waited by an active object. A user can simply add or remove
 * <code>Future</code> objects from this list and then call methods to test for
 * their availability.
 * </p><p>
 * To create a FutureList we need to have the FuturePool of the
 * active object since in the last resort it is the one which deals
 * with updating the futures.
 * </p><p>
 * Future Objects to be watched after are added and removed to this list by the user.
 * This class is not thread safe
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class FutureList {

  private java.util.Vector futureList;
  private FuturePool futurePool;


  public FutureList(FuturePool p) {
    futureList = new java.util.Vector();
    this.futurePool = p;
  }


  /**
   * Add the future to the futureList
   * This method does not test if the future is already in the list.
   */
  public boolean add(Object o) {
    //	System.out.println("Adding future " + o);
    return this.futureList.add(o);
  }


  /**
   * Remove the object from the FutureList
   * Return true if successfull
   */
  public boolean remove(Object o) {
    //	System.out.println("Trying to remove " + o);
    return this.futureList.remove(o);
  }


  /**
   * Return true if all the futures in the current list are awaited
   */
  public boolean allAwaited() {
    boolean value = true;
    synchronized (futurePool) {
      for (int i = 0; i < futureList.size(); i++) {
        value = value && FutureProxy.isAwaited(futureList.elementAt(i));
      }
    }
    return value;
  }


  /**
   * Return true if none of the futures in the current list are awaited
   */
  public boolean noneAwaited() {
    synchronized (futurePool) {
      for (int i = 0; i < futureList.size(); i++) {
        if (FutureProxy.isAwaited(futureList.elementAt(i))) {
          return false;
        }
      }
    }
    return true;
  }


  public int size() {
    return futureList.size();
  }


  /**
   * Retunr the number of currently awaited futures in this list
   */
  public int countAwaited() {
    int count = 0;
    synchronized (futurePool) {
      for (int i = 0; i < futureList.size(); i++) {
        if (FutureProxy.isAwaited(futureList.elementAt(i))) {
          count++;
        }
      }
    }
    return count;
  }


  /**
   * Returns a future available in this list.
   * Returns null if none is available.
   */
  public Object getOne() {
    synchronized (futurePool) {
      if (this.countAwaited() == this.size()) {
        //System.out.println("still waiting " + this.countAwaited()+ " futures");
        //futurePool.waitForReply();
        return null;
      } else {
        Object temp;
        for (int i = 0; i < futureList.size(); i++) {
          temp = futureList.elementAt(i);
          if (! FutureProxy.isAwaited(temp)) {
            return temp;
          }
        }
        return null;
      }
    }
  }


  /**
   * Removes and returns a future available this list.
   * Returns null if none is available.
   */
  public Object removeOne() {
    synchronized (futurePool) {
      Object tmp;
      tmp = this.getOne();
      if (tmp != null) {
        //	System.out.println("Removing future "  + tmp);
        //System.out.println("Result is " + this.remove(tmp));
        this.remove(tmp);
      }
      return tmp;
    }
  }


  public Object waitAndGetOne() {
    synchronized (futurePool) {
      this.waitOne();
      return this.getOne();
    }
  }


  public Object waitAndRemoveOne() {
    synchronized (futurePool) {
      this.waitOne();
      return this.removeOne();
    }
  }


  public void waitAll() {
    synchronized (futurePool) {
      while (this.countAwaited() > 0) {
        //	System.out.println("still waiting " + this.countAwaited()+ " futures");
        futurePool.waitForReply();
      }
    }
  }


  public void waitOne() {
    synchronized (futurePool) {
      //int initialCount = this.countAwaited();
      while (this.countAwaited() == this.size()) {
        //System.out.println("still waiting " + this.countAwaited()+ " futures");
        futurePool.waitForReply();
      }
    }
  }
}
