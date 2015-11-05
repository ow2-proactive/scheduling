package functionaltests.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.util.FileLock;

import com.google.common.collect.ImmutableList;

import functionaltests.monitor.MonitorEventReceiver;
import functionaltests.monitor.SchedulerMonitorsHandler;
import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.TestUsers;


/**
 * Test against method Scheduler.loadJobs
 */
public class TestLoadJobs extends SchedulerFunctionalTest {

    private static final List<SortParameter<JobSortParameter>> SORT_BY_ID_ASC =
            ImmutableList.of(
                    new SortParameter<>(JobSortParameter.ID, SortOrder.ASC));

    private static final List<SortParameter<JobSortParameter>> SORT_BY_ID_DESC =
            ImmutableList.of(
                    new SortParameter<>(JobSortParameter.ID, SortOrder.DESC));

    @Before
    public void setUp() throws Exception {
        logger.setLevel(Level.INFO);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        List<JobInfo> jobs = scheduler.getJobs(0, 1000, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();

        for (JobInfo job : jobs) {
            scheduler.removeJob(job.getJobId());
        }
    }

    @Test
    public void testLoadNoJob() throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        List<JobInfo> jobs = scheduler.getJobs(0, 0, criteria(true, true, true, true), null).getList();
        assertTrue(jobs.isEmpty());
    }

    @Test
    public void testLoadJobs() throws Exception {
        long time = System.currentTimeMillis();

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        FileLock fileLock = new FileLock();
        Path lock = fileLock.lock();
        String fileLockPath = lock.toString();

        logger.info("File lock location is " + fileLockPath);

        JobInfo job;
        List<JobInfo> jobs = scheduler.getJobs(0, 1, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs);

        JobId firstJob = scheduler.submit(createJob(fileLockPath));
        schedulerHelper.waitForEventTaskRunning(firstJob, "Test");

        jobs = scheduler.getJobs(0, 1, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, firstJob);
        job = jobs.get(0);

        assertEquals(this.getClass().getSimpleName(), job.getJobId().getReadableName());
        assertEquals(1, job.getTotalNumberOfTasks());
        assertEquals(0, job.getNumberOfFinishedTasks());
        assertEquals(0, job.getNumberOfPendingTasks());
        assertEquals(1, job.getNumberOfRunningTasks());
        assertEquals(JobStatus.RUNNING, job.getStatus());

        assertTrue("Unexpected submit time: " + job.getSubmittedTime(),
                job.getSubmittedTime() > time && job.getSubmittedTime() < System.currentTimeMillis());
        assertTrue("Unexpected start time: " + job.getStartTime(),
                job.getStartTime() > time && job.getStartTime() < System.currentTimeMillis());
        assertEquals(-1, job.getFinishedTime());
        assertEquals(-1, job.getRemovedTime());
        assertEquals(TestUsers.DEMO.username, job.getJobOwner());
        assertEquals(JobPriority.NORMAL, job.getPriority());

        JobId secondJob = scheduler.submit(createJob(fileLockPath));
        JobId thirdJob = scheduler.submit(createJob(fileLockPath));

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs);

        jobs = scheduler.getJobs(1, 10, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, secondJob, thirdJob);

        jobs = scheduler.getJobs(1, 1, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, secondJob);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, firstJob, secondJob, thirdJob);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, firstJob, secondJob, thirdJob);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_DESC).getList();
        checkJobs(jobs, thirdJob, secondJob, firstJob);

        fileLock.unlock();

        for (JobInfo jobInfo : jobs) {
            schedulerHelper.waitForEventJobFinished(jobInfo.getJobId(), 30000);
        }

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, false), SORT_BY_ID_ASC).getList();
        checkJobs(jobs);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, firstJob, secondJob, thirdJob);

        scheduler.disconnect();

        // connect as another user

        SchedulerMonitorsHandler monitorsHandler = new SchedulerMonitorsHandler();

        SchedulerAuthenticationInterface auth = schedulerHelper.getSchedulerAuth();
        Credentials cred = Credentials.createCredentials(new CredData(TestUsers.USER.username,
                TestUsers.USER.password), auth.getPublicKey());
        scheduler = auth.login(cred);

        MonitorEventReceiver eventReceiver = new MonitorEventReceiver(monitorsHandler);
        eventReceiver = PAActiveObject.turnActive(eventReceiver);
        SchedulerState state = scheduler.addEventListener(eventReceiver, true, true);
        monitorsHandler.init(state);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, firstJob, secondJob, thirdJob);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs);

        fileLockPath = fileLock.lock().toString();

        JobId fourthJob = scheduler.submit(createJob(fileLockPath));
        monitorsHandler.waitForEventTask(SchedulerEvent.TASK_PENDING_TO_RUNNING, fourthJob, "Test", 30000);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, fourthJob);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, firstJob, secondJob, thirdJob, fourthJob);

        jobs = scheduler.getJobs(2, 10, criteria(false, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, thirdJob, fourthJob);

        fileLock.unlock();
        monitorsHandler.waitForFinishedJob(fourthJob, 30000);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, fourthJob);

        jobs = scheduler.getJobs(0, 10, criteria(false, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, firstJob, secondJob, thirdJob, fourthJob);

        jobs = scheduler.getJobs(1, 1, criteria(false, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, secondJob);

        jobs = scheduler.getJobs(1, 2, criteria(false, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, secondJob, thirdJob);

        jobs = scheduler.getJobs(2, 1, criteria(false, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, thirdJob);

        jobs = scheduler.getJobs(2, 2, criteria(false, false, false, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, thirdJob, fourthJob);

        scheduler.disconnect();

        // connect as a user who can see only its own jobs
        cred = Credentials.createCredentials(new CredData("guest", "pwd"), auth.getPublicKey());
        scheduler = auth.login(cred);

        monitorsHandler = new SchedulerMonitorsHandler();
        eventReceiver = new MonitorEventReceiver(monitorsHandler);
        eventReceiver = PAActiveObject.turnActive(eventReceiver);
        state = scheduler.addEventListener(eventReceiver, true, true);
        monitorsHandler.init(state);

        JobId myjob = scheduler.submit(createJob(fileLockPath));

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, myjob);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), SORT_BY_ID_ASC).getList();
        checkJobs(jobs, myjob);

        scheduler.disconnect();
    }

    private void checkJobs(List<JobInfo> jobs, JobId... expectedIds) {
        Set<JobId> jobIds = new HashSet<>(jobs.size());

        for (JobInfo job : jobs) {
            jobIds.add(job.getJobId());
            logger.info("Job " + job.getJobId() + " has status '" + job.getStatus() + "'");
        }

        for (JobId expectedId : expectedIds) {
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
        javaTask.setName("Test");

        job.addTask(javaTask);

        return job;
    }

    public static class TestJavaTask extends JavaExecutable {

        private String fileLockPath;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            FileLock.waitUntilUnlocked(Paths.get(fileLockPath));
            return "OK";
        }

    }

    private JobFilterCriteria criteria(boolean myJobsOnly, boolean pending, boolean running,
            boolean finished) {
        return new JobFilterCriteria(myJobsOnly, pending, running, finished);
    }

}