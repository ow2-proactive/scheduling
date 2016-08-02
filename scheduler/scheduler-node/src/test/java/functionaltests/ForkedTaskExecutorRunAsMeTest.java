package functionaltests;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.TestTaskOutput;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.NodeDataSpacesURIs;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.executors.ForkedTaskExecutor;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import java.security.*;

import static org.junit.Assert.assertEquals;
import static org.ow2.proactive.scheduler.task.TaskAssertions.assertTaskResultOk;


/**
 * The ForkedTaskExecutorRunAsMeTest executes several scenarios on the ForkedTaskExecutor in RunAsMe mode
 * As the ForkedTaskExecutor starts a separate JVM, this test is a functionalTest (to avoid leaving alive subprocesses when cancelled)
 */
@Ignore("Needs a specific system user to exit, can be used for manual testing")
public class ForkedTaskExecutorRunAsMeTest {

    private static final String USERNAME = "admin"; // need to be created manually on your system
    private static final String PASSWORD = "admin";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void runAsMe_PasswordMethod() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        Decrypter decrypter = createCredentials(USERNAME, PASSWORD);

        ForkedTaskExecutor taskExecutor = new ForkedTaskExecutor(tmpFolder.newFolder());

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId((TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L)));

        ScriptExecutableContainer container = new ScriptExecutableContainer(new TaskScript(new SimpleScript(
                "whoami", "native")));

        container.setRunAsUser(true);

        TaskContext taskContext = new TaskContext(container, initializer, null, new NodeDataSpacesURIs("", "", "", "", "", ""), "", "",
                decrypter);
        TaskResultImpl result = taskExecutor.execute(taskContext, taskOutput.outputStream, taskOutput.error);

        assertTaskResultOk(result);
        assertEquals("admin\n", taskOutput.output());
    }

    private Decrypter createCredentials(String username, String password) throws NoSuchAlgorithmException,
            KeyException {
        CredData credData = new CredData(username, password);
        KeyPairGenerator keyGen;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        Decrypter decrypter = new Decrypter(keyPair.getPrivate());
        Credentials credentials = Credentials.createCredentials(credData, keyPair.getPublic());
        decrypter.setCredentials(credentials);
        return decrypter;
    }

}