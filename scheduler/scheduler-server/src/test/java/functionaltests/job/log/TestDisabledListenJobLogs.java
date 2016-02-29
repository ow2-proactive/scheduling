package functionaltests.job.log;

import functionaltests.rm.nodesource.TestJobNodeAccessToken;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.JobId;

import java.io.File;
import java.net.URL;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Checks that if listening to jobs is disabled in the configuration 
 * file, the {@link Scheduler#listenJobLogs} method throws an exception 
 */
public class TestDisabledListenJobLogs extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    private static URL simpleJob = TestJobNodeAccessToken.class
            .getResource("/functionaltests/descriptors/Job_simple.xml");

    private static final String EXPECTED_MESSAGE = "Listening to job logs is disabled by administrator";

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        log("Creating the scheduler");
        schedulerHelper = new SchedulerTHelper(true, new File(SchedulerTHelper.class.getResource(
                "/functionaltests/config/scheduler-disablelistenjoblogs.ini").toURI()).getAbsolutePath());

    }

    @Test
    public void test() throws Throwable {

        log("Submitting job");
        JobId id = schedulerHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath());
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        try {
            scheduler.listenJobLogs(id, null);
            fail("listenJobLogs should throw an exception");
        } catch (PermissionException ex) {
            assertEquals(EXPECTED_MESSAGE, ex.getMessage());
        }

        try {
            scheduler.listenJobLogs(id.value(), null);
            fail("listenJobLogs should throw an exception");
        } catch (PermissionException ex) {
            assertEquals(EXPECTED_MESSAGE, ex.getMessage());
        }

    }
}
