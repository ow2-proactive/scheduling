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
package org.objectweb.proactive.ic2d.event;

import org.objectweb.proactive.core.event.ProActiveListener;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.spy.SpyEvent;

public interface SpyEventListener extends ProActiveListener {

  public void activeObjectAdded(UniqueID id, String nodeURL, String classname, boolean isActive);
  
  public void activeObjectChanged(UniqueID id, boolean isActive, boolean isAlive);
  
  public void objectWaitingForRequest(UniqueID id, SpyEvent spyEvent);
  
  public void objectWaitingByNecessity(UniqueID id, SpyEvent spyEvent);
  
  public void objectReceivedFutureResult(UniqueID uniqueID, SpyEvent spyEvent);

  public void requestMessageSent(UniqueID id, SpyEvent spyEvent);
  
  public void replyMessageSent(UniqueID id, SpyEvent spyEvent);

  public void requestMessageReceived(UniqueID id, SpyEvent spyEvent);
  
  public void replyMessageReceived(UniqueID id, SpyEvent spyEvent);
  
  public void voidRequestServed(UniqueID id, SpyEvent spyEvent);

  public void allEventsProcessed();

  public void servingStarted(UniqueID uniqueID, SpyEvent spyEvent);

}