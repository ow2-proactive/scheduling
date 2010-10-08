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

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.frontend.topology.descriptor.BestProximityDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructure2;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


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
 * Test creates 6 nodes on the current machine and one node on distant and neighbor machine.
 * Nodes are created using ssh infrastructure manager, so all hosts have to be reachable by ssh.
 * RM distrib path as well as jdk path must be the same on all machines.
 *
 */
//build -DdistantHost="bound.inria.fr" -DneighborHost="eon1.inria.fr" -Dtest="**/*SelectionTest*" junit.rm
public class SelectionTest extends FunctionalTest {

    private String vmPropSelectionScriptpath = this.getClass().getResource(
            "../selectionscript/vmPropertySelectionScript.js").getPath();

    private String vmPropKey1 = "myProperty1";
    private String vmPropValue1 = "myValue1";

    /** Actions to be Perform by this test.
    * The method is called automatically by Junit framework.
    * @throws Exception If the test fails.
    */
    @org.junit.Test
    public void action() throws Exception {

        String currentHost = System.getenv("HOSTNAME");
        String rmUrl = URIBuilder.buildURI(currentHost, "", "rmi").toString();

        // checking if properties distantHost and neighborHost are defined
        String distantHost = System.getProperty("distantHost");
        String neighborHost = System.getProperty("neighborHost");

        if (distantHost != null && !distantHost.equals("${distantHost}") && neighborHost != null &&
            !neighborHost.equals("${neighborHost}")) {

            String rmHome = System.getProperty("pa.rm.home");
            String rmCredPath = rmHome + "/config/authentication/rm.cred";
            String javaExec = System.getenv("JAVA_HOME") + "/bin/java";

            // properties are defined, trying to deploy nodes to these hosts
            BooleanWrapper result = RMTHelper.getResourceManager().createNodeSource("remote",
                    SSHInfrastructure2.class.getName(), new Object[] { "", // ssh options
                            javaExec, // java executable path
                            rmHome, // rm distrib path
                            "10000", // node lookup timeout
                            "Linux", // os
                            "", // java options
                            rmUrl, // rm url
                            FileToBytesConverter.convertFileToByteArray(new File(rmCredPath)), // rm credential
                            (distantHost + "\n" + neighborHost).getBytes() }, StaticPolicy.class.getName(),
                    null);

            if (result.booleanValue()) {
                RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
                RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            }

            // we are good - all remote nodes registered
        } else {
            // no parameters provided
            RMTHelper.log("Nothing to do. Set up distantHost & neighborHost parameters.");
            return;
        }

        // adding default nodes
        RMTHelper.createGCMLocalNodeSource();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.GCM_LOCAL);

        // creating the selection script object
        SelectionScript script = new SelectionScript(new File(vmPropSelectionScriptpath), new String[] {
                this.vmPropKey1, this.vmPropValue1 }, true);
        List<SelectionScript> scriptList = new LinkedList<SelectionScript>();
        scriptList.add(script);

        ResourceManager resourceManager = RMTHelper.getResourceManager();

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        String node1 = "node1";

        //a node with the VM properties
        HashMap<String, String> vmProperties = new HashMap<String, String>();
        vmProperties.put(this.vmPropKey1, this.vmPropValue1);

        String node1URL = RMTHelper.createNode(node1, vmProperties).getNodeInformation().getURL();
        resourceManager.addNode(node1URL, NodeSource.GCM_LOCAL);

