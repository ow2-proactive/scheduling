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
package org.ow2.proactive.scheduler.common.util;

import java.io.Serializable;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
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
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.utils.console.MBeanInfoViewer;


/**
 * This class implements an active object managing a connection to the Scheduler (a proxy to the Scheduler)
 * You must init the proxy by calling the {@link #init(String, String, String)} method after having created it
 */
@ActiveObject
public class SchedulerProxyUserInterface implements Scheduler, Serializable {

    protected Scheduler uischeduler;

    protected MBeanInfoViewer mbeaninfoviewer;

    public static final Logger logger = Logger.getLogger(SchedulerProxyUserInterface.class);

    /*
     * a reference to a stub on this active object
     */
    private static SchedulerProxyUserInterface activeInstance;

    /**
     * Default constructor demanded by ProActive.
     * WARNING: Singleton pattern: Use getActiveInstance() instead of this constructor
     * @deprecated
     */
    public SchedulerProxyUserInterface() {

    }

    /**
     * Singleton active object constructor.
     * Creates the singleton
     *
     * Creates an active object on this and returns a reference to its stub.
     * If the active object is already created, returns the reference
     * @return
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */

    public static SchedulerProxyUserInterface getActiveInstance() throws ActiveObjectCreationException, NodeException {
        if (activeInstance != null)
            return activeInstance;

        activeInstance = PAActiveObject.newActive(SchedulerProxyUserInterface.class, new Object[] {});
        return activeInstance;
    }

