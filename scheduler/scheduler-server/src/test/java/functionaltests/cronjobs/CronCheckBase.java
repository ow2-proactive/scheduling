package functionaltests.cronjobs;

import java.util.concurrent.TimeUnit;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.junit.After;

import functionaltests.SchedulerConsecutive;

import static functionaltests.SchedulerTHelper.getSchedulerInterface;


public abstract class CronCheckBase extends SchedulerConsecutive {

    protected static final long task_timeout = TimeUnit.MINUTES.toMillis(5);

    protected JobId jobId;

    @After
    public void tearDown() throws Exception {
        if (jobId != null) {
            JobState jobState = getSchedulerInterface().getJobState(jobId);
            if (!jobState.isFinished()) {
                getSchedulerInterface().killJob(jobId);
            }
        }
    }

}
