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


/**
 *
 * @author ActiveEon Team
 * @since 16/01/17
 */
public abstract class AbstractRMNode implements RMNode, Serializable {

    /**
     * Status associated to a ProActive Node.
     * When a Node is locked, it is no longer eligible for Tasks execution.
     * A ProActive node can be locked whatever its state is.
     */
    protected boolean isLocked;

    /**
     * Defines the time at which the node has been locked.
     * This field has a meaning when {@code isLocked} is {@code true} only.
     */
    protected long lockTime = -1;

    /**
     * Defines who has locked the node.
     * This field has a meaning when {@code isLocked} is {@code true} only.
     */
    protected Client lockedBy;

    @Override
    public RMNodeEvent createNodeEvent() {
        return createNodeEvent(null, null, null);
    }


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

    /**
     * {@inheritDoc}
     */
    @Override
    public void lock(Client client) {
        this.isLocked = true;
        this.lockTime = System.currentTimeMillis();
        this.lockedBy = client;
    }

    @Override
    public void unlock(Client client) {
        this.isLocked = false;
        this.lockTime = -1;
        this.lockedBy = null;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public long getLockTime() {
        return lockTime;
    }

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
