/*
 *  *
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive.scheduler.task.client;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.*;
import org.ow2.proactive.scheduler.common.exception.*;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive.scheduler.task.utils.Decrypter;

import java.security.KeyException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Scheduler api available as a variable during the execution of a task. it is based on the ISchedulerClient interface.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class SchedulerNodeClient implements ISchedulerClient {

    private static final Logger logger = Logger.getLogger(SchedulerNodeClient.class);

    private Decrypter decrypter;

    private String url;

    private ISchedulerClient client;

    public SchedulerNodeClient(Decrypter decrypter, String url) {
        SchedulerNodeClient.this.decrypter = decrypter;
        SchedulerNodeClient.this.url = url;
        client = SchedulerClient.createInstance();

    }

    /**
     * Connects to the scheduler at the default url, using the current user credentials
     *
     * @throws Exception
     */
    public void connect() throws Exception {
        connect(SchedulerNodeClient.this.url);
    }

    /**
     * Connects to the scheduler at the specified url, using the current user credentials
     * @param url url of the scheduler
     * @throws Exception
     */
    public void connect(String url) throws Exception {
        System.out.println("[SchedulerNodeClient] Connecting to scheduler at url " + url);
        CredData userCreds = decrypter.decrypt();
        client.init(new ConnectionInfo(url, userCreds.getLogin(), userCreds.getPassword(), null, true));
        System.out.println("[SchedulerNodeClient] Connected");
    }

    @Override
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate) throws NotConnectedException, PermissionException {
        return client.getMyAccountUsage(startDate, endDate);
    }

    @Override
    public List<JobUsage> getAccountUsage(String user, Date startDate, Date endDate) throws NotConnectedException, PermissionException {
        return client.getAccountUsage(user, startDate, endDate);
    }

    @Override
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        return client.getUserSpaceURIs();
    }

    @Override
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        return client.getGlobalSpaceURIs();
    }

    @Override
    public JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        return client.getJobResult(jobId);
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    @Override
    public boolean killTask(JobId jobId, String taskName) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.killTask(jobId, taskName);
    }

    @Override
    public boolean restartTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.restartTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.preemptTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.removeJob(jobId);
    }

    @Override
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws NotConnectedException, UnknownJobException, PermissionException {
        client.listenJobLogs(jobId, appenderProvider);
    }

    @Override
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.killJob(jobId);
    }

    @Override
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.pauseJob(jobId);
    }

    @Override
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.resumeJob(jobId);
    }

    @Override
    public void changeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        client.changeJobPriority(jobId, priority);
    }

    @Override
    public boolean changePolicy(String policyClassName) throws NotConnectedException, PermissionException {
        return client.changePolicy(policyClassName);
    }

    @Override
    public boolean start() throws NotConnectedException, PermissionException {
        return client.start();
    }

    @Override
    public boolean stop() throws NotConnectedException, PermissionException {
        return client.stop();
    }

    @Override
    public boolean pause() throws NotConnectedException, PermissionException {
        return client.pause();
    }

    @Override
    public boolean freeze() throws NotConnectedException, PermissionException {
        return client.freeze();
    }

    @Override
    public boolean resume() throws NotConnectedException, PermissionException {
        return client.resume();
    }

    @Override
    public boolean shutdown() throws NotConnectedException, PermissionException {
        return client.shutdown();
    }

    @Override
    public boolean kill() throws NotConnectedException, PermissionException {
        return client.kill();
    }

    @Override
    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        return client.linkResourceManager(rmURL);
    }

    @Override
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        return client.reloadPolicyConfiguration();
    }

    @Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return client.submit(job);
    }

    @Override
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        return client.getJobResult(jobId);
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.getTaskResult(jobId, taskName);
    }

    @Override
    public List<TaskResult> getTaskResultsByTag(JobId jobId, String taskTag) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.getTaskResultsByTag(jobId, taskTag);
    }

    @Override
    public List<TaskResult> getTaskResultsByTag(String jobId, String taskTag) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.getTaskResultsByTag(jobId, taskTag);
    }

    @Override
    public TaskResult getTaskResult(JobId jobId, String taskName) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.getTaskResult(jobId, taskName);
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    @Override
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.removeJob(jobId);
    }

    @Override
    public void listenJobLogs(String jobId, AppenderProvider appenderProvider) throws NotConnectedException, UnknownJobException, PermissionException {
        client.listenJobLogs(jobId, appenderProvider);
    }

    @Override
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.killJob(jobId);
    }

    @Override
    public boolean killTask(String jobId, String taskName) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.killTask(jobId, taskName);
    }

    @Override
    public boolean restartTask(String jobId, String taskName, int restartDelay) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.restartTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean restartInErrorTask(String jobId, String taskName) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.restartInErrorTask(jobId, taskName);
    }

    @Override
    public boolean preemptTask(String jobId, String taskName, int restartDelay) throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return client.preemptTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.pauseJob(jobId);
    }

    @Override
    public boolean restartAllInErrorTasks(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.restartAllInErrorTasks(jobId);
    }

    @Override
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.resumeJob(jobId);
    }

    @Override
    public void changeJobPriority(String jobId, JobPriority priority) throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        client.changeJobPriority(jobId, priority);
    }

    @Override
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.getJobState(jobId);
    }

    @Override
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        return client.getStatus();
    }

    @Override
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.getJobState(jobId);
    }

    @Override
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return client.getState();
    }

    @Override
    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        return client.getState(myJobsOnly);
    }

    @Override
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events) throws NotConnectedException, PermissionException {
        client.addEventListener(sel, myEventsOnly, events);
    }

    @Override
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly, boolean getCurrentState, SchedulerEvent... events) throws NotConnectedException, PermissionException {
        return client.addEventListener(sel, myEventsOnly, getCurrentState, events);
    }

    @Override
    public void removeEventListener() throws NotConnectedException, PermissionException {
        client.removeEventListener();
    }

    @Override
    public void disconnect() throws NotConnectedException, PermissionException {
        client.disconnect();
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public void renewSession() throws NotConnectedException {
        client.renewSession();
    }

    @Override
    public String getJobServerLogs(String id) throws UnknownJobException, NotConnectedException, PermissionException {
        return client.getJobServerLogs(id);
    }

    @Override
    public String getTaskServerLogs(String id, String taskName) throws UnknownJobException, UnknownTaskException, NotConnectedException, PermissionException {
        return client.getTaskServerLogs(id, taskName);
    }

    @Override
    public String getTaskServerLogsByTag(String id, String taskTag) throws UnknownJobException, NotConnectedException, PermissionException {
        return client.getTaskServerLogsByTag(id, taskTag);
    }

    @Override
    public Page<JobInfo> getJobs(int offset, int limit, JobFilterCriteria filterCriteria, List<SortParameter<JobSortParameter>> sortParameters) throws NotConnectedException, PermissionException {
        return client.getJobs(offset, limit, filterCriteria, sortParameters);
    }

    @Override
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        return client.getUsers();
    }

    @Override
    public List<SchedulerUserInfo> getUsersWithJobs() throws NotConnectedException, PermissionException {
        return client.getUsersWithJobs();
    }

    @Override
    public Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, boolean running, boolean pending, boolean finished, int offset, int limit) throws NotConnectedException, PermissionException {
        return client.getTaskIds(taskTag, from, to, mytasks, running, pending, finished, offset, limit);
    }

    @Override
    public Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks, boolean running, boolean pending, boolean finished, int offset, int limit, SortSpecifierContainer sortParams) throws NotConnectedException, PermissionException {
        return client.getTaskStates(taskTag, from, to, mytasks, running, pending, finished, offset, limit, sortParams);
    }

    @Override
    public JobInfo getJobInfo(String jobId) throws UnknownJobException, NotConnectedException, PermissionException {
        return client.getJobInfo(jobId);
    }

    @Override
    public boolean changeStartAt(JobId jobId, String startAt) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.changeStartAt(jobId, startAt);
    }

    @Override
    public JobId copyJobAndResubmitWithGeneralInfo(JobId jobId, Map<String, String> generalInfo) throws NotConnectedException, UnknownJobException, PermissionException, SubmissionClosedException, JobCreationException {
        return client.copyJobAndResubmitWithGeneralInfo(jobId, generalInfo);
    }

    @Override
    public void init(ConnectionInfo connectionInfo) throws Exception {
        client.init(connectionInfo);
    }

    @Override
    public void setSession(String sid) {
        client.setSession(sid);
    }

    @Override
    public String getSession() {
        return client.getSession();
    }

    @Override
    public boolean isJobFinished(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.isJobFinished(jobId);
    }

    @Override
    public boolean isJobFinished(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return client.isJobFinished(jobId);
    }

    @Override
    public JobResult waitForJob(JobId jobId, long timeout) throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return client.waitForJob(jobId, timeout);
    }

    @Override
    public JobResult waitForJob(String jobId, long timeout) throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return client.waitForJob(jobId, timeout);
    }

    @Override
    public boolean isTaskFinished(String jobId, String taskName) throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException {
        return client.isTaskFinished(jobId, taskName);
    }

    @Override
    public TaskResult waitForTask(String jobId, String taskName, long timeout) throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        return client.waitForTask(jobId, taskName, timeout);
    }

    @Override
    public List<JobResult> waitForAllJobs(List<String> jobIds, long timeout) throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return client.waitForAllJobs(jobIds, timeout);
    }

    @Override
    public Map.Entry<String, JobResult> waitForAnyJob(List<String> jobIds, long timeout) throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return client.waitForAnyJob(jobIds, timeout);
    }

    @Override
    public Map.Entry<String, TaskResult> waitForAnyTask(String jobId, List<String> taskNames, long timeout) throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        return client.waitForAnyTask(jobId, taskNames, timeout);
    }

    @Override
    public List<Map.Entry<String, TaskResult>> waitForAllTasks(String jobId, List<String> taskNames, long timeout) throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        return client.waitForAllTasks(jobId, taskNames, timeout);
    }

    @Override
    public boolean pushFile(String spacename, String pathname, String filename, String file) throws NotConnectedException, PermissionException {
        return client.pushFile(spacename, pathname, filename, file);
    }

    @Override
    public void pullFile(String space, String pathname, String outputFile) throws NotConnectedException, PermissionException {
        client.pullFile(space, pathname, outputFile);
    }

    @Override
    public boolean deleteFile(String space, String pathname) throws NotConnectedException, PermissionException {
        return client.deleteFile(space, pathname);
    }

    @Override
    public void putThirdPartyCredential(String key, String value) throws NotConnectedException, PermissionException, KeyException {
        client.putThirdPartyCredential(key, value);
    }

    @Override
    public Set<String> thirdPartyCredentialsKeySet() throws NotConnectedException, PermissionException {
        return client.thirdPartyCredentialsKeySet();
    }

    @Override
    public void removeThirdPartyCredential(String key) throws NotConnectedException, PermissionException {
        client.removeThirdPartyCredential(key);
    }
}
