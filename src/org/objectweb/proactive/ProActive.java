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
package org.objectweb.proactive;

import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.request.BodyRequest;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.Constants;

import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.mop.CannotGuessProxyNameException;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.MethodCall;

/**
 * <p>
 * The ProActive class provides a set of static services through static method calls.
 * It is the main entry point for users of ProActive as they will call methods of
 * this class to create active object, to migrate them or to register them to the
 * RMI Registry.
 * </p><p>
 * <b>see <a href="doc-files/ActiveObjectCreation.html">active object creation doumentation</a></b>
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class ProActive {
  
  //
  // -- STATIC MEMBERS -----------------------------------------------
  //
  

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  private ProActive() {}
  
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  /**
   * Creates a new ActiveObject based on classname attached to a default 
   * node in the local JVM.
   * @param classname the fully qualified name of the class to instantiate 
   *    as an active object
   * @param constructorParameters the parameters of the constructor of the object
   *    to instantiate as active. If some parameters are primitive types, the wrapper 
   *    class types should be given here.
   * @return a reference (possibly remote) on a Stub of the newly created 
   *     active object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the DefaultNode cannot be created
   */
  public static Object newActive(String classname, Object[] constructorParameters) throws ActiveObjectCreationException, NodeException {
    return newActive(classname, constructorParameters, (Node) null);
  }


  /**
   * Creates a new ActiveObject based on classname attached to the node  
   * of the given URL.
   * @param classname the fully qualified name of the class to instantiate 
   *    as an active object
   * @param constructorParameters the parameters of the constructor of the object
   *    to instantiate as active. If some parameters are primitive types, the wrapper 
   *    class types should be given here.
   * @param nodeURL the URL of the node where to create the active object on. If null, the active object 
   *       is created localy on a default node 
   * @return a reference (possibly remote) on a Stub of the newly created 
   *     active object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node URL cannot be resolved as an existing Node
   */
  public static Object newActive(String classname, Object[] constructorParameters, String nodeURL) throws ActiveObjectCreationException, NodeException {
    if (nodeURL == null) {
      return newActive(classname, constructorParameters, (Node) null);
    } else {
      return newActive(classname, constructorParameters, NodeFactory.getNode(nodeURL));
    }
  }


  /**
   * Creates a new ActiveObject based on classname attached to the given node or on
   * a default node in the local JVM if the given node is null.
   * If the given class implements <code>org.objectweb.proactive.Active</code> or a sub-interface of 
   * <code>org.objectweb.proactive.Active</code>, the body and proxy used will be given by the 
   * constants defined in the interface.
   * Else the proxy used is <code>org.objectweb.proactive.core.body.proxy.BodyProxy</code>
   * and the body used is <code>org.objectweb.proactive.core.body.BodyImpl</code>
   * If the specified proxy class or body class cannot be loaded, the default ones are used.
   * The object returned is a stub class that extends the target class and that is automatically 
   * generated on the fly. The Stub class reference a the proxy object that reference the body
   * of the active object. The body referenced by the proxy can either be local of remote, 
   * depending or the respective location of the object calling the newActive and the active object
   * itself. 
   * @param classname the name of the class to instanciate as active
   * @param constructorParameters the parameters of the constructor of the object
   *    to instantiate as active. If some parameters are primitive types, the wrapper 
   *    class types should be given here.
   * @param node the possibly null node where to create the active object on. If null, the active object 
   *       is created localy on a default node 
   * @return a reference (possibly remote) on a Stub of the newly created active object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object newActive(String classname, Object[] constructorParameters, Node node) throws ActiveObjectCreationException, NodeException {
    //using default proactive node
    if (node == null) {
      node = NodeFactory.getDefaultNode();
    }
    try {
      return createStubObject(classname, constructorParameters, node);
    } catch (MOPException e) {
      Throwable t = e;
      if (e.getTargetException() != null) t = e.getTargetException();
      throw new ActiveObjectCreationException(t);
    }
  }


  /**
   * Turns the target object into an ActiveObject attached to a default node in the local JVM.
   * The type of the stub is is the type of the existing object.
   * @param target The object to turn active
   * @return a reference (possibly remote) on a Stub of the existing object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the DefaultNode cannot be created
   */
  public static Object turnActive(Object target) throws ActiveObjectCreationException, NodeException {
    return turnActive(target, (Node) null);
  }


  /**
   * Turns the target object into an Active Object and send it to the Node
   * identified by the given url.
   * The type of the stub is is the type of the existing object.
   * @param target The object to turn active
   * @param nodeURL the URL of the node where to create the active object on. If null, the active object 
   *       is created localy on a default node 
   * @return a reference (possibly remote) on a Stub of the existing object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object turnActive(Object target, String nodeURL) throws ActiveObjectCreationException, NodeException {
    if (nodeURL == null) {
      return turnActive(target, (Node) null);
    } else {
      return turnActive(target, NodeFactory.getNode(nodeURL));
    }
  }


  /**
   * Turns the target object into an Active Object and send it to the given Node
   * or to a default node in the local JVM if the given node is null.
   * The type of the stub is is the type of the target object.
   * @param target The object to turn active
   * @param node The Node the object should be sent to or null to create the active 
   *       object in the local JVM
   * @return a reference (possibly remote) on a Stub of the target object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object turnActive(Object target, Node node) throws ActiveObjectCreationException, NodeException {
    return turnActive(target, node, target.getClass().getName());
  }


  /**
   * Turns a Java object into an Active Object and send it to a remote Node or to a 
   * local node if the given node is null.
   * A Stub is dynamically generated for the existing object. The result of the call 
   * will be an instance of the Stub class pointing to the default proxy object pointing
   * to the default body object pointing to the existing object. The body can be remote 
   * or local depending if the existing is sent remotely or not.
   * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
   * @param target The object to turn active
   * @param node The Node the object should be sent to or null to create the active 
   *       object in the local JVM
   * @param nameOfTargetType the fully qualified name of the type the stub class should
   * inherit from. That type can be less specific than the type of the target object.
   * @return a reference (possibly remote) on a Stub of the target object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object turnActive(Object target, Node node, String nameOfTargetType) throws ActiveObjectCreationException, NodeException {
    if (node == null) {
      //using default proactive node
      node = NodeFactory.getDefaultNode();
    }
    try {
      return createStubObject(target, node, nameOfTargetType);
    } catch (MOPException e) {
      Throwable t = e;
      if (e.getTargetException() != null) t = e.getTargetException();
      throw new ActiveObjectCreationException(t);
    }
  }


  /**
   * Turns a Java object into an Active Object and send it to a remote Node or to a 
   * local node if the given node is null.
   * A Stub is dynamically generated for the existing object. The result of the call 
   * will be an instance of the Stub class pointing to a proxy defined by the given 
   * proxy class (parameter <code>nameOfProxyType</code>), pointing to a body defined by
   * the given body class (parameter <code>nameOfBodyType</code>),  pointing to the existing 
   * object. The body can be remote or local depending if the target object is sent remotely
   * or not.
   * The type of the stub is the same as the one of the target object.
   * @param target The object to turn active
   * @param node The Node the object should be sent to or null to create the active 
   *       object in the local JVM
   * @param nameOfBodyType the fully qualified name of the body type to associate with the
   *     target object.
   * @param nameOfProxyType the fully qualified name of the proxy type to associate with the
   *     stub object.
   * @return a reference (possibly remote) on a Stub of the target object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object turnActive(Object target, Node node, String nameOfBodyType, String nameOfProxyType) throws ActiveObjectCreationException, NodeException {
    try {
      return createStubObject(target, node, nameOfBodyType, nameOfProxyType);
    } catch (MOPException e) {
      Throwable t = e;
      if (e.getTargetException() != null) t = e.getTargetException();
      throw new ActiveObjectCreationException(t);
    }
  }


  /**
   * Registers an active object into a RMI registry. In fact it is the
   * remote version of the body of the active object that is registered into the 
   * RMI Registry under the given URL. 
   * @param obj the active object to register.
   * @param url the url under which the remote body is registered.
   * @exception java.io.IOException if the remote body cannot be registered
   */
  public static void register(Object obj, String url) throws java.io.IOException {
    // Check if obj is really a reified object
    if (!(MOP.isReifiedObject(obj))) {
      throw new java.io.IOException("The given object "+obj+" is not a reified object");
    }
    // Find the appropriate remoteBody
    org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject)obj).getProxy();
    if (myProxy == null)
      throw new java.io.IOException("Cannot find a Proxy on the stub object: " + obj);
    BodyProxy myBodyProxy = (BodyProxy)myProxy;
    UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();
    if (body instanceof RemoteBodyAdapter) {
      RemoteBodyAdapter.register((RemoteBodyAdapter) body, url);
      System.out.println("Success at binding url " + url);
    } else {
      throw new java.io.IOException("Cannot reconize the type of this UniversalBody: " + body.getClass().getName());
    }
  }


  /**
   * Unregisters an active object previously registered into a RMI registry.
   * @param url the url under which the active object is registered.
   * @exception java.io.IOException if the remote object cannot be removed from the registry
   */
  public static void unregister(String url) throws java.io.IOException {
    RemoteBodyAdapter.unregister(url);
    System.out.println("Success at unbinding url " + url);
  }


  /**
   * Looks-up an active object previously registered in a RMI registry. In fact it is the
   * remote version of the body of an active object that can be registered into the 
   * RMI Registry under a given URL. If the lookup is successful, the method reconstructs
   * a Stub-Proxy couple and point it to the RemoteBody found.
   * @param classname the fully qualified name of the class the stub should inherit from.
   * @param url the url under which the remote body is registered.
   * @return a remote reference on a Stub of type <code>classname</code> pointing to the 
   *     remote body found
   * @exception java.io.IOException if the remote body cannot be found under the given url
   *      or if the object found is not of type RemoteBody
   * @exception ActiveObjectCreationException if the stub-proxy couple cannot be created
   */
  public static Object lookupActive(String classname, String url) throws ActiveObjectCreationException, java.io.IOException {
    UniversalBody b = RemoteBodyAdapter.lookup(url);
    try {
      return createStubObject(classname, b);
    } catch (MOPException e) {
      Throwable t = e;
      if (e.getTargetException() != null) t = e.getTargetException();
      throw new ActiveObjectCreationException("Exception occured when trying to create stub-proxy", t);
    }
  }


  /**
   * Blocks the calling thread until the object <code>future</code>
   * is available. <code>future</code> must be the result object of an
   * asynchronous call. Usually the the wait by necessity model take care
   * of blocking the caller thread asking for a result not yet available.
   * This method allows to block before the result is first used.
   */
  public static void waitFor(Object future) {
    // If the object is not reified, it cannot be a future
    if ((MOP.isReifiedObject(future)) == false) {
      return;
    } else {
      org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject)future).getProxy();
      // If it is reified but its proxy is not of type future, we cannot wait
      if (! (theProxy instanceof Future)) {
        return;
      } else {
        ((Future)theProxy).waitFor();
      }
    }
  }


  /**
   * When an active object is created, it is associated with a Body that takes care
   * of all non fonctionnal properties. Assuming that the active object is only 
   * accessed by the different Stub objects, all method calls end-up as Requests sent
   * to this Body. Therefore the only thread calling the method of the active object 
   * is the active thread managed by the body. There is an unique mapping between the
   * active thread and the body responsible for it. From any method in the active object
   * the current thread caller of the method is the active thread. When a reified method wants
   * to get a reference to the Body associated to the active object, it can invoke this
   * method. Assuming that the current thread is the active thread, the associated body
   * is returned.
   * @return the body associated to the active object whose active thread is calling
   *     this method.
   */
  public static Body getBodyOnThis() {
    return AbstractBody.getThreadAssociatedBody();
  }


  /**
   * Returns a Stub-Proxy couple pointing to the local body associated to the active
   * object whose active thread is calling this method.
   * @return a Stub-Proxy couple pointing to the local body.
   * @see #getBodyOnThis
   */
  public static StubObject getStubOnThis() {
    Body body = getBodyOnThis();
    //System.out.println("ProActive: getStubOnThis() returns " + body);
    if (body == null) return null;
    return getStubForBody(body);
  }
  
  
  /**
   * Migrates the active object whose active thread is calling this method to the 
   * same location as the active object given in parameter.
   * This method must be called from an active object using the active thread as the
   * current thread will be used to find which active object is calling the method.
   * The object given as destination must be an active object.
   * @param activeObject the active object indicating the destination of the migration.
   * @exception MigrationException if the migration fails
   * @see #getBodyOnThis
   */
  public static void migrateTo(Object activeObject) throws MigrationException {
    migrateTo(getNodeFromURL(getNodeURLFromActiveObject(activeObject)));
  }
  

  /**
   * Migrates the active object whose active thread is calling this method to the 
   * node caracterized by the given url.
   * This method must be called from an active object using the active thread as the
   * current thread will be used to find which active object is calling the method.
   * The url must be the url of an existing node.
   * @param nodeURL the url of an existing where to migrate to.
   * @exception MigrationException if the migration fails
   * @see #getBodyOnThis
   */
  public static void migrateTo(String nodeURL) throws MigrationException {
    ProActive.migrateTo(getNodeFromURL(nodeURL));
  }


  /**
   * Migrates the active object whose active thread is calling this method to the 
   * given node.
   * This method must be called from an active object using the active thread as the
   * current thread will be used to find which active object is calling the method.
   * @param node an existing node where to migrate to.
   * @exception MigrationException if the migration fails
   * @see #getBodyOnThis
   */
  public static void migrateTo(Node node) throws MigrationException {
    Body bodyToMigrate = getBodyOnThis();
    if (! (bodyToMigrate instanceof Migratable)) {
      throw new MigrationException("This body cannot migrate. It doesn't implement Migratable interface");
    }
    ((Migratable) bodyToMigrate).migrateTo(node);
  }
  

  /**
   * Migrates the given body to the same location as the active object given in parameter.
   * This method can be called from any object and does not perform the migration. 
   * Instead it generates a migration request that is sent to the targeted body.
   * Two strategies are possible :
   *   - the request is high priority and is processed before all existing requests
   *   the body may have received (priority = true)
   *   - the request is normal priority and is processed after all existing requests
   *   the body may have received (priority = false)
   * The object given as destination must be an active object.
   * @param bodyToMigrate the body to migrate.
   * @param activeObject the active object indicating the destination of the migration.
   * @param priority a boolean indicating the priority of the migration request sent to the body.
   * @exception MigrationException if the migration fails
   */
  public static void migrateTo(Body bodyToMigrate, Object activeObject, boolean priority) throws MigrationException {
    migrateTo(bodyToMigrate, getNodeFromURL(getNodeURLFromActiveObject(activeObject)), priority);
  }
  

  /**
   * Migrates the given body to the node caracterized by the given url.
   * This method can be called from any object and does not perform the migration. 
   * Instead it generates a migration request that is sent to the targeted body.
   * Two strategies are possible :
   *   - the request is high priority and is processed before all existing requests
   *   the body may have received (priority = true)
   *   - the request is normal priority and is processed after all existing requests
   *   the body may have received (priority = false)
   * The object given as destination must be an active object.
   * @param bodyToMigrate the body to migrate.
   * @param nodeURL the url of an existing where to migrate to.
   * @param priority a boolean indicating the priority of the migration request sent to the body.
   * @exception MigrationException if the migration fails
   */
  public static void migrateTo(Body bodyToMigrate, String nodeURL, boolean priority) throws MigrationException {
    ProActive.migrateTo(bodyToMigrate, getNodeFromURL(nodeURL), priority);
  }


  /**
   * Migrates the body <code>bodyToMigrate</code> to the given node.
   * This method can be called from any object and does not perform the migration. 
   * Instead it generates a migration request that is sent to the targeted body.
   * Two strategies are possible :
   *   - the request is high priority and is processed before all existing requests
   *   the body may have received (priority = true)
   *   - the request is normal priority and is processed after all existing requests
   *   the body may have received (priority = false)
   * The object given as destination must be an active object.
   * @param bodyToMigrate the body to migrate.
   * @param node an existing node where to migrate to.
   * @param priority a boolean indicating the priority of the migration request sent to the body.
   * @exception MigrationException if the migration fails
   */
  public static void migrateTo(Body bodyToMigrate, Node node, boolean priority) throws MigrationException {
    if (! (bodyToMigrate instanceof Migratable)) {
      throw new MigrationException("This body cannot migrate. It doesn't implement Migratable interface");
    }
    Object[] arguments = { node };
    try {
      BodyRequest request = new BodyRequest(bodyToMigrate, "migrateTo", new Class[] {Node.class}, arguments, priority);
      request.send(bodyToMigrate);
    } catch (NoSuchMethodException e) {
      throw new MigrationException("Cannot find method migrateTo this body. Non sense since the body is instance of Migratable",e);
    } catch (java.io.IOException e) {
      throw new MigrationException("Cannot send the request to migrate",e);
    }
  }



  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private static String getNodeURLFromActiveObject(Object o) throws MigrationException {
    //first we check if the parameter is an active object,
    if (!org.objectweb.proactive.core.mop.MOP.isReifiedObject(o)) {
      throw new MigrationException("The parameter is not an active object");
    }
    //now we get a reference on the remoteBody of this guy
    BodyProxy destProxy = (BodyProxy)((org.objectweb.proactive.core.mop.StubObject)o).getProxy();
    String nodeURL = null;
    return destProxy.getBody().getNodeURL();
  }
  
  private static Node getNodeFromURL(String url) throws MigrationException {
    try {
      return NodeFactory.getNode(url);
    } catch (NodeException e) {
      throw new MigrationException("The node of given URL "+url+" cannot be localized", e);
    }
  }



  // -------------------------------------------------------------------------------------------
  // 
  // STUB CREATION
  // 
  // -------------------------------------------------------------------------------------------
  

  private static StubObject getStubForBody(Body body) {
    try  {
      return createStubObject(body.getReifiedObject(), new Object[] { body }, body.getReifiedObject().getClass().getName());
    } catch (MOPException e) {
      throw new ProActiveRuntimeException("Cannot create Stub for this Body e="+e);
    }
  }
  
  private static Object createStubObject(String className, UniversalBody body) throws MOPException {
    return createStubObject(className, null, new Object[] { body });
  }
  
  private static Object createStubObject(String className, Object[] constructorParameters, Node node) throws MOPException {
    // Creates the reified object
    Class bodyClass = null;
    try  {
      bodyClass = determineBodyClass(Class.forName(className));
    } catch (ClassNotFoundException e) {
      throw new ConstructionOfProxyObjectFailedException("Class can't be found e="+e);
    }
    return createStubObject(className, constructorParameters, new Object[] { bodyClass, node});
  }
  
  private static Object createStubObject(String className, Object[] constructorParameters, Object[] proxyParameters) throws MOPException {
    try {
      try {
        return MOP.newInstance(className, constructorParameters, proxyParameters);
      } catch (CannotGuessProxyNameException e) {
        // If we can't guess a proxy name, let's use the default proxy
        // System.err.println("Cannot guess proxy name for class " + className + ", using default proxy " + Constants.DEFAULT_BODY_PROXY_CLASS_NAME);
        return MOP.newInstance(className, constructorParameters, Constants.DEFAULT_BODY_PROXY_CLASS_NAME, proxyParameters);
      }
    } catch (ClassNotFoundException e) {
      throw new ConstructionOfProxyObjectFailedException("Class can't be found e="+e);
    }
  }
  


  private static Object createStubObject(Object target, Node node, String nameOfTargetType) throws MOPException {
    Class bodyClass = determineBodyClass(target.getClass());
    return createStubObject(target, new Object[] { bodyClass, node}, nameOfTargetType);
  }
  

  private static Object createStubObject(Object target, Node node, String nameOfBodyClass, String nameOfProxyClass) throws MOPException {
    Class bodyClass = null;
      if (nameOfBodyClass != null) bodyClass = findBodyClassFromString(nameOfBodyClass);
    if (bodyClass == null) bodyClass = determineBodyClass(target.getClass());
    if (nameOfProxyClass == null) nameOfProxyClass = Constants.DEFAULT_BODY_PROXY_CLASS_NAME;
    Object[] proxyParameters = new Object[] { bodyClass, node};
    try {
      return MOP.turnReified(nameOfProxyClass, proxyParameters, target);
    } catch (ClassNotFoundException e) {
      throw new ConstructionOfProxyObjectFailedException("Class can't be found e="+e);
    }
  }


 private static StubObject createStubObject(Object object, Object[] proxyParameters, String nameOfTargetType) throws MOPException {
   try {
     try {
       return (StubObject) MOP.turnReified(proxyParameters, nameOfTargetType, object);
     } catch (CannotGuessProxyNameException e) {
       return (StubObject) MOP.turnReified(nameOfTargetType, Constants.DEFAULT_BODY_PROXY_CLASS_NAME, proxyParameters, object);
     }
   } catch (ClassNotFoundException e) {
     throw new ConstructionOfProxyObjectFailedException("Class can't be found e="+e);
   }
 }
 
  
 /**
  * The class of the body object is determined as follows<BR><UL>
  * <LI>If the reified class (whose name is contained in the ConstructorCall object
  * passed as a parameter) has a static field of type String whose name
  * is the same as the one given by MAGIC_STRING, then this String is used
  * as the name of the body class.
  * <LI>If this field does not exist in the reified class or if the
  * instantiation of the specified skeleton fails in any way, then
  * a defaul body is created as an instance of the class whose name is
  * given by DEFAULT_CLASS_NAME</LI></UL>
  */
  private static Class determineBodyClass(Class reifiedObjectClass) throws ConstructionOfProxyObjectFailedException {
    String s = null;
    try {
      java.lang.reflect.Field f = resolveAmbiguity(reifiedObjectClass);
      if (f == null) return Constants.DEFAULT_BODY_CLASS;
      s = (String)f.get(reifiedObjectClass);
      Class bodyClass = Class.forName(s);
      if (! checkBodyInheritance(bodyClass)) {
        // The body interface has not been found
        throw new ConstructionOfProxyObjectFailedException("Class " + bodyClass.getName() + " does not implement " + Constants.DEFAULT_BODY_INTERFACE_NAME);
      }
      return bodyClass;
    } catch (ClassNotFoundException e) {
      throw new ConstructionOfProxyObjectFailedException("Variable " + Constants.BODY_CLASS_NAME_FIELD + " in class " + reifiedObjectClass.getName() + " specifies class " + s + " which cannot be found. Using default body instead.");
    } catch (IllegalArgumentException e) {
      throw new ConstructionOfProxyObjectFailedException("Field " + Constants.BODY_CLASS_NAME_FIELD + " does not exist in class " + reifiedObjectClass.getName() + ", although getField tells the contrary");
    } catch (IllegalAccessException e) {
      throw new ConstructionOfProxyObjectFailedException("Field " + Constants.BODY_CLASS_NAME_FIELD + " not accessible in class " + reifiedObjectClass.getName());
    }
  }


  private static Class findBodyClassFromString(String bodyClassName) throws ConstructionOfProxyObjectFailedException {
    try {
      Class bodyClass = Class.forName(bodyClassName);
      if (checkBodyInheritance(bodyClass)) {
        return bodyClass;
      } else {
        // The body interface has not been found
        throw new ConstructionOfProxyObjectFailedException("Class " + bodyClassName + " does not implement " + Constants.DEFAULT_BODY_INTERFACE_NAME);
      }
    } catch (ClassNotFoundException e) {
      //System.err.println("Second parameter passed to BodyProxy (name of the Body class) is invalid: class " + s + " does not exist. Proceeding.");
      //Thread.dumpStack();
      return null;
    }
  }
  

  private static boolean checkBodyInheritance(Class c) {
    // we check if this class implements 'Body' or one of its subinterfaces
    Class currentClass = c;
    Class myInterface = null;
    while ((currentClass != null) && (myInterface == null)) {
      boolean multipleMatches = false;
      Class[] interfaces = currentClass.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        if (Constants.DEFAULT_BODY_INTERFACE.isAssignableFrom(interfaces[i])) {
          if (multipleMatches == false) {
            myInterface = interfaces[i];
            multipleMatches = true;
          } else {
            // There are multiple interfaces in the current class
            // that inherit from bodyinterface.
            System.err.println("More than one interfaces declared in class " + currentClass.getName() + " inherit from " + Constants.DEFAULT_BODY_INTERFACE + ". Using " + myInterface);
          }
        }
      }
      currentClass = currentClass.getSuperclass();
    }
    return (myInterface != null);
  }


  /**
   * Try to solve the Multi-interface field problem
   */
  private static java.lang.reflect.Field resolveAmbiguity(Class reifiedObjectClass) {
    java.lang.reflect.Field[] f;
    java.util.Vector f2 = new java.util.Vector();
    java.lang.reflect.Field winner = null;
    f = reifiedObjectClass.getFields();
    //now we look in the array for the BODY_CLASS_NAME_FIELD
    for (int i = 0; i < f.length; i++) {
      if (f[i].getName().equals(Constants.BODY_CLASS_NAME_FIELD)) {
        f2.addElement(f[i]);
        //	System.out.println("    BodyProxy: resolveAmbiguity()  found = " + f[i]);
      }
    }
    //then, we try to solve the ambiguity when many different interfaces have this field, 
    //we get the bottom most in the inheritance tree which implements active	
    java.lang.reflect.Field tmp;
    for (java.util.Enumeration e = f2.elements(); e.hasMoreElements();) {
      tmp = (java.lang.reflect.Field)e.nextElement();
      if (winner == null)
        winner = tmp;
      else {
        Class tmpDeclaringClass = tmp.getDeclaringClass();
        Class winnerDeclaringClass = winner.getDeclaringClass();
        //System.out.println("         Comparing winner = " + winnerDeclaringClass + " and " + tmpDeclaringClass );
        if (winnerDeclaringClass.isAssignableFrom(tmpDeclaringClass)) {
          winner = tmp;
          //System.out.println("          Found winner " + winner);
        }
      }
    }
    return winner;
  }


}

