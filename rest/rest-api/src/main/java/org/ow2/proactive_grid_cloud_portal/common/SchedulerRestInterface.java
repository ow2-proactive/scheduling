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
package org.ow2.proactive_grid_cloud_portal.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.signal.SignalApiException;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobUsageData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestMapPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.RestPage;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerStatusData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.SchedulerUserData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.LogForwardingRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;


@Path("/scheduler/")
public interface SchedulerRestInterface {

    String defaultStatusFilter = "running;pending;finished";

    /**
     * Returns the url of the scheduler server.
     *
     * @return ProActive url of the scheduler server
     */
    @GET
    @Path("url")
    @Produces("text/plain")
    String getUrl();

    /**
     * Returns the ids of the current jobs under a list of string.
     * 
     * @param sessionId
     *            a valid session id
     * @param index
     *            optional, if a sublist has to be returned the index of the
     *            sublist
     * @param limit
     *            optional, if a sublist has to be returned, the limit of the
     *            sublist
     * @return a list of jobs' ids under the form of a list of string
     */
    @GET
    @Path("jobs")
    @Produces("application/json")
    RestPage<String> jobs(@HeaderParam("sessionid") String sessionId,
            @QueryParam("index") @DefaultValue("-1") int index, @QueryParam("limit") @DefaultValue("-1") int limit)
            throws RestException;

    /**
     * Returns a subset of the scheduler state, including pending, running,
     * finished jobs (in this particular order). each jobs is described using -
     * its id - its owner - the JobInfo class
     * 
     * @param index
     *            optional, if a sublist has to be returned the index of the
     *            sublist
     * @param limit
     *            optional, if a sublist has to be returned, the limit of the
     *            sublist
     * @param sessionId
     *            a valid session id
     * @return a list of UserJobData
     */
    @GET
    @Path("jobsinfo")
    @Produces({ "application/json", "application/xml" })
    RestPage<UserJobData> jobsInfo(@HeaderParam("sessionid") String sessionId,
            @QueryParam("index") @DefaultValue("-1") int index, @QueryParam("limit") @DefaultValue("-1") int limit)
            throws RestException;

    /**
     * Returns a list of jobs info corresponding to the given job IDs (in the same order)
     *
     * @param jobsId
     *            the list of id of the jobs to return, in the same order
     * @param sessionId
     *            a valid session id
     * @return a list of UserJobData
     */
    @GET
    @Path("jobsinfolist")
    @Produces({ "application/json", "application/xml" })
    List<UserJobData> jobsInfoList(@HeaderParam("sessionid") String sessionId,
            @QueryParam("jobsid") List<String> jobsId) throws RestException;

    /**
     * Returns a map containing one entry with the revision id as key and the
     * list of UserJobData as value. each jobs is described using - its id - its
     * owner - the JobInfo class
     * 
     * @param sessionId
     *            a valid session id
     * @param index
     *            optional, if a sublist has to be returned the index of the
     *            sublist
     * @param limit
     *            optional, if a sublist has to be returned, the limit of the
     *            sublist
     * @param myJobs
     *            fetch only the jobs owned by the user making the request
     * @param pending
     *            fetch pending jobs
     * @param running
     *            fetch running jobs
     * @param finished
     *            fetch finished jobs
     * @param sortParams
     *            string containing sort directives. It must contain a comma separated list of sort parameters.
     *            A sort parameter can contain ID,STATE,OWNER,PRIORITY,NAME,SUBMIT_TIME,START_TIME,IN_ERROR_TIME,FINISH_TIME,TOTAL_TASKS,
     *            PENDING_TASKS,RUNNING_TASKS,FINISHED_TASKS,FAILED_TASKS,FAULTY_TASKS,IN_ERROR_TASKS
     *            Each parameter must end with _d for descending order or _a for ascending.
     *            Default value is: "STATE_a,ID_d"
     * @return a map containing one entry with the revision id as key and the
     *         list of UserJobData as value.
     */
    @GET
    @GZIP
    @Path("revisionjobsinfo")
    @Produces({ "application/json", "application/xml" })
    RestMapPage<Long, ArrayList<UserJobData>> revisionAndJobsInfo(@HeaderParam("sessionid") String sessionId,
            @QueryParam("index") @DefaultValue("-1") int index, @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("myjobs") @DefaultValue("false") boolean myJobs,
            @QueryParam("pending") @DefaultValue("true") boolean pending,
            @QueryParam("running") @DefaultValue("true") boolean running,
            @QueryParam("finished") @DefaultValue("true") boolean finished, @QueryParam("sortParams") String sortParams)
            throws RestException;

    /**
     * Returns the revision number of the scheduler state
     * 
     * @param sessionId
     *            a valid session id.
     * @return the revision of the scheduler state
     */
    @GET
    @Path("state/revision")
    @Produces({ "application/json", "application/xml" })
    long schedulerStateRevision(@HeaderParam("sessionid") String sessionId) throws NotConnectedRestException;

