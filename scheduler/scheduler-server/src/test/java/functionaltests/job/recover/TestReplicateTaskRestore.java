package functionaltests.job.recover;

import java.util.Map;

import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;

import static com.google.common.truth.Truth.assertThat;
import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;


/**
 * Test checks that runtime state of workflow job with replicated
 * tasks is properly restored after scheduler is killed and restarted.
 *
 * @author ActiveEon Team
 */
public class TestReplicateTaskRestore extends SchedulerFunctionalTest {

    @Test
    public void test() throws Throwable {
        Job job = parseXml("/functionaltests/job/recover/job-replicate-tasks.xml");

        JobId jobId = schedulerHelper.submitJob(job);

        schedulerHelper.waitForEventTaskFinished(jobId, "SplitTask");
        schedulerHelper.killSchedulerAndNodesAndRestart(SchedulerTHelper.class.getResource(
                "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").getPath());
        schedulerHelper.waitForEventJobFinished(jobId);

        checkTasksResult(jobId);
    }

    private void checkTasksResult(JobId jobId) throws Throwable {
        Map<String, TaskResult> results =
                schedulerHelper.getSchedulerInterface().getJobResult(jobId).getAllResults();

        assertThat(results).hasSize(7);

        Boolean splitResult = getBoolean(results, "SplitTask");
        assertThat(splitResult).isTrue();

        Boolean mergeResult = getBoolean(results, "MergeTask");
        assertThat(mergeResult).isTrue();

        checkProcessTaskResult("ProcessTask", 0);
        for (int i = 1; i < 5; i++) {
            checkProcessTaskResult("ProcessTask*" + i, i);
        }
    }

    private void checkProcessTaskResult(String taskName, int expectedReplicateIndex) {
        assertThat(getReplicateIndex(taskName)).isEqualTo(expectedReplicateIndex);
    }

    private int getReplicateIndex(String taskName) {
        String[] chunks = taskName.split("[*]");

        if (chunks.length < 2) {
            return 0;
        }

        return Integer.parseInt(chunks[1]);
    }

    private Boolean getBoolean(Map<String, TaskResult> results, String taskName) throws Throwable {
        return (Boolean) results.get(taskName).value();
    }

}
