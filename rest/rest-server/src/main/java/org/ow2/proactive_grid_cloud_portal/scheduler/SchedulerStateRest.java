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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.METADATA_CONTENT_TYPE;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.METADATA_FILE_EXTENSION;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.METADATA_FILE_NAME;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.log4j.Logger;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.*;
import org.ow2.proactive.scheduler.common.exception.*;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.job.factories.FlatJobFactory;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.common.util.PageBoundaries;
import org.ow2.proactive.scheduler.common.util.Pagination;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.TaskLoggerRelativePathGenerator;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.signal.SignalApiException;
import org.ow2.proactive.web.WebProperties;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SessionStore;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.dataspace.RestDataspaceImpl;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.*;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventNotification;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.eventing.EventSubscription;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.*;
import org.ow2.proactive_grid_cloud_portal.scheduler.util.EventUtil;
import org.ow2.proactive_grid_cloud_portal.scheduler.util.ValidationUtil;
import org.ow2.proactive_grid_cloud_portal.scheduler.util.WorkflowVariablesTransformer;
import org.ow2.proactive_grid_cloud_portal.webapp.DateFormatter;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;


/**
 * This class exposes the Scheduler as a RESTful service.
 */
@Path("/scheduler/")
public class SchedulerStateRest implements SchedulerRestInterface {

    private static final Logger logger = ProActiveLogger.getLogger(SchedulerStateRest.class);

    /**
     * If the rest api was unable to instantiate the value from byte array
     * representation
     */
    public static final String UNKNOWN_VALUE_TYPE = "Unknown value type";

    private static final String ATM_BROADCASTER_ID = "atmosphere.broadcaster.id";

    private static final String ATM_RESOURCE_ID = "atmosphere.resource.id";

    private static final String NL = System.getProperty("line.separator");

    public static final String YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST = "You are not connected to the scheduler, you should log on first";

    private static final String VARIABLES_KEY = "variables";

    public static final String ASC_SUFFIX = "_a";

    public static final String DESC_SUFFIX = "_d";

    private final SessionStore sessionStore = SharedSessionStore.getInstance();

    private static RestDataspaceImpl dataspaceRestApi = new RestDataspaceImpl();

    private static Map<String, String> sortableTaskAttrMap = null;

    private static final int TASKS_PAGE_SIZE = PASchedulerProperties.TASKS_PAGE_SIZE.getValueAsInt();

    private static final String FILE_ENCODING = PASchedulerProperties.FILE_ENCODING.getValueAsString();

    private static final String PATH_JOBS = "jobs/";

    private static final String PATH_TASKS = "/tasks/";

    private static final String PATH_SERVICES = "/services";

    public static final String DESTINATION_BROWSER = "browser";

    public static final String DESTINATION_FILE = "file";

    public static final String BUCKET_NAME_GI = "bucketName";

    public static final String WORKFLOW_ICON_GI = "workflow.icon";

    public static final String DOCUMENTATION_GI = "Documentation";

    static {
        sortableTaskAttrMap = createSortableTaskAttrMap();
    }

    protected static final List<SortParameter<JobSortParameter>> DEFAULT_JOB_SORT_PARAMS = Arrays.asList(new SortParameter<>(JobSortParameter.STATE,
                                                                                                                             SortOrder.ASC),
                                                                                                         new SortParameter<>(JobSortParameter.ID,
                                                                                                                             SortOrder.DESC));

    private static final Mapper mapper = new DozerBeanMapper(Collections.singletonList("org/ow2/proactive_grid_cloud_portal/scheduler/dozer-mappings.xml"));

    @Context
    private HttpServletRequest httpServletRequest;

    private final WorkflowVariablesTransformer workflowVariablesTransformer = new WorkflowVariablesTransformer();

    private static List<SortParameter<JobSortParameter>> createJobSortParams(String sortString) {
        String[] sortParams = sortString.split(",");
        List<SortParameter<JobSortParameter>> jobSortParamsList = new ArrayList<>(sortParams.length);
        for (String param : sortParams) {
            SortOrder order;
            if (param.endsWith(ASC_SUFFIX)) {
                order = SortOrder.ASC;
                param = param.substring(0, param.length() - ASC_SUFFIX.length());
            } else if (param.endsWith(DESC_SUFFIX)) {
                order = SortOrder.DESC;
                param = param.substring(0, param.length() - DESC_SUFFIX.length());
            } else {
                throw new IllegalArgumentException("Invalid sort order in " + sortString);
            }
            JobSortParameter jobSortParameter = JobSortParameter.valueOf(param);
            jobSortParamsList.add(new SortParameter<>(jobSortParameter, order));
        }
        return jobSortParamsList;
    }

    @Override
    public String getUrl() {
        return PortalConfiguration.SCHEDULER_URL.getValueAsString();
    }

    @Override
    public RestPage<String> jobs(String sessionId, int index, int limit) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobs");

            Page<JobInfo> page = s.getJobs(index,
                                           limit,
                                           new JobFilterCriteria(false, true, true, true, true),
                                           DEFAULT_JOB_SORT_PARAMS);

