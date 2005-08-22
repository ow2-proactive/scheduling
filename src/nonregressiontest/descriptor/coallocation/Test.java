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
package nonregressiontest.descriptor.coallocation;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.FunctionalTest;


/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test extends FunctionalTest {
    ProActiveDescriptor proActiveDescriptor;
    private static String FS = System.getProperty("file.separator");
    private static String AGENT_XML_LOCATION_UNIX = Test.class.getResource(
            "/nonregressiontest/descriptor/coallocation/coallocation.xml")
                                                              .getPath();
    Node node1;
    Node node2;

    public Test() {
        super("Coallocation in deployment descriptor",
            "Test coallocation in deployment descriptors");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        proActiveDescriptor = ProActive.getProactiveDescriptor("file:" +
                AGENT_XML_LOCATION_UNIX);
        proActiveDescriptor.activateMappings();
        VirtualNode vn1 = proActiveDescriptor.getVirtualNode("vn1");
        VirtualNode vn2 = proActiveDescriptor.getVirtualNode("vn2");
        node1 = vn1.getNode();
        node2 = vn2.getNode();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        proActiveDescriptor.killall(false);
    }

    public boolean postConditions() throws Exception {
        return node1.getProActiveRuntime().getURL().equals(node2.getProActiveRuntime()
                                                                .getURL());
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            System.out.println(test.postConditions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
