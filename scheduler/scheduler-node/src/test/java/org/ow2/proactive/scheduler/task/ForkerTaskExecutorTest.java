package org.ow2.proactive.scheduler.task;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.io.File;
import java.security.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ForkerTaskExecutorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    private String oldJavaHome;

    @Test
    public void result_and_variables() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkerTaskExecutor taskExecutor = new ForkerTaskExecutor(tmpFolder.newFolder(), null);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ForkedScriptExecutableContainer(
                new TaskScript(new SimpleScript("print('hello'); variables.put('var','foo'); result='hello'",
                        "javascript"))), initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("hello\n", taskOutput.output());
        assertEquals("hello", result.value());
        assertEquals("foo",
                SerializationUtil.deserializeVariableMap(result.getPropagatedVariables()).get("var"));
    }

    @Test
    public void failToSerialize() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkerTaskExecutor taskExecutor = new ForkerTaskExecutor(new File("non_existing_folder"), null);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ForkedScriptExecutableContainer(
                        new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertNotNull(result.getException());
    }

    @Test
    public void failToFindJava() throws Throwable {
        System.setProperty("java.home", "does not exist");
        TestTaskOutput taskOutput = new TestTaskOutput();

        ForkerTaskExecutor taskExecutor = new ForkerTaskExecutor(new File("non_existing_folder"), null);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false)));

        TaskResultImpl result = taskExecutor.execute(new TaskContext(new ForkedScriptExecutableContainer(
                        new TaskScript(new SimpleScript("print('hello'); result='hello'", "javascript"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertNotNull(result.getException());
    }

    @Test
    public void runAsMe() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        Decrypter decrypter = createCredentials("somebody_that_does_not_exists");

        ForkerTaskExecutor taskExecutor = new ForkerTaskExecutor(tmpFolder.newFolder(), decrypter);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false)));

        ForkedScriptExecutableContainer container = new ForkedScriptExecutableContainer(new TaskScript(
                new SimpleScript("print('hello'); result='hello'", "javascript")));

        container.setRunAsUser(true);

        TaskContext taskContext = new TaskContext(container, initializer);
        taskContext.setDecrypter(decrypter);
        TaskResultImpl result = taskExecutor.execute(taskContext, taskOutput.outputStream, taskOutput.error);

        assertNotNull(result.getException());
    }

    @Test
    public void forkEnvironment() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        File workingDir = tmpFolder.newFolder();

        ForkerTaskExecutor taskExecutor = new ForkerTaskExecutor(workingDir, null);

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false)));

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addSystemEnvironmentVariable("envVar", "envValue", true);
        forkEnvironment.addJVMArgument("-DjvmArg=jvmValue");

        taskExecutor.execute(new TaskContext(new ForkedScriptExecutableContainer(
                new TaskScript(new SimpleScript("println System.getenv('envVar'); " +
                        "println System.getProperty('jvmArg'); " +
                        "println new File('.').getAbsolutePath()", "groovy")),
                forkEnvironment), initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("envValue\njvmValue\n" + new File(workingDir, ".").getAbsolutePath() + "\n", taskOutput.output());
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