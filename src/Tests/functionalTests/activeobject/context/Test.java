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
package functionalTests.activeobject.context;

import org.junit.Before;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;
public class Test extends FunctionalTest {
    private String XML_LOCATION = Test.class.getResource(
            "/functionalTests/loadbalancing/LoadBalancing.xml").getPath();
    Node node1;
    Node node2;

    @Before
    public void initTest() throws Exception {
        ProActiveDescriptor pad = ProActive.getProactiveDescriptor(XML_LOCATION);
        pad.activateMappings();
        VirtualNode vn = pad.getVirtualNode("VN");
        assertTrue(vn.getMinNumberOfNodes() <= vn.getNumberOfCreatedNodesAfterDeployment());
        this.node1 = vn.getNode();
        this.node2 = vn.getNode();
    }

    @org.junit.Test
    public void action() throws Exception {
        // test halfBody creation
        UniqueID myId = null;
        Context c = ProActive.getContext();
        Body myHalfBody = c.getBody();

        // a half body should have been created
        assertTrue(myHalfBody != null);
        myId = myHalfBody.getID();

        boolean exceptionOccured = false;
        try {
            myHalfBody.getRequestQueue();
        } catch (ProActiveRuntimeException e) {
            // Half bodies does not have request queue...
            exceptionOccured = true;
        }
        // myHalfBody should be a half body
        assertTrue(exceptionOccured);

        // test getContext
        A a1 = (A) ProActive.newActive(A.class.getName(), null, this.node1);
        A a2 = (A) ProActive.newActive(A.class.getName(), null, this.node2);

        a1.init();
        a2.init();

        // test between two active objects
        BooleanWrapper res1 = a1.test(a2);
        boolean b = res1.booleanValue();
        assertTrue(b);

        // test from a halfBody
        BooleanWrapper res21 = a1.standardService(myId);
        BooleanWrapper res22 = a1.immediateService(myId);
        assertTrue(res21.booleanValue());
        assertTrue(res22.booleanValue());
    }
}
