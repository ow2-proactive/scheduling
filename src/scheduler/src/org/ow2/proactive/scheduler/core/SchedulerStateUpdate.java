/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import java.io.Serializable;

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
public interface SchedulerStateUpdate extends Serializable {

    /**
     * Invoked each time a scheduler event occurs.
     *
     * @param eventType the type of the event received.
     */
    public void schedulerStateUpdated(SchedulerEvent eventType);

    /**
     * Invoked each time a new job has been submitted to the Scheduler and validated.
     *
     * @param job the newly submitted job.
     */
    public void jobSubmitted(JobState job);

    /**
     * Invoked each time the state of a job has changed.<br>
     * In this case you can use the {@link JobState.update(JobInfo)} method to update the content of your job.
     *
     * @param notification the data composed of the type of the event and the information that have change in the job.
     */
    public void jobStateUpdated(NotificationData<JobInfo> notification);

    /**
     * Invoked each time the state of a task has changed.
     * In this case you can use the {@link JobState.update(TaskInfo)} method to update the content of your job.
     *
     * @param notification the data composed of the type of the event and the information that have change in the task.
     */
    public void taskStateUpdated(NotificationData<TaskInfo> notification);

    /**
     * Invoked each time the policy has changed.
     *
     * @param newPolicyFullName the new policy full name as a string.
     */
    public void policyChanged(String newPolicyFullName);

    /**
     * Invoked each time a something change about users.
     *
     * @param notification the data composed of the type of the event and the data linked to the change.
     */
    public void usersChanged(NotificationData<UserIdentification> notification);

}
