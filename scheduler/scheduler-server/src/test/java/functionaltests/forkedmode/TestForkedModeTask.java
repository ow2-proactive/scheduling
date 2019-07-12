package functionaltests.forkedmode;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import org.junit.Assert;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;


public class TestForkedModeTask extends SchedulerFunctionalTestWithCustomConfigAndRestart {
    public static final String TASK_NAME = "Check_Forked_Mode_Task";

    public void testTaskIsRunningInForkedMode(String jobResource) throws Exception {
        testTaskGetExpectedOutput(jobResource,
                                  TASK_NAME,
                                  System.getProperty("java.io.tmpdir") + File.separator,
                                  "Task output should display a path under the system tmp directory, as the task is supposed to run in a forked JVM.");
    }

    public void testTaskIsRunningInNonForkedMode(String jobResource) throws Exception {
        testTaskGetExpectedOutput(jobResource,
                                  TASK_NAME,
                                  PASchedulerProperties.SCHEDULER_HOME.getValueAsString(),
                                  "Task output should display the proactive home path, as the task is supposed to run in the node JVM.");
    }

    /**
     * Submit the job to the scheduler, check the job contains a task with expected name, and the task output result contains an expected substring 
     * @param jobResource the job resource name to submit to the scheduler
     * @param expectedTaskName the expected task name which should be defined in the job
     * @param expectedTaskOutput the task result output is expected to contain this string
     * @param errorMessage the error message when the task output doesn't contain the expected substring
     * @throws Exception
     */
    protected void testTaskGetExpectedOutput(String jobResource, String expectedTaskName, String expectedTaskOutput,
            String errorMessage) throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobId jobid = schedulerHelper.testJobSubmission(getResourceAbsolutePath(jobResource));
        Map<String, TaskResult> taskResult = scheduler.getJobResult(jobid).getAllResults();
        Assert.assertTrue(String.format("The jobs result should contain the task [%s].", expectedTaskName),
                          taskResult.containsKey(expectedTaskName));
        String taskOutput = taskResult.get(expectedTaskName).getOutput().getStdoutLogs();
        Assert.assertTrue(errorMessage, taskOutput.contains(expectedTaskOutput));
    }

    protected static String getResourceAbsolutePath(String resourceName) throws URISyntaxException {
        return new File(TestTaskForkedModeParameter.class.getResource(resourceName).toURI()).getAbsolutePath();
    }
}
