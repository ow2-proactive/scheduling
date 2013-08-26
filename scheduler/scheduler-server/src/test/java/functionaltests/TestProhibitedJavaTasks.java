package functionaltests;

import java.io.File;
import java.net.URL;

import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.tests.FunctionalTest;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test checks that if in node tasks are prohibited user
 * gets an error on job submission for java task. 
 */
public class TestProhibitedJavaTasks extends FunctionalTest {

    private static URL simpleJob = TestJobNodeAccess.class
            .getResource("/functionaltests/descriptors/Job_simple.xml");

    private static URL forkJob = TestJobNodeAccess.class
            .getResource("/functionaltests/descriptors/Job_fork.xml");

    @Test
    public void test() throws Throwable {
        SchedulerTHelper.log("Creating the scheduler");
        SchedulerTHelper.startScheduler(new File(SchedulerTHelper.class.getResource(
                "config/scheduler-noinnodestasks.ini").toURI()).getAbsolutePath());

        SchedulerTHelper.log("Submitting job with only java tasks");
        try {
            SchedulerTHelper.submitJob(new File(simpleJob.toURI()).getAbsolutePath(), ExecutionMode.normal);
            Assert.fail("Submitted a job with java tasks while it's prohibited");
        } catch (JobCreationException ex) {
            SchedulerTHelper.log("Passed");
        }

        SchedulerTHelper.log("Submitting job with java and native tasks");
        SchedulerTHelper.submitJob(new File(forkJob.toURI()).getAbsolutePath(), ExecutionMode.normal);

        SchedulerTHelper.log("Passed");
    }
}
