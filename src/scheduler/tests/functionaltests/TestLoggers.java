/**
 *
 */
package functionaltests;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.util.logforwarder.LogForwardingService;

import functionalTests.FunctionalTest;
import functionaltests.executables.Logging;


/**
 * @author cdelbe
 *
 */
public class TestLoggers extends FunctionalTest {

    private static String jobDescriptor = TestJobClasspath.class.getResource(
            "/functionaltests/descriptors/Job_Test_Loggers.xml").getPath();

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        System.out.println("TestLoggers.run()*****************");

        LogForwardingService lfs = new LogForwardingService(
            "org.ow2.proactive.scheduler.common.util.logforwarder.providers.ProActiveBasedForwardingProvider");
        lfs.initialize();

        JobId id = SchedulerTHelper.submitJob(jobDescriptor);

        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + "." + id);
        l.removeAllAppenders();
        AppenderTester test1 = new AppenderTester();
        l.addAppender(test1);

        SchedulerTHelper.getUserInterface().listenLog(id, lfs.getAppenderProvider());

        SchedulerTHelper.waitForEventJobFinished(id);

        lfs.terminate();

        assert (test1.receivedOnlyAwaitedEvents() && test1.getNumberOfAppendedLogs() == 1);

    }

    public class AppenderTester extends AppenderSkeleton {

        private boolean allLogsAwaited = true;
        private int numberOfAppendedLogs = 0;

        @Override
        protected void append(LoggingEvent loggingevent) {
            System.out.println("AppenderTester.append() : " + loggingevent.getMessage());
            if (!Logging.MSG.equals(loggingevent.getMessage())) {
                this.allLogsAwaited = false;
            }
            numberOfAppendedLogs++;
        }

        public int getNumberOfAppendedLogs() {
            return this.numberOfAppendedLogs;
        }

        public boolean receivedOnlyAwaitedEvents() {
            return this.allLogsAwaited;
        }

        @Override
        public void close() {
            super.closed = true;
        }

        @Override
        public boolean requiresLayout() {
            return false;
        }
    }
}
