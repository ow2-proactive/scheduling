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


public class TestIfTaskRestore extends SchedulerFunctionalTest {

    public static class TaskA extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "ResA";
        }
    }

    public static class TaskB extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            Thread.sleep(5000);
            return "ResB";
        }
    }

    public static class TaskC extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            throw new Exception("Shouldn't start");
        }
    }

    @Test
    public void test() throws Throwable {
        TaskFlowJob job = createJob();

        log("Submit job");
        JobId jobId = schedulerHelper.submitJob(job);
        log("Submitted job " + jobId);

        log("Waiting for task A to finish");
        schedulerHelper.waitForEventTaskFinished(jobId, "A");

        log("Killing scheduler");

        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        schedulerHelper.waitForEventJobFinished(jobId);

        JobResult jobResult = scheduler.getJobResult(jobId);
        printResultAndCheckNoErrors(jobResult);

        assertEquals(2, jobResult.getAllResults().size());
        assertEquals("ResA", jobResult.getResult("A").value().toString());
        assertEquals("ResB", jobResult.getResult("B").value().toString());
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask A = task("A", TaskA.class);
        FlowScript ifScript = FlowScript.createIfFlowScript("branch = \"if\";", "B", "C", null);
        A.setFlowScript(ifScript);
        job.addTask(A);

        JavaTask B = task("B", TaskB.class);
        job.addTask(B);

        JavaTask C = task("C", TaskC.class);
        job.addTask(C);

        return job;
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

    static JavaTask task(String name, Class<?> klass) {
        JavaTask task = new JavaTask();
        task.setExecutableClassName(klass.getName());
        task.setName(name);
        return task;
    }

}