package org.ow2.proactive.scheduler.task;

import java.io.Serializable;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.executors.InProcessTaskExecutor;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.utils.ClasspathUtils;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ow2.proactive.scheduler.task.TaskAssertions.assertTaskResultOk;


public class InProcessTaskExecutorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void simpleScriptTask() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("println('pre')", "groovy"));
        initializer.setPostScript(new SimpleScript("println('post')", "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
            new ScriptExecutableContainer(new TaskScript(
              new SimpleScript("println('hello'); java.lang.Thread.sleep(5); result='hello'", "groovy"))),
            initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("pre\nhello\npost\n", taskOutput.output());
        assertEquals("hello", result.value());
        assertTrue("Task duration should be at least 5", result.getTaskDuration() >= 5);
    }

    @Test
    public void failingScript() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
          new ScriptExecutableContainer(new TaskScript(
            new SimpleScript("dsfsdfsdf", "groovy"))),
          initializer), taskOutput.outputStream, taskOutput.error);

        assertTrue(result.hadException());
        assertFalse(taskOutput.error().isEmpty());
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

        new InProcessTaskExecutor().execute(new TaskContext(new ScriptExecutableContainer(
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

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
            new ScriptExecutableContainer(new TaskScript(new SimpleScript(script, "groovy"))),
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

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
            new ScriptExecutableContainer(new TaskScript(new SimpleScript(
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

        Map<String, Serializable> variablesFromParent = new HashMap<>();
        variablesFromParent.put("var", "parent");
        variablesFromParent.put(SchedulerVars.PA_TASK_ID.toString(), "1234");

        TaskResult[] previousTasksResults = { new TaskResultImpl(null, null, null, null,SerializationUtil.serializeVariableMap(variablesFromParent)) };

        new InProcessTaskExecutor().execute(new TaskContext(new ScriptExecutableContainer(
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

        new InProcessTaskExecutor().execute(new TaskContext(new ScriptExecutableContainer(
          new TaskScript(new SimpleScript("print(results[0]);", "groovy"))), initializer,
          previousTasksResults), taskOutput.outputStream, taskOutput.error);

        assertEquals("aresult", taskOutput.output());
    }

    @Test
    public void failingScriptTask() throws Exception {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
            new ScriptExecutableContainer(new TaskScript(new SimpleScript("return 10/0", "groovy"))),
            initializer), taskOutput.outputStream, taskOutput.error);

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

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
            new ScriptExecutableContainer(new TaskScript(new SimpleScript(
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

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
            new ScriptExecutableContainer(new TaskScript(new SimpleScript(
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
        initializer.setControlFlowScript(FlowScript.createReplicateFlowScript("print('flow'); runs=5",
                "groovy"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
            new ScriptExecutableContainer(new TaskScript(new SimpleScript(
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

        TaskResultImpl result = new InProcessTaskExecutor().execute(new TaskContext(
            new ScriptExecutableContainer(new TaskScript(new SimpleScript(
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
        initializer
                .setPreScript(new SimpleScript(printEnvVariables, "groovy", new Serializable[] { "Hello" }));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        new InProcessTaskExecutor().execute(new TaskContext(new ScriptExecutableContainer(
            new TaskScript(new SimpleScript("", "groovy"))), initializer), taskOutput.outputStream,
                taskOutput.error);

        assertEquals("Hello", taskOutput.output());
    }

    @Test
    public void scriptArgumentsReplacements() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        String printArgs = "println(args[0] + args[1]);";
        initializer.setPreScript(new SimpleScript(printArgs, "groovy", new Serializable[] {
                "$credentials_PASSWORD", "$PA_JOB_ID" }));
        initializer.setPostScript(new SimpleScript(printArgs, "groovy", new Serializable[] {
                "$credentials_PASSWORD", "$PA_JOB_ID" }));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        Decrypter decrypter = createCredentials("somebody_that_does_not_exists");
        TaskContext taskContext = new TaskContext(new ScriptExecutableContainer(new TaskScript(
            new SimpleScript(printArgs, "groovy", new Serializable[] { "$credentials_PASSWORD",
                    "${PA_JOB_ID}" }))), initializer);
        taskContext.setDecrypter(decrypter);
        new InProcessTaskExecutor().execute(taskContext, taskOutput.outputStream, taskOutput.error);

        assertEquals("p4ssw0rd1000\np4ssw0rd1000\np4ssw0rd1000\n", taskOutput.output()); // pre, task and post
    }

    @Test
    public void schedulerHomeIsInVariables() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        new InProcessTaskExecutor().execute(new TaskContext(new ScriptExecutableContainer(
          new TaskScript(new SimpleScript("print(variables.get('PA_SCHEDULER_HOME'))", "groovy"))),
          initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals(ClasspathUtils.findSchedulerHome(), taskOutput.output());
    }

    @Test
    public void nodesFileIsCreated() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        ScriptExecutableContainer printNodesFileTask = new ScriptExecutableContainer(
            new TaskScript(new SimpleScript("print new File(variables.get('PA_NODESFILE')).text", "groovy")));
        printNodesFileTask.setNodes(mockedNodeSet());

        TaskContext context = new TaskContext(printNodesFileTask, initializer, null, tmpFolder.newFolder()
                .toURI().toString(), "", "", "", "", "", "thisHost");
        TaskResultImpl taskResult = new InProcessTaskExecutor().execute(context, taskOutput.outputStream,
          taskOutput.error);

        assertTaskResultOk(taskResult);
        assertEquals("thisHost\ndummyhost\n", taskOutput.output());
    }

    private NodeSet mockedNodeSet() {
        NodeSet nodes = new NodeSet();
        ProActiveRuntime proActiveRuntime = mock(ProActiveRuntime.class);
        VMInformation vmInformation = mock(VMInformation.class);
        when(vmInformation.getHostName()).thenReturn("dummyhost");
        when(proActiveRuntime.getVMInformation()).thenReturn(vmInformation);
        nodes.add(new NodeImpl(proActiveRuntime, "dummyhost"));
        return nodes;
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