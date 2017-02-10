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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class SchedulerUserData {
    private String hostName;

    private String username;

    private long connectionTime;

    private long lastSubmitTime;

    private int submitNumber;

    public SchedulerUserData() {
    }

    public String getHostName() {
        return hostName;
    }

    public String getUsername() {
        return username;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public long getLastSubmitTime() {
        return lastSubmitTime;
    }

    public int getSubmitNumber() {
        return submitNumber;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setConnectionTime(long connectionTime) {
        this.connectionTime = connectionTime;
    }

    public void setLastSubmitTime(long lastSubmitTime) {
        this.lastSubmitTime = lastSubmitTime;
    }

    public void setSubmitNumber(int submitNumber) {
        this.submitNumber = submitNumber;
    }

    @Override
    public String toString() {
        return "SchedulerUserData{" + "hostName='" + hostName + '\'' + ", username='" + username + '\'' +
               ", connectionTime=" + connectionTime + ", lastSubmitTime=" + lastSubmitTime + ", submitNumber=" +
               submitNumber + '}';
    }
}
