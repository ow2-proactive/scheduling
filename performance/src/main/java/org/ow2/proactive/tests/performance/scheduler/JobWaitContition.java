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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.tests.performance.scheduler;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.tests.performance.utils.WaitFailedException;


public class JobWaitContition extends SchedulerWaitCondition {

    private final String jobName;

    private final JobStatus expectedStatus;

    private JobInfo completedJobInfo;

    public JobWaitContition(String jobName, JobStatus expectedStatus) {
        this.jobName = jobName;
        this.expectedStatus = expectedStatus;
    }

    public JobWaitContition(String jobName) {
        this(jobName, JobStatus.FINISHED);
    }

    @Override
    public boolean stopWait() throws WaitFailedException {
        if (completedJobInfo == null) {
            return false;
        }
        if (completedJobInfo.getStatus().equals(expectedStatus)) {
            return true;
        } else {
            throw new WaitFailedException("Unexpected job status: " + completedJobInfo.getStatus() +
                ", expected status is " + expectedStatus);
        }
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
            this.completedJobInfo = notification.getData();
            this.notifyAll();
        }
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        if (!jobName.equals(notification.getData().getJobId().getReadableName())) {
            return;
        }
        addEventLog("Task state for task '" + jobName + "/" +
            notification.getData().getTaskId().getReadableName() + "' updated " + notification.getEventType());
    }
}
