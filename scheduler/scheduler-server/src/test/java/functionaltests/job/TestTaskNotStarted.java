package functionaltests.job;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.examples.EmptyTask;
import org.ow2.proactive.scripting.SelectionScript;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;

import static org.junit.Assert.*;


/**
 * Test provokes scenario when task gets 'NOT_STARTED' status
 * (job is killed when task has 'pending' or 'submitted' status) . 
 *
 */
public class TestTaskNotStarted extends SchedulerFunctionalTest {

    @Test
    public void test() throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        JobId jobId;
        JobState jobState;

        jobId = scheduler.submit(createJob1());
        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.SUBMITTED, jobState.getTasks().get(0).getStatus());
        scheduler.killJob(jobId);
        jobState = scheduler.getJobState(jobId);
        assertEquals(1, jobState.getTasks().size());
        assertEquals(TaskStatus.NOT_STARTED, jobState.getTasks().get(0).getStatus());

        jobId = scheduler.submit(createJob2());
        schedulerHelper.waitForEventJobRunning(jobId);
        jobState = scheduler.getJobState(jobId);
        assertEquals(2, jobState.getTasks().size());
        assertEquals(TaskStatus.PENDING, getTask(jobState, "task2").getStatus());
        scheduler.killJob(jobId);
        jobState = scheduler.getJobState(jobId);
        assertEquals(2, jobState.getTasks().size());
        assertEquals(TaskStatus.NOT_STARTED, getTask(jobState, "task2").getStatus());
    }

    /*
     * Job with one task, task's selection script always returns 'false' so task can't start
     */
    private TaskFlowJob createJob1() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_1");

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        javaTask.setName("task1");
        SelectionScript selScript = new SelectionScript("selected = false;", "js");
        javaTask.setSelectionScript(selScript);

        job.addTask(javaTask);

        return job;
    }

    /*
     * Job with two task, one task without selection script, and one task with selection script
     * always returning 'false' so this task can't start
     */
    private TaskFlowJob createJob2() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_2");

        JavaTask javaTask1 = new JavaTask();
        javaTask1.setExecutableClassName(EmptyTask.class.getName());
        javaTask1.setName("task1");

        JavaTask javaTask2 = new JavaTask();
        javaTask2.setExecutableClassName(EmptyTask.class.getName());
        javaTask2.setName("task2");
        SelectionScript selScript = new SelectionScript("selected = false;", "js");
        javaTask2.setSelectionScript(selScript);

        job.addTask(javaTask1);
        job.addTask(javaTask2);

        return job;
    }

    private TaskState getTask(JobState jobState, String taskName) {
        for (TaskState task : jobState.getTasks()) {
            if (task.getName().equals(taskName)) {
                return task;
            }
        }
        fail("Failed to find task " + taskName);
        return null;
    }
}
