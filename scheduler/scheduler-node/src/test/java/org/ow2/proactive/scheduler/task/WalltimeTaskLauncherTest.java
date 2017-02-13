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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.executors.ForkedTaskExecutor;
import org.ow2.proactive.scheduler.task.executors.TaskExecutor;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


public class WalltimeTaskLauncherTest {

    @Test(timeout = 5000)
    public void walltime_forked_task() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(new SimpleScript("for(;;){}",
                                                                                                                      "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new ForkingTaskLauncherFactory());

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    @Test(timeout = 5000)
    public void walltime_during_task_execution() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(new SimpleScript("java.lang.Thread.sleep(10000)",
                                                                                                                      "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    @Test(timeout = 5000)
    public void walltime_during_file_copy() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(new SimpleScript("",
                                                                                                                      "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new SlowDataspacesTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    private TaskResult runTaskLauncher(TaskLauncher taskLauncher, ScriptExecutableContainer executableContainer)
            throws InterruptedException, ActiveObjectCreationException, NodeException {

        TaskTerminateNotificationVerifier taskResult = new TaskTerminateNotificationVerifier();
        taskLauncher.doTask(executableContainer, null, taskResult);

        return taskResult.result;
    }

    private static class TaskTerminateNotificationVerifier implements TaskTerminateNotification {
        TaskResult result;

        @Override
        public void terminate(TaskId taskId, TaskResult taskResult) {
            this.result = taskResult;
        }
    }

    private class ForkingTaskLauncherFactory extends ProActiveForkedTaskLauncherFactory {
        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService, boolean isRunAsUser) {
            return new TestTaskLauncherFactory.TaskFileDataspaces();
        }

        @Override
        public TaskExecutor createTaskExecutor(File workingDir) {
            return new ForkedTaskExecutor(workingDir);
        }

    }

}
