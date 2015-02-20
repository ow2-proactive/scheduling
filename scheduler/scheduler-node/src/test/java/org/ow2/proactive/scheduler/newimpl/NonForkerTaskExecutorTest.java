package org.ow2.proactive.scheduler.newimpl;

import java.io.Serializable;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.newimpl.utils.Decrypter;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;

import static org.junit.Assert.*;


public class NonForkerTaskExecutorTest {

    @Test
    public void simpleScriptTask() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("print('pre')", "javascript"));
        initializer.setPostScript(new SimpleScript("print('post')", "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); java.lang.Thread.sleep(5); result='hello'", "javascript"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertEquals("prehellopost", taskOutput.output());
        assertEquals("hello", result.value());
        assertTrue("Task duration should be at least 5", result.getTaskDuration() >= 5);
    }

    @Test
    public void contextVariables() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setReplicationIndex(42);
        String printEnvVariables = "print(variables.get('pas.job.name') + '@' + "
            + "variables.get('pas.job.id') + '@' + variables.get('pas.task.name') "
            + "+ '@' + variables.get('pas.task.id') +'\\n')";
        initializer.setPreScript(new SimpleScript(printEnvVariables, "javascript"));
        initializer.setPostScript(new SimpleScript(printEnvVariables, "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        new NonForkedTaskExecutor().execute(new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript(printEnvVariables, "javascript"))), initializer),
          taskOutput.outputStream, taskOutput.error);

        String[] lines = taskOutput.output().split("\\n");
        assertEquals("job@1000@task@42", lines[0]);
        assertEquals("job@1000@task@42", lines[1]);
        assertEquals("job@1000@task@42", lines[2]);
    }

    /*@Test
    public void contextVariables_index() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setReplicationIndex(7);
        initializer.setIterationIndex(6);
        String script = "result = variables.get('pas.task.iteration') * variables.get('pas.task.replication')";
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(script, "javascript"))),
            initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals(42.0, result.value());
    }*/

    @Test
    public void variablesPropagation() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("print(variables.get('var')); variables.put('var', 'pre')",
            "javascript"));
        initializer.setPostScript(
          new SimpleScript("print(variables.get('var')); variables.put('var', 'post')", "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));
        initializer.setVariables(Collections.singletonMap("var", "value"));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print(variables.get('var')); variables.put('var', 'task')", "javascript"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertEquals("valuepretask", taskOutput.output());
        assertEquals("post", SerializationUtil.deserializeVariableMap(result.getPropagatedVariables()).get(
                "var"));
    }

    @Test
    public void variablesPropagation_fromParentTask() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        Map<String, Serializable> variablesFromParent = new HashMap<String, Serializable>();
        variablesFromParent.put("var", "parent");
        variablesFromParent.put(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString(), "1234");

        TaskResult[] previousTasksResults = { new TaskResultImpl(null, null, null, null, null,
            SerializationUtil.serializeVariableMap(variablesFromParent)) };

        new NonForkedTaskExecutor().execute(new TaskContext(new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("print(variables.get('var'));print(variables.get('pas.task.id'))",
            "javascript"))), initializer, previousTasksResults), taskOutput.outputStream, taskOutput.error);

        assertEquals("parent42", taskOutput.output());
    }

    @Test
    public void result_from_parent_task() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        TaskResult[] previousTasksResults = { new TaskResultImpl(null, "aresult", null, 0) };

        new NonForkedTaskExecutor().execute(new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript("print(results[0]);", "javascript"))), initializer,
            previousTasksResults), taskOutput.outputStream, taskOutput.error);

        assertEquals("aresult", taskOutput.output());
    }

    @Test
    public void failingScriptTask() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(
                new TaskContext(new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                    "return 10/0", "javascript"))), initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("", taskOutput.output());
        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Test
    public void failingPrescript() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("return 10/0", "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "javascript"))), initializer), taskOutput.outputStream,
                taskOutput.error);

        assertEquals("", taskOutput.output());
        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Test
    public void failingPostscript() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPostScript(new SimpleScript("return 10/0", "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "javascript"))), initializer), taskOutput.outputStream,
                taskOutput.error);

        assertEquals("hello", taskOutput.output());
        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Test
    public void taskWithFlowScript() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setControlFlowScript(FlowScript.createReplicateFlowScript("print('flow'); runs=5"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "javascript"))), initializer), taskOutput.outputStream,
                taskOutput.error);

        assertEquals(FlowActionType.REPLICATE, result.getAction().getType());
        assertEquals("helloflow", taskOutput.output());
    }

    @Test
    public void failingFlowScript() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setControlFlowScript(FlowScript.createReplicateFlowScript(""));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "javascript"))), initializer), taskOutput.outputStream,
                taskOutput.error);

        assertEquals("hello", taskOutput.output());
        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Test
    public void scriptArguments() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        String printEnvVariables = "print(args[0])";
        initializer.setPreScript(
          new SimpleScript(printEnvVariables, "javascript", new Serializable[] { "Hello" }));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        new NonForkedTaskExecutor().execute(new TaskContext(
          new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript("", "javascript"))),
          initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("Hello", taskOutput.output());
    }

    @Test
    public void scriptArgumentsReplacements() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        String printEnvVariables = "print(args[0])";
        initializer.setPreScript(
          new SimpleScript(printEnvVariables, "javascript", new Serializable[] { "$CREDENTIALS_PASSWORD" }));
        initializer.setPostScript(
          new SimpleScript(printEnvVariables, "javascript", new Serializable[] { "$CREDENTIALS_PASSWORD" }));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        Decrypter decrypter = createCredentials("somebody_that_does_not_exists");
        TaskContext taskContext = new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript(printEnvVariables, "javascript",
                new Serializable[] { "$CREDENTIALS_PASSWORD" }))), initializer);
        taskContext.setDecrypter(decrypter);
        new NonForkedTaskExecutor().execute(taskContext, taskOutput.outputStream, taskOutput.error);

        assertEquals("p4ssw0rdp4ssw0rdp4ssw0rd", taskOutput.output()); // pre, task and post
    }

    private Decrypter createCredentials(String username) throws NoSuchAlgorithmException, KeyException {
        CredData credData = new CredData(username, "pwd");
        credData.addThirdPartyCredential("PASSWORD", "p4ssw0rd");
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