package org.ow2.proactive_grid_cloud_portal.ds.client;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.util.dsclient.AwaitedJob;
import org.ow2.proactive.scheduler.common.util.dsclient.AwaitedTask;
import org.ow2.proactive.scheduler.common.util.dsclient.ISchedulerEventListenerExtended;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.ISchedulerClient.DataSpace;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.python.google.common.base.Throwables;
import org.python.google.common.collect.Lists;

public class RestSmartProxyImpl extends AbstractSmartProxy implements SchedulerEventListener {

    private static final Logger logger = Logger.getLogger(RestSmartProxyImpl.class);
    private String url, user, pwd;

    private ISchedulerClient restClientImpl;

    @Override
    public void init(String url, String user, String pwd) throws SchedulerException, LoginException {
        restClientImpl = SchedulerClient.createInstance();
        try {
            this.url = url;
            this.user = user;
            this.pwd = pwd;

            restClientImpl.init(url, user, pwd);
            jobDB.loadJobs();
            registerAsListener();
            syncAwaitedJobs();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void init(String url, Credentials credentials) throws SchedulerException, LoginException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init(String url, CredData credData) throws SchedulerException, LoginException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disconnect() throws PermissionException {
        try {
            restClientImpl.disconnect();
        } catch (NotConnectedException e) {

        }
    }

    @Override
    public void reconnect() throws SchedulerException, LoginException {
        init(url, user, pwd);
    }

    @Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        return restClientImpl.submit(job);
    }

    @Override
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return restClientImpl.getJobState(jobId);
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return restClientImpl.getTaskResult(jobId, taskName);
    }

    @Override
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        return restClientImpl.getUserSpaceURIs();
    }

    @Override
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        restClientImpl.addEventListener(sel, myEventsOnly, events);
    }

    /**
     * @throws NotConnectedException
     * @throws PermissionException
     * @see AbstractSmartProxy#uploadInputfiles(TaskFlowJob, String)
     */
    @Override
    public boolean uploadInputfiles(TaskFlowJob job, String localInputFolderPath)
            throws NotConnectedException, PermissionException {

        String userSpace = getUserSpaceURIs().get(0);
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
            for (InputSelector is : t.getInputFilesList()) {
                addfileSelection(is.getInputFiles(), includes, excludes);
            }
            restClientImpl.upload(new File(localInputFolderPath), includes, excludes, DataSpace.USER,
                    remotePath);
        }
        logger.debug("Finished push operation from " + localInputFolderPath + " to " + remotePath);

        return true;
    }

    @Override
    public void downloadTaskOutputFiles(AwaitedJob awaitedjob, String jobId, String t_name, String localFolder)
            throws FileSystemException {
        AwaitedTask atask = awaitedjob.getAwaitedTask(t_name);
        if (atask == null) {
            throw new IllegalArgumentException("The task " + t_name + " does not belong to job " + jobId
                    + " or has already been removed");
        }
        if (atask.isTransferring()) {
            logger.warn("The task " + t_name + " of job " + jobId + " is already transferring its output");
            return;
        }

        String outputSpace = awaitedjob.getOutputSpaceURL();
        String sourceFile;
        try {
            String userSpace = getUserSpaceURIs().get(0);
            if (!outputSpace.startsWith(userSpace)) {
                logger.warn("RestSmartProxy does not support data transfers outside USERSPACE.");
            }
            sourceFile = outputSpace.substring(userSpace.length() + 1);
        } catch (Throwable error) {
            throw Throwables.propagate(error);
        }
        if (awaitedjob.isIsolateTaskOutputs()) {
            // CHECKME
            sourceFile = sourceFile.replace(SchedulerConstants.TASKID_DIR_DEFAULT_NAME,
                    SchedulerConstants.TASKID_DIR_DEFAULT_NAME + "/" + atask.getTaskId());
        }

        List<OutputSelector> ouputFileSelectors = atask.getOutputSelectors();
        List<String> includes = Lists.newArrayList();
        List<String> excludes = Lists.newArrayList();
        for (OutputSelector os : ouputFileSelectors) {
            addfileSelection(os.getOutputFiles(), includes, excludes);
        }

        if (awaitedjob.isAutomaticTransfer()) {
            tpe.submit(new DownloadHandler(jobId, t_name, sourceFile, includes, excludes, localFolder));
        } else {
            try {
                restClientImpl.download(DataSpace.USER, sourceFile, includes, excludes, localFolder);
            } catch (Throwable error) {
                logger.error(String.format(
                        "Cannot download files, jobId=%s, taskId=%s, source=%s, destination=%s", jobId,
                        t_name, sourceFile, localFolder), error);
            }
            jobDB.removeAwaitedTask(jobId, t_name);
        }
        jobDB.setTaskTransferring(jobId, t_name, true);

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
        restClientImpl.addEventListener(this, true, PROXY_SCHED_EVENTS);
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
                restClientImpl.download(DataSpace.USER, sourceFile, includes, excludes, localFolder);
            } catch (Throwable error) {

                logger.error(String.format(
                        "Cannot download output files: job_id=%s, task_name=%s, source=%s, destination=%s",
                        jobId, taskName, sourceFile, localFolder), error);
                logger.warn(String
                        .format("[job_id=%s, task_name=%s] will be removed from the known task list. The system will not attempt again to retrieve data for this task. You could try to manually copy the data from %s in userspace.",
                                jobId, taskName, sourceFile));
                Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
                while (it.hasNext()) {
                    ISchedulerEventListenerExtended l = it.next();
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

            Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
            while (it.hasNext()) {
                ISchedulerEventListenerExtended l = it.next();
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
