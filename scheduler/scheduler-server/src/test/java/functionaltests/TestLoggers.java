/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests;

import java.io.File;
import java.net.URL;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.tests.FunctionalTest;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import functionaltests.executables.Logging;

import static org.junit.Assert.*;


public class TestLoggers extends FunctionalTest {

    private static URL jobDescriptor = TestLoggers.class
            .getResource("/functionaltests/descriptors/Job_Test_Loggers.xml");

    private final static int TEST_TIMEOUT = 10000;

    @Test
    public void run() throws Throwable {

        // socket loggers
        LogForwardingService lfsPA = new LogForwardingService(
            "org.ow2.proactive.scheduler.common.util.logforwarder.providers.ProActiveBasedForwardingProvider");
        lfsPA.initialize();
        LogForwardingService lfsSocket = new LogForwardingService(
            "org.ow2.proactive.scheduler.common.util.logforwarder.providers.SocketBasedForwardingProvider");
        lfsSocket.initialize();

        JobId id1 = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        JobId id2 = SchedulerTHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        AppenderTester test1 = new AppenderTester();
        lfsPA.addAppender(Log4JTaskLogs.JOB_LOGGER_PREFIX + id1, test1);

        AppenderTester test2 = new AppenderTester();
        lfsSocket.addAppender(Log4JTaskLogs.JOB_LOGGER_PREFIX + id2, test2);

        SchedulerTHelper.getSchedulerInterface().listenJobLogs(id1, lfsPA.getAppenderProvider());
        SchedulerTHelper.getSchedulerInterface().listenJobLogs(id2, lfsSocket.getAppenderProvider());

        SchedulerTHelper.waitForEventJobFinished(id1);
        SchedulerTHelper.waitForEventJobFinished(id2);

        // waiting for the end of the job is not enough ... :(
        // listenLog is asynchronous, i.e. "eventually" semantic
        Thread.sleep(TEST_TIMEOUT);

        assertTrue(test1.receivedOnlyAwaitedEvents());
        assertEquals(2, test1.getNumberOfAppendedLogs());
        assertTrue(test2.receivedOnlyAwaitedEvents());
        assertEquals(2, test2.getNumberOfAppendedLogs());

        lfsPA.terminate();
        lfsSocket.terminate();
        SchedulerTHelper.killScheduler();

    }

    public class AppenderTester extends AppenderSkeleton {

        private boolean allLogsAwaited = true;
        private int numberOfAppendedLogs = 0;

        @Override
        protected void append(LoggingEvent loggingevent) {
            System.out.println("====== " + loggingevent.getLoggerName());
            System.out.println("======>" + loggingevent.getMessage() + "<=====");
            System.out.println("====== " + loggingevent.getLevel());
            if (loggingevent.getLevel().equals(Log4JTaskLogs.STDERR_LEVEL)) {
                return;
            } else if (!Logging.MSG.equals(loggingevent.getMessage())) {
                this.allLogsAwaited = false;
            }
            numberOfAppendedLogs++;
            System.out.println("))))) " + numberOfAppendedLogs);
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
