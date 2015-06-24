/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.File;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.containers.ExecutableContainer;
import org.ow2.proactive.scheduler.task.containers.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scheduler.task.utils.TaskKiller;
import org.ow2.proactive.scheduler.task.utils.WallTimer;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


/**
 * The node side of task execution:
 * - communicates with the Scheduler via ProActive
 * - deals with data transfers
 * - deals with task killing and walltime
 * - sends result back to the Scheduler
 */
@ActiveObject
public class TaskLauncher {

    private static final Logger logger = Logger.getLogger(TaskLauncher.class);

    private TaskLauncherFactory factory;

    private TaskId taskId;
    private TaskLauncherInitializer initializer;

    private TaskLogger taskLogger;
    private TaskKiller taskKiller;
    private Decrypter decrypter;

    private ProgressFileReader progressFileReader;

    /**
     * Needed for ProActive but should never be used manually to create an instance of the object.
     */
    public TaskLauncher() {
    }

    public TaskLauncher(TaskLauncherInitializer initializer, TaskLauncherFactory factory) {
        this(initializer);
        this.factory = factory;
    }

    public TaskLauncher(TaskLauncherInitializer initializer) {
        this.initializer = initializer;
        this.taskId = initializer.getTaskId();
        this.taskLogger = new TaskLogger(taskId, getHostname());
        this.progressFileReader = new ProgressFileReader();
    }

    public void doTask(ExecutableContainer executableContainer, TaskResult[] previousTasksResults,
                       TaskTerminateNotification terminateNotification) {

        taskKiller = new TaskKiller(Thread.currentThread()); // what about kill of a non yet started task?
        WallTimer wallTimer = WallTimer.startWallTime(initializer.getWalltime(),
                new TaskKiller(Thread.currentThread()));

        Stopwatch stopwatchWhenTaskFailed = Stopwatch.createStarted();

        try {
            TaskDataspaces dataspaces = factory.createTaskDataspaces(taskId, initializer.getNamingService());

            File taskLogFile = taskLogger.createFileAppender(dataspaces.getScratchFolder());

            dataspaces.copyInputDataToScratch(initializer.getFilteredInputFiles(fileSelectorsFilters(initializer))); // should handle interrupt

            File workingDir = getTaskWorkingDir(executableContainer, dataspaces);

            progressFileReader.start(workingDir, taskId);

            TaskContext context = new TaskContext(executableContainer, initializer, previousTasksResults,
                    dataspaces.getScratchURI(), dataspaces.getInputURI(), dataspaces.getOutputURI(),
                    dataspaces.getUserURI(), dataspaces.getGlobalURI(), progressFileReader.getProgressFile().toString());

            if (decrypter != null) {
                decrypter.setCredentials(executableContainer.getCredentials());
                context.setDecrypter(decrypter);
            }

            TaskResultImpl taskResult = factory.createTaskExecutor(workingDir, decrypter).execute(context,
                    taskLogger.getOutputSink(), taskLogger.getErrorSink());

            if (wallTimer.hasWallTimed()) { // still needed?
                stopwatchWhenTaskFailed.stop();

                taskLogger.getErrorSink().println(
                        "Walltime of " + initializer.getWalltime() + " ms reached on task " +
                                taskId.getReadableName());
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, new WalltimeExceededException(
                        "Walltime of " + initializer.getWalltime() + " ms reached on task " +
                                taskId.getReadableName()), taskLogger.getLogs(), stopwatchWhenTaskFailed.elapsed(TimeUnit.MILLISECONDS));

                sendResultToScheduler(terminateNotification, failedTaskResult);
                return;

            } else if (taskKiller.wasKilled()) {
                stopwatchWhenTaskFailed.stop();

                taskLogger.getErrorSink().println("Task " + taskId.getReadableName() + " has been killed");
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, new TaskAbortedException(
                        "Task " + taskId.getReadableName() + " has been killed"), taskLogger.getLogs(),
                        stopwatchWhenTaskFailed.elapsed(TimeUnit.MILLISECONDS));

                sendResultToScheduler(terminateNotification, failedTaskResult);
                return;
            }

            dataspaces.copyScratchDataToOutput(initializer.getFilteredOutputFiles(fileSelectorsFilters(initializer)));

            copyTaskLogsToUserSpace(taskLogFile, dataspaces);
            FileUtils.deleteQuietly(taskLogFile);

            taskResult.setLogs(taskLogger.getLogs()); // should it be done when killed or walltimed?

            wallTimer.stop();

