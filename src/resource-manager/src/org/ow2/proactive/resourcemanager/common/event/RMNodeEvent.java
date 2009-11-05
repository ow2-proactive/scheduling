/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.common.event;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * This class implements Event object related to an {@link RMNode}
 * This immutable event object is thrown to all Resource Manager Monitors to inform them
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
public final class RMNodeEvent extends RMEvent {

    /** URL of the related node */
    private final String nodeUrl;

    /** {@link NodeSource} name of the node */
    private final String nodeSource;

    /** {@link ProActiveDescriptor} name of the node */
    private final String PADName;

    /** {@link VirtualNode} name of the node */
    private final String VnName;

    /** Host name of the node */
    private final String hostName;

    /** Java virtual machine name of the node */
    private final String VMName;

    /** The state of the associated node */
    private final NodeState nodeState;

    /** The previous state of the associated node */
    private final NodeState previousNodeState;

    /** Time of the last status update */
    private final Calendar stateChangeTime;

    /**
     * ProActive empty constructor
     */
    public RMNodeEvent() {
        this.nodeUrl = null;
        this.nodeSource = null;
        this.PADName = null;
        this.VnName = null;
        this.hostName = null;
        this.VMName = null;
        this.nodeState = null;
        this.previousNodeState = null;
        this.stateChangeTime = null;
    }

    /**
     * Creates a node event object without previous node state.
     * @param rmNode the node concerned by this event
     * @param eventType the resource manager event type 
     */
    public RMNodeEvent(final RMNode rmNode, final RMEventType eventType) {
        this(rmNode, eventType, null);
    }

    /**
     * Creates a node event object with a previous node state. 
     * @param rmNode the node concerned by this event
     * @param eventType the resource manager event type
     * @param previousNodeState the previous state of the node concerned by this event
     */
    public RMNodeEvent(final RMNode rmNode, final RMEventType eventType, final NodeState previousNodeState) {
        super(eventType);
        this.nodeUrl = rmNode.getNodeURL();
        this.nodeSource = rmNode.getNodeSourceId();
        this.PADName = "";
        this.VnName = rmNode.getVNodeName();
        this.hostName = rmNode.getHostName();
        this.VMName = rmNode.getDescriptorVMName();
        this.nodeState = rmNode.getState();
        this.previousNodeState = previousNodeState;
        this.stateChangeTime = rmNode.getStateChangeTime();
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
     * @return the state of the node.
     */
    public NodeState getNodeState() {
        return this.nodeState;
    }

    /**
     * Returns the previous state of the node related to this event.
     *
     * @return the previous state of the node.
     */
    public NodeState getPreviousNodeState() {
        return this.previousNodeState;
    }

    /**
     * Gets the time when state changed the last time
     * @return the time when state changed the last time
     */
    public String getStateChangeTime() {
        return new SimpleDateFormat().format(this.stateChangeTime.getTime());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getEventType() + "[" + this.nodeUrl + ":" + this.nodeState + "]";
    }

}
