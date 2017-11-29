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
package org.ow2.proactive_grid_cloud_portal.smartproxy;

import static org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.Dataspace.USER;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive.scheduler.rest.ds.DataSpaceClient;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;
import org.ow2.proactive.scheduler.rest.ds.LocalDestination;
import org.ow2.proactive.scheduler.rest.ds.LocalDirSource;
import org.ow2.proactive.scheduler.rest.ds.RemoteDestination;
import org.ow2.proactive.scheduler.rest.ds.RemoteSource;
import org.ow2.proactive.scheduler.smartproxy.common.AbstractSmartProxy;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedJob;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedTask;
import org.ow2.proactive.scheduler.smartproxy.common.SchedulerEventListenerExtended;
import org.ow2.proactive_grid_cloud_portal.common.FileType;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;


/**
 * Smart proxy implementation that relies on the REST API for communicating with dataspaces
 * and websockets to push notifications to clients.
 * <p>
 * Any instance of this class must be initialized by calling the {@link ISchedulerClient#init(ConnectionInfo)}
 * method before executing any subsequent method. Not
 * respecting this rule may lead to an NPE or an unexpected behaviour.
 *
 * @author The ProActive Team
 */
public class RestSmartProxyImpl extends AbstractSmartProxy<RestJobTrackerImpl>
        implements ISchedulerClient, SchedulerEventListener {

    private static final Logger logger = Logger.getLogger(RestSmartProxyImpl.class);

    private ISchedulerClient restSchedulerClient;

    private IDataSpaceClient restDataSpaceClient;

    public RestSmartProxyImpl() {
        super(new RestJobTrackerImpl());
    }

    @Override
    public void init(ConnectionInfo connectionInfo) {
        try {
            this.connectionInfo = connectionInfo;
            this.restSchedulerClient = SchedulerClient.createInstance();

            this.restSchedulerClient.init(new ConnectionInfo(connectionInfo.getUrl(),
                                                             connectionInfo.getLogin(),
                                                             connectionInfo.getPassword(),
                                                             connectionInfo.getCredentialFile(),
                                                             connectionInfo.isInsecure()));

            DataSpaceClient restDsClient = new DataSpaceClient();
            restDsClient.init(connectionInfo.getUrl(), this.restSchedulerClient);
            this.restDataSpaceClient = restDsClient;

            super.jobTracker.setRestDataSpaceClient(this.restDataSpaceClient);

            this.jobTracker.loadJobs();

            setInitialized(true);

            registerAsListener();
            syncAwaitedJobs();

        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void disconnect() throws PermissionException {
        try {
            _getScheduler().disconnect();
        } catch (Exception e) {

        }
        setInitialized(false);
    }

    @Override
    public String getCurrentPolicy() throws NotConnectedException, PermissionException {
        return _getScheduler().getCurrentPolicy();
    }

    @Override
    public Map getJobsToSchedule() throws NotConnectedException, PermissionException {
        return _getScheduler().getJobsToSchedule();
    }

    @Override
    public List<TaskDescriptor> getTasksToSchedule() throws NotConnectedException, PermissionException {
        return _getScheduler().getTasksToSchedule();
    }

    @Override
    protected ISchedulerClient _getScheduler() {
        checkInitialized();
        return restSchedulerClient;
    }

    @Override
    protected void createFolder(String fUri) throws NotConnectedException, PermissionException {
        RemoteSource remoteSource = new RemoteSource(USER, fUri);
        remoteSource.setType(FileType.FOLDER);
        restDataSpaceClient.create(remoteSource);
    }

    @Override
    protected void removeJobIO(Job job, String pushURL, String pullURL, String newFolderName) {
        pushURL = pushURL + "/" + newFolderName;

        RemoteSource remoteSource = new RemoteSource(IDataSpaceClient.Dataspace.USER, pushURL);

        try {
            restDataSpaceClient.delete(remoteSource);
        } catch (NotConnectedException | PermissionException e) {
            logger.debug("Error in removeJobIO push for job " + job.getName());
        }

        pullURL = pullURL + "/" + newFolderName;
        remoteSource.setPath(pullURL);

        try {
            restDataSpaceClient.delete(remoteSource);
        } catch (NotConnectedException | PermissionException e) {
            logger.debug("Error in removeJobIO pull for job " + job.getName());
        }
    }

    @Override
    public JobId submit(Job job)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        checkInitialized();
        String inputSpace = job.getInputSpace();
        if (inputSpace == null) {
            throw new IllegalArgumentException("'InputSpace' is NULL. The InputSpace must be set in order to transfer inputfiles by the" +
                                               " SmartProxy. As a default, you may use the 'UserSpace' value.");
        }
        String outputSpace = job.getOutputSpace();
        if (outputSpace == null) {
            throw new IllegalArgumentException("'OutputSpace' is NULL. The OutputSpace must be set in order to retrieve outputfiles by" +
                                               " the SmartProxy. As a default, you may use the 'UserSpace' value.");
        }
        return _getScheduler().submit(job);
    }

    @Override
    public JobId submit(File job)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return _getScheduler().submit(job);
    }

    @Override
    public JobId submit(URL job)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return _getScheduler().submit(job);
    }

    @Override
    public JobId submitFromCatalog(String bucketId, String workflowName)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return _getScheduler().submitFromCatalog(bucketId, workflowName);
    }

    @Override
    public JobId submit(URL job, Map<String, String> variables, Map<String, String> headerParams)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return _getScheduler().submit(job, variables, headerParams);
    }

    @Override
    public JobId submit(File job, Map<String, String> variables)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return _getScheduler().submit(job, variables);
    }

    @Override
    public JobId submit(URL job, Map<String, String> variables)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return _getScheduler().submit(job, variables);
    }

    @Override
    public JobId submitFromCatalog(String bucketId, String workflowName, Map<String, String> variables)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return _getScheduler().submitFromCatalog(bucketId, workflowName, variables);
    }

    @Override
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return _getScheduler().getJobState(jobId);
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return _getScheduler().getTaskResult(jobId, taskName);
    }

    @Override
    public boolean finishInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return _getScheduler().finishInErrorTask(jobId, taskName);
    }

    @Override
    public boolean restartInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return _getScheduler().restartInErrorTask(jobId, taskName);
    }

    @Override
    public boolean restartAllInErrorTasks(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return _getScheduler().restartAllInErrorTasks(jobId);
    }

    @Override
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        return _getScheduler().getGlobalSpaceURIs();
    }

    @Override
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        return _getScheduler().getUserSpaceURIs();
    }

    @Override
    public void addEventListener(SchedulerEventListenerExtended listener, boolean myEventsOnly,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        _getScheduler().addEventListener(listener, myEventsOnly, events);
    }

    /**
     * @throws NotConnectedException
     * @throws PermissionException
     * @see AbstractSmartProxy#uploadInputfiles(TaskFlowJob, String)
     */
    @Override
    public boolean uploadInputfiles(TaskFlowJob job, String localInputFolderPath)
            throws NotConnectedException, PermissionException {

        String userSpace = getLocalUserSpace();
        String inputSpace = job.getInputSpace();

        if (!inputSpace.startsWith(userSpace)) {
            // NOTE: only works for USERSPACE urls
            logger.warn("RestSmartProxy does not support data transfers outside USERSPACE.");
            return false;
        }
        String remotePath = inputSpace.substring(userSpace.length() + (userSpace.endsWith("/") ? 0 : 1));
        String jname = job.getName();
        logger.debug("Pushing files for job " + jname + " from " + localInputFolderPath + " to " + remotePath);
        TaskFlowJob tfj = job;
        for (Task t : tfj.getTasks()) {
            logger.debug("Pushing files for task " + t.getName());
            List<String> includes = Lists.newArrayList();
            List<String> excludes = Lists.newArrayList();

            List<InputSelector> inputFilesList = t.getInputFilesList();

            if (inputFilesList != null) {
                for (InputSelector is : inputFilesList) {
                    addfileSelection(is.getInputFiles(), includes, excludes);
                }
            }

            LocalDirSource source = new LocalDirSource(localInputFolderPath);
            source.setIncludes(includes);
            source.setExcludes(excludes);

            RemoteDestination dest = new RemoteDestination(USER, remotePath);
            restDataSpaceClient.upload(source, dest);
        }
        logger.debug("Finished push operation from " + localInputFolderPath + " to " + remotePath);

        return true;
    }

    @Override
    protected void downloadTaskOutputFiles(AwaitedJob awaitedjob, String jobId, String taskName, String localFolder)
            throws NotConnectedException, PermissionException {
        AwaitedTask atask = awaitedjob.getAwaitedTask(taskName);
        if (atask == null) {
            throw new IllegalArgumentException("The task " + taskName + " does not belong to job " + jobId +
                                               " or has already been removed");
        }
        if (atask.isTransferring()) {
            logger.warn("The task " + taskName + " of job " + jobId + " is already transferring its output");
            return;
        }

        String outputSpace = awaitedjob.getOutputSpaceURL();
        String sourceFile;
        try {
            String userSpace = getLocalUserSpace();
            if (!outputSpace.startsWith(userSpace)) {
                logger.warn("RestSmartProxy does not support data transfers outside USERSPACE.");
            }
            sourceFile = outputSpace.substring(userSpace.length() + 1);
        } catch (Throwable error) {
            throw Throwables.propagate(error);
        }
        if (awaitedjob.isIsolateTaskOutputs()) {
            sourceFile = sourceFile.replace(SchedulerConstants.TASKID_DIR_DEFAULT_NAME,
                                            SchedulerConstants.TASKID_DIR_DEFAULT_NAME + "/" + atask.getTaskId());
        }

        List<OutputSelector> outputFileSelectors = atask.getOutputSelectors();
        List<String> includes = Lists.newArrayList();
        List<String> excludes = Lists.newArrayList();
        if (outputFileSelectors != null) {
            for (OutputSelector os : outputFileSelectors) {
                addfileSelection(os.getOutputFiles(), includes, excludes);
            }
        }

        jobTracker.setTaskTransferring(jobId, taskName, true);

        if (awaitedjob.isAutomaticTransfer()) {
            threadPool.submit(new DownloadHandler(jobId, taskName, sourceFile, includes, excludes, localFolder));
        } else {
            try {
                RemoteSource source = new RemoteSource(USER, sourceFile);
                source.setIncludes(includes);
                source.setExcludes(excludes);

                File localDir = new File(localFolder);
                LocalDestination dest = new LocalDestination(localDir);
                restDataSpaceClient.download(source, dest);
            } catch (NotConnectedException | PermissionException e) {
                logger.error(String.format("Cannot download files, jobId=%s, taskId=%s, source=%s, destination=%s",
                                           jobId,
                                           taskName,
                                           sourceFile,
                                           localFolder),
                             e);
                throw e;
            } finally {
                jobTracker.setTaskTransferring(jobId, taskName, false);
            }
            // task is removed from the job tracker only if the transfer is successful
            jobTracker.removeAwaitedTask(jobId, taskName);
        }

    }

    private void addfileSelection(FileSelector fs, List<String> includes, List<String> excludes) {
        includes.addAll(fs.getIncludes());
        excludes.addAll(fs.getExcludes());
    }

    @Override
    public void registerAsListener() throws NotConnectedException, PermissionException {
        _getScheduler().addEventListener(this, true, PROXY_SCHED_EVENTS);
    }

    /*
     * Implementation of the ISchedulerClient interface
     */

    @Override
    public void setSession(String sid) {
        ((ISchedulerClient) _getScheduler()).setSession(sid);
    }

    @Override
    public String getSession() {
        return ((ISchedulerClient) _getScheduler()).getSession();
    }

    @Override
    public boolean isJobFinished(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return ((ISchedulerClient) _getScheduler()).isJobFinished(jobId);
    }

    @Override
    public boolean isJobFinished(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return ((ISchedulerClient) _getScheduler()).isJobFinished(jobId);
    }

    @Override
    public JobResult waitForJob(JobId jobId, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return ((ISchedulerClient) _getScheduler()).waitForJob(jobId, timeout);
    }

    @Override
    public JobResult waitForJob(String jobId, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return ((ISchedulerClient) _getScheduler()).waitForJob(jobId, timeout);
    }

    @Override
    public boolean isTaskFinished(String jobId, String taskName)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException {
        return ((ISchedulerClient) _getScheduler()).isTaskFinished(jobId, taskName);
    }

    @Override
    public TaskResult waitForTask(String jobId, String taskName, long timeout) throws UnknownJobException,
            NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        return ((ISchedulerClient) _getScheduler()).waitForTask(jobId, taskName, timeout);
    }

    @Override
    public List<JobResult> waitForAllJobs(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return ((ISchedulerClient) _getScheduler()).waitForAllJobs(jobIds, timeout);
    }

    @Override
    public Map.Entry<String, JobResult> waitForAnyJob(List<String> jobIds, long timeout)
            throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return ((ISchedulerClient) _getScheduler()).waitForAnyJob(jobIds, timeout);
    }

    @Override
    public Map.Entry<String, TaskResult> waitForAnyTask(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException {
        return ((ISchedulerClient) _getScheduler()).waitForAnyTask(jobId, taskNames, timeout);
    }

    @Override
    public List<Map.Entry<String, TaskResult>> waitForAllTasks(String jobId, List<String> taskNames, long timeout)
            throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException,
            TimeoutException {
        return ((ISchedulerClient) _getScheduler()).waitForAllTasks(jobId, taskNames, timeout);
    }

    @Override
    public boolean pushFile(String spacename, String pathname, String filename, String file)
            throws NotConnectedException, PermissionException {
        return ((ISchedulerClient) _getScheduler()).pushFile(spacename, pathname, filename, file);
    }

    @Override
    public void pullFile(String space, String pathname, String outputFile)
            throws NotConnectedException, PermissionException {
        ((ISchedulerClient) _getScheduler()).pullFile(space, pathname, outputFile);
    }

    @Override
    public boolean deleteFile(String space, String pathname) throws NotConnectedException, PermissionException {
        return ((ISchedulerClient) _getScheduler()).deleteFile(space, pathname);
    }

    private class DownloadHandler implements Runnable {

        private String jobId;

        private String taskName;

        private String sourceFile;

        private List<String> includes;

        private List<String> excludes;

        private String localFolder;

        public DownloadHandler(String jobId, String taskName, String sourceFile, List<String> includes,
                List<String> excludes, String localFolder) {
            this.jobId = jobId;
            this.taskName = taskName;
            this.sourceFile = sourceFile;
            this.includes = includes;
            this.excludes = excludes;
            this.localFolder = localFolder;
        }

        @Override
        public void run() {
            try {
                RemoteSource source = new RemoteSource(USER, sourceFile);
                source.setIncludes(includes);
                source.setExcludes(excludes);

                File localDir = new File(localFolder);
                LocalDestination dest = new LocalDestination(localDir);

                restDataSpaceClient.download(source, dest);
            } catch (Throwable error) {

                logger.error(String.format("Cannot download output files: job_id=%s, task_name=%s, source=%s, destination=%s",
                                           jobId,
                                           taskName,
                                           sourceFile,
                                           localFolder),
                             error);
                logger.warn(String.format("[job_id=%s, task_name=%s] will be removed from the known task list. The system will not attempt again to retrieve data for this task. You could try to manually copy the data from %s in userspace.",
                                          jobId,
                                          taskName,
                                          sourceFile));
                Iterator<SchedulerEventListenerExtended> it = eventListeners.iterator();
                while (it.hasNext()) {
                    SchedulerEventListenerExtended l = it.next();
                    try {
                        l.pullDataFailed(jobId, taskName, sourceFile, error);
                    } catch (Exception e1) {
                        // if an exception occurs we remove the listener
                        it.remove();
                    }
                }
                removeAwaitedTask(jobId, taskName);
                return;
            }

            Iterator<SchedulerEventListenerExtended> it = eventListeners.iterator();
            while (it.hasNext()) {
                SchedulerEventListenerExtended l = it.next();
                try {
                    l.pullDataFinished(jobId, taskName, localFolder);
                } catch (Exception e1) {
                    // if an exception occurs we remove the listener
                    it.remove();
                }
            }
            removeAwaitedTask(jobId, taskName);
        }
    }

    @Override
    public Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, boolean running,
            boolean pending, boolean finished, int offset, int limit)
            throws NotConnectedException, PermissionException {
        return _getScheduler().getTaskIds(taskTag, from, to, mytasks, running, pending, finished, offset, limit);
    }

    @Override
    public Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks, boolean running,
            boolean pending, boolean finished, int offset, int limit, SortSpecifierContainer sortParams)
            throws NotConnectedException, PermissionException {
        return _getScheduler().getTaskStates(taskTag,
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
        return _getScheduler().getJobInfo(jobId);
    }

    @Override
    public boolean changeStartAt(JobId jobId, String startAt)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return ((ISchedulerClient) _getScheduler()).changeStartAt(jobId, startAt);
    }

    @Override
    public JobId copyJobAndResubmitWithGeneralInfo(JobId jobId, Map<String, String> generalInfo)
            throws NotConnectedException, UnknownJobException, PermissionException, SubmissionClosedException,
            JobCreationException {
        return ((ISchedulerClient) _getScheduler()).copyJobAndResubmitWithGeneralInfo(jobId, generalInfo);
    }

    @Override
    public Map<Object, Object> getPortalConfiguration() throws NotConnectedException, PermissionException {
        return _getScheduler().getPortalConfiguration();
    }

    @Override
    public String getCurrentUser() throws NotConnectedException {
        return ((ISchedulerClient) _getScheduler()).getCurrentUser();
    }

    @Override
    public UserData getCurrentUserData() throws NotConnectedException {
        return ((ISchedulerClient) _getScheduler()).getCurrentUserData();
    }

    @Override
    public Map<String, Object> getSchedulerProperties() throws NotConnectedException, PermissionException {
        return ((ISchedulerClient) _getScheduler()).getSchedulerProperties();
    }

}
