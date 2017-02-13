/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.topology;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


/**
 *
 * Distributed test checking the topology selection mechanism with
 * 3 network hosts - current, one neighbor & one distant. All possible topology descriptors
 * are checked here.
 *
 * Two parameters must be specified for ant
 * -DdistantHost="distant host DNS" -DneighborHost="neighbor host DNS"
 * otherwise test will not be executed (silently pass).
 *
 * Test creates 6 nodes on the current machine, 2 node on distant and one on neighbor machine.
 * Nodes are created using ssh infrastructure manager, so all hosts have to be reachable by ssh.
 * RM distrib path as well as jdk path must be the same on all machines.
 *
 */
@Ignore("requires several machines")
public class SelectionTest extends RMFunctionalTest {

    private String vmPropSelectionScriptpath = this.getClass()
                                                   .getResource("/functionaltests/selectionscript/vmPropertySelectionScript.groovy")
                                                   .getPath();

    private String vmPropKey1 = "myProperty1";

    private String vmPropValue1 = "myValue1";

    @Test
    public void action() throws Exception {

        String currentHost = System.getenv("HOSTNAME");

        // checking if properties distantHost and neighborHost are defined
        String distantHost = System.getProperty("distantHost");
        String neighborHost = System.getProperty("neighborHost");

        if (distantHost != null && !distantHost.equals("${distantHost}") && neighborHost != null &&
            !neighborHost.equals("${neighborHost}")) {

            String rmHome = System.getProperty("pa.rm.home");
            String rmCredPath = rmHome + "/config/authentication/rm.cred";
            String javaExec = System.getenv("JAVA_HOME") + "/bin/java";

            // properties are defined, trying to deploy nodes to these hosts
            BooleanWrapper result = rmHelper.getResourceManager()
                                            .createNodeSource("remote",
                                                              SSHInfrastructure.class.getName(),
                                                              new Object[] { "", // ssh options
                                                                             javaExec, // java executable path
                                                                             rmHome, // rmHelper distrib path
                                                                             "30000", // node lookup timeout
                                                                             "2", //attempts
                                                                             "Linux", // os
                                                                             "", // java options
                                                                             FileToBytesConverter.convertFileToByteArray(new File(rmCredPath)), // rmHelper credential
                                                                             (distantHost + " 2\n" +
                                                                              neighborHost).getBytes() },
                                                              StaticPolicy.class.getName(),
                                                              null);

            if (result.getBooleanValue()) {
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
                //wait for the nodes to be in free state
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            }

            // we are good - all remote nodes registered
        } else {
            // no parameters provided
            RMTHelper.log("Nothing to do. Set up distantHost & neighborHost parameters.");
            return;
        }

        // creating the selection script object
        SelectionScript script = new SelectionScript(new File(vmPropSelectionScriptpath),
                                                     new String[] { this.vmPropKey1, this.vmPropValue1 },
                                                     true);
        List<SelectionScript> scriptList = new LinkedList<>();
        scriptList.add(script);

        ResourceManager resourceManager = rmHelper.getResourceManager();
        String node1 = "node1";

        //a node with the VM properties
        HashMap<String, String> vmProperties = new HashMap<>();
        vmProperties.put(this.vmPropKey1, this.vmPropValue1);

        testNode = rmHelper.createNode(node1, vmProperties);
        String node1URL = testNode.getNode().getNodeInformation().getURL();
        resourceManager.addNode(node1URL, NodeSource.DEFAULT);

        //wait node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //wait for the nodes to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // so now we have 9 node in total
        // 6 local nodes (5 default, 1 marked with property)
        // 2 nodes on distant host
        // 1 node on neighbor host
        Assert.assertEquals(9, resourceManager.getState().getFreeNodesNumber());

        // checking TopologyDescriptor.ARBITRARY
        NodeSet ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.ARBITRARY, null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(8, TopologyDescriptor.ARBITRARY, null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.ARBITRARY, null, null);
        Assert.assertEquals(9, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.ARBITRARY, scriptList, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        // checking TopologyDescriptor.BEST_PROXIMITY
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(9, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(9, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(9, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        //        Node pivotNode = null;
        ns = resourceManager.getAtMostNodes(6, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(6, ns.size());
        for (Node node : ns) {
            //            if (pivotNode == null)
            //                pivotNode = node;
            if (!node.getNodeInformation().getURL().contains(currentHost)) {
                Assert.assertTrue("All nodes have to be from " + currentHost, false);
            }
        }
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(7, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(7, ns.size());
        for (Node node : ns) {
            if (node.getNodeInformation().getURL().contains(distantHost)) {
                Assert.assertTrue("Node from distant host selected", false);
            }
        }
        resourceManager.releaseNodes(ns).getBooleanValue();
        // pivot scenario
        //        List<Node> pivot = new LinkedList<Node>();
        //        pivot.add(pivotNode);
        //        ns = resourceManager.getAtMostNodes(6,
        //                new BestProximityDescriptor(BestProximityDescriptor.MAX, pivot), null, null);
        //        Assert.assertEquals(6, ns.size());
        //        for (Node node : ns) {
        //            if (node.getNodeInformation().getURL().equals(pivotNode.getNodeInformation().getURL())) {
        //                Assert.assertTrue("Pivot must not be in results", false);
        //            }
        //            if (node.getNodeInformation().getURL().contains(distantHost)) {
        //                Assert.assertTrue("Node from distant host selected", false);
        //            }
        //        }
        //        resourceManager.releaseNodes(ns).getBooleanValue();
        //
        //        ns = resourceManager.getAtMostNodes(7,
        //                new BestProximityDescriptor(BestProximityDescriptor.MAX, pivot), null, null);
        //        Assert.assertEquals(7, ns.size());
        //        for (Node node : ns) {
        //            if (node.getNodeInformation().getURL().equals(pivotNode.getNodeInformation().getURL())) {
        //                Assert.assertTrue("Pivot must not be in results", false);
        //            }
        //        }
        //        resourceManager.releaseNodes(ns).getBooleanValue();

        // checking TopologyDescriptor.ThresholdProximityDescriptor
        ns = resourceManager.getAtMostNodes(1, new ThresholdProximityDescriptor(Long.MAX_VALUE), null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(9, new ThresholdProximityDescriptor(Long.MAX_VALUE), null, null);
        Assert.assertEquals(9, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, new ThresholdProximityDescriptor(Long.MAX_VALUE), null, null);
        Assert.assertEquals(9, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        // getting information about topology
        Topology topology = resourceManager.getTopology();
        Assert.assertEquals(3, topology.getHosts().size());

        // looking for distances consistency
        Long current2neighborDistance = topology.getDistance(currentHost, neighborHost);
        Long current2distantDistance = topology.getDistance(currentHost, distantHost);
        Long distant2neightborDistance = topology.getDistance(neighborHost, distantHost);

        if (current2neighborDistance == null || current2distantDistance == null) {
            Assert.assertTrue("Please put full host names to the parameters", false);
        }
        if (current2neighborDistance > current2distantDistance) {
            Assert.assertTrue("Distant host is close to current than neighbor according to the topology", false);
        }

        System.out.println("Distance between " + currentHost + " and " + neighborHost + " is " +
                           current2neighborDistance);
        System.out.println("Distance between " + currentHost + " and " + distantHost + " is " +
                           current2distantDistance);
        System.out.println("Distance between " + neighborHost + " and " + distantHost + " is " +
                           distant2neightborDistance);

        long maxThreshold = Math.max(current2neighborDistance,
                                     Math.max(current2distantDistance, distant2neightborDistance));

        ns = resourceManager.getAtMostNodes(100,
                                            new ThresholdProximityDescriptor(current2neighborDistance - 1),
                                            null,
                                            null);
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100,
                                            new ThresholdProximityDescriptor(current2neighborDistance),
                                            null,
                                            null);
        Assert.assertEquals(7, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, new ThresholdProximityDescriptor(maxThreshold - 1), null, null);
        for (Node node : ns) {
            if (node.getNodeInformation().getURL().contains(distantHost)) {
                Assert.assertTrue("Node from distant host selected", false);
            }
        }
        Assert.assertEquals(7, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(10, new ThresholdProximityDescriptor(maxThreshold), null, null);
        Assert.assertEquals(9, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();
        // pivot scenario
        //        ns = resourceManager.getAtMostNodes(1, new ThresholdProximityDescriptor(0, pivot), null, null);
        //        Assert.assertEquals(1, ns.size());
        //        for (Node node : ns) {
        //            if (node.getNodeInformation().getURL().equals(pivotNode.getNodeInformation().getURL())) {
        //                Assert.assertTrue("Pivot must not be in results", false);
        //            }
        //            if (node.getNodeInformation().getURL().contains(distantHost) ||
        //                node.getNodeInformation().getURL().contains(neighborHost)) {
        //                Assert.assertTrue("Incorrect node selected", false);
        //            }
        //        }
        //        resourceManager.releaseNodes(ns).getBooleanValue();
        //
        //        ns = resourceManager.getAtMostNodes(6, new ThresholdProximityDescriptor(current2neighborDistance,
        //            pivot), null, null);
        //        Assert.assertEquals(6, ns.size());
        //        for (Node node : ns) {
        //            if (node.getNodeInformation().getURL().equals(pivotNode.getNodeInformation().getURL())) {
        //                Assert.assertTrue("Pivot must not be in results", false);
        //            }
        //            if (node.getNodeInformation().getURL().contains(distantHost)) {
        //                Assert.assertTrue("Node from distant host selected", false);
        //            }
        //        }
        //        resourceManager.releaseNodes(ns).getBooleanValue();

        // checking TopologyDescriptor.SINGLE_HOST
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.SINGLE_HOST, null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(9, TopologyDescriptor.SINGLE_HOST, null, null);
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.SINGLE_HOST, null, null);
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.SINGLE_HOST, scriptList, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        // checking TopologyDescriptor.SINGLE_HOST_EXCLUSIVE
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, null, null);
        Assert.assertEquals(1, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());

        if (!ns.get(0).getNodeInformation().getURL().contains(neighborHost)) {
            Assert.assertTrue("Neighbor host shold be selected", false);
        }

        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(9, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, null, null);
        Assert.assertEquals(6, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(2, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, null, null);
        Assert.assertEquals(2, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(3, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, null, null);
        Assert.assertEquals(3, ns.size());
        Assert.assertEquals(3, ns.getExtraNodes().size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, scriptList, null);
        // no hosts matched selection script
        Assert.assertEquals(0, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, null, null);
        // return the host with max capacity
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        // checking TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(1, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(2, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(2, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(3, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(3, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(9, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(9, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(8, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        // current + distant has to be selected
        Assert.assertEquals(8, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(7, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        // current + neighbor has to be selected
        Assert.assertEquals(7, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, scriptList, null);
        // selection script specified => no such set
        Assert.assertEquals(0, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        // get max possible capacity
        Assert.assertEquals(9, ns.size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        // checking TopologyDescriptor.ONE_NODE_PER_HOST_EXCLUSIVE
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(1, ns.size());
        Assert.assertEquals(null, ns.getExtraNodes());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(2, TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(2, ns.size());
        Assert.assertEquals(1, ns.getExtraNodes().size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(3, TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(3, ns.size());
        Assert.assertEquals(6, ns.getExtraNodes().size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        ns = resourceManager.getAtMostNodes(4, TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(3, ns.size());
        Assert.assertEquals(6, ns.getExtraNodes().size());
        resourceManager.releaseNodes(ns).getBooleanValue();

        PAFuture.waitFor(resourceManager.removeNodeSource("remote", true));
    }
}
