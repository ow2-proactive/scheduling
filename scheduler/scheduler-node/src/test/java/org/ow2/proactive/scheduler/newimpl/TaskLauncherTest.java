package org.ow2.proactive.scheduler.newimpl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.util.Object2ByteConverter;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.newimpl.data.TaskDataspaces;
import org.ow2.proactive.scheduler.newimpl.utils.Decrypter;
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

    /**
     * Converts operating system dependent strings.
     * @param stringToNormalize
     * @return
     */
    private String normalize(String stringToNormalize) {
        String returnString;

        returnString = stringToNormalize.replaceAll("\\r\\n", "\n");
        returnString = returnString.replaceAll("\\r", "\n");

        return returnString;
    }

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

        assertEquals(normalize("hello"), normalize(taskResult.value().toString()));
        assertEquals(normalize("pre\nhello\npost\n"), normalize(taskResult.getOutput().getAllLogs(false)));
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
        Credentials thirdPartyCredentials = Credentials.createCredentials(credData, taskLauncher.generatePublicKey());
        executableContainer.setCredentials(thirdPartyCredentials);

        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertEquals(normalize("r00t\n"), normalize(taskResult.getOutput().getAllLogs(false)));
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

    private class TestTaskLauncherFactory extends TaskLauncherFactory {
        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
            return new TaskFileDataspaces();
        }

        public TaskExecutor createTaskExecutor(File workingDir, Decrypter decrypter) {
            return new NonForkedTaskExecutor();
        }

    }

    private class SlowDataspacesTaskLauncherFactory extends TaskLauncherFactory {
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
            try {
                return tmpFolder.newFolder();
            } catch (IOException e){
                throw new RuntimeException(e);
            }
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
            try {
                return tmpFolder.newFolder();
            } catch (IOException e){
                throw new RuntimeException(e);
            }
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