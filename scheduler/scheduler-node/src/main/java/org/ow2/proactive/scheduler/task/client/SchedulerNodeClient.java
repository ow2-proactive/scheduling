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
package org.ow2.proactive.scheduler.task.client;

import static org.ow2.proactive.scheduler.common.SchedulerConstants.PARENT_JOB_ID;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.security.auth.Subject;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
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
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatesPage;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive.scheduler.signal.SignalApiException;
import org.ow2.proactive.scheduler.task.utils.Decrypter;

import com.google.common.collect.Maps;


/**
 * Scheduler api available as a variable during the execution of a task. it is based on the ISchedulerClient interface.
 *
 * @author ActiveEon Team
 */
@PublicAPI
public class SchedulerNodeClient implements ISchedulerClient, Serializable {

    private Decrypter decrypter;

    private String schedulerRestUrl;

    private transient ISchedulerClient client = null;

    private JobId parentJobId;

    private Map<String, String> globalGenericInformation;

    private Map<String, String> globalVariables = new LinkedHashMap<>();

    private Map<String, JobVariable> globalJobVariables = new LinkedHashMap<>();

    public SchedulerNodeClient(Decrypter decrypter, String schedulerRestUrl, JobId parentJobId,
            Map<String, JobVariable> globalVariables, Map<String, String> globalGenericInformation) {
        SchedulerNodeClient.this.decrypter = decrypter;
        SchedulerNodeClient.this.schedulerRestUrl = schedulerRestUrl;
        SchedulerNodeClient.this.parentJobId = parentJobId;
        if (globalVariables != null) {
            SchedulerNodeClient.this.globalVariables = new LinkedHashMap<>(Maps.transformValues(globalVariables,
                                                                                                JobVariable::getValue));
            SchedulerNodeClient.this.globalJobVariables = globalVariables;
        }
        if (globalGenericInformation != null) {
            SchedulerNodeClient.this.globalGenericInformation = globalGenericInformation;
        }
    }

    /**
     * Connects to the scheduler at the default schedulerRestUrl, using the current user credentials
     *
     * @throws Exception
     */
    public void connect() throws Exception {
        connect(SchedulerNodeClient.this.schedulerRestUrl);
    }

    /**
     * Connects to the scheduler at the specified schedulerRestUrl, using the current user credentials
     * @param url schedulerRestUrl of the scheduler
     * @throws Exception
     */
    public void connect(String url) throws Exception {
        CredData userCreds = decrypter.decrypt();
        if (client == null) {
            client = SchedulerClient.createInstance();
        }
        if (client.isConnected()) {
            return;
        }
        client.init(new ConnectionInfo(url, userCreds.getLogin(), userCreds.getPassword(), null, true));
    }

    private Map<String, String> addGlobalGenericInfoAndParentJobId(Map<String, String> genericInformation) {
        Map<String, String> newGenericInformation = new LinkedHashMap<>(globalGenericInformation);
        if (genericInformation != null) {
            newGenericInformation.putAll(genericInformation);
        }
        return addParentJobId(newGenericInformation);
    }

    private Map<String, String> addGlobalVariables(Map<String, String> variables) {
        Map<String, String> newVariables = new LinkedHashMap<>(globalVariables);
        if (variables != null) {
            newVariables.putAll(variables);
        }
        return newVariables;
    }

    private Map<String, JobVariable> addGlobalJobVariables(Map<String, JobVariable> variables) {
        Map<String, JobVariable> newVariables = new LinkedHashMap<>(globalJobVariables);
        if (variables != null) {
            newVariables.putAll(variables);
        }
        return newVariables;
    }

    private Map<String, String> addParentJobId(Map<String, String> genericInformation) {
        if (parentJobId != null) {
            Map<String, String> newGenericInformation;
            if (genericInformation == null) {
                newGenericInformation = new LinkedHashMap<>();
            } else {
                newGenericInformation = new LinkedHashMap<>(genericInformation);
            }
            newGenericInformation.put(PARENT_JOB_ID, parentJobId.value());
            return newGenericInformation;
        }
        return genericInformation;
    }

