package functionaltests.schedulerdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
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
        job.setName("TestLoadJobsPagination");
        job.setDescription("TestLoadJobsPagination desc");
        JavaTask task = new JavaTask();
        task.setExecutableClassName("className");
        job.addTask(task);
        return job;
    }

    @Test
    public void test() throws Exception {
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
        TaskResultImpl result = new TaskResultImpl(null, new TestResult(0, "result"), null, 0, null);
        job.terminateTask(false, task.getId(), null, null, result);
        job.terminate();
        dbManager.updateAfterTaskFinished(job, task, new TaskResultImpl(null, new TestResult(0, "result"),
            null, 0, null));

        // canceled job - 7
        job = defaultSubmitJob(createJob());
        job.failed(job.getITasks().get(0).getId(), JobStatus.CANCELED);
        dbManager.updateAfterJobKilled(job, Collections.<TaskId> emptySet());

        List<JobInfo> jobs;

        jobs = dbManager.loadJobs(5, 1, null, true, true, true);
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

        jobs = dbManager.loadJobs(0, 10, null, true, true, true);
        checkJobs(jobs, 1, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.loadJobs(-1, -1, null, true, true, true);
        checkJobs(jobs, 1, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.loadJobs(-1, 5, null, true, true, true);
        checkJobs(jobs, 1, 2, 3, 4, 5);

        jobs = dbManager.loadJobs(2, -1, null, true, true, true);
        checkJobs(jobs, 3, 4, 5, 6, 7);

        try {
            jobs = dbManager.loadJobs(0, 0, null, true, true, true);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        jobs = dbManager.loadJobs(0, 1, null, true, true, true);
        checkJobs(jobs, 1);

        jobs = dbManager.loadJobs(0, 3, null, true, true, true);
        checkJobs(jobs, 1, 2, 3);

        jobs = dbManager.loadJobs(1, 10, null, true, true, true);
        checkJobs(jobs, 2, 3, 4, 5, 6, 7);

        jobs = dbManager.loadJobs(5, 10, null, true, true, true);
        checkJobs(jobs, 6, 7);

        jobs = dbManager.loadJobs(6, 10, null, true, true, true);
        checkJobs(jobs, 7);

        jobs = dbManager.loadJobs(7, 10, null, true, true, true);
        checkJobs(jobs);

        jobs = dbManager.loadJobs(0, 10, DEFAULT_USER_NAME, true, true, true);
        checkJobs(jobs, 1, 3, 4, 6, 7);

        jobs = dbManager.loadJobs(0, 10, "user1", true, true, true);
        checkJobs(jobs, 2);

        jobs = dbManager.loadJobs(0, 10, DEFAULT_USER_NAME, true, false, false);
        checkJobs(jobs, 1);

        jobs = dbManager.loadJobs(0, 10, DEFAULT_USER_NAME, false, true, false);
        checkJobs(jobs, 3);

        jobs = dbManager.loadJobs(0, 10, DEFAULT_USER_NAME, false, false, true);
        checkJobs(jobs, 4, 6, 7);

        jobs = dbManager.loadJobs(0, 10, DEFAULT_USER_NAME, false, true, true);
        checkJobs(jobs, 3, 4, 6, 7);

        jobs = dbManager.loadJobs(0, 10, DEFAULT_USER_NAME, true, false, true);
        checkJobs(jobs, 1, 4, 6, 7);

        jobs = dbManager.loadJobs(0, 10, DEFAULT_USER_NAME, true, true, false);
        checkJobs(jobs, 1, 3);

        jobs = dbManager.loadJobs(0, 10, DEFAULT_USER_NAME, false, false, false);
        checkJobs(jobs);
    }

    private void checkJobs(List<JobInfo> jobs, Integer... expectedIds) {
        List<Integer> ids = new ArrayList<Integer>();
        for (JobInfo job : jobs) {
            ids.add(Integer.valueOf(job.getJobId().value()));
        }
        Assert.assertEquals(Arrays.asList(expectedIds), ids);
    }
}
