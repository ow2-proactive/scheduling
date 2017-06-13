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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;

import com.google.common.collect.ImmutableMap;


/**
 * @author ActiveEon Team
 * @since 09/06/2017
 */
public class TopologyNodesFilterTest {

    @Test
    public void testFilterNodesBySingleHost2NodesKO() {
        Map map = ImmutableMap.of("host1", 1, "host2", 1);
        unifedTestMethod(TopologyDescriptor.SINGLE_HOST, 2, map, 0);
    }

    @Test
    public void testFilterNodesBySingleHost2NodesOK() {
        Map map = ImmutableMap.of("host1", 2);
        unifedTestMethod(TopologyDescriptor.SINGLE_HOST, 2, map, 2);
    }

    @Test
    public void testFilterNodesBySingleHostExclusive5NodesOK() {
        Map map = ImmutableMap.of("host1", 2, "host2", 1, "host3", 1, "host4", 1);
        unifedTestMethod(TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, 2, map, 2);

    }

    @Test
    public void testFilterNodesBySingleHostExclusive5NodesKO() {
        Map map = ImmutableMap.of("host1", 2, "host2", 1, "host3", 1, "host4", 1);
        unifedTestMethod(TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, 3, map, 0);

    }

    @Test
    public void testFilterNodesByArbitaryNodes4OK() {
        Map map = ImmutableMap.of("host1", 1, "host2", 1, "host3", 1, "host4", 1);
        unifedTestMethod(TopologyDescriptor.ARBITRARY, 2, map, 4);
    }

    @Test
    public void testFilterNodesByArbitaryNodes4KO() {
        Map map = ImmutableMap.of("host1", 1, "host2", 1, "host3", 1, "host4", 1);
        unifedTestMethod(TopologyDescriptor.ARBITRARY, 5, map, 4);
    }

    @Test
    public void testFilterNodesBySingleHostExclusive10NodesOK() {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedTestMethod(TopologyDescriptor.SINGLE_HOST_EXCLUSIVE, 3, map, 3);
    }

    @Test
    public void testFilterNodesBySingleHost10NodesOK() {
        Map map = ImmutableMap.of("host1", 2, "host2", 3, "host3", 2, "host4", 1, "host5", 2);
        unifedTestMethod(TopologyDescriptor.SINGLE_HOST, 2, map, 9);
    }

    @Test
    public void testFilterNodesBySingleHost10NodesKO() {
        Map map = ImmutableMap.of("host1", 2, "host2", 2, "host3", 2, "host4", 2, "host5", 2);
        unifedTestMethod(TopologyDescriptor.SINGLE_HOST, 3, map, 0);
    }

    private void unifedTestMethod(TopologyDescriptor topology, int requiredNodesNumber,
            Map<String, Integer> groupedNodeByHost, int outputNodesNumber) {
        Criteria crit = new Criteria(requiredNodesNumber);
        crit.setTopology(topology);
        crit.setScripts(null);
        crit.setBlackList(null);
        crit.setBestEffort(false);
        List<RMNode> arrangedNodes = new ArrayList<>();
        for (String host : groupedNodeByHost.keySet()) {
            for (int i = 0; i < groupedNodeByHost.get(host); i++) {
                arrangedNodes.add(createMockeNode(host));
            }
        }

        List<RMNode> arrangedFilteredNodes = new TopologyNodesFilter().filterNodes(crit, arrangedNodes);
        assertEquals(outputNodesNumber, arrangedFilteredNodes.size());

    }

    private RMNode createMockeNode(String hostName) {
        RMNode rmNode = mock(RMNode.class);
        when(rmNode.getHostName()).thenReturn(hostName);
        return rmNode;
    }

}
