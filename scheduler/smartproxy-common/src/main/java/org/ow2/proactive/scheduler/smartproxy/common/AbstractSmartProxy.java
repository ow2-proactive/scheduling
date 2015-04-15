package org.ow2.proactive.scheduler.smartproxy.common;

import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.*;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.*;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.base.Throwables.propagateIfInstanceOf;
import static org.ow2.proactive.scheduler.common.SchedulerEvent.*;

/**
 *  Asbtract implementation of smart proxy that factorizes code used by all smart proxy implementations.
 *
 *  @author The ProActive Team
 */
public abstract class AbstractSmartProxy {

    private static final Logger logger = Logger.getLogger(AbstractSmartProxy.class);

    public static final String GENERIC_INFO_INPUT_FOLDER_PROPERTY_NAME = "client_input_data_folder";
    public static final String GENERIC_INFO_OUTPUT_FOLDER_PROPERTY_NAME = "client_output_data_folder";

    public static final String GENERIC_INFO_PUSH_URL_PROPERTY_NAME = "push_url";
    public static final String GENERIC_INFO_PULL_URL_PROPERTY_NAME = "pull_url";

    public static final int MAX_NB_OF_DATA_TRANSFER_THREADS = 3 * Runtime.getRuntime().availableProcessors();

    /**
     * Thread factory for data transfer operations
     */
    private ThreadFactory threadFactory =
            new NamedThreadFactory("Data Transfer Thread");

    protected final ExecutorService threadPool =
            Executors.newFixedThreadPool(MAX_NB_OF_DATA_TRANSFER_THREADS, threadFactory);

    protected static final SchedulerEvent[] PROXY_SCHED_EVENTS = new SchedulerEvent[]{
            JOB_RUNNING_TO_FINISHED, JOB_PENDING_TO_RUNNING, JOB_PENDING_TO_FINISHED, JOB_PAUSED,
            JOB_RESUMED, TASK_PENDING_TO_RUNNING, KILLED, SHUTDOWN, SHUTTING_DOWN, STOPPED, RESUMED,
            TASK_RUNNING_TO_FINISHED};

    protected JobTracker jobTracker;

    /**
     * last url used to connect to the scheduler
     */
    protected String schedulerUrl = null;

    protected String schedulerLogin = null;

    protected String schedulerPassword = null;

    /**
     * listeners registered to this proxy
     */
    protected Set<SchedulerEventListenerExtended> eventListeners =
            Collections.synchronizedSet(new HashSet<SchedulerEventListenerExtended>());

    public void cleanDatabase() {
        jobTracker.cleanDataBase();
    }

    public void terminate() {
        jobTracker.close();
    }

    public void setSessionName(String name) {
        jobTracker.setSessionName(name);
    }

