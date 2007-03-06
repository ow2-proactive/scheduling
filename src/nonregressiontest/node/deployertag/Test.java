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
package nonregressiontest.node.deployertag;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.DeployerTag;

import testsuite.test.FunctionalTest;


// TODO: Test deployment from multiple VMs
public class Test extends FunctionalTest {
    private static String XML_LOCATION;

    static {
        String value = System.getProperty(
                "nonregressiontest.node.deployertag.file");
        if (value != null) {
            XML_LOCATION = Test.class.getResource(value).getPath();
        } else {
            XML_LOCATION = Test.class.getResource(
                    "/nonregressiontest/node/deployertag/groupinformation.xml")
                                     .getPath();
        }
    }

    private static ProActiveDescriptor proActiveDescriptor = null;
    private static VirtualNode[] virtualNodes = null;

    public Test() {
        super("DeployerTag", "Test that DeployerTag are correctly assigned.");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        proActiveDescriptor = ProActive.getProactiveDescriptor("file:" +
                XML_LOCATION);
        proActiveDescriptor.activateMappings();

        virtualNodes = proActiveDescriptor.getVirtualNodes();
        assert (virtualNodes.length == 1);

        VirtualNode virtualNode = virtualNodes[0];
        Node[] nodes = virtualNode.getNodes();

        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                if (i == j) {
                    continue;
                }

                DeployerTag gi_i = nodes[i].getNodeInformation().getDeployerTag();
                DeployerTag gi_j = nodes[j].getNodeInformation().getDeployerTag();
                if (gi_i.equals(gi_j)) {
                    throw new Exception("Two GroupInformations are equals !");
                }

                if (!gi_i.getVMID().equals(gi_j.getVMID())) {
                    throw new Exception(
                        "Two nodes deployed from the same VM have non equals VMID");
                }
            }
        }
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
        if (proActiveDescriptor != null) {
            proActiveDescriptor.killall(false);
            proActiveDescriptor = null;
        }
    }
}
