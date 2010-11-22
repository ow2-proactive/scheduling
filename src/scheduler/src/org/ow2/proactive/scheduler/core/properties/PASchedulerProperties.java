/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * PASchedulerProperties contains all ProActive Scheduler properties.
 * 
 * You must use provided methods in order to get the Scheduler properties.
 * 
 * @author The ProActiveTeam
 * @since ProActive 4.0
 *
 * $Id$
 */
@PublicAPI
public enum PASchedulerProperties {

    /* ***************************************************************** */
    /* ********************** SCHEDULER PROPERTIES ********************* */
    /* ***************************************************************** */

    /** Scheduler default policy full name.  */
    SCHEDULER_DEFAULT_POLICY("pa.scheduler.policy", PropertyType.STRING),

    /** Forked java task default policy path */
    SCHEDULER_DEFAULT_FJT_SECURITY_POLICY("pa.scheduler.forkedtask.security.policy", PropertyType.STRING),

    /** Scheduler main loop time out */
    SCHEDULER_TIME_OUT("pa.scheduler.core.timeout", PropertyType.INTEGER),

    /** Scheduler main loop time out */
    SCHEDULER_START_TERMINATE_RATIO("pa.scheduler.core.stratio", PropertyType.INTEGER),

    /** Scheduler node ping frequency in s. */
    SCHEDULER_NODE_PING_FREQUENCY("pa.scheduler.core.nodepingfrequency", PropertyType.INTEGER),

    /** Name of the JMX MBean for the scheduler */
    SCHEDULER_JMX_CONNECTOR_NAME("pa.scheduler.core.jmx.connectorname", PropertyType.STRING),

    /** Port of the JMX service. Random if not set */
    SCHEDULER_JMX_PORT("pa.scheduler.core.jmx.port", PropertyType.INTEGER),

    /** Accounting refresh rate from the database in seconds */
    SCHEDULER_ACCOUNT_REFRESH_RATE("pa.scheduler.account.refreshrate", PropertyType.INTEGER),

    /** User session time (user is automatically disconnect after this time if no request is made to the scheduler)
    	negative number indicates that session is infinite (value specified in second) */
    SCHEDULER_USER_SESSION_TIME("pa.scheduler.core.usersessiontime", PropertyType.INTEGER),

    /** Timeout for the start task action. Time during which the scheduling could be waiting (in millis) */
    SCHEDULER_STARTTASK_TIMEOUT("pa.scheduler.core.starttask.timeout", PropertyType.INTEGER),

    /** Maximum number of threads used for the start task action. This property defines the number of blocking resources
     * until the scheduling loop will block as well.*/
    SCHEDULER_STARTTASK_THREADNUMBER("pa.scheduler.core.starttask.threadnumber", PropertyType.INTEGER),

    /** Maximum number of threads used to send events to clients. This property defines the number of clients
     * than can block at the same time. If this number is reached, every clients won't receive events until
     * a thread unlock. */
    SCHEDULER_LISTENERS_THREADNUMBER("pa.scheduler.core.listener.threadnumber", PropertyType.INTEGER),

    /* ***************************************************************** */
    /* ********************** AUTHENTICATION PROPERTIES **************** */
    /* ***************************************************************** */

    /** path to the Jaas configuration file which defines what modules are available for
     * internal authentication */
    SCHEDULER_AUTH_JAAS_PATH("pa.scheduler.auth.jaas.path", PropertyType.STRING),

    /** path to the private key file which is used to decrypt credentials passed to the jaas module */
    SCHEDULER_AUTH_PRIVKEY_PATH("pa.scheduler.auth.privkey.path", PropertyType.STRING),

    /** path to the public key file which is used to encrypt credentials for authentication */
    SCHEDULER_AUTH_PUBKEY_PATH("pa.scheduler.auth.pubkey.path", PropertyType.STRING),

    /** 
     * LDAP Authentication configuration file path, used to set LDAP configuration properties
     * If this file path is relative, the path is evaluated from the Scheduler dir (ie application's root dir)
     * with the variable defined below : pa.scheduler.home.
     * else, the path is absolute, so the path is directly interpreted
     */
    SCHEDULER_LDAP_CONFIG_FILE_PATH("pa.scheduler.ldap.config.path", PropertyType.STRING),

    SCHEDULER_LDAP2_CONFIG_FILE_PATH("pa.scheduler.ldap2.config.path", PropertyType.STRING),

