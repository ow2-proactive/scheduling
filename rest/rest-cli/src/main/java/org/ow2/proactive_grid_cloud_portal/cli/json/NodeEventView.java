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
package org.ow2.proactive_grid_cloud_portal.cli.json;

public class NodeEventView {

    private String hostName;

    private String nodeSource;

    private String nodeState;

    private String nodeInfo;

    private String timeStamp;

    private String timeStampFormatted;

    private String nodeUrl;

    private String nodeProvider;

    private String nodeOwner;

    private String defaultJMXUrl;

    private String proactiveJMXUrl;

    private String locked;

    private String lockTime;

    private String nodeLocker;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    public String getNodeState() {
        return nodeState;
    }

    public void setNodeState(String nodeState) {
        this.nodeState = nodeState;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public String getTimeStampFormatted() {
        return timeStampFormatted;
    }

    public void setTimeStampFormatted(String timeStampFormatted) {
        this.timeStampFormatted = timeStampFormatted;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getNodeProvider() {
        return nodeProvider;
    }

    public void setNodeProvider(String nodeProvider) {
        this.nodeProvider = nodeProvider;
    }

    public String getNodeOwner() {
        return nodeOwner;
    }

    public void setNodeOwner(String nodeOwner) {
        this.nodeOwner = nodeOwner;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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

    public String isLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getLockTime() {
        return lockTime;
    }

    public void setLockTime(String lockTime) {
        this.lockTime = lockTime;
    }

    public String getNodeLocker() {
        return nodeLocker;
    }

    public void setNodeLocker(String nodeLocker) {
        this.nodeLocker = nodeLocker;
    }

}
