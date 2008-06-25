/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.ow2.proactive.scheduler.common.scheduler;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskEvent;


/**
 * Class providing events that the scheduler is able to send using the described listener.
 *
 * @author The ProActive Team
 * @version 3.9, Jun 12, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public interface SchedulerEventListener<E extends Job> extends Serializable {

    /**
     * Invoked when the scheduler has just been started.
     */
    public void schedulerStartedEvent();

    /**
     * Invoked when the scheduler has just been stopped.
     */
    public void schedulerStoppedEvent();

    /**
     * Invoked when the scheduler has just been paused.
     *
     */
    public void schedulerPausedEvent();

    /**
     * Invoked when the scheduler has received a freeze signal.
     */
    public void schedulerFrozenEvent();

    /**
     * Invoked when the scheduler has just been resumed.
     */
    public void schedulerResumedEvent();

    /**
     * Invoked when the scheduler shutdown sequence is initialized.
     */
    public void schedulerShuttingDownEvent();

    /**
     * Invoked when the scheduler has just been shutdown.
     */
    public void schedulerShutDownEvent();

    /**
     * Invoked when the scheduler has just been killed.<br>
     * 
     * Scheduler is not reachable anymore.
     */
    public void schedulerKilledEvent();

    /**
     * Invoked when a job has been killed on the scheduler.
     *
     * @param jobId the job to killed.
     */
    public void jobKilledEvent(JobId jobId);

    /**
     * Invoked when a job has been paused on the scheduler.
     *
     * @param event the informations on the paused job.
     */
    public void jobPausedEvent(JobEvent event);

    /**
     * Invoked when a job has been resumed on the scheduler.
     *
     * @param event the informations on the resumed job.
     */
    public void jobResumedEvent(JobEvent event);

    /**
     * Invoked when the scheduler has received a new job to schedule.
     *
     * @param job the new job to schedule.
     */
    public void jobSubmittedEvent(E job);

    /**
     * Invoked when the scheduling of a job has just started.<br>
     * The description of the job is contained in the jobEvent given.<br>
     * Use Job.update(JobEvent) to update your job.
     *
     * @param event the event describing the job concerned.
     */
    public void jobPendingToRunningEvent(JobEvent event);

    /**
     * Invoked when the scheduling of a job has just been terminated.<br>
     * The description of the job is contained in the jobEvent given.<br>
     * Use {@link JOB}.update(JobEvent) to update your job.
     *
     * @param event the event describing the job concerned.
     */
    public void jobRunningToFinishedEvent(JobEvent event);

    /**
     * Invoked when the scheduler has removed a job due to result reclamation.<br>
     * The description of the job is contained in the jobEvent given.<br>
     * Use {@link JOB}.update(JobEvent) to update your job.
     *
     * @param event the event describing the job concerned.
     */
    public void jobRemoveFinishedEvent(JobEvent event);

    /**
     * Invoked when the scheduling of a task has just started.<br>
     * The description of the task is contained in the TaskEvent given.<br>
     * Use {@link JOB}.update(TaskEvent) to update your job.
     *
     * @param event the event describing the task concerned.
     */
    public void taskPendingToRunningEvent(TaskEvent event);

    /**
     * Invoked when the scheduling of a task has just finished.<br>
     * The description of the task is contained in the TaskEvent given.<br>
     * Use {@link JOB}.update(TaskEvent) to update your job.
     *
     * @param event the event describing the task concerned.
     */
    public void taskRunningToFinishedEvent(TaskEvent event);

    /**
     * Invoked when a task had an error (error code or exception).
     * The task will be restart after a dynamic amount of time.
     * This event specified that a task is waiting for restart.
     * 
     * @param event the event describing the task concerned.
     */
    public void taskWaitingForRestart(TaskEvent event);

    /**
     * Invoked when the scheduler has changed the priority of a job.<br>
     * The description of the job is contained in the jobEvent given.<br>
     * Use {@link JOB}.update(JobEvent) to update your job.
     *
     * @param event the event describing the job concerned.
     */
    public void jobChangePriorityEvent(JobEvent event);

    /**
     * Invoked if the Resource Manager has failed.<br>
     * Use the {@link AdminSchedulerInterface.linkResourceManager(String rmURL)} to reconnect a new Resource Manager.
     */
    public void schedulerRMDownEvent();

    /**
     * Invoked when the Resource Manager has been reconnect to the scheduler.
     */
    public void schedulerRMUpEvent();

    /**
     * Invoked when a new user is connected or when a user submit a new job.
     */
    public void usersUpdate(UserIdentification userIdentification);
}
