/*
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.StreamingOutput;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;


public interface ISchedulerClient extends Scheduler {

    /**
     * Initialize this instance.
     *
     * @param url
     *            the REST server URL
     * @param login
     *            the login
     * @param password
     *            the password
     * @throws Exception
     *             if an error occurs during the initialization
     */
    public void init(String url, String login, String password) throws Exception;

    /**
     * Initialize this instance.
     * HTTPS certificate checking is disabled.
     *
     * @param url
     *            the REST server URL
     * @param login
     *            the login
     * @param password
     *            the password
     * @throws Exception
     *             if an error occurs during the initialization
     */
    public void initInsecure(String url, String login, String password) throws Exception;

    /**
     * Sets the session identifier explicitly.
     *
     * @param sid session identifier
     */
    public void setSession(String sid);

    /**
     * Retrieves the current session identifier.
     *
     * @return the current session identifier
     */
    public String getSession();

    /**
     * Returns <tt>true</tt>, if the scheduler has finished the execution of the
     * specified job.
     *
     * @param jobId
     *            the job identifier object
     * @return true if the scheduler has finished the execution of the job
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws UnknownJobException
     *             if the job identifier is invalid
     * @throws PermissionException
     *             if the user does not have permission to view the state of the
     *             specified job
     */
    public boolean isJobFinished(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Returns <tt>true</tt>, if the scheduler has terminated the execution of
     * the specified job.
     *
     * @param jobId
     *            the job identifier string
     *
     * @see #isJobFinished(JobId)
     */
    public boolean isJobFinished(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Causes the current thread to wait until the scheduler has finished the
     * execution of the specified job or the specified amount of time has
     * elapsed.
     *
     * <p>
     * If the job execution finishes before the elapse of wait time, the result
     * of the job is returned. Otherwise a timeout exception is thrown.
     *
     * @param jobId
     *            the job identifier object
     *
     * @param timeout
     *            the maximum amount of time to wait
     * @return
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws UnknownJobException
     *             if the job identifier is invalid
     * @throws PermissionException
     *             if the user does not have permission to view the state of the
     *             specified job
     * @throws TimeoutException
     *             if the job execution does not finish before the elapse of
     *             wait time
     */
    public JobResult waitForJob(JobId jobId, long timeout) throws NotConnectedException, UnknownJobException,
            PermissionException, TimeoutException;

    /**
     * Causes the current thread to wait until the scheduler has finished the
     * execution of the specified job or the specified amount of time has
     * elapsed.
     *
     * <p>
     * If the job execution finishes before the elapse of wait time, the result
     * of the job is returned. Otherwise a timeout exception is thrown.
     *
     * @param jobId
     *            the job identifier string
     * @see #waitForJob(JobId, long)
     */
    public JobResult waitForJob(String jobId, long timeout) throws NotConnectedException,
            UnknownJobException, PermissionException, TimeoutException;

    /**
     * Returns <tt>true</tt>, if the scheduler has finished the execution of the
     * task.
     *
     * @param jobId
     *            the string identifier of the job to which the task is belong
     * @param taskName
     *            the task name
     * @return the task result
     * @throws UnknownJobException
     *             if the job identifier is invalid
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to view the state of the
     *             task
     * @throws UnknownTaskException
     *             if the task name is invalid
     */
    public boolean isTaskFinished(String jobId, String taskName) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException;

    /**
     * Causes the current thread to wait until the scheduler has finished
     * executing the specified task or the elapse of specified elapse time.
     * <p>
     * If the task execution finishes before the elapse of wait time, the result
     * of the task is returned. Otherwise a timeout exception is thrown.
     *
     * @param jobId
     *            the string identifier of the job to which the task is belong
     * @param taskName
     *            the task name
     * @return the task result
     * @throws UnknownJobException
     *             if the job identifier is invalid
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to view the state of
     *             task
     * @throws UnknownTaskException
     *             if the task name is invalid
     * @throws TimeoutException
     *             if the task execution does not finish before the elapse of
     *             wait time
     */
    public TaskResult waitForTask(String jobId, String taskName, long timeout) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException, TimeoutException;

    /**
     * Causes the current thread to wait until the execution of all specified
     * jobs has finished or the elapse of specified wait time.
     * <p>
     * Returns a list of job results, if the execution of all jobs specified
     * finishes before the elapse of the wait time. Otherwise a timeout
     * exception is thrown.
     *
     * @param jobIds
     *            the list of job identifier stings
     * @param timeout
     *            the maximum amount of wait time
     * @return a list of job results
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws UnknownJobException
     *             if one of jobs specified is invalid
     * @throws PermissionException
     *             if the user does not have permission to view the state of one
     *             of the specified jobs
     * @throws TimeoutException
     *             if the execution of all jobs specified does not finish before
     *             the elapse of specified wait time
     */
    public List<JobResult> waitForAllJobs(List<String> jobIds, long timeout) throws NotConnectedException,
            UnknownJobException, PermissionException, TimeoutException;

    /**
     * Causes the current thread to wait until the execution of any job finishes
     * or the elapse of the specified wait time.
     * <p>
     * Returns the string identifier and result of the finished job, if any of
     * the execution of the jobs finishes before the elapse of wait time.
     * Otherwise a timeout exception is thrown.
     *
     * @param jobIds
     *            the list of job identifier strings
     * @param timeout
     *            the maximum amount of wait time
     * @return the identifier and the result of a finished job
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws UnknownJobException
     *             if only of the job identifiers is invalid
     * @throws PermissionException
     *             if the user does not have permission to view the job state
     * @throws TimeoutException
     *             if none of the executions of jobs finishes before the elapse
     *             of wait time
     */
    public Map.Entry<String, JobResult> waitForAnyJob(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException;

    /**
     * Causes the current thread to wait until any of the specified tasks
     * finishes or the elapse of specified amount of time.
     * <p>
     * Returns name and the result of a finished task. Otherwise a timeout
     * exception is thrown.
     *
     * @param jobId
     *            the job identifier of the job to which the specified tasks
     *            belong
     * @param taskNames
     *            the names of the tasks
     * @param timeout
     *            the maximum amount of wait time
     * @return the name and the result of a finished task
     * @throws UnknownJobException
     *             if the job identifier is invalid
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to view the state of the
     *             tasks
     * @throws UnknownTaskException
     *             if the name of a task is invalid
     * @throws TimeoutException
     *             if none of the executions of tasks finish before the elapse
     *             of wait time
     */
    public Entry<String, TaskResult> waitForAnyTask(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException;

    /**
     * Causes the current thread to wait until all the executions of specified
     * tasks finish or the elapse of specified amount of time.
     * <p>
     * Returns a list of task name and task result pairs, if all the executions
     * of specified tasks finishes before the elapse of wait time. Otherwise a
     * timeout exception is thrown.
     *
     * @param jobId
     *            the identifier of the job to which all the specified tasks
     *            belong
     * @param taskNames
     *            the task names
     * @param timeout
     *            the maximum amount of wait time
     * @return a list of task name and task result pairs
     * @throws UnknownJobException
     *             if the job identifier is invalid
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired.
     * @throws PermissionException
     *             if the user does not have permission to view the state of the
     *             tasks
     * @throws UnknownTaskException
     *             if a task name is invalid
     * @throws TimeoutException
     *             if all the executions of the tasks do not finish before the
     *             elapse of maximum wait time
     */
    public List<Entry<String, TaskResult>> waitForAllTasks(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException;

    /**
     * Transfers the specified file from the local file system to the specified
     * dataspace at the server
     *
     * @param spacename
     *            the dataspace name
     * @param pathname
     *            the path of the stored file with respect to the dataspace
     * @param filename
     *            the name of the stored file with respect to the dataspace
     * @param file
     *            the local file
     * @return true, if the file transfer completes successfully
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to upload the file to
     *             the specified dataspace
     */
    public boolean pushFile(String spacename, String pathname, String filename, String file)
            throws NotConnectedException, PermissionException;

    /**
     * Retrieves the specified file from the server.
     *
     * @param space
     *            the dataspace name
     * @param pathname
     *            the pathname of the file with respect the dataspace
     * @param outputFile
     *            the name of the output file
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to retrieve the
     *             specified file from the server
     */
    public void pullFile(String space, String pathname, String outputFile) throws NotConnectedException,
            PermissionException;

    /**
     * Deletes the specified file from the server.
     *
     * @param space
     *            the dataspace name
     * @param pathname
     *            the pathname of the file with respect to the dataspace
     * @return true if the specified file has been deleted successfully
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to delete the file from
     *             the server
     */
    public boolean deleteFile(String space, String pathname) throws NotConnectedException,
            PermissionException;

    /**
     * Creates a job archive with self contained job classpath elements and submits to the scheduler.
     *
     * A user can only managed their jobs.
     * <p>
     * It will execute the tasks of the jobs as soon as resources are available.
     * The job will be considered as finished once every tasks have finished (error or success).
     * </p>
     * Thus, user could get the job result according to the precious result.
     * <br /><br />
     * It is possible to get a listener on the scheduler.
     * (see {@link Scheduler#addEventListener(SchedulerEventListener, boolean, SchedulerEvent...)} for more details)
     *
     * @param job the new job to submit.
     * @return the generated new job ID.
     * @throws NotConnectedException if you are not authenticated.
     * @throws PermissionException if you can't access to this particular method.
     * @throws SubmissionClosedException if the submit action could not be performed.
     * @throws JobCreationException if Their was a problem while creation the job
     */
    public JobId submitAsJobArchive(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException;
}
