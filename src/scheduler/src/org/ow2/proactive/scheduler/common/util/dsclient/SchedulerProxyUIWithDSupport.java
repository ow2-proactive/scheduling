package org.ow2.proactive.scheduler.common.util.dsclient;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.security.auth.login.LoginException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.util.CachingSchedulerProxyUserInterface;


/**
 * A Proxy to the Scheduler with built-in support for automatic data pushing and
 * pulling in order to provide disconnected mode for the dataspace layer.
 * 
 * This implementation assumes that:
 * 
 * <ul>
 * <li>the client application needs to submit to the scheduler jobs which
 * require data transfer to the execution nodes</li>
 * 
 * <li>the input data is accessible by the client on the local file system (or
 * on location accessible on NFS)</li>
 * 
 * <li>the output data is to be copied to the local file system (or on location
 * accessible on NFS)</li>
 * 
 * <li>the local file system is not visible from the computation nodes side</li>
 * 
 * *
 * <li>There is a location (let's call it TMP_INPUT_LOCATION), for transferring
 * input data, accessible from both sides, client side and computation node
 * side. Same for output data (let's call it TMP_OUTPUT_LOCATION). These
 * locations could be the same. It might be a shared folder or a data server.
 * 
 * Let´s call push_url the url used by the client application in order to push
 * the input data to TMP_INPUT_LOCATION.
 * 
 * Let´s call pull_url the url used by the client application in order to pull
 * the output data from TMP_OUTPUT_LOCATION.
 * 
 * The job needs to specify, as input space, an url pointing to
 * TMP_INPUT_LOCATION. The job needs to specify, as output space, an url
 * pointing to TMP_OUTPUT_LOCATION. These urls might or not be the same as
 * push_url and pull_url.
 * </li>
 * 
 * </ul>
 * 
 * 
 * The client application will use this Proxy for communicating with the
 * Scheduler. This Proxy is an ActiveObject.
 * 
 * In order to use this object, a reference to it should be obtained via the
 * {@link #getActiveInstance()} method. One of the init methods should be called
 * afterwards.
 * 
 * The client could add a Listener to this object in order to receive
 * notifications from the Scheduler. The listener is of type
 * {@link ISchedulerEventListenerExtended} which, in addition to the
 * notifications declared by its super type {@link SchedulerEventListener}, can
 * be notified with events related to data transfer.
 * 
 * Remember this is an active object. The listener object needs to be an active
 * or remote object (in order to avoid passing the listene through deep copy).
 * You could use, for instance:
 * 
 * {@code
 * 		ISchedulerEventListenerExtended myListenerRemoteReference = PARemoteObject.turnRemote( new MyEventListener());
 * 		schedProxy.addEventListener(myListenerRemoteReference);
 *  }
 * 
 * 
 * When a listener is added by the client, no new connection will be established
 * with the scheduler. This Proxy object broadcasts events received from the
 * Scheduler to its own listeners. In addition, it adds events related to data
 * transfer.
 * 
 * When a new job is submitted, these operations will be performed:
 * 
 * <ul>
 * <li>A temporary folder, for this execution, is created on the
 * TMP_INPUT_LOCATION. A temporary folder, for this execution, is created on the
 * TMP_OUTPUT_LOCATION (if TMP_INPUT_LOCATION!=TMP_OUTPUT_LOCATION).</li>
 * 
 * <li>The job INPUT_SPACE and OUTPUT_SPACE urls are updated with the new
 * created temporary folders.</li>
 * 
 * 
 * <li>The input data is pushed, via the push_url, from the local file system,
 * to the temporary folder</li>
 * 
 * <li>The job is added to a list of awaited jobs, in order to pull the output
 * data once the job is finished. This list is persisted on disk and will be
 * restored after an application restart.</li>
 * 
 * <li>When the job is finished, the output data is pulled form the
 * TMP_OUTPUT_LOCATION</li>
 * 
 * 
 * <li>The client application will be notified, via the listener, about the
 * evolution of the submitted jobs and about data transfer operations.</li>
 * 
 * </ul>
 * 
 * Each time this object is initialized, it recovers the awaited_jobs list from
 * the persisted file and, for each finished job, it pulls the output data from
 * the TMP_OUTPUT_LOCATION to the local file system the
 * 
 * 
 * 
 * @author esalagea
 * 
 */
public class SchedulerProxyUIWithDSupport extends CachingSchedulerProxyUserInterface {

    /*
     * a reference to a stub on this active object
     */
    private static SchedulerProxyUIWithDSupport activeInstance;

    public static final String GENERIC_INFO_INPUT_FOLDER_PROPERTY_NAME = "client_input_data_folder";
    public static final String GENERIC_INFO_OUTPUT_FOLDER_PROPERTY_NAME = "client_output_data_folder";

