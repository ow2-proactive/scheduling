/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.ow2.proactive_grid_cloud_portal.scheduler.ValidationUtil.validateJobDescriptor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.log4j.Logger;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.*;
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
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.factories.FlatJobFactory;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatesPage;
import org.ow2.proactive.scheduler.common.util.PageBoundaries;
import org.ow2.proactive.scheduler.common.util.Pagination;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive_grid_cloud_portal.common.*;
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
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventNotification;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventSubscription;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobAlreadyFinishedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.LogForwardingRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownTaskRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.util.EventUtil;
import org.ow2.proactive_grid_cloud_portal.webapp.DateFormatter;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;

import com.google.common.collect.Maps;


/**
 * This class exposes the Scheduler as a RESTful service.
 */
@Path("/scheduler/")
public class SchedulerStateRest implements SchedulerRestInterface {

    /**
     * If the rest api was unable to instantiate the value from byte array
     * representation
     */
    public static final String UNKNOWN_VALUE_TYPE = "Unknown value type";

    private static final Logger logger = ProActiveLogger.getLogger(SchedulerStateRest.class);

    private static final String ATM_BROADCASTER_ID = "atmosphere.broadcaster.id";

    private static final String ATM_RESOURCE_ID = "atmosphere.resource.id";

    private final SessionStore sessionStore = SharedSessionStore.getInstance();

    private static FileSystemManager fsManager = null;

    private static Map<String, String> sortableTaskAttrMap = null;

    private static final int TASKS_PAGE_SIZE = PASchedulerProperties.TASKS_PAGE_SIZE.getValueAsInt();

