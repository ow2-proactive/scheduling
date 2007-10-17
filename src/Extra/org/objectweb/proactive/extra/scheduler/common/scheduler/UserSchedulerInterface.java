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

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;


/**
 * Scheduler user interface.
 * This interface provides methods to managed the user task and jobs on the scheduler.
 * A user will only be able to managed his jobs and tasks, and also see the entire scheduling process.
 *
 * @author ProActive Team
 * @version 1.0, Jun 7, 2007
 * @since ProActive 3.2
 */
public interface UserSchedulerInterface extends Serializable {

    /**
     * Submit a new job to the scheduler.
     * A user can only managed their jobs.
     * It will execute the tasks of the jobs as soon as resources are available.
     * The job will be considered as finished once the marked 'final' task has finished,
     * or when every tasks have finished.
     * Thus, user could get the job result according to the final task result.
     *
     * @param job the new job to submit.
     * @return the generated new job ID.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public JobId submit(Job job) throws SchedulerException;

    /**
     * Get the result for the given jobId.
     * A user can only get HIS result back.
     *
     * @param jobId the job on which the result will be send
     * @return a job Result containing information about the result.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public JobResult getResult(JobId jobId) throws SchedulerException;

    /**
     * Listen for the tasks user log.
     * A user can only listen to HIS jobs.
     *
     * @param jobId the id of the job to listen to.
     * @param hostname the hostname where to send the log.
     * @param port the port number on which the log will be sent.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public void listenLog(JobId jobId, String hostname, int port)
        throws SchedulerException;

    /**
     * Add a scheduler event Listener. this listener provides method to notice of
     * new coming job, started task, finished task, running job, finished job.
     *
     * @param sel a SchedulerEventListener on which the scheduler will talk.
     * @return the scheduler current state containing the different lists of jobs.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public SchedulerInitialState<?extends Job> addSchedulerEventListener(
        SchedulerEventListener<?extends Job> sel) throws SchedulerException;

    /**
     * Return the scheduler statistics.
     * It will be possible to get an HashMap of all properties set in the stats class.
     *
     * @return the scheduler statistics.
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public Stats getStats() throws SchedulerException;

    /**
     * Disconnect properly the user from the scheduler.
     *
     * @throws SchedulerException if an exception occurs in the scheduler (depends on your right).
     */
    public void disconnect() throws SchedulerException;

    /**
     * kill the job represented by jobId.
     * This method will kill every running tasks of this job, and remove it from the scheduler.
     * The job won't be terminated, it won't have result.
     *
     * @param jobId the job to kill.
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException;

    /**
     * Pause the job represented by jobId.
     * This method will finish every running tasks of this job, and then pause the job.
     * The job will have to be resumed in order to finish.
     *
     * @param jobId the job to pause.
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException;

    /**
     * Resume the job represented by jobId.
     * This method will restart every non-finished tasks of this job.
     *
     * @param jobId the job to resume.
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException;

    /**
     * Change the priority of the job represented by jobId.
     *
     * @param jobId the job on whitch to change the priority.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public void changePriority(JobId jobId, JobPriority priority)
        throws SchedulerException;
}
