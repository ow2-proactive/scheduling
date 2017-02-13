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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.ow2.proactive.web.WebProperties;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * @author ActiveEon Team
 */
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({ org.apache.http.impl.client.HttpClientBuilder.class })
@RunWith(PowerMockRunner.class)
public class HttpClientBuilderTest {

    private HttpClientBuilder httpClientBuilder;

    private org.apache.http.impl.client.HttpClientBuilder internalHttpClientBuilder;

    @Before
    public void setup() throws Exception {
        httpClientBuilder = PowerMockito.spy(new HttpClientBuilder());
        internalHttpClientBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);

        PowerMockito.when(httpClientBuilder, "createInternalHttpClientBuilder").thenReturn(internalHttpClientBuilder);
    }

    @After
    public void tearDown() {
        System.clearProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE);
        System.clearProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME);
    }

    @Test
    public void testAllowAnyHostnameTrue() throws Exception {
        httpClientBuilder.allowAnyHostname(true);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder)
               .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    @Test
    public void testAllowAnyHostnameFalse() throws Exception {
        httpClientBuilder.allowAnyHostname(false);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder, never()).setHostnameVerifier(Mockito.<X509HostnameVerifier> any());
    }

    @Test
    public void testAllowAnyCertificateTrue() throws Exception {
        httpClientBuilder.allowAnyCertificate(true);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder).setSslcontext(Mockito.<SSLContext> any());
    }

    @Test
    public void testAllowAnyCertificateFalse() throws Exception {
        httpClientBuilder.allowAnyCertificate(false);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder, never()).setSslcontext(Mockito.<SSLContext> any());
    }

    @Test
    public void testDisableContentCompression() throws Exception {
        httpClientBuilder.disableContentCompression();
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder).disableContentCompression();
    }

    @Test
    public void testInsecureTrue() throws Exception {
        httpClientBuilder.insecure(true);
        httpClientBuilder.build();

        Mockito.verify(httpClientBuilder).allowAnyHostname(true);
        Mockito.verify(httpClientBuilder).allowAnyCertificate(true);

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder)
               .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Mockito.verify(internalHttpClientBuilder).setSslcontext(Mockito.<SSLContext> any());
    }

    @Test
    public void testInsecureFalse() throws Exception {
        httpClientBuilder.insecure(false);
        httpClientBuilder.build();

        Mockito.verify(httpClientBuilder).allowAnyHostname(false);
        Mockito.verify(httpClientBuilder).allowAnyCertificate(false);

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder, never()).setHostnameVerifier(Mockito.<X509HostnameVerifier> any());
        Mockito.verify(internalHttpClientBuilder, never()).setSslcontext(Mockito.<SSLContext> any());
    }

    @Test
    public void testMaxConnectionsNegativeInput() throws Exception {
        httpClientBuilder.maxConnections(-42);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder, never()).setMaxConnPerRoute(anyInt());
        Mockito.verify(internalHttpClientBuilder, never()).setMaxConnTotal(anyInt());
    }

    @Test
    public void testMaxConnectionsZeroAsInput() throws Exception {
        httpClientBuilder.maxConnections(0);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder, never()).setMaxConnPerRoute(anyInt());
        Mockito.verify(internalHttpClientBuilder, never()).setMaxConnTotal(anyInt());
    }

    @Test
    public void testMaxConnectionsPositiveInput() throws Exception {
        httpClientBuilder.maxConnections(42);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder).setMaxConnPerRoute(42);
        Mockito.verify(internalHttpClientBuilder).setMaxConnTotal(42);
    }

    @Test
    public void testSetDefaultRequestConfig() throws Exception {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(42).build();

        httpClientBuilder.setDefaultRequestConfig(config);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder).setDefaultRequestConfig(config);
    }

    @Test
    public void testUseSystemPropertiesTrue() throws Exception {
        System.setProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE, "true");
        System.setProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME, "TRUE");

        httpClientBuilder.useSystemProperties(true);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder).useSystemProperties();
        Mockito.verify(internalHttpClientBuilder)
               .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Mockito.verify(internalHttpClientBuilder).setSslcontext(Mockito.<SSLContext> any());
    }

    @Test
    public void testUseSystemPropertiesTrue2() throws Exception {
        httpClientBuilder.useSystemProperties(true);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder).useSystemProperties();
        Mockito.verify(internalHttpClientBuilder, never()).setHostnameVerifier(Mockito.<X509HostnameVerifier> any());
        Mockito.verify(internalHttpClientBuilder, never()).setSslcontext(Mockito.<SSLContext> any());
    }

    @Test
    public void testUseSystemPropertiesFalse() throws Exception {
        System.setProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE, "false");
        System.setProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME, "FALSE");

        httpClientBuilder.useSystemProperties(false);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder, never()).useSystemProperties();
        Mockito.verify(internalHttpClientBuilder, never())
               .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Mockito.verify(internalHttpClientBuilder, never()).setSslcontext(Mockito.<SSLContext> any());
    }

    @Test
    public void testUseSystemPropertiesFalse2() throws Exception {
        System.setProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE, "tRuE");
        System.setProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME, "FalsE");

        httpClientBuilder.useSystemProperties(false);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder, never()).useSystemProperties();
        Mockito.verify(internalHttpClientBuilder, never()).setSslcontext(Mockito.<SSLContext> any());
        Mockito.verify(internalHttpClientBuilder, never()).setHostnameVerifier(Mockito.<X509HostnameVerifier> any());
    }

    @Test
    public void testSystemPropertiesOverridenByProgrammaticSettings() {
        System.setProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE, "true");
        System.setProperty(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME, "false");

        httpClientBuilder.useSystemProperties(true);
        httpClientBuilder.allowAnyCertificate(false);
        httpClientBuilder.allowAnyHostname(true);
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder).useSystemProperties();
        Mockito.verify(internalHttpClientBuilder, never()).setSslcontext(Mockito.<SSLContext> any());
        Mockito.verify(internalHttpClientBuilder)
               .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    @Test
    public void testSystemPropertyNames() {
        assertThat(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE).isEqualTo("web.https.allow_any_certificate");
        assertThat(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME).isEqualTo("web.https.allow_any_hostname");
    }

    @Test
    public void testBuildNoConfiguration() throws Exception {
        httpClientBuilder.build();

        Mockito.verify(internalHttpClientBuilder).build();
        Mockito.verify(internalHttpClientBuilder, never()).disableContentCompression();
        Mockito.verify(internalHttpClientBuilder, never()).setDefaultRequestConfig(Mockito.<RequestConfig> any());
    }

}
