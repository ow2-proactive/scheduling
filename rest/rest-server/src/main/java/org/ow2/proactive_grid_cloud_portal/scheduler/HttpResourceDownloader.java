/*
 * ################################################################
 *
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.ws.rs.core.Response;

import org.ow2.proactive.http.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

public class HttpResourceDownloader {

    private static final Integer CONNECTION_POOL_SIZE = 5;

    private static ResteasyClient client;

    static {
        ApacheHttpClient4Engine engine =
                new ApacheHttpClient4Engine(
                        new HttpClientBuilder().maxConnections(CONNECTION_POOL_SIZE).useSystemProperties().build());

        client = new ResteasyClientBuilder().httpEngine(engine).build();
    }

    public <T> T getResource(String sessionId, String url, Class<T> clazz) throws IOException {
        Response response = null;
        try {
            ResteasyWebTarget target = client.target(url);
            response = target.request().header("sessionid", sessionId).get();
            if (responseIsNotHttpOk(response)) {
                throw new IOException(String.format(
                        "Cannot access resource %s: code %d", url, response.getStatus()));
            }
            return (T) response.readEntity(clazz);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private boolean responseIsNotHttpOk(Response response) {
        return response.getStatus() != HttpURLConnection.HTTP_OK;
    }

}
