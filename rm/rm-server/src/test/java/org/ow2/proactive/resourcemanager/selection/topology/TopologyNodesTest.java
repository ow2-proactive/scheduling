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
package org.ow2.proactive.resourcemanager.selection.topology;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.topology.pinging.HostsPinger;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import com.google.common.collect.ImmutableMap;


/**
 * @author ActiveEon Team
 * @since 09/06/2017
 */
public class TopologyNodesTest {

    private static Map<String, InetAddress> mockedInetAddresses = new HashMap<>();

    private static int nodeNameIndex = 0;

    @BeforeClass
    public static void beforeClass() {
        PAResourceManagerProperties.RM_TOPOLOGY_PINGER.updateProperty(HostsPinger.class.getName());
        PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.updateProperty("true");
        PAResourceManagerProperties.RM_TOPOLOGY_DISTANCE_ENABLED.updateProperty("false");
    }

    @Test
    public void testFilterNodesBySingleHost2NodesKO() {
        Map map = ImmutableMap.of("host1", 1, "host2", 1);
        unifedFilterNodeTestMethod(TopologyDescriptor.SINGLE_HOST, 2, map, 0);
    }

    @Test
    public void testFilterNodesBySingleHost2NodesOK() {
        Map map = ImmutableMap.of("host1", 2);
        unifedFilterNodeTestMethod(TopologyDescriptor.SINGLE_HOST, 2, map, 2);
    }

    @Test
    public void testFilterNodesBySingleHostExclusive5NodesOK() {
        Map map = ImmutableMap.of("host1", 2, "host2", 1, "host3", 1, "host4", 1);
        unifedFilterNodeTestMethod(TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, 2, map, 2);

    }

    @Test
    public void testFilterNodesBySingleHostExclusive5NodesKO() {
        Map map = ImmutableMap.of("host1", 2, "host2", 1, "host3", 1, "host4", 1);
        unifedFilterNodeTestMethod(TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, 3, map, 0);

    }

    @Test
    public void testFilterNodesByArbitaryNodes4OK() {
        Map map = ImmutableMap.of("host1", 1, "host2", 1, "host3", 1, "host4", 1);
        unifedFilterNodeTestMethod(TopologyDescriptor.ARBITRARY, 2, map, 4);
    }

    @Test
    public void testFilterNodesByArbitaryNodes4KO() {
        Map map = ImmutableMap.of("host1", 1, "host2", 1, "host3", 1, "host4", 1);
        unifedFilterNodeTestMethod(TopologyDescriptor.ARBITRARY, 5, map, 4);
    }

    @Test
    public void testFilterNodesBySingleHostExclusive10NodesOK() {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedFilterNodeTestMethod(TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, 3, map, 3);
    }

    @Test
    public void testFilterNodesBySingleHost10NodesOK() {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedFilterNodeTestMethod(TopologyDescriptor.SINGLE_HOST, 2, map, 9);
    }

    @Test
    public void testFilterNodesBySingleHost10NodesKO() {
        Map map = ImmutableMap.of("host1", 2, "host2", 2, "host3", 2, "host4", 2, "host5", 2);
        unifedFilterNodeTestMethod(TopologyDescriptor.SINGLE_HOST, 3, map, 0);
    }

    @Test
    public void testSelectNodesByDifferentHost10NodesOK() throws ClassNotFoundException {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedTopologyTestMethod(TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 1, map, -1);
    }

    @Test
    public void testSelectNodesByDifferentHost10NodesOK2() throws ClassNotFoundException {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedTopologyTestMethod(TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 2, map, -1);
    }

    @Test
    public void testSelectNodesByDifferentHost10NodesOK3() throws ClassNotFoundException {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedTopologyTestMethod(TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 3, map, -1);
    }

    @Test
    public void testSelectNodesByDifferentHost10NodesOK4() throws ClassNotFoundException {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedTopologyTestMethod(TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 4, map, -1);
    }

    @Test
    public void testSelectNodesByDifferentHost10NodesOK5() throws ClassNotFoundException {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedTopologyTestMethod(TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 5, map, -1);
    }

    @Test
    public void testSelectNodesByDifferentHost10NodesKO() throws ClassNotFoundException {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedTopologyTestMethod(TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE, 6, map, 0);
    }

