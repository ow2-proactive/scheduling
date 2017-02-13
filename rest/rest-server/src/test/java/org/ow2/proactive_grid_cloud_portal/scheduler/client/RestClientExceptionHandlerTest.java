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
package org.ow2.proactive_grid_cloud_portal.scheduler.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.security.auth.login.LoginException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRMProxyFactory;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;


public class RestClientExceptionHandlerTest extends RestTestServer {

    @BeforeClass
    public static void setUpRest() throws Exception {
        addResource(new SchedulerStateRest());
    }

    @Test
    public void client_handles_jetty_errors_404() throws Exception {
        try {
            SchedulerRestClient client = new SchedulerRestClient("http://localhost:" + port + "/" +
                                                                 "rest/a_path_that_does_not_exist");

            client.getScheduler().login("demo", "demo");
            fail("Should have throw an exception");
        } catch (WebApplicationException e) {
            assertTrue(e instanceof NotFoundException);
            assertEquals(404, e.getResponse().getStatus());
        }
    }

    @Test
    public void client_handles_jetty_errors_500() throws Exception {
        try {
            SchedulerRMProxyFactory schedulerFactory = mock(SchedulerRMProxyFactory.class);
            when(schedulerFactory.connectToScheduler(Matchers.<CredData> any())).thenThrow(new LoginException());
            SharedSessionStore.getInstance().setSchedulerRMProxyFactory(schedulerFactory);

            SchedulerRestClient client = new SchedulerRestClient("http://localhost:" + port + "/");

            client.getScheduler().login("demo", "demo");
            fail("Should have throw an exception");
        } catch (WebApplicationException e) {
            assertTrue(e instanceof InternalServerErrorException);
            assertEquals(500, e.getResponse().getStatus());
        }
    }

    @Test
    public void client_handles_unknown_session_id() throws Exception {
        try {
            SchedulerRestClient client = new SchedulerRestClient("http://localhost:" + port + "/");

            client.getScheduler().listJobs("nonExisting", "42");
            fail("Should have throw an exception");
        } catch (WebApplicationException e) {
            assertTrue(e instanceof InternalServerErrorException);
            assertEquals(500, e.getResponse().getStatus());
        }
    }
}
