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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyStore;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ow2.proactive.http.HttpClientBuilder;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.web.WebProperties;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;


public class HttpResourceDownloader {

    private static final Logger logger = Logger.getLogger(HttpResourceDownloader.class);

    private static final Integer CONNECTION_POOL_SIZE = 5;

    private static ResteasyClient client;

    private static HttpResourceDownloader instance = null;

    public static HttpResourceDownloader getInstance() {
        if (instance == null) {
            instance = new HttpResourceDownloader();
        }
        return instance;
    }

    private HttpResourceDownloader() {
        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(new HttpClientBuilder().maxConnections(CONNECTION_POOL_SIZE)
                                                                                            .useSystemProperties()
                                                                                            .build());

        ResteasyProviderFactory providerFactory = ResteasyProviderFactory.getInstance();
        SchedulerRestClient.registerGzipEncoding(providerFactory);

        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder().providerFactory(providerFactory)
                                                                         .httpEngine(engine);

        if (WebProperties.WEB_HTTPS_TRUSTSTORE.isSet() && WebProperties.WEB_HTTPS_TRUSTSTORE_PASSWORD.isSet()) {
            String trustStorePath = null;
            try {
                trustStorePath = PASchedulerProperties.getAbsolutePath(WebProperties.WEB_HTTPS_TRUSTSTORE.getValueAsString());
                KeyStore keyStore = KeyStore.getInstance("PKCS12");

                File f = new File(trustStorePath);
                try (FileInputStream fis = new FileInputStream(f)) {
                    char[] password = WebProperties.WEB_HTTPS_TRUSTSTORE_PASSWORD.getValueAsString().toCharArray();
                    keyStore.load(fis, password);
                    clientBuilder = clientBuilder.trustStore(keyStore);
                }
            } catch (Exception e) {
                logger.error("Error why loading keystore " + trustStorePath, e);
            }
        }

        if (WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME.getValueAsBoolean() &&
            WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE.getValueAsBoolean()) {
            clientBuilder = clientBuilder.disableTrustManager();
        } else if (WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME.getValueAsBoolean()) {
            clientBuilder = clientBuilder.hostnameVerification(ResteasyClientBuilder.HostnameVerificationPolicy.ANY);
        }

        if (WebProperties.RESOURCE_DOWNLOADER_PROXY.isSet() && WebProperties.RESOURCE_DOWNLOADER_PROXY_PORT.isSet() &&
            WebProperties.RESOURCE_DOWNLOADER_PROXY_SCHEME.isSet()) {
            try {
                String proxyAddress = WebProperties.RESOURCE_DOWNLOADER_PROXY.getValueAsString();
                int proxyPort = WebProperties.RESOURCE_DOWNLOADER_PROXY_PORT.getValueAsInt();
                String scheme = WebProperties.RESOURCE_DOWNLOADER_PROXY_SCHEME.getValueAsString();
                clientBuilder = clientBuilder.defaultProxy(proxyAddress, proxyPort, scheme);
            } catch (Exception e) {
                logger.error("Error while initializing Resource Downloader Proxy", e);
            }
        }

        client = clientBuilder.build();
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
