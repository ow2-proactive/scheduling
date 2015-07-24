package functionaltests.job.taskkill;

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
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.*;


/**
 * Sanity test against result for killed job.
 *
 */
public class TestKilledJobResult extends SchedulerFunctionalTest {

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

        log("Submit job");
        JobId jobId = schedulerHelper.submitJob(job);
        log("Submitted job " + jobId);

        log("Waiting for task1 to finish");
        schedulerHelper.waitForEventTaskFinished(jobId, "task1");

        schedulerHelper.waitForEventTaskRunning(jobId, "task2");

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        log("Killing job");
        scheduler.killJob(jobId);

        log("Wait when job finishes");
        schedulerHelper.waitForEventJobFinished(jobId);

        JobResult jobResult = scheduler.getJobResult(jobId);
        printResult(jobResult);

        assertEquals(1, jobResult.getAllResults().size());
        assertEquals("OK", jobResult.getAllResults().get("task1").value().toString());

        JobInfo jobInfo = jobResult.getJobInfo();
        assertEquals(JobStatus.KILLED, jobInfo.getStatus());
        assertEquals(1, jobInfo.getNumberOfFinishedTasks());
        assertEquals(2, jobInfo.getTotalNumberOfTasks());
        assertEquals(0, jobInfo.getNumberOfPendingTasks());
        assertEquals(0, jobInfo.getNumberOfRunningTasks());

        JobState state = scheduler.getJobState(jobId);
        assertEquals(JobStatus.KILLED, state.getStatus());
        assertEquals(1, state.getNumberOfFinishedTasks());
        assertEquals(2, state.getTotalNumberOfTasks());
        assertEquals(0, state.getNumberOfPendingTasks());
        assertEquals(0, state.getNumberOfRunningTasks());

        assertEquals(2, state.getTasks().size());
        assertEquals(TaskStatus.FINISHED, findTask(state, "task1").getStatus());
        assertEquals(TaskStatus.ABORTED, findTask(state, "task2").getStatus());

        TaskState taskState0 = state.getTasks().get(0);
        TaskState taskState1 = state.getTasks().get(1);

        assertTrue(taskState0.getStartTime() > 0);
        assertTrue(taskState0.getFinishedTime() > 0);
        assertTrue(taskState0.getExecutionDuration() >= 0);
        assertTrue(
          taskState0.getExecutionDuration() <= taskState0.getFinishedTime() - taskState0.getStartTime());

        assertTrue(taskState1.getStartTime() > 0);
        assertTrue(taskState1.getFinishedTime() > 0);
        assertTrue(taskState1.getExecutionDuration() >= 0);
        assertTrue(
          taskState1.getExecutionDuration() <= taskState1.getFinishedTime() - taskState1.getStartTime());
    }

    protected TaskState findTask(JobState jobState, String taskName) {
        for (TaskState taskState : jobState.getTasks()) {
            if (taskState.getName().equals(taskName)) {
                return taskState;
            }
        }
        fail("Didn't find task with name " + taskName);
        return null;
    }

    private void printResult(JobResult jobResult) throws Throwable {
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            log("Task result for " + taskResult.getTaskId() + " " +
              taskResult.getTaskId().getReadableName());

            if (taskResult.getException() != null) {
                log("Task exception:");
                taskResult.getException().printStackTrace(System.out);
            } else {
                log("Task output:");
                log(taskResult.getOutput().getAllLogs(false));
                log("Task result value: " + taskResult.value());
            }
        }
    }

}
