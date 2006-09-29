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
package nonregressiontest.loadbalancing;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.loadbalancing.LoadBalancing;
import org.objectweb.proactive.loadbalancing.metrics.currenttimemillis.CurrentTimeMillisMetricFactory;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

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
    private static String XML_LOCATION = Test.class.getResource(
            "/nonregressiontest/loadbalancing/LoadBalancing.xml").getPath();
    private ProActiveDescriptor pad;
    private VirtualNode vn1;
    A a;
    Node nodeOne;
    Node nodeTwo;

    public Test() {
        super("load balancing", "Test load balancing");
    }

    public void action() throws Exception {
        a = (A) ProActive.newActive(A.class.getName(), null, nodeOne);

        Thread.sleep(1000);
    }

    public void initTest() throws Exception {
        this.pad = ProActive.getProactiveDescriptor(XML_LOCATION);
        this.pad.activateMappings();
        this.vn1 = this.pad.getVirtualNode("VN");
        this.vn1.getNumberOfCreatedNodesAfterDeployment();
        this.nodeOne = this.vn1.getNode(0);
        this.nodeTwo = this.vn1.getNode(1);

        LoadBalancing.activateOn(new Node[] { nodeOne, nodeTwo },
            new CurrentTimeMillisMetricFactory());
    }

    public void endTest() throws Exception {
        this.pad.killall(false);
    }

    public boolean postConditions() throws Exception {
        return a.getNodeUrl().equals(nodeTwo.getNodeInformation().getURL());
    }
}
