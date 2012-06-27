package functionaltests.schedulerdb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestTaskIdGeneration extends BaseSchedulerDBTest {

    @Test
    public void test() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.addTask(createDefaultTask("task1"));
        jobDef.addTask(createDefaultTask("task2"));
        jobDef.addTask(createDefaultTask("task3"));

        InternalJob job = InternalJobFactory.createJob(jobDef, getDefaultCredentials());
        job.setOwner(DEFAULT_USER_NAME);

        dbManager.newJobSubmitted(job);

        for (InternalTask task : job.getITasks()) {
            Assert.assertSame(task, job.getIHMTasks().get(task.getId()));
        }
        for (EligibleTaskDescriptor task : job.getJobDescriptor().getEligibleTasks()) {
            Assert.assertNotNull(job.getIHMTasks().get(task.getTaskId()));
        }

        checkIds(job);

        SchedulerStateRecoverHelper recoverHelper = new SchedulerStateRecoverHelper(dbManager);
        SchedulerStateRecoverHelper.RecoveredSchedulerState state = recoverHelper.recover();
        Collection<InternalJob> jobs = state.getPendingJobs();
        Assert.assertEquals(1, jobs.size());
        job = jobs.iterator().next();
        checkIds(job);

        JobState jobState = state.getSchedulerState().getPendingJobs().get(0);
        checkIds(jobState);
    }

    private void checkIds(JobState job) throws Exception {
        long jobId = Long.valueOf(job.getId().value());

        Set<String> expected = new HashSet<String>(3);
        expected.add(String.valueOf(jobId * TaskIdImpl.JOB_FACTOR));
        expected.add(String.valueOf(jobId * TaskIdImpl.JOB_FACTOR + 1));
        expected.add(String.valueOf(jobId * TaskIdImpl.JOB_FACTOR + 2));

        Set<String> actual = new HashSet<String>();
        actual.add(findTask(job, "task1").getId().value());
        actual.add(findTask(job, "task2").getId().value());
        actual.add(findTask(job, "task3").getId().value());

        Assert.assertEquals(expected, actual);

        actual = new HashSet<String>();
        actual.add(findTask(job, "task1").getId().getReadableName());
        actual.add(findTask(job, "task2").getId().getReadableName());
        actual.add(findTask(job, "task3").getId().getReadableName());
        expected = new HashSet<String>(3);
        expected.add("task1");
        expected.add("task2");
        expected.add("task3");
        Assert.assertEquals(expected, actual);
    }

}
