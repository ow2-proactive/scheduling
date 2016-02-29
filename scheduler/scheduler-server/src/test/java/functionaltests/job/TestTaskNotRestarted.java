package functionaltests.job;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;


/**
 * Test provokes scenario when task gets 'NOT_RESTARTED' status:
 * - task is submitted and starts execution
 * - user requests to restart task with some delay
 * - before task was restarted job is killed
 *
 */
public class TestTaskNotRestarted extends SchedulerFunctionalTestWithRestart {

    public static class TestJavaTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            Thread.sleep(Long.MAX_VALUE);
            return "OK";
        }
    }

    @Test
    public void test() throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        JobId jobId = scheduler.submit(createJob());

        JobState jobState;

        schedulerHelper.waitForEventTaskRunning(jobId, "task1");
        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.RUNNING, jobState.getTasks().get(0).getStatus());

        scheduler.restartTask(jobId, "task1", Integer.MAX_VALUE);
        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.WAITING_ON_ERROR, jobState.getTasks().get(0).getStatus());

        scheduler.killJob(jobId);

        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.NOT_RESTARTED, jobState.getTasks().get(0).getStatus());
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName("task1");
        javaTask.setMaxNumberOfExecution(10);

        job.addTask(javaTask);

        return job;
    }
}
