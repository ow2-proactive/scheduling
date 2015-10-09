/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.common;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SessionTest {

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
