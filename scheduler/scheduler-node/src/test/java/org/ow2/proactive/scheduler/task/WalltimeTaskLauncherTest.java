package org.ow2.proactive.scheduler.task;

import java.io.File;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.task.executors.ForkedTaskExecutor;
import org.ow2.proactive.scheduler.task.executors.TaskExecutor;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;

import static org.junit.Assert.*;


public class WalltimeTaskLauncherTest {

    @Test(timeout = 5000)
    public void walltime_forked_task() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
          new TaskScript(new SimpleScript("for(;;){}", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new ForkingTaskLauncherFactory());

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    @Test(timeout = 5000)
    public void walltime_during_task_execution() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
            new TaskScript(new SimpleScript("java.lang.Thread.sleep(10000)", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    @Test(timeout = 5000)
    public void walltime_during_file_copy() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
            new TaskScript(new SimpleScript("", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new SlowDataspacesTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    private TaskResult runTaskLauncher(TaskLauncher taskLauncher,
            ScriptExecutableContainer executableContainer) throws InterruptedException, ActiveObjectCreationException, NodeException {

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
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
            return new TestTaskLauncherFactory.TaskFileDataspaces();
        }

        @Override
        public TaskExecutor createTaskExecutor(File workingDir, Decrypter decrypter) {
            return new ForkedTaskExecutor(workingDir, decrypter);
        }

    }

}