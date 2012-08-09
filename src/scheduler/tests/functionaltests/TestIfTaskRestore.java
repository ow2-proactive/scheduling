package functionaltests;

import java.io.File;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.tests.FunctionalTest;


public class TestIfTaskRestore extends FunctionalTest {

    public static class TaskA extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            Thread.sleep(5000);
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

        System.out.println("Submit job");
        JobId jobId = SchedulerTHelper.submitJob(job);
        System.out.println("Submitted job " + jobId);

        System.out.println("Waiting for task A to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "A");

        System.out.println("Killing scheduler");

        SchedulerTHelper.killAndRestartScheduler(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();
        SchedulerTHelper.waitForEventJobFinished(jobId);

        JobResult jobResult = scheduler.getJobResult(jobId);
        printResultAndCheckNoErrors(jobResult);

        Assert.assertEquals(2, jobResult.getAllResults().size());
        Assert.assertEquals("ResA", jobResult.getResult("A").value().toString());
        Assert.assertEquals("ResB", jobResult.getResult("B").value().toString());
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

    static JavaTask task(String name, Class<?> klass) {
        JavaTask task = new JavaTask();
        task.setExecutableClassName(klass.getName());
        task.setName(name);
        return task;
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

}