        //wait node adding event
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);

        // so now we have 6 nodes locally: 5 default, 1 marked with property
        // and two remote nodes - one from neighbor host another from distant

        // checking TopologyDescriptor.ARBITRARY
        NodeSet ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.ARBITRARY, null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(8, TopologyDescriptor.ARBITRARY, null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.ARBITRARY, null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.ARBITRARY, scriptList, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        // checking TopologyDescriptor.BEST_PROXIMITY
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(8, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        Node pivotNode = null;
        ns = resourceManager.getAtMostNodes(6, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(6, ns.size());
        for (Node node : ns) {
            if (pivotNode == null)
                pivotNode = node;
            if (!node.getNodeInformation().getURL().contains(currentHost)) {
                Assert.assertTrue("All nodes have to be from " + currentHost, false);
            }
        }
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(7, TopologyDescriptor.BEST_PROXIMITY, null, null);
        Assert.assertEquals(7, ns.size());
        for (Node node : ns) {
            if (node.getNodeInformation().getURL().contains(distantHost)) {
                Assert.assertTrue("Node from distant host selected", false);
            }
        }
        resourceManager.releaseNodes(ns).booleanValue();
        // pivot scenario
        List<Node> pivot = new LinkedList<Node>();
        pivot.add(pivotNode);
        ns = resourceManager.getAtMostNodes(6,
                new BestProximityDescriptor(BestProximityDescriptor.MAX, pivot), null, null);
        Assert.assertEquals(6, ns.size());
        for (Node node : ns) {
            if (node.getNodeInformation().getURL().equals(pivotNode.getNodeInformation().getURL())) {
                Assert.assertTrue("Pivot must not be in results", false);
            }
            if (node.getNodeInformation().getURL().contains(distantHost)) {
                Assert.assertTrue("Node from distant host selected", false);
            }
        }
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(7,
                new BestProximityDescriptor(BestProximityDescriptor.MAX, pivot), null, null);
        Assert.assertEquals(7, ns.size());
        for (Node node : ns) {
            if (node.getNodeInformation().getURL().equals(pivotNode.getNodeInformation().getURL())) {
                Assert.assertTrue("Pivot must not be in results", false);
            }
        }
        resourceManager.releaseNodes(ns).booleanValue();

        // checking TopologyDescriptor.ThresholdProximityDescriptor
        ns = resourceManager.getAtMostNodes(1, new ThresholdProximityDescriptor(Long.MAX_VALUE), null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(8, new ThresholdProximityDescriptor(Long.MAX_VALUE), null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager
                .getAtMostNodes(100, new ThresholdProximityDescriptor(Long.MAX_VALUE), null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

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
            Assert.assertTrue("Distant host is close to current than neighbor according to the topology",
                    false);
        }

        System.out.println("Distance between " + currentHost + " and " + neighborHost + " is " +
            current2neighborDistance);
        System.out.println("Distance between " + currentHost + " and " + distantHost + " is " +
            current2distantDistance);
        System.out.println("Distance between " + neighborHost + " and " + distantHost + " is " +
            distant2neightborDistance);

        long maxThreshold = Math.max(current2neighborDistance, Math.max(current2distantDistance,
                distant2neightborDistance));

        ns = resourceManager.getAtMostNodes(100, new ThresholdProximityDescriptor(
            current2neighborDistance - 1), null, null);
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, new ThresholdProximityDescriptor(current2neighborDistance),
                null, null);
        Assert.assertEquals(7, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, new ThresholdProximityDescriptor(maxThreshold - 1), null,
                null);
        for (Node node : ns) {
            if (node.getNodeInformation().getURL().contains(distantHost)) {
                Assert.assertTrue("Node from distant host selected", false);
            }
        }
        Assert.assertEquals(7, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(10, new ThresholdProximityDescriptor(maxThreshold), null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();
        // pivot scenario
        ns = resourceManager.getAtMostNodes(1, new ThresholdProximityDescriptor(0, pivot), null, null);
        Assert.assertEquals(1, ns.size());
        for (Node node : ns) {
            if (node.getNodeInformation().getURL().equals(pivotNode.getNodeInformation().getURL())) {
                Assert.assertTrue("Pivot must not be in results", false);
            }
            if (node.getNodeInformation().getURL().contains(distantHost) ||
                node.getNodeInformation().getURL().contains(neighborHost)) {
                Assert.assertTrue("Incorrect node selected", false);
            }
        }
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(6, new ThresholdProximityDescriptor(current2neighborDistance,
            pivot), null, null);
        Assert.assertEquals(6, ns.size());
        for (Node node : ns) {
            if (node.getNodeInformation().getURL().equals(pivotNode.getNodeInformation().getURL())) {
                Assert.assertTrue("Pivot must not be in results", false);
            }
            if (node.getNodeInformation().getURL().contains(distantHost)) {
                Assert.assertTrue("Node from distant host selected", false);
            }
        }
        resourceManager.releaseNodes(ns).booleanValue();

        // checking TopologyDescriptor.SINGLE_HOST
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.SINGLE_HOST, null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(8, TopologyDescriptor.SINGLE_HOST, null, null);
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.SINGLE_HOST, null, null);
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.SINGLE_HOST, scriptList, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        // checking TopologyDescriptor.SINGLE_HOST_EXCLUSIVE
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(8, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, null, null);
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(2, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, null, null);
        Assert.assertEquals(6, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, scriptList, null);
        Assert.assertEquals(0, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        // checking TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE
        ns = resourceManager.getAtMostNodes(1, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(1, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(8, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(8, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(2, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(2, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(7, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, null, null);
        Assert.assertEquals(7, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        ns = resourceManager.getAtMostNodes(100, TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, scriptList,
                null);
        Assert.assertEquals(0, ns.size());
        resourceManager.releaseNodes(ns).booleanValue();

        PAFuture.waitFor(resourceManager.removeNodeSource("remote", true));
    }
}
