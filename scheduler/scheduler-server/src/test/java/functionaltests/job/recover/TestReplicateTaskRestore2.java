package functionaltests.job.recover;

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
import org.junit.Assert;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;


/**
 * Test checks that runtime state of workflow job with replicated
 * tasks is properly restored after scheduler is killed and restarted.
 */
public class TestReplicateTaskRestore2 extends SchedulerFunctionalTest {

    static final int REPLICATED_NUMBER = 2;

    public static class TestTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result = "Task " + getReplicationIndex();
            getOut().println(result);
            return result;
        }
    }

    @Test
    public void test() throws Throwable {
        TaskFlowJob job = createJob();

        log("Submit job");
        JobId jobId = schedulerHelper.submitJob(job);
        log("Submitted job " + jobId);

        log("Waiting for first task to finish");
        schedulerHelper.waitForEventTaskFinished(jobId, "T");

        log("Killing scheduler");

        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        schedulerHelper.waitForEventJobFinished(jobId);

        log("Job finished");

        JobResult jobResult = schedulerHelper.getSchedulerInterface().getJobResult(jobId);
        printResultAndCheckNoErrors(jobResult);

        log("Results: " + jobResult.getAllResults().size());

        assertEquals(10, jobResult.getAllResults().size());
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
