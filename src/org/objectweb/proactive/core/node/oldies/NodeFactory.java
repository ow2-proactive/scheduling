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
package org.objectweb.proactive.core.node.oldies;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.rmi.ClassServerHelper;

/**
 * <p>
 * The <code>NodeFactory</code> provides a generic way to create and lookup <code>Node</code>
 * without protocol specific code (such as RMI or Jini).
 * </p><p>
 * <code>NodeFactory</code> provides a set of static methods to create and lookup <code>Node</code>
 * and to associate protocol specific factory to concrete protocols. To create a node it is only
 * necessary to associate the protocol in the node url. For instance :
 * </p>
 * <pre>
 *    rmi://localhost/node1
 *    jini://localhost/node2
 * </pre>
 * <p>
 * As long as a protocol specific factory has been registered to this <code>NodeFactory</code> for the
 * given protocol, the creation of the node will be delegated to the right factory.
 * </p><p>
 * This class also provide the concept of default node and default protocol. When the protocol is not
 * specified in the node URL, the default protocol is used. When a active object is created in the local
 * JVM but without being attached to any node, a default node is created to hold that active object.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/03/21
 * @since   ProActive 0.9
 *
 */
public abstract class NodeFactory {

  private static final String DEFAULT_NODE_NAME = "//localhost/Node";

  // test with class loader
  //private static final ClassLoader myClassLoader = new NodeClassLoader();

  /** the table where associations Protocol - Factory are kept */
  private static java.util.HashMap protocolFactoryMapping = new java.util.HashMap();
  private static java.util.HashMap instanceFactoryMapping = new java.util.HashMap();
  private static Node defaultNode = null;

  private static NodeFactory defaultNodeFactory;

  public static boolean JINI_ENABLED;
 
  static {
    createClassServer();
    JINI_ENABLED = isJiniEnabled();
    registerProtocolFactories();
  }
  

  //
  // -- PUBLIC METHODS - STATIC -----------------------------------------------
  //

  /**
   * Associates the factory of class <code>factoryClassName</code> as the factory to create
   * nodes for the given protocol. Replaces any previous association.
   * @param <code>protocol</code> the protocol to associate the factory to
   * @param <code>factoryClassName</code> the fully qualified name of the class of the factory
   * responsible of creating the nodes for that protocol
   */
  public static synchronized void setFactory(String protocol, String factoryClassName) {
    protocolFactoryMapping.put(protocol, factoryClassName);
  }


  /**
   * Associates the factory of class <code>factoryClassName</code> as the factory to create
   * nodes for the given protocol. Replaces any previous association.
   * @param <code>protocol</code> the protocol to associate the factory to
   * @param <code>factoryObject</code> the class of the factory
   * responsible of creating the nodes for that protocol
   */
  public static synchronized void setFactory(String protocol, NodeFactory factoryObject) {
    protocolFactoryMapping.put(protocol, factoryObject.getClass().getName());
    instanceFactoryMapping.put(protocol, factoryObject);
  }


  /**
   * Returns the reference to the default node associated to the current JVM
   * If no default node yet exists, it creates a new one using the property
   * <code>org.objectweb.proactive.defaultnode</code>.
   * @return a reference to the default node associated to this JVM
   * @exception NodeException if the default node cannot be instantiated
   */
  public static synchronized Node getDefaultNode() throws NodeException {
    if (defaultNode == null) {
      defaultNode = createDefaultNode(DEFAULT_NODE_NAME);
    }
    return defaultNode;
  }


  /**
   * Returns true if the given node belongs to this JVM false else.
   * @return true if the given node belongs to this JVM false else
   */
  public static boolean isNodeLocal(Node node) {
    return node.getNodeInformation().getVMID().equals(UniqueID.getCurrentVMID());
  }


  /**
   * Creates a new node on the local machine. This call can only be used
   * to create a node on the local JVM on the local machine.
   * The node URL can be in the form
   * <ul>
   * <li>///nodeName</li>
   * <li>//localhost/nodeName</li>
   * <li>//<i>&lt;hostname></i>/nodeName</li>
   * </ul>
   * where <i>&lt;hostname></i> is the name of the localhost.
   * @param <code>nodeURL</code> the URL of the node to create
   * @return the newly created node on the local JVM
   * @exception NodeException if the node cannot be created
   */
  public static Node createNode(String nodeURL) throws NodeException {
    return createNode(nodeURL, false);
  }

  /**
   * Creates a new node on the local machine. This call can only be used
   * to create a node on the local JVM on the local machine.
   * The node URL can be in the form
   * <ul>
   * <li>///nodeName</li>
   * <li>//localhost/nodeName</li>
   * <li>//<i>&lt;hostname></i>/nodeName</li>
   * </ul>
   * where <i>&lt;hostname></i> is the name of the localhost.
   * @param <code>nodeURL</code> the URL of the node to create
   * @return the newly created node on the local JVM
   * @exception NodeException if the node cannot be created
   */
  public static Node createNode(String nodeURL, boolean replacePreviousBinding) throws NodeException {
    //System.out.println("NodeFactory: createNode(" + nodeURL+ ")");
    //first look for the factory
    String protocol = getProtocol(nodeURL);
    NodeFactory factory = getFactory(protocol);
    //then create a node
    Node node = factory.createNodeImpl(removeProtocol(nodeURL,protocol), replacePreviousBinding);
    if (node == null) throw new NodeException("Cannot create a Node based on "+nodeURL);
    return node;
  }


