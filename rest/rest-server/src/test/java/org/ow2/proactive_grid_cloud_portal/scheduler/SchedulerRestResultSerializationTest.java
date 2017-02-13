/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jboss.resteasy.client.ProxyFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;


/**
 * Created by rossigneux on 30/01/15.
 */
public class SchedulerRestResultSerializationTest extends RestTestServer {

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
        scheduler = mock(SchedulerProxyUserInterface.class);
        sessionId = SharedSessionStoreTestUtils.createValidSession(scheduler);
    }

    @Test
    public void killTask() throws Exception {
        when(scheduler.killTask("123", "Test")).thenReturn(true);
        boolean result = client.killTask(sessionId, "123", "Test");
        assertTrue(result);
    }

    @Test
    public void preemptTask() throws Exception {
        when(scheduler.preemptTask("123", "Test", 5)).thenReturn(true);
        boolean result = client.preemptTask(sessionId, "123", "Test");
        assertTrue(result);
    }

    @Test
    public void restartTask() throws Exception {
        when(scheduler.restartTask("123", "Test", 5)).thenReturn(true);
        boolean result = client.restartTask(sessionId, "123", "Test");
        assertTrue(result);
    }
}
