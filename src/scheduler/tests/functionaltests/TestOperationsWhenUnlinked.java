package functionaltests;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.tests.FunctionalTest;


/**
 * Test checks that it is possible to submit job when
 * scheduler isn't linked to RM.
 * 
 * @author ProActive team
 *
 */
public class TestOperationsWhenUnlinked extends FunctionalTest {

    public static class TestJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("OK");
            return "OK";
        }

    }

    @Test
    public void test() throws Exception {
        RMTHelper.getResourceManager();

        String rmUrl = "rmi://localhost:" + CentralPAPropertyRepository.PA_RMI_PORT.getValue() + "/";

        SchedulerTHelper.startScheduler(false, null, null, rmUrl);

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        System.out.println("Submitting job");
        JobId jobId1 = scheduler.submit(createJob());

        System.out.println("Killing RM");
        RMTHelper.killRM();

        System.out.println("Waiting RM_DOWN event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.RM_DOWN, 30000);

        System.out.println("Submitting new job");
        JobId jobId2 = scheduler.submit(createJob());

        System.out.println("Creating new RM");
        RMTHelper.getResourceManager();
        RMTHelper.createLocalNodeSource();

        System.out.println("Linking new RM");
        scheduler.linkResourceManager(rmUrl);

        System.out.println("Waiting when jobs finish");
        SchedulerTHelper.waitForEventJobFinished(jobId1);
        SchedulerTHelper.waitForEventJobFinished(jobId2);

        checkJobResult(scheduler, jobId1);
        checkJobResult(scheduler, jobId1);
    }

    private void checkJobResult(Scheduler scheduler, JobId jobId) throws Exception {
        JobResult jobResult = scheduler.getJobResult(jobId);
        Assert.assertEquals("Result for one task is expected", 1, jobResult.getAllResults().size());
        for (TaskResult taskResult : jobResult.getAllResults().values()) {
            System.out.println("Task " + taskResult.getTaskId());
            Assert.assertNotNull("Unexpected task result exception", taskResult.getException());
            System.out.println("Task output:");
            System.out.println(taskResult.getOutput().getAllLogs(false));
        }
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Test job");

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName("Test task");
        job.addTask(javaTask);

        return job;
    }

}
