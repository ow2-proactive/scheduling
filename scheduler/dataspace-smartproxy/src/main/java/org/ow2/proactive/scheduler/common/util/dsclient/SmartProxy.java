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
package org.ow2.proactive.scheduler.common.util.dsclient;

/**
 * AbstractSchedulerProxyWithDSupport
 *
 * @author The ProActive Team
 */

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.security.auth.login.LoginException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
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
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;


/**
 * A Proxy to the Scheduler with built-in support for automatic data pushing and
 * pulling in order to provide disconnected mode for the dataspace layer.
 * <p/>
 * This implementation assumes that:
 * <p/>
 * <ul>
 * <li>the client application needs to submit to the scheduler jobs which
 * require data transfer to the execution nodes</li>
 * <p/>
 * <li>the input data is accessible by the client on the local file system (or
 * on location accessible on NFS)</li>
 * <p/>
 * <li>the output data is to be copied to the local file system (or on location
 * accessible on NFS)</li>
 * <p/>
 * <li>the local file system is not visible from the computation nodes side</li>
 * <p/>
 * *
 * <li>There is a location (let's call it SHARED_INPUT_LOCATION), for transferring
 * input data, accessible from both sides, client side and computation node
 * side. Same for output data (let's call it SHARED_OUTPUT_LOCATION). These
 * locations could be the same. It might be a shared folder or a data server.
 * <p/>
 * Let´s call push_url the url used by the client application in order to push
 * the input data to SHARED_INPUT_LOCATION.
 * <p/>
 * Let´s call pull_url the url used by the client application in order to pull
 * the output data from SHARED_OUTPUT_LOCATION.
 * <p/>
 * The job needs to specify, as input space, an url pointing to
 * SHARED_INPUT_LOCATION. The job needs to specify, as output space, an url
 * pointing to SHARED_OUTPUT_LOCATION. These urls might or not be the same as
 * push_url and pull_url.
 * </li>
 * <p/>
 * </ul>
 * <p/>
 * <p/>
 * The client application will use this Proxy for communicating with the
 * Scheduler. This Proxy is an ActiveObject.
 * <p/>
 * In order to use this object, a reference to it should be obtained via the
 * {@link #getActiveInstance()} method. One of the init methods should be called
 * afterwards.
 * <p/>
 * The client could add a Listener to this object in order to receive
 * notifications from the Scheduler. The listener is of type
 * {@link ISchedulerEventListenerExtended} which, in addition to the
 * notifications declared by its super type {@link org.ow2.proactive.scheduler.common.SchedulerEventListener}, can
 * be notified with events related to data transfer.
 * <p/>
 * Remember this is an active object. The listener object needs to be an active
 * or remote object (in order to avoid passing the listener through deep copy).
 * You could use, for instance:
 * <p/>
 * {@code
 * ISchedulerEventListenerExtended myListenerRemoteReference = PARemoteObject.turnRemote( new MyEventListener());
 * schedProxy.addEventListener(myListenerRemoteReference);
 * }
 * <p/>
 * <p/>
 * When a listener is added by the client, no new connection will be established
 * with the scheduler. This Proxy object broadcasts events received from the
 * Scheduler to its own listeners. In addition, it adds events related to data
 * transfer.
 * <p/>
 * When a new job is submitted, these operations will be performed:
 * <p/>
 * <ul>
 * <li>A temporary folder, for this execution, is created on the
 * SHARED_INPUT_LOCATION and the
 * SHARED_OUTPUT_LOCATION (if SHARED_INPUT_LOCATION!=SHARED_OUTPUT_LOCATION).</li>
 * <p/>
 * <li>The job INPUT_SPACE and OUTPUT_SPACE urls are updated with the new
 * created temporary folders.</li>
 * <p/>
 * <p/>
 * <li>The input data is pushed, via the push_url, from the local file system,
 * to the temporary folder</li>
 * <p/>
 * <li>The job is added to a list of awaited jobs, in order to pull the output
 * data once the job is finished. This list is persisted on disk and will be
 * restored after an application restart.</li>
 * <p/>
 * <li>When the job is finished, the output data is pulled form the
 * TMP_OUTPUT_LOCATION</li>
 * <p/>
 * <p/>
 * <li>The client application will be notified, via the listener, about the
 * evolution of the submitted jobs and about data transfer operations.</li>
 * <p/>
 * </ul>
 * <p/>
 * Each time this object is initialized, it recovers the awaited_jobs list from
 * the persisted file and, for each finished job, it pulls the output data from
 * the TMP_OUTPUT_LOCATION to the local file system
 *
 * @author esalagea
 */
