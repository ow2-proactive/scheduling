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

/**
 * This class contains static convenience and utility methods
 */
public class StubObjectUtils {

  private StubObjectUtils() {
  }

  public static void processUndeclaredException(Throwable t) {
    if (t instanceof Error)
      throw (Error) t;
    if (t instanceof RuntimeException)
      throw (RuntimeException) t;
  }
    
  public static void processException(Exception t, String method) {
    throw new InternalException("Caught unexpected exception in method "+method+" : "+t);
  }
    
  public static void processThrowable(Throwable t, String method) {
    throw new InternalException("Caught an object that is not an Exception or an Error in method "+method+" : "+t);
  }
    
  public static void processIllegalAccessException(IllegalAccessException e, String method) {
    throw new InternalException("Method "+method+"is inaccessible\nThis may be caused by an inconsistent stub - try to delete the stub class file and rerun the program", e);
  }
    
  public static void processIllegalArgumentException(IllegalArgumentException e, String method) {
    throw new InternalException("Problem with wrong number of parameters or wrapping/unwrapping of arguments for method "+method+"\nThis may be caused by an inconsistent stub. Try to delete the stub class file and rerun the program", e);
  }


}