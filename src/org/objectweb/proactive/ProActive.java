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

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.body.request.BodyRequest;
import org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorHandler;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.RuntimeFactory;

/**
 * <p>
 * The ProActive class provides a set of static services through static method calls.
 * It is the main entry point for users of ProActive as they will call methods of
 * this class to create active object, to migrate them or to register them to the
 * RMI Registry.
 * </p><p>
 * The main role of <code>ProActive</code> is to provides methods to create active objects.
 * It is possible to create an active object through instantiation using one of the version
 * of <code>newActive</code>. It is also possible to create an active object from an existing
 * object using one of the version of <code>turnActive</code>.
 * </p>
 * <h2>Defining Active Object Activity</h2>
 * <p>
 * When an active object is created from a given object, all method calls are sent as requests 
 * to the body (automatically associated to the active object). Those requests are stored in a
 * queue waiting to be served. Serving those requests as well as performing some 
 * other work represent the activity of this active object. One can completely specify what will
 * be this activity. The standard behavior is to serve all incoming requests one by one in a FIFO
 * order.
 * </p><p>
 * In addition to the activity, it is possible to specify what to do before the activity starts,
 * and after it ends. The three steps are :
 * <ul>
 * <li>the initialization of the activity (done only once)</li>
 * <li>the activity itself</li>
 * <li>the end of the activity (done only once)</li>
 * </ul>
 * </p><p>
 * Three interfaces are used to define and implement each step :
 * <ul>
 * <li><a href="InitActive.html">InitActive</a></li>
 * <li><a href="RunActive.html">RunActive</a></li>
 * <li><a href="EndActive.html">EndActive</a></li>
 * </ul>
 * </p><p>
 * In case of a migration, an active object stops and restarts its activity
 * automatically without invoking the init or ending phases. Only the
 * activity itself is restarted.
 * </p>
 * <h2>Setting Active Object Activity</h2>
 * <p>
 * Two ways are possible to define each of the three phases of an active object.
 * </p>
 * <ul>
 * <li>Implementing one or more of the three interfaces directly in the class used to create
 * the active object</li>
 * <li>Passing an object implementing one or more of the three interfaces in parameter to the method
 * <code>newActive</code> or <code>turnActive</code> (parameter active in those methods)</li>
 * </ul>
 * </p><p>
 * Note that the methods defined by those 3 interfaces are guaranted to be called by the active 
 * thread of the active object.
 * </p><p>
 * The algorithms that decide for each phase what to do are the following (<code>activity</code> is 
 * the eventual object passed as a parameter to <code>newActive</code> or <code>turnActive</code>) :
 * </p>
 * <h3>InitActive</h3>
 * <pre>
 * if activity is non null and implements InitActive
 *   we invoke the method initActivity defined in the object activity
 * else if the class of the reified object implements InitActive
 *   we invoke the method initActivity of the reified object
 * else
 *   we don't do any initialization
 * </pre>
 * 
 * <h3>RunActive</h3>
 * <pre>
 * if activity is non null and implements RunActive
 *   we invoke the method runActivity defined in the object activity
 * else if the class of the reified object implements RunActive
 *   we invoke the method runActivity of the reified object
 * else
 *   we run the standard FIFO activity
 * </pre>
 * 
 * <h3>EndActive</h3>
 * <pre>
 * if activity is non null and implements EndActive
 *   we invoke the method endActivity defined in the object activity
 * else if the class of the reified object implements EndActive
 *   we invoke the method endActivity of the reified object
 * else
 *   we don't do any cleanup
 * </pre>
 * <p>
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

  static {
    Class c = org.objectweb.proactive.core.runtime.RuntimeFactory.class;
  }

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  private ProActive() {
  }

  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  /**
   * Creates a new ActiveObject based on classname attached to a default node in the local JVM.
   * @param classname the name of the class to instanciate as active
   * @param constructorParameters the parameters of the constructor.
   * @return a reference (possibly remote) on a Stub of the newly created active object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the DefaultNode cannot be created
   */
  public static Object newActive(String classname, Object[] constructorParameters)
    throws ActiveObjectCreationException, NodeException {
    return newActive(classname, constructorParameters, null, null, null);
  }

  /**
   * Creates a new ActiveObject based on classname attached to the node of the given URL.
   * @param classname the name of the class to instanciate as active
   * @param constructorParameters the parameters of the constructor.
   * @param nodeURL the URL of the node where to create the active object. If null, the active object 
   *       is created localy on a default node 
   * @return a reference (possibly remote) on a Stub of the newly created active object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node URL cannot be resolved as an existing Node
   */
  public static Object newActive(String classname, Object[] constructorParameters, String nodeURL)
    throws ActiveObjectCreationException, NodeException {
    if (nodeURL == null) {
      return newActive(classname, constructorParameters, null, null, null);
    } else {
      return newActive(classname, constructorParameters, NodeFactory.getNode(nodeURL), null, null);
    }
  }

  /**
   * Creates a new ActiveObject based on classname attached to the given node or on
   * a default node in the local JVM if the given node is null.
   * @param classname the name of the class to instanciate as active
   * @param constructorParameters the parameters of the constructor.
   * @param node the possibly null node where to create the active object.
   * @return a reference (possibly remote) on a Stub of the newly created active object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object newActive(String classname, Object[] constructorParameters, Node node)
    throws ActiveObjectCreationException, NodeException {
    return newActive(classname, constructorParameters, node, null, null);
  }

  /**
   * Creates a new ActiveObject based on classname attached to the given node or on
   * a default node in the local JVM if the given node is null.
   * The object returned is a stub class that extends the target class and that is automatically 
   * generated on the fly. The Stub class reference a the proxy object that reference the body
   * of the active object. The body referenced by the proxy can either be local of remote, 
   * depending or the respective location of the object calling the newActive and the active object
   * itself. 
   * @param classname the name of the class to instanciate as active
   * @param constructorParameters the parameters of the constructor of the object
   *    to instantiate as active. If some parameters are primitive types, the wrapper 
   *    class types should be given here. null can be used to specify that no parameter
   *    are passed to the constructor.
   * @param node the possibly null node where to create the active object. If null, the active object 
   *       is created localy on a default node 
   * @param activity the possibly null activity object defining the different step in the activity of the object.
   *               see the definition of the activity in the javadoc of this classe for more information. 
   * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
   *                body associated to the reified object. If null the default ProActive MataObject factory is used.
   * @return a reference (possibly remote) on a Stub of the newly created active object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object newActive(
    String classname,
    Object[] constructorParameters,
    Node node,
    Active activity,
    MetaObjectFactory factory)
    throws ActiveObjectCreationException, NodeException {
    //using default proactive node
    if (node == null) {
      node = NodeFactory.getDefaultNode();
    }
    if (factory == null) {
      factory = ProActiveMetaObjectFactory.newInstance();
    }
    try {
      return createStubObject(classname, constructorParameters, node, activity, factory);
    } catch (MOPException e) {
      Throwable t = e;
      if (e.getTargetException() != null)
        t = e.getTargetException();
      throw new ActiveObjectCreationException(t);
    }
  }
  
	/**
	 * Creates a new ActiveObject based on classname attached to the given virtualnode.
	 * @param classname classname the name of the class to instanciate as active
	 * @param constructorParameters constructorParameters the parameters of the constructor.
	 * @param virtualnode The virtualnode where to create active objects. Active objects will be created 
	 * on each node mapped to the given virtualnode in XML deployment descriptor.
	 * @return Object[] an array of references (possibly remote) on  Stub of newly created active objects
	 * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
	 * @throws NodeException if the virtualnode was null
	 */
  public static Object[] newActive(String classname, Object[] constructorParameters, VirtualNode virtualnode) throws ActiveObjectCreationException, NodeException{
  	if (virtualnode != null){
  		Node[] nodeTab = virtualnode.getNodes();
  		Object[] aoTab = new Object[nodeTab.length];
  		for (int i = 0; i < nodeTab.length; i++)
			{
			 Object tmp = newActive(classname,constructorParameters,(Node)nodeTab[i]);
			 aoTab[i] = tmp;
			}
			return aoTab;
  	}else{
  		throw new NodeException("VirtualNode is null, unable to active the object");
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
      return turnActive(target, target.getClass().getName(), null, null, null);
    } else {
      return turnActive(target, target.getClass().getName(), NodeFactory.getNode(nodeURL), null, null);
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
    return turnActive(target, target.getClass().getName(), node, null, null);
  }

  /**
   * Turns the target object into an Active Object and send it to the given Node
   * or to a default node in the local JVM if the given node is null.
   * The type of the stub is is the type of the target object.
   * @param target The object to turn active
   * @param node The Node the object should be sent to or null to create the active 
   *       object in the local JVM
   * @param activity the possibly null activity object defining the different step in the activity of the object.
   *               see the definition of the activity in the javadoc of this classe for more information. 
   * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
   *                body associated to the reified object. If null the default ProActive MataObject factory is used.
   * @return a reference (possibly remote) on a Stub of the target object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object turnActive(Object target, Node node, 
      Active activity,
      MetaObjectFactory factory)
    throws ActiveObjectCreationException, NodeException {
    return turnActive(target, target.getClass().getName(), node, activity, factory);
  }

  /**
   * Turns a Java object into an Active Object and send it to a remote Node or to a 
   * local node if the given node is null.
   * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
   * @param target The object to turn active
   * @param nameOfTargetType the fully qualified name of the type the stub class should
   * inherit from. That type can be less specific than the type of the target object.
   * @param node The Node the object should be sent to or null to create the active 
   *       object in the local JVM
   * @return a reference (possibly remote) on a Stub of the target object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object turnActive(Object target, String nameOfTargetType, Node node)
    throws ActiveObjectCreationException, NodeException {
    return turnActive(target, nameOfTargetType, node, null, null);
  }

  /**
   * Turns a Java object into an Active Object and send it to a remote Node or to a 
   * local node if the given node is null.
   * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
   * A Stub is dynamically generated for the existing object. The result of the call 
   * will be an instance of the Stub class pointing to the proxy object pointing
   * to the body object pointing to the existing object. The body can be remote 
   * or local depending if the existing is sent remotely or not.
   * @param target The object to turn active
   * @param nameOfTargetType the fully qualified name of the type the stub class should
   * inherit from. That type can be less specific than the type of the target object.
   * @param node The Node the object should be sent to or null to create the active 
   *       object in the local JVM
   * @param activity the possibly null activity object defining the different step in the activity of the object.
   *               see the definition of the activity in the javadoc of this classe for more information. 
   * @param factory the possibly null meta factory giving all factories for creating the meta-objects part of the
   *                body associated to the reified object. If null the default ProActive MataObject factory is used.
   * @return a reference (possibly remote) on a Stub of the target object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object turnActive(Object target, String nameOfTargetType, Node node, 
      Active activity,
      MetaObjectFactory factory)
    throws ActiveObjectCreationException, NodeException {
    if (node == null) {
      //using default proactive node
      node = NodeFactory.getDefaultNode();
    }
    if (factory == null) {
      factory = ProActiveMetaObjectFactory.newInstance();
    }
    try {
      return createStubObject(target, nameOfTargetType, node, activity, factory );
    } catch (MOPException e) {
      Throwable t = e;
      if (e.getTargetException() != null)
        t = e.getTargetException();
      throw new ActiveObjectCreationException(t);
    }
  }

  
  
  /**
   * Turns a Java object into an Active Object and send it to remote Nodes mapped to the given virtualnode in
   * the XML deployment descriptor.
   * The type of the stub is given by the parameter <code>nameOfTargetType</code>.
   * @param target The object to turn active
   * @param nameOfTargetType the fully qualified name of the type the stub class should
   * inherit from. That type can be less specific than the type of the target object.
   * @param virtualnode The VirtualNode where the target object will be turn into an Active Object
   * Target object will be turned into an Active Object on each node mapped to the given virtualnode in XML deployment descriptor.
   * @return an array of references (possibly remote) on a Stub of the target object
   * @exception ActiveObjectCreationException if a problem occur while creating the stub or the body
   * @exception NodeException if the node was null and that the DefaultNode cannot be created
   */
  public static Object[] turnActive(Object target, String nameOfTargetType, VirtualNode virtualnode)
    throws ActiveObjectCreationException, NodeException {
    	if (virtualnode != null){
  		Node[] nodeTab = virtualnode.getNodes();
  		Object[] aoTab = new Object[nodeTab.length];
  		for (int i = 0; i < nodeTab.length; i++)
			{
        Object tmp = turnActive(target, nameOfTargetType, (Node)nodeTab[i], null, null);
        aoTab[i] = tmp;
      }
			return aoTab;
  	}else{
  		throw new NodeException("VirtualNode is null, unable to active the object");
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
      throw new java.io.IOException("The given object " + obj + " is not a reified object");
    }
    // Find the appropriate remoteBody
    org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();
    if (myProxy == null)
      throw new java.io.IOException("Cannot find a Proxy on the stub object: " + obj);
    BodyProxy myBodyProxy = (BodyProxy) myProxy;
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
  public static Object lookupActive(String classname, String url)
    throws ActiveObjectCreationException, java.io.IOException {
    UniversalBody b = RemoteBodyAdapter.lookup(url);
    try {
      return createStubObject(classname, b);
    } catch (MOPException e) {
      Throwable t = e;
      if (e.getTargetException() != null)
        t = e.getTargetException();
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
      org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();
      // If it is reified but its proxy is not of type future, we cannot wait
      if (!(theProxy instanceof Future)) {
        return;
      } else {
        ((Future) theProxy).waitFor();
      }
    }
  }
  
  
  /**
   * Returns a <code>ProActiveDescriptor</code> that gives an object representation
   * of the XML document located at the given url.
   * @param xmlDescriptorUrl. The url of the XML document
   * @return ProActiveDescriptor. The object representation of the XML document
   * @throws ProActiveException if a problem occurs during the creation of the object
   * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor
   * @see org.objectweb.proactive.core.descriptor.data.VirtualNode
   * @see org.objectweb.proactive.core.descriptor.data.VirtualMachine
   */
  public static ProActiveDescriptor getProactiveDescriptor(String xmlDescriptorUrl) throws ProActiveException{
      RuntimeFactory.getDefaultRuntime();
      try{
      	System.out.println("************* Reading deployment descriptor: "+xmlDescriptorUrl+" ********************");
        ProActiveDescriptorHandler proActiveDescriptorHandler = ProActiveDescriptorHandler.createProActiveDescriptor(xmlDescriptorUrl);
        return (ProActiveDescriptor) proActiveDescriptorHandler.getResultObject();
      }catch (org.xml.sax.SAXException e){
      e.printStackTrace();
      System.out.println("a problem occurs when getting the proactiveDescriptor");
      throw new ProActiveException(e);
      } 
      catch(java.io.IOException e){
      e.printStackTrace();
      System.out.println("a problem occurs during the ProactiveDescriptor object creation");
      throw new ProActiveException(e);
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
    return LocalBodyStore.getInstance().getCurrentThreadBody();
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
    if (body == null)
      return null;
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
    if (!(bodyToMigrate instanceof Migratable)) {
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
    if (!(bodyToMigrate instanceof Migratable)) {
      throw new MigrationException("This body cannot migrate. It doesn't implement Migratable interface");
    }
    Object[] arguments = { node };
    try {
      BodyRequest request =
        new BodyRequest(bodyToMigrate, "migrateTo", new Class[] { Node.class }, arguments, priority);
      request.send(bodyToMigrate);
    } catch (NoSuchMethodException e) {
      throw new MigrationException(
        "Cannot find method migrateTo this body. Non sense since the body is instance of Migratable",
        e);
    } catch (java.io.IOException e) {
      throw new MigrationException("Cannot send the request to migrate", e);
    }
  }

  


	/**
	 * Blocks the calling thread until one of the futures in the vector is available.
	 * THIS METHOD MUST BE CALLED FROM AN ACTIVE OBJECT.
	 * @param futures vector of futures
	 * @return index of the available future in the vector
	 */
	public static int waitForAny(java.util.Vector futures) {
		FuturePool fp = getBodyOnThis().getFuturePool();
		synchronized (fp) {
			while (true) {
				java.util.Iterator it = futures.iterator();
				int index = 0;
				while (it.hasNext()) {
					Object current = it.next();
					if (!(isAwaited(current))) {
						return index;
					}
					index++;
				}
				fp.waitForReply();
			}
		}
	}		
	
	/**
	 * Blocks the calling thread until all futures in the vector are available.
	 * THIS METHOD MUST BE CALLED FROM AN ACTIVE OBJECT.
	 * @param futures vector of futures
	 */
	public static void waitForAll(java.util.Vector futures){
		FuturePool fp = getBodyOnThis().getFuturePool();
		synchronized (fp) {
			boolean oneIsMissing = true;
			while (oneIsMissing) {
				oneIsMissing = false;
				java.util.Iterator it = futures.iterator();
				while (it.hasNext()) {
					Object current = it.next();
					if (isAwaited(current)) {
						oneIsMissing = true;
					}
				}
				if (oneIsMissing) {
					fp.waitForReply();
				}
			}	
		}	
	}


	/**
	 * Blocks the calling thread until the N-th of the futures in the vector is available.
	 * THIS METHOD MUST BE CALLED FROM AN ACTIVE OBJECT.
	 * @param futures vector of futures
	 */
	public static void waitForTheNth(java.util.Vector futures, int n) {
		FuturePool fp = getBodyOnThis().getFuturePool();
		synchronized (fp) {
			Object current = futures.get(n);
			if (isAwaited(current)) {
				waitFor(current);
			}
		}
	}	



	/**
	 * Return false if the object <code>future</code> is available.
	 * This method is recursive, i.e. if result of future is a future too,
	 * <CODE>isAwaited</CODE> is called again on this result, and so on.
	 */
	public static boolean isAwaited(Object future) {
		// If the object is not reified, it cannot be a future
		if ((MOP.isReifiedObject(future)) == false) {
			return false;
		} else {
			org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();
			// If it is reified but its proxy is not of type future, we cannot wait
			if (!(theProxy instanceof Future)) {
				return false;
			} else {
				if (((Future) theProxy).isAwaited()) {
					return true;
				} else {
					return isAwaited(((Future)theProxy).getResult());
				}
			}
		}
	}


	/**
	 * Return the object contains by the future (ie its target).
	 * If parameter is not a future, it is returned.
	 * A wait-by-necessity occurs if future is not available. 
	 * This method is recursive, i.e. if result of future is a future too,
	 * <CODE>getFutureValue</CODE> is called again on this result, and so on.
	 */
	public static Object getFutureValue(Object future) {
		// If the object is not reified, it cannot be a future
		if ((MOP.isReifiedObject(future)) == false) {
			return future;
		} else {
			org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();
			// If it is reified but its proxy is not of type future, we cannot wait
			if (!(theProxy instanceof Future)) {
				return future;
			} else {
				Object o = ((Future) theProxy).getResult();
				return getFutureValue(o);
			}
		}
	}

	/** 
	 * Enable the automatic continuation mechanism for this active object.
	 */
	public static void enableAC(Object obj) throws java.io.IOException {
		// Check if obj is really a reified object
		if (!(MOP.isReifiedObject(obj))) {
			throw new ProActiveRuntimeException("The given object " + obj + " is not a reified object");
		}
		// Find the appropriate remoteBody
		org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();
		if (myProxy == null)
			throw new ProActiveRuntimeException("Cannot find a Proxy on the stub object: " + obj);
		BodyProxy myBodyProxy = (BodyProxy) myProxy;
		UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();
		body.enableAC();
	}

	/** 
	 * Disable the automatic continuation mechanism for this active object.
	 */
	public static void disableAC(Object obj) throws java.io.IOException {
		// Check if obj is really a reified object
		if (!(MOP.isReifiedObject(obj))) {
			throw new ProActiveRuntimeException("The given object " + obj + " is not a reified object");
		}
		// Find the appropriate remoteBody
		org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();
		if (myProxy == null)
			throw new ProActiveRuntimeException("Cannot find a Proxy on the stub object: " + obj);
		BodyProxy myBodyProxy = (BodyProxy) myProxy;
		UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();
		body.disableAC();
	}

	/**
	 * Set an immmediate execution for the active object obj, ie request of name methodName
	 * will be executed by the calling thread, and not add in the request queue.
	 * BE CAREFULL : for the first release of this method, do not make use of getCurrentThreadBody nor 
	 * getStubOnThis in the method defined by methodName !!
	 */
	public static void setImmediateService(Object obj, String methodName) throws java.io.IOException{
		 // Check if obj is really a reified object
		if (!(MOP.isReifiedObject(obj))) {
			throw new ProActiveRuntimeException("The given object " + obj + " is not a reified object");
		}
		// Find the appropriate remoteBody
		org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();
		if (myProxy == null)
			throw new ProActiveRuntimeException("Cannot find a Proxy on the stub object: " + obj);
		BodyProxy myBodyProxy = (BodyProxy) myProxy;
		UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();
		body.setImmediateService(methodName);
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
    BodyProxy destProxy = (BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) o).getProxy();
    return destProxy.getBody().getNodeURL();
  }

  private static Node getNodeFromURL(String url) throws MigrationException {
    try {
      return NodeFactory.getNode(url);
    } catch (NodeException e) {
      throw new MigrationException("The node of given URL " + url + " cannot be localized", e);
    }
  }

  // -------------------------------------------------------------------------------------------
  // 
  // STUB CREATION
  // 
  // -------------------------------------------------------------------------------------------

  private static StubObject getStubForBody(Body body) {
    try {
      return createStubObject(
        body.getReifiedObject(),
        new Object[] { body },
        body.getReifiedObject().getClass().getName());
    } catch (MOPException e) {
      throw new ProActiveRuntimeException("Cannot create Stub for this Body e=" + e);
    }
  }

  private static Object createStubObject(String className, UniversalBody body) throws MOPException {
    return createStubObject(className, null, new Object[] { body });
  }

  private static Object createStubObject(
    String className,
    Object[] constructorParameters,
    Node node,
    Active activity,
    MetaObjectFactory factory)
    throws MOPException {
    return createStubObject(className, constructorParameters, new Object[] { node, activity, factory });
  }

  private static Object createStubObject(String className, Object[] constructorParameters, Object[] proxyParameters)
    throws MOPException {
    try {
      return MOP.newInstance(
        className,
        constructorParameters,
        Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
        proxyParameters);
    } catch (ClassNotFoundException e) {
      throw new ConstructionOfProxyObjectFailedException("Class can't be found e=" + e);
    }
  }

  private static Object createStubObject(Object target, String nameOfTargetType, Node node, Active activity, MetaObjectFactory factory)
    throws MOPException {
    return createStubObject(target, new Object[] { node, activity, factory }, nameOfTargetType);
  }

  private static StubObject createStubObject(Object object, Object[] proxyParameters, String nameOfTargetType)
    throws MOPException {
    try {
      return (StubObject) MOP.turnReified(
        nameOfTargetType,
        Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
        proxyParameters,
        object);
    } catch (ClassNotFoundException e) {
      throw new ConstructionOfProxyObjectFailedException("Class can't be found e=" + e);
    }
  }

	

}
