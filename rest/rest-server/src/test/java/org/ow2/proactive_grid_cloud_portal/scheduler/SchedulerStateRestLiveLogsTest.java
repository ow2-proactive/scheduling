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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.jboss.resteasy.client.ProxyFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LoggingEventProcessor;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class SchedulerStateRestLiveLogsTest extends RestTestServer {

    private SchedulerRestInterface client;
    private SchedulerProxyUserInterface scheduler;
    private String sessionId;

    @BeforeClass
    public static void setUpRest() throws Exception {
        addResource(new SchedulerStateRest());
    }

    @Before
    public void setUp() throws Exception {
        client = ProxyFactory.create(SchedulerRestInterface.class, "http://localhost:" + port + "/");
        PortalConfiguration
                .load(new ByteArrayInputStream((PortalConfiguration.scheduler_logforwardingservice_provider +
                    "=" + SynchronousLocalLogForwardingProvider.class.getName()).getBytes()));
        scheduler = mock(SchedulerProxyUserInterface.class);
        sessionId = SharedSessionStoreTestUtils.createValidSession(scheduler);
    }

    @Test
    public void testLiveLogs_OutputRemovedAtEachCall() throws Exception {

        String firstJobId = "42";

        String firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);

        Appender appender = verifyListenAndGetAppender("42");

        assertTrue(firstJobLogs.isEmpty());

        appender.doAppend(createLoggingEvent(firstJobId, "first log"));

        firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        assertThat(firstJobLogs, containsString("first log"));

        appender.doAppend(createLoggingEvent(firstJobId, "other log"));

        firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        assertThat(firstJobLogs, not(containsString("first log")));
        assertThat(firstJobLogs, containsString("other log"));

        firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        assertTrue(firstJobLogs.isEmpty());
    }

    private Appender verifyListenAndGetAppender(String jobId) throws NotConnectedException,
            UnknownJobException, PermissionException, LogForwardingException {
        ArgumentCaptor<AppenderProvider> appenderProviderArgumentCaptor = ArgumentCaptor
                .forClass(AppenderProvider.class);
        verify(scheduler).listenJobLogs(eq(jobId), appenderProviderArgumentCaptor.capture());
        AppenderProvider appenderProvider = appenderProviderArgumentCaptor.getValue();
        return appenderProvider.getAppender();
    }

    private LoggingEvent createLoggingEvent(String firstJobId, String message) {
        return new LoggingEvent(null, Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + firstJobId),
            Level.DEBUG, message, null);
    }

    @Test
    public void testLiveLogs_TwoJobsAtTheSameTime() throws Exception {

        String firstJobId = "42";
        String secondJobId = "43";

        String firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        Appender firstAppender = verifyListenAndGetAppender(firstJobId);
        String secondJobLogs = client.getLiveLogJob(sessionId, secondJobId);
        Appender secondAppender = verifyListenAndGetAppender(secondJobId);

        assertTrue(firstJobLogs.isEmpty());
        assertTrue(secondJobLogs.isEmpty());

        firstAppender.doAppend(createLoggingEvent(firstJobId, "first job"));
        secondAppender.doAppend(createLoggingEvent(secondJobId, "second job"));

        firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        assertThat(firstJobLogs, containsString("first job"));

        secondJobLogs = client.getLiveLogJob(sessionId, secondJobId);
        assertThat(secondJobLogs, containsString("second job"));
    }

    @Test
    public void testLiveLogs_AvailableAndDelete() throws Exception {

        String firstJobId = "42";

        assertEquals(-1, client.getLiveLogJobAvailable(sessionId, "42"));

        String logs = client.getLiveLogJob(sessionId, firstJobId);

        Appender appender = verifyListenAndGetAppender("42");

        assertEquals(0, client.getLiveLogJobAvailable(sessionId, "42"));
        assertTrue(logs.isEmpty());

        appender.doAppend(createLoggingEvent(firstJobId, "first log"));

        assertEquals(1, client.getLiveLogJobAvailable(sessionId, "42"));

        assertTrue(client.deleteLiveLogJob(sessionId, "42"));
        assertEquals(-1, client.getLiveLogJobAvailable(sessionId, "42"));

        // will be lost
        appender.doAppend(createLoggingEvent(firstJobId, "second log"));

        logs = client.getLiveLogJob(sessionId, firstJobId);
        assertTrue(logs.isEmpty());

        appender.doAppend(createLoggingEvent(firstJobId, "other log"));
        appender.doAppend(createLoggingEvent(firstJobId, "more log"));

        assertEquals(2, client.getLiveLogJobAvailable(sessionId, "42"));
        logs = client.getLiveLogJob(sessionId, firstJobId);
        assertThat(logs, not(containsString("first log")));
        assertThat(logs, containsString("other log"));
        assertThat(logs, containsString("more log"));

        assertEquals(0, client.getLiveLogJobAvailable(sessionId, "42"));
        logs = client.getLiveLogJob(sessionId, firstJobId);
        assertTrue(logs.isEmpty());
    }

    public static class SynchronousLocalLogForwardingProvider implements LogForwardingProvider {

        private LoggingEventProcessor eventProcessor;

        @Override
        public AppenderProvider createAppenderProvider(URI serverURI) throws LogForwardingException {
            return new AppenderProvider() {
                @Override
                public Appender getAppender() throws LogForwardingException {
                    return new AppenderSkeleton() {
                        @Override
                        protected void append(LoggingEvent event) {
                            eventProcessor.processEvent(event);
                        }

                        @Override
                        public boolean requiresLayout() {
                            return false;
                        }

                        @Override
                        public void close() {
                        }
                    };
                }
            };
        }

        @Override
        public URI createServer(LoggingEventProcessor eventProcessor) throws LogForwardingException {
            this.eventProcessor = eventProcessor;
            return null;
        }

        @Override
        public void terminateServer() throws LogForwardingException {
        }
    }

}
