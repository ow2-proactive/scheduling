/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.core.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * PAResourceManagerProperties contains all ProActive Resource Manager properties.
 * 
 * You must use provide methods in order to get this RM properties.
 * 
 * @author The ProActiveTeam
 * @since ProActive 4.0
 *
 * $Id$
 */
@PublicAPI
public enum PAResourceManagerProperties {

    /* ***************************************************************** */
    /* ********************** RMCORE PROPERTIES ********************* */
    /* ***************************************************************** */

    /** Name of the ProActive Node containing RM's active objects */
    RM_NODE_NAME("pa.rm.node.name", PropertyType.STRING),

    /** Ping frequency in ms used by node source for keeping a watch on handled nodes */
    RM_NODE_SOURCE_PING_FREQUENCY("pa.rm.node.source.ping.frequency", PropertyType.INTEGER),

    /** Ping frequency used by resource manager to ping connected clients (in ms) */
    RM_CLIENT_PING_FREQUENCY("pa.rm.client.ping.frequency", PropertyType.INTEGER),

    /** Timeout in ms for selection script execution */
    RM_SELECT_SCRIPT_TIMEOUT("pa.rm.select.script.timeout", PropertyType.INTEGER),

    /** The number of selection script digests stored in the cache to predict the execution results */
    RM_SELECT_SCRIPT_CACHE_SIZE("pa.rm.select.script.cache", PropertyType.INTEGER),

    /**
     * The time period when a node has the same dynamic characteristics (in ms).
     * Default is 5 mins, which means that if any dynamic selection scripts returns
     * false on a node it won't be executed there at least for this time.
     */
    RM_SELECT_SCRIPT_NODE_DYNAMICITY("pa.rm.select.node.dynamicity", PropertyType.INTEGER),

    /** Timeout in ms for remote script execution using
     *  {@link SelectionManager#executeScript(org.ow2.proactive.scripting.Script, java.util.HashMap)}*/
    RM_EXECUTE_SCRIPT_TIMEOUT("pa.rm.execute.script.timeout", PropertyType.INTEGER),

    /** 
     * If set to non-null value the resource manager executes only scripts from
     * this directory. All other selection scripts will be rejected.
     */
    RM_EXECUTE_SCRIPT_AUTHORIZED_DIR("pa.rm.select.script.authorized.dir", PropertyType.STRING),

    /** Timeout in ms for node lookup */
    RM_NODELOOKUP_TIMEOUT("pa.rm.nodelookup.timeout", PropertyType.INTEGER),

    /** GCM application template file path, used to perform GCM deployments */
    @Deprecated
    RM_GCM_TEMPLATE_APPLICATION_FILE("pa.rm.gcm.template.application.file", PropertyType.STRING),

    /**
     * Name of a string contained in in the GCM Application (GCMA) XML file, that must mandatory appear
     * as a place of a GCM deployment file.
     */
    @Deprecated
    RM_GCMD_PATH_PROPERTY_NAME("pa.rm.gcmd.path.property.name", PropertyType.STRING),

    /** Path to the Amazon EC2 account credentials properties file,
     *  mandatory when using the EC2 Infrastructure */
    RM_EC2_PATH_PROPERTY_NAME("pa.rm.ec2.properties", PropertyType.STRING),

    /** Resource Manager home directory */
    RM_HOME("pa.rm.home", PropertyType.STRING),

    /** Path to the Jaas configuration file which defines what modules are available for
     * internal authentication */
    RM_AUTH_JAAS_PATH("pa.rm.auth.jaas.path", PropertyType.STRING),

    /** Path to the private key file which is used to decrypt credentials passed to the jaas module */
    RM_AUTH_PRIVKEY_PATH("pa.rm.auth.privkey.path", PropertyType.STRING),

    /** Path to the public key file which is used to encrypt credentials for authentication */
    RM_AUTH_PUBKEY_PATH("pa.rm.auth.pubkey.path", PropertyType.STRING),

    /** Resource Manager authentication method */
    RM_LOGIN_METHOD("pa.rm.authentication.loginMethod", PropertyType.STRING),

    /** Resource Manager ldap configuration file */
    RM_LDAP_CONFIG("pa.rm.ldap.config.path", PropertyType.STRING),

    /** Resource Manager ldap2 configuration file */
    RM_LDAP2_CONFIG("pa.rm.ldap2.config.path", PropertyType.STRING),

    /** Resource Manager login file name */
    RM_LOGIN_FILE("pa.rm.defaultloginfilename", PropertyType.STRING),

    /** Resource Manager group file name */
    RM_GROUP_FILE("pa.rm.defaultgroupfilename", PropertyType.STRING),

    /** Name of the JMX MBean for the RM */
    RM_JMX_CONNECTOR_NAME("pa.rm.jmx.connectorname", PropertyType.STRING),

    /** Port of the JMX service. Random if not set */
    RM_JMX_PORT("pa.rm.jmx.port", PropertyType.INTEGER),

    /** RRD data base with statistic history */
    RM_RRD_DATABASE_NAME("pa.rm.jmx.rrd.name", PropertyType.STRING),

