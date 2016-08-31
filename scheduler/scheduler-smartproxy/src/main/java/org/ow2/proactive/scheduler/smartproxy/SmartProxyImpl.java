package org.ow2.proactive.scheduler.smartproxy;

import java.io.Serializable;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.security.auth.login.LoginException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;
import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
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
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.smartproxy.common.AbstractSmartProxy;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedJob;
import org.ow2.proactive.scheduler.smartproxy.common.AwaitedTask;
import org.ow2.proactive.scheduler.smartproxy.common.SchedulerEventListenerExtended;


/**
 * Smart proxy implementation that relies on active objects for communicating
 * with dataspaces and pushing notifications to clients.
 * <p>
 * This implementation assumes that:
 * <p>
 * <ul>
 * <li>the client application needs to submit to the scheduler jobs which
 * require data transfer to the execution nodes</li>
 * <li>the input data is accessible by the client on the local file system (or
 * on location accessible on NFS)</li>
 * <li>the output data is to be copied to the local file system (or on location
 * accessible on NFS)</li>
 * <li>the local file system is not visible from the computation nodes side</li>
 * <li>There is a location (let's call it SHARED_INPUT_LOCATION), for
 * transferring input data, accessible from both sides, client side and
 * computation node side. Same for output data (let's call it
 * SHARED_OUTPUT_LOCATION). These locations could be the same. It might be a
 * shared folder or a data server. Let´s call push_url the url used by the
 * client application in order to push the input data to SHARED_INPUT_LOCATION.
 * Let´s call pull_url the url used by the client application in order to pull
 * the output data from SHARED_OUTPUT_LOCATION. The job needs to specify, as
 * input space, an url pointing to SHARED_INPUT_LOCATION. The job needs to
 * specify, as output space, an url pointing to SHARED_OUTPUT_LOCATION. These
 * urls might or not be the same as push_url and pull_url.</li>
 * </ul>
 * The client application will use this Proxy for communicating with the
 * Scheduler. This Proxy is an ActiveObject.
 * <p>
 * In order to use this object, a reference to it should be obtained via the
 * {@link #getActiveInstance()} method. One of the init methods should be called
 * afterwards.
 * <p>
 * The client could add a Listener to this object in order to receive
 * notifications from the Scheduler. The listener is of type
 * {@link SchedulerEventListenerExtended} which, in addition to the
 * notifications declared by its super type
 * {@link org.ow2.proactive.scheduler.common.SchedulerEventListener}, can be
 * notified with events related to data transfer.
 * <p>
 * Remember this is an active object. The listener object needs to be an active
 * or remote object (in order to avoid passing the listener through deep copy).
 * You could use, for instance:
 * <p>
 * {@code
 * SchedulerEventListenerExtended myListenerRemoteReference = PARemoteObject.turnRemote( new MyEventListener());
 * schedProxy.addEventListener(myListenerRemoteReference);
 * }
 * <p>
 * When a listener is added by the client, no new connection will be established
 * with the scheduler. This Proxy object broadcasts events received from the
 * Scheduler to its own listeners. In addition, it adds events related to data
 * transfer.
 * <p>
 * When a new job is submitted, these operations will be performed:
 * <p>
 * <ul>
 * <li>A temporary folder, for this execution, is created on the
 * SHARED_INPUT_LOCATION and the SHARED_OUTPUT_LOCATION (if
 * SHARED_INPUT_LOCATION!=SHARED_OUTPUT_LOCATION).</li>
 * <li>The job INPUT_SPACE and OUTPUT_SPACE urls are updated with the new
 * created temporary folders.</li>
 * <li>The input data is pushed, via the push_url, from the local file system,
 * to the temporary folder</li>
 * <li>The job is added to a list of awaited jobs, in order to pull the output
 * data once the job is finished. This list is persisted on disk and will be
 * restored after an application restart.</li>
 * <li>When the job is finished, the output data is pulled form the
 * TMP_OUTPUT_LOCATION</li>
 * <li>The client application will be notified, via the listener, about the
 * evolution of the submitted jobs and about data transfer operations.</li>
 * </ul>
 * Each time this object is initialized, it recovers the awaited_jobs list from
 * the persisted file and, for each finished job, it pulls the output data from
 * the TMP_OUTPUT_LOCATION to the local file system
 *
 * @author The ProActive Team
 */
