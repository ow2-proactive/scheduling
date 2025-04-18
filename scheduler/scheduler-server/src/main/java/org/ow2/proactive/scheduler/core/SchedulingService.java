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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil.HybridEncryptedData;
import org.ow2.proactive.core.properties.PASharedProperties;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.InternalException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.core.db.RecoveredSchedulerState;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.helpers.LogoValidator;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.job.UserIdentificationImpl;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.synchronization.SynchronizationInternal;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.MultipleTimingLogger;
import org.ow2.proactive.scheduler.util.ServerJobAndTaskLogs;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.proactive.utils.Tools;

import it.sauronsoftware.cron4j.Scheduler;


public class SchedulingService {

    static final Logger logger = Logger.getLogger(SchedulingService.class);

    static final TaskLogger tlogger = TaskLogger.getInstance();

    static final JobLogger jlogger = JobLogger.getInstance();

    static final long SCHEDULER_AUTO_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY.getValueAsLong() *
                                                         1000;

    static final long SCHEDULER_AUTO_REMOVED_ERROR_JOB_DELAY = PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_ERROR_JOB_DELAY.getValueAsLong() *
                                                               1000;

    static final long SCHEDULER_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_REMOVED_JOB_DELAY.getValueAsLong() *
                                                    1000;

    static final String GENERIC_INFO_REMOVE_DELAY = "REMOVE_DELAY";

    static final String GENERIC_INFO_REMOVE_DELAY_ON_ERROR = "REMOVE_DELAY_ON_ERROR";

    public static final String SCHEDULING_SERVICE_RECOVER_TASKS_STATE_FINISHED = "SchedulingService::recoverTasksState finished";

    public static final int SCHEDULER_KILL_DELAY = PASchedulerProperties.SCHEDULER_KILL_DELAY.getValueAsInt();

    public static final String LOGO_PATH = PASchedulerProperties.getAbsolutePath("dist/war/getstarted/assets/image/custom-logo.png");

    private final SchedulingInfrastructure infrastructure;

    private final LiveJobs jobs;

    private final SchedulerStateUpdate listener;

    private final ListenJobLogsSupport listenJobLogsSupport;

    volatile SchedulerStatus status = SchedulerStatus.STOPPED;

    private volatile Policy policy;

    private final SchedulingThread schedulingThread;

    private Thread pinger;

    private Scheduler houseKeepingScheduler;

    private Scheduler backupScheduler;

    private SynchronizationInternal synchronizationAPI;

    /**
     * Url used to store the last url of the RM (used to try to reconnect to the rm when it is down)
     */
    private URI lastRmUrl;

    public SchedulingService(SchedulingInfrastructure infrastructure, SchedulerStateUpdate listener,
            RecoveredSchedulerState recoveredState, String policyClassName, SchedulingMethod schedulingMethod,
            SynchronizationInternal synchronizationAPI) throws Exception {
        this.infrastructure = infrastructure;
        this.listener = listener;
        this.jobs = new LiveJobs(infrastructure.getDBManager(), listener, synchronizationAPI, this);
        if (recoveredState != null) {
            recover(recoveredState);
        }
        this.listenJobLogsSupport = ListenJobLogsSupport.newInstance(infrastructure.getDBManager(), jobs);

        this.policy = (Policy) Class.forName(policyClassName).newInstance();
        if (!this.policy.reloadConfig()) {
            throw new RuntimeException("Scheduling policy cannot be started, see log file for details.");
        }
        logger.debug("Instantiated policy : " + policyClassName);

        lastRmUrl = infrastructure.getRMProxiesManager().getRmUrl();

        this.synchronizationAPI = synchronizationAPI;

        if (schedulingMethod == null) {
            schedulingMethod = new SchedulingMethodImpl(this);
        }

        if (recoveredState != null && recoveredState.getSchedulerState() != null &&
            recoveredState.getSchedulerState().getStatus() != null) {
            switch (recoveredState.getSchedulerState().getStatus()) {
                case PAUSED:
                    status = SchedulerStatus.PAUSED;
                    logger.info("Scheduler has just been paused !");
                    listener.schedulerStateUpdated(SchedulerEvent.PAUSED);
                    break;
                case STOPPED:
                    // do nothing, status is stopped by default
                    break;
                case FROZEN:
                    status = SchedulerStatus.FROZEN;
                    logger.info("Scheduler has just been frozen !");
                    listener.schedulerStateUpdated(SchedulerEvent.FROZEN);
                    break;
                default:
                    start();
            }
        } else {
            start();
        }

        schedulingThread = new SchedulingThread(schedulingMethod, this);
        schedulingThread.setPriority(Thread.MAX_PRIORITY);
        schedulingThread.start();

        pinger = new NodePingThread(this);
        pinger.start();

        if (PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY.getValueAsLong() > 0) {
            startHouseKeeping();
        }

        if (PASharedProperties.SERVER_BACKUP.getValueAsBoolean()) {
            startBackuping();
        }
    }

    private void startBackuping() {
        logger.debug("Starting the scheduler backup process...");
        backupScheduler = new it.sauronsoftware.cron4j.Scheduler();
        String cronExpr = PASharedProperties.SERVER_BACKUP_PERIOD.getValueAsString();
        backupScheduler.schedule(cronExpr, new SchedulerBackupRunner(this, synchronizationAPI));
        backupScheduler.start();
    }

