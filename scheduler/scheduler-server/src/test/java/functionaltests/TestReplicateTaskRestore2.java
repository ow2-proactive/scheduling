package functionaltests;

import java.io.File;
import java.io.Serializable;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
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
 */
public class TestReplicateTaskRestore2 extends FunctionalTest {

    static final int REPLICATED_NUMBER = 2;

    public static class TestTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result = "Task " + getReplicationIndex();
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

        System.out.println("Waiting for first task to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "T");

        System.out.println("Killing scheduler");

        SchedulerTHelper.killAndRestartScheduler(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        SchedulerTHelper.waitForEventJobFinished(jobId);

        System.out.println("Job finished");

        JobResult jobResult = SchedulerTHelper.getSchedulerInterface().getJobResult(jobId);
        printResultAndCheckNoErrors(jobResult);

        System.out.println("Results: " + jobResult.getAllResults().size());

        Assert.assertEquals(10, jobResult.getAllResults().size());
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

    static JavaTask task(String name) {
        JavaTask task = new JavaTask();
        task.setExecutableClassName(TestTask.class.getName());
        task.setName(name);
        return task;
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask t = task("T");
        JavaTask t1 = task("T1");
        JavaTask t2 = task("T2");
        JavaTask t3 = task("T3");
        JavaTask t4 = task("T4");

        t1.addDependence(t);
        t2.addDependence(t1);
        t3.addDependence(t2);
        t4.addDependence(t3);

        String replicateScript = String.format("runs = %d", REPLICATED_NUMBER);
        t.setFlowScript(FlowScript.createReplicateFlowScript(replicateScript));

        t1.setFlowBlock(FlowBlock.START);
        t1.setFlowScript(FlowScript.createReplicateFlowScript(replicateScript));

        t3.setFlowBlock(FlowBlock.END);

        job.addTask(t);
        job.addTask(t1);
        job.addTask(t2);
        job.addTask(t3);
        job.addTask(t4);

        return job;
    }

}