    /** RRD data base step in seconds */
    RM_RRD_STEP("pa.rm.jmx.rrd.step", PropertyType.INTEGER),

    /** Resource Manager node source infrastructures file*/
    RM_NODESOURCE_INFRASTRUCTURE_FILE("pa.rm.nodesource.infrastructures", PropertyType.STRING),

    /** Resource Manager node source policies file*/
    RM_NODESOURCE_POLICY_FILE("pa.rm.nodesource.policies", PropertyType.STRING),

    /** Max number of threads in node source for parallel task execution */
    RM_NODESOURCE_MAX_THREAD_NUMBER("pa.rm.nodesource.maxthreadnumber", PropertyType.INTEGER),

    /** The full class name of the policy selected nodes */
    RM_SELECTION_POLICY("pa.rm.selection.policy", PropertyType.STRING),

    /** Max number of threads in node source for parallel task execution */
    RM_SELECTION_MAX_THREAD_NUMBER("pa.rm.selection.maxthreadnumber", PropertyType.INTEGER),

    /** Max number of threads in node source for parallel task execution */
    RM_MONITORING_MAX_THREAD_NUMBER("pa.rm.monitoring.maxthreadnumber", PropertyType.INTEGER),

    /** Max number of threads in the core for cleaning nodes after computations */
    RM_CLEANING_MAX_THREAD_NUMBER("pa.rm.cleaning.maxthreadnumber", PropertyType.INTEGER),

    /** Path to the Resource Manager credentials for adding local nodes */
    RM_CREDS("pa.rm.credentials", PropertyType.STRING),

    /** Resource Manager hibernate configuration file*/
    RM_DB_HIBERNATE_CONFIG("pa.rm.db.hibernate.configuration", PropertyType.STRING),

    /**
     * Drop database before creating a new one
     * If this value is true, the database will be dropped and then re-created
     * If this value is false, database will be updated from the existing one.
     */
    RM_DB_HIBERNATE_DROPDB("pa.rm.db.hibernate.dropdb", PropertyType.BOOLEAN),

    /**
     * Drop only node sources from the database.
     */
    RM_DB_HIBERNATE_DROPDB_NODESOURCES("pa.rm.db.hibernate.dropdb.nodesources", PropertyType.BOOLEAN),

    /**
     * The period of sending "alive" event to resource manager's listeners.
     */
    RM_ALIVE_EVENT_FREQUENCY("pa.rm.aliveevent.frequency", PropertyType.INTEGER),

    /** Accounting refresh rate from the database in seconds */
    RM_ACCOUNT_REFRESH_RATE("pa.rm.account.refreshrate", PropertyType.INTEGER),

    /** Topology feature enable option */
    RM_TOPOLOGY_ENABLED("pa.rm.topology.enabled", PropertyType.BOOLEAN),

    RM_TOPOLOGY_PINGER("pa.rm.topology.pinger.class", PropertyType.STRING),

    /** Resource Manager selection process logs*/
    RM_SELECTION_LOGS_LOCATION("pa.rm.logs.selection.location", PropertyType.STRING);

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    public static final String PA_RM_PROPERTIES_FILEPATH = "pa.rm.properties.filepath";
    /** Default properties file for the RM configuration */
    private static String DEFAULT_PROPERTIES_FILE = null;
    /** to know if the file has been loaded or not */
    private static boolean fileLoaded = false;
    /** memory entity of the properties file. */
    private static Properties prop = null;

    /** Key of the specific instance. */
    private String key;
    /** value of the specific instance. */
    private PropertyType type;

    /**
     * Create a new instance of PAResourceManagerProperties
     *
     * @param str the key of the instance.
     * @param type the real java type of this instance.
     */
    PAResourceManagerProperties(String str, PropertyType type) {
        this.key = str;
        this.type = type;
    }

    /**
     * Get the key.
     *
     * @return the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the value of this property to the given one.
     *
     * @param value the new value to set.
     */
    public void updateProperty(String value) {
        getProperties(null);
        prop.setProperty(key, value);
    }

    /**
     * Set the user java properties to the PASchedulerProperties.<br/>
     * User properties are defined using the -Dname=value in the java command.
     */
    private static void setUserJavaProperties() {
        if (prop != null) {
            for (Object o : prop.keySet()) {
                String s = System.getProperty((String) o);
                if (s != null) {
                    prop.setProperty((String) o, s);
                }
            }
        }
    }

