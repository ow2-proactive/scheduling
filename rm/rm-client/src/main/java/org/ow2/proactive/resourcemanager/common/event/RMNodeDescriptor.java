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

import org.ow2.proactive.resourcemanager.common.NodeState;


public final class RMNodeDescriptor {

    private String nodeURL;

    private String nodeSourceName;

    private String VNodeName;

    private String hostName;

    private NodeState state;

    private String defaultJMXUrl;

    private String proactiveJMXUrl;

    private String descriptorVMName;

    private long stateChangeTime;

    private String providerName;

    private String ownerName;

    private String nodeInfo;

    private boolean isLocked;

    private long lockTime;

    private String lockedBy;

    public String getNodeURL() {
        return nodeURL;
    }

    public void setNodeURL(String nodeURL) {
        this.nodeURL = nodeURL;
    }

    public String getNodeSourceName() {
        return nodeSourceName;
    }

    public void setNodeSourceName(String nodeSourceName) {
        this.nodeSourceName = nodeSourceName;
    }

    public String getVNodeName() {
        return VNodeName;
    }

    public void setVNodeName(String VNodeName) {
        this.VNodeName = VNodeName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public String getDefaultJMXUrl() {
        return defaultJMXUrl;
    }

    public void setDefaultJMXUrl(String defaultJMXUrl) {
        this.defaultJMXUrl = defaultJMXUrl;
    }

    public String getProactiveJMXUrl() {
        return proactiveJMXUrl;
    }

    public void setProactiveJMXUrl(String proactiveJMXUrl) {
        this.proactiveJMXUrl = proactiveJMXUrl;
    }

    public String getDescriptorVMName() {
        return descriptorVMName;
    }

    public void setDescriptorVMName(String descriptorVMName) {
        this.descriptorVMName = descriptorVMName;
    }

    public long getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(long stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

}
