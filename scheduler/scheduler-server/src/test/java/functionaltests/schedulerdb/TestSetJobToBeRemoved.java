package functionaltests.schedulerdb;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.job.InternalJob;


public class TestSetJobToBeRemoved extends BaseSchedulerDBTest {

    @Test
    public void test() throws Exception {
        InternalJob job = defaultSubmitJobAndLoadInternal(false, new TaskFlowJob());
        Assert.assertFalse(job.isToBeRemoved());

        job.setToBeRemoved();
        System.out.println("Set job to be removed");
        dbManager.jobSetToBeRemoved(job.getId());

        SchedulerStateRecoverHelper recoverHelper = new SchedulerStateRecoverHelper(dbManager);
        Collection<InternalJob> jobs = recoverHelper.recover(-1).getPendingJobs();
        Assert.assertEquals(1, jobs.size());
        job = jobs.iterator().next();

        Assert.assertTrue(job.isToBeRemoved());
    }

}
