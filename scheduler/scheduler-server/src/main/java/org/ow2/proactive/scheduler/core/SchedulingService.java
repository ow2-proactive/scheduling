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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
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
import org.ow2.proactive.scheduler.core.db.RecoveredSchedulerState;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.utils.NodeSet;

import it.sauronsoftware.cron4j.Scheduler;


public class SchedulingService {

    static final Logger logger = Logger.getLogger(SchedulingService.class);

    static final TaskLogger tlogger = TaskLogger.getInstance();

    static final JobLogger jlogger = JobLogger.getInstance();

    static final long SCHEDULER_AUTO_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY.getValueAsInt() *
                                                         1000;

    static final long SCHEDULER_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_REMOVED_JOB_DELAY.getValueAsInt() *
                                                    1000;

    final SchedulingInfrastructure infrastructure;

    final LiveJobs jobs;

    final SchedulerStateUpdate listener;

    private final ListenJobLogsSupport listenJobLogsSupport;

    volatile SchedulerStatus status = SchedulerStatus.STOPPED;

    private volatile Policy policy;

    private final SchedulingThread schedulingThread;

    private Thread pinger;

    public ConcurrentLinkedQueue<JobId> jobsToDeleteFromDB;

    private Scheduler houseKeepingScheduler;

    /**
     * Url used to store the last url of the RM (used to try to reconnect to the rm when it is down)
     */
    private URI lastRmUrl;

    public SchedulingService(SchedulingInfrastructure infrastructure, SchedulerStateUpdate listener,
            RecoveredSchedulerState recoveredState, String policyClassName, SchedulingMethod schedulingMethod)
            throws Exception {
        this.infrastructure = infrastructure;
        this.listener = listener;
        this.jobs = new LiveJobs(infrastructure.getDBManager(), listener);
        this.listenJobLogsSupport = ListenJobLogsSupport.newInstance(infrastructure.getDBManager(), jobs);
        this.jobsToDeleteFromDB = new ConcurrentLinkedQueue<JobId>();
        if (recoveredState != null) {
            recover(recoveredState);
        }

        this.policy = (Policy) Class.forName(policyClassName).newInstance();
        if (!this.policy.reloadConfig()) {
            throw new RuntimeException("Scheduling policy cannot be started, see log file for details.");
        }
        logger.debug("Instantiated policy : " + policyClassName);

        lastRmUrl = infrastructure.getRMProxiesManager().getRmUrl();

        if (schedulingMethod == null) {
            schedulingMethod = new SchedulingMethodImpl(this);
        }

        start();

        schedulingThread = new SchedulingThread(schedulingMethod, this);
        schedulingThread.start();

        pinger = new NodePingThread(this);
        pinger.start();

        if (PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY.getValueAsInt() > 0) {
            startHouseKeeping(this.jobsToDeleteFromDB, this.infrastructure);
        }
    }

    public void startHouseKeeping(final ConcurrentLinkedQueue<JobId> jobsQueue, final SchedulingInfrastructure infrastructure) {
        houseKeepingScheduler = new Scheduler();
        String cronExpr = "* * * * *";
        if (PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_CRON_EXPR.isSet()) {
            cronExpr = PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_CRON_EXPR.getValueAsString();
        }
        houseKeepingScheduler.schedule(cronExpr, new Runnable() {
            @Override
            public void run() {
                logger.info("HOUSEKEEPING triggered");
                if (!jobsQueue.isEmpty()) {
                    ArrayList<Long> jobIdList = new ArrayList<>();
                    JobId jobId = jobsQueue.poll();
                    while (jobId != null) {
                        jobIdList.add(jobId.longValue());
                        jobId = jobsQueue.poll();
                    }
                    infrastructure.getDBManager().executeHousekeeping(jobIdList);
                }
            }
        });
        houseKeepingScheduler.start();
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

        wakeUpSchedulingThread();

        return true;
    }

