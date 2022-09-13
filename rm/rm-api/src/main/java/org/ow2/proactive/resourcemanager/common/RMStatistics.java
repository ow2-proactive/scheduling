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
package org.ow2.proactive.resourcemanager.common;

import java.io.Serializable;


public class RMStatistics implements Serializable {
    private int freeNodesCount;

    private int busyNodesCount;

    private int deployingNodesCount;

    private int configNodesCount;

    private int downNodesCount;

    private int lostNodesCount;

    private int toBeRemovedNodesCount;

    private int availableNodesCount;

    public int getFreeNodesCount() {
        return freeNodesCount;
    }

    public void setFreeNodesCount(int freeNodesCount) {
        this.freeNodesCount = freeNodesCount;
    }

    public int getBusyNodesCount() {
        return busyNodesCount;
    }

    public void setBusyNodesCount(int busyNodesCount) {
        this.busyNodesCount = busyNodesCount;
    }

    public int getDeployingNodesCount() {
        return deployingNodesCount;
    }

    public void setDeployingNodesCount(int deployingNodesCount) {
        this.deployingNodesCount = deployingNodesCount;
    }

    public int getConfigNodesCount() {
        return configNodesCount;
    }

    public void setConfigNodesCount(int configNodesCount) {
        this.configNodesCount = configNodesCount;
    }

    public int getDownNodesCount() {
        return downNodesCount;
    }

    public void setDownNodesCount(int downNodesCount) {
        this.downNodesCount = downNodesCount;
    }

    public int getLostNodesCount() {
        return lostNodesCount;
    }

    public void setLostNodesCount(int lostNodesCount) {
        this.lostNodesCount = lostNodesCount;
    }

    public int getToBeRemovedNodesCount() {
        return toBeRemovedNodesCount;
    }

    public void setToBeRemovedNodesCount(int toBeRemovedNodesCount) {
        this.toBeRemovedNodesCount = toBeRemovedNodesCount;
    }

    public int getAvailableNodesCount() {
        availableNodesCount = freeNodesCount + busyNodesCount + deployingNodesCount + configNodesCount +
                              downNodesCount + lostNodesCount + toBeRemovedNodesCount;
        return availableNodesCount;
    }

    public void addFreeNode() {
        this.freeNodesCount++;
    }

    public void addBusyNode() {
        this.busyNodesCount++;
    }

    public void addDeployingNode() {
        this.deployingNodesCount++;
    }

    public void addConfigNode() {
        this.configNodesCount++;
    }

    public void addDownNode() {
        this.downNodesCount++;
    }

    public void addLostNode() {
        this.lostNodesCount++;
    }

    public void addToBeRemovedNodesCount() {
        this.toBeRemovedNodesCount++;
    }
}
