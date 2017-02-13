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
package org.ow2.proactive.resourcemanager.common.event;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;


/**
 * This class implements Event object related to an {@code RMNode}
 * This immutable event object is thrown to all Resource Manager Monitors to inform them
 * about a Node's state modification.
 * <p>
 * This event can be:
 * <ul>
 *     <li>a new Node acquisition.</li>
 *     <li>a node becoming free.</li>
 *     <li>a node becoming busy.</li>
 *     <li>a node becoming to release.</li>
 *     <li>a node becoming to down.</li>
 *     <li>a node removed from the Resource Manager.</li>
 * </ul>
 *
 * An {@link RMNodeSourceEvent} contains all information about its related {@code RMNode}.
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

    /** Node source name of the node */
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

    private final String nodeProvider;

    private final String nodeOwner;

    /** The description of the node */
    private String description;

    /** The node's default JMX url */
    private String defaultJMXUrl;

    /** The node's proactive JMX url */
    private String proactiveJMXUrl;

    private final boolean isLocked;

    private final long lockTime;

    private final String nodeLocker;

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
        this.nodeProvider = null;
        this.nodeOwner = null;
        this.description = null;
        this.defaultJMXUrl = null;
        this.proactiveJMXUrl = null;
        this.isLocked = false;
        this.lockTime = -1;
        this.nodeLocker = null;
    }

    /**
     * Creates a node event object without previous node state.
     * Used to represent the resource manager state @see RMInitialState.
     * @param rmNode the node concerned by this event
     */
    public RMNodeEvent(final RMNodeDescriptor rmNode) {
        this(rmNode, null, null, null);
    }

    /**
     * Creates a node event object with a previous node state. 
     * @param rmNode the node concerned by this event
     * @param eventType the resource manager event type
     * @param previousNodeState the previous state of the node concerned by this event
     * @param initiator the client which initiates the event
     */
    public RMNodeEvent(final RMNodeDescriptor rmNode, final RMEventType eventType, final NodeState previousNodeState,
            final String initiator) {
        super(eventType);

        this.initiator = initiator;
        this.nodeUrl = rmNode.getNodeURL();
        this.nodeSource = rmNode.getNodeSourceName();
        this.PADName = "";
        this.VnName = rmNode.getVNodeName();
        this.hostName = rmNode.getHostName();
        this.VMName = rmNode.getDescriptorVMName();
        this.nodeState = rmNode.getState();
        this.description = rmNode.getNodeInfo();
        this.defaultJMXUrl = rmNode.getDefaultJMXUrl();
        this.proactiveJMXUrl = rmNode.getProactiveJMXUrl();

        // when node is requested to be removed
        // there is no state change in the node itself
        // so the time stamp of the event has to be used
        if (eventType != RMEventType.NODE_REMOVED) {
            this.timeStamp = rmNode.getStateChangeTime();
        }
        this.previousNodeState = previousNodeState;
        this.nodeProvider = rmNode.getProviderName();
        this.nodeOwner = rmNode.getOwnerName();

        this.isLocked = rmNode.isLocked();
        this.lockTime = rmNode.getLockTime();
        this.nodeLocker = rmNode.getLockedBy();
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
     * Returns {@code NodeSource} name of the node
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
     * Gets the provider of the node (who created and deployed it)
     * @return the node provider name
     */
    public String getNodeProvider() {
        return nodeProvider;
    }

    /**
     * Gets the owner of the node (who currently running computations on it)
     * @return the node owner name
     */
    public String getNodeOwner() {
        return nodeOwner;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.nodeUrl + " : " + this.getEventType() + ((counter > 0) ? " counter: " + counter + " " : "") + "[" +
               this.nodeState + "]";
    }

    /**
     * Gets information about the associated node
     * @return information about the associated node
     */
    public String getNodeInfo() {
        return this.description;
    }

    /**
     * Gets the node default JMX url.
     * @return the node default JMX url
     */
    public String getDefaultJMXUrl() {
        return defaultJMXUrl;
    }

    /**
     * Gets the node proactive JMX url.
     * @return the node proactive JMX url
     */
    public String getProactiveJMXUrl() {
        return proactiveJMXUrl;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public long getLockTime() {
        return lockTime;
    }

    public String getNodeLocker() {
        return nodeLocker;
    }

}
