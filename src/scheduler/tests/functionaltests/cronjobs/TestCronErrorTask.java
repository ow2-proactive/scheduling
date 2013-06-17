package functionaltests.cronjobs;

import static functionaltests.SchedulerTHelper.submitJob;
import static functionaltests.SchedulerTHelper.waitForEventJobFinished;
import static functionaltests.SchedulerTHelper.waitForEventTaskFinished;
import static org.junit.Assert.assertEquals;
import static org.ow2.proactive.scheduler.common.task.TaskStatus.FAULTY;

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
 * Checks whether the cron task is executed iteratively irrespective of its
 * previous execution state.
 */

public class TestCronErrorTask extends CronCheckBase {

    @Test
    public void testCronErrorTask() throws Exception {
        Job errorJob = createJob();
        jobId = submitJob(errorJob);
        TaskInfo taskInfo = waitForEventTaskFinished(jobId, "CronErrorTask", task_timeout);
        assertEquals(FAULTY, taskInfo.getStatus());
        waitForEventJobFinished(jobId, job_timeout);
    }

    private Job createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("CronErrorTask");
        task.setFlowScript(FlowScript.createLoopFlowScript("loop = '* * * * * '", "CronErrorTask"));
        task.setExecutableClassName(ErrorExecutable.class.getName());
        job.addTask(task);
        return job;
    }

    public static class ErrorExecutable extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            throw new Exception("Error task exeception.");
        }
    }

}
