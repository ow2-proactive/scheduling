package org.ow2.proactive.scheduler.task;

import java.io.File;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.executors.ForkedTaskExecutor;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ForkedTaskExecutorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    private String oldJavaHome;

    @Test
    public void ensureForkedJvmContainTaskForkProperty() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "sample", 1000L)));

        ForkedTaskExecutor forkedTaskExecutor = new ForkedTaskExecutor(tmpFolder.newFolder());

        TaskResultImpl result =
                forkedTaskExecutor.execute(
                        new TaskContext(
                                new ScriptExecutableContainer(
                                        new TaskScript(
                                                new SimpleScript(
                                                        "result=System.getProperty('"
                                                                + PASchedulerProperties.TASK_FORK.getKey() + "')"
                                                        , "groovy"))), initializer, null, "", "", "", "", "",
                                "", ""),
                        taskOutput.outputStream, taskOutput.error);

        Assert.assertEquals("true", result.value());
    }

    @Test
    public void result_and_variables() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(tmpFolder.newFolder());

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ScriptExecutableContainer(
                        new TaskScript(new SimpleScript("print('hello'); variables.put('var','foo'); result='hello'",
                                "javascript"))), initializer, null, "", "", "", "", "", "", ""),
                taskOutput.outputStream, taskOutput.error);

        assertEquals(String.format("hello%n"), taskOutput.output());
        assertEquals("hello", result.value());
        assertEquals("foo",
                SerializationUtil.deserializeVariableMap(result.getPropagatedVariables()).get("var"));
    }

    @Test
    public void failToSerialize() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(new File("non_existing_folder"));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ScriptExecutableContainer(
                        new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript"))),
                        initializer, null, "", "", "", "", "", "", ""),
                taskOutput.outputStream, taskOutput.error);

        assertNotNull(result.getException());
    }

    @Test
    public void failToFindJava() throws Throwable {
        System.setProperty("java.home", "does not exist");
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(new File("non_existing_folder"));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ScriptExecutableContainer(
                        new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript"))),
                        initializer, null, "", "", "", "", "", "", ""),
                taskOutput.outputStream, taskOutput.error);

        assertNotNull(result.getException());
    }

    @Test
    public void runAsMe_userDoesNotExist() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        Decrypter decrypter = createCredentials("somebody_that_does_not_exists");

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(tmpFolder.newFolder());

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L)));

        ScriptExecutableContainer container = new ScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "javascript")));

        container.setRunAsUser(true);

        TaskContext taskContext = new TaskContext(container, initializer, null, "", "", "", "", "", "", "",
                decrypter);

        TaskResultImpl result = taskExecutor.execute(taskContext, taskOutput.outputStream, taskOutput.error);

        assertNotNull(result.getException());
    }

    @Test
    public void forkEnvironment() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        File workingDir = tmpFolder.newFolder();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(workingDir);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L)));

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addSystemEnvironmentVariable("envVar", "envValue");
        forkEnvironment.addJVMArgument("-DjvmArg=jvmValue");
        initializer.setForkEnvironment(forkEnvironment);

        taskExecutor.execute(new TaskContext(new ScriptExecutableContainer(new TaskScript(new SimpleScript(
                        "println System.getenv('envVar'); " + "println System.getProperty('jvmArg'); " +
                                "println new File('.').getCanonicalPath()", "groovy"))), initializer, null, "", "",
                        "", "", "", "", ""), taskOutput.outputStream,
                taskOutput.error);

        assertEquals(String.format("envValue%njvmValue%n%s%n", new File(workingDir, ".").getCanonicalPath()),
                taskOutput.output());
    }

    @Test
    public void forkEnvironment_WithVariables() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        File workingDir = tmpFolder.newFolder();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(workingDir);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L)));
        initializer.setVariables(Collections.singletonMap("aVar", "aValue"));

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addSystemEnvironmentVariable("envVar", "$aVar");
        forkEnvironment.addJVMArgument("-DjvmArg=$aVar");
        initializer.setForkEnvironment(forkEnvironment);

        taskExecutor.execute(new TaskContext(new ScriptExecutableContainer(new TaskScript(new SimpleScript(
                        "println System.getenv('envVar'); " + "println System.getProperty('jvmArg'); "
                                + "println new File('.').getCanonicalPath()", "groovy"))), initializer, null, "", "",
                        "", "", "", "", ""),
                taskOutput.outputStream, taskOutput.error);

        assertEquals(String.format("aValue%naValue%n%s%n", new File(workingDir, ".").getCanonicalPath()),
                taskOutput.output());
    }

    @Test
    public void forkEnvironment_failingEnvScript() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        File workingDir = tmpFolder.newFolder();

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(workingDir);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L)));

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.setEnvScript(new SimpleScript("should fail execution", "groovy"));
        initializer.setForkEnvironment(forkEnvironment);

        TaskResultImpl taskResult = taskExecutor.execute(new TaskContext(new ScriptExecutableContainer(
                        new TaskScript(new SimpleScript("", "groovy"))), initializer, null, "", "", "", "", "", "",
                        ""), taskOutput.outputStream,
                taskOutput.error);

        assertTrue(taskResult.hadException());
    }

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

    @Before
    public void setUp() throws Exception {
        oldJavaHome = System.getProperty("java.home");
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("java.home", oldJavaHome);
    }
}