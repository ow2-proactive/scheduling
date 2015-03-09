package functionaltests;

import java.io.File;
import java.io.Serializable;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.tests.FunctionalTest;
import org.junit.Assert;
import org.junit.Test;


public class TestLoopTaskRestore extends FunctionalTest {

    static final int ITERATIONS_NUMBER = 3;

    public static class LoopMainTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result = "Main task ok";
            System.out.println(result);
            return result;
        }
    }

    public static class LoopTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result = "Loop result " + getIterationIndex();
            System.out.println(result);
            return result;
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

        Assert.assertEquals(2 + ITERATIONS_NUMBER, jobResult.getAllResults().size());

        TaskResult result;
        result = jobResult.getResult("Main task");
        Assert.assertEquals("Main task ok", result.value().toString());

        for (int i = 0; i <= ITERATIONS_NUMBER; i++) {
            String taskName;
            if (i == 0) {
                taskName = "Loop task";
            } else {
                taskName = "Loop task#" + i;
            }
            result = jobResult.getResult(taskName);
            Assert.assertEquals("Loop result " + i, result.value().toString());
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

    protected TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask mainTask = new JavaTask();
        mainTask.setName("Main task");
        mainTask.setExecutableClassName(LoopMainTask.class.getName());
        job.addTask(mainTask);

        String loopScript = String.format("if (variables.get('pas.task.iteration') < %d) { loop = true; } else { loop = false; }",
                ITERATIONS_NUMBER);

        JavaTask loopTask = new JavaTask();
        loopTask.setExecutableClassName(LoopTask.class.getName());
        loopTask.setName("Loop task");
        loopTask.addDependence(mainTask);
        loopTask.setFlowScript(FlowScript.createLoopFlowScript(loopScript, loopTask.getName()));
        job.addTask(loopTask);

        return job;
    }

}
