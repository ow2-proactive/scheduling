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
package org.objectweb.proactive.core.body;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.util.ProActiveProperties;

/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class is a invoked to create some of the components of the body. Using a factory
 * allows to specialize the component of the body without subclassing it.
 * </p>
 * 
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 */
public class MetaObjectFactory {

  //
  // -- PUBLIC MEMBERS -----------------------------------------------
  //
  
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  private MetaObjectFactory()  {
  }
  
  //
  // -- PUBLIC METHOD -----------------------------------------------
  //
  
  
   public static RequestReceiver createRequestReceiver() {
    try {
      Class c = Class.forName(ProActiveProperties.getRequestReceiverClass());
      java.lang.reflect.Constructor constr = c.getConstructor(new Class[0]);
      return (RequestReceiver)constr.newInstance(new Object[0]);
    } catch (Exception e) {
      throw new ProActiveRuntimeException(e.toString());      
    }
  }    
  
  public static MigrationManager createMigrationManager() {
    try {
      Class c = Class.forName(ProActiveProperties.getMigrationManagerClass());
      java.lang.reflect.Constructor constr = c.getConstructor(new Class[0]);
      return (MigrationManager)constr.newInstance(new Object[0]);
    } catch (Exception e) {
      throw new ProActiveRuntimeException(e.toString());      
    }
  }    

 
}
