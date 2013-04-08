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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.policy.ClientsPolicy;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.JobSortParameter;
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
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.TaskCouldNotRestartException;
import org.ow2.proactive.scheduler.common.exception.TaskCouldNotStartException;
import org.ow2.proactive.scheduler.common.exception.TaskSkippedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.core.account.SchedulerAccountsManager;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.utils.Tools;


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
public class SchedulerFrontend implements InitActive, Scheduler, RunActive {

    /** Delay to wait for between getting a job result and removing the job concerned */
    private static final long SCHEDULER_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_REMOVED_JOB_DELAY
            .getValueAsInt() * 1000;

    /** Scheduler logger */
    public static final Logger logger = Logger.getLogger(SchedulingService.class);
    public static final TaskLogger tlogger = TaskLogger.getInstance();
    public static final JobLogger jlogger = JobLogger.getInstance();

    /** Temporary rmURL at starting process */
    private URI rmURL;

    /** Authentication Interface */
    private SchedulerAuthentication authentication;

    /** Full name of the policy class */
    private String policyFullName;

    /** Users Statistics Manager */
    private SchedulerAccountsManager accountsManager;

    /** JMX Helper reference */
    private SchedulerJMXHelper jmxHelper;

    private SchedulingService schedulingService;

    private SchedulerDBManager dbManager;

    private SchedulerFrontendState frontendState;

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
        this.dbManager = SchedulerDBManager.createUsingProperties();
        this.accountsManager = new SchedulerAccountsManager(dbManager);
        this.jmxHelper = new SchedulerJMXHelper(accountsManager, dbManager);

        logger.info("Creating scheduler Front-end...");
        this.rmURL = rmURL;
        this.policyFullName = policyFullClassName;
        logger.debug("Policy used is " + policyFullClassName);
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
            logger.info("Creating scheduler authentication interface...");
            authentication = PAActiveObject.newActive(SchedulerAuthentication.class,
                    new Object[] { PAActiveObject.getStubOnThis() });
            //creating scheduler core

            RMProxiesManager rmProxiesManager = RMProxiesManager.createRMProxiesManager(rmURL);
            rmProxiesManager.getSchedulerRMProxy();

            DataSpaceServiceStarter dsServiceStarter = new DataSpaceServiceStarter();
            dsServiceStarter.startNamingService();

            ExecutorService clientThreadPool = Executors.newFixedThreadPool(
                    PASchedulerProperties.SCHEDULER_CLIENT_POOL_NBTHREAD.getValueAsInt(),
                    new NamedThreadFactory("ClientRequestsThreadPool"));

            ExecutorService internalThreadPool = Executors.newFixedThreadPool(
                    PASchedulerProperties.SCHEDULER_INTERNAL_POOL_NBTHREAD.getValueAsInt(),
                    new NamedThreadFactory("InternalOperationsThreadPool"));

