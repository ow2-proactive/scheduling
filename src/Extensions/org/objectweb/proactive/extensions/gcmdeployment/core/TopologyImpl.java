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
package org.objectweb.proactive.extensions.gcmdeployment.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.objectweb.proactive.gcmdeployment.GCMHost;
import org.objectweb.proactive.gcmdeployment.Topology;

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;


public class TopologyImpl implements Topology, Serializable {
    protected long id;
    protected String applicationDescriptorPath;
    protected String deploymentDescriptorPath;
    protected String nodeProvider;
    protected List<String> deploymentPath;
    protected Map<String, GCMHost> hostsMap;
    protected List<TopologyImpl> children;

    public TopologyImpl() {
        hostsMap = new HashMap<String, GCMHost>();
        children = new ArrayList<TopologyImpl>();
        id = ProActiveCounter.getUniqID();
    }

    public long getId() {
        return id;
    }

    public List<String> getDeploymentPath() {
        return deploymentPath;
    }

    public String getDeploymentPathStr() {
        StringBuilder sb = new StringBuilder();
        for (String path : deploymentPath) {
            sb.append(path);
            sb.append(':');
        }
        sb.deleteCharAt(sb.length() - 1);
        return new String(sb);
    }

    public void setDeploymentPath(List<String> deploymentPath) {
        this.deploymentPath = deploymentPath;
    }

    public Set<GCMHost> getHosts() {
        return new HashSet<GCMHost>(hostsMap.values());
    }

    public List<? extends Topology> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return children.size() != 0;
    }

    public void addChildren(TopologyImpl node) {
        children.add(node);
    }

    public String getApplicationDescriptorPath() {
        return applicationDescriptorPath;
    }

    public void setApplicationDescriptorPath(String applicationDescriptorPath) {
        this.applicationDescriptorPath = applicationDescriptorPath;
    }

    public String getDeploymentDescriptorPath() {
        return deploymentDescriptorPath;
    }

    public void setDeploymentDescriptorPath(String deploymentDescriptorPath) {
        this.deploymentDescriptorPath = deploymentDescriptorPath;
    }

    public String getNodeProvider() {
        return nodeProvider;
    }

    public void setNodeProvider(String nodeProvider) {
        this.nodeProvider = nodeProvider;
    }

    /*
     * ------------- Only for the root node
     */
    static private Map<Long, Set<Node>> groupByTopologyId(Set<Node> nodes) {
        Map<Long, Set<Node>> ret = new HashMap<Long, Set<Node>>();
        for (Node node : nodes) {
            long id = node.getVMInformation().getTopologyId();
            if (ret.get(id) == null) {
                ret.put(id, new HashSet<Node>());
            }
            Set<Node> nodeSet = ret.get(id);
            nodeSet.add(node);
        }
        return ret;
    }

    static private Map<String, Set<Node>> groupByHost(Set<Node> nodes) {
        Map<String, Set<Node>> ret = new HashMap<String, Set<Node>>();
        for (Node node : nodes) {
            String hostname = node.getVMInformation().getHostName();
            if (ret.get(hostname) == null) {
                ret.put(hostname, new HashSet<Node>());
            }
            Set<Node> nodeSet = ret.get(hostname);
            nodeSet.add(node);
        }
        return ret;
    }

    static public Topology createTopology(TopologyRootImpl emptyTopology, Set<Node> nodes) {
        TopologyRootImpl topology;
        try {
            topology = (TopologyRootImpl) Utils.makeDeepCopy(emptyTopology);
            updateTopology(topology, nodes);
        } catch (IOException e) {
            GCMA_LOGGER.warn(e);
            topology = null;
        }
        return topology;
    }

    static public void updateTopology(Topology topology, Set<Node> nodes) {
        TopologyRootImpl root = (TopologyRootImpl) topology;

        Map<Long, Set<Node>> groupById = groupByTopologyId(nodes);
        for (Long id : groupById.keySet()) {
            TopologyImpl node = root.getNode(id);
            node.updateNodes(groupById.get(id));
        }
    }

    private void updateNodes(Set<Node> nodes) {
        Map<String, Set<Node>> byHost = groupByHost(nodes);
        for (String host : byHost.keySet()) {
            if (hostsMap.containsKey(host)) {
                hostsMap.get(host).update(byHost.get(host));
            } else {
                GCMHost gcmHost = new GCMHost(host, byHost.get(host));
                hostsMap.put(host, gcmHost);
            }
        }
    }
}
