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
package org.ow2.proactive.scheduler.core;

import static org.ow2.proactive.scheduler.common.SchedulerConstants.PARENT_JOB_ID;
import static org.ow2.proactive.scheduler.core.properties.PASchedulerProperties.SCHEDULER_FINISHED_JOBS_LRU_CACHE_SIZE;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.util.converter.ProActiveMakeDeepCopy;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.permissions.MethodCallPermission;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
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
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.permissions.ChangePolicyPermission;
import org.ow2.proactive.scheduler.permissions.ChangePriorityPermission;
import org.ow2.proactive.scheduler.permissions.ConnectToResourceManagerPermission;
import org.ow2.proactive.scheduler.permissions.HandleJobsWithBucketNamePermission;
import org.ow2.proactive.scheduler.permissions.HandleJobsWithGenericInformationPermission;
import org.ow2.proactive.scheduler.permissions.HandleJobsWithGroupNamePermission;
import org.ow2.proactive.scheduler.permissions.HandleOnlyMyJobsPermission;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.utils.Lambda;
import org.ow2.proactive.utils.Lambda.RunnableThatThrows3Exceptions;


class SchedulerFrontendState implements SchedulerStateUpdate {

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_DO_THIS_OPERATION = "You do not have permission to do this operation !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATUS = "You do not have permission to get the status !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_ADD_A_LISTENER = "You do not have permission to add a listener !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_CHANGE_THE_PRIORITY_OF_THIS_JOB = "You do not have permission to change the priority of this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE = "You do not have permission to get the state !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_TASK = "You do not have permission to get the state of this task !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB = "You do not have permission to get the state of this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THIS_JOB = "You do not have permission to get this job content !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_LOGS_OF_THIS_JOB = "You do not have permission to get the task logs of this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK = "You do not have permission to restart this task !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_PREEMPT_THIS_TASK = "You do not have permission to preempt this task !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_ENABLE_VISE_THIS_TASK = "You do not have permission to enable visualization on this task !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_ATTACH_SERVICE_TO_THIS_JOB = "You do not have permission to attach service to this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_JOB = "You do not have permission to kill this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIRD_PARTY_CREDENTIALS_FROM_THE_SCHEDULER = "You do not have permission to remove third-party credentials from the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_TASK = "You do not have permission to kill this task !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_SUBMIT_A_JOB = "You do not have permission to submit a job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_STOP_THE_SCHEDULER = "You do not have permission to stop the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_START_THE_SCHEDULER = "You do not have permission to start the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_SHUTDOWN_THE_SCHEDULER = "You do not have permission to shutdown the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THIS_JOB = "You do not have permission to pause this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_PUT_THIRD_PARTY_CREDENTIALS_IN_THE_SCHEDULER = "You do not have permission to put third-party credentials in the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_IN_ERROR_TASKS_IN_THIS_JOB = "You do not have permission to restart in error tasks in this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB = "You do not have permission to get the task result of this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIS_JOB = "You do not have permission to remove this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THIS_JOB = "You do not have permission to resume this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_LISTEN_THE_LOG_OF_THIS_JOB = "You do not have permission to listen the log of this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_RESULT_OF_THIS_JOB = "You do not have permission to get the result of this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSIONS_TO_GET_THE_LOGS_OF_THIS_JOB = "You do not have permissions to get the logs of this job !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THE_SCHEDULER = "You do not have permission to pause the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THE_SCHEDULER = "You do not have permission to resume the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_FREEZE_THE_SCHEDULER = "You do not have permission to freeze the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_LIST_THIRD_PARTY_CREDENTIALS_IN_THE_SCHEDULER = "You do not have permission to list third-party credentials in the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_RELOAD_POLICY_CONFIGURATION = "You do not have permission to reload policy configuration !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THE_SCHEDULER = "You do not have permission to kill the scheduler !";

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_FINISH_THIS_TASK = "You do not have permission to finish this task!";

    private static final String USERS_UPDATED_EVENT_METHOD = "usersUpdatedEvent";

    private static final String TASK_STATE_UPDATED_EVENT_METHOD = "taskStateUpdatedEvent";

    private static final String JOB_UPDATED_FULL_DATA_EVENT_METHOD = "jobUpdatedFullDataEvent";

    private static final String JOB_STATE_UPDATED_EVENT_METHOD = "jobStateUpdatedEvent";

    private static final String JOB_SUBMITTED_EVENT_METHOD = "jobSubmittedEvent";

    private static final String SCHEDULER_STATE_UPDATED_EVENT_METHOD = "schedulerStateUpdatedEvent";

    /** Scheduler logger */
    private static final Logger logger = Logger.getLogger(SchedulingService.class);

    private static final TaskLogger tlogger = TaskLogger.getInstance();

    private static final JobLogger jlogger = JobLogger.getInstance();

    /** A repeated warning message */
    private static final String ACCESS_DENIED = "Access denied! You are not connected or your session has expired!";

    /** Maximum duration of a session for a useless client */
    private static final long USER_SESSION_DURATION = PASchedulerProperties.SCHEDULER_USER_SESSION_TIME.getValueAsInt() *
                                                      1000;

    /** Stores methods that will be called on clients */
    private static final Map<String, Method> eventMethods;

    /** lock protecting scheduler state changes */
    private ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();

    private ReentrantReadWriteLock.ReadLock stateReadLock = stateLock.readLock();

    private ReentrantReadWriteLock.WriteLock stateWriteLock = stateLock.writeLock();

    static {
        eventMethods = new HashMap<>();
        for (Method m : SchedulerEventListener.class.getMethods()) {
            eventMethods.put(m.getName(), m);
        }
    }

    /**
     * Mapping on the UniqueId of the sender and the user/admin identifications
     */
    private final Map<UniqueID, UserAndCredentials> identifications;

    /** List used to mark the user that does not respond anymore */
    private final Set<UniqueID> dirtyList;

    /** Session timer */
    private final Timer sessionTimer;

    /** JMX Helper reference */
    private final SchedulerJMXHelper jmxHelper;

    /** Job identification management */
    private final Map<JobId, IdentifiedJob> jobs;

    /**
     * Scheduler state maintains by this class : avoid charging the core from
     * some request
     */
    private final SchedulerStateImpl<ClientJobState> schedulerState;

