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
import static org.mockito.Mockito.when;

import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.mockito.Matchers;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;


public class SharedSessionStoreTestUtils {

    public static String createValidSession(RMProxyUserInterface rm)
            throws ActiveObjectCreationException, NodeException, RMException, KeyException, LoginException {
        SchedulerRMProxyFactory schedulerFactory = mock(SchedulerRMProxyFactory.class);
        when(schedulerFactory.connectToRM(Matchers.<CredData> any())).thenReturn(rm);
        SharedSessionStore.getInstance().setSchedulerRMProxyFactory(schedulerFactory);

        // login
        Session session = SharedSessionStore.getInstance().createUnnamedSession();
        session.connectToRM(new CredData());
        return session.getSessionId();
    }

    public static String createValidSession(SchedulerProxyUserInterface scheduler)
            throws LoginException, ActiveObjectCreationException, SchedulerException, NodeException {
        SchedulerRMProxyFactory schedulerFactory = mock(SchedulerRMProxyFactory.class);
        when(schedulerFactory.connectToScheduler(Matchers.<CredData> any())).thenReturn(scheduler);
        SharedSessionStore.getInstance().setSchedulerRMProxyFactory(schedulerFactory);

        // login
        Session session = SharedSessionStore.getInstance().createUnnamedSession();
        session.connectToScheduler(new CredData());
        return session.getSessionId();
    }
}
