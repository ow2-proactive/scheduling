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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.permissions.ChangePolicyPermission;
import org.ow2.proactive.scheduler.permissions.ChangePriorityPermission;
import org.ow2.proactive.scheduler.permissions.ConnectToResourceManagerPermission;
import org.ow2.proactive.scheduler.permissions.GetOwnStateOnlyPermission;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;


class SchedulerFrontendState implements SchedulerStateUpdate {

    /** Scheduler logger */
    private static final Logger logger = Logger.getLogger(SchedulingService.class);
    private static final TaskLogger tlogger = TaskLogger.getInstance();
    private static final JobLogger jlogger = JobLogger.getInstance();

    /** A repeated warning message */
    private static final String ACCESS_DENIED = "Access denied ! You are not connected or your session has expired !";

    /** Maximum duration of a session for a useless client */
    private static final long USER_SESSION_DURATION = PASchedulerProperties.SCHEDULER_USER_SESSION_TIME
            .getValueAsInt() * 1000;

    /** Stores methods that will be called on clients */
    private static final Map<String, Method> eventMethods;
    static {
        eventMethods = new HashMap<String, Method>();
        for (Method m : SchedulerEventListener.class.getMethods()) {
            eventMethods.put(m.getName(), m);
        }
    }

    /** Mapping on the UniqueId of the sender and the user/admin identifications */
    private final Map<UniqueID, UserIdentificationImpl> identifications;

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

    /** Scheduler state maintains by this class : avoid charging the core from some request */
    private final SchedulerStateImpl sState;

    private final Map<JobId, JobState> jobsMap;

    SchedulerFrontendState(SchedulerStateImpl sState, SchedulerJMXHelper jmxHelper) {
        this.identifications = new HashMap<UniqueID, UserIdentificationImpl>();
        this.credentials = new HashMap<UniqueID, Credentials>();
        this.dirtyList = new HashSet<UniqueID>();
        this.jmxHelper = jmxHelper;
        this.jobsMap = new HashMap<JobId, JobState>();
        this.jobs = new HashMap<JobId, IdentifiedJob>();
        this.sessionTimer = new Timer("SessionTimer");

        this.sState = sState;
        recover(sState);
    }

    /**
     * Called to recover the front end state.
     * This method may have to rebuild the different list of userIdentification
     * and job/user association.
     */
    private void recover(SchedulerStateImpl sState) {
        //default state = started
        Set<JobState> jobStates = new HashSet<JobState>();
        logger.info("#Pending jobs list : " + sState.getPendingJobs().size());
        logger.info("#Running jobs list : " + sState.getRunningJobs().size());
        logger.info("#Finished jobs list : " + sState.getFinishedJobs().size());

        for (JobState js : sState.getPendingJobs()) {
            prepare(jobStates, js, false);
        }
        for (JobState js : sState.getRunningJobs()) {
            prepare(jobStates, js, false);
        }
        for (JobState js : sState.getFinishedJobs()) {
            prepare(jobStates, js, true);
        }
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
    synchronized void connect(UniqueID sourceBodyID, UserIdentificationImpl identification, Credentials cred)
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

    synchronized SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        //checking permissions
        checkPermission("getStatus", "You do not have permission to get the status !");

        return sState.getStatus();
    }

    synchronized SchedulerState getState() throws NotConnectedException, PermissionException {
        return getState(false);
    }

