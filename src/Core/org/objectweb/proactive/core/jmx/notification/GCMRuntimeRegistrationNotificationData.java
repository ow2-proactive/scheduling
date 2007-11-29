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
package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;


public class GCMRuntimeRegistrationNotificationData implements Serializable {
    private String childURL;
    private long deploymentId;
    private long topologyId;
    private Set<Node> nodes;

    public GCMRuntimeRegistrationNotificationData() {
        // No-args constructor
    }

    public GCMRuntimeRegistrationNotificationData(String childURL,
        long deploymentId, long topologyId, Set<Node> nodes) {
        this.childURL = childURL;
        this.deploymentId = deploymentId;
        this.topologyId = topologyId;
        this.nodes = nodes;
    }

    public String getChildURL() {
        return childURL;
    }

    public long getDeploymentId() {
        return deploymentId;
    }

    public long getTopologyId() {
        return topologyId;
    }

    public Set<Node> getNodes() {
        return nodes;
    }
}
