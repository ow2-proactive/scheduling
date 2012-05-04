package org.ow2.proactive.scheduler.common.util.dsclient;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.Selectors;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
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
import org.ow2.proactive.scheduler.common.util.CachingSchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;


public class CachingSchedulerProxyUIWithDSupport extends CachingSchedulerProxyUserInterface {

    /*
     * a reference to a stub on this active object
     */
    private static CachingSchedulerProxyUIWithDSupport activeInstance;

    public static final String GENERIC_INFO_INPUT_FOLDER_PROPERTY_NAME = "client_input_data_folder";
    public static final String GENERIC_INFO_OUTPUT_FOLDER_PROPERTY_NAME = "client_output_data_folder";

    //	protected Set<AwaitedJob> awaitedJobsIds = Collections
    //			.synchronizedSet(new HashSet<AwaitedJob>());

    /**
     * A map of jobs that have been launched and wich's results are awaited Each
     * time a new job is sent to the scheduler for computation, it will be
     * added to this map, as an entry of (JobId, AwaitedJob), where JobId is given as a string.  
     * When the merge of a job have been completely
     * performed, the corresponding awaited job will be removed from this map.  This map is persisted in
     * the status file
     */

    protected Map<String, AwaitedJob> awaitedJobs = Collections
            .synchronizedMap(new HashMap<String, AwaitedJob>());

    protected static String statusFilename = "dataTransfer.status";
    /**
     * this file keeps the ids of the awaited jobs it is used in case of crash
     * of the application
     */
    protected File statusFile;

    /**
     * This interface will be used in order to gather the results of the
     * finished jobs
     */
    protected SchedulerProxyUserInterface uiScheduler;

    protected static String awaitedJobsAttrName = "awaited_jobs";
    protected static String awaitedJobsSeparator = " ";

    // TODO: is the FileSystemManager threadSafe ? Do we need to create one
    // instance per thread ?
    // See https://issues.apache.org/jira/browse/VFS-98
    transient private FileSystemManager fsManager = null;

    {
        try {
            fsManager = VFSFactory.createDefaultFileSystemManager();
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger_util.error("Could nnot create Default FileSystem Manager", e);
        }
    }

    /**
     * Singleton active object constructor. Creates the singleton
     *
     * Creates an active object on this and returns a reference to its stub. If
     * the active object is already created, returns the reference
     *
     * @return
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */

    public static CachingSchedulerProxyUIWithDSupport getActiveInstance()
            throws ActiveObjectCreationException, NodeException {
        if (activeInstance != null)
            return activeInstance;

        activeInstance = PAActiveObject.newActive(CachingSchedulerProxyUIWithDSupport.class, new Object[] {});
        return activeInstance;
    }

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

    @Override
    public void init(String url, String user, String pwd) throws SchedulerException, LoginException {
        CredData cred = new CredData(CredData.parseLogin(user), CredData.parseDomain(user), pwd);
        init(url, cred);
    }

