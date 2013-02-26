package functionaltests;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.tests.FunctionalTest;


/**
 * Checks that if listening to jobs is disabled in the configuration 
 * file, the {@link Scheduler#listenJobLogs} method throws an exception 
 */
public class TestDisabledListenJobLogs extends FunctionalTest {

    private static URL simpleJob = TestJobNodeAccess.class
            .getResource("/functionaltests/descriptors/Job_simple.xml");

    private static String EXPECTED_MESSAGE = "Listening to job logs is disabled by administrator";

    @Test
    public void test() throws Throwable {
        SchedulerTHelper.log("Creating the scheduler");
        SchedulerTHelper.startScheduler(new File(SchedulerTHelper.class.getResource(
                "config/scheduler-disablelistenjoblogs.ini").toURI()).getAbsolutePath());

        SchedulerTHelper.log("Submitting job");
        JobId id = SchedulerTHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath(),
                ExecutionMode.normal);
        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        try {
            scheduler.listenJobLogs(id, null);
            Assert.fail("listenJobLogs should throw an exception");
        } catch (PermissionException ex) {
            Assert.assertEquals(EXPECTED_MESSAGE, ex.getMessage());
        }

        try {
            scheduler.listenJobLogs(id.value(), null);
            Assert.fail("listenJobLogs should throw an exception");
        } catch (PermissionException ex) {
            Assert.assertEquals(EXPECTED_MESSAGE, ex.getMessage());
        }

        SchedulerTHelper.log("Passed");
    }
}
