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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.util.ProActiveCounter;


public class DeploymentNode {
    protected long id;
    protected String applicationDescriptorPath;
    protected String deploymentDescriptorPath;
    protected List<String> deploymentPath;
    protected Set<VMNodeList> nodeMap;
    protected List<DeploymentNode> children;

    public DeploymentNode() {
        nodeMap = new HashSet<VMNodeList>();
        children = new ArrayList<DeploymentNode>();
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

    public Set<VMNodeList> getNodeMap() {
        return nodeMap;
    }

    public void addVMNodes(VMNodeList vmNode) {
        nodeMap.add(vmNode);
    }

    public List<DeploymentNode> getChildren() {
        return children;
    }

    public void addChildren(DeploymentNode node) {
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
}