    @Override
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        renewSession();
        return client.getMyAccountUsage(startDate, endDate);
    }

    @Override
    public List<JobUsage> getAccountUsage(String user, Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        renewSession();
        return client.getAccountUsage(user, startDate, endDate);
    }

    @Override
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getUserSpaceURIs();
    }

    @Override
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getGlobalSpaceURIs();
    }

    @Override
    public JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        renewSession();
        return client.getJobResult(jobId);
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    @Override
    public List<TaskResult> getTaskResultAllIncarnations(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.getTaskResultAllIncarnations(jobId, taskName);
    }

    @Override
    public List<TaskResult> getTaskResultAllIncarnations(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.getTaskResultAllIncarnations(jobId, taskName);
    }

    @Override
    public boolean killTask(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.killTask(jobId, taskName);
    }

    @Override
    public boolean restartTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.restartTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.preemptTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.removeJob(jobId);
    }

    @Override
    public boolean removeJobs(List<JobId> jobIds) throws NotConnectedException, PermissionException {
        renewSession();
        return client.removeJobs(jobIds);
    }

    @Override
    public boolean removeJobs(long olderThan) throws NotConnectedException, PermissionException {
        renewSession();
        return client.removeJobs(olderThan);
    }

    @Override
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        client.listenJobLogs(jobId, appenderProvider);
    }

    @Override
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.killJob(jobId);
    }

    @Override
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.pauseJob(jobId);
    }

    @Override
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.resumeJob(jobId);
    }

    @Override
    public void changeJobPriority(JobId jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        renewSession();
        client.changeJobPriority(jobId, priority);
    }

    @Override
    public boolean changePolicy(String policyClassName) throws NotConnectedException, PermissionException {
        renewSession();
        return client.changePolicy(policyClassName);
    }

    @Override
    public boolean start() throws NotConnectedException, PermissionException {
        renewSession();
        return client.start();
    }

    @Override
    public boolean stop() throws NotConnectedException, PermissionException {
        renewSession();
        return client.stop();
    }

    @Override
    public boolean pause() throws NotConnectedException, PermissionException {
        renewSession();
        return client.pause();
    }

    @Override
    public boolean freeze() throws NotConnectedException, PermissionException {
        renewSession();
        return client.freeze();
    }

    @Override
    public boolean resume() throws NotConnectedException, PermissionException {
        renewSession();
        return client.resume();
    }

    @Override
    public boolean shutdown() throws NotConnectedException, PermissionException {
        renewSession();
        return client.shutdown();
    }

    @Override
    public boolean kill() throws NotConnectedException, PermissionException {
        renewSession();
        return client.kill();
    }

    @Override
    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        renewSession();
        return client.linkResourceManager(rmURL);
    }

    @Override
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        renewSession();
        return client.reloadPolicyConfiguration();
    }

    @Override
    public JobId submit(Job job)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        job.setGenericInformation(addGlobalGenericInfoAndParentJobId(job.getGenericInformation()));
        job.setGlobalGenericInformation(globalGenericInformation);
        job.setVariables(addGlobalJobVariables(job.getVariables()));
        return client.submit(job);
    }

    @Override
    public JobId reSubmit(JobId currentJobId, Map<String, String> jobVariables, Map<String, String> jobGenericInfos)
            throws NotConnectedException, UnknownJobException, PermissionException, JobCreationException,
            SubmissionClosedException {
        renewSession();
        return client.reSubmit(currentJobId,
                               addGlobalVariables(jobVariables),
                               addGlobalGenericInfoAndParentJobId(jobGenericInfos));
    }

    @Override
    public JobId submit(File job)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submit(job,
                             addGlobalVariables(Collections.emptyMap()),
                             addGlobalGenericInfoAndParentJobId(Collections.emptyMap()));
    }

    @Override
    public JobId submit(File job, Map<String, String> variables)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return submit(job, addGlobalVariables(variables), addGlobalGenericInfoAndParentJobId(Collections.emptyMap()));
    }

    @Override
    public JobId submit(File job, Map<String, String> variables, Map<String, String> genericInfos)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submit(job, addGlobalVariables(variables), addGlobalGenericInfoAndParentJobId(genericInfos));
    }

    @Override
    public JobId submit(URL job)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submit(job,
                             addGlobalVariables(Collections.emptyMap()),
                             addGlobalGenericInfoAndParentJobId(Collections.emptyMap()),
                             null);
    }

    @Override
    public JobId submit(URL job, Map<String, String> variables)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return submit(job,
                      addGlobalVariables(variables),
                      addGlobalGenericInfoAndParentJobId(Collections.emptyMap()),
                      null);
    }

    @Override
    public JobId submit(Map<String, String> genericInfos, URL job, Map<String, String> variables)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submit(job,
                             addGlobalVariables(variables),
                             addGlobalGenericInfoAndParentJobId(genericInfos),
                             null);
    }

    @Override
    public JobId submit(URL job, Map<String, String> variables, Map<String, String> headerParams)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return submit(job,
                      addGlobalVariables(variables),
                      addGlobalGenericInfoAndParentJobId(Collections.emptyMap()),
                      headerParams);
    }

    @Override
    public JobId submit(URL job, Map<String, String> variables, Map<String, String> genericInfos,
            Map<String, String> headerParams)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        if (client == null)
            throw new NotConnectedException("Client not connected, call connect() before using the scheduler client");
        return this.client.submit(job,
                                  addGlobalVariables(variables),
                                  addGlobalGenericInfoAndParentJobId(genericInfos),
                                  headerParams);
    }

    @Override
    public JobId submitFromCatalog(String catalogRestURL, String bucketName, String workflowName)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submitFromCatalog(catalogRestURL,
                                        bucketName,
                                        workflowName,
                                        addGlobalVariables(Collections.emptyMap()),
                                        addGlobalGenericInfoAndParentJobId(Collections.emptyMap()));
    }

    @Override
    public JobId submitFromCatalog(String catalogRestURL, String bucketName, String workflowName,
            Map<String, String> variables)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return submitFromCatalog(catalogRestURL,
                                 bucketName,
                                 workflowName,
                                 addGlobalVariables(variables),
                                 addGlobalGenericInfoAndParentJobId(Collections.emptyMap()));
    }

    @Override
    public JobId submitFromCatalog(String catalogRestURL, String bucketName, String workflowName,
            Map<String, String> variables, Map<String, String> genericInfo)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submitFromCatalog(catalogRestURL,
                                        bucketName,
                                        workflowName,
                                        addGlobalVariables(variables),
                                        addGlobalGenericInfoAndParentJobId(genericInfo));
    }

    @Override
    public JobId submitFromCatalog(String catalogRestURL, String calledWorkflow)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submitFromCatalog(catalogRestURL,
                                        calledWorkflow,
                                        addGlobalVariables(Collections.emptyMap()),
                                        addGlobalGenericInfoAndParentJobId(Collections.emptyMap()));
    }

    @Override
    public JobId submitFromCatalog(String catalogRestURL, String calledWorkflow, Map<String, String> variables)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submitFromCatalog(catalogRestURL,
                                        calledWorkflow,
                                        addGlobalVariables(variables),
                                        addGlobalGenericInfoAndParentJobId(Collections.emptyMap()));
    }

    @Override
    public JobId submitFromCatalog(String catalogRestURL, String calledWorkflow, Map<String, String> variables,
            Map<String, String> genericInfo)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        renewSession();
        return client.submitFromCatalog(catalogRestURL,
                                        calledWorkflow,
                                        addGlobalVariables(variables),
                                        addGlobalGenericInfoAndParentJobId(genericInfo));
    }

    @Override
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        renewSession();
        return client.getJobResult(jobId);
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.getTaskResult(jobId, taskName);
    }

    @Override
    public List<TaskResult> getTaskResultsByTag(JobId jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.getTaskResultsByTag(jobId, taskTag);
    }

    @Override
    public List<TaskResult> getTaskResultsByTag(String jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.getTaskResultsByTag(jobId, taskTag);
    }

    @Override
    public TaskResult getTaskResult(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.getTaskResult(jobId, taskName);
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    @Override
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.removeJob(jobId);
    }

    @Override
    public void listenJobLogs(String jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        client.listenJobLogs(jobId, appenderProvider);
    }

    @Override
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.killJob(jobId);
    }

    @Override
    public boolean killJobs(List<String> jobsId) throws NotConnectedException, PermissionException {
        renewSession();
        return client.killJobs(jobsId);
    }

    @Override
    public boolean killTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.killTask(jobId, taskName);
    }

    @Override
    public boolean restartTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.restartTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean finishInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.finishInErrorTask(jobId, taskName);
    }

    @Override
    public boolean restartInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.restartInErrorTask(jobId, taskName);
    }

    @Override
    public boolean preemptTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.preemptTask(jobId, taskName, restartDelay);
    }

    @Override
    public void enableRemoteVisualization(String jobId, String taskName, String connectionString)
            throws NotConnectedException, PermissionException, UnknownJobException, UnknownTaskException {
        client.enableRemoteVisualization(jobId, taskName, connectionString);
    }

    @Override
    public void registerService(String jobId, int serviceInstanceid, boolean enableActions)
            throws NotConnectedException, PermissionException, UnknownJobException {
        client.registerService(jobId, serviceInstanceid, enableActions);
    }

    @Override
    public void detachService(String jobId, int serviceInstanceid)
            throws NotConnectedException, PermissionException, UnknownJobException {
        client.detachService(jobId, serviceInstanceid);
    }

    @Override
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.pauseJob(jobId);
    }

    @Override
    public boolean restartAllInErrorTasks(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.restartAllInErrorTasks(jobId);
    }

    @Override
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.resumeJob(jobId);
    }

    @Override
    public void changeJobPriority(String jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        renewSession();
        client.changeJobPriority(jobId, priority);
    }

    @Override
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.getJobState(jobId);
    }

    @Override
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getStatus();
    }

    @Override
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.getJobState(jobId);
    }

    @Override
    public TaskState getTaskState(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        renewSession();
        return client.getTaskState(jobId, taskName);
    }

    @Override
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getState();
    }

    @Override
    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        renewSession();
        return client.getState(myJobsOnly);
    }

    @Override
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        renewSession();
        client.addEventListener(sel, myEventsOnly, events);
    }

    @Override
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly, boolean getCurrentState,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        renewSession();
        return client.addEventListener(sel, myEventsOnly, getCurrentState, events);
    }

    @Override
    public void removeEventListener() throws NotConnectedException, PermissionException {
        renewSession();
        client.removeEventListener();
    }

    @Override
    public void disconnect() throws NotConnectedException, PermissionException {
        if (client == null)
            throw new NotConnectedException("Client not connected, call connect() before using the scheduler client");
        client.disconnect();
    }

    @Override
    public boolean isConnected() {
        try {
            renewSession();
            return client.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getCurrentPolicy() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getCurrentPolicy();
    }

    @Override
    public Map getJobsToSchedule() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getJobsToSchedule();
    }

    @Override
    public List<TaskDescriptor> getTasksToSchedule() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getTasksToSchedule();
    }

    @Override
    public void renewSession() throws NotConnectedException {
        if (client == null)
            throw new NotConnectedException("Client not connected, call connect() before using the scheduler client");
        client.renewSession();
    }

    @Override
    public String getJobServerLogs(String id) throws UnknownJobException, NotConnectedException, PermissionException {
        renewSession();
        return client.getJobServerLogs(id);
    }

    @Override
    public String getTaskServerLogs(String id, String taskName)
            throws UnknownJobException, UnknownTaskException, NotConnectedException, PermissionException {
        renewSession();
        return client.getTaskServerLogs(id, taskName);
    }

    @Override
    public String getTaskServerLogsByTag(String id, String taskTag)
            throws UnknownJobException, NotConnectedException, PermissionException {
        renewSession();
        return client.getTaskServerLogsByTag(id, taskTag);
    }

    @Override
    public Page<JobInfo> getJobs(int offset, int limit, JobFilterCriteria filterCriteria,
            List<SortParameter<JobSortParameter>> sortParameters) throws NotConnectedException, PermissionException {
        renewSession();
        return client.getJobs(offset, limit, filterCriteria, sortParameters);
    }

    @Override
    public List<JobInfo> getJobsInfoList(List<String> jobsId) throws PermissionException, NotConnectedException {
        renewSession();
        return client.getJobsInfoList(jobsId);
    }

    @Override
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getUsers();
    }

    @Override
    public List<SchedulerUserInfo> getUsersWithJobs() throws NotConnectedException, PermissionException {
        renewSession();
        return client.getUsersWithJobs();
    }

    @Override
    public Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, Set<TaskStatus> taskStatuses,
            int offset, int limit) throws SchedulerException {
        renewSession();
        return client.getTaskIds(taskTag, from, to, mytasks, taskStatuses, offset, limit);
    }

    @Override
    public Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks, Set<TaskStatus> statuses,
            int offset, int limit, SortSpecifierContainer sortParams) throws SchedulerException {
        renewSession();
        return client.getTaskStates(taskTag, from, to, mytasks, statuses, offset, limit, sortParams);
    }

    @Override
    public JobInfo getJobInfo(String jobId) throws SchedulerException {
        renewSession();
        return client.getJobInfo(jobId);
    }

    @Override
    public boolean changeStartAt(JobId jobId, String startAt)
            throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.changeStartAt(jobId, startAt);
    }

    @Override
    public String getJobContent(JobId jobId) throws SchedulerException {
        renewSession();
        return client.getJobContent(jobId);
    }

    @Override
    public void init(ConnectionInfo connectionInfo) throws Exception {
        if (client == null)
            throw new NotConnectedException("Client not connected, call connect() before using the scheduler client");
        client.init(connectionInfo);
        renewSession();
    }

    @Override
    public ConnectionInfo getConnectionInfo() {
        return client.getConnectionInfo();
    }

    @Override
    public void setSession(String sid) {
        if (client == null)
            client = SchedulerClient.createInstance();
        client.setSession(sid);
    }

    @Override
    public String getSession() {
        //getSession expects null if session is not initialized
        if (client == null)
            return null;
        return client.getSession();
    }

    @Override
    public boolean isJobFinished(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.isJobFinished(jobId);
    }

    @Override
    public boolean isJobFinished(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.isJobFinished(jobId);
    }

    @Override
    public JobResult waitForJob(JobId jobId, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        renewSession();
        return client.waitForJob(jobId, timeout);
    }

    @Override
    public JobResult waitForJob(String jobId, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        renewSession();
        return client.waitForJob(jobId, timeout);
    }

    @Override
    public boolean isTaskFinished(String jobId, String taskName)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException {
        renewSession();
        return client.isTaskFinished(jobId, taskName);
    }

    @Override
    public TaskResult waitForTask(String jobId, String taskName, long timeout) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        renewSession();
        return client.waitForTask(jobId, taskName, timeout);
    }

    @Override
    public List<JobResult> waitForAllJobs(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        renewSession();
        return client.waitForAllJobs(jobIds, timeout);
    }

    @Override
    public Map.Entry<String, JobResult> waitForAnyJob(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        renewSession();
        return client.waitForAnyJob(jobIds, timeout);
    }

    @Override
    public Map.Entry<String, TaskResult> waitForAnyTask(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException {
        renewSession();
        return client.waitForAnyTask(jobId, taskNames, timeout);
    }

    @Override
    public List<Map.Entry<String, TaskResult>> waitForAllTasks(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException {
        renewSession();
        return client.waitForAllTasks(jobId, taskNames, timeout);
    }

    @Override
    public boolean pushFile(String spacename, String pathname, String filename, String file)
            throws NotConnectedException, PermissionException {
        renewSession();
        return client.pushFile(spacename, pathname, filename, file);
    }

    @Override
    public void pullFile(String space, String pathname, String outputFile)
            throws NotConnectedException, PermissionException {
        renewSession();
        client.pullFile(space, pathname, outputFile);
    }

    @Override
    public boolean deleteFile(String space, String pathname) throws NotConnectedException, PermissionException {
        renewSession();
        return client.deleteFile(space, pathname);
    }

    @Override
    public void putThirdPartyCredential(String key, String value) throws SchedulerException {
        renewSession();
        client.putThirdPartyCredential(key, value);
    }

    @Override
    public Set<String> thirdPartyCredentialsKeySet() throws SchedulerException {
        renewSession();
        return client.thirdPartyCredentialsKeySet();
    }

    @Override
    public void removeThirdPartyCredential(String key) throws SchedulerException {
        renewSession();
        client.removeThirdPartyCredential(key);
    }

    @Override
    public Map<Object, Object> getPortalConfiguration() throws SchedulerException {
        renewSession();
        return client.getPortalConfiguration();
    }

    @Override
    public String getCurrentUser() throws NotConnectedException {
        renewSession();
        return client.getCurrentUser();
    }

    @Override
    public UserData getCurrentUserData() throws NotConnectedException {
        renewSession();
        return client.getCurrentUserData();
    }

    @Override
    public Subject getSubject() throws NotConnectedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getSchedulerProperties() throws SchedulerException {
        renewSession();
        return client.getSchedulerProperties();
    }

    @Override
    public TaskStatesPage getTaskPaginated(String jobId, int offset, int limit)
            throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.getTaskPaginated(jobId, offset, limit);
    }

    @Override
    public TaskStatesPage getTaskPaginated(String jobId, String statusFilter, int offset, int limit)
            throws NotConnectedException, UnknownJobException, PermissionException {
        renewSession();
        return client.getTaskPaginated(jobId, statusFilter, offset, limit);
    }

    @Override
    public List<TaskResult> getPreciousTaskResults(String jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        renewSession();
        return client.getPreciousTaskResults(jobId);
    }

    @Override
    public Map<Long, Map<String, Serializable>> getJobResultMaps(List<String> jobsId)
            throws UnknownJobException, NotConnectedException, PermissionException {
        renewSession();
        return client.getJobResultMaps(jobsId);
    }

    @Override
    public Map<Long, List<String>> getPreciousTaskNames(List<String> jobsId) throws SchedulerException {
        return client.getPreciousTaskNames(jobsId);
    }

    @Override
    public boolean checkJobPermissionMethod(String jobId, String method) throws SchedulerException {
        renewSession();
        return client.checkJobPermissionMethod(jobId, method);
    }

    @Override
    public List<String> checkJobsPermissionMethod(List<String> jobIds, String method) throws SchedulerException {
        renewSession();
        return client.checkJobsPermissionMethod(jobIds, method);
    }

    @Override
    public Set<String> addJobSignal(String jobId, String signal)
            throws NotConnectedException, SignalApiException, UnknownJobException, PermissionException {
        renewSession();
        return client.addJobSignal(jobId, signal);
    }

    @Override
    public Map<String, Map<String, Boolean>> checkJobsPermissionMethods(List<String> jobIds, List<String> methods)
            throws NotConnectedException, UnknownJobException {
        renewSession();
        return client.checkJobsPermissionMethods(jobIds, methods);
    }
}
