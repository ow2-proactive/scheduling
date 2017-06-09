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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;


/**
 * @author ActiveEon Team
 * @since 09/06/2017
 */
public class TopologyNodesFilter {

    public List<RMNode> filterNodes(Criteria criteria, List<RMNode> arrangedNodes) {
        if (criteria.getTopology().toString().equals(TopologyDescriptor.SINGLE_HOST.toString()) ||
            criteria.getTopology().toString().equals(TopologyDescriptor.SINGLE_HOST_EXCLUSIVE.toString())) {
            return filterBySingleHost(criteria, arrangedNodes);
        } else
            return arrangedNodes;
    }

    private List<RMNode> filterBySingleHost(Criteria criteria, List<RMNode> arrangedNodes) {
        List<RMNode> arrangedFilteredNodes = new LinkedList<>();
        Map<String, List<RMNode>> groupedNodeByHost = new LinkedHashMap<>();
        for (RMNode node : arrangedNodes) {
            if (!groupedNodeByHost.containsKey(node.getHostName())) {
                groupedNodeByHost.put(node.getHostName(), new LinkedList<RMNode>());
            }
            groupedNodeByHost.get(node.getHostName()).add(node);
        }
        for (String host : groupedNodeByHost.keySet()) {
            if (groupedNodeByHost.get(host).size() >= criteria.getSize()) {
                arrangedFilteredNodes.addAll(groupedNodeByHost.get(host));
            }
        }

        return arrangedFilteredNodes;
    }

}
