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
import javax.ws.rs.core.PathSegment;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
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
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobAlreadyFinishedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.LogForwardingRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownTaskRestException;


@Path("/scheduler/")
public interface SchedulerRestInterface {

    String ENCODING = "utf-8";

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
            throws NotConnectedRestException, PermissionRestException;

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
            throws PermissionRestException, NotConnectedRestException;

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
            @QueryParam("finished") @DefaultValue("true") boolean finished)
            throws PermissionRestException, NotConnectedRestException;

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
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            throws NotConnectedRestException, PermissionRestException, UnknownJobRestException;

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
            throws NotConnectedRestException, PermissionRestException, UnknownJobRestException;

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
            throws NotConnectedRestException, PermissionRestException, UnknownJobRestException;

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
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    @Produces("application/json")
    String jobServerLog(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    boolean killJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            @QueryParam("limit") @DefaultValue("-1") int limit)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            @PathParam("tasktag") String taskTag)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
     * @param running
     *            fetch running tasks. Default value is <code>True</code>.
     * @param pending
     *            fetch pending tasks. Default value is <code>True</code>.
     * @param finished
     *            fetch finished tasks. Default value is <code>True</code>.
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
            @QueryParam("running") @DefaultValue("true") boolean running,
            @QueryParam("pending") @DefaultValue("true") boolean pending,
            @QueryParam("finished") @DefaultValue("true") boolean finished,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit)
            throws NotConnectedRestException, PermissionRestException;

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
     * @param running
     *            fetch running tasks. Default value is <code>True</code>.
     * @param pending
     *            fetch pending tasks. Default value is <code>True</code>.
     * @param finished
     *            fetch finished tasks. Default value is <code>True</code>.
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
            @QueryParam("running") @DefaultValue("true") boolean running,
            @QueryParam("pending") @DefaultValue("true") boolean pending,
            @QueryParam("finished") @DefaultValue("true") boolean finished,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit)
            throws NotConnectedRestException, PermissionRestException;

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
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            @PathParam("prefix") String prefix)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    @Produces("application/json;charset=" + ENCODING)
    String getJobHtml(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws IOException, NotConnectedRestException;

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
            @PathParam("jobid") String jobId)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            @QueryParam("limit") @DefaultValue("50") int limit)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
     * @param running
     *            fetch running tasks. Default value is <code>True</code>.
     * @param pending
     *            fetch pending tasks. Default value is <code>True</code>.
     * @param finished
     *            fetch finished tasks. Default value is <code>True</code>.
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
            @QueryParam("running") @DefaultValue("true") boolean running,
            @QueryParam("pending") @DefaultValue("true") boolean pending,
            @QueryParam("finished") @DefaultValue("true") boolean finished,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("sortparameters") SortSpecifierContainer sortParams)
            throws NotConnectedRestException, PermissionRestException;

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
     * @param running
     *            fetch running tasks. Default value is <code>True</code>.
     * @param pending
     *            fetch pending tasks. Default value is <code>True</code>.
     * @param finished
     *            fetch finished tasks. Default value is <code>True</code>.
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
            @QueryParam("running") @DefaultValue("true") boolean running,
            @QueryParam("pending") @DefaultValue("true") boolean pending,
            @QueryParam("finished") @DefaultValue("true") boolean finished,
            @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("sortparameters") SortSpecifierContainer sortParams)
            throws NotConnectedRestException, PermissionRestException;

    /**
     * Returns full logs generated by tasks in job.
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
    @Produces("application/json")
    InputStream jobFullLogs(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @QueryParam("sessionid") String session) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException, IOException;

    /**
     * Returns all the logs generated by the job (either stdout and stderr)
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
    @Produces("application/json")
    String jobLogs(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException;

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
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            PermissionRestException, UnknownTaskRestException;

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
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

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
            @PathParam("tasktag") String taskTag)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * Returns all the logs generated by the task (either stdout and stderr)
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
    @Produces("application/json")
    String taskLog(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

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
    @Produces("application/json")
    String taskLogByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * Returns the standard error output (stderr) generated by the task
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
    @Produces("application/json")
    String taskLogErr(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

    /**
     * Returns the list of standard error outputs (stderr) generated by a set of
     * tasks filtered by a given tag.
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
    @Produces("application/json")
    String taskLogErrByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * Returns the standard output (stdout) generated by the task
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
    @Produces("application/json")
    String taskLogout(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

    /**
     * Returns the standard output (stdout) generated by a set of tasks filtered
     * by a given tag.
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
    @Produces("application/json")
    String taskLogoutByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * Returns full logs generated by the task from user data spaces.
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
    @Produces("application/json")
    InputStream taskFullLogs(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname, @QueryParam("sessionid") String session)
            throws NotConnectedRestException, UnknownJobRestException, UnknownTaskRestException,
            PermissionRestException, IOException;

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
    @Produces("application/json")
    String taskServerLog(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

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
    @Produces("application/json")
    String taskServerLogByTag(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    final String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    final String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
    final String jobId) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

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
     * @throws NotConnectedRestException
     * @throws IOException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     */
    @POST
    @Path("submitflat")
    @Produces("application/json")
    JobIdData submitFlat(@HeaderParam("sessionid") String sessionId,
            @FormParam("commandFileContent") String commandFileContent, @FormParam("jobName") String jobName,
            @FormParam("selectionScriptContent") String selectionScriptContent,
            @FormParam("selectionScriptExtension") String selectionScriptExtension) throws NotConnectedRestException,
            IOException, JobCreationRestException, PermissionRestException, SubmissionClosedRestException;

    /**
     * Submits a job to the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @param multipart
     *            a form with the job file as form data
     * @return the <code>jobid</code> of the newly created job
     */
    @POST
    @Path("{path:submit}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    JobIdData submit(@HeaderParam("sessionid") String sessionId, @PathParam("path") PathSegment pathSegment,
            MultipartFormDataInput multipart) throws JobCreationRestException, NotConnectedRestException,
            PermissionRestException, SubmissionClosedRestException, IOException;

    /**
     * submit a planned workflow
     *
     * @param sessionId user's session in the header
     * @param pathSegment path param going to be transferred to the variables
     * @param jobContentXmlString job content in xml string
     * @return true if the submission is done sucessfully, false otherwise
     * @throws JobCreationRestException
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     * @throws IOException
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
     * @return the <code>jobid</code> of the newly created job
     * @throws NotConnectedRestException
     * @throws IOException
     * @throws JobCreationRestException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     */
    @POST
    @Path("jobs")
    @Produces("application/json")
    JobIdData submitFromUrl(@HeaderParam("sessionid") String sessionId, @HeaderParam("link") String url,
            @PathParam("path") PathSegment pathSegment) throws JobCreationRestException, NotConnectedRestException,
            PermissionRestException, SubmissionClosedRestException, IOException;

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
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * pauses the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("pause")
    @Produces("application/json")
    boolean pauseScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * stops the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("stop")
    @Produces("application/json")
    boolean stopScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * resumes the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("resume")
    @Produces("application/json")
    boolean resumeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * changes the priority of a job
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @param priorityName
     *            a string representing the name of the priority
     * @throws NotConnectedRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException
     * @throws PermissionRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobAlreadyFinishedRestException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    void schedulerChangeJobPriorityByName(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("name") String priorityName) throws NotConnectedRestException,
            UnknownJobRestException, PermissionRestException, JobAlreadyFinishedRestException;

    /**
     * changes the priority of a job
     * 
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @param priorityValue
     *            a string representing the value of the priority
     * @throws NumberFormatException
     * @throws NotConnectedRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException
     * @throws PermissionRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobAlreadyFinishedRestException
     */
    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    void schedulerChangeJobPriorityByValue(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("value") String priorityValue)
            throws NumberFormatException, NotConnectedRestException, UnknownJobRestException, PermissionRestException,
            JobAlreadyFinishedRestException;

    /**
     * freezes the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("freeze")
    @Produces("application/json")
    boolean freezeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * returns the status of the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return the scheduler status
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @Path("status")
    @Produces("application/json")
    SchedulerStatusData getSchedulerStatus(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * starts the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("start")
    @Produces("application/json")
    boolean startScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * kills and shutdowns the scheduler
     * 
     * @param sessionId
     *            a valid session id
     * @return true if success, false if not
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("kill")
    @Produces("application/json")
    boolean killScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * Reconnect a new Resource Manager to the scheduler. Can be used if the
     * resource manager has crashed.
     * 
     * @param sessionId
     *            a valid session id
     * @param rmURL
     *            the url of the resource manager
     * @return true if success, false otherwise.
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @POST
    @Path("linkrm")
    @Produces("application/json")
    boolean linkRm(@HeaderParam("sessionid")
    final String sessionId, @FormParam("rmurl") String rmURL) throws NotConnectedRestException, PermissionRestException;

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
     * @throws LoginException
     * @throws SchedulerRestException
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
     * @throws SchedulerRestException
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
     * @throws SchedulerRestException
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
     * @throws KeyException
     * @throws LoginException
     * @throws SchedulerRestException
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
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @GZIP
    @Path("users")
    @Produces("application/json")
    List<SchedulerUserData> getUsers(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    @GET
    @Path("userspace")
    @Produces("application/json")
    List<String> userspaceURIs(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedRestException, PermissionRestException;

    @GET
    @Path("globalspace")
    @Produces("application/json")
    List<String> globalspaceURIs(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedRestException, PermissionRestException;

    /**
     * Users having jobs in the scheduler
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return list of users
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @GZIP
    @Path("userswithjobs")
    @Produces("application/json")
    List<SchedulerUserData> getUsersWithJobs(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * returns statistics about the scheduler
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return a string containing the statistics
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @Path("stats")
    @Produces("application/json")
    Map<String, String> getStatistics(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * returns a string containing some data regarding the user's account
     * 
     * @param sessionId
     *            the session id associated to this new connection
     * @return a string containing some data regarding the user's account
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @Path("stats/myaccount")
    @Produces("application/json")
    Map<String, String> getStatisticsOnMyAccount(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;

    /**
     * generates a credential file from user provided credentials
     * 
     * @return the credential file generated by the scheduler
     * @throws SchedulerRestException
     * @throws LoginException
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
            @QueryParam("startdate") Date startDate, @QueryParam("enddate") Date endDate)
            throws NotConnectedRestException, PermissionRestException;

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
            @QueryParam("startdate") Date startDate, @QueryParam("enddate") Date endDate)
            throws NotConnectedRestException, PermissionRestException;

    @GET
    @GZIP
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    String getLiveLogJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException,
            LogForwardingRestException, IOException;

    @GET
    @Path("jobs/{jobid}/livelog/available")
    @Produces("application/json")
    int getLiveLogJobAvailable(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException;

    @DELETE
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    boolean deleteLiveLogJob(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException;

    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/restart")
    @Produces("application/json")
    boolean restartTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/finishInErrorTask")
    @Produces("application/json")
    boolean finishInErrorTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/restartInErrorTask")
    @Produces("application/json")
    boolean restartInErrorTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/preempt")
    @Produces("application/json")
    boolean preemptTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/kill")
    @Produces("application/json")
    boolean killTask(@HeaderParam("sessionid") String sessionId, @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname) throws NotConnectedRestException, UnknownJobRestException,
            UnknownTaskRestException, PermissionRestException;

    /**
     * Validates a job.
     * 
     * @param multipart
     *            a HTTP multipart form which contains the job-descriptor
     * @return the result of job validation
     */
    @POST
    @Path("{path:validate}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    JobValidationData validate(@PathParam("path") PathSegment pathSegment, MultipartFormDataInput multipart);

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
     * @throws NotConnectedRestException
     * @throws IOException
     * @throws JobCreationRestException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     */
    @POST
    @Path("{path:validateurl}")
    @Produces("application/json")
    public JobValidationData validateFromUrl(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("link") String url, @PathParam("path") PathSegment pathSegment)
            throws NotConnectedRestException;

    @POST
    @Path("/credentials/{key}")
    void putThirdPartyCredential(@HeaderParam("sessionid") String sessionId, @PathParam("key") String key,
            @FormParam("value") String value)
            throws NotConnectedRestException, PermissionRestException, SchedulerRestException;

    @DELETE
    @Path("/credentials/{key}")
    void removeThirdPartyCredential(@HeaderParam("sessionid") String sessionId, @PathParam("key") String key)
            throws NotConnectedRestException, PermissionRestException;

    @GET
    @Path("/credentials/")
    @Produces("application/json")
    Set<String> thirdPartyCredentialsKeySet(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedRestException, PermissionRestException;

    /**
     * Change the START_AT generic information at job level and reset the
     * scheduledAt at task level
     * 
     * @param sessionId
     *            current session
     * @param jobid
     *            id of the job that needs to be updated
     * @param startAt
     *            its value should be ISO 8601 compliant
     * @throws NotConnectedRestException
     * @throws UnknownJobRestException
     * @throws PermissionRestException
     */
    @PUT
    @Path("jobs/{jobid}/startat/{startAt}")
    @Produces("application/json")
    boolean changeStartAt(@HeaderParam("sessionid")
    final String sessionId, @PathParam("jobid")
    final String jobId, @PathParam("startAt")
    final String startAt) throws NotConnectedRestException, UnknownJobRestException, PermissionRestException;

    /**
     * 
     * @param sessionId
     * @param jobId
     * @param generalInformation
     * @return
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     * @throws UnknownJobRestException
     * @throws JobCreationRestException
     * @throws SubmissionClosedRestException
     */
    @POST
    @Path("jobs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JobIdData copyAndResubmitWithGeneralInfo(@HeaderParam("sessionid") String sessionId, @QueryParam("jobid")
    final String jobId, Map<String, String> generalInformation) throws NotConnectedRestException,
            PermissionRestException, UnknownJobRestException, JobCreationRestException, SubmissionClosedRestException;

    /**
     * Get portal configuration properties
     * @param sessionId
     * @return
     * @throws NotConnectedRestException 
     * @throws PermissionRestException 
     */
    @GET
    @Path("configuration/portal")
    @Produces("application/json")
    public Map<Object, Object> getPortalConfiguration(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedRestException, PermissionRestException;

    /**
     * returns scheduler properties
     *
     * @param sessionId
     *            the session id associated to this new connection
     * @return a map containing the properties
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @GET
    @Path("properties")
    @Produces("application/json")
    Map<String, Object> getSchedulerPropertiesFromSessionId(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException;
}
