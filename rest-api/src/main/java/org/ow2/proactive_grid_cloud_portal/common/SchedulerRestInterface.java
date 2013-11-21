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
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobUsageData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerStatusData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerUserData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobAlreadyFinishedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.LogForwardingRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownTaskRestException;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;


@Path("/scheduler/")
public interface SchedulerRestInterface {

    String ENCODING = "utf-8";

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
    public abstract List<String> jobs(@HeaderParam("sessionid")
    String sessionId, @QueryParam("index")
    @DefaultValue("-1")
    int index, @QueryParam("range")
    @DefaultValue("-1")
    int range) throws NotConnectedRestException, PermissionRestException;

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
     * @return a list of UserJobData
     */
    @GET
    @Path("jobsinfo")
    @Produces( { "application/json", "application/xml" })
    public abstract List<UserJobData> jobsinfo(@HeaderParam("sessionid")
    String sessionId, @QueryParam("index")
    @DefaultValue("-1")
    int index, @QueryParam("range")
    @DefaultValue("-1")
    int range) throws PermissionRestException, NotConnectedRestException;

    /**
     * Returns a map containing one entry with the revision id as key and the 
     * list of UserJobData as value.
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
     * list of UserJobData as value.
     */
    @GET
    @GZIP
    @Path("revisionjobsinfo")
    @Produces( { "application/json", "application/xml" })
    public abstract Map<Long, List<UserJobData>> revisionAndjobsinfo(@HeaderParam("sessionid")
    String sessionId, @QueryParam("index")
    @DefaultValue("-1")
    int index, @QueryParam("range")
    @DefaultValue("-1")
    int range, @QueryParam("myjobs")
    @DefaultValue("false")
    boolean myJobs, @QueryParam("pending")
    @DefaultValue("true")
    boolean pending, @QueryParam("running")
    @DefaultValue("true")
    boolean running, @QueryParam("finished")
    @DefaultValue("true")
    boolean finished) throws PermissionRestException, NotConnectedRestException;

