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
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplication;
import org.objectweb.proactive.extra.gcmdeployment.core.Topology;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;

import functionalTests.gcmdeployment.Abstract;


public class TestGCMApplicationDescriptorAPI extends Abstract {
    GCMApplication gcma;

    @Test
    public void test() throws ProActiveException, FileNotFoundException {
        gcma = PAGCMDeployment.loadApplicationDescriptor(getDescriptor(this));

        Assert.assertFalse(gcma.isStarted());
        Assert.assertEquals(0, gcma.getAllCurrentNodes().size());
        Assert.assertEquals(0, gcma.getNbUnmappedNodes());
        Assert.assertEquals(2, gcma.getVirtualNodes().size());

        gcma.startDeployment();
        waitAllocation();

        Assert.assertTrue(gcma.isStarted());
        Assert.assertEquals(10, gcma.getAllCurrentNodes().size());
        Assert.assertEquals(1, gcma.getNbUnmappedNodes());
        Assert.assertEquals(2, gcma.getVirtualNodes().size());

        GCMVirtualNode vn1 = gcma.getVirtualNode("vn1");
        Assert.assertNotNull(vn1);
        Set<Node> nodes = vn1.getCurrentNodes();

        Topology topology = gcma.getAllCurrentNodesTopology();
        Assert.assertNotNull(topology);

        // Check reachable
        for (Node node : nodes) {
            node.getActiveObjects();
        }

        gcma.kill();

        // Check unreachable
        for (Node node : nodes) {
            boolean exception = false;
            try {
                node.getActiveObjects();
            } catch (Throwable e) {
                exception = true;
            }
            Assert.assertTrue(exception);
        }
    }
}
