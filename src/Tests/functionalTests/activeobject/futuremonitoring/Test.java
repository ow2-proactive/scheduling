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
package functionalTests.activeobject.futuremonitoring;

import org.junit.Before;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.future.FutureMonitoring;
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
    Node node;

    @org.junit.Test
    public void action() throws Exception {
        boolean exception = false;
        A a = (A) ProActive.newActive(A.class.getName(), null, this.node);
        A future = a.crash();

        //FutureMonitoring.monitorFuture(future);
        try {
            System.out.println(future);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Before
    public void initTest() throws Exception {
        ProActiveDescriptor pad = ProActive.getProactiveDescriptor(XML_LOCATION);
        pad.activateMappings();
        VirtualNode vn = pad.getVirtualNode("VN");
        assertTrue(vn.getMinNumberOfNodes() <= vn.getNumberOfCreatedNodesAfterDeployment());
        this.node = vn.getNode();
    }
}
