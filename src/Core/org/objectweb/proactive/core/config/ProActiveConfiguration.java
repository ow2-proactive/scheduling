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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.config;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.xml.PropertyHandler;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Configuration parameters may be overriden according to the following priorities:</br>
 * ">" meaning "configuration parameters defined on the left override those defined on the right", we have: </br>
 * JVM > custom config file > default config file
 *
 */
public class ProActiveConfiguration {
    protected static Properties properties;
    protected static final String PROACTIVE_CONFIG_FILENAME = "ProActiveConfiguration.xml";
    protected static ProActiveConfiguration singleton;
    protected static boolean isLoaded = false;
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION);

    private ProActiveConfiguration() {
    }

    protected static synchronized void createConfiguration() {
        if (ProActiveConfiguration.singleton == null) {
            ProActiveConfiguration.singleton = new ProActiveConfiguration();
        }
    }

    /**
     * Load the configuration, first look for user defined configuration files, firstly in
     * the system property Constants.PROPERTY_PA_CONFIGURATION_FILE, then a file called
     * .ProActiveConfiguration.xml at the user homedir.
     * The default file is located in the same directory as the ProActiceConfiguration
     * class with the name proacticeConfiguration
     * It is obtained using Class.geressource
     * If the property proactive.configuration is set then its value is used
     * as the configuration file
     */
    public synchronized static void load() {
        if (!isLoaded) {
            // loading  default values
            String filename = ProActiveConfiguration.class.getResource(PROACTIVE_CONFIG_FILENAME)
                                                          .toString();

            logger.debug("default configuration file " + filename);

            properties = PropertyHandler.createMasterFileHandler(filename,
                    new Properties());

            filename = null;

            /* First we look for the user defined properties */
            if (ProActiveConfiguration.getProperty(
                        Constants.PROPERTY_PA_CONFIGURATION_FILE) != null) {
                // if specified as a system property
                filename = ProActiveConfiguration.getProperty(Constants.PROPERTY_PA_CONFIGURATION_FILE);
            } else {
                // or if the file exists in the user home dir
                File f = new File(System.getProperty("user.dir") +
                        File.separator + "." + PROACTIVE_CONFIG_FILENAME);
                if (f.exists()) {
                    filename = f.getAbsolutePath();
                }
            }

            logger.debug("user configuration file " + filename);
            if (filename != null) {
                // override default properties by the ones defined by the user
                properties = PropertyHandler.createMasterFileHandler(filename,
                        properties);
            }

            // set the properties
            setProperties(properties);

            if (System.getProperty("log4j.configuration") == null) {
                //if logger is not defined create default logger with level info that logs
                // on the console
                Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
                logger.setAdditivity(false);
                logger.setLevel(Level.INFO);
                logger.addAppender(new ConsoleAppender(new PatternLayout()));
            }

            isLoaded = true;
        }
    }

    public synchronized static ProActiveConfiguration getConfiguration() {
        if (ProActiveConfiguration.singleton == null) {
            ProActiveConfiguration.createConfiguration();
        }
        return singleton;
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
            String key = (String) it.next();
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

    public static String getProperty(String property) {
        return System.getProperty(property);
    }

    public static String getProperty(String property, String defaultValue) {
        return System.getProperty(property, defaultValue);
    }

    //    /**
    //     * Dump loaded properties, i.e properties found in the configuration file
    //     */
    //    public void dumpLoadedProperties() {
    //        Iterator it = loadedProperties.keySet().iterator();
    //        while (it.hasNext()) {
    //            String name = (String) it.next();
    //
    //            //            System.out.println("Name = " + name);
    //            //            System.out.println("Value = " + this.loadedProperties.get(name));
    //        }
    //    }
    //
    //    /**
    //     * Dump properties added to the system, i.e loaded properties
    //     * which were not already in the system
    //     */
    //    public void dumpAddedProperties() {
    //        Iterator it = addedProperties.keySet().iterator();
    //        while (it.hasNext()) {
    //            String name = (String) it.next();
    //
    //            //            System.out.println("Name = " + name);
    //            //            System.out.println("Value = " + this.addedProperties.get(name));
    //        }
    //    }
    public static String getLocationServerClass() {
        return System.getProperty("proactive.locationserver");
    }

    public static String getLocationServerRmi() {
        return System.getProperties().getProperty("proactive.locationserver.rmi");
    }

    public static String getACState() {
        return System.getProperty("proactive.future.ac");
    }

    public static String getSchemaValidationState() {
        return System.getProperty("schema.validation");
    }

    // FAULT TOLERANCE
    public static String getFTState() {
        return System.getProperty("proactive.ft");
    }

    public static String getCheckpointServer() {
        return System.getProperty("proactive.ft.server.checkpoint");
    }

    public static String getLocationServer() {
        return System.getProperty("proactive.ft.server.location");
    }

    public static String getRecoveryServer() {
        return System.getProperty("proactive.ft.server.recovery");
    }

    public static String getGlobalFTServer() {
        return System.getProperty("proactive.ft.server.global");
    }

    public static String getTTCValue() {
        return System.getProperty("proactive.ft.ttc");
    }

    public static String getAttachedResourceServer() {
        return System.getProperty("proactive.ft.server.resource");
    }

    public static String getFTProtocol() {
        return System.getProperty("proactive.ft.protocol");
    }

    public static boolean osgiServletEnabled() {
        return "enabled".equals(System.getProperty("proactive.http.servlet"));
    }

    // Cached value since isForwarder is frequently called
    private static boolean isForwarder;

    static {
        String prop = System.getProperty("proactive.hierarchicalRuntime");
        isForwarder = ((prop != null) &&
            (prop.equals("true") || prop.equals("root")));
    }

    public static boolean isForwarder() {
        return isForwarder;
    }

    public static String getGroupInformation() {
        return System.getProperty("proactive.groupInformation",
            UniqueID.getCurrentVMID().toString() + "~-1");
    }
}
