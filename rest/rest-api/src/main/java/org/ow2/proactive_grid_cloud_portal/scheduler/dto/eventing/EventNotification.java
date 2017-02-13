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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing;

public class EventNotification {

    public enum Action {
        NONE,
        SCHEDULER_STATE_UPDATED,
        JOB_SUBMITTED,
        JOB_STATE_UPDATED,
        JOB_FULL_DATA_UPDATED,
        TASK_STATE_UPDATED,
        USERS_UPDATED
    };

    private Action action;

    private String schedulerEvent;

    private Object data;

    public EventNotification() {
    }

    public EventNotification(Action action, String eventyType, Object data) {
        this.action = action;
        this.schedulerEvent = eventyType;
        this.data = data;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public String getSchedulerEvent() {
        return schedulerEvent;
    }

    public void setSchedulerEvent(String schedulerEvent) {
        this.schedulerEvent = schedulerEvent;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
