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
package org.objectweb.proactive.core.event;

/**
 * <p>
 * A class implementating this interface is listener of <code>BodyEvent</code>.
 * </p>
 * 
 * @see BodyEvent
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface BodyEventListener extends ProActiveListener {

  /**
   * Signals that the body encapsulated in the event <code>bodyEvent</code>
   * has been created
   * @param event the body event that detailled the creation of the body
   */
  public void bodyCreated(BodyEvent event);
  
  /**
   * Signals that the body encapsulated in the event <code>event</code>
   * has been destroyed
   * @param event the body event that details the destruction of the body
   */
  public void bodyDestroyed(BodyEvent event);
 
  /**
   * Signals that the body encapsulated in the event <code>event</code>
   * has been changed
   * @param event the body event that details the change of the body
   */
  public void bodyChanged(BodyEvent event);
 
}
