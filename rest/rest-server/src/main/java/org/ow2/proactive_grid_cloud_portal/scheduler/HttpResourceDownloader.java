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

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.ow2.proactive.http.HttpClientBuilder;


public class HttpResourceDownloader {

    private static final Integer CONNECTION_POOL_SIZE = 5;

    private static ResteasyClient client;

    static {
        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(new HttpClientBuilder().maxConnections(CONNECTION_POOL_SIZE)
                                                                                            .useSystemProperties()
                                                                                            .build());

        client = new ResteasyClientBuilder().httpEngine(engine).build();
    }

    public <T> T getResource(String sessionId, String url, Class<T> clazz) throws IOException {
        Response response = null;
        try {
            ResteasyWebTarget target = client.target(url);
            response = target.request().header("sessionid", sessionId).get();
            if (responseIsNotHttpOk(response)) {
                throw new IOException(String.format("Cannot access resource %s: code %d", url, response.getStatus()));
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
