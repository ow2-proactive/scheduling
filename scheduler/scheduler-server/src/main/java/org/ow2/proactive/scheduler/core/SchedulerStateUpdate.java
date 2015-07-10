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
package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * Interface providing update that the scheduler is able to send to the front-end.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public interface SchedulerStateUpdate {

    /**
     * Invoked each time a scheduler event occurs.
     *
     * @param eventType the type of the event received.
     */
    void schedulerStateUpdated(SchedulerEvent eventType);

    /**
     * Invoked each time a new job has been submitted to the Scheduler and validated.
     *
     * @param job the newly submitted job.
     */
    void jobSubmitted(JobState job);

    /**
     * Invoked each time the state of a job has changed.<br>
     * In this case you can use the {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.job.JobInfo)} method to update the content of your job.
     *
     * @param owner the owner of this job
     * @param notification the data composed of the type of the event and the information that have change in the job.
     */
    void jobStateUpdated(String owner, NotificationData<JobInfo> notification);

    /**
     * Invoked each time the state of a task has changed.
     * In this case you can use the {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.task.TaskInfo)} method to update the content of your job.
     *
     * @param owner the owner of this task
     * @param notification the data composed of the type of the event and the information that have change in the task.
     */
    void taskStateUpdated(String owner, NotificationData<TaskInfo> notification);

    /**
     * Invoked each time something change about users.
     *
     * @param notification the data composed of the type of the event and the data linked to the change.
     */
    void usersUpdated(NotificationData<UserIdentification> notification);

}