    protected void reinitializeState(String schedulerUrl)
            throws NotConnectedException, PermissionException {
        this.schedulerUrl = schedulerUrl;
        jobTracker.loadJobs();
        registerAsListener();
        syncAwaitedJobs();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ow2.proactive.scheduler.common.util.dsclient.ISmartProxy#reconnect()
     */
    public void reconnect() throws SchedulerException, LoginException {
        if (this.schedulerUrl == null) {
            throw new IllegalStateException("No connection to the scheduler has been established yet.");
        }
        disconnect();
        init(schedulerUrl, schedulerLogin, schedulerPassword);
        registerAsListener();
        jobTracker.loadJobs();
        syncAwaitedJobs();
    }

    /**
     * This method will synchronize this proxy with a remote Scheduler, it will
     * contact the scheduler and checks the current state of every job being
     * handled. It is called either during the proxy initialization, or after a
     * manual reconnection.
     */
    protected void syncAwaitedJobs() {
        // we make a copy of the awaitedJobsIds set in order to iterate over it.
        Set<String> awaitedJobsIds = jobTracker.getAwaitedJobsIds();
        for (String id : awaitedJobsIds) {
            syncAwaitedJob(id);
        }
    }

    /**
     * This method will synchronize this proxy with a remote Scheduler for the
     * given job
     *
     * @param id jobId
     */
    private void syncAwaitedJob(String id) {
        AwaitedJob awaitedJob = jobTracker.getAwaitedJob(id);

        try {
            JobState js = getJobState(id);
            for (TaskState ts : js.getTasks()) {
                String tname = ts.getName();
                AwaitedTask at = awaitedJob.getAwaitedTask(tname);
                if ((at != null) && (!at.isTransferring())) {
                    TaskResult tres = null;
                    try {
                        tres = getTaskResult(id, tname);
                        if (tres != null) {
                            logger.debug("Synchonizing task " + tname + " of job " + id);
                            taskStateUpdatedEvent(new NotificationData<TaskInfo>(
                                    SchedulerEvent.TASK_RUNNING_TO_FINISHED, ts.getTaskInfo()));
                        }
                    } catch (NotConnectedException e) {
                        e.printStackTrace();
                    } catch (UnknownJobException e) {
                        logger.error("Could not retrieve output data for job " + id +
                                " because this job is not known by the Scheduler. \n ", e);
                    } catch (UnknownTaskException e) {
                        logger.error("Could not retrieve output data for task " + tname + " of job " + id +
                                " because this task is not known by the Scheduler. \n ", e);
                    } catch (Exception e) {
                        // logger.error("Unexpected error while getting the output data for task "
                        // + tname
                        // + " of job " + id, e);
                    }
                }
            }
            if (js.isFinished()) {
                jobStateUpdatedEvent(new NotificationData<JobInfo>(SchedulerEvent.JOB_RUNNING_TO_FINISHED, js
                        .getJobInfo()));
            }

        } catch (NotConnectedException e) {
            logger
                    .error(
                            "A connection error occured while trying to download output data of Job " +
                                    id +
                                    ". This job will remain in the list of awaited jobs. Another attempt to dowload the output data will be made next time the application is initialized. ",
                            e);
        } catch (UnknownJobException e) {
            logger.error("Could not retrieve output data for job " + id +
                    " because this job is not known by the Scheduler. \n ", e);
            logger
                    .warn("Job  " +
                            id +
                            " will be removed from the known job list. The system will not attempt again to retrieve data for this job. You could try to manually copy the data from the location  " +
                            awaitedJob.getPullURL());
            jobTracker.removeAwaitedJob(id);
        } catch (PermissionException e) {
            logger
                    .error(
                            "Could not retrieve output data for job " +
                                    id +
                                    " because you don't have permmission to access this job. You need to use the same connection credentials you used for submitting the job.  \n Another attempt to dowload the output data for this job will be made next time the application is initialized. ",
                            e);
        }

    }

    public JobId submit(TaskFlowJob job, String localInputFolderPath, String localOutputFolderPath,
                        boolean isolateTaskOutputs, boolean automaticTransfer) throws NotConnectedException,
            PermissionException, SubmissionClosedException, JobCreationException {
        return submit(job, localInputFolderPath, null, localOutputFolderPath, null, isolateTaskOutputs,
                automaticTransfer);
    }

    public JobId submit(TaskFlowJob job, String localInputFolderPath, String pushUrl,
                        String localOutputFolderPath, String pullUrl, boolean isolateTaskOutputs,
                        boolean automaticTransfer) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        if (isNullOrEmpty(pushUrl)) {
            pushUrl = getUserSpaceURIs().get(0);
        }
        if (isNullOrEmpty(pullUrl)) {
            pullUrl = getUserSpaceURIs().get(0);
        }
        String newFolderName = createNewFolderName();
        String pushUrlUpdate = prepareJobInput(job, localInputFolderPath, pushUrl, newFolderName);
        String pullUrlUpdate = prepareJobOutput(job, localOutputFolderPath, pullUrl, newFolderName,
                isolateTaskOutputs);
        uploadInputfiles(job, localInputFolderPath);
        JobId id = null;
        try {
            id = submit(job);
        } catch (Exception e) {
            removeJobIO(job, pushUrl, pullUrl, newFolderName);
            propagateIfInstanceOf(e, NotConnectedException.class);
            propagateIfInstanceOf(e, PermissionException.class);
            propagateIfInstanceOf(e, SubmissionClosedException.class);
            propagateIfInstanceOf(e, JobCreationException.class);
            propagateIfInstanceOf(e, RuntimeException.class);
            propagate(e);
        }

        HashMap<String, AwaitedTask> awaitedTaskMap = new HashMap<String, AwaitedTask>();
        for (Task t : job.getTasks()) {
            awaitedTaskMap.put(t.getName(), new AwaitedTask(t.getName(), t.getOutputFilesList()));
        }

        AwaitedJob awaitedJob = new AwaitedJob(id.toString(), localInputFolderPath, job.getInputSpace(),
                pushUrlUpdate, localOutputFolderPath, job.getOutputSpace(), pullUrlUpdate, isolateTaskOutputs,
                automaticTransfer, awaitedTaskMap);
        jobTracker.putAwaitedJob(id.toString(), awaitedJob);

        return id;
    }