    private void unifedTopologyTestMethod(TopologyDescriptor topology, int requiredNodesNumber,
            Map<String, Integer> groupedNodeByHost, int outputNodesNumber) throws ClassNotFoundException {
        Criteria crit = new Criteria(requiredNodesNumber);
        crit.setTopology(topology);
        crit.setScripts(null);
        crit.setBlackList(null);
        crit.setBestEffort(false);
        List<Node> arrangedNodes = new ArrayList<>();
        for (String host : groupedNodeByHost.keySet()) {
            for (int i = 0; i < groupedNodeByHost.get(host); i++) {
                arrangedNodes.add(createMockeNode(host));
            }
        }

        // simulate what the selection manager is doing
        TopologyManager manager = new TopologyManager();
        for (Node node : arrangedNodes) {
            manager.addNode(node);
        }
        TopologyHandler handler = manager.getHandler(crit.getTopology());

        NodeSet selectedNodes = handler.select(crit.getSize(), arrangedNodes);

        if (selectedNodes.size() < crit.getSize() && !crit.isBestEffort()) {
            selectedNodes.clear();
            if (selectedNodes.getExtraNodes() != null) {
                selectedNodes.getExtraNodes().clear();
            }
        }

        Set<Node> allNodes = new HashSet<>(selectedNodes);
        if (selectedNodes.getExtraNodes() != null) {
            allNodes.addAll((selectedNodes.getExtraNodes()));
        }
        System.out.println("Selected nodes : " + allNodes);
        if (outputNodesNumber >= 0) {
            assertEquals(outputNodesNumber, allNodes.size());
        } else {
            // different host exclusive test
            Map<String, Integer> checkNodesByHost = new HashMap<>(groupedNodeByHost);
            for (Node node : allNodes) {
                String hostName = node.getVMInformation().getHostName();
                checkNodesByHost.put(hostName, checkNodesByHost.get(hostName) - 1);
            }
            System.out.println("Free nodes after selection : " + checkNodesByHost);
            Assert.assertEquals(requiredNodesNumber, checkNodesByHost.entrySet()
                                                                     .stream()
                                                                     .filter(entry -> entry.getValue() == 0)
                                                                     .collect(Collectors.toList())
                                                                     .size());
        }
    }

    private void unifedFilterNodeTestMethod(TopologyDescriptor topology, int requiredNodesNumber,
            Map<String, Integer> groupedNodeByHost, int outputNodesNumber) {
        mockedInetAddresses.clear();
        Criteria crit = new Criteria(requiredNodesNumber);
        crit.setTopology(topology);
        crit.setScripts(null);
        crit.setBlackList(null);
        crit.setBestEffort(false);
        List<RMNode> arrangedNodes = new ArrayList<>();
        for (String host : groupedNodeByHost.keySet()) {
            for (int i = 0; i < groupedNodeByHost.get(host); i++) {
                arrangedNodes.add(createMockeRMNode(host));
            }
        }

        List<RMNode> arrangedFilteredNodes = new TopologyNodesFilter().filterNodes(crit, arrangedNodes);
        if (outputNodesNumber >= 0) {
            assertEquals(outputNodesNumber, arrangedFilteredNodes.size());
        } else {
            // different host exclusive test
            Map<String, Integer> checkNodesByHost = new HashMap<>(groupedNodeByHost);
            for (RMNode rmNode : arrangedFilteredNodes) {
                String hostName = rmNode.getHostName();
                checkNodesByHost.put(hostName, checkNodesByHost.get(hostName) - 1);
                System.out.println(hostName + " : " + checkNodesByHost.get(hostName));
            }
            Assert.assertEquals(requiredNodesNumber, checkNodesByHost.entrySet()
                                                                     .stream()
                                                                     .filter(entry -> entry.getValue() == 0)
                                                                     .collect(Collectors.toList())
                                                                     .size());
        }

    }

    private RMNode createMockeRMNode(String hostName) {
        RMNode rmNode = mock(RMNode.class);
        when(rmNode.getHostName()).thenReturn(hostName);
        return rmNode;
    }

    private Node createMockeNode(String hostName) {
        Node node = mock(Node.class);
        String nodeName = hostName + "_" + nodeNameIndex++;
        VMInformation vmInformation = mock(VMInformation.class);
        NodeInformation nodeInformation = mock(NodeInformation.class);
        if (!mockedInetAddresses.containsKey(hostName)) {
            mockedInetAddresses.put(hostName, mock(InetAddress.class));
        }
        InetAddress inetAddress = mockedInetAddresses.get(hostName);
        when(node.getVMInformation()).thenReturn(vmInformation);
        when(node.getNodeInformation()).thenReturn(nodeInformation);
        when(nodeInformation.getVMInformation()).thenReturn(vmInformation);
        when(nodeInformation.getName()).thenReturn(nodeName);
        when(vmInformation.getHostName()).thenReturn(hostName);
        when(vmInformation.getInetAddress()).thenReturn(inetAddress);
        when(inetAddress.getHostName()).thenReturn(hostName);
        when(node.toString()).thenReturn(nodeName);

        return node;
    }

}
