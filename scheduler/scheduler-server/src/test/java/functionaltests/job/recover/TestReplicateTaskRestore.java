package functionaltests.job.recover;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.junit.Assert;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;


/**
 * Test checks that runtime state of workflow job with replicated
 * tasks is properly restored after scheduler is killed and restarted.
 * <p/>
 * Following workflow is used:
 *          A
 *          |
 *  ----------------
 *  |       |       |  
 *  |       |       |
 *  B       B       B
 *  |       |       |  
 *  |       |       |
 *  -----------------
 *          |
 *          C
 */
public class TestReplicateTaskRestore extends SchedulerFunctionalTest {

    static final int REPLICATED_NUMBER = 5;

    public static class ReplicateMainTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result = "Main task ok";
            getOut().println(result);
            return result;
        }
    }

    public static class ReplicatedTask extends JavaExecutable {

        private String taskParameter;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result;
            if (!"test".equals(taskParameter)) {
                result = "Unexpected value for parameter: " + taskParameter;
            } else {
                result = "Replicated result " + getReplicationIndex();
            }
            getOut().println(result);
            return result;
        }
    }

    public static class LastTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            if (results.length != REPLICATED_NUMBER) {
                getOut().println("Error, unexpected number of results: " + results.length);
                return "Failed";
            }
            Set<String> expectedResults = new HashSet<>();
            for (int i = 0; i < REPLICATED_NUMBER; i++) {
                expectedResults.add("Replicated result " + i);
            }

            Set<String> actualResults = new HashSet<>();
            for (TaskResult result : results) {
                getOut().println("Received result: " + result.value());
                actualResults.add(result.value().toString());
            }

            if (!expectedResults.equals(actualResults)) {
                getOut().println("Task received unexpected results: " + actualResults + "(expected: " +
                  expectedResults + ")");
                return "Check Failed";
            }

            return "Last task ok";
        }
    }

    @Test
    public void test() throws Throwable {
        TaskFlowJob job = createJob();

        log("Submit job");
        JobId jobId = schedulerHelper.submitJob(job);
        log("Submitted job " + jobId);

        log("Waiting for Main task to finish");
        schedulerHelper.waitForEventTaskFinished(jobId, "Main task");

        log("Killing scheduler");

        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        schedulerHelper.waitForEventJobFinished(jobId);

        log("Job finished");

        JobResult jobResult = scheduler.getJobResult(jobId);
        printResultAndCheckNoErrors(jobResult);

        assertEquals(2 + REPLICATED_NUMBER, jobResult.getAllResults().size());

        TaskResult result;
        result = jobResult.getResult("Main task");
        assertEquals("Main task ok", result.value().toString());

        result = jobResult.getResult("Replication last task");
        assertEquals("Last task ok", result.value().toString());

        for (int i = 0; i < REPLICATED_NUMBER; i++) {
            String taskName;
            if (i == 0) {
                taskName = "Replicated task";
            } else {
                taskName = "Replicated task*" + i;
            }
            result = jobResult.getResult(taskName);
            assertEquals("Replicated result " + i, result.value().toString());
        }

    }

    private void printResultAndCheckNoErrors(JobResult jobResult) throws Throwable {
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            log("Task result for " + taskResult.getTaskId() + " " +
              taskResult.getTaskId().getReadableName());
            if (taskResult.getException() != null) {
                taskResult.getException().printStackTrace();
                Assert.fail("Task failed with exception " + taskResult.getException());
            }
            log("Task output:");
            log(taskResult.getOutput().getAllLogs(false));
            log("Task result value: " + taskResult.value());
        }
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask mainTask = new JavaTask();
        mainTask.setName("Main task");
        mainTask.setExecutableClassName(ReplicateMainTask.class.getName());
        mainTask.setFlowBlock(FlowBlock.START);
        String replicateScript = String.format("runs = %d", REPLICATED_NUMBER);
        mainTask.setFlowScript(FlowScript.createReplicateFlowScript(replicateScript));
        job.addTask(mainTask);

        JavaTask replicatedTask = new JavaTask();
        replicatedTask.setExecutableClassName(ReplicatedTask.class.getName());
        replicatedTask.setName("Replicated task");
        replicatedTask.addDependence(mainTask);
        replicatedTask.addArgument("taskParameter", "test");
        job.addTask(replicatedTask);

        JavaTask lastTask = new JavaTask();
        lastTask.setExecutableClassName(LastTask.class.getName());
        lastTask.setName("Replication last task");
        lastTask.setFlowBlock(FlowBlock.END);
        lastTask.addDependence(replicatedTask);

        job.addTask(lastTask);

        return job;
    }

}