    public static final String GENERIC_INFO_PUSH_URL_PROPERTY_NAME = "push_url";
    public static final String GENERIC_INFO_PULL_URL_PROPERTY_NAME = "pull_url";

    public static final int MAX_NB_OF_DATA_TRANSFER_THREADS = 20;

    /**
     * Thread factory for data transfer operations
     */
    private transient ThreadFactory tf = new NamedThreadFactory("Data Transfer Thread");
    final private transient ExecutorService tpe = Executors.newFixedThreadPool(
            MAX_NB_OF_DATA_TRANSFER_THREADS, tf);

    private Set<ISchedulerEventListenerExtended> eventListeners = Collections
            .synchronizedSet(new HashSet<ISchedulerEventListenerExtended>());

    /**
     * A map of jobs that have been launched and wich's results are awaited each
     * time a new job is sent to the scheduler for computation, it will be added
     * to this map, as an entry of (JobId, AwaitedJob), where JobId is given as
     * a string. When the output data related to this job has been transfered,
     * the corresponding awaited job will be removed from this map. This map is
     * persisted in the status file
     */
    protected Map<String, AwaitedJob> awaitedJobs = Collections
            .synchronizedMap(new HashMap<String, AwaitedJob>());

    /**
     * XML file which persists the list of {@link AwaitedJob}
     */
    protected File statusFile;
    protected static String statusFilename = "dataTransfer.status";

    // TODO: is the FileSystemManager threadSafe ? Do we need to create one
    // instance per thread ?
    // See https://issues.apache.org/jira/browse/VFS-98
    /**
     * The VFS {@link FileSystemManager} used for file transfer
     */
    transient private FileSystemManager fsManager = null;

    {
        try {
            fsManager = VFSFactory.createDefaultFileSystemManager();
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Could nnot create Default FileSystem Manager", e);
        }
    }

    /**
     * Singleton active object constructor.
     * 
     * Creates an active object on this and returns a reference to its stub. If
     * the active object is already created, returns the reference
     * 
     * @return
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */

    public static synchronized SchedulerProxyUIWithDSupport getActiveInstance()
            throws ActiveObjectCreationException, NodeException {
        if (activeInstance != null)
            return activeInstance;

        activeInstance = PAActiveObject.newActive(SchedulerProxyUIWithDSupport.class, new Object[] {});
        return activeInstance;
    }

    /**
     * Initializes the connection the scheduler. Restores the awaited jobs list
     * from file and starts transferring output data if available
     * 
     * Must be called only once
     * 
     * @param url
     *            the scheduler's url
     * @param credentials
     *            the credential to be passed to the scheduler
     * @throws SchedulerException
     *             thrown if the scheduler is not available
     * @throws LoginException
     *             thrown if the credential is invalid
     */
    @Override
    public void init(String url, Credentials credentials) throws SchedulerException, LoginException {
        // first we load the list of awaited jobs
        loadAwaitedJobs();
        // then we call super.init() which will create the connection to the
        // scheduler and subscribe as event listener
        super.init(url, credentials);
        // now we can can check if we need to transfer any data
        checkResultsForAwaitedJobs();

    }

    /*
     * (non-Javadoc)
     * 
     * @see #init(String url, CredData credData)
     */
    @Override
    public void init(String url, String user, String pwd) throws SchedulerException, LoginException {
        CredData cred = new CredData(CredData.parseLogin(user), CredData.parseDomain(user), pwd);
        init(url, cred);
    }

    /**
     * initialize the connection the scheduler. Must be called only once
     * Restores the awaited jobs list from file and starts transferring output
     * data if available
     * 
     * @param url
     *            the scheduler's url
     * @param credData
     *            the credential data to be passed to the scheduler
     * @throws SchedulerException
     *             thrown if the scheduler is not available
     * @throws LoginException
     *             thrown if the credential is invalid
     */
    @Override
    public void init(String url, CredData credData) throws SchedulerException, LoginException {
        // first we load the list of awaited jobs
        loadAwaitedJobs();
        //  we call super.init() which will create the connection to the
        // scheduler and subscribe as event listener
        super.init(url, credData);
        // now we can can check if we need to transfer any data
        checkResultsForAwaitedJobs();
    }

