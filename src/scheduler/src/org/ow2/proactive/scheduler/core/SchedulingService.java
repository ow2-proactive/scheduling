package org.ow2.proactive.scheduler.core;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.InternalException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.exception.ProgressPingerException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.utils.NodeSet;


public class SchedulingService {

    static final Logger logger = Logger.getLogger(SchedulingService.class);
    static final TaskLogger tlogger = TaskLogger.getInstance();
    static final JobLogger jlogger = JobLogger.getInstance();

    static final long SCHEDULER_AUTO_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY
            .getValueAsInt() * 1000;

    static final long SCHEDULER_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_REMOVED_JOB_DELAY
            .getValueAsInt() * 1000;

    final SchedulingInfrastructure infrastructure;

    final LiveJobs jobs;

    final SchedulerStateUpdate listener;

    private final ListenJobLogsSupport listenJobLogsSupport;

    volatile SchedulerStatus status = SchedulerStatus.STOPPED;

    private volatile Policy policy;

    private final SchedulingThread schedulingThread;

    private Thread pinger;

    /** Url used to store the last url of the RM (used to try to reconnect to the rm when it is down)*/
    private URI lastRmUrl;

    public SchedulingService(SchedulingInfrastructure infrastructure, SchedulerStateUpdate listener,
            SchedulerStateRecoverHelper.RecoveredSchedulerState recoveredState, String policyClassName,
            SchedulingMethod schedulingMethod) throws Exception {
        this.infrastructure = infrastructure;
        this.listener = listener;
        this.jobs = new LiveJobs(infrastructure.getDBManager(), listener);
        this.listenJobLogsSupport = ListenJobLogsSupport.newInstance(infrastructure.getDBManager(), jobs);
        if (recoveredState != null) {
            recover(recoveredState);
        }

        this.policy = (Policy) Class.forName(policyClassName).newInstance();
        if (!this.policy.reloadConfig()) {
            throw new RuntimeException("Scheduling policy cannot be started, see log file for details.");
        }
        logger.info("Instantiated policy : " + policyClassName);

        lastRmUrl = infrastructure.getRMProxiesManager().getRmUrl();

        if (schedulingMethod == null) {
            schedulingMethod = new SchedulingMethodImpl(this);
        }

        start();

        schedulingThread = new SchedulingThread(schedulingMethod, this);
        schedulingThread.start();
        pinger = new NodePingThread(this);
        pinger.start();
    }

    public Policy getPolicy() {
        return policy;
    }

    public boolean isSubmitPossible() {
        return status.isSubmittable();
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
        return true;
    }

    public boolean shutdown() {
        if (status.isDown()) {
            return false;
        }

        status = SchedulerStatus.SHUTTING_DOWN;
        logger.info("Scheduler is shutting down, this make take time to finish every jobs !");
        listener.schedulerStateUpdated(SchedulerEvent.SHUTTING_DOWN);

        logger.info("Unpause all running and pending jobs !");
        jobs.unpauseAll();

        infrastructure.schedule(new Runnable() {
            public void run() {
                if (jobs.getRunningTasks().isEmpty()) {
                    listener.schedulerStateUpdated(SchedulerEvent.SHUTDOWN);
                } else {
                    infrastructure.schedule(this, 5000);
                }
            }
        }, 5000);

        return true;
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
            NodeSet nodes = taskData.getTask().getExecuterInformations().getNodes();
            try {
                taskData.getLauncher().terminate(false);
            } catch (Throwable t) {
                logger.error("Failed to terminate launcher", t);
            }
            try {
                infrastructure.getRMProxiesManager().getUserRMProxy(taskData.getUser(),
                        taskData.getCredendtials()).releaseNodes(nodes,
                        taskData.getTask().getCleaningScript());
            } catch (Throwable t) {
                logger.error("Failed to release nodes", t);
            }
        }

        listenJobLogsSupport.shutdown();
        infrastructure.shutdown();

        listener.schedulerStateUpdated(SchedulerEvent.KILLED);

