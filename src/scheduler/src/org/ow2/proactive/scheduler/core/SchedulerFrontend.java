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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.permissions.MethodCallPermission;
import org.ow2.proactive.policy.ClientsPolicy;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.MaxJobIdReachedException;
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
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.core.account.SchedulerAccountsManager;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.InternalJobWrapper;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.permissions.ChangePolicyPermission;
import org.ow2.proactive.scheduler.permissions.ChangePriorityPermission;
import org.ow2.proactive.scheduler.permissions.ConnectToResourceManagerPermission;
import org.ow2.proactive.scheduler.permissions.GetOwnStateOnlyPermission;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * Scheduler Front-end. This is the API to talk to when you want to managed a scheduler core.
 * Creating this class can only be done by using <code>SchedulerFactory</code>.
 * You can join this front-end by using the <code>join()</code> method
 * in {@link SchedulerConnection}.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@ActiveObject
public class SchedulerFrontend implements InitActive, SchedulerStateUpdate, Scheduler {

    /** Scheduler logger */
    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.FRONTEND);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.FRONTEND);
    public static final Logger logger_console = ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE);

    /** A repeated warning message */
    private static final String ACCESS_DENIED = "Access denied ! You are not connected or your session has expired !";

    /** Maximum duration of a session for a useless client */
    private static final long USER_SESSION_DURATION = PASchedulerProperties.SCHEDULER_USER_SESSION_TIME
            .getValueAsInt() * 1000;

    /** Stores methods that will be called on clients */
    private static Map<String, Method> eventMethods;

    /** Mapping on the UniqueId of the sender and the user/admin identifications */
    private Map<UniqueID, UserIdentificationImpl> identifications;

    /** Map that link uniqueID to user credentials */
    private Map<UniqueID, Credentials> credentials;

    /** List used to mark the user that does not respond anymore */
    private Set<UniqueID> dirtyList;

    /** Temporary rmURL at starting process */
    private URI rmURL;

    /** Authentication Interface */
    private SchedulerAuthentication authentication;

    /** Full name of the policy class */
    private String policyFullName;

    /** Implementation of scheduler main structure */
    private SchedulerCore scheduler;

    /** Direct link to the current job to submit. */
    private InternalJobWrapper currentJobToSubmit;

    /** Job identification management */
    private Map<JobId, IdentifiedJob> jobs;

    /** Session timer */
    private Timer sessionTimer;

    /** Users Statistics Manager */
    private SchedulerAccountsManager accountsManager;

    /** JMX Helper reference */
    private SchedulerJMXHelper jmxHelper;

    /** Scheduler state maintains by this class : avoid charging the core from some request */
    private SchedulerStateImpl sState;
    private Map<JobId, JobState> jobsMap;

    /* ########################################################################################### */
    /*                                                                                             */
    /* ################################## SCHEDULER CONSTRUCTION ################################# */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * ProActive empty constructor
     */
    public SchedulerFrontend() {
    }

    /**
     * Scheduler Front-end constructor.
     *
     * @param rmURL a started Resource Manager URL which
     *              be able to managed the resource used by scheduler.
     * @param policyFullClassName the full class name of the policy to use.
     */
    public SchedulerFrontend(URI rmURL, String policyFullClassName) {
        this.identifications = new HashMap<UniqueID, UserIdentificationImpl>();
        this.credentials = new HashMap<UniqueID, Credentials>();
        this.dirtyList = new HashSet<UniqueID>();
        this.currentJobToSubmit = new InternalJobWrapper();
        this.accountsManager = new SchedulerAccountsManager();
        this.jmxHelper = new SchedulerJMXHelper(this.accountsManager);
        this.jobsMap = new HashMap<JobId, JobState>();

        logger_dev.info("Creating scheduler Front-end...");
        this.rmURL = rmURL;
        this.policyFullName = policyFullClassName;
        logger_dev.debug("Policy used is " + policyFullClassName);
        this.jobs = new HashMap<JobId, IdentifiedJob>();
        this.sessionTimer = new Timer("SessionTimer");
        this.makeEventMethodsList();
    }

    /**
     * Make the event list once
     */
    private void makeEventMethodsList() {
        eventMethods = new HashMap<String, Method>();
        for (Method m : SchedulerEventListener.class.getMethods()) {
            eventMethods.put(m.getName(), m);
        }
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            // setting up the policy
            logger.info("Setting up scheduler security policy");
            ClientsPolicy.init();

            // creating the scheduler authentication interface.
            // if this fails then it will not continue.
            logger_dev.info("Creating scheduler authentication interface...");
            authentication = PAActiveObject.newActive(SchedulerAuthentication.class,
                    new Object[] { PAActiveObject.getStubOnThis() });
            //creating scheduler core
            logger_dev.info("Creating scheduler core...");
            SchedulerCore scheduler_local = new SchedulerCore(rmURL, (SchedulerFrontend) PAActiveObject
                    .getStubOnThis(), policyFullName, currentJobToSubmit);
            scheduler = (SchedulerCore) PAActiveObject.turnActive(scheduler_local);

            logger_dev.info("Registering scheduler...");
            PAActiveObject.registerByName(authentication, SchedulerConstants.SCHEDULER_DEFAULT_NAME);

            // Boot the JMX helper
            logger_dev.info("Booting jmx...");
            this.jmxHelper.boot(authentication);

            // run !!
        } catch (Exception e) {
            logger_console.error("", e);
            System.exit(1);
        }
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ################################### SCHEDULING MANAGEMENT ################################# */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * Called by the scheduler core to recover the front-end.
     * This method may have to rebuild the different list of userIdentification
     * and job/user association.
     * 
     * @param jobList the jobList that may appear in this front-end.
     */
    public void recover(SchedulerStateImpl sState) {
        //default state = started
        this.sState = sState;
        Set<JobState> jobStates = new HashSet<JobState>();
        logger_dev.info("#Pending jobs list : " + sState.getPendingJobs().size());
        logger_dev.info("#Running jobs list : " + sState.getRunningJobs().size());
        logger_dev.info("#Finished jobs list : " + sState.getFinishedJobs().size());

        for (JobState js : sState.getPendingJobs()) {
            prepare(jobStates, js, false);
        }
        for (JobState js : sState.getRunningJobs()) {
            prepare(jobStates, js, false);
        }
        for (JobState js : sState.getFinishedJobs()) {
            prepare(jobStates, js, true);
        }

        // rebuild JMX object
        this.jmxHelper.getSchedulerRuntimeMBean().recover(jobStates);
        //once recovered, activate scheduler communication
        authentication.setActivated(true);
    }

    /**
     * Prepare the job in the frontend
     *
     * @param jobStates a temporary set of jobs
     * @param js the current job to be prepared
     * @param finished if the job is finished or not
     */
    private void prepare(Set<JobState> jobStates, JobState js, boolean finished) {
        jobStates.add(js);
        UserIdentificationImpl uIdent = new UserIdentificationImpl(js.getOwner());
        IdentifiedJob ij = new IdentifiedJob(js.getId(), uIdent);
        jobs.put(js.getId(), ij);
        jobsMap.put(js.getId(), js);
        ij.setFinished(finished);
    }

    /**
     * Connect a new user on the scheduler.
     * This user can interact with the scheduler according to his right.
     *
     * @param sourceBodyID the source ID of the connected object representing a user
     * @param identification the identification of the connected user
     * @throws SchedulerException If an error occurred during connection with the front-end.
     */
    public void connect(UniqueID sourceBodyID, UserIdentificationImpl identification, Credentials cred)
            throws AlreadyConnectedException {
        if (identifications.containsKey(sourceBodyID)) {
            logger.warn("Active object already connected for this user :" + identification.getUsername());
            throw new AlreadyConnectedException("This active object is already connected to the scheduler !");
        }
        logger.info(identification.getUsername() + " successfully connected !");
        identifications.put(sourceBodyID, identification);
        credentials.put(sourceBodyID, cred);
        renewUserSession(sourceBodyID, identification);
        //add this new user in the list of connected user
        sState.getUsers().update(identification);
        //send events
        usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, identification));
    }

    /**
     * Create or renew the session (timer task) for the given user identification.
     * A call to this method will cancel the previous session (timerTask), 
     * create and schedule a new one and purge the timer.
     * 
     * @param id The unique ID of the user
     * @param identification the user on which to renew the session
     */
    private void renewUserSession(final UniqueID id, UserIdentificationImpl identification) {
        if (identifications.get(id).isListening()) {
            //if this id has a listener, do not renew user session
            return;
        }
        final String userName = identification.getUsername();
        TimerTask session = identification.getSession();
        if (session != null) {
            session.cancel();
        }
        identification.setSession(new TimerTask() {
            @Override
            public void run() {
                logger.info("End of session for user " + userName + ", id=" + id);
                disconnect(id);
            }
        });
        sessionTimer.purge();
        sessionTimer.schedule(identification.getSession(), USER_SESSION_DURATION);
    }

    /**
     * {@inheritDoc}
     */
    public JobId submit(Job userJob) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        logger_dev.info("New job submission requested : " + userJob.getName());

        UniqueID id = checkAccess();
        UserIdentificationImpl ident = checkPermission("submit",
                "You do not have permission to submit a job !");

        //check if the scheduler is stopped
        if (!scheduler.isSubmitPossible()) {
            String msg = "Scheduler is stopped, cannot submit job";
            logger_dev.info(msg);
            throw new SubmissionClosedException(msg);
        }
        //get the internal job.
        InternalJob job = InternalJobFactory.createJob(userJob, this.credentials.get(id));
        //setting job informations
        if (job.getTasks().size() == 0) {
            String msg = "This job does not contain Tasks !! Insert tasks before submitting job";
            logger_dev.info(msg);
            throw new JobCreationException(msg);
        }

        // if no GLOBAL spaces, reject a job that attempts to use it
        if (!PASchedulerProperties.DATASPACE_GLOBAL_URL.isSet()) {
            for (InternalTask it : job.getITasks()) {
                if (it.getInputFilesList() != null) {
                    for (InputSelector in : it.getInputFilesList()) {
                        if (in.getMode().equals(InputAccessMode.TransferFromGlobalSpace)) {
                            throw new JobCreationException(
                                "Use of GLOBAL SPACES is disabled in this Scheduler (INPUT for task: " +
                                    it.getName() + ")");
                        }
                    }
                }
                if (it.getOutputFilesList() != null) {
                    for (OutputSelector out : it.getOutputFilesList()) {
                        if (out.getMode().equals(OutputAccessMode.TransferToGlobalSpace)) {
                            throw new JobCreationException(
                                "Use of GLOBAL SPACES is disabled in this Scheduler (OUTPUT for task: " +
                                    it.getName() + ")");
                        }
                    }
                }
            }
        }

        //verifying that the user has right to set the given priority to his job.
        try {
            ident.checkPermission(new ChangePriorityPermission(job.getPriority().ordinal()), ident
                    .getUsername() +
                " does not have rights to set job priority " + job.getPriority());
        } catch (PermissionException ex) {
            logger_dev.info(ex.getMessage());
            throw ex;
        }
        logger_dev.info("Preparing and settings job submission");
        //setting the job properties
        try {
            job.setId(JobIdImpl.nextId(job.getName()));
        } catch (MaxJobIdReachedException e) {
            scheduler.stop();
            logger
                    .fatal("\n****************************************************************************************************\n"
                        + "****************************************************************************************************\n"
                        + "**                                                                                                **\n"
                        + "**  The maximum number of jobs that can be submitted has been reached !                           **\n"
                        + "**  To prevent from any problems, the Scheduler has been stopped,                                 **\n"
                        + "**  all running jobs will be terminated, no submit will be possible until restart.                **\n"
                        + "**  Database should be archived and clean before restarting the Scheduler                         **\n"
                        + "**  /!\\ Restarting the Scheduler without cleaning the DataBase implies some id to be duplicate    **\n"
                        + "**  This is not a critical problem but some finished jobs could be unreachable by the Scheduler.  **\n"
                        + "**                                                                                                **\n"
                        + "****************************************************************************************************\n"
                        + "****************************************************************************************************");
            throw new MaxJobIdReachedException(
                "The maximum number of jobs that can be submitted has been reached !\n"
                    + "To prevent from any problems, the Scheduler has been stopped, "
                    + "all running jobs will be terminated, no submit will be possible until restart.\n"
                    + "Please, contact your administrator !");
        }
        job.setOwner(ident.getUsername());
        //prepare tasks in order to be send into the core
        job.prepareTasks();
        //create job descriptor
        job.setJobDescriptor(new JobDescriptorImpl(job));
        //put the job inside the frontend management list
        jobs.put(job.getId(), new IdentifiedJob(job.getId(), ident));
        //statically reference the job to submit
        currentJobToSubmit.setJob(job);
        scheduler.submit();
        //increase number of submit for this user
        ident.addSubmit();
        //send update user event
        usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident));
        logger.info("New job submitted '" + job.getId() + "' containing " + job.getTotalNumberOfTasks() +
            " tasks (owner is '" + job.getOwner() + "')");
        return job.getId();
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public JobResult getJobResult(JobId jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {

        //checking permissions
        IdentifiedJob ij = checkJobOwner("getJobResult", jobId,
                "You do not have permission to get the result of this job !");

        if (!ij.isFinished()) {
            logger_dev.info("Job '" + jobId + "' is not finished");
            return null;
        }

        //asking the scheduler for the result
        JobResult result = scheduler.getJobResult(jobId);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {
        return this.getJobResult(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public TaskResult getTaskResult(JobId jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {

        //checking permissions
        checkJobOwner("getTaskResult", jobId,
                "You do not have permission to get the task result of this job !");

        //asking the scheduler for the result
        TaskResult result = scheduler.getTaskResult(jobId, taskName);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public TaskResult getTaskResult(String jobId, String taskName) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResult(JobIdImpl.makeJobId(jobId), taskName);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean killTask(JobId jobId, String taskName) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        //checking permissions
        checkJobOwner("killTask", jobId, "You do not have permission to kill this task !");
        return scheduler.killTask(jobId, taskName);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean killTask(String jobId, String taskName) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        return killTask(JobIdImpl.makeJobId(jobId), taskName);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean restartTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        //checking permissions
        checkJobOwner("restartTask", jobId, "You do not have permission to restart this task !");
        return scheduler.restartTask(jobId, taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean restartTask(String jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return restartTask(JobIdImpl.makeJobId(jobId), taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        //checking permissions
        checkJobOwner("preemptTask", jobId, "You do not have permission to preempt this task !");
        return scheduler.preemptTask(jobId, taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean preemptTask(String jobId, String taskName, int restartDelay) throws NotConnectedException,
            UnknownJobException, UnknownTaskException, PermissionException {
        return preemptTask(JobIdImpl.makeJobId(jobId), taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {

        //checking permissions
        checkJobOwner("removeJob", jobId, "You do not have permission to remove this job !");

        //asking the scheduler for the result
        return scheduler.removeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException {
        //checking permissions
        checkJobOwner("listenJobLogs", jobId, "You do not have permission to listen the log of this job !");

        scheduler.listenJobLogs(jobId, appenderProvider);
    }

    /**
     * {@inheritDoc}
     */
    public void listenJobLogs(String jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException {
        this.listenJobLogs(JobIdImpl.makeJobId(jobId), appenderProvider);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        //checking permissions
        checkPermission("getStatus", "You do not have permission to get the status !");

        return sState.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return getState(false);
    }

    /**
     * Check if the given user can get the state as it is demanded (full or user only)
     *
     * @param myOnly true, if the user wants only its events or jobs, false if user want the full state
     * @param ui the user identication
     * @throws PermissionException if permission is denied
     */
    private void checkOwnStatePermission(boolean myOnly, UserIdentificationImpl ui)
            throws PermissionException {
        ui.checkPermission(new GetOwnStateOnlyPermission(myOnly), ui.getUsername() +
            " does not have permissions to retrieve full state");
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        //checking permissions
        checkPermission("getState", "You do not have permission to get the state !");

        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());
        try {
            checkOwnStatePermission(myJobsOnly, ui);
            return myJobsOnly ? sState.filterOnUser(ui.getUsername()) : sState;
        } catch (PermissionException ex) {
            logger_dev.info(ex.getMessage());
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        addEventListener(sel, myEventsOnly, false, events);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            boolean getCurrentState, SchedulerEvent... events) throws NotConnectedException,
            PermissionException {
        //checking permissions
        UserIdentificationImpl uIdent = checkPermission("addEventListener",
                "You do not have permission to add a listener !");

        // check if listener is not null
        if (sel == null) {
            String msg = "Scheduler listener must be not null";
            logger_dev.info(msg);
            throw new IllegalArgumentException(msg);
        }
        // check if the listener is a reified remote object
        if (!MOP.isReifiedObject(sel)) {
            String msg = "Scheduler listener must be a remote object";
            logger_dev.info(msg);
            throw new IllegalArgumentException(msg);
        }

        //get the scheduler State
        SchedulerState currentState = null;
        if (getCurrentState) {
            //check get state permission is checked in getState method
            currentState = getState(myEventsOnly);
        } else {
            //check get state permission
            checkOwnStatePermission(myEventsOnly, uIdent);
        }
        //prepare user for receiving events
        uIdent.setUserEvents(events);
        //set if the user wants to get its events only or every events
        uIdent.setMyEventsOnly(myEventsOnly);
        //add the listener to the list of listener for this user.
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        uIdent.setListener(new ClientRequestHandler(this, id, sel));
        //cancel timer for this user : session is now managed by events
        uIdent.getSession().cancel();
        //return to the user
        return currentState;
    }

    /**
     * {@inheritDoc}
     */
    public void removeEventListener() throws NotConnectedException, PermissionException {
        //Remove the listener on that user designated by its given UniqueID,
        //then renew its user session as it is no more managed by the listener.
        UniqueID id = checkAccess();
        UserIdentificationImpl uIdent = identifications.get(id);
        uIdent.clearListener();
        //recreate the session for this user which is no more managed by listener
        renewUserSession(id, uIdent);
    }

    /**
     * Get the unique ID of the caller, check the access, and return the id.
     * 
     * @return the id of the caller if it is known
     * @throws NotConnectedException 'access denied' if the caller is not known
     */
    private UniqueID checkAccess() throws NotConnectedException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
            throw new NotConnectedException(ACCESS_DENIED);
        }
        return id;
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ##################################### SCHEDULER ORDERS #################################### */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * Check permission and rights
     *
     * @param permissionMsg the message to log if an error occurs.
     * @return the user identification found for this client
     * @throws NotConnectedException if the caller is not authenticated.
     * @throws PermissionException if the user has not the permission to access this method.
     */
    private UserIdentificationImpl checkPermission(String methodName, String permissionMsg)
            throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        UserIdentificationImpl ident = identifications.get(id);
        //renew session for this user
        renewUserSession(id, ident);

        final String fullMethodName = getClass().getName() + "." + methodName;
        final MethodCallPermission methodCallPermission = new MethodCallPermission(fullMethodName);

        try {
            ident.checkPermission(methodCallPermission, permissionMsg);
        } catch (PermissionException ex) {
            logger_dev.warn(permissionMsg);
            throw ex;
        }
        return ident;
    }

    /**
     * {@inheritDoc}
     */
    public boolean start() throws NotConnectedException, PermissionException {
        checkPermission("start", "You do not have permission to start the scheduler !");
        return scheduler.start();
    }

    /**
     * {@inheritDoc}
     */
    public boolean stop() throws NotConnectedException, PermissionException {
        checkPermission("stop", "You do not have permission to stop the scheduler !");
        return scheduler.stop();
    }

    /**
     * {@inheritDoc}
     */
    public boolean pause() throws NotConnectedException, PermissionException {
        checkPermission("pause", "You do not have permission to pause the scheduler !");
        return scheduler.pause();
    }

    /**
     * {@inheritDoc}
     */
    public boolean freeze() throws NotConnectedException, PermissionException {
        checkPermission("freeze", "You do not have permission to freeze the scheduler !");
        return scheduler.freeze();
    }

    /**
     * {@inheritDoc}
     */
    public boolean resume() throws NotConnectedException, PermissionException {
        checkPermission("resume", "You do not have permission to resume the scheduler !");
        return scheduler.resume();
    }

    /**
     * {@inheritDoc}
     */
    public boolean shutdown() throws NotConnectedException, PermissionException {
        checkPermission("shutdown", "You do not have permission to shutdown the scheduler !");
        return scheduler.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    public boolean kill() throws NotConnectedException, PermissionException {
        checkPermission("kill", "You do not have permission to kill the scheduler !");
        return scheduler.kill();
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();
        disconnect(id);
    }

    /**
     * Disconnect a user, remove and clean user dependent lists and objects
     * 
     * @param id the uniqueID of the user
     */
    private void disconnect(UniqueID id) {
        credentials.remove(id);
        UserIdentificationImpl ident = identifications.remove(id);
        if (ident != null) {
            //remove listeners if needed
            ident.clearListener();
            //remove this user to the list of connected user if it has not already been removed
            ident.setToRemove();
            sState.getUsers().update(ident);
            //cancel the timer
            ident.getSession().cancel();
            //log and send events
            String user = ident.getUsername();
            logger_dev.info("User '" + user + "' has disconnect the scheduler !");
            dispatchUsersUpdated(
                    new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident), false);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConnected() {
        try {
            checkAccess();
            return true;
        } catch (NotConnectedException nce) {
            return false;
        }
    }

    /**
     * Factoring of exception management for the 5 next jobs order.
     *
     * @param methodName the name of the method to be checked
     * @param jobId the jobId concerned by the order.
     * @param permissionMsg the message to send the user if he has no right.
     * @return the job identified object in case it can be useful
     * @throws NotConnectedException if user is not authenticated.
     * @throws UnknownJobException if the job does not exist.
     * @throws PermissionException if user can't access to this particular job.
     */
    private IdentifiedJob checkJobOwner(String methodName, JobId jobId, String permissionMsg)
            throws NotConnectedException, UnknownJobException, PermissionException {
        UserIdentificationImpl ident = checkPermission(methodName, permissionMsg);

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            String msg = "The job represented by this ID '" + jobId + "' is unknown !";
            logger_dev.info(msg);
            throw new UnknownJobException(msg);
        }

        if (!ij.hasRight(ident)) {
            logger_dev.info(permissionMsg);
            throw new PermissionException(permissionMsg);
        }

        return ij;
    }

    /**
     * {@inheritDoc}
     */
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        checkJobOwner("pauseJob", jobId, "You do not have permission to pause this job !");

        return scheduler.pauseJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        checkJobOwner("resumeJob", jobId, "You do not have permission to resume this job !");

        return scheduler.resumeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        checkJobOwner("killJob", jobId, "You do not have permission to kill this job !");

        return scheduler.killJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public void changeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        checkJobOwner("changeJobPriority", jobId,
                "You do not have permission to change the priority of this job !");

        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());

        try {
            ui.checkPermission(new ChangePriorityPermission(priority.getPriority()), ui.getUsername() +
                " does not have permissions to set job priority to " + priority);
        } catch (PermissionException ex) {
            logger_dev.info(ex.getMessage());
            throw ex;
        }

        if (jobs.get(jobId).isFinished()) {
            String msg = "Job '" + jobId + "' is already finished";
            logger_dev.info(msg);
            throw new JobAlreadyFinishedException(msg);
        }

        scheduler.changeJobPriority(jobId, priority);
    }

    /**
     * {@inheritDoc}
     */
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        checkJobOwner("getJobState", jobId, "You do not have permission to get the state of this job !");
        return jobsMap.get(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return this.killJob(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return this.pauseJob(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return this.removeJob(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return this.resumeJob(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public void changeJobPriority(String jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        this.changeJobPriority(JobIdImpl.makeJobId(jobId), priority);
    }

    /**
     * {@inheritDoc}
     */
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return this.getJobState(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    public boolean changePolicy(String newPolicyClassname) throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        UserIdentificationImpl ident = identifications.get(id);
        //renew session for this user
        renewUserSession(id, ident);

        try {
            ident.checkPermission(new ChangePolicyPermission(), ident.getUsername() +
                " does not have permissions to change the policy of the scheduler");
        } catch (PermissionException ex) {
            logger_dev.info(ex.getMessage());
            throw ex;
        }
        policyFullName = newPolicyClassname;
        return scheduler.changePolicy(newPolicyClassname);
    }

    /**
     * {@inheritDoc}
     */
    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        UserIdentificationImpl ident = identifications.get(id);
        //renew session for this user
        renewUserSession(id, ident);

        try {
            ident.checkPermission(new ConnectToResourceManagerPermission(), ident.getUsername() +
                " does not have permissions to change RM in the scheduler");
        } catch (PermissionException ex) {
            logger_dev.info(ex.getMessage());
            throw ex;
        }
        return scheduler.linkResourceManager(rmURL);
    }

    /**
     * {@inheritDoc}
     */
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        checkPermission("reloadPolicyConfiguration",
                "You do not have permission to reload policy configuration !");
        return scheduler.reloadPolicyConfiguration();
    }

    /**
     * Terminate the schedulerConnexion active object and then this object.
     * 
     * @return always true;
     */
    public boolean terminate() {
        if (authentication != null) {
            authentication.terminate();
        }

        ClientRequestHandler.terminate();

        PAActiveObject.terminateActiveObject(false);
        logger.info("Scheduler frontend is now shutdown !");

        return true;
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ################################## LISTENER DISPATCHER #################################### */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * Clear every dirty listeners that are no more responding
     */
    private void clearListeners() {
        for (UniqueID uId : dirtyList) {
            disconnect(uId);
        }
        dirtyList.clear();
    }

    /**
     * Put this is to be removed in the dirty list.
     * 
     * @param id the id of the user to be removed.
     */
    void markAsDirty(UniqueID id) {
        dirtyList.add(id);
    }

    /**
     * Dispatch the scheduler state updated event
     * 
     * @param eventType the type of the concrete event
     */
    private void dispatchSchedulerStateUpdated(SchedulerEvent eventType) {
        try {
            if (logger_dev.isDebugEnabled()) {
                logger_dev.debug("Dispatch event '" + eventType.toString() + "'");
            }
            for (UserIdentificationImpl userId : identifications.values()) {
                //if this user has a listener
                if (userId.isListening()) {
                    //if there is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) || userId.getUserEvents().contains(eventType)) {
                        userId.getListener().addEvent(eventMethods.get("schedulerStateUpdatedEvent"),
                                eventType);
                    }
                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger_dev.error(e);
        }
    }

    /**
     * Dispatch the job submitted event
     * 
     * @param job the new submitted job
     */
    private void dispatchJobSubmitted(JobState job) {
        try {
            if (logger_dev.isDebugEnabled()) {
                logger_dev.debug("Dispatch event '" + SchedulerEvent.JOB_SUBMITTED + "'");
            }
            for (UserIdentificationImpl userId : identifications.values()) {
                //if this user has a listener
                if (userId.isListening()) {
                    try {
                        //if there is no specified event OR if the specified event is allowed
                        if ((userId.getUserEvents() == null) ||
                            userId.getUserEvents().contains(SchedulerEvent.JOB_SUBMITTED)) {
                            //if this userId have the myEventOnly=false or (myEventOnly=true and it is its event)
                            if (!userId.isMyEventsOnly() ||
                                (userId.isMyEventsOnly() && userId.getUsername().equals(job.getOwner()))) {
                                userId.getListener().addEvent(eventMethods.get("jobSubmittedEvent"), job);
                            }
                        }
                    } catch (NullPointerException e) {
                        //can't do anything
                        logger_dev.debug("", e);
                    }
                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger_dev.error(e);
        }
    }

    /**
     * Dispatch the job state updated event
     * 
     * @param owner the owner of this job
     * @param notification the data to send to every client
     */
    private void dispatchJobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        try {
            if (logger_dev.isDebugEnabled()) {
                logger_dev.debug("Dispatch event '" + notification.getEventType() + "'");
            }
            for (UserIdentificationImpl userId : identifications.values()) {
                //if this user has a listener
                if (userId.isListening()) {
                    //if there is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        //if this userId have the myEventOnly=false or (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(owner))) {
                            userId.getListener().addEvent(eventMethods.get("jobStateUpdatedEvent"),
                                    notification);
                        }
                    }
                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger_dev.error(e);
        }
    }

    /**
     * Dispatch the task state updated event
     * 
     * @param owner the owner of this task
     * @param notification the data to send to every client
     */
    private void dispatchTaskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        try {
            if (logger_dev.isDebugEnabled()) {
                logger_dev.debug("Dispatch event '" + notification.getEventType() + "'");
            }
            for (UserIdentificationImpl userId : identifications.values()) {
                //if this user has a listener
                if (userId.isListening()) {
                    //if there is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        //if this userId have the myEventOnly=false or (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(owner))) {
                            userId.getListener().addEvent(eventMethods.get("taskStateUpdatedEvent"),
                                    notification);
                        }
                    }
                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger_dev.error(e);
        }
    }

    /**
     * Dispatch the users updated event
     * 
     * @param notification the data to send to every client
     */
    private void dispatchUsersUpdated(NotificationData<UserIdentification> notification,
            boolean checkForDownUser) {
        try {
            if (logger_dev.isDebugEnabled()) {
                logger_dev.debug("Dispatch event '" + notification.getEventType() + "'");
            }
            for (UserIdentificationImpl userId : identifications.values()) {
                //if this user has a listener
                if (userId.isListening()) {
                    //if there is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        //if this userId have the myEventOnly=false or (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(
                                    notification.getData().getUsername()))) {
                            userId.getListener()
                                    .addEvent(eventMethods.get("usersUpdatedEvent"), notification);
                        }
                    }
                }
            }
            //Important condition to avoid recursive checks
            if (checkForDownUser) {
                clearListeners();
            }
        } catch (SecurityException e) {
            logger_dev.error(e);
        }
        this.jmxHelper.getSchedulerRuntimeMBean().usersUpdatedEvent(notification);
    }

    //--------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void schedulerStateUpdated(SchedulerEvent eventType) {
        switch (eventType) {
            case STARTED:
                sState.setState(SchedulerStatus.STARTED);
                break;
            case STOPPED:
                sState.setState(SchedulerStatus.STOPPED);
                break;
            case PAUSED:
                sState.setState(SchedulerStatus.PAUSED);
                break;
            case FROZEN:
                sState.setState(SchedulerStatus.FROZEN);
                break;
            case RESUMED:
                sState.setState(SchedulerStatus.STARTED);
                break;
            case SHUTTING_DOWN:
                sState.setState(SchedulerStatus.SHUTTING_DOWN);
                break;
            case SHUTDOWN:
                sState.setState(SchedulerStatus.STOPPED);
                break;
            case KILLED:
                sState.setState(SchedulerStatus.KILLED);
                break;
            case DB_DOWN:
                sState.setState(SchedulerStatus.DB_DOWN);
                break;
            case RM_DOWN:
            case RM_UP:
            case POLICY_CHANGED:
                break;
            default:
                logger_dev.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                    eventType);
                return;
        }
        // send the event for all case, except default
        dispatchSchedulerStateUpdated(eventType);
        this.jmxHelper.getSchedulerRuntimeMBean().schedulerStateUpdatedEvent(eventType);
    }

    /**
     * {@inheritDoc}
     */
    public void jobSubmitted(JobState job) {
        jobsMap.put(job.getId(), job);
        sState.getPendingJobs().add(job);
        dispatchJobSubmitted(job);
        this.jmxHelper.getSchedulerRuntimeMBean().jobSubmittedEvent(job);
    }

    /**
     * {@inheritDoc}
     */
    public void jobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        JobState js = jobsMap.get(notification.getData().getJobId());
        js.update(notification.getData());
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_RUNNING:
                sState.getPendingJobs().remove(js);
                sState.getRunningJobs().add(js);
                dispatchJobStateUpdated(owner, notification);
                this.jmxHelper.getSchedulerRuntimeMBean().jobStateUpdatedEvent(notification);
                break;
            case JOB_PAUSED:
            case JOB_RESUMED:
            case JOB_CHANGE_PRIORITY:
            case TASK_REPLICATED:
            case TASK_SKIPPED:
                dispatchJobStateUpdated(owner, notification);
                this.jmxHelper.getSchedulerRuntimeMBean().jobStateUpdatedEvent(notification);
                break;
            case JOB_PENDING_TO_FINISHED:
                sState.getPendingJobs().remove(js);
                sState.getFinishedJobs().add(js);
                //set this job finished, user can get its result
                jobs.get(notification.getData().getJobId()).setFinished(true);
                dispatchJobStateUpdated(owner, notification);
                this.jmxHelper.getSchedulerRuntimeMBean().jobStateUpdatedEvent(notification);
                break;
            case JOB_RUNNING_TO_FINISHED:
                sState.getRunningJobs().remove(js);
                sState.getFinishedJobs().add(js);
                //set this job finished, user can get its result
                jobs.get(notification.getData().getJobId()).setFinished(true);
                dispatchJobStateUpdated(owner, notification);
                this.jmxHelper.getSchedulerRuntimeMBean().jobStateUpdatedEvent(notification);
                break;
            case JOB_REMOVE_FINISHED:
                //removing jobs from the global list : this job is no more managed
                sState.getFinishedJobs().remove(js);
                jobsMap.remove(js.getId());
                jobs.remove(notification.getData().getJobId());
                dispatchJobStateUpdated(owner, notification);
                this.jmxHelper.getSchedulerRuntimeMBean().jobStateUpdatedEvent(notification);
                break;
            default:
                logger_dev.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void taskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        jobsMap.get(notification.getData().getJobId()).update(notification.getData());
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
            case TASK_RUNNING_TO_FINISHED:
            case TASK_WAITING_FOR_RESTART:
                dispatchTaskStateUpdated(owner, notification);
                this.jmxHelper.getSchedulerRuntimeMBean().taskStateUpdatedEvent(notification);
                break;
            case TASK_PROGRESS:
                //this event can be sent while task is already finished,
                //as it is not a correct behavior, event is dropped if task is already finished.
                //so if task is not finished, send event
                if (notification.getData().getFinishedTime() <= 0) {
                    dispatchTaskStateUpdated(owner, notification);
                }
                break;
            default:
                logger_dev.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void usersUpdated(NotificationData<UserIdentification> notification) {
        switch (notification.getEventType()) {
            case USERS_UPDATE:
                dispatchUsersUpdated(notification, true);
                break;
            default:
                logger_dev.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
        }
    }

}
