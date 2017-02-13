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
package org.ow2.proactive_grid_cloud_portal.common;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;


public class SessionStoreTest {

    private SessionStore sessionStore;

    private SchedulerRMProxyFactory schedulerProxyFactory;

    private Clock clock;

    @Before
    public void setUp() throws Exception {
        sessionStore = new SessionStore();
        schedulerProxyFactory = mock(SchedulerRMProxyFactory.class);
        sessionStore.setSchedulerRMProxyFactory(schedulerProxyFactory);
        clock = mock(Clock.class);
        sessionStore.setClock(clock);
    }

    @Test
    public void testNoSessionWhenNotLoggedIn() throws Exception {
        assertNull(sessionStore.get("unknownSession"));
    }

    @Test
    public void testSessionCreatedWhenLoggedIn() throws Exception {
        String sessionId = sessionStore.createUnnamedSession().getSessionId();
        Session session = sessionStore.get(sessionId);

        assertNotNull(session);
    }

    @Test
    public void testSchedulerCreatedOnLoginAndRmLazyCreation() throws Exception {
        String sessionId = sessionStore.createUnnamedSession().getSessionId();
        Session session = sessionStore.get(sessionId);

        verifyZeroInteractions(schedulerProxyFactory);
        assertNull(session.getScheduler());

        when(schedulerProxyFactory.connectToScheduler(Matchers.<CredData> any())).thenReturn(mock(SchedulerProxyUserInterface.class));
        when(schedulerProxyFactory.connectToRM(Matchers.<CredData> any())).thenReturn(mock(RMProxyUserInterface.class));
        session.connectToScheduler(new CredData("login", "password"));

        assertNotNull(session.getScheduler());
        assertNotNull(session.getRM());
    }

    @Test
    public void testRmCreatedOnLoginAndSchedulerLazyCreation() throws Exception {
        String sessionId = sessionStore.createUnnamedSession().getSessionId();
        Session session = sessionStore.get(sessionId);

        verifyZeroInteractions(schedulerProxyFactory);
        assertNull(session.getScheduler());

        when(schedulerProxyFactory.connectToScheduler(Matchers.<CredData> any())).thenReturn(mock(SchedulerProxyUserInterface.class));
        when(schedulerProxyFactory.connectToRM(Matchers.<CredData> any())).thenReturn(mock(RMProxyUserInterface.class));
        session.connectToRM(new CredData("login", "password"));

        assertNotNull(session.getRM());
        assertNotNull(session.getScheduler());
    }

    @Test
    public void testSessionAllExpired() throws Exception {
        when(clock.now()).thenReturn(0L);

        sessionStore.createUnnamedSession();

        assertEquals(1, sessionStore.size());

        sessionStore.terminateExpiredSessions(0);

        assertEquals(0, sessionStore.size());
    }

    @Test
    public void testSessionOneIsExpired() throws Exception {
        when(clock.now()).thenReturn(0L);
        sessionStore.createUnnamedSession();

        when(clock.now()).thenReturn(50L);
        sessionStore.createUnnamedSession();

        assertEquals(2, sessionStore.size());

        when(clock.now()).thenReturn(100L);

        sessionStore.terminateExpiredSessions(100);

        assertEquals(1, sessionStore.size());
    }

    @Test
    public void testSessionIsRenewed() throws Exception {
        when(clock.now()).thenReturn(0L);
        Session session = sessionStore.createUnnamedSession();

        // sometimes later we access the session
        when(clock.now()).thenReturn(50L);
        session.getRM();

        // this session should not be removed
        when(clock.now()).thenReturn(100L);
        sessionStore.terminateExpiredSessions(100);

        assertEquals(1, sessionStore.size());

        // sometimes later we access the session
        when(clock.now()).thenReturn(150L);
        session.getScheduler();

        // this session should not be removed
        when(clock.now()).thenReturn(200L);
        sessionStore.terminateExpiredSessions(100);

        assertEquals(1, sessionStore.size());
    }

    @Test
    public void testSessionLogout() throws Exception {
        Session session = sessionStore.createUnnamedSession();
        sessionStore.terminate(session.getSessionId());

        assertEquals(0, sessionStore.size());

        sessionStore.terminate(session.getSessionId());
    }

    @Test
    public void testShutdownTerminateAll() throws Exception {
        sessionStore.createUnnamedSession();
        sessionStore.terminateAll();

        assertEquals(0, sessionStore.size());

        sessionStore.terminateAll();
    }

}
