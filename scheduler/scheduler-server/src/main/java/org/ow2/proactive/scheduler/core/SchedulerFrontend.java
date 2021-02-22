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

import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSIONS_TO_GET_THE_LOGS_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_ATTACH_SERVICE_TO_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_DO_THIS_OPERATION;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_ENABLE_VISE_THIS_TASK;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_FINISH_THIS_TASK;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_FREEZE_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_RESULT_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_LOGS_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_TASK;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_LISTEN_THE_LOG_OF_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_LIST_THIRD_PARTY_CREDENTIALS_IN_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_PREEMPT_THIS_TASK;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_PUT_THIRD_PARTY_CREDENTIALS_IN_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_RELOAD_POLICY_CONFIGURATION;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIRD_PARTY_CREDENTIALS_FROM_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_IN_ERROR_TASKS_IN_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THIS_JOB;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_SHUTDOWN_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_START_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_STOP_THE_SCHEDULER;
import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.YOU_DO_NOT_HAVE_PERMISSION_TO_SUBMIT_A_JOB;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.policy.ClientsPolicy;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.Page;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
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
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatesPage;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.core.account.SchedulerAccountsManager;
import org.ow2.proactive.scheduler.core.db.RecoveredSchedulerState;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.core.helpers.JobsMemoryMonitorRunner;
import org.ow2.proactive.scheduler.core.helpers.TableSizeMonitorRunner;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxy;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.job.IdentifiedJob;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.SchedulerUserInfo;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.signal.SignalApiException;
import org.ow2.proactive.scheduler.signal.SignalApiImpl;
import org.ow2.proactive.scheduler.synchronization.AOSynchronization;
import org.ow2.proactive.scheduler.synchronization.CompilationException;
import org.ow2.proactive.scheduler.synchronization.InvalidChannelException;
import org.ow2.proactive.scheduler.synchronization.SynchronizationInternal;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.SchedulerPortalConfiguration;
import org.ow2.proactive.scheduler.util.ServerJobAndTaskLogs;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.proactive.utils.PAExecutors;
import org.ow2.proactive.utils.Tools;


