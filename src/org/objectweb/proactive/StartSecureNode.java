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

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ext.security.SecureRemoteNodeFactory;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

/**
 * <p>
 * <font color="#FF0000"><b>*unsupported*</b></font>
 * This class is a utility class allowing to start a secure ProActive node with a JVM.
 * It is very useful to start a node on a given host that will receive later 
 * active objects created by other distributed applications.
 * </p><p>
 * This class has a main method and can be used directly from the java command.
 * <br>
 * use<br>
 * &nbsp;&nbsp;&nbsp;java org.objectweb.proactive.StartSecureNode<br>
 * to print the options from command line or see the java doc of the main method.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/03/21
 * @since   ProActive 0.9
 *
 */
public class StartSecureNode extends StartNode {
  
  protected String publicCertificateFilename;
  protected String privateCertificateFilename;
  protected String acPublicKeyFilename;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  private StartSecureNode(String[] args) {
    if (args.length < 4) {
      printUsage();
      throw new IllegalArgumentException("Missing parameter");
    }
    nodeURL = args[0];
    publicCertificateFilename = args[1];
    privateCertificateFilename = args[2];
    acPublicKeyFilename = args[3];
    registryPortNumber = getPort(nodeURL, DEFAULT_PORT);
    checkOptions(args, 4);
    readClassPath(args, 4);
  }
  
  
  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  /**
   * Starts a ProActive node on the localhost host
   * usage: java org.objectweb.proactive.rmi.StartSecureNode &lt;node URL> &lt;publicCertificateFilename> &lt;privateCertificateFilename> &lt;acPublicKeyFilename> [options]<br>
   * where options are amongst<br>
   * <ul>
   * <li>noClassServer : indicates not to create a ClassServer for RMI.
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
    try {
      new StartSecureNode(args).run();    
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }
  

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //
  
  /**
   * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
   * Associates the secure RMI node factory to the RMI protocol
   */
  protected void setNodeFactory() throws java.io.IOException,NodeException {
    //NodeFactory.setFactory(org.objectweb.proactive.core.Constants.RMI_PROTOCOL_IDENTIFIER, new SecureRemoteNodeFactory(publicCertificateFilename, privateCertificateFilename, acPublicKeyFilename));
  }
  
  
  //
  // -- PRIVATE METHODS -----------------------------------------------
  //

  private void printUsage() {
    System.out.println("usage: java "+this.getClass().getName()+" <node URL> <publicCertificateFilename> <privateCertificateFilename> <acPublicKeyFilename> [options]");
    System.out.println(" - options");
    System.out.println("     "+NO_CLASS_SERVER_OPTION_NAME+" : indicates not to create a ClassServer for RMI.");
    System.out.println("                      By default a ClassServer is automatically created");
    System.out.println("                      to serve class files on demand.");
    System.out.println("     "+NO_REBIND_OPTION_NAME+"      : indicates not to use rebind when registering the");
    System.out.println("                      node to the RMIRegistry. If a node of the same name");
    System.out.println("                      already exists, the creation of the new node will fail.");
    System.out.println("  for instance: java "+this.getClass().getName()+" //localhost/node1 publicCertificate privateCertificate acPublicKey");
    System.out.println("                java "+this.getClass().getName()+" //localhost/node2 -noClassServer -noRebind");
  }
  
  
} // end class
