package functionaltests.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.junit.Before;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.monitor.MonitorEventReceiver;
import functionaltests.monitor.SchedulerMonitorsHandler;
import functionaltests.utils.ProActiveLock;
import functionaltests.utils.TestUsers;

import static org.junit.Assert.*;


/**
 * Test against method Scheduler.loadJobs 
 *
 */
public class TestLoadJobs extends SchedulerFunctionalTest {

    public static class TestJavaTask extends JavaExecutable {

        private String communicationObjectUrl;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            ProActiveLock communicationObject = PAActiveObject.lookupActive(ProActiveLock.class,
                    communicationObjectUrl);

            ProActiveLock.waitUntilUnlocked(communicationObject);
            return "OK";
        }

    }

    @Before
    public void setUp() throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        List<JobInfo> jobs = scheduler.getJobs(0, 1000, criteria(true, true, true, true), null);

        for (JobInfo job : jobs) {
            scheduler.removeJob(job.getJobId());
        }
    }

    @Test
    public void testLoadJobs() throws Exception {
        long time = System.currentTimeMillis();

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        ProActiveLock communicationObject = PAActiveObject.newActive(ProActiveLock.class, new Object[] {});
        String communicationObjectUrl = PAActiveObject.getUrl(communicationObject);

        JobId jobId;
        List<JobInfo> jobs;
        JobInfo job;

        jobs = scheduler.getJobs(0, 1, criteria(true, true, true, true), null);
        checkJobs(jobs);

        JobId firstJob = scheduler.submit(createJob(communicationObjectUrl));
        schedulerHelper.waitForEventTaskRunning(firstJob, "Test task");

        jobs = scheduler.getJobs(0, 1, criteria(true, true, true, true), null);
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

        JobId secondJob = scheduler.submit(createJob(communicationObjectUrl));
        JobId thirdJob = scheduler.submit(createJob(communicationObjectUrl));

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), null);
        checkJobs(jobs);

        jobs = scheduler.getJobs(1, 10, criteria(true, true, true, true), null);
        checkJobs(jobs, secondJob, thirdJob);

        jobs = scheduler.getJobs(1, 1, criteria(true, true, true, true), null);
        checkJobs(jobs, secondJob);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), null);
        checkJobs(jobs, firstJob, secondJob, thirdJob);

        List<SortParameter<JobSortParameter>> sortParameters = new ArrayList<>();

        sortParameters.add(new SortParameter<>(JobSortParameter.ID, SortOrder.ASC));
        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), sortParameters);
        checkJobs(jobs, firstJob, secondJob, thirdJob);

        sortParameters.clear();
        sortParameters.add(new SortParameter<>(JobSortParameter.ID, SortOrder.DESC));
        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), sortParameters);
        checkJobs(jobs, thirdJob, secondJob, firstJob);

        communicationObject.unlock();

        for (JobInfo jobInfo : jobs) {
            schedulerHelper.waitForEventJobFinished(jobInfo.getJobId(), 30000);
        }

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, false), null);
        checkJobs(jobs);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), null);
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

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), null);
        checkJobs(jobs, firstJob, secondJob, thirdJob);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), null);
        checkJobs(jobs);

        communicationObject.lock();

        JobId fourthJob = scheduler.submit(createJob(communicationObjectUrl));
        monitorsHandler.waitForEventTask(SchedulerEvent.TASK_PENDING_TO_RUNNING, fourthJob, "Test task", 30000);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), null);
        checkJobs(jobs, fourthJob);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), null);
        checkJobs(jobs);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), null);
        checkJobs(jobs, firstJob, secondJob, thirdJob, fourthJob);

        jobs = scheduler.getJobs(2, 10, criteria(false, true, true, true), null);
        checkJobs(jobs, thirdJob, fourthJob);

        communicationObject.unlock();
        monitorsHandler.waitForFinishedJob(fourthJob, 30000);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), null);
        checkJobs(jobs, fourthJob);

        jobs = scheduler.getJobs(0, 10, criteria(false, false, false, true), null);
        checkJobs(jobs, firstJob, secondJob, thirdJob, fourthJob);

        jobs = scheduler.getJobs(1, 1, criteria(false, false, false, true), null);
        checkJobs(jobs, secondJob);

        jobs = scheduler.getJobs(1, 2, criteria(false, false, false, true), null);
        checkJobs(jobs, secondJob, thirdJob);

        jobs = scheduler.getJobs(2, 1, criteria(false, false, false, true), null);
        checkJobs(jobs, thirdJob);

        jobs = scheduler.getJobs(2, 2, criteria(false, false, false, true), null);
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

        JobId myjob = scheduler.submit(createJob(communicationObjectUrl));

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), null);
        checkJobs(jobs, myjob);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), null);
        checkJobs(jobs, myjob);

        scheduler.disconnect();

    }

    private void checkJobs(List<JobInfo> jobs, JobId... expectedIds) {
        List<JobId> ids = new ArrayList<>(jobs.size());
        for (JobInfo job : jobs) {
            ids.add(job.getJobId());
        }
        assertEquals(Arrays.asList(expectedIds), ids);
    }

    private TaskFlowJob createJob(String communicationObjectUrl) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.addArgument("communicationObjectUrl", communicationObjectUrl);
        javaTask.setName("Test task");

        job.addTask(javaTask);

        return job;
    }

    private JobFilterCriteria criteria(boolean myJobsOnly, boolean pending, boolean running, boolean finished) {
        return new JobFilterCriteria(myJobsOnly, pending, running, finished);
    }

}
