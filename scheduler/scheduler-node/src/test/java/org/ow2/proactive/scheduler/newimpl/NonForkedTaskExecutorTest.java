package org.ow2.proactive.scheduler.newimpl;

import java.util.Collections;

import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;

import static org.junit.Assert.*;


public class NonForkedTaskExecutorTest {

    @Test
    public void simpleScriptTask() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("print('pre')", "javascript"));
        initializer.setPostScript(new SimpleScript("print('post')", "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(
                "print('hello'); result='hello'", "javascript"))), initializer), taskOutput.outputStream,
                taskOutput.error);

        assertEquals("prehellopost", taskOutput.output());
        assertEquals("hello", result.value());
    }

    @Test
    public void contextVariables() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        String printEnvVariables = "print(variables.get('pas.job.name') + '@' + "
            + "variables.get('pas.job.id') + '@' + variables.get('pas.task.name') " +
          "+ '@' + variables.get('pas.task.id') +'\\n')";
        initializer.setPreScript(new SimpleScript(printEnvVariables, "javascript"));
        initializer.setPostScript(new SimpleScript(printEnvVariables, "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
            new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript(printEnvVariables,
                "javascript"))), initializer), taskOutput.outputStream, taskOutput.error);

        String[] lines = taskOutput.output().split("\\n");
        assertEquals("job@1000@task@42", lines[0]);
        assertEquals("job@1000@task@42", lines[1]);
        assertEquals("job@1000@task@42", lines[2]);
    }

    @Test
    public void variablesPropagation() throws Throwable {
        TestTaskOutput taskOutput = new TestTaskOutput();

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setPreScript(new SimpleScript("print(variables.get('var')); variables.put('var', 'pre')", "javascript"));
        initializer.setPostScript(
          new SimpleScript("print(variables.get('var')); variables.put('var', 'post')", "javascript"));
        initializer.setTaskId(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L, false));
        initializer.setVariables(Collections.singletonMap("var", "value"));

        TaskResultImpl result = new NonForkedTaskExecutor().execute(new TaskContext(
          new ForkedScriptExecutableContainer(new TaskScript(new SimpleScript("print(variables.get('var')); variables.put('var', 'task')",
            "javascript"))), initializer), taskOutput.outputStream, taskOutput.error);

        assertEquals("valuepretask", taskOutput.output());
        assertEquals("post", SerializationUtil.deserializeVariableMap(result.getPropagatedVariables()).get("var"));
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

        assertNotNull(result.getException());
        assertNotEquals("", taskOutput.error());
        assertEquals("hello", taskOutput.output());
    }

}