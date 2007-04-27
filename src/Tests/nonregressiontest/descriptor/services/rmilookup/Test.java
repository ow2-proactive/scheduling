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
package nonregressiontest.descriptor.services.rmilookup;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.FunctionalTest;


/**
 * @author ProActiveTeam
 * @version 1.0 6 aout 2004
 * @since ProActive 2.0.1
 */
public class Test extends FunctionalTest {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6700674087317492737L;
	private static String ONEVM_XML_LOCATION_UNIX = Test.class.getResource(
            "/nonregressiontest/descriptor/services/rmilookup/OneVM.xml")
                                                              .getPath();
    private static String LOOK_XML_LOCATION_UNIX = Test.class.getResource(
            "/nonregressiontest/descriptor/services/rmilookup/LookupRMI.xml")
                                                             .getPath();
    Node node;
    ProActiveDescriptor pad;
    ProActiveDescriptor pad1;

    public Test() {
        super("PART acquisition with RMI in deployment descriptor",
            "Test service:  JVM acquisition with RMI in deployment descriptor");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        pad = ProActive.getProactiveDescriptor(ONEVM_XML_LOCATION_UNIX);
        pad.activateMappings();
        Thread.sleep(5000);
        pad1 = ProActive.getProactiveDescriptor(LOOK_XML_LOCATION_UNIX);
        pad1.activateMappings();
        VirtualNode vn = pad1.getVirtualNode("VnTest");
        node = vn.getNode();
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
	public void endTest() throws Exception {
    	pad.killall(false);
    	pad1.killall(false);
    }

    @Override
	public boolean postConditions() throws Exception {
        return node.getProActiveRuntime().getVMInformation().getName().equals("PA_JVM1");
    }

    public static void main(String[] args) {
        Test test = new Test();

        try {
            test.action();
            System.out.println(test.postConditions());
            test.endTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
