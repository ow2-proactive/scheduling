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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.common;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.collection.PersistentMap;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive_grid_cloud_portal.scheduler.UserJobInfo;
import org.ow2.proactive_grid_cloud_portal.webapp.PersistentMapConverter;

/**
 * fdsfsd
 */
@Path("/scheduler/")
public interface SchedulerRestInterface {

    /**
     * Returns the ids of the current jobs under a list of string.
     * @param sessionId a valid session id
     * @param index optional, if a sublist has to be returned the index of the sublist
     * @param range optional, if a sublist has to be returned, the range of the sublist
     * @return a list of jobs' ids under the form of a list of string
    */
    @GET
    @Path("jobs")
    @Produces("application/json")
    public abstract List<String> jobs(@HeaderParam("sessionid") String sessionId,
            @QueryParam("index") @DefaultValue("-1") int index,
            @QueryParam("range") @DefaultValue("-1") int range) throws NotConnectedException,
            PermissionException;

    /**
     * Returns a subset of the scheduler state, including pending, running, finished
     * jobs (in this particular order).
     * each jobs is described using
     *   - its id
     *   - its owner
     *   - the JobInfo class
     * @param index optional, if a sublist has to be returned the index of the sublist
     * @param range optional, if a sublist has to be returned, the range of the sublist
     * @param sessionId a valid session id
     * @return a list of UserJobInfo
     */
    @GET
    @Path("jobsinfo")
    @Produces({ "application/json", "application/xml" })
    public abstract List<UserJobInfo> jobsinfo(@HeaderParam("sessionid") String sessionId,
            @QueryParam("index") @DefaultValue("-1") int index,
            @QueryParam("range") @DefaultValue("-1") int range) throws PermissionException,
            NotConnectedException;

    /**
     * Returns a map containing one entry with the revision id as key and the 
     * list of UserJobInfo as value.
     * each jobs is described using
     *   - its id
     *   - its owner
     *   - the JobInfo class
     * @param sessionId a valid session id
     * @param index optional, if a sublist has to be returned the index of the sublist
     * @param range optional, if a sublist has to be returned, the range of the sublist
     * @param myJobs fetch only the jobs owned by the user making the request
     * @param pending fetch pending jobs
     * @param running fetch running jobs
     * @param finished fetch finished jobs
     * @return a map containing one entry with the revision id as key and the 
     * list of UserJobInfo as value.
     */
    @GET
    @GZIP
    @Path("revisionjobsinfo")
    @Produces({ "application/json", "application/xml" })
    public abstract Map<AtomicLong, List<UserJobInfo>> revisionAndjobsinfo(
            @HeaderParam("sessionid") String sessionId, @QueryParam("index") @DefaultValue("-1") int index,
            @QueryParam("range") @DefaultValue("-1") int range,
            @QueryParam("myjobs") @DefaultValue("false") boolean myJobs,
            @QueryParam("pending") @DefaultValue("true") boolean pending,
            @QueryParam("running") @DefaultValue("true") boolean running,
            @QueryParam("finished") @DefaultValue("true") boolean finished) throws PermissionException,
            NotConnectedException;

    /**
     * Returns the state of the scheduler
     * @param sessionId a valid session id.
     * @return the scheduler state 
     */
    @GET
    @Path("state")
    @Produces({ "application/json", "application/xml" })
    public abstract SchedulerState schedulerState(@HeaderParam("sessionid") String sessionId)
            throws PermissionException, NotConnectedException;

    /**
     * Returns the revision number of the scheduler state
     * @param sessionId a valid session id.
     * @return the revision of the scheduler state 
     */
    @GET
    @Path("state/revision")
    @Produces({ "application/json", "application/xml" })
    public abstract long schedulerStateRevision(@HeaderParam("sessionid") String sessionId)
            throws PermissionException, NotConnectedException;

    /**
     * Returns a map with only one entry containing as key the revision and as content
     * the scheduler state
     * @param sessionId a valid session id.
     * @return a map of one entry containing the revision and the corresponding scheduler state 
     */
    @GET
    @Path("revisionandstate")
    @Produces({ "application/json", "application/xml" })
    public abstract Map<AtomicLong, SchedulerState> getSchedulerStateAndRevision(
            @HeaderParam("sessionid") String sessionId) throws PermissionException, NotConnectedException;

