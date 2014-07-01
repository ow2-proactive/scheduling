package functionaltests.service;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


public class SchedulingServiceTest3 extends BaseServiceTest {

    private TaskFlowJob createTestJob(boolean cancelJobOnError) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask task1 = new JavaTask();
        task1.setName("javaTask");
        task1.setExecutableClassName("class");
        task1.setCancelJobOnError(cancelJobOnError);
        job.addTask(task1);

        NativeTask task2 = new NativeTask();
        task2.setName("nativeTask");
        task2.setCommandLine("command line");
        job.addTask(task2);

        return job;
    }

    @Test
    public void testTaskKillAndJobCancel() throws Exception {
        service.submitJob(createJob(createTestJob(true)));
        listener.assertEvents(SchedulerEvent.JOB_SUBMITTED);

        Map<JobId, JobDescriptor> jobsMap;
        JobDescriptor jobDesc;

        jobsMap = service.lockJobsToSchedule();

        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(2, jobDesc.getEligibleTasks().size());

        for (EligibleTaskDescriptor taskDesc : jobDesc.getEligibleTasks()) {
            taskStarted(jobDesc, taskDesc);
        }
        service.unlockJobsToSchedule(jobsMap.values());

        Assert.assertTrue(service.killTask(jobDesc.getJobId(), "javaTask"));

        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.TASK_RUNNING_TO_FINISHED,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.JOB_RUNNING_TO_FINISHED);

        infrastructure.assertRequests(2);
    }

    @Test
    public void testTaskKill() throws Exception {
        service.submitJob(createJob(createTestJob(false)));
        listener.assertEvents(SchedulerEvent.JOB_SUBMITTED);

        Map<JobId, JobDescriptor> jobsMap;
        JobDescriptor jobDesc;

        jobsMap = service.lockJobsToSchedule();

        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(2, jobDesc.getEligibleTasks().size());

        for (EligibleTaskDescriptor taskDesc : jobDesc.getEligibleTasks()) {
            taskStarted(jobDesc, taskDesc);
        }
        service.unlockJobsToSchedule(jobsMap.values());

        try {
            service.killTask(jobDesc.getJobId(), "invalid task name");
            Assert.fail();
        } catch (UnknownTaskException e) {
        }
        try {
            service.killTask(JobIdImpl.makeJobId("1234567"), "javaTask");
            Assert.fail();
        } catch (UnknownJobException e) {
        }

        Assert.assertTrue(service.killTask(jobDesc.getJobId(), "javaTask"));

        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.TASK_RUNNING_TO_FINISHED);
        infrastructure.assertRequests(1);

        Assert.assertFalse(service.killTask(jobDesc.getJobId(), "javaTask"));

        TaskId nativeTaskId = jobDesc.getInternal().getTask("nativeTask").getId();
        service.taskTerminatedWithResult(nativeTaskId, new TaskResultImpl(nativeTaskId, new Integer(0), null,
            0));

        listener
                .assertEvents(SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.JOB_RUNNING_TO_FINISHED);
        infrastructure.assertRequests(1);

        try {
            service.killTask(jobDesc.getJobId(), "javaTask");
            Assert.fail();
        } catch (UnknownJobException e) {
        }
    }

}
