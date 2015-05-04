/*
 *  *
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.smartproxy;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.*;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive.scheduler.rest.ds.*;
import org.ow2.proactive.scheduler.smartproxy.common.AbstractSmartProxy;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedJob;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedTask;
import org.ow2.proactive.scheduler.smartproxy.common.SchedulerEventListenerExtended;
import org.ow2.proactive_grid_cloud_portal.common.FileType;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient.Dataspace.USER;

/**
 * Smart proxy implementation that relies on the REST API for communicating with dataspaces
 * and websockets to push notifications to clients.
 * <p>
 * Any instance of this class must be initialized by calling the {@link #init(String, String, String)}
 * or {@link #initInsecure(String, String, String)} method before executing any subsequent method. Not
 * respecting this rule may lead to an NPE or an unexpected behaviour.
 *
 * @author The ProActive Team
 */
public class RestSmartProxyImpl extends AbstractSmartProxy<RestJobTrackerImpl> implements ISchedulerClient, SchedulerEventListener {

    private static final Logger logger = Logger.getLogger(RestSmartProxyImpl.class);

    private ISchedulerClient restSchedulerClient;

    private IDataSpaceClient restDataSpaceClient;

    public RestSmartProxyImpl() {
        super(new RestJobTrackerImpl());
    }

    @Override
    public void init(String url, String user, String pwd) throws SchedulerException, LoginException {
       init(url, user, pwd, false);
    }

    private void init(String url, String user, String pwd, boolean insecureConnection) {
        try {
            this.schedulerUrl = url;
            this.schedulerLogin = user;
            this.schedulerPassword = pwd;
            this.restSchedulerClient = SchedulerClient.createInstance();

            if (insecureConnection) {
                this.restSchedulerClient.initInsecure(url, user, pwd);
            } else {
                this.restSchedulerClient.init(url, user, pwd);
            }

            DataSpaceClient restDsClient = new DataSpaceClient();
            restDsClient.init(url, this.restSchedulerClient);
            this.restDataSpaceClient = restDsClient;

            super.jobTracker.setRestDataSpaceClient(this.restDataSpaceClient);

            this.jobTracker.loadJobs();
            registerAsListener();
            syncAwaitedJobs();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void disconnect() throws PermissionException {
        try {
            restSchedulerClient.disconnect();
        } catch (NotConnectedException e) {

        }
    }

    @Override
    protected Scheduler _getScheduler() {
        return restSchedulerClient;
    }

    @Override
    public void reconnect() throws SchedulerException, LoginException {
        init(schedulerUrl, schedulerLogin, schedulerPassword);
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

        RemoteSource remoteSource
                = new RemoteSource(IDataSpaceClient.Dataspace.USER, pushURL);

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
    public JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        String inputSpace = job.getInputSpace();
        if (inputSpace == null) {
            throw new IllegalArgumentException(
                    "'InputSpace' is NULL. The InputSpace must be set in order to transfer inputfiles by the"
                            + " SmartProxy. As a default, you may use the 'UserSpace' value.");
        }
        String outputSpace = job.getOutputSpace();
        if (outputSpace == null) {
            throw new IllegalArgumentException(
                    "'OutputSpace' is NULL. The OutputSpace must be set in order to retrieve outputfiles by"
                            + " the SmartProxy. As a default, you may use the 'UserSpace' value.");
        }
        return restSchedulerClient.submit(job);
    }

    @Override
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return restSchedulerClient.getJobState(jobId);
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return restSchedulerClient.getTaskResult(jobId, taskName);
    }

    @Override
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        return restSchedulerClient.getGlobalSpaceURIs();
    }

