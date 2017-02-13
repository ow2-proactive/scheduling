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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;


public class SessionTest {

    /**
     * Check that session id does not change if {@link Session#renewSession} is called on an existing session instance.
     * The test also checks that {@link SchedulerProxyUserInterface#renewSession} is invoked on embedded scheduler
     * instance if available.
     *
     * @throws NotConnectedException
     */
    @Test
    public void testRenewSession() throws NotConnectedException {
        SchedulerRMProxyFactory schedulerProxyFactory = mock(SchedulerRMProxyFactory.class);
        SchedulerProxyUserInterface scheduler = mock(SchedulerProxyUserInterface.class);

        Session session = new Session("sessionId", schedulerProxyFactory, new Clock());
        session.setScheduler(scheduler);
        session.renewSession();

        Assert.assertEquals("sessionId", session.getSessionId());
        verify(scheduler).renewSession();
    }

}
