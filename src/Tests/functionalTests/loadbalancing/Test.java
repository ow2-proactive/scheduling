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
package functionalTests.loadbalancing;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.loadbalancing.LoadBalancing;
import org.objectweb.proactive.loadbalancing.metrics.currenttimemillis.CurrentTimeMillisMetricFactory;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test load balancing
 */
public class Test extends FunctionalTest {
    private String XML_LOCATION = Test.class.getResource(
            "/functionalTests/loadbalancing/LoadBalancing.xml").getPath();
    private ProActiveDescriptor pad;
    private VirtualNode vn1;
    A a;
    Node nodeOne;
    Node nodeTwo;

    @org.junit.Test
    public void action() throws Exception {
        a = (A) PAActiveObject.newActive(A.class.getName(), null, nodeOne);
        Thread.sleep(1000);
        assertTrue(a.getNodeUrl().equals(nodeTwo.getNodeInformation().getURL()));
    }

    @Before
    public void initTest() throws Exception {
        this.pad = PADeployment.getProactiveDescriptor(XML_LOCATION);
        this.pad.activateMappings();
        this.vn1 = this.pad.getVirtualNode("VN");
        assertTrue(this.vn1.getMinNumberOfNodes() <= this.vn1.getNumberOfCreatedNodesAfterDeployment());
        this.nodeOne = this.vn1.getNode();
        this.nodeTwo = this.vn1.getNode();

        LoadBalancing.activateOn(new Node[] { nodeOne, nodeTwo },
            new CurrentTimeMillisMetricFactory());
    }
}