    @Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        return super.submit(job);
    }

    /**
     * Does the following steps:
     * <ul>
     * <li>Prepares the temporary folders. See
     * {@link #prepareDataFolderAndUpdateJob(Job, String, String, String, String)}
     * </li>
     * 
     * <li>pushes all files from localInputFolderPath to the push_url location
     * (see {@link #pushData(Job, String)})</li>
     * 
     * <li>submits the job to the scheduler</li>
     * 
     * <li>adds the job to the awaited jobs list in order to download the output
     * data when the job is finished</li>
     * 
     * </ul>
     * 
     * Note: this method is synchronous. The caller will be blocked until the
     * the data is pushed and the job submitted.
     * 
     * @param job
     *            job object to be submitted to the Scheduler Server for
     *            execution
     * @param localInputFolderPath
     *            path to the folder containing the input data for this job
     * 
     * @param push_url
     *            the url where input data is to be pushed before the job
     *            submission
     * 
     * @param localOutputFolderPath
     *            path to the folder where the output data produced by tasks in
     *            this job should be copied
     * 
     * @param pull_url
     *            the url where the data is to be retrieved after the job is
     *            finished
     * 
     * @param localOutputFolderPath
     * 
     * @return
     * @throws JobCreationException
     * @throws SubmissionClosedException
     * @throws PermissionException
     * @throws NotConnectedException
     * @throws FileSystemException
     */
    public JobId submit(Job job, String localInputFolderPath, String push_url, String localOutputFolderPath,
            String pull_url) throws NotConnectedException, PermissionException, SubmissionClosedException,
            JobCreationException, FileSystemException {

        if (((push_url == null) || (push_url.equals(""))) && ((pull_url == null) || (pull_url.equals("")))) {
            logger
                    .warn("For the job " + job.getId() +
                        " no push or pull urls are defined. No data will be transfered for this job from the local machine ");
            return super.submit(job);
        }

        String newFolderName = createNewFolderName();
        String push_Url_update = prepareJobInput(job, localInputFolderPath, push_url, newFolderName);
        String pull_url_update = prepareJobOutput(job, localOutputFolderPath, pull_url, newFolderName);

        pushData(job, localInputFolderPath);
        JobId id = super.submit(job);

        AwaitedJob aj = new AwaitedJob(id.toString(), localInputFolderPath, job.getInputSpace(),
            push_Url_update, localOutputFolderPath, job.getOutputSpace(), pull_url_update);

        addAwaitedJob(aj);
        return id;
    }

    private void createFolder(String fUri) throws FileSystemException {

        FileObject fo = fsManager.resolveFile(fUri);
        fo.createFolder();

        logger.debug("Created remote folder: " + fUri);
    }

    //    /**
    //     *
    //     * This method will create remote folders for the input and output of this
    //     * job and update the inputSpace and outputSpace job properties. If the
    //     * localInputFolder parameter is null, or push_url is null, no action will
    //     * be performed concerning this job's input. If the localOutputFolder
    //     * parameter is null, or pull_url no action will be performed concerning
    //     * this job's output.
    //     *
    //     * <p/>
    //     * We suppose there is file storage accessible by the client application as
    //     * well as the tasks on the computation nodes.
    //     * <p/>
    //     * This storage could be different for input and for output.
    //     * <p/>
    //     * The input storage can be accessed, by the client application, using the
    //     * push_url and by the tasks on the nodes using the job's input space url.
    //     * <p/>
    //     * This output storage can be accessed, by the client application, using the
    //     * pull_url and by the tasks on the nodes using the job's output space url.
    //     *
    //     * <p/>
    //     * Prepare Input Data Transfer
    //     *
    //     * A folder will be created at push_url/NewFolder/input (which, from the
    //     * nodes side, is the job.InputSpace/NewFolder/input) . The InputSpace
    //     * property of the job will be changed to the new location. job.InputSpace =
    //     * job.InputSpace/NewFolder/input
    //     * <p/>
    //     *
    //     * A generic information will be attached to the job containing the local
    //     * input folder path.
    //     *
    //     * Prepare Output Data Transfer
    //     *
    //     * A folder will be created at pull_url/NewFolder/output (which, from the
    //     * nodes side, is job.OutputSpace/NewFolder/output).
    //     * <p/>
    //     * The OutputSpace property of the job will be changed to the new location.
    //     * job.OutputSpace = job.OutputSpace/NewFolder/output
    //     * <p/>
    //     * A generic information will be attached to the job containing the local
    //     * output folder path
    //     *
    //     * NewFolder is a random name.
    //     *
    //     * @param job
    //     * @param localInputFolder
    //     *            path to the input folder on local machine if null, no actions
    //     *            will be performed concerning the input data for this job
    //     *
    //     * @param push_url
    //     *            the url where input data is to be pushed before the job
    //     *            submission
    //     *
    //     * @param localOutputFolder
    //     *            path to the output folder on local machine if null, no actions
    //     *            will be performed concerning the output data for this job
    //     *
    //     * @param pull_url
    //     *            - the url where the data is to be retrieved after the job is
    //     *            finished
    //     *
    //     *
    //     * @return an array of 2 String elements representing the value of the
    //     *         suffixes added to the job's input space and respectively output
    //     *         space properties.
    //     * @throws FileSystemException
    //     */
    //    private String[] prepareDataFolderAndUpdateJob(Job job, String localInputFolder, String push_url,
    //            String localOutputFolder, String pull_url) throws FileSystemException {
    //
    //        // Choose a random name for a new folder to be created
    //        String newFolderName = "" + System.currentTimeMillis() + new Random().nextInt(1000);
    //
    //        // if the job defines an input space
    //        // and the localInputFolder is not null
    //        // create a remote folder for the input data
    //        // and update the InputSpace property of the job to reference that
    //        // folder
    //
    //        String inputSpace_url = job.getInputSpace();
    //        String push_url_updated = "";
    //        String pull_url_updated = "";
    //
    //        // the input folder, on the remote input space, relative to the root url
    //        String inputFolder = "";
    //        if ((localInputFolder != null) && (inputSpace_url != null) && (!inputSpace_url.equals("")) &&
    //            (push_url != null)) {
    //            inputFolder = newFolderName + "/input";
    //            push_url_updated = push_url + "//" + inputFolder;
    //            String inputSpace_url_updated = inputSpace_url + "//" + inputFolder;
    //            createFolder(push_url_updated);
    //            job.setInputSpace(inputSpace_url_updated);
    //            job.addGenericInformation(GENERIC_INFO_INPUT_FOLDER_PROPERTY_NAME, new File(localInputFolder)
    //                    .getAbsolutePath());
    //
    //            job.addGenericInformation(GENERIC_INFO_PUSH_URL_PROPERTY_NAME, push_url_updated);
    //        }
    //
    //        // if the job defines an output space
    //        // and the localOutputFolder is not null
    //        // create a remote folder for the output data
    //        // and update the OutputSpace property of the job to reference that
    //        // folder
    //
    //        String outputSpace_url = job.getOutputSpace();
    //        // the input folder, on the remote output space, relative to the root
    //        // url
    //        String outputFolder = "";
    //
    //        if ((localOutputFolder != null) && (outputSpace_url != null) && (!outputSpace_url.equals("")) &&
    //            (pull_url != null)) {
    //            outputFolder = newFolderName + "/output";
    //
    //            pull_url_updated = pull_url + "//" + outputFolder;
    //            String outputSpace_url_updated = outputSpace_url + "//" + outputFolder;
    //            createFolder(pull_url_updated);
    //
    //            job.setOutputSpace(outputSpace_url_updated);
    //            job.addGenericInformation(GENERIC_INFO_OUTPUT_FOLDER_PROPERTY_NAME, new File(localOutputFolder)
    //                    .getAbsolutePath());
    //            job.addGenericInformation(GENERIC_INFO_PULL_URL_PROPERTY_NAME, pull_url_updated);
    //
    //        }
    //        return new String[] { push_url_updated, pull_url_updated };
    //    }

    private String createNewFolderName() {
        String user = System.getProperty("user.name");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
        Date now = new Date();
        String strDate = sdf.format(now);

        String newFolderName = user + "_" + strDate;
        return newFolderName;

    }

    /**
     * 
     * This method will create a remote folder for output of this
     * job and update the outputSpace job property. If the localOutputFolder
     * parameter is null, or pull_url no action will be performed concerning
     * this job's output.
     * 
     * <p/>
     * We suppose there is file storage accessible by the client application as
     * well as the tasks on the computation nodes.
     * <p/>
     * This storage could be different for input and for output.
     * <p/>
     *
     * This output storage can be accessed, by the client application, using the
     * pull_url and by the tasks on the nodes using the job's output space url.
     * 
     * 
     * Prepare Output Data Transfer
     * 
     * A folder will be created at pull_url/NewFolder/output (which, from the
     * nodes side, is job.OutputSpace/NewFolder/output).
     * <p/>
     * The OutputSpace property of the job will be changed to the new location.
     * job.OutputSpace = job.OutputSpace/NewFolder/output
     * <p/>
     * A generic information will be attached to the job containing the local
     * output folder path
     * 
     * 
     * @param job
     * 
     * @param localOutputFolder
     *            path to the output folder on local machine if null, no actions
     *            will be performed concerning the output data for this job
     * 
     * @param pull_url
     *            - the url where the data is to be retrieved after the job is
     *            finished
     * 
     * @param newFolderName
     *            name of the folder to be used for pushing the output
     * 
     * @return a String representing the updated value of the pull_url
     * @throws FileSystemException
     */
    private String prepareJobOutput(Job job, String localOutputFolder, String pull_url, String newFolderName)
            throws FileSystemException {
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
            outputFolder = newFolderName + "/output";

            pull_url_updated = pull_url + "//" + outputFolder;
            String outputSpace_url_updated = outputSpace_url + "//" + outputFolder;
            createFolder(pull_url_updated);

            job.setOutputSpace(outputSpace_url_updated);
            job.addGenericInformation(GENERIC_INFO_OUTPUT_FOLDER_PROPERTY_NAME, new File(localOutputFolder)
                    .getAbsolutePath());
            job.addGenericInformation(GENERIC_INFO_PULL_URL_PROPERTY_NAME, pull_url_updated);
        }

        return pull_url_updated;
    }

    /**
     *
     * This method will create a remote folder for the input data of this
     * job and update the inputSpace job property.
     * If the  localInputFolder parameter is null, or push_url is null, no action will
     * be performed concerning this job's input.
     *
     * <p/>
     * We suppose there is file storage accessible by the client application as
     * well as the tasks on the computation nodes.
     * <p/>
     * This storage could be different for input and for output.
     * <p/>
     * The input storage can be accessed, by the client application, using the
     * push_url and by the tasks on the nodes using the job's input space url.
     * <p/>
     *
     * Prepare Input Data Transfer
     *
     * A folder will be created at push_url/newFolderName/input (which, from the
     * nodes side, is the job.InputSpace/newFolderName/input) . The InputSpace
     * property of the job will be changed to the new location. job.InputSpace =
     * job.InputSpace/NewFolder/input
     * <p/>
     *
     * A generic information will be attached to the job containing the local
     * input folder path.
     *
     *
     * @param job
     * @param localInputFolder
     *            path to the input folder on local machine if null, no actions
     *            will be performed concerning the input data for this job
     *
     * @param push_url
     *            the url where input data is to be pushed before the job
     *            submission
     *
     * @param newFolderName
     *            name of the new folder to be created
     *
     * @return  String representing the updated value of the push_url
     *
     * @throws FileSystemException
     */
    private String prepareJobInput(Job job, String localInputFolder, String push_url, String newFolderName)
            throws FileSystemException {
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
            (push_url != null)) {
            inputFolder = newFolderName + "/input";
            push_url_updated = push_url + "//" + inputFolder;
            String inputSpace_url_updated = inputSpace_url + "//" + inputFolder;
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

    /**
     * This method will check, for each awaited job, if the result is available
     * on the Scheduler. If positive, the will call the performPostTreatment
     * method in order to perform the post treatment.
     */
    protected void checkResultsForAwaitedJobs() {
        // we make a copy of the awaitedJobsIds set in order to iterate over it.
        Set<AwaitedJob> awaitedJobsIdsCopy = new HashSet<AwaitedJob>(awaitedJobs.values());

        Iterator<AwaitedJob> it = awaitedJobsIdsCopy.iterator();
        while (it.hasNext()) {
            AwaitedJob awaitedJob = it.next();
            String id = awaitedJob.getJobId();

            try {
                JobState js = uischeduler.getJobState(id);
                if (js.isFinished()) {
                    pullData(awaitedJob);
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
                removeAwaitedJob(id);
            } catch (PermissionException e) {
                logger
                        .error(
                                "Could not retrieve output data for job " +
                                    id +
                                    " because you don't have permmission to access this job. You need to use the same connection credentials you used for submitting the job.  \n Another attempt to dowload the output data for this job will be made next time the application is initialized. ",
                                e);
            }
        }
    }

    /**
     * 
     * @param job
     * @param inputFolder
     * @return
     * @throws FileSystemException
     */
    protected boolean pushData(Job job, String localInputFolderPath) throws FileSystemException {

        String push_URL = job.getGenericInformations().get(GENERIC_INFO_PUSH_URL_PROPERTY_NAME);

        if ((push_URL == null) || (push_URL.trim().equals(""))) {
            return false;
        }// push inputData

        // TODO - if the copy fails, try to remove the files from the remote
        // folder before throwing an exception
        FileObject remoteFolder = fsManager.resolveFile(push_URL);
        FileObject localfolder = fsManager.resolveFile(localInputFolderPath);
        logger.debug("Pushing files from " + localfolder + " to " + remoteFolder);

        //create the selector
        DSFileSelector fileSelector = new DSFileSelector();

        TaskFlowJob tfj = (TaskFlowJob) job;
        for (Task t : tfj.getTasks()) {
            List<InputSelector> inputFileSelectors = t.getInputFilesList();
            for (InputSelector is : inputFileSelectors) {
                org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector fs = is.getInputFiles();
                if (fs.getIncludes() != null)
                    fileSelector.addIncludes(Arrays.asList(fs.getIncludes()));

                if (fs.getExcludes() != null)
                    fileSelector.addExcludes(Arrays.asList(fs.getExcludes()));
            }
        }

        //We need to check if a pattern exist in both includes and excludes.
        // This may happen if a task one defines, for instance "*.txt" as includes
        //and a task two defines "*.txt" as excludes. In this case we should remove it from the fileSelector's excludes.

        Set<String> includes = fileSelector.getIncludes();
        Set<String> excludes = fileSelector.getExcludes();

        Set<String> intersection = new HashSet<String>(includes);
        intersection.retainAll(excludes);
        excludes.removeAll(intersection);
        fileSelector.setExcludes(excludes);

        remoteFolder.copyFrom(localfolder, fileSelector);

        logger.debug("Finished push operation from " + localfolder + " to " + remoteFolder);
        return true;
    }

    /**
     * Retrieves the output files produced by the job having the id given as
     * argument. If the transfer finishes successfully it deletes the temporary
     * folders (at push_url and pull_url location) and send notification to the
     * listeners. Otherwise it notifies the listeners of the failure.
     * 
     * The transfer data operation is executed by a fixed thread pool executor
     * (see {@link DataTransferProcessor})
     * 
     * @param awaitedjob
     * @return
     * @throws FileSystemException
     */
    protected void pullData(AwaitedJob awaitedjob) {
        String localOutFolderPath = awaitedjob.getLocalOutputFolder();
        if (localOutFolderPath == null) {
            logger.warn("The job " + awaitedjob.getJobId() +
                " does not define an output folder on local machine. No output data will be retrieved");
            return;
        }

        String jobId = awaitedjob.getJobId();
        String pull_URL = awaitedjob.getPullURL();
        String pushUrl = awaitedjob.getPushURL();

        FileObject remotePullFolder = null;
        FileObject remotePushFolder = null;
        FileObject localfolder = null;

        Set<FileObject> foldersToDelete = new HashSet<FileObject>();

        try {
            remotePullFolder = fsManager.resolveFile(pull_URL);
            remotePushFolder = fsManager.resolveFile(pushUrl);
            localfolder = fsManager.resolveFile(localOutFolderPath);
        } catch (Exception e) {
            logger.error("Could not retrieve data for job " + jobId, e);
            logger
                    .info("Job  " +
                        jobId +
                        " will be removed from the known job list. The system will not attempt again to retrieve data for this job. You couyld try to manually copy the data from the location  " +
                        pull_URL);
            removeAwaitedJob(jobId);
            return;
        }

        try {
            foldersToDelete.add(remotePullFolder.getParent());
            if (!remotePullFolder.getParent().equals(remotePushFolder.getParent()))
                foldersToDelete.add(remotePushFolder.getParent());
        } catch (FileSystemException e) {
            logger.warn("Data in folders " + pull_URL + " and " + pushUrl +
                " cannot be deleted due to an unexpected error ", e);
            e.printStackTrace();
        }

        FileSelector fileSelector = Selectors.SELECT_ALL;
        // The code bellow has been commented:
        // We do not need to build a file selector because the files in the temporary folder
        // have been copied by the data space layer which already used a FastFileSelector
        // configured with includes and excludes patterns

        //         DSFileSelector fileSelector  = new DSFileSelector();
        //          try{
        //            JobState jobstate = uischeduler.getJobState(jobId);
        //            for (TaskState ts : jobstate.getTasks() )
        //     	   {
        //         	 List<OutputSelector> of =  ts.getOutputFilesList();
        //         	 for (OutputSelector outputSelector : of) {
        //         		 org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector fs = outputSelector.getOutputFiles();
        //
        //         		fileSelector.addIncludes(fs.getIncludes());
        //         		fileSelector.addExcludes(fs.getExcludes());
        //     		}
        //     	   }
        //          }catch (Exception e)
        //     	   {
        //     		 logger_util.error("An exception occured while computing which output files to download for job "+ jobId+". All available files will be downloaded for this job");
        //         	 e.printStackTrace();
        //     	   }

        DataTransferProcessor dtp = new DataTransferProcessor(remotePullFolder, localfolder, jobId,
            foldersToDelete, fileSelector);
        tpe.submit(dtp);
    }

    /**
     * creates the list of {@link AwaitedJob} from the statusFile
     */
    protected void loadAwaitedJobs() {
        statusFile = new File(statusFilename);
        if (!statusFile.isFile()) {
            return;
        }

        awaitedJobs = Collections.synchronizedMap(new HashMap<String, AwaitedJob>());

        XMLDecoder decoder = null;
        try {
            decoder = new java.beans.XMLDecoder(new FileInputStream(statusFile));
        } catch (FileNotFoundException e1) {
            logger
                    .error(
                            "Could not load the status file. No data will be retrieved for previousley submitted jobs.",
                            e1);
            return;
        }

        boolean finishRead = false;
        while (!finishRead) {
            try {
                AwaitedJob aj = (AwaitedJob) decoder.readObject();
                awaitedJobs.put(aj.getJobId(), aj);
            } catch (IndexOutOfBoundsException e) {
                finishRead = true;
            }
        }
    }

    /**
     * Persists the list of {@link AwaitedJob} to file
     * @throws FileNotFoundException
     */
    protected synchronized void saveAwaitedJobsToFile() throws FileNotFoundException {

        File statusFileBK = new File(statusFilename + ".BAK");
        if (statusFileBK.isFile()) {
            statusFileBK.delete();
        }

        if (statusFile != null && statusFile.isFile())
            statusFile.renameTo(statusFileBK);

        try {
            statusFile = new File(statusFilename);
            XMLEncoder encoder = new XMLEncoder(new FileOutputStream(statusFile));

            for (AwaitedJob aj : awaitedJobs.values()) {
                encoder.writeObject(aj);
            }
            encoder.flush();
            encoder.close();
        } catch (Throwable t) {
            logger
                    .error(
                            "Could not persist the list of awaited jobs. Some jobs output data might not be transfered after application restart ",
                            t);
            //recover the status file from the backup file
            statusFile.delete();
            statusFileBK.renameTo(statusFile);
        }

    }

    /**
     * Check if the job concerned by this notification is awaited. Retrieve
     * corresponding data if needed
     * 
     * @param notification
     */
    protected void update(NotificationData<?> notification) {

        // am I interested in this job?
        JobId id = ((NotificationData<JobInfo>) notification).getData().getJobId();

        AwaitedJob aj = awaitedJobs.get(id.toString());

        if (aj == null)
            return;

        JobState jobState = null;

        try {
            jobState = this.getJobState(id);
        } catch (NotConnectedException e) {
            logger.error("Could not retreive output data for job " + id, e);
        } catch (UnknownJobException e) {
            logger.error("Could not retreive output data for job " + id, e);
        } catch (PermissionException e) {
            logger.error("Could not retreive output data for job " + id +
                ". Did you connect with a diffrent user ? ", e);
        }

        if (jobState == null) {
            logger.warn("The job " + id +
                " is listed as awaited but is unknown bby the scheduler. It will be removed from local list");
            removeAwaitedJob(id.toString());
        }

        JobStatus status = jobState.getStatus();
        switch (status) {
            case KILLED: {
                logger.info("The job " + id + "has been killed. No data will be transfered");
                removeAwaitedJob(id.toString());
                break;
            }
            case FINISHED: {
                logger.info("Transfering data for finished job " + id);
                pullData(aj);
                this.removeAwaitedJob(id.toString());
                logger.info("Data transfer finished for finished job " + id);

                break;
            }
            case CANCELED: {
                logger.info("Transfering data for canceled job " + id);
                pullData(aj);
                this.removeAwaitedJob(id.toString());
                break;
            }
            case FAILED: {
                logger.info("Transfering data for failed job " + id);
                pullData(aj);
                this.removeAwaitedJob(id.toString());
                break;
            }
        }
    }

    // ******** Scheduler Event Listener *********************** //

    /**
     * Subscribes a listener to the Scheduler
     */
    public void addEventListener(ISchedulerEventListenerExtended sel) throws NotConnectedException,
            PermissionException {

        eventListeners.add(sel);
    }

    public void removeEventListener(ISchedulerEventListenerExtended sel) {
        eventListeners.remove(sel);
    }

    // ***  Forward all events from the Scheduler to the local listeners *************** //

    /**
     * Invoked each time a scheduler event occurs.<br />
     * Scheduler events are stopped,started, paused, frozen, ...
     * 
     * @param eventType
     *            the type of the event received.
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {

        for (SchedulerEventListener l : eventListeners) {
            l.schedulerStateUpdatedEvent(eventType);
        }
        super.schedulerStateUpdatedEvent(eventType);

    }

    /**
     * Invoked each time a new job has been submitted to the Scheduler and
     * validated.
     * 
     * @param job
     *            the newly submitted job.
     */
    public void jobSubmittedEvent(JobState job) {
        for (SchedulerEventListener l : eventListeners) {
            l.jobSubmittedEvent(job);
        }

        super.jobSubmittedEvent(job);
    }

    /**
     * Invoked each time the state of a job has changed.<br>
     * If you want to maintain an up to date list of jobs, just use the
     * {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.job.JobInfo)}
     * method to update the content of your job.
     * 
     * @param notification
     *            the data composed of the type of the event and the information
     *            that have change in the job.
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        for (SchedulerEventListener l : eventListeners) {
            l.jobStateUpdatedEvent(notification);
        }
        super.jobStateUpdatedEvent(notification);

        update(notification);

    }

    /**
     * Invoked each time the state of a task has changed.<br>
     * In this case you can use the
     * {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.task.TaskInfo)}
     * method to update the content of the designated task inside your job.
     * 
     * @param notification
     *            the data composed of the type of the event and the information
     *            that have change in the task.
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        for (SchedulerEventListener l : eventListeners) {
            l.taskStateUpdatedEvent(notification);
        }
        super.taskStateUpdatedEvent(notification);
    }

    /**
     * Invoked each time something change about users.
     * 
     * @param notification
     *            the data composed of the type of the event and the data linked
     *            to the change.
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        for (SchedulerEventListener l : eventListeners) {
            l.usersUpdatedEvent(notification);
        }
        super.usersUpdatedEvent(notification);
    }

    // ********* Awaited Jobs methods ******************************* //
    /**
     * 
     * @return a new HashSet with the awaited jobs. Modifying the result of this
     *         method will not affect the source HashSet (the awaited jobs)
     */
    public HashSet<AwaitedJob> getAwaitedJobs() {
        return new HashSet<AwaitedJob>(awaitedJobs.values());
    }

    public boolean isAwaitedJob(String id) {
        if (awaitedJobs.get(id) != null)
            return true;
        else
            return false;
    }

    protected void addAwaitedJob(AwaitedJob aj) {
        this.awaitedJobs.put(aj.getJobId(), aj);
        try {
            this.saveAwaitedJobsToFile();
        } catch (FileNotFoundException e) {
            logger.error("Could not save status file after adding job on awaited jobs list " + aj.getJobId());
        } catch (IOException e) {
            logger.error("Could not save status file after adding job on awaited jobs list " + aj.getJobId(),
                    e);
        }
    }

    protected void removeAwaitedJob(String id) {
        this.awaitedJobs.remove(id);

        try {
            this.saveAwaitedJobsToFile();
        } catch (FileNotFoundException e) {
            logger.error("Could not save status file after removing job " + id, e);
        } catch (IOException e) {
            logger.error("Could not save status file after removing job " + id, e);
        }
    }

    private class DataTransferProcessor implements Runnable {
        private FileObject source;
        private FileObject dest;
        private String jobId;
        private Set<FileObject> foldersToDelete;
        private FileSelector fileSelector;

        /**
         * 
         * @param source
         *            source folder
         * @param dest
         *            dest folder
         * @param _jobId
         *            - only used for pull operations. For push operations, the
         *            jobId is null
         */
        public DataTransferProcessor(FileObject source, FileObject dest, String _jobId,
                Set<FileObject> foldersToDelete, FileSelector fileSelector) {
            this.source = source;
            this.dest = dest;
            this.jobId = _jobId;
            this.foldersToDelete = foldersToDelete;
            this.fileSelector = fileSelector;
        }

        @Override
        public void run() {

            String sourceUrl = "NOT YET DEFINED";
            String destUrl = "NOT YET DEFINED";
            try {
                sourceUrl = source.getURL().toString();
                destUrl = dest.getURL().toString();

                logger.debug("Copying files from " + source + " to " + dest);
                dest.copyFrom(source, fileSelector);
                logger.debug("Finished copying files from " + source + " to " + dest);

            } catch (FileSystemException e) {
                logger.error("An error occured while copying files from " + source + " to " + dest, e);
                if (jobId != null) {
                    logger
                            .info("Job  " +
                                jobId +
                                " will be removed from the known job list. The system will not attempt again to retrieve data for this job. You could try to manually copy the data from the location  " +
                                sourceUrl);

                    for (ISchedulerEventListenerExtended l : eventListeners) {
                        l.pullDataFailed(jobId, sourceUrl, e);
                    }
                    removeAwaitedJob(jobId);
                    return;
                }
            }// catch

            removeAwaitedJob(jobId);
            // delete source data
            String url = "NOT YET DEFINED";
            for (FileObject fo : foldersToDelete) {
                try {
                    url = fo.getURL().toString();
                    fo.delete(Selectors.SELECT_ALL);
                    fo.delete();
                } catch (FileSystemException e) {
                    logger.warn("Could not delete temporary fioles at location " + url +
                        " . although the copy of these files to " + destUrl +
                        " has been successffully performed.");
                }
            }

            for (ISchedulerEventListenerExtended l : eventListeners) {
                l.pullDataFinished(jobId, destUrl);
            }
        }

    }
}
