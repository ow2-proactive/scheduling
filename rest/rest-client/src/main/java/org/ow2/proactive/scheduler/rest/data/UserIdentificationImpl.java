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
package org.ow2.proactive.scheduler.rest.data;

import java.util.Set;
import java.util.TimerTask;

import org.ow2.proactive.scheduler.common.job.UserIdentification;


public class UserIdentificationImpl extends UserIdentification {

    private String username;

    private Set<String> groups;

    private int submitNumber;

    private String hostName;

    private long connectionTime;

    private long lastSubmitTime = -1;

    private boolean myEventsOnly;

    public UserIdentificationImpl() {

    }

    public UserIdentificationImpl(String username, Set<String> groups, int submitNumber, String hostName,
            long connectionTime, long lastSubmitTime, boolean myEventsOnly) {
        this.username = username;
        this.groups = groups;
        this.submitNumber = submitNumber;
        this.hostName = hostName;
        this.connectionTime = connectionTime;
        this.lastSubmitTime = lastSubmitTime;
        this.myEventsOnly = myEventsOnly;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Set<String> getGroups() {
        return groups;
    }

    @Override
    public int getSubmitNumber() {
        return submitNumber;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public long getConnectionTime() {
        return connectionTime;
    }

    @Override
    public long getLastSubmitTime() {
        return lastSubmitTime;
    }

    @Override
    public boolean isMyEventsOnly() {
        return myEventsOnly;
    }

    @Override
    public TimerTask getSession() {
        return null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public void setSubmitNumber(int submitNumber) {
        this.submitNumber = submitNumber;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setConnectionTime(long connectionTime) {
        this.connectionTime = connectionTime;
    }

    public void setLastSubmitTime(long lastSubmitTime) {
        this.lastSubmitTime = lastSubmitTime;
    }

    public void setMyEventsOnly(boolean myEventsOnly) {
        this.myEventsOnly = myEventsOnly;
    }

}
