package org.ow2.proactive.scheduler.newimpl;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.newimpl.data.TaskDataspaces;
import org.ow2.proactive.scheduler.newimpl.utils.Decrypter;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class WalltimeTaskLauncherTest {

    @Test(timeout = 5000)
    public void walltime_forked_task() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("for(;;){}", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskLauncher taskLauncher = new TaskLauncher(initializer, new ForkingTaskLauncherFactory());

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    @Test(timeout = 5000)
    public void walltime_during_task_execution() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript("java.lang.Thread.sleep(10000)", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory());

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    @Test(timeout = 5000)
    public void walltime_during_file_copy() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript("", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setWalltime(500);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskLauncher taskLauncher = new TaskLauncher(initializer, new SlowDataspacesTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(WalltimeExceededException.class, taskResult.getException().getClass());
    }

    private TaskResult runTaskLauncher(TaskLauncher taskLauncher,
            ForkedScriptExecutableContainer executableContainer) throws InterruptedException, ActiveObjectCreationException, NodeException {

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

    private class TestTaskLauncherFactory implements TaskLauncherFactory {
        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
            return new TaskFileDataspaces();
        }

        public TaskExecutor createTaskExecutor(File workingDir, Decrypter decrypter) {
            return new NonForkedTaskExecutor();
        }

    }

    private class ForkingTaskLauncherFactory implements TaskLauncherFactory {
        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
            return new TaskFileDataspaces();
        }

        public TaskExecutor createTaskExecutor(File workingDir, Decrypter decrypter) {
            return new DockerForkerTaskExecutor(workingDir, decrypter);
        }

    }

    private class SlowDataspacesTaskLauncherFactory implements TaskLauncherFactory {
        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
            return new SlowDataspaces();
        }

        public TaskExecutor createTaskExecutor(File workingDir, Decrypter decrypter) {
            return new NonForkedTaskExecutor();
        }

    }

    private class TaskFileDataspaces implements TaskDataspaces {

        @Override
        public File getScratchFolder() {
            return new File(".");
        }

        @Override
        public String getScratchURI() {
            return null;
        }

        @Override
        public String getInputURI() {
            return null;
        }

        @Override
        public String getOutputURI() {
            return null;
        }

        @Override
        public String getUserURI() {
            return null;
        }

        @Override
        public String getGlobalURI() {
            return null;
        }

        @Override
        public void copyInputDataToScratch(List<InputSelector> inputFiles) throws FileSystemException {

        }

        @Override
        public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {

        }
    }

    private class SlowDataspaces implements TaskDataspaces {

        @Override
        public File getScratchFolder() {
            return new File(".");
        }

        @Override
        public String getScratchURI() {
            return null;
        }

        @Override
        public String getInputURI() {
            return null;
        }

        @Override
        public String getOutputURI() {
            return null;
        }

        @Override
        public String getUserURI() {
            return null;
        }

        @Override
        public String getGlobalURI() {
            return null;
        }

        @Override
        public void copyInputDataToScratch(List<InputSelector> inputFiles) throws FileSystemException {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {

        }
    }
}