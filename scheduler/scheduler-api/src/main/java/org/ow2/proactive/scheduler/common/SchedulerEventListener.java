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
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * Class providing events that the scheduler is able to send using the described listener.
 * Each event may represent a group of similar events.<br>
 * The difference between events is specified by the SchedulerEvent contains in each events.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface SchedulerEventListener {

    /**
     * Invoked each time a scheduler event occurs.<br>
     * Scheduler events are stopped,started, paused, frozen, etc.
     *
     * @param eventType the type of the event received.
     */
    void schedulerStateUpdatedEvent(SchedulerEvent eventType);

    /**
     * Invoked each time a new job has been submitted to the Scheduler and validated.
     *
     * @param job the newly submitted job.
     */
    void jobSubmittedEvent(JobState job);

    /**
     * Invoked each time the state of a job has changed.<br>
     * If you want to maintain an up to date list of jobs, 
     * just use the {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.job.JobInfo)} 
     * method to update the content of your job.
     *
     * @param notification the data composed of the type of the event and the information that have change in the job.
     */
    void jobStateUpdatedEvent(NotificationData<JobInfo> notification);

    /**
     * Invoked each time the state of a job has changed and each time a scheduler event occurs.<br>
     *
     * @param job the updated job.
     */
    void jobUpdatedFullDataEvent(JobState job);

    /**
     * Invoked each time the state of a task has changed.<br>
     * In this case you can use the 
     * {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.task.TaskInfo)} 
     * method to update the content of the designated task inside your job.
     *
     * @param notification the data composed of the type of the event and the information that have change in the task.
     */
    void taskStateUpdatedEvent(NotificationData<TaskInfo> notification);

    /**
     * Invoked each time something change about users.
     *
     * @param notification the data composed of the type of the event and the data linked to the change.
     */
    void usersUpdatedEvent(NotificationData<UserIdentification> notification);

}
