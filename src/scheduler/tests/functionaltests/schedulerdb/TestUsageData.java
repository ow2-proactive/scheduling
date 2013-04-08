package functionaltests.schedulerdb;

import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.ow2.proactive.scheduler.common.usage.TaskUsage;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestUsageData extends BaseSchedulerDBTest {

    private static final String USER_WITH_JOBS = "bob";
    private static final String USER_WITHOUT_JOBS = "albert";

    @Test
    public void testEmptyDatabase() throws Exception {
        List<JobUsage> usages = dbManager.getUsage(USER_WITH_JOBS, new Date(), new Date());
        assertTrue(usages.isEmpty());
    }

    @Test(expected = DatabaseManagerException.class)
    public void testNullDatesDatabase() throws Exception {
        dbManager.getUsage(USER_WITH_JOBS, null, null);
    }

    @Test
    public void testNonEmptyDatabase() throws Exception {
        Date beforeJobExecution = new Date();

        InternalJob job = defaultSubmitJob(createJob("job", "task1"), USER_WITH_JOBS);
        InternalTask task = job.getITasks().get(0);

        job.start();
        startTask(job, task);
        finishTask(job, task);

        Date afterJobExecution = new Date();

        List<JobUsage> usagesBeforeJobRan = dbManager.getUsage(USER_WITH_JOBS, beforeJobExecution,
                beforeJobExecution);
        assertTrue(usagesBeforeJobRan.isEmpty());

        List<JobUsage> usagesAfterJobRan = dbManager.getUsage(USER_WITH_JOBS, afterJobExecution,
                afterJobExecution);
        assertTrue(usagesAfterJobRan.isEmpty());

        List<JobUsage> usagesForDifferentUser = dbManager.getUsage(USER_WITHOUT_JOBS, beforeJobExecution,
                afterJobExecution);
        assertTrue(usagesForDifferentUser.isEmpty());

        List<JobUsage> usagesWithinJobRun = dbManager.getUsage(USER_WITH_JOBS, beforeJobExecution,
                afterJobExecution);
        assertFalse(usagesWithinJobRun.isEmpty());

        JobUsage onlyOneUsage = usagesWithinJobRun.get(0);
        assertEquals("job", onlyOneUsage.getJobName());
        assertTrue(onlyOneUsage.getJobDuration() > 0);

        TaskUsage onlyOneTaskUsage = onlyOneUsage.getTaskUsages().get(0);
        assertEquals("task1", onlyOneTaskUsage.getTaskName());
        assertEquals(1, onlyOneTaskUsage.getTaskNodeNumber());
        assertTrue(onlyOneTaskUsage.getTaskExecutionDuration() > 0);
    }

    private TaskFlowJob createJob(String name, String taskName) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(name);
        job.setPriority(JobPriority.IDLE);
        JavaTask task = new JavaTask();
        task.setName(taskName);
        task.setExecutableClassName("className");
        job.addTask(task);
        return job;
    }

    protected InternalTask startTask(InternalJob job, InternalTask task) throws Exception {
        super.startTask(job, task);
        dbManager.jobTaskStarted(job, task, false);
        return task;
    }

    private void finishTask(InternalJob job, InternalTask task) throws Exception {
        Thread.sleep(10);
        TaskResultImpl res = new TaskResultImpl(null, "ok", null, 42, null);
        job.terminateTask(false, task.getId(), null, null, res);
        if (job.isFinished()) {
            job.terminate();
        }
        dbManager.updateAfterTaskFinished(job, task, res);
    }
}