    /**
     * This method will create a remote folder for output of this job and update
     * the outputSpace job property. If the localOutputFolder parameter is null,
     * or pull_url no action will be performed concerning this job's output.
     * <p>
     * <p>
     * We suppose there is file storage accessible by the client application as
     * well as the tasks on the computation nodes.
     * <p>
     * This storage could be different for input and for output.
     * <p>
     * <p>
     * This output storage can be accessed, by the client application, using the
     * pull_url and by the tasks on the nodes using the job's output space url.
     * <p>
     * <p>
     * Prepare Output Data Transfer
     * <p>
     * A folder will be created at pull_url/NewFolder/output (which, from the
     * nodes side, is job.OutputSpace/NewFolder/output).
     * <p>
     * The OutputSpace property of the job will be changed to the new location.
     * job.OutputSpace = job.OutputSpace/NewFolder/output
     * <p>
     * A generic information will be attached to the job containing the local
     * output folder path If the option isolateTaskOutputs is set, a subfolder
     * of "output" named "[TASKID]" will be created, it will behave as a tag to
     * tell the TaskLauncher to create a subfolder with the real taskid when the
     * task is executed.
     *
     * @param job
     * @param localOutputFolder  path to the output folder on local machine if null, no actions
     *                           will be performed concerning the output data for this job
     * @param pull_url           - the url where the data is to be retrieved after the job is
     *                           finished
     * @param newFolderName      name of the folder to be used for pushing the output
     * @param isolateTaskOutputs task output isolation (see method submit)
     * @return a String representing the updated value of the pull_url
     */
    protected String prepareJobOutput(TaskFlowJob job, String localOutputFolder, String pull_url,
                                      String newFolderName, boolean isolateTaskOutputs)
            throws NotConnectedException, PermissionException {
        // if the job defines an output space
        // and the localOutputFolder is not null
        // create a remote folder for the output data
        // and update the OutputSpace property of the job to reference that
        // folder
        String outputSpace_url = job.getOutputSpace();
        String pull_url_updated = "";

        // the output folder, on the remote output space, relative to the root
        // url
        String outputFolder = "";

        if ((localOutputFolder != null) && (outputSpace_url != null) && (!outputSpace_url.equals("")) &&
                (pull_url != null)) {
            if (isolateTaskOutputs) {
                // at the end we add the [TASKID] pattern without creating the
                // folder
                outputFolder = newFolderName + "/output/" + SchedulerConstants.TASKID_DIR_DEFAULT_NAME;
            } else {
                outputFolder = newFolderName + "/output";
            }

            pull_url_updated = pull_url + "/" + outputFolder;
            String outputSpace_url_updated = outputSpace_url + "/" + outputFolder;
            logger.debug("Output space of job " + job.getName() + " will be " + outputSpace_url_updated);

            createFolder(pull_url_updated);

            job.setOutputSpace(outputSpace_url_updated);
            job.addGenericInformation(GENERIC_INFO_OUTPUT_FOLDER_PROPERTY_NAME, new File(localOutputFolder)
                    .getAbsolutePath());
            job.addGenericInformation(GENERIC_INFO_PULL_URL_PROPERTY_NAME, pull_url_updated);
        }

        return pull_url_updated;
    }

