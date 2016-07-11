package functionaltests.service;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;


public class SchedulingServiceTest5 extends BaseServiceTest {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask task1 = new JavaTask();
        task1.setName("task1");
        task1.setExecutableClassName("class");
        job.addTask(task1);

        JavaTask task2 = new JavaTask();
        task2.setName("task2");
        task2.setExecutableClassName("class");
        job.addTask(task2);

        return job;
    }

    @Test
    public void testJobKill() throws Exception {
        service.submitJob(createJob(createTestJob()));
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

        Assert.assertTrue(service.killJob(jobDesc.getJobId()));
        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.JOB_UPDATED,
                SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.TASK_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.JOB_UPDATED);
        infrastructure.assertRequests(2);

        Assert.assertFalse(service.killJob(jobDesc.getJobId()));

    }

}
