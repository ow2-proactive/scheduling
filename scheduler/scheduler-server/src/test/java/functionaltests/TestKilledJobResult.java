package functionaltests;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.junit.Assert;
import org.junit.Test;


/**
 * Sanity test against result for killed job.
 *
 */
public class TestKilledJobResult extends SchedulerConsecutive {

    public static class TestJavaTask1 extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "OK";
        }

    }

    public static class TestJavaTask2 extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            Thread.sleep(5 * 60000);
            return "OK";
        }

    }

    @Test
    public void test() throws Throwable {
        TaskFlowJob job = new TaskFlowJob();
        JavaTask task1 = new JavaTask();
        task1.setName("task1");
        task1.setExecutableClassName(TestJavaTask1.class.getName());
        job.addTask(task1);

        JavaTask task2 = new JavaTask();
        task2.setName("task2");
        task2.setExecutableClassName(TestJavaTask2.class.getName());
        job.addTask(task2);

        task2.addDependence(task1);

        System.out.println("Submit job");
        JobId jobId = SchedulerTHelper.submitJob(job);
        System.out.println("Submitted job " + jobId);

        System.out.println("Waiting for task1 to finish");
        SchedulerTHelper.waitForEventTaskFinished(jobId, "task1");

        SchedulerTHelper.waitForEventTaskRunning(jobId, "task2");

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        System.out.println("Killing job");
        scheduler.killJob(jobId);

        System.out.println("Wait when job finishes");
        SchedulerTHelper.waitForEventJobFinished(jobId);

        JobResult jobResult = scheduler.getJobResult(jobId);
        printResult(jobResult);

        Assert.assertEquals(1, jobResult.getAllResults().size());
        Assert.assertEquals("OK", jobResult.getAllResults().get("task1").value().toString());

        JobInfo jobInfo = jobResult.getJobInfo();
        Assert.assertEquals(JobStatus.KILLED, jobInfo.getStatus());
        Assert.assertEquals(1, jobInfo.getNumberOfFinishedTasks());
        Assert.assertEquals(2, jobInfo.getTotalNumberOfTasks());
        Assert.assertEquals(0, jobInfo.getNumberOfPendingTasks());
        Assert.assertEquals(0, jobInfo.getNumberOfRunningTasks());

        JobState state = scheduler.getJobState(jobId);
        Assert.assertEquals(JobStatus.KILLED, state.getStatus());
        Assert.assertEquals(1, state.getNumberOfFinishedTasks());
        Assert.assertEquals(2, state.getTotalNumberOfTasks());
        Assert.assertEquals(0, state.getNumberOfPendingTasks());
        Assert.assertEquals(0, state.getNumberOfRunningTasks());

        Assert.assertEquals(2, state.getTasks().size());
        Assert.assertEquals(TaskStatus.FINISHED, findTask(state, "task1").getStatus());
        Assert.assertEquals(TaskStatus.ABORTED, findTask(state, "task2").getStatus());

        TaskState taskState0 = state.getTasks().get(0);
        TaskState taskState1 = state.getTasks().get(1);

        Assert.assertTrue(taskState0.getStartTime() > 0);
        Assert.assertTrue(taskState0.getFinishedTime() > 0);
        Assert.assertTrue(taskState0.getExecutionDuration() >= 0);
        Assert.assertTrue(taskState0.getExecutionDuration() < taskState1.getFinishedTime() -
            taskState1.getStartTime());

        Assert.assertTrue(taskState1.getStartTime() > 0);
        Assert.assertTrue(taskState1.getFinishedTime() > 0);
        Assert.assertTrue(taskState1.getExecutionDuration() >= 0);
        Assert.assertTrue(taskState1.getExecutionDuration() <= taskState1.getFinishedTime() -
            taskState1.getStartTime());
    }

    protected TaskState findTask(JobState jobState, String taskName) {
        for (TaskState taskState : jobState.getTasks()) {
            if (taskState.getName().equals(taskName)) {
                return taskState;
            }
        }
        Assert.fail("Didn't find task with name " + taskName);
        return null;
    }

    private void printResult(JobResult jobResult) throws Throwable {
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            System.out.println("Task result for " + taskResult.getTaskId() + " " +
                taskResult.getTaskId().getReadableName());

            if (taskResult.getException() != null) {
                System.out.println("Task exception:");
                taskResult.getException().printStackTrace(System.out);
            } else {
                System.out.println("Task output:");
                System.out.println(taskResult.getOutput().getAllLogs(false));
                System.out.println("Task result value: " + taskResult.value());
            }
        }
    }

}
