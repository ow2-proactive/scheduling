/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.utils;

import java.util.Properties;

import org.ow2.proactive.web.WebProperties;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

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
        Server server = jettyStarter.createHttpServer(new Properties(), 8080, 8443, false, false);

        Connector[] connectors = server.getConnectors();

        assertThat(connectors).hasLength(1);
        assertThat(connectors[0].getName()).isEqualTo(JettyStarter.HTTP_CONNECTOR_NAME);
        assertThat(connectors[0].getConnectionFactory(HttpConnectionFactory.class)).isNotNull();
    }

    @Test
    public void testCreateHttpServerUsingHttps() {
        Properties properties = createHttpsContextProperties();

        Server server = jettyStarter.createHttpServer(properties, 8080, 8443, true, false);

        Connector[] connectors = server.getConnectors();

        assertThat(connectors).hasLength(1);
        assertThat(connectors[0].getName()).isEqualTo(JettyStarter.HTTPS_CONNECTOR_NAME);
        assertThat(connectors[0].getConnectionFactory(HttpConnectionFactory.class)).isNotNull();
        assertThat(connectors[0].getConnectionFactory(SslConnectionFactory.class)).isNotNull();
    }

    @Test
    public void testCreateHttpServerUsingHttpsAndRedirection() {
        Properties properties = createHttpsContextProperties();

        Server server = jettyStarter.createHttpServer(properties, 8080, 8443, true, true);

        Connector[] connectors = server.getConnectors();

        assertThat(connectors).hasLength(2);
        assertThat(connectors[0].getName()).isEqualTo(JettyStarter.HTTP_CONNECTOR_NAME);
        assertThat(connectors[0].getConnectionFactory(HttpConnectionFactory.class)).isNotNull();
        assertThat(connectors[1].getName()).isEqualTo(JettyStarter.HTTPS_CONNECTOR_NAME.toLowerCase());
        assertThat(connectors[1].getConnectionFactory(HttpConnectionFactory.class)).isNotNull();
        assertThat(connectors[1].getConnectionFactory(SslConnectionFactory.class)).isNotNull();
    }

    private Properties createHttpsContextProperties() {
        Properties properties = new Properties();
        properties.put(WebProperties.WEB_HTTPS_KEYSTORE, "path/to/keystore");
        properties.put(WebProperties.WEB_HTTPS_KEYSTORE_PASSWORD, "example");
        return properties;
    }

    @Test
    public void testGetJettyHttpPortDefaultValue() {
        int jettyHttpPort =
                jettyStarter.getJettyHttpPort(new Properties());

        assertThat(jettyHttpPort).isEqualTo(8080);
    }

    @Test
    public void testGetJettyHttpPortUsingWebPortProperty() {
        Properties properties = new Properties();
        properties.put(WebProperties.WEB_PORT, "42");

        int jettyHttpPort =
                jettyStarter.getJettyHttpPort(properties);

        assertThat(jettyHttpPort).isEqualTo(42);
    }

    @Test
    public void testGetJettyHttpPortUsingWebHttpPortProperty() {
        Properties properties = new Properties();
        properties.put(WebProperties.WEB_HTTP_PORT, "42");

        int jettyHttpPort =
                jettyStarter.getJettyHttpPort(properties);

        assertThat(jettyHttpPort).isEqualTo(42);
    }

    @Test
    public void testGetJettyHttpPortUsingWebPortAndWebHttpPortProperties() {
        Properties properties = new Properties();
        properties.put(WebProperties.WEB_PORT, "42");
        properties.put(WebProperties.WEB_HTTP_PORT, "43");

        int jettyHttpPort =
                jettyStarter.getJettyHttpPort(properties);

        assertThat(jettyHttpPort).isEqualTo(43);
    }

}