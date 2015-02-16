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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.newimpl;

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
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.apache.log4j.Logger;


/**
 *
 * @author The ProActive Team
 */
@ActiveObject
public class TaskLauncher {

    private static final Logger logger = Logger.getLogger(TaskLauncher.class);

    /** For tests */
    private TaskLauncherFactory factory = new TaskLauncherFactory();

    private TaskId taskId;
    private TaskLauncherInitializer initializer;

    private int pingAttempts;
    private int pingPeriodMs;

    private TaskLogger taskLogger;

    private Decrypter decrypter;

    private TaskKiller taskKiller;

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
        this.initializer.setTaskId(taskId);

        this.pingAttempts = initializer.getPingAttempts();
        this.pingPeriodMs = initializer.getPingPeriod() * 1000;

        taskLogger = new TaskLogger(taskId, getHostname());
    }

    // should be execute(executable)
    public void doTask(ExecutableContainer executableContainer, TaskResult[] previousTasksResults,
            TaskTerminateNotification terminateNotification) {

        taskKiller = new TaskKiller(Thread.currentThread()); // what about kill of a non yet started task?
        WallTimer wallTimer = new WallTimer(initializer.getWalltime(), Thread.currentThread()); // stop when finished

        StopWatch stopWatch = new StopWatch(); // for task failure cases
        stopWatch.start();

        try {
            TaskDataspaces dataspaces = factory.createTaskDataspaces(taskId, initializer.getNamingService());

            dataspaces.copyInputDataToScratch(initializer.getTaskInputFiles()); // should handle interrupt

            TaskContext context = new TaskContext(executableContainer,
                initializer, previousTasksResults);

            if (decrypter != null) {
                decrypter.setCredentials(executableContainer.getCredentials());
                // TODO serialize private key + encrypted data
                context.setDecrypter(decrypter);
            }

            TaskResultImpl taskResult = factory.createTaskExecutor(dataspaces.getScratchFolder(), decrypter)
                    .execute(context, taskLogger.getOutputSink(), taskLogger.getErrorSink()); // TODO decrypter

            if (wallTimer.hasWallTimed()) {
                taskLogger.getErrorSink().println(
                        "Walltime of " + initializer.getWalltime() + " ms reached on task " +
                            taskId.getReadableName());
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, new WalltimeExceededException(
                    "Walltime of " + initializer.getWalltime() + " ms reached on task " +
                        taskId.getReadableName()), taskLogger.getLogs(), stopWatch.stop());
                sendResultToScheduler(terminateNotification, failedTaskResult);
                return;
            } else if (taskKiller.wasKilled()) {
                taskLogger.getErrorSink().println("Task " + taskId.getReadableName() + " has been killed");
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, new TaskAbortedException(
                    "Task " + taskId.getReadableName() + " has been killed"), taskLogger.getLogs(), stopWatch.stop());
                sendResultToScheduler(terminateNotification, failedTaskResult);
                return;
            }

            dataspaces.copyScratchDataToOutput(initializer.getTaskOutputFiles());

            taskResult.setLogs(taskLogger.getLogs());

            logger.fatal("@@@" + taskLogger.getLogs().getStderrLogs(false));

            sendResultToScheduler(terminateNotification, taskResult);

        } catch (Throwable throwable) {
            if (wallTimer.hasWallTimed()) {
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, new WalltimeExceededException(
                    "Walltime of " + initializer.getWalltime() + " ms reached on task " +
                        taskId.getReadableName(), throwable), taskLogger.getLogs(), stopWatch.stop());
                sendResultToScheduler(terminateNotification, failedTaskResult);
            } else if (taskKiller.wasKilled()) {
                taskLogger.getErrorSink().println("Task " + taskId.getReadableName() + " has been killed");
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, new TaskAbortedException(
                    "Task " + taskId.getReadableName() + " has been killed"), taskLogger.getLogs(), stopWatch.stop());
                sendResultToScheduler(terminateNotification, failedTaskResult);
            } else {
                throwable.printStackTrace(taskLogger.getErrorSink());
                logger.warn("Failed to execute task", throwable);
                TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, throwable, taskLogger.getLogs(),
                  stopWatch.stop());
                sendResultToScheduler(terminateNotification, failedTaskResult);
            }
        } finally {
            wallTimer.stop();
            taskLogger.close();
        }
        // finally kill what's left

        // copy output files ? task log files?

        //try {
        //    PAActiveObject.terminateActiveObject(!normalTermination);
       // } catch (Exception e) {
        //    logger.info("Exception when terminating task launcher active object", e);
        //}
    }

    private void sendResultToScheduler(TaskTerminateNotification terminateNotification,
            TaskResultImpl taskResult) {
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
        // The task killer will interrupt the current thread
        taskLogger.resetLogContextForImmediateService();
        if (normalTermination) {
            logger.info("Terminate message received for task " + taskId);
        } else {
            logger.info("Kill message received for task " + taskId);
            taskKiller.kill();
        }

        logger.info("TaskLauncher terminated");
    }


    public int getProgress() {
        // not supported anymore
        return 0;
    }

    private String getHostname() {
        return ProActiveInet.getInstance().getInetAddress().getHostName();
    }
}
