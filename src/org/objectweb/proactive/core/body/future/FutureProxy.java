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
package org.objectweb.proactive.core.body.future;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import java.lang.reflect.InvocationTargetException;

/**
 * This proxy class manages the semantic of future objects
 *
 * @author Julien Vayssière - INRIA
 * @see org.objectweb.proactive.core.mop.Proxy
 *
 */
public class FutureProxy implements Future, Proxy, java.io.Serializable {

  //
  // -- STATIC MEMBERS -----------------------------------------------
  //

  /**
   *  The size of the pool we use for recycling FutureProxy objects.
   */
  public static final int RECYCLE_POOL_SIZE = 1000;
  private static FutureProxy[] recyclePool;

  /**
   *  Indicates if the recycling of FutureProxy objects is on.
   */
  private static boolean shouldPoolFutureProxyObjects;


  private static int index;


  //
  // -- PROTECTED MEMBERS -----------------------------------------------
  //

  /**
   *	The object the proxy sends calls to
   */
  protected Object target;

  /**
   * To mark the Proxy before migration
   * Usually, the Proxy cannot be serialized if the result is not available (no automatic continuation)
   * but if we migrate, we don't want to wait for the result
   */
  protected boolean migration;

  /**
   * This flag indicates the status of the future object
   */
  protected boolean isAvailable;

  /**
   * Indicates if the returned object is an exception
   */
  protected boolean isException;


  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  /**
   * As this proxy does not create a reified object (as opposed to
   * BodyProxy for example), it is the noargs constructor that
   * is usually called.
   */

  public FutureProxy() throws ConstructionOfReifiedObjectFailedException {
  }


  /**
   * This constructor is provided for compatibility with other proxies.
   * More precisely, this permits proxy instanciation via the Meta.newMeta
   * method.
   */
  public FutureProxy(ConstructorCall c, Object[] p) throws ConstructionOfReifiedObjectFailedException {
    // we don't care what the arguments are
    this();
  }


  //
  // -- PUBLIC STATIC METHODS -----------------------------------------------
  //

  /**
   * Tests if the object <code>obj</code> is awaited or not. Always returns
   * <code>false</code> if <code>obj</code> is not a future object.
   */
  public static boolean isAwaited(Object obj) {
    // If the object is not reified, it cannot be a future
    if ((MOP.isReifiedObject(obj)) == false) return false;
    Proxy theProxy = ((StubObject)obj).getProxy();
    // If it is reified but its proxy is not of type future, we cannot wait
    if (!(theProxy instanceof Future)) return false;
    return ((Future)theProxy).isAwaited();
  }


