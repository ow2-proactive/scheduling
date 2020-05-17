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
package org.ow2.proactive.scheduler.task;

import java.io.File;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;
import org.ow2.proactive.resourcemanager.utils.RMNodeStarter;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.common.util.TaskLoggerRelativePathGenerator;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.context.NodeDataSpacesURIs;
import org.ow2.proactive.scheduler.task.context.NodeInfo;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.executors.TaskExecutor;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scheduler.task.utils.WallTimer;
import org.ow2.proactive.scheduler.task.utils.task.termination.CleanupTimeoutGetterDoubleValue;
import org.ow2.proactive.scheduler.task.utils.task.termination.TaskKiller;

import com.google.common.base.Stopwatch;


/**
 * The node side of task execution:
 * - communicates with the Scheduler via ProActive
 * - deals with data transfers
 * - deals with task killing and walltime
 * - sends result back to the Scheduler
 */
@ActiveObject
public class TaskLauncher implements InitActive {

    static final String PROGRESS_FILE_READER_PERIOD = "progress.file.reader.period";

    static final String PROGRESS_FILE_READER_OLD = "progress.file.reader.old";

    private static final Logger logger = Logger.getLogger(TaskLauncher.class);

    final private TaskContextVariableExtractor taskContextVariableExtractor = new TaskContextVariableExtractor();

    private TaskLauncherFactory factory;

    private TaskId taskId;

    private TaskLauncherInitializer initializer;

    private TaskLogger taskLogger;

    private TaskKiller taskKiller;

    private Decrypter decrypter;

    private ProgressFileReaderInterface progressFileReader;

    private Thread nodeShutdownHook;

    private TaskLauncherRebinder taskLauncherRebinder;

    private AtomicBoolean taskStarted = new AtomicBoolean(false);

    /**
     * Needed for ProActive but should never be used manually to create an instance of the object.
     */
    public TaskLauncher() {
        // Needed for ProActive but should never be used manually to create an instance of the object.
    }

    public TaskLauncher(TaskLauncherInitializer initializer, TaskLauncherFactory factory) {
        this(initializer);
        this.factory = factory;
    }

