package functionaltests.workflow.cronjobs;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.ow2.proactive.scheduler.common.task.TaskStatus.FAULTY;


/**
 * Checks whether the cron task is executed iteratively irrespective of its
 * previous execution state.
 */

public class TestCronErrorTask extends CronCheckBase {

    @Test
    public void testCronErrorTask() throws Exception {
        Job errorJob = createJob();
        jobId = schedulerHelper.submitJob(errorJob);
        TaskInfo taskInfo = schedulerHelper.waitForEventTaskFinished(jobId, "CronErrorTask", task_timeout);
        assertEquals(FAULTY, taskInfo.getStatus());

        TaskInfo taskInfo2 = schedulerHelper.waitForEventTaskFinished(jobId, "CronErrorTask#1", task_timeout);
        assertEquals(FAULTY, taskInfo2.getStatus());
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
            throw new Exception("Error task exception.");
        }
    }

}