    /**
     * Returns the revision number of the scheduler state
     * @param sessionId a valid session id.
     * @return the revision of the scheduler state 
     */
    @GET
    @Path("state/revision")
    @Produces( { "application/json", "application/xml" })
    public abstract long schedulerStateRevision(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedRestException;

    /**
     * Returns a JobState of the job identified by the id <code>jobid</code>
     * @param sessionId a valid session id
     * @param jobId the id of the job to retrieve
     */
    @GET
    @Path("jobs/{jobid}")
    @Produces( { "application/json", "application/xml" })
    public abstract JobStateData listJobs(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * Returns the job result associated to the job referenced by the 
     * id <code>jobid</code>
     * @param sessionId a valid session id
     * @return  the job result of the corresponding job
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result")
    @Produces("application/json")
    public abstract JobResultData jobResult(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, PermissionRestException, UnknownJobRestException;

    /**
     * Returns all the task results of this job as a map whose the key is the
     * name of the task and its task result.<br>
     * If the result cannot be instantiated, the content is replaced by the 
     * string 'Unknown value type'. To get the serialized form of a given result,
     * one has to call the following restful service 
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * @param sessionId a valid session id
     * @param jobId a job id
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result/value")
    @Produces("application/json")
    public abstract Map<String, String> jobResultValue(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, PermissionRestException, UnknownJobRestException;

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
    public abstract boolean removeJob(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     *  Returns job server logs
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return job traces from the scheduler and resource manager
    */
    @GET
    @GZIP
    @Path("jobs/{jobid}/log/server")
    @Produces("application/json")
    public abstract String jobServerLog(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    public abstract boolean killJob(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * Returns a list of the name of the tasks belonging to job <code>jobId</code>
     * @param sessionId a valid session id
     * @param jobId jobid one wants to list the tasks' name
     * @return a list of tasks' name 
     */
    @GET
    @Path("jobs/{jobid}/tasks")
    @Produces("application/json")
    public abstract List<String> getJobTasksIds(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    public String getJobMap(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws IOException, NotConnectedRestException;

    /**
     * Returns a base64 utf-8 encoded png image corresponding to the jobid
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @return Returns a base64 encoded png image corresponding to the jobid
     * @throws IOException
     *             when it is not possible to access to the archive
     */
    @GET
    @Path("jobs/{jobid}/image")
    @Produces("application/json;charset=" + ENCODING)
    public String getJobImage(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws IOException, NotConnectedRestException;

    /**
     * Returns a base64 utf-8 encoded html visualization corresponding to the jobid.
     * This visualization exists when job is created in the web studio
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @return Returns a base64 encoded png image corresponding to the jobid
     * @throws IOException
     *             when it is not possible to access to the archive
     */
    @GET
    @Path("jobs/{jobid}/html")
    @Produces("application/json;charset=" + ENCODING)
    public String getJobHtml(@HeaderParam("sessionid")
                              String sessionId, @PathParam("jobid")
                              String jobId) throws IOException, NotConnectedRestException;

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
    public abstract List<TaskStateData> getJobTaskStates(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    public abstract TaskStateData jobtasks(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException,
            UnknownTaskRestException;

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
    public abstract Serializable valueOftaskresult(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws Throwable;

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
    public abstract byte[] serializedValueOftaskresult(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws Throwable;

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
    public abstract TaskResultData taskresult(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException;

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
    public abstract String tasklog(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException;

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
    public abstract String tasklogErr(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException;

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
    public abstract String tasklogout(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException;

    /**
     *  Returns task server logs
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return task traces from the scheduler and resource manager
    */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/log/server")
    @Produces("application/json")
    public abstract String taskServerLog(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId, @PathParam("taskname")
    String taskname) throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException;

    /**
     * Pauses the job represented by jobid
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/pause")
    @Produces("application/json")
    public abstract boolean pauseJob(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * Resumes the job represented by jobid
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/resume")
    @Produces("application/json")
    public abstract boolean resumeJob(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * Submit job using flat command file
     * @param sessionId valid session id
     * @param commandFileContent content of a command file: line separated native commands
     * @param jobName name of the job to create
     * @param selectionScriptContent content of a selection script, or null
     * @param selectionScriptExtension extension of the selectionscript to determine script engine ("js", "py", "rb")
     * @return Id of the submitted job
     * @throws NotConnectedRestException
     * @throws IOException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     */
    @POST
    @Path("submitflat")
    @Produces("application/json")
    public abstract JobIdData submitFlat(@HeaderParam("sessionid")
    String sessionId, @FormParam("commandFileContent")
    String commandFileContent, @FormParam("jobName")
    String jobName, @FormParam("selectionScriptContent")
    String selectionScriptContent, @FormParam("selectionScriptExtension")
    String selectionScriptExtension) throws NotConnectedRestException, IOException, JobCreationRestException,
            PermissionRestException, SubmissionClosedRestException;

    /**
     * Submits a job to the scheduler 
     * @param sessionId a valid session id
     * @param multipart a form with the job file as form data
     * @return the <code>jobid</code> of the newly created job 
     */
    @POST
    @Path("submit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public abstract JobIdData submit(@HeaderParam("sessionid")
    String sessionId, MultipartFormDataInput multipart) throws IOException, JobCreationRestException,
            NotConnectedRestException, PermissionRestException, SubmissionClosedRestException;

    /**
     * Pushes a file from the local file system into the given DataSpace
     * @param sessionId a valid session id
     * @param spaceName the name of the DataSpace
     * @param filePath the path inside the DataSpace  where to put the file e.g. "/myfolder"
     * @param multipart the form data containing :
     *   - fileName the name of the file that will be created on the DataSpace
     *   - fileContent the content of the file
     * @return true if the transfer succeeded
     * @see org.ow2.proactive.scheduler.common.SchedulerConstants for spaces names
     **/
    @POST
    @Path("dataspace/{spaceName:[a-zA-Z][a-zA-Z_0-9]*}{filePath:.*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public abstract boolean pushFile(@HeaderParam("sessionid")
    String sessionId, @PathParam("spaceName")
    String spaceName, @PathParam("filePath")
    String filePath, MultipartFormDataInput multipart) throws IOException, NotConnectedRestException,
            PermissionRestException;

    /**
     * Either Pulls a file from the given DataSpace to the local file system
     * or list the content of a directory if the path refers to a directory
     * In the case the path to a file is given, the content of this file will be returns as an input stream
     * In the case the path to a directory is given, the input stream returned will be a text stream containing at each line
     * the content of the directory
     * @param sessionId a valid session id
     * @param spaceName the name of the data space involved (GLOBAL or USER)
     * @param filePath the path to the file or directory whose content must be received
     **/
    @GET
    @Path("dataspace/{spaceName:[a-zA-Z][a-zA-Z_0-9]*}{filePath:.*}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public abstract InputStream pullFile(@HeaderParam("sessionid")
    String sessionId, @PathParam("spaceName")
    String spaceName, @PathParam("filePath")
    String filePath) throws IOException, NotConnectedRestException, PermissionRestException;

    /**
     * Deletes a file or recursively delete a directory from the given DataSpace
     * @param sessionId a valid session id
     * @param spaceName the name of the data space involved (GLOBAL or USER)
     * @param filePath the path to the file or directory which must be deleted
     **/
    @DELETE
    @Path("dataspace/{spaceName:[a-zA-Z][a-zA-Z_0-9]*}{filePath:.*}")
    public abstract boolean deleteFile(@HeaderParam("sessionid")
    String sessionId, @PathParam("spaceName")
    String spaceName, @PathParam("filePath")
    String filePath) throws IOException, NotConnectedRestException, PermissionRestException;

    /**
     * terminates the session id <code>sessionId</code>
     * @param sessionId a valid session id
     * @throws NotConnectedRestException if the scheduler cannot be contacted
     * @throws PermissionRestException if you are not authorized to perform the action
     */
    @PUT
    @Path("disconnect")
    @Produces("application/json")
    public abstract void disconnect(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * pauses the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("pause")
    @Produces("application/json")
    public abstract boolean pauseScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * stops the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("stop")
    @Produces("application/json")
    public abstract boolean stopScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * resumes the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("resume")
    @Produces("application/json")
    public abstract boolean resumeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * changes the priority of a job
     * @param sessionId a valid session id 
     * @param jobId the job id 
     * @param priorityName a string representing the name of the priority
     * @throws NotConnectedRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException
     * @throws PermissionRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobAlreadyFinishedRestException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    public abstract void schedulerChangeJobPriorityByName(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("name")
    String priorityName) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException,
            JobAlreadyFinishedRestException;

    /**
     * changes the priority of a job
     * @param sessionId a valid session id 
     * @param jobId the job id 
     * @param priorityValue a string representing the value of the priority
     * @throws NumberFormatException
     * @throws NotConnectedRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException
     * @throws PermissionRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobAlreadyFinishedRestException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    public abstract void schedulerChangeJobPriorityByValue(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("value")
    String priorityValue) throws NumberFormatException, NotConnectedRestException, UnknownJobRestException,
            PermissionRestException, JobAlreadyFinishedRestException;

    /**
     * freezes the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("freeze")
    @Produces("application/json")
    public abstract boolean freezeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * returns the status of the scheduler 
     * @param sessionId a valid session id
     * @return the scheduler status
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @Path("status")
    @Produces("application/json")
    public abstract SchedulerStatusData getSchedulerStatus(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * starts the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("start")
    @Produces("application/json")
    public abstract boolean startScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * kills and shutdowns the scheduler 
     * @param sessionId a valid session id
     * @return true if success, false if not
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("kill")
    @Produces("application/json")
    public abstract boolean killScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * Reconnect a new Resource Manager to the scheduler. 
     * Can be used if the resource manager has crashed.
     * @param sessionId a valid session id
     * @param rmURL the url of the resource manager 
     * @return true if success, false otherwise.
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @POST
    @Path("linkrm")
    @Produces("application/json")
    public abstract boolean linkRm(@HeaderParam("sessionid")
    final String sessionId, @FormParam("rmurl")
    String rmURL) throws NotConnectedRestException, PermissionRestException;

    /**
     * Tests whether or not the user is connected to the ProActive Scheduler
     * @param sessionId the session to test
     * @return true if the user connected to a Scheduler, false otherwise.
     * @throws NotConnectedRestException
     */
    @PUT
    @Path("isconnected")
    @Produces("application/json")
    public abstract boolean isConnected(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException;

    /**
     * login to the scheduler using an form containing 2 fields (username & password)
     *  
     * @param username username
     * @param password password 
     * @return the session id associated to the login
     * @throws LoginException
     * @throws SchedulerRestException
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("login")
    @Produces("application/json")
    public abstract String login(@FormParam("username")
    String username, @FormParam("password")
    String password) throws LoginException, SchedulerRestException;

    /**
     * login to the scheduler using a multipart form
     *  can be used either by submitting 
     *   - 2 fields username & password
     *   - a credential file with field name 'credential'
     * @return the session id associated to this new connection
     * @throws KeyException
     * @throws LoginException
     * @throws SchedulerRestException
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces("application/json")
    public abstract String loginWithCredential(@MultipartForm
    LoginForm multipart) throws KeyException, LoginException, SchedulerRestException;

    /**
     * Users currently connected to the scheduler
     * 
     * @param sessionId the session id associated to this new connection
     * @return list of users
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @GZIP
    @Path("users")
    @Produces("application/json")
    public List<SchedulerUserData> getUsers(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * Users having jobs in the scheduler
     * 
     * @param sessionId the session id associated to this new connection
     * @return list of users
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @GZIP
    @Path("userswithjobs")
    @Produces("application/json")
    public List<SchedulerUserData> getUsersWithJobs(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * returns statistics about the scheduler
     * @param sessionId the session id associated to this new connection
     * @return a string containing the statistics
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @Path("stats")
    @Produces("application/json")
    public abstract Map<String, String> getStatistics(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * returns a string containing some data regarding the user's account
     * @param sessionId the session id associated to this new connection
     * @return a string containing some data regarding the user's account
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @Path("stats/myaccount")
    @Produces("application/json")
    public abstract Map<String, String> getStatisticsOnMyAccount(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * generates a credential file from user provided credentials
     * @return the credential file generated by the scheduler
     * @throws SchedulerRestException
     * @throws LoginException
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("createcredential")
    @Produces("*/*")
    public byte[] getCreateCredential(@MultipartForm
    LoginForm multipart) throws LoginException, SchedulerRestException;

    /**
     * Returns details on job and task execution times for the caller's executions.
     * <p>
     * Only the jobs finished between the start date and the end date will be returned:
     * i.e startDate <= job.finishedTime <= endDate.
     *</p>
     * @param sessionId a valid session id to identify the caller
     * @param startDate must not be null, inclusive
     * @param endDate must not be null, inclusive
     * @return a list of {@link org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobUsageData} objects where job finished times are between start date and end date
     * @throws NotConnectedRestException if user not logger in
     * @throws PermissionRestException if user has insufficient rights
     *
     * @see org.ow2.proactive.scheduler.common.usage.SchedulerUsage#getMyAccountUsage(java.util.Date, java.util.Date)
     */
    @GET
    @Path("usage/myaccount")
    @Produces("application/json")
    List<JobUsageData> getUsageOnMyAccount(@HeaderParam("sessionid")
    String sessionId, @QueryParam("startdate")
    Date startDate, @QueryParam("enddate")
    Date endDate) throws NotConnectedRestException, PermissionRestException;

    /**
     * Returns details on job and task execution times for the caller's executions.
     * <p>
     * Only the jobs finished between the start date and the end date will be returned:
     * i.e startDate <= job.finishedTime <= endDate.
     *</p>
     * @param sessionId a valid session id to identify the caller
     * @param user name
     * @param startDate must not be null, inclusive
     * @param endDate must not be null, inclusive
     * @return a list of {@link org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobUsageData} objects where job finished times are between start date and end date
     * @throws NotConnectedRestException if user not logger in
     * @throws PermissionRestException if user has insufficient rights
     *
     * @see org.ow2.proactive.scheduler.common.usage.SchedulerUsage#getMyAccountUsage(java.util.Date, java.util.Date)
     */
    @GET
    @Path("usage/account")
    @Produces("application/json")
    List<JobUsageData> getUsageOnAccount(@HeaderParam("sessionid")
    String sessionId, @QueryParam("user")
    String user, @QueryParam("startdate")
    Date startDate, @QueryParam("enddate")
    Date endDate) throws NotConnectedRestException, PermissionRestException;

    @GET
    @GZIP
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    String getLiveLogJob(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException,
            LogForwardingRestException, IOException;

    @GET
    @Path("jobs/{jobid}/livelog/available")
    @Produces("application/json")
    int getLiveLogJobAvailable(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException;

    @DELETE
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    boolean deleteLiveLogJob(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobId) throws NotConnectedRestException;

    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/restart")
    boolean restartTask(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobid, @PathParam("taskname")
    String taskname) throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException;

    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/preempt")
    boolean preemptTask(@HeaderParam("sessionid")
    String sessionId, @PathParam("jobid")
    String jobid, @PathParam("taskname")
    String taskname) throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException;
    
    
    /**
     * Validates a job.
     * @param multipart a HTTP multipart form which contains the job-descriptor or the job archive file
     * @return the result of job validation 
     * 
     */
    @POST
    @Path("validate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public abstract JobValidationData validate(MultipartFormDataInput multipart);
}
