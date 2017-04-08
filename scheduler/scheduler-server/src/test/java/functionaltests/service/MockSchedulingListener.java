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
package functionaltests.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;


public class MockSchedulingListener implements SchedulerStateUpdate {

    private List<SchedulerEvent> events = new ArrayList<>();

    @Override
    public void schedulerStateUpdated(SchedulerEvent eventType) {
        System.out.println("Scheduler state updated " + eventType);
    }

    @Override
    public void jobSubmitted(JobState jobState) {
        System.out.println("Submitted " + jobState.getName() + " " + jobState.getJobInfo().getJobId());
        events.add(SchedulerEvent.JOB_SUBMITTED);
    }

    @Override
    public void jobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        System.out.println("Job state updated " + notification.getData().getJobId() + " " +
                           notification.getEventType());
        events.add(notification.getEventType());
    }

    @Override
    public void taskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        System.out.println("Task state updated " + notification.getData().getName() + " " +
                           notification.getEventType());
        events.add(notification.getEventType());
    }

    void assertEvents(SchedulerEvent... expectedEvents) {
        List<SchedulerEvent> expected = Arrays.asList(expectedEvents);
        Assert.assertEquals(expected, events);
        events.clear();
    }

    @Override
    public void usersUpdated(NotificationData<UserIdentification> notification) {
    }

    @Override
    public void jobUpdatedFullData(JobState jobState) {
        System.out.println("Updated " + jobState.getName() + " " + jobState.getJobInfo().getJobId());
        events.add(SchedulerEvent.JOB_UPDATED);
    }

}