    public boolean shutdown() {
        if (status.isDown()) {
            return false;
        }

        status = SchedulerStatus.SHUTTING_DOWN;
        logger.info("Scheduler is shutting down, this may take time to finish every jobs!");
        listener.schedulerStateUpdated(SchedulerEvent.SHUTTING_DOWN);

        logger.info("Unpause all running and pending jobs!");
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
            NodeSet nodes = taskData.getTask().getExecuterInformation().getNodes();
            try {
                taskData.getLauncher().kill();
            } catch (Throwable t) {
                logger.error("Failed to terminate launcher", t);
            }
            try {
                infrastructure.getRMProxiesManager()
                              .getUserRMProxy(taskData.getUser(), taskData.getCredentials())
                              .releaseNodes(nodes, taskData.getTask().getCleaningScript());
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
    public void simulateJobStartAndCancelIt(final List<EligibleTaskDescriptor> tasksToSchedule, final String errorMsg) {
        infrastructure.getInternalOperationsThreadPool().submit(new Runnable() {
            public void run() {
                TerminationData terminationData = jobs.simulateJobStart(tasksToSchedule, errorMsg);
                try {
                    terminationData.handleTermination(SchedulingService.this);
                } catch (Exception e) {
                    logger.error("Exception occurred, fail to get variables into the cleaning script: ", e);
                }
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

    public boolean changeStartAt(final JobId jobId, final String startAt) {
        try {
            if (status.isShuttingDown()) {
                return false;
            }
            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return jobs.updateStartAt(jobId, startAt);
                }

            }).get();
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    public boolean restartAllInErrorTasks(final JobId jobId) {
        try {
            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Boolean result = jobs.restartAllInErrorTasks(jobId);
                    wakeUpSchedulingThread();
                    return result;
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
                    Boolean result = jobs.resumeJob(jobId);
                    wakeUpSchedulingThread();
                    return result;
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
                    wakeUpSchedulingThread();
                }
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

    public void scheduleJobRemove(JobId jobId, long delay) {
        infrastructure.scheduleHousekeeping(new HousekeepingHandler(this, jobId), delay);
    }

    public void restartTaskOnNodeFailure(final InternalTask task) {
        if (status.isUnusable()) {
            return;
        }
        infrastructure.getInternalOperationsThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                TerminationData terminationData = jobs.restartTaskOnNodeFailure(task);
                try {
                    terminationData.handleTermination(SchedulingService.this);
                } catch (Exception e) {
                    logger.error("Exception occurred, fail to get variables into the cleaning script: ", e);
                }
                wakeUpSchedulingThread();
            }
        });
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

            Boolean result = infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.killJob(jobId);
                    boolean jobKilled = terminationData.jobTerminated(jobId);
                    submitTerminationDataHandler(terminationData);
                    wakeUpSchedulingThread();
                    return jobKilled;
                }
            }).get();

            return result;
        } catch (Exception e) {
            throw handleFutureWaitException(e);
        }
    }

    void submitTerminationDataHandler(TerminationData terminationData) {
        if (!terminationData.isEmpty()) {
            getInfrastructure().getInternalOperationsThreadPool().submit(new TerminationDataHandler(terminationData));
        }
    }

    public boolean killTask(final JobId jobId, final String taskName) throws UnknownJobException, UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            Boolean result = infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.killTask(jobId, taskName);
                    boolean taskKilled = terminationData.taskTerminated(jobId, taskName);
                    submitTerminationDataHandler(terminationData);
                    wakeUpSchedulingThread();
                    return taskKilled;
                }
            }).get();

            return result;
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

            Boolean result = infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.restartTask(jobId, taskName, restartDelay);
                    boolean taskRestarted = terminationData.taskTerminated(jobId, taskName);
                    submitTerminationDataHandler(terminationData);
                    wakeUpSchedulingThread();
                    return taskRestarted;
                }
            }).get();

            return result;
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

            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.finishInErrorTask(jobId, taskName);
                    boolean taskfinished = terminationData.taskTerminated(jobId, taskName);
                    submitTerminationDataHandler(terminationData);
                    wakeUpSchedulingThread();
                    return taskfinished;
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

    public boolean restartInErrorTask(final JobId jobId, final String taskName)
            throws UnknownJobException, UnknownTaskException {
        try {
            if (status.isUnusable()) {
                return false;
            }

            return infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    jobs.restartInErrorTask(jobId, taskName);
                    wakeUpSchedulingThread();
                    return Boolean.TRUE;
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
            Boolean result = infrastructure.getClientOperationsThreadPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TerminationData terminationData = jobs.preemptTask(jobId, taskName, restartDelay);
                    boolean taskRestarted = terminationData.taskTerminated(jobId, taskName);
                    submitTerminationDataHandler(terminationData);
                    wakeUpSchedulingThread();
                    return taskRestarted;
                }
            }).get();

            return result;
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
                try {
                    TerminationData terminationData = jobs.taskTerminatedWithResult(taskId,
                                                                                    (TaskResultImpl) taskResult);
                    terminationData.handleTermination(SchedulingService.this);
                    wakeUpSchedulingThread();
                } catch (Throwable e) {
                    logger.error("Failed to terminate task " + taskId, e);
                }
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

    void terminateJobHandling(final JobId jobId) {
        try {
            listenJobLogsSupport.cleanLoggers(jobId);

            // auto remove
            if (SchedulingService.SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
                scheduleJobRemove(jobId, SchedulingService.SCHEDULER_AUTO_REMOVED_JOB_DELAY);
            }
        } catch (Throwable t) {
            logger.warn("", t);
        }
    }

    private void recover(RecoveredSchedulerState recoveredState) {
        Vector<InternalJob> finishedJobs = recoveredState.getFinishedJobs();
        Vector<InternalJob> pendingJobs = recoveredState.getPendingJobs();
        Vector<InternalJob> runningJobs = recoveredState.getRunningJobs();

        jobsRecovered(pendingJobs);
        jobsRecovered(runningJobs);

        recoverTasksState(finishedJobs, false);
        recoverTasksState(runningJobs, true);
        recoverTasksState(pendingJobs, true);

        if (SCHEDULER_REMOVED_JOB_DELAY > 0 || SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
            logger.debug("Removing non-managed jobs");
            Iterator<InternalJob> iterJob = recoveredState.getFinishedJobs().iterator();

            while (iterJob.hasNext()) {
                final InternalJob job = iterJob.next();
                //re-set job removed delay (if job result has been sent to user)
                long toWait = 0;
                if (job.isToBeRemoved()) {
                    toWait = SCHEDULER_REMOVED_JOB_DELAY *
                             SCHEDULER_AUTO_REMOVED_JOB_DELAY == 0 ? SCHEDULER_REMOVED_JOB_DELAY +
                                                                     SCHEDULER_AUTO_REMOVED_JOB_DELAY
                                                                   : Math.min(SCHEDULER_REMOVED_JOB_DELAY,
                                                                              SCHEDULER_AUTO_REMOVED_JOB_DELAY);
                } else {
                    toWait = SCHEDULER_AUTO_REMOVED_JOB_DELAY;
                }
                if (toWait > 0) {
                    scheduleJobRemove(job.getId(), toWait);
                    jlogger.debug(job.getId(), "will be removed in " + (SCHEDULER_REMOVED_JOB_DELAY / 1000) + "sec");
                }
            }
        }
    }

    private void recoverTasksState(Vector<InternalJob> jobs, boolean restoreInErrorTasks) {
        Iterator<InternalJob> iterJob = jobs.iterator();
        while (iterJob.hasNext()) {
            InternalJob job = iterJob.next();

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

    protected void sleepSchedulingThread() throws InterruptedException {
        schedulingThread.sleepSchedulingThread();
    }

    protected void wakeUpSchedulingThread() {
        schedulingThread.wakeUpSchedulingThread();
    }

}