	/**
	 * Returns the reference to the node located at the given url.
	 * This url can be either local or remote.
	 * @param nodeURL. The url of the node
	 * @return Node. The reference of the node
	 * @throws NodeException if the node cannot be found
	 */
  public static Node getNode(String nodeURL) throws NodeException {
    // System.out.println("NodeFactory: getNode() for " + nodeURL);
    //do we have any association for this node?
    String protocol = getProtocol(nodeURL);
    NodeFactory factory = getFactory(protocol);
    //		System.out.println("NodeFactory: getNode " + s + " got factory " + tmp);
    return factory.getNodeImpl(removeProtocol(nodeURL, protocol));
  }


  //
  // -- PROTECTED METHODS -----------------------------------------------
  //


  /**
   * Creates a new node on the local machine. This call can only be used
   * to create a node on the local JVM on the local machine.
   * The node URL can be in the form
   * <ul>
   * <li>///nodeName</li>
   * <li>//localhost/nodeName</li>
   * <li>//<i>&lt;hostname></i>/nodeName</li>
   * </ul>
   * where <i>&lt;hostname></i> is the name of the localhost.
   * @param <code>nodeURL</code> the URL of the node to create
   * @param <code>replacePreviousBinding</code>
   * @return the newly created node on the local JVM
   * @exception NodeException if the node cannot be created
   */
  protected abstract Node createNodeImpl(String nodeURL, boolean replacePreviousBinding) throws NodeException;


  /**
   * Creates the default node of the local JVM.
   * @return the newly created default node on the local JVM
   * @exception NodeException if the default node cannot be created
   */
  protected abstract Node createDefaultNodeImpl(String baseName) throws NodeException;


  /**
   * Returns the reference to the node located at s
   */
  protected abstract Node getNodeImpl(String s) throws NodeException;



  //
  // -- PRIVATE METHODS - STATIC -----------------------------------------------
  //

  private static void createClassServer() {
    try {
      new ClassServerHelper().initializeClassServer();
    } catch (Exception e) {
      System.out.println("Error with the ClassServer : "+e.getMessage());
    }
  }
  
  
  private static void registerProtocolFactories() {
    if (JINI_ENABLED) {
      setFactory(Constants.JINI_PROTOCOL_IDENTIFIER, "org.objectweb.proactive.core.node.jini.JiniNodeFactory");
    }
    setFactory(Constants.RMI_PROTOCOL_IDENTIFIER, "org.objectweb.proactive.core.node.rmi.RemoteNodeFactory");
  }
  

  private static boolean isJiniEnabled() {
    try {
      // test if Jini is available
      Class.forName("net.jini.discovery.DiscoveryManagement");
      System.out.println("Jini enabled");
      return true;
    } catch (ClassNotFoundException e) {
      System.out.println("Jini disabled");
      return false;
    }
  }


  private static Node createDefaultNode(String nodeURL) throws NodeException {
    //first look for the factory
    String protocol = getProtocol(nodeURL);
    NodeFactory factory = getFactory(protocol);
    //then create the default node
    Node node = factory.createDefaultNodeImpl(removeProtocol(nodeURL,protocol));
    if (node == null) throw new NodeException("Cannot create a DefaultNode based on "+nodeURL);
    return node;
  }


  private static NodeFactory createNodeFactory(Class factoryClass, String protocol) throws NodeException {
    try {
      NodeFactory nf = (NodeFactory) factoryClass.newInstance();
      instanceFactoryMapping.put(protocol, nf);
      return nf;
    } catch (Exception e) {
      e.printStackTrace();
      throw new NodeException("Error while creating the factory "+factoryClass.getName()+" for the protocol "+protocol);
    }
  }

  private static NodeFactory createNodeFactory(String factoryClassName, String protocol) throws NodeException {
    Class factoryClass = null;
    try {
      factoryClass = Class.forName(factoryClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new NodeException("Error while getting the class of the factory "+factoryClassName+" for the protocol "+protocol);
    }
    return createNodeFactory(factoryClass, protocol);
  }


  private static synchronized NodeFactory getFactory(String protocol) throws NodeException {
    //System.out.println("NodeFactory: Protocol is " + protocol);
    NodeFactory factory = (NodeFactory) instanceFactoryMapping.get(protocol);
    if (factory != null) return factory;
    String factoryClassName = (String) protocolFactoryMapping.get(protocol);
    if (factoryClassName != null) {
      return createNodeFactory(factoryClassName, protocol);
    }
    throw new NodeException("No NodeFactory is registered for the protocol "+protocol);
  }


  /**
   * Return the protocol specified in the string
   * The same convention as in URL is used
   */
  private static String getProtocol(String nodeURL) {
    if (nodeURL == null) return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
    int n = nodeURL.indexOf("://");
    if (n <= 0) return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
    return nodeURL.substring(0, n+1);
  }


  /**
   */
  private static String removeProtocol(String url, String protocol) {
    if (url.startsWith(protocol)) return url.substring(protocol.length());
    return url;
  }
}