    /** Login default file name */
    SCHEDULER_LOGIN_FILENAME("pa.scheduler.core.defaultloginfilename", PropertyType.STRING),

    /** Group default filename */
    SCHEDULER_GROUP_FILENAME("pa.scheduler.core.defaultgroupfilename", PropertyType.STRING),

    /** Property that define the method that have to be used for logging users to the Scheduler */
    SCHEDULER_LOGIN_METHOD("pa.scheduler.core.authentication.loginMethod", PropertyType.STRING),

    /* ***************************************************************** */
    /* ******************** CLASSLOADING PROPERTIES ******************** */
    /* ***************************************************************** */

    /** Cache classes definition in task class servers  */
    SCHEDULER_CLASSSERVER_USECACHE("pa.scheduler.classserver.usecache", PropertyType.BOOLEAN),

    /** Temporary directory for jobclasspathes  */
    SCHEDULER_CLASSSERVER_TMPDIR("pa.scheduler.classserver.tmpdir", PropertyType.STRING),

    /* ***************************************************************** */
    /* ************************* JOBS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Multiplicative factor for job id (taskId will be : this_factor*jobID+taskID) */
    JOB_FACTOR("pa.scheduler.job.factor", PropertyType.INTEGER),

    /** Delay to wait between getting a job result and removing the job concerned (0 = infinite) */
    SCHEDULER_REMOVED_JOB_DELAY("pa.scheduler.core.removejobdelay", PropertyType.INTEGER),

    /** Delay to wait between a job is terminated and removing the it from Scheduler (0 = infinite) */
    SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY("pa.scheduler.core.automaticremovejobdelay", PropertyType.INTEGER),

    /** Remove job in dataBase when removing it from scheduler. */
    JOB_REMOVE_FROM_DB("pa.scheduler.job.removeFromDataBase", PropertyType.BOOLEAN),

    /* ***************************************************************** */
    /* ************************ TASKS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Initial time to wait before the re-execution of a task. */
    REEXECUTION_INITIAL_WAITING_TIME("pa.scheduler.task.initialwaitingtime", PropertyType.INTEGER),

    /** Maximum number of execution for a task in case of failure (node down) */
    NUMBER_OF_EXECUTION_ON_FAILURE("pa.scheduler.task.numberofexecutiononfailure", PropertyType.INTEGER),

    /* ***************************************************************** */
    /* ********************** DATASPACES PROPERTIES ******************** */
    /* ***************************************************************** */

    /** Default INPUT space URL. Used to define INPUT space of each job that does not define an INPUT space. */
    DATASPACE_DEFAULTINPUTURL("pa.scheduler.dataspace.defaultinputurl", PropertyType.STRING),

    /** Default INPUT space path. Used to define the same INPUT space but with a local (faster) access (if possible). */
    DATASPACE_DEFAULTINPUTURL_LOCALPATH("pa.scheduler.dataspace.defaultinputurl.localpath",
            PropertyType.STRING),

    /** Host name from which the localpath is accessible */
    DATASPACE_DEFAULTINPUTURL_HOSTNAME("pa.scheduler.dataspace.defaultinputurl.hostname", PropertyType.STRING),

    /** The same for the OUPUT */
    DATASPACE_DEFAULTOUTPUTURL("pa.scheduler.dataspace.defaultoutputurl", PropertyType.STRING),
    /** */
    DATASPACE_DEFAULTOUTPUTURL_LOCALPATH("pa.scheduler.dataspace.defaultoutputurl.localpath",
            PropertyType.STRING),
    /** */
    DATASPACE_DEFAULTOUTPUTURL_HOSTNAME("pa.scheduler.dataspace.defaultoutputurl.hostname",
            PropertyType.STRING),

    /** GlobalSpace URL : DataSpaces for all nodes */
    DATASPACE_GLOBAL_URL("pa.scheduler.dataspace.globalurl", PropertyType.STRING),

    /** GlobalSpace local shortcut */
    DATASPACE_GLOBAL_URL_LOCALPATH("pa.scheduler.dataspace.globalurl.localpath", PropertyType.STRING),

    /** GlobalSpace local shortcut hostname */
    DATASPACE_GLOBAL_URL_HOSTNAME("pa.scheduler.dataspace.globalurl.hostname", PropertyType.STRING),

