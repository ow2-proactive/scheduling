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
package org.ow2.proactive.http;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.ow2.proactive.web.WebProperties;


public class HttpClientBuilder {

    private static final AllowAllTrustStrategy ACCEPT_ANY_CERTIFICATE_TRUST_STRATEGY = new AllowAllTrustStrategy();

    private Boolean allowAnyHostname;

    private Boolean allowAnyCertificate;

    private int maxConnections;

    private RequestConfig requestConfig;

    private boolean useContentCompression;

    private boolean useSystemProperties;

    public HttpClientBuilder() {
        this.useContentCompression = true;
    }

    public HttpClientBuilder allowAnyHostname(boolean acceptAnyHostname) {
        this.allowAnyHostname = acceptAnyHostname;
        return this;
    }

    public HttpClientBuilder allowAnyCertificate(boolean acceptAnyCertificate) {
        this.allowAnyCertificate = acceptAnyCertificate;
        return this;
    }

    public HttpClientBuilder disableContentCompression() {
        this.useContentCompression = false;
        return this;
    }

    public HttpClientBuilder insecure(boolean insecure) {
        allowAnyHostname(insecure);
        allowAnyCertificate(insecure);
        return this;
    }

    public HttpClientBuilder maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public HttpClientBuilder setDefaultRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public HttpClientBuilder useSystemProperties() {
        return useSystemProperties(true);
    }

    public HttpClientBuilder useSystemProperties(boolean useSystemProperties) {
        this.useSystemProperties = useSystemProperties;
        return this;
    }

    public CloseableHttpClient build() {
        org.apache.http.impl.client.HttpClientBuilder internalHttpClientBuilder = createInternalHttpClientBuilder();

        boolean acceptAnyCertificate = false;
        boolean acceptAnyHostname = false;

        if (useSystemProperties) {
            internalHttpClientBuilder.useSystemProperties();

            String property = System.getProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE);
            if ("true".equalsIgnoreCase(property)) {
                acceptAnyCertificate = true;
            }

            property = System.getProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME);
            if ("true".equalsIgnoreCase(property)) {
                acceptAnyHostname = true;
            }
        }

        if (allowAnyHostname != null) {
            if (allowAnyHostname) {
                acceptAnyHostname = true;
            } else {
                acceptAnyHostname = false;
            }
        }

        if (allowAnyCertificate != null) {
            if (allowAnyCertificate) {
                acceptAnyCertificate = true;
            } else {
                acceptAnyCertificate = false;
            }
        }

        if (acceptAnyCertificate) {
            internalHttpClientBuilder.setSslcontext(createSslContext());
        }

        if (acceptAnyHostname) {
            internalHttpClientBuilder.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }

        if (maxConnections > 0) {
            internalHttpClientBuilder.setMaxConnPerRoute(maxConnections);
            internalHttpClientBuilder.setMaxConnTotal(maxConnections);
        }

        if (requestConfig != null) {
            internalHttpClientBuilder.setDefaultRequestConfig(requestConfig);
        }

        if (!useContentCompression) {
            internalHttpClientBuilder.disableContentCompression();
        }

        return internalHttpClientBuilder.build();
    }

    public org.apache.http.impl.client.HttpClientBuilder createInternalHttpClientBuilder() {
        org.apache.http.impl.client.HttpClientBuilder result = org.apache.http.impl.client.HttpClientBuilder.create();

        result.setUserAgent("ProActive");

        return result;
    }

    protected SSLContext createSslContext() {
        try {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial(null, ACCEPT_ANY_CERTIFICATE_TRUST_STRATEGY);
            return sslContextBuilder.build();
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
