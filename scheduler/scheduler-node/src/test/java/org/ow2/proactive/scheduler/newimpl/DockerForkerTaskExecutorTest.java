package org.ow2.proactive.scheduler.newimpl;

import java.io.*;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.junit.*;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;


public class DockerForkerTaskExecutorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private static String oldDocker;
    private static String oldSudo;
    private static String oldImage;

    // Class to test behavior
    private class testExecutor extends TimedCommandExecutor {

        public ArrayList<String[]> lastCommands = new ArrayList<String[]>();
        public ArrayList<PrintStream> outputSinks = new ArrayList<PrintStream>();
        public ArrayList<PrintStream> errorSinks = new ArrayList<PrintStream>();

        public int commandExitCode = 0;

        @Override
        public int executeCommand(PrintStream outputSink, PrintStream errorSink, String... command)
                throws FailedExecutionException, InterruptedException {
            this.lastCommands.add(command);
            this.outputSinks.add(outputSink);
            this.errorSinks.add(errorSink);

            return this.commandExitCode;
        }
    }

    @BeforeClass
    public static void prepare() throws InvalidScriptException {

        //Set system properties
        oldDocker = System.getProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY, "docker");
        oldSudo = System.getProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY, "sudo");
        oldImage = System.getProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY, "test/image");
        System.setProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY, "docker");
        System.setProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY, "sudo");
        System.setProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY, "test/image");
    }

    @Test
    public void properPrintStreamAndCommands() throws Throwable {
        // Prepare TaskId
        TaskId taskId = TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false);

        // Create result
        int duration = 5000;
        TaskResult result = new TaskResultImpl(taskId, new String("ResultOfCommand"), new SimpleTaskLogs(
            "Standard", "Error"), duration);

        // Create executable container
        ExecutableContainer execContainer = new ForkedScriptExecutableContainer(new TaskScript(
            new SimpleScript("print('hello'); variables.put('var','foo'); result='hello'", "javascript")));

        // Prepare initializer
        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(taskId);

        // Prepare context
        TaskContext context = new TaskContext(execContainer, initializer);

        TestTaskOutput taskOutput = new TestTaskOutput();

        // Create test executor
        testExecutor testExec = new testExecutor();

        // Create folder and set it for execution test class
        File folder = tmpFolder.newFolder();
        DockerForkerTaskExecutor taskExecutor = new DockerForkerTaskExecutor(folder, null, testExec);

        taskExecutor.execute(context, taskOutput.outputStream, taskOutput.error);

        // Check whether Printstreams have been applied to executor
        assertEquals(taskOutput.outputStream, testExec.outputSinks.get(0));
        assertEquals(taskOutput.error, testExec.errorSinks.get(0));

        // Check whether commands do not contain anything null
        for (String[] command : testExec.lastCommands) {
            for (String argument : command) {
                assertNotNull("Command must not be set up properly", argument);
            }
        }

    }

    @Test
    public void failToSerialize() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        // Create test executor
        testExecutor testExec = new testExecutor();

        DockerForkerTaskExecutor taskExecutor = new DockerForkerTaskExecutor(new File("non_existing_folder"),
            null, testExec);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertNotNull(result.getException());
    }

    // This seems like functionality which is not provided by the DockerForkerTaskExecutor

    /*@Test
    public void runAsMe() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        Decrypter decrypter = createCredentials("somebody_that_does_not_exists");

        // Create test executor
        testExecutor testExec = new testExecutor();

        DockerForkerTaskExecutor taskExecutor = new DockerForkerTaskExecutor(tmpFolder.newFolder(),
                decrypter,
                testExec );

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false)));

        ForkedScriptExecutableContainer container = new ForkedScriptExecutableContainer(new TaskScript(
            new SimpleScript("print('hello'); result='hello'", "javascript")));

        container.setRunAsUser(true);

        TaskContext taskContext = new TaskContext(container, initializer);
        taskContext.setDecrypter(decrypter);
        TaskResultImpl result = taskExecutor.execute(taskContext, taskOutput.outputStream, taskOutput.error);

        assertEquals(OSUserException.class, result.getException().getCause().getClass());
    }*/

    private Decrypter createCredentials(String username) throws NoSuchAlgorithmException, KeyException {
        CredData credData = new CredData(username, "pwd");
        KeyPairGenerator keyGen;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        Decrypter decrypter = new Decrypter(keyPair.getPrivate());
        Credentials credentials = Credentials.createCredentials(credData, keyPair.getPublic());
        decrypter.setCredentials(credentials);
        return decrypter;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        // Restore System properties
        System.setProperty(DockerContainerWrapper.DOCKER_COMMAND_PROPERTY, oldDocker);
        System.setProperty(DockerContainerWrapper.SUDO_COMMAND_PROPERTY, oldSudo);
        System.setProperty(DockerContainerWrapper.DOCKER_IMAGE_PROPERTY, oldImage);
    }
}