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
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.web.WebProperties;

import com.netiq.websockify.WebsockifyServer;


public class NoVncBootstrap implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {

    private static final Logger LOGGER = ProActiveLogger.getLogger(NoVncBootstrap.class);

    private WebsockifyServer websocketProxy;

    public NoVncBootstrap() {
        // Public constructor is required by servlet spec
    }

    public void contextInitialized(ServletContextEvent sce) {
        if (PortalConfiguration.NOVNC_ENABLED.getValueAsBoolean()) {

            int port = PortalConfiguration.NOVNC_PORT.getValueAsInt();
            WebsockifyServer.SSLSetting sslSetting = WebsockifyServer.SSLSetting.valueOf(PortalConfiguration.NOVNC_SECURED.getValueAsString());
            String keystorePath = PortalConfiguration.getAbsolutePath(PortalConfiguration.NOVNC_KEYSTORE.getValueAsString());
            String password = PortalConfiguration.NOVNC_PASSWORD.getValueAsString();
            String keyPassword = PortalConfiguration.NOVNC_KEYPASSWORD.getValueAsString();

            websocketProxy = new WebsockifyServer();
            websocketProxy.connect(port,
                                   new NoVncSecuredTargetResolver(),
                                   sslSetting,
                                   keystorePath,
                                   password,
                                   keyPassword,
                                   null); // not used parameter

            String noVncProxyUrl = null;
            if (PortalConfiguration.NOVNC_URL.isSet()) {
                noVncProxyUrl = PortalConfiguration.NOVNC_URL.getValueAsString();
            } else if (websocketProxy.getChannel().isBound()) {
                String protocol = "http://";
                if (WebProperties.WEB_HTTPS.getValueAsBoolean() &&
                    (sslSetting == WebsockifyServer.SSLSetting.ON ||
                     sslSetting == WebsockifyServer.SSLSetting.REQUIRED)) {

                    // Testing that the keystore configuration is correct, otherwise NoVNC will not accept https connections
                    try {
                        websocketProxy.validateKeystore(keystorePath, password, keyPassword);
                        protocol = "https://";
                    } catch (Exception e) {
                        LOGGER.warn("NoVNC keystore invalid", e);
                    }
                }
                noVncProxyUrl = protocol + ProActiveInet.getInstance().getHostname() + ":" + port;
                PortalConfiguration.NOVNC_URL.updateProperty(noVncProxyUrl);
            } else {
                LOGGER.warn("Cannot determine NoVNC proxy url");
            }

            LOGGER.info("NoVNC websocket proxy started at " + noVncProxyUrl);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        if (websocketProxy != null) {
            websocketProxy.close();
            LOGGER.info("noVNC websocket proxy stopped");
        }
    }

    public void sessionCreated(HttpSessionEvent se) {
    }

    public void sessionDestroyed(HttpSessionEvent se) {
    }

    public void attributeAdded(HttpSessionBindingEvent sbe) {
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
    }
}
