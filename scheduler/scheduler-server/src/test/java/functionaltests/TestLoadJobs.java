package functionaltests;

import com.google.common.collect.ImmutableList;
import functionaltests.monitor.MonitorEventReceiver;
import functionaltests.monitor.SchedulerMonitorsHandler;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.*;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.util.FileLock;
import org.ow2.tests.FunctionalTest;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;


/**
 * Test against method Scheduler.loadJobs 
 *
 */
public class TestLoadJobs extends FunctionalTest {


    private static final List<SortParameter<JobSortParameter>> SORT_BY_ID_ASC =
            ImmutableList.of(
                    new SortParameter<>(JobSortParameter.ID, SortOrder.ASC));

    private static final List<SortParameter<JobSortParameter>> SORT_BY_ID_DESC =
            ImmutableList.of(
                    new SortParameter<>(JobSortParameter.ID, SortOrder.DESC));


    public static class TestJavaTask extends JavaExecutable {

        private String fileLockPath;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            FileLock.waitUntilUnlocked(Paths.get(fileLockPath));
            return "OK";
        }

    }

//    @Test
//    public void testLoadNoJob() throws Exception {
//        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();
//        List<JobInfo> jobs = scheduler.getJobs(0, 0, criteria(true, true, true, true), null);
//        assertTrue(jobs.isEmpty());
//    }

    @Test
    public void testLoadJobs() throws Exception {
        long time = System.currentTimeMillis();

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        FileLock fileLock = new FileLock();
        Path lock = fileLock.lock();
        String fileLockPath = lock.toString();

        JobId jobId;
        List<JobInfo> jobs;
        JobInfo job;

        jobs = scheduler.getJobs(0, 1, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs);

        jobId = scheduler.submit(createJob(fileLockPath));
        SchedulerTHelper.waitForEventTaskRunning(jobId, "Test task");

        jobs = scheduler.getJobs(0, 1, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 1);
        job = jobs.get(0);
        Assert.assertEquals(this.getClass().getSimpleName(), job.getJobId().getReadableName());
        Assert.assertEquals(1, job.getTotalNumberOfTasks());
        Assert.assertEquals(0, job.getNumberOfFinishedTasks());
        Assert.assertEquals(0, job.getNumberOfPendingTasks());
        Assert.assertEquals(1, job.getNumberOfRunningTasks());
        Assert.assertEquals(JobStatus.RUNNING, job.getStatus());
        assertTrue("Unexpected submit time: " + job.getSubmittedTime(),
                job.getSubmittedTime() > time && job.getSubmittedTime() < System.currentTimeMillis());
        assertTrue("Unexpected start time: " + job.getStartTime(), job.getStartTime() > time &&
                job.getStartTime() < System.currentTimeMillis());
        Assert.assertEquals(-1, job.getFinishedTime());
        Assert.assertEquals(-1, job.getRemovedTime());
        Assert.assertEquals(SchedulerTHelper.admin_username, job.getJobOwner());
        Assert.assertEquals(JobPriority.NORMAL, job.getPriority());

        scheduler.submit(createJob(fileLockPath));
        scheduler.submit(createJob(fileLockPath));

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs);

        jobs = scheduler.getJobs(1, 10, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 2, 3);

        jobs = scheduler.getJobs(1, 1, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 2);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 1, 2, 3);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 1, 2, 3);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_DESC);
        checkJobs(jobs, 3, 2, 1);

        fileLock.unlock();

        for (JobInfo jobInfo : jobs) {
            SchedulerTHelper.waitForEventJobFinished(jobInfo.getJobId(), 30000);
        }

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, false), SORT_BY_ID_ASC);
        checkJobs(jobs);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 1, 2, 3);

        scheduler.disconnect();

        // connect as another user

        SchedulerMonitorsHandler monitorsHandler = new SchedulerMonitorsHandler();

        SchedulerAuthenticationInterface auth = SchedulerTHelper.getSchedulerAuth();
        Credentials cred = Credentials.createCredentials(new CredData(SchedulerTHelper.user_username,
            SchedulerTHelper.user_password), auth.getPublicKey());
        scheduler = auth.login(cred);

        MonitorEventReceiver eventReceiver = new MonitorEventReceiver(monitorsHandler);
        eventReceiver = (MonitorEventReceiver) PAActiveObject.turnActive(eventReceiver);
        SchedulerState state = scheduler.addEventListener((SchedulerEventListener) eventReceiver, true, true);
        monitorsHandler.init(state);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 1, 2, 3);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs);

        fileLockPath = fileLock.lock().toString();

        jobId = scheduler.submit(createJob(fileLockPath));
        monitorsHandler.waitForEventTask(SchedulerEvent.TASK_PENDING_TO_RUNNING, jobId, "Test task", 30000);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 4);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 1, 2, 3, 4);

        jobs = scheduler.getJobs(2, 10, criteria(false, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 3, 4);

        fileLock.unlock();
        monitorsHandler.waitForFinishedJob(jobId, 30000);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 4);

        jobs = scheduler.getJobs(0, 10, criteria(false, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 1, 2, 3, 4);

        jobs = scheduler.getJobs(1, 1, criteria(false, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 2);

        jobs = scheduler.getJobs(1, 2, criteria(false, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 2, 3);

        jobs = scheduler.getJobs(2, 1, criteria(false, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 3);

        jobs = scheduler.getJobs(2, 2, criteria(false, false, false, true), SORT_BY_ID_ASC);
        checkJobs(jobs, 3, 4);

        scheduler.disconnect();

        // connect as a user who can see only its own jobs
        cred = Credentials.createCredentials(new CredData("guest", "pwd"), auth.getPublicKey());
        scheduler = auth.login(cred);

        monitorsHandler = new SchedulerMonitorsHandler();
        eventReceiver = new MonitorEventReceiver(monitorsHandler);
        eventReceiver = (MonitorEventReceiver) PAActiveObject.turnActive(eventReceiver);
        state = scheduler.addEventListener((SchedulerEventListener) eventReceiver, true, true);
        monitorsHandler.init(state);

        JobId myjob = scheduler.submit(createJob(fileLockPath));
        int myjobId = Integer.parseInt(myjob.value());

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, myjobId);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), SORT_BY_ID_ASC);
        checkJobs(jobs, myjobId);

        scheduler.disconnect();

    }

    private void checkJobs(List<JobInfo> jobs, Integer... expectedIds) {
        Set<Integer> jobIds = new HashSet<>(jobs.size());
        for (JobInfo job : jobs) {
            jobIds.add(Integer.valueOf(job.getJobId().value()));
            logger.info("Job " + job.getJobId() + " has status '" + job.getStatus() + "'");
        }
        for (Integer expectedId : expectedIds) {
            final boolean expectedJobIdContained = jobIds.contains(expectedId);
            logger.info("Checking if " + jobs + " contains " + expectedId + "? " + expectedJobIdContained);
            assertTrue(expectedJobIdContained);
        }
    }

    private TaskFlowJob createJob(String communicationObjectUrl) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.addArgument("fileLockPath", communicationObjectUrl);
        javaTask.setName("Test task");

        job.addTask(javaTask);

        return job;
    }

    private JobFilterCriteria criteria(boolean myJobsOnly, boolean pending, boolean running, boolean finished) {
        return new JobFilterCriteria(myJobsOnly, pending, running, finished);
    }

}
