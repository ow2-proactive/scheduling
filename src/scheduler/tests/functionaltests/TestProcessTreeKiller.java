package functionaltests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;

import functionalTests.FunctionalTest;


/**
 * @author ProActive team
 *
 * this test check whether ProcessTreeKiller kill properly detached processes launched
 * by a native Task, and only processes launched by this native task.
 *
 */
public class TestProcessTreeKiller extends FunctionalTest {

    private static String nativeLinuxExecLauncher = TestProcessTreeKiller.class.getResource(
            "/functionaltests/executables/PTK_launcher.sh").getPath();

    private static String unixPTKProcessName = "PTK_process.sh";

    private static String nativeLinuxDetachedProcess = TestProcessTreeKiller.class.getResource(
            "/functionaltests/executables/" + unixPTKProcessName).getPath();

    private static int detachedProcNumber = 4;

    private final static int wait_time = 1000;

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        Runtime.getRuntime().exec("killall " + unixPTKProcessName);

        SchedulerTHelper.log("Test 1 : Creating jobs...");

        //create job 1
        TaskFlowJob job1 = new TaskFlowJob();
        job1.setName("Test PTK1");
        job1.setDescription("a command that launches detached commands");

        NativeTask task1 = new NativeTask();
        String task1Name = "TestPTK1";
        task1.setName(task1Name);
        task1.setCommandLine(new String[] { nativeLinuxExecLauncher });
        job1.addTask(task1);

        //create job 2
        TaskFlowJob job2 = new TaskFlowJob();
        job2.setName("Test PTK2");
        job2.setDescription("a command that launches detached commands");

        NativeTask task2 = new NativeTask();
        String task2Name = "TestPTK1";
        task2.setName(task2Name);
        task2.setCommandLine(new String[] { nativeLinuxExecLauncher });
        job2.addTask(task2);

        SchedulerTHelper.setExecutable(nativeLinuxExecLauncher + " " + nativeLinuxDetachedProcess);

        //submit two jobs
        JobId id1 = SchedulerTHelper.submitJob(job1);
        SchedulerTHelper.waitForEventTaskRunning(id1, task1Name);
        JobId id2 = SchedulerTHelper.submitJob(job2);
        SchedulerTHelper.waitForEventTaskRunning(id2, task2Name);

        Thread.sleep(wait_time);

        int runningDetachedProcNumber = getProcessNumber(unixPTKProcessName);

        //we should have 2 times number of detached processes
        SchedulerTHelper.log("number of processes : " + runningDetachedProcNumber);
        Assert.assertEquals(detachedProcNumber * 2, runningDetachedProcNumber);

        //kill the first job
        SchedulerTHelper.getUserInterface().kill(id1);
        SchedulerTHelper.waitForEventJobFinished(id1);

        //we should have 1 time number of detached processes
        runningDetachedProcNumber = getProcessNumber(unixPTKProcessName);
        SchedulerTHelper.log("number of processes : " + runningDetachedProcNumber);
        Assert.assertEquals(detachedProcNumber, runningDetachedProcNumber);

        //kill the second job
        SchedulerTHelper.getUserInterface().kill(id2);
        SchedulerTHelper.waitForEventJobFinished(id2);

        //we should have 0 detached processes
        runningDetachedProcNumber = getProcessNumber(unixPTKProcessName);
        SchedulerTHelper.log("number of processes : " + runningDetachedProcNumber);
        Assert.assertEquals(0, runningDetachedProcNumber);

        JobResult res = SchedulerTHelper.getJobResult(id1);
        Assert.assertEquals(JobStatus.KILLED, res.getJobInfo().getStatus());

        res = SchedulerTHelper.getJobResult(id2);
        Assert.assertEquals(JobStatus.KILLED, res.getJobInfo().getStatus());
    }

    private int getProcessNumber(String executableName) throws IOException {
        int toReturn = 0;
        String line;
        Process p = Runtime.getRuntime().exec("ps -e");
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            if (line.contains(executableName)) {
                toReturn++;
            }
        }
        input.close();
        return toReturn;
    }
}
