/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.config;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Configuration parameters may be overriden according to the following priorities:</br> ">"
 * meaning "configuration parameters defined on the left override those defined on the right", we
 * have: </br> JVM > custom config file > default config file
 *
 */
public class ProActiveConfiguration {
    protected static Properties properties;
    protected static final String PROACTIVE_CONFIG_FILENAME = "ProActiveConfiguration.xml";
    protected static final String PROACTIVE_USER_CONFIG_FILENAME = Constants.USER_CONFIG_DIR +
        File.separator + PROACTIVE_CONFIG_FILENAME;
    protected static ProActiveConfiguration singleton;
    protected static boolean isLoaded = false;
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION);

    static {
        singleton = new ProActiveConfiguration();
    }

    private ProActiveConfiguration() {
        load();
    }

    public static ProActiveConfiguration getInstance() {
        return singleton;
    }

    /**
     * Load the configuration, first look for user defined configuration files, firstly in the
     * system property Constants.PROPERTY_PA_CONFIGURATION_FILE, then a file called
     * .ProActiveConfiguration.xml at the user homedir. The default file is located in the same
     * directory as the ProActiceConfiguration class with the name proacticeConfiguration It is
     * obtained using Class.geressource If the property proactive.configuration is set then its
     * value is used as the configuration file
     */
    public synchronized static void load() {
        if (!isLoaded) {
            checkSystemProperties();

            // loading default values
            String filename = ProActiveConfiguration.class.getResource(PROACTIVE_CONFIG_FILENAME)
                                                          .toString();

            logger.debug("default configuration file " + filename);

            properties = ProActiveConfigurationParser.parse(filename,
                    new Properties());

            filename = null;

            /* First we look for the user defined properties */
            if (System.getProperty(PAProperties.PA_CONFIGURATION_FILE.getKey()) != null) {
                // if specified as a system property
                filename = System.getProperty(PAProperties.PA_CONFIGURATION_FILE.getKey());
            } else {
                // or if the file exists in the user home dir
                File f = new File(System.getProperty("user.home") +
                        File.separator + PROACTIVE_USER_CONFIG_FILENAME);
                if (f.exists()) {
                    filename = f.getAbsolutePath();
                }
            }

            if (filename != null) {
                // override default properties by the ones defined by the user
                logger.debug("using user configuration file : " + filename);
                properties = ProActiveConfigurationParser.parse(filename,
                        properties);
            } else {
                logger.debug("no user configuration file");
            }

            // set the properties
            setProperties(properties);

            if (System.getProperty(PAProperties.LOG4J.getKey()) == null) {
                // if logger is not defined create default logger with level info that logs
                // on the console
                Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
                logger.setAdditivity(false);
                logger.setLevel(Level.INFO);
                logger.addAppender(new ConsoleAppender(new PatternLayout()));
            }

            isLoaded = true;
        }
    }

    static private void checkSystemProperties() {
        Iterator<Object> it = System.getProperties().keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            PAProperties prop = PAProperties.getProperty(key);
            if (prop != null) {
                String value = System.getProperty(key);
                if (!prop.isValid(value)) {
                    logger.warn("Invalid value, " + value + " for key " + key +
                        ". Must be a " + prop.getType().toString());
                }
            } else {
                if (key.startsWith("proactive.")) {
                    logger.warn("Property " + key + " is not declared inside " +
                        PAProperties.class.getSimpleName() + " , ignoring");
                } else {
                    logger.debug("System property " + key +
                        " is not a ProAtive property");
                }
            }
        }
    }

    /**
     * Add the loaded properties to the system
     */
    protected static void setProperties(Properties properties) {
        // order the properties by name
        // increase output readability
        Vector<String> v = new Vector(properties.keySet());
        Collections.sort(v);
        Iterator<String> it = v.iterator();

        while (it.hasNext()) {
            String key = it.next();
            String value = properties.getProperty(key);

            if (System.getProperty(key) == null) {
                logger.debug("key:" + key + " --> value:" + value);
                System.setProperty(key, value);
            } else {
                logger.debug("do not override " + key + ":" +
                    System.getProperty(key) + " with value:" + value);
            }
        }
    }

    /**
     * returns the value of a property or null
     *
     * @param property
     *            the property
     * @return the value of the property
     */
    public String getProperty(String property) {
        return System.getProperty(property);
    }

    /**
     * returns the value of a property or the default value
     *
     * @param property
     *            the property
     * @return the value of the property or the default value if the property does not exist
     */
    public String getProperty(String property, String defaultValue) {
        return System.getProperty(property, defaultValue);
    }

    /**
     * set the value 'value' for the property key 'key'. <i>override any previous value</i>
     *
     * @param key
     *            the of the property
     * @param value
     *            the value of the property
     */
    protected void setProperty(String key, String value) {
        properties.setProperty(key, value);
        System.setProperty(key, value);
    }
}
