/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerInitialState;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.Stats;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.job.InternalJobWrapper;
import org.ow2.proactive.scheduler.job.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * Scheduler Front-end. This is the API to talk to when you want to managed a scheduler core.
 * Creating this class can only be done by using <code>AdminScheduler</code>.
 * You can join this front-end by using the <code>join()</code> method
 * in {@link SchedulerConnection} .
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerFrontend implements InitActive, SchedulerEventListener<InternalJob>,
        AdminSchedulerInterface {

    /** Scheduler logger */
    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.FRONTEND);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.FRONTEND);

    /** A repeated  warning message */
    private static final String ACCESS_DENIED = "Access denied !";

    /** Mapping on the UniqueId of the sender and the user/admin identifications */
    private Map<UniqueID, UserIdentificationImpl> identifications = new HashMap<UniqueID, UserIdentificationImpl>();

    /** List of connected user */
    private SchedulerUsers connectedUsers = new SchedulerUsers();

    /** Implementation of Resource Manager */
    private transient ResourceManagerProxy resourceManager;

    /** Authentication Interface */
    private SchedulerAuthentication authentication;

    /** Full name of the policy class */
    private String policyFullName;

    /** Implementation of scheduler main structure */
    private transient SchedulerCore scheduler;

    /** Direct link to the current job to submit. */
    private InternalJobWrapper currentJobToSubmit = new InternalJobWrapper();

    /** Job identification management */
    private Map<JobId, IdentifiedJob> jobs;

    /** scheduler listeners */
    private Map<UniqueID, SchedulerEventListener<? extends Job>> schedulerListeners = new HashMap<UniqueID, SchedulerEventListener<? extends Job>>();

    /** Scheduler's statistics */
    private StatsImpl stats = new StatsImpl(SchedulerState.STARTED);

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
     * @param imp a resource manager which
     *                                 be able to managed the resource used by scheduler.
     * @param policyFullClassName the full class name of the policy to use.
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */
    public SchedulerFrontend(ResourceManagerProxy imp, String policyFullClassName)
            throws ActiveObjectCreationException, NodeException {
        logger_dev.info("Creating scheduler Front-end...");
        resourceManager = imp;
        policyFullName = policyFullClassName;
        logger_dev.debug("Policy used is " + policyFullClassName);
        jobs = new HashMap<JobId, IdentifiedJob>();
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            body.setImmediateService("getTaskResult");
            body.setImmediateService("getJobResult");
            logger_dev.debug("Front-end immediate services : getTaskResult,getJobResult");
            //scheduler URL
            String schedulerUrl = "//" + NodeFactory.getDefaultNode().getVMInformation().getHostName() + "/" +
                SchedulerConstants.SCHEDULER_DEFAULT_NAME;
            //creating scheduler authentication
            // creating the scheduler authentication interface.
            // if this fails then it will not continue.
            logger_dev.info("Creating scheduler authentication interface...");
            SchedulerAuthentication schedulerAuth = (SchedulerAuthentication) PAActiveObject.newActive(
                    SchedulerAuthentication.class.getName(), new Object[] { PAActiveObject.getStubOnThis() });
            //creating scheduler core
            logger_dev.info("Creating scheduler core...");
            SchedulerCore scheduler_local = new SchedulerCore(resourceManager,
                (SchedulerFrontend) PAActiveObject.getStubOnThis(), policyFullName, currentJobToSubmit);
            scheduler = (SchedulerCore) PAActiveObject.turnActive(scheduler_local);

            logger_dev.info("Registering scheduler...");
            PAActiveObject.register(schedulerAuth, schedulerUrl);
            // run !!
        } catch (Exception e) {
            logger_dev.error(e);
            System.exit(1);
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
        logger_dev.info(" ");//just trace method name
        authentication = (SchedulerAuthentication) PAActiveObject.getContext().getStubOnCaller();
    }

    /**
     * Called by the scheduler core to recover the front-end.
     * This method may have to rebuild the different list of userIdentification
     * and job/user association.
     * 
     * @param jobList the jobList that may appear in this front-end.
     */
    public void recover(Map<JobId, InternalJob> jobList) {
        logger_dev.info("job list : " + jobList);
        if (jobList != null) {
            for (Entry<JobId, InternalJob> e : jobList.entrySet()) {
                UserIdentificationImpl uIdent = new UserIdentificationImpl(e.getValue().getOwner());
                IdentifiedJob ij = new IdentifiedJob(e.getKey(), uIdent);
                jobs.put(e.getKey(), ij);

                //if the job is finished set it
                switch (e.getValue().getState()) {
                    case CANCELED:
                    case FINISHED:
                    case FAILED:
                        ij.setFinished(true);
                        break;
                }
            }
        }
        //once recovered, activate scheduler communication
        authentication.setActivated(true);
    }

    /**
     * Connect a new user on the scheduler.
     * This user can interact with the scheduler according to his right.
     *
     * @param sourceBodyID the source ID of the connected object representing a user
     * @param identification the identification of the connected user
     * @throws SchedulerException If an error occurred during connection with the front-end.
     */
    public void connect(UniqueID sourceBodyID, UserIdentificationImpl identification)
            throws SchedulerException {
        if (identifications.containsKey(sourceBodyID)) {
            logger.warn("Active object already connected for this user :" + identification.getUsername());
            throw new SchedulerException("This active object is already connected to the scheduler !");
        }
        logger.info(identification.getUsername() + " successfully connected !");
        identifications.put(sourceBodyID, identification);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#submit(org.ow2.proactive.scheduler.common.job.Job)
     */
    public JobId submit(Job userJob) throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        logger_dev.info("New job submission requested : " + userJob.getName());

        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }

        //check if the scheduler is stopped
        if (!scheduler.isSubmitPossible()) {
            String msg = "Scheduler is stopped, cannot submit job";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }
        //get the internal job.
        InternalJob job = InternalJobFactory.createJob(userJob);
        //setting job informations
        if (job.getTasks().size() == 0) {
            String msg = "This job does not contain Tasks !! Insert tasks before submitting job";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        //verifying that the user has right to set the given priority to his job. 
        if (!identifications.get(id).isAdmin()) {
            if ((job.getPriority().getPriority() > 3) || (job.getPriority() == JobPriority.IDLE)) {
                String msg = "Only the administrator can submit a job with such priority : " +
                    job.getPriority();
                logger_dev.info(msg);
                throw new SchedulerException(msg);
            }
        }
        logger_dev.info("Preparing and settings job submission");
        UserIdentificationImpl ident = identifications.get(id);
        //setting the job properties
        job.setId(JobIdImpl.nextId(job.getName()));
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
        //send update user event only if the user is in the list of connected users.
        if (connectedUsers.getUsers().contains(ident)) {
            usersUpdate(ident);
        }
        //stats
        stats.increaseSubmittedJobCount(job.getType());
        stats.submitTime();
        jobSubmittedEvent(job);
        logger.info("New job submitted '" + job.getId() + "' containing " + job.getTotalNumberOfTasks() +
            " tasks");
        return job.getId();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException {
        //checking permissions
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            String msg = "The job represented by this ID is unknow !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        if (!ij.hasRight(identifications.get(id))) {
            String msg = "You do not have permission to access this job !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        if (!ij.isFinished()) {
            logger_dev.info("Job '" + jobId + "' is not finished");
            return null;
        }

        //asking the scheduler for the result
        JobResult result = scheduler.getJobResult(jobId);

        return result;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getJobResult(java.lang.String)
     */
    public JobResult getJobResult(String jobId) throws SchedulerException {
        return this.getJobResult(JobIdImpl.makeJobId(jobId));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getTaskResult(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String)
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws SchedulerException {
        //checking permissions
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            String msg = "The job represented by this ID is unknow !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        if (!ij.hasRight(identifications.get(id))) {
            String msg = "You do not have permission to access this job !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        //asking the scheduler for the result
        TaskResult result = scheduler.getTaskResult(jobId, taskName);

        return result;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getTaskResult(java.lang.String, java.lang.String)
     */
    public TaskResult getTaskResult(String jobId, String taskName) throws SchedulerException {
        return this.getTaskResult(JobIdImpl.makeJobId(jobId), taskName);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#remove(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public void remove(JobId jobId) throws SchedulerException {
        //checking permissions
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            String msg = "The job represented by this ID is unknow !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        if (!ij.hasRight(identifications.get(id))) {
            String msg = "You do not have permission to remove this job !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        //asking the scheduler for the result
        scheduler.remove(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#listenLog(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String, int)
     */
    public void listenLog(JobId jobId, String hostname, int port) throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            String msg = "The job represented by this ID is unknow !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        if (!ij.hasRight(identifications.get(id))) {
            String msg = "You do not have permission to listen the log of this job !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        scheduler.listenLog(jobId, hostname, port);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#addSchedulerEventListener(org.ow2.proactive.scheduler.common.SchedulerEventListener, org.ow2.proactive.scheduler.common.SchedulerEvent[])
     */
    public SchedulerInitialState<? extends Job> addSchedulerEventListener(
            SchedulerEventListener<? extends Job> sel, SchedulerEvent... events) throws SchedulerException {

        // first check if the listener is a reified remote object
        if (!MOP.isReifiedObject(sel)) {
            String msg = "Scheduler listener must be a remote object !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        UserIdentificationImpl uIdent = identifications.get(id);

        if (uIdent == null) {
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }

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
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#removeSchedulerEventListener()
     */
    public void removeSchedulerEventListener() throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }
        schedulerListeners.remove(id);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getStats()
     */
    public Stats getStats() throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
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
            logger_dev.info(ACCESS_DENIED);
            return false;
        }

        if (!identifications.get(id).isAdmin()) {
            logger_dev.warn(permissionMsg);
            return false;
        }

        return true;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#start()
     */
    public BooleanWrapper start() throws SchedulerException {
        if (!ssprsc("You do not have permission to start the scheduler !")) {
            return new BooleanWrapper(false);
        }

        BooleanWrapper bool = scheduler.start();
        if (bool.booleanValue()) {
            //stats
            stats.startTime();
            stats.updateStatus(SchedulerState.STARTED);
        }
        return bool;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#stop()
     */
    public BooleanWrapper stop() throws SchedulerException {
        if (!ssprsc("You do not have permission to stop the scheduler !")) {
            return new BooleanWrapper(false);
        }

        BooleanWrapper bool = scheduler.stop();
        if (bool.booleanValue()) {
            //stats
            stats.stopTime();
            stats.updateStatus(SchedulerState.STOPPED);
        }
        return bool;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#pause()
     */
    public BooleanWrapper pause() throws SchedulerException {
        if (!ssprsc("You do not have permission to pause the scheduler !")) {
            return new BooleanWrapper(false);
        }

        BooleanWrapper bool = scheduler.pause();
        if (bool.booleanValue()) {
            //stats
            stats.pauseTime();
            stats.updateStatus(SchedulerState.PAUSED);
        }
        return bool;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#freeze()
     */
    public BooleanWrapper freeze() throws SchedulerException {
        if (!ssprsc("You do not have permission to pause the scheduler !")) {
            return new BooleanWrapper(false);
        }

        BooleanWrapper bool = scheduler.freeze();
        if (bool.booleanValue()) {
            //stats
            stats.pauseTime();
            stats.updateStatus(SchedulerState.FROZEN);
        }
        return bool;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#resume()
     */
    public BooleanWrapper resume() throws SchedulerException {
        if (!ssprsc("You do not have permission to resume the scheduler !")) {
            return new BooleanWrapper(false);
        }

        BooleanWrapper bool = scheduler.resume();
        if (bool.booleanValue()) {
            //stats
            stats.updateStatus(SchedulerState.STARTED);
        }
        return bool;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#shutdown()
     */
    public BooleanWrapper shutdown() throws SchedulerException {
        if (!ssprsc("You do not have permission to shutdown the scheduler !")) {
            return new BooleanWrapper(false);
        }

        BooleanWrapper bool = scheduler.shutdown();
        if (bool.booleanValue()) {
            //stats
            stats.updateStatus(SchedulerState.SHUTTING_DOWN);
        }
        return bool;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#kill()
     */
    public BooleanWrapper kill() throws SchedulerException {
        if (!ssprsc("You do not have permission to kill the scheduler !")) {
            return new BooleanWrapper(false);
        }

        BooleanWrapper bool = scheduler.kill();
        if (bool.booleanValue()) {
            //stats
            stats.updateStatus(SchedulerState.KILLED);
        }
        return bool;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#disconnect()
     */
    public void disconnect() throws SchedulerException {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();

        if (!identifications.containsKey(id)) {
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }

        String user = identifications.get(id).getUsername();
        schedulerListeners.remove(id);
        UserIdentificationImpl ident = identifications.remove(id);
        //remove this user to the list of connected user
        ident.setToRemove();
        connectedUsers.update(ident);
        usersUpdate(ident);
        logger_dev.info("User '" + user + "' has left the scheduler !");
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#isConnected()
     */
    public BooleanWrapper isConnected() {
        UniqueID id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        return new BooleanWrapper(identifications.containsKey(id));
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
            logger_dev.info(ACCESS_DENIED);
            throw new SchedulerException(ACCESS_DENIED);
        }

        IdentifiedJob ij = jobs.get(jobId);

        if (ij == null) {
            String msg = "The job represented by this ID is unknow !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        if (!ij.hasRight(identifications.get(id))) {
            logger_dev.info(permissionMsg);
            throw new SchedulerException(permissionMsg);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#pause(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to pause this job !");

        return scheduler.pause(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#resume(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to resume this job !");

        return scheduler.resume(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#kill(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to kill this job !");

        return scheduler.kill(jobId);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#changePriority(org.ow2.proactive.scheduler.common.job.JobId, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority) throws SchedulerException {
        prkcp(jobId, "You do not have permission to change the priority of this job !");

        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());

        if (!ui.isAdmin()) {
            if (priority == JobPriority.HIGHEST || priority == JobPriority.HIGH ||
                priority == JobPriority.IDLE) {
                String msg = "Only an administrator can change the priority to " +
                    priority.toString().toUpperCase();
                logger_dev.info(msg);
                throw new SchedulerException(msg);
            }
        }

        scheduler.changePriority(jobId, priority);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#changePolicy(java.lang.Class)
     */
    public BooleanWrapper changePolicy(Class<? extends Policy> newPolicyFile) throws SchedulerException {
        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());

        if (!ui.isAdmin()) {
            String msg = "You do not have permission to change the policy of the scheduler !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }
        policyFullName = newPolicyFile.getClass().getName();
        return scheduler.changePolicy(newPolicyFile);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#linkResourceManager(java.lang.String)
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        UserIdentificationImpl ui = identifications.get(PAActiveObject.getContext().getCurrentRequest()
                .getSourceBodyID());

        if (!ui.isAdmin()) {
            String msg = "You do not have permission to reconnect a RM to the scheduler !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }
        return scheduler.linkResourceManager(rmURL);
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
            logger_dev.info("Dispatch event '" + methodName + "'");

            Method method = SchedulerEventListener.class.getMethod(methodName.toString(), types);
            Iterator<UniqueID> iter = schedulerListeners.keySet().iterator();

            while (iter.hasNext()) {
                UniqueID id = iter.next();
                UserIdentificationImpl userId = null;
                try {
                    userId = identifications.get(id);

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
                    logger.warn(userId.getUsername() + "@" + userId.getHostName() +
                        " has been disconnected from events listener!");
                }
            }
        } catch (SecurityException e) {
            logger_dev.error(e);
        } catch (NoSuchMethodException e) {
            logger_dev.error(e);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerFrozenEvent()
     */
    public void schedulerFrozenEvent() {
        dispatch(SchedulerEvent.FROZEN, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerPausedEvent()
     */
    public void schedulerPausedEvent() {
        dispatch(SchedulerEvent.PAUSED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerResumedEvent()
     */
    public void schedulerResumedEvent() {
        dispatch(SchedulerEvent.RESUMED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerShutDownEvent()
     */
    public void schedulerShutDownEvent() {
        dispatch(SchedulerEvent.SHUTDOWN, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerShuttingDownEvent()
     */
    public void schedulerShuttingDownEvent() {
        dispatch(SchedulerEvent.SHUTTING_DOWN, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStartedEvent()
     */
    public void schedulerStartedEvent() {
        dispatch(SchedulerEvent.STARTED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStoppedEvent()
     */
    public void schedulerStoppedEvent() {
        dispatch(SchedulerEvent.STOPPED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerKilledEvent()
     */
    public void schedulerKilledEvent() {
        dispatch(SchedulerEvent.KILLED, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobPausedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobPausedEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_PAUSED, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobResumedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobResumedEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_RESUMED, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.Job)
     * @param job The job that have just be submitted.
     */
    public void jobSubmittedEvent(InternalJob job) {
        dispatch(SchedulerEvent.JOB_SUBMITTED, new Class<?>[] { Job.class }, job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobPendingToRunningEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobPendingToRunningEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_PENDING_TO_RUNNING, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobRunningToFinishedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobRunningToFinishedEvent(JobEvent event) {
        jobs.get(event.getJobId()).setFinished(true);
        dispatch(SchedulerEvent.JOB_RUNNING_TO_FINISHED, new Class<?>[] { JobEvent.class }, event);
        //stats
        stats.increaseFinishedJobCount(event.getNumberOfFinishedTasks());
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobRemoveFinishedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobRemoveFinishedEvent(JobEvent event) {
        //removing jobs from the global list : this job is no more managed
        jobs.remove(event.getJobId());
        dispatch(SchedulerEvent.JOB_REMOVE_FINISHED, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskPendingToRunningEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskPendingToRunningEvent(TaskEvent event) {
        dispatch(SchedulerEvent.TASK_PENDING_TO_RUNNING, new Class<?>[] { TaskEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskRunningToFinishedEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskRunningToFinishedEvent(TaskEvent event) {
        dispatch(SchedulerEvent.TASK_RUNNING_TO_FINISHED, new Class<?>[] { TaskEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskWaitingForRestart(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskWaitingForRestart(TaskEvent event) {
        dispatch(SchedulerEvent.TASK_WAITING_FOR_RESTART, new Class<?>[] { TaskEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobChangePriorityEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobChangePriorityEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_CHANGE_PRIORITY, new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerRMDownEvent()
     */
    public void schedulerRMDownEvent() {
        dispatch(SchedulerEvent.RM_DOWN, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerRMUpEvent()
     */
    public void schedulerRMUpEvent() {
        dispatch(SchedulerEvent.RM_UP, null);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdate(org.ow2.proactive.scheduler.common.job.UserIdentification)
     */
    public void usersUpdate(UserIdentification userIdentification) {
        dispatch(SchedulerEvent.USERS_UPDATE, new Class<?>[] { UserIdentification.class }, userIdentification);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerPolicyChangedEvent(java.lang.String)
     */
    public void schedulerPolicyChangedEvent(String newPolicyName) {
        dispatch(SchedulerEvent.POLICY_CHANGED, new Class<?>[] { String.class }, newPolicyName);
    }

}