    /**
     * Returns a JobState of the job identified by the id <code>jobid</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job to retrieve
     */
    @GET
    @Path("jobs/{jobid}")
    @Produces({ "application/json", "application/xml" })
    JobStateData listJobs(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * Returns the job result associated to the job referenced by the id
     * <code>jobid</code>
     * 
     * @param sessionId
     *            a valid session id
     * @return the job result of the corresponding job
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result")
    @Produces("application/json")
    JobResultData jobResult(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * Returns the job results map associated to the job referenced by the id
     * <code>jobid</code>
     *
     * @param sessionId
     *            a valid session id
     * @return the job results map of the corresponding job
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/resultmap")
    @Produces("application/json")
    Map<String, String> jobResultMap(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * @param sessionId a valid session id
     * @param jobsId the list of job ids
     * @return a map which contains job id as key and resultMap of this job as value
     */
    @GET
    @GZIP
    @Path("jobs/resultmap")
    @Produces("application/json")
    Map<Long, Map<String, String>> jobResultMaps(@HeaderParam("sessionid") String sessionId,
            @QueryParam("jobsid") List<String> jobsId) throws RestException;

    /**
     * Returns the job info associated to the job referenced by the id
     * <code>jobid</code>
     * 
     * @param sessionId
     *            a valid session id
     * @return the job info of the corresponding job
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/info")
    @Produces("application/json")
    JobInfoData jobInfo(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * Returns all the task results of this job as a map whose the key is the
     * name of the task and its task result.<br>
     * If the result cannot be instantiated, the content is replaced by the
     * string 'Unknown value type'. To get the serialized form of a given
     * result, one has to call the following restful service
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            a job id
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result/value")
    @Produces("application/json")
    Map<String, String> jobResultValue(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * Delete a job
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job to delete
     * @return true if success, false if the job not yet finished (not removed,
     *         kill the job then remove it)
     * 
     */
    @DELETE
    @Path("jobs/{jobid}")
    @Produces("application/json")
    boolean removeJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * @param jobsId to remove
     * @return true if all jobs with jobIds were removed, otherwise false
     */
    @DELETE
    @Path("jobs")
    @Produces("application/json")
    boolean removeJobs(@HeaderParam("sessionid") String sessionId, @QueryParam("jobsid") List<String> jobsId)
            throws RestException;

    /**
     * Returns job server logs
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return job traces from the scheduler and resource manager
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/log/server")
    @Produces("text/plain")
    String jobServerLog(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * Kill the job represented by jobId.<br>
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job to kill.
     * @return true if success, false if not.
     */
    @PUT
    @Path("jobs/{jobid}/kill")
    @Produces("application/json")
    boolean killJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) throws RestException;

    /**
     * Kill multiple jobs
     * @param sessionId a valid session id
     * @param jobsId job ids to kill
     * @return true if all requested jobs were killed
     */
    @PUT
    @Path("jobs/kill")
    @Produces("application/json")
    boolean killJobs(@HeaderParam("sessionid") String sessionId, @QueryParam("jobsid") List<String> jobsId)
            throws RestException;

    /**
     * Returns a list of the name of the tasks belonging to job
     * <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            jobid one wants to list the tasks' name
     * @return a list of tasks' name
     */
    @GET
    @Path("jobs/{jobid}/tasks")
    @Produces("application/json")
    RestPage<String> getTasksNames(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * Returns a list of the name of the tasks belonging to job
     * <code>jobId</code> with pagination
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            jobid one wants to list the tasks' name
     * @param offset
     *            the number of the first task to fetch
     * @param limit
     *            the number of the last task to fetch (non inclusive)
     * @return the list of task ids with the total number of them
     */
    @GET
    @Path("jobs/{jobid}/tasks/paginated")
    @Produces("application/json")
    RestPage<String> getTasksNamesPaginated(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("-1") int limit) throws RestException;

    /**
     * Returns a list of the name of the tasks belonging to job
     * <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            jobid one wants to list the tasks' name
     * @param taskTag
     *            the tag used to filter the tasks.
     * @return a list of task ids filtered by the tag and with the total number
     *         of tasks ids
     */
    @GET
    @Path("jobs/{jobid}/tasks/tag/{tasktag}")
    @Produces("application/json")
    RestPage<String> getJobTasksIdsByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag) throws RestException;

    /**
     * Returns a list of the name of the tasks belonging to job
     * <code>jobId</code> (with pagination)
     * 
     * @param sessionId
     *            a valid session id.
     * @param jobId
     *            the job id.
     * @param taskTag
     *            the tag used to filter the tasks.
     * @param offset
     *            the number of the first task to fetch
     * @param limit
     *            the number of the last task to fetch (non inclusive)
     * @return a list of task ids filtered by the tag and with the total number
     *         of tasks ids
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/paginated")
    @Produces("application/json")
    RestPage<String> getJobTasksIdsByTagPaginated(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit)
            throws RestException;

    /**
     * Returns all tasks name regarding the given parameters (decoupled from the
     * associated jobs). The result is paginated using the optional
     * <code>offset</code> and <code>limit</code> parameters. If those
     * parameters are not specified, the following values will be used: [0,
     * DEFAULT_VALUE[ The DEFAULT_VALUE can be set in the scheduler config file
     * as the <code>pa.scheduler.tasks.page.size</code> parameter.
     * 
     * @param sessionId
     *            a valid session id.
     * @param from
     *            the scheduled date to which we start fetching tasks. The
     *            format is in Epoch time.
     * @param to
     *            the end scheduled end date to stop fetching tasks. The format
     *            is in Epoch time.
     * @param mytasks
     *            <code>True</code> if you want to fetch only the user's tasks.
     *            Default value is <code>False</code>.
     * @param statusFilter
     *            String contains list of aggregated task statuses, like "Current", "Past", etc.
     *            These values joined by semi colon.
     * @param offset
     *            the index of the first task to fetch (for pagination).
     * @param limit
     *            the index of the last (excluded) task to fetch (for
     *            pagination).
     * @return a list of task ids and the total number of tasks ids
     */
    @GET
    @GZIP
    @Path("tasks")
    @Produces("application/json")
    RestPage<String> getTaskIds(@HeaderParam("sessionid") String sessionId,
            @QueryParam("from") @DefaultValue("0") long from, @QueryParam("to") @DefaultValue("0") long to,
            @QueryParam("mytasks") @DefaultValue("false") boolean mytasks,
            @QueryParam("statusFilter") @DefaultValue(value = defaultStatusFilter) String statusFilter,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit)
            throws RestException;

    /**
     * Returns all tasks name regarding the given parameters (decoupled from the
     * associated jobs). The result is paginated using the optional
     * <code>offset</code> and <code>limit</code> parameters. If those
     * parameters are not specified, the following values will be used: [0,
     * DEFAULT_VALUE[ The DEFAULT_VALUE can be set in the scheduler config file
     * as the <code>pa.scheduler.tasks.page.size</code> parameter.
     * 
     * @param sessionId
     *            a valid session id.
     * @param taskTag
     *            tag to filter the tasks. The tag should be complete as the
     *            criteria is strict.
     * @param from
     *            the scheduled date to which we start fetching tasks. The
     *            format is in Epoch time.
     * @param to
     *            the end scheduled end date to stop fetching tasks. The format
     *            is in Epoch time.
     * @param mytasks
     *            <code>True</code> if you want to fetch only the user's tasks.
     *            Default value is <code>False</code>.
     * @param statusFilter
     *            String contains list of aggregated task statuses, like "Current", "Past", etc.
     *            These values joined by semi colon.
     * @param offset
     *            the index of the first task to fetch (for pagination).
     * @param limit
     *            the index of the last (excluded) task to fetch (for
     *            pagination).
     * @return a list of task ids and the total number of tasks ids
     */
    @GET
    @GZIP
    @Path("tasks/tag/{tasktag}")
    @Produces("application/json")
    RestPage<String> getTaskIdsByTag(@HeaderParam("sessionid") String sessionId, @PathParam("tasktag") String taskTag,
            @QueryParam("from") @DefaultValue("0") long from, @QueryParam("to") @DefaultValue("0") long to,
            @QueryParam("mytasks") @DefaultValue("false") boolean mytasks,
            @QueryParam("statusFilter") @DefaultValue(value = defaultStatusFilter) String statusFilter,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit)
            throws RestException;

    /**
     * Returns a list of the tags of the tasks belonging to job
     * <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            jobid one wants to list the tasks' tags
     * @return a list of tasks' name
     */
    @GET
    @Path("jobs/{jobid}/tasks/tags")
    @Produces("application/json")
    List<String> getJobTaskTags(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * Returns a list of the tags of the tasks belonging to job
     * <code>jobId</code> and filtered by a prefix pattern
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            jobid one wants to list the tasks' tags
     * @param prefix
     *            the prefix used to filter tags
     * @return a list of tasks' name
     */
    @GET
    @Path("jobs/{jobid}/tasks/tags/startsWith/{prefix}")
    @Produces("application/json")
    List<String> getJobTaskTagsPrefix(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("prefix") String prefix) throws RestException;

    /**
     * Returns a base64 utf-8 encoded html visualization corresponding to the
     * jobid. This visualization exists when job is created in the web studio
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
    @Produces("application/json;charset=utf-8")
    String getJobHtml(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws IOException, NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     *
     * @param sessionId
     *          a valid session id
     * @param jobId
     *          job id which corresponds to already submitted job
     * @return xml representation of the submitted job
     * @throws UnknownJobRestException if <code>jobId</code> does not correspond to any job
     * @throws PermissionRestException if current user does not have rights to access job with <code>jobId</code>
     */
    @GET
    @Path("jobs/{jobid}/xml")
    @Produces("application/xml")
    String getJobContent(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * Returns a list of taskState
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @return a list of task' states of the job <code>jobId</code> and the
     *         total number
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates")
    @Produces("application/json")
    RestPage<TaskStateData> getJobTaskStates(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws RestException;

    /**
     * Returns a list of taskStates, only tasks with visualization activated
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @return a list of task' states with visualization activated
     */
    @GET
    @Path("jobs/{jobid}/taskstates/visualization")
    @Produces("application/json")
    List<TaskStateData> getJobTaskStatesWithVisualization(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws RestException;

    /**
     * Returns a list of taskState with pagination
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @param offset
     *            the index of the first TaskState to return
     * @param limit
     *            the index (non inclusive) of the last TaskState to return
     * @return a list of task' states of the job <code>jobId</code> and the
     *         total number
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates/paginated")
    @Produces("application/json")
    RestPage<TaskStateData> getJobTaskStatesPaginated(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("50") int limit) throws RestException;

    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates/filtered/paginated")
    @Produces("application/json")
    RestPage<TaskStateData> getJobTaskStatesFilteredPaginated(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("50") int limit,
            @QueryParam("statusFilter") @DefaultValue("") String statusFilter) throws RestException;

    /**
     * Returns a list of taskState of the tasks filtered by a given tag.
     * 
     * @param sessionId
     *            a valid session id.
     * @param jobId
     *            the job id.
     * @param taskTag
     *            the tag used to filter the tasks.
     * @return a list of task' states of the job <code>jobId</code> filtered by
     *         a given tag and the total number
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates/{tasktag}")
    @Produces("application/json")
    RestPage<TaskStateData> getJobTaskStatesByTag(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag) throws RestException;

    /**
     * Returns a list of taskState of the tasks filtered by a given tag and
     * paginated.
     * 
     * @param sessionId
     *            a valid session id.
     * @param jobId
     *            the job id.
     * @param taskTag
     *            the tag used to filter the tasks.
     * @param offset
     *            the number of the first task to fetch
     * @param limit
     *            the number of the last task to fetch (non inclusive)
     * @return a list of task' states of the job <code>jobId</code> filtered by
     *         a given tag, and the total number
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates/{tasktag}/paginated")
    @Produces("application/json")
    RestPage<TaskStateData> getJobTaskStatesByTagPaginated(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("50") int limit)
            throws RestException;

    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates/{tasktag}/{statusFilter}/paginated")
    @Produces("application/json")
    RestPage<TaskStateData> getJobTaskStatesByTagByStatusPaginated(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("50") int limit, @PathParam("tasktag") String taskTag,
            @PathParam("statusFilter") String statusFilter) throws RestException;

    /**
     * Returns a paginated list of <code>TaskStateData</code> regarding the
     * given parameters (decoupled from the associated jobs). The result is
     * paginated using the optional <code>offset</code> and <code>limit</code>
     * parameters. If those parameters are not specified, the following values
     * will be used: [0, DEFAULT_VALUE[ The DEFAULT_VALUE can be set in the
     * scheduler config file as the <code>pa.scheduler.tasks.page.size</code>
     * parameter.
     * 
     * @param sessionId
     *            a valid session id.
     * @param from
     *            the scheduled date to which we start fetching tasks. The
     *            format is in Epoch time.
     * @param to
     *            the end scheduled end date to stop fetching tasks. The format
     *            is in Epoch time.
     * @param mytasks
     *            <code>True</code> if you want to fetch only the user's tasks.
     *            Default value is <code>False</code>.
     * @param statusFilter
     *            String contains list of aggregated task statuses, like "Current", "Past", etc.
     *            These values joined by semi colon.
     * @param offset
     *            the index of the first task to fetch (for pagination).
     * @param limit
     *            the index of the last (excluded) task to fetch (for
     *            pagination).
     * @return a list of <code>TaskStateData</code> and the total number of
     *         them.
     */
    @GET
    @GZIP
    @Path("taskstates")
    @Produces("application/json")
    RestPage<TaskStateData> getTaskStates(@HeaderParam("sessionid") String sessionId,
            @QueryParam("from") @DefaultValue("0") long from, @QueryParam("to") @DefaultValue("0") long to,
            @QueryParam("mytasks") @DefaultValue("false") boolean mytasks,
            @QueryParam("statusFilter") @DefaultValue(value = defaultStatusFilter) String statusFilter,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("sortparameters") SortSpecifierContainer sortParams) throws RestException;

    /**
     * Returns a paginated list of <code>TaskStateData</code> regarding the
     * given parameters (decoupled from the associated jobs). The result is
     * paginated using the optional <code>offset</code> and <code>limit</code>
     * parameters. If those parameters are not specified, the following values
     * will be used: [0, DEFAULT_VALUE[ The DEFAULT_VALUE can be set in the
     * scheduler config file as the <code>pa.scheduler.tasks.page.size</code>
     * parameter.
     * 
     * @param sessionId
     *            a valid session id.
     * @param taskTag
     *            tag to filter the tasks. The tag should be complete as the
     *            criteria is strict.
     * @param from
     *            the scheduled date to which we start fetching tasks. The
     *            format is in Epoch time.
     * @param to
     *            the end scheduled end date to stop fetching tasks. The format
     *            is in Epoch time.
     * @param mytasks
     *            <code>True</code> if you want to fetch only the user's tasks.
     *            <code>False</code> will fetch everything.
     * @param statusFilter
     *            String contains list of aggregated task statuses, like "Current", "Past", etc.
     *            These values joined by semi colon.
     * @param offset
     *            the index of the first task to fetch (for pagination).
     * @param limit
     *            the index of the last (excluded) task to fetch (for
     *            pagination).
     * @return a list of <code>TaskStateData</code> and the total number of
     *         them.
     */
    @GET
    @GZIP
    @Path("taskstates/tag/{tasktag}")
    @Produces("application/json")
    RestPage<TaskStateData> getTaskStatesByTag(@HeaderParam("sessionid") String sessionId,
            @PathParam("tasktag") String taskTag, @QueryParam("from") @DefaultValue("0") long from,
            @QueryParam("to") @DefaultValue("0") long to, @QueryParam("mytasks") @DefaultValue("false") boolean mytasks,
            @QueryParam("statusFilter") @DefaultValue(value = defaultStatusFilter) String statusFilter,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("sortparameters") SortSpecifierContainer sortParams) throws RestException;

    /**
     * Returns full logs generated by tasks in job.
     *
     * Multiple task attempts are included in the full logs.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return all the logs generated by the tasks, empty if task is not
     *         finished or has been killed
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/log/full")
    @Produces("text/plain")
    InputStream jobFullLogs(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @QueryParam("sessionid") String session) throws RestException, IOException;

    /**
     * Returns all the logs generated by the job (either stdout and stderr)
     *
     * When tasks inside this jobs have several execution attempts, only last attempt logs with be contained in the job log.
     *
     * Multiple attempts are available using "jobs/{jobid}/log/full" rest endpoint.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return all the logs generated by the job (either stdout and stderr) or
     *         an empty string if the result is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/result/log/all")
    @Produces("text/plain")
    String jobLogs(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId) throws RestException;

    /**
     * Return the task state of the task <code>taskname</code> of the job
     * <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the task state of the task <code>taskname</code> of the job
     *         <code>jobId</code>
     */
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}")
    @Produces("application/json")
    TaskStateData jobTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Returns the value of the task result of task <code>taskName</code> of the
     * job <code>jobId</code> <strong>the result is deserialized before sending
     * to the client, if the class is not found the content is replaced by the
     * string 'Unknown value type' </strong>. To get the serialized form of a
     * given result, one has to call the following restful service
     * jobs/{jobid}/tasks/{taskname}/result/serializedvalue
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the value of the task result
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/value")
    @Produces("*/*")
    Serializable valueOfTaskResult(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws Throwable;

    /**
     * Returns the values of a set of tasks of the job <code>jobId</code>
     * filtered by a given tag. <strong>The result is deserialized before
     * sending to the client, if the class is not found the content is replaced
     * by the string 'Unknown value type' </strong>. To get the serialized form
     * of a given result, one has to call the following restful service
     * jobs/{jobid}/tasks/tag/{tasktag}/result/serializedvalue
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskTag
     *            the tag used to filter the tasks.
     * @return the value of the task result
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/value")
    @Produces("application/json")
    Map<String, String> valueOfTaskResultByTag(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag) throws Throwable;

    /**
     * Returns the metadata of the task result of task <code>taskName</code> of the
     * job <code>jobId</code>.
     *
     * Metadata is a map containing additional information associated with a result. For example a file name if the result represents a file.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the metadata of the task result
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/metadata")
    @Produces("*/*")
    Map<String, String> metadataOfTaskResult(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("taskname") String taskname) throws Throwable;

    /**
     * Returns the metadata of the task result of task <code>taskName</code> of the
     * job <code>jobId</code>filtered by a given tag.
     * <p>
     * Metadata is a map containing additional information associated with a result. For example a file name if the result represents a file.
     *
     * @param sessionId a valid session id
     * @param jobId     the id of the job
     * @param taskTag   the tag used to filter the tasks.
     * @return a map containing for each task entry, the metadata of the task result
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/metadata")
    @Produces("application/json")
    Map<String, Map<String, String>> metadataOfTaskResultByTag(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag) throws Throwable;

    /**
     * Returns the name of the tasks, which has precious result property set on,
     * and they all releated to the job with <code>jobId</code>.
     *
     * @param sessionId a valid session id
     * @param jobId     the id of the job
     * @return a list of task names
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/results/precious/metadata")
    @Produces("application/json")
    List<String> getPreciousTaskName(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws RestException;

    /**
     * @param sessionId a valid session id
     * @param jobsId the list of job ids
     * @return a map where key is a job id, and value is list of precious tasks names of this job
     **/
    @GET
    @GZIP
    @Path("jobs/result/precious/metadata")
    @Produces("application/json")
    Map<Long, List<String>> getPreciousTaskNames(@HeaderParam("sessionid") String sessionId,
            @QueryParam("jobsid") List<String> jobsId) throws RestException;

    /**
     * Returns the value of the task result of the task <code>taskName</code> of
     * the job <code>jobId</code> This method returns the result as a byte array
     * whatever the result is.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the value of the task result as a byte array.
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/serializedvalue")
    @Produces("*/*")
    byte[] serializedValueOfTaskResult(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws Throwable;

    /**
     * Returns the values of a set of tasks of the job <code>jobId</code>
     * filtered by a given tag. This method returns the result as a byte array
     * whatever the result is.
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskTag
     *            the tag used to filter the tasks.
     * @return the values of the set of tasks result as a byte array, indexed by
     *         the readable name of the task.
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/serializedvalue")
    @Produces("application/json")
    Map<String, byte[]> serializedValueOfTaskResultByTag(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag) throws Throwable;

    /**
     * Returns the task result of the task <code>taskName</code> of the job
     * <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the task result of the task <code>taskName</code>
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result")
    @Produces("application/json")
    TaskResultData taskResult(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Returns the task results of the set of task filtered by a given tag and
     * owned by the job <code>jobId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskTag
     *            the tag used to filter the tasks.
     * @return the task results of the set of tasks filtered by the given tag.
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result")
    @Produces("application/json")
    List<TaskResultData> taskResultByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag) throws RestException;

    /**
     * Returns all the logs generated by the task (either stdout and stderr).
     *
     * Multiple task execution attempts are included.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return all the logs generated by the task (either stdout and stderr) or
     *         an empty string if the result is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/all")
    @Produces("text/plain")
    String taskLog(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Returns all the logs generated by a set of the tasks (either stdout and
     * stderr) filtered by a tag.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskTag
     *            the tag used to filter the tasks.
     * @return the list of logs generated by each filtered task (either stdout
     *         and stderr) or an empty string if the result is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/log/all")
    @Produces("text/plain")
    String taskLogByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag) throws RestException;

    /**
     * Returns the standard error output (stderr) generated by the task
     *
     * Multiple task execution attempts are included.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the stderr generated by the task or an empty string if the result
     *         is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/err")
    @Produces("text/plain")
    String taskLogErr(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Returns the list of standard error outputs (stderr) generated by a set of
     * tasks filtered by a given tag.
     *
     * Multiple task execution attempts are NOT included (these logs only contain last execution).
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskTag
     *            the tag used to filter the tasks
     * @return the list of stderr generated by the set of tasks filtered by the
     *         given tag or an empty string if the result is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/log/err")
    @Produces("text/plain")
    String taskLogErrByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag) throws RestException;

    /**
     * Returns the standard output (stdout) generated by the task
     *
     * Multiple task execution attempts are included.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return the stdout generated by the task or an empty string if the result
     *         is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/out")
    @Produces("text/plain")
    String taskLogout(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Returns the standard output (stdout) generated by a set of tasks filtered
     * by a given tag.
     *
     * Multiple task execution attempts are NOT included (these logs only contain last execution).
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskTag
     *            the tag used to filter the tasks.
     * @return the stdout generated by the task or an empty string if the result
     *         is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/log/out")
    @Produces("text/plain")
    String taskLogoutByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag) throws RestException;

    /**
     * Returns full logs generated by the task from user data spaces if task was
     * run using the precious logs option. Otherwise, logs are retrieved from
     * the database. In this last case they may be truncated.
     *
     * Multiple task execution attempts are included.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return all the logs generated by the task (either stdout and stderr) or
     *         an empty string if the result is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/full")
    @Produces("text/plain")
    InputStream taskFullLogs(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname, @QueryParam("sessionid") String session)
            throws RestException, IOException;

    /**
     * Returns task server logs
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskname
     *            the name of the task
     * @return task traces from the scheduler and resource manager
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/log/server")
    @Produces("text/plain")
    String taskServerLog(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Returns server logs for a set of tasks filtered by a given tag.
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @param taskTag
     *            the tag used to filter the tasks in the job.
     * @return task traces from the scheduler and resource manager
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/log/server")
    @Produces("text/plain")
    String taskServerLogByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag) throws RestException;

    /**
     * Pauses the job represented by jobid
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/pause")
    @Produces("application/json")
    boolean pauseJob(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId) throws RestException;

    /**
     * Restart all tasks in error in the job represented by jobid
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/restartAllInErrorTasks")
    @Produces("application/json")
    boolean restartAllInErrorTasks(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId) throws RestException;

    /**
     * Resumes the job represented by jobid
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return true if success, false if not
     */
    @PUT
    @Path("jobs/{jobid}/resume")
    @Produces("application/json")
    boolean resumeJob(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId) throws RestException;

    /**
     * Submit job using flat command file
     * 
     * @param sessionId
     *            valid session id
     * @param commandFileContent
     *            content of a command file: line separated native commands
     * @param jobName
     *            name of the job to create
     * @param selectionScriptContent
     *            content of a selection script, or null
     * @param selectionScriptExtension
     *            extension of the selectionscript to determine script engine
     *            ("js", "py", "rb")
     * @return Id of the submitted job
     */
    @POST
    @Path("submitflat")
    @Produces("application/json")
    JobIdData submitFlat(@HeaderParam("sessionid") String sessionId,
            @FormParam("commandFileContent") String commandFileContent, @FormParam("jobName") String jobName,
            @FormParam("selectionScriptContent") String selectionScriptContent,
            @FormParam("selectionScriptExtension") String selectionScriptExtension) throws IOException, RestException;

    /**
     * Submits a job to the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @param pathSegment
     *            variables of the workflow
     * @param multipart
     *            a form with the job file as form data
     * @param contextInfos
     *            the context informations (generic parameters,..)
     * @return the <code>jobid</code> of the newly created job
     */
    @POST
    @Path("{path:submit}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    JobIdData submit(@HeaderParam("sessionid") String sessionId, @PathParam("path") PathSegment pathSegment,
            MultipartFormDataInput multipart, @Context UriInfo contextInfos) throws JobCreationRestException,
            NotConnectedRestException, PermissionRestException, SubmissionClosedRestException, IOException;

    /**
     * Re-submit a job to the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            job id of the already submitted job
     * @param pathSegment
     *            variables of the workflow
     * @return the <code>jobid</code> of the newly created job
     * @throws PermissionRestException if user does not have rights to access job with <code>jobId</code>
     */
    @GET
    @Path("jobs/{jobid}/{path:resubmit}")
    @Produces("application/json")
    JobIdData reSubmit(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("path") PathSegment pathSegment, @Context UriInfo contextInfos)
            throws IOException, RestException;

    /**
     * Re-submit a list of jobs to the scheduler
     *
     * @param sessionId
     *          a valid session id
     * @param jobsId
     *          a list of job ids of already submitted jobs
     * @return a list of <code>jobid</code> of the newly created jobs. If a job submission fails, it will be discarded with a log message.
     * @throws RestException thrown when not connected or similar errors.
     */
    @POST
    @Path("jobs/resubmit")
    @Produces("application/json")
    List<JobIdData> reSubmitAll(@HeaderParam("sessionid") String sessionId, @QueryParam("jobsid") List<String> jobsId)
            throws RestException;

    /**
     * submit a planned workflow
     *
     * @param sessionId user's session in the header
     * @param pathSegment path param going to be transferred to the variables
     * @param jobContentXmlString job content in xml string
     * @return true if the submission is done sucessfully, false otherwise
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Path("{path:plannings}")
    @Produces("application/json")
    String submitPlannings(@HeaderParam("sessionid") String sessionId, @PathParam("path") PathSegment pathSegment,
            Map<String, String> jobContentXmlString) throws JobCreationRestException, NotConnectedRestException,
            PermissionRestException, SubmissionClosedRestException, IOException;

    /**
     * Submits a workflow to the scheduler from a workflow URL, creating hence a
     * new job resource.
     *
     * @param sessionId
     *            a valid session id
     * @param url
     *            url to the workflow content
     * @param pathSegment
     *            variables of the workflow
     * @param contextInfos
     *            the context informations (generic parameters,..)
     * @return the <code>jobid</code> of the newly created job
     */
    @POST
    @Path("{path:jobs}")
    @Produces("application/json")
    JobIdData submitFromUrl(@HeaderParam("sessionid") String sessionId, @HeaderParam("link") String url,
            @PathParam("path") PathSegment pathSegment, @Context UriInfo contextInfos) throws JobCreationRestException,
            NotConnectedRestException, PermissionRestException, SubmissionClosedRestException, IOException;

    /**
     * Submits a workflow to the scheduler from a workflow URL, creating hence a
     * new job resource.
     *
     * @param sessionId
     *            a valid session id
     * @param url
     *            url to the workflow content
     * @param pathSegment
     *            variables of the workflow
     * @param multipart
     *            a form with the variables of the workflow
     * @param contextInfos
     *            the context informations (generic parameters,..)
     * @return the <code>jobid</code> of the newly created job
     */
    @POST
    @Path("{path:jobs}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    JobIdData submitFromUrl(@HeaderParam("sessionid") String sessionId, @HeaderParam("link") String url,
            @PathParam("path") PathSegment pathSegment, MultipartFormDataInput multipart, @Context UriInfo contextInfos)
            throws JobCreationRestException, NotConnectedRestException, PermissionRestException,
            SubmissionClosedRestException, IOException;

    /**
     * Pushes a file from the local file system into the given DataSpace
     * 
     * @param sessionId
     *            a valid session id
     * @param spaceName
     *            the name of the DataSpace
     * @param filePath
     *            the path inside the DataSpace where to put the file e.g.
     *            "/myfolder"
     * @param multipart
     *            the form data containing : - fileName the name of the file
     *            that will be created on the DataSpace - fileContent the
     *            content of the file
     * @return true if the transfer succeeded
     **/
    @POST
    @Path("dataspace/{spaceName:[a-zA-Z][a-zA-Z_0-9]*}{filePath:.*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    boolean pushFile(@HeaderParam("sessionid") String sessionId, @PathParam("spaceName") String spaceName,
            @PathParam("filePath") String filePath, MultipartFormDataInput multipart)
            throws IOException, NotConnectedRestException, PermissionRestException;

    /**
     * Either Pulls a file from the given DataSpace to the local file system or
     * list the content of a directory if the path refers to a directory In the
     * case the path to a file is given, the content of this file will be
     * returns as an input stream In the case the path to a directory is given,
     * the input stream returned will be a text stream containing at each line
     * the content of the directory
     * 
     * @param sessionId
     *            a valid session id
     * @param spaceName
     *            the name of the data space involved (GLOBAL or USER)
     * @param filePath
     *            the path to the file or directory whose content must be
     *            received
     **/
    @GET
    @Path("dataspace/{spaceName:[a-zA-Z][a-zA-Z_0-9]*}{filePath:.*}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    InputStream pullFile(@HeaderParam("sessionid") String sessionId, @PathParam("spaceName") String spaceName,
            @PathParam("filePath") String filePath)
            throws IOException, NotConnectedRestException, PermissionRestException;

    /**
     * Deletes a file or recursively delete a directory from the given DataSpace
     * 
     * @param sessionId
     *            a valid session id
     * @param spaceName
     *            the name of the data space involved (GLOBAL or USER)
     * @param filePath
     *            the path to the file or directory which must be deleted
     **/
    @DELETE
    @Path("dataspace/{spaceName:[a-zA-Z][a-zA-Z_0-9]*}{filePath:.*}")
    @Produces("application/json")
    boolean deleteFile(@HeaderParam("sessionid") String sessionId, @PathParam("spaceName") String spaceName,
            @PathParam("filePath") String filePath)
            throws IOException, NotConnectedRestException, PermissionRestException;

    /**
     * terminates the session id <code>sessionId</code>
     * 
     * @param sessionId
     *            a valid session id
     * @throws NotConnectedRestException
     *             if the scheduler cannot be contacted
     * @throws PermissionRestException
     *             if you are not authorized to perform the action
     */
    @PUT
    @Path("disconnect")
    @Produces("application/json")
    void disconnect(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * pauses the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     */
    @PUT
    @Path("pause")
    @Produces("application/json")
    boolean pauseScheduler(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * stops the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     */
    @PUT
    @Path("stop")
    @Produces("application/json")
    boolean stopScheduler(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * resumes the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     */
    @PUT
    @Path("resume")
    @Produces("application/json")
    boolean resumeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * changes the priority of a job
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @param priorityName
     *            a string representing the name of the priority
     */
    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    void schedulerChangeJobPriorityByName(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("name") String priorityName) throws RestException;

    /**
     * changes the priority of a job
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @param priorityValue
     *            a string representing the value of the priority
     */
    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    void schedulerChangeJobPriorityByValue(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("value") String priorityValue) throws RestException;

    /**
     * freezes the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     */
    @PUT
    @Path("freeze")
    @Produces("application/json")
    boolean freezeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * returns the status of the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return the scheduler status
     */
    @GET
    @Path("status")
    @Produces("application/json")
    SchedulerStatusData getSchedulerStatus(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * starts the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     */
    @PUT
    @Path("start")
    @Produces("application/json")
    boolean startScheduler(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * kills the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false if not
     */
    @PUT
    @Path("kill")
    @Produces("application/json")
    boolean killScheduler(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * shutdown the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return true if success, false if not
     */
    @PUT
    @Path("shutdown")
    @Produces("application/json")
    boolean shutdownScheduler(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * Reconnect a new Resource Manager to the scheduler. Can be used if the
     * resource manager has crashed.
     * 
     * @param sessionId
     *            a valid session id
     * @param rmURL
     *            the url of the resource manager
     * @return true if success, false otherwise.
     */
    @POST
    @Path("linkrm")
    @Produces("application/json")
    boolean linkRm(@HeaderParam("sessionid")
    final String sessionId, @FormParam("rmurl") String rmURL) throws RestException;

    /**
     * Tests whether or not the user is connected to the ProActive Scheduler
     * 
     * @param sessionId
     *            the session to test
     * @return true if the user connected to a Scheduler, false otherwise.
     * @throws NotConnectedRestException
     */
    @GET
    @Path("isconnected")
    @Produces("application/json")
    boolean isConnected(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException;

    /**
     * login to the scheduler using an form containing 2 fields (username and
     * password)
     * 
     * @param username
     *            username
     * @param password
     *            password
     * @return the session id associated to the login
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("login")
    @Produces("application/json")
    String login(@FormParam("username") String username, @FormParam("password") String password)
            throws LoginException, SchedulerRestException;

    /**
     * Renew the session identified by the given {@code sessionId} if it exists
     * or create a new session.
     *
     * @param username
     *            username
     * @param password
     *            password
     * @param sessionId
     *            session id identifying a session to renew.
     * @return the new session id to use.
     */
    @PUT
    @Path("session")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json")
    String loginOrRenewSession(@HeaderParam("sessionid") String sessionId, @FormParam("username") String username,
            @FormParam("password") String password)
            throws SchedulerRestException, LoginException, NotConnectedRestException;

    /**
     * Renew the session identified by the given {@code sessionId} if it exists
     * or create a new session.
     * <p>
     * login to the scheduler using a multipart form can be used either by
     * submitting - 2 fields: username and password - a credential file with
     * field name 'credential'
     *
     * @param multipart
     *            password
     * @param sessionId
     *            session id identifying a session to renew.
     * @return the new session id to use.
     */
    @PUT
    @Path("session")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    String loginOrRenewSession(@HeaderParam("sessionid") String sessionId, @MultipartForm LoginForm multipart)
            throws KeyException, SchedulerRestException, LoginException, NotConnectedRestException;

    /**
     * Get the login string associated to the {@code sessionId} if it exists
     * 
     * In case that the given sessionId doesn't have an associated login (session id expired, or invalid), 
     * this endpoint will return an empty string
     * 
     * @param sessionId with which the endpoint is going to look for the login value
     * @return the associated login value or an empty string
     */
    @GET
    @Path("logins/sessionid/{sessionId}")
    @Produces("application/json")
    String getLoginFromSessionId(@PathParam("sessionId") String sessionId);

    /**
     * Get a UserData object associated to the user connected with the {@code sessionId}
     *
     * In case that the given sessionId doesn't have an associated login (session id expired, or invalid),
     * this endpoint will return null
     *
     * @param sessionId with which the endpoint is going to look for the login value
     * @return a UserData object or null
     */
    @GET
    @Path("logins/sessionid/{sessionId}/userdata")
    @Produces("application/json")
    UserData getUserDataFromSessionId(@PathParam("sessionId") String sessionId);

    /**
     * login to the scheduler using a multipart form can be used either by
     * submitting - 2 fields: username and password - a credential file with
     * field name 'credential'
     * 
     * @return the session id associated to this new connection
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces("application/json")
    String loginWithCredential(@MultipartForm LoginForm multipart)
            throws KeyException, LoginException, SchedulerRestException;

    /**
     * Users currently connected to the scheduler
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return list of users
     */
    @GET
    @GZIP
    @Path("users")
    @Produces("application/json")
    List<SchedulerUserData> getUsers(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    @GET
    @Path("userspace")
    @Produces("application/json")
    List<String> userspaceURIs(@HeaderParam("sessionid") String sessionId) throws RestException;

    @GET
    @Path("globalspace")
    @Produces("application/json")
    List<String> globalspaceURIs(@HeaderParam("sessionid") String sessionId) throws RestException;

    /**
     * Users having jobs in the scheduler
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return list of users
     */
    @GET
    @GZIP
    @Path("userswithjobs")
    @Produces("application/json")
    List<SchedulerUserData> getUsersWithJobs(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * returns statistics about the scheduler
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return a string containing the statistics
     */
    @GET
    @Path("stats")
    @Produces("application/json")
    Map<String, String> getStatistics(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * Get the statistics history for the last 24 hours.
     * @param sessionId current session
     * @param function function applying to statistics, one of: <ul>
     *            <li>AVERAGE: The average of the data points is stored.</li>
     *            <li>MIN: The smallest of the data points is stored.</li>
     *            <li>MAX: The largest of the data points is stored.</li>
     *            <li>LAST: The last data point is used.</li>
     *            <li>FIRST: The fist data point is used.</li>
     *            <li>TOTAL: The total of the data points is stored.</li>
     *            </ul>
     *            Default value is AVERAGE.
     * @return a string containing the history of the statistics for 1 day
     */
    @GET
    @Path("stathistory")
    @Produces("application/json")
    public String getStatHistory(@HeaderParam("sessionid")
    final String sessionId, @QueryParam("function") String function) throws NotConnectedRestException;

    /**
     * returns a string containing some data regarding the user's account
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return a string containing some data regarding the user's account
     */
    @GET
    @Path("stats/myaccount")
    @Produces("application/json")
    Map<String, String> getStatisticsOnMyAccount(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * generates a credential file from user provided credentials
     * 
     * @return the credential file generated by the scheduler
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("createcredential")
    @Produces("*/*")
    byte[] getCreateCredential(@MultipartForm LoginForm multipart) throws LoginException, SchedulerRestException;

    /**
     * Returns details on job and task execution times for the caller's
     * executions.
     * <p>
     * Only the jobs finished between the start date and the end date will be
     * returned: i.e {@code startDate <= job.finishedTime <= endDate}.
     * </p>
     * 
     * @param sessionId
     *            a valid session id to identify the caller
     * @param startDate
     *            must not be null, inclusive
     * @param endDate
     *            must not be null, inclusive
     * @return a list of
     *         {@link org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobUsageData}
     *         objects where job finished times are between start date and end
     *         date
     * @throws NotConnectedRestException
     *             if user not logger in
     * @throws PermissionRestException
     *             if user has insufficient rights
     */
    @GET
    @Path("usage/myaccount")
    @Produces("application/json")
    List<JobUsageData> getUsageOnMyAccount(@HeaderParam("sessionid") String sessionId,
            @QueryParam("startdate") Date startDate, @QueryParam("enddate") Date endDate) throws RestException;

    /**
     * Returns details on job and task execution times for the caller's
     * executions.
     * <p>
     * Only the jobs finished between the start date and the end date will be
     * returned: i.e {@code startDate <= job.finishedTime <= endDate}.
     * </p>
     * 
     * @param sessionId
     *            a valid session id to identify the caller
     * @param user
     *            name
     * @param startDate
     *            must not be null, inclusive
     * @param endDate
     *            must not be null, inclusive
     * @return a list of
     *         {@link org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobUsageData}
     *         objects where job finished times are between start date and end
     *         date
     * @throws NotConnectedRestException
     *             if user not logger in
     * @throws PermissionRestException
     *             if user has insufficient rights
     */
    @GET
    @Path("usage/account")
    @Produces("application/json")
    List<JobUsageData> getUsageOnAccount(@HeaderParam("sessionid") String sessionId, @QueryParam("user") String user,
            @QueryParam("startdate") Date startDate, @QueryParam("enddate") Date endDate) throws RestException;

    /**
     * Stream the output of job identified by the id <code>jobid</code> only
     * stream currently available logs, call this method several times to get
     * the complete output.
     *
     * @param sessionId a valid session id
     * @param jobId     the id of the job to retrieve
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    String getLiveLogJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException,
            LogForwardingRestException, IOException;

    /**
     * number of available bytes in the stream or -1 if the stream does not
     * exist.
     *
     * @param sessionId a valid session id
     * @param jobId     the id of the job to retrieve
     */
    @GET
    @Path("jobs/{jobid}/livelog/available")
    @Produces("application/json")
    int getLiveLogJobAvailable(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException;

    /**
     * remove the live log object.
     *
     * @param sessionId a valid session id
     * @param jobId     the id of the job to retrieve
     */
    @DELETE
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    boolean deleteLiveLogJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException;

    /**
     * Enable Remote Visualization for a task
     *
     * @param sessionId current session
     * @param jobid     id of the job
     * @param taskname  name of the task
     * @param connectionString noVNC connection string
     */
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/visu")
    @Produces("application/json")
    void enableRemoteVisualization(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname, @QueryParam("connectionString") String connectionString)
            throws RestException;

    /**
     * Register a cloud automation service associated with a job.<br>
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            id of the job
     * @param serviceid
     *            id of the cloud automation service to register
     */
    @POST
    @Path("jobs/{jobid}/services")
    @Produces("application/json")
    void registerService(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @QueryParam("serviceid") int serviceid) throws RestException;

    /**
     * Detach a cloud automation service previously associated with a job.<br>
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            id of the job
     * @param serviceid
     *            id of the cloud automation service to detach
     */
    @DELETE
    @Path("jobs/{jobid}/services")
    @Produces("application/json")
    void detachService(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @QueryParam("serviceid") int serviceid) throws RestException;

    /**
     * Restart a task within a job
     *
     * @param sessionId current session
     * @param jobid     id of the job containing the task to kill
     * @param taskname  name of the task to kill
     */
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/restart")
    @Produces("application/json")
    boolean restartTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Finish a task, which is in InError state inside a job.
     *
     * @param sessionId current session
     * @param jobid     id of the job containing the task to finish (only when InError state)
     * @param taskname  name of the task to finish (only when InError state)
     */
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/finishInErrorTask")
    @Produces("application/json")
    boolean finishInErrorTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Restart a pause on error task within a job
     *
     * @param sessionId current session
     * @param jobid     id of the job containing the task to kill
     * @param taskname  name of the task to kill
     */
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/restartInErrorTask")
    @Produces("application/json")
    boolean restartInErrorTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Preempt a task within a job
     * <p>
     * The task will be stopped and restarted later
     *
     * @param sessionId current session
     * @param jobid     id of the job containing the task to preempt
     * @param taskname  name of the task to preempt
     */
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/preempt")
    @Produces("application/json")
    boolean preemptTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Kill a task within a job
     *
     * @param sessionId current session
     * @param jobid     id of the job containing the task to kill
     * @param taskname  name of the task to kill
     */
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/kill")
    @Produces("application/json")
    boolean killTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws RestException;

    /**
     * Validates a job
     *
     * @param sessionId
     *            user session id used to connect to scheduler
     * @param pathSegment
     *            variables of the workflow
     * @param multipart
     *            a HTTP multipart form which contains the job-descriptor
     * @return the result of job validation
     */
    @POST
    @Path("{path:validate}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    JobValidationData validate(@HeaderParam("sessionid") String sessionId, @PathParam("path") PathSegment pathSegment,
            MultipartFormDataInput multipart) throws NotConnectedRestException;

    /**
     * Validates a workflow taken from a given URL
     *
     * @param sessionId
     *            a valid session id
     * @param url
     *            url to the workflow content
     * @param pathSegment
     *            variables of the workflow
     * @return the result of job validation
     */
    @POST
    @Path("{path:validateurl}")
    @Produces("application/json")
    public JobValidationData validateFromUrl(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("link") String url, @PathParam("path") PathSegment pathSegment)
            throws NotConnectedRestException;

    /**
     * Validates a workflow taken from a given URL
     *
     * @param sessionId
     *            a valid session id
     * @param url
     *            url to the workflow content
     * @param pathSegment
     *            variables of the workflow
     * @param multipart
     *            a form with the variables of the workflow
     * @return the result of job validation
     */
    @POST
    @Path("{path:validateurl}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public JobValidationData validateFromUrl(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("link") String url, @PathParam("path") PathSegment pathSegment,
            MultipartFormDataInput multipart) throws NotConnectedRestException;

    @POST
    @Path("/credentials/{key}")
    void putThirdPartyCredential(@HeaderParam("sessionid") String sessionId, @PathParam("key") @Encoded String key,
            @FormParam("value") String value) throws RestException;

    @DELETE
    @Path("/credentials/{key}")
    void removeThirdPartyCredential(@HeaderParam("sessionid") String sessionId, @PathParam("key") @Encoded String key)
            throws RestException;

    @GET
    @Path("/credentials/")
    @Produces("application/json")
    Set<String> thirdPartyCredentialsKeySet(@HeaderParam("sessionid") String sessionId) throws RestException;

    /**
     * Change the START_AT generic information at job level and reset the
     * scheduledAt at task level
     * 
     * @param sessionId
     *            current session
     * @param jobId
     *            id of the job that needs to be updated
     * @param startAt
     *            its value should be ISO 8601 compliant
     */
    @PUT
    @Path("jobs/{jobid}/startat/{startAt}")
    @Produces("application/json")
    boolean changeStartAt(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("startAt")
    final String startAt) throws RestException;

    /**
     * Get portal configuration properties
     */
    @GET
    @Path("configuration/portal")
    @Produces("application/json")
    public Map<Object, Object> getPortalConfiguration(@HeaderParam("sessionid") String sessionId) throws RestException;

    /**
     * returns scheduler properties and web properties in a single map
     *
     * @param sessionId
     *            the session id associated to this new connection
     * @return a map containing the properties
     */
    @GET
    @Path("properties")
    @Produces("application/json")
    Map<String, Object> getSchedulerPropertiesFromSessionId(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * 
     * Check if the user has the permission to execute the method passed as argument
     * 
     * @return true if the user has the permission to execute the java method
     */
    @GET
    @Path("job/{jobid}/permission/{method}")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces("application/json")
    boolean checkJobPermissionMethod(@HeaderParam("sessionid") String sessionId, @PathParam("method") String method,
            @PathParam("jobid") String jobId) throws RestException;

    /**
     *
     * Add a signal to the list of signals used by the considered job
     *
     * @return true if the given signal is added to the list of job signals
     */
    @POST
    @Path("job/{jobid}/signals")
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces("application/json")
    Set<String> addJobSignal(@HeaderParam("sessionid") String sessionId, @QueryParam("signal") String signal,
            @PathParam("jobid") String jobId) throws RestException, SignalApiException;
}