            List<String> ids = new ArrayList<>(page.getList().size());
            for (JobInfo jobInfo : page.getList()) {
                ids.add(jobInfo.getJobId().value());
            }
            return new RestPage<>(ids, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean checkJobPermissionMethod(String sessionId, String method, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/job/" + jobId + "/permission");
            return s.checkJobPermissionMethod(jobId, method);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<String> checkJobsPermissionMethod(String sessionId, String method, List<String> jobsId)
            throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobs/permission");
            return s.checkJobsPermissionMethod(jobsId, method);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Set<String> addJobSignal(String sessionId, String signal, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobs/" + jobId);
            return s.addJobSignal(jobId, signal);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<UserJobData> jobsInfo(String sessionId, int index, int limit) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobsinfo");

            Page<JobInfo> page = s.getJobs(index,
                                           limit,
                                           new JobFilterCriteria(false, true, true, true, true),
                                           DEFAULT_JOB_SORT_PARAMS);
            List<UserJobData> userJobInfoList = new ArrayList<>(page.getList().size());
            for (JobInfo jobInfo : page.getList()) {
                userJobInfoList.add(new UserJobData(mapper.map(jobInfo, JobInfoData.class)));
            }

            return new RestPage<>(userJobInfoList, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<UserJobData> jobsInfoList(String sessionId, List<String> jobsId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobsinfolist");
            List<JobInfo> jobInfoList = s.getJobsInfoList(jobsId);
            return jobInfoList.stream()
                              .map(jobInfo -> new UserJobData(mapper.map(jobInfo, JobInfoData.class)))
                              .collect(Collectors.toList());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<UserJobData> jobsInfoListPost(String sessionId, List<String> jobsId) throws RestException {
        return jobsInfoList(sessionId, jobsId);
    }

    @Override
    public RestMapPage<Long, ArrayList<UserJobData>> revisionAndJobsInfo(String sessionId, int index, int limit,
            boolean myJobs, boolean pending, boolean running, boolean finished, boolean childJobs, String sortParams)
            throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "revisionjobsinfo?index=" + index + "&limit=" + limit);
            String user = sessionStore.get(sessionId).getUserName();

            boolean onlyUserJobs = (myJobs && user != null && user.trim().length() > 0);
            List<SortParameter<JobSortParameter>> sortParameterList;
            if (Strings.isNullOrEmpty(sortParams)) {
                sortParameterList = DEFAULT_JOB_SORT_PARAMS;
            } else {
                try {
                    sortParameterList = createJobSortParams(sortParams);
                } catch (Exception e) {
                    logger.warn("Invalid sort parameter string, using default : " + sortParams);
                    sortParameterList = DEFAULT_JOB_SORT_PARAMS;
                }
            }

            Page<JobInfo> page = s.getJobs(index,
                                           limit,
                                           new JobFilterCriteria(onlyUserJobs, pending, running, finished, childJobs),
                                           sortParameterList);
            List<JobInfo> jobsInfo = page.getList();
            ArrayList<UserJobData> jobs = new ArrayList<>(jobsInfo.size());
            for (JobInfo jobInfo : jobsInfo) {
                jobs.add(new UserJobData(mapper.map(jobInfo, JobInfoData.class)));
            }

            HashMap<Long, ArrayList<UserJobData>> map = new HashMap<>(1);
            map.put(SchedulerStateListener.getInstance().getSchedulerStateRevision(), jobs);
            RestMapPage<Long, ArrayList<UserJobData>> restMapPage = new RestMapPage<>();
            restMapPage.setMap(map);
            restMapPage.setSize(page.getSize());
            return restMapPage;
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public long schedulerStateRevision(String sessionId) throws NotConnectedRestException {
        checkAccess(sessionId, "/scheduler/revision");
        return SchedulerStateListener.getInstance().getSchedulerStateRevision();
    }

    @Override
    public JobStateData listJobs(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "/scheduler/jobs/" + jobId);

            JobState js = s.getJobState(jobId);
            js = PAFuture.getFutureValue(js);

            return mapper.map(js, JobStateData.class);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String getLiveLogJob(String sessionId, String jobId, boolean allLogs) throws NotConnectedRestException,
            UnknownJobRestException, PermissionRestException, LogForwardingRestException, IOException {
        try {
            Scheduler scheduler = checkAccess(sessionId, "/scheduler/jobs/" + jobId + "/livelog");
            Session session = sessionStore.get(sessionId);

            JobState jobState = scheduler.getJobState(jobId);
            boolean isFinished = jobState != null && jobState.isFinished();
            int availableLinesCount = session.getJobsOutputController().availableLinesCount(jobId);
            if (allLogs) {
                if (!isFinished || availableLinesCount > 0) {
                    return session.getJobsOutputController().getAllLogs(jobId);
                } else {
                    session.getJobsOutputController().removeAppender(jobId);
                    return "";
                }
            } else {

                if (!isFinished || availableLinesCount > 0) {
                    return session.getJobsOutputController().getNewLogs(jobId);
                } else {
                    session.getJobsOutputController().removeAppender(jobId);
                    return "";
                }
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

    @Override
    public int getLiveLogJobAvailable(String sessionId, String jobId) throws NotConnectedRestException {
        checkAccess(sessionId, "/scheduler/jobs/" + jobId + "/livelog/available");
        Session ss = sessionStore.get(sessionId);
        return ss.getJobsOutputController().availableLinesCount(jobId);
    }

    @Override
    public boolean deleteLiveLogJob(String sessionId, String jobId) throws NotConnectedRestException {
        checkAccess(sessionId, "delete /scheduler/jobs/livelog" + jobId);
        Session ss = sessionStore.get(sessionId);
        ss.getJobsOutputController().removeAppender(jobId);
        return true;

    }

    @Override
    public void enableRemoteVisualization(String sessionId, String jobId, String taskName, String connectionString)
            throws RestException {
        Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobId + PATH_TASKS + taskName + "/visualization");
        Session ss = sessionStore.get(sessionId);
        try {
            ss.getScheduler().enableRemoteVisualization(jobId, taskName, connectionString);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        } catch (UnknownTaskException e) {
            throw new UnknownTaskRestException(e);
        }
    }

    @Override
    public void registerService(String sessionId, String jobId, int serviceInstanceid, boolean enableActions)
            throws RestException {
        Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId + PATH_SERVICES);
        Session ss = sessionStore.get(sessionId);
        try {
            ss.getScheduler().registerService(jobId, serviceInstanceid, enableActions);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        }
    }

    @Override
    public void detachService(String sessionId, String jobId, int serviceInstanceid) throws RestException {
        Scheduler s = checkAccess(sessionId, "DELETE jobs/" + jobId + PATH_SERVICES);
        Session ss = sessionStore.get(sessionId);
        try {
            ss.getScheduler().detachService(jobId, serviceInstanceid);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        } catch (UnknownJobException e) {
            throw new UnknownJobRestException(e);
        }
    }

    @Override
    public JobResultData jobResult(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/result");
            return mapper.map(PAFuture.getFutureValue(s.getJobResult(jobId)), JobResultData.class);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<String, String> jobResultMap(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/resultmap");
            Map<Long, Map<String, Serializable>> maps = PAFuture.getFutureValue(s.getJobResultMaps(Collections.singletonList(jobId)));
            Map<String, Serializable> resultMap = maps.get(Long.valueOf(jobId));
            if (resultMap == null) {
                return null;
            } else {
                return getJobResultMapAsString(resultMap);
            }
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<Long, Map<String, String>> jobResultMaps(String sessionId, List<String> jobsId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + "/resultmap");
            Map<Long, Map<String, Serializable>> maps = PAFuture.getFutureValue(s.getJobResultMaps(jobsId));
            if (maps != null) {
                return maps.entrySet()
                           .stream()
                           .collect(Collectors.toMap(Entry::getKey,
                                                     entry -> getJobResultMapAsString(entry.getValue())));
            }
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
        return new HashMap<>();
    }

    public Map getJobResultMapAsString(Map<String, Serializable> source) {
        if (source == null) {
            return null;
        }
        return source.entrySet()
                     .stream()
                     .collect(Collectors.toMap(Entry::getKey,
                                               entry -> entry.getValue() != null ? entry.getValue().toString() : ""));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobInfoData jobInfo(String sessionId, String jobId) throws RestException {
        Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/info");
        try {
            return mapper.map(s.getJobInfo(jobId), JobInfoData.class);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<String, String> jobResultValue(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/result/value");
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
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean removeJob(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "DELETE jobs/" + jobId);
            return s.removeJob(jobId);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean removeJobs(String sessionId, List<String> jobsId, long olderThan) throws RestException {
        try {
            // checking permissions
            Scheduler s = checkAccess(sessionId, "DELETE jobs");
            if (jobsId != null && !jobsId.isEmpty()) {
                return s.removeJobs(jobsId.stream().map(JobIdImpl::makeJobId).collect(Collectors.toList()));
            } else if (olderThan > 0) {
                return s.removeJobs(olderThan);
            } else {
                throw new IllegalArgumentException("Either jobsId or olderThan parameter must be provided");
            }
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String jobServerLog(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/log/server");
            return s.getJobServerLogs(jobId);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean killJob(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobId + "/kill");
            return s.killJob(jobId);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean killJobs(String sessionId, List<String> jobsId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/kill");
            return s.killJobs(jobsId);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean killTask(String sessionId, String jobid, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobid + PATH_TASKS + taskname + "/kill");
            return s.killTask(jobid, taskname);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean preemptTask(String sessionId, String jobid, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobid + PATH_TASKS + taskname + "/preempt");
            return s.preemptTask(jobid, taskname, 5);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean restartTask(String sessionId, String jobid, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobid + PATH_TASKS + taskname + "/restart");
            return s.restartTask(jobid, taskname, 5);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean finishInErrorTask(String sessionId, String jobid, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobid + PATH_TASKS + taskname + "/finishInErrorTask");
            return s.finishInErrorTask(jobid, taskname);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean restartInErrorTask(String sessionId, String jobid, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "PUT jobs/" + jobid + PATH_TASKS + taskname + "/restartInErrorTask");
            return s.restartInErrorTask(jobid, taskname);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<String> getTasksNames(String sessionId, String jobId) throws RestException {
        return getTasksNamesPaginated(sessionId, jobId, 0, TASKS_PAGE_SIZE);
    }

    @Override
    public RestPage<String> getTasksNamesPaginated(String sessionId, String jobId, int offset, int limit)
            throws RestException {
        if (limit == -1)
            limit = TASKS_PAGE_SIZE;
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks");

            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTasksPaginated(offset, limit);
            List<String> tasksNames = new ArrayList<>(page.getTaskStates().size());
            for (TaskState ts : page.getTaskStates()) {
                tasksNames.add(ts.getId().getReadableName());
            }
            return new RestPage<>(tasksNames, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<String> getJobTasksIdsByTag(String sessionId, String jobId, String taskTag) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks");

            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagPaginated(taskTag, 0, TASKS_PAGE_SIZE);
            List<TaskState> tasks = page.getTaskStates();
            List<String> tasksName = new ArrayList<>(tasks.size());
            for (TaskState ts : tasks) {
                tasksName.add(ts.getId().getReadableName());
            }

            return new RestPage<>(tasksName, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<String> getJobTasksIdsByTagPaginated(String sessionId, String jobId, String taskTag, int offset,
            int limit) throws RestException {
        if (limit == -1)
            limit = TASKS_PAGE_SIZE;
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskTag + "/paginated");

            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagPaginated(taskTag, offset, limit);
            List<TaskState> tasks = page.getTaskStates();
            List<String> tasksName = new ArrayList<>(tasks.size());

            for (TaskState ts : tasks) {
                tasksName.add(ts.getId().getReadableName());
            }

            return new RestPage<>(tasksName, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<String> getJobTaskTags(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tags");
            JobState jobState = s.getJobState(jobId);
            return jobState.getTags();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<String> getJobTaskTagsPrefix(String sessionId, String jobId, String prefix) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tags/startswith/" + prefix);
            JobState jobState = s.getJobState(jobId);
            return jobState.getTags(prefix);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJobHtml(String sessionId, @PathParam("jobid") String jobId)
            throws NotConnectedRestException, IOException {
        checkAccess(sessionId);

        File jobHtml = new File(PortalConfiguration.jobIdToPath(jobId) + ".html");
        if (!jobHtml.exists()) {
            throw new IOException("the file " + jobHtml.getAbsolutePath() + " was not found on the server");
        }
        try (InputStream ips = new BufferedInputStream(new FileInputStream(jobHtml))) {
            return new String(IOUtils.toByteArray(ips));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJobContent(String sessionId, String jobId) throws NotConnectedRestException, RestException {
        try {
            Scheduler scheduler = checkAccess(sessionId, "/scheduler/jobs/" + jobId + "/xml");
            return scheduler.getJobContent(JobIdImpl.makeJobId(jobId));
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<TaskStateData> getJobTaskStates(String sessionId, String jobId) throws RestException {
        return getJobTaskStatesPaginated(sessionId, jobId, 0, TASKS_PAGE_SIZE);
    }

    @Override
    public List<TaskStateData> getJobTaskStatesWithVisualization(String sessionId, String jobId) throws RestException {
        List<TaskStateData> answer = new ArrayList<>();
        try {
            Scheduler scheduler = checkAccess(sessionId, PATH_JOBS + jobId + "/taskstates/visualization");
            JobState jobState = scheduler.getJobState(jobId);
            for (TaskState task : jobState.getTasks()) {
                if (task.getTaskInfo().isVisualizationActivated()) {
                    answer.add(mapper.map(task, TaskStateData.class));
                }
            }
            return answer;
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<TaskStateData> getJobTaskStatesPaginated(String sessionId, String jobId, int offset, int limit)
            throws RestException {
        if (limit == -1)
            limit = TASKS_PAGE_SIZE;
        try {
            Scheduler scheduler = checkAccess(sessionId, PATH_JOBS + jobId + "/taskstates/paginated");
            TaskStatesPage page = scheduler.getTaskPaginated(jobId, offset, limit);
            List<TaskStateData> tasks = map(page.getTaskStates(), TaskStateData.class);
            return new RestPage<>(tasks, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<TaskStateData> getJobTaskStatesFilteredPaginated(String sessionId, String jobId, int offset,
            int limit, String statusFilter) throws RestException {
        if (limit == -1)
            limit = TASKS_PAGE_SIZE;
        try {
            Scheduler scheduler = checkAccess(sessionId, PATH_JOBS + jobId + "/taskstates/paginated");
            TaskStatesPage page = scheduler.getTaskPaginated(jobId, statusFilter, offset, limit);
            List<TaskStateData> tasks = map(page.getTaskStates(), TaskStateData.class);
            return new RestPage<>(tasks, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<TaskStateData> getJobTaskStatesByTag(String sessionId, String jobId, String taskTag)
            throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/taskstates/" + taskTag);
            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagPaginated(taskTag, 0, TASKS_PAGE_SIZE);
            List<TaskStateData> tasks = map(page.getTaskStates(), TaskStateData.class);
            return new RestPage<>(tasks, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestPage<TaskStateData> getJobTaskStatesByTagPaginated(String sessionId, String jobId, String taskTag,
            int offset, int limit) throws RestException {
        if (limit == -1)
            limit = TASKS_PAGE_SIZE;
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/taskstates/" + taskTag + "/paginated");
            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagPaginated(taskTag, offset, limit);
            List<TaskStateData> tasks = map(page.getTaskStates(), TaskStateData.class);
            return new RestPage<>(tasks, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<TaskStateData> getJobTaskStatesByTagByStatusPaginated(String sessionId, String jobId, int offset,
            int limit, String taskTag, String statusFilter) throws RestException {
        if (limit == -1)
            limit = TASKS_PAGE_SIZE;
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/taskstates/" + taskTag + "/paginated");
            JobState jobState = s.getJobState(jobId);
            TaskStatesPage page = jobState.getTaskByTagByStatusPaginated(offset, limit, taskTag, statusFilter);
            List<TaskStateData> tasks = map(page.getTaskStates(), TaskStateData.class);
            return new RestPage<>(tasks, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Response jobFullLogs(String sessionId, String jobId, String session, String destination)
            throws RestException {

        if (sessionId == null) {
            sessionId = session;
        }

        try {
            Scheduler scheduler = checkAccess(sessionId, PATH_JOBS + jobId + "/log/full");

            if (destination == null) {
                destination = DESTINATION_BROWSER;
            }

            Response.ResponseBuilder builder;

            JobState jobState = scheduler.getJobState(jobId);

            List<TaskState> tasks = jobState.getTasks();
            List<InputStream> streams = new ArrayList<>(tasks.size());

            tasks.sort(TaskState.COMPARE_BY_FINISHED_TIME_ASC);

            for (TaskState taskState : tasks) {

                InputStream inputStream = retrieveTaskLogs(taskState, sessionId, jobId);

                if (inputStream != null) {
                    streams.add(inputStream);
                }
            }

            builder = Response.ok()
                              .entity(streams.isEmpty() ? IOUtils.toInputStream("",
                                                                                Charset.forName(PASchedulerProperties.FILE_ENCODING.getValueAsString()))
                                                        : new SequenceInputStream(Collections.enumeration(streams)));

            if (DESTINATION_FILE.equals(destination)) {
                builder = builder.header(HttpHeaders.CONTENT_DISPOSITION,
                                         "attachment; filename=job_" + jobId + "_logs.log");
            }

            return builder.build();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    protected InputStream retrieveTaskLogs(TaskState taskState, String sessionId, String jobId) {
        InputStream inputStream = null;
        try {
            if (taskState.isPreciousLogs()) {
                inputStream = retrieveTaskLogsUsingDataspaces(sessionId, jobId, taskState.getId());
            } else {
                String taskLogs = retrieveTaskLogsUsingDatabase(sessionId, jobId, taskState.getName());

                if (!taskLogs.isEmpty()) {
                    inputStream = IOUtils.toInputStream(taskLogs, Charset.forName(FILE_ENCODING));
                }

                logger.warn("Retrieving truncated logs for task '" + taskState.getId() + "'");
            }
        } catch (Exception e) {
            logger.info("Could not retrieve logs for task " + taskState.getId() +
                        " (could be a non finished or killed task)", e);
        }
        return inputStream;
    }

    public InputStream retrieveTaskLogsUsingDataspaces(String sessionId, String jobId, TaskId taskId)
            throws PermissionRestException, IOException, NotConnectedRestException {
        return pullFile(sessionId,
                        SchedulerConstants.USERSPACE_NAME,
                        new TaskLoggerRelativePathGenerator(taskId).getRelativePath());
    }

    @Override
    public TaskStateData jobTask(String sessionId, String jobId, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname);

            JobState jobState = s.getJobState(jobId);

            for (TaskState ts : jobState.getTasks()) {
                if (ts.getId().getReadableName().equals(taskname)) {
                    return mapper.map(ts, TaskStateData.class);
                }
            }

            throw new UnknownTaskRestException("task " + taskname + "not found");
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String valueOfTaskResult(String sessionId, String jobId, String taskname) throws RestException {
        Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname + "/result/value");
        try {
            TaskResult taskResult = s.getTaskResult(jobId, taskname);
            return getTaskResultValueAsStringOrExceptionStackTrace(taskResult);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<String, String> valueOfTaskResultByTag(String sessionId, String jobId, String taskTag)
            throws RestException {
        Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tag/" + taskTag + "/result/value");
        try {
            List<TaskResult> taskResults = s.getTaskResultsByTag(jobId, taskTag);
            Map<String, String> result = new HashMap<>(taskResults.size());
            for (TaskResult currentTaskResult : taskResults) {
                result.put(currentTaskResult.getTaskId().getReadableName(),
                           getTaskResultValueAsStringOrExceptionStackTrace(currentTaskResult));
            }
            return result;
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
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

    @Override
    public Map<String, String> metadataOfTaskResult(String sessionId, String jobId, String taskname)
            throws RestException {
        Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname + "/result/value");
        try {
            TaskResult taskResult = s.getTaskResult(jobId, taskname);
            taskResult = PAFuture.getFutureValue(taskResult);
            return taskResult.getMetadata();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<String, Map<String, String>> metadataOfTaskResultByTag(String sessionId, String jobId, String taskTag)
            throws RestException {
        Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tag" + taskTag + "/result/serializedvalue");
        try {
            List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
            Map<String, Map<String, String>> result = new HashMap<>(trs.size());
            for (TaskResult currentResult : trs) {
                TaskResult r = PAFuture.getFutureValue(currentResult);
                result.put(r.getTaskId().getReadableName(), r.getMetadata());
            }
            return result;
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<String> getPreciousTaskName(String sessionId, String jobId) throws RestException {
        Scheduler scheduler = checkAccess(sessionId, "metadataOfPreciousResults");
        try {
            return scheduler.getPreciousTaskResults(jobId)
                            .stream()
                            .map(TaskResult::getTaskId)
                            .map(TaskId::getReadableName)
                            .collect(Collectors.toList());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<Long, List<String>> getPreciousTaskNames(String sessionId, List<String> jobsId) throws RestException {
        try {
            Scheduler scheduler = checkAccess(sessionId, "metadataOfPreciousResults");
            return scheduler.getPreciousTaskNames(jobsId);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public byte[] serializedValueOfTaskResult(String sessionId, String jobId, String taskname) throws RestException {
        Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname + "/result/serializedvalue");
        try {
            TaskResult tr = s.getTaskResult(jobId, taskname);
            tr = PAFuture.getFutureValue(tr);
            return tr.getSerializedValue();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Response downloadTaskResult(String sessionId, String jobId, String taskname, String destination,
            String sessionToken) throws RestException {

        String session = sessionId != null ? sessionId : sessionToken;

        Scheduler s = checkAccess(session, PATH_JOBS + jobId + PATH_TASKS + taskname + "/result/value");
        try {
            if (destination == null) {
                destination = DESTINATION_BROWSER;
            }

            TaskResult tr = s.getTaskResult(jobId, taskname);
            tr = PAFuture.getFutureValue(tr);
            byte[] serializedValue;
            Map<String, String> resultMetadata = tr.getMetadata();
            if (resultMetadata == null) {
                resultMetadata = new HashMap<>();
            }

            if (tr.hadException()) {
                serializedValue = StackTraceUtil.getStackTrace(tr.getException()).getBytes();
                resultMetadata.put(METADATA_CONTENT_TYPE, "text/plain");
            } else if (tr.isRaw()) {
                serializedValue = tr.getSerializedValue();
                if (!resultMetadata.containsKey(METADATA_CONTENT_TYPE)) {
                    resultMetadata.put(METADATA_CONTENT_TYPE, "application/octet-stream");
                }
            } else {
                try {
                    Serializable resultValue = tr.getValue();
                    serializedValue = resultValue != null ? resultValue.toString().getBytes() : "null".getBytes();
                } catch (Throwable t) {
                    serializedValue = StackTraceUtil.getStackTrace(t).getBytes();
                }
                resultMetadata.put(METADATA_CONTENT_TYPE, "text/plain");
            }

            Response.ResponseBuilder builder = Response.ok().entity(new ByteArrayInputStream(serializedValue));

            if (resultMetadata.containsKey(METADATA_CONTENT_TYPE)) {
                builder = builder.header(HttpHeaders.CONTENT_TYPE, resultMetadata.get(METADATA_CONTENT_TYPE));
            }
            if (DESTINATION_FILE.equals(destination)) {
                if (resultMetadata.containsKey(METADATA_FILE_NAME)) {
                    builder = builder.header(HttpHeaders.CONTENT_DISPOSITION,
                                             "attachment; filename=" + resultMetadata.get(METADATA_FILE_NAME));
                } else if (resultMetadata.containsKey((METADATA_FILE_EXTENSION))) {

                    builder = builder.header(HttpHeaders.CONTENT_DISPOSITION,
                                             "attachment; filename=job_" + jobId + "_" + taskname + "_result" +
                                                                              resultMetadata.get(METADATA_FILE_EXTENSION));
                }
            }

            return builder.build();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<String, byte[]> serializedValueOfTaskResultByTag(String sessionId, String jobId, String taskTag)
            throws RestException {
        Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tag" + taskTag + "/result/serializedvalue");
        try {
            List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
            Map<String, byte[]> result = new HashMap<>(trs.size());
            for (TaskResult currentResult : trs) {
                TaskResult r = PAFuture.getFutureValue(currentResult);
                result.put(r.getTaskId().getReadableName(), r.getSerializedValue());
            }
            return result;
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public TaskResultData taskResult(String sessionId, String jobId, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname + "/result");
            TaskResult taskResult = s.getTaskResult(jobId, taskname);
            if (taskResult == null) {
                TaskIdData taskIdData = new TaskIdData();
                taskIdData.setReadableName(taskname);
                TaskResultData taskResultData = new TaskResultData();
                taskResultData.setId(taskIdData);
                return taskResultData;
            }
            return buildTaskResultData(taskResult);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    private TaskResultData buildTaskResultData(TaskResult taskResult) {
        return mapper.map(PAFuture.getFutureValue(taskResult), TaskResultData.class);
    }

    @Override
    public List<TaskResultData> taskResultByTag(String sessionId, String jobId, String taskTag) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskTag + "/result");
            List<TaskResult> taskResults = s.getTaskResultsByTag(jobId, taskTag);
            ArrayList<TaskResultData> results = new ArrayList<>(taskResults.size());
            for (TaskResult current : taskResults) {
                TaskResultData r = buildTaskResultData(PAFuture.getFutureValue(current));
                results.add(r);
            }

            return results;
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String taskLog(String sessionId, String jobId, String taskname) throws RestException {
        try {
            return retrieveTaskLogsUsingDatabase(sessionId, jobId, taskname);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    private String retrieveTaskLogsUsingDatabase(String sessionId, String jobId, String taskName)
            throws NotConnectedRestException, UnknownJobException, UnknownTaskException, NotConnectedException,
            PermissionException {
        Scheduler scheduler = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskName + "/result/log/all");
        StringBuilder allLogs = new StringBuilder();
        List<TaskResult> resultList = scheduler.getTaskResultAllIncarnations(jobId, taskName);
        for (TaskResult result : resultList) {
            if (result.getOutput() != null) {
                String taskAllLogs = result.getOutput().getAllLogs(true);
                allLogs.append(taskAllLogs);
                if (!taskAllLogs.endsWith("\n")) {
                    allLogs.append(NL);
                }
            }
        }
        return allLogs.toString();
    }

    @Override
    public String taskLogByTag(String sessionId, String jobId, String taskTag) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tag/" + taskTag + "/result/log/err");
            List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
            StringBuffer buf = new StringBuffer();
            for (TaskResult tr : trs) {
                if (tr.getOutput() != null) {
                    buf.append(tr.getOutput().getAllLogs(true));
                }
            }
            return buf.toString();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String jobLogs(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/result/log/all");
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
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String taskLogErr(String sessionId, String jobId, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname + "/result/log/err");
            StringBuilder errLogs = new StringBuilder();
            List<TaskResult> resultList = s.getTaskResultAllIncarnations(jobId, taskname);
            for (TaskResult result : resultList) {
                if (result.getOutput() != null) {
                    String taskErrLogs = result.getOutput().getStderrLogs(true);
                    errLogs.append(taskErrLogs);
                    if (!taskErrLogs.endsWith("\n")) {
                        errLogs.append(NL);
                    }
                }
            }
            return errLogs.toString();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String taskLogErrByTag(String sessionId, String jobId, String taskTag) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tag/" + taskTag + "/result/log/err");
            List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
            StringBuffer buf = new StringBuffer();
            for (TaskResult tr : trs) {
                if (tr.getOutput() != null) {
                    buf.append(tr.getOutput().getStderrLogs(true));
                }
            }
            return buf.toString();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String taskLogout(String sessionId, String jobId, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname + "/result/log/out");
            StringBuilder outLogs = new StringBuilder();
            List<TaskResult> resultList = s.getTaskResultAllIncarnations(jobId, taskname);
            for (TaskResult result : resultList) {
                if (result.getOutput() != null) {
                    String taskOutLogs = result.getOutput().getStdoutLogs(true);
                    outLogs.append(taskOutLogs);
                    if (!taskOutLogs.endsWith("\n")) {
                        outLogs.append(NL);
                    }
                }
            }
            return outLogs.toString();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String taskLogoutByTag(String sessionId, String jobId, String taskTag) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tag/" + taskTag + "/result/log/out");
            List<TaskResult> trs = s.getTaskResultsByTag(jobId, taskTag);
            StringBuffer result = new StringBuffer();
            for (TaskResult tr : trs) {
                if (tr.getOutput() != null) {
                    result.append(tr.getOutput().getStdoutLogs(true));
                }
            }
            return result.toString();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public InputStream taskFullLogs(String sessionId, String jobId, String taskname, String session)
            throws RestException, IOException {
        try {

            if (sessionId == null) {
                sessionId = session;
            }

            Scheduler scheduler = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname + "/result/log/all");
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
                    return IOUtils.toInputStream(retrieveTaskLogsUsingDatabase(sessionId, jobId, taskname),
                                                 Charset.forName(FILE_ENCODING));
                }
            } else {
                return null;
            }
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String taskServerLog(String sessionId, String jobId, String taskname) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + PATH_TASKS + taskname + "/log/server");
            return s.getTaskServerLogs(jobId, taskname);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public String taskServerLogByTag(String sessionId, String jobId, String taskTag) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/tasks/tag/" + taskTag + "/log/server");
            return s.getTaskServerLogsByTag(jobId, taskTag);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    /**
     * the method check if the session id is valid i.e. a scheduler client is
     * associated to the session id in the session map. If not, a
     * NotConnectedRestException is thrown specifying the invalid access *
     *
     * @return the scheduler linked to the session id, an
     * NotConnectedRestException, if no such mapping exists.
     * @throws NotConnectedRestException
     */
    private SchedulerProxyUserInterface checkAccess(String sessionId, String path) throws NotConnectedRestException {
        Session session = sessionStore.get(sessionId);

        SchedulerProxyUserInterface schedulerProxy = session.getScheduler();

        if (schedulerProxy == null) {
            throw new NotConnectedRestException(YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST);
        }

        renewSession(sessionId);

        return schedulerProxy;
    }

    private SchedulerSpaceInterface getSpaceInterface(String sessionId) throws NotConnectedRestException {

        renewSession(sessionId);

        return sessionStore.get(sessionId).getSpace();
    }

    /**
     * Call a method on the scheduler's frontend in order to renew the lease the
     * user has on this frontend. see PORTAL-70
     *
     * @throws NotConnectedRestException
     */
    protected void renewSession(String sessionId) throws NotConnectedRestException {
        try {
            sessionStore.renewSession(sessionId);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST, e);
        }
    }

    private SchedulerProxyUserInterface checkAccess(String sessionId) throws NotConnectedRestException {
        return checkAccess(sessionId, "");
    }

    @Override
    public boolean pauseJob(String sessionId, String jobId) throws RestException {
        try {
            final Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId + "/pause");
            return s.pauseJob(jobId);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean restartAllInErrorTasks(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId + "/restartAllInErrorTasks");
            return s.restartAllInErrorTasks(jobId);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean resumeJob(String sessionId, String jobId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId + "/resume");
            return s.resumeJob(jobId);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public JobIdData submitFlat(String sessionId, String commandFileContent, String jobName,
            String selectionScriptContent, String selectionScriptExtension) throws IOException, RestException {
        Scheduler s = checkAccess(sessionId, "submitflat");

        try {
            File command = File.createTempFile("flatsubmit_commands_", ".txt");
            command.deleteOnExit();

            String selectionPath = null;
            File selection = null;

            if (selectionScriptContent != null && selectionScriptContent.trim().length() > 0) {
                selection = File.createTempFile("flatsubmit_selection_", "." + selectionScriptExtension);
                selection.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(new FileOutputStream(selection))) {
                    pw.print(selectionScriptContent);
                }
                selectionPath = selection.getAbsolutePath();
            }

            try (PrintWriter pw = new PrintWriter(new FileOutputStream(command))) {
                pw.print(commandFileContent);
            }

            Job j = FlatJobFactory.getFactory().createNativeJobFromCommandsFile(command.getAbsolutePath(),
                                                                                jobName,
                                                                                selectionPath,
                                                                                null);
            JobId id = s.submit(j);

            command.delete();
            if (selection != null) {
                selection.delete();
            }

            return mapper.map(id, JobIdData.class);
        } catch (IOException e) {
            throw new IOException("I/O Error: " + e.getMessage(), e);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    private Map<String, String> getMapWithFirstValues(MultivaluedMap<String, String> queryParameters) {

        Map<String, String> parameters = new HashMap<String, String>();

        for (String str : queryParameters.keySet()) {
            parameters.put(str, queryParameters.getFirst(str));
        }
        return parameters;

    }

    @Override
    public JobIdData submitFromUrl(String sessionId, String url, PathSegment pathSegment, UriInfo contextInfos)
            throws JobCreationRestException, NotConnectedRestException, PermissionRestException,
            SubmissionClosedRestException, IOException {
        Scheduler scheduler = checkAccess(sessionId, "jobs");
        SchedulerSpaceInterface space = getSpaceInterface(sessionId);

        String jobXml = downloadWorkflowContent(sessionId, url);
        JobId jobId;
        try (InputStream tmpWorkflowStream = IOUtils.toInputStream(jobXml, Charset.forName(FILE_ENCODING))) {

            // Get the job submission variables
            Map<String, String> jobVariables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

            // Get the job submission generic infos
            Map<String, String> genericInfos = null;
            if (contextInfos != null)
                genericInfos = getMapWithFirstValues(contextInfos.getQueryParameters());

            WorkflowSubmitter workflowSubmitter = new WorkflowSubmitter(scheduler, space);
            jobId = workflowSubmitter.submit(tmpWorkflowStream, jobVariables, genericInfos);
        }

        return mapper.map(jobId, JobIdData.class);
    }

    @Override
    public JobIdData submitFromUrl(String sessionId, String url, PathSegment pathSegment,
            MultipartFormDataInput multipart, UriInfo contextInfos) throws JobCreationRestException,
            NotConnectedRestException, PermissionRestException, SubmissionClosedRestException, IOException {
        Scheduler scheduler = checkAccess(sessionId, "jobs");
        SchedulerSpaceInterface space = getSpaceInterface(sessionId);

        String jobXml = downloadWorkflowContent(sessionId, url);
        JobId jobId;
        try (InputStream tmpWorkflowStream = IOUtils.toInputStream(jobXml, Charset.forName(FILE_ENCODING))) {

            // Get the job submission variables from pathSegment
            Map<String, String> jobVariables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

            // Get job variables from multipart
            if (multipart.getFormDataMap().containsKey(VARIABLES_KEY)) {
                Map<String, String> variablesFromMultipart = multipart.getFormDataPart(VARIABLES_KEY,
                                                                                       new GenericType<Map<String, String>>() {
                                                                                       });

                if (jobVariables != null) {
                    jobVariables.putAll(variablesFromMultipart);
                } else {
                    jobVariables = variablesFromMultipart;
                }
            }

            // Get the job submission generic infos
            Map<String, String> genericInfos = null;
            if (contextInfos != null)
                genericInfos = getMapWithFirstValues(contextInfos.getQueryParameters());

            WorkflowSubmitter workflowSubmitter = new WorkflowSubmitter(scheduler, space);
            jobId = workflowSubmitter.submit(tmpWorkflowStream, jobVariables, genericInfos);
        }

        return mapper.map(jobId, JobIdData.class);
    }

    @Override
    public JobIdData submit(String sessionId, PathSegment pathSegment, MultipartFormDataInput multipart,
            UriInfo contextInfos) throws JobCreationRestException, NotConnectedRestException, PermissionRestException,
            SubmissionClosedRestException, IOException {
        Scheduler scheduler = checkAccess(sessionId, "submit");
        SchedulerSpaceInterface space = getSpaceInterface(sessionId);

        try {
            // In multipart, we can find the "variables" key for job variables, AND/OR ...
            // ... "job.xml" for a job submitted from the studio OR "file" for a job submitted by job planner
            // take and remove the "variables" key before
            Map<String, String> variablesFromMultipart = null;
            if (multipart.getFormDataMap().containsKey(VARIABLES_KEY)) {
                variablesFromMultipart = multipart.getFormDataPart(VARIABLES_KEY,
                                                                   new GenericType<Map<String, String>>() {
                                                                   });
                multipart.getFormDataMap().remove(VARIABLES_KEY);
            }

            // Get job from multipart
            InputPart part1 = multipart.getFormDataMap().values().iterator().next().get(0);

            String fileType = part1.getMediaType().toString().toLowerCase();
            if (!fileType.contains(MediaType.APPLICATION_XML.toLowerCase()) &&
                !fileType.contains(MediaType.TEXT_XML.toLowerCase())) {
                throw new JobCreationRestException("Unknown job descriptor type: " + fileType);
            }

            JobId jobId;

            // is the name of the browser's input field
            try (InputStream tmpWorkflowStream = part1.getBody(new GenericType<InputStream>() {

            })) {

                // Get the job submission variables
                Map<String, String> jobVariables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

                // Add multipart variables to variables
                if (variablesFromMultipart != null) {

                    if (jobVariables != null) {
                        jobVariables.putAll(variablesFromMultipart);
                    } else {
                        jobVariables = variablesFromMultipart;
                    }
                }

                // Get the job submission generic infos
                Map<String, String> genericInfos = null;
                if (contextInfos != null)
                    genericInfos = getMapWithFirstValues(contextInfos.getQueryParameters());

                WorkflowSubmitter workflowSubmitter = new WorkflowSubmitter(scheduler, space);
                jobId = workflowSubmitter.submit(tmpWorkflowStream, jobVariables, genericInfos);
            }

            return mapper.map(jobId, JobIdData.class);

        } finally {
            if (multipart != null) {
                multipart.close();
            }
        }
    }

    private String normalizeFilePath(String filePath, String fileName) {
        if (filePath == null) {
            filePath = "";
        }
        if (fileName != null && filePath.length() > 0 && !filePath.endsWith("/")) {
            filePath = (filePath + File.separator + fileName);
        } else if (fileName != null) {
            filePath = fileName;
        }

        if (filePath.length() > 0 && filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        return filePath;
    }

    @Override
    public String submitPlannings(String sessionId, PathSegment pathSegment, Map<String, String> jobContentXmlString)
            throws JobCreationRestException, NotConnectedRestException, IOException {

        checkAccess(sessionId, "plannings");

        Map<String, String> jobVariables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

        if (jobContentXmlString == null || jobContentXmlString.size() != 1) {
            throw new JobCreationRestException("Cannot find job body: code " + HttpURLConnection.HTTP_BAD_REQUEST);
        }

        Map<String, Object> requestBody = new HashMap<>(2);
        requestBody.put(VARIABLES_KEY, jobVariables);
        requestBody.put("xmlContentString", jobContentXmlString.entrySet().iterator().next().getValue());

        Response response = null;
        try {
            ResteasyProviderFactory providerFactory = ResteasyProviderFactory.getInstance();
            SchedulerRestClient.registerGzipEncoding(providerFactory);
            ResteasyClient client = new ResteasyClientBuilder().providerFactory(providerFactory).build();
            ResteasyWebTarget target = client.target(PortalConfiguration.JOBPLANNER_URL.getValueAsString());
            response = target.request()
                             .header("sessionid", sessionId)
                             .post(Entity.entity(requestBody, "application/json"));

            if (HttpURLConnection.HTTP_OK != response.getStatus()) {
                throw new IOException(String.format("Cannot access resource %s: code %d",
                                                    PortalConfiguration.JOBPLANNER_URL.getValueAsString(),
                                                    response.getStatus()));
            }
            return response.readEntity(String.class);
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

    @Override
    public JobIdData reSubmit(String sessionId, String jobId, PathSegment pathSegment, UriInfo contextInfos)
            throws IOException, RestException {
        Scheduler scheduler = checkAccess(sessionId, "/scheduler/jobs/" + jobId + "/resubmit");
        SchedulerSpaceInterface space = getSpaceInterface(sessionId);

        JobId newJobId;
        String jobXml;
        try {
            jobXml = scheduler.getJobContent(JobIdImpl.makeJobId(jobId));
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }

        try (InputStream tmpWorkflowStream = IOUtils.toInputStream(jobXml, Charset.forName(FILE_ENCODING))) {
            // Get the job submission variables
            Map<String, String> jobVariables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

            // Get the job submission generic infos
            Map<String, String> genericInfos = null;
            if (contextInfos != null) {
                genericInfos = getMapWithFirstValues(contextInfos.getQueryParameters());
            }

            WorkflowSubmitter workflowSubmitter = new WorkflowSubmitter(scheduler, space);
            newJobId = workflowSubmitter.submit(tmpWorkflowStream, jobVariables, genericInfos);
        }
        return mapper.map(newJobId, JobIdData.class);
    }

    @Override
    public WorkflowDescription getDescription(String sessionId, String jobId) throws IOException, RestException {
        Scheduler scheduler = checkAccess(sessionId, "/scheduler/jobs/" + jobId + "/variables");
        SchedulerSpaceInterface space = getSpaceInterface(sessionId);

        try {
            String jobXml = scheduler.getJobContent(JobIdImpl.makeJobId(jobId));
            Job job;
            try (InputStream tmpWorkflowStream = IOUtils.toInputStream(jobXml, Charset.forName(FILE_ENCODING))) {
                job = JobFactory.getFactory().createJob(tmpWorkflowStream, null, null, scheduler, space);
            }
            WorkflowDescription workflowDescription = new WorkflowDescription();
            workflowDescription.setName(job.getName());
            workflowDescription.setProjectName(job.getProjectName());
            workflowDescription.setDescription(job.getDescription());
            workflowDescription.setVariables(job.getVariables());
            Map<String, String> genericInfo = job.getGenericInformation();
            workflowDescription.setGenericInformation(genericInfo);
            if (genericInfo != null) {
                workflowDescription.setBucketName(genericInfo.get(BUCKET_NAME_GI));
                workflowDescription.setIcon(genericInfo.get(WORKFLOW_ICON_GI));
                workflowDescription.setDocumentation(genericInfo.get(DOCUMENTATION_GI));
            }
            return workflowDescription;
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<JobIdData> reSubmitAll(String sessionId, List<String> jobsId) throws RestException {
        Scheduler scheduler = checkAccess(sessionId, "/scheduler/jobs/resubmit");
        SchedulerSpaceInterface space = getSpaceInterface(sessionId);
        WorkflowSubmitter workflowSubmitter = new WorkflowSubmitter(scheduler, space);
        List<JobIdData> newJobIds = new ArrayList<>(jobsId.size());

        for (String jobId : jobsId) {
            String jobXml;
            try {
                jobXml = scheduler.getJobContent(JobIdImpl.makeJobId(jobId));
            } catch (SchedulerException e) {
                logger.error("Error occurred when resubmitting job " + jobId, e);
                continue;
            }
            try (InputStream tmpWorkflowStream = IOUtils.toInputStream(jobXml, Charset.forName(FILE_ENCODING))) {

                JobId newJobId = workflowSubmitter.submit(tmpWorkflowStream,
                                                          Collections.emptyMap(),
                                                          Collections.emptyMap());
                newJobIds.add(mapper.map(newJobId, JobIdData.class));
            } catch (Exception e) {
                logger.error("Error occurred when resubmitting job " + jobId, e);
            }
        }
        return newJobIds;
    }

    @Override
    public boolean pushFile(String sessionId, String spaceName, String filePath, MultipartFormDataInput multipart)
            throws IOException, NotConnectedRestException, PermissionRestException {
        try {
            checkAccess(sessionId, "pushFile");

            Session session = dataspaceRestApi.checkSessionValidity(sessionId);

            Map<String, List<InputPart>> formDataMap = multipart.getFormDataMap();

            List<InputPart> fNL = formDataMap.get("fileName");
            if ((fNL == null) || (fNL.isEmpty())) {
                throw new IllegalArgumentException("Illegal multipart argument definition (fileName), received " + fNL);
            }
            String fileName = fNL.get(0).getBody(String.class, null);

            List<InputPart> fCL = formDataMap.get("fileContent");
            if ((fCL == null) || (fCL.isEmpty())) {
                throw new IllegalArgumentException("Illegal multipart argument definition (fileContent), received " +
                                                   fCL);
            }
            InputStream fileContent = fCL.get(0).getBody(InputStream.class, null);

            if (fileName == null) {
                throw new IllegalArgumentException("Wrong file name : " + fileName);
            }

            filePath = normalizeFilePath(filePath, fileName);

            FileObject destfo = dataspaceRestApi.resolveFile(session, spaceName, filePath);

            URL targetUrl = destfo.getURL();
            logger.info("[pushFile] pushing file to " + targetUrl);

            if (!destfo.isWriteable()) {
                RuntimeException ex = new IllegalArgumentException("File " + filePath + " is not writable in space " +
                                                                   spaceName);
                logger.error(ex);
                throw ex;
            }
            if (destfo.exists()) {
                destfo.delete();
            }
            // used to create the necessary directories if needed
            destfo.createFile();

            dataspaceRestApi.writeFile(fileContent, destfo, null);

            return true;
        } finally {
            if (multipart != null) {
                multipart.close();
            }
        }
    }

    @Override
    public InputStream pullFile(String sessionId, String spaceName, String filePath)
            throws IOException, NotConnectedRestException, PermissionRestException {

        checkAccess(sessionId, "pullFile");
        Session session = dataspaceRestApi.checkSessionValidity(sessionId);

        filePath = normalizeFilePath(filePath, null);

        FileObject sourcefo = dataspaceRestApi.resolveFile(session, spaceName, filePath);

        if (!sourcefo.exists() || !sourcefo.isReadable()) {
            RuntimeException ex = new IllegalArgumentException("File " + filePath +
                                                               " does not exist or is not readable in space " +
                                                               spaceName);
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
            return IOUtils.toInputStream(sb.toString(), Charset.forName(FILE_ENCODING));

        } else if (sourcefo.getType().equals(FileType.FILE)) {
            logger.info("[pullFile] reading file content from " + sourcefo.getURL());
            return sourcefo.getContent().getInputStream();
        } else {
            RuntimeException ex = new IllegalArgumentException("File " + filePath + " has an unsupported type " +
                                                               sourcefo.getType());
            logger.error(ex);
            throw ex;
        }

    }

    @Override
    public boolean deleteFile(String sessionId, String spaceName, String filePath)
            throws IOException, NotConnectedRestException, PermissionRestException {
        checkAccess(sessionId, "deleteFile");

        Session session = dataspaceRestApi.checkSessionValidity(sessionId);

        filePath = normalizeFilePath(filePath, null);

        FileObject sourcefo = dataspaceRestApi.resolveFile(session, spaceName, filePath);

        if (!sourcefo.exists() || !sourcefo.isWriteable()) {
            RuntimeException ex = new IllegalArgumentException("File or Folder " + filePath +
                                                               " does not exist or is not writable in space " +
                                                               spaceName);
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
            RuntimeException ex = new IllegalArgumentException("File " + filePath + " has an unsupported type " +
                                                               sourcefo.getType());
            logger.error(ex);
            throw ex;
        }
        return true;
    }

    @Override
    public void disconnect(String sessionId) throws RestException {
        try {
            final Scheduler s = checkAccess(sessionId, "disconnect");
            logger.info("disconnection user " + sessionStore.get(sessionId) + " to session " + sessionId);
            s.disconnect();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        } finally {
            sessionStore.terminate(sessionId);
            logger.debug("sessionid " + sessionId + " terminated");
        }
    }

    @Override
    public boolean pauseScheduler(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "pause");
            return s.pause();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean stopScheduler(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "stop");
            return s.stop();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean resumeScheduler(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "resume");
            return s.resume();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public void schedulerChangeJobPriorityByName(String sessionId, String jobId, String priorityName)
            throws RestException, JobAlreadyFinishedRestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/priority/byname/" + priorityName);
            s.changeJobPriority(jobId, JobPriority.findPriority(priorityName));
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public void schedulerChangeJobPriorityByValue(String sessionId, String jobId, String priorityValue)
            throws RestException, JobAlreadyFinishedRestException {
        try {
            Scheduler s = checkAccess(sessionId, PATH_JOBS + jobId + "/priority/byvalue" + priorityValue);
            s.changeJobPriority(jobId, JobPriority.findPriority(Integer.parseInt(priorityValue)));
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }

    }

    @Override
    public boolean freezeScheduler(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "freeze");
            return s.freeze();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public SchedulerStatusData getSchedulerStatus(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "status");
            return SchedulerStatusData.valueOf(SchedulerStateListener.getInstance().getSchedulerStatus(s).name());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean startScheduler(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "start");
            return s.start();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean killScheduler(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "kill");
            return s.kill();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean shutdownScheduler(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "shutdown");
            return s.shutdown();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean linkRm(String sessionId, String rmURL) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "linkrm");
            return s.linkResourceManager(rmURL);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public boolean isConnected(String sessionId) throws NotConnectedRestException {
        Scheduler s = checkAccess(sessionId, "isconnected");
        return s.isConnected();
    }

    @Override
    public String login(String username, String password) throws LoginException, SchedulerRestException {
        try {
            if ((username == null) || (password == null)) {
                throw new LoginException("Empty login/password");
            }
            Session session = sessionStore.create(username);
            session.connectToScheduler(new CredData(username, password));
            logger.info("Binding user " + username + " to session " + session.getSessionId());

            return session.getSessionId();
        } catch (ActiveObjectCreationException | SchedulerException | NodeException e) {
            throw new SchedulerRestException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String loginOrRenewSession(String sessionId, String username, String password)
            throws SchedulerRestException, LoginException, NotConnectedRestException {
        if (sessionId == null || !sessionStore.exists(sessionId)) {
            return login(username, password);
        }

        renewSession(sessionId);
        return sessionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String loginOrRenewSession(String sessionId, LoginForm multipart)
            throws KeyException, SchedulerRestException, LoginException, NotConnectedRestException {
        if (sessionId == null || !sessionStore.exists(sessionId)) {
            return loginWithCredential(multipart);
        }

        renewSession(sessionId);
        return sessionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLoginFromSessionId(String sessionId) {
        if (sessionId != null && sessionStore.exists(sessionId)) {
            try {
                renewSession(sessionId);
                return sessionStore.get(sessionId).getUserName();
            } catch (NotConnectedRestException e) {
                logger.trace(e);
            } catch (Exception e) {
                logger.warn(e);
            }
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserData getUserDataFromSessionId(@PathParam("sessionId") String sessionId) {
        if (sessionId != null && sessionStore.exists(sessionId)) {
            try {
                renewSession(sessionId);
                Scheduler scheduler = sessionStore.get(sessionId).getScheduler();
                return scheduler.getCurrentUserData();
            } catch (NotConnectedRestException | NotConnectedException e) {
                logger.trace(e);
            } catch (Exception e) {
                logger.warn(e);
            }
        }
        return null;
    }

    @Override
    public String loginWithCredential(LoginForm multipart) throws LoginException, KeyException, SchedulerRestException {
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
                                                 CredData.parseDomain(multipart.getUsername()),
                                                 multipart.getPassword(),
                                                 multipart.getSshKey());
                session.connectToScheduler(credData);
            }

            return session.getSessionId();

        } catch (ActiveObjectCreationException | NodeException | SchedulerException e) {
            throw new SchedulerRestException(e);
        }
    }

    @Override
    public Map<String, String> getStatistics(String sessionId)
            throws NotConnectedRestException, PermissionRestException {
        SchedulerProxyUserInterface s = checkAccess(sessionId, "stats");
        return s.getMappedInfo("ProActiveScheduler:name=RuntimeData");
    }

    @Override
    public String getStatHistory(String sessionId, String function) throws NotConnectedRestException {
        SchedulerProxyUserInterface s = checkAccess(sessionId, "stats");
        return s.getStatHistory("ProActiveScheduler:name=RuntimeData",
                                "dddd", // all for ranges for the days
                                new String[] { "PendingJobsCount", "PausedJobsCount", "RunningJobsCount",
                                               "StalledJobsCount", "InErrorJobsCount" },
                                function);
    }

    @Override
    public Map<String, String> getStatisticsOnMyAccount(String sessionId) throws RestException {
        SchedulerProxyUserInterface s = checkAccess(sessionId, "stats/myaccount");
        return s.getMappedInfo("ProActiveScheduler:name=MyAccount");
    }

    @Override
    public List<SchedulerUserData> getUsers(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "users");
            return map(s.getUsers(), SchedulerUserData.class);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<SchedulerUserData> getUsersWithJobs(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId, "userswithjobs");
            return map(s.getUsersWithJobs(), SchedulerUserData.class);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    private static <T> List<T> map(List<?> toMaps, Class<T> type) {
        return toMaps.stream().map(toMap -> mapper.map(toMap, type)).collect(Collectors.toList());
    }

    /**
     * returns the version of the rest api
     *
     * @return returns the version of the rest api
     */
    @GET
    @Path("version")
    public String getVersion() {
        return String.format("{ \"scheduler\" : \"%s\", \"rest\" : \"%s\"}",
                             SchedulerStateRest.class.getPackage().getSpecificationVersion(),
                             SchedulerStateRest.class.getPackage().getImplementationVersion());
    }

    @Override
    public byte[] getCreateCredential(LoginForm multipart) throws LoginException, SchedulerRestException {

        String username = multipart.getUsername();
        String password = multipart.getPassword();
        byte[] privKey = multipart.getSshKey();

        try {
            String url = PortalConfiguration.SCHEDULER_URL.getValueAsString();
            SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
            PublicKey pubKey = auth.getPublicKey();
            sessionStore.create(username);
            Credentials cred = Credentials.createCredentials(new CredData(CredData.parseLogin(username),
                                                                          CredData.parseDomain(username),
                                                                          password,
                                                                          privKey),
                                                             pubKey);
            return cred.getBase64();
        } catch (ConnectionException | KeyException e) {
            throw new SchedulerRestException(e);
        }
    }

    @GET
    @Path("usage/myaccount")
    @Produces("application/json")
    @Override
    public List<JobUsageData> getUsageOnMyAccount(@HeaderParam("sessionid") String sessionId,
            @QueryParam("startdate") @DateFormatter.DateFormat() Date startDate,
            @QueryParam("enddate") @DateFormatter.DateFormat() Date endDate) throws RestException {
        try {
            Scheduler scheduler = checkAccess(sessionId);
            return map(scheduler.getMyAccountUsage(startDate, endDate), JobUsageData.class);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @GET
    @Path("usage/account")
    @Produces("application/json")
    @Override
    public List<JobUsageData> getUsageOnAccount(@HeaderParam("sessionid") String sessionId,
            @QueryParam("user") String user, @QueryParam("startdate") @DateFormatter.DateFormat() Date startDate,
            @QueryParam("enddate") @DateFormatter.DateFormat() Date endDate) throws RestException {
        try {
            Scheduler scheduler = checkAccess(sessionId);
            return map(scheduler.getAccountUsage(user, startDate, endDate), JobUsageData.class);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<String> userspaceURIs(String sessionId) throws RestException {
        SchedulerProxyUserInterface proxy = checkAccess(sessionId);
        try {
            return proxy.getUserSpaceURIs();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public List<String> globalspaceURIs(String sessionId) throws RestException {
        SchedulerProxyUserInterface proxy = checkAccess(sessionId);
        try {
            return proxy.getGlobalSpaceURIs();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public JobValidationData validate(String sessionId, PathSegment pathSegment, MultipartFormDataInput multipart)
            throws NotConnectedRestException {
        File tmpFile = null;
        try {
            Scheduler scheduler = null;
            SchedulerSpaceInterface space = null;
            if (sessionId != null) {
                scheduler = checkAccess(sessionId);
                space = getSpaceInterface(sessionId);
            }
            // In multipart, we can find the "variables" key for job variables, AND/OR ...
            // ... "job.xml" for a job submitted from the studio OR "file" for a job submitted by job planner
            // take and remove the "variables" key before
            Map<String, String> variablesFromMultipart = null;
            if (multipart.getFormDataMap().containsKey(VARIABLES_KEY)) {
                variablesFromMultipart = multipart.getFormDataPart(VARIABLES_KEY,
                                                                   new GenericType<Map<String, String>>() {
                                                                   });
                multipart.getFormDataMap().remove(VARIABLES_KEY);
            }

            // Get job from multipart
            InputStream is = multipart.getFormDataMap()
                                      .values()
                                      .iterator()
                                      .next()
                                      .get(0)
                                      .getBody(new GenericType<InputStream>() {
                                      });

            // Write job to file
            tmpFile = File.createTempFile("valid-job", "d");
            Map<String, String> jobVariables;
            try (OutputStream outputStream = new FileOutputStream(tmpFile)) {
                IOUtils.copy(is, outputStream);

                // Get the job variables
                jobVariables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

                // Add multipart variables to variables
                if (variablesFromMultipart != null) {

                    if (jobVariables != null) {
                        jobVariables.putAll(variablesFromMultipart);
                    } else {
                        jobVariables = variablesFromMultipart;
                    }
                }
            }

            return ValidationUtil.validateJobDescriptor(tmpFile, jobVariables, scheduler, space);
        } catch (IOException e) {
            JobValidationData validation = new JobValidationData();
            validation.setErrorMessage("Cannot read from the job validation request.");
            validation.setStackTrace(getStackTrace(e));
            return validation;
        } catch (NotConnectedRestException e) {
            throw new NotConnectedRestException(e);
        } finally {
            if (tmpFile != null) {
                FileUtils.deleteQuietly(tmpFile);
            }
            if (multipart != null) {
                multipart.close();
            }
        }
    }

    @Override
    public JobValidationData validateFromUrl(String sessionId, String url, PathSegment pathSegment)
            throws NotConnectedRestException {

        File tmpWorkflowFile = null;
        try {
            Scheduler scheduler = checkAccess(sessionId);
            SchedulerSpaceInterface space = getSpaceInterface(sessionId);
            String jobXml = downloadWorkflowContent(sessionId, url);
            tmpWorkflowFile = File.createTempFile("job", "d");
            Map<String, String> jobVariables;
            try (OutputStream outputStream = new FileOutputStream(tmpWorkflowFile)) {
                IOUtils.write(jobXml, outputStream, Charset.forName(FILE_ENCODING));

                jobVariables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);
            }

            return ValidationUtil.validateJobDescriptor(tmpWorkflowFile, jobVariables, scheduler, space);

        } catch (JobCreationRestException | IOException e) {
            JobValidationData validation = new JobValidationData();
            validation.setErrorMessage("Error while reading workflow at url: " + url);
            validation.setStackTrace(getStackTrace(e));
            return validation;
        } finally {
            FileUtils.deleteQuietly(tmpWorkflowFile);
        }
    }

    @Override
    public JobValidationData validateFromUrl(String sessionId, String url, PathSegment pathSegment,
            MultipartFormDataInput multipart) throws NotConnectedRestException {

        File tmpWorkflowFile = null;
        try {
            Scheduler scheduler = checkAccess(sessionId);
            SchedulerSpaceInterface space = getSpaceInterface(sessionId);
            String jobXml = downloadWorkflowContent(sessionId, url);
            tmpWorkflowFile = File.createTempFile("job", "d");
            Map<String, String> jobVariables;
            try (OutputStream outputStream = new FileOutputStream(tmpWorkflowFile)) {
                IOUtils.write(jobXml, outputStream, Charset.forName(FILE_ENCODING));

                // Get the job submission variables from pathSegment
                jobVariables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

                // Get job variables from multipart
                if (multipart.getFormDataMap().containsKey(VARIABLES_KEY)) {
                    Map<String, String> variablesFromMultipart = multipart.getFormDataPart(VARIABLES_KEY,
                                                                                           new GenericType<Map<String, String>>() {
                                                                                           });

                    if (jobVariables != null) {
                        jobVariables.putAll(variablesFromMultipart);
                    } else {
                        jobVariables = variablesFromMultipart;
                    }
                }

            }

            return ValidationUtil.validateJobDescriptor(tmpWorkflowFile, jobVariables, scheduler, space);

        } catch (JobCreationRestException | IOException e) {
            JobValidationData validation = new JobValidationData();
            validation.setErrorMessage("Error while reading workflow at url: " + url);
            validation.setStackTrace(getStackTrace(e));
            return validation;
        } finally {
            FileUtils.deleteQuietly(tmpWorkflowFile);
        }
    }

    @Override
    public void putThirdPartyCredential(String sessionId, String key, String value) throws RestException {
        try {
            key = java.net.URLDecoder.decode(key.trim(), Charsets.UTF_8.displayName());
            Scheduler s = checkAccess(sessionId);
            s.putThirdPartyCredential(key, value == null ? "" : value);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        } catch (Exception e) {
            throw new SchedulerRestException(e);
        }
    }

    @Override
    public void removeThirdPartyCredential(String sessionId, String key) throws RestException {
        try {
            key = java.net.URLDecoder.decode(key, Charsets.UTF_8.displayName());
            Scheduler s = checkAccess(sessionId);
            s.removeThirdPartyCredential(key);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        } catch (Exception e) {
            throw new SchedulerRestException(e);
        }
    }

    @Override
    public Set<String> thirdPartyCredentialsKeySet(String sessionId) throws RestException {
        try {
            Scheduler s = checkAccess(sessionId);
            return s.thirdPartyCredentialsKeySet();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    /*
     * Atmosphere 2.0 framework based implementation of Scheduler Eventing mechanism for REST
     * clients. It is configured to use WebSocket as the underneath protocol between the client and
     * the server.
     */

    /**
     * Initialize WebSocket based communication channel between the client and
     * the server.
     */
    @GET
    @Path("/events")
    public String subscribe(@Context HttpServletRequest req, @HeaderParam("sessionid") String sessionId)
            throws NotConnectedRestException {
        checkAccess(sessionId);
        HttpSession session = checkNotNull(req.getSession(),
                                           "HTTP session object is null. HTTP session support is requried for REST Scheduler eventing.");
        AtmosphereResource atmosphereResource = checkNotNull((AtmosphereResource) req.getAttribute(AtmosphereResource.class.getName()),
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
            throws RestException {
        HttpSession session = req.getSession();
        String broadcasterId = (String) session.getAttribute(ATM_BROADCASTER_ID);
        final SchedulerProxyUserInterface scheduler = checkAccess(broadcasterId);
        SchedulerEventBroadcaster eventListener = new SchedulerEventBroadcaster(broadcasterId);
        try {
            final SchedulerEventBroadcaster activedEventListener = PAActiveObject.turnActive(eventListener);
            scheduler.addEventListener(activedEventListener,
                                       subscription.isMyEventsOnly(),
                                       EventUtil.toSchedulerEvents(subscription.getEvents()));

            AtmosphereResource atmResource = getAtmosphereResourceFactory().find((String) session.getAttribute(ATM_RESOURCE_ID));

            atmResource.addEventListener(new WebSocketEventListenerAdapter() {
                @Override
                public void onDisconnect(@SuppressWarnings("rawtypes") WebSocketEvent event) {
                    try {
                        logger.info("#### websocket disconnected remove listener ####");
                        scheduler.removeEventListener();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                    PAActiveObject.terminateActiveObject(activedEventListener, true);
                }
            });
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        } catch (ActiveObjectCreationException | NodeException e) {
            throw new RuntimeException(e);
        }

        return new EventNotification(EventNotification.Action.NONE, null, null);
    }

    private AtmosphereResourceFactory getAtmosphereResourceFactory() {
        return ((AtmosphereResource) httpServletRequest.getAttribute("org.atmosphere.cpr.AtmosphereResource")).getAtmosphereConfig()
                                                                                                              .resourcesFactory();
    }

    private Broadcaster lookupBroadcaster(String topic, boolean createNew) {
        AtmosphereResource atmosphereResource = (AtmosphereResource) httpServletRequest.getAttribute("org.atmosphere.cpr.AtmosphereResource");
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
        if (workflowUrl.startsWith("http")) {
            HttpResourceDownloader httpResourceDownloader = HttpResourceDownloader.getInstance();
            return httpResourceDownloader.getResource(sessionId, workflowUrl, String.class);
        } else {
            URL nonHttpURL = new URL(workflowUrl);
            return IOUtils.toString(nonHttpURL.openStream(), Charsets.UTF_8);
        }
    }

    protected static Map<String, String> createSortableTaskAttrMap() {
        HashMap<String, String> sortableTaskAttrMap = new HashMap<>(13);
        sortableTaskAttrMap.put("id", "id.taskId");
        sortableTaskAttrMap.put("status", "taskStatus");
        sortableTaskAttrMap.put("name", "taskName");
        sortableTaskAttrMap.put("tag", "tag");
        sortableTaskAttrMap.put("jobId", "jobData.id");
        sortableTaskAttrMap.put("jobName", "jobData.jobName");
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
    public RestPage<String> getTaskIds(String sessionId, long from, long to, boolean mytasks, String statusFilter,
            int offset, int limit) throws RestException {
        return getTaskIdsByTag(sessionId, null, from, to, mytasks, statusFilter, offset, limit);
    }

    @Override
    public RestPage<TaskStateData> getTaskStates(String sessionId, long from, long to, boolean mytasks,
            String statusFilter, int offset, int limit, SortSpecifierContainer sortParams) throws RestException {
        return getTaskStatesByTag(sessionId,
                                  null,
                                  from,
                                  to,
                                  mytasks,
                                  statusFilter,
                                  offset,
                                  limit,
                                  mapToDBNamespace(sortParams));
    }

    @Override
    public RestPage<String> getTaskIdsByTag(String sessionId, String taskTag, long from, long to, boolean mytasks,
            String statusFilter, int offset, int limit) throws RestException {
        Scheduler s = checkAccess(sessionId, "tasks");

        PageBoundaries boundaries = Pagination.getTasksPageBoundaries(offset, limit, TASKS_PAGE_SIZE);

        try {
            final Set<TaskStatus> statuses = TaskStatus.expandAggregatedStatusesToRealStatuses(Stream.of(statusFilter.split(";"))
                                                                                                     .collect(Collectors.toList()));
            Page<TaskId> page = s.getTaskIds(taskTag,
                                             from,
                                             to,
                                             mytasks,
                                             statuses,
                                             boundaries.getOffset(),
                                             boundaries.getLimit());
            List<TaskId> taskIds = page.getList();
            List<String> taskNames = new ArrayList<>(taskIds.size());
            for (TaskId taskId : taskIds) {
                taskNames.add(taskId.getReadableName());
            }
            return new RestPage<>(taskNames, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public RestPage<TaskStateData> getTaskStatesByTag(String sessionId, String taskTag, long from, long to,
            boolean mytasks, String statusFilter, int offset, int limit, SortSpecifierContainer sortParams)
            throws RestException {
        Scheduler s = checkAccess(sessionId, "tasks/tag/" + taskTag);

        PageBoundaries boundaries = Pagination.getTasksPageBoundaries(offset, limit, TASKS_PAGE_SIZE);

        Page<TaskState> page = null;

        // if that method is called directly from REST without any sorting
        // parameters
        // sortParams will be null
        if (sortParams == null) {
            sortParams = new SortSpecifierContainer();
        }
        try {
            final Set<TaskStatus> statuses = TaskStatus.expandAggregatedStatusesToRealStatuses(Stream.of(statusFilter.split(";"))
                                                                                                     .collect(Collectors.toList()));

            page = s.getTaskStates(taskTag,
                                   from,
                                   to,
                                   mytasks,
                                   statuses,
                                   boundaries.getOffset(),
                                   boundaries.getLimit(),
                                   sortParams);
            List<TaskStateData> tasks = map(page.getList(), TaskStateData.class);
            return new RestPage<>(tasks, page.getSize());
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    /**
     * Translates the tasks attributes names that are used to sort the result
     * For example the task status is called `status` client-side, it is
     * represented by `taskStatus` in the DB
     *
     * @param sortParams The sort parameters using the client-side namespace
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

    @Override
    public boolean changeStartAt(String sessionId, String jobId, String startAt) throws RestException {
        try {
            final Scheduler s = checkAccess(sessionId, "POST jobs/" + jobId + "/startat/" + startAt);
            return s.changeStartAt(JobIdImpl.makeJobId(jobId), startAt);
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<Object, Object> getPortalConfiguration(String sessionId) throws RestException {
        try {
            final Scheduler s = checkAccess(sessionId, "GET configuration/portal");
            return s.getPortalConfiguration();
        } catch (SchedulerException e) {
            throw RestException.wrapExceptionToRest(e);
        }
    }

    @Override
    public Map<String, Object> getSchedulerPropertiesFromSessionId(String sessionId) throws RestException {
        SchedulerProxyUserInterface scheduler = checkAccess(sessionId, "properties");
        Map<String, Object> schedulerProperties;
        try {
            schedulerProperties = scheduler.getSchedulerProperties();
            schedulerProperties.putAll(WebProperties.getPropertiesAsHashMap());
            schedulerProperties.putAll(PortalConfiguration.getPropertiesAsHashMap());

        } catch (SchedulerException e) {
            logger.warn("Attempt to retrieve scheduler properties but failed because connection exception", e);
            throw RestException.wrapExceptionToRest(e);
        }
        return schedulerProperties;

    }

}
