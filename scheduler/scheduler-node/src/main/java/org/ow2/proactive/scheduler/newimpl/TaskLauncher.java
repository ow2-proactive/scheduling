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

import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskIdPojo;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
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
    private TaskLogger taskLogger;

    /** Needed for ProActive */
    public TaskLauncher() {
    }

    public TaskLauncher(TaskLauncherInitializer initializer) {
        this.initializer = initializer;
        this.taskId = TaskIdPojo.createTaskId(initializer.getTaskId());
        this.initializer.setTaskId(taskId);
        taskLogger = new TaskLogger(taskId, getHostname());
    }

    public TaskLauncher(TaskLauncherInitializer initializer, TaskLauncherFactory factory) {
        this(initializer);
        this.factory = factory;
    }

    // should be execute(executable)
    public void doTask(ExecutableContainer executableContainer, TaskResult[] previousTasksResults,
            TaskTerminateNotification terminateNotification) {

        try {

            // start wall time timer

            TaskDataspaces dataspaces = factory.createTaskDataspaces(taskId, initializer.getNamingService());

            dataspaces.copyInputDataToScratch(initializer.getTaskInputFiles()); // should handle interrupt

            TaskContext context = new TaskContext((ForkedScriptExecutableContainer) executableContainer,
                initializer);

            TaskResultImpl taskResult = factory.createTaskExecutor(dataspaces.getScratchFolder(), null).execute(context,
              taskLogger.getOutputSink(), taskLogger.getErrorSink()); // TOO decrypter

            dataspaces.copyScratchDataToOutput(initializer.getTaskOutputFiles());

            taskResult.setLogs(taskLogger.getLogs()); // TODO close

            sendResultToScheduler(terminateNotification, taskResult);
        } catch (Throwable throwable) {
            logger.warn("Failed to execute task", throwable);
            TaskResultImpl failedTaskResult = new TaskResultImpl(taskId, throwable, taskLogger.getLogs(), 0);
            sendResultToScheduler(terminateNotification, failedTaskResult);
        }
        // kill what's left

        // copy output files ? task log files?

    }

    private void sendResultToScheduler(TaskTerminateNotification terminateNotification,
      TaskResultImpl taskResult) {
        // TODO retries / wait
        terminateNotification.terminate(taskId, taskResult);
    }

    // take log support out and use composition
    public void activateLogs(AppenderProvider logSink) {
        taskLogger.activateLogs(logSink);
    }

    public void getStoredLogs(AppenderProvider logSink) {
        taskLogger.getStoredLogs(logSink);
    }

    public PublicKey generatePublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        //connect to the authentication interface and ask for new cred
        //        decrypter = new Decrypter(keyPair.getPrivate());
        return keyPair.getPublic();
    }

    public void terminate(boolean normalTermination) {
        // kill process for real

    }

    public int getProgress() {
        // not supported anymore
        return 0;
    }

    private String getHostname() {
        return ProActiveInet.getInstance().getInetAddress().getHostName();
    }
}
