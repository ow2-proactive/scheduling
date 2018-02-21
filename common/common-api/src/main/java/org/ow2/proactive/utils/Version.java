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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;


public class Version {

    public static final String UNDEFINED = "Undefined";

    public static final String VERSION_FILE = "version.ini";

    public static final String VERSION_PROPERTY_NAME = "pa.version";

    private static Logger logger = Logger.getLogger(Version.class);

    public static final String PA_VERSION = initVersion();

    private static String initVersion() {
        URL versionFile = Version.class.getResource(VERSION_FILE);
        if (versionFile == null) {
            return UNDEFINED;
        } else {
            Properties properties = new Properties();
            try (InputStream in = versionFile.openStream()) {
                properties.load(in);
                String version = properties.getProperty(VERSION_PROPERTY_NAME);
                if (version == null) {
                    return UNDEFINED;
                } else {
                    return version;
                }
            } catch (IOException e) {
                logger.error("Error when loading version file", e);
                return UNDEFINED;
            }
        }
    }
}
