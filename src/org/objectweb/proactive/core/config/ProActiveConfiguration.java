/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import org.objectweb.proactive.core.config.xml.MasterFileHandler;

import java.util.HashMap;
import java.util.Iterator;


public class ProActiveConfiguration {
    //protected static Logger logger = Logger.getLogger(ProActiveConfiguration.class);
    protected HashMap loadedProperties;
    protected HashMap addedProperties;
    protected static ProActiveConfiguration singleton;
    protected static boolean isLoaded = false;

    private ProActiveConfiguration() {
        this.loadedProperties = new HashMap();
        this.addedProperties = new HashMap();
        //setDefaultProActiveHome();
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
            String filename = null;
            if (System.getProperty("proactive.configuration") != null) {
                filename = System.getProperty("proactive.configuration");
            } else {
                filename = ProActiveConfiguration.class.getResource(
                        "ProActiveConfiguration.xml").toString();
            }
            ProActiveConfiguration.load(filename);
            isLoaded = true;
        }
    }

    /**
     * Load the configuration given in filename
     * @param filename an XML file name
     */
    public static void load(String filename) {
        if (!isLoaded) {
            MasterFileHandler.createMasterFileHandler(filename,
                ProActiveConfiguration.getConfiguration());
            ProActiveConfiguration.getConfiguration().addProperties();
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
    public void addProperties() {
        //		we don't override existing value
        Iterator it = loadedProperties.keySet().iterator();
        String name = null;
        String value = null;
        while (it.hasNext()) {
            name = (String) it.next();
            value = (String) this.loadedProperties.get(name);
            setProperty(name, value);
        }
        loadDefaultProperties();
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
     * Sets mandatory properties if forgotten by users
     */
    private void loadDefaultProperties() {
        setProperty("proactive.communication.protocol", "rmi");
        setProperty("proactive.future.ac", "enable");
        setProperty("schema.validation", "disable");
        if (System.getProperty("log4j.configuration") == null) {
            loadLogger();
        }
    }

    /**
     *
     */
    private void loadLogger() {
        //if logger is not defined create default logger with level info that logs
        // on the console
        Logger logger = Logger.getLogger("org.objectweb.proactive");
        logger.setAdditivity(false);
        logger.setLevel(Level.INFO);
        logger.addAppender(new ConsoleAppender(new PatternLayout()));
    }

    private void setProperty(String name, String value) {
        if (System.getProperty(name) == null) {
            System.setProperty(name, value);
            this.addedProperties.put(name, value);
        }
    }
}
