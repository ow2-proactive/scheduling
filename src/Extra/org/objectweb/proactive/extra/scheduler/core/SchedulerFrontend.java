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
package org.objectweb.proactive.extra.scheduler.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEvent;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerInitialState;
import org.objectweb.proactive.extra.scheduler.common.scheduler.Stats;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.job.IdentifyJob;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;
import org.objectweb.proactive.extra.scheduler.job.InternalJobFactory;
import org.objectweb.proactive.extra.scheduler.job.JobDescriptor;
import org.objectweb.proactive.extra.scheduler.job.UserIdentification;
import org.objectweb.proactive.extra.scheduler.resourcemanager.InfrastructureManagerProxy;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


/**
 * Scheduler Front-end. This is the API to talk to when you want to managed a scheduler core.
 * Creating this class can only be done by using <code>AdminScheduler</code>.
 * You can join this front-end by using the <code>join()</code> method
 * in {@link SchedulerConnection} .
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jun 28, 2007
 * @since ProActive 3.2
 */
public class SchedulerFrontend implements InitActive,
    SchedulerEventListener<InternalJob>, UserSchedulerInterface,
    SchedulerCoreInterface {

    /** Serial Version UID */
    private static final long serialVersionUID = -7843011649407086298L;

    /** Scheduler logger */
    public static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    /** A repeated  warning message */
    private static final String ACCESS_DENIED = "Access denied !";

    /** Mapping on the UniqueId of the sender and the user/admin identifications */
    private HashMap<UniqueID, UserIdentification> identifications = new HashMap<UniqueID, UserIdentification>();

    /** Implementation of Resource Manager */
    private InfrastructureManagerProxy resourceManager;

    /** Authentication Interface */
    private SchedulerAuthentication authenticationInterface;

    /** Full name of the policy class */
    private String policyFullName;

    /** Implementation of scheduler main structure */
    private SchedulerCore scheduler;

    /** Job identification management */
    private HashMap<JobId, IdentifyJob> jobs;

    /** scheduler listeners */
    private HashMap<UniqueID, SchedulerEventListener<?extends Job>> schedulerListeners =
        new HashMap<UniqueID, SchedulerEventListener<?extends Job>>();

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
     * @param imp a resource manager which
     *                                 be able to managed the resource used by scheduler.
     * @throws NodeException
     * @throws ActiveObjectCreationException
     */
    public SchedulerFrontend(InfrastructureManagerProxy imp,
        String policyFullClassName)
        throws ActiveObjectCreationException, NodeException {
        logger.info("Creating scheduler core...");
        resourceManager = imp;
        policyFullName = policyFullClassName;
        jobs = new HashMap<JobId, IdentifyJob>();
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            scheduler = (SchedulerCore) ProActiveObject.newActive(SchedulerCore.class.getName(),
                    new Object[] {
                        resourceManager, ProActiveObject.getStubOnThis(),
                        policyFullName
                    });
            //ProActive.addNFEListenerOnAO(scheduler,
            //    new NFEHandler("Scheduler Core"));
            logger.info("Scheduler successfully created on " +
                ProActiveObject.getNode().getNodeInformation().getVMInformation()
                               .getHostName());
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
        authenticationInterface = (SchedulerAuthentication) ProActiveObject.getContext()
                                                                           .getStubOnCaller();
        //authenticationInterface.activate();
    }

    /**
     * Connect a new user on the scheduler.
     * This user can interact with the scheduler according to his right.
     *
     * @param sourceBodyID the source ID of the connected object representing a user
     * @param identification the identification of the connected user
     */
    public void connect(UniqueID sourceBodyID, UserIdentification identification)
        throws SchedulerException {
        if (identifications.containsKey(sourceBodyID)) {
            logger.warn("Active object already connected !");
            throw new SchedulerException(
                "This active object is already connected to the scheduler !");
        }
        logger.info(identification.getUsername() + " successfully connected !");
        identifications.put(sourceBodyID, identification);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#getJobResult(org.objectweb.proactive.extra.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException {
        //checking permissions
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }
        IdentifyJob ij = jobs.get(jobId);
        if (ij == null) {
            throw new SchedulerException(
                "The job represented by this ID is unknow !");
        }
        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException(
                "You do not have permission to access this job !");
        }
        if (!ij.isFinished()) {
            return null;
        }

        //asking the scheduler for the result
        JobResult result = scheduler.getJobResult(jobId);
        if (result == null) {
            throw new SchedulerException(
                "The result of this job is no longer available !");
        }
        //removing jobs from the global list : this job is no more managed
        jobs.remove(jobId);
        return result;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#getTaskResult(org.objectweb.proactive.extra.scheduler.common.job.JobId, java.lang.String)
     */
    public TaskResult getTaskResult(JobId jobId, String taskName)
        throws SchedulerException {
        //checking permissions
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }
        IdentifyJob ij = jobs.get(jobId);
        if (ij == null) {
            throw new SchedulerException(
                "The job represented by this ID is unknow !");
        }
        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException(
                "You do not have permission to access this job !");
        }

        //asking the scheduler for the result
        TaskResult result = scheduler.getTaskResult(jobId, taskName);
        if (result == null) {
            throw new SchedulerException(
                "Error while getting the result of this task !");
        }
        return result;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#submit(org.objectweb.proactive.extra.scheduler.common.job.Job)
     */
    public JobId submit(Job userJob) throws SchedulerException {
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
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
            if ((job.getPriority().getPriority() > 3) ||
                    (job.getPriority() == JobPriority.IDLE)) {
                throw new SchedulerException(
                    "Only the administrator can submit a job with such priority : " +
                    job.getPriority());
            }
        }
        //setting the job properties
        job.setId(JobId.nextId());
        job.setOwner(identifications.get(id).getUsername());
        TaskId.initialize();
        for (InternalTask td : job.getTasks()) {
            job.setTaskId(td, TaskId.nextId(job.getId()));
            td.setJobInfo(job.getJobInfo());
        }
        jobs.put(job.getId(),
            new IdentifyJob(job.getId(), identifications.get(id)));
        //make the light job
        job.setJobDescriptor(new JobDescriptor(job));
        scheduler.submit(job);
        //stats
        stats.increaseSubmittedJobCount(job.getType());
        return job.getId();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#listenLog(org.objectweb.proactive.extra.scheduler.common.job.JobId, java.lang.String, int)
     */
    public void listenLog(JobId jobId, String hostname, int port)
        throws SchedulerException {
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }
        IdentifyJob ij = jobs.get(jobId);
        if (ij == null) {
            throw new SchedulerException(
                "The job represented by this ID is unknow !");
        }
        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException(
                "You do not have permission to listen the log of this job !");
        }
        scheduler.listenLog(jobId, hostname, port);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#addSchedulerEventListener(org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener)
     */
    public SchedulerInitialState<?extends Job> addSchedulerEventListener(
        SchedulerEventListener<?extends Job> sel, SchedulerEvent... events)
        throws SchedulerException {
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }
        if (events.length > 0) {
            identifications.get(id).setUserEvents(events);
        }
        schedulerListeners.put(id, sel);
        return scheduler.getSchedulerInitialState();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#getStats()
     */
    public Stats getStats() throws SchedulerException {
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
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
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
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
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#start()
     */
    public BooleanWrapper coreStart() {
        if (!ssprsc("You do not have permission to start the scheduler !")) {
            return new BooleanWrapper(false);
        }
        //stats
        stats.startTime();
        return scheduler.coreStart();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#stop()
     */
    public BooleanWrapper coreStop() {
        if (!ssprsc("You do not have permission to stop the scheduler !")) {
            return new BooleanWrapper(false);
        }
        //stats
        stats.stopTime();
        return scheduler.coreStop();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#pause()
     */
    public BooleanWrapper corePause() {
        if (!ssprsc("You do not have permission to pause the scheduler !")) {
            return new BooleanWrapper(false);
        }
        //stats
        stats.pauseTime();
        return scheduler.corePause();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#corePauseImmediate()
     */
    public BooleanWrapper coreImmediatePause() {
        if (!ssprsc("You do not have permission to pause the scheduler !")) {
            return new BooleanWrapper(false);
        }
        //stats
        stats.pauseTime();
        return scheduler.coreImmediatePause();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface#resume()
     */
    public BooleanWrapper coreResume() {
        if (!ssprsc("You do not have permission to resume the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.coreResume();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#shutdown()
     */
    public BooleanWrapper coreShutdown() {
        if (!ssprsc("You do not have permission to shutdown the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.coreShutdown();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#coreKill()
     */
    public BooleanWrapper coreKill() {
        if (!ssprsc("You do not have permission to kill the scheduler !")) {
            return new BooleanWrapper(false);
        }
        return scheduler.coreKill();
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#disconnect()
     */
    public void disconnect() throws SchedulerException {
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }
        String user = identifications.get(id).getUsername();
        schedulerListeners.remove(id);
        identifications.remove(id);
        logger.info("User " + user + " has left the scheduler !");
    }

    /**
     * Factoring of exception management for the 4 next jobs order.
     *
     * @param jobId the jobId concerned by the order.
     * @param permissionMsg the message to send the user if he has no right.
     * @throws SchedulerException the exception send if there is a probleme of authentication.
     */
    private void prkcp(JobId jobId, String permissionMsg)
        throws SchedulerException {
        UniqueID id = ProActiveObject.getContext().getCurrentRequest()
                                     .getSourceBodyID();
        if (!identifications.containsKey(id)) {
            throw new SchedulerException(ACCESS_DENIED);
        }
        IdentifyJob ij = jobs.get(jobId);
        if (ij == null) {
            throw new SchedulerException(
                "The job represented by this ID is unknow !");
        }
        if (!ij.hasRight(identifications.get(id))) {
            throw new SchedulerException(permissionMsg);
        }
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#pause(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public BooleanWrapper pause(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to pause this job !");
        return scheduler.pause(jobId);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#resume(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public BooleanWrapper resume(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to resume this job !");
        return scheduler.resume(jobId);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#kill(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public BooleanWrapper kill(JobId jobId) throws SchedulerException {
        prkcp(jobId, "You do not have permission to kill this job !");
        return scheduler.kill(jobId);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface#changePriority(org.objectweb.proactive.extra.scheduler.job.JobId, javax.print.attribute.standard.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority)
        throws SchedulerException {
        prkcp(jobId,
            "You do not have permission to change the priority of this job !");
        UserIdentification ui = identifications.get(ProActiveObject.getContext()
                                                                   .getCurrentRequest()
                                                                   .getSourceBodyID());
        if (!ui.isAdmin()) {
            if (priority == JobPriority.HIGHEST) {
                throw new SchedulerException(
                    "Only an administrator can change the priority to HIGHEST");
            } else if (priority == JobPriority.HIGH) {
                throw new SchedulerException(
                    "Only an administrator can change the priority to HIGH");
            } else if (priority == JobPriority.IDLE) {
                throw new SchedulerException(
                    "Only an administrator can change the priority to IDLE");
            }
        }
        scheduler.changePriority(jobId, priority);
    }

    /**
     * Terminate the schedulerConnexion active object and then this object.
     */
    public boolean terminate() {
        if (authenticationInterface != null) {
            authenticationInterface.terminate();
        }
        ProActiveObject.terminateActiveObject(false);
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
    private void dispatch(SchedulerEvent methodName, Class<?>[] types,
        Object... params) {
        try {
            Method method = SchedulerEventListener.class.getMethod(methodName.toString(),
                    types);
            Iterator<UniqueID> iter = schedulerListeners.keySet().iterator();
            while (iter.hasNext()) {
                UniqueID id = iter.next();
                try {
                    UserIdentification userId = identifications.get(id);

                    if ((userId.getUserEvents() == null) ||
                            userId.getUserEvents().contains(methodName)) {
                        method.invoke(schedulerListeners.get(id), params);
                    }
                } catch (Exception e) {
                    iter.remove();
                    identifications.remove(id);
                    logger.error(
                        "!!!!!!!!!!!!!! Scheduler has detected that a listener is not connected anymore !");
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#SchedulerImmediatePausedEvent()
     */
    public void schedulerImmediatePausedEvent() {
        dispatch(SchedulerEvent.IMMEDIATE_PAUSED, null);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#SchedulerPausedEvent()
     */
    public void schedulerPausedEvent() {
        dispatch(SchedulerEvent.PAUSED, null);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#SchedulerResumedEvent()
     */
    public void schedulerResumedEvent() {
        dispatch(SchedulerEvent.RESUMED, null);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#SchedulerShutDownEvent()
     */
    public void schedulerShutDownEvent() {
        dispatch(SchedulerEvent.SHUTDOWN, null);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#SchedulerShuttingDownEvent()
     */
    public void schedulerShuttingDownEvent() {
        dispatch(SchedulerEvent.SHUTTING_DOWN, null);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#SchedulerStartedEvent()
     */
    public void schedulerStartedEvent() {
        dispatch(SchedulerEvent.STARTED, null);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#SchedulerStoppedEvent()
     */
    public void schedulerStoppedEvent() {
        dispatch(SchedulerEvent.STOPPED, null);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#SchedulerkilledEvent()
     */
    public void schedulerKilledEvent() {
        dispatch(SchedulerEvent.KILLED, null);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#jobKilledEvent(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void jobKilledEvent(JobId jobId) {
        dispatch(SchedulerEvent.JOB_KILLED, new Class<?>[] { JobId.class },
            jobId);
        jobs.remove(jobId);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#jobPausedEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    public void jobPausedEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_PAUSED, new Class<?>[] { JobEvent.class },
            event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#jobResumedEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    public void jobResumedEvent(JobEvent event) {
        dispatch(SchedulerEvent.JOB_RESUMED, new Class<?>[] { JobEvent.class },
            event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#newPendingJobEvent(org.objectweb.proactive.extra.scheduler.job.JobU)
     */
    public void newPendingJobEvent(InternalJob job) {
        dispatch(SchedulerEvent.NEW_PENDING_JOB, new Class<?>[] { Job.class },
            job);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#pendingToRunningJobEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    public void pendingToRunningJobEvent(JobEvent event) {
        dispatch(SchedulerEvent.PENDING_TO_RUNNING_JOB,
            new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#runningToFinishedJobEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    public void runningToFinishedJobEvent(JobEvent event) {
        dispatch(SchedulerEvent.RUNNING_TO_FINISHED_JOB,
            new Class<?>[] { JobEvent.class }, event);
        jobs.get(event.getJobId()).setFinished(true);
        //stats
        stats.increaseFinishedJobCount(event.getNumberOfFinishedTasks());
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#removeFinishedJobEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    public void removeFinishedJobEvent(JobEvent event) {
        dispatch(SchedulerEvent.REMOVE_FINISHED_JOB,
            new Class<?>[] { JobEvent.class }, event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#pendingToRunningTaskEvent(org.objectweb.proactive.extra.scheduler.common.task.TaskEvent)
     */
    public void pendingToRunningTaskEvent(TaskEvent event) {
        dispatch(SchedulerEvent.PENDING_TO_RUNNING_TASK,
            new Class<?>[] { TaskEvent.class }, event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#runningToFinishedTaskEvent(org.objectweb.proactive.extra.scheduler.common.task.TaskEvent)
     */
    public void runningToFinishedTaskEvent(TaskEvent event) {
        dispatch(SchedulerEvent.RUNNING_TO_FINISHED_TASK,
            new Class<?>[] { TaskEvent.class }, event);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener#changeJobPriorityEvent(org.objectweb.proactive.extra.scheduler.common.job.JobEvent)
     */
    public void changeJobPriorityEvent(JobEvent event) {
        dispatch(SchedulerEvent.CHANGE_JOB_PRIORITY,
            new Class<?>[] { JobEvent.class }, event);
    }
}
