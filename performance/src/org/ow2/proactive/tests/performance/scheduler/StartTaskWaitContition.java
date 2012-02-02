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

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.tests.performance.utils.WaitFailedException;


public class StartTaskWaitContition extends SchedulerWaitCondition {

    private final String jobName;

    private final String taskName;

    private boolean jobCompleted;

    private boolean taskStarted;

    public StartTaskWaitContition(String jobName, String taskName) {
        this.jobName = jobName;
        this.taskName = taskName;
    }

    @Override
    public boolean stopWait() throws WaitFailedException {
        if (taskStarted) {
            return true;
        }
        if (jobCompleted) {
            throw new WaitFailedException("Unexpected job finished before task was run");
        }
        return false;
    }

    @Override
    public void jobSubmittedEvent(JobState job) {
        if (!jobName.equals(job.getId().getReadableName())) {
            return;
        }
        addEventLog("Job '" + jobName + "' submitted");
    }

    @Override
    public synchronized void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        if (!jobName.equals(notification.getData().getJobId().getReadableName())) {
            return;
        }
        addEventLog("Job '" + jobName + "' state updated " + notification.getEventType());
        if (SchedulerEventsMonitor.completedJobStatus.contains(notification.getData().getStatus())) {
            this.jobCompleted = true;
            this.notifyAll();
        }
    }

    @Override
    public synchronized void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        if (!jobName.equals(notification.getData().getJobId().getReadableName())) {
            return;
        }
        addEventLog("Task state for job '" + jobName + "' updated " + notification.getEventType());
        if (taskName.equals(notification.getData().getTaskId().getReadableName())) {
            if (notification.getEventType().equals(SchedulerEvent.TASK_PENDING_TO_RUNNING)) {
                this.taskStarted = true;
                this.notifyAll();
            }
        }
    }
}
