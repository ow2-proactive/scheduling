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

import java.io.Serializable;

public class FixWrapper implements Serializable {

  public boolean isPrimitive;
  public Class encapsulated;


  public FixWrapper() {}


  /**
   * Encapsulate primitives types into Class 
   */
  public FixWrapper(Class c) {
    if (!c.isPrimitive()) {
      this.encapsulated = c;
      return;
    }
    if (c.getName().equals("int")) {
      this.isPrimitive = true;
      this.encapsulated = Integer.class;
    } else if (c.getName().equals("boolean")) {
      this.isPrimitive = true;
      this.encapsulated = Boolean.class;
    }

  }


  /**
   * Give back the original class
   *
   */
  public Class getWrapped() {
    if (this.isPrimitive) {
      if (this.encapsulated.getName().equals("java.lang.Integer")) {
        //	System.out.println("Found an encapsulated integer");
        return Integer.TYPE;
      }
      if (this.encapsulated.getName().equals("java.lang.Boolean")) {
        //System.out.println("Found an encapsulated boolean");
        return Boolean.TYPE;
      }
      return null;
    } else
      return this.encapsulated;

  }


  public String toString() {

    return "FixWrapper: " + this.encapsulated.toString();
  }
}