    /* ***************************************************************** */
    /* ************************* LOGS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Log forwarder provider */
    LOGS_FORWARDING_PROVIDER("pa.scheduler.logs.provider", PropertyType.STRING),

    //    /** Log max size (in lines per tasks) */
    //    LOGS_MAX_SIZE("pa.scheduler.logs.maxsize", PropertyType.INTEGER),

    /* ***************************************************************** */
    /* ************************ OTHER PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Scheduler home directory */
    SCHEDULER_HOME("pa.scheduler.home", PropertyType.STRING),

    /* ***************************************************************** */
    /* ************************** RM PROPERTIES ************************ */
    /* ***************************************************************** */

    /** Path to the Scheduler credentials file for RM authentication */
    RESOURCE_MANAGER_CREDS("pa.scheduler.resourcemanager.authentication.credentials", PropertyType.STRING),

    /**
     * Use single or multiple connection to RM
     * (If true)  the scheduler user will do the requests to rm
     * (If false) each Scheduler users have their own connection to RM using same credentials
     */
    RESOURCE_MANAGER_SINGLE_CONNECTION("pa.scheduler.resourcemanager.authentication.single",
            PropertyType.BOOLEAN),

    /* ***************************************************************** */
    /* ********************** HIBERNATE PROPERTIES ********************* */
    /* ***************************************************************** */
    /** Hibernate configuration file (relative to home directory) */
    SCHEDULER_DB_HIBERNATE_CONFIG("pa.scheduler.db.hibernate.configuration", PropertyType.STRING),

    /**
     * Drop database before creating a new one
     * If this value is true, the database will be dropped and then re-created
     * If this value is false, database will be updated from the existing one.
     */
    SCHEDULER_DB_HIBERNATE_DROPDB("pa.scheduler.db.hibernate.dropdb", PropertyType.BOOLEAN);

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    public static final String PA_SCHEDULER_PROPERTIES_FILEPATH = "pa.scheduler.properties.filepath";
    /** Default properties file for the scheduler configuration */
    private static String DEFAULT_PROPERTIES_FILE = null;
    /** to know if the file has been loaded or not */
    private static boolean fileLoaded;
    /** memory entity of the properties file. */
    private static Properties prop = null;

    /** Key of the specific instance. */
    private String key;
    /** value of the specific instance. */
    private PropertyType type;

    /**
     * Create a new instance of PASchedulerProperties
     *
     * @param str the key of the instance.
     * @param type the real java type of this instance.
     */
    PASchedulerProperties(String str, PropertyType type) {
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
     * Finally, if the selected file is a relative path, the file will be relative to the SCHEDULER_HOME property.
     *
     * @param filename the file to load or null to use the default one or the one set in java property.
     */
    private static void init(String filename) {
        String propertiesPath;
        boolean jPropSet = false;
        if (filename == null) {
            if (System.getProperty(PA_SCHEDULER_PROPERTIES_FILEPATH) != null) {
                propertiesPath = System.getProperty(PA_SCHEDULER_PROPERTIES_FILEPATH);
                jPropSet = true;
            } else {
                propertiesPath = "config/scheduler/settings.ini";
            }
        } else {
            propertiesPath = filename;
        }
        if (!new File(propertiesPath).isAbsolute()) {
            propertiesPath = System.getProperty(SCHEDULER_HOME.key) + File.separator + propertiesPath;
        }
        DEFAULT_PROPERTIES_FILE = propertiesPath;
        fileLoaded = new File(propertiesPath).exists();
        if (jPropSet && !fileLoaded) {
            throw new RuntimeException("Scheduler properties file not found : '" + propertiesPath + "'");
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
            if (filename == null && fileLoaded == false) {
                return prop;
            }
            try {
                if (filename == null) {
                    filename = DEFAULT_PROPERTIES_FILE;
                }
                prop.load(new FileInputStream(filename));
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
            ptmp.load(new FileInputStream(filename));
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
     * It the path is absolute, then it is returned. If the path is relative, then the Scheduler_home directory is 
     * concatenated in front of the given string.
     *
     * @param userPath the path to check transform.
     * @return the absolute path of the given path.
     */
    public static String getAbsolutePath(String userPath) {
        if (new File(userPath).isAbsolute()) {
            return userPath;
        } else {
            return PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + File.separator + userPath;
        }
    }

    /**
     * Supported types for PASchedulerProperties
     */
    public enum PropertyType {
        STRING, BOOLEAN, INTEGER;
    }
}
