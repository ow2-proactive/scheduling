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
package org.ow2.proactive.scheduler.core.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.utils.PAPropertiesLazyLoader;


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

    /** Scheduler default policy full name. */
    SCHEDULER_DEFAULT_POLICY("pa.scheduler.policy", PropertyType.STRING),

    /** Defines the maximum number of tasks to be scheduled in each scheduling loop. */
    SCHEDULER_POLICY_NBTASKPERLOOP("pa.scheduler.policy.nbtaskperloop", PropertyType.INTEGER),

    /** Forked java task default policy path */
    SCHEDULER_DEFAULT_FJT_SECURITY_POLICY("pa.scheduler.forkedtask.security.policy", PropertyType.STRING),

    /** Log4J forked java task default file path */
    SCHEDULER_DEFAULT_FJT_LOG4J("pa.scheduler.forkedtask.log4j", PropertyType.STRING),

    /** ProActiveConfiguration forked java task default file path */
    SCHEDULER_DEFAULT_FJT_PAConfig("pa.scheduler.forkedtask.paconfig", PropertyType.STRING),

    /** Scheduler main loop time out */
    SCHEDULER_TIME_OUT("pa.scheduler.core.timeout", PropertyType.INTEGER),

    /** Scheduler auto reconnection to the RM when the connection is down */
    SCHEDULER_RMCONNECTION_AUTO_CONNECT("pa.scheduler.core.rmconnection.autoconnect", PropertyType.BOOLEAN),

    /** Scheduler time span between two reconnection attempts  */
    SCHEDULER_RMCONNECTION_TIMESPAN("pa.scheduler.core.rmconnection.timespan", PropertyType.INTEGER),

    /** Scheduler maximum number of reconnection attempts  */
    SCHEDULER_RMCONNECTION_ATTEMPTS("pa.scheduler.core.rmconnection.attempts", PropertyType.INTEGER),

    /** Scheduler node ping frequency in s. */
    SCHEDULER_NODE_PING_FREQUENCY("pa.scheduler.core.nodepingfrequency", PropertyType.INTEGER),

    /** Scheduler node ping number before restarting the task. */
    SCHEDULER_NODE_PING_ATTEMPTS("pa.scheduler.core.node.ping.attempts", PropertyType.INTEGER),

    /** Number of threads used to execute client requests  */
    SCHEDULER_CLIENT_POOL_NBTHREAD("pa.scheduler.core.clientpoolnbthreads", PropertyType.INTEGER),

    /** Number of threads used to execute internal scheduling operations */
    SCHEDULER_INTERNAL_POOL_NBTHREAD("pa.scheduler.core.internalpoolnbthreads", PropertyType.INTEGER),

    /** Name of the JMX MBean for the scheduler */
    SCHEDULER_JMX_CONNECTOR_NAME("pa.scheduler.core.jmx.connectorname", PropertyType.STRING),

    /** Port of the JMX service. Random if not set */
    SCHEDULER_JMX_PORT("pa.scheduler.core.jmx.port", PropertyType.INTEGER),

    /** RRD data base with statistic history */
    SCHEDULER_RRD_DATABASE_NAME("pa.scheduler.jmx.rrd.name", PropertyType.STRING),

    /** RRD data base step in seconds */
    SCHEDULER_RRD_STEP("pa.scheduler.jmx.rrd.step", PropertyType.INTEGER),

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

    /** If true script tasks are ran in a forked JVM, if false they are ran in the node's JVM */
    FORKED_SCRIPT_TASKS("pa.scheduler.task.scripttasks.fork", PropertyType.BOOLEAN),

    /* ***************************************************************** */
    /* ********************** DATASPACES PROPERTIES ******************** */
    /* ***************************************************************** */

    /** Default INPUT space URL. Used to define INPUT space of each job that does not define an INPUT space. */
    /** Several URLs can be specified (which should all point to the same physical space, in that case, urls must be separated by spaces. */
    DATASPACE_DEFAULTINPUT_URL("pa.scheduler.dataspace.defaultinput.url", PropertyType.LIST),

    /** Default INPUT space path. Used to define the same INPUT space but with a local (faster) access (if possible). */
    DATASPACE_DEFAULTINPUT_LOCALPATH("pa.scheduler.dataspace.defaultinput.localpath", PropertyType.STRING),

    /** Host name from which the localpath is accessible */
    DATASPACE_DEFAULTINPUT_HOSTNAME("pa.scheduler.dataspace.defaultinput.hostname", PropertyType.STRING),

    /** The same for the OUPUT */
    /** Several URLs can be specified, separated by space */
    DATASPACE_DEFAULTOUTPUT_URL("pa.scheduler.dataspace.defaultoutput.url", PropertyType.LIST),
    /** */
    DATASPACE_DEFAULTOUTPUT_LOCALPATH("pa.scheduler.dataspace.defaultoutput.localpath", PropertyType.STRING),
    /** */
    DATASPACE_DEFAULTOUTPUT_HOSTNAME("pa.scheduler.dataspace.defaultoutput.hostname", PropertyType.STRING),

    /** Default Global space URL for all user. This space is supposed public to all users.
     * Used to define a Global space of each job that does not define a Global space
     **/
    /** Several URLs can be specified, separated by space */
    DATASPACE_DEFAULTGLOBAL_URL("pa.scheduler.dataspace.defaultglobal.url", PropertyType.STRING),
    /** */
    DATASPACE_DEFAULTGLOBAL_LOCALPATH("pa.scheduler.dataspace.defaultglobal.localpath", PropertyType.STRING),
    /** */
    DATASPACE_DEFAULTGLOBAL_HOSTNAME("pa.scheduler.dataspace.defaultglobal.hostname", PropertyType.STRING),

    /** Default User Space URL
     * Used to define a User space of each job which doesn't define one, the actual User Space will be inferred
     * from this space by appending the username in the path */

    /** Several URLs can be specified, separated by space */
    DATASPACE_DEFAULTUSER_URL("pa.scheduler.dataspace.defaultuser.url", PropertyType.STRING),
    /** */
    DATASPACE_DEFAULTUSER_LOCALPATH("pa.scheduler.dataspace.defaultuser.localpath", PropertyType.STRING),
    /** */
    DATASPACE_DEFAULTUSER_HOSTNAME("pa.scheduler.dataspace.defaultuser.hostname", PropertyType.STRING),

    /* ***************************************************************** */
    /* ************************* LOGS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Log forwarder provider */
    LOGS_FORWARDING_PROVIDER("pa.scheduler.logs.provider", PropertyType.STRING),

    // Put logs dedicated to jobs/tasks to this location 
    SCHEDULER_JOB_LOGS_LOCATION("pa.scheduler.job.logs.location", PropertyType.STRING),

    SCHEDULER_JOB_LOGS_MAX_SIZE("pa.scheduler.job.logs.max.size", PropertyType.STRING),

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

    /** Set a timeout for initial connection to the RM connection (in ms) */
    RESOURCE_MANAGER_CONNECTION_TIMEOUT("pa.scheduler.resourcemanager.connection.timeout",
            PropertyType.INTEGER),

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
    SCHEDULER_DB_HIBERNATE_DROPDB("pa.scheduler.db.hibernate.dropdb", PropertyType.BOOLEAN),

    /**
     * This property is used to limit number of finished jobs loaded from the database 
     * at scheduler startup. For example setting this property to '10d' means that
     * scheduler should load only finished jobs which were submitted during last
     * 10 days. In the period expression it is also possible to use symbols 'h' (hours) 
     * and 'm' (minutes).
     * If property isn't set then all finished jobs are loaded. 
     */
    SCHEDULER_DB_LOAD_JOB_PERIOD("pa.scheduler.db.load.job.period", PropertyType.STRING),

    EMAIL_NOTIFICATIONS_ENABLED("pa.scheduler.notifications.email.enabled", PropertyType.BOOLEAN),

    EMAIL_NOTIFICATIONS_SENDER_ADDRESS("pa.scheduler.notifications.email.from", PropertyType.STRING);

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    public static final String PA_SCHEDULER_PROPERTIES_FILEPATH = "pa.scheduler.properties.filepath";
    public static final String PA_SCHEDULER_PROPERTIES_RELATIVE_FILEPATH = "config/scheduler/settings.ini";

    /** memory entity of the properties file. */
    private static PAPropertiesLazyLoader propertiesLoader = new PAPropertiesLazyLoader(SCHEDULER_HOME.key,
        PA_SCHEDULER_PROPERTIES_FILEPATH, PA_SCHEDULER_PROPERTIES_RELATIVE_FILEPATH);

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
        propertiesLoader.getProperties().setProperty(key, value);
    }

    /**
     * Load the properties from the given file.
     * This method will clean every loaded properties before.
     *
     * @param filename the file containing the properties to be loaded.
     */
    public static void loadProperties(String filename) {
        propertiesLoader = new PAPropertiesLazyLoader(SCHEDULER_HOME.key, PA_SCHEDULER_PROPERTIES_FILEPATH,
            PA_SCHEDULER_PROPERTIES_RELATIVE_FILEPATH, filename);
    }

    /**
     * Override properties defined in the default configuration file,
     * by properties defined in another file.
     * Call this method implies the default properties to be loaded
     * @param filename path of file containing some properties to override
     */
    public static void updateProperties(String filename) {
        Properties prop = propertiesLoader.getProperties();
        Properties ptmp = new Properties();
        try {
            FileInputStream stream = new FileInputStream(filename);
            ptmp.load(stream);
            stream.close();
            for (Object o : ptmp.keySet()) {
                prop.setProperty((String) o, (String) ptmp.get(o));
            }
            PAPropertiesLazyLoader.updateWithSystemProperties(prop);
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
        return propertiesLoader.getProperties().containsKey(key);
    }

    /**
     * unsets this property
     *
     */
    public void unSet() {
        propertiesLoader.getProperties().remove(key);
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
        if (propertiesLoader.getProperties().containsKey(key)) {
            String valueS = getValueAsString();
            try {
                return Integer.parseInt(valueS);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(key +
                    " is not an integer property. getValueAsInt cannot be called on this property");
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
        Properties prop = propertiesLoader.getProperties();
        if (prop.containsKey(key)) {
            return prop.getProperty(key);
        } else {
            return "";
        }
    }

    /**
     * Returns the value of this property as a List of strings.
     *
     * @param separator the separator to use
     *
     * @return the list of values of this property.
     */
    public List<String> getValueAsList(String separator) {
        Properties prop = propertiesLoader.getProperties();
        ArrayList<String> valueList = new ArrayList<String>();
        if (prop.containsKey(key)) {
            String value = prop.getProperty(key);
            for (String val : value.split(Pattern.quote(separator))) {
                val = val.trim();
                if (val.length() > 0) {
                    valueList.add(val);
                }
            }
        }
        return valueList;
    }

    /**
     * Returns the value of this property as a string.
     * If the property is not defined, then null is returned
     *
     * @return the value of this property.
     */
    public String getValueAsStringOrNull() {
        Properties prop = propertiesLoader.getProperties();
        if (prop.containsKey(key)) {
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
        if (propertiesLoader.getProperties().containsKey(key)) {
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
        STRING, BOOLEAN, INTEGER, LIST
    }
}
