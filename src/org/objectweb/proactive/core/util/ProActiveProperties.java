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
package org.objectweb.proactive.core.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;


public final class ProActiveProperties {
    protected static Logger logger = Logger.getLogger(ProActiveProperties.class.getName());
    public static final String PROACTIVE_DEFAULT_LOCATIONSERVER = "proactive.locationserver";

    /**
     * The rmi name of the location server
     */
    public static final String PROACTIVE_DEFAULT_LOCATIONSERVER_RMI = "proactive.locationserver.rmi";

    /**
     * default state (enable or disable) of the automatic continuation mechanism
     */
    public static final String PROACTIVE_DEFAULT_AC_STATE = "proactive.future.ac";

    /**
     * default state(enable or disable) of the schema validation mechanism for deployment descriptors
     */
    public static final String PROACTIVE_DEFAULT_SCHEMA_VALIDATION = "schema.validation";
    private static java.util.Properties defaultProperties;

    static {
        defaultProperties = new java.util.Properties();
        ProActiveProperties.loadDefaultProperties();
    }

    protected static void loadDefaultProperties() {
//        defaultProperties.setProperty(PROACTIVE_DEFAULT_SCHEMA_VALIDATION,
//            "enable");
        //ProActiveProperties.addPropertiesToSystem(defaultProperties);
    }

    /**
     *  Load the default properties
     */
    public static void load() {
        //ProActiveProperties.loadDefaultProperties();
    }

    /**
     * Load additional properties from a file
     * Erase any previously defined property with its new value from the file
     * @param filename
     */
    public static void load(String filename) {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties p = new Properties();
        try {
            p.load(fi);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        //System.se  setProperties(p);
        for (java.util.Enumeration e = p.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            System.setProperty(s, p.getProperty(s));
        }
    }

    /**
     * Add a set of properties to the system properties
     * Does not overide any existing one
     *
     */
    protected static void addPropertiesToSystem(java.util.Properties p) {
        for (java.util.Enumeration e = p.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
//System.out.println("XXXXXXXXXXXXXXXXXXX " + s);
//Thread.dumpStack();
            //we don't override existing value
            if (System.getProperty(s) == null) {
                System.setProperty(s, p.getProperty(s));
            }
        }
    }

    public static String getLocationServerClass() {
        return System.getProperties().getProperty(ProActiveProperties.PROACTIVE_DEFAULT_LOCATIONSERVER);
    }

    public static String getLocationServerRmi() {
        return System.getProperties().getProperty(ProActiveProperties.PROACTIVE_DEFAULT_LOCATIONSERVER_RMI);
    }

    public static String getACState() {
        return System.getProperties().getProperty(ProActiveProperties.PROACTIVE_DEFAULT_AC_STATE);
    }

    public static String getSchemaValidationState() {
        return System.getProperties().getProperty(ProActiveProperties.PROACTIVE_DEFAULT_SCHEMA_VALIDATION);
    }
}
