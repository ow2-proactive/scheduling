package org.ow2.proactive.scheduler.newimpl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;


public class TaskLauncherTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void emptyConstructorForProActiveExists() throws Exception {
        new TaskLauncher();
    }

    @Test
    public void simpleTask() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setPreScript(new SimpleScript("print('pre')", "javascript"));
        initializer.setPostScript(new SimpleScript("print('post')", "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskTerminateNotificationVerifier taskResult = new TaskTerminateNotificationVerifier();

        TaskLauncherFactory factory = new TestTaskLauncherFactory();
        new TaskLauncher(initializer, factory).doTask(executableContainer, null, taskResult);

        assertEquals("hello", taskResult.result.value());
        assertEquals("pre\nhello\npost\n", taskResult.result.getOutput().getAllLogs(false));
    }

    @Test
    public void failedTask() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("failing task'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskTerminateNotificationVerifier taskResult = new TaskTerminateNotificationVerifier();

        TaskLauncherFactory factory = new TestTaskLauncherFactory();
        new TaskLauncher(initializer, factory).doTask(executableContainer, null, taskResult);

        assertNotNull(taskResult.result.getException());
        assertNotEquals("", taskResult.result.getOutput().getStderrLogs(false));
    }

    private static class TaskTerminateNotificationVerifier implements TaskTerminateNotification {
        TaskResult result;

        @Override
        public void terminate(TaskId taskId, TaskResult taskResult) {
            this.result = taskResult;
        }
    }

    private class TestTaskLauncherFactory extends TaskLauncherFactory {
        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
            return new TaskDataspaces(){

                @Override
                public File getScratchFolder() {
                    try {
                        return tmpFolder.newFolder();
                    } catch (IOException e){
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void copyInputDataToScratch(List<InputSelector> inputFiles) throws FileSystemException {

                }

                @Override
                public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {

                }
            };
        }

        @Override
        public TaskExecutor createTaskExecutor(File workingDir, Decrypter decrypter) {
            return new NonForkedTaskExecutor();
        }
    }
}