    /**
     * returns only the jobs of the current user
     * @param sessionId a valid session id
     * @return a scheduler state that contains only the jobs of the user that
     * owns the session <code>sessionid</code>  
    */
    @GET
    @Path("state/myjobsonly")
    @Produces({ "application/json", "application/xml" })
    public abstract SchedulerState getSchedulerStateMyJobsOnly(@HeaderParam("sessionid") String sessionId)
            throws PermissionException, NotConnectedException;

    /**
     * Returns a JobState of the job identified by the id <code>jobid</code>
     * @param sessionid a valid session id
     * @param jobid the id of the job to retrieve
     */
    @GET
    @Path("jobs/{jobid}")
    @Produces({ "application/json", "application/xml" })
    @XmlJavaTypeAdapter(value = PersistentMapConverter.class, type = PersistentMap.class)
    public abstract JobState listJobs(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Returns the job result associated to the job referenced by the 
     * id <code>jobid</code>
     * @param sessionid a valid session id
     * @result the job result of the corresponding job  
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result")
    @Produces("application/json")
    public abstract JobResult jobResult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, PermissionException,
            UnknownJobException;

    /**
     * Returns all the task results of this job as a map whose the key is the
     * name of the task and its task result.<br>
     * If the result cannot be instantiated, the content is replaced by the 
     * string 'Unknown value type'. To get the serialized form of a given result,
     * one has to call the following restful service 
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * @param sessionid a valid session id
     * @param jobid a job id
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result/value")
    @Produces("application/json")
    public abstract Map<String, Serializable> jobResultValue(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, PermissionException,
            UnknownJobException;

    /**
     * Delete a job
     * @param sessionId a valid session id
     * @param jobId the id of the job to delete
     * @return true if success, false if the job not yet finished (not removed,
     * kill the job then remove it) 
     * 
     */
    @DELETE
    @Path("jobs/{jobid}")
    @Produces("application/json")
    public abstract boolean removeJob(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
    * Kill the job represented by jobId.<br>
    *
    * @param sessionId a valid session id
    * @param jobId the job to kill.
    * @return true if success, false if not.
    */
    @PUT
    @Path("jobs/{jobid}/kill")
    @Produces("application/json")
    public abstract boolean killJob(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Returns a list of the name of the tasks belonging to job <code>jobId</code>
     * @param sessionId a valid session id
     * @param the jobid one wants to list the tasks' name
     * @return a list of tasks' name 
     */
    @GET
    @Path("jobs/{jobid}/tasks")
    @Produces("application/json")
    public abstract List<String> getJobTasksIds(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Returns a map corresponding of a jobid
     * @param sessionId a valid session id
     * @param jobId the job id
     * @return a map corresponding of a <code>jobId</code>
     * @throws IOException when the job archive is not found 
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/map")
    @Produces("application/xml")
    public String getJobMap(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) throws IOException;

    /**
     * Returns an image corresponding of a jobid
     * @param sessionId a valid session id
     * @param jobId the job id
     * @return a map corresponding of a <code>jobId</code>
     * @throws IOException when it is not possible to access to the archive
     */
    @GET
    @Path("jobs/{jobid}/image")
    @Produces("application/json")
    public String getJobImage(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) throws IOException;

    /**
     * Returns a list of taskState 
     * @param sessionId a valid session id
     * @param jobId the job id
     * @return a list of task' states of the job <code>jobId</code>
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates")
    @Produces("application/json")
    public abstract List<TaskState> getJobTaskStates(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Return the task state of the task <code>taskname</code> of the job <code>jobId</code> 
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return the task state of the task  <code>taskname</code> of the job <code>jobId</code> 
     */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}")
    @Produces("application/json")
    public abstract TaskState jobtasks(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname)
            throws NotConnectedException, UnknownJobException, PermissionException, UnknownTaskException;

    /**
     * Returns the value of the task result of task <code>taskName</code> of the job <code>jobId</code>
     * <strong>the result is deserialized before sending to the client, if the class is
     * not found the content is replaced by the string 'Unknown value type' </strong>. To get the serialized form of a given result,
     * one has to call the following restful service 
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return the value of the task result
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/value")
    @Produces("*/*")
    public abstract Serializable valueOftaskresult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname) throws Throwable;

    /**
     * Returns the value of the task result of the task <code>taskName</code> of the job <code>jobId</code>
     * This method returns the result as a byte array whatever the result is.
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return the value of the task result as a byte array.
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/serializedvalue")
    @Produces("*/*")
    public abstract byte[] serializedValueOftaskresult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname) throws Throwable;

    /**
     * Returns the task result of the task <code>taskName</code> 
     * of the job <code>jobId</code>
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return the task result of the task <code>taskName</code> 
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result")
    @Produces("application/json")
    public abstract TaskResult taskresult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     *  Returns all the logs generated by the task (either stdout and stderr)
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return  all the logs generated by the task (either stdout and stderr) or an empty string if the result is not yet available
    */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/all")
    @Produces("application/json")
    public abstract String tasklog(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     *  Returns the standard error output (stderr) generated by the task
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return  the stderr generated by the task or an empty string if the result is not yet available 
    */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/err")
    @Produces("application/json")
    public abstract String tasklogErr(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     *  Returns the standard output (stderr) generated by the task
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return  the stdout generated by the task or an empty string if the result is not yet available
    */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/out")
    @Produces("application/json")
    public abstract String tasklogout(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException;

    /**
     * Pauses the job represented by jobid
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/pause")
    @Produces("application/json")
    public abstract boolean pauseJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    /**
     * Resumes the job represented by jobid
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/resume")
    @Produces("application/json")
    public abstract boolean resumeJob(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

	/**
	 * Submit job using flat command file
	 * @param sessionId valid session id
	 * @param commandFileContent content of a command file: line separated native commands
	 * @param jobName name of the job to create
	 * @param selectionScriptContent content of a selection script, or null
	 * @param selectionScriptExtension extension of the selectionscript to determine script engine ("js", "py", "rb")
	 * @return Id of the submitted job
	 * @throws NotConnectedException
	 * @throws IOException
	 * @throws JobCreationException
	 * @throws PermissionException
	 * @throws SubmissionClosedException
	 */
	@POST
	@Path("submitflat")
	@Produces("application/json")
	public abstract JobId submitFlat(
			@HeaderParam("sessionid") String sessionId,
			@FormParam("commandFileContent") String commandFileContent,
			@FormParam("jobName") String jobName,
			@FormParam("selectionScriptContent") String selectionScriptContent,
			@FormParam("selectionScriptExtension") String selectionScriptExtension)
			throws NotConnectedException, IOException, JobCreationException,
			PermissionException, SubmissionClosedException;
    
    /**
     * Submits a job to the scheduler 
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return the <code>jobid</code> of the newly created job 
     */
    @POST
    @Path("submit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public abstract JobId submit(@HeaderParam("sessionid") String sessionId, MultipartFormDataInput multipart)
            throws IOException, JobCreationException, NotConnectedException, PermissionException,
            SubmissionClosedException;

    /**
     * terminates the session id <code>sessionId</code>
     * @param sessionId a valid session id
     * @throws NotConnectedException if the scheduler cannot be contacted
     * @throws PermissionException if you are not authorized to perform the action
     */
    @PUT
    @Path("disconnect")
    @Produces("application/json")
    public abstract void disconnect(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * pauses the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("pause")
    @Produces("application/json")
    public abstract boolean pauseScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * stops the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("stop")
    @Produces("application/json")
    public abstract boolean stopScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * resumes the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("resume")
    @Produces("application/json")
    public abstract boolean resumeScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * changes the priority of a job
     * @param sessionId a valid session id 
     * @param jobId the job id 
     * @param priorityName a string representing the name of the priority
     * @throws NotConnectedException
     * @throws UnknownJobException
     * @throws PermissionException
     * @throws JobAlreadyFinishedException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    public abstract void schedulerChangeJobPriorityByName(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId, @PathParam("name") String priorityName)
            throws NotConnectedException, UnknownJobException, PermissionException,
            JobAlreadyFinishedException;

    /**
     * changes the priority of a job
     * @param sessionId a valid session id 
     * @param jobId the job id 
     * @param priorityValue a string representing the value of the priority
     * @throws NumberFormatException
     * @throws NotConnectedException
     * @throws UnknownJobException
     * @throws PermissionException
     * @throws JobAlreadyFinishedException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    public abstract void schedulerChangeJobPriorityByValue(@HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId, @PathParam("value") String priorityValue)
            throws NumberFormatException, NotConnectedException, UnknownJobException, PermissionException,
            JobAlreadyFinishedException;

    /**
     * freezes the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("freeze")
    @Produces("application/json")
    public abstract boolean freezeScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * returns the status of the scheduler 
     * @param sessionId a valid session id
     * @return the scheduler status
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @Path("status")
    @Produces("application/json")
    public abstract SchedulerStatus getSchedulerStatus(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * starts the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("start")
    @Produces("application/json")
    public abstract boolean startScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * kills and shutdowns the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false if not
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("kill")
    @Produces("application/json")
    public abstract boolean killScheduler(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * Reconnect a new Resource Manager to the scheduler. 
     * Can be used if the resource manager has crashed.
     * @param sessionId a valid session id
     * @param rmURL the url of the resource manager 
     * @return true if success, false otherwise.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @POST
    @Path("linkrm")
    @Produces("application/json")
    public abstract boolean killScheduler(@HeaderParam("sessionid") final String sessionId,
            @FormParam("rmurl") String rmURL) throws NotConnectedException, PermissionException;

    /**
     * Tests whether or not the user is connected to the ProActive Scheduler
     * @param sessionId the session to test
     * @return true if the user connected to a Scheduler, false otherwise.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @PUT
    @Path("isconnected")
    @Produces("application/json")
    public abstract boolean isConnected(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * login to the scheduler using an form containing 2 fields (username & password)
     *  
     * @param username username
     * @param password password 
     * @return the session id associated to the login
     * @throws ActiveObjectCreationException
     * @throws NodeException
     * @throws LoginException
     * @throws SchedulerException
     * @throws KeyException
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("login")
    @Produces("application/json")
    public abstract String login(@FormParam("username") String username,
            @FormParam("password") String password) throws ActiveObjectCreationException, NodeException,
            LoginException, SchedulerException, KeyException;

    /**
     * login to the scheduler using a multipart form
     *  can be used either by submitting 
     *   - 2 fields username & password
     *   - a credential file with field name 'credential'
     * @param multipart
     * @return the session id associated to this new connection
     * @throws ActiveObjectCreationException
     * @throws NodeException
     * @throws KeyException
     * @throws LoginException
     * @throws SchedulerException
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces("application/json")
    public abstract String loginWithCredential(@MultipartForm LoginForm multipart)
            throws ActiveObjectCreationException, NodeException, KeyException, LoginException,
            SchedulerException, IOException;

    /**
     * Users currently connected to the scheduler
     * 
     * @param sessionId the session id associated to this new connection
     * @return list of users
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @GZIP
    @Path("users")
    @Produces("application/json")
    public List<UserIdentification> getUsers(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * returns statistics about the scheduler
     * @param sessionId the session id associated to this new connection
     * @return a string containing the statistics
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @Path("stats")
    @Produces("application/json")
    public abstract Map<String, String> getStatistics(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * returns a string containing some data regarding the user's account
     * @param sessionId the session id associated to this new connection
     * @return a string containing some data regarding the user's account
     * @throws NotConnectedException
     * @throws PermissionException
     */
    @GET
    @Path("stats/myaccount")
    @Produces("application/json")
    public abstract Map<String, String> getStatisticsOnMyAccount(@HeaderParam("sessionid") final String sessionId)
            throws NotConnectedException, PermissionException;

    /**
     * generates a credential file from user provided credentials
     * @return the credential file generated by the scheduler
     * @throws ConnectionException
     * @throws LoginException
     * @throws InternalSchedulerException
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("createcredential")
    @Produces("*/*")
    public byte[] getCreateCredential(@MultipartForm LoginForm multipart) throws ConnectionException,
            LoginException, InternalSchedulerException;

}