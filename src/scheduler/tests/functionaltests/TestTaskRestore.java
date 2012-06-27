package functionaltests;

import java.io.File;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.tests.FunctionalTest;


/**
 * Test checks that runtime state of tasks is properly 
 * restored after scheduler is killed and restarted.
 *
 */
public class TestTaskRestore extends FunctionalTest {

    public static class TestJavaTask1 extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "Res ok";
        }

    }

    public static class TestJavaTask2 extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            Thread.sleep(5000);
            if (results.length != 1) {
                return "Results length: " + results.length;
            }
            TaskResult res = results[0];
            String val = (String) res.value();
            if (!val.equals("Res ok")) {
                return "Unexpected parent res: " + val;
            }
            System.out.println("OK " + val);
            return "OK";
        }

    }

    @Test
    public void test() throws Throwable {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Test job");

        JavaTask javaTask1 = new JavaTask();
        javaTask1.setName("task1");
        javaTask1.setExecutableClassName(TestJavaTask1.class.getName());

        JavaTask javaTask2 = new JavaTask();
        javaTask2.setName("task2");
        javaTask2.setExecutableClassName(TestJavaTask2.class.getName());
        javaTask2.addDependence(javaTask1);

        NativeTask nativeTask1 = new NativeTask();
        nativeTask1.setName("task3");

        File script;
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.unix) {
            script = new File(System.getProperty("pa.scheduler.home") +
                "/classes/schedulerTests/functionaltests/executables/test_echo_task.sh");
        } else {
            script = new File(System.getProperty("pa.scheduler.home") +
                "/classes/schedulerTests/functionaltests/executables/test_echo_task.bat");
        }

        if (!script.exists()) {
            Assert.fail("Can't find script " + script.getAbsolutePath());
        }
        nativeTask1.setCommandLine(script.getAbsolutePath());
        nativeTask1.addDependence(javaTask1);

        job.addTask(javaTask1);
        job.addTask(javaTask2);
        job.addTask(nativeTask1);

        System.out.println("Submit job");
        JobId jobId = SchedulerTHelper.submitJob(job);
        System.out.println("Submitted job " + jobId);

        System.out.println("Waiting for task1 to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "task1");

        System.out.println("Killing scheduler");

        SchedulerTHelper.killAndRestartScheduler(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        System.out.println("get state");
        SchedulerState state = scheduler.getState();
        Assert.assertEquals(1, state.getRunningJobs().size());

        System.out.println("State: " + state.getPendingJobs().size() + " " + state.getRunningJobs().size() +
            " " + state.getFinishedJobs().size());

        JobState jobState = state.getRunningJobs().get(0);
        Assert.assertEquals(1, jobState.getNumberOfFinishedTasks());
        Assert.assertEquals(2, jobState.getNumberOfPendingTasks() + jobState.getNumberOfRunningTasks());

        SchedulerTHelper.waitForEventJobFinished(jobId);

        System.out.println("Job finished");

        JobResult jobResult = scheduler.getJobResult(jobId);
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

}
