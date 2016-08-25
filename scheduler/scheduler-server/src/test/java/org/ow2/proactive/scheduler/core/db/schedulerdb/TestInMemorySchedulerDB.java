package org.ow2.proactive.scheduler.core.db.schedulerdb;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.tests.ProActiveTest;


// FIXME: This is not a unit test
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
    
    @Test
    public void sanityTestJobContent() throws Exception {
        SchedulerDBManager dbManager = SchedulerDBManager.createInMemorySchedulerDBManager();

        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(BaseSchedulerDBTest.createDefaultTask("task1"));
        jobDef.addTask(BaseSchedulerDBTest.createDefaultTask("task2"));
        jobDef.addTask(BaseSchedulerDBTest.createDefaultTask("task3"));

        InternalJob job = InternalJobFactory.createJob(jobDef, BaseSchedulerDBTest.getDefaultCredentials());
        job.setOwner("test");

        dbManager.newJobSubmitted(job);

        Job content = dbManager.loadInitalJobContent(job.getId());
        
        Assert.assertTrue(content instanceof TaskFlowJob);
        Assert.assertThat(((TaskFlowJob)content).getTasks().size(), is(3));
    }

}
