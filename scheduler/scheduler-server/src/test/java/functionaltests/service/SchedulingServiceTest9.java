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
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class SchedulingServiceTest9 extends BaseServiceTest {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask task1 = new JavaTask();
        task1.setName("javaTask1");
        task1.setExecutableClassName("class");
        job.addTask(task1);

        JavaTask task2 = new JavaTask();
        task2.setName("javaTask2");
        task2.setExecutableClassName("class");
        job.addTask(task2);

        return job;
    }

    @Test
    public void testRestartOnNodeFailureAndJobCancel() throws Exception {
        service.submitJob(createJob(createTestJob()));
        listener.assertEvents(SchedulerEvent.JOB_SUBMITTED);

        JobDescriptor jobDesc;

        jobDesc = startTask(2);
        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.JOB_UPDATED,
                SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.TASK_PENDING_TO_RUNNING);

        InternalTask task = jobDesc.getInternal().getTask("javaTask1");
        service.restartTaskOnNodeFailure(task);
        listener.assertEvents(SchedulerEvent.TASK_WAITING_FOR_RESTART);
        infrastructure.assertRequests(1);

        startTask(1);
        service.restartTaskOnNodeFailure(task);
        listener.assertEvents(SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.JOB_UPDATED,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.TASK_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_RUNNING_TO_FINISHED);
        infrastructure.assertRequests(2);
    }

    private JobDescriptor startTask(int expectedTasks) throws Exception {
        Map<JobId, JobDescriptor> jobsMap;
        JobDescriptor jobDesc;

        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(expectedTasks, jobDesc.getEligibleTasks().size());
        for (EligibleTaskDescriptor taskDesc : jobDesc.getEligibleTasks()) {
            taskStarted(jobDesc, taskDesc);
        }
        service.unlockJobsToSchedule(jobsMap.values());

        return jobDesc;
    }

}
