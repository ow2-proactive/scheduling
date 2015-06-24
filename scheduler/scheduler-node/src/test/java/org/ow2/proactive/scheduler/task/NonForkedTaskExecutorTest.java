package org.ow2.proactive.scheduler.task;

import org.junit.Test;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.executors.NonForkedTaskExecutor;
import org.ow2.proactive.scheduler.task.containers.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.utils.ClasspathUtils;

import java.io.Serializable;
import java.security.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class NonForkedTaskExecutorTest {

    @Test
    public void simpleScriptTask() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("println('pre')", "groovy"));
        initializer.setPostScript(new SimpleScript("println('post')", "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(
              new SimpleScript("println('hello'); java.lang.Thread.sleep(5); result='hello'", "groovy"))),
            initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("pre\nhello\npost\n", taskOutput.output());
        assertEquals("hello", result.value());
        assertTrue("Task duration should be at least 5", result.getTaskDuration() >= 5);
    }

    @Test
    public void contextVariables() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setReplicationIndex(42);
        String printEnvVariables = "print(variables.get('PA_JOB_NAME') + '@' + "
            + "variables.get('PA_JOB_ID') + '@' + variables.get('PA_TASK_NAME') "
            + "+ '@' + variables.get('PA_TASK_ID') +'\\n')";
        initializer.setPreScript(new SimpleScript(printEnvVariables, "groovy"));
        initializer.setPostScript(new SimpleScript(printEnvVariables, "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        new NonForkedTaskExecutor().execute(new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript(printEnvVariables, "groovy"))), initializer),
          taskOutput.outputStream, taskOutput.error);

        String[] lines = taskOutput.output().split("\\n");
        assertEquals("job@1000@task@42", lines[0]);
        assertEquals("job@1000@task@42", lines[1]);
        assertEquals("job@1000@task@42", lines[2]);
    }

    @Test
    public void contextVariables_index() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setReplicationIndex(7);
        initializer.setIterationIndex(6);
        String script = "result = variables.get('PA_TASK_ITERATION') * variables.get('PA_TASK_REPLICATION')";
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(script, "groovy"))),
            initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals(42, result.value());
    }

    @Test
    public void variablesPropagation() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("print(variables.get('var')); variables.put('var', 'pre')",
            "groovy"));
        initializer.setPostScript(
          new SimpleScript("print(variables.get('var')); variables.put('var', 'post')", "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));
        initializer.setVariables(Collections.singletonMap("var", "value"));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print(variables.get('var')); variables.put('var', 'task')", "groovy"))), initializer),
                taskOutput.outputStream, taskOutput.error);

        assertEquals("valuepretask", taskOutput.output());
        assertEquals("post",
                SerializationUtil.deserializeVariableMap(result.getPropagatedVariables()).get("var"));
    }

    @Test
    public void variablesPropagation_fromParentTask() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        Map<String, Serializable> variablesFromParent = new HashMap<String, Serializable>();
        variablesFromParent.put("var", "parent");
        variablesFromParent.put(SchedulerVars.PA_TASK_ID.toString(), "1234");

        TaskResult[] previousTasksResults = { new TaskResultImpl(null, null, null, null, null,
            SerializationUtil.serializeVariableMap(variablesFromParent)) };

        new NonForkedTaskExecutor().execute(new TaskContext(new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("print(variables.get('var'));print(variables.get('PA_TASK_ID'))",
            "groovy"))), initializer, previousTasksResults), taskOutput.outputStream, taskOutput.error);

        assertEquals("parent42", taskOutput.output());
    }

    @Test
    public void result_from_parent_task() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        TaskResult[] previousTasksResults = { new TaskResultImpl(null, "aresult", null, 0) };

        new NonForkedTaskExecutor().execute(new TaskContext(new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("print(results[0]);", "groovy"))), initializer,
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
                    "return 10/0", "groovy"))), initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("", taskOutput.output());
        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Test
    public void failingPrescript() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("return 10/0", "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "groovy"))), initializer), taskOutput.outputStream,
                taskOutput.error);

        assertEquals("", taskOutput.output());
        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Test
    public void failingPostscript() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPostScript(new SimpleScript("return 10/0", "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "groovy"))), initializer), taskOutput.outputStream,
                taskOutput.error);

        assertEquals("hello", taskOutput.output());
        assertNotEquals("", taskOutput.error());
        assertNotNull(result.getException());
    }

    @Test
    public void taskWithFlowScript() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setControlFlowScript(FlowScript.createReplicateFlowScript("print('flow'); runs=5", "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "groovy"))), initializer), taskOutput.outputStream,
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
                "print('hello'); result='hello'", "groovy"))), initializer), taskOutput.outputStream,
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
          new SimpleScript(printEnvVariables, "groovy", new Serializable[] { "Hello" }));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        new NonForkedTaskExecutor().execute(new TaskContext(
          new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript("", "groovy"))),
          initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("Hello", taskOutput.output());
    }

    @Test
    public void scriptArgumentsReplacements() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        String printArgs = "println(args[0] + args[1]);";
        initializer.setPreScript(
          new SimpleScript(printArgs, "groovy", new Serializable[] { "$CREDENTIALS_PASSWORD", "$PA_JOB_ID" }));
        initializer.setPostScript(new SimpleScript(printArgs, "groovy",
          new Serializable[] { "$CREDENTIALS_PASSWORD", "$PA_JOB_ID" }));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        Decrypter decrypter = createCredentials("somebody_that_does_not_exists");
        TaskContext taskContext = new TaskContext(new ForkedScriptExecutableContainer(
            new TaskScript(new SimpleScript(printArgs, "groovy",
                new Serializable[] { "$CREDENTIALS_PASSWORD", "${PA_JOB_ID}" }))), initializer);
        taskContext.setDecrypter(decrypter);
        new NonForkedTaskExecutor().execute(taskContext, taskOutput.outputStream, taskOutput.error);

        assertEquals("p4ssw0rd1000\np4ssw0rd1000\np4ssw0rd1000\n", taskOutput.output()); // pre, task and post
    }

    @Test
    public void schedulerHomeIsInVariables() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        new NonForkedTaskExecutor().execute(new TaskContext(new ForkedScriptExecutableContainer(
          new TaskScript(new SimpleScript("print(variables.get('PA_SCHEDULER_HOME'))", "groovy"))),
          initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals(ClasspathUtils.findSchedulerHome(), taskOutput.output());
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