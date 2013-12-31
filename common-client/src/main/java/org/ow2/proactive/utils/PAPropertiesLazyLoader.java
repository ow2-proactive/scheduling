/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


public class PAPropertiesLazyLoader {

    private static Logger logger = Logger.getLogger(PAPropertiesLazyLoader.class);

    private Properties properties;

    private String propertiesFileName;

    /** if properties file is relative, will use this folder as base folder */
    private String propertiesRelativeFolder;

    /** if properties filename is not defined, will search this system property for a filename */
    private String systemPropertyForFileName;

    /** if properties filename and systemPropertyForFileName are not defined, will use this default file name
     * and search in propertiesRelativeFolder and then JARs */
    private String pathToRelativePropertiesFile;

    public PAPropertiesLazyLoader(String propertiesRelativeFolder,
      String systemPropertyForFileName,
      String pathToRelativePropertiesFile) {
        this.propertiesRelativeFolder = propertiesRelativeFolder;
        this.pathToRelativePropertiesFile = pathToRelativePropertiesFile;
        this.systemPropertyForFileName = systemPropertyForFileName;
    }

    public PAPropertiesLazyLoader(String propertiesRelativeFolder,
      String systemPropertyForFileName,
      String pathToRelativePropertiesFile,
      String propertiesFileName) {
        this.propertiesRelativeFolder = propertiesRelativeFolder;
        this.pathToRelativePropertiesFile = pathToRelativePropertiesFile;
        this.propertiesFileName = propertiesFileName;
        this.systemPropertyForFileName = systemPropertyForFileName;
    }

    private InputStream init(String filename) throws FileNotFoundException {
        String propertiesPath;
        boolean jPropSet = false;
        if (filename == null) {
            if (System.getProperty(systemPropertyForFileName) != null) {
                propertiesPath = System.getProperty(systemPropertyForFileName);
                jPropSet = true;
            } else {
                propertiesPath = pathToRelativePropertiesFile;
            }
        } else {
            propertiesPath = filename;
        }
        if (!new File(propertiesPath).isAbsolute()) {
            propertiesPath = propertiesRelativeFolder + File.separator + propertiesPath;
        }

        if (new File(propertiesPath).exists()) {
            logger.info("Loading properties from file " + propertiesPath);
            return new FileInputStream(propertiesPath);
        } else {
            if (jPropSet) {
                throw new RuntimeException("RM properties file not found : '" + propertiesPath + "'");
            }
            String propertiesFromJar = "/" + pathToRelativePropertiesFile;
            logger.info("Loading properties from JAR " + propertiesFromJar);
            return PAPropertiesLazyLoader.class.getResourceAsStream(propertiesFromJar);
        }
    }

    /**
     * Get the properties map or load it if needed.
     *
     * @return the properties map corresponding to the default property file.
     */
    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                InputStream stream = init(propertiesFileName);
                if (stream == null) {
                    return properties;
                }
                properties.load(stream);
                stream.close();
                updateWithSystemProperties(properties);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    /**
     * Set the user java properties to the PASchedulerProperties.<br/>
     * User properties are defined using the -Dname=value in the java command.
     */
    public static void updateWithSystemProperties(Properties properties) {
        if (properties != null) {
            for (Object o : properties.keySet()) {
                String s = System.getProperty((String) o);
                if (s != null) {
                    properties.setProperty((String) o, s);
                }
            }
        }
    }
}
