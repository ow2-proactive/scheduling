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

import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.common.event.RMNodeDescriptor;


/**
 *
 * @author ActiveEon Team
 * @since 16/01/17
 */
public abstract class AbstractRMNode implements RMNode, Serializable {

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

}
