package org.ow2.proactive.scheduler.core.db.schedulerdb;

import java.util.Collection;
import java.util.Set;

import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.db.SchedulerStateRecoverHelper;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;


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
        SchedulerStateRecoverHelper.RecoveredSchedulerState state = recoverHelper.recover(-1);
        Collection<InternalJob> jobs = state.getPendingJobs();
        Assert.assertEquals(1, jobs.size());
        job = jobs.iterator().next();
        checkIds(job);

        JobState jobState = state.getSchedulerState().getPendingJobs().get(0);
        checkIds(jobState);
    }

    private void checkIds(JobState job) throws Exception {
        Set<String> expected = ImmutableSet.of("0", "1", "2");

        Set<String> actual =
                ImmutableSet.of(
                        getTaskValue(job, "task1"),
                        getTaskValue(job, "task2"),
                        getTaskValue(job, "task3"));

        Assert.assertEquals(expected, actual);

        actual =
                ImmutableSet.of(
                        getTaskReadableName(job, "task1"),
                        getTaskReadableName(job, "task2"),
                        getTaskReadableName(job, "task3")
                );

        expected = ImmutableSet.of("task1", "task2", "task3");

        Assert.assertEquals(expected, actual);
    }

    private String getTaskReadableName(JobState job, String taskName) {
        return getTaskId(job, taskName).getReadableName();
    }

    private String getTaskValue(JobState job, String taskName) {
        return getTaskId(job, taskName).value();
    }

    private TaskId getTaskId(JobState job, String taskName) {
        return findTask(job, taskName).getId();
    }

}
