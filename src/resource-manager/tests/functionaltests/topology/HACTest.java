/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.topology;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.clustering.Cluster;
import org.ow2.proactive.resourcemanager.frontend.topology.clustering.HAC;
import org.ow2.proactive.topology.descriptor.BestProximityDescriptor;
import org.ow2.proactive.topology.descriptor.DistanceFunction;

import org.ow2.tests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * Test create different graphs and checks how HAC clustering works.
 *
 */
public class HACTest extends FunctionalTest {

    protected static HashMap<Node, HashMap<Node, Long>> distances = new HashMap<Node, HashMap<Node, Long>>();

    private class LocalTopology implements Topology {

        private static final long serialVersionUID = 32L;

        public Long getDistance(Node node, Node node2) {
            Long distance = null;
            if (distances.get(node) != null && distances.get(node).get(node2) != null) {
                distance = distances.get(node).get(node2);
            }
            if (distances.get(node2) != null && distances.get(node2).get(node) != null) {
                distance = distances.get(node2).get(node);
            }
            return distance;
        }

        public Long getDistance(InetAddress hostAddress, InetAddress hostAddress2) {
            return null;
        }

        public Long getDistance(String hostName, String hostName2) {
            return null;
        }

        public HashMap<InetAddress, Long> getHostTopology(InetAddress hostAddress) {
            return null;
        }

        public Set<InetAddress> getHosts() {
            return null;
        }

        public boolean knownHost(InetAddress hostAddress) {
            return false;
        }

        public boolean onSameHost(Node node, Node node2) {
            return false;
        }

        public List<Cluster<String>> clusterize(int numberOfClusters, DistanceFunction distanceFunction) {
            return null;
        }
    }

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {
        // building graph
        Node n1 = new DummyNode("1");
        Node n2 = new DummyNode("2");
        Node n3 = new DummyNode("3");
        distances.put(n1, new HashMap<Node, Long>());
        distances.put(n2, new HashMap<Node, Long>());
        distances.put(n3, new HashMap<Node, Long>());
        distances.get(n1).put(n2, new Long(2));
        distances.get(n1).put(n3, new Long(4));
        distances.get(n2).put(n3, new Long(-1));

        HAC hac = new HAC(new LocalTopology(), null, BestProximityDescriptor.AVG, Long.MAX_VALUE);
        HAC hacPivot = new HAC(new LocalTopology(), Collections.singletonList(n1),
            BestProximityDescriptor.AVG, Long.MAX_VALUE);
        RMTHelper.log("Test 1: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3]");
        List<Node> result = hac.select(20, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 2", result.size() == 2);
        if (!(result.contains(new DummyNode("1")) && result.contains(new DummyNode("2")))) {
            Assert.assertTrue("Selection is incorrect", false);
        }

        RMTHelper.log("Test 2: [pivot - node 1], graph [1 -(2)- 2 , 1 -(4)- 3]");
        result = hacPivot.select(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 1", result.size() == 1);
        if (!result.contains(new DummyNode("2"))) {
            Assert.assertTrue("Selection is incorrect", false);
        }

        distances.get(n2).put(n3, new Long(10));
        RMTHelper.log("Test 3: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3]");
        result = hac.select(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 3", result.size() == 3);

        RMTHelper.log("Test 4: [pivot - node 1], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3]");
        result = hacPivot.select(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 2", result.size() == 2);
        if (!(result.contains(new DummyNode("2")) && result.contains(new DummyNode("3")))) {
            Assert.assertTrue("Selection is incorrect", false);
        }

        Node n4 = new DummyNode("4");
        distances.put(n4, new HashMap<Node, Long>());
        distances.get(n2).put(n4, new Long(1));
        distances.get(n3).put(n4, new Long(3));
        distances.get(n1).put(n4, new Long(-1));
        RMTHelper.log("Test 5: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4]");
        result = hac.select(4, new LinkedList<Node>(distances.keySet()));
        // HAC cannot cluster 3 nodes together so the expected result is 2
        Assert.assertTrue("Selection size is not 2", result.size() == 2);

        RMTHelper
                .log("Test 6: [pivot - node 1], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4]");
        result = hacPivot.select(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 2", result.size() == 2);
        if (!(result.contains(new DummyNode("2")) && result.contains(new DummyNode("3")))) {
            Assert.assertTrue("Selection is incorrect", false);
        }

        distances.get(n1).put(n4, new Long(3));
        RMTHelper
                .log("Test 7: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(3)- 4]]");
        result = hac.select(4, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 4", result.size() == 4);

        RMTHelper
                .log("Test 8 - optimal: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(3)- 4]]");
        result = hac.select(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 3", result.size() == 3);
        if (!(result.contains(new DummyNode("1")) && result.contains(new DummyNode("2")) && result
                .contains(new DummyNode("4")))) {
            Assert.assertTrue("Selection is incorrect", false);
        }

        RMTHelper
                .log("Test 8: [pivot - node 1], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(3)- 4]]");
        result = hacPivot.select(2, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 2", result.size() == 2);
        if (!(result.contains(new DummyNode("2")) && result.contains(new DummyNode("4")))) {
            Assert.assertTrue("Selection is incorrect", false);
        }

        distances.get(n1).put(n4, new Long(30));
        RMTHelper
                .log("Test 9 - optimal: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(30)- 4]");
        result = hac.select(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 3", result.size() == 3);
        if (!(result.contains(new DummyNode("2")) && result.contains(new DummyNode("3")) && result
                .contains(new DummyNode("4")))) {
            Assert.assertTrue("Selection is incorrect", false);
        }

        Node n5 = new DummyNode("5");
        distances.put(n5, new HashMap<Node, Long>());

        hacPivot = new HAC(new LocalTopology(), Collections.singletonList(n5), BestProximityDescriptor.AVG,
            Long.MAX_VALUE);
        distances.get(n5).put(n1, new Long(-1));
        distances.get(n5).put(n2, new Long(-1));
        distances.get(n5).put(n3, new Long(10));
        distances.get(n5).put(n4, new Long(10));
        RMTHelper
                .log("Test 10: [pivot - node 5], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(30)- 4, 5 -(10)- 3, 5 -(10)- 4]");
        result = hacPivot.select(4, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Selection size is not 2", result.size() == 2);
        if (!(result.contains(new DummyNode("3")) && result.contains(new DummyNode("4")))) {
            Assert.assertTrue("Selection is incorrect", false);
        }
    }
}
