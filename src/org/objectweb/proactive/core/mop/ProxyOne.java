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
 *	This proxy simply behave like if there were no reification at all. It may
 * be seen as an 'identity' operator
 */
public class ProxyOne extends AbstractProxy {

  public ProxyOne() throws ConstructionOfReifiedObjectFailedException {
  }


  public ProxyOne(ConstructorCall c, Object[] p) throws ConstructionOfReifiedObjectFailedException {
    try {
      this.setTarget(c.execute());
    } catch (ConstructorCallExecutionFailedException e) {
      this.setTarget(null);
    } catch (InvocationTargetException e) {
      throw new ConstructionOfReifiedObjectFailedException(e.getTargetException());
    }
  }


  public Object reify(MethodCall c) throws InvocationTargetException {
    try {
      return c.execute(this.getTarget());
    } catch (MethodCallExecutionFailedException e) {
      System.err.println(e);
      e.printStackTrace();
      return null;
    }
  }
}



