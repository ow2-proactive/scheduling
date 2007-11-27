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
package functionalTests.gcmdeployment.virtualnode;

import java.io.FileNotFoundException;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.API;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;

import functionalTests.gcmdeployment.Abstract;


public class TestVirtualNodeAPI extends Abstract {
    static GCMApplicationDescriptor gcma;

    @BeforeClass
    static public void setup() throws FileNotFoundException, ProActiveException {
        gcma = API.getGCMApplicationDescriptor(getDescriptor(
                    TestVirtualNodeAPI.class));
        gcma.startDeployment();
        waitAllocation();
    }

    @Test
    public void testGetName() {
        VirtualNode vn1 = gcma.getVirtualNode("vn1");
        VirtualNode vn2 = gcma.getVirtualNode("vn2");
        VirtualNode vn3 = gcma.getVirtualNode("vn3");

        Assert.assertEquals("vn1", vn1.getName());
        Assert.assertEquals("vn2", vn2.getName());
        Assert.assertEquals("vn3", vn3.getName());
    }

    @Test
    public void testIsGreedy() {
        VirtualNode vn1 = gcma.getVirtualNode("vn1");
        VirtualNode vn2 = gcma.getVirtualNode("vn2");
        VirtualNode vn3 = gcma.getVirtualNode("vn3");

        Assert.assertNull(gcma.getVirtualNode("IDontExist"));
        Assert.assertTrue(vn1.isGreedy());
        Assert.assertTrue(vn3.isGreedy());
        Assert.assertFalse(vn2.isGreedy());
    }

    @Test
    public void testIsReady() {
        VirtualNode vn1 = gcma.getVirtualNode("vn1");
        VirtualNode vn2 = gcma.getVirtualNode("vn2");
        VirtualNode vn3 = gcma.getVirtualNode("vn3");

        Assert.assertTrue(vn1.isReady());
        Assert.assertTrue(vn2.isReady());
        Assert.assertFalse(vn3.isReady());
    }

    @Test
    public void testGetNbRequiredNodes() {
        VirtualNode vn1 = gcma.getVirtualNode("vn1");
        VirtualNode vn2 = gcma.getVirtualNode("vn2");
        VirtualNode vn3 = gcma.getVirtualNode("vn3");
        VirtualNode vn4 = gcma.getVirtualNode("vn4");
        VirtualNode vn5 = gcma.getVirtualNode("vn5");

        Assert.assertEquals(0, vn1.getNbRequiredNodes());
        Assert.assertEquals(1, vn2.getNbRequiredNodes());
        Assert.assertEquals(2, vn3.getNbRequiredNodes());
        Assert.assertEquals(2, vn4.getNbRequiredNodes());
        Assert.assertEquals(3, vn5.getNbRequiredNodes());
    }

    @Test
    public void testGetNbCurrentNodes() {
        VirtualNode vn2 = gcma.getVirtualNode("vn2");
        VirtualNode vn3 = gcma.getVirtualNode("vn3");
        VirtualNode vn4 = gcma.getVirtualNode("vn4");
        VirtualNode vn5 = gcma.getVirtualNode("vn5");

        // VN1 is blocked by VN3
        Assert.assertEquals(1, vn2.getCurrentNodes().size());
        Assert.assertEquals(1, vn3.getCurrentNodes().size());
        Assert.assertEquals(1, vn4.getCurrentNodes().size());
        Assert.assertEquals(2, vn5.getCurrentNodes().size());
    }

    @Test
    public void testGetCurrentNodes() {
        VirtualNode vn5 = gcma.getVirtualNode("vn5");

        // Check isolation
        Set<Node> vn5Nodes = vn5.getCurrentNodes();
        vn5Nodes.remove(vn5Nodes.iterator().next());
        Assert.assertTrue(vn5.getCurrentNodes().size() == ((vn5Nodes.size()) +
            1));
    }

    @Test
    public void testGetNewNodes() {
        VirtualNode vn1 = gcma.getVirtualNode("vn1");

        // Check isolation
        Set<Node> vn1Nodes = vn1.getCurrentNodes();
        Set<Node> set1 = vn1.getNewNodes();
        Assert.assertTrue(set1.containsAll(vn1Nodes) &&
            (set1.size() == vn1Nodes.size()));
        Assert.assertTrue(vn1.getNewNodes().size() == 0);

        // TODO register manually some Node and check the returned set again
    }
}
