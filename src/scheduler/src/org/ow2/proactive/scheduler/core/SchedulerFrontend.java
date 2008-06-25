/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.scheduler.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerConnection;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerInitialState;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerUsers;
import org.ow2.proactive.scheduler.common.scheduler.Stats;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.JobDescriptor;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.policy.PolicyInterface;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;


/**
 * Scheduler Front-end. This is the API to talk to when you want to managed a scheduler core.
 * Creating this class can only be done by using <code>AdminScheduler</code>.
 * You can join this front-end by using the <code>join()</code> method
 * in {@link SchedulerConnection} .
 *
 * @author The ProActive Team
 * @version 3.9, Jun 28, 2007
 * @since ProActive 3.9
 */
public class SchedulerFrontend implements InitActive, SchedulerEventListener<InternalJob>,
        AdminSchedulerInterface {

    /** Scheduler logger */
    public static final Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    /** A repeated  warning message */
    private static final String ACCESS_DENIED = "Access denied !";

    /** Mapping on the UniqueId of the sender and the user/admin identifications */
    private HashMap<UniqueID, UserIdentificationImpl> identifications = new HashMap<UniqueID, UserIdentificationImpl>();

    /** List of connected user */
    private SchedulerUsers connectedUsers = new SchedulerUsers();

    /** Implementation of Resource Manager */
    private transient ResourceManagerProxy resourceManager;

    /** Authentication Interface */
    private SchedulerAuthentication authenticationInterface;

    /** Full name of the policy class */
    private String policyFullName;

    /** Path to the database configuration file */
    private String dataBaseConfigFile;

    /** Implementation of scheduler main structure */
    private transient SchedulerCore scheduler;

    /** Job identification management */
    private HashMap<JobId, IdentifiedJob> jobs;

    /** scheduler listeners */
    private HashMap<UniqueID, SchedulerEventListener<? extends Job>> schedulerListeners = new HashMap<UniqueID, SchedulerEventListener<? extends Job>>();

    /** Scheduler's statistics */
    private StatsImpl stats = new StatsImpl();

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
     * Scheduler Proxy constructor.
     *
     * @param configFile the file that contains the description of the database.
     * @param imp a resource manager which
     *                                 be able to managed the resource used by scheduler.
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */
    public SchedulerFrontend(String configFile, ResourceManagerProxy imp, String policyFullClassName)
            throws ActiveObjectCreationException, NodeException {
        logger.info("Creating scheduler core...");
        dataBaseConfigFile = configFile;
        resourceManager = imp;
        policyFullName = policyFullClassName;
        jobs = new HashMap<JobId, IdentifiedJob>();
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            scheduler = (SchedulerCore) PAActiveObject.newActive(SchedulerCore.class.getName(), new Object[] {
                    dataBaseConfigFile, resourceManager, PAActiveObject.getStubOnThis(), policyFullName });
            logger.info("Scheduler successfully created on " +
                PAActiveObject.getNode().getNodeInformation().getVMInformation().getHostName());
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ################################### SCHEDULING MANAGEMENT ################################# */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * Connect the scheduler front-end to the scheduler authentication.
     */
    public void connect() {
        authenticationInterface = (SchedulerAuthentication) PAActiveObject.getContext().getStubOnCaller();
    }

    /**
     * Called by the scheduler core to recover the front-end.
     * This method may have to rebuild the different list of userIdentification
     * and job/user association.
     */
    public void recover(HashMap<JobId, InternalJob> jobList) {
        if (jobList != null) {
            for (Entry<JobId, InternalJob> e : jobList.entrySet()) {
                UserIdentificationImpl uIdent = new UserIdentificationImpl(e.getValue().getOwner());
                IdentifiedJob ij = new IdentifiedJob(e.getKey(), uIdent);
                jobs.put(e.getKey(), ij);

                //if the job is finished set it
                switch (e.getValue().getState()) {
                    case CANCELLED:
                    case FINISHED:
                    case FAILED:
                        ij.setFinished(true);
                        break;
                }
            }
        }

        //once recovered, activate scheduler communication
        authenticationInterface.activate();
    }

    /**
     * Connect a new user on the scheduler.
     * This user can interact with the scheduler according to his right.
     *
     * @param sourceBodyID the source ID of the connected object representing a user
     * @param identification the identification of the connected user
     */
    public void connect(UniqueID sourceBodyID, UserIdentificationImpl identification)
            throws SchedulerException {
        if (identifications.containsKey(sourceBodyID)) {
            logger.warn("Active object already connected !");
            throw new SchedulerException("This active object is already connected to the scheduler !");
        }

        logger.info(identification.getUsername() + " successfully connected !");
        identifications.put(sourceBodyID, identification);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#submit(org.ow2.proactive.scheduler.common.job.Job)
     */
    public JobId submit(Job userJob) throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        //check if the scheduler is stopped
        if (!scheduler.isSubmitPossible()) {
            throw new SchedulerException("Scheduler is stopped, cannot submit job !!");
        }

        //get the internal job.
        InternalJob job = InternalJobFactory.createJob(userJob);

        //setting job informations
        if (job.getTasks().size() == 0) {
            throw new SchedulerException(
                "This job does not contain Tasks !! Insert tasks before submitting job.");
        }

        //verifying that the user has right to set the given priority to his job. 
        if (!identifications.get(id).isAdmin()) {
            if ((job.getPriority().getPriority() > 3) || (job.getPriority() == JobPriority.IDLE)) {
                throw new SchedulerException("Only the administrator can submit a job with such priority : " +
                    job.getPriority());
            }
        }

        UserIdentificationImpl ident = identifications.get(id);
        //setting the job properties
        job.setId(JobId.nextId(job.getName()));
        job.setOwner(ident.getUsername());
        //prepare tasks in order to be send into the core
        job.prepareTasks();
        //put the job inside the frontend management list
        jobs.put(job.getId(), new IdentifiedJob(job.getId(), ident));
        //make the job descriptor
        job.setJobDescriptor(new JobDescriptor(job));
        scheduler.submit(job);
        //increase number of submit for this user
        ident.addSubmit();
        //send update user event only if the user is in the list of connected users.
        if (connectedUsers.getUsers().contains(ident)) {
            usersUpdate(ident);
        }
        //stats
        stats.increaseSubmittedJobCount(job.getType());
        stats.submitTime();
        return job.getId();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException {
        //checking permissions
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            throw new SchedulerException("The job represented by this ID is unknow !");
        }

        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException("You do not have permission to access this job !");
        }

        if (!ij.isFinished()) {
            return null;
        }

        //asking the scheduler for the result
        JobResult result = scheduler.getJobResult(jobId);

        if (result == null) {
            throw new SchedulerException("The result of this job is no longer available !");
        }

        return result;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#remove(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public void remove(JobId jobId) throws SchedulerException {
        //checking permissions
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            throw new SchedulerException("The job represented by this ID is unknow !");
        }

        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException("You do not have permission to remove this job !");
        }

        //asking the scheduler for the result
        scheduler.remove(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#getTaskResult(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String)
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws SchedulerException {
        //checking permissions
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            throw new SchedulerException("The job represented by this ID is unknow !");
        }

        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException("You do not have permission to access this job !");
        }

        //asking the scheduler for the result
        TaskResult result = scheduler.getTaskResult(jobId, taskName);

        if (result == null) {
            throw new SchedulerException(
                "Error while getting the result of this task !\nProblems may be :\n\t The task name you try to join is incorrect,\n\t the task you want to join is not finished.");
        }

        return result;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#listenLog(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String, int)
     */
    public void listenLog(JobId jobId, String hostname, int port) throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            throw new SchedulerException("The job represented by this ID is unknow !");
        }

        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException("You do not have permission to listen the log of this job !");
        }

        scheduler.listenLog(jobId, hostname, port);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#addSchedulerEventListener(org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener)
     */
    public SchedulerInitialState<? extends Job> addSchedulerEventListener(
            SchedulerEventListener<? extends Job> sel, SchedulerEvent... events) throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        UserIdentificationImpl uIdent = identifications.get(id);

        if (events.length > 0) {
            uIdent.setUserEvents(events);
        }
        //put this new user in the list of connected user
        connectedUsers.addUser(uIdent);
        usersUpdate(uIdent);
        //add the listener to the list of listener for this user.
        schedulerListeners.put(id, sel);
        //get the initialState
        SchedulerInitialState<? extends Job> initState = scheduler.getSchedulerInitialState();
        //and update the connected users list.
        initState.setUsers(connectedUsers);
        //return to the user
        return initState;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#getStats()
     */
    public Stats getStats() throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        return stats;
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ##################################### SCHEDULER ORDERS #################################### */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * Factoring for the next 7 scheduler orders.
     *
     * @param permissionMsg the message to log if an error occurs.
     * @return true if order can continue, false if not.
     */
    private boolean ssprsc(String permissionMsg) {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            logger.warn(ACCESS_DENIED);

            return false;
        }

        if (!identifications.get(id).isAdmin()) {
            logger.warn(permissionMsg);

            return false;
        }

        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#start()
     */
    public BooleanWrapper start() throws SchedulerException {
        if (!ssprsc("You do not have permission to start the scheduler !")) {
            return new BooleanWrapper(false);
        }

        //stats
        stats.startTime();

        return scheduler.start();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#stop()
     */
    public BooleanWrapper stop() throws SchedulerException {
        if (!ssprsc("You do not have permission to stop the scheduler !")) {
            return new BooleanWrapper(false);
        }

        //stats
        stats.stopTime();

        return scheduler.stop();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#pause()
     */
    public BooleanWrapper pause() throws SchedulerException {
        if (!ssprsc("You do not have permission to pause the scheduler !")) {
            return new BooleanWrapper(false);
        }

        //stats
        stats.pauseTime();

        return scheduler.pause();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#freeze()
     */
    public BooleanWrapper freeze() throws SchedulerException {
        if (!ssprsc("You do not have permission to pause the scheduler !")) {
            return new BooleanWrapper(false);
        }

        //stats
        stats.pauseTime();

        return scheduler.freeze();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#resume()
     */
    public BooleanWrapper resume() throws SchedulerException {
        if (!ssprsc("You do not have permission to resume the scheduler !")) {
            return new BooleanWrapper(false);
        }

        return scheduler.resume();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#shutdown()
     */
    public BooleanWrapper shutdown() throws SchedulerException {
        if (!ssprsc("You do not have permission to shutdown the scheduler !")) {
            return new BooleanWrapper(false);
        }

        return scheduler.shutdown();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#kill()
     */
    public BooleanWrapper kill() throws SchedulerException {
        if (!ssprsc("You do not have permission to kill the scheduler !")) {
            return new BooleanWrapper(false);
        }

        return scheduler.kill();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#disconnect()
     */
    public void disconnect() throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        String user = identifications.get(id).getUsername();
        schedulerListeners.remove(id);
        UserIdentificationImpl ident = identifications.remove(id);
        //remove this user to the list of connected user
        ident.setToRemove();
        connectedUsers.update(ident);
        usersUpdate(ident);
        logger.info("User " + user + " has left the scheduler !");
    }

    /**
     * Factoring of exception management for the 4 next jobs order.
     *
     * @param jobId the jobId concerned by the order.
     * @param permissionMsg the message to send the user if he has no right.
     * @throws SchedulerException the exception send if there is a problem of authentication.
     */
    private void prkcp(JobId jobId, String permissionMsg) throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            throw new SchedulerException("The job represented by this ID is unknow !");
        }

        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException(permissionMsg);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#pause(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to pause this job !");

        return scheduler.pause(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#resume(org.ow2.proactive.scheduler.job.JobId)
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to resume this job !");

        return scheduler.resume(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#kill(org.ow2.proactive.scheduler.job.JobId)
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to kill this job !");

        return scheduler.kill(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface#changePriority(org.ow2.proactive.scheduler.job.JobId, javax.print.attribute.standard.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority) throws SchedulerException {
        prkcp(jobId, "You do not have permission to change the priority of this job !");

        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());

        if (!ui.isAdmin()) {
            if (priority == JobPriority.HIGHEST) {
                throw new SchedulerException("Only an administrator can change the priority to HIGHEST");
            } else if (priority == JobPriority.HIGH) {
                throw new SchedulerException("Only an administrator can change the priority to HIGH");
            } else if (priority == JobPriority.IDLE) {
                throw new SchedulerException("Only an administrator can change the priority to IDLE");
            }
        }

        scheduler.changePriority(jobId, priority);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#changePolicy(java.lang.Class)
     */
    public BooleanWrapper changePolicy(Class<? extends PolicyInterface> newPolicyFile)
            throws SchedulerException {
        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());

        if (!ui.isAdmin()) {
            throw new SchedulerException("You do not have permission to change the policy of the scheduler !");
        }
        policyFullName = newPolicyFile.getClass().getName();
        return scheduler.changePolicy(newPolicyFile);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface#linkResourceManager(java.lang.String)
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());

        if (!ui.isAdmin()) {
            throw new SchedulerException("You do not have permission to reconnect a RM to the scheduler !");
        }
        return scheduler.linkResourceManager(rmURL);
    }

    /**
     * Terminate the schedulerConnexion active object and then this object.
     */
    public boolean terminate() {
        if (authenticationInterface != null) {
            authenticationInterface.terminate();
        }

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
     * Events dispatcher.
     * This method will invoke the given method on every listener.
     * The method is selected with the class type arguments and an optional parameter.
     * WARNING : in case of re-factoring, make sure that the textual occurrences of method names are modify as well.
     *
     * @param methodName a string representing the name of the method to invoke.
     * @param types the class type to select the right method.
     * @param params the arguments to send to the method.
     */
    private void dispatch(SchedulerEvent methodName, Class<?>[] types, Object... params) {
        try {
            Method method = SchedulerEventListener.class.getMethod(methodName.toString(), types);
            Iterator<UniqueID> iter = schedulerListeners.keySet().iterator();

            while (iter.hasNext()) {
                UniqueID id = iter.next();

                try {
                    UserIdentificationImpl userId = identifications.get(id);

                    if ((userId.getUserEvents() == null) || userId.getUserEvents().contains(methodName)) {
                        method.invoke(schedulerListeners.get(id), params);
                    }
                } catch (Exception e) {
                    iter.remove();
                    UserIdentificationImpl ident = identifications.remove(id);
                    //remove this user to the list of connected user
                    ident.setToRemove();
                    connectedUsers.update(ident);
                    usersUpdate(ident);
                    logger.error("!! Scheduler has detected that a listener is not connected anymore !!");
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerFrozenEvent()
     */
    public void schedulerFrozenEvent() {
        dispatch(SchedulerEvent.FROZEN, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerPausedEvent()
     */
    public void schedulerPausedEvent() {
        dispatch(SchedulerEvent.PAUSED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerResumedEvent()
     */
    public void schedulerResumedEvent() {
        dispatch(SchedulerEvent.RESUMED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerShutDownEvent()
     */
    public void schedulerShutDownEvent() {
        dispatch(SchedulerEvent.SHUTDOWN, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerShuttingDownEvent()
     */
    public void schedulerShuttingDownEvent() {
        dispatch(SchedulerEvent.SHUTTING_DOWN, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerStartedEvent()
     */
    public void schedulerStartedEvent() {
        dispatch(SchedulerEvent.STARTED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerStoppedEvent()
     */
    public void schedulerStoppedEvent() {
        dispatch(SchedulerEvent.STOPPED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerkilledEvent()
     */
    public void schedulerKilledEvent() {
        dispatch(SchedulerEvent.KILLED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobKilledEvent(org.ow2.proactive.scheduler.job.JobId)
     */
    public void jobKilledEvent(JobId jobId) {
        dispatch(SchedulerEvent.JOB_KILLED, new Class<?>[] { JobId.class }, jobId);
        jobs.remove(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobPausedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobPausedEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_PAUSED, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobResumedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobResumedEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_RESUMED, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#newPendingJobEvent(org.ow2.proactive.scheduler.job.JobU)
     */
    public void jobSubmittedEvent(InternalJob job) {
        dispatch(SchedulerEvent.JOB_SUBMITTED, new Class<?>[] { Job.class }, job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#pendingToRunningJobEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobPendingToRunningEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_PENDING_TO_RUNNING, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#runningToFinishedJobEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobRunningToFinishedEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_RUNNING_TO_FINISHED, new Class<?>[] { JobEvent.class }, event);
        jobs.get(event.getJobId()).setFinished(true);
        //stats
        stats.increaseFinishedJobCount(event.getNumberOfFinishedTasks());
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#removeFinishedJobEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobRemoveFinishedEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_REMOVE_FINISHED, new Class<?>[] { JobEvent.class }, event);
        //removing jobs from the global list : this job is no more managed
        jobs.remove(event.getJobId());
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#pendingToRunningTaskEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskPendingToRunningEvent(TaskEvent event) {
        dispatch(SchedulerEvent.TASK_PENDING_TO_RUNNING, new Class<?>[] { TaskEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#runningToFinishedTaskEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskRunningToFinishedEvent(TaskEvent event) {
        dispatch(SchedulerEvent.TASK_RUNNING_TO_FINISHED, new Class<?>[] { TaskEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#taskWaitingForRestart(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskWaitingForRestart(TaskEvent event) {
        dispatch(SchedulerEvent.TASK_WAITING_FOR_RESTART, new Class<?>[] { TaskEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#changeJobPriorityEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobChangePriorityEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_CHANGE_PRIORITY, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerRMDownEvent()
     */
    public void schedulerRMDownEvent() {
        dispatch(SchedulerEvent.RM_DOWN, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerRMUpEvent()
     */
    public void schedulerRMUpEvent() {
        dispatch(SchedulerEvent.RM_UP, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#usersUpdate(org.ow2.proactive.scheduler.common.job.UserIdentification)
     */
    public void usersUpdate(UserIdentification userIdentification) {
        dispatch(SchedulerEvent.USERS_UPDATE, new Class<?>[] { UserIdentification.class }, userIdentification);
    }
}
