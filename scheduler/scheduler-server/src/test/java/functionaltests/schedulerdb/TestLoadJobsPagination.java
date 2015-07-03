package functionaltests.schedulerdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestLoadJobsPagination extends BaseSchedulerDBTest {

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.setDescription("TestLoadJobsPagination desc");
        JavaTask task = new JavaTask();
        task.setExecutableClassName("className");
        job.addTask(task);
        return job;
    }

    private TaskFlowJob createJob(String name, JobPriority priority) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(name);
        job.setPriority(priority);
        JavaTask task = new JavaTask();
        task.setExecutableClassName("className");
        job.addTask(task);
        return job;
    }

    @Test
    public void testSorting() throws Exception {
        InternalJob job1 = defaultSubmitJob(createJob("A", JobPriority.IDLE), "user_a"); // 1
        defaultSubmitJob(createJob("B", JobPriority.LOWEST), "user_b"); // 2
        InternalJob job3 = defaultSubmitJob(createJob("C", JobPriority.LOW), "user_c"); // 3
        defaultSubmitJob(createJob("A", JobPriority.NORMAL), "user_d"); // 4
        InternalJob job5 = defaultSubmitJob(createJob("B", JobPriority.HIGH), "user_e"); // 5
        defaultSubmitJob(createJob("C", JobPriority.HIGHEST), "user_f"); // 6

        // change status for some jobs 
        job1.failed(null, JobStatus.KILLED);
        dbManager.updateAfterJobKilled(job1, Collections.<TaskId> emptySet());

        job3.setPaused();
        dbManager.updateJobAndTasksState(job3);

        job5.start();
        InternalTask taskJob5 = startTask(job5, job5.getITasks().get(0));
        dbManager.jobTaskStarted(job5, taskJob5, true);

        List<JobInfo> jobs;

        jobs = dbManager.getJobs(0, 10, null, true, true, true,
                sortParameters(new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.ASC)));
        checkJobs(jobs, 1, 2, 3, 4, 5, 6);

        jobs = dbManager.getJobs(0, 10, null, true, true, true,
                sortParameters(new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.DESC)));
        checkJobs(jobs, 6, 5, 4, 3, 2, 1);

        jobs = dbManager.getJobs(0, 10, null, true, true, true, sortParameters(
                new SortParameter<JobSortParameter>(JobSortParameter.NAME, SortOrder.ASC),
                new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.ASC)));
        checkJobs(jobs, 1, 4, 2, 5, 3, 6);

        jobs = dbManager.getJobs(0, 10, null, true, true, true, sortParameters(
                new SortParameter<JobSortParameter>(JobSortParameter.NAME, SortOrder.ASC),
                new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.DESC)));
        checkJobs(jobs, 4, 1, 5, 2, 6, 3);

        jobs = dbManager.getJobs(0, 10, null, true, true, true,
                sortParameters(new SortParameter<JobSortParameter>(JobSortParameter.OWNER, SortOrder.ASC)));
        checkJobs(jobs, 1, 2, 3, 4, 5, 6);

        jobs = dbManager.getJobs(0, 10, null, true, true, true,
                sortParameters(new SortParameter<JobSortParameter>(JobSortParameter.OWNER, SortOrder.DESC)));
        checkJobs(jobs, 6, 5, 4, 3, 2, 1);

        jobs = dbManager
                .getJobs(0, 10, null, true, true, true, sortParameters(new SortParameter<JobSortParameter>(
                    JobSortParameter.PRIORITY, SortOrder.ASC)));
        checkJobs(jobs, 1, 2, 3, 4, 5, 6);

        jobs = dbManager
                .getJobs(0, 10, null, true, true, true, sortParameters(new SortParameter<JobSortParameter>(
                    JobSortParameter.PRIORITY, SortOrder.DESC)));
        checkJobs(jobs, 6, 5, 4, 3, 2, 1);

        jobs = dbManager.getJobs(0, 10, null, true, true, true, sortParameters(
                new SortParameter<JobSortParameter>(JobSortParameter.STATE, SortOrder.ASC),
                new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.ASC)));
        checkJobs(jobs, 2, 4, 6, 3, 5, 1);

        jobs = dbManager.getJobs(0, 10, null, true, true, true, sortParameters(
                new SortParameter<JobSortParameter>(JobSortParameter.STATE, SortOrder.DESC),
                new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.ASC)));
        checkJobs(jobs, 1, 3, 5, 2, 4, 6);
    }

    @Test
    public void testPagingAndFilteting() throws Exception {
        InternalJob job;
        InternalTask task;

        // pending job - 1
        defaultSubmitJob(createJob());

        // job for user1 - 2
        defaultSubmitJob(createJob(), "user1");

        // running job - 3
        job = defaultSubmitJob(createJob());
        job.start();
        task = startTask(job, job.getITasks().get(0));
        dbManager.jobTaskStarted(job, task, true);

        // killed job - 4
        job = defaultSubmitJob(createJob());
        job.failed(null, JobStatus.KILLED);
        dbManager.updateAfterJobKilled(job, Collections.<TaskId> emptySet());

        // job for user2 - 5
        defaultSubmitJob(createJob(), "user2");

        // finished job - 6
        job = defaultSubmitJob(createJob());
        job.start();
        task = startTask(job, job.getITasks().get(0));
        dbManager.jobTaskStarted(job, task, true);
        TaskResultImpl result = new TaskResultImpl(null, new TestResult(0, "result"), null, 0);
        job.terminateTask(false, task.getId(), null, null, result);
        job.terminate();
        dbManager.updateAfterTaskFinished(job, task, new TaskResultImpl(null, new TestResult(0, "result"),
            null, 0));

        // canceled job - 7
        job = defaultSubmitJob(createJob());
        job.failed(job.getITasks().get(0).getId(), JobStatus.CANCELED);
        dbManager.updateAfterJobKilled(job, Collections.<TaskId> emptySet());

        // job marked as removed, method 'getJobs' shouldn't return it
        job = defaultSubmitJob(createJob());
        dbManager.removeJob(job.getId(), System.currentTimeMillis(), false);

        List<JobInfo> jobs;

        List<SortParameter<JobSortParameter>> sortParameters = new ArrayList<SortParameter<JobSortParameter>>();
        sortParameters.add(new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.ASC));

        jobs = dbManager.getJobs(5, 1, null, true, true, true, sortParameters);
        JobInfo jobInfo = jobs.get(0);
        Assert.assertEquals("6", jobInfo.getJobId().value());
        Assert.assertEquals(JobStatus.FINISHED, jobInfo.getStatus());
        Assert.assertEquals("TestLoadJobsPagination", jobInfo.getJobId().getReadableName());
        Assert.assertEquals(1, jobInfo.getTotalNumberOfTasks());
        Assert.assertEquals(1, jobInfo.getNumberOfFinishedTasks());
        Assert.assertEquals(0, jobInfo.getNumberOfRunningTasks());
        Assert.assertEquals(0, jobInfo.getNumberOfPendingTasks());
        Assert.assertEquals(JobPriority.NORMAL, jobInfo.getPriority());
        Assert.assertEquals(DEFAULT_USER_NAME, jobInfo.getJobOwner());

        jobs = dbManager.getJobs(0, 10, null, true, true, true, sortParameters);
        checkJobs(jobs, 1, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.getJobs(-1, -1, null, true, true, true, sortParameters);
        checkJobs(jobs, 1, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.getJobs(-1, 5, null, true, true, true, sortParameters);
        checkJobs(jobs, 1, 2, 3, 4, 5);

        jobs = dbManager.getJobs(2, -1, null, true, true, true, sortParameters);
        checkJobs(jobs, 3, 4, 5, 6, 7);

        try {
            jobs = dbManager.getJobs(0, 0, null, true, true, true, sortParameters);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        jobs = dbManager.getJobs(0, 1, null, true, true, true, sortParameters);
        checkJobs(jobs, 1);

        jobs = dbManager.getJobs(0, 3, null, true, true, true, sortParameters);
        checkJobs(jobs, 1, 2, 3);

        jobs = dbManager.getJobs(1, 10, null, true, true, true, sortParameters);
        checkJobs(jobs, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.getJobs(5, 10, null, true, true, true, sortParameters);
        checkJobs(jobs, 6, 7);

        jobs = dbManager.getJobs(6, 10, null, true, true, true, sortParameters);
        checkJobs(jobs, 7);

        jobs = dbManager.getJobs(7, 10, null, true, true, true, sortParameters);
        checkJobs(jobs);

        jobs = dbManager.getJobs(0, 10, DEFAULT_USER_NAME, true, true, true, sortParameters);
        checkJobs(jobs, 1, 3, 4, 6, 7);

        jobs = dbManager.getJobs(0, 10, "user1", true, true, true, sortParameters);
        checkJobs(jobs, 2);

        jobs = dbManager.getJobs(0, 10, DEFAULT_USER_NAME, true, false, false, sortParameters);
        checkJobs(jobs, 1);

        jobs = dbManager.getJobs(0, 10, DEFAULT_USER_NAME, false, true, false, sortParameters);
        checkJobs(jobs, 3);

        jobs = dbManager.getJobs(0, 10, DEFAULT_USER_NAME, false, false, true, sortParameters);
        checkJobs(jobs, 4, 6, 7);

        jobs = dbManager.getJobs(0, 10, DEFAULT_USER_NAME, false, true, true, sortParameters);
        checkJobs(jobs, 3, 4, 6, 7);

        jobs = dbManager.getJobs(0, 10, DEFAULT_USER_NAME, true, false, true, sortParameters);
        checkJobs(jobs, 1, 4, 6, 7);

        jobs = dbManager.getJobs(0, 10, DEFAULT_USER_NAME, true, true, false, sortParameters);
        checkJobs(jobs, 1, 3);

        jobs = dbManager.getJobs(0, 10, DEFAULT_USER_NAME, false, false, false, sortParameters);
        checkJobs(jobs);
    }

    private List<SortParameter<JobSortParameter>> sortParameters(SortParameter<JobSortParameter>... params) {
        return Arrays.asList(params);
    }

    private void checkJobs(List<JobInfo> jobs, Integer... expectedIds) {
        List<Integer> ids = new ArrayList<Integer>();
        for (JobInfo job : jobs) {
            ids.add(Integer.valueOf(job.getJobId().value()));
        }
        Assert.assertEquals(Arrays.asList(expectedIds), ids);
    }
}
