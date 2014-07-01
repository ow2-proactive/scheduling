package functionaltests.service;

import java.util.Map;

import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SchedulingServiceTest1 extends BaseServiceTest {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        JavaTask task1 = new JavaTask();
        task1.setExecutableClassName("class");
        task1.setName("task1");
        job.addTask(task1);
        return job;
    }

    @Test
    public void testLockJobsToSchedule() throws Exception {
        Map<JobId, JobDescriptor> jobsMap;

        service.submitJob(createJob(createTestJob()));
        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        runInAnotherThread(new TestRunnable() {
            @Override
            public void run() {
                Assert.assertEquals(0, service.lockJobsToSchedule().size());
            }
        });
        service.unlockJobsToSchedule(jobsMap.values());

        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        runInAnotherThread(new TestRunnable() {
            @Override
            public void run() throws Exception {
                service.submitJob(createJob(createTestJob()));
                Map<JobId, JobDescriptor> jobsMap = service.lockJobsToSchedule();
                Assert.assertEquals(1, jobsMap.size());
                service.unlockJobsToSchedule(jobsMap.values());
            }
        });
        service.unlockJobsToSchedule(jobsMap.values());

        jobsMap = service.lockJobsToSchedule();
        assertEquals(2, jobsMap.size());
        service.unlockJobsToSchedule(jobsMap.values());

        infrastructure.assertRequests(0);
    }

    @Test
    public void testSimpleJob() throws Exception {
        service.submitJob(createJob(createTestJob()));
        listener.assertEvents(SchedulerEvent.JOB_SUBMITTED);

        Map<JobId, JobDescriptor> jobsMap;
        JobDescriptor jobDesc;

        jobsMap = service.lockJobsToSchedule();

        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(1, jobDesc.getEligibleTasks().size());

        taskStarted(jobDesc, jobDesc.getEligibleTasks().iterator().next());

        service.unlockJobsToSchedule(jobsMap.values());

        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(0, jobDesc.getEligibleTasks().size());
        service.unlockJobsToSchedule(jobsMap.values());

        TaskId taskId = jobDesc.getInternal().getTask("task1").getId();
        service.taskTerminatedWithResult(taskId, new TaskResultImpl(taskId, "Result", null, 0));

        jobsMap = service.lockJobsToSchedule();
        assertEquals(0, jobsMap.size());

        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.JOB_RUNNING_TO_FINISHED);

        infrastructure.assertRequests(1);
    }

}
