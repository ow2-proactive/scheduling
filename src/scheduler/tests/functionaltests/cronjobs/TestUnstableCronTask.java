package functionaltests.cronjobs;

import static functionaltests.SchedulerTHelper.submitJob;
import static functionaltests.SchedulerTHelper.waitForEventJobFinished;
import static functionaltests.SchedulerTHelper.waitForEventTaskFinished;
import static org.junit.Assert.assertEquals;
import static org.ow2.proactive.scheduler.common.task.TaskStatus.FAULTY;
import static org.ow2.proactive.scheduler.common.task.TaskStatus.FINISHED;

import java.io.File;
import java.io.Serializable;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;

/**
 * Checks whether a cron task is executed iteratively irrespectively of possible
 * errors in previous task executions.
 */
public class TestUnstableCronTask extends CronCheckBase {

    private static final String tmpFile = "unstable-executable.tmp";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        File file = new File(System.getProperty("java.io.tmpdir"), tmpFile);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testUnstableCronTask() throws Exception {
        Job errorJob = createUnstableCronTaskJob();
        jobId = submitJob(errorJob);

        // first iteration - successful
        TaskInfo taskInfo = waitForEventTaskFinished(jobId, "UnstableCronTask",
                task_timeout);
        assertEquals(FINISHED, taskInfo.getStatus());

        // second iteration - failure
        TaskInfo taskInfo2 = waitForEventTaskFinished(jobId,
                "UnstableCronTask#1", task_timeout);
        assertEquals(FAULTY, taskInfo2.getStatus());

        waitForEventJobFinished(jobId, job_timeout);
    }

    private Job createUnstableCronTaskJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("UnstableCronTask");
        task.setFlowScript(FlowScript.createLoopFlowScript(
                "loop = '* * * * *'", "UnstableCronTask"));
        task.setExecutableClassName(UnstableExecutable.class.getName());
        job.addTask(task);
        return job;
    }

    public static class UnstableExecutable extends JavaExecutable {
        /*
         * This will cause an error in every other execution.
         */
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            File file = new File(System.getProperty("java.io.tmpdir"), tmpFile);
            if (file.exists()) {
                file.delete();
                throw new Exception("Unstable task exeception.");
            }
            file.createNewFile();
            return "task-result";
        }
    }

}
