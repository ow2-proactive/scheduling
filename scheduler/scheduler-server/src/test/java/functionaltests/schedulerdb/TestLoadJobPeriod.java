package functionaltests.schedulerdb;

import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.InternalJob;


public class TestLoadJobPeriod extends BaseSchedulerDBTest {

    @Test
    public void test() throws Exception {
        final long currrentTime = System.currentTimeMillis();

        final long hour = 1000 * 60 * 60;
        final long day = 1000 * 60 * 60 * 24;

        // add one not finished job
        defaultSubmitJob(new TaskFlowJob());

        addFinishedJob(currrentTime - hour);
        addFinishedJob(currrentTime - 2 * hour);
        addFinishedJob(currrentTime - 3 * hour);
        addFinishedJob(currrentTime - 2 * day);
        addFinishedJob(currrentTime - 10 * day);

        Assert.assertEquals(5, dbManager.loadFinishedJobs(false, -1).size());
        Assert.assertEquals(3, dbManager.loadFinishedJobs(false, day).size());
        Assert.assertEquals(4, dbManager.loadFinishedJobs(false, 3 * day).size());
        Assert.assertEquals(3, dbManager.loadFinishedJobs(false, 4 * hour).size());
    }

    private void addFinishedJob(long submittedTime) throws Exception {
        InternalJob job = defaultSubmitJob(new TaskFlowJob(), DEFAULT_USER_NAME, submittedTime);
        job.failed(null, JobStatus.KILLED);
        dbManager.updateAfterJobKilled(job, Collections.<TaskId> emptySet());

    }
}
