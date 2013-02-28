package functionaltests.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.core.SchedulingMethod;
import org.ow2.proactive.scheduler.core.SchedulingService;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.policy.DefaultPolicy;


/**
 * Test trying to catch SCHEDULING-1775.
 *
 */
public class SchedulingServiceTest10 {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Job1");
        JavaTask task1 = new JavaTask();
        task1.setExecutableClassName("class");
        task1.setName("task1");
        job.addTask(task1);
        return job;
    }

    private SchedulingService service;

    @Before
    public void init() throws Exception {
        // default test infrastructure executes all requests in the same thread, for this test real thread pool is needed
        SchedulerDBManager dbManager = SchedulerDBManager.createInMemorySchedulerDBManager();
        MockSchedulingInfrastructure infrastructure = new MockSchedulingInfrastructure(dbManager, Executors
                .newFixedThreadPool(1));
        MockSchedulingListener listener = new MockSchedulingListener();
        service = new SchedulingService(infrastructure, listener, null, DefaultPolicy.class.getName(),
            Mockito.mock(SchedulingMethod.class));
    }

    @After
    public void clean() throws Exception {
        service.shutdown();
    }

    @Test
    public void testLockJobsToSchedule() throws Exception {
        Map<JobId, JobDescriptor> jobsMap;

        service.submitJob(BaseServiceTest.createJob(createTestJob()));
        service.submitJob(BaseServiceTest.createJob(createTestJob()));
        jobsMap = service.lockJobsToSchedule();
        Assert.assertEquals(2, jobsMap.size());
        List<EligibleTaskDescriptor> tasks = new ArrayList<EligibleTaskDescriptor>(jobsMap.entrySet()
                .iterator().next().getValue().getEligibleTasks());
        service.simulateJobStartAndCancelIt(tasks, "");
        service.unlockJobsToSchedule(jobsMap.values());

        // wait when request started by the 'simulateJobStartAndCancelIt' finishes
        service.getInfrastructure().getInternalOperationsThreadPool().shutdown();
        boolean terminated = service.getInfrastructure().getInternalOperationsThreadPool().awaitTermination(
                1, TimeUnit.MINUTES);
        Assert.assertTrue(terminated);

        jobsMap = service.lockJobsToSchedule();
        Assert.assertEquals(1, jobsMap.size());
    }

}