    /**
     * This method will create a remote folder for the input data of this job
     * and update the inputSpace job property. If the localInputFolder parameter
     * is null, or push_url is null, no action will be performed concerning this
     * job's input.
     * <p>
     * <p>
     * We suppose there is file storage accessible by the client application as
     * well as the tasks on the computation nodes.
     * <p>
     * This storage could be different for input and for output.
     * <p>
     * The input storage can be accessed, by the client application, using the
     * push_url and by the tasks on the nodes using the job's input space url.
     * <p>
     * <p>
     * Prepare Input Data Transfer
     * <p>
     * A folder will be created at push_url/newFolderName/input (which, from the
     * nodes side, is the job.InputSpace/newFolderName/input) . The InputSpace
     * property of the job will be changed to the new location. job.InputSpace =
     * job.InputSpace/NewFolder/input
     * <p>
     * <p>
     * A generic information will be attached to the job containing the local
     * input folder path.
     *
     * @param job
     * @param localInputFolder path to the input folder on local machine if null, no actions
     *                         will be performed concerning the input data for this job
     * @param pushURL         the url where input data is to be pushed before the job
     *                         submission
     * @param newFolderName    name of the new folder to be created
     * @return String representing the updated value of the push_url
     */
    protected String prepareJobInput(Job job, String localInputFolder, String pushURL, String newFolderName)
            throws NotConnectedException, PermissionException {
        // if the job defines an input space
        // and the localInputFolder is not null
        // create a remote folder for the input data
        // and update the InputSpace property of the job to reference that
        // folder

        String inputSpace_url = job.getInputSpace();
        String push_url_updated = "";

        // the input folder, on the remote input space, relative to the root url
        String inputFolder = "";
        if ((localInputFolder != null) && (inputSpace_url != null) && (!inputSpace_url.equals("")) &&
                (pushURL != null)) {
            inputFolder = newFolderName + "/input";
            push_url_updated = pushURL + "/" + inputFolder;
            String inputSpace_url_updated = inputSpace_url + "/" + inputFolder;
            createFolder(push_url_updated);
            job.setInputSpace(inputSpace_url_updated);
            job.addGenericInformation(GENERIC_INFO_INPUT_FOLDER_PROPERTY_NAME, new File(localInputFolder)
                    .getAbsolutePath());

            job.addGenericInformation(GENERIC_INFO_PUSH_URL_PROPERTY_NAME, push_url_updated);
        }

        // if the job defines an output space
        // and the localOutputFolder is not null
        // create a remote folder for the output data
        // and update the OutputSpace property of the job to reference that
        // folder

        return push_url_updated;
    }

    protected String createNewFolderName() {
        String user = System.getProperty("user.name");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
        Date now = new Date();
        String strDate = sdf.format(now);

        String newFolderName = user + "_" + strDate;
        return newFolderName;
    }

    // ******** Pushing and Pulling Data *********************** //

    public void pullData(String jobId, String t_name, String localFolder) {

        AwaitedJob awaitedjob = jobTracker.getAwaitedJob(jobId);
        if (awaitedjob == null) {
            throw new IllegalArgumentException("The job " + jobId + " is unknown or has been removed");
        }
        if (awaitedjob.isAutomaticTransfer()) {
            throw new UnsupportedOperationException("Transfer of input files with job " + jobId +
                    " is handled automatically.");
        }

        String localOutFolderPath = null;
        if (localFolder == null) {
            localOutFolderPath = awaitedjob.getLocalOutputFolder();
        } else {
            localOutFolderPath = localFolder;
        }
        if (localOutFolderPath == null) {
            throw new IllegalArgumentException("The job " + awaitedjob.getJobId() +
                    " does not define an output folder on local machine, please provide an outputFolder.");
        }
        downloadTaskOutputFiles(awaitedjob, jobId, t_name, localOutFolderPath);
    }

    // ******** Scheduler Event Listener *********************** //

    public void addEventListener(SchedulerEventListenerExtended sel) throws NotConnectedException,
            PermissionException {
        eventListeners.add(sel);
    }

    public void removeEventListener(SchedulerEventListenerExtended sel) {
        eventListeners.remove(sel);
    }

