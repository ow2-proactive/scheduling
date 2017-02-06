package org.ow2.proactive.scheduler.task.executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.NodeDataSpacesURIs;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.executors.forked.env.ForkedJvmTaskExecutionCommandCreator;
import org.ow2.proactive.scripting.ForkEnvironmentScriptResult;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

public class ForkedProcessBuilderCreatorTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    private String jobNameValue = "TestJobName";
    private String jobOwnerValue = "TestOwner";
    private String taskNameValue = "TestTaskName";
    private long taskIdValue = 20L;
    private long jobIdValue = 12L;
    private int iterationIndexValue = 40;
    private int taskReplicationValue = 20;
    private String testVariable1Key = "TestVariable1";
    private String testVariable1Value = "valueForTest1";
    private String[] forkEnvJvmArguments = new String[] { "Arg1", "Arg2" };
    private String[] forkEnJavaCommandString = new String[] { "My", "Command" };
    private Map<String, String> taskContextExtractedVariables;
    private String extractedVariable1Key = "extracted1Key";
    private String extractedVariable1Value = "extracted1Value";
    private String extractedVariable2Key = "extracted2Key";
    private String extractedVariable2Value = "extracted2Value";

    @Before
    public void init() {
        taskContextExtractedVariables = new HashMap<>();
        taskContextExtractedVariables.put(extractedVariable1Key, extractedVariable1Value);
        taskContextExtractedVariables.put(extractedVariable2Key, extractedVariable2Value);
    }

    @Test
    public void testForEnvPrefixCommandIsAddedToCommand() throws Exception {
        ForkedProcessBuilderCreator forkedProcessBuilderCreator = new ForkedProcessBuilderCreator();

        setMocks(forkedProcessBuilderCreator);

        OSProcessBuilder processBuilder = forkedProcessBuilderCreator.createForkedProcessBuilder(
                createTaskContext(),
                tmpFolder.newFolder(),
                System.out,
                System.out,
                tmpFolder.newFolder());

        assertThat(processBuilder.command(), hasItems(forkEnJavaCommandString));
    }

    @Test(expected = IllegalStateException.class)
    public void testTaskContextVariableExtractorThrowsExceptionAndIsRethrown() throws Exception {
        ForkedProcessBuilderCreator forkedProcessBuilderCreator = new ForkedProcessBuilderCreator();

        TaskContextVariableExtractor taskContextVariableExtractor = mock(TaskContextVariableExtractor.class);
        given(taskContextVariableExtractor.extractVariablesThirdPartyCredentialsAndSystemEnvironmentVariables(
                any(TaskContext.class)))
                .willThrow(IllegalArgumentException.class);

        setPrivateField(ForkedProcessBuilderCreator.class.getDeclaredField("taskContextVariableExtractor"),
                forkedProcessBuilderCreator,
                taskContextVariableExtractor);

        TaskContext taskContext = createTaskContext();

        forkedProcessBuilderCreator.createForkedProcessBuilder(taskContext,
                tmpFolder.newFolder(),
                System.out,
                System.out,
                tmpFolder.newFolder());
    }

    @Test
    public void testEnvPrefixCommandIsAddedToCommandRunAsMeIsTrue() throws Exception {
        ForkedProcessBuilderCreator forkedProcessBuilderCreator = new ForkedProcessBuilderCreator();

        setMocks(forkedProcessBuilderCreator);

        TaskContext taskContext = createTaskContext();
        taskContext.getExecutableContainer().setRunAsUser(true);

        OSProcessBuilder processBuilder = forkedProcessBuilderCreator.createForkedProcessBuilder(
                createTaskContext(),
                tmpFolder.newFolder(),
                System.out,
                System.out,
                tmpFolder.newFolder());

        assertThat(processBuilder.command(), hasItems(forkEnJavaCommandString));
    }

    private void setMocks(ForkedProcessBuilderCreator forkedProcessBuilderCreator) throws Exception {
        ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator = mock(
                ForkedJvmTaskExecutionCommandCreator.class);
        TaskContextVariableExtractor taskContextVariableExtractor = mock(TaskContextVariableExtractor.class);
        ForkEnvironmentScriptExecutor forkEnvironmentScriptExecutor = mock(
                ForkEnvironmentScriptExecutor.class);
        given(forkedJvmTaskExecutionCommandCreator.createForkedJvmTaskExecutionCommand(any(TaskContext.class),
                any(ScriptResult.class), any(String.class))).willReturn(
                Arrays.asList(forkEnJavaCommandString));
        given(taskContextVariableExtractor.extractVariablesThirdPartyCredentialsAndSystemEnvironmentVariables(
                any(TaskContext.class))).willReturn(
                taskContextExtractedVariables);
        given(forkEnvironmentScriptExecutor.executeForkEnvironmentScript(any(TaskContext.class),
                any(PrintStream.class), any(
                        PrintStream.class))).willReturn(
                new ScriptResult<ForkEnvironmentScriptResult>(new ForkEnvironmentScriptResult()));

        setPrivateField(
                ForkedProcessBuilderCreator.class.getDeclaredField("forkedJvmTaskExecutionCommandCreator"),
                forkedProcessBuilderCreator,
                forkedJvmTaskExecutionCommandCreator);
        setPrivateField(ForkedProcessBuilderCreator.class.getDeclaredField("taskContextVariableExtractor"),
                forkedProcessBuilderCreator,
                taskContextVariableExtractor);
        setPrivateField(ForkedProcessBuilderCreator.class.getDeclaredField("forkEnvironmentScriptExecutor"),
                forkedProcessBuilderCreator,
                forkEnvironmentScriptExecutor);
    }

    private TaskContext createTaskContext() throws InvalidScriptException, NodeException {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(
                        "print('hello'); result='hello'", "javascript")));
        TaskLauncherInitializer taskLauncherInitializer = getTaskLauncherInitializerWithWorkflowVariableAndForkEnvironment();

        TaskContext taskContext = new TaskContext(scriptContainer, taskLauncherInitializer, null,
                new NodeDataSpacesURIs(null, null, null, null, null, null), null, null);
        return taskContext;
    }

    private TaskLauncherInitializer getTaskLauncherInitializerWithWorkflowVariableAndForkEnvironment() {
        // Create and setup task launcher initializer
        TaskLauncherInitializer taskLauncherInitializer = createTaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(createForkEnvironment());
        Map<String, JobVariable> variablesToPut = new HashMap<>();
        variablesToPut.put(testVariable1Key, new JobVariable(testVariable1Key, testVariable1Value));
        taskLauncherInitializer.setJobVariables(variablesToPut);
        return taskLauncherInitializer;
    }

    private TaskLauncherInitializer createTaskLauncherInitializer() {
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        TaskId taskId = createTaskId();

        taskLauncherInitializer.setTaskId(taskId);
        taskLauncherInitializer.setIterationIndex(iterationIndexValue);
        taskLauncherInitializer.setJobOwner(jobOwnerValue);
        taskLauncherInitializer.setReplicationIndex(taskReplicationValue);
        return taskLauncherInitializer;
    }

    private ForkEnvironment createForkEnvironment() {
        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.addJVMArgument(forkEnvJvmArguments[0]);
        forkEnv.addJVMArgument(forkEnvJvmArguments[1]);
        return forkEnv;
    }

    private TaskId createTaskId() {
        return TaskIdImpl.createTaskId(new JobIdImpl(jobIdValue, jobNameValue), taskNameValue, taskIdValue);
    }


    /**
     * Sets a private field.
     *
     * @param privateField The private field to set.
     * @param target       Instance of class, in which to set the field.
     * @param value        Value to set the field to.
     */
    private void setPrivateField(Field privateField, Object target,
            Object value) throws IllegalAccessException {
        privateField.setAccessible(true);
        privateField.set(target, value);
        privateField.setAccessible(false);
    }
}