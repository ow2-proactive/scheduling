package org.ow2.proactive.scheduler.task.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.Object2ByteConverter;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TaskContextVariableExtractorTest {
    private String jobNameValue = "TestJobName";
    private String jobOwnerValue = "TestOwner";
    private String taskNameValue = "TestTaskName";
    private long taskIdValue = 20L;
    private long jobIdValue = 12L;
    private int iterationIndexValue = 40;
    private int taskReplicationValue = 20;

    private String testVariable1Key = "TestVariable1";
    private String testVariable1Value = "valueForTest1";

    private String taskResultPropagatedVariables1Key = "TaskResultVariable1";
    private String taskResultPropagatedVariables1Value = "TaksResultValue1";

    private String nodesfileContent = "Nodesfilecontent";

    @Test(expected = Exception.class)
    public void testExtractThrowsExceptionIfVariablesAreInvalidByteStream() throws Exception {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(
                        "print('hello'); result='hello'", "javascript")));
        TaskLauncherInitializer taskLauncherInitializer = getTaskLauncherInitializerWithWorkflowVariable();

        Map<String, byte[]> taskResultVariables = new HashMap<>();
        // The task result variables are expected to be converted to byte streams.
        taskResultVariables.put(taskResultPropagatedVariables1Key,
                taskResultPropagatedVariables1Value.getBytes());

        TaskResultImpl taskResult = new TaskResultImpl(taskLauncherInitializer.getTaskId(),
                new Exception("Exception"));
        taskResult.setPropagatedVariables(taskResultVariables);
        TaskResult[] taskResultArray = { taskResult };

        TaskContext taskContext = new TaskContext(scriptContainer, taskLauncherInitializer, taskResultArray,
                null, null,
                null, null, null, null, null);

        new TaskContextVariableExtractor().extractTaskVariables(taskContext, (TaskResult) null);

    }

    @Test(expected = NullPointerException.class)
    public void testExtractThrowsNullPointerExceptionIfTaskContextIsNull() throws Exception {

        Map<String, Serializable> contextVariables
                = new TaskContextVariableExtractor().extractTaskVariables(null, (TaskResult) null);
    }

    @Test
    public void testExtractTaskResultVariablesFromPreviousTaskResultsInsideTheTaskContext() throws Exception {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(
                        "print('hello'); result='hello'", "javascript")));
        TaskLauncherInitializer taskLauncherInitializer = getTaskLauncherInitializerWithWorkflowVariable();

        Map<String, byte[]> taskResultVariables = new HashMap<>();
        // The task result variables are expected to be converted to byte streams.
        taskResultVariables.put(taskResultPropagatedVariables1Key,
                Object2ByteConverter.convertObject2Byte(taskResultPropagatedVariables1Value));

        TaskResultImpl taskResult = new TaskResultImpl(taskLauncherInitializer.getTaskId(),
                new Exception("Exception"));
        taskResult.setPropagatedVariables(taskResultVariables);
        TaskResult[] taskResultArray = { taskResult };

        TaskContext taskContext = new TaskContext(scriptContainer, taskLauncherInitializer, taskResultArray,
                null, null,
                null, null, null, null, null);


        Map<String, Serializable> contextVariables
                = new TaskContextVariableExtractor().extractTaskVariables(taskContext, (TaskResult) null);

        assertThat((String) contextVariables.get(taskResultPropagatedVariables1Key),
                is(taskResultPropagatedVariables1Value));
    }

    @Test
    public void testExtractTaskResultVariablesFromTaskResult() throws Exception {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(
                        "print('hello'); result='hello'", "javascript")));
        TaskLauncherInitializer taskLauncherInitializer = getTaskLauncherInitializerWithWorkflowVariable();


        TaskContext taskContext = new TaskContext(scriptContainer, taskLauncherInitializer, null, null, null,
                null, null, null, null, null);

        Map<String, byte[]> taskResultVariables = new HashMap<>();

        // The task result variables are expected to be converted to byte streams.
        taskResultVariables.put(taskResultPropagatedVariables1Key,
                Object2ByteConverter.convertObject2Byte(taskResultPropagatedVariables1Value));

        TaskResultImpl taskResult = new TaskResultImpl(taskContext.getTaskId(), new Exception("Exception"));
        taskResult.setPropagatedVariables(taskResultVariables);

        Map<String, Serializable> contextVariables
                = new TaskContextVariableExtractor().extractTaskVariables(taskContext, taskResult);

        assertThat((String) contextVariables.get(taskResultPropagatedVariables1Key),
                is(taskResultPropagatedVariables1Value));
    }

    @Test
    public void testExtractNodefileVariablesAndProgressVariablesWithGivenNodesfile() throws Exception {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(
                        "print('hello'); result='hello'", "javascript")));
        TaskLauncherInitializer taskLauncherInitializer = getTaskLauncherInitializerWithWorkflowVariable();

        TaskContext taskContext = new TaskContext(scriptContainer, taskLauncherInitializer, null, null, null,
                null, null, null, null, null);
        Map<String, Serializable> contextVariables
                = new TaskContextVariableExtractor().extractTaskVariables(taskContext, nodesfileContent);

        assertThat("Nodes number must be equal to number of other nodes + 1 (for the own node).",
                (int) contextVariables.get(SchedulerVars.PA_NODESNUMBER.toString()),
                is(taskContext.getOtherNodesURLs().size() + 1));
        assertThat("Node file content of passed node file must be represented as a variable.",
                (String) contextVariables.get(SchedulerVars.PA_NODESFILE.toString()),
                is(nodesfileContent));
        assertThat("Task progress path must be represented as a variable.",
                (String) contextVariables.get(SchedulerVars.PA_TASK_PROGRESS_FILE.toString()),
                is(taskContext.getProgressFilePath()));
    }

    @Test
    public void testExtractWorkflowVariablesFromTaskLauncherInitializerInsideTaskContext() throws Exception {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(
                        "print('hello'); result='hello'", "javascript")));
        TaskLauncherInitializer taskLauncherInitializer = getTaskLauncherInitializerWithWorkflowVariable();

        TaskContext taskContext = new TaskContext(scriptContainer, taskLauncherInitializer, null, null, null,
                null, null, null, null, null);

        Map<String, Serializable> contextVariables
                = new TaskContextVariableExtractor().extractTaskVariables(taskContext);

        assertThat((String) contextVariables.get(testVariable1Key), is(testVariable1Value));
    }

    private TaskLauncherInitializer getTaskLauncherInitializerWithWorkflowVariable() {
        // Create and setup task launcher initializer
        TaskLauncherInitializer taskLauncherInitializer = createTaskLauncherInitializer();
        Map<String, String> variablesToPut = new HashMap<>();
        variablesToPut.put(testVariable1Key, testVariable1Value);
        taskLauncherInitializer.setVariables(variablesToPut);
        return taskLauncherInitializer;
    }

    @Test
    public void testExtractTaskLauncherInitializerVariablesFromTaskContext() throws Exception {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(
                new TaskScript(new SimpleScript(
                        "print('hello'); result='hello'", "javascript")));
        TaskContext taskContext = new TaskContext(scriptContainer, createTaskLauncherInitializer(), null,
                null, null, null, null, null, null, null);

        Map<String, Serializable> contextVariables
                = new TaskContextVariableExtractor().extractTaskVariables(taskContext);

        validateExtractedVariablesFromTaskLauncherInitializer(contextVariables);
    }

    @Test
    public void testExtractTaskVariablesFromTaskLauncherInitializer() throws Exception {
        TaskLauncherInitializer taskLauncherInitializer = createTaskLauncherInitializer();

        Map<String, Serializable> contextVariables
                = new TaskContextVariableExtractor().retrieveContextVariables(taskLauncherInitializer);

        // assertThat
        validateExtractedVariablesFromTaskLauncherInitializer(contextVariables);
    }

    private void validateExtractedVariablesFromTaskLauncherInitializer(
            Map<String, Serializable> contextVariables) {
        // Validate that variables are actually set
        assertThat((String) contextVariables.get(SchedulerVars.PA_JOB_NAME.toString()), is(jobNameValue));
        assertThat((String) contextVariables.get(SchedulerVars.PA_USER.toString()), is(jobOwnerValue));
        assertThat((String) contextVariables.get(SchedulerVars.PA_TASK_NAME.toString()), is(taskNameValue));
        assertThat((String) contextVariables.get(SchedulerVars.PA_TASK_ID.toString()),
                is(Long.toString(taskIdValue)));
        assertThat((String) contextVariables.get(SchedulerVars.PA_JOB_ID.toString()),
                is(Long.toString(jobIdValue)));
        assertThat((Integer) contextVariables.get(SchedulerVars.PA_TASK_ITERATION.toString()),
                is(iterationIndexValue));
        assertThat((Integer) contextVariables.get(SchedulerVars.PA_TASK_REPLICATION.toString()),
                is(taskReplicationValue));
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


}