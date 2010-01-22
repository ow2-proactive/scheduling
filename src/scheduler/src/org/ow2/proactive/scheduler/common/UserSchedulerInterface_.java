/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.exception.AccessRightException;
import org.ow2.proactive.scheduler.common.exception.AuthenticationException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UnknowJobException;
import org.ow2.proactive.scheduler.common.exception.UnknowTaskResultException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;


/**
 * UserSchedulerInterface_ represents the methods that have to access the Core to be performed.
 * This interface only exists to make the refactoring easy.
 * It contains all the method that HAVE to access the core. In fact, the core implements it.
 * It provides methods for someone connected to the scheduler as user.<br>
 * This interface provides methods to managed the user task and jobs on the scheduler.
 *
 * @author The ProActive Team 
 * @since ProActive Scheduling 0.9
 *
 * $Id$
 */
@PublicAPI
public interface UserSchedulerInterface_ extends Serializable {

    /**
     * Get the result for the given jobId.
     * A user can only get HIS result back except if he is admin.<br>
     * If the job does not exist, a schedulerException is sent with the proper message.<br>
     * So, if you have the right to get the job result represented by the given jobId and if the job exists,
     * so you will receive the result. In any other cases a schedulerException will be thrown.
     *
     * @param jobId the job on which the result will be send
     * @return a job Result containing information about the result.
     * 		If the job result is not yet available (job not finished), null is returned.
     * @throws SchedulerException if an exception occurs while serving the request.
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException;

    /**
     * Get the result for the given task name in the given jobId.
     * A user can only get HIS result back.<br>
     * If the job does not exist, a schedulerException is sent with the proper message.<br>
     * So, if you have the right to get the task result that is in the job represented by the given jobId and if the job and task name exist,
     * so you will receive the result. In any other cases a schedulerException will be thrown.<br>
     *
     * @param jobId the job in which the task result is.
     * @param taskName the name of the task in which the result is.
     * @return a job Result containing information about the result.
     * 		If the task result is not yet available, null is returned.
     * @throws SchedulerException if an exception occurs while serving the request.
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws UnknowTaskResultException if this task result does not exist or is unknown.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws SchedulerException;

    /**
     * Remove the job from the scheduler.
     *
     * @param jobId the job to be removed.
     * @throws SchedulerException if an exception occurs while serving the request.
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public void remove(JobId jobId) throws SchedulerException;

    /**
     * Listen for the tasks user log.<br>
     * A user can only listen to HIS jobs.
     *
     * @param jobId the id of the job to listen to.
     * @param appenderProvider a provider for an appender that must be connected on a log server on the caller side (see {@link LogForwardingService})
     * @throws SchedulerException if an exception occurs while serving the request, specially if the appender cannot be created.
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public void listenLog(JobId jobId, AppenderProvider appenderProvider) throws SchedulerException;

    /**
     * Kill the job represented by jobId.<br>
     * This method will kill every running tasks of this job, and remove it from the scheduler.<br>
     * The job won't be terminated, it won't have result.
     *
     * @param jobId the job to kill.
     * @return true if success, false if not.
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException;

    /**
     * Pause the job represented by jobId.<br>
     * This method will finish every running tasks of this job, and then pause the job.<br>
     * The job will have to be resumed in order to finish.
     *
     * @param jobId the job to pause.
     * @return true if success, false if not.
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException;

    /**
     * Resume the job represented by jobId.<br>
     * This method will restart every non-finished tasks of this job.
     *
     * @param jobId the job to resume.
     * @return true if success, false if not.
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException;

    /**
     * Change the priority of the job represented by jobId.<br>
     * Only administrator can change the priority to HIGH, HIGEST, IDLE.
     *
     * @param jobId the job on which to change the priority.
     * @param priority The new priority to apply to the job.
     * @throws SchedulerException if the job is already finished.
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public void changePriority(JobId jobId, JobPriority priority) throws SchedulerException;

    /**
     * Return the state of the given job.<br>
     * The state contains informations about the job, every tasks and informations about the tasks.<br><br>
     * A user can only get the state of HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper message.
     *
     * @param jobId the job on which to get the state.
     * @return the current state of the given job
     * @throws AuthenticationException if you are not authenticated.
     * @throws UnknowJobException if the job does not exist.
     * @throws AccessRightException if you can't access to this particular job.
     */
    public JobState getJobState(JobId jobId) throws SchedulerException;

    /**
     * Get the list of job states that describe every jobs in the Scheduler.
     * The SchedulerState contains 3 list of jobs, pending, running, and finished
     *
     * @return the list of every jobs in the Scheduler
     * @throws SchedulerException if you are not authenticated.
     */
    public SchedulerState getSchedulerState() throws SchedulerException;

}
