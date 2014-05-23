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

import java.io.ByteArrayInputStream;
import java.net.URI;

import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ProxyFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;


public class SchedulerStateRestLiveLogsTest extends RestTestServer {

    private SchedulerRestInterface client;

    @BeforeClass
    public static void setUpRest() throws Exception {
        addResource(new SchedulerStateRest());
    }

    @Before
    public void setUp() throws Exception {
        client = ProxyFactory.create(SchedulerRestInterface.class, "http://localhost:" + port + "/");
        PortalConfiguration.load(new ByteArrayInputStream(
            (PortalConfiguration.scheduler_logforwardingservice_provider + "=" + LogService.class.getName())
                    .getBytes()));
    }

    @Test
    public void testLiveLogs_OutputRemovedAtEachCall() throws Exception {
        String sessionId = SharedSessionStoreTestUtils
                .createValidSession(mock(SchedulerProxyUserInterface.class));

        String firstJobId = "42";

        String firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);

        assertEquals("", firstJobLogs);

        Logger firstJobLogger = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + firstJobId);

        firstJobLogger.debug("first log");

        firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        assertThat(firstJobLogs, containsString("first log"));

        firstJobLogger.debug("other log");

        firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        assertThat(firstJobLogs, not(containsString("first log")));
        assertThat(firstJobLogs, containsString("other log"));

        firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        assertEquals("", firstJobLogs);
    }

    @Test
    public void testLiveLogs_TwoJobsAtTheSameTime() throws Exception {
        String sessionId = SharedSessionStoreTestUtils
                .createValidSession(mock(SchedulerProxyUserInterface.class));

        String firstJobId = "42";
        String secondJobId = "43";

        String firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        String secondJobLogs = client.getLiveLogJob(sessionId, secondJobId);

        assertEquals("", firstJobLogs);
        assertEquals("", secondJobLogs);

        Logger firstJobLogger = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + firstJobId);
        Logger secondJobLogger = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + secondJobId);

        firstJobLogger.debug("first job");
        secondJobLogger.debug("second job");

        firstJobLogs = client.getLiveLogJob(sessionId, firstJobId);
        assertThat(firstJobLogs, containsString("first job"));

        secondJobLogs = client.getLiveLogJob(sessionId, secondJobId);
        assertThat(secondJobLogs, containsString("second job"));
    }

    public static class LogService implements LogForwardingProvider {

        @Override
        public AppenderProvider createAppenderProvider(URI serverURI) throws LogForwardingException {
            return null;
        }

        @Override
        public URI createServer() throws LogForwardingException {
            return null;
        }

        @Override
        public void terminateServer() throws LogForwardingException {
        }
    }
}