    /**
     * initialize the connection the scheduler. 
     * Must be called only once
     * @param url the scheduler's url 
     * @param credentials the credential to be passed to the scheduler
     * @throws SchedulerException thrown if the scheduler is not available
     * @throws LoginException thrown if the credential is invalid
     */
    public void init(String url, Credentials credentials) throws SchedulerException, LoginException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        this.uischeduler = auth.login(credentials);
        mbeaninfoviewer = new MBeanInfoViewer(auth, null, credentials);
    }

    /**
     * initialize the connection the scheduler. 
     * Must be called only once.
     * Create the corresponding credential object before sending it
     * to the scheduler.
     * @param url the scheduler's url 
     * @param user the username to use
     * @param pwd the password to use
     * @throws SchedulerException thrown if the scheduler is not available
     * @throws LoginException if the couple username/password is invalid
     */
    public void init(String url, String user, String pwd) throws SchedulerException, LoginException {
        CredData cred = new CredData(CredData.parseLogin(user), CredData.parseDomain(user), pwd);
        init(url, cred);
    }

    /**
     * initialize the connection the scheduler. 
     * Must be called only once.
     * Create the corresponding credential object before sending it
     * to the scheduler.
     * @param url the scheduler's url 
     * @param credData the credential object that contains user-related data
     * @throws SchedulerException thrown if the scheduler is not available
     * @throws LoginException if the couple username/password is invalid
     * @since Scheduling 3.1.0
     */
    public void init(String url, CredData credData) throws SchedulerException, LoginException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        PublicKey pubKey = auth.getPublicKey();

        try {

            Credentials cred = Credentials.createCredentials(credData, pubKey);
            this.uischeduler = auth.login(cred);
            mbeaninfoviewer = new MBeanInfoViewer(auth, credData.getLogin(), cred);
        } catch (KeyException e) {
            throw new InternalSchedulerException(e);
        }
    }

    /**
     * Subscribes a listener to the Scheduler
     */
    @Override
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly, boolean getCurrentState,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        checkSchedulerConnection();

        return uischeduler.addEventListener(sel, myEventsOnly, true, events);

    }

    @Override
    public void disconnect() throws NotConnectedException, PermissionException {
        if (uischeduler == null)
            throw new NotConnectedException("Not connected to the scheduler.");

        uischeduler.disconnect();

    }

    @Override
    public boolean isConnected() {
        if (uischeduler == null) {
            return false;
        } else
            try {
                return uischeduler.isConnected();
            } catch (Exception e) {
                logger.error("Error when callling " + this.getClass().getCanonicalName() +
                             " -> isConnected() method: " + e.getMessage() + ". The connection is considered lost. ",
                             e);
                return false;
            }
    }

    @Override
    public String getCurrentPolicy() throws NotConnectedException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.getCurrentPolicy();
    }

    @Override
    public Map getJobsToSchedule() throws NotConnectedException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.getJobsToSchedule();
    }

    @Override
    public List<TaskDescriptor> getTasksToSchedule() throws NotConnectedException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.getTasksToSchedule();
    }

    @Override
    public void renewSession() throws NotConnectedException {
        checkSchedulerConnection();
        uischeduler.renewSession();
    }

    @Override
    public void removeEventListener() throws NotConnectedException, PermissionException {
        checkSchedulerConnection();
        uischeduler.removeEventListener();

    }

    @Override
    public JobId submit(Job job)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        checkSchedulerConnection();
        return uischeduler.submit(job);
    }

    @Override
    public void changeJobPriority(JobId jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        checkSchedulerConnection();
        uischeduler.changeJobPriority(jobId, priority);

    }

    @Override
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        checkSchedulerConnection();
        return uischeduler.getJobResult(jobId);
    }

    @Override
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        return uischeduler.getUserSpaceURIs();
    }

    @Override
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        return uischeduler.getGlobalSpaceURIs();
    }

    @Override
    public JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        checkSchedulerConnection();
        return uischeduler.getJobResult(jobId);
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.getTaskResult(jobId, taskName);
    }

    @Override
    public TaskResult getTaskResult(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.getTaskResult(jobId, taskName);
    }

    @Override
    public List<TaskResult> getTaskResultsByTag(JobId jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.getTaskResultsByTag(jobId, taskTag);
    }

    @Override
    public List<TaskResult> getTaskResultsByTag(String jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.getTaskResultsByTag(jobId, taskTag);
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return uischeduler.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return uischeduler.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    public boolean killTask(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.killTask(jobId, taskName);
    }

    public boolean restartTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.restartTask(jobId, taskName, restartDelay);
    }

    public boolean preemptTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.preemptTask(jobId, taskName, restartDelay);
    }

    public boolean killTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.killTask(jobId, taskName);
    }

    public boolean restartTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.restartTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean finishInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.finishInErrorTask(jobId, taskName);
    }

    @Override
    public boolean restartInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.restartInErrorTask(jobId, taskName);
    }

    public boolean preemptTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.preemptTask(jobId, taskName, restartDelay);
    }

    @Override
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.killJob(jobId);
    }

    @Override
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException {
        checkSchedulerConnection();
        uischeduler.listenJobLogs(jobId, appenderProvider);

    }

    @Override
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.pauseJob(jobId);

    }

    @Override
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.removeJob(jobId);

    }

    @Override
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        checkSchedulerConnection();
        return uischeduler.resumeJob(jobId);
    }

    private void checkSchedulerConnection() throws NotConnectedException {
        if (uischeduler == null) {
            throw new NotConnectedException("Not connected to the scheduler.");
        }
    }

    @Override
    public void changeJobPriority(String jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        uischeduler.changeJobPriority(jobId, priority);

    }

    @Override
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        return uischeduler.getStatus();
    }

    @Override
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return uischeduler.killJob(jobId);
    }

    @Override
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return uischeduler.pauseJob(jobId);
    }

    @Override
    public boolean restartAllInErrorTasks(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return uischeduler.restartAllInErrorTasks(jobId);
    }

    @Override
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return uischeduler.removeJob(jobId);
    }

    @Override
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return uischeduler.resumeJob(jobId);
    }

    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        uischeduler.addEventListener(sel, myEventsOnly, events);
    }

    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return uischeduler.getJobState(jobId);
    }

    public void listenJobLogs(String jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException {
        uischeduler.listenJobLogs(jobId, appenderProvider);
    }

    public boolean changePolicy(String newPolicyClassName) throws NotConnectedException, PermissionException {
        return uischeduler.changePolicy(newPolicyClassName);
    }

    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        return uischeduler.reloadPolicyConfiguration();
    }

    public boolean freeze() throws NotConnectedException, PermissionException {
        return uischeduler.freeze();
    }

    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return uischeduler.getJobState(jobId);
    }

    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return uischeduler.getState();
    }

    public boolean kill() throws NotConnectedException, PermissionException {
        return uischeduler.kill();
    }

    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        return uischeduler.linkResourceManager(rmURL);
    }

    public boolean pause() throws NotConnectedException, PermissionException {
        return uischeduler.pause();
    }

    public boolean resume() throws NotConnectedException, PermissionException {
        return uischeduler.resume();
    }

    public boolean shutdown() throws NotConnectedException, PermissionException {
        return uischeduler.shutdown();
    }

    public boolean start() throws NotConnectedException, PermissionException {
        return uischeduler.start();
    }

    public boolean stop() throws NotConnectedException, PermissionException {
        return uischeduler.stop();
    }

    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        return uischeduler.getState(myJobsOnly);
    }

    /**
     *
     * Return the informations about the Scheduler MBean as a formatted string.
     * The first time this method is called it connects to the JMX connector server.
     * The default behavior will try to establish a connection using RMI protocol, if it fails
     * the RO (Remote Object) protocol is used.
     *
     * @param mbeanName the object name of the MBean
     * @return the informations about the MBean as a formatted string
     *
     * @see MBeanInfoViewer#getInfo(String)
     */
    @Deprecated
    public String getInfo(String mbeanName) {
        try {
            return mbeaninfoviewer.getInfo(mbeanName);
        } catch (RuntimeException e) {
            return e.getMessage() + ", you are probably not authorized to access to this information.";
        }
    }

    /**
     * Return the informations about the Scheduler MBean as a Map.
     * The first time this method is called it connects to the JMX connector server.
     * The default behavior will try to establish a connection using RMI protocol, if it fails 
     * the RO (Remote Object) protocol is used.
     *
     * @param mbeanNameAsString the object name of the MBean
     * @return the informations about the MBean as a formatted string
     * 
     * @throws RuntimeException if mbean cannot access or connect the service
     */
    public Map<String, String> getMappedInfo(final String mbeanNameAsString) throws RuntimeException {
        return mbeaninfoviewer.getMappedInfo(mbeanNameAsString);
    }

    @Override
    public String getJobServerLogs(String id) throws UnknownJobException, NotConnectedException, PermissionException {
        return uischeduler.getJobServerLogs(id);
    }

    @Override
    public String getTaskServerLogs(String id, String taskName)
            throws UnknownJobException, UnknownTaskException, NotConnectedException, PermissionException {
        return uischeduler.getTaskServerLogs(id, taskName);
    }

    @Override
    public String getTaskServerLogsByTag(String id, String taskTag)
            throws UnknownJobException, NotConnectedException, PermissionException {
        return uischeduler.getTaskServerLogsByTag(id, taskTag);
    }

    @Override
    public Page<JobInfo> getJobs(int index, int range, JobFilterCriteria filterCriteria,
            List<SortParameter<JobSortParameter>> sortParameters) throws NotConnectedException, PermissionException {
        return uischeduler.getJobs(index, range, filterCriteria, sortParameters);
    }

    @Override
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        return uischeduler.getUsers();
    }

    @Override
    public List<SchedulerUserInfo> getUsersWithJobs() throws NotConnectedException, PermissionException {
        return uischeduler.getUsersWithJobs();
    }

    @Override
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        return uischeduler.getMyAccountUsage(startDate, endDate);
    }

    @Override
    public List<JobUsage> getAccountUsage(String user, Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        return uischeduler.getAccountUsage(user, startDate, endDate);
    }

    @Override
    public void putThirdPartyCredential(String key, String value)
            throws NotConnectedException, KeyException, PermissionException {
        uischeduler.putThirdPartyCredential(key, value);
    }

    @Override
    public Set<String> thirdPartyCredentialsKeySet() throws NotConnectedException, PermissionException {
        return uischeduler.thirdPartyCredentialsKeySet();
    }

    @Override
    public void removeThirdPartyCredential(String key) throws NotConnectedException, PermissionException {
        uischeduler.removeThirdPartyCredential(key);
    }

    @Override
    public Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, boolean running,
            boolean pending, boolean finished, int offset, int limit)
            throws NotConnectedException, PermissionException {
        return uischeduler.getTaskIds(taskTag, from, to, mytasks, running, pending, finished, offset, limit);
    }

    @Override
    public Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks, boolean running,
            boolean pending, boolean finished, int offset, int limit, SortSpecifierContainer sortParams)
            throws NotConnectedException, PermissionException {
        return uischeduler.getTaskStates(taskTag,
                                         from,
                                         to,
                                         mytasks,
                                         running,
                                         pending,
                                         finished,
                                         offset,
                                         limit,
                                         sortParams);
    }

    @Override
    public JobInfo getJobInfo(String jobId) throws UnknownJobException, NotConnectedException, PermissionException {
        return uischeduler.getJobInfo(jobId);
    }

    @Override
    public boolean changeStartAt(JobId jobId, String startAt)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return uischeduler.changeStartAt(jobId, startAt);
    }

    @Override
    public JobId copyJobAndResubmitWithGeneralInfo(JobId jobId, Map<String, String> generalInfo)
            throws NotConnectedException, UnknownJobException, PermissionException, SubmissionClosedException,
            JobCreationException {
        return uischeduler.copyJobAndResubmitWithGeneralInfo(jobId, generalInfo);
    }

    @Override
    public Map<Object, Object> getPortalConfiguration() throws NotConnectedException, PermissionException {
        return uischeduler.getPortalConfiguration();
    }

    @Override
    public String getCurrentUser() throws NotConnectedException {
        return uischeduler.getCurrentUser();
    }

    @Override
    public UserData getCurrentUserData() throws NotConnectedException {
        return uischeduler.getCurrentUserData();
    }

    @Override
    public Map getSchedulerProperties() throws NotConnectedException, PermissionException {
        return uischeduler.getSchedulerProperties();
    }

}
