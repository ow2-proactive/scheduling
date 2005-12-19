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
package nonregressiontest.descriptor.defaultnodes;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.FunctionalTest;


/**
 * @author Alexandre di Costanzo
 *
 */
public class TestNodes extends FunctionalTest {
    private static String XML_LOCATION;

    static {
        String value = System.getProperty(
                "nonregressiontest.descriptor.defaultnodes.file");
        if (value != null) {
            XML_LOCATION = TestNodes.class.getResource(value).getPath();
        } else {  
      	  if ("ibis".equals(System.getProperty("proactive.communication.protocol"))) {
      		XML_LOCATION = TestNodes.class.getResource(
            "/nonregressiontest/descriptor/defaultnodes/NodesIbis.xml")
                                  .getPath();
    	  } else {
    		  XML_LOCATION = TestNodes.class.getResource(
              "/nonregressiontest/descriptor/defaultnodes/Nodes.xml")
                                    .getPath();
    	  }
      }
    }
           

    private static String FS = System.getProperty("file.separator");
    private static ProActiveDescriptor proActiveDescriptor = null;
    private static VirtualNode[] virtualNodes = null;
    private static Node sameVMNode = null;
    private static Node localVMNode = null;
    private static Node remoteVMNode = null;
    private static Node remoteACVMNode = null;
    private static String remoteHostname = "localhost";

    /**
     *
     */
    public TestNodes() {
        super("Default deployment descriptor",
            " Test reading a descriptor file to create nodes needed by other tests.");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        proActiveDescriptor = ProActive.getProactiveDescriptor("file:" +
                XML_LOCATION);
        proActiveDescriptor.activateMappings();
        TestNodes.virtualNodes = proActiveDescriptor.getVirtualNodes();
        for (int i = 0; i < virtualNodes.length; i++) {
            VirtualNode virtualNode = virtualNodes[i];
            if (virtualNode.getName().compareTo("Dispatcher") == 0) {
                sameVMNode = virtualNode.getNode();
            } else if (virtualNode.getName().compareTo("Dispatcher1") == 0) {
                localVMNode = virtualNode.getNode();
            } else if (virtualNode.getName().compareTo("Dispatcher3-AC") == 0) {
                remoteACVMNode = virtualNode.getNode();
            } else {
                remoteVMNode = virtualNode.getNode();
            }
        }
        remoteHostname = remoteVMNode.getNodeInformation().getInetAddress()
                                     .getHostName();
    }

    /**
     * TODO : remove this method, as it is used as a walkaround to use deployed nodes
     * outside of the first packageGroup group of the MainManager.xml
     * (the endTest() method, that kills all the deployed nodes, is indeed called at the end of each
     * group of tests)
     *
     */

    // COMPONENTS
    public Object[] action(Object[] parameters) throws Exception {
        action();
        return null;
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        // nothing to do
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        //killNodes();
    }

    public void uponEndOfGroupOfTests() throws Exception {
        killNodes();
    }

    public static void killNodes() throws ProActiveException {
        if (proActiveDescriptor != null) {
            proActiveDescriptor.killall(false);
            proActiveDescriptor = null;
        }
    }

    /**
     * @return
     */
    public static Node getLocalVMNode() {
        return localVMNode;
    }

    /**
     * @return
     */
    public static Node getRemoteVMNode() {
        return remoteVMNode;
    }

    /**
     * @return
     */
    public static Node getSameVMNode() {
        return sameVMNode;
    }

    /**
     * @return
     */
    public static String getRemoteHostname() {
        return remoteHostname;
    }

    /**
     * @return the node with automatic continuations enabled
     */
    public static Node getRemoteACVMNode() {
        return remoteACVMNode;
    }

    public static VirtualNode getVirtualNode(String name) {
        for (int i = 0; i < virtualNodes.length; i++) {
            if (virtualNodes[i].getName().equals(name)) {
                return virtualNodes[i];
            }
        }
        return null;
    }
}