  public synchronized static FutureProxy getFutureProxy() {
    FutureProxy result;
    if (shouldPoolFutureProxyObjects && (index > 0)) {
      // gets the object from the pool
      index--;
      result = recyclePool[index];
      recyclePool[index] = null;
    } else {
      try {
        result = new FutureProxy();
      } catch (ConstructionOfReifiedObjectFailedException e) {
        result = null;
      }
    }
    return result;
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public boolean equals(Object obj) {
    //we test if we have a future object
    if (isFutureObject(obj)) {
      return (((StubObject)obj).getProxy().hashCode() == this.hashCode());
    }
    return false;
  }


  //
  // -- Implements Future -----------------------------------------------
  //

  /**
   * Invoked by a thread of the skeleton that performed the service in order
   * to tie the result object to the proxy.
   *
   * If the execution of the call raised an exception, this exception is put
   * into an object of class InvocationTargetException and returned, just like
   * for any returned object
   */

  public synchronized void receiveReply(Object obj) throws java.io.IOException {
    if (target != null) {
      throw new java.io.IOException("FutureProxy receives a reply and this target field is not null");
    }
    target = obj;
    if (target != null) {
      isException = (target instanceof Throwable);
    }
    isAvailable = true;
    this.notifyAll();
  }


  /**
   * Returns the result this future is for as an exception if an exception has been raised
   * or null if the result is not an exception. The method blocks until the result is available.
   * @return the exception raised once available or null if no exception.
   */
  public synchronized Throwable getRaisedException() {
    waitFor();
    if (isException) return (Throwable)target;
    return null;
  }


  /**
   * Returns the result this future is for. The method blocks until the future is available
   * @return the result of this future object once available.
   */
  public synchronized Object getResult() {
    waitFor();
    return target;
  }


  /**
   * Tests the status of the returned object
   * @return <code>true</code> if the future object is NOT yet available, <code>false</code> if it is.
   */
  public synchronized boolean isAwaited() {
    return ! isAvailable;
  }


  /**
   * Blocks the calling thread until the future object is available.
   */
  public synchronized void waitFor() {
    while (! isAvailable) {
      try {
        this.wait();
      } catch (InterruptedException e) {}
    }
  }


  //
  // -- Implements Proxy -----------------------------------------------
  //

  /**
   * Blocks until the future object is available, then executes Call <code>c</code> on the now-available object.
   *
   *  As future and process behaviors are mutually exclusive, we know that
   * the invocation of a method on a future objects cannot lead to wait-by
   * necessity. Thus, we can propagate all exceptions raised by this invocation
   *
   * @exception InvocationTargetException If the invokation of the method represented by the
   * <code>Call</code> object <code>c</code> on the reified object
   * throws an exception, this exception is thrown as-is here. The stub then
   * throws this exception to the calling thread after checking that it is
   * declared in the throws clause of the reified method. Otherwise, the stub
   * does nothing except print a message on System.err (or out ?).
   */
  public Object reify(MethodCall c) throws InvocationTargetException {
    Object result = null;
    //	System.out.println("FutureProxy: c.getName() = " +c.getName());
    if ((c.getName()).equals("equals") || (c.getName()).equals("hashCode")) {
      //System.out.println("FutureProxy: now executing " + c.getName());
      try {
        result = c.execute(this);
      } catch (MethodCallExecutionFailedException e) {
        throw new ProActiveRuntimeException("FutureProxy: Illegal arguments in call " + c.getName());
      }
      return result;
    }
    waitFor();

    // Now that the object is available, execute the call
    if (this.isException) {
      throw ((InvocationTargetException)this.target);
    } else {
      try {
        result = c.execute(this.target);
      } catch (MethodCallExecutionFailedException e) {
        throw new ProActiveRuntimeException("FutureProxy: Illegal arguments in call " + c.getName());
      }
    }
    return result;
  }



  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected void finalize() {
    returnFutureProxy(this);
  }


  protected void setMigrationTag() {
    migration = true;
  }


  protected void unsetMigrationTag() {
    migration = false;
  }



  //
  // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
  //

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    // We wait until the result is available
    if (!migration) waitFor();
    // Now that the result is available, we can copy the future
    out.writeObject(target);
    // It is impossible that a future object can be passed
    // as a parameter if it has raised a checked exception
    // For the other exceptions...
    out.writeBoolean(isException);
    out.writeBoolean(isAvailable);
  }


  //for the moment, we set the value of migration to false here
  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    target = (Object)in.readObject();
    isException = (boolean)in.readBoolean();
    //	this.isAvailable = true;
    isAvailable = (boolean)in.readBoolean();
    //now we restore migration to its normal value
    migration = false;
  }



  //
  // -- PRIVATE STATIC METHODS -----------------------------------------------
  //

  private static boolean isFutureObject(Object obj) {
    // If obj is not reified, it cannot be a future
    if (!(MOP.isReifiedObject(obj))) return false;
    // Being a future object is equivalent to have a stub/proxy pair
    // where the proxy object implements the interface FUTURE_PROXY_INTERFACE
    // if the proxy does not inherit from FUTURE_PROXY_ROOT_CLASS
    // it is not a future
    Class proxyclass = ((StubObject)obj).getProxy().getClass();
    Class[] ints = proxyclass.getInterfaces();
    for (int i = 0; i < ints.length; i++) {
      if (Constants.FUTURE_PROXY_INTERFACE.isAssignableFrom(ints[i]))
        return true;
    }
    return false;
  }


  private static synchronized void setShouldPoolFutureProxyObjects(boolean value) {
    if (shouldPoolFutureProxyObjects == value) return;
    shouldPoolFutureProxyObjects = value;
    if (shouldPoolFutureProxyObjects) {
      // Creates the recycle poll for FutureProxy objects
      recyclePool = new FutureProxy[RECYCLE_POOL_SIZE];
      index = 0;
    } else {
      // If we do not want to recycle FutureProxy objects anymore,
      // let's free some memory by permitting the reyclePool to be
      // garbage-collecting
      recyclePool = null;
    }
  }


  private static synchronized void returnFutureProxy(FutureProxy futureProxy) {
    if (! shouldPoolFutureProxyObjects) return;
    // If there's still one slot left in the pool
    if (recyclePool[index] == null) {
      // Cleans up a FutureProxy object
      // It is prefereable to do it here rather than at the moment
      // the object is picked out of the pool, because it allows
      // garbage-collecting the objects referenced in here
      futureProxy.target = null;
      futureProxy.isAvailable = false;
      futureProxy.isException = false;

      // Inserts the object in the pool
      recyclePool[index] = futureProxy;
      index++;
      if (index == RECYCLE_POOL_SIZE)
        index = RECYCLE_POOL_SIZE - 1;
    }
  }


}