        return true;
    }

    public ListenJobLogsSupport getListenJobLogsSupport() {
        return listenJobLogsSupport;
    }

    public boolean reloadPolicyConfiguration() {
        if (status.isShuttingDown()) {
            logger.warn("Policy configuration can only be reloaded when Scheduler is up, current state : " +
                status);
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

    public SchedulingInfrastructure getInfrastructure() {
        return infrastructure;
    }

    /*
     * Should be called only by scheduling method impl when job scheduling starts
     */
    public Map<JobId, JobDescriptor> lockJobsToSchedule() {
        return jobs.lockJobsToSchedule();
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
    public void taskStarted(InternalJob job, InternalTask task, TaskLauncher launcher) {
        jobs.taskStarted(job, task, launcher);
    }

    /*
     * Should be called only by scheduling method impl while it holds job lock
     */
    public void simulateJobStartAndCancelIt(final List<EligibleTaskDescriptor> tasksToSchedule,
            final String errorMsg) {
        infrastructure.getInternalOperationsThreadPool().submit(new Runnable() {
            public void run() {
                TerminationData terminationData = jobs.simulateJobStart(tasksToSchedule, errorMsg);
                terminationData.handleTermination(SchedulingService.this);
            }
        });
    }

    public void submitJob(InternalJob job) {
        try {
            infrastructure.getClientOperationsThreadPool().submit(new SubmitHandler(this, job)).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean pauseJob(final JobId jobId) {
        try {
            if (status.isShuttingDown()) {
                return false;
            }
            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return jobs.pauseJob(jobId);
                }

            }).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean resumeJob(final JobId jobId) {
        try {
            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return jobs.resumeJob(jobId);
                }

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
            infrastructure.getClientOperationsThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    jlogger.info(jobId, "request to change the priority to " + priority);
                    jobs.changeJobPriority(jobId, priority);
                }
            }).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean removeJob(JobId jobId) {
        try {
            return infrastructure.getClientOperationsThreadPool().submit(new JobRemoveHandler(this, jobId))
                    .get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public void scheduleJobRemove(JobId jobId, long delay) {
        infrastructure.schedule(new JobRemoveHandler(this, jobId), delay);
    }

    public void restartTaskOnNodeFailure(final InternalTask task) {
        try {
            if (status.isUnusable()) {
                return;
            }
            infrastructure.getInternalOperationsThreadPool().submit(new Runnable() {
                @Override
                public void run() {
                    TerminationData terminationData = jobs.restartTaskOnNodeFailure(task);
                    terminationData.handleTermination(SchedulingService.this);
                }
            }).get();
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
            terminationData.handleTermination(SchedulingService.this);
        }
    }

    public boolean killJob(final JobId jobId) {
        try {
            if (status.isUnusable()) {
                return false;
            }
            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.killJob(jobId);
                    boolean jobKilled = terminationData.jobTeminated(jobId);
                    submitTerminationDataHandler(terminationData);
                    return jobKilled;
                }

            }).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    void submitTerminationDataHandler(TerminationData terminationData) {
        if (!terminationData.isEmpty()) {
            getInfrastructure().getInternalOperationsThreadPool().submit(
                    new TerminationDataHandler(terminationData));
        }
    }

    public boolean killTask(final JobId jobId, final String taskName) throws UnknownJobException,
            UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }
            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.killTask(jobId, taskName);
                    boolean taskKilled = terminationData.taskTeminated(jobId, taskName);
                    submitTerminationDataHandler(terminationData);
                    return taskKilled;
                }
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

    public boolean restartTask(final JobId jobId, final String taskName, final int restartDelay)
            throws UnknownJobException, UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }
            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.restartTask(jobId, taskName, restartDelay);
                    boolean taskRestarted = terminationData.taskTeminated(jobId, taskName);
                    submitTerminationDataHandler(terminationData);
                    return taskRestarted;
                }
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
            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.preemptTask(jobId, taskName, restartDelay);
                    boolean taskRestarted = terminationData.taskTeminated(jobId, taskName);
                    submitTerminationDataHandler(terminationData);
                    return taskRestarted;
                }
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

    public void listenJobLogs(final JobId jobId, final AppenderProvider appenderProvider)
            throws UnknownJobException {
        try {
            infrastructure.getClientOperationsThreadPool().submit(new Callable<Void>() {
                @Override
                public Void call() throws UnknownJobException {
                    getListenJobLogsSupport().listenJobLogs(jobId, appenderProvider);
                    return null;
                }
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
        infrastructure.getInternalOperationsThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                TerminationData terminationData = jobs.taskTerminatedWithResult(taskId,
                        (TaskResultImpl) taskResult);
                terminationData.handleTermination(SchedulingService.this);
            }
        });
    }

    void handleException(Throwable t) {
        logger.error("Unexpected exception", t);
        try {
            PAFuture.waitFor(infrastructure.getRMProxiesManager().getSchedulerRMProxy().isActive(), true);
        } catch (Exception rme) {
            // Check and tries to reconnect the RM proxies else Freeze the scheduler.
            checkAndReconnectRM();
        }
    }

    /**
     * Check the connection to the RM. If the connection is down and automatic reconnection is enabled, this method performs n reconnection attempts before returning the result.
     * These parameters can be set in the configuration :
     * - Enabling/Disabling automatic reconnection: pa.scheduler.core.rmconnection.autoconnect (default is true)
     * - Delay in ms between 2 consecutive attempts: pa.scheduler.core.rmconnection.timespan (default is 5000 ms)
     * - Maximum number of attempts: pa.scheduler.core.rmconnection.attempts (default is 10)
     * @return true if the RM is alive, false otherwise.
     */
    private boolean checkAndReconnectRM() {
        // Result of the method.
        boolean alive = false;

        // Checks if the option is enabled (false by default)
        boolean autoReconnectRM = PASchedulerProperties.SCHEDULER_RMCONNECTION_AUTO_CONNECT.isSet() ? PASchedulerProperties.SCHEDULER_RMCONNECTION_AUTO_CONNECT
                .getValueAsBoolean()
                : false;

        // Delay (in ms) between each connection attempts (5s by default)
        int timespan = PASchedulerProperties.SCHEDULER_RMCONNECTION_TIMESPAN.isSet() ? PASchedulerProperties.SCHEDULER_RMCONNECTION_TIMESPAN
                .getValueAsInt()
                : 5000;

        // Maximum number of attempts (10 by default)
        int maxAttempts = PASchedulerProperties.SCHEDULER_RMCONNECTION_ATTEMPTS.isSet() ? PASchedulerProperties.SCHEDULER_RMCONNECTION_ATTEMPTS
                .getValueAsInt()
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

        logger.info("Trying to retrieve RM connection at url " + rmURL + "...");

        while (!alive && nbAttempts <= maxAttempts) {
            try {

                // Call isActive and wait recursively on the two futures (this -> proxy -> RM)
                PAFuture.waitFor(infrastructure.getRMProxiesManager().getSchedulerRMProxy().isActive(), true);
                alive = true;
                logger.info("Resource Manager successfully retrieved on " + rmURL);

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

            logger
                    .fatal("\n*****************************************************************************************************************\n" +
                        "* Resource Manager is no more available, Scheduler has been paused waiting for a resource manager to be reconnect\n" +
                        "* Scheduler is in critical state and its functionalities are reduced : \n" +
                        "* \t-> use the linkrm(\"" +
                        rmURL +
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

    void terminateJobHandling(final JobId jobId) {
        try {
            listenJobLogsSupport.cleanLoggers(jobId);

            infrastructure.getTaskClassServer().removeTaskClassServer(jobId);

            //auto remove
            if (SchedulingService.SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
                scheduleJobRemove(jobId, SchedulingService.SCHEDULER_AUTO_REMOVED_JOB_DELAY);
            }
        } catch (Throwable t) {
            logger.warn("", t);
        }
    }

    private void recover(SchedulerStateRecoverHelper.RecoveredSchedulerState recoveredState) {
        jobsRecovered(recoveredState.getPendingJobs());
        jobsRecovered(recoveredState.getRunningJobs());

        if (SCHEDULER_REMOVED_JOB_DELAY > 0 || SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
            logger.info("Removing non-managed jobs");
            Iterator<InternalJob> iterJob = recoveredState.getFinishedJobs().iterator();

            while (iterJob.hasNext()) {
                final InternalJob job = iterJob.next();
                //re-set job removed delay (if job result has been sent to user)
                long toWait = 0;
                if (job.isToBeRemoved()) {
                    toWait = SCHEDULER_REMOVED_JOB_DELAY * SCHEDULER_AUTO_REMOVED_JOB_DELAY == 0 ? SCHEDULER_REMOVED_JOB_DELAY +
                        SCHEDULER_AUTO_REMOVED_JOB_DELAY
                            : Math.min(SCHEDULER_REMOVED_JOB_DELAY, SCHEDULER_AUTO_REMOVED_JOB_DELAY);
                } else {
                    toWait = SCHEDULER_AUTO_REMOVED_JOB_DELAY;
                }
                if (toWait > 0) {
                    scheduleJobRemove(job.getId(), toWait);
                    jlogger.debug(job.getId(), "will be removed in " + (SCHEDULER_REMOVED_JOB_DELAY / 1000) +
                        "sec");
                }
            }
        }
    }

    private void jobsRecovered(Collection<InternalJob> jobs) {
        DataSpaceServiceStarter dsStarter = infrastructure.getDataSpaceServiceStarter();
        SchedulerClassServers classServers = infrastructure.getTaskClassServer();

        for (InternalJob job : jobs) {
            this.jobs.jobRecovered(job);
            switch (job.getStatus()) {
                case PENDING:
                    // restart classserver if needed
                    infrastructure.getTaskClassServer().createTaskClassServer(job, false);
                    break;
                case STALLED:
                case RUNNING:
                    //start dataspace app for this job
                    job.startDataSpaceApplication(dsStarter.getNamingService(), dsStarter
                            .getNamingServiceURL());
                    // restart classServer if needed
                    classServers.createTaskClassServer(job, false);
                    break;
                case FINISHED:
                case CANCELED:
                case FAILED:
                case KILLED:
                    break;
                case PAUSED:
                    // restart classserver if needed
                    classServers.createTaskClassServer(job, false);
            }
        }
    }

    void pingTaskNode(RunningTaskData taskData) {
        if (!jobs.canPingTask(taskData)) {
            return;
        }

        InternalTask task = taskData.getTask();
        try {
            int progress = taskData.getLauncher().getProgress();//(2)
            //get previous inside td
            if (progress != task.getProgress()) {
                task.setProgress(progress);//(1)
                //if progress != previously set progress (0 by default) -> update
                listener.taskStateUpdated(taskData.getUser(), new NotificationData<TaskInfo>(
                    SchedulerEvent.TASK_PROGRESS, new TaskInfoImpl((TaskInfoImpl) task.getTaskInfo())));
            }
        } catch (NullPointerException e) {
            //should not happened, but avoid restart if execInfo or launcher is null
            //nothing to do
            if (tlogger.isDebugEnabled()) {
                tlogger.debug(task.getId(), "getProgress failed", e);
            }
        } catch (IllegalArgumentException e) {
            //thrown by (1)
            //avoid setting bad value, no event if bad
            if (tlogger.isDebugEnabled()) {
                tlogger.debug(task.getId(), "getProgress failed", e);
            }
        } catch (ProgressPingerException e) {
            //thrown by (2) in one of this two cases :
            // * when user has overridden getProgress method and the method throws an exception
            // * if forked JVM process is dead
            //nothing to do in any case
            if (tlogger.isDebugEnabled()) {
                tlogger.debug(task.getId(), "getProgress failed", e);
            }
        } catch (Throwable t) {
            tlogger.info(task.getId(), "node failed", t);
            restartTaskOnNodeFailure(task);
        }
    }

}
