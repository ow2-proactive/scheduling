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
package functionaltests.monitor;

import static functionaltests.utils.SchedulerTHelper.log;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * Scheduler event receiver for functional tests
 * Receives all scheduler events and
 * forward them a SchedulerMonitorsHandler object to handle them.
 *
 * @author ProActive team
 *
 */
public class MonitorEventReceiver implements SchedulerEventListener {

    private SchedulerMonitorsHandler monitorsHandler;

    /**
     * ProActive Empty constructor
     */
    public MonitorEventReceiver() {
    }

    /**
     * @param monitor SchedulerMonitorsHandler object which is notified
     * of Schedulers events.
     */
    public MonitorEventReceiver(SchedulerMonitorsHandler monitor) {
        this.monitorsHandler = monitor;
    }

    //---------------------------------------------------------------//
    //Methods inherited form SchedulerEventListener
    //---------------------------------------------------------------//

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        log("SchedulerEvent: " + eventType);
        switch (eventType) {
            case STARTED:
            case STOPPED:
            case KILLED:
            case FROZEN:
            case PAUSED:
            case RM_DOWN:
            case RM_UP:
            case RESUMED:
            case SHUTDOWN:
            case SHUTTING_DOWN:
            case POLICY_CHANGED:
                monitorsHandler.handleSchedulerStateEvent(eventType);
                break;
        }

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        log("NotificationData: " + notification);
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_RUNNING:
            case JOB_PENDING_TO_FINISHED:
            case JOB_RUNNING_TO_FINISHED:
            case JOB_REMOVE_FINISHED:
            case JOB_CHANGE_PRIORITY:
            case JOB_PAUSED:
            case JOB_RESUMED:
            case JOB_RESTARTED_FROM_ERROR:
            case TASK_REPLICATED:
            case TASK_SKIPPED:
                monitorsHandler.handleJobEvent(notification.getEventType(), notification.getData());
                break;
        }

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        log("NotificationData: " + notification);
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
            case TASK_RUNNING_TO_FINISHED:
            case TASK_WAITING_FOR_RESTART:
                monitorsHandler.handleTaskEvent(notification.getEventType(), notification.getData());
                break;
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmittedEvent(JobState jState) {
        log("JobState: " + jState);
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_SUBMITTED, jState);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        log("NotificationData: " + notification);
        monitorsHandler.handleSchedulerStateEvent(SchedulerEvent.USERS_UPDATE);
    }

    @Override
    public void jobUpdatedFullDataEvent(JobState job) {
        log("JobState: " + job);
        monitorsHandler.handleJobEvent(SchedulerEvent.JOB_UPDATED, job);
    }

}
