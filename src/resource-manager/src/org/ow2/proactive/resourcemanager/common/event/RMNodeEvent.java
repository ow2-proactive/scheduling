/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.common.event;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * This class implements Event object related to an {@link RMNode}
 * This event object is thrown to all Resource Manager Monitors to inform them
 * about a Node's state modification.<BR>
 * This event can be :<BR>
 * -a new Node acquisition.<BR>
 * -a node becoming free.<BR>
 * -a node becoming busy.<BR>
 * -a node becoming to release.<BR>
 * -a node becoming to down.<BR>
 * -a node removed from the Resource Manager.<BR><BR>
 *
 * An RMNodesourceEvent contains all information about its related {@link RMNode}.
 *
 * @see RMMonitoring
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class RMNodeEvent extends RMEvent {

    /** URL of the related node */
    private String nodeUrl = null;

    /** {@link NodeSource} name of the node */
    private String nodeSource = null;

    /** {@link ProActiveDescriptor} name of the node */
    private String PADName = null;

    /** {@link VirtualNode} name of the node */
    private String VnName = null;

    /** Host name of the node */
    private String hostName = null;

    /** Java virtual machine name of the node */
    private String VMName = null;

    /** State of the associated node */
    private NodeState nodeState;

    /**
     * ProActive empty constructor
     */
    public RMNodeEvent() {
    }

    /**
     * Creates the node event object.
     */
    public RMNodeEvent(RMNode rmnode, RMEventType type) {
        this.nodeUrl = rmnode.getNodeURL();
        this.nodeSource = rmnode.getNodeSourceId();
        this.PADName = "";
        this.VnName = rmnode.getVNodeName();
        this.hostName = rmnode.getHostName();
        this.VMName = rmnode.getDescriptorVMName();
        this.nodeState = rmnode.getState();
        this.type = type;
    }

    /**
     * Compare two RMNodeEvent objects.
     *
     * @param obj RMNodeEvent object to compare.
     * @return true if the two events represent the same Node.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RMNodeEvent) {
            return ((RMNodeEvent) obj).nodeUrl.equals(this.nodeUrl);
        }
        return false;
    }

    /**
     * Returns node's URL.
     *
     * @return URL of the node.
     */
    public String getNodeUrl() {
        return this.nodeUrl;
    }

    /**
     * Returns {@link NodeSource} name of the node
     *
     * @return name of the node.
     */
    public String getNodeSource() {
        return this.nodeSource;
    }

    /**
     * Returns {@link ProActiveDescriptor} name of the node.
     *
     * @return ProActiveDescriptor name of the node.
     */
    public String getPADName() {
        return this.PADName;
    }

    /**
     * Returns {@link VirtualNode} name of the node.
     *
     * @return Virtual Node name of the node.
     */
    public String getVnName() {
        return this.VnName;
    }

    /**
     * Returns host name of the node.
     *
     * @return host name of the node.
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Returns java Virtual machine name of the node.
     *
     * @return java Virtual machine name of the node.
     */
    public String getVMName() {
        return this.VMName;
    }

    /**
     * Returns the state of the node related to this event.
     *
     * @return state of the node.
     */
    public NodeState getNodeState() {
        return nodeState;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getEventType() + "[" + this.nodeUrl + ":" + this.nodeState + "]";
    }

}
