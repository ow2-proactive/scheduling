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

public class FuturePool extends Object implements java.io.Serializable {

  protected boolean newState;
  private java.util.HashMap futures;


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public FuturePool() {
    futures = new java.util.HashMap();
    this.newState = false;
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public int size() {
    return futures.size();
  }


  public synchronized Future get(long id) {
    return (Future) futures.get(new Long(id));
  }

/*
  public synchronized Future remove(long id) {
    Future temp = (Future) futures.remove(new Long(id));
    stateChange();
    return temp;
  }
  */
  
  public synchronized Future receiveResult(long id, Object result) throws java.io.IOException {
    Future future = (Future) futures.remove(new Long(id));
    if (future != null) {
      // Sets the result into the future
      future.receiveReply(result);
      stateChange();
    }
    return future;
  }


  public synchronized void put(long id, Future value) {
    futures.put(new Long(id), value);
    stateChange();
  }


  public synchronized void waitForReply() {
    this.newState = false;
    while (! newState) {
      try {
        wait();
      } catch (InterruptedException e) {}
    }

  }


  public synchronized void unsetMigrationTag() {
    java.util.Iterator iterator = futures.values().iterator();
    while (iterator.hasNext()) {
      FutureProxy p = (FutureProxy)iterator.next();
      p.unsetMigrationTag();
    }
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private void stateChange() {
    this.newState = true;
    notifyAll();
  }


  //
  // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
  //
  
  private synchronized void setMigrationTag() {
    java.util.Iterator iterator = futures.values().iterator();
    while (iterator.hasNext()) {
      FutureProxy p = (FutureProxy)iterator.next();
      p.setMigrationTag();
    }
  }


  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    setMigrationTag();
    out.defaultWriteObject();
  }


  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
    unsetMigrationTag();
  }
}