    static {
        try {
            fsManager = VFSFactory.createDefaultFileSystemManager();
            sortableTaskAttrMap = createSortableTaskAttrMap();
        } catch (FileSystemException e) {
            logger.error("Could not create Default FileSystem Manager", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected static final List<SortParameter<JobSortParameter>> DEFAULT_JOB_SORT_PARAMS = Arrays.asList(
            new SortParameter<>(JobSortParameter.STATE, SortOrder.ASC),
            new SortParameter<>(JobSortParameter.ID, SortOrder.DESC));

    private static final Mapper mapper = new DozerBeanMapper(
        Collections.singletonList("org/ow2/proactive_grid_cloud_portal/scheduler/dozer-mappings.xml"));

    @Context
    private HttpServletRequest httpServletRequest;

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
    @Override
    @GET
    @Path("jobs")
    @Produces("application/json")
    public RestPage<String> jobs(
            @HeaderParam("sessionid") String sessionId,
            @QueryParam("index") @DefaultValue("-1") int index,
            @QueryParam("limit") @DefaultValue("-1") int limit)
                    throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobs");

            Page<JobInfo> page = s.getJobs(index, limit, new JobFilterCriteria(false, true, true, true),
                    DEFAULT_JOB_SORT_PARAMS);

            List<String> ids = new ArrayList<String>(page.getList().size());
            for (JobInfo jobInfo : page.getList()) {
                ids.add(jobInfo.getJobId().value());
            }
            return new RestPage<String>(ids, page.getSize());
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
    }

    /**
     * Call a method on the scheduler's frontend in order to renew the lease the
     * user has on this frontend. see PORTAL-70
     *
     * @throws NotConnectedRestException
     */
    protected void renewLeaseForClient(Scheduler scheduler) throws NotConnectedRestException {
        try {
            scheduler.renewSession();
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

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
    @Override
    @GET
    @Path("jobsinfo")
    @Produces({ "application/json", "application/xml" })
    public RestPage<UserJobData> jobsInfo(
            @HeaderParam("sessionid") String sessionId,
            @QueryParam("index") @DefaultValue("-1") int index,
            @QueryParam("limit") @DefaultValue("-1") int limit)
                    throws PermissionRestException, NotConnectedRestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobsinfo");

            Page<JobInfo> page = s.getJobs(index, limit,
                    new JobFilterCriteria(false, true, true, true), DEFAULT_JOB_SORT_PARAMS);
            List<UserJobData> userJobInfoList = new ArrayList<UserJobData>(page.getList().size());
            for (JobInfo jobInfo : page.getList()) {
                userJobInfoList.add(new UserJobData(mapper.map(jobInfo, JobInfoData.class)));
            }

            return new RestPage<UserJobData>(userJobInfoList, page.getSize());
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
    }

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
     *            fetch only the jobs for the user making the request
     * @param pending
     *            fetch pending jobs
     * @param running
     *            fetch running jobs
     * @param finished
     *            fetch finished jobs
     * @return a map containing one entry with the revision id as key and the
     *         list of UserJobData as value.
     */
    @Override
    @GET
    @GZIP
    @Path("revisionjobsinfo")
    @Produces({ "application/json", "application/xml" })
    public RestMapPage<Long, ArrayList<UserJobData>> revisionAndJobsInfo(
            @HeaderParam("sessionid") String sessionId,
            @QueryParam("index") @DefaultValue("-1") int index,
            @QueryParam("limit") @DefaultValue("-1") int limit,
            @QueryParam("myjobs") @DefaultValue("false") boolean myJobs,
            @QueryParam("pending") @DefaultValue("true") boolean pending,
            @QueryParam("running") @DefaultValue("true") boolean running,
            @QueryParam("finished") @DefaultValue("true") boolean finished)
                    throws PermissionRestException, NotConnectedRestException {
        try {
            Scheduler s = checkAccess(sessionId, "revisionjobsinfo?index=" + index + "&limit=" + limit);
            String user = sessionStore.get(sessionId).getUserName();

            boolean onlyUserJobs = (myJobs && user != null && user.trim().length() > 0);

            Page<JobInfo> page = s.getJobs(index, limit, new JobFilterCriteria(onlyUserJobs, pending, running, finished), DEFAULT_JOB_SORT_PARAMS);
            List<JobInfo> jobsInfo = page.getList();
            ArrayList<UserJobData> jobs = new ArrayList<>(jobsInfo.size());
            for (JobInfo jobInfo : jobsInfo) {
                jobs.add(new UserJobData(mapper.map(jobInfo, JobInfoData.class)));
            }

            HashMap<Long, ArrayList<UserJobData>> map = new HashMap<Long, ArrayList<UserJobData>>(1);
            map.put(SchedulerStateListener.getInstance().getSchedulerStateRevision(), jobs);
            RestMapPage<Long, ArrayList<UserJobData>> restMapPage = new RestMapPage<Long, ArrayList<UserJobData>>();
            restMapPage.setMap(map);
            restMapPage.setSize(page.getSize());
            return restMapPage;
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Returns the revision number of the scheduler state
     *
     * @param sessionId
     *            a valid session id.
     * @return the revision of the scheduler state
     */
    @Override
    @GET
    @Path("state/revision")
    @Produces({ "application/json", "application/xml" })
    public long schedulerStateRevision(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedRestException {
        checkAccess(sessionId, "/scheduler/revision");
        return SchedulerStateListener.getInstance().getSchedulerStateRevision();
    }

    /**
     * Returns a JobState of the job identified by the id <code>jobid</code>
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job to retrieve
     */
    @Override
    @GET
    @Path("jobs/{jobid}")
    @Produces({ "application/json", "application/xml" })
    public JobStateData listJobs(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
            throws NotConnectedRestException, UnknownJobRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobs/" + jobId);

            JobState js = s.getJobState(jobId);
            js = PAFuture.getFutureValue(js);

            return mapper.map(js, JobStateData.class);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        }
    }

    /**
     * Stream the output of job identified by the id <code>jobid</code> only
     * stream currently available logs, call this method several times to get
     * the complete output.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job to retrieve
     * @throws IOException
     * @throws LogForwardingRestException
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    @Override
    public String getLiveLogJob(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) 
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException, LogForwardingRestException, IOException {
        try {
            Scheduler scheduler = checkAccess(sessionId, "/scheduler/jobs/" + jobId + "/livelog");
            Session session = sessionStore.get(sessionId);

            JobState jobState = scheduler.getJobState(jobId);
            boolean isFinished = jobState != null && jobState.isFinished();
            int availableLinesCount = session.getJobsOutputController().availableLinesCount(jobId);

            if (!isFinished || availableLinesCount > 0) {
                return session.getJobsOutputController().getNewLogs(jobId);
            } else {
                session.getJobsOutputController().removeAppender(jobId);
                return "";
            }

        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (LogForwardingException e) {
            throw new LogForwardingRestException(e);
        }
    }

    /**
     * number of available bytes in the stream or -1 if the stream does not
     * exist.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job to retrieve
     */
    @Override
    @GET
    @Path("jobs/{jobid}/livelog/available")
    @Produces("application/json")
    public int getLiveLogJobAvailable(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedRestException {
        checkAccess(sessionId, "/scheduler/jobs/" + jobId + "/livelog/available");
        Session ss = sessionStore.get(sessionId);

        return ss.getJobsOutputController().availableLinesCount(jobId);
    }

    /**
     * remove the live log object.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job to retrieve
     * @throws NotConnectedRestException
     */
    @Override
    @DELETE
    @Path("jobs/{jobid}/livelog")
    @Produces("application/json")
    public boolean deleteLiveLogJob(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) throws NotConnectedRestException {
        checkAccess(sessionId, "delete /scheduler/jobs/livelog" + jobId);
        Session ss = sessionStore.get(sessionId);
        ss.getJobsOutputController().removeAppender(jobId);
        return true;

    }

    /**
     * Returns the job result associated to the job referenced by the id
     * <code>jobid</code>
     *
     * @param sessionId
     *            a valid session id
     * @return the job result of the corresponding job
     */
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/result")
    @Produces("application/json")
    public JobResultData jobResult(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
                    throws NotConnectedRestException, PermissionRestException,
                    UnknownJobRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/result");
            return mapper.map(PAFuture.getFutureValue(s.getJobResult(jobId)),
                    JobResultData.class);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JobInfoData jobInfo(String sessionId, String jobId)
            throws NotConnectedRestException, PermissionRestException, UnknownJobRestException {
        Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/info");
        JobInfoData job = null;
        try {
            job = mapper.map(s.getJobState(jobId).getJobInfo(), JobInfoData.class);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
        return job;
    }

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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/result/value")
    @Produces("application/json")
    public Map<String, String> jobResultValue(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
                    throws NotConnectedRestException, PermissionRestException, UnknownJobRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/result/value");
            JobResult jobResult = PAFuture.getFutureValue(s.getJobResult(jobId));
            if (jobResult == null) {
                return null;
            }
            Map<String, TaskResult> allResults = jobResult.getAllResults();
            Map<String, String> res = new HashMap<>(allResults.size());
            for (final Entry<String, TaskResult> entry : allResults.entrySet()) {
                TaskResult taskResult = entry.getValue();
                String value = getTaskResultValueAsStringOrExceptionStackTrace(taskResult);
                res.put(entry.getKey(), value);
            }
            return res;
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

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
    @Override
    @DELETE
    @Path("jobs/{jobid}")
    @Produces("application/json")
    public boolean removeJob(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId) 
                    throws NotConnectedRestException, UnknownJobRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "DELETE jobs/" + jobId);
            return s.removeJob(jobId);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     *  Returns job server logs
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @return job traces from the scheduler and resource manager
    */
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/log/server")
    @Produces("application/json")
    public String jobServerLog(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/log/server");
            return s.getJobServerLogs(jobId);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Kill the job represented by jobId.<br>
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job to kill.
     * @return true if success, false if not.
     */
    @Override
    @PUT
    @Path("jobs/{jobid}/kill")
    @Produces("application/json")
    public boolean killJob(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobId + "/kill");
            return s.killJob(jobId);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Kill a task within a job
     * @param sessionId current session
     * @param jobid id of the job containing the task to kill
     * @param taskname name of the task to kill
     * @throws UnknownJobRestException
     * @throws UnknownTaskRestException
     * @throws PermissionRestException
     * @throws NotConnectedRestException
     */
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/kill")
    @Produces("application/json")
    public boolean killTask(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobid + "/tasks/" + taskname + "/kill");
            return s.killTask(jobid, taskname);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    /**
     * Preempt a task within a job
     * <p>
     * The task will be stopped and restarted later
     * @param sessionId current session
     * @param jobid id of the job containing the task to preempt
     * @param taskname name of the task to preempt
     * @throws NotConnectedRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownTaskRestException
     * @throws org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException
     */
    @Override
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/preempt")
    @Produces("application/json")
    public boolean preemptTask(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobid + "/tasks/" + taskname + "/preempt");
            return s.preemptTask(jobid, taskname, 5);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    /**
     * Restart a task within a job
     * @param sessionId current session
     * @param jobid id of the job containing the task to kill
     * @param taskname name of the task to kill
     * @throws NotConnectedRestException
     * @throws UnknownJobRestException
     * @throws UnknownTaskRestException
     * @throws PermissionRestException
     */
    @Override
    @PUT
    @Path("jobs/{jobid}/tasks/{taskname}/restart")
    @Produces("application/json")
    public boolean restartTask(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobid,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobid + "/tasks/" + taskname + "/restart");
            return s.restartTask(jobid, taskname, 5);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    /**
     * Returns a list of the name of the tasks belonging to job <code>jobId</code>
     * @param sessionId a valid session id
     * @param jobId jobid one wants to list the tasks' name
     * @return a list of tasks' name 
     */
    @Override
    @GET
    @Path("jobs/{jobid}/tasks")
    @Produces("application/json")
    public RestPage<String> getTasksNames(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        return getTasksNamesPaginated(sessionId, jobId, 0, TASKS_PAGE_SIZE);
    }

    /**
     * Returns a list of the name of the tasks belonging to job <code>jobId</code> with pagination
     * @param sessionId a valid session id
     * @param jobId jobid one wants to list the tasks' name
     * @param offset the number of the first task to fetch
     * @param limit the number of the last task to fetch (non inclusive)
     * @return the list of task ids with the total number of them
     */
    @Override
    @GET
    @Path("jobs/{jobid}/tasks/paginated")
    @Produces("application/json")
    public RestPage<String> getTasksNamesPaginated(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("-1") int limit)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        if (limit == -1) limit = TASKS_PAGE_SIZE;
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks");

            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTasksPaginated(offset, limit);
            List<String> tasksNames = new ArrayList<>(page.getTaskStates().size());
            for (TaskState ts : page.getTaskStates()) {
                tasksNames.add(ts.getId().getReadableName());
            }
            return new RestPage<String>(tasksNames, page.getSize());
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Returns a list of the name of the tasks belonging to job and filtered by a given tag.
     * <code>jobId</code>
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            jobid one wants to list the tasks' name
     * @param taskTag
     *            the tag used to filter the tasks.
     * @return the list of task ids with the total number of them
     */
    @Override
    @GET
    @Path("jobs/{jobid}/tasks/tag/{tasktag}")
    @Produces("application/json")
    public RestPage<String> getJobTasksIdsByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks");

            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagPaginated(taskTag, 0, TASKS_PAGE_SIZE);
            List<TaskState> tasks = page.getTaskStates();
            List<String> tasksName = new ArrayList<>(tasks.size());
            for (TaskState ts : tasks) {
                tasksName.add(ts.getId().getReadableName());
            }

            return new RestPage<String>(tasksName, page.getSize());
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Returns a list of the name of the tasks belonging to job <code>jobId</code> (with pagination)
     * @param sessionId a valid session id.
     * @param jobId the job id.
     * @param taskTag the tag used to filter the tasks.
     * @param offset the number of the first task to fetch
     * @param limit the number of the last task to fetch (non inclusive)
     * @return a list of task' states of the job <code>jobId</code> filtered by a given tag, for a given pagination.
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/paginated")
    @Produces("application/json")
    public RestPage<String> getJobTasksIdsByTagPaginated(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("-1") int limit)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        if (limit == -1) limit = TASKS_PAGE_SIZE;
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/" + taskTag + "/paginated");

            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagPaginated(taskTag, offset, limit);
            List<TaskState> tasks = page.getTaskStates();
            List<String> tasksName = new ArrayList<>(tasks.size());

            for (TaskState ts : tasks) {
                tasksName.add(ts.getId().getReadableName());
            }

            return new RestPage<String>(tasksName, page.getSize());
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Returns a list of the tags of the tasks belonging to job <code>jobId</code>
     * @param sessionId a valid session id
     * @param jobId jobid one wants to list the tasks' tags
     * @return a list of tasks' name
     */
    @GET
    @Path("jobs/{jobid}/tasks/tags")
    @Produces("application/json")
    public List<String> getJobTaskTags(@HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
                    throws NotConnectedRestException, UnknownJobRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/tags");
            JobState jobState = s.getJobState(jobId);
            return jobState.getTags();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Returns a list of the tags of the tasks belonging to job <code>jobId</code> and filtered by a prefix pattern
     * @param sessionId a valid session id
     * @param jobId jobid one wants to list the tasks' tags
     * @param prefix the prefix used to filter tags
     * @return a list of tasks' name
     */
    @GET
    @Path("jobs/{jobid}/tasks/tags/startsWith/{prefix}")
    @Produces("application/json")
    public List<String> getJobTaskTagsPrefix(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("prefix") String prefix)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/tags/startswith/" + prefix);
            JobState jobState = s.getJobState(jobId);
            return jobState.getTags(prefix);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Path("jobs/{jobid}/html")
    @Produces("text/html")
    public String getJobHtml(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
            throws IOException, NotConnectedRestException {
        checkAccess(sessionId);

        File jobHtml = new File(PortalConfiguration.jobIdToPath(jobId) + ".html");
        if (!jobHtml.exists()) {
            throw new IOException("the file " + jobHtml.getAbsolutePath() + " was not found on the server");
        }
        InputStream ips = new BufferedInputStream(new FileInputStream(jobHtml));
        return new String(IOUtils.toByteArray(ips));
    }

    /**
     * Returns a list of taskState
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @return a list of task' states of the job <code>jobId</code>
     */
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates")
    @Produces("application/json")
    public RestPage getJobTaskStates(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        return getJobTaskStatesPaginated(sessionId, jobId, 0, TASKS_PAGE_SIZE);
    }

    /**
     * Returns a list of taskState with pagination
     *
     * @param sessionId a valid session id
     * @param jobId the job id
     * @param offset the index of the first TaskState to return
     * @param limit the index (non inclusive) of the last TaskState to return
     * @return a list of task' states of the job <code>jobId</code>
     */
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates/paginated")
    @Produces("application/json")
    public RestPage getJobTaskStatesPaginated(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("-1") int limit)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        if (limit == -1) limit = TASKS_PAGE_SIZE;
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/taskstates/paginated");
            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTasksPaginated(offset, limit);
            List<TaskStateData> tasks = map(page.getTaskStates(), TaskStateData.class);
            return new RestPage(tasks, page.getSize());
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Returns a list of taskState of the tasks filtered by a given tag.
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the job id
     * @param taskTag
     *             the tag used to filter the tasks
     * @return a list of task' states of the job <code>jobId</code> filtered by a given tag.
     */
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates/{tasktag}")
    @Produces("application/json")
    public RestPage getJobTaskStatesByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/taskstates/" + taskTag);
            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagPaginated(taskTag, 0, TASKS_PAGE_SIZE);
            List<TaskStateData> tasks = map(page.getTaskStates(), TaskStateData.class);
            return new RestPage(tasks, page.getSize());
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/taskstates/{tasktag}/paginated")
    @Produces("application/json")
    public RestPage getJobTaskStatesByTagPaginated(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId, @PathParam("tasktag") String taskTag,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("-1") int limit)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        if (limit == -1) limit = TASKS_PAGE_SIZE;
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/taskstates/" + taskTag + "/paginated");
            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagPaginated(taskTag, offset, limit);
            List<TaskStateData> tasks = map(page.getTaskStates(), TaskStateData.class);
            return new RestPage(tasks, page.getSize());
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/log/full")
    @Produces("application/json")
    public InputStream jobFullLogs(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @QueryParam("sessionid") String session)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException, IOException {

        if (sessionId == null) {
            sessionId = session;
        }

        try {
            Scheduler scheduler = checkAccess(sessionId, "jobs/" + jobId + "/log/full");

            JobState jobState = scheduler.getJobState(jobId);

            List<TaskState> tasks = jobState.getTasks();
            List<InputStream> streams = new ArrayList<>(tasks.size());

            Collections.sort(tasks, TaskState.COMPARE_BY_FINISHED_TIME_ASC);

            for (TaskState taskState : tasks) {

                InputStream inputStream = null;

                try {
                    if (taskState.isPreciousLogs()) {
                        inputStream = retrieveTaskLogsUsingDataspaces(sessionId, jobId, taskState.getId());
                    } else {
                        String taskLogs = retrieveTaskLogsUsingDatabase(sessionId, jobId,
                                taskState.getName());

                        if (!taskLogs.isEmpty()) {
                            inputStream = IOUtils.toInputStream(taskLogs);
                        }

                        logger.warn("Retrieving truncated logs for task '" + taskState.getId() + "'");
                    }
                } catch (Exception e) {
                    logger.info("Could not retrieve logs for task " + taskState.getId() +
                        " (could be a non finished or killed task)", e);
                }

                if (inputStream != null) {
                    streams.add(inputStream);
                }
            }

            if (streams.isEmpty()) {
                return null; // will produce HTTP 204 code
            } else {
                return new SequenceInputStream(Collections.enumeration(streams));
            }
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    public InputStream retrieveTaskLogsUsingDataspaces(String sessionId, String jobId, TaskId taskId)
            throws PermissionRestException, IOException, NotConnectedRestException {
        String fullTaskLogsFile = "TaskLogs-" + jobId + "-" + taskId.value() + ".log";
        return pullFile(sessionId, SchedulerConstants.USERSPACE_NAME, fullTaskLogsFile);
    }

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
    @Override
    @GET
    @Path("jobs/{jobid}/tasks/{taskname}")
    @Produces("application/json")
    public TaskStateData jobTask(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException, UnknownTaskRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/" + taskname);

            JobState jobState = s.getJobState(jobId);

            for (TaskState ts : jobState.getTasks()) {
                if (ts.getId().getReadableName().equals(taskname)) {
                    return mapper.map(ts, TaskStateData.class);
                }
            }

            throw new UnknownTaskRestException("task " + taskname + "not found");
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/value")
    @Produces("*/*")
    public Serializable valueOfTaskResult(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws Throwable {
        Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/" +
            taskname + "/result/value");
        TaskResult taskResult = s.getTaskResult(jobId, taskname);
        return getTaskResultValueAsStringOrExceptionStackTrace(taskResult);
    }

    /**
     * Returns the value of the task result for a set of tasks of the job <code>jobId</code> filtered by a given tag.
     * <strong>the result is deserialized before sending to the client, if the class is
     * not found the content is replaced by the string 'Unknown value type' </strong>. To get the serialized form of a given result,
     * one has to call the following restful service
     * jobs/{jobid}/tasks/tag/{tasktag}/result/serializedvalue
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskTag the tag used to filter the tasks.
     * @return the value of the task result
     */

    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/value")
    @Produces("application/json")
    public Map<String, String> valueOfTaskResultByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag) throws Throwable {
        Scheduler s = checkAccess(sessionId, "jobs/" + jobId +
                "/tasks/tag/" + taskTag + "/result/value");
        List<TaskResult> taskResults = s.getTaskResultsByTag(jobId, taskTag);
        Map<String, String> result = new HashMap<String, String>(taskResults.size());
        for (TaskResult currentTaskResult : taskResults) {
            result.put(currentTaskResult.getTaskId().getReadableName(),
                    getTaskResultValueAsStringOrExceptionStackTrace(currentTaskResult));
        }
        return result;
    }

    private String getTaskResultValueAsStringOrExceptionStackTrace(TaskResult taskResult) {
        if (taskResult == null) {
            // task is not finished yet
            return null;
        }
        String value = null;
        // No entry if the task had exception
        if (taskResult.hadException()) {
            value = StackTraceUtil.getStackTrace(taskResult.getException());
        } else {
            try {
                Serializable instanciatedValue = taskResult.value();
                if (instanciatedValue != null) {
                    value = instanciatedValue.toString();
                }
            } catch (InternalSchedulerException e) {
                value = UNKNOWN_VALUE_TYPE;
            } catch (Throwable t) {
                value = "Unable to get the value due to " + t.getMessage();
            }
        }
        return value;
    }

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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/serializedvalue")
    @Produces("*/*")
    public byte[] serializedValueOfTaskResult(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname) throws Throwable {
        Scheduler s = checkAccess(sessionId,
                "jobs/" + jobId + "/tasks/" + taskname + "/result/serializedvalue");
        TaskResult tr = s.getTaskResult(jobId, taskname);
        tr = PAFuture.getFutureValue(tr);
        return tr.getSerializedValue();
    }

    /**
     * Returns the values of a set of tasks of the job <code>jobId</code> filtered by a given tag.
     * This method returns the result as a byte array whatever the result is.
     * @param sessionId
     *          a valid session id
     * @param jobId
     *          the id of the job
     * @param taskTag
     *          the tag used to filter the tasks.
     * @return
     *          the values of the set of tasks result as a byte array, indexed by the readable name of the task.
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/serializedvalue")
    @Produces("application/json")
    public Map<String, byte[]> serializedValueOfTaskResultByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag) throws Throwable {
        Scheduler s = checkAccess(sessionId,
                "jobs/" + jobId + "/tasks/tag" + taskTag + "/result/serializedvalue");
        List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
        Map<String, byte[]> result = new HashMap<>(trs.size());
        for (TaskResult currentResult : trs) {
            TaskResult r = PAFuture.getFutureValue(currentResult);
            result.put(r.getTaskId().getReadableName(), r.getSerializedValue());
        }
        return result;
    }

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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result")
    @Produces("application/json")
    public TaskResultData taskResult(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/" + taskname + "/result");
            TaskResult taskResult = s.getTaskResult(jobId, taskname);
            if (taskResult == null) {
                TaskIdData taskIdData = new TaskIdData();
                taskIdData.setReadableName(taskname);
                TaskResultData taskResultData = new TaskResultData();
                taskResultData.setId(taskIdData);
                return taskResultData;
            }
            return mapper.map(PAFuture.getFutureValue(taskResult), TaskResultData.class);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    /**
     * Returns the task results of the set of task filtered by a given tag
     * and owned by the job <code>jobId</code>
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskTag the tag used to filter the tasks.
     * @return the task results of the set of tasks filtered by the given tag.
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result")
    @Produces("application/json")
    public List<TaskResultData> taskResultByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/" + taskTag + "/result");
            List<TaskResult> taskResults = s.getTaskResultsByTag(jobId, taskTag);
            ArrayList<TaskResultData> results = new ArrayList<TaskResultData>(taskResults.size());
            for (TaskResult current : taskResults) {
                TaskResultData r = mapper.map(PAFuture.getFutureValue(current), TaskResultData.class);
                results.add(r);
            }

            return results;
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/all")
    @Produces("application/json")
    public String taskLog(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            return retrieveTaskLogsUsingDatabase(sessionId, jobId, taskname);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    private String retrieveTaskLogsUsingDatabase(String sessionId, String jobId, String taskName)
            throws NotConnectedRestException, UnknownJobException, UnknownTaskException,
            NotConnectedException, PermissionException, PermissionRestException {
        Scheduler scheduler = checkAccess(sessionId,
                "jobs/" + jobId + "/tasks/" + taskName + "/result/log/all");

        TaskResult taskResult = scheduler.getTaskResult(jobId, taskName);

        if (taskResult != null && taskResult.getOutput() != null) {
            return taskResult.getOutput().getAllLogs(true);
        }

        return "";
    }

    /**
     *  Returns all the logs generated by a set of the tasks (either stdout and stderr) filtered by a tag.
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskTag the tag used to filter the tasks.
     * @return  the list of logs generated by each filtered task (either stdout and stderr) or an empty string if the result is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/log/all")
    @Produces("application/json")
    public String taskLogByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId,
                    "jobs/" + jobId + "/tasks/tag/" + taskTag + "/result/log/err");
            List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
            StringBuffer buf = new StringBuffer();
            for (TaskResult tr : trs) {
                if (tr.getOutput() != null) {
                    buf.append(tr.getOutput().getAllLogs(true));
                }
            }
            return buf.toString();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/result/log/all")
    @Produces("application/json")
    public String jobLogs(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/result/log/all");
            JobResult jobResult = s.getJobResult(jobId);
            if (jobResult == null) {
                return "";
            }

            StringBuilder jobOutput = new StringBuilder();
            for (TaskResult tr : jobResult.getAllResults().values()) {
                if ((tr != null) && (tr.getOutput() != null)) {
                    jobOutput.append(tr.getOutput().getAllLogs(true));
                }
            }
            return jobOutput.toString();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/err")
    @Produces("application/json")
    public String taskLogErr(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/" + taskname + "/result/log/err");
            TaskResult tr = s.getTaskResult(jobId, taskname);
            if ((tr != null) && (tr.getOutput() != null)) {
                return tr.getOutput().getStderrLogs(true);
            } else {
                return "";
            }
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    /**
     *  Returns the list of standard error outputs (stderr) generated by a set of tasks filtered by a given tag.
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskTag the tag used to filter the tasks
     * @return  the list of stderr generated by the set of tasks filtered by the given tag or an empty string if the result is not yet available
     */
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/log/err")
    @Produces("application/json")
    public String taskLogErrByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId,
                    "jobs/" + jobId + "/tasks/tag/" + taskTag + "/result/log/err");
            List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
            StringBuffer buf = new StringBuffer();
            for (TaskResult tr : trs) {
                if (tr.getOutput() != null) {
                    buf.append(tr.getOutput().getStderrLogs(true));
                }
            }
            return buf.toString();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Returns the standard output (stderr) generated by the task
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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/out")
    @Produces("application/json")
    public String taskLogout(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/" + taskname + "/result/log/out");
            TaskResult tr = s.getTaskResult(jobId, taskname);
            if ((tr != null) && (tr.getOutput() != null)) {
                return tr.getOutput().getStdoutLogs(true);
            } else {
                return "";
            }
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    /**
     *  Returns the standard output (stdout) generated by a set of tasks filtered by a given tag.
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskTag the tag used to filter the tasks.
     * @return  the stdout generated by the task or an empty string if the result is not yet available
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/result/log/out")
    @Produces("application/json")
    public String taskLogoutByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId,
                    "jobs/" + jobId + "/tasks/tag/" + taskTag + "/result/log/out");
            List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
            StringBuffer result = new StringBuffer();
            for (TaskResult tr : trs) {
                if (tr.getOutput() != null) {
                    result.append(tr.getOutput().getStdoutLogs(true));
                }
            }
            return result.toString();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Returns full logs generated by the task from user data spaces if task was run using the precious
     * logs option. Otherwise, logs are retrieved from the database. In this last case they may be truncated.
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
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/result/log/full")
    @Produces("application/json")
    public InputStream taskFullLogs(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname,
            @QueryParam("sessionid") String session)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException, IOException {
        try {

            if (sessionId == null) {
                sessionId = session;
            }

            Scheduler scheduler = checkAccess(sessionId,
                    "jobs/" + jobId + "/tasks/" + taskname + "/result/log/all");
            TaskResult taskResult = scheduler.getTaskResult(jobId, taskname);

            if (taskResult != null) {
                JobState jobState = scheduler.getJobState(taskResult.getTaskId().getJobId());

                boolean hasPreciousLogs = false;
                for (Task task : jobState.getTasks()) {
                    if (task.getName().equals(taskname)) {
                        hasPreciousLogs = task.isPreciousLogs();
                        break;
                    }
                }

                if (hasPreciousLogs) {
                    return retrieveTaskLogsUsingDataspaces(sessionId, jobId, taskResult.getTaskId());
                } else {
                    logger.warn("Retrieving truncated logs for task '" + taskname + "'");
                    return IOUtils.toInputStream(retrieveTaskLogsUsingDatabase(sessionId, jobId, taskname));
                }
            } else {
                return null;
            }
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    /**
     *  Returns task server logs
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskname the name of the task
     * @return task traces from the scheduler and resource manager
    */
    @Override
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/{taskname}/log/server")
    @Produces("application/json")
    public String taskServerLog(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("taskname") String taskname)
                    throws NotConnectedRestException, UnknownJobRestException,
                    UnknownTaskRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/" + taskname + "/log/server");
            return s.getTaskServerLogs(jobId, taskname);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    /**
     * Returns server logs for a set of tasks filtered by a given tag.
     * @param sessionId a valid session id
     * @param jobId the id of the job
     * @param taskTag the tag used to filter the tasks in the job.
     * @return task traces from the scheduler and resource manager
     */
    @GET
    @GZIP
    @Path("jobs/{jobid}/tasks/tag/{tasktag}/log/server")
    @Produces("application/json")
    public String taskServerLogByTag(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("jobid") String jobId,
            @PathParam("tasktag") String taskTag)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/tasks/tag/" + taskTag + "/log/server");
            return s.getTaskServerLogsByTag(jobId, taskTag);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * the method check is the session id is valid i.e. a scheduler client is
     * associated to the session id in the session map. If not, a
     * NotConnectedRestException is thrown specifying the invalid access *
     *
     * @return the scheduler linked to the session id, an NotConnectedRestException,
     *         if no such mapping exists.
     * @throws NotConnectedRestException
     */
    private SchedulerProxyUserInterface checkAccess(String sessionId, String path)
            throws NotConnectedRestException {
        Session session = sessionStore.get(sessionId);

        if (session == null) {
            throw new NotConnectedRestException(
                "You are not connected to the scheduler, you should log on first");
        }

        SchedulerProxyUserInterface schedulerProxy = session.getScheduler();

        if (schedulerProxy == null) {
            throw new NotConnectedRestException(
                "You are not connected to the scheduler, you should log on first");
        }

        renewLeaseForClient(schedulerProxy);

        return schedulerProxy;
    }

    private SchedulerProxyUserInterface checkAccess(String sessionId)
            throws NotConnectedRestException {
        return checkAccess(sessionId, "");
    }

    /**
     * Pauses the job represented by jobid
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return true if success, false if not
     */
    @Override
    @PUT
    @Path("jobs/{jobid}/pause")
    @Produces("application/json")
    public boolean pauseJob(
            @HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            final Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId + "/pause");
            return s.pauseJob(jobId);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Resumes the job represented by jobid
     *
     * @param sessionId
     *            a valid session id
     * @param jobId
     *            the id of the job
     * @return true if success, false if not
     */
    @Override
    @PUT
    @Path("jobs/{jobid}/resume")
    @Produces("application/json")
    public boolean resumeJob(
            @HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId + "/resume");
            return s.resumeJob(jobId);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Submit job using flat command file
     * @param sessionId valid session id
     * @param commandFileContent content of a command file: endline separated native commands
     * @param jobName name of the job to create
     * @param selectionScriptContent content of a selection script, or null
     * @param selectionScriptExtension extension of the selectionscript to determine script engine ("js", "py", "rb")
     * @return Id of the submitted job
     * @throws NotConnectedRestException
     * @throws IOException
     * @throws JobCreationRestException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     */
    @Override
    @POST
    @Path("submitflat")
    @Produces("application/json")
    public JobIdData submitFlat(
            @HeaderParam("sessionid") String sessionId,
            @FormParam("commandFileContent") String commandFileContent,
            @FormParam("jobName") String jobName,
            @FormParam("selectionScriptContent") String selectionScriptContent,
            @FormParam("selectionScriptExtension") String selectionScriptExtension)
                    throws NotConnectedRestException, IOException, JobCreationRestException,
                    PermissionRestException, SubmissionClosedRestException {
        Scheduler s = checkAccess(sessionId, "submitflat");

        try {
            File command = File.createTempFile("flatsubmit_commands_", ".txt");
            command.deleteOnExit();

            String selectionPath = null;
            File selection = null;

            if (selectionScriptContent != null && selectionScriptContent.trim().length() > 0) {
                selection = File.createTempFile("flatsubmit_selection_", "." + selectionScriptExtension);
                selection.deleteOnExit();
                PrintWriter pw = new PrintWriter(new FileOutputStream(selection));
                pw.print(selectionScriptContent);
                pw.close();
                selectionPath = selection.getAbsolutePath();
            }

            PrintWriter pw = new PrintWriter(new FileOutputStream(command));
            pw.print(commandFileContent);
            pw.close();

            Job j = FlatJobFactory.getFactory().createNativeJobFromCommandsFile(command.getAbsolutePath(),
                    jobName, selectionPath, null);
            JobId id = s.submit(j);

            command.delete();
            if (selection != null) {
                selection.delete();
            }

            return mapper.map(id, JobIdData.class);
        } catch (IOException e) {
            throw new IOException("I/O Error: " + e.getMessage(), e);
        } catch (JobCreationException e) {
            throw new JobCreationRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (SubmissionClosedException e) {
            throw new SubmissionClosedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
    }

    /**
     * Submits a workflow to the scheduler from a workflow URL,
     * creating hence a new job resource.
     *
     * @param sessionId a valid session id
     * @param url url to the workflow content
     * @param pathSegment variables of the workflow
     * @return the <code>jobid</code> of the newly created job
     * @throws NotConnectedRestException
     * @throws IOException
     * @throws JobCreationRestException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     */
    @Override
    @POST
    @Path("{path:jobs}")
    @Produces("application/json")
    public JobIdData submitFromUrl(
            @HeaderParam("sessionid") String sessionId,
            @HeaderParam("link") String url,
            @PathParam("path") PathSegment pathSegment)
                    throws JobCreationRestException, NotConnectedRestException,
                    PermissionRestException, SubmissionClosedRestException, IOException {
        Scheduler s = checkAccess(sessionId, "jobs");

        File tmpWorkflowFile = null;
        try {
            String jobXml = downloadWorkflowContent(sessionId, url);
            tmpWorkflowFile = File.createTempFile("job", "d");
            IOUtils.write(jobXml, new FileOutputStream(tmpWorkflowFile));

            WorkflowSubmitter workflowSubmitter = new WorkflowSubmitter(s);
            JobId jobId = workflowSubmitter.submit(tmpWorkflowFile,
                    getWorkflowVariablesFromPathSegment(pathSegment));

            return mapper.map(jobId, JobIdData.class);
        } catch (IOException e) {
            throw new IOException("Cannot save temporary job file on submission: " + e.getMessage(), e);
        } finally {
            FileUtils.deleteQuietly(tmpWorkflowFile);
        }
    }

    /**
     * Submits a job to the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return the <code>jobid</code> of the newly created job
     * @throws IOException
     *             if the job was not correctly uploaded/stored
     */
    @Override
    @POST
    @Path("{path:submit}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public JobIdData submit(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("path") PathSegment pathSegment,
            MultipartFormDataInput multipart)
                    throws JobCreationRestException, NotConnectedRestException,
                    PermissionRestException, SubmissionClosedRestException, IOException {
        try {
            Scheduler scheduler = checkAccess(sessionId, "submit");

            Map<String, List<InputPart>> formDataMap = multipart.getFormDataMap();

            String name = formDataMap.keySet().iterator().next();
            File tmpJobFile = null;
            try {

                InputPart part1 = multipart.getFormDataMap().get(name).get(0); // "file"

                String fileType = part1.getMediaType().toString().toLowerCase();
                if (!fileType.contains(MediaType.APPLICATION_XML.toLowerCase())) {
                    throw new JobCreationRestException("Unknown job descriptor type: " + fileType);
                }

                // is the name of the browser's input field
                InputStream is = part1.getBody(new GenericType<InputStream>() {
                });
                tmpJobFile = File.createTempFile("job", "d");

                IOUtils.copy(is, new FileOutputStream(tmpJobFile));

                Map<String, String> jobVariables = getWorkflowVariablesFromPathSegment(pathSegment);

                WorkflowSubmitter workflowSubmitter = new WorkflowSubmitter(scheduler);

                JobId jobId = workflowSubmitter.submit(tmpJobFile, jobVariables);

                return mapper.map(jobId, JobIdData.class);

            } finally {
                if (tmpJobFile != null) {
                    // clean the temporary file
                    tmpJobFile.delete();
                }
            }
        } catch (IOException e) {
            throw new IOException("I/O Error: " + e.getMessage(), e);
        }
    }

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
    @Override
    public boolean pushFile(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("spaceName") String spaceName,
            @PathParam("filePath") String filePath,
            MultipartFormDataInput multipart)
                    throws IOException, NotConnectedRestException, PermissionRestException {
        Scheduler s = checkAccess(sessionId, "pushFile");

        Map<String, List<InputPart>> formDataMap = multipart.getFormDataMap();

        List<InputPart> fNL = formDataMap.get("fileName");
        if ((fNL == null) || (fNL.size() == 0)) {
            throw new IllegalArgumentException(
                "Illegal multipart argument definition (fileName), received " + fNL);
        }
        String fileName = fNL.get(0).getBody(String.class, null);

        List<InputPart> fCL = formDataMap.get("fileContent");
        if ((fCL == null) || (fCL.size() == 0)) {
            throw new IllegalArgumentException(
                "Illegal multipart argument definition (fileContent), received " + fCL);
        }
        InputStream fileContent = fCL.get(0).getBody(InputStream.class, null);

        if (fileName == null) {
            throw new IllegalArgumentException("Wrong file name : " + fileName);
        }

        String spaceURI = resolveSpaceUri(s, spaceName);
        if (filePath == null) {
            filePath = "";
        }
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        String destUri = spaceURI + filePath;
        if (!destUri.endsWith("/")) {
            destUri += "/";
        }
        destUri += fileName;
        FileObject destfo = fsManager.resolveFile(destUri);
        if (!destfo.isWriteable()) {
            RuntimeException ex = new IllegalArgumentException(
                "File " + filePath + " is not writable in space " + spaceName);
            logger.error(ex);
            throw ex;
        }
        if (destfo.exists()) {
            destfo.delete();
        }
        // used to create the necessary directories if needed
        destfo.createFile();
        URL targetUrl = destfo.getURL();

        if (targetUrl.toString().startsWith("file:")) {
            // if the url is a file:// url, we push directly the InputStream to the destination file
            File targetFile = null;
            try {
                targetFile = new File(targetUrl.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
            logger.info("[pushFile] pushing input file to " + targetFile);
            try {
                FileUtils.copyInputStreamToFile(fileContent, targetFile);
            } catch (IOException e) {
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                throw e;
            }
        } else {
            // in the other case, we need to push the inputStream to a tempFile and then transfer the file via dataspaces
            File tmpFile = File.createTempFile("pushedFile", ".tmp");
            try {
                FileUtils.copyInputStreamToFile(fileContent, tmpFile);
                FileObject sourcefo = fsManager.resolveFile(tmpFile.getCanonicalPath());
                destfo.copyFrom(sourcefo, Selectors.SELECT_SELF);
            } finally {
                tmpFile.delete();
            }
        }

        return true;
    }

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
    @Override
    public InputStream pullFile(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("spaceName") String spaceName,
            @PathParam("filePath") String filePath)
                    throws IOException, NotConnectedRestException, PermissionRestException {

        Scheduler s = checkAccess(sessionId, "pullFile");

        String spaceURI = resolveSpaceUri(s, spaceName);
        if (filePath == null) {
            filePath = "";
        }
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        String destUri = spaceURI + filePath;
        FileObject sourcefo = fsManager.resolveFile(destUri);
        if (!sourcefo.exists() || !sourcefo.isReadable()) {
            RuntimeException ex = new IllegalArgumentException(
                "File " + filePath + " does not exist or is not readable in space " + spaceName);
            logger.error(ex);
            throw ex;
        }

        if (sourcefo.getType().equals(FileType.FOLDER)) {
            logger.info("[pullFile] reading directory content from " + sourcefo.getURL());
            // if it's a folder we return an InputStream listing its content
            StringBuilder sb = new StringBuilder();
            String nl = System.lineSeparator();
            for (FileObject fo : sourcefo.getChildren()) {
                sb.append(fo.getName().getBaseName() + nl);

            }
            return IOUtils.toInputStream(sb.toString());

        } else if (sourcefo.getType().equals(FileType.FILE)) {
            logger.info("[pullFile] reading file content from " + sourcefo.getURL());
            return sourcefo.getContent().getInputStream();
        } else {
            RuntimeException ex = new IllegalArgumentException(
                "File " + filePath + " has an unsupported type " + sourcefo.getType());
            logger.error(ex);
            throw ex;
        }

    }

    /**
     * Deletes a file or recursively delete a directory from the given DataSpace
     * @param sessionId a valid session id
     * @param spaceName the name of the data space involved (GLOBAL or USER)
     * @param filePath the path to the file or directory which must be deleted
     **/
    @Override
    public boolean deleteFile(
            @HeaderParam("sessionid") String sessionId,
            @PathParam("spaceName") String spaceName,
            @PathParam("filePath") String filePath)
                    throws IOException, NotConnectedRestException,
                    PermissionRestException {
        Scheduler s = checkAccess(sessionId, "deleteFile");

        String spaceURI = resolveSpaceUri(s, spaceName);
        if (filePath == null) {
            filePath = "";
        }
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        String destUri = spaceURI + filePath;

        FileObject sourcefo = fsManager.resolveFile(destUri);
        if (!sourcefo.exists() || !sourcefo.isWriteable()) {
            RuntimeException ex = new IllegalArgumentException(
                "File or Folder " + filePath + " does not exist or is not writable in space " + spaceName);
            logger.error(ex);
            throw ex;
        }
        if (sourcefo.getType().equals(FileType.FILE)) {
            logger.info("[deleteFile] deleting file " + sourcefo.getURL());
            sourcefo.delete();
        } else if (sourcefo.getType().equals(FileType.FOLDER)) {
            logger.info("[deleteFile] deleting folder (and all its descendants) " + sourcefo.getURL());
            sourcefo.delete(Selectors.SELECT_ALL);
        } else {
            RuntimeException ex = new IllegalArgumentException(
                "File " + filePath + " has an unsupported type " + sourcefo.getType());
            logger.error(ex);
            throw ex;
        }
        return true;
    }

    private String resolveSpaceUri(Scheduler s, String spaceName)
            throws NotConnectedRestException, PermissionRestException {
        try {
            if (SchedulerConstants.GLOBALSPACE_NAME.equals(spaceName)) {
                return s.getGlobalSpaceURIs().get(0);

            } else if (SchedulerConstants.USERSPACE_NAME.equals(spaceName)) {
                return s.getUserSpaceURIs().get(0);
            } else {
                RuntimeException ex = new IllegalArgumentException("Wrong Data Space name : " + spaceName);
                logger.error(ex);
                throw ex;
            }
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
    }

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
    @Override
    @PUT
    @Path("disconnect")
    @Produces("application/json")
    public void disconnect(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException {
        try {
            final Scheduler s = checkAccess(sessionId, "disconnect");
            logger.info("disconnection user " + sessionStore.get(sessionId) + " to session " + sessionId);
            s.disconnect();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } finally {
            sessionStore.terminate(sessionId);
            logger.debug("sessionid " + sessionId + " terminated");
        }
    }

    /**
     * pauses the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @PUT
    @Path("pause")
    @Produces("application/json")
    public boolean pauseScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "pause");
            return s.pause();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * stops the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @PUT
    @Path("stop")
    @Produces("application/json")
    public boolean stopScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "stop");
            return s.stop();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * resumes the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @PUT
    @Path("resume")
    @Produces("application/json")
    public boolean resumeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "resume");
            return s.resume();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

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
     * @throws UnknownJobRestException
     * @throws PermissionRestException
     * @throws JobAlreadyFinishedRestException
     */
    @Override
    @PUT
    @Path("jobs/{jobid}/priority/byname/{name}")
    public void schedulerChangeJobPriorityByName(
            @HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId,
            @PathParam("name") String priorityName)
                    throws NotConnectedRestException, UnknownJobRestException,
                    PermissionRestException, JobAlreadyFinishedRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/priority/byname/" + priorityName);
            s.changeJobPriority(jobId, JobPriority.findPriority(priorityName));
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (JobAlreadyFinishedException e) {
            throw new JobAlreadyFinishedRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        }
    }

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
     * @throws UnknownJobRestException
     * @throws PermissionRestException
     * @throws JobAlreadyFinishedRestException
     */
    @Override
    @PUT
    @Path("jobs/{jobid}/priority/byvalue/{value}")
    public void schedulerChangeJobPriorityByValue(
            @HeaderParam("sessionid") final String sessionId,
            @PathParam("jobid") final String jobId,
            @PathParam("value") String priorityValue)
                    throws NumberFormatException, NotConnectedRestException,
                    UnknownJobRestException, PermissionRestException, JobAlreadyFinishedRestException {
        try {
            Scheduler s = checkAccess(sessionId, "jobs/" + jobId + "/priority/byvalue" + priorityValue);
            s.changeJobPriority(jobId, JobPriority.findPriority(Integer.parseInt(priorityValue)));
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (JobAlreadyFinishedException e) {
            throw new JobAlreadyFinishedRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        }
    }

    /**
     * freezes the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @PUT
    @Path("freeze")
    @Produces("application/json")
    public boolean freezeScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "freeze");
            return s.freeze();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * returns the status of the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return the scheduler status
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @GET
    @Path("status")
    @Produces("application/json")
    public SchedulerStatusData getSchedulerStatus(
            @HeaderParam("sessionid") final String sessionId)
                    throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "status");
            return SchedulerStatusData
                    .valueOf(SchedulerStateListener.getInstance().getSchedulerStatus(s).name());
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * starts the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return true if success, false otherwise
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @PUT
    @Path("start")
    @Produces("application/json")
    public boolean startScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "start");
            return s.start();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * kills and shutdowns the scheduler
     *
     * @param sessionId
     *            a valid session id
     * @return true if success, false if not
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @PUT
    @Path("kill")
    @Produces("application/json")
    public boolean killScheduler(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "kill");
            return s.kill();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

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
    @Override
    @POST
    @Path("linkrm")
    @Produces("application/json")
    public boolean linkRm(
            @HeaderParam("sessionid") final String sessionId,
            @FormParam("rmurl") String rmURL)
                    throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "linkrm");
            return s.linkResourceManager(rmURL);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Tests whether or not the user is connected to the ProActive Scheduler
     *
     * @param sessionId
     *            the session to test
     * @return true if the user connected to a Scheduler, false otherwise.
     * @throws NotConnectedRestException
     */
    @Override
    @GET
    @Path("isconnected")
    @Produces("application/json")
    public boolean isConnected(@HeaderParam("sessionid")
    final String sessionId) throws NotConnectedRestException {
        Scheduler s = checkAccess(sessionId, "isconnected");
        return s.isConnected();
    }

    /**
     * Login to the scheduler using a form containing 2 fields (username and
     * password).
     *
     * @param username
     *            username
     * @param password
     *            password
     * @return the session id associated to the login.
     * @throws LoginException
     */
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("login")
    @Produces("application/json")
    public String login(
            @FormParam("username") String username,
            @FormParam("password") String password)
                    throws LoginException, SchedulerRestException {
        try {
            if ((username == null) || (password == null)) {
                throw new LoginException("Empty login/password");
            }
            Session session = sessionStore.create(username);
            session.connectToScheduler(new CredData(username, password));
            logger.info("Binding user " + username + " to session " + session.getSessionId());
            return session.getSessionId();
        } catch (ActiveObjectCreationException e) {
            throw new SchedulerRestException(e);
        } catch (SchedulerException e) {
            throw new SchedulerRestException(e);
        } catch (NodeException e) {
            throw new SchedulerRestException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PUT
    @Path("session")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/json")
    public String loginOrRenewSession(
            @HeaderParam("sessionid") String sessionId,
            @FormParam("username") String username,
            @FormParam("password") String password)
                    throws SchedulerRestException, LoginException, NotConnectedRestException {
        if (sessionId == null || !sessionStore.exists(sessionId)) {
            return login(username, password);
        }

        try {
            sessionStore.renewSession(sessionId);
            return sessionId;
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Login to the scheduler using a multipart form can be used either by
     * submitting 2 fields ({@code username} and {@code password}) or by sending a credential
     * file with field name {@code credential}.
     *
     * @return the session id associated to this new connection.
     * @throws KeyException
     * @throws LoginException
     * @throws SchedulerRestException
     */
    @Override
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces("application/json")
    public String loginWithCredential(@MultipartForm LoginForm multipart)
            throws LoginException, KeyException, SchedulerRestException {
        try {
            Session session;
            if (multipart.getCredential() != null) {
                Credentials credentials;
                try {
                    session = sessionStore.createUnnamedSession();
                    credentials = Credentials.getCredentials(multipart.getCredential());
                    session.connectToScheduler(credentials);
                } catch (IOException e) {
                    throw new LoginException(e.getMessage());
                }
            } else {
                if ((multipart.getUsername() == null) || (multipart.getPassword() == null)) {
                    throw new LoginException("empty login/password");
                }

                session = sessionStore.create(multipart.getUsername());
                CredData credData = new CredData(CredData.parseLogin(multipart.getUsername()),
                    CredData.parseDomain(multipart.getUsername()), multipart.getPassword(),
                    multipart.getSshKey());
                session.connectToScheduler(credData);
            }

            return session.getSessionId();

        } catch (PermissionException e) {
            throw new SchedulerRestException(e);
        } catch (ActiveObjectCreationException e) {
            throw new SchedulerRestException(e);
        } catch (SchedulerException e) {
            throw new SchedulerRestException(e);
        } catch (NodeException e) {
            throw new SchedulerRestException(e);
        }
    }

    /**
     * returns statistics about the scheduler
     *
     * @param sessionId
     *            the session id associated to this new connection
     * @return a string containing the statistics
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @GET
    @Path("stats")
    @Produces("application/json")
    public Map<String, String> getStatistics(
            @HeaderParam("sessionid") final String sessionId)
                    throws NotConnectedRestException, PermissionRestException {
        SchedulerProxyUserInterface s = checkAccess(sessionId, "stats");
        return s.getMappedInfo("ProActiveScheduler:name=RuntimeData");
    }

    /**
     * returns a string containing some data regarding the user's account
     *
     * @param sessionId
     *            the session id associated to this new connection
     * @return a string containing some data regarding the user's account
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @GET
    @Path("stats/myaccount")
    @Produces("application/json")
    public Map<String, String> getStatisticsOnMyAccount(
            @HeaderParam("sessionid") final String sessionId)
                    throws NotConnectedRestException, PermissionRestException {
        SchedulerProxyUserInterface s = checkAccess(sessionId, "stats/myaccount");
        return s.getMappedInfo("ProActiveScheduler:name=MyAccount");
    }

    /**
     * Users currently connected to the scheduler
     *
     * @param sessionId
     *            the session id associated to this new connection\
     * @return list of users
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @GET
    @GZIP
    @Path("users")
    @Produces("application/json")
    public List<SchedulerUserData> getUsers(
            @HeaderParam("sessionid") final String sessionId)
                    throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "users");
            return map(s.getUsers(), SchedulerUserData.class);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /**
     * Users having jobs in the scheduler
     *
     * @param sessionId
     *            the session id associated to this new connection\
     * @return list of users
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     */
    @Override
    @GET
    @GZIP
    @Path("userswithjobs")
    @Produces("application/json")
    public List<SchedulerUserData> getUsersWithJobs(
            @HeaderParam("sessionid") final String sessionId)
                    throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId, "userswithjobs");
            return map(s.getUsersWithJobs(), SchedulerUserData.class);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    private static <T> List<T> map(List<?> toMaps, Class<T> type) {
        List<T> result = new ArrayList<>(toMaps.size());
        for (Object toMap : toMaps) {
            result.add(mapper.map(toMap, type));
        }
        return result;
    }

    /**
     * returns the version of the rest api
     *
     * @return returns the version of the rest api
     */
    @GET
    @Path("version")
    public String getVersion() {
        return "{ " + "\"scheduler\" : \"" + SchedulerStateRest.class.getPackage().getSpecificationVersion() +
            "\", " + "\"rest\" : \"" + SchedulerStateRest.class.getPackage().getImplementationVersion() +
            "\"" + "}";
    }

    /**
     * generates a credential file from user provided credentials
     *
     * @return the credential file generated by the scheduler
     * @throws LoginException
     * @throws SchedulerRestException
     */
    @Override
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("createcredential")
    @Produces("*/*")
    public byte[] getCreateCredential(@MultipartForm LoginForm multipart)
            throws LoginException, SchedulerRestException {
        try {
            String url = PortalConfiguration.getProperties().getProperty(PortalConfiguration.scheduler_url);

            SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
            PublicKey pubKey = auth.getPublicKey();

            try {
                Credentials cred = Credentials
                        .createCredentials(new CredData(CredData.parseLogin(multipart.getUsername()),
                            CredData.parseDomain(multipart.getUsername()), multipart.getPassword(),
                            multipart.getSshKey()), pubKey);

                return cred.getBase64();
            } catch (KeyException e) {
                throw new SchedulerRestException(e);
            }
        } catch (ConnectionException e) {
            throw new SchedulerRestException(e);
        }
    }

    @GET
    @Path("usage/myaccount")
    @Produces("application/json")
    @Override
    public List<JobUsageData> getUsageOnMyAccount(
            @HeaderParam("sessionid") String sessionId,
            @QueryParam("startdate") @DateFormatter.DateFormat() Date startDate,
            @QueryParam("enddate") @DateFormatter.DateFormat() Date endDate)
                    throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler scheduler = checkAccess(sessionId);
            return map(scheduler.getMyAccountUsage(startDate, endDate), JobUsageData.class);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    @GET
    @Path("usage/account")
    @Produces("application/json")
    @Override
    public List<JobUsageData> getUsageOnAccount(@
            HeaderParam("sessionid") String sessionId,
            @QueryParam("user") String user,
            @QueryParam("startdate") @DateFormatter.DateFormat() Date startDate,
            @QueryParam("enddate") @DateFormatter.DateFormat() Date endDate)
                    throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler scheduler = checkAccess(sessionId);
            return map(scheduler.getAccountUsage(user, startDate, endDate), JobUsageData.class);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    @GET
    @Path("/userspace")
    @Produces("application/json")
    @Override
    public List<String> userspaceURIs(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedRestException, PermissionRestException {
        SchedulerProxyUserInterface proxy = checkAccess(sessionId);
        try {
            return proxy.getUserSpaceURIs();
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
    }

    @Override
    public JobValidationData validate(MultipartFormDataInput multipart) {
        File tmpFile = null;
        try {
            Map<String, List<InputPart>> formDataMap = multipart.getFormDataMap();
            String name = formDataMap.keySet().iterator().next();
            InputPart part1 = formDataMap.get(name).get(0);
            InputStream is = part1.getBody(new GenericType<InputStream>() {
            });

            tmpFile = File.createTempFile("valid-job", "d");
            IOUtils.copy(is, new FileOutputStream(tmpFile));

            return validateJobDescriptor(tmpFile);
        } catch (IOException e) {
            JobValidationData validation = new JobValidationData();
            validation.setErrorMessage("Cannot read from the job validation request.");
            validation.setStackTrace(getStackTrace(e));
            return validation;
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    @Override
    public void putThirdPartyCredential(String sessionId, String key, String value)
            throws NotConnectedRestException, PermissionRestException, SchedulerRestException {
        try {
            Scheduler s = checkAccess(sessionId);
            s.putThirdPartyCredential(key, value);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (KeyException e) {
            throw new SchedulerRestException(e);
        }
    }

    @Override
    public void removeThirdPartyCredential(String sessionId, String key)
            throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId);
            s.removeThirdPartyCredential(key);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    @Override
    public Set<String> thirdPartyCredentialsKeySet(String sessionId)
            throws NotConnectedRestException, PermissionRestException {
        try {
            Scheduler s = checkAccess(sessionId);
            return s.thirdPartyCredentialsKeySet();
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        }
    }

    /*
     * Atmosphere 2.0 framework based implementation of Scheduler Eventing mechanism for REST
     * clients. It is configured to use WebSocket as the underneath protocol between the client and
     * the server.
     */

    /**
     * Initialize WebSocket based communication channel between the client and the server.
     */
    @GET
    @Path("/events")
    public String subscribe(
            @Context HttpServletRequest req,
            @HeaderParam("sessionid") String sessionId)
                    throws NotConnectedRestException {
        checkAccess(sessionId);
        HttpSession session = checkNotNull(req.getSession(),
                "HTTP session object is null. HTTP session support is requried for REST Scheduler eventing.");
        AtmosphereResource atmosphereResource = checkNotNull(
                (AtmosphereResource) req.getAttribute(AtmosphereResource.class.getName()),
                "No AtmosphereResource is attached with current request.");
        // use session id as the 'topic' (or 'id') of the broadcaster
        session.setAttribute(ATM_BROADCASTER_ID, sessionId);
        session.setAttribute(ATM_RESOURCE_ID, atmosphereResource.uuid());
        Broadcaster broadcaster = lookupBroadcaster(sessionId, true);
        if (broadcaster != null) {
            atmosphereResource.setBroadcaster(broadcaster).suspend();
        }
        return null;
    }

    /**
     * Accepts an {@link EventSubscription} instance which specifies the types
     * of SchedulerEvents which interest the client. When such Scheduler event
     * occurs, it will be communicated to the client in the form of
     * {@link EventNotification} utilizing the WebSocket channel initialized
     * previously.
     */
    @POST
    @Path("/events")
    @Produces("application/json")
    public EventNotification publish(@Context HttpServletRequest req, EventSubscription subscription)
            throws NotConnectedRestException, PermissionRestException {
        HttpSession session = req.getSession();
        String broadcasterId = (String) session.getAttribute(ATM_BROADCASTER_ID);
        final SchedulerProxyUserInterface scheduler = checkAccess(broadcasterId);
        SchedulerEventBroadcaster eventListener = new SchedulerEventBroadcaster(broadcasterId);
        try {
            final SchedulerEventBroadcaster activedEventListener = PAActiveObject.turnActive(eventListener);
            scheduler.addEventListener(activedEventListener, subscription.isMyEventsOnly(),
                    EventUtil.toSchedulerEvents(subscription.getEvents()));

            AtmosphereResource atmResource = getAtmosphereResourceFactory()
                    .find((String) session.getAttribute(ATM_RESOURCE_ID));

            atmResource.addEventListener(new WebSocketEventListenerAdapter() {
                @Override
                public void onDisconnect(@SuppressWarnings("rawtypes") WebSocketEvent event) {
                    try {
                        scheduler.removeEventListener();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                    PAActiveObject.terminateActiveObject(activedEventListener, true);
                }
            });
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (ActiveObjectCreationException | NodeException e) {
            throw new RuntimeException(e);
        }

        return new EventNotification(EventNotification.Action.NONE, null, null);
    }

    private AtmosphereResourceFactory getAtmosphereResourceFactory() {
        return ((AtmosphereResource) httpServletRequest.getAttribute("org.atmosphere.cpr.AtmosphereResource"))
                .getAtmosphereConfig().resourcesFactory();
    }

    private Broadcaster lookupBroadcaster(String topic, boolean createNew) {
        AtmosphereResource atmosphereResource = (AtmosphereResource) httpServletRequest
                .getAttribute("org.atmosphere.cpr.AtmosphereResource");
        return atmosphereResource.getAtmosphereConfig().getBroadcasterFactory().lookup(topic, createNew);
    }

    @GET
    @Path("/")
    public Response index() throws URISyntaxException {
        return Response.seeOther(new URI("doc/jaxrsdocs/scheduler/index.html")).build();
    }

    private String downloadWorkflowContent(String sessionId, String workflowUrl)
            throws JobCreationRestException, IOException {
        if (StringUtils.isBlank(workflowUrl))
            throw new JobCreationRestException("Cannot create workflow without url");
        HttpResourceDownloader httpResourceDownloader = new HttpResourceDownloader();
        return httpResourceDownloader.getResource(sessionId, workflowUrl, String.class);
    }

    private Map<String, String> getWorkflowVariablesFromPathSegment(PathSegment pathSegment) {
        Map<String, String> variables = null;
        MultivaluedMap<String, String> matrixParams = pathSegment.getMatrixParameters();
        if (matrixParams != null && !matrixParams.isEmpty()) {
            variables = Maps.newHashMap();
            for (String key : matrixParams.keySet()) {
                variables.put(key, matrixParams.getFirst(key));
            }
        }
        return variables;
    }

    private boolean isXmlWorkflow(InputPart fileInputPart) {
        return fileInputPart.getMediaType().toString().toLowerCase()
                .contains(MediaType.APPLICATION_XML.toLowerCase());
    }

    protected static Map<String, String> createSortableTaskAttrMap() {
        HashMap<String, String> sortableTaskAttrMap = new HashMap<>(13);
        sortableTaskAttrMap.put("id", "id.taskId");
        sortableTaskAttrMap.put("status", "taskStatus");
        sortableTaskAttrMap.put("name", "taskName");
        sortableTaskAttrMap.put("tag", "tag");
        sortableTaskAttrMap.put("execDuration", "executionDuration");
        sortableTaskAttrMap.put("nodeCount", "parallelEnvNodesNumber");
        sortableTaskAttrMap.put("executions", "numberOfExecutionLeft");
        sortableTaskAttrMap.put("nodeFailure", "numberOfExecutionOnFailureLeft");
        sortableTaskAttrMap.put("host", "executionHostName");
        sortableTaskAttrMap.put("startTime", "startTime");
        sortableTaskAttrMap.put("finishedTime", "finishedTime");
        sortableTaskAttrMap.put("description", "description");
        sortableTaskAttrMap.put("scheduledAt", "scheduledTime");
        return sortableTaskAttrMap;
    }

    @Override
    public RestPage<String> getTaskIds(String sessionId, long from, long to, boolean mytasks, boolean running,
            boolean pending, boolean finished, int offset, int limit)
                    throws NotConnectedRestException, PermissionRestException {
        return getTaskIdsByTag(sessionId, null, from, to, mytasks, running, pending, finished, offset, limit);
    }
    
    @Override
    public RestPage<String> getTaskIdsByTag(String sessionId, String taskTag, long from, long to, boolean mytasks,
            boolean running, boolean pending, boolean finished, int offset, int limit)
                    throws NotConnectedRestException, PermissionRestException {
        Scheduler s = checkAccess(sessionId, "tasks");

        PageBoundaries boundaries = Pagination.getTasksPageBoundaries(offset, limit, TASKS_PAGE_SIZE);

        Page<TaskId> page = null;
        try {
            page = s.getTaskIds(taskTag, from, to, mytasks, running, pending, finished,
                    boundaries.getOffset(), boundaries.getLimit());
            List<TaskId> taskIds = page.getList();
            List<String> taskNames = new ArrayList<>(taskIds.size());
            for (TaskId taskId : taskIds) {
                taskNames.add(taskId.getReadableName());
            }
            return new RestPage<String>(taskNames, page.getSize());
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
    }

    @Override
    public RestPage<TaskStateData> getTaskStates(String sessionId, long from, long to, boolean mytasks,
                                                 boolean running, boolean pending, boolean finished,
                                                 int offset, int limit, SortSpecifierContainer sortParams)
            throws NotConnectedRestException, PermissionRestException {
        return getTaskStatesByTag(sessionId, null, from, to, mytasks, running, pending, finished,
                offset, limit, mapToDBNamespace(sortParams));
    }

    @Override
    public RestPage<TaskStateData> getTaskStatesByTag(String sessionId, String taskTag, long from, long to,
                                                      boolean mytasks, boolean running, boolean pending, boolean finished,
                                                      int offset, int limit, SortSpecifierContainer sortParams)
            throws NotConnectedRestException, PermissionRestException {
        Scheduler s = checkAccess(sessionId, "tasks/tag/" + taskTag);

        PageBoundaries boundaries = Pagination.getTasksPageBoundaries(offset, limit, TASKS_PAGE_SIZE);

        Page<TaskState> page = null;

        // if that method is called directly from REST without any sorting parameters
        // sortParams will be null
        if (sortParams == null) {
            sortParams = new SortSpecifierContainer();
        }
        try {
            page = s.getTaskStates(taskTag, from, to, mytasks, running, pending, finished,
                    boundaries.getOffset(), boundaries.getLimit(), sortParams);
            List<TaskStateData> tasks = map(page.getList(), TaskStateData.class);
            return new RestPage<TaskStateData>(tasks, page.getSize());
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
    }

    /**
     * Translates the tasks attributes names that are used to sort the result
     * For example the task status is called `status` client-side, it is represented by
     * `taskStatus` in the DB
     * @param sortParams  The sort parameters using the client-side namespace
     * @return the sort parameters using the DB namespace
     */
    private SortSpecifierContainer mapToDBNamespace(SortSpecifierContainer sortParams) {
        SortSpecifierContainer filteredSorts = new SortSpecifierContainer();
        if (sortParams != null) {
            for (SortSpecifierContainer.SortSpecifierItem i : sortParams.getSortParameters()) {
                if (sortableTaskAttrMap.containsKey(i.getField())) {
                    filteredSorts.add(sortableTaskAttrMap.get(i.getField()), i.getOrder());
                }
            }
        }
        return filteredSorts;
    }

}