    public void startHouseKeeping() {
        houseKeepingScheduler = new Scheduler();
        String cronExpr = "* * * * *";
        if (PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_CRON_EXPR.isSet()) {
            cronExpr = PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_CRON_EXPR.getValueAsString();
        }
        houseKeepingScheduler.schedule(cronExpr, new HousekeepingRunner());
        houseKeepingScheduler.start();
    }

    public SynchronizationInternal getSynchronizationAPI() {
        return synchronizationAPI;
    }

    public Policy getPolicy() {
        return policy;
    }

    public LiveJobs getJobs() {
        return jobs;
    }

    public SchedulerStateUpdate getListener() {
        return listener;
    }

    public boolean isSubmitPossible() {
        return status.isSubmittable();
    }

    public boolean isPausedOrStopped() {
        return status == SchedulerStatus.PAUSED || status == SchedulerStatus.STOPPED;
    }

    public boolean start() {
        if (!status.isStartable()) {
            return false;
        }

        status = SchedulerStatus.STARTED;
        logger.info("Scheduler has just been started !");
        listener.schedulerStateUpdated(SchedulerEvent.STARTED);

        return true;
    }

    public boolean stop() {
        if (!status.isStoppable()) {
            return false;
        }

        status = SchedulerStatus.STOPPED;
        logger.info("Scheduler has just been stopped, no tasks will be launched until start.");
        listener.schedulerStateUpdated(SchedulerEvent.STOPPED);

        return true;
    }

    public boolean pause() {
        if (!status.isPausable()) {
            return false;
        }

        status = SchedulerStatus.PAUSED;
        logger.info("Scheduler has just been paused !");
        listener.schedulerStateUpdated(SchedulerEvent.PAUSED);

        return true;
    }

    public boolean freeze() {
        if (!status.isFreezable()) {
            return false;
        }

        status = SchedulerStatus.FROZEN;
        logger.info("Scheduler has just been frozen !");
        listener.schedulerStateUpdated(SchedulerEvent.FROZEN);
        return true;
    }

    public boolean resume() {
        if (!status.isResumable()) {
            return false;
        }

        status = SchedulerStatus.STARTED;
        logger.info("Scheduler has just been resumed !");
        listener.schedulerStateUpdated(SchedulerEvent.RESUMED);

        wakeUpSchedulingThread();

        return true;
    }

    public boolean shutdown() {
        if (status.isDown()) {
            return false;
        }

        status = SchedulerStatus.SHUTTING_DOWN;
        logger.info("Scheduler is shutting down, this may take time to finish every running tasks!");
        listener.schedulerStateUpdated(SchedulerEvent.SHUTTING_DOWN);

        infrastructure.schedule(new Runnable() {
            public void run() {
                if (jobs.getRunningTasks().isEmpty()) {
                    kill();
                } else {
                    infrastructure.schedule(this, 5000);
                }
            }
        }, 5000);

        return true;
    }

    /**
     * Create a new Credential object containing users' 3rd Party Credentials.
     *
     * @param creds credentials for specific user
     * @return in case of success new object containing the 3rd party credentials used to create bindings
     * at clean script
     */
    Credentials addThirdPartyCredentials(Credentials creds) throws KeyException, IllegalAccessException {
        //retrieve scheduler key pair
        String privateKeyPath = PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PRIVKEY_PATH.getValueAsString());
        String publicKeyPath = PASharedProperties.getAbsolutePath(PASharedProperties.AUTH_PUBKEY_PATH.getValueAsString());

        //get keys from task
        PrivateKey privateKey = Credentials.getPrivateKey(privateKeyPath);
        PublicKey publicKey = Credentials.getPublicKey(publicKeyPath);

        //retrieve the current creData from task
        CredData credData = creds.decrypt(privateKey);

