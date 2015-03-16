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
package org.ow2.proactive.scheduler.newimpl;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.newimpl.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
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

    private TaskLauncherFactory factory = new TaskLauncherFactory();

    private TaskId taskId;
    private TaskLauncherInitializer initializer;

    private TaskLogger taskLogger;
    private TaskKiller taskKiller;
    private Decrypter decrypter;

    /** Needed for ProActive */
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
    }

    public void doTask(ExecutableContainer executableContainer, TaskResult[] previousTasksResults,
            TaskTerminateNotification terminateNotification) {

        taskKiller = new TaskKiller(Thread.currentThread()); // what about kill of a non yet started task?
        WallTimer wallTimer = new WallTimer(initializer.getWalltime(), taskKiller);

        StopWatch stopWatchWhenTaskFailed = new StopWatch();
        stopWatchWhenTaskFailed.start();

        try {
            TaskDataspaces dataspaces = factory.createTaskDataspaces(taskId, initializer.getNamingService());

            dataspaces.copyInputDataToScratch(initializer.getTaskInputFiles()); // should handle interrupt

            TaskContext context = new TaskContext(executableContainer, initializer, previousTasksResults,
                dataspaces.getScratchURI(), dataspaces.getInputURI(), dataspaces.getOutputURI(),
                dataspaces.getUserURI(), dataspaces.getGlobalURI());

            if (decrypter != null) {
                decrypter.setCredentials(executableContainer.getCredentials());
                context.setDecrypter(decrypter);
            }

            File workingDir = getTaskWorkingDir(executableContainer, dataspaces);

            TaskResultImpl taskResult = factory.createTaskExecutor(workingDir, decrypter).execute(context,
                    taskLogger.getOutputSink(), taskLogger.getErrorSink());

            if (wallTimer.hasWallTimed()) { // still needed?
                taskLogger.getErrorSink().println(
                        "Walltime of " + initializer.getWalltime() + " ms reached on task " +
                            taskId.getReadableName());
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, new WalltimeExceededException(
                    "Walltime of " + initializer.getWalltime() + " ms reached on task " +
                        taskId.getReadableName()), taskLogger.getLogs(), stopWatchWhenTaskFailed.stop());
                sendResultToScheduler(terminateNotification, failedTaskResult);
                return;

            } else if (taskKiller.wasKilled()) {
                taskLogger.getErrorSink().println("Task " + taskId.getReadableName() + " has been killed");
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, new TaskAbortedException(
                    "Task " + taskId.getReadableName() + " has been killed"), taskLogger.getLogs(),
                    stopWatchWhenTaskFailed.stop());
                sendResultToScheduler(terminateNotification, failedTaskResult);
                return;
            }

            dataspaces.copyScratchDataToOutput(initializer.getTaskOutputFiles());

            taskResult.setLogs(taskLogger.getLogs()); // should it be done when killed or walltimed?

            wallTimer.stop();
            sendResultToScheduler(terminateNotification, taskResult);

        } catch (Throwable taskFailure) {
            wallTimer.stop();

            TaskResultImpl failedTaskResult;

            if (wallTimer.hasWallTimed()) {
                logger.debug("Walltime reached for task", taskFailure);
                failedTaskResult = new TaskResultImpl(taskId, new WalltimeExceededException("Walltime of " +
                    initializer.getWalltime() + " ms reached on task " + taskId.getReadableName(),
                    taskFailure), taskLogger.getLogs(), stopWatchWhenTaskFailed.stop());

            } else if (taskKiller.wasKilled()) {
                logger.debug("Task has been killed", taskFailure);
                failedTaskResult = new TaskResultImpl(taskId, new TaskAbortedException("Task " +
                    taskId.getReadableName() + " has been killed"), taskLogger.getLogs(),
                    stopWatchWhenTaskFailed.stop());

            } else {
                logger.info("Failed to execute task", taskFailure);// TODO level?
                taskFailure.printStackTrace(taskLogger.getErrorSink());
                failedTaskResult = new TaskResultImpl(taskId, taskFailure, taskLogger.getLogs(),
                    stopWatchWhenTaskFailed.stop());
            }

            sendResultToScheduler(terminateNotification, failedTaskResult);
        } finally {
            taskLogger.close();
        }
        // FIXME finally kill what's left

        // FIXME copy task log files?
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
        logger.error("Cannot notify task termination " + taskId + " after " + pingAttempts + " attempts");
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
        taskLogger.resetLogContextForImmediateService();
        if (normalTermination) {
            logger.info("Terminate message received for task " + taskId);
        } else {
            logger.info("Kill message received for task " + taskId);
            taskKiller.kill();
        }

        try {
            PAActiveObject.terminateActiveObject(!normalTermination);
        } catch (Exception e) {
            logger.info("Exception when terminating task launcher active object", e);
        }
        logger.info("TaskLauncher terminated");
    }

    public int getProgress() {
        // not supported anymore, still needed for ping purpose
        return 0;
    }

    private static String getHostname() {
        return ProActiveInet.getInstance().getInetAddress().getHostName();
    }
}
