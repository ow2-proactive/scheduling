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
package org.objectweb.proactive.ic2d.data;

import org.objectweb.proactive.ic2d.event.ActiveObjectListener;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;

/**
 * Holder class for the host data representation
 */
public class ActiveObject extends AbstractDataObject {

  private static final int ACTIVE_OBJECT_CACHE_CLEANUP_INTERVAL = 300000; // 300 seconds

 /**
  * Every so often we cleanup the cache
  */
  private static long lastActiveObjectCacheCleanupTime = System.currentTimeMillis();

  protected UniqueID id;
  protected String className;
  protected String name;
    
  // false if waiting by necessity
  protected boolean isWaitingForRequest = true;
  
  // true if the activeObject is active
  protected boolean isActive;
  
  protected ActiveObjectListener listener;


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public ActiveObject(NodeObject parent, UniqueID id, String className, boolean isActive) {
    super(parent);
    this.className = className;
    this.isActive = isActive;
    name = shortClassName(className)+"#"+counter(id);
    this.id = id;
    //controller.log("ActiveObject "+className+" id="+id+" created.");
    if (isMonitoring()) monitoringMessageEventChanged(this, true);
  }
                           
                           
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //
  
  public String toString() {
    return "Object "+name+" ID#" + id + "\n" + 
           "  class :                    " + className                 + "\n" + 
           "  monitoring : RequestReceiver(" + monitoringRequestReceiver +
           "), RequestSender(" + monitoringRequestSender   +
           "), ReplyReceiver(" + monitoringReplyReceiver   +
           "), ReplyReceiver(" + monitoringReplySender     + ")\n";
  }


  public boolean migrateTo(String nodeTargetURL) {
    if (isDestroyed) return false;
    if (! isActive) return false;
    try {
      getTypedParent().getTypedParent().migrateTo(id, nodeTargetURL);
      if (controller != null) controller.log("Successfully migrated " + className + " to "+nodeTargetURL);
      return true;
    } catch (MigrationException e) {
      if (controller != null) controller.log("Couldn't migrate "+className+" to "+nodeTargetURL, e);
      return false;                         
    }
  }

  
  public boolean isInsideSameVM(ActiveObject o) {
    if (isDestroyed || o.isDestroyed) return false;
    return getTypedParent().isInsideSameVM(o.getTypedParent());
  }
 
  
  public void setWaitingForRequest(boolean value) {
    if (isDestroyed) return;
    if (value != isWaitingForRequest) {
      isWaitingForRequest = value;
      if (listener != null) listener.waitingStatusChanged(value);
    }
  }
  
  
  public void setIsActive(boolean value) {
    if (isDestroyed) return;
    
    System.out.println("setIsActive value="+value+" isActive="+isActive);

    if (value != isActive) {
      isActive = value;
      if (listener != null) listener.activeStatusChanged(value);
    }
  }


  public ActiveObject findActiveObjectById(UniqueID id) {
    if (id == this.id) 
      return this;
    else return null;  
  }


  public void destroyObject() {
    getTypedParent().removeActiveObject(id);
  }


  //
  // Event Listener
  //
    
  public void registerListener(ActiveObjectListener listener) {
    this.messageMonitoringListener = listener;
    this.listener = listener;
  }
  
  
  //
  // Accessor methods
  //
    
  public String getName() { 
    return name;
  }

  public String getClassName() { 
    return className;
  }

  public UniqueID getID() { 
    return id;
  }
  
  public boolean isActive() {
    return isActive;
  }
  

  //
  // -- implements MessageMonitoringController -----------------------------------------------
  //

  public void monitorRequestReceiver(boolean shouldMonitor) {
    if (isDestroyed) return;
    if (monitoringRequestReceiver == shouldMonitor) return; // already doing the right thing
    if (shouldMonitor) {
      controller.log("Starting monitoring of RequestReceiver for " + className);
    } else {
      controller.log("Stopping monitoring of RequestReceiver for " + className);
    }
    boolean isMonitoringBefore = isMonitoring();
    monitoringRequestReceiver = shouldMonitor;
    boolean isMonitoringAfter = isMonitoring();
    if (isMonitoringBefore != isMonitoringAfter) monitoringMessageEventChanged(this, isMonitoringAfter);
    if (listener != null) listener.monitoringRequestReceiverChanged(shouldMonitor);
  }


