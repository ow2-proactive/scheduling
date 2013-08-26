package functionaltests.service;

import java.util.Map;

import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SchedulingServiceTest8 extends BaseServiceTest {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask task1 = new JavaTask();
        task1.setName("javaTask");
        task1.setExecutableClassName("class");
        job.addTask(task1);

        return job;
    }

    @Test
    public void testTaskPreempt() throws Exception {
        service.submitJob(createJob(createTestJob()));
        listener.assertEvents(SchedulerEvent.JOB_SUBMITTED);

        JobDescriptor jobDesc = startTask();

        try {
            service.preemptTask(jobDesc.getJobId(), "invalid task name", 100);
            Assert.fail();
        } catch (UnknownTaskException e) {
        }
        try {
            service.preemptTask(JobIdImpl.makeJobId("1234567"), "javaTask", 100);
            Assert.fail();
        } catch (UnknownJobException e) {
        }

        service.preemptTask(jobDesc.getJobId(), "javaTask", 100);
        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_WAITING_FOR_RESTART);
        infrastructure.assertRequests(1);

        startTask();
        service.preemptTask(jobDesc.getJobId(), "javaTask", 100);
        listener
                .assertEvents(SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.TASK_WAITING_FOR_RESTART);
        infrastructure.assertRequests(1);

        startTask();
        TaskId taskId = jobDesc.getInternal().getTask("javaTask").getId();
        service.taskTerminatedWithResult(taskId, new TaskResultImpl(taskId, "OK", null, 0));
        listener.assertEvents(SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.JOB_RUNNING_TO_FINISHED);
        infrastructure.assertRequests(1);

        try {
            service.preemptTask(jobDesc.getJobId(), "javaTask", 100);
            Assert.fail();
        } catch (UnknownJobException e) {
        }
    }

    private JobDescriptor startTask() throws Exception {
        Map<JobId, JobDescriptor> jobsMap;
        JobDescriptor jobDesc;

        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(1, jobDesc.getEligibleTasks().size());
        for (EligibleTaskDescriptor taskDesc : jobDesc.getEligibleTasks()) {
            taskStarted(jobDesc, taskDesc);
        }
        service.unlockJobsToSchedule(jobsMap.values());

        return jobDesc;
    }
}
