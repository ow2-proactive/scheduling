/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;

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
public interface SchedulerEventListener extends Serializable {

    /**
     * Invoked each time a scheduler event occurs.<br />
     * Scheduler events are stopped,started, paused, frozen, ...
     *
     * @param eventType the type of the event received.
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType);

    /**
     * Invoked each time a new job has been submitted to the Scheduler and validated.
     *
     * @param job the newly submitted job.
     */
    public void jobSubmittedEvent(JobState job);

    /**
     * Invoked each time the state of a job has changed.<br>
     * If you want to maintain an up to date list of jobs, 
     * just use the {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.job.JobInfo)} 
     * method to update the content of your job.
     *
     * @param notification the data composed of the type of the event and the information that have change in the job.
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification);

    /**
     * Invoked each time the state of a task has changed.<br>
     * In this case you can use the 
     * {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.task.TaskInfo)} 
     * method to update the content of the designated task inside your job.
     *
     * @param notification the data composed of the type of the event and the information that have change in the task.
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification);

    /**
     * Invoked each time something change about users.
     *
     * @param notification the data composed of the type of the event and the data linked to the change.
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification);

}
