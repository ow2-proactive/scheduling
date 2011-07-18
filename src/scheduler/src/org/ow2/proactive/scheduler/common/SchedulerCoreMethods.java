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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.policy.Policy;


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
     * Get the result for the given task name and the given incarnation in the given jobId. <br >
     * A user can only get HIS result back.<br>
     * The incarnation argument represents the task result to get. If the task has failed 3 times and then has worked,
     * then 0 represents the last result, 1 the previous, ..., and 3 represents the result of the first execution.<br/><br/>
     * If the job does not exist, a schedulerException is sent with the proper message.<br>
     * So, if you have the right to get the task result that is in the job represented by the given jobId and if the job and task name exist,
     * so you will receive the result. In any other cases a schedulerException will be thrown.<br>
     *
     * @param jobId the job in which the task result is.
     * @param taskName the name of the task in which the result is.
     * @param inc id of incarnation (0 is the last one, 1 the previous, and so on...)
     * @return a job Result containing information about the result.
     * 		If null is returned, this task is not yet terminated or not available.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws UnknownTaskException if this task does not exist in the job.
     * @throws PermissionException if you can't access to this particular job.
     * @throws IllegalArgumentException if the incarnation argument is lower than 0 or greater than the number of terminated execution.
     */
    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Try to kill the task with the given task name in the given jobId.
     * A user can only kill HIS task.<br>
     * If the job does not exist, an UnknownJobException is sent with the proper message.<br>
     * So, if you have the right to kill this task and if the job and task name exist and is running,
     * the task will be killed and this method will return <code>true</code>.<br/>
     * In any other cases a {@link schedulerException} will be thrown.<br>
     *
     * @param jobId the job containing the task to be killed.
     * @param taskName the name of the task to kill.
     * @return true if the action to kill the task has succeed,
     * 			false if the task cannot be killed because it's not running.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws UnknownTaskException if this task does not exist in the job.
     * @throws PermissionException if you can't access to this particular job and task.
     */
    public boolean killTask(JobId jobId, String taskName) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException;

    /**
     * Try to restart the task represented by the given task name in the given jobId.
     * A user can only restart HIS task.<br>
     * If the job does not exist, an UnknownJobException is sent with the proper message.<br>
     * So, if you have the right to restart this task and if the job and task name exist and is running,
     * the task will be restarted and this method will return <code>true</code>.<br/>
     * The given delay is the delay between the task termination and it's eligibility to be re-scheduled.
     * In any other cases a {@link schedulerException} will be thrown.<br><br/>
     *
     * After this call, the following situations can occur :
     * <ul>
     * 	<li>The task has not yet reached its max number of execution : it will be re-scheduled after delay</li>
     * 	<li>The task has reached its max number of execution : it becomes faulty</li>
     * 	<li>The task has reached its max number of execution and is cancelJobOnError : it becomes faulty and the job is terminated</li>
     * </ul>
     *
     * @param jobId the job containing the task to be restarted.
     * @param taskName the name of the task to restart.
     * @param restartDelay the delay between the task termination and it's eligibility to be re-scheduled (in sec)
     * @return true if the action to restart the task has succeed,
     * 			false if the task cannot be restarted because it's not running.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws UnknownTaskException if this task does not exist in the job.
     * @throws PermissionException if you can't access to this particular job and task.
     */
    public boolean restartTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Try to stop the task execution represented by the given task name in the given jobId.<br/>
     * If the job does not exist, an UnknownJobException is sent with the proper message.<br>
     * So, if you have the right to stop this task and if the job and task name exist and is running,
     * the task will be stopped and restarted later and this method will return <code>true</code>.<br/>
     * The given delay is the delay between the task termination and it's eligibility to be re-scheduled.
     * In any other cases a {@link schedulerException} will be thrown.<br><br/>
     *
     * @param jobId the job containing the task to be stopped.
     * @param taskName the name of the task to stop.
     * @param restartDelay the delay between the task termination and it's eligibility to be re-scheduled (in sec)
     * @return true if the action to stop the task has succeed,
     * 			false if the task cannot be stopped because it's not running.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws UnknownTaskException if this task does not exist in the job.
     * @throws PermissionException if you can't access to this particular job and task.
     */
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Remove the job from the scheduler.
     *
     * @param jobId the job to be removed.
     * @throws NotConnectedException if you are not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if you can't access to this particular job.
     */
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

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
     * For administrator only, change the policy of the scheduler.<br>
     * This method will immediately change the policy and so the whole scheduling process.
     *
     * @param policyClassName the new policy full class name.
     * @return true if the policy has been correctly change, false if not.
     * @throws SchedulerException if an exception occurs while serving the request.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean changePolicy(String policyClassName) throws NotConnectedException, PermissionException;

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

    /**
     * For administrator only, order a reload to the policy.<br/>
     * The default behavior reload the configuration file and update the properties available in policy.
     *
     * This will cause the {@link Policy#reloadConfig()} method to be called.
     * This last method can be overridden in the policy to perform a custom behavior on reload.
     *
     * @return true if success, false otherwise.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you have not enough permission to access this method.
     */
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException;

}
