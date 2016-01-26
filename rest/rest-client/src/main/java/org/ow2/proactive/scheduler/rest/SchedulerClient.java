/*
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.*;
import org.ow2.proactive.scheduler.common.exception.*;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.job.factories.Job2XMLTransformer;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.rest.data.DataUtility;
import org.ow2.proactive.scheduler.rest.data.JobInfoImpl;
import org.ow2.proactive.scheduler.rest.data.TaskResultImpl;
import org.ow2.proactive.scheduler.rest.data.TaskStateImpl;
import org.ow2.proactive.scheduler.rest.readers.OctetStreamReader;
import org.ow2.proactive.scheduler.rest.readers.TaskResultReader;
import org.ow2.proactive.scheduler.rest.readers.WildCardTypeReader;
import org.ow2.proactive.scheduler.rest.utils.HttpUtility;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.*;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;

import java.io.*;
import java.lang.reflect.Proxy;
import java.security.KeyException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.ow2.proactive.scheduler.rest.ExceptionUtility.*;
import static org.ow2.proactive.scheduler.rest.data.DataUtility.*;


public class SchedulerClient extends ClientBase implements ISchedulerClient {

    private static final long RETRY_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    private SchedulerRestClient schedulerRestClient;

    private String sid;
    private String url;
    private String login;
    private String password;

    private boolean initialized = false;

    private SchedulerEventReceiver schedulerEventReceiver;
    
    private static final Logger logger = ProActiveLogger.getLogger(SchedulerClient.class);

    private SchedulerClient() {
    }

    /**
     * Creates an ISchedulerClient instance.
     *
     * @return an ISchedulerClient instance
     */
    public static ISchedulerClient createInstance() {
        SchedulerClient client = new SchedulerClient();
        return (ISchedulerClient) Proxy.newProxyInstance(ISchedulerClient.class.getClassLoader(),
                new Class[] { ISchedulerClient.class }, new SessionHandler(client));
    }

    /**
     * Initialize this instance. Retrieves a new session from the server.
     */
    @Override
    public void init(String url, String login, String password) throws Exception {
        init(url, login, password, false);
    }

    /**
     * Initialize this instance. Retrieves a new session from the server.
     */
    @Override
    public void initInsecure(String url, String login, String password) throws Exception {
        init(url, login, password, true);
    }

    private void init(String url, String login, String password, boolean insecure) throws Exception {
        HttpClient client = HttpUtility.threadSafeClient();
        if (insecure) {
            HttpUtility.setInsecureAccess(client);
        }
        SchedulerRestClient restApiClient = new SchedulerRestClient(url, new ApacheHttpClient4Engine(client));

        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        factory.register(new WildCardTypeReader());
        factory.register(new OctetStreamReader());
        factory.register(new TaskResultReader());

        setApiClient(restApiClient);

        this.url = url;
        this.login = login;
        this.password = password;
        this.initialized = true;

        renewSession();

    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("SchedulerClient not initialized.");
        }
    }

    @Override
    public List<JobUsage> getAccountUsage(String user, Date start, Date end)
            throws NotConnectedException, PermissionException {
        List<JobUsage> jobUsages = null;
        try {
            List<JobUsageData> jobUsageDataList = restApi().getUsageOnAccount(sid, user, start, end);
            jobUsages = toJobUsages(jobUsageDataList);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return jobUsages;
    }

    @Override
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        List<JobUsage> jobUsages = null;
        try {
            List<JobUsageData> jobUsageDataList = restApi().getUsageOnMyAccount(sid, startDate, endDate);
            jobUsages = toJobUsages(jobUsageDataList);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return jobUsages;
    }

    @Override
    public void changeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        changeJobPriority(jobId.value(), priority);
    }

    @Override
    public void changeJobPriority(String jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        try {
            restApi().schedulerChangeJobPriorityByName(sid, jobId, priority.name());
        } catch (Exception e) {
            throwJAFEOrUJEOrNCEOrPE(e);
        }
    }

    @Override
    public void disconnect() throws NotConnectedException, PermissionException {
        try {
            restApi().disconnect(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        initialized = false;
    }

    @Override
    public boolean freeze() throws NotConnectedException, PermissionException {
        boolean success = false;
        try {
            success = restApi().freezeScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return success;
    }

    @Override
    public JobResult getJobResult(JobId jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        return getJobResult(jobId.value());
    }

    @Override
    public JobResult getJobResult(String jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        JobResult jobResult = null;
        try {
            JobResultData jobResultData = restApi().jobResult(sid, jobId);
            jobResult = toJobResult(jobResultData);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return jobResult;
    }

    @Override
    public JobState getJobState(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        JobState jobState = null;
        try {
            JobStateData jobStateData = restApi().listJobs(sid, jobId);
            jobState = toJobState(jobStateData);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return jobState;
    }

    @Override
    public JobState getJobState(JobId jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return getJobState(jobId.value());
    }

    @Override
    public Page<JobInfo> getJobs(int index, int range, JobFilterCriteria criteria,
            List<SortParameter<JobSortParameter>> arg3) throws NotConnectedException, PermissionException {
        Page<JobInfo> jobInfos = null;
        try {
            RestPage<UserJobData> userJobDataList = restApi().jobsInfo(sid, index, range);
            jobInfos = new Page<JobInfo>(toJobInfos(userJobDataList.getList()), userJobDataList.getSize());
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return jobInfos;
    }

    @Override
    public String getJobServerLogs(String jobId)
            throws UnknownJobException, NotConnectedException, PermissionException {
        String jobServerLog = "";
        try {
            jobServerLog = restApi().jobServerLog(sid, jobId);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return jobServerLog;
    }

    @Override
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        SchedulerStatus status = null;
        try {
            SchedulerStatusData schedulerStatus = restApi().getSchedulerStatus(sid);
            status = SchedulerStatus.valueOf(schedulerStatus.name());
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return status;
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        TaskResultImpl taskResult = null;
        try {
            TaskResultData taskResultData = restApi().taskResult(sid, jobId, taskName);
            taskResult = (TaskResultImpl) toTaskResult(JobIdImpl.makeJobId(jobId), taskResultData);
            if (taskResult.value() == null) {
                Serializable value = restApi().valueOfTaskResult(sid, jobId, taskName);
                if (value != null) {
                    taskResult.setHadException(true);
                    taskResult.setValue(value);
                }
            }

            String all = restApi().taskLog(sid, jobId, taskName);
            String out = restApi().taskLogout(sid, jobId, taskName);
            String err = restApi().taskLogErr(sid, jobId, taskName);

            taskResult.setOutput(DataUtility.toTaskLogs(all, out, err));

        } catch (Throwable t) {
            throwUJEOrNCEOrPEOrUTE(exception(t));
        }
        return taskResult;
    }

    @Override
    public TaskResult getTaskResult(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return getTaskResult(jobId.value(), taskName);
    }

    @Override
    public List<TaskResult> getTaskResultsByTag(JobId jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        List<TaskState> taskStates = getJobState(jobId).getTasksByTag(taskTag);
        ArrayList<TaskResult> results = new ArrayList<TaskResult>(taskStates.size());
        for (TaskState currentState : taskStates) {
            String taskName = currentState.getTaskInfo().getName();
            try {
                TaskResult currentResult = getTaskResult(jobId, taskName);
                results.add(currentResult);
            }
            catch(UnknownTaskException ex){
                //never occurs because tasks are filtered by tag so they cannot be unknown.
                logger.warn("Unknown task.", ex);
            }
        }
        return results;
    }

    @Override
    public List<TaskResult> getTaskResultsByTag(String jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return this.getTaskResultsByTag(JobIdImpl.makeJobId(jobId), taskTag);
    }

    @Override
    public String getTaskServerLogs(String jobId, String taskName)
            throws UnknownJobException, UnknownTaskException, NotConnectedException, PermissionException {
        String taskLogs = "";
        try {
            taskLogs = restApi().taskLog(sid, jobId, taskName);
        } catch (Exception e) {
            throwUJEOrNCEOrPEOrUTE(e);
        }
        return taskLogs;
    }

    @Override
    public String getTaskServerLogsByTag(String jobId, String tag)
            throws UnknownJobException, NotConnectedException, PermissionException {
        String taskLogs = "";
        try {
            taskLogs = restApi().taskLogByTag(sid, jobId, tag);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return taskLogs;
    }

    @Override
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        List<SchedulerUserInfo> schedulerUserInfos = null;
        try {
            List<SchedulerUserData> users = restApi().getUsers(sid);
            schedulerUserInfos = toSchedulerUserInfos(users);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return schedulerUserInfos;
    }

    @Override
    public List<SchedulerUserInfo> getUsersWithJobs() throws NotConnectedException, PermissionException {
        List<SchedulerUserInfo> schedulerUserInfos = null;
        try {
            List<SchedulerUserData> usersWithJobs = restApi().getUsersWithJobs(sid);
            schedulerUserInfos = toSchedulerUserInfos(usersWithJobs);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return schedulerUserInfos;
    }

    @Override
    public boolean isConnected() {
        boolean isConnected = false;
        if (initialized) {
            try {
                isConnected = restApi().isConnected(sid);
            } catch (Throwable e) {
                // ignore
            }
        }
        return isConnected;
    }

    @Override
    public boolean kill() throws NotConnectedException, PermissionException {
        boolean isKilled = false;
        try {
            isKilled = restApi().killScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isKilled;
    }

    @Override
    public boolean killJob(JobId jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return killJob(jobId.value());
    }

    @Override
    public boolean killJob(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        boolean isJobKilled = false;
        try {
            isJobKilled = restApi().killJob(sid, jobId);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isJobKilled;
    }

    @Override
    public boolean linkResourceManager(String rmUrl) throws NotConnectedException, PermissionException {
        boolean isLinked = false;
        try {
            isLinked = restApi().linkRm(sid, rmUrl);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isLinked;
    }

    @Override
    public boolean pause() throws NotConnectedException, PermissionException {
        boolean isSchedulerPaused = false;
        try {
            isSchedulerPaused = restApi().pauseScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isSchedulerPaused;
    }

    @Override
    public boolean pauseJob(JobId jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return pauseJob(jobId.value());
    }

    @Override
    public boolean pauseJob(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        boolean isJobPaused = false;
        try {
            isJobPaused = restApi().pauseJob(sid, jobId);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return isJobPaused;
    }

    @Override
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return preemptTask(jobId.value(), taskName, restartDelay);
    }

    @Override
    public boolean preemptTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        boolean isTaskPreempted = false;
        try {
            isTaskPreempted = restApi().preemptTask(sid, jobId, taskName);
        } catch (Exception e) {
            throwUJEOrNCEOrPEOrUTE(e);
        }
        return isTaskPreempted;
    }

    @Override
    public boolean removeJob(JobId jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return removeJob(jobId.value());
    }

    @Override
    public boolean removeJob(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        boolean isJobRemoved = false;
        try {
            isJobRemoved = restApi().removeJob(sid, jobId);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isJobRemoved;
    }

    @Override
    public boolean restartTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return restartTask(jobId.value(), taskName, restartDelay);
    }

    @Override
    public boolean restartTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        boolean isTaskRestarted = false;
        try {
            isTaskRestarted = restApi().restartTask(sid, jobId, taskName);
        } catch (Exception e) {
            throwUJEOrNCEOrPEOrUTE(e);
        }
        return isTaskRestarted;
    }

    @Override
    public boolean resume() throws NotConnectedException, PermissionException {
        boolean isResumed = false;
        try {
            isResumed = restApi().resumeScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isResumed;
    }

    @Override
    public boolean resumeJob(JobId jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return resumeJob(jobId.value());
    }

    @Override
    public boolean resumeJob(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        boolean isJobResumed = false;
        try {
            isJobResumed = restApi().resumeJob(sid, jobId);
        } catch (Exception e) {
            throwUJEOrNCEOrPE(e);
        }
        return isJobResumed;
    }

    @Override
    public boolean shutdown() throws NotConnectedException, PermissionException {
        boolean isShutdown = false;
        try {
            isShutdown = restApi().killScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isShutdown;
    }

    @Override
    public boolean start() throws NotConnectedException, PermissionException {
        boolean success = false;
        try {
            success = restApi().startScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return success;
    }

    @Override
    public boolean stop() throws NotConnectedException, PermissionException {
        boolean isStopped = false;
        try {
            isStopped = restApi().stopScheduler(sid);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return isStopped;
    }

    @Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException, SubmissionClosedException,
            JobCreationException {
        JobIdData jobIdData = null;
        try {
            InputStream is = (new Job2XMLTransformer()).jobToxml((TaskFlowJob) job);
            jobIdData = restApiClient().submitXml(sid, is);
        } catch (Exception e) {
            throwNCEOrPEOrSCEOrJCE(e);
        }
        return jobId(jobIdData);
    }

    @Override
    public boolean isJobFinished(JobId jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return isJobFinished(jobId.toString());
    }

    @Override
    public boolean isJobFinished(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return !getJobState(jobId).getStatus().isJobAlive();
    }

    @Override
    public JobResult waitForJob(JobId jobId, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return waitForJob(jobId.value(), timeout);
    }

    @Override
    public JobResult waitForJob(String jobId, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        timeout += currentTimeMillis();
        while (currentTimeMillis() < timeout) {
            if (isJobFinished(jobId)) {
                return getJobResult(jobId);
            }
            if (currentTimeMillis() + RETRY_INTERVAL < timeout) {
                sleep(RETRY_INTERVAL);
            } else {
                break;
            }
        }
        throw new TimeoutException(format("Timeout waiting for the job: job-id=%s", jobId));
    }

    @Override
    public boolean isTaskFinished(String jobId, String taskName)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException {
        boolean finished = false;
        try {
            TaskStateData taskStateData = restApi().jobTask(sid, jobId, taskName);
            TaskState taskState = taskState(taskStateData);
            finished = !taskState.getStatus().isTaskAlive();
        } catch (Exception e) {
            throwUJEOrNCEOrPEOrUTE(e);
        }
        return finished;
    }

    @Override
    public TaskResult waitForTask(String jobId, String taskName, long timeout) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        timeout += currentTimeMillis();
        while (currentTimeMillis() < timeout) {
            if (isTaskFinished(jobId, taskName)) {
                return getTaskResult(jobId, taskName);
            }
            if (currentTimeMillis() + RETRY_INTERVAL < timeout) {
                sleep(RETRY_INTERVAL);
            } else {
                break;
            }
        }
        throw new TimeoutException(
            format("Timeout waiting for the task: job-id=%s, task-id=%s", jobId, taskName));
    }

    @Override
    public List<JobResult> waitForAllJobs(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        long timestamp = 0;
        List<JobResult> results = new ArrayList<>(jobIds.size());
        for (String jobId : jobIds) {
            timestamp = currentTimeMillis();
            results.add(waitForJob(jobId, timeout));
            timeout = timeout - (currentTimeMillis() - timestamp);
        }
        return results;
    }

    @Override
    public Map.Entry<String, JobResult> waitForAnyJob(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        timeout += currentTimeMillis();
        while (currentTimeMillis() < timeout) {
            for (String jobId : jobIds) {
                if (isJobFinished(jobId)) {
                    return toEntry(jobId, getJobResult(jobId));
                }
            }
            if (currentTimeMillis() + RETRY_INTERVAL < timeout) {
                sleep(RETRY_INTERVAL);
            } else {
                break;
            }
        }
        throw new TimeoutException(format("Timeout waiting for any job: jobIds=%s.", String.valueOf(jobIds)));
    }

    @Override
    public Entry<String, TaskResult> waitForAnyTask(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException {
        timeout += currentTimeMillis();
        while (currentTimeMillis() < timeout) {
            for (String taskName : taskNames) {
                if (isTaskFinished(jobId, taskName)) {
                    return toEntry(taskName, getTaskResult(jobId, taskName));
                }
            }
            if (currentTimeMillis() + RETRY_INTERVAL < timeout) {
                sleep(RETRY_INTERVAL);
            } else {
                break;
            }
        }
        throw new TimeoutException(format("Timeout waiting for any task: job-id=%s, task-ids=%s.", jobId,
                String.valueOf(taskNames)));
    }

    @Override
    public List<Entry<String, TaskResult>> waitForAllTasks(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException {
        long timestamp = 0;
        List<Map.Entry<String, TaskResult>> taskResults = new ArrayList<>();
        for (String taskName : taskNames) {
            timestamp = currentTimeMillis();
            Entry<String, TaskResult> taskResultEntry = toEntry(taskName,
                    waitForTask(jobId, taskName, timeout));
            taskResults.add(taskResultEntry);
            timeout = timeout - (currentTimeMillis() - timestamp);
        }
        return taskResults;
    }

    @Override
    public boolean pushFile(String spacename, String pathname, String filename, String file)
            throws NotConnectedException, PermissionException {
        boolean uploaded = false;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            uploaded = restApiClient().pushFile(sid, spacename, pathname, filename, inputStream);
        } catch (Exception e) {
            throwNCEOrPE(e);
        } finally {
            closeIfPossible(inputStream);
        }
        return uploaded;
    }

    @Override
    public void pullFile(String space, String pathname, String outputFile)
            throws NotConnectedException, PermissionException {
        try {
            restApiClient().pullFile(sid, space, pathname, outputFile);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
    }

    @Override
    public boolean deleteFile(String space, String pathname)
            throws NotConnectedException, PermissionException {
        boolean deleted = false;
        try {
            deleted = restApi().deleteFile(sid, space, pathname);
        } catch (Exception e) {
            throwNCEOrPE(e);
        }
        return deleted;
    }

    @Override
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        try {
            return restApi().userspaceURIs(sid);
        } catch (Exception error) {
            throw throwNCEOrPE(error);
        }
    }

    @Override
    public void renewSession() throws NotConnectedException {
        try {
            sid = restApi().loginOrRenewSession(sid, login, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setSession(String sid) {
        this.sid = sid;
    }

    @Override
    public String getSession() {
        return sid;
    }

    @Override
    public void addEventListener(SchedulerEventListener listener, boolean myEventsOnly,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        try {
            removeEventListener();
            schedulerEventReceiver = (new SchedulerEventReceiver.Builder()).restServerUrl(url).sessionId(sid)
                    .schedulerEventListener(listener).myEventsOnly(myEventsOnly).selectedEvents(events)
                    .build();
            schedulerEventReceiver.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeEventListener() throws NotConnectedException, PermissionException {
        if (schedulerEventReceiver != null) {
            schedulerEventReceiver.stop();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            // ignore
        }
    }

    private void closeIfPossible(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private SchedulerRestInterface restApi() {
        checkInitialized();
        return schedulerRestClient.getScheduler();
    }

    private void setApiClient(SchedulerRestClient schedulerRestClient) {
        this.schedulerRestClient = schedulerRestClient;
    }

    private SchedulerRestClient restApiClient() {
        return schedulerRestClient;
    }

    private <K, V> Map.Entry<K, V> toEntry(final K k, final V v) {
        return new AbstractMap.SimpleEntry<>(k, v);

    }

    @Override
    public void putThirdPartyCredential(String key, String value)
            throws NotConnectedException, PermissionException, KeyException {
        try {
            restApi().putThirdPartyCredential(sid, key, value);
        } catch (NotConnectedRestException e) {
            throw new NotConnectedException(e);
        } catch (PermissionRestException e) {
            throw new PermissionException(e);
        } catch (SchedulerRestException e) {
            throw new KeyException(e);
        }
    }

    @Override
    public Set<String> thirdPartyCredentialsKeySet() throws NotConnectedException, PermissionException {
        try {
            return restApi().thirdPartyCredentialsKeySet(sid);
        } catch (NotConnectedRestException e) {
            throw new NotConnectedException(e);
        } catch (PermissionRestException e) {
            throw new PermissionException(e);
        }
    }

    @Override
    public void removeThirdPartyCredential(String key) throws NotConnectedException, PermissionException {
        try {
            restApi().removeThirdPartyCredential(sid, key);
        } catch (NotConnectedRestException e) {
            throw new NotConnectedException(e);
        } catch (PermissionRestException e) {
            throw new PermissionException(e);
        }
    }

    @Override
    public Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, boolean running,
            boolean pending, boolean finished, int offset, int limit)
                    throws NotConnectedException, PermissionException {
        RestPage<TaskStateData> page = null;
        try {
            page = restApi().getTaskStates(sid, from, to, mytasks, running, pending, finished, offset, limit, null);
        } catch (NotConnectedRestException e) {
            throw new NotConnectedException(e);
        } catch (PermissionRestException e) {
            throw new PermissionException(e);
        }
        List<TaskId> lTaskIds = new ArrayList<TaskId>(page.getList().size());
        for (TaskStateData taskStateData : page.getList()) {
            TaskInfoData taskInfo = taskStateData.getTaskInfo();
            TaskIdData taskIdData = taskInfo.getTaskId();
            JobId jobId = new JobIdImpl(taskInfo.getJobId().getId(), taskInfo.getJobId().getReadableName());
            TaskId taskId = TaskIdImpl.createTaskId(jobId, taskIdData.getReadableName(), taskIdData.getId());
            lTaskIds.add(taskId);
        }
        return new Page<TaskId>(lTaskIds, page.getSize());
    }

    @Override
    public Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks,
                                         boolean running, boolean pending, boolean finished,
                                         int offset, int limit, SortSpecifierContainer sortParams)
                    throws NotConnectedException, PermissionException {
        RestPage<TaskStateData> page = null;
        SortSpecifierContainer sortContainer = new SortSpecifierContainer(sortParams.toString());
        try {
            page = restApi().getTaskStates(sid, from, to, mytasks, running, pending, finished, offset,
                    limit, sortContainer);
        } catch (NotConnectedRestException e) {
            throw new NotConnectedException(e);
        } catch (PermissionRestException e) {
            throw new PermissionException(e);
        }
        List<TaskState> lTaskStates = new ArrayList<TaskState>(page.getList().size());
        for (TaskStateData taskStateData : page.getList()) {
            lTaskStates.add(new TaskStateImpl(taskStateData));
        }
        return new Page<TaskState>(lTaskStates, page.getSize());
    }

    @Override
    public JobInfo getJobInfo(String jobId)
            throws UnknownJobException, NotConnectedException, PermissionException {
        JobInfoData jobInfoData = null;
        try {
            jobInfoData = restApi().jobInfo(sid, jobId);
        } catch (NotConnectedRestException e) {
            throw new NotConnectedException(e);
        } catch (PermissionRestException e) {
            throw new PermissionException(e);
        } catch (UnknownJobRestException e) {
            throw new UnknownJobException(e);
        }
        JobInfoImpl jobInfoImpl = new JobInfoImpl();
        JobId newJobId = JobIdImpl.makeJobId(jobId);
        jobInfoImpl.setJobId(newJobId);
        jobInfoImpl.setJobOwner(jobInfoData.getJobOwner());
        jobInfoImpl.setFinishedTime(jobInfoData.getFinishedTime());
        jobInfoImpl.setRemovedTime(jobInfoData.getRemovedTime());
        jobInfoImpl.setStartTime(jobInfoData.getStartTime());
        jobInfoImpl.setSubmittedTime(jobInfoData.getSubmittedTime());
        jobInfoImpl.setNumberOfFinishedTasks(jobInfoData.getNumberOfFinishedTasks());
        jobInfoImpl.setNumberOfPendingTasks(jobInfoData.getNumberOfPendingTasks());
        jobInfoImpl.setNumberOfRunningTasks(jobInfoData.getNumberOfRunningTasks());
        jobInfoImpl.setJobPriority(JobPriority.findPriority(jobInfoData.getPriority().toString()));
        jobInfoImpl.setJobStatus(JobStatus.findPriority(jobInfoData.getStatus().toString()));
        if (jobInfoData.isToBeRemoved()) jobInfoImpl.setToBeRemoved();
        return jobInfoImpl;
    }
}