    public TaskLauncher(TaskLauncherInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void initActivity(Body body) {
        this.taskId = initializer.getTaskId();
        this.taskLogger = new TaskLogger(taskId, getHostName());
        if (Boolean.valueOf(System.getProperty(PROGRESS_FILE_READER_OLD))) {
            this.progressFileReader = new ProgressFileReader();
        } else {
            this.progressFileReader = new ProgressFileReaderPoller();
        }
        this.taskKiller = new TaskKiller(Thread.currentThread(), new CleanupTimeoutGetterDoubleValue());
        nodeShutdownHook = new Thread(this::kill);
    }

    /**
     * Method used to wait until the TaskLauncher is activated (i.e. the initActivity method has been run).
     *
     * @return dummy boolean value
     */
    public boolean isActivated() {
        return true;
    }

    @ImmediateService
    public boolean isTaskStarted() {
        return taskStarted.get();
    }

    void doTask(ExecutableContainer executableContainer, TaskResult[] previousTasksResults,
            TaskTerminateNotification terminateNotification) {
        doTask(executableContainer, previousTasksResults, terminateNotification, null, false);
    }

    public void doTask(ExecutableContainer executableContainer, TaskResult[] previousTasksResults,
            TaskTerminateNotification terminateNotification, String terminateNotificationNodeURL,
            boolean taskRecoverable) {

        TaskResultImpl taskResult;
        WallTimer wallTimer = null;
        TaskContext context = null;
        Stopwatch taskStopwatchForFailures = null;
        TaskDataspaces dataspaces = null;
        File taskLogFile = null;

        try {
            taskStarted.set(true);

            logger.info("Task started " + taskId.getJobId().getReadableName() + " : " + taskId.getReadableName());

            wallTimer = new WallTimer(initializer.getWalltime(), taskKiller);

            taskStopwatchForFailures = Stopwatch.createUnstarted();

            taskLauncherRebinder = new TaskLauncherRebinder(taskId, terminateNotificationNodeURL, taskRecoverable);

            addShutdownHook();
            // lock the cache space cleaning mechanism
            DataSpaceNodeConfigurationAgent.lockCacheSpaceCleaning();
            dataspaces = factory.createTaskDataspaces(taskId,
                                                      initializer.getNamingService(),
                                                      executableContainer.isRunAsUser(),
                                                      taskLogger);

            copyTaskLogsFromUserSpace(taskLogger.createLogFilePath(dataspaces.getScratchFolder()), dataspaces);

            taskLogFile = taskLogger.createFileAppender(dataspaces.getScratchFolder());

            progressFileReader.start(dataspaces.getScratchFolder(), taskId);

            context = new TaskContext(executableContainer,
                                      initializer,
                                      previousTasksResults,
                                      new NodeDataSpacesURIs(dataspaces.getScratchURI(),
                                                             dataspaces.getCacheURI(),
                                                             dataspaces.getInputURI(),
                                                             dataspaces.getOutputURI(),
                                                             dataspaces.getUserURI(),
                                                             dataspaces.getGlobalURI()),
                                      progressFileReader.getProgressFile().toString(),
                                      new NodeInfo(getHostName(), getNodeUrl(), getNodeName(), getNodeSourceName()),
                                      decrypter);

            File workingDir = getTaskWorkingDir(context, dataspaces);

            logger.info("Task working dir: " + workingDir);
            logger.info("Cache space: " + context.getNodeDataSpaceURIs().getCacheURI());
            logger.info("Input space: " + context.getNodeDataSpaceURIs().getInputURI());
            logger.info("Output space: " + context.getNodeDataSpaceURIs().getOutputURI());
            logger.info("User space: " + context.getNodeDataSpaceURIs().getUserURI());
            logger.info("Global space: " + context.getNodeDataSpaceURIs().getGlobalURI());
            logger.info("Scheduler rest url: " + context.getSchedulerRestUrl());

            wallTimer.start();

            dataspaces.copyInputDataToScratch(initializer.getFilteredInputFiles(fileSelectorsFilters(context))); // should handle interrupt

            if (decrypter != null) {
                decrypter.setCredentials(executableContainer.getCredentials());
            }

            TaskExecutor taskExecutor = factory.createTaskExecutor(workingDir);

            taskStopwatchForFailures.start();
            taskResult = taskExecutor.execute(context, taskLogger.getOutputSink(), taskLogger.getErrorSink());
            taskStopwatchForFailures.stop();

            // by the time the task finishes, the scheduler might have had a
            // transient failure, so we need to make sure that the placeholder
            // for the task's result still exists, or get the new place for
            // the result if it does not exist anymore.
            TaskTerminateNotification rebindedTerminateNotification = taskLauncherRebinder.makeSureSchedulerIsConnected(terminateNotification);

            switch (taskKiller.getStatus()) {
                case WALLTIME_REACHED:
                    taskResult = getWalltimedTaskResult(context, taskStopwatchForFailures);
                    copyTaskLogsToUserSpace(taskLogFile, dataspaces);
                    sendResultToScheduler(rebindedTerminateNotification, taskResult);
                    return;
                case KILLED_MANUALLY:
                    // killed by Scheduler, no need to send results back
                    copyTaskLogsToUserSpace(taskLogFile, dataspaces);
                    return;
            }

            dataspaces.copyScratchDataToOutput(initializer.getFilteredOutputFiles(fileSelectorsFilters(context,
                                                                                                       taskResult)));

            wallTimer.stop();

            copyTaskLogsToUserSpace(taskLogFile, dataspaces);

            sendResultToScheduler(rebindedTerminateNotification, taskResult);
        } catch (Throwable taskFailure) {
            if (wallTimer != null) {
                wallTimer.stop();
            }

            switch (taskKiller.getStatus()) {
                case WALLTIME_REACHED:
                    taskResult = getWalltimedTaskResult(context, taskStopwatchForFailures);
                    copyTaskLogsToUserSpace(taskLogFile, dataspaces);
                    sendResultToScheduler(terminateNotification, taskResult);
                    break;
                case KILLED_MANUALLY:
                    // killed by Scheduler, no need to send results back
                    copyTaskLogsToUserSpace(taskLogFile, dataspaces);
                    return;
                default:
                    logger.info("Failed to execute task", taskFailure);

                    long elapsedTime = 0;
                    if (taskStopwatchForFailures != null) {
                        elapsedTime = taskStopwatchForFailures.elapsed(TimeUnit.MILLISECONDS);
                    }

                    taskFailure.printStackTrace(taskLogger.getErrorSink());
                    Map<String, byte[]> serializedVariables = extractVariablesFromContext(context);
                    taskResult = new TaskResultImpl(taskId, taskFailure, taskLogger.getLogs(), elapsedTime);
                    taskResult.setPropagatedVariables(serializedVariables);

                    sendResultToScheduler(terminateNotification, taskResult);
            }
        } finally {
            try {
                progressFileReader.stop();
                taskLogger.close();

                if (dataspaces != null) {
                    dataspaces.close();
                }
                // unlocks the cache space cleaning thread
                DataSpaceNodeConfigurationAgent.unlockCacheSpaceCleaning();
                removeShutdownHook();
            } finally {
                terminate();
            }
        }
    }

    private Map<String, byte[]> extractVariablesFromContext(TaskContext context) {
        if (context != null) {
            try {
                return SerializationUtil.serializeVariableMap(taskContextVariableExtractor.getAllNonTaskVariables(context));
            } catch (Exception e) {
                e.printStackTrace(taskLogger.getErrorSink());
            }
        }
        return null;
    }

    private void addShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(nodeShutdownHook);
        } catch (IllegalStateException ignored) {
            // ignore
        }
    }

    private void removeShutdownHook() {
        try {
            Runtime.getRuntime().removeShutdownHook(nodeShutdownHook);
        } catch (IllegalStateException ignored) {
            // ignored
        }
    }

    private TaskResultImpl getWalltimedTaskResult(TaskContext context, Stopwatch taskStopwatchForFailures) {
        String message = "Walltime of " + initializer.getWalltime() + " ms reached on task " + taskId.getReadableName();

        return getTaskResult(context, taskStopwatchForFailures, new WalltimeExceededException(message));
    }

    private TaskResultImpl getTaskResult(TaskContext context, Stopwatch taskStopwatchForFailures,
            SchedulerException exception) {
        taskLogger.getErrorSink().println(exception.getMessage());

        Map<String, byte[]> serializedVariables = extractVariablesFromContext(context);

        long elapsedTime = 0;
        if (taskStopwatchForFailures != null) {
            elapsedTime = taskStopwatchForFailures.elapsed(TimeUnit.MILLISECONDS);
        }
        TaskResultImpl result = new TaskResultImpl(taskId, exception, taskLogger.getLogs(), elapsedTime);

        result.setPropagatedVariables(serializedVariables);
        return result;
    }

    private Map<String, Serializable> fileSelectorsFilters(TaskContext taskContext, TaskResult taskResult)
            throws Exception {
        return taskContextVariableExtractor.getAllVariablesWithTaskResult(taskContext, taskResult);
    }

    private Map<String, Serializable> fileSelectorsFilters(TaskContext taskContext) throws Exception {
        return taskContextVariableExtractor.getAllVariables(taskContext);
    }

    private void copyTaskLogsFromUserSpace(File taskLogFile, TaskDataspaces dataspaces) {
        if (initializer.isPreciousLogs()) {
            try {
                FileSelector taskLogFileSelector = new FileSelector(taskLogFile.getName());
                taskLogFileSelector.setIncludes(new TaskLoggerRelativePathGenerator(taskId).getRelativePath());
                dataspaces.copyInputDataToScratch(Collections.singletonList(new InputSelector(taskLogFileSelector,
                                                                                              InputAccessMode.TransferFromUserSpace)));
            } catch (Exception e) {
                logger.warn("Cannot copy logs of task to user data spaces", e);
            }
        }
    }

    private void copyTaskLogsToUserSpace(File taskLogFile, TaskDataspaces dataspaces) {
        if (initializer.isPreciousLogs() && taskLogFile != null) {
            try {
                FileSelector taskLogFileSelector = new FileSelector(taskLogFile.getName());
                taskLogFileSelector.setIncludes(new TaskLoggerRelativePathGenerator(taskId).getRelativePath());
                dataspaces.copyScratchDataToOutput(Collections.singletonList(new OutputSelector(taskLogFileSelector,
                                                                                                OutputAccessMode.TransferToUserSpace)));
            } catch (FileSystemException e) {
                logger.warn("Cannot copy logs of task to user data spaces", e);
            }
        }
    }

    private File getTaskWorkingDir(TaskContext taskContext, TaskDataspaces dataspaces) throws Exception {
        File workingDir = dataspaces.getScratchFolder();
        if (taskContext.getInitializer().getForkEnvironment() != null) {
            String workingDirPath = taskContext.getInitializer().getForkEnvironment().getWorkingDir();
            if (workingDirPath != null) {
                workingDirPath = VariableSubstitutor.filterAndUpdate(workingDirPath,
                                                                     taskContextVariableExtractor.getAllVariables(taskContext));
                workingDir = new File(workingDirPath);
            }
        }
        return workingDir;
    }

    private void sendResultToScheduler(TaskTerminateNotification terminateNotification, TaskResultImpl taskResult) {
        if (isNodeShuttingDown()) {
            return;
        }
        int pingAttempts = initializer.getPingAttempts();
        int pingPeriodMs = initializer.getPingPeriod() * 1000;
        taskLogger.close();
        taskResult.setLogs(taskLogger.getLogs());

        // We are going to contact the recipient of the task result. This
        // recipient is the TaskTerminateNotification, an active object on the
        // scheduler side. If the scheduler experienced a transient failure
        // while the task was computing, then the reference to this
        // TaskTerminateNotification is obsolete and we need to update it. This
        // is what the following code does.
        TaskTerminateNotification currentTerminateNotification = terminateNotification;
        for (int i = 0; i < pingAttempts; i++) {
            try {
                currentTerminateNotification.terminate(taskId, taskResult);
                logger.debug("Successfully notified task termination " + taskId);
                // termination has succeeded, exit the method
                return;
            } catch (Throwable t) {
                logger.warn("Cannot notify task termination, trying to rebind to the task termination handler");
                TaskTerminateNotification rebindedTerminateNotification = taskLauncherRebinder.getReboundTaskTerminateNotificationHandler(t);
                if (rebindedTerminateNotification != null) {
                    currentTerminateNotification = rebindedTerminateNotification;
                } else {
                    decreasePingAttemptsAndWait(pingAttempts, pingPeriodMs, i, t);
                }
            }
        }

        logger.error("Cannot notify task termination " + taskId + " after " + pingAttempts +
                     " attempts, terminating task launcher now");
    }

    private void decreasePingAttemptsAndWait(int pingAttempts, int pingPeriodMs, int i, Throwable t) {
        logger.warn("Cannot notify task termination " + taskId + ", will try again in " + pingPeriodMs + " ms", t);

        if (i != pingAttempts - 1) {
            try {
                Thread.sleep(pingPeriodMs);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting to notify task termination", e);
            }
        }
    }

    private boolean isNodeShuttingDown() {
        Thread shutdownHookThead = new Thread();
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHookThead);
        } catch (IllegalStateException e) {
            return true;
        }
        Runtime.getRuntime().removeShutdownHook(shutdownHookThead);
        return false;
    }

    @ImmediateService
    public void activateLogs(AppenderProvider logSink) {
        taskLogger.resetLogContextForImmediateService();
        taskLogger.activateLogs(logSink);
    }

    @ImmediateService
    public void getStoredLogs(AppenderProvider logSink) {
        taskLogger.resetLogContextForImmediateService();
        taskLogger.getStoredLogs(logSink);
    }

    public KeyPair getKeyPair() throws NoSuchAlgorithmException {
        return RMNodeStarter.getKeyPair();
    }

    public PublicKey generatePublicKey() throws NoSuchAlgorithmException {
        final KeyPair keyPair = getKeyPair();
        decrypter = new Decrypter(keyPair.getPrivate());
        return keyPair.getPublic();
    }

    @ImmediateService
    public void kill() {
        taskLogger.resetLogContextForImmediateService();
        logger.info("Kill received for task");
        taskKiller.kill(TaskKiller.Status.KILLED_MANUALLY);
    }

    private void terminate() {
        try {
            if (PAActiveObject.isInActiveObject()) {
                PAActiveObject.terminateActiveObject(false);
            }
        } catch (Exception e) {
            logger.info("Exception when terminating task launcher active object", e);
        }
        logger.info("Task terminated");
    }

    @ImmediateService
    public int getProgress() {
        return progressFileReader.getProgress();
    }

    private static String getHostName() {
        return ProActiveInet.getInstance().getInetAddress().getHostName();
    }

    private String getNodeUrl() {
        try {
            return PAActiveObject.getNode().getNodeInformation().getURL();
        } catch (Exception e) {
            logger.debug("Failed to acquire task launcher node information", e);
            return null;
        }
    }

    private String getNodeName() {
        try {
            return PAActiveObject.getNode().getNodeInformation().getName();
        } catch (Exception e) {
            logger.debug("Failed to acquire task launcher node information", e);
            return null;
        }
    }

    private String getNodeSourceName() {
        try {
            return System.getProperty("proactive.node.nodesource", "Default");
        } catch (Exception e) {
            logger.debug("Failed to acquire task launcher node source information", e);
            return null;
        }
    }

}
