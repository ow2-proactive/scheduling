package org.ow2.proactive_grid_cloud_portal.webapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import com.netiq.websockify.WebsockifyServer;
import org.apache.log4j.Logger;


public class NoVncBootstrap implements ServletContextListener, HttpSessionListener,
        HttpSessionAttributeListener {

    private static final Logger LOGGER = ProActiveLogger.getLogger(NoVncBootstrap.class);

    private WebsockifyServer websocketProxy;

    public NoVncBootstrap() {
        // Public constructor is required by servlet spec
    }

    public void contextInitialized(ServletContextEvent sce) {
        if (Boolean.parseBoolean(PortalConfiguration.getProperties().getProperty(
                PortalConfiguration.novnc_enabled))) {

            int port = Integer.parseInt(PortalConfiguration.getProperties().getProperty(
                    PortalConfiguration.novnc_port));
            WebsockifyServer.SSLSetting sslSetting = WebsockifyServer.SSLSetting.valueOf(PortalConfiguration
                    .getProperties().getProperty(PortalConfiguration.novnc_secured));
            String keystorePath = PortalConfiguration.getProperties().getProperty(
                    PortalConfiguration.novnc_keystore);
            String password = PortalConfiguration.getProperties().getProperty(
                    PortalConfiguration.novnc_password);
            String keyPassword = PortalConfiguration.getProperties().getProperty(
                    PortalConfiguration.novnc_keypassword);

            websocketProxy = new WebsockifyServer();
            websocketProxy.connect(port, new NoVncSecuredTargetResolver(), sslSetting, keystorePath,
                    password, keyPassword, null); // not used parameter

            LOGGER.info("noVNC websocket proxy started");
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