    synchronized SchedulerState getState(boolean myJobsOnly) throws NotConnectedException,
            PermissionException {
        //checking permissions
        checkPermission("getState", "You do not have permission to get the state !");

        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());
        try {
            checkOwnStatePermission(myJobsOnly, ui);
            return myJobsOnly ? sState.filterOnUser(ui.getUsername()) : sState;
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }
    }

    /**
     * Check if the given user can get the state as it is demanded (full or user only)
     *
     * @param myOnly true, if the user wants only its events or jobs, false if user want the full state
     * @param ui the user identification
     * @throws PermissionException if permission is denied
     */
    synchronized void checkOwnStatePermission(boolean myOnly, UserIdentificationImpl ui)
            throws PermissionException {
        ui.checkPermission(new GetOwnStateOnlyPermission(myOnly), ui.getUsername() +
            " does not have permissions to retrieve full state");
    }

    synchronized void addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        addEventListener(sel, myEventsOnly, false, events);
    }

    synchronized SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            boolean getCurrentState, SchedulerEvent... events) throws NotConnectedException,
            PermissionException {
        //checking permissions
        UserIdentificationImpl uIdent = checkPermission("addEventListener",
                "You do not have permission to add a listener !");

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

    synchronized void removeEventListener() throws NotConnectedException, PermissionException {
        //Remove the listener on that user designated by its given UniqueID,
        //then renew its user session as it is no more managed by the listener.
        UniqueID id = checkAccess();
        UserIdentificationImpl uIdent = identifications.get(id);
        uIdent.clearListener();
        //recreate the session for this user which is no more managed by listener
        renewUserSession(id, uIdent);
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

        //get the internal job.
        InternalJob job = InternalJobFactory.createJob(userJob, this.credentials.get(id));

        //setting job informations
        if (job.getTasks().size() == 0) {
            String msg = "This job does not contain Tasks !! Insert tasks before submitting job";
            logger.info(msg);
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
            logger.info(ex.getMessage());
            throw ex;
        }
        //setting the job properties
        job.setOwner(ident.getUsername());

        return job;
    }

    synchronized void jobSubmitted(InternalJob job, UserIdentificationImpl ident)
            throws NotConnectedException, PermissionException, SubmissionClosedException,
            JobCreationException {
        //put the job inside the frontend management list
        jobs.put(job.getId(), new IdentifiedJob(job.getId(), ident));
        //increase number of submit for this user
        ident.addSubmit();
        //send update user event
        usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident));
        jlogger.info(job.getId(), "submitted: name '" + job.getName() + "', tasks '" +
            job.getTotalNumberOfTasks() + "', owner '" + job.getOwner() + "'");
    }

    synchronized UserIdentificationImpl checkPermission(String methodName, String permissionMsg)
            throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        UserIdentificationImpl ident = identifications.get(id);
        //renew session for this user
        renewUserSession(id, ident);

        final String fullMethodName = SchedulerFrontend.class.getName() + "." + methodName;
        final MethodCallPermission methodCallPermission = new MethodCallPermission(fullMethodName);

        try {
            ident.checkPermission(methodCallPermission, permissionMsg);
        } catch (PermissionException ex) {
            logger.warn(permissionMsg);
            throw ex;
        }
        return ident;
    }

    synchronized void disconnect() throws NotConnectedException, PermissionException {
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
            logger.info("User '" + user + "' has disconnect the scheduler !");
            dispatchUsersUpdated(
                    new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident), false);
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
        UserIdentificationImpl ident = identifications.get(id);
        //renew session for this user
        renewUserSession(id, ident);
    }

    synchronized IdentifiedJob checkJobOwner(String methodName, JobId jobId, String permissionMsg)
            throws NotConnectedException, UnknownJobException, PermissionException {
        UserIdentificationImpl ident = checkPermission(methodName, permissionMsg);

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            String msg = "The job represented by this ID '" + jobId + "' is unknown !";
            logger.info(msg);
            throw new UnknownJobException(msg);
        }

        if (!ij.hasRight(ident)) {
            logger.info(permissionMsg);
            throw new PermissionException(permissionMsg);
        }

        return ij;
    }

    synchronized void checkChangeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        checkJobOwner("changeJobPriority", jobId,
                "You do not have permission to change the priority of this job !");

        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());

        try {
            ui.checkPermission(new ChangePriorityPermission(priority.getPriority()), ui.getUsername() +
                " does not have permissions to set job priority to " + priority);
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

    synchronized Set<TaskId> getJobTasks(JobId jobId) {
        JobState jobState = jobsMap.get(jobId);
        if (jobState == null) {
            return Collections.emptySet();
        } else {
            Set<TaskId> tasks = new HashSet<TaskId>(jobState.getTasks().size());
            for (TaskState task : jobState.getTasks()) {
                tasks.add(task.getId());
            }
            return tasks;
        }
    }

    synchronized JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        checkJobOwner("getJobState", jobId, "You do not have permission to get the state of this job !");
        return jobsMap.get(jobId);
    }

    synchronized void checkChangePolicy() throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        UserIdentificationImpl ident = identifications.get(id);
        //renew session for this user
        renewUserSession(id, ident);

        try {
            ident.checkPermission(new ChangePolicyPermission(), ident.getUsername() +
                " does not have permissions to change the policy of the scheduler");
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }
    }

    synchronized void checkLinkResourceManager() throws NotConnectedException, PermissionException {
        UniqueID id = checkAccess();

        UserIdentificationImpl ident = identifications.get(id);
        //renew session for this user
        renewUserSession(id, ident);

        try {
            ident.checkPermission(new ConnectToResourceManagerPermission(), ident.getUsername() +
                " does not have permissions to change RM in the scheduler");
        } catch (PermissionException ex) {
            logger.info(ex.getMessage());
            throw ex;
        }
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
        Set<UniqueID> toRemove;

        synchronized (dirtyList) {
            if (dirtyList.isEmpty()) {
                return;
            }
            toRemove = new HashSet<UniqueID>(dirtyList);
            dirtyList.clear();
        }

        for (UniqueID uId : toRemove) {
            disconnect(uId);
        }
    }

    /**
     * Put this is to be removed in the dirty list.
     * 
     * @param id the id of the user to be removed.
     */
    void markAsDirty(UniqueID id) {
        synchronized (dirtyList) {
            dirtyList.add(id);
        }
    }

    /**
     * Dispatch the scheduler state updated event
     * 
     * @param eventType the type of the concrete event
     */
    private void dispatchSchedulerStateUpdated(SchedulerEvent eventType) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("event [" + eventType.toString() + "]");
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
            logger.error("", e);
        }
    }

    /**
     * Dispatch the job submitted event
     * 
     * @param job the new submitted job
     */
    private void dispatchJobSubmitted(JobState job) {
        try {
            if (logger.isDebugEnabled()) {
                jlogger.debug(job.getJobInfo().getJobId(), " event [" + SchedulerEvent.JOB_SUBMITTED + "]");
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
                        logger.debug("", e);
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
     * @param owner the owner of this job
     * @param notification the data to send to every client
     */
    private void dispatchJobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        try {
            if (logger.isDebugEnabled()) {
                // if in process of job removal do not use jlogger as job log file
                // was already removed and it will create it again
                if (notification.getEventType() == SchedulerEvent.JOB_REMOVE_FINISHED) {
                    logger.debug("job " + notification.getData().getJobId() + " event [" +
                        notification.getEventType() + "]");
                } else {
                    jlogger.debug(notification.getData().getJobId(), " event [" +
                        notification.getEventType() + "]");
                }
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
            logger.error("", e);
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
            if (logger.isDebugEnabled()) {
                tlogger.debug(notification.getData().getTaskId(), "event [" + notification.getEventType() +
                    "]");
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
            logger.error("", e);
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
            if (logger.isDebugEnabled()) {
                logger.debug("event [" + notification.getEventType() + "]");
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
                logger.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                    eventType);
                return;
        }
        // send the event for all case, except default
        dispatchSchedulerStateUpdated(eventType);
        this.jmxHelper.getSchedulerRuntimeMBean().schedulerStateUpdatedEvent(eventType);
    }

    @Override
    public synchronized void jobSubmitted(JobState job) {
        JobState storedJobState = new ClientJobState(job);
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
                dispatchJobStateUpdated(owner, notification);
                break;
            case JOB_PAUSED:
            case JOB_RESUMED:
            case JOB_CHANGE_PRIORITY:
            case TASK_REPLICATED:
            case TASK_SKIPPED:
                dispatchJobStateUpdated(owner, notification);
                break;
            case JOB_PENDING_TO_FINISHED:
                sState.getPendingJobs().remove(js);
                sState.getFinishedJobs().add(js);
                //set this job finished, user can get its result
                jobs.get(notification.getData().getJobId()).setFinished(true);
                dispatchJobStateUpdated(owner, notification);
                break;
            case JOB_RUNNING_TO_FINISHED:
                sState.getRunningJobs().remove(js);
                sState.getFinishedJobs().add(js);
                //set this job finished, user can get its result
                jobs.get(notification.getData().getJobId()).setFinished(true);
                dispatchJobStateUpdated(owner, notification);
                break;
            case JOB_REMOVE_FINISHED:
                //removing jobs from the global list : this job is no more managed
                sState.getFinishedJobs().remove(js);
                jobsMap.remove(js.getId());
                jobs.remove(notification.getData().getJobId());
                dispatchJobStateUpdated(owner, notification);
                break;
            default:
                logger.warn("**WARNING** - Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
        }
    }

    @Override
    public synchronized void taskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        jobsMap.get(notification.getData().getJobId()).update(notification.getData());
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
            case TASK_RUNNING_TO_FINISHED:
            case TASK_WAITING_FOR_RESTART:
                dispatchTaskStateUpdated(owner, notification);
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

}