public class SmartProxy extends SchedulerProxyUserInterface implements InitActive, EndActive,
        SchedulerEventListener {

    private static final long serialVersionUID = 60L;

    public static final String GENERIC_INFO_INPUT_FOLDER_PROPERTY_NAME = "client_input_data_folder";
    public static final String GENERIC_INFO_OUTPUT_FOLDER_PROPERTY_NAME = "client_output_data_folder";

    public static final String GENERIC_INFO_PUSH_URL_PROPERTY_NAME = "push_url";
    public static final String GENERIC_INFO_PULL_URL_PROPERTY_NAME = "pull_url";

    public static final int MAX_NB_OF_DATA_TRANSFER_THREADS = 20;

    /**
     * Thread factory for data transfer operations
     */
    protected transient ThreadFactory tf = new NamedThreadFactory("Data Transfer Thread");

    final protected transient ExecutorService tpe = Executors.newFixedThreadPool(
            MAX_NB_OF_DATA_TRANSFER_THREADS, tf);

    private transient JobDB jobDB = new JobDB();

    /**
     * last url used to connect to the scheduler
     */
    private transient String lastSchedUrl = null;

    /**
     * last creddata used to connect to the scheduler
     */
    private transient CredData lastCredData = null;

    /**
     * last credentials used to connect to the scheduler
     */
    private transient Credentials lastCredentials = null;

    /**
     * ProActive stup on this activeobject
     */
    private SmartProxy stubOnThis;

    /*
     * ProActive singleton pattern
     */
    private transient static SmartProxy activeInstance;

    /**
     * body of the instance
     */
    private transient Body bodyOnThis;

    /**
     * Thread of this active object's service
     */
    private transient Thread serviceThread;

    /*
     * Standard singleton pattern
     */
    private transient static SmartProxy stdInstance;

    /**
     * listeners registered to this proxy
     */
    protected transient Set<ISchedulerEventListenerExtended> eventListeners = Collections
            .synchronizedSet(new HashSet<ISchedulerEventListenerExtended>());

    /**
     * Returns a stub to the only active instance of the proxy (proactive singleton pattern)
     *
     * @return instance of the proxy
     * @throws org.objectweb.proactive.ActiveObjectCreationException
     * @throws org.objectweb.proactive.core.node.NodeException
     */
    public static synchronized SmartProxy getActiveInstance() throws ActiveObjectCreationException,
            NodeException {
        // we check if the activeInstance exists and is alive (otherwise we create a new one)
        if ((activeInstance != null) && (activeInstance.bodyOnThis != null) &&
            activeInstance.bodyOnThis.isActive())
            return activeInstance;
        stdInstance = getInstance();
        activeInstance = PAActiveObject.turnActive(stdInstance);

        return activeInstance;
    }

    /**
     * Returns the real singleton instance of the proxy
     *
     * @return instance of the proxy
     */
    public static synchronized SmartProxy getInstance() {

        if (stdInstance != null)
            return stdInstance;

        stdInstance = new SmartProxy();
        return stdInstance;
    }

    /**
     * Cleans the job database
     */
    public void cleanDatabase() {
        jobDB.cleanDataBase();
    }

    /**
     * This method forcefully terminates the activity of the proxy
     * This method should not be called via a proactive stub
     */
    public void terminateFast() {
        // if the service thread is locked on a user-level Thread.sleep() :
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
        jobDB.close();
    }

    public void setSessionName(String name) {
        jobDB.setSessionName(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see #init(String url, String user, String pwd)
     */
    @Override
    public void init(String url, String user, String pwd) throws SchedulerException, LoginException {
        CredData cred = new CredData(CredData.parseLogin(user), CredData.parseDomain(user), pwd);
        init(url, cred);
    }

    /*
     * (non-Javadoc)
     *
     * @see #init(String url, Credentials credentials)
     */
    @Override
    public void init(String url, Credentials credentials) throws SchedulerException, LoginException {
        // then we call super.init() which will create the connection to the
        // scheduler and subscribe as event listener
        super.init(url, credentials);
        this.lastSchedUrl = url;
        this.lastCredentials = credentials;
        this.lastCredData = null;
        // now we can can check if we need to transfer any data
        jobDB.loadJobs();

        registerAsListener();
        syncAwaitedJobs();
    }

    /*
     * (non-Javadoc)
     *
     * @see #init(String url, Credentials credentials)
     */
    @Override
    public void init(String url, CredData credData) throws SchedulerException, LoginException {
        //  we call super.init() which will create the connection to the
        // scheduler and subscribe as event listener
        super.init(url, credData);
        this.lastSchedUrl = url;
        this.lastCredentials = null;
        this.lastCredData = credData;
        // now we can can check if we need to transfer any data
        jobDB.loadJobs();
        registerAsListener();
        syncAwaitedJobs();
    }

    private void registerAsListener() throws NotConnectedException, PermissionException {
        super.addEventListener(stubOnThis, true, SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.JOB_PENDING_TO_FINISHED,
                SchedulerEvent.JOB_PAUSED, SchedulerEvent.JOB_RESUMED,
                SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.KILLED, SchedulerEvent.SHUTDOWN,
                SchedulerEvent.SHUTTING_DOWN, SchedulerEvent.STOPPED, SchedulerEvent.RESUMED,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED);
    }

    @Override
    public void initActivity(Body body) {
        stubOnThis = (SmartProxy) PAActiveObject.getStubOnThis();
        bodyOnThis = PAActiveObject.getBodyOnThis();
        serviceThread = Thread.currentThread();
    }

    @Override
    public void endActivity(Body body) {
        jobDB.close();
    }

    /**
     * Try to reconnect to a previously connected scheduler. The same url and credentials used during the previous connection will be reused.
     */
    public void reconnect() throws SchedulerException, LoginException {
        if (this.lastSchedUrl == null) {
            throw new IllegalStateException("No connection to the scheduler has been established yet.");
        }
        disconnect();
        if (this.lastCredentials == null) {
            super.init(lastSchedUrl, lastCredData);
        } else {
            super.init(lastSchedUrl, lastCredentials);
        }
        registerAsListener();
        jobDB.loadJobs();
        syncAwaitedJobs();
    }

    public void disconnect() throws PermissionException {
        try {
            super.disconnect();
        } catch (NotConnectedException e) {
            // we ignore this exception
        } catch (PermissionException e) {
            throw e;
        } catch (Exception e) {
            // we ignore any runtime exception
        }
    }

    /**
     * This method will synchronize this proxy with a remote Scheduler, it will contact the scheduler and checks the current state of every job being handled.
     * It is called either during the proxy initialization, or after a manual reconnection.
     */
    protected void syncAwaitedJobs() {
        // we make a copy of the awaitedJobsIds set in order to iterate over it.
        Set<String> awaitedJobsIds = jobDB.getAwaitedJobsIds();
        for (String id : awaitedJobsIds) {
            syncAwaitedJob(id);
        }
    }

    /**
     * This method will synchronize this proxy with a remote Scheduler for the given job
     *
     * @param id jobId
     */
    protected void syncAwaitedJob(String id) {

        AwaitedJob awaitedJob = jobDB.getAwaitedJob(id);

        try {
            JobState js = uischeduler.getJobState(id);

            for (TaskState ts : js.getTasks()) {
                String tname = ts.getName();
                AwaitedTask at = awaitedJob.getAwaitedTask(tname);
                if ((at != null) && (!at.isTransferring())) {
                    TaskResult tres = null;
                    try {
                        tres = uischeduler.getTaskResult(id, tname);
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
                        logger.error("Unexpected error while getting the output data for task " + tname +
                            " of job " + id, e);
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
            jobDB.removeAwaitedJob(id);
        } catch (PermissionException e) {
            logger
                    .error(
                            "Could not retrieve output data for job " +
                                id +
                                " because you don't have permmission to access this job. You need to use the same connection credentials you used for submitting the job.  \n Another attempt to dowload the output data for this job will be made next time the application is initialized. ",
                            e);
        }

    }

    @Override
    public JobId submit(Job job) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        return super.submit(job);
    }

    /**
     * Convenient shortcut for submit(job, localInputFolderPath, localOutputFolderPath, isolateTaskOutputs, automaticTransfer)
     * @param job                   job object to be submitted to the Scheduler Server for
     *                              execution
     * @param localInputFolderPath  path to the folder containing the input data for this job
     * @param localOutputFolderPath path to the folder where the output data produced by tasks in
     *                              this job should be copied
     * @param isolateTaskOutputs    isolate the output produced by each task in a dedicated subfolder.
     *                              It will not be possible to reuse a file produced by a task as an input of a latter task,
     *                              but we guaranty this way that their will be no overlapping of files produced by parallel tasks.
     * @param automaticTransfer     when this is set to true, the transfer or files between the pull_url shared space to
     *                              the local machine will be done automatically by the proxy. Notifications according to
     *                              the ISchedulerEventListenerExtended interface will be sent to the listeners upon transfer
     *                              completion or failure.
     */
    public JobId submit(TaskFlowJob job, String localInputFolderPath, String localOutputFolderPath,
            boolean isolateTaskOutputs, boolean automaticTransfer) throws NotConnectedException,
            PermissionException, SubmissionClosedException, JobCreationException, FileSystemException {
        return submit(job, localInputFolderPath, null, localOutputFolderPath, null, isolateTaskOutputs,
                automaticTransfer);
    }

    /**
     * Does the following steps:
     * <ul>
     * <li>Prepares the temporary folders.
     * </li>
     * <p/>
     * <li>pushes all files from localInputFolderPath to the push_url location
     * <p/>
     * <li>submits the job to the scheduler</li>
     * <p/>
     * <li>adds the job to the awaited jobs list in order to download the output
     * data when the job is finished</li>
     * <p/>
     * </ul>
     * <p/>
     * Note: this method is synchronous. The caller will be blocked until the
     * the data is pushed and the job submitted.
     *
     * @param job                   job object to be submitted to the Scheduler Server for
     *                              execution
     * @param localInputFolderPath  path to the folder containing the input data for this job
     * @param push_url              the url where input data is to be pushed before the job
     *                              submission. if push_url is null, the USER space will be used
     * @param localOutputFolderPath path to the folder where the output data produced by tasks in
     *                              this job should be copied
     * @param pull_url              the url where the data is to be retrieved after the job is
     *                              finished. if push_url is null, the USER space will be used
     * @param isolateTaskOutputs    isolate the output produced by each task in a dedicated subfolder.
     *                              It will not be possible to reuse a file produced by a task as an input of a latter task,
     *                              but we guaranty this way that their will be no overlapping of files produced by parallel tasks.
     * @param automaticTransfer     when this is set to true, the transfer or files between the pull_url shared space to
     *                              the local machine will be done automatically by the proxy. Notifications according to
     *                              the ISchedulerEventListenerExtended interface will be sent to the listeners upon transfer
     *                              completion or failure.
     * @return id of the job created
     * @throws org.ow2.proactive.scheduler.common.exception.JobCreationException
     * @throws org.ow2.proactive.scheduler.common.exception.SubmissionClosedException
     * @throws org.ow2.proactive.scheduler.common.exception.PermissionException
     * @throws org.ow2.proactive.scheduler.common.exception.NotConnectedException
     * @throws org.apache.commons.vfs2.FileSystemException
     */
    public JobId submit(TaskFlowJob job, String localInputFolderPath, String push_url,
            String localOutputFolderPath, String pull_url, boolean isolateTaskOutputs,
            boolean automaticTransfer) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException, FileSystemException {

        if ((push_url == null) || (push_url.equals(""))) {
            push_url = super.getUserSpaceURIs().get(0);
        }
        if ((pull_url == null) || (pull_url.equals(""))) {
            pull_url = super.getUserSpaceURIs().get(0);
        }

        String newFolderName = createNewFolderName();
        String push_Url_update = prepareJobInput(job, localInputFolderPath, push_url, newFolderName);
        String pull_url_update = prepareJobOutput(job, localOutputFolderPath, pull_url, newFolderName,
                isolateTaskOutputs);

        pushData(job, localInputFolderPath);
        JobId id = null;
        try {
            id = super.submit(job);
        } catch (NotConnectedException e) {
            removeJobIO(job, push_url, pull_url, newFolderName);
            throw e;
        } catch (PermissionException e) {
            removeJobIO(job, push_url, pull_url, newFolderName);
            throw e;
        } catch (SubmissionClosedException e) {
            removeJobIO(job, push_url, pull_url, newFolderName);
            throw e;
        } catch (JobCreationException e) {
            removeJobIO(job, push_url, pull_url, newFolderName);
            throw e;
        } catch (RuntimeException e) {
            removeJobIO(job, push_url, pull_url, newFolderName);
            throw e;
        }

        HashMap<String, AwaitedTask> ats = new HashMap<String, AwaitedTask>();
        for (Task t : job.getTasks()) {
            ats.put(t.getName(), new AwaitedTask(t.getName(), t.getOutputFilesList()));
        }

        AwaitedJob aj = new AwaitedJob(id.toString(), localInputFolderPath, job.getInputSpace(),
            push_Url_update, localOutputFolderPath, job.getOutputSpace(), pull_url_update,
            isolateTaskOutputs, automaticTransfer, ats);

        jobDB.putAwaitedJob(id.toString(), aj);
        return id;
    }

    /**
     * This method will create a remote folder for output of this
     * job and update the outputSpace job property. If the localOutputFolder
     * parameter is null, or pull_url no action will be performed concerning
     * this job's output.
     * <p/>
     * <p/>
     * We suppose there is file storage accessible by the client application as
     * well as the tasks on the computation nodes.
     * <p/>
     * This storage could be different for input and for output.
     * <p/>
     * <p/>
     * This output storage can be accessed, by the client application, using the
     * pull_url and by the tasks on the nodes using the job's output space url.
     * <p/>
     * <p/>
     * Prepare Output Data Transfer
     * <p/>
     * A folder will be created at pull_url/NewFolder/output (which, from the
     * nodes side, is job.OutputSpace/NewFolder/output).
     * <p/>
     * The OutputSpace property of the job will be changed to the new location.
     * job.OutputSpace = job.OutputSpace/NewFolder/output
     * <p/>
     * A generic information will be attached to the job containing the local
     * output folder path
     * If the option isolateTaskOutputs is set, a subfolder of "output" named "[TASKID]" will be created, it will behave as a tag to tell the
     * TaskLauncher to create a subfolder with the real taskid when the task is executed.
     *
     * @param job
     * @param localOutputFolder  path to the output folder on local machine if null, no actions
     *                           will be performed concerning the output data for this job
     * @param pull_url           - the url where the data is to be retrieved after the job is
     *                           finished
     * @param newFolderName      name of the folder to be used for pushing the output
     * @param isolateTaskOutputs task output isolation (see method submit)
     * @return a String representing the updated value of the pull_url
     * @throws org.apache.commons.vfs2.FileSystemException
     */
    protected String prepareJobOutput(TaskFlowJob job, String localOutputFolder, String pull_url,
            String newFolderName, boolean isolateTaskOutputs) throws FileSystemException {
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
                // at the end we add the [TASKID] pattern without creating the folder
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
     * This method will create a remote folder for the input data of this
     * job and update the inputSpace job property.
     * If the  localInputFolder parameter is null, or push_url is null, no action will
     * be performed concerning this job's input.
     * <p/>
     * <p/>
     * We suppose there is file storage accessible by the client application as
     * well as the tasks on the computation nodes.
     * <p/>
     * This storage could be different for input and for output.
     * <p/>
     * The input storage can be accessed, by the client application, using the
     * push_url and by the tasks on the nodes using the job's input space url.
     * <p/>
     * <p/>
     * Prepare Input Data Transfer
     * <p/>
     * A folder will be created at push_url/newFolderName/input (which, from the
     * nodes side, is the job.InputSpace/newFolderName/input) . The InputSpace
     * property of the job will be changed to the new location. job.InputSpace =
     * job.InputSpace/NewFolder/input
     * <p/>
     * <p/>
     * A generic information will be attached to the job containing the local
     * input folder path.
     *
     * @param job
     * @param localInputFolder path to the input folder on local machine if null, no actions
     *                         will be performed concerning the input data for this job
     * @param push_url         the url where input data is to be pushed before the job
     *                         submission
     * @param newFolderName    name of the new folder to be created
     * @return String representing the updated value of the push_url
     * @throws org.apache.commons.vfs2.FileSystemException
     */
    protected String prepareJobInput(Job job, String localInputFolder, String push_url, String newFolderName)
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
            push_url_updated = push_url + "/" + inputFolder;
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

    protected void removeJobIO(Job job, String push_url, String pull_url, String newFolderName)
            throws FileSystemException {
        String push_url_updated = push_url + "/" + newFolderName;
        FileObject fo = jobDB.resolveFile(push_url_updated);
        try {

            fo.delete(Selectors.SELECT_ALL);
            fo.delete();
        } catch (Exception e) {
            logger.debug("Error in removeJobIO push for job " + job.getName());
        }
        String pull_url_updated = pull_url + "/" + newFolderName;
        fo = jobDB.resolveFile(pull_url_updated);
        try {
            fo.delete(Selectors.SELECT_ALL);
            fo.delete();
        } catch (Exception e) {
            logger.debug("Error in removeJobIO pull for job " + job.getName());
        }
    }

    protected void createFolder(String fUri) throws FileSystemException {

        FileObject fo = jobDB.resolveFile(fUri);
        fo.createFolder();

        logger.debug("Created remote folder: " + fUri);
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

    /**
     * Push the input files of the given job from the local input folder to the push_url
     *
     * @param job                  job to push data for
     * @param localInputFolderPath local input folder
     * @return
     * @throws org.apache.commons.vfs2.FileSystemException
     */
    protected boolean pushData(TaskFlowJob job, String localInputFolderPath) throws FileSystemException {

        String push_URL = job.getGenericInformations().get(GENERIC_INFO_PUSH_URL_PROPERTY_NAME);

        if ((push_URL == null) || (push_URL.trim().equals(""))) {
            return false;
        }// push inputData

        // TODO - if the copy fails, try to remove the files from the remote
        // folder before throwing an exception
        FileObject remoteFolder = jobDB.resolveFile(push_URL);
        FileObject localfolder = jobDB.resolveFile(localInputFolderPath);
        String jname = job.getName();
        logger.debug("Pushing files for job " + jname + " from " + localfolder + " to " + remoteFolder);

        List<DataTransferProcessor> transferCallables = new ArrayList<DataTransferProcessor>();
        TaskFlowJob tfj = job;
        for (Task t : tfj.getTasks()) {
            logger.debug("Pushing files for task " + t.getName());
            List<InputSelector> inputFileSelectors = t.getInputFilesList();
            //create the selector
            DSFileSelector fileSelector = new DSFileSelector();
            for (InputSelector is : inputFileSelectors) {
                org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector fs = is.getInputFiles();
                if (fs.getIncludes() != null)
                    fileSelector.addIncludes(Arrays.asList(fs.getIncludes()));

                if (fs.getExcludes() != null)
                    fileSelector.addExcludes(Arrays.asList(fs.getExcludes()));

                //We should check if a pattern exist in both includes and excludes. But that would be a user mistake.
            }
            DataTransferProcessor dtp = new DataTransferProcessor(localfolder, remoteFolder, tfj.getName(), t
                    .getName(), fileSelector);
            transferCallables.add(dtp);
        }

        List<Future<Boolean>> futures = null;
        try {
            futures = tpe.invokeAll(transferCallables);
        } catch (InterruptedException e) {
            logger.error("Interrupted while transferring files of job " + jname, e);
            throw new RuntimeException(e);
        }
        for (int i = 0; i < futures.size(); i++) {
            Future<Boolean> answer = futures.get(i);
            String tname = tfj.getTasks().get(i).getName();
            try {
                if (!answer.get()) {
                    // this should not happen
                    throw new RuntimeException("Files of task " + tname + " for job " + jname +
                        " could not be transferred");
                }
            } catch (InterruptedException e) {
                logger
                        .error("Interrupted while transferring files of task " + tname + " for job " + jname,
                                e);
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                logger.error("Exception occured while transferring files of task " + tname + " for job " +
                    jname, e);
                throw new RuntimeException(e);
            }
        }

        logger.debug("Finished push operation from " + localfolder + " to " + remoteFolder);
        return true;
    }

    /**
     * Pull the output files of the given task from the pull_url either to the localFolder defined for the job or to the localFolder specified as argument, if it is not null.
     * The call to the method pullData is triggered by the user. If the job is configured to be handled asynchronously, call to this method will trigger a RuntimeException.
     *
     * @param jobId       job to pull data for
     * @param t_name      name of the task
     * @param localFolder local output folder, if not null, it overrides the folder specified as output folder for the job
     * @return
     * @throws org.apache.commons.vfs2.FileSystemException if there is a problem during the transfer
     */
    public void pullData(String jobId, String t_name, String localFolder) throws FileSystemException {

        AwaitedJob awaitedjob = jobDB.getAwaitedJob(jobId);
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
        pullDataInternal(awaitedjob, jobId, t_name, localOutFolderPath);
    }

    /**
     * Internal version of pullData, will use a separate Thread to transfer files if automaticTransfer is set to true
     *
     * @param awaitedjob  job to handle
     * @param jobId       job id
     * @param t_name      task name
     * @param localFolder local folder to copy files to or null if we use the job's local folder
     * @throws org.apache.commons.vfs2.FileSystemException
     */
    public void pullDataInternal(AwaitedJob awaitedjob, String jobId, String t_name, String localFolder)
            throws FileSystemException {

        AwaitedTask atask = awaitedjob.getAwaitedTask(t_name);
        if (atask == null) {
            throw new IllegalArgumentException("The task " + t_name + " does not belong to job " + jobId +
                " or has already been removed");
        }
        if (atask.isTransferring()) {
            logger.warn("The task " + t_name + " of job " + jobId + " is already transferring its output");
            return;
        }
        String pull_URL = awaitedjob.getPullURL();

        if (awaitedjob.isIsolateTaskOutputs()) {
            pull_URL = pull_URL.replace(SchedulerConstants.TASKID_DIR_DEFAULT_NAME,
                    SchedulerConstants.TASKID_DIR_DEFAULT_NAME + "/" + atask.getTaskId());
        }

        FileObject remotePullFolderFO = null;
        FileObject localfolderFO = null;

        try {
            remotePullFolderFO = jobDB.resolveFile(pull_URL);

            localfolderFO = jobDB.resolveFile(localFolder);
        } catch (FileSystemException e) {
            logger.error("Could not retrieve data for job " + jobId, e);
            throw new IllegalStateException("Could not retrieve data for job " + jobId, e);
        }

        String sourceUrl = remotePullFolderFO.getURL().toString();
        String destUrl = localfolderFO.getURL().toString();
        //create the selector
        DSFileSelector fileSelector = new DSFileSelector();

        List<OutputSelector> ouputFileSelectors = atask.getOutputSelectors();
        for (OutputSelector os : ouputFileSelectors) {
            org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector fs = os.getOutputFiles();
            if (fs.getIncludes() != null)
                fileSelector.addIncludes(Arrays.asList(fs.getIncludes()));

            if (fs.getExcludes() != null)
                fileSelector.addExcludes(Arrays.asList(fs.getExcludes()));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Looking at files in " + sourceUrl + " with " + fileSelector.getIncludes() + "-" +
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

                    }
                }
            }

            if (fos != null && fos.length > 0) {
                for (FileObject fo : fos) {
                    logger.debug("Found " + fo.getName());
                }
            } else {
                logger.warn("Couldn't find " + fileSelector.getIncludes() + "-" + fileSelector.getExcludes() +
                    " in " + sourceUrl);
            }
        }
        if (awaitedjob.isAutomaticTransfer()) {
            DataTransferProcessor dtp = new DataTransferProcessor(remotePullFolderFO, localfolderFO, jobId,
                t_name, fileSelector);
            jobDB.setTaskTransferring(jobId, t_name, true);
            tpe.submit((Runnable) dtp);
        } else {
            logger.debug("Copying files from " + sourceUrl + " to " + destUrl);
            localfolderFO.copyFrom(remotePullFolderFO, fileSelector);
            logger.debug("Finished copying files from " + sourceUrl + " to " + destUrl);
            // ok we can remove the task
            jobDB.removeAwaitedTask(jobId, t_name);
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
     * @param eventType the type of the event received.
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            ISchedulerEventListenerExtended l = it.next();
            try {
                l.schedulerStateUpdatedEvent(eventType);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }

    }

    /**
     * Invoked each time a new job has been submitted to the Scheduler and
     * validated.
     *
     * @param job the newly submitted job.
     */
    public void jobSubmittedEvent(JobState job) {
        Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            ISchedulerEventListenerExtended l = it.next();
            try {
                l.jobSubmittedEvent(job);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }
    }

    /**
     * Invoked each time the state of a job has changed.<br>
     * If you want to maintain an up to date list of jobs, just use the
     * {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.job.JobInfo)}
     * method to update the content of your job.
     *
     * @param notification the data composed of the type of the event and the information
     *                     that have change in the job.
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        updateJob(notification);
        Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            ISchedulerEventListenerExtended l = it.next();
            try {
                l.jobStateUpdatedEvent(notification);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }
    }

    /**
     * Invoked each time the state of a task has changed.<br>
     * In this case you can use the
     * {@link org.ow2.proactive.scheduler.common.job.JobState#update(org.ow2.proactive.scheduler.common.task.TaskInfo)}
     * method to update the content of the designated task inside your job.
     *
     * @param notification the data composed of the type of the event and the information
     *                     that have change in the task.
     */
    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        updateTask(notification);
        Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            ISchedulerEventListenerExtended l = it.next();
            try {
                l.taskStateUpdatedEvent(notification);
            } catch (Exception e) {
                // if an exception occurs we remove the listener
                it.remove();
            }
        }

    }

    /**
     * Invoked each time something change about users.
     *
     * @param notification the data composed of the type of the event and the data linked
     *                     to the change.
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
        while (it.hasNext()) {
            ISchedulerEventListenerExtended l = it.next();
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

        AwaitedJob aj = jobDB.getAwaitedJob(id.toString());

        if (aj == null)
            return;

        JobStatus status = ((NotificationData<JobInfo>) notification).getData().getStatus();
        switch (status) {
            case KILLED: {
                logger.debug("The job " + id + "has been killed.");
                jobDB.removeAwaitedJob(id.toString());
                break;
            }
            case FINISHED: {
                logger.debug("The job " + id + " is finished.");
                //removeAwaitedJob(id.toString());
                break;
            }
            case CANCELED: {
                logger.debug("The job " + id + " is canceled.");
                jobDB.removeAwaitedJob(id.toString());
                break;
            }
            case FAILED: {
                logger.debug("The job " + id + " is failed.");
                //removeAwaitedJob(id.toString());
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
    protected void updateTask(NotificationData<?> notification) {

        // am I interested in this task?
        JobId id = ((NotificationData<TaskInfo>) notification).getData().getJobId();
        String tname = ((NotificationData<TaskInfo>) notification).getData().getName();
        TaskId tid = ((NotificationData<TaskInfo>) notification).getData().getTaskId();
        TaskStatus status = ((NotificationData<TaskInfo>) notification).getData().getStatus();

        AwaitedJob aj = jobDB.getAwaitedJob(id.toString());

        if (aj == null)
            return;

        AwaitedTask at = aj.getAwaitedTask(tname);

        if (at == null)
            return;

        at.setTaskId(tid.toString());
        jobDB.putAwaitedJob(id.toString(), aj);

        switch (status) {
            case ABORTED:
            case NOT_RESTARTED:
            case NOT_STARTED:
            case SKIPPED: {
                logger.debug("The task " + tname + " from job " + id +
                    " couldn't start. No data will be transfered");
                jobDB.removeAwaitedTask(id.toString(), tname);
                break;
            }
            case FINISHED: {
                logger.debug("The task " + tname + " from job " + id + " is finished.");
                if (aj.isAutomaticTransfer()) {
                    logger.debug("Transferring data for finished task " + tname + " from job " + id);
                    try {
                        pullDataInternal(aj, id.toString(), tname, aj.getLocalOutputFolder());
                    } catch (FileSystemException e) {
                        logger.error("Error while handling data for finished task " + tname + " for job " +
                            id + ", task will be removed");
                        jobDB.removeAwaitedTask(id.toString(), tname);
                    }
                }
                break;
            }
            case FAULTY: {
                logger.debug("The task " + tname + " from job " + id + " is faulty.");
                if (aj.isAutomaticTransfer()) {
                    logger.debug("Transfering data for failed task " + tname + " from job " + id);
                    try {
                        pullDataInternal(aj, id.toString(), tname, aj.getLocalOutputFolder());
                    } catch (FileSystemException e) {
                        logger.error("Error while handling data for finished task " + tname + " for job " +
                            id + ", task will be removed");
                        jobDB.removeAwaitedTask(id.toString(), tname);
                    }
                }
                break;
            }
        }
    }

    /**
     * The method from jobDB is encapsulated in order to be called via the SmartProxy stub
     * @param jobId
     * @param taskName
     */
    protected void removeAwaitedTask(String jobId, String taskName) {
        jobDB.removeAwaitedTask(jobId, taskName);
    }

    // ******** Scheduler Event Listener *********************** //

    /**
     * Handles the transfer of data asynchronously, the run method is used when pulling data, the call method is used when pushing
     */
    private class DataTransferProcessor implements Runnable, Callable<Boolean> {
        private FileObject source;
        private FileObject dest;
        private String jobId;
        private String taskName;
        private FileSelector fileSelector;
        private String sourceUrl;
        private String destUrl;

        /**
         * @param source source folder
         * @param dest   dest folder
         * @param _jobId - only used for pull operations. For push operations, the
         *               jobId is null
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
            logger.debug("Copying files of task " + taskName + " of job " + jobId + " from " + source +
                " to " + dest);
            if (logger.isDebugEnabled()) {

                FileObject[] fos = source.findFiles(fileSelector);
                for (FileObject fo : fos) {
                    logger.debug("Found " + fo.getName());
                }

            }
            dest.copyFrom(source, fileSelector);
            logger.debug("Finished copying files of task " + taskName + " of job " + jobId + " from " +
                source + " to " + dest);
        }

        @Override
        public void run() {

            try {
                transfer();

            } catch (Exception e) {
                logger.error("An error occurred while copying files of task " + taskName + " of job " +
                    jobId + " from " + source + " to " + dest, e);

                logger
                        .warn("Task " +
                            taskName +
                            " of job " +
                            jobId +
                            " will be removed from the known task list. The system will not attempt again to retrieve data for this task. You could try to manually copy the data from the location  " +
                            sourceUrl);

                Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
                while (it.hasNext()) {
                    ISchedulerEventListenerExtended l = it.next();
                    try {
                        l.pullDataFailed(jobId, taskName, sourceUrl, e);
                    } catch (Exception e1) {
                        // if an exception occurs we remove the listener
                        it.remove();
                    }
                }
                stubOnThis.removeAwaitedTask(jobId, taskName);
                return;

            }// catch

            Iterator<ISchedulerEventListenerExtended> it = eventListeners.iterator();
            while (it.hasNext()) {
                ISchedulerEventListenerExtended l = it.next();
                try {
                    l.pullDataFinished(jobId, taskName, destUrl);
                } catch (Exception e1) {
                    // if an exception occurs we remove the listener
                    it.remove();
                }
            }
            stubOnThis.removeAwaitedTask(jobId, taskName);
        }

        @Override
        public Boolean call() throws Exception {
            try {

                transfer();

            } catch (Exception e) {
                logger.error("An error occured while copying files of task " + taskName + " of job " + jobId +
                    " from " + source + " to " + dest, e);
                throw e;
            }// catch

            return true;
        }
    }

}
