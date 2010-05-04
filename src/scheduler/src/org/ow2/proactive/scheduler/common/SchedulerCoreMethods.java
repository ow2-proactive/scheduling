/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;


/**
 * It necessarily provides additional methods for someone connected to the scheduler.<br>
 * it represents the methods that have deep access to the Core to be performed.<br>
 * This interface only exists to make the refactoring easier.<br>
 * It contains all the method that HAVE to access the core. In fact, the core implements it.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 *
 * $Id$
 */
@PublicAPI
public interface SchedulerCoreMethods {

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
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     */
    public JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException,
            UnknownJobException;

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
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws UnknownTaskException if this task result does not exist or is unknown.
     * @throws PermissionException if you can't access to this particular job.
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Remove the job from the scheduler.
     *
     * @param jobId the job to be removed.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     */
    public void removeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Listen for the tasks user logs.<br>
     * A user can only listen to HIS jobs.
     *
     * @param jobId the id of the job to listen to.
     * @param appenderProvider a provider for an appender that must be connected on a log server on the caller side (see {@link LogForwardingService})
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     */
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException;

    /**
     * Kill the job represented by jobId.<br>
     * This method will kill every running tasks of this job, and remove it from the scheduler.<br>
     * The job won't be terminated, it won't have result.
     *
     * @param jobId the job to kill.
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     */
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Pause the job represented by jobId.<br>
     * This method will finish every running tasks of this job, and then pause the job.<br>
     * The job will have to be resumed in order to finish.
     *
     * @param jobId the job to pause.
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     */
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Resume the job represented by jobId.<br>
     * This method will restart every non-finished tasks of this job.
     *
     * @param jobId the job to resume.
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     */
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Change the priority of the job represented by jobId.<br>
     * Only administrator can change the priority to HIGH, HIGEST, IDLE.
     *
     * @param jobId the job on which to change the priority.
     * @param priority The new priority to apply to the job.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     * @throws JobAlreadyFinishedException if you want to change the priority on a finished job.
     */
    public void changeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException;

    /**
     * Return the state of the given job.<br>
     * The state contains informations about the job, every tasks and informations about the tasks.<br><br>
     * A user can only get the state of HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper message.
     *
     * @param jobId the job on which to get the state.
     * @return the current state of the given job
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     */
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Get the list of job states that describe every jobs in the Scheduler.
     * The SchedulerState contains 3 list of jobs, pending, running, and finished
     *
     * @return the list of every jobs in the Scheduler
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you can't access to this particular method.
     */
    public SchedulerState getState() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Change the policy of the scheduler.<br>
     * This method will immediately change the policy and so the whole scheduling process.
     *
     * @param newPolicyFile the new policy file as a class.
     * @return true if the policy has been correctly change, false if not.
     * @throws SchedulerException if an exception occurs while serving the request.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean changePolicy(Class<? extends Policy> newPolicyFile) throws NotConnectedException,
            PermissionException;

    /**
     * For administrator only, Start the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean start() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Stop the scheduler.<br>
     * Once done, you won't be able to submit job, and the scheduling will be stopped.<br>
     * Every running jobs will be terminated.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean stop() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Pause the scheduler by terminating running jobs.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean pause() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Freeze the scheduler by terminating running tasks.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean freeze() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Resume the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean resume() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Shutdown the scheduler.<br>
     * It will terminate every submitted jobs but won't accept new submit.<br>
     * Use {@link #kill()} if you want to stop the scheduling and exit the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean shutdown() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Kill the scheduler.<br>
     * Will stop the scheduling, and shutdown the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean kill() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Reconnect a new Resource Manager to the scheduler.<br>
     * Can be used if the resource manager has crashed.
     *
     * @param rmURL the URL of the new Resource Manager to link to the scheduler.<br>
     * 		Example : //host/RM_node_name
     * @return true if success, false otherwise.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException;

}
