package functionaltests.schedulerdb;

import org.junit.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestReportingQueries extends BaseSchedulerDBTest {

    @Test
    public void doTest() throws Exception {
        checkInvalidIds();

        checkJobAndTasksNumbers(0, 0, 0, 0, 0, 0, 0, 0);
        checkMeanPendingTime();
        checkMeanExecutionTime();
        checkMeanSubmittingPeriod();

        TaskFlowJob jobDef1 = new TaskFlowJob();
        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestDummyExecutable.class.getName());
        javaTask.setName("task1");
        jobDef1.addTask(javaTask);

        JavaTask forkJavaTask = createDefaultTask("task2");
        forkJavaTask.setExecutableClassName(TestDummyExecutable.class.getName());
        forkJavaTask.setForkEnvironment(new ForkEnvironment());
        jobDef1.addTask(forkJavaTask);

        NativeTask nativeTask = new NativeTask();
        nativeTask.setName("task3");
        nativeTask.setCommandLine("command");
        jobDef1.addTask(nativeTask);

        InternalJob job1 = defaultSubmitJobAndLoadInternal(true, jobDef1);
        try {
            dbManager.getJobPendingTime(job1.getJobInfo().getJobId().value());
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getJobRunningTime(job1.getJobInfo().getJobId().value());
            Assert.fail();
        } catch (RuntimeException e) {
        }

        checkJobAndTasksNumbers(1, 0, 0, 1, 3, 0, 0, 3);
        checkMeanSubmittingPeriod(job1);
        checkMeanTaskPendingTime(job1);
        checkMeanTaskRunningTime(job1);
        checkNumberOfHosts(job1, 0);

        Thread.sleep(100);

        InternalJob job2 = defaultSubmitJobAndLoadInternal(true, jobDef1);
        checkMeanSubmittingPeriod(job1, job2);
        checkJobAndTasksNumbers(2, 0, 0, 2, 6, 0, 0, 6);

        // job1: task1 started
        job1.start();
        startTask(job1, job1.getTask("task1"));
        dbManager.jobTaskStarted(job1, job1.getTask("task1"), true);
        checkNumberOfHosts(job1, 1);

        checkJobPendingTime(job1);
        checkMeanPendingTime(job1);
        checkMeanTaskPendingTime(job1);
        checkMeanTaskRunningTime(job1);
        checkJobAndTasksNumbers(1, 1, 0, 2, 5, 1, 0, 6);

        // job1: task1 finished
        finishTask(job1, "task1");

        checkJobAndTasksNumbers(1, 1, 0, 2, 5, 0, 1, 6);
        checkMeanTaskRunningTime(job1);
        checkNumberOfHosts(job1, 1);

        // job2: task1 started
        job2.start();
        startTask(job2, job2.getTask("task1"));
        dbManager.jobTaskStarted(job2, job2.getTask("task1"), true);

        checkMeanPendingTime(job1, job2);
        checkMeanTaskRunningTime(job2);
        checkJobAndTasksNumbers(0, 2, 0, 2, 4, 1, 1, 6);

        // job1: task2 and task3 started
        startTask(job1, job1.getTask("task2"));
        dbManager.jobTaskStarted(job1, job1.getTask("task2"), false);
        startTask(job1, job1.getTask("task3"));
        dbManager.jobTaskStarted(job1, job1.getTask("task3"), false);

        checkMeanTaskPendingTime(job1);
        checkMeanTaskRunningTime(job1);
        checkJobAndTasksNumbers(0, 2, 0, 2, 2, 3, 1, 6);

        // job1: task2 and task3 finished, job1 finished
        finishTask(job1, "task2");
        finishTask(job1, "task3");

        checkJobRunningTime(job1);
        checkMeanExecutionTime(job1);
        checkMeanTaskRunningTime(job1);
        checkJobAndTasksNumbers(0, 1, 1, 2, 2, 1, 3, 6);
        checkNumberOfHosts(job1, 1);

        // job2: task1, task2 and task3 finished, job2 finished
        finishTask(job2, "task1");
        finishTask(job2, "task2");
        finishTask(job2, "task3");

        checkJobRunningTime(job2);
        checkMeanExecutionTime(job1, job2);
        checkMeanTaskRunningTime(job2);
        checkJobAndTasksNumbers(0, 0, 2, 2, 0, 0, 6, 6);

        // remove job2
        dbManager.removeJob(job2.getId(), System.currentTimeMillis(), false);

        checkJobAndTasksNumbers(0, 0, 1, 1, 0, 0, 3, 3);

        checkMeanPendingTime(job1, job2);
        checkMeanExecutionTime(job1, job2);
        checkMeanSubmittingPeriod(job1, job2);

        InternalJob job3 = defaultSubmitJobAndLoadInternal(true, jobDef1);
        checkMeanSubmittingPeriod(job1, job2, job3);
    }

    private void checkNumberOfHosts(InternalJob job, int expected) {
        Assert.assertEquals(expected, dbManager
                .getTotalNumberOfHostsUsed(job.getJobInfo().getJobId().value()));
    }

    private void checkJobPendingTime(InternalJob job) {
        Assert.assertEquals(job.getStartTime() - job.getSubmittedTime(), dbManager.getJobPendingTime(job
                .getJobInfo().getJobId().value()));
    }

    private void checkMeanTaskPendingTime(InternalJob job) {
        job = loadInternalJob(true, job.getId());
        double expected = 0;
        int counter = 0;
        for (InternalTask task : job.getITasks()) {
            if (task.getStartTime() > 0) {
                expected += task.getStartTime() - job.getSubmittedTime();
                counter++;
            }
        }
        if (counter == 0) {
            expected = 0;
        } else {
            expected /= counter;
        }
        Assert.assertEquals(expected, dbManager.getMeanTaskPendingTime(job.getJobInfo().getJobId().value()),
                001);
    }

    private void checkMeanTaskRunningTime(InternalJob job) {
        job = loadInternalJob(true, job.getId());
        double expected = 0;
        int counter = 0;
        for (InternalTask task : job.getITasks()) {
            if (task.getStartTime() > 0 && task.getFinishedTime() > 0) {
                expected += (task.getFinishedTime() - task.getStartTime());
                counter++;
            }
        }
        if (counter == 0) {
            expected = 0;
        } else {
            expected /= counter;
        }
        Assert.assertEquals(expected, dbManager.getMeanTaskRunningTime(job.getJobInfo().getJobId().value()),
                001);
    }

    private void checkJobRunningTime(InternalJob job) {
        Assert.assertEquals(job.getFinishedTime() - job.getStartTime(), dbManager.getJobRunningTime(job
                .getJobInfo().getJobId().value()));
    }

    private void checkMeanSubmittingPeriod(InternalJob... jobs) {
        double expected = 0;
        if (jobs.length > 1) {
            for (int i = 1; i < jobs.length; i++) {
                InternalJob job = jobs[i];
                InternalJob prevoiusJob = jobs[i - 1];
                expected += job.getSubmittedTime() - prevoiusJob.getSubmittedTime();
            }
            expected /= (jobs.length - 1);
        }
        Assert.assertEquals(expected, dbManager.getMeanJobSubmittingPeriod(), 0.001);
    }

    private void checkMeanPendingTime(InternalJob... jobs) {
        double expected = 0;
        if (jobs.length > 0) {
            for (InternalJob job : jobs) {
                expected += (job.getStartTime() - job.getSubmittedTime());
            }
            expected /= jobs.length;
        }
        Assert.assertEquals(expected, dbManager.getMeanJobPendingTime(), 0.001);
    }

    private void checkMeanExecutionTime(InternalJob... jobs) {
        double expected = 0;
        if (jobs.length > 0) {
            for (InternalJob job : jobs) {
                expected += (job.getFinishedTime() - job.getStartTime());
            }
            expected /= jobs.length;
        }
        Assert.assertEquals(expected, dbManager.getMeanJobExecutionTime(), 0.001);
    }

    private void checkJobAndTasksNumbers(long pendingJobs, long runningJobs, long finishedJobs,
            long totalJobs, long pendingTasks, long runningTasks, long finishedTasks, long totalTasks) {
        Assert.assertEquals(pendingJobs, dbManager.getPendingJobsCount());
        Assert.assertEquals(runningJobs, dbManager.getRunningJobsCount());
        Assert.assertEquals(finishedJobs, dbManager.getFinishedJobsCount());
        Assert.assertEquals(totalJobs, dbManager.getTotalJobsCount());

        Assert.assertEquals(pendingTasks, dbManager.getPendingTasksCount());
        Assert.assertEquals(runningTasks, dbManager.getRunningTasksCount());
        Assert.assertEquals(finishedTasks, dbManager.getFinishedTasksCount());
        Assert.assertEquals(totalTasks, dbManager.getTotalTasksCount());
    }

    private long finishTask(InternalJob job, String taskName) throws Exception {
        Thread.sleep(100);

        InternalTask task = job.getTask(taskName);
        TaskResultImpl res = new TaskResultImpl(null, "ok", null, 0, null);
        job.terminateTask(false, task.getId(), null, null, res);
        if (job.isFinished()) {
            job.terminate();
        }

        dbManager.updateAfterTaskFinished(job, task, res);

        return task.getFinishedTime() - task.getStartTime();
    }

    private void checkInvalidIds() {
        try {
            dbManager.getJobPendingTime("invalid_id");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getJobRunningTime("invalid_id");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getMeanTaskPendingTime("invalid_id");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getMeanTaskRunningTime("invalid_id");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getTotalNumberOfHostsUsed("invalid_id");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getJobPendingTime("0");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getJobRunningTime("0");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getMeanTaskPendingTime("0");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getMeanTaskRunningTime("0");
            Assert.fail();
        } catch (RuntimeException e) {
        }
        try {
            dbManager.getTotalNumberOfHostsUsed("0");
            Assert.fail();
        } catch (RuntimeException e) {
        }
    }

}
