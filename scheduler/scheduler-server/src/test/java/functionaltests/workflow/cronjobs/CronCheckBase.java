package functionaltests.workflow.cronjobs;

import java.util.concurrent.TimeUnit;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.junit.After;

import functionaltests.utils.SchedulerFunctionalTest;


public abstract class CronCheckBase extends SchedulerFunctionalTest {

    protected static final long task_timeout = TimeUnit.MINUTES.toMillis(5);

    protected JobId jobId;

    @After
    public void tearDown() throws Exception {
        if (jobId != null) {
            JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(jobId);
            if (!jobState.isFinished()) {
                schedulerHelper.getSchedulerInterface().killJob(jobId);
            }
        }
    }

}
