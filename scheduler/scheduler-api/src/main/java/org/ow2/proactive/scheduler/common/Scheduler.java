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

import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.usage.SchedulerUsage;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;


/**
 * Scheduler interface for someone connected to the scheduler. This is suitable
 * for every role in the Scheduler<br>
 * This interface provides methods to managed the user task and jobs on the
 * scheduler.
 * <p>
 * Scheduler currently has 2 roles:
 * <ul>
 * <li>User: will only be able to managed his jobs and tasks, and also see the
 * entire scheduling process.</li>
 * <li>Admin: should do what user can do + administration stuffs.</li>
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
@PublicAPI
public interface Scheduler extends SchedulerUsage, ThirdPartyCredentials {

    /**
     * Returns the USER DataSpace URIs associated with the current user
     * 
     * @return USER Space URIs (one element for each available protocol)
     * @throws NotConnectedException
     *             if you are not authenticated.
     */
    List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException;

    /**
     * Returns the GLOBAL DataSpace URI available to all users
     * 
     * @return GLOBAL Space URIs (one element for each available protocol)
     * @throws NotConnectedException
     *             if you are not authenticated.
     */
    List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException;

    /**
     * Get the result for the given jobId. A user can only get HIS result back
     * except if he is admin.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.<br>
     * So, if you have the right to get the job result represented by the given
     * jobId and if the job exists, so you will receive the result. In any other
     * cases a schedulerException will be thrown.
     *
     * @param jobId
     *            the job on which the result will be send
     * @return a job Result containing information about the result. If the job
     *         result is not yet available (job not finished), null is returned.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException, UnknownJobException;

    /**
     * Get the result for the given task name and the given incarnation in the
     * given jobId.
     * <p>
     * A user can only get HIS result back.
     * <p>
     * The incarnation argument represents the task result to get. If the task
     * has failed 3 times and then has worked, then 0 represents the last
     * result, 1 the previous, ..., and 3 represents the result of the first
     * execution.
     * <p>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     * <p>
     * So, if you have the right to get the task result that is in the job
     * represented by the given jobId and if the job and task name exist, so you
     * will receive the result. In any other cases a schedulerException will be
     * thrown.
     *
     * @param jobId
     *            the job in which the task result is.
     * @param taskName
     *            the name of the task in which the result is.
     * @param inc
     *            id of incarnation (0 is the last one, 1 the previous, and so
     *            on...)
     * @return a job Result containing information about the result. If null is
     *         returned, this task is not yet terminated or not available.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job.
     * @throws IllegalArgumentException
     *             if the incarnation argument is lower than 0 or greater than
     *             the number of terminated execution.
     */
    TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Try to kill the task with the given task name in the given jobId. A user
     * can only kill HIS task.
     * <p>
     * If the job does not exist, an UnknownJobException is sent with the proper
     * message.
     * <p>
     * So, if you have the right to kill this task and if the job and task name
     * exist and is running, the task will be killed and this method will return
     * <code>true</code>. In any other cases a {@link SchedulerException} will
     * be thrown.
     *
     * @param jobId
     *            the job containing the task to be killed.
     * @param taskName
     *            the name of the task to kill.
     * @return true if the action to kill the task has succeed, false if the
     *         task cannot be killed because it's not running.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job and task.
     */
    boolean killTask(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Try to restart the task represented by the given task name in the given
     * jobId. A user can only restart HIS task.
     * <p>
     * If the job does not exist, an UnknownJobException is sent with the proper
     * message. So, if you have the right to restart this task and if the job
     * and task name exist and is running, the task will be restarted and this
     * method will return <code>true</code>.
     * <p>
     * The given delay is the delay between the task termination and it's
     * eligibility to be re-scheduled. In any other cases a
     * {@link SchedulerException} will be thrown.
     * <p>
     * After this call, the following situations can occur :
     * <ul>
     * <li>The task has not yet reached its max number of execution : it will be
     * re-scheduled after delay</li>
     * <li>The task has reached its max number of execution : it becomes faulty
     * </li>
     * <li>The task has reached its max number of execution and is
     * cancelJobOnError : it becomes faulty and the job is terminated</li>
     * </ul>
     *
     * @param jobId
     *            the job containing the task to be restarted.
     * @param taskName
     *            the name of the task to restart.
     * @param restartDelay
     *            the delay between the task termination and it's eligibility to
     *            be re-scheduled (in sec)
     * @return true if the action to restart the task has succeed, false if the
     *         task cannot be restarted because it's not running.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job and task.
     */
    boolean restartTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Try to stop the task execution represented by the given task name in the
     * given jobId.
     * <p>
     * If the job does not exist, an UnknownJobException is sent with the proper
     * message.
     * <p>
     * So, if you have the right to stop this task and if the job and task name
     * exist and is running, the task will be stopped and restarted later and
     * this method will return <code>true</code>.
     * <p>
     * The given delay is the delay between the task termination and it's
     * eligibility to be re-scheduled. In any other cases a
     * {@link SchedulerException} will be thrown.
     *
     * @param jobId
     *            the job containing the task to be stopped.
     * @param taskName
     *            the name of the task to stop.
     * @param restartDelay
     *            the delay between the task termination and it's eligibility to
     *            be re-scheduled (in sec)
     * @return true if the action to stop the task has succeed, false if the
     *         task cannot be stopped because it's not running.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job and task.
     */
    boolean preemptTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Remove the job from the scheduler.
     *
     * @param jobId
     *            the job to be removed.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Listen for the tasks user logs.
     * <p>
     * A user can only listen to HIS jobs.
     *
     * @param jobId
     *            the id of the job to listen to.
     * @param appenderProvider
     *            a provider for an appender that must be connected on a log
     *            server on the caller side (see {@link LogForwardingService})
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    void listenJobLogs(JobId jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Kill the job represented by jobId.<br>
     * This method will kill every running tasks of this job, and remove it from
     * the scheduler.<br>
     * The job won't be terminated, it won't have result.
     *
     * @param jobId
     *            the job to kill.
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Pause the job represented by jobId.<br>
     * This method will finish every running tasks of this job, and then pause
     * the job.<br>
     * The job will have to be resumed in order to finish.
     *
     * @param jobId
     *            the job to pause.
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Resume the job represented by jobId.<br>
     * This method will restart every non-finished tasks of this job.
     *
     * @param jobId
     *            the job to resume.
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Change the priority of the job represented by jobId.<br>
     * Only administrator can change the priority to HIGH, HIGEST, IDLE.
     *
     * @param jobId
     *            the job on which to change the priority.
     * @param priority
     *            The new priority to apply to the job.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     * @throws JobAlreadyFinishedException
     *             if you want to change the priority on a finished job.
     */
    void changeJobPriority(JobId jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException;

    /**
     * For administrator only, change the policy of the scheduler.
     * <p>
     * This method will immediately change the policy and so the whole
     * scheduling process.
     *
     * @param policyClassName
     *            the new policy full class name.
     * @return true if the policy has been correctly change, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean changePolicy(String policyClassName) throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Start the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean start() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Stop the scheduler.<br>
     * Once done, you won't be able to submit job, and the scheduling will be
     * stopped.<br>
     * Every running jobs will be terminated.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean stop() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Pause the scheduler by terminating running jobs.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean pause() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Freeze the scheduler by terminating running
     * tasks.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean freeze() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Resume the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean resume() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Shutdown the scheduler.<br>
     * It will terminate every submitted jobs but won't accept new submit.<br>
     * Use {@link #kill()} if you want to stop the scheduling and exit the
     * scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean shutdown() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Kill the scheduler.<br>
     * Will stop the scheduling, and shutdown the scheduler.
     *
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean kill() throws NotConnectedException, PermissionException;

    /**
     * For administrator only, Reconnect a new Resource Manager to the
     * scheduler.
     * <p>
     * Can be used if the resource manager has crashed.
     *
     * @param rmURL
     *            the URL of the new Resource Manager to link to the scheduler.
     *            Example: {@code //host/RM_node_name}
     * @return true if success, false otherwise.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException;

    /**
     * For administrator only, order a reload to the policy.
     * <p>
     * The default behavior reload the configuration file and update the
     * properties available in policy.
     *
     * This will cause the {@code Policy#reloadConfig()} method to be called.
     * This last method can be overridden in the policy to perform a custom
     * behavior on reload.
     *
     * @return true if success, false otherwise.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException;

    /**
     * Submit a new job to the scheduler. A user can only managed their jobs.
     * <p>
     * It will execute the tasks of the jobs as soon as resources are available.
     * The job will be considered as finished once every tasks have finished
     * (error or success). Thus, user could get the job result according to the
     * precious result.
     * <p>
     * It is possible to get a listener on the scheduler. (see
     * {@link Scheduler#addEventListener(SchedulerEventListener, boolean, SchedulerEvent...)}
     * for more details)
     *
     * @param job
     *            the new job to submit.
     * @return the generated new job ID.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you can't access to this particular method.
     * @throws SubmissionClosedException
     *             if the submit action could not be performed.
     * @throws JobCreationException
     *             if Their was a problem while creation the job
     */
    JobId submit(Job job)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException;

    /**
     * Get the result for the given jobId.<br>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only get HIS result back except if he is admin.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.<br>
     * So, if you have the right to get the job result represented by the given
     * jobId and if the job exists, so you will receive the result. In any other
     * cases a schedulerException will be thrown.
     *
     * @param jobId
     *            the job on which the result will be send
     * @return a job Result containing information about the result. If the job
     *         result is not yet available (job not finished), null is returned.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException, UnknownJobException;

    /**
     * Get the result for the given task name in the given jobId. <br >
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only get HIS result back.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.<br>
     * So, if you have the right to get the task result that is in the job
     * represented by the given jobId and if the job and task name exist, so you
     * will receive the result. In any other cases a schedulerException will be
     * thrown.<br>
     *
     * @param jobId
     *            the job in which the task result is.
     * @param taskName
     *            the name of the task in which the result is.
     * @return a job Result containing information about the result. If null is
     *         returned, this task is not yet terminated or not available.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    TaskResult getTaskResult(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Get the results for a set of tasks in the given jobId and filtered by a
     * given tag. A user can only get HIS result back.<br>
     * If the job does not exist, an UnknownJobException is sent with the proper
     * message.<br>
     * So, if you have the right to get the task result that is in the job
     * represented by the given jobId and if the job and task name exist, so you
     * will receive the result. In any other cases a schedulerException will be
     * thrown.<br>
     *
     * @param jobId
     *            the job in which the task result is.
     * @param taskTag
     *            the tag used to filter the tasks in which the result is.
     * @return a job Result containing information about the result. If the task
     *         result is not yet available, null is returned.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    List<TaskResult> getTaskResultsByTag(JobId jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Get the results for a set of tasks in the given jobId and filtered by a
     * given tag. <br >
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only get HIS result back.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.<br>
     * So, if you have the right to get the task result that is in the job
     * represented by the given jobId and if the job and task name exist, so you
     * will receive the result of these tasks. In any other cases a
     * schedulerException will be thrown.<br>
     *
     * @param jobId
     *            the job in which the task result is.
     * @param taskTag
     *            the tag used to filter the tasks in which the result is.
     * @return the list of task result containing information about the result.
     *         If null is returned, this task is not yet terminated or not
     *         available.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    List<TaskResult> getTaskResultsByTag(String jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Get the result for the given task name in the given jobId. A user can
     * only get HIS result back.<br>
     * If the job does not exist, an UnknownJobException is sent with the proper
     * message.<br>
     * So, if you have the right to get the task result that is in the job
     * represented by the given jobId and if the job and task name exist, so you
     * will receive the result. In any other cases a schedulerException will be
     * thrown.<br>
     *
     * @param jobId
     *            the job in which the task result is.
     * @param taskName
     *            the name of the task in which the result is.
     * @return a job Result containing information about the result. If the task
     *         result is not yet available, null is returned.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    TaskResult getTaskResult(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Get the result for the given task name and the given incarnation in the
     * given jobId. The jobId is given as a string. It's in fact the string
     * returned by the {@link JobId#value()} method. A user can only get HIS
     * result back. The incarnation argument represents the task result to get.
     * If the task has failed 3 times and then has worked, then 0 represents the
     * last result, 1 the previous, ..., and 3 represents the result of the
     * first execution.
     * <p>
     * If the job does not exist, a schedulerException is sent with the proper
     * message. So, if you have the right to get the task result that is in the
     * job represented by the given jobId and if the job and task name exist, so
     * you will receive the result. In any other cases a schedulerException will
     * be thrown.
     *
     * @param jobId
     *            the job in which the task result is.
     * @param taskName
     *            the name of the task in which the result is.
     * @param inc
     *            id of incarnation (0 is the last one, 1 the previous, and so
     *            on...)
     * @return a job Result containing information about the result. If null is
     *         returned, this task is not yet terminated or not available.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job.
     * @throws IllegalArgumentException
     *             if the incarnation argument is lower than 0 or greater than
     *             the number of terminated execution.
     */
    TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Remove the job from the scheduler.
     * <p>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only remove HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     *
     * @param jobId
     *            the job to be removed.
     * @return true if success, false if job is not terminated.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Listen for the tasks user logs.<br>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only listen to HIS jobs.
     *
     * @param jobId
     *            the id of the job to listen to.
     * @param appenderProvider
     *            a provider for an appender that must be connected on a log
     *            server on the caller side (see {@link LogForwardingService})
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    void listenJobLogs(String jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Kill the job represented by jobId.<br>
     * This method will kill every running tasks of this job, and remove it from
     * the scheduler.<br>
     * The job won't be terminated, it won't have result.<br>
     * <br>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only kill HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     *
     * @param jobId
     *            the job to kill.
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean killJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Try to kill the task with the given task name in the given jobId. A user
     * can only kill HIS task.<br>
     * If the job does not exist, an UnknownJobException is sent with the proper
     * message.<br>
     * So, if you have the right to kill this task and if the job and task name
     * exist and is running, the task will be killed and this method will return
     * <code>true</code>.<br>
     * In any other cases a {@link SchedulerException} will be thrown.<br>
     *
     * @param jobId
     *            the job containing the task to be killed.
     * @param taskName
     *            the name of the task to kill.
     * @return true if the action to kill the task has succeed, false if the
     *         task cannot be killed because it's not running.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job and task.
     */
    boolean killTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Try to restart the task represented by the given task name in the given
     * jobId. A user can only restart HIS task.<br>
     * If the job does not exist, an UnknownJobException is sent with the proper
     * message.<br>
     * So, if you have the right to restart this task and if the job and task
     * name exist and is running, the task will be restarted and this method
     * will return <code>true</code>.<br>
     * The given delay is the delay between the task termination and it's
     * eligibility to be re-scheduled. In any other cases a
     * {@link SchedulerException} will be thrown.
     * <p>
     * After this call, the following situations can occur :
     * <ul>
     * <li>The task has not yet reached its max number of execution : it will be
     * re-scheduled after delay</li>
     * <li>The task has reached its max number of execution : it becomes faulty
     * </li>
     * <li>The task has reached its max number of execution and is
     * cancelJobOnError : it becomes faulty and the job is terminated</li>
     * </ul>
     *
     * @param jobId
     *            the job containing the task to be restarted.
     * @param taskName
     *            the name of the task to restart.
     * @param restartDelay
     *            the delay between the task termination and it's eligibility to
     *            be re-scheduled (in sec)
     * @return true if the action to restart the task has succeed, false if the
     *         task cannot be restarted because it's not running.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job and task.
     */
    boolean restartTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    boolean finishInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    boolean restartInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Try to stop the task execution represented by the given task name in the
     * given jobId.<br>
     * If the job does not exist, an UnknownJobException is sent with the proper
     * message.<br>
     * So, if you have the right to stop this task and if the job and task name
     * exist and is running, the task will be stopped and restarted later and
     * this method will return <code>true</code>.<br>
     * The given delay is the delay between the task termination and it's
     * eligibility to be re-scheduled. In any other cases a
     * {@link SchedulerException} will be thrown.
     *
     * @param jobId
     *            the job containing the task to be stopped.
     * @param taskName
     *            the name of the task to stop.
     * @param restartDelay
     *            the delay between the task termination and it's eligibility to
     *            be re-scheduled (in sec)
     * @return true if the action to stop the task has succeed, false if the
     *         task cannot be stopped because it's not running.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws PermissionException
     *             if you can't access to this particular job and task.
     */
    boolean preemptTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Pause the job represented by jobId.<br>
     * This method will finish every running tasks of this job, and then pause
     * the job.<br>
     * The job will have to be resumed in order to finish.<br>
     * <br>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only pause HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     *
     * @param jobId
     *            the job to pause.
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Restart all in error tasks in the job represented by jobId.<br>
     * This method will restart every in error tasks of this job.<br>
     * <br>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only restart HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     *
     * @param jobId
     *            the job to resume.
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean restartAllInErrorTasks(String jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Resume the job represented by jobId.<br>
     * This method will restart every non-finished tasks of this job.<br>
     * <br>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only resume HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     *
     * @param jobId
     *            the job to resume.
     * @return true if success, false if not.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Change the priority of the job represented by jobId.<br>
     * Only administrator can change the priority to HIGH, HIGEST, IDLE.<br>
     * <br>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only change HIS job priority.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     *
     * @param jobId
     *            the job on which to change the priority.
     * @param priority
     *            The new priority to apply to the job.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     * @throws JobAlreadyFinishedException
     *             if you want to change the priority on a finished job.
     */
    void changeJobPriority(String jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException;

    /**
     * Return the state of the given job.<br>
     * The state contains informations about the job, every tasks and
     * informations about the tasks.<br>
     * <br>
     * The jobId is given as a string. It's in fact the string returned by the
     * {@link JobId#value()} method.<br>
     * A user can only get the state of HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     *
     * @param jobId
     *            the job on which to get the state.
     * @return the current state of the given job
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Get the current status of the Scheduler
     *
     * @return the current status of the Scheduler
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you can't access to this particular method.
     */
    SchedulerStatus getStatus() throws NotConnectedException, PermissionException;

    /**
     * Return the state of the given job.<br>
     * The state contains informations about the job, every tasks and
     * informations about the tasks.<br>
     * <br>
     * A user can only get the state of HIS job.<br>
     * If the job does not exist, a schedulerException is sent with the proper
     * message.
     *
     * @param jobId
     *            the job on which to get the state.
     * @return the current state of the given job
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * Get the list of job states that describe every jobs in the Scheduler. The
     * SchedulerState contains 3 list of jobs, pending, running, and finished.
     * Every jobs will be returned depending on your right.
     * <p>
     * If a PermissionException is thrown, try using {@link #getState(boolean)}
     * method with argument {@code true}.
     *
     * @return the list of every jobs in the Scheduler
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you can't access to this particular method.
     */
    SchedulerState getState() throws NotConnectedException, PermissionException;

    /**
     * Get the list of job states that describe every jobs in the Scheduler. The
     * SchedulerState contains 3 list of jobs, pending, running, and finished If
     * the given argument is true, only job that you own will be returned,
     * otherwise every jobs will be returned depending on your right.
     *
     * @param myJobsOnly
     *            true to get only my jobs, false to get any.
     * @return the list of every jobs in the Scheduler
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you can't access to this particular method.
     */
    SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException;

    /**
     * Add a scheduler event Listener. this listener provides method to notice
     * of new coming job, started task, finished task, running job, finished
     * job, etc...<br>
     * <p>
     * This method behaves exactly the same as a call to addEventListener(sel,
     * myEventsOnly, false, events); but return nothing
     * </p>
     *
     * @param sel
     *            a SchedulerEventListener on which the scheduler will talk.
     * @param myEventsOnly
     *            a boolean that indicates if you want to receive every event or
     *            just the one concerning your jobs. This won't affect the
     *            scheduler state event that will be sent anyway.
     * @param events
     *            An array of events that you want to receive from the
     *            scheduler.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException;

    /**
     * Add a scheduler event Listener. this listener provides method to notice
     * of new coming job, started task, finished task, running job, finished
     * job, etc...<br>
     * <p>
     * You may use this method once by remote or active object.<br>
     * Every call to this method will remove your previous listening settings.
     * <br>
     * If you want to get 2 type of events, add the 2 events type you want at
     * the end of this method. If no type is specified, all of them will be
     * sent.
     * </p>
     * <p>
     * If you want to received the events concerning your job only, just set the
     * 'myEventsOnly' parameter to true. otherwise, you will received events
     * coming from any user.
     * </p>
     *
     * @param sel
     *            a SchedulerEventListener on which the scheduler will talk.
     * @param myEventsOnly
     *            a boolean that indicates if you want to receive every events
     *            or just those concerning your jobs. This won't affect the
     *            scheduler state event that will be sent anyway.
     * @param getCurrentState
     *            if false, this method returns null, if true, it returns the
     *            Scheduler current state.
     * @param events
     *            An array of events that you want to receive from the
     *            scheduler.
     * @return the scheduler current state containing the different lists of
     *         jobs if the getInitialState parameter is true, null if false.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you can't access to this particular job.
     */
    SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly, boolean getCurrentState,
            SchedulerEvent... events) throws NotConnectedException, PermissionException;

    /**
     * Remove the current event listener your listening on.<br>
     * If no listener is defined, this method has no effect.
     *
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you can't access to this particular method.
     */
    void removeEventListener() throws NotConnectedException, PermissionException;

    /**
     * Disconnect properly the user from the scheduler.
     *
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you can't access to this particular method.
     */
    void disconnect() throws NotConnectedException, PermissionException;

    /**
     * Test whether or not the user is connected to the ProActive Scheduler.
     * Note that a call to this method DOES NOT renew the connection lease.
     * 
     * @return true if the user connected to a Scheduler, false otherwise.
     */
    boolean isConnected();

    /**
     * This method renew the connection lease without other side effect.
     * 
     * @throws NotConnectedException
     *             if you are not authenticated.
     */
    void renewSession() throws NotConnectedException;

    /**
     * Retrieves server logs for a job with the given id. Only the job owner of
     * admin if the scheduler can request these logs.
     * 
     * It's a combination of corresponding tasks logs belonging to this job plus
     * some extra job specific information.
     * 
     * @param id
     *            of the job for which logs are requested
     * 
     * @return job's logs
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    String getJobServerLogs(String id) throws UnknownJobException, NotConnectedException, PermissionException;

    /**
     * Retrieves server logs for a task with the given id. Only the job owner of
     * admin if the scheduler can request these logs.
     * 
     * It's a combination of corresponding tasks logs belonging to this job plus
     * some extra job specific information.
     * 
     * @param id
     *            of the job where the task is.
     * @param taskName
     *            the name of the task.
     * 
     * @return tasks's logs
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws UnknownTaskException
     *             if this task does not exist in the job.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    String getTaskServerLogs(String id, String taskName)
            throws UnknownJobException, UnknownTaskException, NotConnectedException, PermissionException;

    /**
     * Retrieves server logs for a set of tasks filtered by the given tag. Only
     * the job owner of admin if the scheduler can request these logs.
     *
     * It's a combination of corresponding tasks logs belonging to this job plus
     * some extra job specific information.
     *
     * @param id
     *            of the job where the task is.
     * @param taskTag
     *            the tag used to filter tasks.
     *
     * @return tasks's logs
     * @throws UnknownJobException
     *             if the job does not exist.
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    String getTaskServerLogsByTag(String id, String taskTag)
            throws UnknownJobException, NotConnectedException, PermissionException;

    /**
     * Retrieves a job list of the scheduler.
     *
     * @param offset
     *            says to start from this job is
     * @param limit
     *            max number of jobs to retrieve
     * @param filterCriteria
     *            defines types of job (myonly, pending, running, finished).
     *            Important! If user tries to get all jobs (myonly is false) but
     *            does not have permissions to do it (namely
     *            HandleOnlyMyJobsPermission(true)) user will get his own jobs
     *            instead of a PermissionException. This behavior should
     *            simplify the client design.
     *
     * @param sortParameters
     *            defines in how jobs must be sorted
     *
     * @return jobs list according to all criteria
     * @throws NotConnectedException
     *             if you are not authenticated.
     * @throws PermissionException
     *             if you have not enough permission to access this method.
     */
    Page<JobInfo> getJobs(int offset, int limit, JobFilterCriteria filterCriteria,
            List<SortParameter<JobSortParameter>> sortParameters) throws NotConnectedException, PermissionException;

    /**
     * Returns a list of connected users.
     */
    List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException;

    /**
     * Returns a list of users having jobs. These are meaningful users for
     * accounting {@link SchedulerUsage}
     */
    List<SchedulerUserInfo> getUsersWithJobs() throws NotConnectedException, PermissionException;

    /**
     * Retrieve a tasks names list from the scheduler.
     * 
     * @param taskTag
     *            a complete tag to use to filter tasks
     * @param from
     *            the starting date to fetch tasks from. The format is in Epoch
     *            time.
     * @param to
     *            the end date to stop fetching tasks. The format is in Epoch
     *            time.
     * @param mytasks
     *            <code>True</code> will only fetch the user tasks,
     *            <code>False</code> will fetch everyones.
     * @param running
     *            fetch the running tasks.
     * @param pending
     *            fetch the pending tasks.
     * @param finished
     *            fetch the finished tasks.
     * @param offset
     *            the starting task to include in the paginated list.
     * @param limit
     *            the last task (not included) before stopping fetching tasks in
     *            the paginated list.
     * @return the paginated list of tasks names satisfying the given criterias.
     *         The total number of tasks (without pagination() is also returned.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, boolean running, boolean pending,
            boolean finished, int offset, int limit) throws NotConnectedException, PermissionException;

    /**
     * Retrieve a taskstates list from the scheduler.
     * 
     * @param taskTag
     *            a complete tag to use to filter tasks
     * @param from
     *            the starting date to fetch tasks from. The format is in Epoch
     *            time.
     * @param to
     *            the end date to stop fetching tasks. The format is in Epoch
     *            time.
     * @param mytasks
     *            <code>True</code> will only fetch the user tasks,
     *            <code>False</code> will fetch everyones.
     * @param running
     *            fetch the running tasks.
     * @param pending
     *            fetch the pending tasks.
     * @param finished
     *            fetch the finished tasks.
     * @param offset
     *            the starting task to include in the paginated list.
     * @param limit
     *            the last task (not included) before stopping fetching tasks in
     *            the paginated list.
     * @return the paginated list of taskstates satisfying the given criterias.
     *         The total number of tasks (without pagination() is also returned.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks, boolean running, boolean pending,
            boolean finished, int offset, int limit, SortSpecifierContainer sortParams)
            throws NotConnectedException, PermissionException;

    /**
     * Retrieve a job info by it id.
     * 
     * @param jobId
     *            the id of the job we want to fetch info.
     * @return the <code>JobInfo</code> associated to the given id
     * @throws UnknownJobException
     * @throws NotConnectedException
     * @throws PermissionException
     */
    JobInfo getJobInfo(String jobId) throws UnknownJobException, NotConnectedException, PermissionException;

    /**
     * Change the START_AT generic information at job level and reset the
     * scheduledAt at task level
     * 
     * @param jobId
     *            id of the job that needs to be updated
     * @param startAt
     *            its value should be ISO 8601 compliant
     * @throws NotConnectedException
     * @throws UnknownJobException
     * @throws PermissionException
     */
    boolean changeStartAt(JobId jobId, String startAt)
            throws NotConnectedException, UnknownJobException, PermissionException;

    /**
     * retrieve a job content with the given job id, replace the general
     * information with the given one, resubmit the job
     * 
     * @param jobId
     *            job id used to retrieve the job content
     * @param generalInfo
     *            general information to replace or add to the job
     * @return job id of the new created job
     * @throws NotConnectedException
     * @throws UnknownJobException
     * @throws PermissionException
     * @throws SubmissionClosedException
     * @throws JobCreationException
     */
    JobId copyJobAndResubmitWithGeneralInfo(JobId jobId, Map<String, String> generalInfo) throws NotConnectedException,
            UnknownJobException, PermissionException, SubmissionClosedException, JobCreationException;

    /**
     * @return
     * @throws PermissionException 
     * @throws NotConnectedException 
     */
    Map<Object, Object> getPortalConfiguration() throws NotConnectedException, PermissionException;

}
