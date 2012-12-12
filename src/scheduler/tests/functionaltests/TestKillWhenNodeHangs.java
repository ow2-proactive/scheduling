package functionaltests;

import java.io.Serializable;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.tests.FunctionalTest;


/**
 * Test checks that Scheduler doesn't hangs when user kills task
 * which is running on the hanging node (scheduler shouldn't execute
 * blocking remote calls from the main active object's thread).
 * 
 * Test checks two methods killing task: killJob and killTask.
 * 
 * To simulate node hanging it suspends node using java debug interface.
 */
public class TestKillWhenNodeHangs extends FunctionalTest {

    public static class SleepForeverTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("Taks started");
            Thread.sleep(Long.MAX_VALUE);
            return "OK";
        }

    }

    public static class SimpleTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return "OK";
        }

    }

    @Test
    public void test() throws Throwable {
        DebugHelper debugHelper = new DebugHelper();

        // create RM and node source with one node, node JVM listens for debugger connections
        int rmiPort = CentralPAPropertyRepository.PA_RMI_PORT.getValue();
        RMTHelper rmHelper = RMTHelper.getDefaultInstance();
        String rmUrl = rmHelper.startRM(null, rmiPort);
        List<String> vmOptions = debugHelper.getDebuggedVMOptions();
        rmHelper.createNodeSource(rmiPort, 1, vmOptions);

        SchedulerTHelper.startScheduler(false, null, null, rmUrl);
        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        debugHelper.connect();

        // start job, job's forked java task sleeps forever
        JobId jobId;

        jobId = SchedulerTHelper.submitJob(createJobWithSleepingTask());
        SchedulerTHelper.waitForEventTaskRunning(jobId, TASK_NAME, 30000);

        // suspend node JVM so all remote calls to this node should hang 
        debugHelper.suspendVM();

        // try to kill job, check this call doesn't hang
        System.out.println("Killing job");
        scheduler.killJob(jobId);
        System.out.println("Job killed");
        SchedulerTHelper.waitForEventJobFinished(jobId);
        JobState jobState;
        jobState = scheduler.getJobState(jobId);
        Assert.assertEquals(JobStatus.KILLED, jobState.getStatus());

        // resume node JVM
        debugHelper.resumeVM();

        // start one more job, job's forked java task sleeps forever
        jobId = SchedulerTHelper.submitJob(createJobWithSleepingTask());
        SchedulerTHelper.waitForEventTaskRunning(jobId, TASK_NAME, 30000);

        // suspend node JVM so all remote calls to this node should hang 
        debugHelper.suspendVM();

        // try to terminate task, check this call doesn't hang
        System.out.println("Killing task");
        scheduler.killTask(jobId, TASK_NAME);
        System.out.println("Task killed");

        SchedulerTHelper.waitForEventJobFinished(jobId);
        jobState = scheduler.getJobState(jobId);
        Assert.assertEquals(JobStatus.FINISHED, jobState.getStatus());

        // at the end disconnect debugger from the node JVM and check scheduler still can execute simple forked java task

        debugHelper.resumeVM();
        debugHelper.disconnect();

        jobId = SchedulerTHelper.submitJob(createJobWithSimpleTask());
        SchedulerTHelper.waitForEventTaskRunning(jobId, TASK_NAME, 30000);
        SchedulerTHelper.waitForEventJobFinished(jobId);
        jobState = scheduler.getJobState(jobId);
        Assert.assertEquals(JobStatus.FINISHED, jobState.getStatus());
        JobResult jobResult = scheduler.getJobResult(jobId);
        Assert.assertEquals("OK", jobResult.getResult(TASK_NAME).value());
    }

    private static final String TASK_NAME = "task1";

    private TaskFlowJob createJobWithSleepingTask() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Test job");

        JavaTask javaTask = new JavaTask();
        javaTask.setName(TASK_NAME);
        javaTask.setExecutableClassName(SleepForeverTask.class.getName());
        ForkEnvironment env = new ForkEnvironment();
        javaTask.setForkEnvironment(env);

        job.addTask(javaTask);

        return job;
    }

    private TaskFlowJob createJobWithSimpleTask() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Test job");

        JavaTask javaTask = new JavaTask();
        javaTask.setName(TASK_NAME);
        javaTask.setExecutableClassName(SimpleTask.class.getName());
        ForkEnvironment env = new ForkEnvironment();
        javaTask.setForkEnvironment(env);

        job.addTask(javaTask);

        return job;
    }

}