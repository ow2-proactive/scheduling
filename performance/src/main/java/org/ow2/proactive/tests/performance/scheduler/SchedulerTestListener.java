/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.scheduler;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


public class SchedulerTestListener implements SchedulerEventListener {

    private SchedulerEventListener eventsMonitor;

    public SchedulerTestListener() {
    }

    public SchedulerTestListener(SchedulerEventListener eventsMonitor) {
        this.eventsMonitor = eventsMonitor;
    }

    public static SchedulerTestListener createListener(SchedulerEventListener eventsMonitor) throws Exception {
        SchedulerTestListener listener = new SchedulerTestListener(eventsMonitor);
        listener = PAActiveObject.turnActive(listener);
        return listener;
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        eventsMonitor.schedulerStateUpdatedEvent(eventType);
    }

    @Override
    public void jobSubmittedEvent(JobState job) {
        eventsMonitor.jobSubmittedEvent(job);
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        eventsMonitor.jobStateUpdatedEvent(notification);
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        eventsMonitor.taskStateUpdatedEvent(notification);
    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        eventsMonitor.usersUpdatedEvent(notification);
    }

}
