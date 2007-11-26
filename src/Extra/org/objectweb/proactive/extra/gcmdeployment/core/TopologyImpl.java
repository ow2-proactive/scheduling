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
package org.objectweb.proactive.extra.gcmdeployment.core;

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
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.ProActiveCounter;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;


public class TopologyImpl implements Topology, Serializable {
    protected long id;
    protected String applicationDescriptorPath;
    protected String deploymentDescriptorPath;
    protected String nodeProvider;
    protected List<String> deploymentPath;
    protected Set<TopologyRuntime> runtimesMap;
    protected List<TopologyImpl> children;

    public TopologyImpl() {
        runtimesMap = new HashSet<TopologyRuntime>();
        children = new ArrayList<TopologyImpl>();
        id = ProActiveCounter.getUniqID();
    }

    public long getId() {
        return id;
    }

    public List<String> getDeploymentPath() {
        return deploymentPath;
    }

    public void setDeploymentPath(List<String> deploymentPath) {
        this.deploymentPath = deploymentPath;
    }

    public Set<TopologyRuntime> getRuntimes() {
        return runtimesMap;
    }

    public void addRuntime(TopologyRuntime runtime) {
        runtimesMap.add(runtime);
    }

    public List<TopologyImpl> getChildren() {
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

    /* -------------
     *  Only for the root node
     */
    static public Topology createTopology(TopologyRootImpl emptyTopology,
        Set<Node> nodes) {
        TopologyRootImpl topology;
        try {
            topology = (TopologyRootImpl) Utils.makeDeepCopy(emptyTopology);

            // Group Node per Runtime
            Map<VMInformation, Set<Node>> sorted = new HashMap<VMInformation, Set<Node>>();
            for (Node node : nodes) {
                VMInformation vmIformation = node.getVMInformation();
                if (sorted.get(vmIformation) == null) {
                    sorted.put(vmIformation, new HashSet<Node>());
                }

                sorted.get(vmIformation).add(node);
            }

            // Add each Runtime to the Topology
            for (VMInformation vmInformation : sorted.keySet()) {
                TopologyRuntime runtime = new TopologyRuntime(vmInformation);
                runtime.addNodes(sorted.get(vmInformation));
                TopologyImpl tn = topology.getNode(vmInformation.getDeploymentId());
                tn.addRuntime(runtime);
            }
        } catch (IOException e) {
            GCMA_LOGGER.error(e);
            topology = null;
        }
        return topology;
    }
}
