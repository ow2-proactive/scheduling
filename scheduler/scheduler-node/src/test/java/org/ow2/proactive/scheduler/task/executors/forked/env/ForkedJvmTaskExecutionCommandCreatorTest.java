package org.ow2.proactive.scheduler.task.executors.forked.env;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.NodeDataSpacesURIs;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.executors.forked.env.command.JavaPrefixCommandExtractor;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

public class ForkedJvmTaskExecutionCommandCreatorTest {
    private String jobNameValue = "TestJobName";
    private String jobOwnerValue = "TestOwner";
    private String taskNameValue = "TestTaskName";
    private long taskIdValue = 20L;
    private long jobIdValue = 12L;
    private int iterationIndexValue = 40;
    private int taskReplicationValue = 20;

    private String testVariable1Key = "TestVariable1";
    private String testVariable1Value = "valueForTest1";

    private String[] testPreJaveCommandString = new String[] { "My", "Command" };
    private String[] forkEnvJvmArguments = new String[] { "Arg1", "Arg2" };
    private String serializedContextAbsolutePath = "/some/absolute/path.file";
    private String additionalClasspath = "additionalClasspath";
    private String forkenvironmentJavaHome = "java/home/from/fork/env";

    @Test
    public void testAllNullReturnsAnEmptyList() throws Exception {
        ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator = new ForkedJvmTaskExecutionCommandCreator();
        List<String> containsJavaHome = forkedJvmTaskExecutionCommandCreator.createForkedJvmTaskExecutionCommand(
                null,
                null,
                null);
        assertThat(containsJavaHome.size(), is(0));
    }

    @Test
    public void testExecCommandUsesJavaHomeFromSystemProperties() throws Exception {
        javaCommandContains(Arrays.asList(new String[] { System.getProperty("java.home") }),
                createForkEnvironment());
    }

    @Test
    public void testExecCommandUsesClassPathSystemProperties() throws Exception {
        javaCommandContains(Arrays.asList(new String[] { "-cp", System.getProperty("java.class.path") }),
                createForkEnvironment());
    }

    @Test
    public void testExecCommandContainsJavaCommandPrefix() throws Exception {
        javaCommandContains(Arrays.asList(testPreJaveCommandString), createForkEnvironment());
    }

    @Test
    public void testExecCommandContainsJavaArgumentsExtractedFromForkEnvironment() throws Exception {
        javaCommandContains(Arrays.asList(forkEnvJvmArguments), createForkEnvironment());
    }

    @Test
    public void testExecCommandContainsAbsolutePathOfSerializedContext() throws Exception {
        javaCommandContains(Arrays.asList(serializedContextAbsolutePath), createForkEnvironment());
    }

    @Test
    public void testExecCommandContainsAdditionalClasspathSavedInForkEnvironment() throws Exception {
        javaCommandContains(Arrays.asList(additionalClasspath), createForkEnvironment());
    }

    @Test
    public void testExecCommandOverwritesJavaHomeFromForkEnvironment() throws Exception {
        ForkEnvironment forkEnvironment = createForkEnvironment();
        forkEnvironment.setJavaHome(forkenvironmentJavaHome);
        javaCommandContains(Arrays.asList(forkenvironmentJavaHome), forkEnvironment);
    }


    private void javaCommandContains(List<String> stringsContained,
            ForkEnvironment forkEnvironment) throws Exception {
        ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator = new ForkedJvmTaskExecutionCommandCreator();
        replaceJavaPrefixCommandCreatorWithMock(forkedJvmTaskExecutionCommandCreator);

        TaskContext taskContext = createTaskContext();
        taskContext.getInitializer().setForkEnvironment(forkEnvironment);

        List<String> containsJavaHome = forkedJvmTaskExecutionCommandCreator.createForkedJvmTaskExecutionCommand(
                taskContext,
                null,
                serializedContextAbsolutePath);

        for (String insideJavaCommand : stringsContained) {
            assertThatListHasAtLeastOneStringWhichContains(containsJavaHome,
                    insideJavaCommand);
        }
    }

    /**
     * Replaces the java prefix command creator service with a mock
     *
     * @param forkedJvmTaskExecutionCommandCreator Instance which will have the service replaced.
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    private void replaceJavaPrefixCommandCreatorWithMock(
            ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator) throws IllegalAccessException, NoSuchFieldException {
        JavaPrefixCommandExtractor javaPrefixCommandExtractor = mock(JavaPrefixCommandExtractor.class);
        given(javaPrefixCommandExtractor.extractJavaPrefixCommandToCommandListFromScriptResult(any(
                ScriptResult.class))).willReturn(Arrays.asList(testPreJaveCommandString));

        setPrivateField(
                ForkedJvmTaskExecutionCommandCreator.class.getDeclaredField("javaPrefixCommandExtractor"),
                forkedJvmTaskExecutionCommandCreator,
                javaPrefixCommandExtractor);
    }

    /**
     * Assert that a list if strings has at least one string which contains as a substring the matchig string.
     *
     * @param list     List of strings.
     * @param matching String to look for in the list of strings.
     */
    private void assertThatListHasAtLeastOneStringWhichContains(List<String> list, String matching) {
        boolean hasOneStringContaining = false;
        for (String string : list) {
            if (string.contains(matching)) {
                hasOneStringContaining = true;
            }
        }
        assertThat("List did not contain string with: " + matching + ".\n But was: " + list,
                hasOneStringContaining,
                is(true));
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

    private TaskContext createTaskContext() throws InvalidScriptException, NodeException {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(
                        "print('hello'); result='hello'", "javascript")));
        TaskLauncherInitializer taskLauncherInitializer = getTaskLauncherInitializerWithWorkflowVariableAndForkEnvironment();

        TaskContext taskContext = new TaskContext(scriptContainer, taskLauncherInitializer, null,
                new NodeDataSpacesURIs(null, null, null, null, null, null), null, null);
        return taskContext;
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


    private TaskId createTaskId() {
        return TaskIdImpl.createTaskId(new JobIdImpl(jobIdValue, jobNameValue), taskNameValue, taskIdValue);
    }


    private TaskLauncherInitializer getTaskLauncherInitializerWithWorkflowVariableAndForkEnvironment() {
        // Create and setup task launcher initializer
        TaskLauncherInitializer taskLauncherInitializer = createTaskLauncherInitializer();
        Map<String, JobVariable> variablesToPut = new HashMap<>();
        variablesToPut.put(testVariable1Key, new JobVariable(testVariable1Key, testVariable1Value));
        taskLauncherInitializer.setJobVariables(variablesToPut);
        return taskLauncherInitializer;
    }

    private ForkEnvironment createForkEnvironment() {
        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.addJVMArgument(forkEnvJvmArguments[0]);
        forkEnv.addJVMArgument(forkEnvJvmArguments[1]);
        forkEnv.addAdditionalClasspath(additionalClasspath);
        return forkEnv;
    }
}