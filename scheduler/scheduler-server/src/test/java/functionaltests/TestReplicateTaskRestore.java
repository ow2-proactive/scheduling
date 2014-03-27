package functionaltests;

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
import org.ow2.tests.FunctionalTest;
import org.junit.Assert;
import org.junit.Test;


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
public class TestReplicateTaskRestore extends FunctionalTest {

    static final int REPLICATED_NUMBER = 5;

    public static class ReplicateMainTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result = "Main task ok";
            System.out.println(result);
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
            System.out.println(result);
            return result;
        }
    }

    public static class LastTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            if (results.length != REPLICATED_NUMBER) {
                System.out.println("Error, unexpected number of results: " + results.length);
                return "Failed";
            }
            Set<String> expectedResults = new HashSet<String>();
            for (int i = 0; i < REPLICATED_NUMBER; i++) {
                expectedResults.add("Replicated result " + i);
            }

            Set<String> actualResults = new HashSet<String>();
            for (int i = 0; i < results.length; i++) {
                System.out.println("Received result: " + results[i].value());
                actualResults.add(results[i].value().toString());
            }

            if (!expectedResults.equals(actualResults)) {
                System.out.println("Task received unexpected results: " + actualResults + "(expected: " +
                    expectedResults + ")");
                return "Check Failed";
            }

            return "Last task ok";
        }
    }

    @Test
    public void test() throws Throwable {
        TaskFlowJob job = createJob();

        System.out.println("Submit job");
        JobId jobId = SchedulerTHelper.submitJob(job);
        System.out.println("Submitted job " + jobId);

        System.out.println("Waiting for Main task to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "Main task");

        System.out.println("Killing scheduler");

        SchedulerTHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        SchedulerTHelper.waitForEventJobFinished(jobId);

        System.out.println("Job finished");

        JobResult jobResult = scheduler.getJobResult(jobId);
        printResultAndCheckNoErrors(jobResult);

        Assert.assertEquals(2 + REPLICATED_NUMBER, jobResult.getAllResults().size());

        TaskResult result;
        result = jobResult.getResult("Main task");
        Assert.assertEquals("Main task ok", result.value().toString());

        result = jobResult.getResult("Replication last task");
        Assert.assertEquals("Last task ok", result.value().toString());

        for (int i = 0; i < REPLICATED_NUMBER; i++) {
            String taskName;
            if (i == 0) {
                taskName = "Replicated task";
            } else {
                taskName = "Replicated task*" + i;
            }
            result = jobResult.getResult(taskName);
            Assert.assertEquals("Replicated result " + i, result.value().toString());
        }

    }

    private void printResultAndCheckNoErrors(JobResult jobResult) throws Throwable {
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            System.out.println("Task result for " + taskResult.getTaskId() + " " +
                taskResult.getTaskId().getReadableName());
            if (taskResult.getException() != null) {
                taskResult.getException().printStackTrace();
                Assert.fail("Task failed with exception " + taskResult.getException());
            }
            System.out.println("Task output:");
            System.out.println(taskResult.getOutput().getAllLogs(false));
            System.out.println("Task result value: " + taskResult.value());
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