/**
 * Scheduler Front-end. This is the API to talk to when you want to managed a
 * scheduler core. Creating this class can only be done by using
 * <code>SchedulerFactory</code>. You can join this front-end by using the
 * <code>join()</code> method in {@link SchedulerConnection}.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@ActiveObject
public class SchedulerFrontend implements InitActive, Scheduler, RunActive, EndActive, SchedulerSpaceInterface {

    /**
     * Delay to wait for between getting a job result and removing the job
     * concerned
     */
    private static final long SCHEDULER_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_REMOVED_JOB_DELAY.getValueAsLong() *
                                                            1000;

    private static final Logger logger = Logger.getLogger(SchedulerFrontend.class);

    /**
     * Temporary rmURL at starting process
     */
    private URI rmURL;

    /**
     * Authentication Interface
     */
    private SchedulerAuthentication authentication;

    /**
     * Full name of the policy class
     */
    private String policyFullName;

    /**
     * JMX Helper reference
     */
    private SchedulerJMXHelper jmxHelper;

    private SchedulingService schedulingService;

    private SchedulerDBManager dbManager;

    private SchedulerFrontendState frontendState;

    private SchedulerSpacesSupport spacesSupport;

    private SchedulerStatus initialStatus = SchedulerStatus.STARTED;

    private PublicKey corePublicKey;

    private SchedulerPortalConfiguration schedulerPortalConfiguration = SchedulerPortalConfiguration.getConfiguration();

    private it.sauronsoftware.cron4j.Scheduler metricsMonitorScheduler;

    /**
     * Attributes used for the signal api
     */
    private SynchronizationInternal publicStore;

    private String signalsChannel = PASchedulerProperties.SCHEDULER_SIGNALS_CHANNEL.getValueAsString();

    private static final String SIGNAL_ORIGINATOR = "scheduler";

    private static final String SIGNAL_TASK = "0t0";

    private static final TaskId SIGNAL_TASK_ID = TaskIdImpl.makeTaskId(SIGNAL_TASK);

    /*
     * ######################################################################### ##################
     */
    /*                                                                                             */
    /*
     * ################################## SCHEDULER CONSTRUCTION #################################
     */
    /*                                                                                             */
    /*
     * ######################################################################### ##################
     */

    /**
     * ProActive empty constructor
     */
    public SchedulerFrontend() {
    }

    /**
     * Mainly there for testing purposes.
     *
     * It allows to create a SchedulerFrontend instance without breaking
     * encapsulation.
     *
     * @param schedulerSpacesSupport
     */
    SchedulerFrontend(SchedulerFrontendState schedulerFrontendState, SchedulerSpacesSupport schedulerSpacesSupport) {
        this.frontendState = schedulerFrontendState;
        this.spacesSupport = schedulerSpacesSupport;
    }

    /**
     * Scheduler Front-end constructor.
     *
     * @param rmURL
     *            a started Resource Manager URL which be able to managed the
     *            resource used by scheduler.
     * @param policyFullClassName
     *            the full class name of the policy to use.
     */
    public SchedulerFrontend(URI rmURL, String policyFullClassName, SchedulerStatus initialStatus) {
        this.dbManager = SchedulerDBManager.createUsingProperties();
        SchedulerAccountsManager accountsManager = new SchedulerAccountsManager(dbManager);
        this.jmxHelper = new SchedulerJMXHelper(accountsManager, dbManager);

        logger.debug("Creating scheduler Front-end...");
        this.rmURL = rmURL;
        this.policyFullName = policyFullClassName;
        this.initialStatus = initialStatus;
        logger.debug("Policy used is " + policyFullClassName);
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    @Override
    public void initActivity(Body body) {
        try {
            // setting up the policy
            logger.debug("Setting up scheduler security policy");
            ClientsPolicy.init();

            // creating the scheduler authentication interface.
            // if this fails then it will not continue.
            logger.debug("Creating scheduler authentication interface...");
            authentication = PAActiveObject.newActive(SchedulerAuthentication.class,
                                                      new Object[] { PAActiveObject.getStubOnThis() });
            // creating scheduler core

            DataSpaceServiceStarter dsServiceStarter = DataSpaceServiceStarter.getDataSpaceServiceStarter();
            dsServiceStarter.startNamingService();

            ExecutorService clientThreadPool = PAExecutors.newCachedBoundedThreadPool(1,
                                                                                      PASchedulerProperties.SCHEDULER_CLIENT_POOL_NBTHREAD.getValueAsInt(),
                                                                                      120L,
                                                                                      TimeUnit.SECONDS,
                                                                                      new NamedThreadFactory("ClientRequestsThreadPool"));

            ExecutorService internalThreadPool = PAExecutors.newCachedBoundedThreadPool(1,
                                                                                        PASchedulerProperties.SCHEDULER_INTERNAL_POOL_NBTHREAD.getValueAsInt(),
                                                                                        120L,
                                                                                        TimeUnit.SECONDS,
                                                                                        new NamedThreadFactory("InternalOperationsThreadPool"));

            ExecutorService taskPingerThreadPool = PAExecutors.newCachedBoundedThreadPool(1,
                                                                                          PASchedulerProperties.SCHEDULER_TASK_PINGER_POOL_NBTHREAD.getValueAsInt(),
                                                                                          120L,
                                                                                          TimeUnit.SECONDS,
                                                                                          new NamedThreadFactory("TaskPingerThreadPool"));

            ScheduledExecutorService scheduledThreadPool = new ScheduledThreadPoolExecutor(PASchedulerProperties.SCHEDULER_SCHEDULED_POOL_NBTHREAD.getValueAsInt(),
                                                                                           new NamedThreadFactory("SchedulingServiceTimerThread"));

            // at this point we must wait the resource manager
            RMConnection.waitAndJoin(rmURL.toString());
            RMProxiesManager rmProxiesManager = RMProxiesManager.createRMProxiesManager(rmURL);
            RMProxy rmProxy = rmProxiesManager.getRmProxy();

            long loadJobPeriod = -1;
            if (PASchedulerProperties.SCHEDULER_DB_LOAD_JOB_PERIOD.isSet()) {
                String periodStr = PASchedulerProperties.SCHEDULER_DB_LOAD_JOB_PERIOD.getValueAsString();
                if (periodStr != null && !periodStr.isEmpty()) {
                    try {
                        loadJobPeriod = Tools.parsePeriod(periodStr);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid load job period string: " + periodStr + ", this setting is ignored", e);
                    }
                }
            }

            logger.debug("Booting jmx...");
            this.jmxHelper.boot(authentication);
            publicStore = startSynchronizationService();

            RecoveredSchedulerState recoveredState = new SchedulerStateRecoverHelper(dbManager).recover(loadJobPeriod,
                                                                                                        rmProxy,
                                                                                                        initialStatus);

            this.frontendState = new SchedulerFrontendState(recoveredState.getSchedulerState(), jmxHelper, dbManager);

            SchedulingInfrastructure infrastructure = new SchedulingInfrastructureImpl(dbManager,
                                                                                       rmProxiesManager,
                                                                                       dsServiceStarter,
                                                                                       clientThreadPool,
                                                                                       internalThreadPool,
                                                                                       taskPingerThreadPool,
                                                                                       scheduledThreadPool);

            this.spacesSupport = infrastructure.getSpacesSupport();

            ServerJobAndTaskLogs.getInstance().setSpacesSupport(this.spacesSupport);

            this.corePublicKey = Credentials.getPublicKey(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_AUTH_PUBKEY_PATH.getValueAsString()));
            this.schedulingService = new SchedulingService(infrastructure,
                                                           frontendState,
                                                           recoveredState,
                                                           policyFullName,
                                                           null,
                                                           publicStore);

            recoveredState.enableLiveLogsForRunningTasks(schedulingService);
            releaseBusyNodesWithNoRunningTask(rmProxy, recoveredState);

            logger.debug("Registering scheduler...");
            PAActiveObject.registerByName(authentication, SchedulerConstants.SCHEDULER_DEFAULT_NAME);
            authentication.setActivated(true);

            Tools.logAvailableScriptEngines(logger);

            if (PASchedulerProperties.SCHEDULER_MEM_MONITORING_FREQ.isSet()) {
                logger.debug("Starting the memory monitoring process...");
                metricsMonitorScheduler = new it.sauronsoftware.cron4j.Scheduler();
                String cronExpr = PASchedulerProperties.SCHEDULER_MEM_MONITORING_FREQ.getValueAsString();
                metricsMonitorScheduler.schedule(cronExpr,
                                                 new TableSizeMonitorRunner(dbManager.getTransactionHelper()));
                metricsMonitorScheduler.schedule(cronExpr,
                                                 new JobsMemoryMonitorRunner(dbManager.getSessionFactory()
                                                                                      .getStatistics(),
                                                                             recoveredState.getSchedulerState()));
                metricsMonitorScheduler.start();
            }

        } catch (Exception e) {
            logger.fatal("Failed to start Scheduler", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private SynchronizationInternal startSynchronizationService() throws java.io.IOException, ProActiveException {
        // Create and start the Synchronization Service Active Object
        final AOSynchronization privateStore = PAActiveObject.newActive(AOSynchronization.class,
                                                                        new Object[] { PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_SYNCHRONIZATION_DATABASE.getValueAsString()) });

        // Wait for the service to be actually started
        privateStore.isStarted();

        Runtime.getRuntime()
               .addShutdownHook(new Thread(() -> PAActiveObject.terminateActiveObject(privateStore, true)));

        // We use the following trick to obtain a ProActive Stub which only implements methods declared in the SynchronizationInternal interface.
        // As this stub will be used remotely inside task, we make sure that it does not drag unnecessary dependencies (static fields, internal methods, etc)
        SynchronizationInternal publicStore = PAActiveObject.lookupActive(SynchronizationInternal.class,
                                                                          PAActiveObject.getUrl(privateStore));

        // register this service and give it a name
        PAActiveObject.registerByName(publicStore, SchedulerConstants.SYNCHRONIZATION_DEFAULT_NAME);

        publicStore.createChannelIfAbsent(SIGNAL_ORIGINATOR, SIGNAL_TASK_ID, signalsChannel, true);

        return publicStore;
    }

    private void releaseBusyNodesWithNoRunningTask(RMProxy rmProxy, RecoveredSchedulerState recoveredState) {
        List<InternalJob> runningJobs = recoveredState.getRunningJobs();
        List<NodeSet> busyNodesWithTask = findBusyNodesCorrespondingToRunningTasks(runningJobs);

        rmProxy.releaseDanglingBusyNodes(busyNodesWithTask);
    }

    private List<NodeSet> findBusyNodesCorrespondingToRunningTasks(List<InternalJob> runningJobs) {
        List<NodeSet> busyNodesWithTask = new LinkedList<>();

        for (InternalJob runningJob : runningJobs) {
            List<InternalTask> tasks = runningJob.getITasks();

            for (InternalTask task : tasks) {
                if (task.getStatus().equals(TaskStatus.RUNNING)) {

                    busyNodesWithTask.add(task.getExecuterInformation().getNodes());
                }
            }
        }

        return busyNodesWithTask;
    }

    /*
     * *******************************
     *
     * SCHEDULING MANAGEMENT
     *
     * *******************************
     */

    /**
     * Connect a new user on the scheduler. This user can interact with the
     * scheduler according to his right.
     *
     * @param sourceBodyID
     *            the source ID of the connected object representing a user
     * @param identification
     *            the identification of the connected user
     * @param cred
     *            the credentials of the user
     * @throws AlreadyConnectedException
     *             if the user is already connected
     */
    @ImmediateService
    public void connect(UniqueID sourceBodyID, UserIdentificationImpl identification, Credentials cred)
            throws AlreadyConnectedException {
        Credentials enrichedCreds = cred;
        try {
            enrichedCreds = schedulingService.addThirdPartyCredentials(cred);
        } catch (Exception e) {
            logger.warn("Could not add third party credentials to connection of user " + identification.getUsername() +
                        " from " + identification.getHostName());
        }
        this.frontendState.connect(sourceBodyID, identification, enrichedCreds);

        this.spacesSupport.registerUserSpace(identification.getUsername(), cred);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public JobId reSubmit(JobId currentJobId, Map<String, String> jobVariables, Map<String, String> jobGenericInfos)
            throws NotConnectedException, UnknownJobException, PermissionException, JobCreationException,
            SubmissionClosedException {
        final String jobContent = getJobContent(currentJobId);
        final Job job = JobFactory.getFactory().createJob(IOUtils.toInputStream(jobContent, Charset.forName("UTF-8")),
                                                          jobVariables,
                                                          jobGenericInfos,
                                                          this,
                                                          this);
        return submit(job);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public JobId submit(Job userJob)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("New job submission requested : " + userJob.getName());
            }
            long t0 = System.currentTimeMillis();
            // check if the scheduler is stopped
            if (!schedulingService.isSubmitPossible()) {
                String msg = "Scheduler is stopped, cannot submit job";
                logger.info(msg);
                throw new SubmissionClosedException(msg);
            }

            long t1 = System.currentTimeMillis();
            UserIdentificationImpl ident = frontendState.checkPermission("submit",
                                                                         YOU_DO_NOT_HAVE_PERMISSION_TO_SUBMIT_A_JOB);
            long t2 = System.currentTimeMillis();
            InternalJob job = frontendState.createJob(userJob, ident);
            long t3 = System.currentTimeMillis();
            schedulingService.submitJob(job);
            long t4 = System.currentTimeMillis();
            frontendState.jobSubmitted(job, ident);
            long t5 = System.currentTimeMillis();
            long d1 = t1 - t0;
            long d2 = t2 - t1;
            long d3 = t3 - t2;
            long d4 = t4 - t3;
            long d5 = t5 - t4;
            logger.debug(String.format("timer;%d;%d;%d;%d;%d;%d", job.getId().longValue(), d1, d2, d3, d4, d5));
            return job.getId();
        } catch (Exception e) {
            logger.warn("Error when submitting job.", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        SchedulerFrontendState.UserAndCredentials userAndCredentials = frontendState.checkPermissionReturningCredentials("getUserSpaceURIs",
                                                                                                                         "You don't have permissions to read the USER Space URIs",
                                                                                                                         false);
        return this.spacesSupport.getUserSpaceURIs(userAndCredentials.getListeningUser().getUser().getUsername(),
                                                   userAndCredentials.getCredentials());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("getGlobalSpaceURIs", "You don't have permissions to read the GLOBAL Space URI");
        return this.spacesSupport.getGlobalSpaceURIs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public JobResult getJobResult(final JobId jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {

        // checking permissions
        IdentifiedJob ij = frontendState.getIdentifiedJob(jobId);

        frontendState.checkPermissions("getJobResult", ij, YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_RESULT_OF_THIS_JOB);

        if (!ij.isFinished()) {
            logger.info("Job " + jobId + " is not finished");
            logger.info("Job " + jobId + " state: " + frontendState.getJobState(jobId).getStatus());
            return null;
        }

        logger.info("Trying to get result of job " + jobId);

        JobResult result = dbManager.loadJobResult(jobId);
        if (result == null) {
            throw new UnknownJobException(jobId);
        }

        if (!result.getJobInfo().isToBeRemoved() && SCHEDULER_REMOVED_JOB_DELAY > 0) {
            // remember that this job is to be removed
            dbManager.jobSetToBeRemoved(jobId);
            schedulingService.scheduleJobRemove(jobId, System.currentTimeMillis() + SCHEDULER_REMOVED_JOB_DELAY);
            logger.info("Job " + jobId + " will be removed in " + (SCHEDULER_REMOVED_JOB_DELAY / 1000) + "sec");
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        return this.getJobResult(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public TaskResult getTaskResult(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResultFromIncarnation(jobId, taskName, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public TaskResult getTaskResult(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResult(JobIdImpl.makeJobId(jobId), taskName);
    }

    @Override
    @ImmediateService
    public List<TaskResult> getTaskResultsByTag(JobId jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        frontendState.checkPermission("getTaskResultByTag",
                                      YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB);
        List<TaskState> taskStates = getJobState(jobId).getTasksByTag(taskTag);
        ArrayList<TaskResult> results = new ArrayList<>(taskStates.size());
        for (TaskState currentState : taskStates) {
            String taskName = currentState.getTaskInfo().getName();
            try {
                TaskResult currentResult = getTaskResult(jobId, taskName);
                results.add(currentResult);
            } catch (UnknownTaskException ex) {
                // never occurs because tasks are filtered by tag so they cannot
                // be unknown.
                logger.warn("Unknown task.", ex);
            }
        }
        return results;
    }

    @Override
    @ImmediateService
    public List<TaskResult> getTaskResultsByTag(String jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return this.getTaskResultsByTag(JobIdImpl.makeJobId(jobId), taskTag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResultFromIncarnation(JobIdImpl.makeJobId(jobId), taskName, inc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {

        // checking permissions
        frontendState.checkPermissions("getTaskResultFromIncarnation",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB);

        if (inc < 0) {
            throw new IllegalArgumentException("Incarnation must be 0 or greater.");
        }

        logger.debug("Job " + jobId + ", trying to get the task result, incarnation " + inc);

        try {
            TaskResult result = dbManager.loadTaskResult(jobId, taskName, inc);
            // handling special statuses
            TaskState ts = frontendState.getTaskState(jobId, taskName);
            switch (ts.getStatus()) {
                case NOT_STARTED:
                    if (result == null) {
                        return new TaskResultImpl(frontendState.getTaskId(jobId, taskName),
                                                  new TaskCouldNotStartException(),
                                                  new SimpleTaskLogs("",
                                                                     "The task could not start due to dependency failure"),
                                                  0);
                    } else {
                        Throwable newException = new TaskCouldNotStartException("The task could not start due to dependency failure",
                                                                                result.getException());
                        ((TaskResultImpl) result).setException(newException);
                    }
                    break;
                case NOT_RESTARTED:
                    if (result == null) {
                        return new TaskResultImpl(frontendState.getTaskId(jobId, taskName),
                                                  new TaskCouldNotRestartException(),
                                                  new SimpleTaskLogs("",
                                                                     "The task could not be restarted after an error during the previous execution"),
                                                  0);
                    } else {
                        Throwable newException = new TaskCouldNotRestartException("The task could not be restarted after an error during the previous execution",
                                                                                  result.getException());
                        ((TaskResultImpl) result).setException(newException);
                    }
                    break;
                case SKIPPED:
                    // result should always be null
                    return new TaskResultImpl(frontendState.getTaskId(jobId, taskName),
                                              new TaskSkippedException(),
                                              new SimpleTaskLogs("", "The task was skipped in the workflow"),
                                              0);
                default:
                    if (result == null) {
                        // otherwise the task is not finished
                        logger.info("Task " + taskName + " of job " + jobId + " is not finished");
                    }
                    break;
            }
            return result;

        } catch (DatabaseManagerException e) {
            throw new UnknownTaskException("Unknown task " + taskName + ", job: " + jobId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<TaskResult> getTaskResultAllIncarnations(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResultAllIncarnations(JobIdImpl.makeJobId(jobId), taskName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<TaskResult> getTaskResultAllIncarnations(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        frontendState.checkPermissions("getTaskResultFromIncarnation",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB);

        logger.debug("Job " + jobId + ", trying to get all task results associated with task " + taskName +
                     " from job " + jobId);

        try {
            return dbManager.loadTaskResultAllAttempts(jobId, taskName);

        } catch (DatabaseManagerException e) {
            throw new UnknownTaskException("Unknown task " + taskName + ", job: " + jobId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean killTask(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        // checking permissions
        frontendState.checkPermissions("killTask",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_TASK);
        logger.info("Request to kill task " + taskName + " of job " + jobId + " received from " + currentUser);
        return schedulingService.killTask(jobId, taskName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean killTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return killTask(JobIdImpl.makeJobId(jobId), taskName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean restartTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermissions("restartTask",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK);
        logger.info("Request to restart task " + taskName + " of job " + jobId + " received from " + currentUser);
        return schedulingService.restartTask(jobId, taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean restartTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return restartTask(JobIdImpl.makeJobId(jobId), taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean finishInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions("finishInErrorTask",
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_FINISH_THIS_TASK);
        logger.info("Request to finish in-error task " + taskName + " of job " + jobId + " received from " +
                    currentUser);
        return schedulingService.finishInErrorTask(jobIdObject, taskName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean restartInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions("restartInErrorTask",
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK);
        logger.info("Request to restart in-error task " + taskName + " of job " + jobId + " received from " +
                    currentUser);
        return schedulingService.restartInErrorTask(jobIdObject, taskName);
    }

    @Override
    @ImmediateService
    public void enableRemoteVisualization(String jobId, String taskName, String connectionString)
            throws NotConnectedException, PermissionException, UnknownJobException, UnknownTaskException {
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions("enableRemoteVisualization",
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_ENABLE_VISE_THIS_TASK);
        logger.info("Request to enable visualization on task " + taskName + " of job " + jobId + " received from " +
                    currentUser);
        schedulingService.enableRemoteVisualization(jobIdObject, taskName, connectionString);
    }

    @Override
    @ImmediateService
    public void registerService(String jobId, int serviceId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions("registerService",
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_ATTACH_SERVICE_TO_THIS_JOB);
        logger.info("Request to register service " + serviceId + " on job " + jobId + " received from " + currentUser);
        schedulingService.registerService(jobIdObject, serviceId);
    }

    @Override
    @ImmediateService
    public void detachService(String jobId, int serviceId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions("detachService",
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_ATTACH_SERVICE_TO_THIS_JOB);
        logger.info("Request to detach service " + serviceId + " on job " + jobId + " received from " + currentUser);
        schedulingService.detachService(jobIdObject, serviceId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermissions("preemptTask",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_PREEMPT_THIS_TASK);
        logger.info("Request to preempt task " + taskName + " of job " + jobId + " received from " + currentUser);
        return schedulingService.preemptTask(jobId, taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean preemptTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return preemptTask(JobIdImpl.makeJobId(jobId), taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {

        String currentUser = frontendState.getCurrentUser();
        // checking permissions
        frontendState.checkPermissions("removeJob",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIS_JOB);
        logger.info("Request to remove job " + jobId + " received from " + currentUser);

        // asking the scheduler for the result
        return schedulingService.removeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public boolean removeJobs(List<JobId> jobIds) throws NotConnectedException, PermissionException {
        if (jobIds.isEmpty()) {
            return false;
        }
        // checking permission for each of the job
        for (JobId jobId : jobIds) {
            try {
                frontendState.checkPermissions("removeJob",
                                               frontendState.getIdentifiedJob(jobId),
                                               YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIS_JOB);
            } catch (UnknownJobException e) {
                logger.debug(e);
            }
        }

        return schedulingService.removeJobs(jobIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException {
        // checking permissions
        frontendState.checkPermissions("listenJobLogs",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_LISTEN_THE_LOG_OF_THIS_JOB);

        if (!schedulingService.getListenJobLogsSupport().isEnabled()) {
            throw new PermissionException("Listening to job logs is disabled by administrator");
        }

        schedulingService.listenJobLogs(jobId, appenderProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public void listenJobLogs(String jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException {
        this.listenJobLogs(JobIdImpl.makeJobId(jobId), appenderProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        return frontendState.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return getState(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        return frontendState.getState(myJobsOnly);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        addEventListener(sel, myEventsOnly, false, events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly, boolean getCurrentState,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        return frontendState.addEventListener(sel, myEventsOnly, getCurrentState, events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public void removeEventListener() throws NotConnectedException, PermissionException {
        frontendState.removeEventListener();
    }

    /*
     * ######################################################################### ##################
     */
    /*                                                                                             */
    /*
     * ##################################### SCHEDULER ORDERS ####################################
     */
    /*                                                                                             */
    /*
     * ######################################################################### ##################
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean start() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermission("start", YOU_DO_NOT_HAVE_PERMISSION_TO_START_THE_SCHEDULER);
        logger.info("Request to start scheduler server received from " + currentUser);
        return schedulingService.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stop() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermission("stop", YOU_DO_NOT_HAVE_PERMISSION_TO_STOP_THE_SCHEDULER);
        logger.info("Request to stop scheduler server received from " + currentUser);
        return schedulingService.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pause() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermission("pause", YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THE_SCHEDULER);
        logger.info("Request to pause scheduler server received from " + currentUser);
        return schedulingService.pause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean freeze() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermission("freeze", YOU_DO_NOT_HAVE_PERMISSION_TO_FREEZE_THE_SCHEDULER);
        logger.info("Request to freeze scheduler server received from " + currentUser);
        return schedulingService.freeze();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean resume() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermission("resume", YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THE_SCHEDULER);
        logger.info("Request to resume scheduler server received from " + currentUser);
        return schedulingService.resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shutdown() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermission("shutdown", YOU_DO_NOT_HAVE_PERMISSION_TO_SHUTDOWN_THE_SCHEDULER);
        logger.info("Request to shutdown scheduler server received from " + currentUser);
        return schedulingService.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean kill() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermission("kill", YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THE_SCHEDULER);
        logger.info("Request to kill scheduler server received from " + currentUser);
        return schedulingService.kill();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public void disconnect() throws NotConnectedException, PermissionException {
        frontendState.disconnect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean isConnected() {
        return frontendState.isConnected();
    }

    @Override
    public String getCurrentPolicy() throws NotConnectedException, PermissionException {
        return policyFullName;
    }

    @Override
    @ImmediateService
    public Map<JobId, JobDescriptor> getJobsToSchedule() throws NotConnectedException, PermissionException {
        Map<JobId, JobDescriptor> jobMap = schedulingService.lockJobsToSchedule();
        schedulingService.unlockJobsToSchedule(jobMap.values());
        return jobMap;
    }

    @Override
    @ImmediateService
    public List<TaskDescriptor> getTasksToSchedule() throws NotConnectedException, PermissionException {
        Policy policy = null;
        List<TaskDescriptor> eligibleTasks = new ArrayList<>();
        Map<JobId, JobDescriptor> jobMap = null;
        try {
            jobMap = schedulingService.lockJobsToSchedule();
            policy = (Policy) Class.forName(getCurrentPolicy()).newInstance();

            // If there are some jobs which could not be locked it is not possible to do any priority scheduling decision,
            // we wait for next scheduling loop
            if (jobMap.isEmpty()) {
                return eligibleTasks;
            }
            List<JobDescriptor> descriptors = new ArrayList<>(jobMap.values());
            LinkedList<EligibleTaskDescriptor> taskRetrievedFromPolicy = policy.getOrderedTasks(descriptors);
            //if there is no task to scheduled, return
            if (taskRetrievedFromPolicy.isEmpty()) {
                return eligibleTasks;
            }
            eligibleTasks = (List) taskRetrievedFromPolicy;
        } catch (Exception e) {
            logger.error("Error Loading Current Policy:", e);
        }

        finally {
            schedulingService.unlockJobsToSchedule(jobMap.values());
        }
        return eligibleTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public void renewSession() throws NotConnectedException {
        frontendState.renewSession(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermissions("pauseJob",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THIS_JOB);
        logger.info("Request to pause job " + jobId + " received from " + currentUser);
        return schedulingService.pauseJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermissions("resumeJob",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THIS_JOB);
        logger.info("Request to resume job " + jobId + " received from " + currentUser);
        return schedulingService.resumeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermissions("killJob",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_JOB);
        logger.info("Request to kill job " + jobId + " received from " + currentUser);
        return schedulingService.killJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public void changeJobPriority(JobId jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkChangeJobPriority(jobId, priority);
        logger.info("Request to change job " + jobId + " priority to " + priority + " received from " + currentUser);
        schedulingService.changeJobPriority(jobId, priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return frontendState.getJobState(jobId);
    }

    @Override
    public TaskState getTaskState(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, PermissionException, UnknownTaskException {
        return frontendState.getTaskState(jobId, taskName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public TaskStatesPage getTaskPaginated(String jobId, int offset, int limit)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return frontendState.getTaskPaginated(JobIdImpl.makeJobId(jobId), offset, limit);
    }

    @Override
    @ImmediateService
    public TaskStatesPage getTaskPaginated(String jobId, String statusFilter, int offset, int limit)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return frontendState.getTaskPaginated(JobIdImpl.makeJobId(jobId), statusFilter, offset, limit);
    }

    @Override
    @ImmediateService
    public List<TaskResult> getPreciousTaskResults(String jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        frontendState.checkPermission("getJobResult", YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB);
        List<TaskState> taskStates = getJobState(jobId).getTasks()
                                                       .stream()
                                                       .filter(Task::isPreciousResult)
                                                       .collect(Collectors.toList());

        List<TaskResult> results = new ArrayList<>();
        for (TaskState currentState : taskStates) {
            String taskName = currentState.getTaskInfo().getName();
            try {
                TaskResult currentResult = getTaskResult(jobId, taskName);
                if (currentResult != null) {
                    results.add(currentResult);
                }
            } catch (UnknownTaskException ex) {
                // never occurs because tasks are filtered by tag so they cannot
                // be unknown.
                logger.warn("Unknown task.", ex);
            }
        }
        return results;
    }

    @Override
    @ImmediateService
    public Map<Long, Map<String, Serializable>> getJobResultMaps(List<String> jobsId) {
        return dbManager.getJobResultMaps(jobsId);
    }

    @Override
    @ImmediateService
    public Map<Long, List<String>> getPreciousTaskNames(List<String> jobsId)
            throws NotConnectedException, PermissionException {
        return dbManager.getPreciousTaskNames(jobsId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.killJob(JobIdImpl.makeJobId(jobId));
    }

    @Override
    @ImmediateService
    public boolean killJobs(List<String> jobIds) throws NotConnectedException, PermissionException {

        if (jobIds.isEmpty()) {
            return false;
        }
        String currentUser = frontendState.getCurrentUser();
        List<JobId> jobIdsConverted = jobIds.stream().map(JobIdImpl::makeJobId).collect(Collectors.toList());
        // checking permission for each of the job
        for (JobId jobId : jobIdsConverted) {
            try {
                frontendState.checkPermissions("killJob",
                                               frontendState.getIdentifiedJob(jobId),
                                               YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THIS_JOB);
            } catch (UnknownJobException e) {
                logger.debug(e);
            }
        }

        logger.info("Request to kill jobs " + jobIds + " received from " + currentUser);

        return schedulingService.killJobs(jobIdsConverted);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.pauseJob(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.removeJob(JobIdImpl.makeJobId(jobId));
    }

    @Override
    @ImmediateService
    public boolean restartAllInErrorTasks(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermissions("restartAllInErrorTasks",
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_IN_ERROR_TASKS_IN_THIS_JOB);
        logger.info("Request to restart all in-error tasks on job " + jobId + " received from " + currentUser);
        return schedulingService.restartAllInErrorTasks(jobIdObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.resumeJob(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public void changeJobPriority(String jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        this.changeJobPriority(JobIdImpl.makeJobId(jobId), priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.getJobState(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changePolicy(String newPolicyClassname) throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkChangePolicy();
        policyFullName = newPolicyClassname;
        logger.info("Request to change scheduler policy to " + newPolicyClassname + " received from " + currentUser);
        return schedulingService.changePolicy(newPolicyClassname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean linkResourceManager(String rmURL) throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkLinkResourceManager();
        logger.info("Request link on Resource Manager " + rmURL + " received from " + currentUser);
        return schedulingService.linkResourceManager(rmURL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        frontendState.checkPermission("reloadPolicyConfiguration",
                                      YOU_DO_NOT_HAVE_PERMISSION_TO_RELOAD_POLICY_CONFIGURATION);
        logger.info("Request to reload policy configuration received from " + currentUser);
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
     * Method controls the execution of every request. Tries to keep this active
     * object alive in case of any exception.
     */
    @Override
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            try {
                Request request = service.blockingRemoveOldest();
                if (request != null) {
                    try {
                        service.serve(request);
                    } catch (Throwable e) {
                        logger.error("Cannot serve request: " + request, e);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("SchedulerFrontend runActivity interrupted", e);
            }
        }
    }

    @Override
    @ImmediateService
    public String getJobServerLogs(String jobId)
            throws UnknownJobException, NotConnectedException, PermissionException {
        JobId id = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions("getJobServerLogs",
                                       frontendState.getIdentifiedJob(id),
                                       YOU_DO_NOT_HAVE_PERMISSIONS_TO_GET_THE_LOGS_OF_THIS_JOB);

        return ServerJobAndTaskLogs.getInstance().getJobLog(JobIdImpl.makeJobId(jobId), frontendState.getJobTasks(id));
    }

    @Override
    @ImmediateService
    public String getTaskServerLogs(String jobId, String taskName)
            throws UnknownJobException, UnknownTaskException, NotConnectedException, PermissionException {

        JobId id = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions("getTaskServerLogs",
                                       frontendState.getIdentifiedJob(id),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_LOGS_OF_THIS_JOB);

        for (TaskId taskId : frontendState.getJobTasks(id)) {
            if (taskId.getReadableName().equals(taskName)) {
                return ServerJobAndTaskLogs.getInstance().getTaskLog(taskId);
            }
        }

        throw new UnknownTaskException("Unknown task " + taskName + " in job " + jobId);
    }

    @Override
    @ImmediateService
    public String getTaskServerLogsByTag(String jobId, String taskTag)
            throws UnknownJobException, NotConnectedException, PermissionException {
        JobId id = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions("getTaskServerLogsByTag",
                                       frontendState.getIdentifiedJob(id),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_LOGS_OF_THIS_JOB);
        List<TaskState> lTaskState = frontendState.getJobState(id).getTasksByTag(taskTag);
        Set<TaskId> tasksIds = new HashSet<>(lTaskState.size());
        for (TaskState taskState : lTaskState) {
            tasksIds.add(taskState.getId());
        }

        return ServerJobAndTaskLogs.getInstance().getJobLog(id, tasksIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public Page<JobInfo> getJobs(int offset, int limit, JobFilterCriteria filterCriteria,
            List<SortParameter<JobSortParameter>> sortParameters) throws NotConnectedException, PermissionException {
        UserIdentificationImpl ident = frontendState.checkPermission("getJobs",
                                                                     "You don't have permissions to load jobs");

        boolean myJobsOnly = filterCriteria.isMyJobsOnly();

        String user = null;
        if (myJobsOnly) {
            user = ident.getUsername();
        }

        Page<JobInfo> jobsInfo = dbManager.getJobs(offset,
                                                   limit,
                                                   user,
                                                   filterCriteria.isPending(),
                                                   filterCriteria.isRunning(),
                                                   filterCriteria.isFinished(),
                                                   sortParameters);
        /**
         * Add/inject to each JobInfo the list of signals used by the job, if they exist.
         */
        insertSignals(jobsInfo);

        /**
         * Inject visualization connection strings for running job when available
         */
        insertVisualization(jobsInfo);

        return jobsInfo;
    }

    private void insertVisualization(Page<JobInfo> jobsInfo) {
        jobsInfo.getList()
                .stream()
                .filter(jobInfo -> jobInfo.isStarted() && jobInfo.getFinishedTime() <= 0)
                .forEach(jobInfo -> insertVisualization(jobInfo));
    }

    private void insertSignals(Page<JobInfo> jobsInfo) {
        try {
            List<String> jobHavingSignalsIds = new ArrayList<>(publicStore.keySet(SIGNAL_ORIGINATOR,
                                                                                  SIGNAL_TASK_ID,
                                                                                  signalsChannel));
            jobsInfo.getList()
                    .stream()
                    .filter(jobInfo -> jobHavingSignalsIds.contains(jobInfo.getJobId().value()))
                    .forEach(jobInfo -> insertJobSignals(jobInfo));
        } catch (InvalidChannelException e) {
            logger.warn("Could not acquire the list of jobs having signals. No signals will be included in the returned jobs info");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<JobInfo> getJobsInfoList(List<String> jobsId) throws PermissionException, NotConnectedException {
        List<JobInfo> jobsInfoList = new ArrayList<>();
        for (String jobId : jobsId) {
            try {
                jobsInfoList.add(this.getJobInfo(jobId));
            } catch (UnknownJobException e) {
                logger.warn("The job with job ID " + jobId + " couldn't be found");
            }
        }
        return jobsInfoList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("getUsers", "You don't have permissions to get users");
        return frontendState.getUsers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<SchedulerUserInfo> getUsersWithJobs() throws NotConnectedException, PermissionException {
        frontendState.checkPermission("getUsersWithJobs", "You don't have permissions to get users with jobs");
        return dbManager.loadUsersWithJobs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        UserIdentificationImpl ident = frontendState.checkPermission("getMyAccountUsage",
                                                                     "You don't have permissions to get usage data for your account");
        return dbManager.getUsage(ident.getUsername(), startDate, endDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public List<JobUsage> getAccountUsage(String user, Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        try {
            frontendState.checkPermission("getAccountUsage", "You don't have permissions to get usage data of " + user);

            return dbManager.getUsage(user, startDate, endDate);
        } catch (PermissionException e) {
            // try to fallback on my account usage if user is the caller
            UserIdentificationImpl ident = frontendState.checkPermission("getMyAccountUsage",
                                                                         "You don't have permissions to get usage data of " +
                                                                                              user);
            if (user != null && user.equals(ident.getUsername())) {
                return dbManager.getUsage(ident.getUsername(), startDate, endDate);
            }
            throw e;
        }
    }

    @Override
    @ImmediateService
    public void putThirdPartyCredential(String key, String value) throws NotConnectedException, PermissionException {
        UserIdentificationImpl ident = frontendState.checkPermission("putThirdPartyCredential",
                                                                     YOU_DO_NOT_HAVE_PERMISSION_TO_PUT_THIRD_PARTY_CREDENTIALS_IN_THE_SCHEDULER);

        HybridEncryptionUtil.HybridEncryptedData encryptedData = null;
        try {
            encryptedData = HybridEncryptionUtil.encryptString(value, corePublicKey);
        } catch (KeyException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        dbManager.putThirdPartyCredential(ident.getUsername(), key, encryptedData);
    }

    @Override
    @ImmediateService
    public Set<String> thirdPartyCredentialsKeySet() throws NotConnectedException, PermissionException {
        UserIdentificationImpl ident = frontendState.checkPermission("thirdPartyCredentialsKeySet",
                                                                     YOU_DO_NOT_HAVE_PERMISSION_TO_LIST_THIRD_PARTY_CREDENTIALS_IN_THE_SCHEDULER);
        return dbManager.thirdPartyCredentialsKeySet(ident.getUsername());
    }

    @Override
    @ImmediateService
    public void removeThirdPartyCredential(String key) throws NotConnectedException, PermissionException {
        UserIdentificationImpl ident = frontendState.checkPermission("removeThirdPartyCredential",
                                                                     YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIRD_PARTY_CREDENTIALS_FROM_THE_SCHEDULER);
        dbManager.removeThirdPartyCredential(ident.getUsername(), key);
    }

    @Override
    @ImmediateService
    public Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, Set<TaskStatus> taskStatuses,
            int offset, int limit) throws NotConnectedException, PermissionException {
        String userName = null;
        if (mytasks) {
            userName = frontendState.checkPermission("getTaskIds", "You do not have permission to use frontendState")
                                    .getUsername();
        }
        Page<TaskInfo> pTaskInfo = dbManager.getTasks(from, to, taskTag, offset, limit, userName, taskStatuses);
        List<TaskId> lTaskId = new ArrayList<>(pTaskInfo.getList().size());
        for (TaskInfo taskInfo : pTaskInfo.getList()) {
            lTaskId.add(taskInfo.getTaskId());
        }
        return new Page<>(lTaskId, pTaskInfo.getSize());
    }

    @Override
    @ImmediateService
    public Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks,
            Set<TaskStatus> statusFilter, int offset, int limit, SortSpecifierContainer sortParams)
            throws NotConnectedException, PermissionException {

        String userName = null;
        if (mytasks) {
            userName = frontendState.checkPermission("getTaskStates", "You do not have permission to use frontendState")
                                    .getUsername();
        }
        return dbManager.getTaskStates(from, to, taskTag, offset, limit, userName, statusFilter, sortParams);

    }

    @Override
    @ImmediateService
    public JobInfo getJobInfo(String jobId) throws UnknownJobException, NotConnectedException, PermissionException {
        JobInfo jobInfo = getJobState(JobIdImpl.makeJobId(jobId)).getJobInfo();
        insertJobSignals(jobInfo);
        insertVisualization(jobInfo);
        return jobInfo;
    }

    private JobInfo insertVisualization(JobInfo jobInfo) {
        try {
            JobInfo jobInfoState = frontendState.getJobState(jobInfo.getJobId()).getJobInfo();
            jobInfo.setVisualizationConnectionStrings(jobInfoState.getVisualizationConnectionStrings());
            jobInfo.setVisualizationIcons(jobInfoState.getVisualizationIcons());
        } catch (UnknownJobException | NotConnectedException | PermissionException e) {
            logger.warn("Could not add visualization info for job " + jobInfo.getJobId(), e);
        }
        return jobInfo;
    }

    private JobInfo insertJobSignals(JobInfo jobInfo) {

        String jobid = jobInfo.getJobId().value();

        try {

            if (publicStore.containsKey(SIGNAL_ORIGINATOR, SIGNAL_TASK_ID, signalsChannel, jobid)) {

                Set<String> jobSignals = jobInfo.getSignals();
                Set<String> signalsToBeAdded = (HashSet) publicStore.get(SIGNAL_ORIGINATOR,
                                                                         SIGNAL_TASK_ID,
                                                                         signalsChannel,
                                                                         jobid);
                if (signalsToBeAdded != null && !signalsToBeAdded.isEmpty()) {
                    jobSignals.addAll(signalsToBeAdded);
                    jobInfo.setSignals(jobSignals);
                }
            }
        } catch (InvalidChannelException e) {
            logger.warn("Could not retrieve the signals of the job " + jobid);
        }
        return jobInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public boolean changeStartAt(JobId jobId, String startAt) {
        return schedulingService.changeStartAt(jobId, startAt);
    }

    @Override
    @ImmediateService
    public String getJobContent(JobId jobId) throws UnknownJobException, NotConnectedException, PermissionException {
        frontendState.checkPermissions("getJobContent",
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THIS_JOB);
        return dbManager.loadInitalJobContent(jobId);
    }

    @Override
    @ImmediateService
    public Map<Object, Object> getPortalConfiguration() {
        return schedulerPortalConfiguration.getProperties();
    }

    @Override
    @ImmediateService
    public String getCurrentUser() throws NotConnectedException {
        return frontendState.getCurrentUser();
    }

    @Override
    @ImmediateService
    public UserData getCurrentUserData() throws NotConnectedException {
        return frontendState.getCurrentUserData();
    }

    @Override
    @ImmediateService
    public Subject getSubject() throws NotConnectedException {
        return frontendState.getSubject();
    }

    @Override
    @ImmediateService
    public Map getSchedulerProperties() throws NotConnectedException {
        return frontendState.getSchedulerProperties();
    }

    @Override
    @ImmediateService
    public boolean checkJobPermissionMethod(String sessionId, String jobId, String method)
            throws NotConnectedException, UnknownJobException {
        try {
            JobId id = JobIdImpl.makeJobId(jobId);
            frontendState.checkPermissions(method,
                                           frontendState.getIdentifiedJob(id),
                                           YOU_DO_NOT_HAVE_PERMISSION_TO_DO_THIS_OPERATION);
        } catch (PermissionException p) {
            return false;
        }
        return true;
    }

    @Override
    @ImmediateService
    public boolean isFolder(String dataspace, String pathname) throws NotConnectedException, PermissionException {
        try {
            SchedulerFrontendState.UserAndCredentials userAndCredentials = frontendState.checkPermissionReturningCredentials("isFolder",
                                                                                                                             "You don't have permissions to check the file type in the DataSpace",
                                                                                                                             false);
            DataSpacesFileObject file = this.spacesSupport.resolveFile(dataspace,
                                                                       userAndCredentials.getListeningUser()
                                                                                         .getUser()
                                                                                         .getUsername(),
                                                                       userAndCredentials.getCredentials(),
                                                                       pathname);
            return file.isFolder();
        } catch (FileSystemException e) {
            logger.debug(String.format("Can't parse the directory [%s] in the user space.", pathname), e);
            return false;
        }
    }

    @Override
    @ImmediateService
    public boolean checkFileExists(String dataspace, String pathname)
            throws NotConnectedException, PermissionException {
        try {
            SchedulerFrontendState.UserAndCredentials userAndCredentials = frontendState.checkPermissionReturningCredentials("checkFileExists",
                                                                                                                             "You don't have permissions to check the file existence in the DataSpace",
                                                                                                                             false);
            DataSpacesFileObject file = this.spacesSupport.resolveFile(dataspace,
                                                                       userAndCredentials.getListeningUser()
                                                                                         .getUser()
                                                                                         .getUsername(),
                                                                       userAndCredentials.getCredentials(),
                                                                       pathname);
            return file.exists();
        } catch (FileSystemException e) {
            logger.debug(String.format("Can't parse the directory [%s] in the user space.", pathname), e);
            return false;
        }
    }

    @Override
    public void endActivity(Body body) {
        ServerJobAndTaskLogs.terminateActiveInstance();
        PAActiveObject.terminateActiveObject(authentication, true);
    }

    @Override
    @ImmediateService
    public Set<String> addJobSignal(String sessionId, String jobId, String signal) throws SignalApiException {
        try {

            Set<String> signals = (HashSet<String>) publicStore.get(SIGNAL_ORIGINATOR,
                                                                    SIGNAL_TASK_ID,
                                                                    signalsChannel,
                                                                    jobId);
            if (signals == null) {
                signals = new HashSet<>();
            }

            // Throw an error if: (i) the corresponding ready signal does not exist and (ii) the signal is not a ready signal
            String readyPrefix = SignalApiImpl.READY_PREFIX;
            if (!signals.contains(readyPrefix + signal) && !signal.startsWith(readyPrefix)) {
                throw new SignalApiException("Job " + jobId + " is not ready to receive the signal " + signal);
            }

            // Remove the existing ready signal
            signals.remove(readyPrefix + signal);

            // Add the signal
            signals.add(signal);

            publicStore.put(SIGNAL_ORIGINATOR, SIGNAL_TASK_ID, signalsChannel, jobId, (Serializable) signals);

            return (HashSet<String>) publicStore.get(SIGNAL_ORIGINATOR, SIGNAL_TASK_ID, signalsChannel, jobId);

        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not add signal for the job " + jobId, e);
        }
    }
}