            SchedulingInfrastructure infrastructure = new SchedulingInfrastructureImpl(dbManager,
                rmProxiesManager, dsServiceStarter, clientThreadPool, internalThreadPool,
                new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("SchedulingServiceTimerThread")));

            long loadJobPeriod = -1;
            if (PASchedulerProperties.SCHEDULER_DB_LOAD_JOB_PERIOD.isSet()) {
                String periodStr = PASchedulerProperties.SCHEDULER_DB_LOAD_JOB_PERIOD.getValueAsString();
                if (periodStr != null && !periodStr.isEmpty()) {
                    try {
                        loadJobPeriod = Tools.parsePeriod(periodStr);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid load job period string: " + periodStr +
                            ", this setting is ignored", e);
                    }
                }
            }

            // Boot the JMX helper
            logger.info("Booting jmx...");
            this.jmxHelper.boot(authentication);

            SchedulerStateRecoverHelper.RecoveredSchedulerState recoveredState = new SchedulerStateRecoverHelper(
                infrastructure.getDBManager()).recover(loadJobPeriod);

            this.frontendState = new SchedulerFrontendState(recoveredState.getSchedulerState(), jmxHelper);

            this.schedulingService = new SchedulingService(infrastructure, frontendState, recoveredState,
                policyFullName, null);

            logger.info("Registering scheduler...");
            PAActiveObject.registerByName(authentication, SchedulerConstants.SCHEDULER_DEFAULT_NAME);
            authentication.setActivated(true);
            // run !!
        } catch (Exception e) {
            logger.error("", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ################################### SCHEDULING MANAGEMENT ################################# */
    /*                                                                                             */
    /* ########################################################################################### */

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
        frontendState.connect(sourceBodyID, identification, cred);
    }

    /**
     * {@inheritDoc}
     */
    public JobId submit(Job userJob) throws NotConnectedException, PermissionException,
            SubmissionClosedException, JobCreationException {
        logger.info("New job submission requested : " + userJob.getName());

        //check if the scheduler is stopped
        if (!schedulingService.isSubmitPossible()) {
            String msg = "Scheduler is stopped, cannot submit job";
            logger.info(msg);
            throw new SubmissionClosedException(msg);
        }

        UserIdentificationImpl ident = frontendState.checkPermission("submit",
                "You do not have permission to submit a job !");

        InternalJob job = frontendState.createJob(userJob, ident);

        schedulingService.submitJob(job);

        frontendState.jobSubmitted(job, ident);

        return job.getId();
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public JobResult getJobResult(final JobId jobId) throws NotConnectedException, PermissionException,
            UnknownJobException {

        //checking permissions
        IdentifiedJob ij = frontendState.checkJobOwner("getJobResult", jobId,
                "You do not have permission to get the result of this job !");

        if (!ij.isFinished()) {
            jlogger.info(jobId, "is not finished");
            return null;
        }

        jlogger.info(jobId, "trying to get the job result");

        JobResult result = dbManager.loadJobResult(jobId);
        if (result == null) {
            throw new UnknownJobException(jobId);
        }

        if (!result.getJobInfo().isToBeRemoved() && SCHEDULER_REMOVED_JOB_DELAY > 0) {
            //remember that this job is to be removed
            dbManager.jobSetToBeRemoved(jobId);
            schedulingService.scheduleJobRemove(jobId, SCHEDULER_REMOVED_JOB_DELAY);
            jlogger.info(jobId, "will be removed in " + (SCHEDULER_REMOVED_JOB_DELAY / 1000) + "sec");
        }

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
        return this.getTaskResultFromIncarnation(jobId, taskName, 0);
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
    public TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResultFromIncarnation(JobIdImpl.makeJobId(jobId), taskName, inc);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {

        //checking permissions
        frontendState.checkJobOwner("getTaskResultFromIncarnation", jobId,
                "You do not have permission to get the task result of this job !");

        if (inc < 0) {
            throw new IllegalArgumentException("Incarnation must be 0 or greater.");
        }

        jlogger.info(jobId, "trying to get the task result, incarnation " + inc);

        if (inc < 0) {
            throw new IllegalArgumentException("Incarnation must be 0 or greater.");
        }

        try {
            TaskResult result = dbManager.loadTaskResult(jobId, taskName, inc);
            // handling special statuses
            TaskState ts = frontendState.getTaskState(jobId, taskName);
            switch (ts.getStatus()) {
                case NOT_STARTED:
                    if (result == null) {
                        return new TaskResultImpl(frontendState.getTaskId(jobId, taskName),
                            new TaskCouldNotStartException(), new SimpleTaskLogs("",
                                "The task could not start due to dependency failure"), 0);
                    } else {
                        Throwable newException = new TaskCouldNotStartException(
                            "The task could not start due to dependency failure", result.getException());
                        ((TaskResultImpl) result).setException(newException);
                    }
                    break;
                case NOT_RESTARTED:
                    if (result == null) {
                        return new TaskResultImpl(
                            frontendState.getTaskId(jobId, taskName),
                            new TaskCouldNotRestartException(),
                            new SimpleTaskLogs("",
                                "The task could not be restarted after an error during the previous execution"),
                            0);
                    } else {
                        Throwable newException = new TaskCouldNotRestartException(
                            "The task could not be restarted after an error during the previous execution",
                            result.getException());
                        ((TaskResultImpl) result).setException(newException);
                    }
                    break;
                case SKIPPED:
                    // result should always be null
                    return new TaskResultImpl(frontendState.getTaskId(jobId, taskName),
                        new TaskSkippedException(), new SimpleTaskLogs("",
                            "The task was skipped in the workflow"), 0);
            }
            if (result == null) {
                //otherwise the task is not finished
                jlogger.info(jobId, taskName + " is not finished");
                return null;
            } else {
                return result;
            }
        } catch (DatabaseManagerException e) {
            throw new UnknownTaskException("Unknown task " + taskName + ", job: " + jobId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean killTask(JobId jobId, String taskName) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        //checking permissions
        frontendState.checkJobOwner("killTask", jobId, "You do not have permission to kill this task !");
        return schedulingService.killTask(jobId, taskName);
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
        frontendState
                .checkJobOwner("restartTask", jobId, "You do not have permission to restart this task !");
        return schedulingService.restartTask(jobId, taskName, restartDelay);
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
        frontendState
                .checkJobOwner("preemptTask", jobId, "You do not have permission to preempt this task !");
        return schedulingService.preemptTask(jobId, taskName, restartDelay);
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
        frontendState.checkJobOwner("removeJob", jobId, "You do not have permission to remove this job !");

        //asking the scheduler for the result
        return schedulingService.removeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws NotConnectedException,
            UnknownJobException, PermissionException {

        if (!schedulingService.getListenJobLogsSupport().isEnabled()) {
            throw new PermissionException("Listening to job logs is disabled by administrator");
        }

        //checking permissions
        frontendState.checkJobOwner("listenJobLogs", jobId,
                "You do not have permission to listen the log of this job !");

        schedulingService.listenJobLogs(jobId, appenderProvider);
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
        return frontendState.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return getState(false);
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        return frontendState.getState(myJobsOnly);
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
        return frontendState.addEventListener(sel, myEventsOnly, getCurrentState, events);
    }

    /**
     * {@inheritDoc}
     */
    public void removeEventListener() throws NotConnectedException, PermissionException {
        frontendState.removeEventListener();
    }

    /* ########################################################################################### */
    /*                                                                                             */
    /* ##################################### SCHEDULER ORDERS #################################### */
    /*                                                                                             */
    /* ########################################################################################### */

    /**
     * {@inheritDoc}
     */
    public boolean start() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("start", "You do not have permission to start the scheduler !");
        return schedulingService.start();
    }

    /**
     * {@inheritDoc}
     */
    public boolean stop() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("stop", "You do not have permission to stop the scheduler !");
        return schedulingService.stop();
    }

    /**
     * {@inheritDoc}
     */
    public boolean pause() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("pause", "You do not have permission to pause the scheduler !");
        return schedulingService.pause();
    }

    /**
     * {@inheritDoc}
     */
    public boolean freeze() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("freeze", "You do not have permission to freeze the scheduler !");
        return schedulingService.freeze();
    }

    /**
     * {@inheritDoc}
     */
    public boolean resume() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("resume", "You do not have permission to resume the scheduler !");
        return schedulingService.resume();
    }

    /**
     * {@inheritDoc}
     */
    public boolean shutdown() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("shutdown", "You do not have permission to shutdown the scheduler !");
        return schedulingService.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    public boolean kill() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("kill", "You do not have permission to kill the scheduler !");
        return schedulingService.kill();
    }

    /**
     * {@inheritDoc}
     */
    public void disconnect() throws NotConnectedException, PermissionException {
        frontendState.disconnect();
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean isConnected() {
        return frontendState.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public void renewSession() throws NotConnectedException {
        frontendState.renewSession();
    }

    /**
     * {@inheritDoc}
     */
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        frontendState.checkJobOwner("pauseJob", jobId, "You do not have permission to pause this job !");
        return schedulingService.pauseJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        frontendState.checkJobOwner("resumeJob", jobId, "You do not have permission to resume this job !");
        return schedulingService.resumeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        frontendState.checkJobOwner("killJob", jobId, "You do not have permission to kill this job !");
        return schedulingService.killJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    public void changeJobPriority(JobId jobId, JobPriority priority) throws NotConnectedException,
            UnknownJobException, PermissionException, JobAlreadyFinishedException {
        frontendState.checkChangeJobPriority(jobId, priority);

        schedulingService.changeJobPriority(jobId, priority);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException,
            PermissionException {
        return frontendState.getJobState(jobId);
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
        frontendState.checkChangePolicy();
        policyFullName = newPolicyClassname;
        return schedulingService.changePolicy(newPolicyClassname);
    }

    /**
     * {@inheritDoc}
     */
    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        frontendState.checkLinkResourceManager();
        return schedulingService.linkResourceManager(rmURL);
    }

    /**
     * {@inheritDoc}
     */
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("reloadPolicyConfiguration",
                "You do not have permission to reload policy configuration !");
        return schedulingService.reloadPolicyConfiguration();
    }

    /**
     * Terminate the schedulerConnexion active object and then this object.
     * 
     * @return always true;
     */
    public boolean terminate() {
        logger.debug("Closing Scheduler database");
        dbManager.close();

        if (authentication != null) {
            authentication.terminate();
        }

        ClientRequestHandler.terminate();

        PAActiveObject.terminateActiveObject(false);
        logger.info("Scheduler frontend is now shutdown !");

        return true;
    }

    /**
     * Method controls the execution of every request.
     * Tries to keep this active object alive in case of any exception.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            Request request = service.blockingRemoveOldest();
            if (request != null) {
                try {
                    service.serve(request);
                } catch (Throwable e) {
                    logger.error("Cannot serve request: " + request, e);
                }
            }
        }
    }

    @Override
    @ImmediateService
    public String getJobServerLogs(String jobId) throws UnknownJobException, NotConnectedException,
            PermissionException {
        JobId id = JobIdImpl.makeJobId(jobId);
        frontendState.checkJobOwner("getJobServerLogs", id,
                "You do not have permissions to get the logs of this job");

        String folderName = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
            System.getProperty("file.separator") +
            PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.getValueAsString();

        StringBuilder result = new StringBuilder();
        File jobLogsFile = new File(folderName + jobId);
        if (jobLogsFile.exists()) {
            try {
                result.append("================= Job " + jobId + " logs =================\n");
                result.append(new Scanner(jobLogsFile).useDelimiter("\\Z").next());

                for (TaskId taskId : frontendState.getJobTasks(id)) {
                    result.append("\n================ Task " + taskId + " logs =================\n");
                    result.append(getTaskServerLogs(taskId));
                }

                return result.toString();
            } catch (FileNotFoundException e) {
                // should be be here
                logger.warn(e);
            }
        }

        return "Cannot retrieve logs for job " + jobId;
    }

    @Override
    @ImmediateService
    public String getTaskServerLogs(String jobId, String taskName) throws UnknownJobException,
            UnknownTaskException, NotConnectedException, PermissionException {

        JobId id = JobIdImpl.makeJobId(jobId);
        frontendState.checkJobOwner("getTaskServerLogs", id,
                "You do not have permission to get the task logs of this job");

        for (TaskId taskId : frontendState.getJobTasks(id)) {
            if (taskId.getReadableName().equals(taskName)) {
                return getTaskServerLogs(taskId);
            }
        }

        throw new UnknownTaskException("Unknown task " + taskName + " in job " + jobId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<JobInfo> getJobs(int offset, int limit, JobFilterCriteria filterCriteria,
            List<SortParameter<JobSortParameter>> sortParameters) throws NotConnectedException,
            PermissionException {
        UserIdentificationImpl ident = frontendState.checkPermission("loadJobs",
                "You don't have permissions to load jobs");
        frontendState.checkOwnStatePermission(filterCriteria.isMyJobsOnly(), ident);

        String user;
        if (filterCriteria.isMyJobsOnly()) {
            user = ident.getUsername();
        } else {
            user = null;
        }
        return dbManager.getJobs(offset, limit, user, filterCriteria.isPending(), filterCriteria.isRunning(),
                filterCriteria.isFinished(), sortParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        UserIdentificationImpl ident = frontendState.checkPermission("getUsers",
                "You don't have permissions to get users");
        frontendState.checkOwnStatePermission(false, ident);
        return frontendState.getUsers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate) throws NotConnectedException,
            PermissionException {
        UserIdentificationImpl ident = frontendState.checkPermission("getMyAccountUsage",
                "You don't have permissions to get usage data for your account");
        return dbManager.getUsage(ident.getUsername(), startDate, endDate);
    }

    private String getTaskServerLogs(TaskId id) {
        String folderName = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
            System.getProperty("file.separator") +
            PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.getValueAsString();

        File taskLogsFile = new File(folderName + id);
        if (taskLogsFile.exists()) {
            try {
                return new Scanner(taskLogsFile).useDelimiter("\\Z").next();
            } catch (FileNotFoundException e) {
                // should not be here
                logger.warn(e);
            }
        }
        return "Cannot retrieve logs for task " + id;
    }
}
