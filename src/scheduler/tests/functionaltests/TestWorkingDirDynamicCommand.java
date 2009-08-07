package functionaltests;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;

import functionalTests.FunctionalTest;


/**
 * Test whether attribute 'workingDir' set in native task element in a job descriptor
 * set properly the launching directory of the native executable (equivalent to linux PWD)
 *
 * @author The ProActive Team
 * @date 2 jun 08
 */
public class TestWorkingDirDynamicCommand extends FunctionalTest {

    private static String jobDescriptor = TestWorkingDirStaticCommand.class.getResource(
            "/functionaltests/descriptors/Job_test_workingDir_dynamic_Command.xml").getPath();

    private static String executablePathPropertyName = "EXEC_PATH";

    private static String executablePath = TestWorkingDirStaticCommand.class.getResource(
            "/functionaltests/executables/test_working_dir.sh").getPath();

    private static String WorkingDirPropertyName = "WDIR";

    private static String workingDirPath = TestWorkingDirStaticCommand.class.getResource(
            "/functionaltests/executables").getPath();

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        String task1Name = "task1";

        //set system Property for executable path
        System.setProperty(executablePathPropertyName, executablePath);
        System.setProperty(WorkingDirPropertyName, workingDirPath);

        SchedulerTHelper.setExecutable(executablePath);

        //test submission and event reception
        JobId id = SchedulerTHelper.submitJob(jobDescriptor);

        SchedulerTHelper.log("Job submitted, id " + id.toString());

        SchedulerTHelper.log("Waiting for jobSubmitted Event");
        JobState receivedState = SchedulerTHelper.waitForEventJobSubmitted(id);

        Assert.assertEquals(receivedState.getId(), id);

        SchedulerTHelper.log("Waiting for job running");
        JobInfo jInfo = SchedulerTHelper.waitForEventJobRunning(id);
        Assert.assertEquals(jInfo.getJobId(), id);
        Assert.assertEquals(JobStatus.RUNNING, jInfo.getStatus());

        SchedulerTHelper.waitForEventTaskRunning(id, task1Name);
        TaskInfo tInfo = SchedulerTHelper.waitForEventTaskFinished(id, task1Name);

        SchedulerTHelper.log(SchedulerTHelper.getUserInterface().getTaskResult(id, "task1").getOutput()
                .getAllLogs(false));

        Assert.assertEquals(TaskStatus.FINISHED, tInfo.getStatus());

        SchedulerTHelper.waitForEventJobFinished(id);
        JobResult res = SchedulerTHelper.getJobResult(id);

        //check that there is one exception in results
        Assert.assertTrue(res.getExceptionResults().size() == 0);

        //remove job
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);
    }
}
