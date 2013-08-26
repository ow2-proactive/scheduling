package functionaltests.cronjobs;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.junit.Test;

import static functionaltests.SchedulerTHelper.submitJob;
import static functionaltests.SchedulerTHelper.waitForEventTaskFinished;
import static org.junit.Assert.assertEquals;
import static org.ow2.proactive.scheduler.common.task.TaskStatus.FINISHED;
import static org.ow2.proactive.scheduler.common.task.flow.FlowScript.createLoopFlowScript;


/**
 * Check whether a task annotated with 'CRON_EXPR' generic info is executed
 * iteratively.
 */
public class TestCronTask extends CronCheckBase {

    @Test
    public void testSimpleCronTask() throws Exception {
        Job job = createSimpleCronTaskJob();
        jobId = submitJob(job);

        // first iteration
        TaskInfo taskInfo = waitForEventTaskFinished(jobId, "SimpleCronTask", task_timeout);
        assertEquals(FINISHED, taskInfo.getStatus());

        // second iteration
        TaskInfo taskInfo2 = waitForEventTaskFinished(jobId, "SimpleCronTask#1", task_timeout);
        assertEquals(FINISHED, taskInfo2.getStatus());
    }

    private Job createSimpleCronTaskJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("SimpleCronTask");
        task.setFlowScript(createLoopFlowScript("loop = '* * * * *'", "SimpleCronTask"));
        task.setExecutableClassName(SimpleCronTaskExecutable.class.getName());
        job.addTask(task);
        return job;
    }

    public static class SimpleCronTaskExecutable extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "simple-cron-result";
        }
    }

}
