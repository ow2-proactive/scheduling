package org.ow2.proactive.scheduler.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.util.Object2ByteConverter;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


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

        TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals("hello", taskResult.value());
        assertEquals("prehellopost\n", taskResult.getOutput().getAllLogs(false));
    }

    @Test
    public void javaTask() throws Throwable {
        HashMap<String, byte[]> args = new HashMap<String, byte[]>();
        args.put("number", Object2ByteConverter.convertObject2Byte(123));
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript(WaitAndPrint.class.getName(), "java", new Serializable[]{
            args
          })));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job*1", 1000L, false));

        TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertNotEquals("", taskResult.value());
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("123"));
    }

    @Test
    public void failedTask() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("failing task'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertNotNull(taskResult.getException());
        assertNotEquals("", taskResult.getOutput().getStderrLogs(false));
    }

    @Test
    public void thirdPartyCredentials() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("print(credentials.get('password'))", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory());

        CredData credData = new CredData("john", "pwd");
        credData.addThirdPartyCredential("password", "r00t");
        Credentials thirdPartyCredentials = Credentials.createCredentials(credData,
          taskLauncher.generatePublicKey());
        executableContainer.setCredentials(thirdPartyCredentials);

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals("r00t\n", taskResult.getOutput().getAllLogs(false));
    }

    @Test
    public void nativeTask_Working_Dir() throws Throwable {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("pwd", "native")), "/tmp");

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals("/tmp\n", taskResult.getOutput().getAllLogs(false));
    }

    @Test
    public void taskLogsAreCopiedToUserSpace() throws Exception {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setPreciousLogs(true);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        final TaskDataspaces dataspacesMock = mock(TaskDataspaces.class);
        TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory(){

            @Override
            public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
                return dataspacesMock;
            }
        });
        runTaskLauncher(taskLauncher, executableContainer);

        verify(dataspacesMock, times(2)).copyScratchDataToOutput(Matchers.<List<OutputSelector>>any());
    }

    @Test
    public void taskLogsAreNotCopiedToUserSpace_PreciousLogsDisabled() throws Exception {
        ForkedScriptExecutableContainer executableContainer = new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setPreciousLogs(false);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        final TaskDataspaces dataspacesMock = mock(TaskDataspaces.class);
        TaskLauncher taskLauncher = new TaskLauncher(initializer, new TestTaskLauncherFactory(){

            @Override
            public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
                return dataspacesMock;
            }
        });
        runTaskLauncher(taskLauncher, executableContainer);

        verify(dataspacesMock, times(1)).copyScratchDataToOutput(Matchers.<List<OutputSelector>>any());
    }

    private TaskResult runTaskLauncher(TaskLauncher taskLauncher, ForkedScriptExecutableContainer executableContainer) {
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

}