    private final Map<JobId, ClientJobState> jobsMap;

    private final LinkedHashMap<JobId, ClientJobState> finishedJobsLRUCache;

    private SchedulerDBManager dbManager = null;

    SchedulerFrontendState(SchedulerStateImpl schedulerState, SchedulerJMXHelper jmxHelper) {
        this.identifications = new ConcurrentHashMap<>();
        this.dirtyList = new HashSet<>();
        this.jmxHelper = jmxHelper;
        this.jobsMap = new HashMap<>();
        this.finishedJobsLRUCache = new LinkedHashMap<JobId, ClientJobState>(10, 0.75f, true) {
            @Override
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > SCHEDULER_FINISHED_JOBS_LRU_CACHE_SIZE.getValueAsInt();
            }
        };
        this.jobs = new HashMap<>();
        this.sessionTimer = new Timer("SessionTimer");
        this.schedulerState = schedulerState;
        recover(schedulerState);
    }

    SchedulerFrontendState(SchedulerStateImpl schedulerState, SchedulerJMXHelper jmxHelper,
            SchedulerDBManager dbManager) {
        this(schedulerState, jmxHelper);
        this.dbManager = dbManager;
    }

    /**
     * Called to recover the front end state. This method may have to rebuild
     * the different list of userIdentification and job/user association.
     */
    private void recover(SchedulerStateImpl sState) {
        Vector<ClientJobState> pendingJobs = sState.getPendingJobs();
        Vector<ClientJobState> runningJobs = sState.getRunningJobs();
        Vector<ClientJobState> finishedJobs = sState.getFinishedJobs();

        // default state = started
        Set<JobState> jobStates = new HashSet<>(pendingJobs.size() + runningJobs.size() + finishedJobs.size());

        if (logger.isInfoEnabled()) {
            logger.info("#Pending jobs: " + pendingJobs.size() + " #Running jobs: " + runningJobs.size() +
                        " #Finished jobs: " + finishedJobs.size());
        }

        for (ClientJobState js : pendingJobs) {
            prepare(jobStates, js, false);
        }
        for (ClientJobState js : runningJobs) {
            prepare(jobStates, js, false);
        }
        for (ClientJobState js : finishedJobs) {
            prepare(jobStates, js, true);
        }
    }

    /**
     * Prepare the job in the frontend
     *
     * @param jobStates
     *            a temporary set of jobs
     * @param js
     *            the current job to be prepared
     * @param finished
     *            if the job is finished or not
     */
    private void prepare(Set<JobState> jobStates, ClientJobState js, boolean finished) {
        jobStates.add(js);
        IdentifiedJob ij = toIdentifiedJob(js);
        jobs.put(js.getId(), ij);
        jobsMap.put(js.getId(), js);
        ij.setFinished(finished);
    }

    /**
     * Connect a new user on the scheduler. This user can interact with the
     * scheduler according to his right.
     *
     * @param sourceBodyID
     *            the source ID of the connected object representing a user
     * @param identification
     *            the identification of the connected user
     * @throws SchedulerException
     *             If an error occurred during connection with the front-end.
     */
    void connect(UniqueID sourceBodyID, UserIdentificationImpl identification, Credentials cred)
            throws AlreadyConnectedException {
        if (identifications.containsKey(sourceBodyID)) {
            logger.warn("Active object already connected for this user :" + identification.getUsername());
            throw new AlreadyConnectedException("This active object is already connected to the scheduler !");
        }
        // add this new user in the list of connected user
        logger.info(identification.getUsername() + " successfully connected !");
        identifications.put(sourceBodyID, new UserAndCredentials(new ListeningUser(identification), cred));
        renewUserSession(sourceBodyID, identification);
        // add this new user in the list of connected user
        Lambda.withLock(stateWriteLock, () -> schedulerState.getUsers().update(identification));
        // send events
        usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, identification));
    }

    /**
     * Create or renew the session (timer task) for the given user
     * identification. A call to this method will cancel the previous session
     * (timerTask), create and schedule a new one and purge the timer.
     * 
     * @param id
     *            The unique ID of the user
     * @param identification
     *            the user on which to renew the session
     */
    private void renewUserSession(final UniqueID id, UserIdentificationImpl identification) {
        if (identifications.get(id).getListeningUser().isListening()) {
            // if this id has a listener, do not renew user session
            return;
        }
        final String userName = identification.getUsername();
        synchronized (sessionTimer) {
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
    }

    SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        // checking permissions
        checkPermission("getStatus", YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATUS);
        return Lambda.withLock(stateReadLock, () -> schedulerState.getStatus());
    }

    SchedulerState getState() throws NotConnectedException, PermissionException {
        return getState(false);
    }

    SchedulerState getStateInternally() {
        return schedulerState;
    }

    SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        // checking permissions
        ListeningUser ui = checkPermissionReturningListeningUser("getState",
                                                                 YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE);

        return Lambda.withLock(stateReadLock,
                               () -> myJobsOnly ? schedulerState.filterOnUser(ui.getUser().getUsername())
                                                : schedulerState);

    }

    /**
     * Check if the given user can get the state as it is demanded (full or user
     * only)
     *
     * @param myOnly
     *            true, if the user wants only its events or jobs, false if user
     *            want the full state
     * @param ui
     *            the user identification
     * @throws PermissionException
     *             if permission is denied
     */
    void handleOnlyMyJobsPermission(boolean myOnly, UserIdentificationImpl ui, String errorMessage)
            throws PermissionException {
        ui.checkPermission(new HandleOnlyMyJobsPermission(myOnly),
                           ui.getUsername() + " does not have permissions to handle other users jobs (" + errorMessage +
                                                                   ")");
    }

    /**
     * Check if the given user can get the state as it is demanded (based on the
     * generic information content)
     *
     * @param genericInformation
     *            generic information of the job. In order to have the
     *            authorisation, this generic information need to contains the
     *            keys-values specified in the security file.
     *
     * @param ui
     *            the user identification
     * @throws PermissionException
     *             if permission is denied
     */
    private void handleJobsWithGenericInformationPermission(Map<String, String> genericInformation,
            UserIdentificationImpl ui, String errorMessage) throws PermissionException {
        ui.checkPermission(new HandleJobsWithGenericInformationPermission(genericInformation),
                           ui.getUsername() + " does not have permissions to handle this job (" + errorMessage + ")");
    }

    private void handleJobBucketNameGenericInformationPermission(Map<String, String> genericInformation,
            UserIdentificationImpl ui, String errorMessage) throws PermissionException {
        ui.checkPermission(new HandleJobsWithBucketNamePermission(genericInformation),
                           ui.getUsername() + " does not have permissions to handle this job (" + errorMessage + ")");
    }

    private void handleJobGroupNameGenericInformationPermission(Map<String, String> genericInformation,
            UserIdentificationImpl ui, String errorMessage) throws PermissionException {
        ui.checkPermission(new HandleJobsWithGroupNamePermission(genericInformation),
                           ui.getUsername() + " does not have permissions to handle this job (" + errorMessage + ")");
    }

    void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        addEventListener(sel, myEventsOnly, false, events);
    }

    SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly, boolean getCurrentState,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        // checking permissions
        ListeningUser uIdent = checkPermissionReturningListeningUser("addEventListener",
                                                                     YOU_DO_NOT_HAVE_PERMISSION_TO_ADD_A_LISTENER);

        // check if listener is not null
        if (sel == null) {
            String msg = "Scheduler listener must be not null";
            logger.info(msg);
            throw new IllegalArgumentException(msg);
        }
        // check if the listener is a reified remote object
        if (!MOP.isReifiedObject(sel)) {
            String msg = "Scheduler listener must be a remote object";
            logger.info(msg);
            throw new IllegalArgumentException(msg);
        }

        // get the scheduler State
        SchedulerState currentState = null;
        if (getCurrentState) {
            // check get state permission is checked in getState method
            currentState = getState(myEventsOnly);
        } else {
            // check get state permission
            handleOnlyMyJobsPermission(myEventsOnly, uIdent.getUser(), YOU_DO_NOT_HAVE_PERMISSION_TO_ADD_A_LISTENER);
        }
        // prepare user for receiving events
        uIdent.getUser().setUserEvents(events);
        // set if the user wants to get its events only or every events
        uIdent.getUser().setMyEventsOnly(myEventsOnly);
        // add the listener to the list of listener for this user.
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        uIdent.setListener(new ClientRequestHandler(this, id, sel));
        // cancel timer for this user : session is now managed by events
        uIdent.getUser().cancelSession();

        // return to the user
        return currentState;
    }

    void removeEventListener() throws NotConnectedException, PermissionException {
        // Remove the listener on that user designated by its given UniqueID,
        // then renew its user session as it is no more managed by the listener.
        renewSession(true);
    }

    private UniqueID checkAccess() throws NotConnectedException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        if (!identifications.containsKey(id)) {
            logger.info(ACCESS_DENIED);
            throw new NotConnectedException(ACCESS_DENIED);
        }
        return id;
    }

    InternalJob createJob(Job userJob, UserIdentificationImpl ident)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        UniqueID id = checkAccess();

        Credentials userCreds = identifications.get(id).getCredentials();

        // get the internal job.
        InternalJob job = InternalJobFactory.createJob(userJob, userCreds);

        // setting job informations
        if (job.getTasks().size() == 0) {
            String msg = "Job " + job.getId().value() +
                         " contains no task. You need to insert at least one task before submitting job";
            logger.info(msg);
            throw new JobCreationException(msg);
        }

        // verifying that the user has right to set the given priority to his
        // job.
        try {
            ident.checkPermission(new ChangePriorityPermission(job.getPriority().ordinal()),
                                  ident.getUsername() + " does not have rights to set job priority " +
                                                                                             job.getPriority());
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }
        // setting the job properties
        job.setOwner(ident.getUsername());
        // route project name inside job info
        job.setProjectName(job.getProjectName());

        fillParentJobIdIfExistsInGenInfo(userJob, job);

        return job;
    }

    private void fillParentJobIdIfExistsInGenInfo(Job userJob, InternalJob job) {
        if (userJob.getGenericInformation() != null && userJob.getGenericInformation().containsKey(PARENT_JOB_ID)) {
            String parentJobIdString = userJob.getGenericInformation().get(PARENT_JOB_ID);
            try {
                long parentJobId = Long.parseLong(parentJobIdString);
                job.setParentId(parentJobId);
            } catch (NumberFormatException e) {
                logger.error("Cannot parse '" + PARENT_JOB_ID + "' in the generic info: " + parentJobIdString);
            }
        }
    }

    void jobSubmitted(InternalJob job, UserIdentificationImpl ident) {
        // put the job inside the frontend management list
        Lambda.withLock(stateWriteLock,
                        () -> jobs.put(job.getId(),
                                       new IdentifiedJob(job.getId(), ident, job.getGenericInformation())));
        // increase number of submit for this user
        ident.addSubmit();
        // send update user event
        usersUpdated(new NotificationData<>(SchedulerEvent.USERS_UPDATE, ident));
        jlogger.info(job.getId(),
                     "submitted: name '" + job.getName() + "', tasks '" + job.getTotalNumberOfTasks() + "', owner '" +

                                  job.getOwner() + "'");
        if (jlogger.isTraceEnabled()) {
            try {
                jlogger.trace(job.getId(), job.display());
            } catch (Exception e) {
                jlogger.error(job.getId(), "Error while displaying the job :", e);
            }
        }
        // check whether job property conflicts with the global configuration
        for (Task task : job.getTasks()) {
            if (PASchedulerProperties.TASK_RUNASME.getValueAsBoolean()) {
                if (!task.isRunAsMe()) {
                    jlogger.warn(job.getId(),
                                 String.format("The task [%s] configuration 'runAsMe=%b' is ignored, as it conflicts with the global configuration [%s=%b].",
                                               task.getName(),
                                               task.isRunAsMe(),
                                               PASchedulerProperties.TASK_RUNASME.getKey(),
                                               PASchedulerProperties.TASK_RUNASME.getValueAsBoolean()));
                }
                if (Boolean.FALSE.equals(task.isFork())) {
                    jlogger.warn(job.getId(),
                                 String.format("The task [%s] configuration 'fork=%b' is ignored, as it conflicts with the global configuration [%s=%b].",
                                               task.getName(),
                                               task.isFork(),
                                               PASchedulerProperties.TASK_RUNASME.getKey(),
                                               PASchedulerProperties.TASK_RUNASME.getValueAsBoolean()));
                }
            }
            if (PASchedulerProperties.TASK_FORK.getValueAsStringOrNull() != null) {
                if (task.isFork() != null && PASchedulerProperties.TASK_FORK.getValueAsBoolean() != task.isFork()) {
                    jlogger.debug(job.getId(),
                                  String.format("The task [%s] configuration 'fork=%s' is ignored, as it conflicts with the global configuration [%s=%s].",
                                                task.getName(),
                                                task.isFork(),
                                                PASchedulerProperties.TASK_FORK.getKey(),
                                                PASchedulerProperties.TASK_FORK.getValueAsStringOrNull()));
                }
                if (!PASchedulerProperties.TASK_FORK.getValueAsBoolean() && task.isRunAsMe()) {
                    jlogger.warn(job.getId(),
                                 String.format("The task [%s] configuration 'runAsMe=%s' is ignored, as it conflicts with the global configuration [%s=%s].",
                                               task.getName(),
                                               task.isRunAsMe(),
                                               PASchedulerProperties.TASK_FORK.getKey(),
                                               PASchedulerProperties.TASK_FORK.getValueAsStringOrNull()));
                }
            }
        }
    }

    UserAndCredentials checkPermissionReturningCredentials(String methodName, String permissionMsg,
            boolean clearListener) throws NotConnectedException, PermissionException {

        UniqueID id = checkAccess();
        UserAndCredentials userAndCredentials = identifications.get(id);
        ListeningUser listeningUser = userAndCredentials.getListeningUser();
        UserIdentificationImpl ident = listeningUser.getUser();
        if (clearListener) {
            listeningUser.clearListener();
        }
        // renew session for this user
        renewUserSession(id, ident);

        Pair<ListeningUser, UserIdentificationImpl> userSessionInfo = renewSession(false);

        final String fullMethodName = SchedulerFrontend.class.getName() + "." + methodName;
        final MethodCallPermission methodCallPermission = new MethodCallPermission(fullMethodName);

        try {
            userSessionInfo.getRight().checkPermission(methodCallPermission, permissionMsg);
        } catch (PermissionException ex) {
            logger.debug(permissionMsg);
            throw ex;
        }
        return userAndCredentials;

    }

    ListeningUser checkPermissionReturningListeningUser(String methodName, String permissionMsg)
            throws NotConnectedException, PermissionException {

        Pair<ListeningUser, UserIdentificationImpl> userSessionInfo = renewSession(false);

        final String fullMethodName = SchedulerFrontend.class.getName() + "." + methodName;
        final MethodCallPermission methodCallPermission = new MethodCallPermission(fullMethodName);

        try {
            userSessionInfo.getRight().checkPermission(methodCallPermission, permissionMsg);
        } catch (PermissionException ex) {
            logger.debug(permissionMsg);
            throw ex;
        }
        return userSessionInfo.getLeft();

    }

    UserIdentificationImpl checkPermission(String methodName, String permissionMsg)
            throws NotConnectedException, PermissionException {
        return checkPermissionReturningListeningUser(methodName, permissionMsg).getUser();
    }

    void disconnect() throws NotConnectedException {
        UniqueID id = checkAccess();
        disconnect(id);
    }

    /**
     * Disconnect a user, remove and clean user dependent lists and objects
     * 
     * @param id
     *            the uniqueID of the user
     */
    private void disconnect(UniqueID id) {
        UserAndCredentials userAndCredentials = identifications.remove(id);
        if (userAndCredentials != null) {
            // remove listeners if needed
            userAndCredentials.getListeningUser().clearListener();
            // remove this user to the list of connected user if it has not
            // already been removed
            userAndCredentials.getListeningUser().getUser().setToRemove();
            Lambda.withLock(stateWriteLock,
                            () -> schedulerState.getUsers().update(userAndCredentials.getListeningUser().getUser()));
            // cancel the timer
            userAndCredentials.getListeningUser().getUser().cancelSession();
            // log and send events
            String user = userAndCredentials.getListeningUser().getUser().getUsername();
            logger.info("User '" + user + "' has disconnected from the scheduler.");
            dispatchUsersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE,
                                                                          userAndCredentials.getListeningUser()
                                                                                            .getUser()),
                                 false);
        }

    }

    boolean isConnected() {
        try {
            checkAccess();
            return true;
        } catch (NotConnectedException nce) {
            return false;
        }
    }

    Pair<ListeningUser, UserIdentificationImpl> renewSession(boolean clearListener) throws NotConnectedException {
        UniqueID id = checkAccess();
        ListeningUser listeningUser = identifications.get(id).getListeningUser();
        UserIdentificationImpl ident = listeningUser.getUser();
        if (clearListener) {
            listeningUser.clearListener();
        }
        // renew session for this user
        renewUserSession(id, ident);
        return Pair.of(listeningUser, ident);

    }

    IdentifiedJob getIdentifiedJob(JobId jobId) throws UnknownJobException {
        return Lambda.withLockException1(stateReadLock, () -> {
            IdentifiedJob identifiedJob = jobs.get(jobId);

            if (identifiedJob == null) {

                ClientJobState clientJobState = getClientJobState(jobId);
                if (clientJobState != null) {
                    identifiedJob = toIdentifiedJob(clientJobState);
                    identifiedJob.setFinished(true); // because wherenever there is job in jobsMap, but not in jobs, it is always finished
                } else {
                    String msg = "The job represented by this ID '" + jobId + "' is unknown !";
                    logger.info(msg);
                    throw new UnknownJobException(msg);
                }
            }

            return identifiedJob;
        }, UnknownJobException.class);
    }

    void checkChangeJobPriority(JobId jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {

        checkPermissions("changeJobPriority",
                         getIdentifiedJob(jobId),
                         YOU_DO_NOT_HAVE_PERMISSION_TO_CHANGE_THE_PRIORITY_OF_THIS_JOB);

        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext()
                                                                      .getCurrentRequest()
                                                                      .getSourceBodyID())
                                                   .getListeningUser()
                                                   .getUser();

        try {
            ui.checkPermission(new ChangePriorityPermission(priority.getPriority()),
                               ui.getUsername() + " does not have permissions to set job priority to " + priority);
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }

        if (Lambda.withLock(stateReadLock, () -> jobs.get(jobId).isFinished())) {
            String msg = " is already finished";
            jlogger.info(jobId, msg);
            throw new JobAlreadyFinishedException("Job " + jobId + msg);
        }
    }

    void checkPermissions(String methodName, IdentifiedJob identifiedJob, String errorMessage)
            throws NotConnectedException, UnknownJobException, PermissionException {
        checkPermissionChain(
                             // if we are job owner
                             () -> checkJobOwner(methodName, identifiedJob, errorMessage),
                             // if it is 'only my jobs' permission
                             () -> handleOnlyMyJobsPermission(false,
                                                              checkPermission(methodName, errorMessage),
                                                              errorMessage),
                             // if generic info matches
                             () -> handleJobsWithGenericInformationPermission(identifiedJob.getGenericInformation(),
                                                                              checkPermission(methodName, errorMessage),
                                                                              errorMessage),
                             // if bucket name is allowed
                             () -> handleJobBucketNameGenericInformationPermission(identifiedJob.getGenericInformation(),
                                                                                   checkPermission(methodName,
                                                                                                   errorMessage),
                                                                                   errorMessage),
                             // if group name is allows
                             () -> handleJobGroupNameGenericInformationPermission(identifiedJob.getGenericInformation(),
                                                                                  checkPermission(methodName,
                                                                                                  errorMessage),
                                                                                  errorMessage));
    }

    void checkPermissionChain(
            RunnableThatThrows3Exceptions<PermissionException, NotConnectedException, UnknownJobException>... checks)
            throws NotConnectedException, UnknownJobException, PermissionException {
        for (int i = 0; i < checks.length; ++i) {
            RunnableThatThrows3Exceptions<PermissionException, NotConnectedException, UnknownJobException> check = checks[i];
            try {
                check.run();
            } catch (PermissionException pe) {
                if (i == checks.length - 1) {
                    throw pe; // all checks failed, no more hope
                } else {
                    continue; // we will try another one check
                }
            } catch (NotConnectedException | UnknownJobException se) {
                // we failed and we cannot continue
                throw se;
            }
            break; // all good
        }

    }

    void checkJobOwner(String methodName, IdentifiedJob IdentifiedJob, String permissionMsg)
            throws NotConnectedException, UnknownJobException, PermissionException {
        ListeningUser ident = checkPermissionReturningListeningUser(methodName, permissionMsg);

        if (!IdentifiedJob.hasRight(ident.getUser())) {
            throw new PermissionException(permissionMsg);
        }
    }

    Set<TaskId> getJobTasks(JobId jobId) {
        return (Set<TaskId>) Lambda.withLock(stateReadLock, () -> {
            Set<TaskId> tasks;
            ClientJobState jobState = getClientJobState(jobId);
            if (jobState == null) {
                return new HashSet<>();
            } else {
                jobState.readLock();
                try {
                    tasks = new HashSet<>(jobState.getTasks().size());
                    for (TaskState task : jobState.getTasks()) {
                        tasks.add(task.getId());
                    }
                    return tasks;
                } finally {
                    jobState.readUnlock();
                }
            }
        });
    }

    JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        checkPermissions("getJobState",
                         getIdentifiedJob(jobId),
                         YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB);
        return Lambda.withLock(stateReadLock, () -> {
            ClientJobState jobState = getClientJobState(jobId);
            ClientJobState jobStateCopy;
            if (jobState == null) {
                throw new UnknownJobException(jobId);
            }
            try {
                jobState.readLock();
                try {
                    jobStateCopy = (ClientJobState) ProActiveMakeDeepCopy.WithProActiveObjectStream.makeDeepCopy(jobState);
                } catch (Exception e) {
                    logger.error("Error when copying job state", e);
                    throw new IllegalStateException(e);
                }
            } finally {
                jobState.readUnlock();
            }
            return jobStateCopy;
        });
    }

    TaskState getTaskState(JobId jobId, TaskId taskId)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkPermissions("getJobState",
                         getIdentifiedJob(jobId),
                         YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_TASK);
        return Lambda.withLockException2(stateReadLock, () -> {
            ClientJobState jobState = getClientJobState(jobId);
            if (jobState == null) {
                throw new UnknownJobException(jobId);
            }
            try {
                jobState.readLock();
                TaskState ts = jobState.getHMTasks().get(taskId);
                if (ts == null) {
                    throw new UnknownTaskException(taskId, jobId);
                }
                return ts;
            } finally {
                jobState.readUnlock();
            }
        }, UnknownJobException.class, UnknownTaskException.class);
    }

    TaskState getTaskState(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {

        checkPermissions("getJobState",
                         getIdentifiedJob(jobId),
                         YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_TASK);

        return Lambda.withLockException2(stateReadLock, () -> {
            ClientJobState jobState = getClientJobState(jobId);
            if (jobState == null) {
                throw new UnknownJobException(jobId);
            }
            TaskId taskId = null;
            for (TaskId t : getJobTasks(jobId)) {
                if (t.getReadableName().equals(taskName)) {
                    taskId = t;
                }
            }
            if (taskId == null) {
                throw new UnknownTaskException(taskName, jobId);
            }
            try {
                jobState.readLock();
                TaskState ts = jobState.getHMTasks().get(taskId);
                if (ts == null) {
                    throw new UnknownTaskException(taskId, jobId);
                }
                return ts;
            } finally {
                jobState.readUnlock();
            }
        }, UnknownJobException.class, UnknownTaskException.class);
    }

    TaskId getTaskId(JobId jobId, String taskName) throws UnknownTaskException, UnknownJobException {
        if (getClientJobState(jobId) == null) {
            throw new UnknownJobException(jobId);
        }
        TaskId taskId = null;
        for (TaskId t : getJobTasks(jobId)) {
            if (t.getReadableName().equals(taskName)) {
                taskId = t;
            }
        }
        if (taskId == null) {
            throw new UnknownTaskException(taskName, jobId);
        }
        return taskId;
    }

    void checkChangePolicy() throws NotConnectedException, PermissionException {
        Pair<ListeningUser, UserIdentificationImpl> userSessionInfo = renewSession(false);

        try {
            userSessionInfo.getRight()
                           .checkPermission(new ChangePolicyPermission(),
                                            userSessionInfo.getRight().getUsername() +
                                                                          " does not have permissions to change the policy of the scheduler");
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }
    }

    void checkLinkResourceManager() throws NotConnectedException, PermissionException {
        Pair<ListeningUser, UserIdentificationImpl> userSessionInfo = renewSession(false);

        try {
            userSessionInfo.getRight()
                           .checkPermission(new ConnectToResourceManagerPermission(),
                                            userSessionInfo.getRight()
                                                           .getUsername() + " does not have permissions to change RM in the scheduler");
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }
    }

    /*
     * ###########################################################################################
     */
    /*                                                                                             */
    /*
     * ################################## LISTENER DISPATCHER ####################################
     */
    /*                                                                                             */
    /*
     * ###########################################################################################
     */

    /**
     * Clear every dirty listeners that are no more responding
     */
    private void clearListeners() {
        Set<UniqueID> toRemove;

        synchronized (dirtyList) {
            if (dirtyList.isEmpty()) {
                return;
            }
            toRemove = new HashSet<>(dirtyList);
            dirtyList.clear();
        }

        for (UniqueID uId : toRemove) {
            disconnect(uId);
        }
    }

    /**
     * Put this is to be removed in the dirty list.
     * 
     * @param id
     *            the id of the user to be removed.
     */
    void markAsDirty(UniqueID id) {
        synchronized (dirtyList) {
            dirtyList.add(id);
        }
    }

    /**
     * Dispatch the scheduler state updated event
     * 
     * @param eventType
     *            the type of the concrete event
     */
    private void dispatchSchedulerStateUpdated(SchedulerEvent eventType) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("event [" + eventType.toString() + "]");
            }
            for (UserAndCredentials userAndCredentials : identifications.values()) {
                ListeningUser userId = userAndCredentials.getListeningUser();
                // if this user has a listener
                if (userId.isListening()) {
                    // if there is no specified event OR if the specified event
                    // is allowed
                    if ((userId.getUser().getUserEvents() == null) ||
                        userId.getUser().getUserEvents().contains(eventType)) {
                        userId.getListener().addEvent(eventMethods.get(SCHEDULER_STATE_UPDATED_EVENT_METHOD),
                                                      eventType);
                    }
                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger.error("", e);
        }
    }

    /**
     * Dispatch the job submitted event
     * 
     * @param job
     *            the new submitted job
     */
    private void dispatchJobSubmitted(JobState job) {
        try {
            if (logger.isDebugEnabled()) {
                jlogger.debug(job.getJobInfo().getJobId(), " event [" + SchedulerEvent.JOB_SUBMITTED + "]");
            }
            for (UserAndCredentials userAndCredentials : identifications.values()) {
                ListeningUser listeningUserId = userAndCredentials.getListeningUser();
                // if this user has a listener
                if (listeningUserId.isListening()) {
                    UserIdentificationImpl userId = listeningUserId.getUser();
                    // if there is no specified event OR if the specified
                    // event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(SchedulerEvent.JOB_SUBMITTED)) {
                        // if this userId have the myEventOnly=false or
                        // (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(job.getOwner()))) {
                            listeningUserId.getListener().addEvent(eventMethods.get(JOB_SUBMITTED_EVENT_METHOD), job);
                        }
                    }

                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger.error("", e);
        }
    }

    /**
     * Dispatch the job state updated event
     * 
     * @param owner
     *            the owner of this job
     * @param notification
     *            the data to send to every client
     */
    private void dispatchJobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        try {
            if (logger.isDebugEnabled()) {
                // if in process of job removal do not use jlogger as job log
                // file
                // was already removed and it will create it again
                if (notification.getEventType() == SchedulerEvent.JOB_REMOVE_FINISHED) {
                    logger.debug("job " + notification.getData().getJobId() + " event [" + notification.getEventType() +
                                 "]");
                } else {
                    jlogger.debug(notification.getData().getJobId(), " event [" + notification.getEventType() + "]");
                }
            }
            for (UserAndCredentials userAndCredentials : identifications.values()) {
                ListeningUser listeningUserId = userAndCredentials.getListeningUser();
                // if this user has a listener
                if (listeningUserId.isListening()) {
                    UserIdentificationImpl userId = listeningUserId.getUser();
                    // if there is no specified event OR if the specified event
                    // is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        // if this userId have the myEventOnly=false or
                        // (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(owner))) {
                            listeningUserId.getListener().addEvent(eventMethods.get(JOB_STATE_UPDATED_EVENT_METHOD),
                                                                   notification);
                        }
                    }
                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger.error("", e);
        }
    }

    /**
     * Dispatch the job state updated event
     * 
     * @param job
     *            the job state
     */
    private void dispatchJobUpdatedFullData(JobState job) {
        try {
            if (logger.isDebugEnabled()) {
                jlogger.debug(job.getJobInfo().getJobId(), " event [" + SchedulerEvent.JOB_UPDATED + "]");
            }
            for (UserAndCredentials userAndCredentials : identifications.values()) {
                ListeningUser listeningUserId = userAndCredentials.getListeningUser();
                // if this user has a listener
                if (listeningUserId.isListening()) {
                    UserIdentificationImpl userId = listeningUserId.getUser();
                    // if there is no specified event OR if the specified
                    // event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(SchedulerEvent.JOB_UPDATED)) {
                        // if this userId have the myEventOnly=false or
                        // (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(job.getOwner()))) {
                            listeningUserId.getListener().addEvent(eventMethods.get(JOB_UPDATED_FULL_DATA_EVENT_METHOD),
                                                                   job);
                        }
                    }

                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger.error("", e);
        }
    }

    /**
     * Dispatch the task state updated event
     * 
     * @param owner
     *            the owner of this task
     * @param notification
     *            the data to send to every client
     */
    private void dispatchTaskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        try {
            if (logger.isDebugEnabled()) {
                tlogger.debug(notification.getData().getTaskId(), "event [" + notification.getEventType() + "]");
            }
            for (UserAndCredentials userAndCredentials : identifications.values()) {
                ListeningUser listeningUserId = userAndCredentials.getListeningUser();
                // if this user has a listener
                if (listeningUserId.isListening()) {
                    UserIdentificationImpl userId = listeningUserId.getUser();
                    // if there is no specified event OR if the specified event
                    // is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        // if this userId have the myEventOnly=false or
                        // (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(owner))) {
                            listeningUserId.getListener().addEvent(eventMethods.get(TASK_STATE_UPDATED_EVENT_METHOD),
                                                                   notification);
                        }
                    }
                }
            }
            clearListeners();
        } catch (SecurityException e) {
            logger.error("", e);
        }
    }

    /**
     * Dispatch the users updated event
     * 
     * @param notification
     *            the data to send to every client
     */
    private void dispatchUsersUpdated(NotificationData<UserIdentification> notification, boolean checkForDownUser) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("event [" + notification.getEventType() + "]");
            }
            for (UserAndCredentials userAndCredentials : identifications.values()) {
                ListeningUser listeningUserId = userAndCredentials.getListeningUser();
                // if this user has a listener
                if (listeningUserId.isListening()) {
                    UserIdentificationImpl userId = listeningUserId.getUser();
                    // if there is no specified event OR if the specified event
                    // is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        // if this userId have the myEventOnly=false or
                        // (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() &&
                             userId.getUsername().equals(notification.getData().getUsername()))) {
                            listeningUserId.getListener().addEvent(eventMethods.get(USERS_UPDATED_EVENT_METHOD),
                                                                   notification);
                        }
                    }
                }
            }
            // Important condition to avoid recursive checks
            if (checkForDownUser) {
                clearListeners();
            }
        } catch (SecurityException e) {
            logger.error("", e);
        }
        this.jmxHelper.getSchedulerRuntimeMBean().usersUpdatedEvent(notification);
    }

    @Override
    public void updateNeededNodes(int needed) {
        this.jmxHelper.getSchedulerRuntimeMBean().setNeededNodes(needed);
    }

    @Override
    public void schedulerStateUpdated(SchedulerEvent eventType) {
        Lambda.withLock(stateWriteLock, () -> {
            switch (eventType) {
                case STARTED:
                    schedulerState.setState(SchedulerStatus.STARTED);
                    break;
                case STOPPED:
                    schedulerState.setState(SchedulerStatus.STOPPED);
                    break;
                case PAUSED:
                    schedulerState.setState(SchedulerStatus.PAUSED);
                    break;
                case FROZEN:
                    schedulerState.setState(SchedulerStatus.FROZEN);
                    break;
                case RESUMED:
                    schedulerState.setState(SchedulerStatus.STARTED);
                    break;
                case SHUTTING_DOWN:
                    schedulerState.setState(SchedulerStatus.SHUTTING_DOWN);
                    break;
                case SHUTDOWN:
                    schedulerState.setState(SchedulerStatus.STOPPED);
                    break;
                case KILLED:
                    schedulerState.setState(SchedulerStatus.KILLED);
                    break;
                case DB_DOWN:
                    schedulerState.setState(SchedulerStatus.DB_DOWN);
                    break;
                case RM_DOWN:
                case RM_UP:
                case POLICY_CHANGED:
                    break;
                default:
                    logger.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " + eventType);
                    return;
            }
        });
        // send the event for all case, except default
        dispatchSchedulerStateUpdated(eventType);
        this.jmxHelper.getSchedulerRuntimeMBean().schedulerStateUpdatedEvent(eventType);
    }

    @Override
    public void jobSubmitted(JobState job) {
        Lambda.withLock(stateWriteLock, () -> {
            ClientJobState storedJobState = new ClientJobState(job);
            jobsMap.put(job.getId(), storedJobState);
            schedulerState.update(storedJobState);
        });
        dispatchJobSubmitted(job);
    }

    @Override
    public void jobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        Lambda.withLock(stateWriteLock, () -> {
            if (notification.getEventType().equals(SchedulerEvent.JOB_REMOVE_FINISHED)) {
                // removing jobs from the global list : this job is no more managed
                schedulerState.removeFinished(notification.getData().getJobId());
                jobsMap.remove(notification.getData().getJobId());
                finishedJobsLRUCache.remove(notification.getData().getJobId());
                jobs.remove(notification.getData().getJobId());
                logger.debug("HOUSEKEEPING removed the finished job " + notification.getData().getJobId() +
                             " from the SchedulerFrontEndState");
                return;
            }
            ClientJobState js = getClientJobState(notification.getData().getJobId());

            boolean withAttachment = false;
            if (js != null) {
                try {
                    js.writeLock();
                    js.update(notification.getData());
                    switch (notification.getEventType()) {
                        case JOB_PENDING_TO_RUNNING:
                            schedulerState.pendingToRunning(js);
                            break;
                        case JOB_PAUSED:
                        case JOB_IN_ERROR:
                        case JOB_RESUMED:
                        case JOB_RESTARTED_FROM_ERROR:
                        case JOB_CHANGE_PRIORITY:
                        case TASK_REPLICATED:
                        case TASK_SKIPPED:
                        case JOB_UPDATED:
                            break;
                        case JOB_PENDING_TO_FINISHED:
                            schedulerState.pendingToFinished(js);
                            // set this job finished, user can get its result
                            jobs.remove(notification.getData().getJobId()).setFinished(true);
                            jobsMap.remove(notification.getData().getJobId());
                            withAttachment = true;
                            break;
                        case JOB_RUNNING_TO_FINISHED:
                            schedulerState.runningToFinished(js);
                            // set this job finished, user can get its result
                            jobs.remove(notification.getData().getJobId()).setFinished(true);
                            jobsMap.remove(notification.getData().getJobId());
                            withAttachment = true;
                            break;
                        default:
                            logger.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                                        notification.getEventType());
                            return;
                    }
                    new JobEmailNotification(js, notification, dbManager).checkAndSendAsync(withAttachment);
                } finally {
                    js.writeUnlock();
                }
            }
        });
        dispatchJobStateUpdated(owner, notification);
    }

    @Override
    public void jobUpdatedFullData(JobState jobstate) {
        ClientJobState storedJobState = new ClientJobState(jobstate);
        dispatchJobUpdatedFullData(storedJobState);
    }

    @Override
    public void taskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        ClientJobState jobState = getClientJobState(notification.getData().getJobId());
        if (jobState != null) {
            try {
                jobState.writeLock();
                jobState.update(notification.getData());
                switch (notification.getEventType()) {
                    case TASK_PENDING_TO_RUNNING:
                    case TASK_RUNNING_TO_FINISHED:
                    case TASK_WAITING_FOR_RESTART:
                    case TASK_IN_ERROR:
                    case TASK_SKIPPED:
                    case TASK_REPLICATED:
                    case TASK_IN_ERROR_TO_FINISHED:
                        dispatchTaskStateUpdated(owner, notification);
                        break;
                    case TASK_PROGRESS:
                    case TASK_VISU_ACTIVATED:
                        // these events can be sent while task is already finished,
                        // as it is not a correct behavior, event is dropped if task is
                        // already finished.
                        // so if task is not finished, send event
                        if (notification.getData().getFinishedTime() <= 0) {
                            dispatchTaskStateUpdated(owner, notification);
                        }
                        break;
                    default:
                        logger.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                                    notification.getEventType());
                }
            } finally {
                jobState.writeUnlock();
            }
        }
    }

    @Override
    public void usersUpdated(NotificationData<UserIdentification> notification) {
        switch (notification.getEventType()) {
            case USERS_UPDATE:
                dispatchUsersUpdated(notification, true);
                break;
            default:
                logger.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                            notification.getEventType());
        }
    }

    public String getCurrentUser() throws NotConnectedException {
        Pair<ListeningUser, UserIdentificationImpl> userSessionInfo = renewSession(false);
        return userSessionInfo.getRight().getUsername();
    }

    public Subject getSubject() throws NotConnectedException {
        Pair<ListeningUser, UserIdentificationImpl> userSessionInfo = renewSession(false);
        return userSessionInfo.getRight().getSubject();
    }

    public UserData getCurrentUserData() throws NotConnectedException {
        Pair<ListeningUser, UserIdentificationImpl> userSessionInfo = renewSession(false);
        UserData userData = new UserData();
        userData.setUserName(userSessionInfo.getRight().getUsername());
        userData.setGroups(userSessionInfo.getRight().getGroups());
        return userData;
    }

    List<SchedulerUserInfo> getUsers() {
        return identifications.values().stream().map(userAndCredentials -> {
            ListeningUser listeningUser = userAndCredentials.getListeningUser();
            UserIdentificationImpl user = listeningUser.getUser();
            return new SchedulerUserInfo(user.getHostName(),
                                         user.getUsername(),
                                         user.getConnectionTime(),
                                         user.getLastSubmitTime(),
                                         user.getSubmitNumber());
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getSchedulerProperties() throws NotConnectedException {
        renewSession(false);
        return PASchedulerProperties.getPropertiesAsHashMap();
    }

    ClientJobState getClientJobState(JobId jobId) {
        return Lambda.withLock(stateReadLock, () -> {
            if (!jobsMap.containsKey(jobId)) {
                if (!finishedJobsLRUCache.containsKey(jobId)) {
                    List<InternalJob> internalJobs = dbManager.loadInternalJob(jobId.longValue());
                    if (!internalJobs.isEmpty()) {
                        InternalJob internalJob = internalJobs.get(0);
                        ClientJobState clientJobState = new ClientJobState(internalJob);
                        finishedJobsLRUCache.put(jobId, clientJobState);
                    }
                }
                return finishedJobsLRUCache.get(jobId);
            } else {
                return jobsMap.get(jobId);
            }
        });
    }

    IdentifiedJob toIdentifiedJob(ClientJobState clientJobState) {
        UserIdentificationImpl uIdent = new UserIdentificationImpl(clientJobState.getOwner());
        return new IdentifiedJob(clientJobState.getId(), uIdent, clientJobState.getGenericInformation());
    }

    TaskStatesPage getTaskPaginated(JobId jobId, int offset, int limit)
            throws UnknownJobException, NotConnectedException, PermissionException {
        checkPermissions("getJobState",
                         getIdentifiedJob(jobId),
                         YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB);
        ClientJobState jobState = getClientJobState(jobId);
        if (jobState == null) {
            throw new UnknownJobException(jobId);
        }
        try {
            jobState.readLock();
            try {
                final TaskStatesPage tasksPaginated = jobState.getTasksPaginated(offset, limit);
                final List<TaskState> taskStatesCopy = (List<TaskState>) ProActiveMakeDeepCopy.WithProActiveObjectStream.makeDeepCopy(new ArrayList<>(tasksPaginated.getTaskStates()));
                return new TaskStatesPage(taskStatesCopy, tasksPaginated.getSize());
            } catch (Exception e) {
                logger.error("Error when copying tasks page", e);
                throw new IllegalStateException(e);
            }
        } finally {
            jobState.readUnlock();
        }
    }

    public TaskStatesPage getTaskPaginated(JobId jobId, String statusFilter, int offset, int limit)
            throws UnknownJobException, NotConnectedException, PermissionException {
        checkPermissions("getJobState",
                         getIdentifiedJob(jobId),
                         YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB);
        ClientJobState jobState = getClientJobState(jobId);
        if (jobState == null) {
            throw new UnknownJobException(jobId);
        }
        try {
            jobState.readLock();
            try {
                final TaskStatesPage tasksPaginated = jobState.getTasksPaginated(statusFilter, offset, limit);
                final List<TaskState> taskStatesCopy = (List<TaskState>) ProActiveMakeDeepCopy.WithProActiveObjectStream.makeDeepCopy(new ArrayList<>(tasksPaginated.getTaskStates()));
                return new TaskStatesPage(taskStatesCopy, tasksPaginated.getSize());
            } catch (Exception e) {
                logger.error("Error when copying tasks page", e);
                throw new IllegalStateException(e);
            }
        } finally {
            jobState.readUnlock();
        }
    }

    public static class UserAndCredentials {

        private ListeningUser listeningUser;

        public UserAndCredentials(ListeningUser listeningUser, Credentials credentials) {
            this.listeningUser = listeningUser;
            this.credentials = credentials;
        }

        private Credentials credentials;

        public ListeningUser getListeningUser() {
            return listeningUser;
        }

        public Credentials getCredentials() {
            return credentials;
        }
    }
}
