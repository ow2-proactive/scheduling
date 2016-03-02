package functionaltests.job.taskkill;

import functionaltests.utils.DebugHelper;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import java.io.Serializable;
import java.util.List;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;


/**
 * Test checks that Scheduler doesn't hangs when user kills task
 * which is running on the hanging node (scheduler shouldn't execute
 * blocking remote calls from the main active object's thread).
 * 
 * Test checks two methods killing task: killJob and killTask.
 * 
 * To simulate node hanging it suspends node using java debug interface.
 */
public class TestKillWhenNodeHangs extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        // we start a scheduler with an empty RM
        schedulerHelper = new SchedulerTHelper(true, true);
    }

    public static class SleepForeverTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            getOut().println("Task started");
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

    @Ignore
    @Test
    public void test() throws Throwable {
        DebugHelper debugHelper = new DebugHelper();

        // create RM and node source with one node, node JVM listens for debugger connections
        List<String> vmOptions = debugHelper.getDebuggedVMOptions();
        testNodes.addAll(schedulerHelper.addNodesToDefaultNodeSource(1, vmOptions));

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        debugHelper.connect();

        // start job, job's forked java task sleeps forever
        JobId jobId;

        jobId = schedulerHelper.submitJob(createJobWithSleepingTask());
        schedulerHelper.waitForEventTaskRunning(jobId, TASK_NAME, 30000);

        // suspend node JVM so all remote calls to this node should hang 
        debugHelper.suspendVM();

        // try to kill job, check this call doesn't hang
        log("Killing job");
        scheduler.killJob(jobId);
        log("Job killed");
        schedulerHelper.waitForEventJobFinished(jobId);
        JobState jobState;
        jobState = scheduler.getJobState(jobId);
        assertEquals(JobStatus.KILLED, jobState.getStatus());

        // resume node JVM
        debugHelper.resumeVM();

        // start one more job, job's forked java task sleeps forever
        jobId = schedulerHelper.submitJob(createJobWithSleepingTask());
        schedulerHelper.waitForEventTaskRunning(jobId, TASK_NAME, 30000);

        // suspend node JVM so all remote calls to this node should hang 
        debugHelper.suspendVM();

        // try to terminate task, check this call doesn't hang
        log("Killing task");
        scheduler.killTask(jobId, TASK_NAME);
        log("Task killed");

        schedulerHelper.waitForEventJobFinished(jobId);
        jobState = scheduler.getJobState(jobId);
        assertEquals(JobStatus.FINISHED, jobState.getStatus());

        // at the end disconnect debugger from the node JVM and check scheduler still can execute simple forked java task

        debugHelper.resumeVM();
        debugHelper.disconnect();

        jobId = schedulerHelper.submitJob(createJobWithSimpleTask());
        schedulerHelper.waitForEventTaskRunning(jobId, TASK_NAME, 30000);
        schedulerHelper.waitForEventJobFinished(jobId);
        jobState = scheduler.getJobState(jobId);
        assertEquals(JobStatus.FINISHED, jobState.getStatus());
        JobResult jobResult = scheduler.getJobResult(jobId);
        assertEquals("OK", jobResult.getResult(TASK_NAME).value());
    }

    private static final String TASK_NAME = "task1";

    private TaskFlowJob createJobWithSleepingTask() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setName(TASK_NAME);
        javaTask.setExecutableClassName(SleepForeverTask.class.getName());

        job.addTask(javaTask);

        return job;
    }

    private TaskFlowJob createJobWithSimpleTask() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        JavaTask javaTask = new JavaTask();
        javaTask.setName(TASK_NAME);
        javaTask.setExecutableClassName(SimpleTask.class.getName());

        job.addTask(javaTask);

        return job;
    }

}