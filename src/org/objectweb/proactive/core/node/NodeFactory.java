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
package org.objectweb.proactive.core.node;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.ProActiveProperties;
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

  protected static ClassServerHelper classServerHelper = new ClassServerHelper();
  
  private static final String DEFAULT_NODE_NAME = "//localhost/Node";

  // test with class loader
  //private static final ClassLoader myClassLoader = new NodeClassLoader();

  /** the table where associations Protocol - Factory are kept */
  private static java.util.Hashtable protocolFactoryMapping = new java.util.Hashtable();
  private static java.util.Hashtable instanceFactoryMapping = new java.util.Hashtable();
  private static Node defaultNode = null;

  private static NodeFactory defaultNodeFactory;


  static {
    try {
      classServerHelper.initializeClassServer();
    } catch (Exception e) {
      System.out.println("Error with the ClassServer : "+e.getMessage());
      e.printStackTrace();
    }
  }


  //
  // -- PUBLIC METHODS - STATIC -----------------------------------------------
  //

  /**
   * Creates the factory for the default protocol. This call is useful in order to
   * pre-initialized the factory asssociated to the default protocol.
   */
  public static void createDefaultFactory() {
    try {
      defaultNodeFactory = createNodeFactory(ProActiveProperties.getNodeFactory());
    } catch (NodeException e) {
    }
  }



  /**
   * Associates the factory of class <code>factoryClassName</code> as the factory to create
   * nodes for the given protocol. Replaces any previous association.
   * @param <code>protocol</code> the protocol to associate the factory to
   * @param <code>factoryClassName</code> the fully qualified name of the class of the factory
   * responsible of creating the nodes for that protocol
   * @exception NodeException if a problem occurs while loading or instantiating the class
   */
  static public void setFactory(String protocol, String factoryClassName) throws NodeException {
    try {
      protocolFactoryMapping.put(protocol, Class.forName(factoryClassName));
    } catch (Exception e) {
      throw new NodeException("Error while getting the NodeFactory e="+e);
    }
  }


  /**
   * Associates the factory of class <code>factoryClassName</code> as the factory to create
   * nodes for the given protocol. Replaces any previous association.
   * @param <code>protocol</code> the protocol to associate the factory to
   * @param <code>factoryClass</code> the class of the factory
   * responsible of creating the nodes for that protocol
   * @exception NodeException if a problem occurs while loading or instantiating the class
   */
  static public void setFactory(String protocol, NodeFactory factoryClass) throws NodeException {
    try {
      String factoryName = factoryClass.getClass().getName();

      protocolFactoryMapping.put(protocol, factoryName);

      instanceFactoryMapping.put(factoryName, factoryClass);
    } catch (Exception e) {
      throw new NodeException("Error while getting the  NodeFactory e="+e);
    }
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
  static synchronized public Node createNode(String nodeURL) throws NodeException {
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
  static synchronized public Node createNode(String nodeURL, boolean replacePreviousBinding) throws NodeException {
    //System.out.println("NodeFactory: createNode(" + nodeURL+ ")");
    //first look for the factory
    String protocol = getProtocol(nodeURL);
    NodeFactory factory = getFactory(protocol);
    //then create a node
    Node node = factory._createNode(removeProtocol(nodeURL,protocol), replacePreviousBinding);
    if (node == null) throw new NodeException("Cannot create a Node based on "+nodeURL);
    return node;
  }


  static public Node getNode(String nodeURL) throws NodeException {
    // System.out.println("NodeFactory: getNode() for " + nodeURL);
    //do we have any association for this node?
    String protocol = getProtocol(nodeURL);
    NodeFactory factory = getFactory(protocol);
    //		System.out.println("NodeFactory: getNode " + s + " got factory " + tmp);
    return factory._getNode(removeProtocol(nodeURL, protocol));
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
   * @return the newly created node on the local JVM
   * @exception NodeException if the node cannot be created
   */
  protected abstract Node _createNode(String nodeURL, boolean replacePreviousBinding) throws NodeException;


  /**
   * Creates the default node of the local JVM.
   * @return the newly created default node on the local JVM
   * @exception NodeException if the default node cannot be created
   */
  protected abstract Node _createDefaultNode(String baseName) throws NodeException;


  /**
   * Returns the reference to the node located at s
   */
  protected abstract Node _getNode(String s) throws NodeException;



  //
  // -- PRIVATE METHODS - STATIC -----------------------------------------------
  //

  static synchronized private Node createDefaultNode(String nodeURL) throws NodeException {
    //first look for the factory
    String protocol = getProtocol(nodeURL);
    NodeFactory factory = getFactory(protocol);
    //then create the default node
    Node node = factory._createDefaultNode(removeProtocol(nodeURL,protocol));
    if (node == null) throw new NodeException("Cannot create a DefaultNode based on "+nodeURL);
    return node;
  }


  static private NodeFactory createNodeFactory(Class factoryClass) throws NodeException {
    try {
      String factoryName = factoryClass.getName();
      NodeFactory nf = null;
      if ((nf = (NodeFactory)instanceFactoryMapping.get(factoryName)) == null) {
        nf =  (NodeFactory)factoryClass.newInstance();
        instanceFactoryMapping.put(factoryName, nf);
      }
      return nf;
    } catch (Exception e) {
      throw new NodeException("Error while getting the default NodeFactory e="+e);
    }
  }

  static private NodeFactory createNodeFactory(String factoryName) throws NodeException {
    try {
      NodeFactory nf = null;
      if ((nf = (NodeFactory)instanceFactoryMapping.get(factoryName)) == null) {
        Class factoryClass = Class.forName(factoryName);
         nf =  (NodeFactory)factoryClass.newInstance();
        instanceFactoryMapping.put(factoryName, nf);
      }
      return nf;
    } catch (Exception e) {
      throw new NodeException("Error while getting the default NodeFactory e="+e);
    }
  }


  static private NodeFactory getFactory(String protocol) throws NodeException {
    //System.out.println("NodeFactory: Protocol is " + protocol);
    Class factoryClass =  (Class)protocolFactoryMapping.get(protocol);


    if (factoryClass != null) {
      return createNodeFactory(factoryClass);
    }
    //no factory matches the protocol : used default one
    if (defaultNodeFactory == null) defaultNodeFactory = createNodeFactory(ProActiveProperties.getNodeFactory());
    return defaultNodeFactory;
  }


  /**
   * Return the protocol specified in the string
   * The same convention as in URL is used
   */
  static private String getProtocol(String nodeURL) {
    if (nodeURL == null) return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
    int n = nodeURL.indexOf("://");
    if (n <= 0) return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
    return nodeURL.substring(0, n+1);
  }


  /**
   */
  static private String removeProtocol(String url, String protocol) {
    if (url.startsWith(protocol)) return url.substring(protocol.length());
    return url;
  }
}
