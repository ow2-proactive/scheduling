package functionaltests.cronjobs;

import static functionaltests.SchedulerTHelper.getSchedulerInterface;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;

import functionaltests.SchedulerConsecutive;


public abstract class CronCheckBase extends SchedulerConsecutive {

    protected static final long job_timeout = TimeUnit.MINUTES.toMillis(5);
    protected static final long task_timeout = TimeUnit.MINUTES.toMillis(5);

    protected JobId jobId;

    @Before
    public void setUp() throws Exception {
        getSchedulerInterface().changePolicy(ExtendedSchedulerPolicy.class.getName());
    }

    @After
    public void tearDown() {
        if (jobId != null) {
            try {
                JobState jobState = getSchedulerInterface().getJobState(jobId);
                if (!jobState.isFinished()) {
                    getSchedulerInterface().killJob(jobId);
                }
            } catch (Exception e) {
                System.out.println("An error occurred while killing the job: " + jobId);
                e.printStackTrace();
            }
        }
    }

}
