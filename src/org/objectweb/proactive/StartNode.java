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
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.jini.JiniNodeFactory;
import org.objectweb.proactive.core.runtime.rmi.RemoteRuntimeFactory;
import org.objectweb.proactive.core.node.NodeException;
//import org.objectweb.proactive.core.node.jini.JiniNodeFactory;
//import org.objectweb.proactive.core.node.rmi.RemoteNodeFactory;

/**
 * <p>
 * This class is a utility class allowing to start a ProActive node with a JVM.
 * It is very useful to start a node on a given host that will receive later 
 * active objects created by other distributed applications.
 * </p><p>
 * This class has a main method and can be used directly from the java command.
 * <br>
 * use<br>
 * &nbsp;&nbsp;&nbsp;java org.objectweb.proactive.StartNode<br>
 * to print the options from command line or see the java doc of the main method.
 * </p><p>
 * A node represents the minimum services ProActive needs to work with a remote JVM.
 * Any JVM that is going to interact with active objects has at least one associated
 * node. The node must have a remote implementation that allow an object to remotely 
 * invoke its methods.
 * </p> 
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class StartNode {
  
  public static final int DEFAULT_CLASSFILE_SERVER_PORT = 2001;
  
  protected static final int DEFAULT_PORT = 1099;
  protected static final int MAX_RETRY = 3;
  
  protected static final String NO_REBIND_OPTION_NAME = "-noRebind";
  protected static final String NO_CLASS_SERVER_OPTION_NAME = "-noClassServer";
  protected static final String NO_REGISTRY_OPTION_NAME = "-noRegistry";
  protected static final String MULTICAST_LOCATOR_NAME = "-multicastLocator";
  
  private static final String FS = System.getProperty("file.separator");
  private static final String XML_LOCATION = System.getProperty("user.dir")+FS+".."+FS+".."+FS+"descriptors"+FS+"RemoteGlobusSetup.xml";
  
  protected boolean noClassServer = false;
  protected boolean noRebind = false;
  protected boolean noRegistry = false;
  protected boolean multicastLocator = false;

  protected int registryPortNumber = DEFAULT_PORT;

  protected String classpath;
  protected String nodeURL;
  
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //
  
  protected StartNode() {
  }

  private StartNode(String[] args) {
    if (args.length == 0) {
      nodeURL = null;
      registryPortNumber = DEFAULT_PORT;
    } else {
      nodeURL = args[0];
      registryPortNumber = getPort(nodeURL, DEFAULT_PORT);
      checkOptions(args, 1);
      readClassPath(args, 1);
    }
    /*
    // debug
    System.out.println("Node name = "+nodeURL);
    if (noRebind)
      System.out.println(" - NoRebind");
    if (noClassServer)
      System.out.println(" - No ClassServer");
    else System.out.println("ClassServer classpath = "+classpath);
    */
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //


  /**
   * Starts a ProActive node on the localhost host
   * usage: java org.objectweb.proactive.StartNode &lt;node URL> [options]<br>
   * where options are amongst<br>
   * <ul>
   * <li>noClassServer : indicates not to create a ClassServer for JINI.
   *                     By default a ClassServer is automatically created
   *                     to serve class files on demand.</li>
   * <li>noRebind      : indicates not to use rebind when registering the
   *                     node to the RMIRegistry. If a node of the same name
   *                     already exists, the creation of the new node will fail.</li>
   * </ul>
   * for instance: java org.objectweb.proactive.StartNode //localhost/node1<br>
   *               java org.objectweb.proactive.StartNode //localhost/node2 -noClassServer -noRebind<br>
   */
  public static void main(String[] args) {
  	
  	if (args[0].compareTo("-g")==0){
    	try{
    	//ProActiveDescriptor pad = ProActive.getProactiveDescriptor("file://Z:/test/ProActive/classes/GlobusSetupWithRlogin.xml");
    	ProActiveDescriptor pad = ProActive.getProactiveDescriptor("file:"+XML_LOCATION);
			JVMProcess jvmProcess = (JVMProcess)pad.getProcess("linuxJVM");
			jvmProcess.setParameters(args[1]);
			pad.activateMappings();
			//gp.startNodeWithGlobus(args[1]);
    	}catch(ProActiveException e){
    		e.printStackTrace();
    	}
    }else{
    	try {
      	new StartNode(args).run();    
   		}catch (Exception e) {
      	e.printStackTrace();
      	System.out.println(e.toString());
    	}	
  	}
  }
  

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  
  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * Checks options from the arguments
   */
  protected void checkOptions(String[] args, int start) {
    for (int i=start; i<args.length; i++) checkOption(args[i]);
  }
  
  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * Reads the classpath from the arguments
   */
  protected void readClassPath(String[] args, int start) {  
    if (noClassServer) return;
    // look for classpath
    for (int i=start; i<args.length; i++) {
      String s = args[i];
      if (s.charAt(0) != '-') {
        classpath = s;
        break;
      }
    }
  }
  
  
  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * sets the properties needed for the node creation
   */
  protected void setProperties() {
    //  System.setProperty("sun.rmi.dgc.checkInterval","400");
    //  System.setProperty("java.rmi.dgc.leaseValue","800");
    //  System.setProperty("sun.rmi.dgc.cleanInterval","400");
    //  System.setProperty("sun.rmi.dgc.client.gcInterval","400");
  }
  
  
  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * Creates the node at the given URL with the rebind option
   */
  protected void createNode(String nodeURL, boolean noRebind) throws NodeException {
    int exceptionCount = 0;
    while (true) {
      try {
        Node node = null;
        if (nodeURL == null) {
	        node = NodeFactory.getDefaultNode();
        } else {
	         node = NodeFactory.createNode(nodeURL, ! noRebind);
        }
        //System.out.println("nodeurl "+node.getNodeInformation().getURL());
        System.out.println("OK. Node "+node.getNodeInformation().getName()+" is created in VM id=" + UniqueID.getCurrentVMID());
	      break;
      } catch (NodeException e) {
        exceptionCount++;
        if (exceptionCount == MAX_RETRY) {
          throw e;
        } else {
          System.out.println("Error, retrying ("+exceptionCount+")");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e2) {}
        } // end if
      } // try
    } // end while
  }
  
  
  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * Run the complete creation of the node step by step by invoking the other 
   * helper methods
   */
  protected void run() throws java.io.IOException, NodeException {
    setProperties();
    // set options on node factory
    RemoteRuntimeFactory.setShouldCreateClassServer(!noClassServer);
    RemoteRuntimeFactory.setShouldCreateRegistry(!noRegistry);
    RemoteRuntimeFactory.setRegistryPortNumber(registryPortNumber);
    if (RuntimeFactory.JINI_ENABLED) {
      JiniNodeFactory.setMulticastLocator(multicastLocator);
     // System.out.println("jini not yet implemented");
    }
    // create node
    createNode(nodeURL, noRebind);
  }


  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * Checks one given option from the arguments
   */
  protected void checkOption(String option) {
    if (NO_REBIND_OPTION_NAME.equals(option)) {
      noRebind = true;
    } else if (NO_CLASS_SERVER_OPTION_NAME.equals(option)) {
      noClassServer = true;
    } else if (NO_REGISTRY_OPTION_NAME.equals(option)) {
      noRegistry = true;
    } else if (MULTICAST_LOCATOR_NAME.equals(option)) {
      multicastLocator = true;
    }
  }
  
  
  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * Extracts the port number from the node URL
   */
  protected static int getPort(String nodeURL, int defaultValue) {
    int deb = nodeURL.lastIndexOf(":");
    if (deb > -1) {
      //there is a port number specified
       try {
	  //  System.out.println("StartNode: " + nodeURL.substring(deb + 1, nodeURL.lastIndexOf("/")));getporrt
        return Integer.parseInt(nodeURL.substring(deb + 1, nodeURL.lastIndexOf("/")));
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private void printUsage() {
    String localhost = "localhost";
    try {
      localhost = java.net.InetAddress.getLocalHost().getHostName();
    } catch(java.net.UnknownHostException  e) {
      System.err.println("InetAddress failed: " + e.getMessage());
      e.printStackTrace();
    } catch(java.lang.SecurityException e) {
      System.err.println("InetAddress failed: " + e.getMessage());
      e.printStackTrace();
    }
    
    System.out.println("usage: java "+this.getClass().getName()+" <node URL> [options]");
    System.out.println(" - options");
    System.out.println("     "+NO_CLASS_SERVER_OPTION_NAME+" : indicates not to create a ClassServer for JINI and RMI.");
    System.out.println("                      By default a ClassServer is automatically created");
    System.out.println("                      to serve class files on demand.");
    System.out.println("     "+NO_REBIND_OPTION_NAME+"      : indicates not to use rebind when registering the");
    System.out.println("                      node to the RMIRegistry. If a node of the same name");
    System.out.println("                      already exists, the creation of the new node will fail.");
    System.out.println("  for instance: java "+StartNode.class.getName()+" "+Constants.RMI_PROTOCOL_IDENTIFIER+"//"+localhost+"/node1");
    System.out.println("                java "+StartNode.class.getName()+" "+Constants.RMI_PROTOCOL_IDENTIFIER+"://"+localhost+"/node2  "+NO_CLASS_SERVER_OPTION_NAME+" "+NO_REBIND_OPTION_NAME);
    System.out.println("                java "+StartNode.class.getName()+" "+Constants.JINI_PROTOCOL_IDENTIFIER+"://"+localhost+"/node3");
    System.out.println("                java "+StartNode.class.getName()+" "+Constants.JINI_PROTOCOL_IDENTIFIER+"://"+localhost+"/node4 "+MULTICAST_LOCATOR_NAME);
  }
} // end class




