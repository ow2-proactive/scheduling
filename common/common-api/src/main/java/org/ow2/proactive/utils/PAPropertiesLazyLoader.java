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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;


public class PAPropertiesLazyLoader {

    private static Logger logger = Logger.getLogger(PAPropertiesLazyLoader.class);

    private volatile Properties properties = null; // double checked locking with volatile

    private String propertiesFileName;

    /** if properties file is relative, will use this system property value as base folder */
    private String systemPropertyForRelativeFolder;

    /** if properties filename is not defined, will search this system property for a filename */
    private String systemPropertyForFileName;

    /** if properties filename and systemPropertyForFileName are not defined, will use this default file name
     * and search in systemPropertyForRelativeFolder */
    private String pathToRelativePropertiesFile;

    public PAPropertiesLazyLoader(String systemPropertyForRelativeFolder, String systemPropertyForFileName,
            String pathToRelativePropertiesFile) {
        this.systemPropertyForRelativeFolder = systemPropertyForRelativeFolder;
        this.pathToRelativePropertiesFile = pathToRelativePropertiesFile;
        this.systemPropertyForFileName = systemPropertyForFileName;
    }

    public PAPropertiesLazyLoader(String systemPropertyForRelativeFolder, String systemPropertyForFileName,
            String pathToRelativePropertiesFile, String propertiesFileName) {
        this.systemPropertyForRelativeFolder = systemPropertyForRelativeFolder;
        this.pathToRelativePropertiesFile = pathToRelativePropertiesFile;
        this.propertiesFileName = propertiesFileName;
        this.systemPropertyForFileName = systemPropertyForFileName;
    }

    private InputStream resolvePropertiesFile() throws FileNotFoundException {
        String propertiesPath;
        boolean systemPropertyForFileNameSet = false;

        if (propertiesFileName == null) {
            if (System.getProperty(systemPropertyForFileName) != null) {
                propertiesPath = System.getProperty(systemPropertyForFileName);
                systemPropertyForFileNameSet = true;
            } else {
                propertiesPath = pathToRelativePropertiesFile;
            }
        } else {
            propertiesPath = propertiesFileName;
        }

        if (!new File(propertiesPath).isAbsolute()) {
            propertiesPath = System.getProperty(systemPropertyForRelativeFolder) + File.separator + propertiesPath;
        }

        if (new File(propertiesPath).exists()) {
            logger.debug("Loading properties from file " + propertiesPath);
            return new FileInputStream(propertiesPath);
        } else {
            if (systemPropertyForFileNameSet) {
                throw new RuntimeException("RM properties file not found : '" + propertiesPath + "'");
            }
            logger.debug("Loading empty properties");
            return null;
        }
    }

    /**
     * Get the properties map or load it if needed.
     *
     * @return the properties map corresponding to the default property file.
     */
    public Properties getProperties() {
        // double checked locking with volatile
        Properties result = properties; // to avoid volatile access twice
        if (result == null) {
            synchronized (this) {
                result = properties;
                if (result == null) {
                    properties = result = new Properties();
                    try {
                        InputStream stream = resolvePropertiesFile();
                        if (stream != null) {
                            properties.load(stream);
                            stream.close();
                        }
                        updateWithSystemProperties(properties);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Set the user java properties to the PASchedulerProperties.
     * <p>
     * User properties are defined using the -Dname=value in the java command.
     */
    public static void updateWithSystemProperties(Properties properties) {
        if (properties != null) {
            synchronized (System.getProperties()) {
                for (Map.Entry<Object, Object> property : System.getProperties().entrySet()) {
                    if (property.getValue() != null) {
                        properties.setProperty(property.getKey().toString(), property.getValue().toString());
                    }
                }
            }
        }
    }
}