    /**
     * Initialize the file to be loaded by this properties.
     * It first check the filename argument :<br>
     * - if null  : default config file is used (first check if java property file exist)<br>
     * - if exist : use the filename argument to read configuration.<br>
     *
     * Finally, if the selected file is a relative path, the file will be relative to the RM_HOME property.
     *
     * @param filename the file to load or null to use the default one or the one set in java property.
     */
    private static void init(String filename) {
        String propertiesPath;
        boolean jPropSet = false;
        if (filename == null) {
            if (System.getProperty(PA_RM_PROPERTIES_FILEPATH) != null) {
                propertiesPath = System.getProperty(PA_RM_PROPERTIES_FILEPATH);
                jPropSet = true;
            } else {
                propertiesPath = "config/rm/settings.ini";
            }
        } else {
            propertiesPath = filename;
        }
        if (!new File(propertiesPath).isAbsolute()) {
            propertiesPath = System.getProperty(RM_HOME.key) + File.separator + propertiesPath;
        }
        DEFAULT_PROPERTIES_FILE = propertiesPath;
        fileLoaded = new File(propertiesPath).exists();
        if (jPropSet && !fileLoaded) {
            throw new RuntimeException("RM properties file not found : '" + propertiesPath + "'");
        }
    }

    /**
     * Get the properties map or load it if needed.
     * 
     * @param filename the new file to be loaded.
     * @return the properties map corresponding to the default property file.
     */
    private static Properties getProperties(String filename) {
        if (prop == null) {
            prop = new Properties();
            init(filename);
            if (filename == null && !fileLoaded) {
                return prop;
            }
            try {
                if (filename == null) {
                    filename = DEFAULT_PROPERTIES_FILE;
                }
                FileInputStream stream = new FileInputStream(filename);
                prop.load(stream);
                stream.close();
                setUserJavaProperties();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return prop;
    }

    /**
     * Load the properties from the given file.
     * This method will clean every loaded properties before.
     *
     * @param filename the file containing the properties to be loaded.
     */
    public static void loadProperties(String filename) {
        DEFAULT_PROPERTIES_FILE = null;
        fileLoaded = false;
        prop = null;
        getProperties(filename);
    }

    /**
     * Override properties defined in the default configuration file,
     * by properties defined in another file.
     * Call this method implies the default properties to be loaded
     * @param filename path of file containing some properties to override
     */
    public static void updateProperties(String filename) {
        getProperties(null);
        Properties ptmp = new Properties();
        try {
            FileInputStream stream = new FileInputStream(filename);
            ptmp.load(stream);
            stream.close();
            for (Object o : ptmp.keySet()) {
                prop.setProperty((String) o, (String) ptmp.get(o));
            }
            setUserJavaProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return true if this property is set, false otherwise.
     *
     * @return true if this property is set, false otherwise.
     */
    public boolean isSet() {
        getProperties(null);
        if (fileLoaded) {
            return prop.containsKey(key);
        } else {
            return false;
        }
    }

    /**
     * Returns the string to be passed on the command line
     *
     * The property surrounded by '-D' and '='
     *
     * @return the string to be passed on the command line
     */
    public String getCmdLine() {
        return "-D" + key + '=';
    }

    /**
     * Returns the value of this property as an integer.
     * If value is not an integer, an exception will be thrown.
     * 
     * @return the value of this property.
     */
    public int getValueAsInt() {
        getProperties(null);
        if (fileLoaded) {
            String valueS = getValueAsString();
            try {
                int value = Integer.parseInt(valueS);
                return value;
            } catch (NumberFormatException e) {
                RuntimeException re = new IllegalArgumentException(key +
                    " is not an integer property. getValueAsInt cannot be called on this property");
                throw re;
            }
        } else {
            return 0;
        }
    }

    /**
     * Returns the value of this property as a string.
     * 
     * @return the value of this property.
     */
    public String getValueAsString() {
        Properties prop = getProperties(null);
        if (fileLoaded) {
            return prop.getProperty(key);
        } else {
            return "";
        }
    }

    /**
     * Returns the value of this property as a string.
     * If the property is not defined, then null is returned
     *
     * @return the value of this property.
     */
    public String getValueAsStringOrNull() {
        Properties prop = getProperties(null);
        if (fileLoaded) {
            String ret = prop.getProperty(key);
            if ("".equals(ret)) {
                return null;
            }
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Returns the value of this property as a boolean.
     * If value is not a boolean, an exception will be thrown.<br>
     * The behavior of this method is the same as the {@link java.lang.Boolean#parseBoolean(String s)}. 
     * 
     * @return the value of this property.
     */
    public boolean getValueAsBoolean() {
        getProperties(null);
        if (fileLoaded) {
            return Boolean.parseBoolean(getValueAsString());
        } else {
            return false;
        }
    }

    /**
     * Return the type of the given properties.
     *
     * @return the type of the given properties.
     */
    public PropertyType getType() {
        return type;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return getValueAsString();
    }

    /**
     * Get the absolute path of the given path.<br/>
     * It the path is absolute, then it is returned. If the path is relative, then the RM_home directory is 
     * concatenated in front of the given string.
     *
     * @param userPath the path to check transform.
     * @return the absolute path of the given path.
     */
    public static String getAbsolutePath(String userPath) {
        if (new File(userPath).isAbsolute()) {
            return userPath;
        } else {
            return PAResourceManagerProperties.RM_HOME.getValueAsString() + File.separator + userPath;
        }
    }

    /**
     * Supported types for PAResourceManagerProperties
     */
    public enum PropertyType {
        STRING, BOOLEAN, INTEGER;
    }

}
