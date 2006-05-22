/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.objectweb.proactive.core.config.xml.MasterFileHandler;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Configuration parameters may be overriden according to the following priorities:</br>
 * > meaning "configuration parameters defined on the left override those defined on the right", we have: </br>
 * JVM > custom config file > default config file
 *
 */
public class ProActiveConfiguration {
    protected HashMap loadedProperties;
    protected HashMap addedProperties;
    protected static ProActiveConfiguration singleton;
    protected static boolean isLoaded = false;
    protected static boolean defaultConfigAlreadyLoaded = false;
    protected static List jvmDefinedProperties = new ArrayList();

    private ProActiveConfiguration() {
        this.loadedProperties = new HashMap();
        this.addedProperties = new HashMap();
    }

    protected static synchronized void createConfiguration() {
        if (ProActiveConfiguration.singleton == null) {
            ProActiveConfiguration.singleton = new ProActiveConfiguration();
        }
    }

    /**
     * Load the default configuration
     * The default file is located in the same directory as the ProActiceConfiguration
     * class with the name proacticeConfiguration
     * It is obtained using Class.geressource
     * If the property proactive.configuration is set then its value is used
     * as the configuration file
     */
    public static void load() {
        if (!isLoaded) {
            loadDefaultConfig();
            isLoaded = true;
        }
    }

    private static void loadDefaultConfig() {
        String filename = null;
        filename = ProActiveConfiguration.class.getResource(
                "ProActiveConfiguration.xml").toString();
        MasterFileHandler.createMasterFileHandler(filename,
            ProActiveConfiguration.getConfiguration());
        ProActiveConfiguration.getConfiguration().loadProperties();
        if (System.getProperty("log4j.configuration") == null) {
            loadDefaultLogger();
        }
        defaultConfigAlreadyLoaded = true;
    }

    /**
     * Load the configuration given in filename
     * @param filename an XML file name
     */
    public static void load(String filename) {
        if (!isLoaded) {
            if (!defaultConfigAlreadyLoaded) {
                loadDefaultConfig();
            }
            MasterFileHandler.createMasterFileHandler(filename,
                ProActiveConfiguration.getConfiguration());
            ProActiveConfiguration.getConfiguration().loadProperties();
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
     * Called by the parser when a property has been found
     * @param name  name of the property
     * @param value value of the property
     */
    public void propertyFound(String name, String value) {
        this.loadedProperties.put(name, value);
    }

    /**
     * Add the loaded properties to the system
     */
    public void loadProperties() {
        Iterator it = loadedProperties.keySet().iterator();
        String name = null;
        String value = null;
        while (it.hasNext()) {
            name = (String) it.next();
            value = (String) this.loadedProperties.get(name);
            if (!defaultConfigAlreadyLoaded) {
                // JVM parameters cannot be overriden
                if (System.getProperty(name) == null) {
                    System.setProperty(name, value);
                } else {
                    jvmDefinedProperties.add(name);
                }
            } else {
                if (!jvmDefinedProperties.contains(name)) {
                    // override default properties, except JVM defined properties
                    System.setProperty(name, value);
                }
            }
        }
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

    //To be used for the launcher 
    //    /**
    //     * Sets the value of proactive.home if not already set
    //     */
    //    private void setDefaultProActiveHome() {
    //        File file = null;
    //        if (System.getProperty("proactive.home") == null) {
    //            String location = ProActiveConfiguration.class.getResource(
    //                    "ProActiveConfiguration.class").getPath();
    //            try {
    //                file = new File(location, "/../../../../../../../../ProActive/").getCanonicalFile();
    //                String proactivehome = file.getCanonicalPath();
    //                System.setProperty("proactive.home", proactivehome);
    //            } catch (IOException e) {
    //                System.err.println(
    //                    "WARNING: Unable to set proactive.home property. ProActive dir cannot be found! ");
    //            }
    //        }
    //    }

    /**
     *
     */
    private static void loadDefaultLogger() {
        //if logger is not defined create default logger with level info that logs
        // on the console
        Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
        logger.setAdditivity(false);
        logger.setLevel(Level.INFO);
        logger.addAppender(new ConsoleAppender(new PatternLayout()));
    }
}
