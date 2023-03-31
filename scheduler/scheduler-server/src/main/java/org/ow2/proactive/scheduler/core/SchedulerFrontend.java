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

import static org.ow2.proactive.scheduler.core.SchedulerFrontendState.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.KeyException;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.security.auth.Subject;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
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
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.permissions.*;
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
import org.ow2.proactive.scheduler.common.exception.JobValidationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.TaskCouldNotRestartException;
import org.ow2.proactive.scheduler.common.exception.TaskCouldNotStartException;
import org.ow2.proactive.scheduler.common.exception.TaskSkippedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.DefaultModelJobValidatorServiceProvider;
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
import org.ow2.proactive.scheduler.signal.Signal;
import org.ow2.proactive.scheduler.signal.SignalApiException;
import org.ow2.proactive.scheduler.signal.SignalApiImpl;
import org.ow2.proactive.scheduler.synchronization.AOSynchronization;
import org.ow2.proactive.scheduler.synchronization.InvalidChannelException;
import org.ow2.proactive.scheduler.synchronization.SynchronizationInternal;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.MultipleTimingLogger;
import org.ow2.proactive.scheduler.util.SchedulerPortalConfiguration;
import org.ow2.proactive.scheduler.util.ServerJobAndTaskLogs;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.proactive.utils.PAExecutors;
import org.ow2.proactive.utils.Tools;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;


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

    public static final String FAKE_TASK_NAME = "notask";

    public static final int FAKE_TASK_ID = -1;

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

    /**
     * Attributes used for XSLT transformation of updating job descriptor schema version
     */
    private static final String xslFilePath = "stylesheet/schemas.xsl";

    private static final String saxonFactoryClassName = "net.sf.saxon.TransformerFactoryImpl";

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
                                                                                      new NamedThreadFactory("ClientRequestsThreadPool",
                                                                                                             false,
                                                                                                             3));

            ExecutorService internalThreadPool = PAExecutors.newCachedBoundedThreadPool(1,
                                                                                        PASchedulerProperties.SCHEDULER_INTERNAL_POOL_NBTHREAD.getValueAsInt(),
                                                                                        120L,
                                                                                        TimeUnit.SECONDS,
                                                                                        new NamedThreadFactory("InternalOperationsThreadPool",
                                                                                                               false,
                                                                                                               7));

            ExecutorService taskPingerThreadPool = PAExecutors.newCachedBoundedThreadPool(1,
                                                                                          PASchedulerProperties.SCHEDULER_TASK_PINGER_POOL_NBTHREAD.getValueAsInt(),
                                                                                          120L,
                                                                                          TimeUnit.SECONDS,
                                                                                          new NamedThreadFactory("TaskPingerThreadPool",
                                                                                                                 false,
                                                                                                                 2));

            ScheduledExecutorService scheduledThreadPool = new ScheduledThreadPoolExecutor(PASchedulerProperties.SCHEDULER_SCHEDULED_POOL_NBTHREAD.getValueAsInt(),
                                                                                           new NamedThreadFactory("SchedulingServiceTimerThread",
                                                                                                                  false,
                                                                                                                  2));

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

    @VisibleForTesting
    public Method findMethod(String methodName) throws PermissionException {
        for (Method method : this.getClass().getMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        throw new PermissionException("Method " + methodName + " does not exist in " + this.getClass().getName());
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
    @RoleBasic
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
    @RoleWrite
    public JobId reSubmit(JobId currentJobId, Map<String, String> jobVariables, Map<String, String> jobGenericInfos,
            String sessionId) throws NotConnectedException, UnknownJobException, PermissionException,
            JobCreationException, SubmissionClosedException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_SUBMIT_A_JOB);
        final String jobContent = getJobContent(currentJobId);
        final Job job = JobFactory.getFactory().createJob(IOUtils.toInputStream(jobContent, Charset.forName("UTF-8")),
                                                          jobVariables,
                                                          jobGenericInfos,
                                                          this,
                                                          this,
                                                          sessionId);
        return submit(job);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public JobId submit(Job userJob)
            throws NotConnectedException, PermissionException, SubmissionClosedException, JobCreationException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("New job submission requested : " + userJob.getName());
            }

            // check if the scheduler is stopped
            if (!schedulingService.isSubmitPossible()) {
                String msg = "Scheduler is stopped, cannot submit job";
                logger.info(msg);
                throw new SubmissionClosedException(msg);
            }
            Method currentMethod = new Object() {
            }.getClass().getEnclosingMethod();
            UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                         YOU_DO_NOT_HAVE_PERMISSION_TO_SUBMIT_A_JOB);
            MultipleTimingLogger timingLogger = new MultipleTimingLogger("SubmitTimer", logger, true);
            timingLogger.start("createJob");
            InternalJob job = frontendState.createJob(userJob, ident);
            timingLogger.end("createJob");
            timingLogger.start("submitJob");
            schedulingService.submitJob(job, frontendState, ident, timingLogger);
            timingLogger.end("submitJob");
            return job.getId();
        } catch (Exception e) {
            logger.warn("Error when submitting job.", e);
            throw e;
        }
    }

    @Override
    @ImmediateService
    @RoleWrite
    public List<JobIdDataAndError> submit(List<Job> jobs) throws NotConnectedException {

        List<JobIdDataAndError> answer = new ArrayList<>(jobs.size());
        // check if the scheduler is stopped
        if (!schedulingService.isSubmitPossible()) {
            String msg = "Scheduler is stopped, cannot submit job";
            logger.info(msg);
            SubmissionClosedException exp = new SubmissionClosedException(msg);
            for (int i = 0; i < jobs.size(); i++) {
                answer.add(new JobIdDataAndError(msg, StackTraceUtil.getStackTrace(exp)));
            }
            return answer;
        }

        UserIdentificationImpl ident = null;
        try {
            Method currentMethod = new Object() {
            }.getClass().getEnclosingMethod();
            ident = frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_SUBMIT_A_JOB);
        } catch (Exception e) {
            for (int i = 0; i < jobs.size(); i++) {
                answer.add(new JobIdDataAndError(e.getMessage(), StackTraceUtil.getStackTrace(e)));
            }
            return answer;
        }

        MultipleTimingLogger timingLogger = new MultipleTimingLogger("SubmitTimer", logger, true);

        timingLogger.start("createJobs");

        List<InternalJob> internalJobs = new ArrayList<>(jobs.size());

        for (Job userJob : jobs) {
            if (logger.isDebugEnabled()) {
                logger.debug("New job submission requested : " + userJob.getName());
            }
            InternalJob job = null;
            try {
                if (userJob != null) {
                    timingLogger.start("createJob");
                    job = frontendState.createJob(userJob, ident);
                    timingLogger.end("createJob");
                    internalJobs.add(job);
                } else {
                    internalJobs.add(null);
                }
                answer.add(null);

            } catch (Exception e) {
                answer.add(new JobIdDataAndError(e.getMessage(), StackTraceUtil.getStackTrace(e)));
                internalJobs.add(null);
            }
        }
        timingLogger.end("createJobs");
        timingLogger.start("submitJobs");
        schedulingService.submitJobs(internalJobs, frontendState, ident, timingLogger);

        for (int i = 0; i < jobs.size(); i++) {
            InternalJob internalJob = internalJobs.get(i);
            if (internalJob != null) {
                JobId jobId = internalJob.getId();
                answer.set(i, new JobIdDataAndError(jobId.longValue(), jobId.getReadableName()));
            }
        }
        timingLogger.end("submitJobs");
        timingLogger.printTimings(Level.DEBUG);
        return answer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public List<String> getUserSpaceURIs() throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        SchedulerFrontendState.UserAndCredentials userAndCredentials = frontendState.checkPermissionReturningCredentials(currentMethod,
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
    @RoleRead
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, "You don't have permissions to read the GLOBAL Space URI");
        return this.spacesSupport.getGlobalSpaceURIs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public JobResult getJobResult(final JobId jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {

        // checking permissions
        IdentifiedJob ij = frontendState.getIdentifiedJob(jobId);

        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();

        frontendState.checkPermissions(currentMethod, ij, YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_RESULT_OF_THIS_JOB);

        if (!ij.isFinished()) {
            logger.info("Job " + jobId + " is not finished");
            logger.info("Job " + jobId + " state: " + getJobState(jobId).getStatus());
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
    @RoleRead
    public JobResult getJobResult(String jobId) throws NotConnectedException, PermissionException, UnknownJobException {
        return this.getJobResult(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public TaskResult getTaskResult(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResultFromIncarnation(jobId, taskName, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public TaskResult getTaskResult(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResult(JobIdImpl.makeJobId(jobId), taskName);
    }

    @Override
    @ImmediateService
    @RoleRead
    public List<TaskResult> getTaskResultsByTag(JobId jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB);
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
    @RoleRead
    public List<TaskResult> getTaskResultsByTag(String jobId, String taskTag)
            throws NotConnectedException, UnknownJobException, PermissionException {
        return this.getTaskResultsByTag(JobIdImpl.makeJobId(jobId), taskTag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public TaskResult getTaskResultFromIncarnation(String jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResultFromIncarnation(JobIdImpl.makeJobId(jobId), taskName, inc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public TaskResult getTaskResultFromIncarnation(JobId jobId, String taskName, int inc)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {

        // checking permissions
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleRead
    public List<TaskResult> getTaskResultAllIncarnations(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return this.getTaskResultAllIncarnations(JobIdImpl.makeJobId(jobId), taskName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public List<TaskResult> getTaskResultAllIncarnations(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean killTask(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        // checking permissions
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean killTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return killTask(JobIdImpl.makeJobId(jobId), taskName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean restartTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean restartTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return restartTask(JobIdImpl.makeJobId(jobId), taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean finishInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean restartInErrorTask(String jobId, String taskName)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_RESTART_THIS_TASK);
        logger.info("Request to restart in-error task " + taskName + " of job " + jobId + " received from " +
                    currentUser);
        return schedulingService.restartInErrorTask(jobIdObject, taskName);
    }

    @Override
    @ImmediateService
    @RoleWrite
    public void enableRemoteVisualization(String jobId, String taskName, String connectionString)
            throws NotConnectedException, PermissionException, UnknownJobException, UnknownTaskException {
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_ENABLE_VISE_THIS_TASK);
        logger.info("Request to enable visualization on task " + taskName + " of job " + jobId + " received from " +
                    currentUser);
        schedulingService.enableRemoteVisualization(jobIdObject, taskName, connectionString);
    }

    @Override
    @ImmediateService
    @RoleWrite
    public void registerService(String jobId, int serviceInstanceid, boolean enableActions)
            throws NotConnectedException, PermissionException, UnknownJobException {
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_REGISTER_SERVICE_TO_THIS_JOB);
        logger.info("Request to register service instance " + serviceInstanceid + " on job " + jobId +
                    " received from " + currentUser);
        schedulingService.registerService(jobIdObject, serviceInstanceid, enableActions);
    }

    @Override
    @ImmediateService
    @RoleWrite
    public void detachService(String jobId, int serviceInstanceid)
            throws NotConnectedException, PermissionException, UnknownJobException {
        String currentUser = frontendState.getCurrentUser();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_DETACH_SERVICE_TO_THIS_JOB);
        logger.info("Request to detach service instance " + serviceInstanceid + " on job " + jobId + " received from " +
                    currentUser);
        schedulingService.detachService(jobIdObject, serviceInstanceid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        // checking permissions
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean preemptTask(String jobId, String taskName, int restartDelay)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        return preemptTask(JobIdImpl.makeJobId(jobId), taskName, restartDelay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean removeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {

        String currentUser = frontendState.getCurrentUser();
        // checking permissions
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIS_JOB);
        logger.info("Request to remove job " + jobId + " received from " + currentUser);

        // asking the scheduler for the result
        return schedulingService.removeJob(jobId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean removeJobs(List<JobId> jobIds) throws NotConnectedException, PermissionException {
        if (jobIds.isEmpty()) {
            return false;
        }
        List<JobId> jobIdsDup = new ArrayList<>(jobIds);
        // checking permission for each of the job
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        for (Iterator<JobId> it = jobIdsDup.iterator(); it.hasNext();) {
            JobId jobId = it.next();
            try {
                frontendState.checkPermissions(currentMethod,
                                               frontendState.getIdentifiedJob(jobId),
                                               YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIS_JOB);
            } catch (UnknownJobException e) {
                logger.warn("Cannot remove job " + jobId, e);
                it.remove();
            } catch (PermissionException e) {
                logger.warn("Cannot remove job " + jobId, e);
                it.remove();
            }
        }

        return jobIdsDup.size() == jobIds.size() && schedulingService.removeJobs(jobIds);
    }

    @Override
    @ImmediateService
    @RoleWrite
    public boolean removeJobs(long olderThan) throws NotConnectedException, PermissionException {
        List<JobId> jobsId = dbManager.getJobsByFinishedTime(olderThan);
        return removeJobs(jobsId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException {
        // checking permissions
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleRead
    public void listenJobLogs(String jobId, AppenderProvider appenderProvider)
            throws NotConnectedException, UnknownJobException, PermissionException {
        this.listenJobLogs(JobIdImpl.makeJobId(jobId), appenderProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public SchedulerStatus getStatus() throws NotConnectedException, PermissionException {
        // checking permissions
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATUS);
        return frontendState.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        return getState(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public SchedulerState getState(boolean myJobsOnly) throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        ListeningUser ui = frontendState.checkPermissionReturningListeningUser(currentMethod,
                                                                               YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE);
        return frontendState.getState(myJobsOnly, ui);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public void addEventListener(SchedulerEventListener sel, boolean myEventsOnly, SchedulerEvent... events)
            throws NotConnectedException, PermissionException {
        addEventListener(sel, myEventsOnly, false, events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public SchedulerState addEventListener(SchedulerEventListener sel, boolean myEventsOnly, boolean getCurrentState,
            SchedulerEvent... events) throws NotConnectedException, PermissionException {
        ListeningUser uIdent = frontendState.checkPermissionReturningListeningUser(new Object() {
        }.getClass().getEnclosingMethod(), YOU_DO_NOT_HAVE_PERMISSION_TO_ADD_A_LISTENER);
        return frontendState.addEventListener(sel, myEventsOnly, getCurrentState, uIdent, events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
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
    @RoleAdmin
    public boolean start() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_START_THE_SCHEDULER);
        logger.info("Request to start scheduler server received from " + currentUser);
        return schedulingService.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RoleAdmin
    public boolean stop() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_STOP_THE_SCHEDULER);
        logger.info("Request to stop scheduler server received from " + currentUser);
        return schedulingService.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RoleAdmin
    public boolean pause() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_PAUSE_THE_SCHEDULER);
        logger.info("Request to pause scheduler server received from " + currentUser);
        return schedulingService.pause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RoleAdmin
    public boolean freeze() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_FREEZE_THE_SCHEDULER);
        logger.info("Request to freeze scheduler server received from " + currentUser);
        return schedulingService.freeze();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RoleAdmin
    public boolean resume() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_RESUME_THE_SCHEDULER);
        logger.info("Request to resume scheduler server received from " + currentUser);
        return schedulingService.resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RoleAdmin
    public boolean shutdown() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_SHUTDOWN_THE_SCHEDULER);
        logger.info("Request to shutdown scheduler server received from " + currentUser);
        return schedulingService.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RoleAdmin
    public boolean kill() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_KILL_THE_SCHEDULER);
        logger.info("Request to kill scheduler server received from " + currentUser);
        return schedulingService.kill();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleBasic
    public void disconnect() throws NotConnectedException, PermissionException {
        frontendState.disconnect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleBasic
    public boolean isConnected() {
        return frontendState.isConnected();
    }

    @Override
    @RoleRead
    public String getCurrentPolicy() throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_READ_THE_CURRENT_POLICY);
        return policyFullName;
    }

    @Override
    @ImmediateService
    @RoleAdmin
    public Map<JobId, JobDescriptor> getJobsToSchedule() throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_READ_THE_JOBS_TO_SCHEDULE);
        Map<JobId, JobDescriptor> jobMap = schedulingService.lockJobsToSchedule();
        schedulingService.unlockJobsToSchedule(jobMap.values());
        return jobMap;
    }

    @Override
    @ImmediateService
    @RoleAdmin
    public List<TaskDescriptor> getTasksToSchedule() throws NotConnectedException, PermissionException {
        Policy policy = null;
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_READ_THE_TASKS_TO_SCHEDULE);
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
    @RoleBasic
    public void renewSession() throws NotConnectedException {
        frontendState.renewSession(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean pauseJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean resumeJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean killJob(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public void changeJobPriority(JobId jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_CHANGE_THE_PRIORITY_OF_THIS_JOB);
        frontendState.checkChangeJobPriority(jobId, priority);
        logger.info("Request to change job " + jobId + " priority to " + priority + " received from " + currentUser);
        schedulingService.changeJobPriority(jobId, priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public JobState getJobState(JobId jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB);
        return frontendState.getJobState(jobId);
    }

    @Override
    @RoleRead
    public TaskState getTaskState(JobId jobId, String taskName)
            throws NotConnectedException, UnknownJobException, PermissionException, UnknownTaskException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_TASK);
        return frontendState.getTaskState(jobId, taskName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public TaskStatesPage getTaskPaginated(String jobId, int offset, int limit)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(JobIdImpl.makeJobId(jobId)),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB);
        return frontendState.getTaskPaginated(JobIdImpl.makeJobId(jobId), offset, limit);
    }

    @Override
    @ImmediateService
    @RoleRead
    public TaskStatesPage getTaskPaginated(String jobId, String statusFilter, int offset, int limit)
            throws NotConnectedException, UnknownJobException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(JobIdImpl.makeJobId(jobId)),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_STATE_OF_THIS_JOB);
        return frontendState.getTaskPaginated(JobIdImpl.makeJobId(jobId), statusFilter, offset, limit);
    }

    @Override
    @ImmediateService
    @RoleRead
    public List<TaskResult> getPreciousTaskResults(String jobId)
            throws NotConnectedException, PermissionException, UnknownJobException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(JobIdImpl.makeJobId(jobId)),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB);
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
    @RoleRead
    public Map<Long, Map<String, Serializable>> getJobResultMaps(List<String> jobsId)
            throws UnknownJobException, NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        for (String jobId : jobsId) {
            frontendState.checkPermissions(currentMethod,
                                           frontendState.getIdentifiedJob(JobIdImpl.makeJobId(jobId)),
                                           YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THE_TASK_RESULT_OF_THIS_JOB);
        }
        return dbManager.getJobResultMaps(jobsId);
    }

    @Override
    @ImmediateService
    @RoleRead
    public Map<Long, List<String>> getPreciousTaskNames(List<String> jobsId)
            throws NotConnectedException, PermissionException {
        // permission check is skipped for efficiency reason, security is not mandatory on this method
        return dbManager.getPreciousTaskNames(jobsId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean killJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.killJob(JobIdImpl.makeJobId(jobId));
    }

    @Override
    @ImmediateService
    @RoleWrite
    public boolean killJobs(List<String> jobIds) throws NotConnectedException, PermissionException {

        if (jobIds.isEmpty()) {
            return false;
        }
        String currentUser = frontendState.getCurrentUser();
        List<JobId> jobIdsConverted = jobIds.stream().map(JobIdImpl::makeJobId).collect(Collectors.toList());
        // checking permission for each of the job
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        for (JobId jobId : jobIdsConverted) {
            try {
                frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean pauseJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.pauseJob(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean removeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.removeJob(JobIdImpl.makeJobId(jobId));
    }

    @Override
    @ImmediateService
    @RoleWrite
    public boolean restartAllInErrorTasks(String jobId)
            throws NotConnectedException, UnknownJobException, PermissionException {
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleWrite
    public boolean resumeJob(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.resumeJob(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public void changeJobPriority(String jobId, JobPriority priority)
            throws NotConnectedException, UnknownJobException, PermissionException, JobAlreadyFinishedException {
        this.changeJobPriority(JobIdImpl.makeJobId(jobId), priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public JobState getJobState(String jobId) throws NotConnectedException, UnknownJobException, PermissionException {
        return this.getJobState(JobIdImpl.makeJobId(jobId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RoleAdmin
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
    @RoleAdmin
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
    @RoleAdmin
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, YOU_DO_NOT_HAVE_PERMISSION_TO_RELOAD_POLICY_CONFIGURATION);
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
    @RoleRead
    public String getJobServerLogs(String jobId)
            throws UnknownJobException, NotConnectedException, PermissionException {
        JobId id = JobIdImpl.makeJobId(jobId);
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(id),
                                       YOU_DO_NOT_HAVE_PERMISSIONS_TO_GET_THE_LOGS_OF_THIS_JOB);

        return ServerJobAndTaskLogs.getInstance().getJobLog(JobIdImpl.makeJobId(jobId), frontendState.getJobTasks(id));
    }

    @Override
    @ImmediateService
    @RoleRead
    public String getTaskServerLogs(String jobId, String taskName)
            throws UnknownJobException, UnknownTaskException, NotConnectedException, PermissionException {

        JobId id = JobIdImpl.makeJobId(jobId);
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleRead
    public String getTaskServerLogsByTag(String jobId, String taskTag)
            throws UnknownJobException, NotConnectedException, PermissionException {
        JobId id = JobIdImpl.makeJobId(jobId);
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
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
    @RoleRead
    public Page<JobInfo> getJobs(int offset, int limit, JobFilterCriteria filterCriteria,
            List<SortParameter<JobSortParameter>> sortParameters) throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to load jobs");

        boolean myJobsOnly = filterCriteria.isMyJobsOnly();

        String user = filterCriteria.getUserName();
        String tenant = filterCriteria.getTenant();
        if (myJobsOnly) {
            user = ident.getUsername();
        }
        boolean isExplicitTenantFilter = !Strings.isNullOrEmpty(tenant);
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }

        Page<JobInfo> jobsInfo = dbManager.getJobs(offset,
                                                   limit,
                                                   user,
                                                   tenant,
                                                   isExplicitTenantFilter,
                                                   filterCriteria.isPending(),
                                                   filterCriteria.isRunning(),
                                                   filterCriteria.isFinished(),
                                                   filterCriteria.isWithIssuesOnly(),
                                                   filterCriteria.isChildJobs(),
                                                   filterCriteria.getJobName(),
                                                   filterCriteria.getProjectName(),
                                                   filterCriteria.getBucketName(),
                                                   filterCriteria.getParentId(),
                                                   filterCriteria.getSubmissionMode(),
                                                   sortParameters);
        /**
         * Add/inject to each JobInfo the list of signals used by the job, if they exist.
         */
        insertSignals(jobsInfo.getList());

        /**
         * Inject visualization connection strings for running job when available
         */
        insertVisualization(jobsInfo.getList());

        /**
         * Remove variables and generic info when the user is not allowed to see these details
         */
        filterVariablesGenericInfoAndEndpoints(jobsInfo.getList());

        return jobsInfo;
    }

    private void insertVisualization(List<JobInfo> jobsInfo) {
        jobsInfo.stream()
                .filter(jobInfo -> jobInfo.isStarted() && jobInfo.getFinishedTime() <= 0)
                .forEach(jobInfo -> insertVisualization(jobInfo));
    }

    private void insertSignals(List<JobInfo> jobsInfo) {
        jobsInfo.stream().filter(jobInfo -> {
            try {
                TaskId taskId = createFakeTaskId(jobInfo.getJobId().value());
                return publicStore.keySet(SIGNAL_ORIGINATOR,
                                          taskId,
                                          signalsChannel + jobInfo.getJobId().value()) != null;
            } catch (Exception e) {
            }
            return false;
        }).forEach(jobInfo -> insertJobSignals(jobInfo));
    }

    private TaskId createFakeTaskId(String jobId) {
        JobId jobIdObj = JobIdImpl.makeJobId(jobId);
        return TaskIdImpl.createTaskId(jobIdObj, FAKE_TASK_NAME, FAKE_TASK_ID);
    }

    private void filterVariablesGenericInfoAndEndpoints(List<JobInfo> jobsInfo) {
        jobsInfo.stream().forEach(jobInfo -> filterVariablesGenericInfoAndEndpoints(jobInfo));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public List<JobInfo> getJobsInfoList(List<String> jobsId) throws PermissionException, NotConnectedException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, "You don't have permissions to load jobs");
        List<JobInfo> jobsInfo = dbManager.getJobs(jobsId);
        /**
         * Add/inject to each JobInfo the list of signals used by the job, if they exist.
         */
        insertSignals(jobsInfo);

        /**
         * Inject visualization connection strings for running job when available
         */
        insertVisualization(jobsInfo);

        /**
         * Remove variables and generic info when the user is not allowed to see these details
         */
        filterVariablesGenericInfoAndEndpoints(jobsInfo);
        return jobsInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public List<SchedulerUserInfo> getUsers() throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, "You don't have permissions to get users");
        return frontendState.getUsers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleAdmin
    public List<SchedulerUserInfo> getUsersWithJobs() throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, "You don't have permissions to get users with jobs");
        return dbManager.loadUsersWithJobs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleRead
    public List<JobUsage> getMyAccountUsage(Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get usage data for your account");
        return dbManager.getUsage(ident.getUsername(), startDate, endDate);
    }

    @Override
    @ImmediateService
    @RoleRead
    public FilteredStatistics getFilteredStatistics(String workflowName, String bucketName, Boolean myJobs,
            long startDate, long endDate) throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get filtered statistics");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        return dbManager.getFilteredStatistics(workflowName,
                                               bucketName,
                                               myJobs ? ident.getUsername() : null,
                                               tenant,
                                               startDate,
                                               endDate);
    }

    @Override
    @ImmediateService
    @RoleRead
    public List<FilteredTopWorkflow> getTopWorkflowsWithIssues(int numberOfWorkflows, String workflowName,
            String bucketName, Boolean myJobs, long startDate, long endDate)
            throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get top workflows with issues");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        return dbManager.getTopWorkflowsWithIssues(numberOfWorkflows,
                                                   workflowName,
                                                   bucketName,
                                                   myJobs ? ident.getUsername() : null,
                                                   tenant,
                                                   startDate,
                                                   endDate);
    }

    @Override
    @ImmediateService
    @RoleRead
    public List<FilteredTopWorkflowsCumulatedCoreTime> getTopWorkflowsCumulatedCoreTime(int numberOfWorkflows,
            String workflowName, String bucketName, Boolean myJobs, long startDate, long endDate)
            throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get top workflows consuming the most CPU");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        return dbManager.getTopWorkflowsMostConsumingNodes(numberOfWorkflows,
                                                           workflowName,
                                                           bucketName,
                                                           myJobs ? ident.getUsername() : null,
                                                           tenant,
                                                           startDate,
                                                           endDate);
    }

    @Override
    @ImmediateService
    @RoleRead
    public List<FilteredTopWorkflowsNumberOfNodes> getTopWorkflowsNumberOfNodes(int numberOfWorkflows,
            String workflowName, String bucketName, boolean myJobs, long startDate, long endDate, boolean inParallel)
            throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get top workflows that use the most nodes");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        return dbManager.getTopWorkflowsNumberOfNodes(numberOfWorkflows,
                                                      workflowName,
                                                      bucketName,
                                                      myJobs ? ident.getUsername() : null,
                                                      tenant,
                                                      startDate,
                                                      endDate,
                                                      inParallel);
    }

    @Override
    @ImmediateService
    @RoleRead
    public List<WorkflowDuration> getTopExecutionTimeWorkflows(int numberOfWorkflows, String workflowName,
            String bucketName, Boolean myJobs, long startDate, long endDate)
            throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get top execution time workflows");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        return dbManager.getTopExecutionTimeWorkflows(numberOfWorkflows,
                                                      workflowName,
                                                      bucketName,
                                                      myJobs ? ident.getUsername() : null,
                                                      tenant,
                                                      startDate,
                                                      endDate);
    }

    @Override
    @ImmediateService
    @RoleRead
    public List<WorkflowDuration> getTopPendingTimeWorkflows(int numberOfWorkflows, String workflowName,
            String bucketName, Boolean myJobs, long startDate, long endDate)
            throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get top pending time workflows");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        return dbManager.getTopPendingTimeWorkflows(numberOfWorkflows,
                                                    workflowName,
                                                    bucketName,
                                                    myJobs ? ident.getUsername() : null,
                                                    tenant,
                                                    startDate,
                                                    endDate);
    }

    @Override
    @ImmediateService
    @RoleRead
    public Map<String, Integer> getSubmissionModeCount(String workflowName, String bucketName, Boolean myJobs,
            long startDate, long endDate) throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get submission from count");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        return dbManager.getNumberOfJobsSubmittedFromEachPortals(workflowName,
                                                                 bucketName,
                                                                 myJobs ? ident.getUsername() : null,
                                                                 tenant,
                                                                 startDate,
                                                                 endDate);
    }

    @Override
    @ImmediateService
    @RoleRead
    public CompletedJobsCount getCompletedJobs(Boolean myJobs, String workflowName, String bucketName, long startDate,
            long endDate, int numberOfIntervals) throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get completed jobs");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        if (endDate == -1) {
            endDate = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return dbManager.getCompletedJobs(myJobs ? ident.getUsername() : null,
                                          tenant,
                                          workflowName,
                                          bucketName,
                                          startDate,
                                          endDate,
                                          numberOfIntervals);
    }

    @Override
    @ImmediateService
    @RoleRead
    public CompletedTasksCount getCompletedTasks(Boolean myTasks, String taskName, long startDate, long endDate,
            int numberOfIntervals) throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     "You don't have permissions to get completed tasks");
        String tenant = null;
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !ident.isAllTenantPermission()) {
            // overwrite tenant filter if the user only has access to his own tenant
            tenant = ident.getTenant();
        }
        if (endDate == -1) {
            endDate = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return dbManager.getCompletedTasks(myTasks ? ident.getUsername() : null,
                                           tenant,
                                           taskName,
                                           startDate,
                                           endDate,
                                           numberOfIntervals);
    }

    @Override
    @ImmediateService
    @RoleRead
    public Set<String> getSubmissionModeValues() throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermission(currentMethod, "You don't have permissions to get submission mode values");
        return dbManager.getSubmissionModeValues();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleAdmin
    public List<JobUsage> getAccountUsage(String user, Date startDate, Date endDate)
            throws NotConnectedException, PermissionException {
        try {
            Method currentMethod = new Object() {
            }.getClass().getEnclosingMethod();
            frontendState.checkPermission(currentMethod, "You don't have permissions to get usage data of " + user);

            return dbManager.getUsage(user, startDate, endDate);
        } catch (PermissionException e) {
            // try to fallback on my account usage if user is the caller
            UserIdentificationImpl ident = frontendState.checkPermission(findMethod("getMyAccountUsage"),
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
    @RoleWrite
    public void putThirdPartyCredential(String key, String value) throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
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
    @RoleRead
    public Set<String> thirdPartyCredentialsKeySet() throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     YOU_DO_NOT_HAVE_PERMISSION_TO_LIST_THIRD_PARTY_CREDENTIALS_IN_THE_SCHEDULER);
        return dbManager.thirdPartyCredentialsKeySet(ident.getUsername());
    }

    @Override
    @ImmediateService
    @RoleWrite
    public void removeThirdPartyCredential(String key) throws NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl ident = frontendState.checkPermission(currentMethod,
                                                                     YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_THIRD_PARTY_CREDENTIALS_FROM_THE_SCHEDULER);
        dbManager.removeThirdPartyCredential(ident.getUsername(), key);
    }

    @Override
    @ImmediateService
    @RoleRead
    public Page<TaskId> getTaskIds(String taskTag, long from, long to, boolean mytasks, Set<TaskStatus> taskStatuses,
            int offset, int limit) throws NotConnectedException, PermissionException {
        String userName = null;
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl userIdentification = frontendState.checkPermission(currentMethod,
                                                                                  YOU_DO_NOT_HAVE_PERMISSION_TO_GET_TASK_IDS);
        String tmpUserName = userIdentification.getUsername();
        String tenant = null;
        if (mytasks) {
            userName = tmpUserName;
        }
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() &&
            !userIdentification.isAllTenantPermission()) {
            tenant = userIdentification.getTenant();
        }
        Page<TaskInfo> pTaskInfo = dbManager.getTasks(from, to, taskTag, offset, limit, userName, tenant, taskStatuses);
        List<TaskId> lTaskId = new ArrayList<>(pTaskInfo.getList().size());
        for (TaskInfo taskInfo : pTaskInfo.getList()) {
            if (checkJobPermissionMethod(taskInfo.getJobId().value(), "getTaskIds")) {
                lTaskId.add(taskInfo.getTaskId());
            }
        }
        return new Page<>(lTaskId, pTaskInfo.getSize());
    }

    @Override
    @ImmediateService
    @RoleRead
    public Page<TaskState> getTaskStates(String taskTag, long from, long to, boolean mytasks,
            Set<TaskStatus> statusFilter, int offset, int limit, SortSpecifierContainer sortParams)
            throws NotConnectedException, PermissionException {

        String userName = null;
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        UserIdentificationImpl userIdentification = frontendState.checkPermission(currentMethod,
                                                                                  YOU_DO_NOT_HAVE_PERMISSION_TO_GET_TASK_STATES);
        String tmpUserName = userIdentification.getUsername();
        String tenant = null;
        if (mytasks) {
            userName = tmpUserName;
        }
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() &&
            !userIdentification.isAllTenantPermission()) {
            tenant = userIdentification.getTenant();
        }
        Page<TaskState> page = dbManager.getTaskStates(from,
                                                       to,
                                                       taskTag,
                                                       offset,
                                                       limit,
                                                       userName,
                                                       tenant,
                                                       statusFilter,
                                                       sortParams);
        for (Iterator<TaskState> it = page.getList().iterator(); it.hasNext();) {
            TaskState taskState = it.next();
            if (!checkJobPermissionMethod(taskState.getId().getJobId().value(), "getTaskStates")) {
                it.remove();
            }
        }
        return page;
    }

    @Override
    @ImmediateService
    @RoleRead
    public JobInfo getJobInfo(String jobId) throws UnknownJobException, NotConnectedException, PermissionException {
        // authorization is performed in the following call
        List<JobInfo> jobInfoList = getJobsInfoList(ImmutableList.of(jobId));
        if (jobInfoList == null || jobInfoList.size() != 1) {
            throw new UnknownJobException(JobIdImpl.makeJobId(jobId));
        }
        JobInfo jobInfo = jobInfoList.get(0);
        insertJobSignals(jobInfo);
        insertVisualization(jobInfo);
        filterVariablesGenericInfoAndEndpoints(jobInfo);
        return jobInfo;
    }

    private JobInfo insertVisualization(JobInfo jobInfo) {
        String jobid = jobInfo.getJobId().value();

        if (checkJobPermissionMethod(jobid, "enableRemoteVisualization")) {
            try {
                JobInfo jobInfoState = frontendState.getJobInfo(jobInfo.getJobId());
                jobInfo.setVisualizationConnectionStrings(jobInfoState.getVisualizationConnectionStrings());
                jobInfo.setVisualizationIcons(jobInfoState.getVisualizationIcons());
            } catch (UnknownJobException e) {
                logger.warn("Could not add visualization info for job " + jobInfo.getJobId(), e);
            }
        }
        return jobInfo;
    }

    private JobInfo insertJobSignals(JobInfo jobInfo) {

        String jobid = jobInfo.getJobId().value();

        if (checkJobPermissionMethod(jobid, "addJobSignal")) {

            try {
                TaskId taskId = createFakeTaskId(jobInfo.getJobId().value());
                if (publicStore.channelExists(SIGNAL_ORIGINATOR, taskId, signalsChannel + jobid)) {

                    Set<Map.Entry<String, Serializable>> signalEntries = publicStore.entrySet(SIGNAL_ORIGINATOR,
                                                                                              taskId,
                                                                                              signalsChannel + jobid);
                    Set<String> signalsToBeAdded = signalEntries.stream()
                                                                .map(entry -> entry.getKey())
                                                                .collect(Collectors.toSet());
                    List<Signal> signalList = signalEntries.stream()
                                                           .map(entry -> (Signal) entry.getValue())
                                                           .collect(Collectors.toList());

                    Map<String, Map<String, JobVariable>> detailedSignalsToBeAdded = new LinkedHashMap<>();
                    signalList.forEach(signal -> {
                        Map<String, JobVariable> variableMap = new LinkedHashMap<>();
                        if (signal.getInputVariables() != null) {
                            signal.getInputVariables()
                                  .forEach(jobVariable -> variableMap.put(jobVariable.getName(), jobVariable));
                        }
                        detailedSignalsToBeAdded.put(signal.getName(), variableMap);
                    });

                    Set<String> jobSignals = jobInfo.getSignals();
                    Map<String, Map<String, JobVariable>> jobDetailedSignals = jobInfo.getDetailedSignals();

                    if (signalsToBeAdded != null && !signalsToBeAdded.isEmpty()) {
                        jobSignals.addAll(signalsToBeAdded);
                        jobInfo.setSignals(jobSignals);
                    }

                    if (detailedSignalsToBeAdded != null && !detailedSignalsToBeAdded.isEmpty()) {
                        jobDetailedSignals.putAll(detailedSignalsToBeAdded);
                        jobInfo.setDetailedSignals(jobDetailedSignals);
                    }
                }
            } catch (InvalidChannelException e) {
                logger.warn("Could not retrieve the signals of the job " + jobid);
            }
        }
        return jobInfo;
    }

    private void filterVariablesGenericInfoAndEndpoints(JobInfo jobInfo) {
        String jobid = jobInfo.getJobId().value();
        if (!checkJobPermissionMethod(jobid, "getJobState")) {
            if (jobInfo.getVariables() != null) {
                jobInfo.getVariables().clear();
            }
            if (jobInfo.getGenericInformation() != null) {
                jobInfo.getGenericInformation().clear();
            }
            if (jobInfo.getAttachedServices() != null) {
                jobInfo.getAttachedServices().clear();
            }
            if (jobInfo.getExternalEndpointUrls() != null) {
                jobInfo.getExternalEndpointUrls().clear();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    @RoleWrite
    public boolean changeStartAt(JobId jobId, String startAt)
            throws UnknownJobException, NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_CHANGE_THE_START_AT_VALUE_OF_THIS_JOB);
        return schedulingService.changeStartAt(jobId, startAt);
    }

    @Override
    @ImmediateService
    @RoleRead
    public String getJobContent(JobId jobId) throws UnknownJobException, NotConnectedException, PermissionException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobId),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_GET_THIS_JOB);
        return updateJobSchemaVersionToLatest(dbManager.loadInitalJobContent(jobId));
    }

    /**
     * Use XSLT to change the schema version of the job descriptor to the latest.
     * Specifically, it's to change "xmlns" and "xsi:schemaLocation" to the "dev" version.
     * This function is used to fix the potential problem of schema version mismatch between global variables and job descriptor.
     * Since the schema change is backward compatible, updating the job to the latest version will fix the version mismatch problem.
     *
      * @param jobContent the String content of job descriptor (in xml)
     * @return the updated String content of job descriptor (in xml) which is changed to the latest version
     */
    private String updateJobSchemaVersionToLatest(String jobContent) {
        try {
            StreamSource xslSource = new StreamSource(this.getClass()
                                                          .getClassLoader()
                                                          .getResourceAsStream(xslFilePath));

            StreamSource xmlInput = new StreamSource(new StringReader(jobContent));
            StringWriter xmlOutput = new StringWriter();
            Result result = new StreamResult(xmlOutput);

            TransformerFactory factory = TransformerFactory.newInstance(saxonFactoryClassName, null);
            Transformer transformer = factory.newTransformer(xslSource);
            transformer.transform(xmlInput, result);

            return xmlOutput.toString();
        } catch (Exception e) {
            logger.warn("Error during transforming the job descriptor schema to the latest version, it's kept unchanged.",
                        e);
            return jobContent;
        }
    }

    @Override
    @ImmediateService
    @RoleRead
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
    @RoleRead
    public Map getSchedulerProperties() throws NotConnectedException {
        return frontendState.getSchedulerProperties();
    }

    @Override
    @ImmediateService
    @RoleBasic
    public boolean checkPermission(String method) throws SecurityException {
        try {
            frontendState.checkPermission(findMethod(method), YOU_DO_NOT_HAVE_PERMISSION_TO_DO_THIS_OPERATION);
        } catch (Exception e) {
            throw new SecurityException(e.getMessage(), e);
        }

        return true;
    }

    @Override
    @ImmediateService
    @RoleBasic
    public boolean checkJobPermissionMethod(String jobId, String method) {
        try {
            JobId id = JobIdImpl.makeJobId(jobId);
            frontendState.checkPermissions(findMethod(method),
                                           frontendState.getIdentifiedJob(id),
                                           YOU_DO_NOT_HAVE_PERMISSION_TO_DO_THIS_OPERATION);
        } catch (Exception p) {
            return false;
        }
        return true;
    }

    @Override
    @ImmediateService
    @RoleBasic
    public List<String> checkJobsPermissionMethod(List<String> jobIds, String method) {
        List<String> answer = new ArrayList<>();
        if (jobIds != null) {
            for (String jobId : jobIds) {
                try {
                    JobId id = JobIdImpl.makeJobId(jobId);
                    frontendState.checkPermissions(findMethod(method),
                                                   frontendState.getIdentifiedJob(id),
                                                   YOU_DO_NOT_HAVE_PERMISSION_TO_DO_THIS_OPERATION);
                    answer.add(jobId);
                } catch (Exception p) {
                    // not authorized
                }
            }
        }
        return answer;
    }

    @Override
    @ImmediateService
    @RoleRead
    public boolean isFolder(String dataspace, String pathname) throws NotConnectedException, PermissionException {
        try {
            Method currentMethod = new Object() {
            }.getClass().getEnclosingMethod();
            SchedulerFrontendState.UserAndCredentials userAndCredentials = frontendState.checkPermissionReturningCredentials(currentMethod,
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
    @RoleRead
    public boolean checkFileExists(String dataspace, String pathname)
            throws NotConnectedException, PermissionException {
        try {
            Method currentMethod = new Object() {
            }.getClass().getEnclosingMethod();
            SchedulerFrontendState.UserAndCredentials userAndCredentials = frontendState.checkPermissionReturningCredentials(currentMethod,
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
    @RoleWrite
    public Set<String> addJobSignal(String jobId, String signalName, Map<String, String> updatedVariables)
            throws NotConnectedException, UnknownJobException, PermissionException, SignalApiException,
            JobValidationException {

        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        logger.info("Request to send signalName " + signalName + " on job " + jobId + " received from " + currentUser);
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_SEND_SIGNALS_TO_THIS_JOB);
        if (StringUtils.isBlank(signalName.trim())) {
            throw new SignalApiException("Empty signals are not allowed");
        }
        try {
            TaskId taskId = createFakeTaskId(jobId);
            publicStore.createChannelIfAbsent(SIGNAL_ORIGINATOR, taskId, signalsChannel + jobId, true);

            Set<String> signals = publicStore.keySet(SIGNAL_ORIGINATOR, taskId, signalsChannel + jobId);
            String readyPrefix = SignalApiImpl.READY_PREFIX;
            if (!(signals.contains(readyPrefix + signalName) || signalName.startsWith(readyPrefix))) {
                throw new SignalApiException("Job " + jobId + " is not ready to receive the signalName " + signalName);
            }

            // Remove the existing ready signalName, add the signalName and return the set of signals
            Signal readySignal = (Signal) publicStore.get(SIGNAL_ORIGINATOR,
                                                          taskId,
                                                          signalsChannel + jobId,
                                                          readyPrefix + signalName);
            setUpdatedVariables(updatedVariables, readySignal);
            publicStore.remove(SIGNAL_ORIGINATOR, taskId, signalsChannel + jobId, readyPrefix + signalName);
            publicStore.put(SIGNAL_ORIGINATOR, taskId, signalsChannel + jobId, signalName, readySignal);

            Set<String> finalSignals = publicStore.keySet(SIGNAL_ORIGINATOR, taskId, signalsChannel + jobId);
            return finalSignals;
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not add signalName for the job " + jobId, e);
        }
    }

    private void setUpdatedVariables(Map<String, String> updatedVariables, Signal readySignal)
            throws JobValidationException {
        if (updatedVariables != null && !updatedVariables.isEmpty()) {
            validateUpdatedVariables(updatedVariables, readySignal.getInputVariables());
            Map<String, String> validatedUpdatedValues = new LinkedHashMap<>();
            readySignal.getInputVariables()
                       .forEach(inputVariable -> validatedUpdatedValues.put(inputVariable.getName(),
                                                                            inputVariable.getValue()));
            readySignal.setUpdatedVariables(validatedUpdatedValues);
        } else {
            readySignal.setUpdatedVariables(getDefaultUpdatedValues(readySignal.getInputVariables()));
        }
    }

    private boolean validateUpdatedVariables(Map<String, String> updatedVariables, List<JobVariable> inputVariables)
            throws JobValidationException {
        DefaultModelJobValidatorServiceProvider validatorServiceProvider = new DefaultModelJobValidatorServiceProvider();
        Map<String, Serializable> serializableUpdatedVariables = new LinkedHashMap<>(updatedVariables);
        validatorServiceProvider.validateVariables(inputVariables, serializableUpdatedVariables, this, this);
        return true;
    }

    private Map<String, String> getDefaultUpdatedValues(List<JobVariable> inputVariables) {
        Map<String, String> defaultUpdatedValues = new LinkedHashMap<>();
        if (inputVariables != null && !inputVariables.isEmpty()) {
            inputVariables.forEach(inputVariable -> defaultUpdatedValues.put(inputVariable.getName(),
                                                                             inputVariable.getValue()));
        }
        return defaultUpdatedValues;
    }

    @Override
    @ImmediateService
    @RoleWrite
    public List<JobVariable> validateJobSignal(String jobId, String signalName, Map<String, String> updatedVariables)
            throws NotConnectedException, UnknownJobException, PermissionException, SignalApiException,
            JobValidationException {

        if (updatedVariables == null || updatedVariables.isEmpty()) {
            return null;
        }
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        logger.debug("Request to validate signal " + signalName + " on job " + jobId + " received from " + currentUser);
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_SEND_SIGNALS_TO_THIS_JOB);

        String readyPrefix = SignalApiImpl.READY_PREFIX;
        if (StringUtils.isBlank(signalName.trim())) {
            throw new SignalApiException("Empty signals are not allowed");
        }
        try {
            TaskId taskId = createFakeTaskId(jobId);
            publicStore.createChannelIfAbsent(SIGNAL_ORIGINATOR, taskId, signalsChannel + jobId, true);

            Signal signal = (Signal) publicStore.get(SIGNAL_ORIGINATOR,
                                                     taskId,
                                                     signalsChannel + jobId,
                                                     readyPrefix + signalName);
            if (signal != null) {
                DefaultModelJobValidatorServiceProvider validatorServiceProvider = new DefaultModelJobValidatorServiceProvider();
                Map<String, Serializable> serializableUpdatedVariables = new LinkedHashMap<>();
                serializableUpdatedVariables.putAll(updatedVariables);
                validatorServiceProvider.validateVariables(signal.getInputVariables(),
                                                           serializableUpdatedVariables,
                                                           this,
                                                           this);
                return signal.getInputVariables();
            } else {
                throw new SignalApiException("Signal not found");
            }
        } catch (InvalidChannelException e) {
            throw new SignalApiException("Could not read signals channel", e);
        } catch (IOException e) {
            throw new SignalApiException("Could not add signalName for the job " + jobId, e);
        }
    }

    @Override
    @ImmediateService
    @RoleBasic
    public Map<String, Map<String, Boolean>> checkJobsPermissionMethods(List<String> jobIds, List<String> methods)
            throws NotConnectedException, UnknownJobException {
        if (methods == null || jobIds == null) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, Boolean>> answer = new HashMap<>(jobIds.size());
        for (String jobId : jobIds) {
            Map<String, Boolean> methodsPermissions = new HashMap<>(methods.size());
            for (String method : methods) {
                boolean hasPermission = true;
                try {
                    frontendState.checkPermissions(findMethod(method),
                                                   frontendState.getIdentifiedJob(JobIdImpl.makeJobId(jobId)),
                                                   SchedulerFrontendState.getErrorMessageForMethodPermission(method));
                } catch (PermissionException e) {
                    hasPermission = false;
                }
                methodsPermissions.put(method, hasPermission);
            }
            answer.put(jobId, methodsPermissions);
        }
        return answer;
    }

    @Override
    @ImmediateService
    @RoleWrite
    public void addExternalEndpointUrl(String jobId, String endpointName, String externalEndpointUrl,
            String endpointIconUri) throws NotConnectedException, PermissionException, UnknownJobException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_ADD_EXTERNAL_ENDPOINT_URL_TO_THIS_JOB);
        logger.info("Request to add external endpoint " + endpointName + " on job " + jobId + " received from " +
                    currentUser);
        schedulingService.addExternalEndpointUrl(jobIdObject, endpointName, externalEndpointUrl, endpointIconUri);
    }

    @Override
    @ImmediateService
    @RoleWrite
    public void removeExternalEndpointUrl(String jobId, String endpointName)
            throws NotConnectedException, PermissionException, UnknownJobException {
        String currentUser = frontendState.getCurrentUser();
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        final JobId jobIdObject = JobIdImpl.makeJobId(jobId);
        frontendState.checkPermissions(currentMethod,
                                       frontendState.getIdentifiedJob(jobIdObject),
                                       YOU_DO_NOT_HAVE_PERMISSION_TO_REMOVE_EXTERNAL_ENDPOINT_URL_TO_THIS_JOB);
        logger.info("Request to remove external endpoint " + endpointName + " on job " + jobId + " received from " +
                    currentUser);
        schedulingService.removeExternalEndpointUrl(jobIdObject, endpointName);
    }
}
