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
package org.objectweb.proactive.extra.scheduler.common.scheduler;

import java.io.Serializable;

import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;


/**
 * Class providing events that the scheduler is able to send using the described listener.
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jun 12, 2007
 * @since ProActive 3.2
 */
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
     * @param event the scheduler informations about the status of every tasks.
     *                 use event.update method to update your job.
     */
    public void schedulerPausedEvent();

    /**
     * Invoked when the scheduler has received a paused immediate signal.
     */
    public void schedulerImmediatePausedEvent();

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
     *
     * @param job the new scheduled job.
     */
    public void schedulerShutDownEvent();

    /**
     * Invoked when the scheduler has just been killed.
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
     * Invoked when the scheduling of a job has just started.
     * The description of the job is contained in the jobEvent given.
     * Use Job.update(JobEvent) to update your job.
     *
     * @param event the event describing the job concerned.
     */
    public void jobPendingToRunningEvent(JobEvent event);

    /**
     * Invoked when the scheduling of a job has just been terminated.
     * The description of the job is contained in the jobEvent given.
     * Use Job.update(JobEvent) to update your job.
     *
     * @param event the event describing the job concerned.
     */
    public void jobRunningToFinishedEvent(JobEvent event);

    /**
     * Invoked when the scheduler has removed a job due to result reclamation.
     * The description of the job is contained in the jobEvent given.
     * Use Job.update(JobEvent) to update your job.
     *
     * @param event the event describing the job concerned.
     */
    public void jobRemoveFinishedEvent(JobEvent event);

    /**
     * Invoked when the scheduling of a task has just started.
     * The description of the task is contained in the TaskEvent given.
     * Use Job.update(TaskEvent) to update your job.
     *
     * @param event the event describing the task concerned.
     */
    public void taskPendingToRunningEvent(TaskEvent event);

    /**
     * Invoked when the scheduling of a task has just finished.
     * The description of the task is contained in the TaskEvent given.
     * Use Job.update(TaskEvent) to update your job.
     *
     * @param event the event describing the task concerned.
     */
    public void taskRunningToFinishedEvent(TaskEvent event);

    /**
     * Invoked when the scheduler has changed the priority of a job.
     * The description of the job is contained in the jobEvent given.
     * Use Job.update(JobEvent) to update your job.
     *
     * @param event the event describing the job concerned.
     */
    public void jobChangePriorityEvent(JobEvent event);
}
