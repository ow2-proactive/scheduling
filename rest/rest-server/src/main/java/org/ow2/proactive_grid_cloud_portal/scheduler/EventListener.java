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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateListener.State;


public class EventListener implements SchedulerEventListener {

    private State state;

    public EventListener() {
    }

    public EventListener(State state) {
        this.state = state;
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> data) {
        state.incrementRevision();
    }

    @Override
    public void jobSubmittedEvent(JobState jobState) {
        state.incrementRevision();
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent event) {
        state.incrementRevision();
        // event doesn't provide current state, just reset stored value so that state will be re-read on demand
        state.setStatus(null);
    }

    @Override
    public void jobUpdatedFullDataEvent(JobState jobState) {
        state.incrementRevision();

    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> event) {
        state.incrementRevision();
    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> event) {
        state.incrementRevision();
    }

}
