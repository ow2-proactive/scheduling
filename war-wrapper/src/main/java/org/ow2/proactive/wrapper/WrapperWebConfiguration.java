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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.web.WebProperties;


/**
 * WrapperWebConfiguration is a class that provides information about the deployment
 * of ProActive on the target Application Server. It mainly returns the host, port and context root
 * of ProActive getstarted.
 */

public class WrapperWebConfiguration {

    private static final Logger logger = Logger.getLogger(WrapperWebConfiguration.class);

    protected WrapperWebConfiguration() {

    }

    /**
     * Returns the url of the getstarted web application
     */
    public static String getStartedUrl() {

        try {

            String paHost = ProActiveInet.getInstance().getHostname();
            int port = WebProperties.WAR_WRAPPER_HTTP_PORT.getValueAsInt();
            String contextRoot = WebProperties.WAR_WRAPPER_CONTEXT_ROOT.getValueAsString();

            String httpProtocol;

            if (WebProperties.WAR_WRAPPER_HTTPS_ENABLED.getValueAsBoolean()) {
                httpProtocol = "https";
                port = WebProperties.WAR_WRAPPER_HTTPS_PORT.getValueAsInt();
            } else {
                httpProtocol = "http";
            }

            return httpProtocol + "://" + paHost + ":" + port + contextRoot;

        } catch (Exception e) {
            logger.warn("Could not find the getStarted URL", e);
            return null;
        }
    }
}