  public void monitorRequestSender(boolean shouldMonitor) {
    if (isDestroyed) return;
    if (monitoringRequestSender == shouldMonitor) return; // already doing the right thing
    if (shouldMonitor) {
      controller.log("Starting monitoring RequestSender for " + className);
    } else {
      controller.log("Stopping monitoring RequestSender for " + className);
    }
    boolean isMonitoringBefore = isMonitoring();
    monitoringRequestSender = shouldMonitor;
    boolean isMonitoringAfter = isMonitoring();
    if (isMonitoringBefore != isMonitoringAfter) monitoringMessageEventChanged(this, isMonitoringAfter);
    if (listener != null) listener.monitoringRequestSenderChanged(shouldMonitor);
  }


  public void monitorReplyReceiver(boolean shouldMonitor) {
    if (isDestroyed) return;
    if (monitoringReplyReceiver == shouldMonitor) return; // already doing the right thing
    if (shouldMonitor) {
      controller.log("Starting monitoring ReplyReceiver for " + className);
    } else {
      controller.log("Stopping monitoring ReplyReceiver for " + className);
    }
    boolean isMonitoringBefore = isMonitoring();
    monitoringReplyReceiver = shouldMonitor;
    boolean isMonitoringAfter = isMonitoring();
    if (isMonitoringBefore != isMonitoringAfter) monitoringMessageEventChanged(this, isMonitoringAfter);
    if (listener != null) listener.monitoringReplyReceiverChanged(shouldMonitor);
  }
  

  public void monitorReplySender(boolean shouldMonitor) {
    if (isDestroyed) return;
    if (monitoringReplySender == shouldMonitor) return; // already doing the right thing
    if (shouldMonitor) {
      controller.log("Starting monitoring ReplySender for " + className);
    } else {
      controller.log("Stopping monitoring ReplySender for " + className);
    }
    boolean isMonitoringBefore = isMonitoring();
    monitoringReplySender = shouldMonitor;
    boolean isMonitoringAfter = isMonitoring();
    if (isMonitoringBefore != isMonitoringAfter) monitoringMessageEventChanged(this, isMonitoringAfter);
    if (listener != null) listener.monitoringReplySenderChanged(shouldMonitor);
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected NodeObject getTypedParent() {
    return (NodeObject)parent;
  }


  protected boolean destroy() {
    destroyCachedObject(id);
    return super.destroy();
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //
  
  
  private static String shortClassName(String fqn) {
    int n = fqn.lastIndexOf('.');
    if (n == -1 || n == fqn.length()-1) return fqn;
    return fqn.substring(n+1);
  }

   
  //
  // -- PRIVATE STATIC METHODS -----------------------------------------------
  //
  
  private static int counter = 1;
  private static java.util.HashMap knownActiveObjectCache = new java.util.HashMap();
  
  private static synchronized int counter(UniqueID id) {
    CachedActiveObject cachedObject = (CachedActiveObject) knownActiveObjectCache.get(id);
    if (lastActiveObjectCacheCleanupTime + ACTIVE_OBJECT_CACHE_CLEANUP_INTERVAL > System.currentTimeMillis()) {
      lastActiveObjectCacheCleanupTime = System.currentTimeMillis();
      clearOldCachedObject();
    }
    if (cachedObject == null) {
      // not cached
      int count = counter++;
      cachedObject = new CachedActiveObject(count);
      knownActiveObjectCache.put(id,cachedObject);
      return count;
    } else {
      cachedObject.resurrect();
      return cachedObject.count;
    }
  }
  
  private static void clearOldCachedObject() {
    long oldestAcceptableTime = System.currentTimeMillis() - 300000; // 5mn
    java.util.Collection values = knownActiveObjectCache.values();
    java.util.Iterator iterator = values.iterator();
    while (iterator.hasNext()) {
      CachedActiveObject cachedObject = (CachedActiveObject) iterator.next();
      if (cachedObject.destroyed && cachedObject.timestamp < oldestAcceptableTime) {
        // remove from the cache
        iterator.remove();
      }
    }
  }
  
  private static void destroyCachedObject(UniqueID id) {
    CachedActiveObject cachedObject = (CachedActiveObject) knownActiveObjectCache.get(id);
    if (cachedObject == null) return;
    cachedObject.destroy();
  }
  
  private static class CachedActiveObject {
    long timestamp;
    int count;
    boolean destroyed;
    CachedActiveObject(int count) {
      this.count = count;
    }
    void destroy() {
      this.timestamp = System.currentTimeMillis();
      this.destroyed = true;
    }
    void resurrect() {
      this.timestamp = 0;
      this.destroyed = false;
    }
  }

}