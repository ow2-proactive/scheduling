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
package org.ow2.proactive_grid_cloud_portal.rm;

import org.ow2.proactive.resourcemanager.common.util.RMCachingProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RMRestTest extends RestTestServer {

    @BeforeClass
    public static void setUpRest() throws Exception {
        addResource(new RMRest());
    }

    @Test
    public void testShutdown_NoPreemptParameter() throws Exception {
        RMCachingProxyUserInterface rm = mock(RMCachingProxyUserInterface.class);
        when(rm.shutdown(false)).thenReturn(new BooleanWrapper(true));

        String sessionId = RMSessionMapper.getInstance().add(rm);
        HttpResponse response = callHttpMethod("shutdown", sessionId);

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
        verify(rm).shutdown(false);
    }

    @Test
    public void testShutdown_PreemptParameter() throws Exception {
        RMCachingProxyUserInterface rm = mock(RMCachingProxyUserInterface.class);
        when(rm.shutdown(true)).thenReturn(new BooleanWrapper(true));

        String sessionId = RMSessionMapper.getInstance().add(rm);
        HttpResponse response = callHttpMethod("shutdown?preempt=true", sessionId);

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusLine().getStatusCode());
        verify(rm).shutdown(true);
    }

    private HttpResponse callHttpMethod(String httpMethod, String sessionId) throws IOException {
        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/rm/" + httpMethod);
        httpGet.setHeader("sessionid", sessionId);
        return new DefaultHttpClient().execute(httpGet);
    }

}
