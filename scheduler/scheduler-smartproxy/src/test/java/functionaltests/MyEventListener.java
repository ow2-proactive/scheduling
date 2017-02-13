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
package functionaltests;

import java.io.Serializable;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.smartproxy.common.SchedulerEventListenerExtended;

import functionaltests.monitor.EventMonitor;


public class MyEventListener implements SchedulerEventListenerExtended, Serializable {

    boolean jobFinished = false;

    boolean pullDataFinished = false;

    JobId jobId;

    int count = 0;

    boolean synchronous = false;

    EventMonitor monitor;

    /**
     *
     * @param id
     *            of the job this Listener is intrested in
     */
    public boolean setJobID(JobId id) {
        this.jobId = id;
        return true;
    }

    public MyEventListener() {

    }

    public boolean reset() {
        jobFinished = false;
        count = 0;
        return true;
    }

    public synchronized boolean setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
        return true;
    }

    /**
     * to be called directly on the java object (not on the remote active
     * reference) the monitor needs to be copied by reference
     */
    public boolean setMonitor(EventMonitor monitor) {
        this.monitor = monitor;
        return true;
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        System.out.println("MyEventListener.schedulerStateUpdatedEvent() ");

    }

    @Override
    public void jobSubmittedEvent(JobState job) {
        System.out.println("MyEventListener.jobSubmittedEvent()");

    }

    @Override
    public void jobUpdatedFullDataEvent(JobState job) {
        System.out.println("MyEventListener.jobUpdatedFullDataEvent()");

    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        System.out.println("MyEventListener.jobStateUpdatedEvent() " + notification);

        JobId id = notification.getData().getJobId();
        if (!id.equals(jobId))
            return;

        SchedulerEvent event = notification.getEventType();
        if (event == SchedulerEvent.JOB_RUNNING_TO_FINISHED) {
            jobFinished = true;
            if (synchronous) {
                synchronized (monitor) {
                    System.out.println("[MyEventListener] job finished event occured for " + jobId);
                    monitor.setEventOccured();
                    monitor.notifyAll();
                }
            }
        }
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        System.out.println("MyEventListener.taskStateUpdatedEvent()");
    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        System.out.println("MyEventListener.usersUpdatedEvent()");
    }

    @Override
    public void pullDataFinished(String jobId, String taskName, String localFolderPath) {

        if (!jobId.equals(this.jobId.toString()))
            return;

        synchronized (monitor) {
            count++;
            System.out.println("[MyEventListener] pull data finished event occured for " + taskName);
            if (count == TestSmartProxy.NB_TASKS) {
                count = 0;
                monitor.setEventOccured();
                monitor.notifyAll();
            }
        }
    }

    @Override
    public void pullDataFailed(String jobId, String taskName, String remoteFolder_URL, Throwable t) {
        t.printStackTrace();
        count = 0;
        Assert.assertTrue("[MyEventListener] Pull data operation failed: " + t.getMessage(), false);
    }

    /**
     *
     * @return true if an event indicating the job is finished has been received
     *         by this listener
     */
    public boolean getJobFinished() {
        return jobFinished;
    }

}
