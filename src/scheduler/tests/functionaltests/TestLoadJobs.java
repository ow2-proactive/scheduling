package functionaltests;

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
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.tests.FunctionalTest;
import functionaltests.monitor.MonitorEventReceiver;
import functionaltests.monitor.SchedulerMonitorsHandler;
import junit.framework.Assert;
import org.junit.Test;


/**
 * Test against method Scheduler.loadJobs 
 *
 */
public class TestLoadJobs extends FunctionalTest {

    public static class CommunicationObject {

        private boolean canFinish;

        public boolean isCanFinish() {
            return canFinish;
        }

        public void setCanFinish(boolean canFinish) {
            this.canFinish = canFinish;
        }

    }

    public static class TestJavaTask extends JavaExecutable {

        private String communicationObjectUrl;

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            CommunicationObject communicationObject = PAActiveObject.lookupActive(CommunicationObject.class,
                    communicationObjectUrl);

            while (!communicationObject.isCanFinish()) {
                Thread.sleep(1000);
            }

            return "OK";
        }

    }

    @Test
    public void testLoadJobs() throws Exception {
        long time = System.currentTimeMillis();

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        CommunicationObject communicationObject = PAActiveObject.newActive(CommunicationObject.class,
                new Object[] {});
        String communicationObjectUrl = PAActiveObject.getUrl(communicationObject);

        JobId jobId;
        List<JobInfo> jobs;
        JobInfo job;

        jobs = scheduler.getJobs(0, 1, criteria(true, true, true, true), null);
        checkJobs(jobs);

        jobId = scheduler.submit(createJob(communicationObjectUrl));
        SchedulerTHelper.waitForEventTaskRunning(jobId, "Test task");

        jobs = scheduler.getJobs(0, 1, criteria(true, true, true, true), null);
        checkJobs(jobs, 1);
        job = jobs.get(0);
        Assert.assertEquals(this.getClass().getSimpleName(), job.getJobId().getReadableName());
        Assert.assertEquals(1, job.getTotalNumberOfTasks());
        Assert.assertEquals(0, job.getNumberOfFinishedTasks());
        Assert.assertEquals(0, job.getNumberOfPendingTasks());
        Assert.assertEquals(1, job.getNumberOfRunningTasks());
        Assert.assertEquals(JobStatus.RUNNING, job.getStatus());
        Assert.assertTrue("Unexpected submit time: " + job.getSubmittedTime(),
                job.getSubmittedTime() > time && job.getSubmittedTime() < System.currentTimeMillis());
        Assert.assertTrue("Unexpected start time: " + job.getStartTime(), job.getStartTime() > time &&
            job.getStartTime() < System.currentTimeMillis());
        Assert.assertEquals(-1, job.getFinishedTime());
        Assert.assertEquals(-1, job.getRemovedTime());
        Assert.assertEquals(SchedulerTHelper.admin_username, job.getJobOwner());
        Assert.assertEquals(JobPriority.NORMAL, job.getPriority());

        scheduler.submit(createJob(communicationObjectUrl));
        scheduler.submit(createJob(communicationObjectUrl));

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), null);
        checkJobs(jobs);

        jobs = scheduler.getJobs(1, 10, criteria(true, true, true, true), null);
        checkJobs(jobs, 2, 3);

        jobs = scheduler.getJobs(1, 1, criteria(true, true, true, true), null);
        checkJobs(jobs, 2);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), null);
        checkJobs(jobs, 1, 2, 3);

        List<SortParameter<JobSortParameter>> sortParameters = new ArrayList<SortParameter<JobSortParameter>>();

        sortParameters.add(new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.ASC));
        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), sortParameters);
        checkJobs(jobs, 1, 2, 3);

        sortParameters.clear();
        sortParameters.add(new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.DESC));
        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), sortParameters);
        checkJobs(jobs, 3, 2, 1);

        communicationObject.setCanFinish(true);

        for (JobInfo jobInfo : jobs) {
            SchedulerTHelper.waitForEventJobFinished(jobInfo.getJobId(), 30000);
        }

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, false), null);
        checkJobs(jobs);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), null);
        checkJobs(jobs, 1, 2, 3);

        scheduler.disconnect();

        // connect as another user

        SchedulerMonitorsHandler monitorsHandler = new SchedulerMonitorsHandler();

        SchedulerAuthenticationInterface auth = SchedulerTHelper.getSchedulerAuth();
        Credentials cred = Credentials.createCredentials(new CredData(SchedulerTHelper.user_username, SchedulerTHelper.user_password), auth.getPublicKey());
        scheduler = auth.login(cred);

        MonitorEventReceiver eventReceiver = new MonitorEventReceiver(monitorsHandler);
        eventReceiver = (MonitorEventReceiver) PAActiveObject.turnActive(eventReceiver);
        SchedulerState state = scheduler.addEventListener((SchedulerEventListener) eventReceiver, true, true);
        monitorsHandler.init(state);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), null);
        checkJobs(jobs, 1, 2, 3);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), null);
        checkJobs(jobs);

        communicationObject.setCanFinish(false);

        jobId = scheduler.submit(createJob(communicationObjectUrl));
        monitorsHandler.waitForEventTask(SchedulerEvent.TASK_PENDING_TO_RUNNING, jobId, "Test task", 30000);

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), null);
        checkJobs(jobs, 4);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), null);
        checkJobs(jobs);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), null);
        checkJobs(jobs, 1, 2, 3, 4);

        jobs = scheduler.getJobs(2, 10, criteria(false, true, true, true), null);
        checkJobs(jobs, 3, 4);

        communicationObject.setCanFinish(true);
        monitorsHandler.waitForFinishedJob(jobId, 30000);

        jobs = scheduler.getJobs(0, 10, criteria(true, false, false, true), null);
        checkJobs(jobs, 4);

        jobs = scheduler.getJobs(0, 10, criteria(false, false, false, true), null);
        checkJobs(jobs, 1, 2, 3, 4);

        jobs = scheduler.getJobs(1, 1, criteria(false, false, false, true), null);
        checkJobs(jobs, 2);

        jobs = scheduler.getJobs(1, 2, criteria(false, false, false, true), null);
        checkJobs(jobs, 2, 3);

        jobs = scheduler.getJobs(2, 1, criteria(false, false, false, true), null);
        checkJobs(jobs, 3);

        jobs = scheduler.getJobs(2, 2, criteria(false, false, false, true), null);
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

        JobId myjob = scheduler.submit(createJob(communicationObjectUrl));
        int myjobId = Integer.parseInt(myjob.value());

        jobs = scheduler.getJobs(0, 10, criteria(true, true, true, true), null);
        checkJobs(jobs, myjobId);

        jobs = scheduler.getJobs(0, 10, criteria(false, true, true, true), null);
        checkJobs(jobs, myjobId);

        scheduler.disconnect();

    }

    private void checkJobs(List<JobInfo> jobs, Integer... expectedIds) {
        List<Integer> ids = new ArrayList<Integer>();
        for (JobInfo job : jobs) {
            ids.add(Integer.valueOf(job.getJobId().value()));
        }
        Assert.assertEquals(Arrays.asList(expectedIds), ids);
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