            sendResultToScheduler(terminateNotification, taskResult);
        } catch (Throwable taskFailure) {
            wallTimer.stop();

            TaskResultImpl failedTaskResult;
            stopwatchWhenTaskFailed.stop();

            if (wallTimer.hasWallTimed()) {
                logger.debug("Walltime reached for task", taskFailure);
                failedTaskResult = new TaskResultImpl(taskId, new WalltimeExceededException("Walltime of " +
                        initializer.getWalltime() + " ms reached on task " + taskId.getReadableName(),
                        taskFailure), taskLogger.getLogs(), stopwatchWhenTaskFailed.elapsed(TimeUnit.MILLISECONDS));

            } else if (taskKiller.wasKilled()) {
                logger.debug("Task has been killed", taskFailure);
                failedTaskResult = new TaskResultImpl(taskId, new TaskAbortedException("Task " +
                        taskId.getReadableName() + " has been killed"), taskLogger.getLogs(),
                        stopwatchWhenTaskFailed.elapsed(TimeUnit.MILLISECONDS));

            } else {
                logger.info("Failed to execute task", taskFailure);
                taskFailure.printStackTrace(taskLogger.getErrorSink());
                failedTaskResult = new TaskResultImpl(taskId, taskFailure, taskLogger.getLogs(),
                        stopwatchWhenTaskFailed.elapsed(TimeUnit.MILLISECONDS));
            }

            sendResultToScheduler(terminateNotification, failedTaskResult);
        } finally {
            taskLogger.close();
        }
    }

    private HashMap<String, Serializable> fileSelectorsFilters(TaskLauncherInitializer initializer) {
        HashMap<String, Serializable> replacements = new HashMap<>();

        replacements.put(SchedulerVars.PA_JOB_ID.toString(), initializer.getTaskId().getJobId().value());
        replacements.put(SchedulerVars.PA_JOB_NAME.toString(), initializer.getTaskId().getJobId().getReadableName());
        replacements.put(SchedulerVars.PA_TASK_ID.toString(), initializer.getTaskId().value());
        replacements.put(SchedulerVars.PA_TASK_NAME.toString(), initializer.getTaskId().getReadableName());
        replacements.put(SchedulerVars.PA_TASK_ITERATION.toString(), Integer.toString(initializer.getIterationIndex()));
        replacements.put(SchedulerVars.PA_TASK_REPLICATION.toString(), Integer.toString(initializer.getReplicationIndex()));

        return replacements;
    }

    private void copyTaskLogsToUserSpace(File taskLogFile, TaskDataspaces dataspaces) {
        if (initializer.isPreciousLogs()) {
            try {
                dataspaces.copyScratchDataToOutput(Collections.singletonList(new OutputSelector(
                        new FileSelector(taskLogFile.getName()), OutputAccessMode.TransferToUserSpace)));
            } catch (FileSystemException e) {
                logger.warn("Cannot copy logs of task to user data spaces", e);
            }
        }
    }

    private File getTaskWorkingDir(ExecutableContainer executableContainer, TaskDataspaces dataspaces) {
        File workingDir = dataspaces.getScratchFolder(); // hack for native working dir
        if (executableContainer instanceof ForkedScriptExecutableContainer) {
            String workingDirPath = ((ForkedScriptExecutableContainer) executableContainer).getWorkingDir();
            if (workingDirPath != null) {
                workingDir = new File(workingDirPath);
            }
        }
        return workingDir;
    }

    private void sendResultToScheduler(TaskTerminateNotification terminateNotification,
                                       TaskResultImpl taskResult) {
        int pingAttempts = initializer.getPingAttempts();
        int pingPeriodMs = initializer.getPingPeriod() * 1000;

        for (int i = 0; i < pingAttempts; i++) {
            try {
                terminateNotification.terminate(taskId, taskResult);
                logger.debug("Successfully notified task termination " + taskId);
                return;
            } catch (Throwable th) {
                logger.warn("Cannot notify task termination " + taskId + ", will try again in " +
                        pingPeriodMs + " ms", th);
                try {
                    Thread.sleep(pingPeriodMs);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting to notify task termination", e);
                }
            }
        }
        logger.error("Cannot notify task termination " + taskId + " after " + pingAttempts + " attempts, terminating task launcher now");
        terminate(true);
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

    public PublicKey generatePublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        decrypter = new Decrypter(keyPair.getPrivate()); // in constructor?
        return keyPair.getPublic();
    }

    @ImmediateService
    public void terminate(boolean normalTermination) {
        progressFileReader.stop();

        taskLogger.resetLogContextForImmediateService();

        if (normalTermination) {
            logger.debug("Terminate message received for task " + taskId);
        } else {
            logger.debug("Kill message received for task " + taskId);
            taskKiller.kill();
        }

        try {
            PAActiveObject.terminateActiveObject(!normalTermination);
        } catch (Exception e) {
            logger.info("Exception when terminating task launcher active object", e);
        }
        logger.info("Task terminated");
    }

    @ImmediateService
    public int getProgress() {
        return progressFileReader.getProgress();
    }

    private static String getHostname() {
        return ProActiveInet.getInstance().getInetAddress().getHostName();
    }

}