@ActiveObject
public class SmartProxyImpl extends AbstractSmartProxy<JobTrackerImpl>
        implements InitActive, EndActive, Serializable {

    private static final Logger log = Logger.getLogger(SmartProxyImpl.class);

    private transient static SmartProxyImpl activeInstance;

    private transient static SmartProxyImpl instance;

    protected transient Scheduler schedulerProxy;

    private transient Credentials credentials;

    private transient CredData credData;

    private transient Body bodyOnThis;

    private transient SmartProxyImpl stubOnThis;

    /**
     * Thread of this active object's service
     */
    private transient Thread serviceThread;

    /**
     * Empty constructor required by ProActive.
     *
     * @deprecated Use {@link #getActiveInstance()} or {@link #getInstance()}
     */
    public SmartProxyImpl() {
        // Does not set job tracker instance to prevent useless object
        // instantiations for AO proxies. The initialization is done
        // in initActive
    }

    /**
     * Returns a stub to the only active instance of the proxy (proactive
     * singleton pattern)
     *
     * @return instance of the proxy
     * @throws org.objectweb.proactive.ActiveObjectCreationException
     * @throws org.objectweb.proactive.core.node.NodeException
     */
    public static synchronized SmartProxyImpl getActiveInstance()
            throws ActiveObjectCreationException, NodeException {
        if (!((activeInstance != null) && activeInstance.bodyOnThis != null &&
            activeInstance.bodyOnThis.isActive())) {
            instance = getInstance();
            activeInstance = PAActiveObject.turnActive(instance);
        }

        return activeInstance;
    }

    /**
     * Returns the real singleton instance of the proxy
     *
     * @return instance of the proxy
     */
    public static synchronized SmartProxyImpl getInstance() {
        if (instance == null) {
            instance = new SmartProxyImpl();
        }

        return instance;
    }

    @Override
    public void init(ConnectionInfo connectionInfo) throws SchedulerException, LoginException {
        this.connectionInfo = connectionInfo;
        if (connectionInfo.getCredentialFile() != null) {
            try {
                Credentials credentials = Credentials
                        .getCredentials(connectionInfo.getCredentialFile().getAbsolutePath());
                init(connectionInfo.getUrl(), credentials);
            } catch (KeyException e) {
                throw new LoginException(e.getMessage());
            }
        } else {
            CredData cred = new CredData(CredData.parseLogin(connectionInfo.getLogin()),
                CredData.parseDomain(connectionInfo.getLogin()), connectionInfo.getPassword());
            init(connectionInfo.getUrl(), cred);
        }
    }

    public void init(String url, Credentials credentials) throws SchedulerException, LoginException {
        this.init(url, credentials, null);
    }

    public void init(String url, CredData credData) throws SchedulerException, LoginException {
        this.init(url, null, credData);
    }

    private void init(String url, Credentials credentials, CredData credData)
            throws SchedulerException, LoginException {
        if (this.connectionInfo == null) {
            this.connectionInfo = new ConnectionInfo(url, null, null, null, false);
        }
        this.connectionInfo.setUrl(url);
        this.credentials = credentials;
        this.credData = credData;

        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        PublicKey pubKey = auth.getPublicKey();

        if (this.credentials != null) {
            this.credentials = credentials;
            this.credData = null;
        } else if (this.credData != null) {
            this.credData = credData;

            try {
                this.credentials = Credentials.createCredentials(credData, pubKey);
            } catch (KeyException e) {
                throw new InternalSchedulerException(e);
            }
        } else {
            throw new IllegalStateException("No valid credential available to connect to the scheduler");
        }

        this.schedulerProxy = auth.login(this.credentials);

        jobTracker.loadJobs();

        setInitialized(true);

        registerAsListener();
        syncAwaitedJobs();
    }

    @Override
    public void disconnect() throws NotConnectedException, PermissionException {
        checkInitialized();
        try {
            schedulerProxy.disconnect();

        } catch (NotConnectedException e) {
            // we ignore this exception
        } catch (PermissionException e) {
            throw e;
        } catch (Exception e) {
            // we ignore any runtime exception
        }
        setInitialized(false);
    }

    @Override
    protected Scheduler _getScheduler() {
        return schedulerProxy;
    }

    @Override
    public void reconnect() throws SchedulerException, LoginException {
        disconnect();

        if (this.credentials == null) {
            this.init(this.connectionInfo.getUrl(), this.credData);
        } else {
            this.init(this.connectionInfo.getUrl(), this.credentials);
        }
    }

    @Override
    public void registerAsListener() throws NotConnectedException, PermissionException {
        schedulerProxy.addEventListener(stubOnThis, true, PROXY_SCHED_EVENTS);
    }

    @Override
    public boolean uploadInputfiles(TaskFlowJob job, String localInputFolderPath) throws Exception {
        String push_URL = job.getGenericInformation().get(GENERIC_INFO_PUSH_URL_PROPERTY_NAME);

        if ((push_URL == null) || (push_URL.trim().equals(""))) {
            return false;
        } // push inputData

        // TODO - if the copy fails, try to remove the files from the remote
        // folder before throwing an exception
        FileObject remoteFolder = jobTracker.resolveFile(push_URL);
        FileObject localfolder = jobTracker.resolveFile(localInputFolderPath);
        String jname = job.getName();
        log.debug("Pushing files for job " + jname + " from " + localfolder + " to " + remoteFolder);

        TaskFlowJob tfj = job;
        ArrayList<Task> tasks = tfj.getTasks();
        List<DataTransferProcessor> transferCallables = new ArrayList<>(tasks.size());
        for (Task t : tasks) {
            log.debug("Pushing files for task " + t.getName());
            List<InputSelector> inputFileSelectors = t.getInputFilesList();
            // create the selector
            org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector fileSelector = new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector();
            for (InputSelector is : inputFileSelectors) {
                org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector fs = is
                        .getInputFiles();
                if (!fs.getIncludes().isEmpty()) {
                    fileSelector.addIncludes(fs.getIncludes());
                }

                if (!fs.getExcludes().isEmpty()) {
                    fileSelector.addExcludes(fs.getExcludes());
                }
                // We should check if a pattern exist in both includes and
                // excludes. But that would be a user mistake.
            }
            DataTransferProcessor dtp = new DataTransferProcessor(localfolder, remoteFolder, tfj.getName(),
                t.getName(), fileSelector);
            transferCallables.add(dtp);
        }

        List<Future<Boolean>> futures;
        try {
            futures = threadPool.invokeAll(transferCallables);
        } catch (InterruptedException e) {
            log.error("Interrupted while transferring files of job " + jname, e);
            throw new RuntimeException(e);
        }
        for (int i = 0; i < futures.size(); i++) {
            Future<Boolean> answer = futures.get(i);
            String tname = tfj.getTasks().get(i).getName();
            try {
                if (!answer.get()) {
                    // this should not happen
                    throw new RuntimeException(
                        "Files of task " + tname + " for job " + jname + " could not be transferred");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while transferring files of task " + tname + " for job " + jname, e);
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                log.error("Exception occured while transferring files of task " + tname + " for job " + jname,
                        e);
                throw new RuntimeException(e);
            }
        }

        log.debug("Finished push operation from " + localfolder + " to " + remoteFolder);
        return true;
    }

    @Override
    protected void downloadTaskOutputFiles(AwaitedJob awaitedjob, String jobId, String t_name,
            String localFolder) throws Exception {
        AwaitedTask atask = awaitedjob.getAwaitedTask(t_name);
        if (atask == null) {
            throw new IllegalArgumentException(
                "The task " + t_name + " does not belong to job " + jobId + " or has already been removed");
        }
        if (atask.isTransferring()) {
            log.warn("The task " + t_name + " of job " + jobId + " is already transferring its output");
            return;
        }
        String pull_URL = awaitedjob.getPullURL();

        if (awaitedjob.isIsolateTaskOutputs()) {
            pull_URL = pull_URL.replace(SchedulerConstants.TASKID_DIR_DEFAULT_NAME,
                    SchedulerConstants.TASKID_DIR_DEFAULT_NAME + "/" + atask.getTaskId());
        }

        FileObject remotePullFolderFO;
        FileObject localfolderFO;

        try {
            remotePullFolderFO = jobTracker.resolveFile(pull_URL);

            localfolderFO = jobTracker.resolveFile(localFolder);
        } catch (FileSystemException e) {
            log.error("Could not retrieve data for job " + jobId, e);
            throw new IllegalStateException("Could not retrieve data for job " + jobId, e);
        }

        String sourceUrl = remotePullFolderFO.getURL().toString();
        String destUrl = localfolderFO.getURL().toString();

        org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector fileSelector = new org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector();

        List<OutputSelector> ouputFileSelectors = atask.getOutputSelectors();
        for (OutputSelector os : ouputFileSelectors) {
            org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector fs = os.getOutputFiles();
            if (!fs.getIncludes().isEmpty()) {
                fileSelector.addIncludes(fs.getIncludes());
            }

            if (!fs.getExcludes().isEmpty()) {
                fileSelector.addExcludes(fs.getExcludes());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Looking at files in " + sourceUrl + " with " + fileSelector.getIncludes() + "-" +
                fileSelector.getExcludes());
            boolean goon = true;
            int cpt = 0;
            FileObject[] fos = null;
            while (goon) {
                fos = remotePullFolderFO.findFiles(fileSelector);
                goon = cpt < 50 && (fos == null || fos.length == 0);
                cpt++;
                if (goon) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (fos != null && fos.length > 0) {
                for (FileObject fo : fos) {
                    log.debug("Found " + fo.getName());
                }
            } else {
                log.warn("Couldn't find " + fileSelector.getIncludes() + "-" + fileSelector.getExcludes() +
                    " in " + sourceUrl);
            }
        }
        if (awaitedjob.isAutomaticTransfer()) {
            DataTransferProcessor dtp = new DataTransferProcessor(remotePullFolderFO, localfolderFO, jobId,
                t_name, fileSelector);
            jobTracker.setTaskTransferring(jobId, t_name, true);
            threadPool.submit((Runnable) dtp);
        } else {
            log.debug("Copying files from " + sourceUrl + " to " + destUrl);

            try {
                localfolderFO.copyFrom(remotePullFolderFO, fileSelector);
            } catch (FileSystemException e) {
                log.error(e);
                throw e;
            } finally {
                jobTracker.setTaskTransferring(jobId, t_name, false);
                jobTracker.removeAwaitedTask(jobId, t_name);
            }

            log.debug("Finished copying files from " + sourceUrl + " to " + destUrl);
            // ok we can remove the task
        }
    }

    @Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException, SubmissionClosedException,
            JobCreationException {
        if (schedulerProxy == null) {
            throw new NotConnectedException("Not connected to the scheduler.");
        }

        return schedulerProxy.submit(job);
    }

    @Override
    public JobState getJobState(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return schedulerProxy.getJobState(jobId);
    }

    @Override
    public TaskResult getTaskResult(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        if (schedulerProxy == null) {
            throw new NotConnectedException("Not connected to the scheduler.");
        }

        return schedulerProxy.getTaskResult(jobId, taskName);
    }

    public TaskResult getTaskResult(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        if (schedulerProxy == null) {
            throw new NotConnectedException("Not connected to the scheduler.");
        }

        return schedulerProxy.getTaskResult(jobId, taskName);
    }

    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return schedulerProxy.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    public TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return schedulerProxy.getTaskResultFromIncarnation(jobId, taskName, inc);
    }

    @Override
    public boolean restartInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return schedulerProxy.restartInErrorTask(jobId, taskName);
    }

    @Override
    public boolean restartAllInErrorTasks(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return schedulerProxy.restartAllInErrorTasks(jobId);
    }

    @Override
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        return schedulerProxy.getGlobalSpaceURIs();
    }

    @Override
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        return schedulerProxy.getUserSpaceURIs();
    }

    @Override
    public void addEventListener(SchedulerEventListenerExtended listener, boolean myEventsOnly,
            SchedulerEvent[] events) throws NotConnectedException, PermissionException {
        eventListeners.add(listener);
    }

    @Override
    protected void removeJobIO(Job job, String pushURL, String pullURL, String newFolderName) {
        pushURL = pushURL + "/" + newFolderName;
        FileObject fo;

        try {
            fo = jobTracker.resolveFile(pushURL);
            fo.delete(Selectors.SELECT_ALL);
            fo.delete();
        } catch (Exception e) {
            log.error("Error in removeJobIO push for job " + job.getName(), e);
        }

        pullURL = pullURL + "/" + newFolderName;

        try {
            fo = jobTracker.resolveFile(pullURL);
            fo.delete(Selectors.SELECT_ALL);
            fo.delete();
        } catch (Exception e) {
            log.error("Error in removeJobIO pull for job " + job.getName(), e);
        }
    }

    @Override
    protected void createFolder(String fUri) throws NotConnectedException, PermissionException {
        FileObject fo = null;
        try {
            fo = jobTracker.resolveFile(fUri);
            fo.createFolder();
        } catch (FileSystemException e) {
            log.error("Error while creating folder: " + fo, e);
        }

        log.debug("Created remote folder: " + fUri);
    }

    @Override
    public void initActivity(Body body) {
        super.jobTracker = new JobTrackerImpl();

        bodyOnThis = PAActiveObject.getBodyOnThis();
        stubOnThis = (SmartProxyImpl) PAActiveObject.getStubOnThis();
        serviceThread = Thread.currentThread();
    }

    @Override
    public void endActivity(Body body) {
        jobTracker.close();
    }

    /**
     * This method forcefully terminates the activity of the proxy This method
     * should not be called via a proactive stub
     */
    public void terminateFast() {
        // if the service thread is locked on a user-level Thread.sleep():
        serviceThread.interrupt();

        // destroy the request queue
        BlockingRequestQueueImpl rq = (BlockingRequestQueueImpl) bodyOnThis.getRequestQueue();
        rq.destroy();

        // kill the body
        try {
            bodyOnThis.terminate(false);
        } catch (Exception e) {

        }

        while (serviceThread.isAlive()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        jobTracker.close();
    }

    /**
     * Handles the transfer of data asynchronously. The run method is used when
     * pulling data, the call method is used for pushing data.
     */
    private class DataTransferProcessor implements Runnable, Callable<Boolean> {

        private final FileObject source;
        private final FileObject dest;
        private final String jobId;
        private final String taskName;
        private final FileSelector fileSelector;
        private String sourceUrl;
        private String destUrl;

        /**
         * @param source
         *            source folder
         * @param dest
         *            dest folder
         * @param _jobId
         *            - only used for pull operations. For push operations, the
         *            jobId is null
         */
        public DataTransferProcessor(FileObject source, FileObject dest, String _jobId, String tname,
                FileSelector fileSelector) {
            this.source = source;
            this.dest = dest;
            this.jobId = _jobId;
            this.fileSelector = fileSelector;
            this.taskName = tname;
        }

        protected void transfer() throws Exception {
            sourceUrl = source.getURL().toString();
            destUrl = dest.getURL().toString();
            log.debug("Copying files of task " + taskName + " of job " + jobId + " from " + source + " to " +
                dest);
            if (log.isDebugEnabled()) {

                FileObject[] fos = source.findFiles(fileSelector);
                for (FileObject fo : fos) {
                    log.debug("Found " + fo.getName());
                }

            }
            dest.copyFrom(source, fileSelector);
            log.debug("Finished copying files of task " + taskName + " of job " + jobId + " from " + source +
                " to " + dest);
        }

        @Override
        public void run() {
            try {
                transfer();
            } catch (Exception e) {
                log.error("An error occurred while copying files of task " + taskName + " of job " + jobId +
                    " from " + source + " to " + dest, e);

                log.warn("Task " + taskName + " of job " + jobId +
                    " will be removed from the known task list. The system will not attempt again to retrieve data for this task. You could try to manually copy the data from the location  " +
                    sourceUrl);

                Iterator<SchedulerEventListenerExtended> it = eventListeners.iterator();
                while (it.hasNext()) {
                    SchedulerEventListenerExtended l = it.next();
                    try {
                        l.pullDataFailed(jobId, taskName, sourceUrl, e);
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
                    l.pullDataFinished(jobId, taskName, destUrl);
                } catch (Exception e1) {
                    // if an exception occurs we remove the listener
                    it.remove();
                }
            }
            removeAwaitedTask(jobId, taskName);
        }

        @Override
        public Boolean call() throws Exception {
            try {
                transfer();
            } catch (Exception e) {
                log.error("An error occured while copying files of task " + taskName + " of job " + jobId +
                    " from " + source + " to " + dest, e);
                throw e;
            }

            return true;
        }
    }

    @Override
    public Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, boolean running,
            boolean pending, boolean finished, int offset, int limit)
            throws NotConnectedException, PermissionException {
        return schedulerProxy.getTaskIds(taskTag, from, to, mytasks, running, pending, finished, offset,
                limit);
    }

    @Override
    public Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks, boolean running,
            boolean pending, boolean finished, int offset, int limit, SortSpecifierContainer sortParams)
            throws NotConnectedException, PermissionException {
        return schedulerProxy.getTaskStates(taskTag, from, to, mytasks, running, pending, finished, offset,
                limit, sortParams);
    }

    @Override
    public JobInfo getJobInfo(String jobId)
            throws UnknownJobException, NotConnectedException, PermissionException {
        return schedulerProxy.getJobInfo(jobId);
    }

    @Override
    public boolean changeStartAt(JobId jobId, String startAt)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return schedulerProxy.changeStartAt(jobId, startAt);
    }

    @Override
    public JobId copyJobAndResubmitWithGeneralInfo(JobId jobId, Map<String, String> generalInfo)
            throws NotConnectedException, UnknownJobException, PermissionException, SubmissionClosedException,
            JobCreationException {
        return schedulerProxy.copyJobAndResubmitWithGeneralInfo(jobId, generalInfo);
    }

}