        //retrive database to get third party credentials from
        SchedulerDBManager dbManager = getInfrastructure().getDBManager();
        if (dbManager != null) {
            Map<String, HybridEncryptedData> thirdPartyCredentials = dbManager.thirdPartyCredentialsMap(credData.getLogin());
            if (thirdPartyCredentials == null) {
                logger.error("Failed to retrieve Third Party Credentials!");
                throw new KeyException("Failed to retrieve thirdPartyCredentials!");
            } else {
                //cycle third party credentials, add one-by-one to the decrypter
                for (Map.Entry<String, HybridEncryptedData> thirdPartyCredential : thirdPartyCredentials.entrySet()) {
                    String decryptedValue = HybridEncryptionUtil.decryptString(thirdPartyCredential.getValue(),
                                                                               privateKey);
                    credData.addThirdPartyCredential(thirdPartyCredential.getKey(), decryptedValue);
                }
            }
        }
        return Credentials.createCredentials(credData, publicKey);
    }

    public boolean kill() {
        if (status.isKilled()) {
            return false;
        }

        status = SchedulerStatus.KILLED;

        pinger.interrupt();
        schedulingThread.interrupt();

        logger.info("Killing all running task processes...");
        for (RunningTaskData taskData : jobs.getRunningTasks()) {
            NodeSet nodes = taskData.getTask().getExecuterInformation().getNodes();
            try {
                taskData.getLauncher().kill();
            } catch (Throwable t) {
                logger.error("Failed to terminate launcher", t);
            }
            try {
                infrastructure.getRMProxiesManager()
                              .getUserRMProxy(taskData.getUser(), taskData.getCredentials())
                              .releaseNodes(nodes,
                                            taskData.getTask().getCleaningScript(),
                                            addThirdPartyCredentials(taskData.getCredentials()));
            } catch (Throwable t) {
                logger.error("Failed to release nodes", t);
            }
        }

        listenJobLogsSupport.shutdown();
        infrastructure.shutdown();

        listener.schedulerStateUpdated(SchedulerEvent.KILLED);

        // To properly exit the java scheduling process
        new Thread(() -> {
            try {
                Thread.sleep(SCHEDULER_KILL_DELAY);
            } catch (InterruptedException e) {
                logger.warn("Scheduler.kill() was interrupted while waiting for its configured delay, now terminating the process",
                            e);
            }
            System.exit(0);
        }).start();

        return true;
    }

    public ListenJobLogsSupport getListenJobLogsSupport() {
        return listenJobLogsSupport;
    }

    public boolean reloadPolicyConfiguration() {
        if (status.isShuttingDown()) {
            logger.warn("Policy configuration can only be reloaded when Scheduler is up, current state : " + status);
            return false;
        }
        return policy.reloadConfig();
    }

    public boolean changePolicy(String newPolicyClassName) {
        try {
            if (status.isShuttingDown()) {
                logger.warn("Policy can only be changed when Scheduler is up, current state : " + status);
                return false;
            }
            //TODO class loading ? (for now, class must be in scheduler classpath or addons)
            Policy newPolicy = (Policy) Class.forName(newPolicyClassName).newInstance();
            //newPolicy.setCore(this);
            if (!newPolicy.reloadConfig()) {
                return false;
            }
            //if success, change current policy
            policy = newPolicy;
            listener.schedulerStateUpdated(SchedulerEvent.POLICY_CHANGED);
            logger.info("Policy changed ! new policy name : " + newPolicyClassName);
            return true;
        } catch (InstantiationException e) {
            logger.error("", e);
            throw new InternalException("Exception occurs while instanciating the policy !", e);
        } catch (IllegalAccessException e) {
            logger.error("", e);
            throw new InternalException("Exception occurs while accessing the policy !", e);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
            throw new InternalException("Exception occurs while loading the policy class !", e);
        }
    }

    public boolean linkResourceManager(String rmURL) {
        try {
            //re-link the RM
            getInfrastructure().getRMProxiesManager().rebindRMProxiesManager(new URI(rmURL.trim()));
            logger.info("New resource manager has been linked to the scheduler");
            if (status == SchedulerStatus.UNLINKED) {
                logger.info("Resume to continue the scheduling.");
                listener.schedulerStateUpdated(SchedulerEvent.RM_UP);
                //restart the scheduler
                status = SchedulerStatus.STARTED;
                listener.schedulerStateUpdated(SchedulerEvent.STARTED);
            }
            return true;
        } catch (Exception e) {
            throw new InternalException("Error while connecting the new Resource Manager !", e);
        }
    }

    public void taskVariablesUpdated(InternalTask task) {
        listener.taskStateUpdated(task.getOwner(),
                                  new NotificationData<TaskInfo>(SchedulerEvent.TASK_VARIABLES_UPDATED,
                                                                 new TaskInfoImpl((TaskInfoImpl) task.getTaskInfo())));
    }

    public SchedulingInfrastructure getInfrastructure() {
        return infrastructure;
    }

    /*
     * Should be called only by scheduling method impl when job scheduling starts
     */
    public Map<JobId, JobDescriptor> lockJobsToSchedule() {
        return jobs.lockJobsToSchedule(isPausedOrStopped());
    }

    /*
     * Should be called only by scheduling method impl when job scheduling starts
     */
    public LiveJobs.JobData lockJob(JobId jid) {
        return jobs.lockJob(jid);
    }

    /*
     * Should be called only by scheduling method impl after job scheduling finished
     */
    public void unlockJobsToSchedule(Collection<JobDescriptor> jobDescriptors) {
        jobs.unlockJobsToSchedule(jobDescriptors);
    }

    /*
     * Should be called only by scheduling method impl while it holds job lock
     */
    public void taskStarted(InternalJob job, InternalTask task, TaskLauncher launcher, String taskLauncherNodeUrl) {
        jobs.taskStarted(job, task, launcher, taskLauncherNodeUrl);
    }

    /*
     * Should be called only by scheduling method impl while it holds job lock
     */
    public void simulateJobStartAndCancelIt(final List<EligibleTaskDescriptor> tasksToSchedule, final String errorMsg) {
        infrastructure.getInternalOperationsThreadPool().submit(() -> {
            TerminationData terminationData = jobs.simulateJobStart(tasksToSchedule, errorMsg);
            try {
                terminationData.handleTermination(SchedulingService.this);
            } catch (Exception e) {
                logger.error("Exception occurred, fail to get variables into the cleaning script: ", e);
            }
        });
    }

    public boolean isJobAlive(JobId jobId) {
        return jobs.isJobAlive(jobId);
    }

    public JobStatus getJobStatus(JobId jobId) {
        return jobs.getJobStatus(jobId);
    }

    public boolean isTaskAlive(TaskId taskId) {
        return jobs.isTaskAlive(taskId);
    }

    public TaskStatus getTaskStatus(TaskId taskId) {
        return jobs.getTaskStatus(taskId);
    }

    // for tests only
    public void submitJob(InternalJob job) {
        try {
            infrastructure.getClientOperationsThreadPool()
                          .submit(new SubmitHandler(this,
                                                    job,
                                                    null,
                                                    null,
                                                    new MultipleTimingLogger("TestTiming", logger)))
                          .get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public void submitJob(InternalJob job, SchedulerFrontendState frontendState, UserIdentificationImpl ident,
            MultipleTimingLogger timingLogger) {
        try {
            infrastructure.getClientOperationsThreadPool()
                          .submit(new SubmitHandler(this, job, frontendState, ident, timingLogger))
                          .get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public void submitJobs(List<InternalJob> jobs, SchedulerFrontendState frontendState, UserIdentificationImpl ident,
            MultipleTimingLogger timingLogger) {
        try {
            infrastructure.getClientOperationsThreadPool()
                          .submit(new SubmitHandler(this, jobs, frontendState, ident, timingLogger))
                          .get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean pauseJob(final JobId jobId) {
        try {
            if (status.isShuttingDown()) {
                return false;
            }
            return infrastructure.getClientOperationsThreadPool().submit(() -> jobs.pauseJob(jobId)).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean changeStartAt(final JobId jobId, final String startAt) {
        try {
            if (status.isShuttingDown()) {
                return false;
            }
            return infrastructure.getClientOperationsThreadPool()
                                 .submit(() -> jobs.updateStartAt(jobId, startAt))
                                 .get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean restartAllInErrorTasks(final JobId jobId) {
        try {
            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                Boolean result = jobs.restartAllInErrorTasks(jobId);
                wakeUpSchedulingThread();
                return result;
            }).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean resumeJob(final JobId jobId) {
        try {
            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                Boolean result = jobs.resumeJob(jobId);
                wakeUpSchedulingThread();
                return result;
            }).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public void changeJobPriority(final JobId jobId, final JobPriority priority) {
        if (status.isShuttingDown()) {
            return;
        }
        try {
            infrastructure.getClientOperationsThreadPool().submit(() -> {
                jlogger.info(jobId, "request to change the priority to " + priority);
                jobs.changeJobPriority(jobId, priority);
                wakeUpSchedulingThread();
            }).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean removeJob(JobId jobId) {
        try {
            return infrastructure.getClientOperationsThreadPool().submit(new JobRemoveHandler(this, jobId)).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public void scheduleJobRemove(JobId jobId, long at) {
        List<InternalJob> tempJobs = infrastructure.getDBManager().loadJobWithTasksIfNotRemoved(jobId);
        boolean shouldRemoveFromDb = PASchedulerProperties.JOB_REMOVE_FROM_DB.getValueAsBoolean();

        if (tempJobs.size() == 1) {
            infrastructure.getDBManager().scheduleJobForRemoval(tempJobs.get(0).getJobInfo().getJobId(),
                                                                at,
                                                                shouldRemoveFromDb);
        }
    }

    public void restartTaskOnNodeFailure(final InternalTask task) {
        if (status.isUnusable()) {
            return;
        }
        infrastructure.getInternalOperationsThreadPool().submit(() -> {
            TerminationData terminationData = jobs.restartTaskOnNodeFailure(task);
            try {
                terminationData.handleTermination(SchedulingService.this);
            } catch (Exception e) {
                logger.error("Exception occurred, fail to get variables into the cleaning script: ", e);
            }
            wakeUpSchedulingThread();
        });
    }

    public boolean removeJobs(List<JobId> jobIds) {
        try {
            return infrastructure.getClientOperationsThreadPool().submit(new JobRemoveHandler(this, jobIds)).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    class TerminationDataHandler implements Runnable {

        private final TerminationData terminationData;

        public TerminationDataHandler(TerminationData terminationData) {
            this.terminationData = terminationData;
        }

        public void run() {
            try {
                terminationData.handleTermination(SchedulingService.this);
            } catch (Exception e) {
                logger.error("Exception occurred, fail to get variables into the cleaning script:", e);
            }
        }
    }

    public boolean killJob(final JobId jobId) {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                TerminationData terminationData = jobs.killJob(jobId);
                boolean jobKilled = terminationData.jobTerminated(jobId);
                submitTerminationDataHandler(terminationData);
                wakeUpSchedulingThread();
                return jobKilled;
            }).get();

        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean killJobs(List<JobId> jobIds) {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                TerminationData terminationData = jobs.killJobs(jobIds);
                submitTerminationDataHandler(terminationData);
                wakeUpSchedulingThread();
                return true;
            }).get();

        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    void submitTerminationDataHandler(TerminationData terminationData) {
        if (!terminationData.isEmpty()) {
            try {
                getInfrastructure().getInternalOperationsThreadPool()
                                   .submit(new TerminationDataHandler(terminationData))
                                   .get();
            } catch (Exception e) {
                logger.warn("Error when executing job or task termination", e);
            }
        }
    }

    public boolean killTask(final JobId jobId, final String taskName) throws UnknownJobException, UnknownTaskException {
        return killTask(jobId, taskName, LiveJobs.KILL_TASK_DEFAULT_MESSAGE);
    }

    public boolean killTask(final JobId jobId, final String taskName, final String message)
            throws UnknownJobException, UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                TerminationData terminationData = jobs.killTask(jobId, taskName, message);
                boolean taskKilled = terminationData.taskTerminated(jobId, taskName);
                submitTerminationDataHandler(terminationData);
                wakeUpSchedulingThread();
                return taskKilled;
            }).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTaskException) {
                throw (UnknownTaskException) e.getCause();
            } else if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public boolean enableRemoteVisualization(final JobId jobId, final String taskName, final String connectionString)
            throws UnknownJobException, UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                return jobs.enableRemoteVisualization(jobId, taskName, connectionString);
            }).get();

        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTaskException) {
                throw (UnknownTaskException) e.getCause();
            } else if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public boolean registerService(final JobId jobId, final int serviceId, boolean enableActions)
            throws UnknownJobException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                return jobs.registerService(jobId, serviceId, enableActions);
            }).get();

        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public boolean detachService(final JobId jobId, final int serviceId) throws UnknownJobException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                return jobs.detachService(jobId, serviceId);
            }).get();

        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public boolean addExternalEndpointUrl(JobId jobId, String endpointName, String externalEndpointUrl,
            String endpointIconUri) throws UnknownJobException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                return jobs.addExternalEndpointUrl(jobId, endpointName, externalEndpointUrl, endpointIconUri);
            }).get();

        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public void updateLogo(byte[] image, String userName) throws IOException {
        byte[] imageAsPng = LogoValidator.checkImage(image);
        FileUtils.writeByteArrayToFile(new File(LOGO_PATH), imageAsPng);
        logger.info(String.format("User %s has changed the logo at %s", userName, LOGO_PATH));
    }

    public boolean removeExternalEndpointUrl(JobId jobId, String endpointName) throws UnknownJobException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                return jobs.removeExternalEndpointUrl(jobId, endpointName);
            }).get();

        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public boolean restartTask(final JobId jobId, final String taskName, final int restartDelay)
            throws UnknownJobException, UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                TerminationData terminationData = jobs.restartTask(jobId, taskName, restartDelay);
                boolean taskRestarted = terminationData.taskTerminated(jobId, taskName);
                submitTerminationDataHandler(terminationData);
                wakeUpSchedulingThread();
                return taskRestarted;
            }).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTaskException) {
                throw (UnknownTaskException) e.getCause();
            } else if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public boolean finishInErrorTask(final JobId jobId, final String taskName)
            throws UnknownJobException, UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                TerminationData terminationData = jobs.finishInErrorTask(jobId, taskName);
                boolean taskfinished = terminationData.taskTerminated(jobId, taskName);
                submitTerminationDataHandler(terminationData);
                wakeUpSchedulingThread();
                return taskfinished;
            }).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTaskException) {
                throw (UnknownTaskException) e.getCause();
            } else if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public boolean restartInErrorTask(final JobId jobId, final String taskName)
            throws UnknownJobException, UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                jobs.restartInErrorTask(jobId, taskName);
                wakeUpSchedulingThread();
                return Boolean.TRUE;
            }).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTaskException) {
                throw (UnknownTaskException) e.getCause();
            } else if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public boolean preemptTask(final JobId jobId, final String taskName, final int restartDelay)
            throws UnknownJobException, UnknownTaskException {
        try {
            return infrastructure.getClientOperationsThreadPool().submit(() -> {
                TerminationData terminationData = jobs.preemptTask(jobId, taskName, restartDelay);
                boolean taskRestarted = terminationData.taskTerminated(jobId, taskName);
                submitTerminationDataHandler(terminationData);
                wakeUpSchedulingThread();
                return taskRestarted;
            }).get();

        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTaskException) {
                throw (UnknownTaskException) e.getCause();
            } else if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public void listenJobLogs(final JobId jobId, final AppenderProvider appenderProvider) throws UnknownJobException {
        try {
            infrastructure.getClientOperationsThreadPool().submit((Callable<Void>) () -> {
                getListenJobLogsSupport().listenJobLogs(jobId, appenderProvider);
                return null;
            }).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownJobException) {
                throw (UnknownJobException) e.getCause();
            } else {
                throw launderThrowable(e.getCause());
            }
        } catch (Exception e) {
            throw launderThrowable(e);
        }
    }

    public void taskTerminatedWithResult(final TaskId taskId, final TaskResult taskResult) {
        infrastructure.getInternalOperationsThreadPool().submit(() -> {
            try {
                TerminationData terminationData = jobs.taskTerminatedWithResult(taskId, (TaskResultImpl) taskResult);
                terminationData.handleTermination(SchedulingService.this);
                wakeUpSchedulingThread();
            } catch (Throwable e) {
                logger.error("Failed to terminate task " + taskId, e);
            }
        });
    }

    void handleException(Throwable t) {
        logger.error("Unexpected exception in the scheduling thread - checking the connection to resource manager", t);
        try {
            // check if the connection to RM is still active
            // if not reactivate it for all the proxies
            checkAndReconnectRM();
        } catch (Exception rme) {
            logger.error("Error while reconnecting to the resource manager", rme);
        }
    }

    /**
     * Check the connection to the RM. If the connection is down and automatic reconnection is enabled, this method performs n reconnection attempts before returning the result.
     * These parameters can be set in the configuration :
     * - Enabling/Disabling automatic reconnection: pa.scheduler.core.rmconnection.autoconnect (default is true)
     * - Delay in ms between 2 consecutive attempts: pa.scheduler.core.rmconnection.timespan (default is 5000 ms)
     * - Maximum number of attempts: pa.scheduler.core.rmconnection.attempts (default is 10)
     *
     * @return true if the RM is alive, false otherwise.
     */
    private boolean checkAndReconnectRM() {
        // Result of the method.
        boolean alive = false;

        // Checks if the option is enabled (false by default)
        boolean autoReconnectRM = PASchedulerProperties.SCHEDULER_RMCONNECTION_AUTO_CONNECT.isSet() ? PASchedulerProperties.SCHEDULER_RMCONNECTION_AUTO_CONNECT.getValueAsBoolean()
                                                                                                    : false;

        // Delay (in ms) between each connection attempts (5s by default)
        int timespan = PASchedulerProperties.SCHEDULER_RMCONNECTION_TIMESPAN.isSet() ? PASchedulerProperties.SCHEDULER_RMCONNECTION_TIMESPAN.getValueAsInt()
                                                                                     : 5000;

        // Maximum number of attempts (10 by default)
        int maxAttempts = PASchedulerProperties.SCHEDULER_RMCONNECTION_ATTEMPTS.isSet() ? PASchedulerProperties.SCHEDULER_RMCONNECTION_ATTEMPTS.getValueAsInt()
                                                                                        : 10;

        // If the options is disabled or the number of attempts is wrong, it is set to 1
        if (!autoReconnectRM || maxAttempts <= 0)
            maxAttempts = 1;

        // Check the timespan option
        if (timespan <= 0)
            timespan = 5000;

        // Save the url in a string of the last connected RM.
        String rmURL = this.lastRmUrl.toString();
        int nbAttempts = 1;

        logger.info("Automatically reconnecting to RM at url " + rmURL + "...");

        while (!alive && nbAttempts <= maxAttempts) {
            try {
                infrastructure.getRMProxiesManager().rebindRMProxiesManager(new URI(rmURL));
                logger.info("Successfully reconnected to Resource Manager at " + rmURL);
                alive = true;
            } catch (Exception rme) {
                alive = false;

                if (nbAttempts != maxAttempts) {
                    try {
                        // Sleep before two attempts
                        logger.info("Waiting " + timespan + " ms before the next attempt...");
                        Thread.sleep(timespan);
                    } catch (InterruptedException ex) {
                        logger.error("An exception has occurred while waiting.");
                    }
                }
            }
            nbAttempts++;
        }

        if (!alive) {

            logger.info("Resource Manager seems to be dead.");

            // Disconnect proxies and freeze the scheduler.
            clearProxiesAndFreeze();

            logger.fatal("\n*****************************************************************************************************************\n" +
                         "* Resource Manager is no more available, Scheduler has been paused waiting for a resource manager to be reconnect\n" +
                         "* Scheduler is in critical state and its functionalities are reduced : \n" +
                         "* \t-> use the linkrm(\"" + rmURL +
                         "\") command in scheduler-client to reconnect a new one.\n" +
                         "*****************************************************************************************************************");

            listener.schedulerStateUpdated(SchedulerEvent.RM_DOWN);
        }

        return alive;
    }

    /**
     * Terminate all proxies and freeze the scheduler.
     */
    private void clearProxiesAndFreeze() {

        // Terminate proxy and disconnect RM
        logger.error("Resource Manager will be disconnected");
        infrastructure.getRMProxiesManager().terminateAllProxies();

        //if failed
        freeze();

        //scheduler functionality are reduced until now
        status = SchedulerStatus.UNLINKED;
    }

    static RuntimeException handleFutureWaitException(Exception e) {
        if (e instanceof ExecutionException) {
            return launderThrowable(e.getCause());
        } else {
            return launderThrowable(e);
        }
    }

    static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new IllegalStateException("Not unchecked", t);
        }
    }

    void terminateJobHandling(final JobId jobId, final Map<String, String> jobGenericInfo, boolean isJobWithErrors) {
        try {
            listenJobLogsSupport.cleanLoggers(jobId);
            jlogger.close(jobId);

            long removeDelay = Long.MAX_VALUE;
            if (SchedulingService.SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
                removeDelay = SchedulingService.SCHEDULER_AUTO_REMOVED_JOB_DELAY;
            }
            if (isJobWithErrors && SchedulingService.SCHEDULER_AUTO_REMOVED_ERROR_JOB_DELAY > 0) {
                removeDelay = SchedulingService.SCHEDULER_AUTO_REMOVED_ERROR_JOB_DELAY;
            }
            removeDelay = getRemoveDelayFromGenericInfo(jobId, jobGenericInfo, isJobWithErrors, removeDelay);

            // auto remove
            if (removeDelay < Long.MAX_VALUE) {
                long timeToRemove = System.currentTimeMillis() + removeDelay;
                jlogger.info(jobId, "Job " + jobId + " will be removed at " + new Date(timeToRemove));
                scheduleJobRemove(jobId, timeToRemove);
            }
        } catch (Throwable t) {
            logger.warn("", t);
        }
    }

    private long getRemoveDelayFromGenericInfo(JobId jobId, Map<String, String> jobGenericInfo, boolean isJobWithErrors,
            long removeDelay) {
        if (jobGenericInfo != null) {
            if (jobGenericInfo.containsKey(GENERIC_INFO_REMOVE_DELAY)) {
                try {
                    removeDelay = Tools.parsePeriod(jobGenericInfo.get(GENERIC_INFO_REMOVE_DELAY));
                } catch (Exception e) {
                    logger.error("Error when parsing generic information " + GENERIC_INFO_REMOVE_DELAY + " for job " +
                                 jobId, e);
                }
            }
            if (isJobWithErrors && jobGenericInfo.containsKey(GENERIC_INFO_REMOVE_DELAY_ON_ERROR)) {
                try {
                    removeDelay = Tools.parsePeriod(jobGenericInfo.get(GENERIC_INFO_REMOVE_DELAY_ON_ERROR));
                } catch (Exception e) {
                    logger.error("Error when parsing generic information " + GENERIC_INFO_REMOVE_DELAY_ON_ERROR +
                                 " for job " + jobId, e);
                }
            }
        }
        return removeDelay;
    }

    private void recover(RecoveredSchedulerState recoveredState) {
        List<InternalJob> finishedJobs = recoveredState.getFinishedJobs();
        List<InternalJob> pendingJobs = recoveredState.getPendingJobs();
        List<InternalJob> runningJobs = recoveredState.getRunningJobs();

        jobsRecovered(pendingJobs);
        jobsRecovered(runningJobs);

        recoverTasksState(finishedJobs, false);

        recoverTasksState(runningJobs, true);
        logger.info(SCHEDULING_SERVICE_RECOVER_TASKS_STATE_FINISHED); // this log is important for performance tests

        recoverTasksState(pendingJobs, true);

        if (SCHEDULER_REMOVED_JOB_DELAY > 0 || SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
            logger.debug("Removing non-managed jobs");
            // Note : by default, no finished jobs are recovered from the database, the following code
            // will not be executed
            for (InternalJob job : recoveredState.getFinishedJobs()) {
                //re-set job removed delay (if job result has been sent to user)
                long toWait = Long.MAX_VALUE;
                boolean isJobWithErrors = LiveJobs.isJobWithErrors(job);
                long configuredAutoRemove = isJobWithErrors &&
                                            SCHEDULER_AUTO_REMOVED_ERROR_JOB_DELAY > 0 ? SCHEDULER_AUTO_REMOVED_ERROR_JOB_DELAY
                                                                                       : (SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0 ? SCHEDULER_AUTO_REMOVED_JOB_DELAY
                                                                                                                               : Long.MAX_VALUE);

                if (job.isToBeRemoved()) {
                    toWait = SCHEDULER_REMOVED_JOB_DELAY > 0 ? SCHEDULER_REMOVED_JOB_DELAY : configuredAutoRemove;
                } else {
                    toWait = configuredAutoRemove;
                    toWait = getRemoveDelayFromGenericInfo(job.getId(),
                                                           job.getGenericInformation(),
                                                           isJobWithErrors,
                                                           toWait);
                }
                if (toWait < Long.MAX_VALUE) {
                    long removalDate = job.getFinishedTime() + toWait;
                    if (job.getScheduledTimeForRemoval() == 0 || removalDate < job.getScheduledTimeForRemoval()) {
                        scheduleJobRemove(job.getId(), removalDate);
                        jlogger.info(job.getId(), "will be removed at " + new Date(removalDate));
                    }
                }
            }
        }
    }

    private void recoverTasksState(List<InternalJob> jobs, boolean restoreInErrorTasks) {
        for (InternalJob job : jobs) {
            int faultyTasksCount = 0;

            for (InternalTask internalTask : job.getITasks()) {
                switch (internalTask.getStatus()) {
                    case FAULTY:
                        faultyTasksCount++;
                        break;
                    case WAITING_ON_ERROR:
                        faultyTasksCount++;
                        job.saveFaultyTaskId(internalTask.getId());
                        break;
                }
            }

            if (faultyTasksCount != job.getNumberOfFaultyTasks()) {
                logger.warn("Number of faulty tasks saved in DB for Job " + job.getId() +
                            " does not match the one computed using task statuses");
            }

            if (restoreInErrorTasks) {
                job.getJobDescriptor().restoreInErrorTasks();
            }
            job.getJobDescriptor().restoreRunningTasks();
        }
    }

    private void jobsRecovered(Collection<InternalJob> jobs) {
        DataSpaceServiceStarter dsStarter = infrastructure.getDataSpaceServiceStarter();

        for (InternalJob job : jobs) {
            this.jobs.jobRecovered(job);
            switch (job.getStatus()) {
                case PENDING:
                    break;
                case STALLED:
                case RUNNING:
                    //start dataspace app for this job
                    job.startDataSpaceApplication(dsStarter.getNamingService(), job.getITasks());
                    // restart classServer if needed
                    break;
                case FINISHED:
                case CANCELED:
                case FAILED:
                case KILLED:
                    break;
                case PAUSED:
            }
        }
    }

    void getProgressAndPingTaskNode(RunningTaskData taskData) {
        if (!jobs.canPingTask(taskData) ||
            taskData.getPingAttempts() > PASchedulerProperties.SCHEDULER_NODE_PING_ATTEMPTS.getValueAsInt()) {
            return;
        }

        InternalTask task = taskData.getTask();
        try {
            int progress = taskData.getLauncher().getProgress();//(2)
            //get previous inside td
            if (progress != task.getProgress()) {
                task.setProgress(progress);//(1)
                //if progress != previously set progress (0 by default) -> update
                listener.taskStateUpdated(taskData.getUser(),
                                          new NotificationData<TaskInfo>(SchedulerEvent.TASK_PROGRESS,
                                                                         new TaskInfoImpl((TaskInfoImpl) task.getTaskInfo())));
            }
        } catch (Throwable t) {
            tlogger.debug(task.getId(), "TaskLauncher is not accessible, checking if the node can be reached.", t);
            pingTaskNodeAndInitiateRestart(task);
        }
    }

    private void pingTaskNodeAndInitiateRestart(InternalTask task) {

        RunningTaskData runningTask = jobs.getRunningTask(task.getId());
        if (runningTask != null) {
            // We try to ping the node where the task is running to make sure the exception raised is due to a node failure.
            // We don't consider here other nodes reserved for the task,
            // as it is the responsibility of the task itself to manage extra nodes lifecycle
            // in case of complex multinodes task deployment.
            Node nodeUsedToExecuteTask = runningTask.getNodeExecutor();

            try {
                nodeUsedToExecuteTask.getNumberOfActiveObjects();

            } catch (Exception e) {
                int attempts = runningTask.increaseAndGetPingAttempts();
                String nodeUrl = nodeUsedToExecuteTask.getNodeInformation().getURL();
                if (attempts > PASchedulerProperties.SCHEDULER_NODE_PING_ATTEMPTS.getValueAsInt()) {
                    tlogger.error(task.getId(), "node failed " + nodeUrl + ", initiate task restart.", e);
                    restartTaskOnNodeFailure(task);
                } else {
                    tlogger.warn(task.getId(),
                                 "cannot contact node " + nodeUrl + " - waiting while it comes back, attempt " +
                                               attempts,
                                 e);
                }
            }
        }
    }

    public void sleepSchedulingThread() throws InterruptedException {
        schedulingThread.sleepSchedulingThread();
    }

    public void wakeUpSchedulingThread() {
        schedulingThread.wakeUpSchedulingThread();
    }

    /**
     * This Runnable handles the Housekeeping
     */
    public class HousekeepingRunner implements Runnable {

        private ReentrantLock lock = new ReentrantLock();

        private List<Long> removeFromContext(Map<JobId, String> jobIdList) {
            for (Map.Entry<JobId, String> jobIdStringEntry : jobIdList.entrySet()) {
                JobId jobId = jobIdStringEntry.getKey();
                String owner = jobIdStringEntry.getValue();

                Credentials credentials = null;

                if (jobs.isJobAlive(jobId)) {
                    TerminationData terminationData = jobs.killJob(jobId);
                    credentials = terminationData.getCredentials(jobId);
                    submitTerminationDataHandler(terminationData);
                }

                getListener().jobStateUpdated(owner,
                                              new NotificationData<>(SchedulerEvent.JOB_REMOVE_FINISHED,
                                                                     new JobInfoImpl(jobId, owner)));
                ServerJobAndTaskLogs.getActiveInstance().remove(jobId, owner, credentials);
                logger.debug("HOUSEKEEPING sent JOB_REMOVE_FINISHED notification for job " + jobId);

            }

            wakeUpSchedulingThread();
            return jobIdList.keySet().stream().map(JobId::longValue).collect(Collectors.toList());
        }

        private void removeFromDB(List<Long> longJobIdList) {
            if (!longJobIdList.isEmpty()) {
                getInfrastructure().getDBManager()
                                   .executeHousekeepingInDB(longJobIdList,
                                                            PASchedulerProperties.JOB_REMOVE_FROM_DB.getValueAsBoolean());
            }
        }

        @Override
        public void run() {
            try {
                logger.info("Waiting for previous scheduler HOUSEKEEPING execution to complete");
                lock.lockInterruptibly();
                logger.info("Previous scheduler HOUSEKEEPING is terminated, now performing a new housekeeping");

                long timeNow = System.currentTimeMillis();
                try {
                    Map<JobId, String> jobIdList = getInfrastructure().getDBManager().getJobsToRemove(timeNow);

                    // remove from the memory context
                    long inMemoryTimeStart = System.currentTimeMillis();
                    List<Long> longJobIdList = removeFromContext(jobIdList);
                    long inMemoryTimeStop = System.currentTimeMillis();

                    // set the removedTime and also remove if required by the JOB_REMOVE_FROM_DB setting
                    long dbTimeStart = System.currentTimeMillis();
                    removeFromDB(longJobIdList);
                    long dbTimeStop = System.currentTimeMillis();

                    logger.info("HOUSEKEEPING of jobs " + longJobIdList +
                                " performed (Hibernate context removal took " + (inMemoryTimeStop - inMemoryTimeStart) +
                                " ms" + " and db removal took " + (dbTimeStop - dbTimeStart) + " ms)");
                } catch (Throwable e) {
                    logger.error("Error performing HOUSEKEEPING of jobs", e);
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for scheduler HousekeepingRunner lock", e);
            } finally {
                lock.unlock();
            }

        }
    }

}