    @Override
    public void init(String url, CredData credData) throws SchedulerException, LoginException {
        // first we load the list of awaited jobs
        loadAwaitedJobs();
        // then we call super.init() which will create the connection to the
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
     * Suppose the job has defined Input Space and/or OutputSpace property. If
     * none of these properties is defined, just submits the job, without data
     * management. Otherwise
     *
     *
     * @param job
     *            - job object to be submitted to the Scheduler Server for
     *            execution
     * @param localInputFolderPath
     *            - path to the folder containing the input data for this job
     * @param localOutputFolderPath
     *            - path to the folder where the output data produced by tasks
     *            in this job should be copied
     * @return
     * @throws JobCreationException
     * @throws SubmissionClosedException
     * @throws PermissionException
     * @throws NotConnectedException
     * @throws FileSystemException
     */
    public JobId submit(Job job, String localInputFolderPath, String localOutputFolderPath)
            throws NotConnectedException, PermissionException, SubmissionClosedException,
            JobCreationException, FileSystemException {

        String inputSpace = job.getInputSpace();
        String outputSpace = job.getOutputSpace();

        if (((inputSpace == null) || (inputSpace.trim().equals(""))) &&
            ((outputSpace == null) || (outputSpace.trim().equals("")))) {
            logger_util.warn("Job " + job.getId() +
                " does not define input space nor output space. No data will be transfered for this job. ");
            return super.submit(job);
        }

        prepareDataFolderAndUpdateJob(job, localInputFolderPath, localOutputFolderPath);
        pushData(job, localInputFolderPath);
        JobId id = super.submit(job);

        AwaitedJob aj = new AwaitedJob(id.toString(), localInputFolderPath, job.getInputSpace(),
            localInputFolderPath, job.getOutputSpace());
        addAwaitedJob(aj);

        return null;
    }

    private String createFolder(String uri, String folderName) throws FileSystemException {
        String fUri = uri + "//" + folderName;
        FileObject fo = fsManager.resolveFile(fUri);
        fo.createFolder();

        logger_util.debug("Created remote folder: " + fUri);
        return fUri;
    }

    /**
     *
     * This method will create remote folders for the input and output of this
     * job and update the inputSpace and outputSpace job properties If the
     * localInputFolder parameter is null, no action will be performed
     * concerning this job's input. If the localOutputFolder parameter is null,
     * no action will be performed concerning this job's output.
     *
     * The job is supposed to have the input space property pointing to a global
     * input space and/or output space property pointing to a global output
     * space
     *
     * Prepare Input Data Transfer
     *
     * A folder will be created at job.InputSpace/NewFolder/input. The
     * InputSpace property of the job will be changed to the new location.
     * job.InputSpace = job.InputSpace/NewFolder/input A generic information
     * will be attached to the job containing the local input folder path
     *
     * Prepare Output Data Transfer
     *
     * A folder will be created at job.OutputSpace/NewFolder/output. The
     * OutputSpace property of the job will be changed to the new location.
     * job.OutputSpace = job.OutputSpace/NewFolder/output A generic information
     * will be attached to the job containing the local output folder path
     *
     * NewFolder is a random name.
     *
     * @param job
     * @param localInputFolder
     *            path to the input folder on local machine if null, no actions
     *            will be performed concerning the input data for this job
     *
     * @param localOutputFolder
     *            path to the output folder on local machine if null, no actions
     *            will be performed concerning the output data for this job
     *
     * @return an array of 2 String elements representing the value of the
     *         suffixes added to the job's input space and respectively output
     *         space properties.
     * @throws FileSystemException
     */
    private String[] prepareDataFolderAndUpdateJob(Job job, String localInputFolder, String localOutputFolder)
            throws FileSystemException {

        // Choose a random name for a new folder to be created
        String newFolderName = "" + System.currentTimeMillis() + new Random().nextInt(1000);

        // if the job defines an input space
        // and the localInputFolder is not null
        // create a remote folder for the input data
        // and update the InputSpace property of the job to reference that
        // folder

        String inputSpace = job.getInputSpace();
        // the input folder, on the remote input space, relative to the root url
        String inputFolder = "";
        if ((localInputFolder != null) && (inputSpace != null) && (!inputSpace.equals(""))) {
            inputFolder = newFolderName + "/input";
            String newUri = createFolder(inputSpace, inputFolder);
            job.setInputSpace(newUri);
            job.addGenericInformation(GENERIC_INFO_INPUT_FOLDER_PROPERTY_NAME, new File(localInputFolder)
                    .getAbsolutePath());
        }

        // if the job defines an output space
        // and the localOutputFolder is not null
        // create a remote folder for the output data
        // and update the OutputSpace property of the job to reference that
        // folder

        String outputSpace = job.getOutputSpace();
        // the input folder, on the remote output space, relative to the root
        // url
        String outputFolder = "";

        if ((localOutputFolder != null) && (outputSpace != null) && (!outputSpace.equals(""))) {
            outputFolder = newFolderName + "/output";
            String newUri = createFolder(outputSpace, outputFolder);
            job.setOutputSpace(newUri);
            job.addGenericInformation(GENERIC_INFO_OUTPUT_FOLDER_PROPERTY_NAME, new File(localOutputFolder)
                    .getAbsolutePath());
        }

        return new String[] { inputFolder, outputFolder };
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
            if (pullData(awaitedJob))
                this.removeAwaitedJob(id);
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
        String inputSpace = job.getInputSpace();
        if ((inputSpace == null) || (inputSpace.trim().equals(""))) {
            return false;
        }// push inputData

        // TODO - if the copy fails, try to remove the files from the remote
        // folder before throwing an exception
        FileObject remoteFolder = fsManager.resolveFile(inputSpace);
        FileObject localfolder = fsManager.resolveFile(localInputFolderPath);
        remoteFolder.copyFrom(localfolder, Selectors.SELECT_ALL);
        return true;
    }

    /**
     * Retrieve the output files produced by the job having the id given as
     * argument.
     *
     * @param jobId_srt
     * @return
     * @throws FileSystemException
     */
    protected boolean pullData(AwaitedJob awaitedjob) {

        String localOutFolderPath = awaitedjob.getLocalOutputFolder();
        if (localOutFolderPath == null) {
            logger_util.warn("The job " + awaitedjob.getJobId() +
                " does not define an output folder on local machine. No output data will be retrieved");
            return false;
        }

        String jobId = awaitedjob.getJobId();
        JobState jobState = null;
        try {
            jobState = getJobState(jobId);
        } catch (NotConnectedException e1) {
            e1.printStackTrace();
            logger_util.error("Could not retrieve data for job " + jobId, e1);
            return false;
        } catch (UnknownJobException e1) {
            logger_util.error("Could not retrieve data for job " + jobId, e1);
            e1.printStackTrace();
            return false;
        } catch (PermissionException e1) {
            logger_util.error("Could not retrieve data for job " + jobId, e1);
            e1.printStackTrace();
            return false;
        }

        // Is the job finished ?

        if (!jobState.isFinished()) {

            logger_util.info("Job " + awaitedjob.getJobId() +
                " is not finihed. It remains on the awated jobs list. ");
            return false;
        }

        String outputSpaceURL = jobState.getOutputSpace();
        FileObject remoteFolder;
        try {
            remoteFolder = fsManager.resolveFile(outputSpaceURL);
            FileObject localfolder = fsManager.resolveFile(localOutFolderPath);
            localfolder.copyFrom(remoteFolder, Selectors.SELECT_ALL);
        } catch (FileSystemException e) {
            logger_util.error("Could not retrieve data for job " + jobId, e);
            return false;
        }
        return true;
    }

    // /**
    // * This reads the jobs Ids in the status file and updates the awaitedJobs
    // * list This operation should be performed when the application starts in
    // * order to update the awaited jobs conform to the status file
    // *
    // * @throws IOException
    // * @throws FileNotFoundException
    // */
    // protected synchronized void loadAwaitedJobs() {
    // statusFile = new File(statusFilename);
    // if (!statusFile.isFile()) {
    // return;
    // }
    //
    // String awaitedJobs = "";
    // Properties properties = new Properties();
    // try {
    // properties.load(new FileInputStream(statusFile));
    // } catch (FileNotFoundException e) {
    // logger_util
    // .error("Could not load the status file. No data will be retrieved for previousley submitted jobs.",
    // e);
    // return;
    // } catch (IOException e) {
    // logger_util
    // .error("Could not load the status file. No data will be retrieved for previousley submitted jobs.",
    // e);
    // return;
    // }
    //
    // awaitedJobs = properties.getProperty(awaitedJobsAttrName);
    // if ((awaitedJobs == null) || (awaitedJobs.length() == 0))
    // return;
    // awaitedJobs = awaitedJobs.trim();
    // String[] awaitedJobsStrArrray = awaitedJobs.split(awaitedJobsSeparator);
    // awaitedJobsIds = Collections.synchronizedSet(new HashSet<String>(Arrays
    // .asList(awaitedJobsStrArrray)));
    // }

    // /**
    // * Saves the awaitedJobs ids to the status file
    // *
    // * @throws IOException
    // * @throws FileNotFoundException
    // */
    // protected synchronized void saveAwaitedJobsToFile()
    // throws FileNotFoundException, IOException {
    // String awaitedJobsStr = "";
    // for (String s : awaitedJobsIds) {
    // awaitedJobsStr += s + awaitedJobsSeparator;
    // }
    // Properties properties = new Properties();
    // properties.put(awaitedJobsAttrName, awaitedJobsStr);
    // properties.store(new FileOutputStream(statusFile), null);
    // }

    protected synchronized void loadAwaitedJobs() {
        statusFile = new File(statusFilename);
        if (!statusFile.isFile()) {
            return;
        }

        awaitedJobs = Collections.synchronizedMap(new HashMap<String, AwaitedJob>());

        XMLDecoder decoder = null;
        try {
            decoder = new java.beans.XMLDecoder(new FileInputStream(statusFile));
        } catch (FileNotFoundException e1) {
            logger_util
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

    protected synchronized void saveAwaitedJobsToFile() throws FileNotFoundException {
        if (statusFile != null && statusFile.isFile())
            statusFile.delete();

        XMLEncoder encoder = new XMLEncoder(new FileOutputStream(statusFile));

        for (AwaitedJob aj : awaitedJobs.values()) {
            encoder.writeObject(aj);
        }
        encoder.flush();
        encoder.close();
    }

    /**
     * This is called by the Scheduler when the state of job changes because
     * this object implements {@link SchedulerEventListener}
     */
    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        super.jobStateUpdatedEvent(notification);
        update(notification);
    }

    /**
     * Check if the job concerned by this notification is awated. Retrieve
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
            logger_util.error("Could not retreive output data for job " + id, e);
        } catch (UnknownJobException e) {
            logger_util.error("Could not retreive output data for job " + id, e);
        } catch (PermissionException e) {
            logger_util.error("Could not retreive output data for job " + id +
                ". Did you connect with a diffrent user ? ", e);
        }

        if (jobState == null) {
            logger_util.warn("The job " + id +
                " is listed as awaited but is unknown bby the scheduler. It will be removed from local list");
            removeAwaitedJob(id.toString());
        }

        JobStatus status = jobState.getStatus();
        switch (status) {
            case KILLED: {
                logger_util.info("The job " + id + "has been killed. No data will be transfered");
                break;
            }
            case FINISHED: {
                logger_util.info("Transfering data for finished job " + id);
                pullData(aj);
                break;
            }
            case CANCELED: {
                logger_util.info("Transfering data for canceled job " + id);
                pullData(aj);
                break;
            }
            case FAILED: {
                logger_util.info("Transfering data for failed job " + id);
                pullData(aj);
                break;
            }
        }
    }

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

    protected synchronized void addAwaitedJob(AwaitedJob aj) {
        this.awaitedJobs.put(aj.getJobId(), aj);
        try {
            this.saveAwaitedJobsToFile();
        } catch (FileNotFoundException e) {
            logger_util.error("Could not save status file after adding job on awaited jobs list " +
                aj.getJobId());
        } catch (IOException e) {
            logger_util.error("Could not save status file after adding job on awaited jobs list " +
                aj.getJobId(), e);
        }
    }

    protected synchronized void removeAwaitedJob(String id) {
        this.awaitedJobs.remove(id);

        try {
            this.saveAwaitedJobsToFile();
        } catch (FileNotFoundException e) {
            logger_util.error("Could not save status file after removing job " + id, e);
        } catch (IOException e) {
            logger_util.error("Could not save status file after removing job " + id, e);
        }
    }

}