    @Override
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        return restSchedulerClient.getUserSpaceURIs();
    }

    @Override
    public void addEventListener(SchedulerEventListenerExtended listener, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        restSchedulerClient.addEventListener(listener, myEventsOnly, events);
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
        logger
                .debug("Pushing files for job " + jname + " from " + localInputFolderPath + " to " +
                        remotePath);
        TaskFlowJob tfj = job;
        for (Task t : tfj.getTasks()) {
            logger.debug("Pushing files for task " + t.getName());
            List<String> includes = Lists.newArrayList();
            List<String> excludes = Lists.newArrayList();
            for (InputSelector is : t.getInputFilesList()) {
                addfileSelection(is.getInputFiles(), includes, excludes);
            }
            LocalDirSource source = new LocalDirSource(localInputFolderPath);
            if (!includes.isEmpty()) {
                source.setIncludes(includes);
            }
            if (!excludes.isEmpty()) {
                source.setExcludes(excludes);
            }
            RemoteDestination dest = new RemoteDestination(USER, remotePath);
            restDataSpaceClient.upload(source, dest);
        }
        logger.debug("Finished push operation from " + localInputFolderPath + " to " + remotePath);

        return true;
    }

    @Override
    protected void downloadTaskOutputFiles(AwaitedJob awaitedjob, String jobId, String taskName, String localFolder) throws NotConnectedException, PermissionException {
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
                if (!includes.isEmpty()) {
                    source.setIncludes(includes);
                }
                if (!excludes.isEmpty()) {
                    source.setExcludes(excludes);
                }
                File localDir = new File(localFolder);
                LocalDestination dest = new LocalDestination(localDir);
                restDataSpaceClient.download(source, dest);
            } catch (NotConnectedException | PermissionException e) {
                logger.error(String.format(
                        "Cannot download files, jobId=%s, taskId=%s, source=%s, destination=%s", jobId,
                        taskName, sourceFile, localFolder), e);
                throw e;
            } finally {
                jobTracker.setTaskTransferring(jobId, taskName, false);
                jobTracker.removeAwaitedTask(jobId, taskName);
            }
        }

    }

    private void addfileSelection(FileSelector fs, List<String> includes, List<String> excludes) {
        if (fs.getIncludes() != null && fs.getIncludes().length > 0) {
            includes.addAll(Arrays.asList(fs.getIncludes()));
        }
        if (fs.getExcludes() != null && fs.getExcludes().length > 0) {
            excludes.addAll(Arrays.asList(fs.getExcludes()));
        }

    }

    @Override
    public void registerAsListener() throws NotConnectedException, PermissionException {
        restSchedulerClient.addEventListener(this, true, PROXY_SCHED_EVENTS);
    }

    /*
     * Implementation of the ISchedulerClient interface
     */

    @Override
    public void initInsecure(String url, String user, String pwd) throws Exception {
        init(url, user, pwd, true);
    }

    @Override
    public void setSession(String sid) {
        restSchedulerClient.setSession(sid);
    }

    @Override
    public String getSession() {
        return restSchedulerClient.getSession();
    }

    @Override
    public boolean isJobFinished(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return restSchedulerClient.isJobFinished(jobId);
    }

    @Override
    public boolean isJobFinished(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return restSchedulerClient.isJobFinished(jobId);
    }

    @Override
    public JobResult waitForJob(JobId jobId, long timeout) throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return restSchedulerClient.waitForJob(jobId, timeout);
    }

    @Override
    public JobResult waitForJob(String jobId, long timeout) throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return restSchedulerClient.waitForJob(jobId, timeout);
    }

    @Override
    public boolean isTaskFinished(String jobId, String taskName) throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException {
        return restSchedulerClient.isTaskFinished(jobId, taskName);
    }

    @Override
    public TaskResult waitForTask(String jobId, String taskName, long timeout) throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        return restSchedulerClient.waitForTask(jobId, taskName, timeout);
    }

    @Override
    public List<JobResult> waitForAllJobs(List<String> jobIds, long timeout) throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return restSchedulerClient.waitForAllJobs(jobIds, timeout);
    }

    @Override
    public Map.Entry<String, JobResult> waitForAnyJob(List<String> jobIds, long timeout) throws NotConnectedException, UnknownJobException, PermissionException, TimeoutException {
        return restSchedulerClient.waitForAnyJob(jobIds, timeout);
    }

    @Override
    public Map.Entry<String, TaskResult> waitForAnyTask(String jobId, List<String> taskNames, long timeout) throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        return restSchedulerClient.waitForAnyTask(jobId, taskNames, timeout);
    }

    @Override
    public List<Map.Entry<String, TaskResult>> waitForAllTasks(String jobId, List<String> taskNames, long timeout) throws UnknownJobException, NotConnectedException, PermissionException, UnknownTaskException, TimeoutException {
        return restSchedulerClient.waitForAllTasks(jobId, taskNames, timeout);
    }

    @Override
    public boolean pushFile(String spacename, String pathname, String filename, String file) throws NotConnectedException, PermissionException {
        return restSchedulerClient.pushFile(spacename, pathname, filename, file);
    }

    @Override
    public void pullFile(String space, String pathname, String outputFile) throws NotConnectedException, PermissionException {
        restSchedulerClient.pullFile(space, pathname, outputFile);
    }

    @Override
    public boolean deleteFile(String space, String pathname) throws NotConnectedException, PermissionException {
        return restSchedulerClient.deleteFile(space, pathname);
    }

    @Override
    public JobId submitAsJobArchive(Job job) throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        return restSchedulerClient.submitAsJobArchive(job);
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
                if (!includes.isEmpty()) {
                    source.setIncludes(includes);
                }
                if (!excludes.isEmpty()) {
                    source.setExcludes(excludes);
                }
                File localDir = new File(localFolder);
                LocalDestination dest = new LocalDestination(localDir);

                restDataSpaceClient.download(source, dest);
            } catch (Throwable error) {

                logger.error(String.format(
                        "Cannot download output files: job_id=%s, task_name=%s, source=%s, destination=%s",
                        jobId, taskName, sourceFile, localFolder), error);
                logger
                        .warn(String
                                .format(
                                        "[job_id=%s, task_name=%s] will be removed from the known task list. The system will not attempt again to retrieve data for this task. You could try to manually copy the data from %s in userspace.",
                                        jobId, taskName, sourceFile));
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

}
