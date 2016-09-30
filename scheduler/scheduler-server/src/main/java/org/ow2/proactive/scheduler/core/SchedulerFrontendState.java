/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.mop.MOP;
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
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
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
import org.ow2.proactive.scheduler.permissions.HandleOnlyMyJobsPermission;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;


class SchedulerFrontendState implements SchedulerStateUpdate {

    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATUS = "You do not have permission to get the status !";
    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_ADD_A_LISTENER = "You do not have permission to add a listener !";
    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_CHANGE_THE_PRIORITY_OF_THIS_JOB = "You do not have permission to change the priority of this job !";
    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE = "You do not have permission to get the state !";
    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_TASK = "You do not have permission to get the state of this task !";
    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB = "You do not have permission to get the state of this job !";
    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_LOGS_OF_THIS_JOB = "You do not have permission to get the task logs of this job !";
    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK = "You do not have permission to restart this task !";
    public static final String YOU_DO_NOT_HAVE_PERMISSION_TO_PREEMPT_THIS_TASK = "You do not have permission to preempt this task !";
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
    private static final long USER_SESSION_DURATION = PASchedulerProperties.SCHEDULER_USER_SESSION_TIME
            .getValueAsInt() *
        1000;

    /** Stores methods that will be called on clients */
    private static final Map<String, Method> eventMethods;

    static {
        eventMethods = new HashMap<>();
        for (Method m : SchedulerEventListener.class.getMethods()) {
            eventMethods.put(m.getName(), m);
        }
    }

    /**
     * Mapping on the UniqueId of the sender and the user/admin identifications
     */
    private final Map<UniqueID, ListeningUser> identifications;

    /** Map that link uniqueID to user credentials */
    private final Map<UniqueID, Credentials> credentials;

    /** List used to mark the user that does not respond anymore */
    private final Set<UniqueID> dirtyList;

    /** Job identification management */
    private final Map<JobId, IdentifiedJob> jobs;

    /** Session timer */
    private final Timer sessionTimer;

    /** JMX Helper reference */
    private final SchedulerJMXHelper jmxHelper;

    /**
     * Scheduler state maintains by this class : avoid charging the core from
     * some request
     */
    private final SchedulerStateImpl sState;

    private final Map<JobId, JobState> jobsMap;

    SchedulerFrontendState(SchedulerStateImpl sState, SchedulerJMXHelper jmxHelper) {
        this.identifications = new HashMap<>();
        this.credentials = new HashMap<>();
        this.dirtyList = new HashSet<>();
        this.jmxHelper = jmxHelper;
        this.jobsMap = new HashMap<>();
        this.jobs = new HashMap<>();
        this.sessionTimer = new Timer("SessionTimer");
        this.sState = sState;
        recover(sState);
    }