    // *** Forward all events from the Scheduler to the local listeners
    // *************** //

    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        Iterator<SchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            SchedulerEventListenerExtended l = it.next();
            try {
                l.schedulerStateUpdatedEvent(eventType);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }
    }

    public void jobSubmittedEvent(JobState job) {
        Iterator<SchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            SchedulerEventListenerExtended l = it.next();
            try {
                l.jobSubmittedEvent(job);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }
    }

    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        updateJob(notification);
        Iterator<SchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            SchedulerEventListenerExtended l = it.next();
            try {
                l.jobStateUpdatedEvent(notification);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        updateTask(notification);
        Iterator<SchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            SchedulerEventListenerExtended l = it.next();
            try {
                l.taskStateUpdatedEvent(notification);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }

    }

    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        Iterator<SchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            SchedulerEventListenerExtended l = it.next();
            try {
                l.usersUpdatedEvent(notification);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }
    }

    // ********* Awaited Jobs methods ******************************* //

    /**
     * @return a new HashSet with the awaited jobs. Modifying the result of this
     *         method will not affect the source HashSet (the awaited jobs)
     */

    /**
     * Check if the job concerned by this notification is awaited. Retrieve
     * corresponding data if needed
     *
     * @param notification
     */
    protected void updateJob(NotificationData<?> notification) {

        // am I interested in this job?
        JobId id = ((NotificationData<JobInfo>) notification).getData().getJobId();

        AwaitedJob aj = jobTracker.getAwaitedJob(id.toString());

        if (aj == null)
            return;

        JobStatus status = ((NotificationData<JobInfo>) notification).getData().getStatus();
        switch (status) {
            case KILLED: {
                logger.debug("The job " + id + "has been killed.");
                jobTracker.removeAwaitedJob(id.toString());
                break;
            }
            case FINISHED: {
                logger.debug("The job " + id + " is finished.");
                // removeAwaitedJob(id.toString());
                break;
            }
            case CANCELED: {
                logger.debug("The job " + id + " is canceled.");
                jobTracker.removeAwaitedJob(id.toString());
                break;
            }
            case FAILED: {
                logger.debug("The job " + id + " is failed.");
                // removeAwaitedJob(id.toString());
                break;
            }
        }
    }

    /**
     * Check if the task concerned by this notification is awaited. Retrieve
     * corresponding data if needed
     *
     * @param notification
     */
    protected void updateTask(NotificationData<TaskInfo> notification) {

        // am I interested in this task?
        TaskInfo taskInfoData = ((NotificationData<TaskInfo>) notification).getData();
        JobId id = taskInfoData.getJobId();
        TaskId tid = taskInfoData.getTaskId();
        String tname = tid.getReadableName();
        TaskStatus status = taskInfoData.getStatus();

        AwaitedJob aj = jobTracker.getAwaitedJob(id.toString());

        if (aj == null)
            return;

        AwaitedTask at = aj.getAwaitedTask(tname);

        if (at == null)
            return;

        at.setTaskId(tid.toString());
        jobTracker.putAwaitedJob(id.toString(), aj);

        switch (status) {
            case ABORTED:
            case NOT_RESTARTED:
            case NOT_STARTED:
            case SKIPPED: {
                logger.debug("The task " + tname + " from job " + id +
                        " couldn't start. No data will be transfered");
                jobTracker.removeAwaitedTask(id.toString(), tname);
                break;
            }
            case FINISHED: {
                logger.debug("The task " + tname + " from job " + id + " is finished.");
                if (aj.isAutomaticTransfer()) {
                    logger.debug("Transferring data for finished task " + tname + " from job " + id);
                    try {
                        downloadTaskOutputFiles(aj, id.toString(), tname, aj.getLocalOutputFolder());
                    } catch (Throwable t) {
                        logger.error("Error while handling data for finished task " + tname + " for job " +
                                id + ", task will be removed");
                        jobTracker.removeAwaitedTask(id.toString(), tname);
                    }
                }
                break;
            }
            case FAULTY: {
                logger.debug("The task " + tname + " from job " + id + " is faulty.");
                if (aj.isAutomaticTransfer()) {
                    logger.debug("Transfering data for failed task " + tname + " from job " + id);
                    try {
                        downloadTaskOutputFiles(aj, id.toString(), tname, aj.getLocalOutputFolder());
                    } catch (Throwable t) {
                        logger.error("Error while handling data for finished task " + tname + " for job " +
                                id + ", task will be removed");
                        jobTracker.removeAwaitedTask(id.toString(), tname);
                    }
                }
                break;
            }
        }
    }

    /**
     * The method from jobTracker is encapsulated in order to be called via the
     * SmartProxy stub
     *
     * @param jobId
     * @param taskName
     */
    protected void removeAwaitedTask(String jobId, String taskName) {
        jobTracker.removeAwaitedTask(jobId, taskName);
    }

    public abstract void init(String url, String user, String pwd) throws SchedulerException, LoginException;

    public abstract void disconnect() throws PermissionException;

    public abstract void registerAsListener() throws NotConnectedException, PermissionException;

    /**
     * Push the input files of the given job from the local input folder to the
     * location specified by PUSH_URL.
     *
     * @param job                  job to push data for
     * @param localInputFolderPath local input folder
     * @return true if all input files are uploaded to the location specified by
     * the PUSH_URL.
     * @throws NotConnectedException
     * @throws PermissionException
     */
    public abstract boolean uploadInputfiles(TaskFlowJob job, String localInputFolderPath)
            throws NotConnectedException, PermissionException;

    public abstract void downloadTaskOutputFiles(AwaitedJob awaitedjob, String jobId, String t_name,
                                                 String localFolder);

    public abstract JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException;

    public abstract JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException;

    public abstract TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException;

    public abstract List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException;

    public abstract void addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
                                          SchedulerEvent[] events) throws NotConnectedException, PermissionException;

    protected abstract void removeJobIO(Job job, String pushURL, String pullURL, String newFolderName);

    protected abstract void createFolder(String fUri) throws NotConnectedException, PermissionException;

}
