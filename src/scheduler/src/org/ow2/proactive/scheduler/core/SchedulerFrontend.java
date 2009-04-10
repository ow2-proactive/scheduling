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

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.exception.MaxJobIdReachedException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.jmx.mbean.SchedulerWrapper;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
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
/**
 * @author cdelbe
 *
 */
/**
 * @author cdelbe
 *
 */
public class SchedulerFrontend implements InitActive, SchedulerStateUpdate, AdminSchedulerInterface {

    /** Scheduler logger */
    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.FRONTEND);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.FRONTEND);

    /** A repeated  warning message */
    private static final String ACCESS_DENIED = "Access denied !";
    private static final String SCHEDULER_BEAN_NAME = PASchedulerProperties.SCHEDULER_JMX_MBEAN_NAME
            .getValueAsString();

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
    private Map<UniqueID, SchedulerEventListener> schedulerListeners = new ConcurrentHashMap<UniqueID, SchedulerEventListener>();

    /** Scheduler's MBean */
    private SchedulerWrapper schedulerBean;

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
        //Register the scheduler MBean
        registerMBean();
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            body.setImmediateService("getTaskResult");
            body.setImmediateService("getJobResult");
            logger_dev.debug("Front-end immediate services : getTaskResult,getJobResult");

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
            PAActiveObject.registerByName(schedulerAuth, SchedulerConstants.SCHEDULER_DEFAULT_NAME);
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
        if (jobList != null) {
            logger_dev.info("job list : " + jobList.size());
            for (Entry<JobId, InternalJob> e : jobList.entrySet()) {
                UserIdentificationImpl uIdent = new UserIdentificationImpl(e.getValue().getOwner());
                IdentifiedJob ij = new IdentifiedJob(e.getKey(), uIdent);
                jobs.put(e.getKey(), ij);

                //if the job is finished set it
                switch (e.getValue().getStatus()) {
                    case CANCELED:
                    case FINISHED:
                    case FAILED:
                        ij.setFinished(true);
                        break;
                }
            }
        } else {
            logger_dev.info("job list empty");
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
        //send update user event only if the user is in the list of connected users.
        if (connectedUsers.getUsers().contains(ident)) {
            usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident));
        }
        logger.info("New job submitted '" + job.getId() + "' containing " + job.getTotalNumberOfTasks() +
            " tasks (owner is '" + job.getOwner() + "')");
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
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getJobResult(java.lang.String)
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
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getTaskResult(java.lang.String, java.lang.String)
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


    /* (non-Javadoc)
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#listenLog(org.ow2.proactive.scheduler.common.job.JobId, org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider)
     */
    public void listenLog(JobId jobId, AppenderProvider appenderProvider) throws SchedulerException {
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

        scheduler.listenLog(jobId, appenderProvider);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#getStatus()
     */
    public SchedulerStatus getStatus() {
        return schedulerBean.getSchedulerStatus_();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#addSchedulerEventListener(org.ow2.proactive.scheduler.common.SchedulerEventListener, boolean, org.ow2.proactive.scheduler.common.SchedulerEvent[])
     */
    public SchedulerState addSchedulerEventListener(SchedulerEventListener sel, boolean myEventsOnly,
            SchedulerEvent... events) throws SchedulerException {

        // check if listener is not null
        if (sel == null) {
            String msg = "Scheduler listener must not be null !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }
        // check if the listener is a reified remote object
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
        //set if the user wants to get its events only or every events
        uIdent.setMyEventsOnly(myEventsOnly);
        //put this new user in the list of connected user
        connectedUsers.addUser(uIdent);
        usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, uIdent));
        //add the listener to the list of listener for this user.
        schedulerListeners.put(id, sel);
        //get the scheduler State
        SchedulerStateImpl initState = (SchedulerStateImpl) (PAFuture.getFutureValue(scheduler
                .getSchedulerState()));
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
        return scheduler.start();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#stop()
     */
    public BooleanWrapper stop() throws SchedulerException {
        if (!ssprsc("You do not have permission to stop the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.stop();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#pause()
     */
    public BooleanWrapper pause() throws SchedulerException {
        if (!ssprsc("You do not have permission to pause the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.pause();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#freeze()
     */
    public BooleanWrapper freeze() throws SchedulerException {
        if (!ssprsc("You do not have permission to pause the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.freeze();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#resume()
     */
    public BooleanWrapper resume() throws SchedulerException {
        if (!ssprsc("You do not have permission to resume the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.resume();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#shutdown()
     */
    public BooleanWrapper shutdown() throws SchedulerException {
        if (!ssprsc("You do not have permission to shutdown the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.shutdown();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminSchedulerInterface#kill()
     */
    public BooleanWrapper kill() throws SchedulerException {
        if (!ssprsc("You do not have permission to kill the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.kill();
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
        usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident));
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

        IdentifiedJob ij = jobs.get(jobId);
        if (ij == null) {
            String msg = "Job '" + jobId + "' is unknown";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }
        if (ij.isFinished()) {
            String msg = "Job '" + jobId + "' is already finished";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        scheduler.changePriority(jobId, priority);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#kill(java.lang.String)
     */
    public BooleanWrapper kill(String jobId) throws SchedulerException {
        return this.kill(JobIdImpl.makeJobId(jobId));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#pause(java.lang.String)
     */
    public BooleanWrapper pause(String jobId) throws SchedulerException {
        return this.pause(JobIdImpl.makeJobId(jobId));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#remove(java.lang.String)
     */
    public void remove(String jobId) throws SchedulerException {
        this.remove(JobIdImpl.makeJobId(jobId));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#resume(java.lang.String)
     */
    public BooleanWrapper resume(String jobId) throws SchedulerException {
        return this.resume(JobIdImpl.makeJobId(jobId));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface#changePriority(java.lang.String, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(String jobId, JobPriority priority) throws SchedulerException {
        this.changePriority(JobIdImpl.makeJobId(jobId), priority);
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

    /**
     * Register the Scheduler MBean
     */
    private void registerMBean() {
        //Get the platform MBeanServer
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        // Unique identification of Scheduler MBean
        schedulerBean = new SchedulerWrapper();
        ObjectName schedulerName = null;
        try {
            // Uniquely identify the MBeans and register them with the platform MBeanServer 
            schedulerName = new ObjectName(SCHEDULER_BEAN_NAME);
            mbs.registerMBean(schedulerBean, schedulerName);
            //            ServerConnector connector = new ServerConnector("ServerFrontend");
            //            connector.start();
        } catch (Exception e) {
            logger_dev.debug("", e);
        }
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ################################## LISTENER DISPATCHER #################################### */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * Clear a listener that is no more responding
     * 
     * @param iter the iterator corresponding to the listener
     * @param id the identification of the user that is no more available
     * @param userId the userIdentification corresponding to the id.
     */
    private void clearListener(Iterator<UniqueID> iter, UniqueID id, UserIdentification userId) {
        iter.remove();
        UserIdentificationImpl ident = identifications.remove(id);
        //remove this user to the list of connected user
        ident.setToRemove();
        connectedUsers.update(ident);
        usersUpdated(new NotificationData<UserIdentification>(SchedulerEvent.USERS_UPDATE, ident));
        logger.warn(userId.getUsername() + "@" + userId.getHostName() +
            " has been disconnected from events listener!");
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
            Iterator<UniqueID> iter = schedulerListeners.keySet().iterator();
            while (iter.hasNext()) {
                UniqueID id = iter.next();
                UserIdentificationImpl userId = null;
                try {
                    userId = identifications.get(id);
                    //if their is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) || userId.getUserEvents().contains(eventType)) {
                        schedulerListeners.get(id).schedulerStateUpdatedEvent(eventType);
                    }
                } catch (Exception e) {
                    clearListener(iter, id, userId);
                }
            }
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
            Iterator<UniqueID> iter = schedulerListeners.keySet().iterator();
            while (iter.hasNext()) {
                UniqueID id = iter.next();
                UserIdentificationImpl userId = null;
                try {
                    userId = identifications.get(id);
                    //if their is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(SchedulerEvent.JOB_SUBMITTED)) {
                        //if this userId have the myEventOnly=false or (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(job.getOwner()))) {
                            schedulerListeners.get(id).jobSubmittedEvent(job);
                        }
                    }
                } catch (NullPointerException e) {
                    //can't do anything
                    logger_dev.debug("", e);
                } catch (Exception e) {
                    clearListener(iter, id, userId);
                }
            }
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
            Iterator<UniqueID> iter = schedulerListeners.keySet().iterator();
            while (iter.hasNext()) {
                UniqueID id = iter.next();
                UserIdentificationImpl userId = null;
                try {
                    userId = identifications.get(id);
                    //if their is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        //if this userId have the myEventOnly=false or (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(owner))) {
                            schedulerListeners.get(id).jobStateUpdatedEvent(notification);
                        }
                    }
                } catch (Exception e) {
                    clearListener(iter, id, userId);
                }
            }
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
            Iterator<UniqueID> iter = schedulerListeners.keySet().iterator();
            while (iter.hasNext()) {
                UniqueID id = iter.next();
                UserIdentificationImpl userId = null;
                try {
                    userId = identifications.get(id);
                    //if their is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        //if this userId have the myEventOnly=false or (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(owner))) {
                            schedulerListeners.get(id).taskStateUpdatedEvent(notification);
                        }
                    }
                } catch (Exception e) {
                    clearListener(iter, id, userId);
                }
            }
        } catch (SecurityException e) {
            logger_dev.error(e);
        }
    }

    /**
     * Dispatch the users updated event
     * 
     * @param notification the data to send to every client
     */
    private void dispatchUsersUpdated(NotificationData<UserIdentification> notification) {
        try {
            if (logger_dev.isDebugEnabled()) {
                logger_dev.debug("Dispatch event '" + notification.getEventType() + "'");
            }
            Iterator<UniqueID> iter = schedulerListeners.keySet().iterator();
            while (iter.hasNext()) {
                UniqueID id = iter.next();
                UserIdentificationImpl userId = null;
                try {
                    userId = identifications.get(id);
                    //if their is no specified event OR if the specified event is allowed
                    if ((userId.getUserEvents() == null) ||
                        userId.getUserEvents().contains(notification.getEventType())) {
                        //if this userId have the myEventOnly=false or (myEventOnly=true and it is its event)
                        if (!userId.isMyEventsOnly() ||
                            (userId.isMyEventsOnly() && userId.getUsername().equals(
                                    notification.getData().getUsername()))) {
                            schedulerListeners.get(id).usersUpdatedEvent(notification);
                        }
                    }
                } catch (Exception e) {
                    clearListener(iter, id, userId);
                }
            }
        } catch (SecurityException e) {
            logger_dev.error(e);
        }
    }

    //--------------------------------------------------------------------------------------------

    /**
     * @see org.ow2.proactive.scheduler.core.SchedulerStateUpdate#schedulerStateUpdated(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdated(SchedulerEvent eventType) {
        switch (eventType) {
            case STARTED:
            case STOPPED:
            case PAUSED:
            case FROZEN:
            case RESUMED:
            case SHUTTING_DOWN:
            case SHUTDOWN:
            case KILLED:
            case RM_DOWN:
            case RM_UP:
            case POLICY_CHANGED:
                dispatchSchedulerStateUpdated(eventType);
                schedulerBean.schedulerStateUpdated(eventType);
                break;
            default:
                logger_dev.info("Unconsistent update type received from Scheduler Core : " + eventType);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.core.SchedulerStateUpdate#jobSubmitted(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmitted(JobState job) {
        dispatchJobSubmitted(job);
        schedulerBean.jobSubmittedEvent(job);
    }

    /**
     * @see org.ow2.proactive.scheduler.core.SchedulerStateUpdate#jobStateUpdated(java.lang.String, org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdated(String owner, NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_PAUSED:
            case JOB_RESUMED:
            case JOB_PENDING_TO_RUNNING:
            case JOB_CHANGE_PRIORITY:
                dispatchJobStateUpdated(owner, notification);
                schedulerBean.jobStateUpdated(notification);
                break;
            case JOB_RUNNING_TO_FINISHED:
                //set this job finished, user can get its result
                jobs.get(notification.getData().getJobId()).setFinished(true);
                dispatchJobStateUpdated(owner, notification);
                schedulerBean.jobStateUpdated(notification);
                break;
            case JOB_REMOVE_FINISHED:
                //removing jobs from the global list : this job is no more managed
                jobs.remove(notification.getData().getJobId());
                dispatchJobStateUpdated(owner, notification);
                schedulerBean.jobStateUpdated(notification);
                break;
            default:
                logger_dev.info("Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.core.SchedulerStateUpdate#taskStateUpdated(java.lang.String, org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void taskStateUpdated(String owner, NotificationData<TaskInfo> notification) {
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
            case TASK_RUNNING_TO_FINISHED:
            case TASK_WAITING_FOR_RESTART:
                dispatchTaskStateUpdated(owner, notification);
                schedulerBean.taskStateUpdated(notification);
                break;
            default:
                logger_dev.info("Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.core.SchedulerStateUpdate#usersUpdated(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdated(NotificationData<UserIdentification> notification) {
        switch (notification.getEventType()) {
            case USERS_UPDATE:
                dispatchUsersUpdated(notification);
                schedulerBean.usersUpdate(notification.getData());
                break;
            default:
                logger_dev.info("Unconsistent update type received from Scheduler Core : " +
                    notification.getEventType());
        }
    }

}
