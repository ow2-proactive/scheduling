package org.ow2.proactive.scheduler.task;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.util.Object2ByteConverter;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;


public class TaskLauncherTest {
	
	private final static String HEADLESS_JAVA_LOG = "Picked up _JAVA_OPTIONS: -Djava.awt.headless=true";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void emptyConstructorForProActiveExists() throws Exception {
        new TaskLauncher();
    }

    @Test
    public void simpleTask() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript("print('hello'); result='hello'", "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setPreScript(new SimpleScript("print('pre')", "groovy"));
        initializer.setPostScript(new SimpleScript("print('post')", "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertThat((String)taskResult.value(), is("hello"));
        assertThat(taskResult.getOutput().getAllLogs(false).contains(String.format("prehellopost%n")), is(true));
    }

    @Test
    public void javaTask() throws Throwable {
        HashMap<String, byte[]> args = new HashMap<>();
        args.put("number", Object2ByteConverter.convertObject2Byte(123));
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
          new TaskScript(new SimpleScript(WaitAndPrint.class.getName(), "java", new Serializable[]{
            args
          })));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job*1", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);
        
        assertThat((String)taskResult.value(), is(not("")));
        assertThat(taskResult.getOutput().getAllLogs(false).contains("123"), is(true));
    }

    @Test
    public void failedTask() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
          new TaskScript(new SimpleScript("failing task'", "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertNotNull(taskResult.getException());
        assertNotEquals("", taskResult.getOutput().getStderrLogs(false).replace(HEADLESS_JAVA_LOG,""));
    }

    @Test
    public void thirdPartyCredentials() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
          new TaskScript(new SimpleScript("print(credentials.get('password'))", "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());

        CredData credData = new CredData("john", "pwd");
        credData.addThirdPartyCredential("password", "r00t");
        Credentials thirdPartyCredentials =
                Credentials.createCredentials(credData, taskLauncher.generatePublicKey());
        executableContainer.setCredentials(thirdPartyCredentials);

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        
        assertThat(taskResult.getOutput().getAllLogs(false).contains(String.format("r00t%n")), is(true));
    }

    @Test
    public void nativeTask_WorkingDir() throws Throwable {
        String tempFolder = tmpFolder.newFolder().getCanonicalPath();
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(pwdCommand(), "native")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setForkEnvironment(new ForkEnvironment(tempFolder));

        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

    
        assertThat(taskResult.getOutput().getAllLogs(false).contains(String.format("%s%n", tempFolder)), is(true));
    }

    @Test
    public void nativeTask_WorkingDir_WithVariableReplacement() throws Throwable {
        String tempFolder = tmpFolder.newFolder().getCanonicalPath();
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(pwdCommand(), "native")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setVariables(singletonMap("folder", tempFolder));
        initializer.setForkEnvironment(new ForkEnvironment("$folder"));

        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

  
        assertThat(taskResult.getOutput().getAllLogs(false).contains(String.format("%s%n", tempFolder)), is(true));
    }

    private String pwdCommand() {
        if (System.getProperty("os.name").contains("Windows")) {
            return "cmd.exe /c cd ,";
        } else {
            return "pwd";
        }
    }

    @Test
    public void taskLogsAreCopiedToUserSpace() throws Exception {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript("print('hello'); result='hello'", "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setPreciousLogs(true);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        final TaskDataspaces dataspacesMock = mock(TaskDataspaces.class);
        when(dataspacesMock.getScratchFolder()).thenReturn(tmpFolder.newFolder());

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory() {

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
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
          new TaskScript(new SimpleScript("print('hello'); result='hello'", "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();

        initializer.setPreciousLogs(false);
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        final TaskDataspaces dataspacesMock = mock(TaskDataspaces.class);
        when(dataspacesMock.getScratchFolder()).thenReturn(tmpFolder.newFolder());

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory(){

            @Override
            public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
                return dataspacesMock;
            }
        });
        runTaskLauncher(taskLauncher, executableContainer);

        verify(dataspacesMock, times(1)).copyScratchDataToOutput(Matchers.<List<OutputSelector>>any());
    }

    @Test
    public void scratchDirDeletedAfterTaskCompleted() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript("print('hello'); result='hello'", "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));

        final TaskDataspaces dataspacesMock = mock(TaskDataspaces.class);
        when(dataspacesMock.getScratchFolder()).thenReturn(tmpFolder.newFolder());

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory() {

            @Override
            public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
                return dataspacesMock;
            }
        });
        runTaskLauncher(taskLauncher, executableContainer);

        verify(dataspacesMock).cleanScratchSpace();
    }

    @Test
    public void testProgressFileReaderIntegration() throws Throwable {
        int nbIterations = 3;

        String taskScript = CharStreams.toString(new InputStreamReader(
                getClass().getResourceAsStream("/task-report-progress.py"), Charsets.UTF_8));

        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(taskScript, "python",
                        new String [] {Integer.toString(nbIterations)})));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("42"), "job", 1000L));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, new TestTaskLauncherFactory());
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        List result = (List) taskResult.value();

        for (int i=1; i<=result.size(); i++) {
            assertEquals(i * (100 / nbIterations), result.get(i-1));
        }
    }

    private TaskResult runTaskLauncher(TaskLauncher taskLauncher, ScriptExecutableContainer executableContainer) {
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