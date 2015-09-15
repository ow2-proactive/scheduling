package org.ow2.proactive.scheduler.core.db.schedulerdb;

import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.tests.ProActiveTest;
import org.junit.Assert;
import org.junit.Test;


public class TestInMemorySchedulerDB extends ProActiveTest {

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
