package functionaltests.job.recover;

import java.io.File;
import java.io.Serializable;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


public class TestLoopTaskRestore extends SchedulerFunctionalTest {

    static final int ITERATIONS_NUMBER = 3;

    public static class LoopMainTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result = "Main task ok";
            getOut().println(result);
            return result;
        }
    }

    public static class LoopTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            String result = "Loop result " + getIterationIndex();
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

        assertEquals(2 + ITERATIONS_NUMBER, jobResult.getAllResults().size());

        TaskResult result;
        result = jobResult.getResult("Main task");
        assertEquals("Main task ok", result.value().toString());

        for (int i = 0; i <= ITERATIONS_NUMBER; i++) {
            String taskName;
            if (i == 0) {
                taskName = "Loop task";
            } else {
                taskName = "Loop task#" + i;
            }
            result = jobResult.getResult(taskName);
            assertEquals("Loop result " + i, result.value().toString());
        }
    }

    private void printResultAndCheckNoErrors(JobResult jobResult) throws Throwable {
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            log("Task result for " + taskResult.getTaskId() + " " + taskResult.getTaskId().getReadableName());
            if (taskResult.getException() != null) {
                taskResult.getException().printStackTrace();
                fail("Task failed with exception " + taskResult.getException());
            }
            log("Task output:");
            log(taskResult.getOutput().getAllLogs(false));
            log("Task result value: " + taskResult.value());
        }
    }

    protected TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask mainTask = new JavaTask();
        mainTask.setName("Main task");
        mainTask.setExecutableClassName(LoopMainTask.class.getName());
        job.addTask(mainTask);

        String loopScript = String.format(
                "if (variables.get('PA_TASK_ITERATION') < %d) { loop = true; } else { loop = false; }",
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
