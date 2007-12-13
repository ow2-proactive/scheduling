/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.descriptor.defaultnodes;

import org.junit.Test;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;


/**
 * Test reading a descriptor file to create nodes needed by other tests.
 * @author Alexandre di Costanzo
 *
 */
public class TestNodes extends FunctionalTest {
    private static String XML_LOCATION;

    static {
        String value = System.getProperty("functionalTests.descriptor.defaultnodes.file");
        if (value != null) {
            XML_LOCATION = TestNodes.class.getResource(value).getPath();
        } else {
            if ("ibis".equals(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue())) {
                XML_LOCATION = TestNodes.class.getResource(
                        "/functionalTests/descriptor/defaultnodes/NodesIbis.xml").getPath();
            } else {
                XML_LOCATION = TestNodes.class.getResource(
                        "/functionalTests/descriptor/defaultnodes/Nodes.xml").getPath();
            }
        }
    }

    private static ProActiveDescriptor proActiveDescriptor = null;
    private static VirtualNode[] virtualNodes = null;
    private static Node sameVMNode = null;
    private static Node localVMNode = null;
    private static Node remoteVMNode = null;
    private static Node remoteACVMNode = null;
    private static String remoteHostname = "localhost";

    @Test
    public void action() throws Exception {
        proActiveDescriptor = PADeployment.getProactiveDescriptor("file:" + XML_LOCATION);
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
        remoteHostname = remoteVMNode.getVMInformation().getHostName();
    }

    /**
     * TODO : remove this method, as it is used as a walkaround to use deployed nodes
     * outside of the first packageGroup group of the testsuite.xml
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
