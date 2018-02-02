package performancetests.metrics;

import functionaltests.utils.SchedulerTHelper;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import performancetests.recovery.PeformanceTestBase;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

@RunWith(Parameterized.class)
public class GetResultMetricTest extends PeformanceTestBase {

    private static final Logger LOGGER = Logger.getLogger(GetResultMetricTest.class);

    SchedulerTHelper schedulerHelper;


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{1, 3000}});
    }

    private final int taskNumber;

    private final long timeLimit;

    private JobId jobId;

    public GetResultMetricTest(int taskNumber, int timeLimit) {
        this.taskNumber = taskNumber;
        this.timeLimit = timeLimit;
    }

    @Test(timeout = 3600000)
    public void test() throws Exception {

        final int taskDuration = 1;
        final int nodeNumber = 1;

        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                SchedulerEfficiencyMetricsTest.SCHEDULER_CONFIGURATION_START.getPath(),
                SchedulerEfficiencyMetricsTest.RM_CONFIGURATION_START.getPath(),
                null);

        schedulerHelper.createNodeSourceWithInfiniteTimeout("local", nodeNumber);

        final TaskFlowJob job = SchedulerEfficiencyMetricsTest.createJob(taskNumber, taskDuration);

        final long start = System.currentTimeMillis();

        jobId = schedulerHelper.submitJob(job);

        schedulerHelper.waitForEventJobFinished(jobId);

        final JobResult jobResult = schedulerHelper.getSchedulerInterface().getJobResult(jobId);

        long timeToGetResult = System.currentTimeMillis() - start - (taskDuration * 1000);

        LOGGER.info(makeCSVString(TaskCreationTimeTest.class.getSimpleName(),
                taskNumber,
                timeLimit,
                timeToGetResult,
                ((timeToGetResult < timeLimit) ? SUCCESS : FAILURE)));

        assertThat(String.format("Task creation rate for job with %s tasks", taskNumber),
                timeToGetResult,
                lessThan(timeLimit));
    }

    @After
    public void after() throws Exception {
        if (schedulerHelper != null) {
            if (!schedulerHelper.getSchedulerInterface().getJobState(jobId).isFinished()) {
                schedulerHelper.getSchedulerInterface().killJob(jobId);
            }
            schedulerHelper.getSchedulerInterface().removeJob(jobId);
            schedulerHelper.log("Kill Scheduler after test.");
            schedulerHelper.killScheduler();
        }
    }
}
