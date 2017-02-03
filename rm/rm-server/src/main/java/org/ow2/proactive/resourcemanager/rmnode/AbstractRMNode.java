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
package org.ow2.proactive.resourcemanager.rmnode;

import java.io.Serializable;
import java.util.Date;

import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeDescriptor;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;


/**
 * Defines abstractions common to all RM node implementations.
 *
 * @author ActiveEon Team
 * @since 16/01/17
 *
 * @see RMDeployingNode
 * @see RMNodeImpl
 */
public abstract class AbstractRMNode implements RMNode, Serializable {

    /** The add event */
    protected RMNodeEvent addEvent;

    /**
     * Status associated to a ProActive Node.
     * When a Node is locked, it is no longer eligible for Tasks execution.
     * A ProActive node can be locked whatever its state is.
     */
    protected boolean isLocked;

    /** The last event */
    protected RMNodeEvent lastEvent;

    /**
     * Defines who has locked the node.
     * This field has a meaning when {@code isLocked} is {@code true} only.
     */
    protected Client lockedBy;

    /**
     * Defines the time at which the node has been locked.
     * This field has a meaning when {@code isLocked} is {@code true} only.
     */
    protected long lockTime = -1;

    /** Name of the node */
    protected final String nodeName;

    /** {@link NodeSource} Stub that handles the node */
    protected NodeSource nodeSource;

    /** Name of the NodeSource that handles the RMNode */
    protected String nodeSourceName;

    /** URL of the node, considered as its unique ID */
    private final String nodeURL;

    /** client registered the node in the resource manager */
    protected Client provider;

    /** State of the node */
    protected NodeState state;

    /** Time stamp of the latest state change */
    protected long stateChangeTime;


    public AbstractRMNode() {
        this.nodeName = null;
        this.nodeURL = null;
    }

    public AbstractRMNode(NodeSource nodeSource, String nodeName, String nodeURL, Client provider) {
        this.addEvent = null;
        this.lastEvent = null;
        this.nodeSource = nodeSource;

        if (nodeSource != null) {
            this.nodeSourceName = nodeSource.getName();
        }

        this.nodeName = nodeName;
        this.nodeURL = nodeURL;
        this.provider = provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RMNodeEvent createNodeEvent() {
        return createNodeEvent(null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RMNodeEvent createNodeEvent(RMEventType eventType, NodeState previousNodeState, String initiator) {

        RMNodeEvent rmNodeEvent = new RMNodeEvent(toNodeDescriptor(), eventType, previousNodeState, initiator);

        // The rm node always keeps track on its last event, this is needed for rm node events logic
        if (eventType != null) {
            switch (eventType) {
                case NODE_ADDED:
                    this.setAddEvent(rmNodeEvent);
                    break;
            }
            this.setLastEvent(rmNodeEvent);
        }
        return rmNodeEvent;
    }

    protected void changeState(NodeState newState) {
        this.state = newState;
        this.stateChangeTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lock(Client client) {
        this.isLocked = true;
        this.lockTime = System.currentTimeMillis();
        this.lockedBy = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlock(Client client) {
        this.isLocked = false;
        this.lockTime = -1;
        this.lockedBy = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RMNodeEvent getAddEvent() {
        return addEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RMNodeEvent getLastEvent() {
        return lastEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLockTime() {
        return lockTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client getLockedBy() {
        return lockedBy;
    }

    protected String getLockStatus() {
        String result = "Locked: " + Boolean.toString(isLocked);

        if (isLocked) {
            result += " (";

            if (lockedBy != null) {
                result += "by " + lockedBy.getName() + " ";
            }

            result += "since " + new Date(lockTime);
            result += ")";
        }

        result += System.lineSeparator();

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object rmNode) {
        return this == rmNode ||
                rmNode instanceof RMNode
                        && this.nodeURL.equals(((RMNode) rmNode).getNodeURL());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.nodeURL.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeName() {
        return nodeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeSource getNodeSource() {
        return nodeSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeSourceName() {
        return nodeSourceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeState getState() {
        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getStateChangeTime() {
        return stateChangeTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeURL() {
        return nodeURL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client getProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAddEvent(final RMNodeEvent addEvent) {
        this.addEvent = addEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastEvent(final RMNodeEvent lastEvent) {
        this.lastEvent = lastEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
        this.nodeSourceName = nodeSource.getName();
    }

    protected RMNodeDescriptor toNodeDescriptor() {

        RMNodeDescriptor rmNodeDescriptor = new RMNodeDescriptor();
        rmNodeDescriptor.setDefaultJMXUrl(getJMXUrl(JMXTransportProtocol.RMI));
        rmNodeDescriptor.setDescriptorVMName(getDescriptorVMName());
        rmNodeDescriptor.setHostName(getHostName());
        rmNodeDescriptor.setLocked(isLocked());
        rmNodeDescriptor.setLockedBy(getLockedBy() == null ? null : getLockedBy().getName());
        rmNodeDescriptor.setLockTime(getLockTime());
        rmNodeDescriptor.setNodeInfo(getNodeInfo());
        rmNodeDescriptor.setNodeSourceName(getNodeSourceName());
        rmNodeDescriptor.setNodeURL(getNodeURL());
        rmNodeDescriptor.setOwnerName(getOwner() == null ? null : getOwner().getName());
        rmNodeDescriptor.setProactiveJMXUrl(getJMXUrl(JMXTransportProtocol.RO));
        rmNodeDescriptor.setProviderName(getProvider() == null ? null : getProvider().getName());
        rmNodeDescriptor.setState(getState());
        rmNodeDescriptor.setStateChangeTime(getStateChangeTime());
        rmNodeDescriptor.setVNodeName(getVNodeName());

        return rmNodeDescriptor;
    }

    @Override
    public String toString() {
        return getNodeInfo();
    }

}
