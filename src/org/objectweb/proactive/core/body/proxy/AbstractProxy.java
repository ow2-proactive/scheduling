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
package org.objectweb.proactive.core.body.proxy;

import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;

public abstract class AbstractProxy implements Proxy, java.io.Serializable {

  //
  // -- STATIC MEMBERS -----------------------------------------------
  //


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public AbstractProxy() {
  }

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //


  //
  // -- STATIC METHODS -----------------------------------------------
  //


  // -------------------------------------------------------------------------------------------
  //
  // ASYNCHRONOUS CALL
  //
  // -------------------------------------------------------------------------------------------

  /**
   * The hashtable that caches Method/isAsynchronousCall
   * This dramatically improves performances, since we do not have to call
   * isAsynchronousCall for every call, but only once for a given method
   */
  private static transient java.util.Hashtable ASYNORNOT = new java.util.Hashtable();


  /**
   * Checks if the given <code>Call</code> object <code>c</code> can be
   * processed with a future semantics, i-e if its returned object
   * can be a future object.
   *
   * Two conditions must be met : <UL>
   * <LI> The returned object is reifiable
   * <LI> The invoked method does not throw any exceptions
   * </UL>
   * @return true if and only if the method call can be asynchronous
   */
  protected static boolean isAsynchronousCall(MethodCall c) {
    return isAsynchronousCall(c.getReifiedMethod());
  }


  /**
   * Returns a boolean saying whether the methode is one-way or not.
   * Being one-way method is equivalent to <UL>
   * <LI>having <code>void</code> as return type
   * <LI>and not throwing any checked exceptions</UL>
   * @return true if and only if the method call is one way
   */
  protected static boolean isOneWayCall(MethodCall c) {
    return isOneWayCall(c.getReifiedMethod());
  }


  /**
   * Returns a boolean saying whether the methode is one-way or not.
   * Being one-way method is equivalent to <UL>
   * <LI>having <code>void</code> as return type
   * <LI>and not throwing any checked exceptions</UL>
   * @return true if and only if the method call is one way
   */
  protected static boolean isOneWayCall(java.lang.reflect.Method m) {
    return (m.getReturnType().equals(java.lang.Void.TYPE)) && (m.getExceptionTypes().length == 0);
  }


  /**
   * Checks if the given <code>Call</code> object <code>c</code> can be
   * processed with a future semantics, i-e if its returned object
   * can be a future object.
   *
   * Two conditions must be met : <UL>
   * <LI> The returned object is reifiable
   * <LI> The invoked method does not throw any exceptions
   * </UL>
   */
  private static boolean isAsynchronousCall(java.lang.reflect.Method m) {
    // Is the result cached ?
    Boolean b = (Boolean)ASYNORNOT.get(m);
    if (b != null) {
      return b.booleanValue();
    } else // Computes the result
    {
      boolean result;
      // A Method that returns void is the only case where a method that returns
      // a non-reifiable type is asynchronous
      if (isOneWayCall(m)) {
        result = true;
      } else {
        try {
          MOP.checkClassIsReifiable(m.getReturnType());
          // If the method can throw exceptions, then the result if false
          if (m.getExceptionTypes().length > 0) {
            //System.out.println(" ------ isAsynchronousCall() The method can throw exceptions ");
            result = false;
          } else {
            result = true;
          }
        } catch (ClassNotReifiableException e) {
          //System.out.println(" ------ isAsynchronousCall() The class " + m.getReturnType() + " is not reifiable ");
          result = false;
        }
        // Now that we have computed the result, let's cache it
        //System.out.println(" ------ isAsynchronousCall() method " + m + " ===> "+result);
        ASYNORNOT.put(m, new Boolean(result));
      }
      return result;
    }
  }


}
