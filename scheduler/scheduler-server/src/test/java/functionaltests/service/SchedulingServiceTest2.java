package functionaltests.service;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class SchedulingServiceTest2 extends BaseServiceTest {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask task1 = new JavaTask();
        task1.setName("javaTask");
        task1.setExecutableClassName("class");
        task1.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.addTask(task1);

        NativeTask task2 = new NativeTask();
        task2.setName("nativeTask");
        task2.setCommandLine("command line");
        task2.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.addTask(task2);

        return job;
    }

    @Test
    public void testFailedNativeTask() throws Exception {
        testFailedTask(false);
    }

    @Test
    public void testFailedJavaTask() throws Exception {
        testFailedTask(true);
    }

    @Test
    public void testNotStartedJob() throws Exception {
        InternalJob job = createJob(createTestJob());
        service.submitJob(job);
        listener.assertEvents(SchedulerEvent.JOB_SUBMITTED);

        service.killJob(job.getId());
        listener.assertEvents(SchedulerEvent.TASK_RUNNING_TO_FINISHED,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.JOB_PENDING_TO_FINISHED,
                SchedulerEvent.JOB_UPDATED);
    }

    private void testFailedTask(boolean failNativeTask) throws Exception {
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

        if (failNativeTask) {
            InternalTask nativeTask = jobDesc.getInternal().getTask("nativeTask");
            // native task terminates with code 1, flag 'cancelJobOnError' was
            // set so job should be cancelled
            service.taskTerminatedWithResult(nativeTask.getId(),
                    new TaskResultImpl(nativeTask.getId(), new RuntimeException(), null, 0));
        } else {
            InternalTask javaTask = jobDesc.getInternal().getTask("javaTask");
            // java task terminates with exception, flag 'cancelJobOnError' was
            // set so job should be cancelled
            service.taskTerminatedWithResult(javaTask.getId(),
                    new TaskResultImpl(javaTask.getId(), new RuntimeException(), null, 0));
        }

        jobsMap = service.lockJobsToSchedule();
        assertEquals(0, jobsMap.size());

        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.JOB_UPDATED,
                SchedulerEvent.TASK_PENDING_TO_RUNNING, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.TASK_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.JOB_UPDATED);

        infrastructure.assertRequests(2);
    }

}
