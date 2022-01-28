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
package org.ow2.proactive.resourcemanager.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.web.WebProperties;

import lombok.Getter;


/**
 * @author ActiveEon Team
 * @since 19/02/20
 */
public class WebPropertiesLoader {

    private static final int DEFAULT_JETTY_HTTP_PORT = 8080;

    private static final int DEFAULT_JETTY_HTTPS_PORT = 8443;

    private static final Logger logger = Logger.getLogger(WebPropertiesLoader.class);

    @Getter
    private String httpProtocol;

    @Getter
    private int restPort;

    public WebPropertiesLoader() {
        initializeRestProperties();
        boolean httpsEnabled = WebProperties.WEB_HTTPS.getValueAsBoolean();
        if (httpsEnabled) {
            httpProtocol = "https";
            restPort = getJettyHttpsPort();
        } else {
            httpProtocol = "http";
            restPort = getJettyHttpPort();
        }
    }

    private void initializeRestProperties() {
        String schedulerHome = getSchedulerHome();
        System.setProperty(WebProperties.REST_HOME.getKey(), schedulerHome);
        WebProperties.load();
        if (!schedulerHome.equals(WebProperties.REST_HOME.getValueAsString())) {
            throw new IllegalStateException("Rest home directory could not be initialized");
        }
    }

    private String getSchedulerHome() {
        if (PAResourceManagerProperties.RM_HOME.isSet()) {
            return PAResourceManagerProperties.RM_HOME.getValueAsString();
        } else {
            return ".";
        }
    }

    private int getJettyHttpPort() {
        if (WebProperties.WEB_HTTP_PORT.isSet()) {
            return WebProperties.WEB_HTTP_PORT.getValueAsInt();
        }
        return DEFAULT_JETTY_HTTP_PORT;
    }

    private int getJettyHttpsPort() {
        if (WebProperties.WEB_HTTPS_PORT.isSet()) {
            return WebProperties.WEB_HTTPS_PORT.getValueAsInt();
        }
        return DEFAULT_JETTY_HTTPS_PORT;
    }

    public String generateDefaultRMHostname() {
        try {
            // best effort, may not work for all machines
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            logger.warn("The hostname is unknown ", e);
            return "localhost";
        }
    }

}
