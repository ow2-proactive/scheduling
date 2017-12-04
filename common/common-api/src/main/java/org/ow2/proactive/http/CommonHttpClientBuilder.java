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


/**
 * Utility class used to perform http connections with trust strategy
 */
public class CommonHttpClientBuilder {

    private static final AllowAllTrustStrategy ACCEPT_ANY_CERTIFICATE_TRUST_STRATEGY = new AllowAllTrustStrategy();

    private Boolean overrideAllowAnyHostname;

    private Boolean overrideAllowAnyCertificate;

    private int maxConnections;

    private RequestConfig requestConfig;

    private boolean useContentCompression;

    protected boolean useSystemProperties;

    protected boolean acceptAnyCertificate = false;

    protected boolean acceptAnyHostname = false;

    public CommonHttpClientBuilder() {
        this.useContentCompression = true;
    }

    public CommonHttpClientBuilder allowAnyHostname(boolean acceptAnyHostname) {
        this.overrideAllowAnyHostname = acceptAnyHostname;
        return this;
    }

    public CommonHttpClientBuilder allowAnyCertificate(boolean acceptAnyCertificate) {
        this.overrideAllowAnyCertificate = acceptAnyCertificate;
        return this;
    }

    public CommonHttpClientBuilder disableContentCompression() {
        this.useContentCompression = false;
        return this;
    }

    public CommonHttpClientBuilder insecure(boolean insecure) {
        allowAnyHostname(insecure);
        allowAnyCertificate(insecure);
        return this;
    }

    public CommonHttpClientBuilder maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public CommonHttpClientBuilder setDefaultRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public CommonHttpClientBuilder useSystemProperties() {
        return useSystemProperties(true);
    }

    public CommonHttpClientBuilder useSystemProperties(boolean useSystemProperties) {
        this.useSystemProperties = useSystemProperties;
        return this;
    }

    public CloseableHttpClient build() {
        org.apache.http.impl.client.HttpClientBuilder internalHttpClientBuilder = createInternalHttpClientBuilder();

        if (useSystemProperties) {
            internalHttpClientBuilder.useSystemProperties();
        }

        if (overrideAllowAnyHostname != null) {
            if (overrideAllowAnyHostname) {
                acceptAnyHostname = true;
            } else {
                acceptAnyHostname = false;
            }
        }

        if (overrideAllowAnyCertificate != null) {
            if (overrideAllowAnyCertificate) {
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
