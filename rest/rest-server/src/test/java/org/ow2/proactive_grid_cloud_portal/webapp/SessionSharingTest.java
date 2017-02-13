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
package org.ow2.proactive_grid_cloud_portal.webapp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import javax.ws.rs.NotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.RMStateNodeUrls;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRMProxyFactory;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.rm.RMRest;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.studio.StudioRest;


public class SessionSharingTest {

    private SchedulerStateRest schedulerRest;

    private RMRest rmRest;

    private StudioRest studioRest;

    private RMProxyUserInterface rmMock;

    private SchedulerProxyUserInterface schedulerMock;

    @Before
    public void setUp() throws Exception {
        schedulerRest = new SchedulerStateRest();
        rmRest = new RMRest();
        studioRest = new StudioRest();

        SchedulerRMProxyFactory schedulerFactory = mock(SchedulerRMProxyFactory.class);
        rmMock = mock(RMProxyUserInterface.class);
        when(schedulerFactory.connectToRM(Matchers.<CredData> any())).thenReturn(rmMock);
        schedulerMock = mock(SchedulerProxyUserInterface.class);
        when(schedulerFactory.connectToScheduler(Matchers.<CredData> any())).thenReturn(schedulerMock);

        SharedSessionStore.getInstance().setSchedulerRMProxyFactory(schedulerFactory);
    }

    @Test
    public void sessions_are_shared_scheduler_login() throws Exception {
        String sessionId = schedulerRest.login("login", "pw");

        when(rmMock.getState()).thenReturn(new RMState(new RMStateNodeUrls(new HashSet<String>(),
                                                                           new HashSet<String>(),
                                                                           new HashSet<String>()),
                                                       Long.valueOf(-1)));
        RMState rmState = rmRest.getState(sessionId);
        assertNotNull(rmState);

        when(schedulerMock.freeze()).thenReturn(true);
        boolean frozen = schedulerRest.freezeScheduler(sessionId);
        assertTrue(frozen);

        schedulerRest.disconnect(sessionId);

        try {
            rmRest.getState(sessionId);
            fail();
        } catch (NotConnectedException expected) {
            // expected
        }

        try {
            schedulerRest.freezeScheduler(sessionId);
            fail();
        } catch (NotConnectedRestException expected) {
            // expected
        }
    }

    @Test
    public void sessions_scheduler_login_by_sessionid() throws Exception {
        String sessionId = schedulerRest.login("login", "pw");

        String login = schedulerRest.getLoginFromSessionId(sessionId);

        assertThat(login, is("login"));
    }

    @Test
    public void sessions_scheduler_login_by_wrong_sessionid() throws Exception {
        schedulerRest.login("login", "pw");

        String login = schedulerRest.getLoginFromSessionId("whatever");
        assertThat(login, is(""));
    }

    @Test
    public void sessions_are_shared_rm_login() throws Exception {
        String sessionId = rmRest.rmConnect("login", "pw");

        assertTrue(studioRest.getWorkflows(sessionId).isEmpty());

        when(schedulerMock.freeze()).thenReturn(true);
        boolean frozen = schedulerRest.freezeScheduler(sessionId);
        assertTrue(frozen);

        when(rmMock.getState()).thenReturn(new RMState(new RMStateNodeUrls(new HashSet<String>(),
                                                                           new HashSet<String>(),
                                                                           new HashSet<String>()),
                                                       Long.valueOf(-1)));
        RMState rmState = rmRest.getState(sessionId);
        assertNotNull(rmState);

        rmRest.rmDisconnect(sessionId);

        try {
            rmRest.getState(sessionId);
            fail();
        } catch (NotConnectedException expected) {
            // expected
        }

        try {
            schedulerRest.freezeScheduler(sessionId);
            fail();
        } catch (NotConnectedRestException expected) {
            // expected
        }
    }
}