    /**
     * Called to recover the front end state. This method may have to rebuild
     * the different list of userIdentification and job/user association.
     */
    private void recover(SchedulerStateImpl sState) {
        Vector<JobState> pendingJobs = sState.getPendingJobs();
        Vector<JobState> runningJobs = sState.getRunningJobs();
        Vector<JobState> finishedJobs = sState.getFinishedJobs();

        // default state = started
        Set<JobState> jobStates = new HashSet<>(
            pendingJobs.size() + runningJobs.size() + finishedJobs.size());

        if (logger.isInfoEnabled()) {
            logger.info("#Pending jobs: " + pendingJobs.size() + " #Running jobs: " + runningJobs.size() +
                " #Finished jobs: " + finishedJobs.size());
        }

        for (JobState js : pendingJobs) {
            prepare(jobStates, js, false);
        }
        for (JobState js : runningJobs) {
            prepare(jobStates, js, false);
        }
        for (JobState js : finishedJobs) {
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
    private void prepare(Set<JobState> jobStates, JobState js, boolean finished) {
        jobStates.add(js);
        UserIdentificationImpl uIdent = new UserIdentificationImpl(js.getOwner());
        IdentifiedJob ij = new IdentifiedJob(js.getId(), uIdent);
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
    synchronized void connect(UniqueID sourceBodyID, UserIdentificationImpl identification, Credentials cred)
            throws AlreadyConnectedException {
        if (identifications.containsKey(sourceBodyID)) {
            logger.warn("Active object already connected for this user :" + identification.getUsername());
            throw new AlreadyConnectedException("This active object is already connected to the scheduler !");
        }
        logger.info(identification.getUsername() + " successfully connected !");
        identifications.put(sourceBodyID, new ListeningUser(identification));
        credentials.put(sourceBodyID, cred);
        renewUserSession(sourceBodyID, identification);
        // add this new user in the list of connected user
        sState.getUsers().update(identification);
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
        if (identifications.get(id).isListening()) {
            // if this id has a listener, do not renew user session
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

    synchronized SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        // checking permissions
        checkPermission("getStatus", YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATUS);

        return sState.getStatus();
    }

    synchronized SchedulerState getState() throws NotConnectedException, PermissionException {
        return getState(false);
    }

    synchronized SchedulerState getState(boolean myJobsOnly)
            throws NotConnectedException, PermissionException {
        // checking permissions
        checkPermission("getState", YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE);

        ListeningUser ui = identifications
                .get(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID());

        return myJobsOnly ? sState.filterOnUser(ui.getUser().getUsername()) : sState;

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
    synchronized void handleOnlyMyJobsPermission(boolean myOnly, UserIdentificationImpl ui,
            String errorMessage) throws PermissionException {
        ui.checkPermission(new HandleOnlyMyJobsPermission(myOnly), ui.getUsername() +
            " does not have permissions to handle other users jobs (" + errorMessage + ")");
    }

    synchronized void addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        addEventListener(sel, myEventsOnly, false, events);
    }

    synchronized SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            boolean getCurrentState, SchedulerEvent... events)
                    throws NotConnectedException, PermissionException {
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
            handleOnlyMyJobsPermission(myEventsOnly, uIdent.getUser(),
                    YOU_DO_NOT_HAVE_PERMISSION_TO_ADD_A_LISTENER);
        }
        // prepare user for receiving events
        uIdent.getUser().setUserEvents(events);
        // set if the user wants to get its events only or every events
        uIdent.getUser().setMyEventsOnly(myEventsOnly);
        // add the listener to the list of listener for this user.
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        uIdent.setListener(new ClientRequestHandler(this, id, sel));
        // cancel timer for this user : session is now managed by events
        uIdent.getUser().getSession().cancel();
        // return to the user
        return currentState;
    }

    synchronized void removeEventListener() throws NotConnectedException, PermissionException {
        // Remove the listener on that user designated by its given UniqueID,
        // then renew its user session as it is no more managed by the listener.
        UniqueID id = checkAccess();
        ListeningUser uIdent = identifications.get(id);
        uIdent.clearListener();
        // recreate the session for this user which is no more managed by
        // listener
        renewUserSession(id, uIdent.getUser());
    }

    private UniqueID checkAccess() throws NotConnectedException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        if (!identifications.containsKey(id)) {
            logger.info(ACCESS_DENIED);
            throw new NotConnectedException(ACCESS_DENIED);
        }
        return id;
    }

    synchronized InternalJob createJob(Job userJob, UserIdentificationImpl ident)
            throws NotConnectedException, PermissionException, SubmissionClosedException,
            JobCreationException {
        UniqueID id = checkAccess();

        // get the internal job.
        InternalJob job = InternalJobFactory.createJob(userJob, this.credentials.get(id));

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
                    ident.getUsername() + " does not have rights to set job priority " + job.getPriority());
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }
        // setting the job properties
        job.setOwner(ident.getUsername());

