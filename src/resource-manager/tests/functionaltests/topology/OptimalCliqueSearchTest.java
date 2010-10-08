/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.topology;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.frontend.topology.BestProximityDescriptor;
import org.ow2.proactive.resourcemanager.selection.topology.clique.CliqueFinder;
import org.ow2.proactive.resourcemanager.selection.topology.clique.OptimalCliqueFinder;
import org.ow2.proactive.resourcemanager.selection.topology.clustering.DistanceFunction;

import functionaltests.RMTHelper;


/**
 * Test creates different sort of graphs and checks that found clique is correct and optimal.
 */
public class OptimalCliqueSearchTest extends CliqueSearchTest {

    private static class LocalOptimalCliqueFinder extends OptimalCliqueFinder {

        public LocalOptimalCliqueFinder(List<Node> pivot, long threshold, DistanceFunction distanceFunction) {
            super(pivot, threshold, distanceFunction);
        }

        protected Long getDistance(Node node, Node node2) {
            return getTestDistances(node, node2);
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

        CliqueFinder cf = new LocalOptimalCliqueFinder(null, 10, BestProximityDescriptor.AVG);
        CliqueFinder cfPivot = new LocalOptimalCliqueFinder(Collections.singletonList(n1), 10,
            BestProximityDescriptor.AVG);
        RMTHelper.log("Test 1: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3]");
        List<Node> clique = cf.getClique(20, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 2", clique.size() == 2);
        if (clique.contains(new DummyNode("2")) && clique.contains(new DummyNode("3"))) {
            Assert.assertTrue("Clique is incorrect", false);
        }

        RMTHelper.log("Test 2: [pivot - node 1], graph [1 -(2)- 2 , 1 -(4)- 3]");
        clique = cfPivot.getClique(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 1", clique.size() == 1);
        if (clique.contains(new DummyNode("1"))) {
            Assert.assertTrue("Clique is incorrect", false);
        }

        distances.get(n2).put(n3, new Long(10));
        RMTHelper.log("Test 3: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3]");
        clique = cf.getClique(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 3", clique.size() == 3);
        RMTHelper.log("Test 4: [pivot - node 1], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3]");
        clique = cfPivot.getClique(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 2", clique.size() == 2);
        if (clique.contains(new DummyNode("1"))) {
            Assert.assertTrue("Clique is incorrect", false);
        }

        Node n4 = new DummyNode("4");
        distances.put(n4, new HashMap<Node, Long>());
        distances.get(n2).put(n4, new Long(1));
        distances.get(n3).put(n4, new Long(3));
        RMTHelper.log("Test 5: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4]");
        clique = cf.getClique(4, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 3", clique.size() == 3);

        RMTHelper
                .log("Test 6: [pivot - node 1], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4]");
        clique = cfPivot.getClique(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 2", clique.size() == 2);
        if (clique.contains(new DummyNode("1"))) {
            Assert.assertTrue("Clique is incorrect", false);
        }

        distances.get(n1).put(n4, new Long(3));
        RMTHelper
                .log("Test 7: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(3)- 4]");
        clique = cf.getClique(4, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 4", clique.size() == 4);
        RMTHelper
                .log("Test 8 - optimal: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(3)- 4]");
        clique = cf.getClique(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 3", clique.size() == 3);
        if (!(clique.contains(new DummyNode("1")) && clique.contains(new DummyNode("2")) && clique
                .contains(new DummyNode("4")))) {
            Assert.assertTrue("Clique is incorrect", false);
        }

        distances.get(n1).put(n4, new Long(30));
        RMTHelper
                .log("Test 9 - optimal: [no pivot], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(30)- 4]");
        clique = cf.getClique(3, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 3", clique.size() == 3);
        if (!(clique.contains(new DummyNode("2")) && clique.contains(new DummyNode("3")) && clique
                .contains(new DummyNode("4")))) {
            Assert.assertTrue("Clique is incorrect", false);
        }

        Node n5 = new DummyNode("5");
        distances.put(n5, new HashMap<Node, Long>());

        cfPivot = new LocalOptimalCliqueFinder(Collections.singletonList(n5), 10, BestProximityDescriptor.AVG);
        distances.get(n5).put(n3, new Long(10));
        distances.get(n5).put(n4, new Long(10));
        RMTHelper
                .log("Test 10: [pivot - node 5], graph [1 -(2)- 2 , 1 -(4)- 3, 2 -(10)- 3, 2 -(1)- 4, 3 -(3)- 4, 1 -(30)- 4, 5 -(10)- 3, 5 -(10)- 4]");
        clique = cfPivot.getClique(4, new LinkedList<Node>(distances.keySet()));
        Assert.assertTrue("Cliques size is not 2", clique.size() == 2);
        if (!(clique.contains(new DummyNode("3")) && clique.contains(new DummyNode("4")))) {
            Assert.assertTrue("Clique is incorrect", false);
        }
    }
}
