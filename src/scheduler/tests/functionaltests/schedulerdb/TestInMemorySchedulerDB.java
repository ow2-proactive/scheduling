package functionaltests.schedulerdb;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;


public class TestInMemorySchedulerDB {

    @Test
    public void sanityTest() throws Exception {
        SchedulerDBManager dbManager = SchedulerDBManager.createInMemorySchedulerDBManager();

        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(BaseSchedulerDBTest.createDefaultTask("task1"));
        jobDef.addTask(BaseSchedulerDBTest.createDefaultTask("task2"));
        jobDef.addTask(BaseSchedulerDBTest.createDefaultTask("task3"));

        InternalJob job = InternalJobFactory.createJob(jobDef, BaseSchedulerDBTest.getDefaultCredentials());
        job.setOwner("test");

        dbManager.newJobSubmitted(job);

        dbManager.readAccount("test");
        dbManager.changeJobPriority(job.getId(), JobPriority.HIGH);
        Assert.assertEquals(1, dbManager.loadNotFinishedJobs(true).size());
    }

}
