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
package org.ow2.proactive.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * Created by root on 30/03/17.
 */
public class WrapperWebConfiguration {

    protected static final String REST_CONFIG_PATH = "/config/web/settings.ini";

    private static final Logger logger = Logger.getLogger(WrapperWebConfiguration.class);

    public static String getStartedUrl() {

        try {

            String paHost = ProActiveInet.getInstance().getHostname();

            Properties properties = readWebDeploymentProperties();

            int httpPort = Integer.parseInt(properties.getProperty("ear.wrapper.target.server.http.port", "9080"));
            int httpsPort = Integer.parseInt(properties.getProperty("ear.wrapper.target.server.https.port", "9443"));

            boolean httpsEnabled = isHttpsEnabled(properties);

            int restPort = httpPort;

            String httpProtocol;

            if (httpsEnabled) {
                httpProtocol = "https";
                restPort = httpsPort;
            } else {
                httpProtocol = "http";
            }

            return httpProtocol + "://" + paHost + ":" + restPort;

        } catch (Exception e) {
            logger.warn("Could not find the getStarted URL", e);
            return null;
        }
    }

    private static boolean isHttpsEnabled(Properties properties) {
        return properties.getProperty("ear.wrapper.https.enabled", "false").equalsIgnoreCase("true");
    }

    private static String getSchedulerHome() {
        if (PASchedulerProperties.SCHEDULER_HOME.isSet()) {
            return PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
        } else {
            return ".";
        }
    }

    private static Properties readWebDeploymentProperties() {
        File webPropertiesFile = new File(getSchedulerHome() + REST_CONFIG_PATH);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(webPropertiesFile));
        } catch (IOException e) {
            logger.warn("Could not find Web deployment properties" + webPropertiesFile, e);
        }
        properties.putAll(System.getProperties());
        return properties;
    }
}
