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
package org.ow2.proactive.utils;

import static com.google.common.truth.Truth.assertThat;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.web.WebProperties;


/**
 * @author ActiveEon Team
 */
public class JettyStarterTest {

    private JettyStarter jettyStarter;

    @Before
    public void setUp() {
        jettyStarter = new JettyStarter();
    }

    @Test
    public void testCreateHttpServerUsingHttp() {
        Server server = jettyStarter.createHttpServer(8080, 8443, false, false);

        Connector[] connectors = server.getConnectors();

        assertThat(connectors).hasLength(1);
        assertThat(connectors[0].getName()).isEqualTo(JettyStarter.HTTP_CONNECTOR_NAME);
        assertThat(connectors[0].getConnectionFactory(HttpConnectionFactory.class)).isNotNull();
    }

    @Test
    public void testCreateHttpServerUsingHttps() {
        createHttpsContextProperties();

        Server server = jettyStarter.createHttpServer(8080, 8443, true, false);

        Connector[] connectors = server.getConnectors();

        assertThat(connectors).hasLength(1);
        assertThat(connectors[0].getName()).isEqualTo(JettyStarter.HTTPS_CONNECTOR_NAME);
        assertThat(connectors[0].getConnectionFactory(HttpConnectionFactory.class)).isNotNull();
        assertThat(connectors[0].getConnectionFactory(SslConnectionFactory.class)).isNotNull();

        unsetHttpsContextProperties();
    }

    @Test
    public void testCreateHttpServerUsingHttpsAndRedirection() {
        createHttpsContextProperties();

        Server server = jettyStarter.createHttpServer(8080, 8443, true, true);

        Connector[] connectors = server.getConnectors();

        assertThat(connectors).hasLength(2);
        assertThat(connectors[0].getName()).isEqualTo(JettyStarter.HTTP_CONNECTOR_NAME);
        assertThat(connectors[0].getConnectionFactory(HttpConnectionFactory.class)).isNotNull();
        assertThat(connectors[1].getName()).isEqualTo(JettyStarter.HTTPS_CONNECTOR_NAME.toLowerCase());
        assertThat(connectors[1].getConnectionFactory(HttpConnectionFactory.class)).isNotNull();
        assertThat(connectors[1].getConnectionFactory(SslConnectionFactory.class)).isNotNull();

        unsetHttpsContextProperties();
    }

    private void createHttpsContextProperties() {
        WebProperties.WEB_HTTPS_KEYSTORE.updateProperty("path/to/keystore");
        WebProperties.WEB_HTTPS_KEYSTORE_PASSWORD.updateProperty("example");
        WebProperties.WEB_HTTPS_TRUSTSTORE.updateProperty("path/to/truststore");
        WebProperties.WEB_HTTPS_TRUSTSTORE_PASSWORD.updateProperty("example");
    }

    private void unsetHttpsContextProperties() {
        WebProperties.WEB_HTTPS_KEYSTORE.unSet();
        WebProperties.WEB_HTTPS_KEYSTORE_PASSWORD.unSet();
        WebProperties.WEB_HTTPS_TRUSTSTORE.unSet();
        WebProperties.WEB_HTTPS_TRUSTSTORE_PASSWORD.unSet();
    }

    @Test
    public void testGetJettyHttpPortDefaultValue() {
        int jettyHttpPort = jettyStarter.getJettyHttpPort();

        assertThat(jettyHttpPort).isEqualTo(8080);
    }

    @Test
    public void testGetJettyHttpPortUsingWebPortProperty() {
        WebProperties.WEB_PORT.updateProperty("42");

        int jettyHttpPort = jettyStarter.getJettyHttpPort();

        assertThat(jettyHttpPort).isEqualTo(42);

        WebProperties.WEB_PORT.unSet();
    }

    @Test
    public void testGetJettyHttpPortUsingWebHttpPortProperty() {
        WebProperties.WEB_HTTP_PORT.updateProperty("42");

        int jettyHttpPort = jettyStarter.getJettyHttpPort();

        assertThat(jettyHttpPort).isEqualTo(42);

        WebProperties.WEB_HTTP_PORT.unSet();
    }

    @Test
    public void testGetJettyHttpPortUsingWebPortAndWebHttpPortProperties() {
        WebProperties.WEB_PORT.updateProperty("42");
        WebProperties.WEB_HTTP_PORT.updateProperty("43");

        int jettyHttpPort = jettyStarter.getJettyHttpPort();

        assertThat(jettyHttpPort).isEqualTo(43);

        WebProperties.WEB_PORT.unSet();
        WebProperties.WEB_HTTP_PORT.unSet();
    }

}
