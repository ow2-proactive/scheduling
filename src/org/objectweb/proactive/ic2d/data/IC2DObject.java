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

import org.objectweb.proactive.ic2d.util.IC2DMessageLogger;
import org.objectweb.proactive.ic2d.util.HostNodeFinder;
import org.objectweb.proactive.ic2d.util.RMIHostNodeFinder;
import org.objectweb.proactive.ic2d.event.IC2DObjectListener;
import org.objectweb.proactive.ic2d.event.CommunicationEventListener;
import org.objectweb.proactive.ic2d.spy.SpyEvent;

/**
 * Holder class for all hosts
 */
public class IC2DObject extends AbstractDataObject {

  protected IC2DObjectListener listener;
  protected WorldObject worldObject;
  protected IC2DMessageLogger logger;
  protected CommunicationEventListener communicationEventListener;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public IC2DObject() {
    super();
    controller = new MyController();
    communicationEventListener = new MyCommunicationEventListener();
    worldObject = new WorldObject(this);
    putChild(worldObject, worldObject);
  }


  //
  // -- PUBLIC METHOD -----------------------------------------------
  //
  
  
  //
  // Event Listener
  //
    
  public void registerListener(IC2DObjectListener listener) {
    this.listener = listener;
  }
  
  
  //
  // IC2DMessageLogger
  //
    
  public void registerLogger(IC2DMessageLogger logger) {
    this.logger = logger;
  }
  

  
  //
  // World related method
  //
    
  public WorldObject getWorldObject() {
    return worldObject;
  }


  public void destroyObject() {
    worldObject.destroyObject();
  }


  //
  // -- PROTECTED METHOD -----------------------------------------------
  //

  protected void activeObjectAdded(ActiveObject activeObject) {
    if (listener != null) listener.activeObjectAdded(activeObject);
  }
  
  
  protected void activeObjectRemoved(ActiveObject activeObject) {
    if (listener != null) listener.activeObjectRemoved(activeObject);
  }

  protected CommunicationEventListener getCommunicationEventListener() {
    return communicationEventListener;
  }


  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  //
  // -- INNER CLASSES -----------------------------------------------
  //
  
  private class MyCommunicationEventListener implements CommunicationEventListener {
  
    // -- implements CommunicationEventListener -----------------------------------------------
  
    public void objectWaitingForRequest(ActiveObject object, SpyEvent spyEvent) {
      if (listener != null) {
        listener.objectWaitingByNecessity(object, spyEvent);
      }
    }
  
    public void objectWaitingByNecessity(ActiveObject object, SpyEvent spyEvent) {
      if (listener != null) {
        listener.objectWaitingByNecessity(object, spyEvent);
      }
    }
  
    public void requestMessageSent(ActiveObject object, SpyEvent spyEvent) {
       if (listener != null) {
         //System.out.println("requestMessageSent method="+((org.objectweb.proactive.ic2d.spy.SpyMessageEvent)spyEvent).getMethodName());
         listener.requestMessageSent(object, spyEvent);
       }
    }
  
    public void requestMessageReceived(ActiveObject object, SpyEvent spyEvent) {
       if (listener != null) {
         //System.out.println("requestMessageReceived method="+((org.objectweb.proactive.ic2d.spy.SpyMessageEvent)spyEvent).getMethodName());
         listener.requestMessageReceived(object, spyEvent);
       }
    }
  
    public void replyMessageSent(ActiveObject object, SpyEvent spyEvent) {
       if (listener != null) {
         //System.out.println("replyMessageSent method="+((org.objectweb.proactive.ic2d.spy.SpyMessageEvent)spyEvent).getMethodName());
         listener.replyMessageSent(object, spyEvent);
       }
    }

    public void replyMessageReceived(ActiveObject object, SpyEvent spyEvent) {
       if (listener != null) {
         // System.out.println("replyMessageReceived method="+((org.objectweb.proactive.ic2d.spy.SpyMessageEvent)spyEvent).getMethodName());
         listener.replyMessageReceived(object, spyEvent);
       }
    }

    public void allEventsProcessed() {
      if (listener != null) {
        listener.allEventsProcessed();
      }
    }
  }


  
  private class MyController implements DataObjectController {
  
    private boolean isMonitoring = true;

    public MyController() {
    }
    
    public boolean isMonitoring() {
      return isMonitoring;
    }

    public void setMonitoring(boolean b) {
      isMonitoring = b;
    }

    public void warn(String message) {
      if (logger != null) logger.warn(message);
    }

    public void log(String message) {
      if (logger != null) logger.log(message);
    }

    public void log(String message, Throwable e) {
      if (logger != null) logger.log(message, e);
    }

    public void log(Throwable e) {
      if (logger != null) logger.log(e);
    }

  }

}
