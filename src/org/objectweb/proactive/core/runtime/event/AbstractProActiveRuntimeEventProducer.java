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
package org.objectweb.proactive.core.runtime.event;

import java.util.ArrayList;

import org.objectweb.proactive.core.event.ProActiveEvent;
import org.objectweb.proactive.core.event.ProActiveListener;
import org.objectweb.proactive.core.event.RuntimeRegistrationEventListener;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;

/**
 * <p>
 * Provides support for adding, removing and notifying <code>RuntimeRegistrationEventListener</code>.
 * This class can be derived to get immediate support for sending <code>RuntimeRegistrationEvent</code>
 * to <code>RuntimeRegistrationEventListener</code>.
 * </p>
 */
public abstract class AbstractProActiveRuntimeEventProducer implements RuntimeRegistrationEventProducer
{
	
	protected ArrayList eventListeners;
	
	
	//
	//--------------------Constructor--------------------------------
	//
	public AbstractProActiveRuntimeEventProducer(){
		eventListeners = new java.util.ArrayList();
		//System.out.println("created listener table");
	}
	
	//
	//-------------------implements RuntimeRegistrationEventProducer------------------
	//
	/**
	 * @see org.objectweb.proactive.core.runtime.event.RuntimeRegistrationEventProducer#addRuntimeRegistrationEventListener(RuntimeRegistrationEventListener)
	 */
	public void addRuntimeRegistrationEventListener(RuntimeRegistrationEventListener listener){
		addListener(listener);
	}
	
	
	/**
	 * @see org.objectweb.proactive.core.runtime.event.RuntimeRegistrationEventProducer#removeRuntimeRegistrationEventListener(RuntimeRegistrationEventListener)
	 */
	public void removeRuntimeRegistrationEventListener(RuntimeRegistrationEventListener listener){
		removeListener(listener);
	}
	
	//
  // -- PROTECTED METHODS -----------------------------------------------
  //

  /**
   * Returns true is this event producer has at least one registered listener
   * @return true is this event producer has at least one registered listener
   */
  protected boolean hasListeners()  {
    return ! eventListeners.isEmpty();
  }
  

  /**
   * Adds the given listener
   * @param <code>listener</code> the listener to register
   */
  protected void addListener(ProActiveListener listener) {
    synchronized (eventListeners)  {
      if (! eventListeners.contains(listener)) {
        eventListeners.add(listener);
      }
    }
  }
  

  /**
   * Removes the given listener
   * @param <code>listener</code> the listener to remove
   */
  protected void removeListener(ProActiveListener listener) {
    synchronized (eventListeners)  {
      eventListeners.remove(listener);
    }
  }


  /**
   * Notifies all registered listener with the event. This method call 
   * <code>notifyOneListener</code> on each registered listener.
   * @param <code>event</code> the event to fire to all listeners.
   */
  protected void notifyAllListeners(ProActiveEvent event) {
    synchronized (eventListeners) {
      java.util.Iterator iterator = eventListeners.iterator();
      while (iterator.hasNext()) {
        notifyOneListener((ProActiveListener)iterator.next(),event);
      }
    }
  }
  
  //
  //-------------------PROTECTED ABSTRACT METHODS--------------------------------
  /**
   * Builds the evnt and notifies all listeners.This method should call <code>notifyAllListeners</code>
   */ 
	protected abstract void notifyListeners(ProActiveRuntime proActiveRuntime, int type, String creatorID, String RegisteredRuntimeName, String protocol);
	
	/**
	 * Notifies one listener with the event
	 */
	protected abstract void notifyOneListener(ProActiveListener proActiveListener, ProActiveEvent event);

}
