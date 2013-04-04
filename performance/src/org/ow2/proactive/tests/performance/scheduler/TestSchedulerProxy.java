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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.scheduler;

import java.security.KeyException;
import java.util.Date;
import java.util.List;

import javax.management.JMException;
import javax.security.auth.login.LoginException;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;


public class TestSchedulerProxy implements Scheduler {

    protected Scheduler target;

    private String jmxROUrl;

    public static TestSchedulerProxy connectWithProxy(String url, CredData credData, long timeout)
            throws Exception {
        TestSchedulerProxy proxy = PAActiveObject.newActive(TestSchedulerProxy.class, new Object[] {});
        proxy.init(url, credData, timeout);
        return proxy;
    }

    public void init(String url, CredData credData, long timeout) throws ConnectionException, LoginException,
            KeyException, AlreadyConnectedException, JMException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin(url, timeout);
        Credentials cred = Credentials.createCredentials(credData, auth.getPublicKey());
        target = auth.login(cred);
        jmxROUrl = auth.getJMXConnectorURL(JMXTransportProtocol.RO);
    }

    public String getJmxROUrl() {
        return jmxROUrl;
    }

    @Override
    public JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {
        return target.getJobResult(jobId);
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return target.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    @Override
    public boolean killTask(JobId jobId, String taskName) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        return target.killTask(jobId, taskName);
    }

    @Override
    public boolean restartTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return target.restartTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return target.preemptTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.removeJob(jobId);
    }

    @Override
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException {
        target.listenJobLogs(jobId, appenderProvider);
    }

    @Override
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.killJob(jobId);
    }

    @Override
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.pauseJob(jobId);
    }

    @Override
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.resumeJob(jobId);
    }

    @Override
    public void changeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        target.changeJobPriority(jobId, priority);
    }

    @Override
    public boolean changePolicy(String policyClassName) throws NotConnectedException, PermissionException {
        return target.changePolicy(policyClassName);
    }

    @Override
    public boolean start() throws NotConnectedException, PermissionException {
        return target.start();
    }

    @Override
    public boolean stop() throws NotConnectedException, PermissionException {
        return target.stop();
    }

    @Override
    public boolean pause() throws NotConnectedException, PermissionException {
        return target.pause();
    }

    @Override
    public boolean freeze() throws NotConnectedException, PermissionException {
        return target.freeze();
    }

    @Override
    public boolean resume() throws NotConnectedException, PermissionException {
        return target.resume();
    }

    @Override
    public boolean shutdown() throws NotConnectedException, PermissionException {
        return target.shutdown();
    }

    @Override
    public boolean kill() throws NotConnectedException, PermissionException {
        return target.kill();
    }

    @Override
    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        return target.linkResourceManager(rmURL);
    }

    @Override
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        return target.reloadPolicyConfiguration();
    }

    @Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        return target.submit(job);
    }

    @Override
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {
        return target.getJobResult(jobId);
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return target.getTaskResult(jobId, taskName);
    }

    @Override
    public TaskResult getTaskResult(JobId jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return target.getTaskResult(jobId, taskName);
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return target.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    @Override
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.removeJob(jobId);
    }

    @Override
    public void listenJobLogs(String jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException {
        target.listenJobLogs(jobId, appenderProvider);
    }

    @Override
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.killJob(jobId);
    }

    @Override
    public boolean killTask(String jobId, String taskName) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        return target.killTask(jobId, taskName);
    }

    @Override
    public boolean restartTask(String jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return target.restartTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean preemptTask(String jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return target.preemptTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.pauseJob(jobId);
    }

    @Override
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.resumeJob(jobId);
    }

    @Override
    public void changeJobPriority(String jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        target.changeJobPriority(jobId, priority);
    }

    @Override
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.getJobState(jobId);
    }

    @Override
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        return target.getStatus();
    }

    @Override
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return target.getJobState(jobId);
    }

    @Override
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return target.getState();
    }

    @Override
    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        return target.getState(myJobsOnly);
    }

    @Override
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        target.addEventListener(sel, myEventsOnly, events);
    }

    @Override
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            boolean getCurrentState, SchedulerEvent... events) throws NotConnectedException,
            PermissionException {
        return target.addEventListener(sel, myEventsOnly, getCurrentState, events);
    }

    @Override
    public void removeEventListener() throws NotConnectedException, PermissionException {
        target.removeEventListener();
    }

    @Override
    public void disconnect() throws NotConnectedException, PermissionException {
        target.disconnect();
        PAActiveObject.terminateActiveObject(true);
    }

    @Override
    public boolean isConnected() {
        return target.isConnected();
    }

    @Override
    public void renewSession() throws NotConnectedException {
        target.renewSession();
    }

    @Override
    public String getJobServerLogs(String id) throws UnknownJobException, NotConnectedException,
            PermissionException {
        return target.getJobServerLogs(id);
    }

    @Override
    public String getTaskServerLogs(String id, String taskName) throws UnknownJobException,
            UnknownTaskException, NotConnectedException, PermissionException {
        return target.getTaskServerLogs(id, taskName);
    }

    @Override
    public List<JobInfo> getJobs(int index, int range, JobFilterCriteria filterCriteria,
            List<SortParameter<JobSortParameter>> sortParameters) throws NotConnectedException,
            PermissionException {
        return target.getJobs(index, range, filterCriteria, sortParameters);
    }

    @Override
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        return target.getUsers();
    }

    @Override
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate) throws NotConnectedException,
            PermissionException {
        return target.getMyAccountUsage(startDate, endDate);
    }
}
