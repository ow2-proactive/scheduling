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
package functionalTests.activeobject.futuremonitoring;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test monitoring the futures
 */
public class Test extends FunctionalTest {
    private String XML_LOCATION = Test.class.getResource(
            "/functionalTests/loadbalancing/LoadBalancing.xml").getPath();
    Node node1;
    Node node2;

    @org.junit.Test
    public void action() throws Exception {
        // With AC
        boolean exception = false;
        A a1 = (A) PAActiveObject.newActive(A.class.getName(), null, this.node1);
        A future = a1.sleepForever();
        A a2 = (A) PAActiveObject.newActive(A.class.getName(), null, this.node2);
        A ac = a2.wrapFuture(future);
        a2.crash();
        try {
            //System.out.println(ac);
            ac.toString();
        } catch (FutureMonitoringPingFailureException fmpfe) {
            exception = true;
        }
        assertTrue(exception);

        // Without AC
        exception = false;
        A a1bis = (A) PAActiveObject.newActive(A.class.getName(), null,
                this.node1);
        a1bis.crash();
        try {
            //System.out.println(future);
            future.toString();
        } catch (FutureMonitoringPingFailureException fmpfe) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Before
    public void initTest() throws Exception {
        /* This must be done before initializing ProActive, and the monitoring */
        System.setProperty("proactive.futuremonitoring.ttm", "500");

        ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(XML_LOCATION);
        pad.activateMappings();
        VirtualNode vn = pad.getVirtualNode("VN");
        assertTrue(vn.getMinNumberOfNodes() <= vn.getNumberOfCreatedNodesAfterDeployment());
        this.node1 = vn.getNode();
        this.node2 = vn.getNode();
    }
}