        return job;
    }

    synchronized void jobSubmitted(InternalJob job, UserIdentificationImpl ident)
            throws NotConnectedException, PermissionException, SubmissionClosedException,
            JobCreationException {
        // put the job inside the frontend management list
        jobs.put(job.getId(), new IdentifiedJob(job.getId(), ident));
        // increase number of submit for this user
        ident.addSubmit();
        // send update user event
        usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident));
        jlogger.info(job.getId(), "submitted: name '" + job.getName() + "', tasks '" +
            job.getTotalNumberOfTasks() + "', owner '" + job.getOwner() + "'");
        try {
            jlogger.info(job.getId(), job.display());
        } catch (Exception e) {
            jlogger.error(job.getId(), "Error while displaying the job :", e);
        }
    }

    synchronized ListeningUser checkPermissionReturningListeningUser(String methodName, String permissionMsg)
            throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        ListeningUser ident = identifications.get(id);
        // renew session for this user
        renewUserSession(id, ident.getUser());

        final String fullMethodName = SchedulerFrontend.class.getName() + "." + methodName;
        final MethodCallPermission methodCallPermission = new MethodCallPermission(fullMethodName);

        try {
            ident.getUser().checkPermission(methodCallPermission, permissionMsg);
        } catch (PermissionException ex) {
            logger.warn(permissionMsg);
            throw ex;
        }
        return ident;
    }

    synchronized UserIdentificationImpl checkPermission(String methodName, String permissionMsg)
            throws NotConnectedException, PermissionException {
        return checkPermissionReturningListeningUser(methodName, permissionMsg).getUser();
    }

    synchronized void disconnect() throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();
        disconnect(id);
    }

    /**
     * Disconnect a user, remove and clean user dependent lists and objects
     * 
     * @param id
     *            the uniqueID of the user
     */
    private synchronized void disconnect(UniqueID id) {
        credentials.remove(id);
        ListeningUser ident = identifications.remove(id);
        if (ident != null) {
            // remove listeners if needed
            ident.clearListener();
            // remove this user to the list of connected user if it has not
            // already been removed
            ident.getUser().setToRemove();
            sState.getUsers().update(ident.getUser());
            // cancel the timer
            ident.getUser().getSession().cancel();
            // log and send events
            String user = ident.getUser().getUsername();
            logger.info("User '" + user + "' has disconnect the scheduler !");
            dispatchUsersUpdated(
                    new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident.getUser()),
                    false);
        }
    }

    synchronized boolean isConnected() {
        try {
            checkAccess();
            return true;
        } catch (NotConnectedException nce) {
            return false;
        }
    }

    synchronized void renewSession() throws NotConnectedException {
        UniqueID id = checkAccess();
        UserIdentificationImpl ident = identifications.get(id).getUser();
        // renew session for this user
        renewUserSession(id, ident);
    }

    synchronized IdentifiedJob getIdentifiedJob(JobId jobId) throws UnknownJobException {
        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            String msg = "The job represented by this ID '" + jobId + "' is unknown !";
            logger.info(msg);
            throw new UnknownJobException(msg);
        }

        return ij;

    }

    synchronized void checkChangeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {

        checkPermissions("changeJobPriority", getIdentifiedJob(jobId),
                YOU_DO_NOT_HAVE_PERMISSION_TO_CHANGE_THE_PRIORITY_OF_THIS_JOB);

        UserIdentificationImpl ui = identifications
                .get(PAActiveObject.getContext().getCurrentRequest().getSourceBodyID()).getUser();

        try {
            ui.checkPermission(new ChangePriorityPermission(priority.getPriority()),
                    ui.getUsername() + " does not have permissions to set job priority to " + priority);
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }

        if (jobs.get(jobId).isFinished()) {
            String msg = " is already finished";
            jlogger.info(jobId, msg);
            throw new JobAlreadyFinishedException("Job " + jobId + msg);
        }
    }

    synchronized void checkPermissions(String methodName, IdentifiedJob identifiedJob, String errorMessage)
            throws NotConnectedException, UnknownJobException, PermissionException {
        try {
            checkJobOwner(methodName, identifiedJob, errorMessage);
        } catch (PermissionException pe) {
            UserIdentificationImpl ident = checkPermission(methodName, errorMessage);
            handleOnlyMyJobsPermission(false, ident, errorMessage);
        }
    }

    synchronized void checkJobOwner(String methodName, IdentifiedJob IdentifiedJob, String permissionMsg)
            throws NotConnectedException, UnknownJobException, PermissionException {
        ListeningUser ident = checkPermissionReturningListeningUser(methodName, permissionMsg);

        if (!IdentifiedJob.hasRight(ident.getUser())) {
            throw new PermissionException(permissionMsg);
        }
    }

    synchronized Set<TaskId> getJobTasks(JobId jobId) {
        JobState jobState = jobsMap.get(jobId);
        if (jobState == null) {
            return Collections.emptySet();
        } else {
            Set<TaskId> tasks = new HashSet<>(jobState.getTasks().size());
            for (TaskState task : jobState.getTasks()) {
                tasks.add(task.getId());
            }
            return tasks;
        }
    }

    synchronized JobState getJobState(JobId jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        checkPermissions("getJobState", getIdentifiedJob(jobId),
                YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB);
        return jobsMap.get(jobId);
    }

    synchronized TaskState getTaskState(JobId jobId, TaskId taskId)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        checkPermissions("getJobState", getIdentifiedJob(jobId),
                YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_TASK);
        if (jobsMap.get(jobId) == null) {
            throw new UnknownJobException(jobId);
        }
        TaskState ts = jobsMap.get(jobId).getHMTasks().get(taskId);
        if (ts == null) {
            throw new UnknownTaskException(taskId, jobId);
        }
        return ts;
    }

    synchronized TaskState getTaskState(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {

        checkPermissions("getJobState", getIdentifiedJob(jobId),
                YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_TASK);

        if (jobsMap.get(jobId) == null) {
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
        TaskState ts = jobsMap.get(jobId).getHMTasks().get(taskId);
        if (ts == null) {
            throw new UnknownTaskException(taskId, jobId);
        }
        return ts;
    }

    synchronized TaskId getTaskId(JobId jobId, String taskName)
            throws UnknownTaskException, UnknownJobException {
        if (jobsMap.get(jobId) == null) {
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

    synchronized void checkChangePolicy() throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        UserIdentificationImpl ident = identifications.get(id).getUser();
        // renew session for this user
        renewUserSession(id, ident);

        try {
            ident.checkPermission(new ChangePolicyPermission(),
                    ident.getUsername() + " does not have permissions to change the policy of the scheduler");
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }
    }

    synchronized void checkLinkResourceManager() throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        UserIdentificationImpl ident = identifications.get(id).getUser();
        // renew session for this user
        renewUserSession(id, ident);

        try {
            ident.checkPermission(new ConnectToResourceManagerPermission(),
                    ident.getUsername() + " does not have permissions to change RM in the scheduler");
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
            for (ListeningUser userId : identifications.values()) {
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
            for (ListeningUser listeningUserId : identifications.values()) {
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
                            listeningUserId.getListener()
                                    .addEvent(eventMethods.get(JOB_SUBMITTED_EVENT_METHOD), job);
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
                    logger.debug("job " + notification.getData().getJobId() + " event [" +
                        notification.getEventType() + "]");
                } else {
                    jlogger.debug(notification.getData().getJobId(),
                            " event [" + notification.getEventType() + "]");
                }
            }
            for (ListeningUser listeningUserId : identifications.values()) {
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
                            listeningUserId.getListener()
                                    .addEvent(eventMethods.get(JOB_STATE_UPDATED_EVENT_METHOD), notification);
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
    private void dispatchJobUpdatedFullData(JobState job) {
        try {
            if (logger.isDebugEnabled()) {
                jlogger.debug(job.getJobInfo().getJobId(), " event [" + SchedulerEvent.JOB_UPDATED + "]");
            }
            for (ListeningUser listeningUserId : identifications.values()) {
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
                            listeningUserId.getListener()
                                    .addEvent(eventMethods.get(JOB_UPDATED_FULL_DATA_EVENT_METHOD), job);
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
                tlogger.debug(notification.getData().getTaskId(),
                        "event [" + notification.getEventType() + "]");
            }
            for (ListeningUser listeningUserId : identifications.values()) {
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
                            listeningUserId.getListener().addEvent(
                                    eventMethods.get(TASK_STATE_UPDATED_EVENT_METHOD), notification);
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
    private void dispatchUsersUpdated(NotificationData<UserIdentification> notification,
            boolean checkForDownUser) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("event [" + notification.getEventType() + "]");
            }
            for (ListeningUser listeningUserId : identifications.values()) {
                // if this user has a listener
                if (listeningUserId.isListening()) {
                    UserIdentificationImpl userId = listeningUserId.getUser();
                    // if there is no specified event OR if the specified event
                    // is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        // if this userId have the myEventOnly=false or
                        // (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() || (userId.isMyEventsOnly() &&
                            userId.getUsername().equals(notification.getData().getUsername()))) {
                            listeningUserId.getListener()
                                    .addEvent(eventMethods.get(USERS_UPDATED_EVENT_METHOD), notification);
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
    public synchronized void schedulerStateUpdated(SchedulerEvent eventType) {
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
                logger.warn(
                        "**WARNING** - Unconsistent update type received from Scheduler Core : " + eventType);
                return;
        }
        // send the event for all case, except default
        dispatchSchedulerStateUpdated(eventType);
        this.jmxHelper.getSchedulerRuntimeMBean().schedulerStateUpdatedEvent(eventType);
    }

    @Override
    public synchronized void jobSubmitted(JobState job) {
        ClientJobState storedJobState = new ClientJobState(job);
        jobsMap.put(job.getId(), storedJobState);
        sState.getPendingJobs().add(storedJobState);
        dispatchJobSubmitted(job);
    }

    @Override
    public synchronized void jobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        JobState js = jobsMap.get(notification.getData().getJobId());
        js.update(notification.getData());
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_RUNNING:
                sState.getPendingJobs().remove(js);
                sState.getRunningJobs().add(js);
                break;
            case JOB_PAUSED:
            case JOB_IN_ERROR:
            case JOB_RESUMED:
            case JOB_RESTARTED_FROM_ERROR:
            case JOB_CHANGE_PRIORITY:
            case TASK_REPLICATED:
            case TASK_SKIPPED:
                break;
            case JOB_PENDING_TO_FINISHED:
                sState.getPendingJobs().remove(js);
                sState.getFinishedJobs().add(js);
                // set this job finished, user can get its result
                jobs.get(notification.getData().getJobId()).setFinished(true);
                break;
            case JOB_RUNNING_TO_FINISHED:
                sState.getRunningJobs().remove(js);
                sState.getFinishedJobs().add(js);
                // set this job finished, user can get its result
                jobs.get(notification.getData().getJobId()).setFinished(true);
                break;
            case JOB_REMOVE_FINISHED:
                // removing jobs from the global list : this job is no more managed
                sState.getFinishedJobs().remove(js);
                jobsMap.remove(js.getId());
                jobs.remove(notification.getData().getJobId());
                break;
            default:
                logger.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
                return;
        }
        dispatchJobStateUpdated(owner, notification);
        new JobEmailNotification(js, notification).checkAndSend();
    }

    @Override
    public synchronized void jobUpdatedFullData(JobState jobstate) {
        ClientJobState storedJobState = new ClientJobState(jobstate);
        dispatchJobUpdatedFullData(storedJobState);
    }

    @Override
    public synchronized void taskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        jobsMap.get(notification.getData().getJobId()).update(notification.getData());
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
            case TASK_RUNNING_TO_FINISHED:
            case TASK_WAITING_FOR_RESTART:
            case TASK_IN_ERROR:
                dispatchTaskStateUpdated(owner, notification);
                break;
            case TASK_PROGRESS:
                // this event can be sent while task is already finished,
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
    }

    @Override
    public synchronized void usersUpdated(NotificationData<UserIdentification> notification) {
        switch (notification.getEventType()) {
            case USERS_UPDATE:
                dispatchUsersUpdated(notification, true);
                break;
            default:
                logger.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
        }
    }

    synchronized List<SchedulerUserInfo> getUsers() {
        List<SchedulerUserInfo> users = new ArrayList<>(identifications.size());
        for (ListeningUser listeningUser : identifications.values()) {
            UserIdentificationImpl user = listeningUser.getUser();
            users.add(new SchedulerUserInfo(user.getHostName(), user.getUsername(), user.getConnectionTime(),
                user.getLastSubmitTime(), user.getSubmitNumber()));
        }
        return users;
    }

}