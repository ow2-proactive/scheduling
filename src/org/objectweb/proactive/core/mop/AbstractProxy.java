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
package org.objectweb.proactive.core.mop;

import java.lang.reflect.InvocationTargetException;

/**
 *	A convenience class for helping developers implement their
 *	own proxy classes.
 */
public abstract class AbstractProxy extends Object implements Proxy {

  /**
   *	The object the proxy sends calls to
   */
  protected Object target;


  public AbstractProxy() throws ConstructionOfReifiedObjectFailedException {
  }


  /**
   *	Concrete subclasses of this class have to provide an implementation
   *	for this method, which is really where the specific behavior of a proxy
   *	class is described.
   */

  public abstract Object reify(MethodCall c) throws InvocationTargetException;


  /**
   *	Sets the target object
   */

  public void setTarget(Object target) {
    this.target = target;
    return;
  }


  public Object getTarget() {
    return this.target;
  }
}

