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
package org.ow2.proactive.scheduler.core.properties;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.core.properties.PACommonProperties;
import org.ow2.proactive.core.properties.PACommonPropertiesHelper;
import org.ow2.proactive.core.properties.PropertyType;
import org.ow2.proactive.utils.PAProperties;
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
public enum PASchedulerProperties implements PACommonProperties {

    /* ***************************************************************** */
    /* ********************** SCHEDULER PROPERTIES ********************* */
    /* ***************************************************************** */

    /** Scheduler default policy full name. */
    SCHEDULER_DEFAULT_POLICY("pa.scheduler.policy", PropertyType.STRING, "org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy"),

    /** Defines the maximum number of tasks to be scheduled in each scheduling loop. */
    SCHEDULER_POLICY_NBTASKPERLOOP("pa.scheduler.policy.nbtaskperloop", PropertyType.INTEGER, "10"),

    /** Scheduler main loop time out */
    SCHEDULER_TIME_OUT("pa.scheduler.core.timeout", PropertyType.INTEGER, "100"),

    /** Scheduler auto reconnection to the RM when the connection is down */
    SCHEDULER_RMCONNECTION_AUTO_CONNECT("pa.scheduler.core.rmconnection.autoconnect", PropertyType.BOOLEAN, "true"),

    /** Scheduler time span between two reconnection attempts  */
    SCHEDULER_RMCONNECTION_TIMESPAN("pa.scheduler.core.rmconnection.timespan", PropertyType.INTEGER, "10000"),

    /** Scheduler maximum number of reconnection attempts  */
    SCHEDULER_RMCONNECTION_ATTEMPTS("pa.scheduler.core.rmconnection.attempts", PropertyType.INTEGER, "360"),

    /** Scheduler node ping frequency in s. */
    SCHEDULER_NODE_PING_FREQUENCY("pa.scheduler.core.nodepingfrequency", PropertyType.INTEGER, "20"),

    /** Scheduler number of node ping attempts before restarting the task. This value corresponds to the number of
     * tolerated failed attempts to ping a node, before the scheduler decides to restart the task running on it */
    SCHEDULER_NODE_PING_ATTEMPTS("pa.scheduler.core.node.ping.attempts", PropertyType.INTEGER, "1"),

    /** Number of threads used to execute client requests  */
    SCHEDULER_CLIENT_POOL_NBTHREAD("pa.scheduler.core.clientpoolnbthreads", PropertyType.INTEGER, "5"),

    /** Number of threads used to execute internal scheduling operations */
    SCHEDULER_INTERNAL_POOL_NBTHREAD("pa.scheduler.core.internalpoolnbthreads", PropertyType.INTEGER, "5"),

    /** Number of threads used to ping tasks */
    SCHEDULER_TASK_PINGER_POOL_NBTHREAD("pa.scheduler.core.taskpingerpoolnbthreads", PropertyType.INTEGER, "10"),

    /** Number of threads used to handle scheduled operations other than housekeeping operations */
    SCHEDULER_SCHEDULED_POOL_NBTHREAD("pa.scheduler.core.scheduledpoolnbthreads", PropertyType.INTEGER, "2"),

    /** Number of threads used to handle scheduled operations related to housekeeping */
    SCHEDULER_HOUSEKEEPING_SCHEDULED_POOL_NBTHREAD("pa.scheduler.core.housekeeping.scheduledpoolnbthreads", PropertyType.INTEGER, "5"),

    /** Name of the JMX MBean for the scheduler */
    SCHEDULER_JMX_CONNECTOR_NAME("pa.scheduler.core.jmx.connectorname", PropertyType.STRING, "JMXSchedulerAgent"),

    /** Port of the JMX service. Random if not set */
    SCHEDULER_JMX_PORT("pa.scheduler.core.jmx.port", PropertyType.INTEGER, "5822"),

    /** RRD data base with statistic history */
    SCHEDULER_RRD_DATABASE_NAME("pa.scheduler.jmx.rrd.name", PropertyType.STRING, "data/scheduler_statistics.rrd"),

    /** RRD data base step in seconds */
    SCHEDULER_RRD_STEP("pa.scheduler.jmx.rrd.step", PropertyType.INTEGER, "4"),

    /** Accounting refresh rate from the database in seconds */
    SCHEDULER_ACCOUNT_REFRESH_RATE("pa.scheduler.account.refreshrate", PropertyType.INTEGER, "180"),

    /** User session time (user is automatically disconnect after this time if no request is made to the scheduler)
    	negative number indicates that session is infinite (value specified in second) */
    SCHEDULER_USER_SESSION_TIME("pa.scheduler.core.usersessiontime", PropertyType.INTEGER, "28800"),

    /** Timeout for the start task action. Time during which the scheduling could be waiting (in millis) */
    SCHEDULER_STARTTASK_TIMEOUT("pa.scheduler.core.starttask.timeout", PropertyType.INTEGER, "5000"),

    /** Maximum number of threads used for the start task action. This property defines the number of blocking resources
     * until the scheduling loop will block as well.*/
    SCHEDULER_STARTTASK_THREADNUMBER("pa.scheduler.core.starttask.threadnumber", PropertyType.INTEGER, "5"),

    /** Maximum number of threads used to send events to clients. This property defines the number of clients
     * than can block at the same time. If this number is reached, every clients won't receive events until
     * a thread unlock. */
    SCHEDULER_LISTENERS_THREADNUMBER("pa.scheduler.core.listener.threadnumber", PropertyType.INTEGER, "5"),

    /** List of the scripts paths to execute at scheduler start. Paths are separated by a ';'. */
    SCHEDULER_STARTSCRIPTS_PATHS("pa.scheduler.startscripts.paths", PropertyType.LIST),

    /* ***************************************************************** */
    /* ********************** AUTHENTICATION PROPERTIES **************** */
    /* ***************************************************************** */

    /** path to the Jaas configuration file which defines what modules are available for
     * internal authentication */
    SCHEDULER_AUTH_JAAS_PATH("pa.scheduler.auth.jaas.path", PropertyType.STRING, "config/authentication/jaas.config"),

    /** path to the private key file which is used to decrypt credentials passed to the jaas module */
    SCHEDULER_AUTH_PRIVKEY_PATH("pa.scheduler.auth.privkey.path", PropertyType.STRING, "config/authentication/keys/priv.key"),

    /** path to the public key file which is used to encrypt credentials for authentication */
    SCHEDULER_AUTH_PUBKEY_PATH("pa.scheduler.auth.pubkey.path", PropertyType.STRING, "config/authentication/keys/pub.key"),

    /** 
     * LDAP Authentication configuration file path, used to set LDAP configuration properties
     * If this file path is relative, the path is evaluated from the Scheduler dir (ie application's root dir)
     * with the variable defined below : pa.scheduler.home.
     * else, the path is absolute, so the path is directly interpreted
     */
    SCHEDULER_LDAP_CONFIG_FILE_PATH("pa.scheduler.ldap.config.path", PropertyType.STRING, "config/authentication/ldap.cfg"),

    /** Login default file name */
    SCHEDULER_LOGIN_FILENAME("pa.scheduler.core.defaultloginfilename", PropertyType.STRING, "config/authentication/login.cfg"),

    /** Group default filename */
    SCHEDULER_GROUP_FILENAME("pa.scheduler.core.defaultgroupfilename", PropertyType.STRING, "config/authentication/group.cfg"),

    /** Property that define the method that have to be used for logging users to the Scheduler */
    SCHEDULER_LOGIN_METHOD("pa.scheduler.core.authentication.loginMethod", PropertyType.STRING, "SchedulerFileLoginMethod"),

    /* ***************************************************************** */
    /* ************************* JOBS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Delay to wait between getting a job result and removing the job concerned (0 = infinite) */
    SCHEDULER_REMOVED_JOB_DELAY("pa.scheduler.core.removejobdelay", PropertyType.INTEGER, "0"),

    /** Delay to wait between a job is terminated and removing the it from Scheduler (0 = infinite) */
    SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY("pa.scheduler.core.automaticremovejobdelay", PropertyType.INTEGER, "0"),

    /** Cron expression to automatically remove finished jobs */
    SCHEDULER_AUTOMATIC_REMOVED_JOB_CRON_EXPR("pa.scheduler.core.automaticremovejobexpression", PropertyType.STRING, "*/10 * * * *"),

    /** Remove job in dataBase when removing it from scheduler. */
    JOB_REMOVE_FROM_DB("pa.scheduler.job.removeFromDataBase", PropertyType.BOOLEAN, "true"),

    /** File encoding used by the scheduler */
    FILE_ENCODING(PAProperties.KEY_PA_FILE_ENCODING, PropertyType.STRING, "UTF-8"),

    /* ***************************************************************** */
    /* ************************ TASKS PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Initial time to wait before the re-execution of a task. */
    REEXECUTION_INITIAL_WAITING_TIME("pa.scheduler.task.initialwaitingtime", PropertyType.INTEGER, "1000"),

    /** Maximum number of execution for a task in case of failure (node down) */
    NUMBER_OF_EXECUTION_ON_FAILURE("pa.scheduler.task.numberofexecutiononfailure", PropertyType.INTEGER, "2"),

    /** If true script tasks are ran in a forked JVM, if false they are ran in the node's JVM */
    TASK_FORK("pa.scheduler.task.fork", PropertyType.BOOLEAN, "true"),

    /**
     * If true tasks are always ran in RunAsMe mode (implies automatically fork), if false, the user can choose whether to run the task in runasme mode or not
     **/
    TASK_RUNASME("pa.scheduler.task.runasme", PropertyType.BOOLEAN, "false"),

    /** Number of tasks to fetch per page when pagination is used */
    TASKS_PAGE_SIZE("pa.scheduler.tasks.page.size", PropertyType.INTEGER, "100"),

    /**
     * If set to non-null value the scheduler can executes only forkenvironment and clean scripts from
     * this directory. All other scripts will be rejected.
     */
    EXECUTE_SCRIPT_AUTHORIZED_DIR("pa.scheduler.script.authorized.dir", PropertyType.STRING),

    /**
     * The pa.scheduler.script.authorized.dir is browsed every refreshperiod time to load authorized scripts.
     */
    EXECUTE_SCRIPT_AUTHORIZED_DIR_REFRESHPERIOD("pa.scheduler.script.authorized.dir.refreshperiod", PropertyType.INTEGER, "60000"),

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
    LOGS_FORWARDING_PROVIDER("pa.scheduler.logs.provider", PropertyType.STRING, "org.ow2.proactive.scheduler.common.util.logforwarder.providers.ProActiveBasedForwardingProvider"),

    // Put logs dedicated to jobs/tasks to this location 
    SCHEDULER_JOB_LOGS_LOCATION("pa.scheduler.job.logs.location", PropertyType.STRING, "logs/jobs/"),

    SCHEDULER_JOB_LOGS_MAX_SIZE("pa.scheduler.job.logs.max.size", PropertyType.STRING, "10000"),

    //    /** Log max size (in lines per tasks) */
    //    LOGS_MAX_SIZE("pa.scheduler.logs.maxsize", PropertyType.INTEGER),

    // Define the logging format pattern of tasks on the scheduler
    SCHEDULER_JOB_LOGS_PATTERN(
            "pa.scheduler.job.task.output.logs.pattern",
            PropertyType.STRING,
            "[%X{job.id}t%X{task.id}@%X{host};%d{HH:mm:ss}] %m %n"),

    /** Defines the cron expression for the db sizes polling */
    SCHEDULER_DB_SIZE_MONITORING_FREQ("pa.scheduler.db.size.monitoring.freq", PropertyType.STRING),

    /* ***************************************************************** */
    /* ************************ OTHER PROPERTIES *********************** */
    /* ***************************************************************** */

    /** Scheduler home directory */
    SCHEDULER_HOME("pa.scheduler.home", PropertyType.STRING),

    /**
     * Scheduler rest url directory
     */
    SCHEDULER_REST_URL("pa.scheduler.rest.url", PropertyType.STRING),

    /* ***************************************************************** */
    /* ************************** RM PROPERTIES ************************ */
    /* ***************************************************************** */

    /** Path to the Scheduler credentials file for RM authentication */
    RESOURCE_MANAGER_CREDS("pa.scheduler.resourcemanager.authentication.credentials", PropertyType.STRING, "config/authentication/scheduler.cred"),

    /**
     * Use single or multiple connection to RM
     * (If true)  the scheduler user will do the requests to rm
     * (If false) each Scheduler users have their own connection to RM using same credentials
     */
    RESOURCE_MANAGER_SINGLE_CONNECTION("pa.scheduler.resourcemanager.authentication.single", PropertyType.BOOLEAN, "true"),

    /** Set a timeout for initial connection to the RM connection (in ms) */
    RESOURCE_MANAGER_CONNECTION_TIMEOUT("pa.scheduler.resourcemanager.connection.timeout", PropertyType.INTEGER, "120000"),

    /* ***************************************************************** */
    /* ********************** HIBERNATE PROPERTIES ********************* */
    /* ***************************************************************** */
    /** Hibernate configuration file (relative to home directory) */
    SCHEDULER_DB_HIBERNATE_CONFIG("pa.scheduler.db.hibernate.configuration", PropertyType.STRING, "config/scheduler/database.properties"),

    /**
     * Drop database before creating a new one
     * If this value is true, the database will be dropped and then re-created
     * If this value is false, database will be updated from the existing one.
     */
    SCHEDULER_DB_HIBERNATE_DROPDB("pa.scheduler.db.hibernate.dropdb", PropertyType.BOOLEAN, "false"),

    /**
     * This property is used to limit number of finished jobs loaded from the database 
     * at scheduler startup. For example setting this property to '10d' means that
     * scheduler should load only finished jobs which were submitted during last
     * 10 days. In the period expression it is also possible to use symbols 'h' (hours) 
     * and 'm' (minutes).
     * If property isn't set then all finished jobs are loaded. 
     */
    SCHEDULER_DB_LOAD_JOB_PERIOD("pa.scheduler.db.load.job.period", PropertyType.STRING),

    SCHEDULER_DB_TRANSACTION_DAMPING_FACTOR("pa.scheduler.db.transactions.damping.factor", PropertyType.INTEGER, "2"),

    SCHEDULER_DB_TRANSACTION_MAXIMUM_RETRIES("pa.scheduler.db.transactions.maximum.retries", PropertyType.INTEGER, "5"),

    SCHEDULER_DB_TRANSACTION_SLEEP_DELAY("pa.scheduler.db.transactions.sleep.delay", PropertyType.INTEGER, "1000"),

    SCHEDULER_DB_RECOVERY_LOAD_JOBS_BATCH_SIZE(
            "pa.scheduler.db.recovery.load.jobs.batch_size",
            PropertyType.INTEGER,
            "100"),

    /* ***************************************************************** */
    /* ***************** EMAIL NOTIFICATION PROPERTIES ***************** */
    /* ***************************************************************** */

    EMAIL_NOTIFICATIONS_CONFIGURATION(
            "pa.scheduler.notification.email.configuration",
            PropertyType.STRING,
            "config/scheduler/emailnotification.properties"),

    EMAIL_NOTIFICATIONS_ENABLED("pa.scheduler.notifications.email.enabled", PropertyType.BOOLEAN, "false"),

    EMAIL_NOTIFICATIONS_SENDER_ADDRESS(
            "pa.scheduler.notifications.email.from",
            PropertyType.STRING,
            "example@username.com"),

    /* ***************************************************************** */
    /* ******************* PORTAL DISPLAY PROPERTIES ******************* */
    /* ***************************************************************** */

    SCHEDULER_PORTAL_CONFIGURATION(
            "pa.scheduler.portal.configuration",
            PropertyType.STRING,
            "config/portal/scheduler-portal-display.conf");

    /* ***************************************************************************** */
    /* ***************************************************************************** */

    public static final String PA_SCHEDULER_PROPERTIES_FILEPATH = "pa.scheduler.properties.filepath";

    public static final String PA_SCHEDULER_PROPERTIES_RELATIVE_FILEPATH = "config/scheduler/settings.ini";

    /** memory entity of the properties file. */
    private static PAPropertiesLazyLoader propertiesLoader;

    private static PACommonPropertiesHelper propertiesHelper;

    static {
        load();
    }

    /** Key of the specific instance. */
    private String key;

    /** value of the specific instance. */
    private PropertyType type;

    /** default value to use if the property is not defined **/
    private String defaultValue;

    PASchedulerProperties(String str, PropertyType type) {
        this(str, type, null);
    }

    /**
     * Create a new instance of PASchedulerProperties
     *
     * @param str the key of the instance.
     * @param type the real java type of this instance.
     * @param defaultValue value to use if the property is not defined
     */
    PASchedulerProperties(String str, PropertyType type, String defaultValue) {
        this.key = str;
        this.type = type;
        this.defaultValue = defaultValue;
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
        propertiesHelper.updateProperty(key, value);
    }

    /**
     * Return all properties as a HashMap.
     */
    public static Map<String, Object> getPropertiesAsHashMap() {
        return propertiesHelper.getPropertiesAsHashMap();
    }

    /**
     * Load the properties from the given file.
     * This method will clean every loaded properties before.
     *
     * @param filename the file containing the properties to be loaded.
     */
    public static void loadProperties(String filename) {
        propertiesLoader = new PAPropertiesLazyLoader(SCHEDULER_HOME.key,
                                                      PA_SCHEDULER_PROPERTIES_FILEPATH,
                                                      PA_SCHEDULER_PROPERTIES_RELATIVE_FILEPATH,
                                                      filename);
        propertiesHelper = new PACommonPropertiesHelper(propertiesLoader);
    }

    public static synchronized void load() {
        propertiesLoader = new PAPropertiesLazyLoader(SCHEDULER_HOME.key,
                                                      PA_SCHEDULER_PROPERTIES_FILEPATH,
                                                      PA_SCHEDULER_PROPERTIES_RELATIVE_FILEPATH);
        propertiesHelper = new PACommonPropertiesHelper(propertiesLoader);
    }

    /**
     * Override properties defined in the default configuration file,
     * by properties defined in another file.
     * Call this method implies the default properties to be loaded
     * @param filename path of file containing some properties to override
     */
    public static void updateProperties(String filename) {
        propertiesHelper.updateProperties(filename);
    }

    @Override
    public String getConfigurationFilePathPropertyName() {
        return PA_SCHEDULER_PROPERTIES_FILEPATH;
    }

    @Override
    public String getConfigurationDefaultRelativeFilePath() {
        return PA_SCHEDULER_PROPERTIES_RELATIVE_FILEPATH;
    }

    @Override
    public void loadPropertiesFromFile(String filename) {
        loadProperties(filename);
    }

    @Override
    public void reloadConfiguration() {
        load();
    }

    @Override
    public boolean isSet() {
        return propertiesHelper.isSet(key, defaultValue);
    }

    @Override
    public void unSet() {
        propertiesHelper.unSet(key);
    }

    @Override
    public String getCmdLine() {
        return propertiesHelper.getCmdLine(key);
    }

    @Override
    public long getValueAsLong() {
        return propertiesHelper.getValueAsLong(key, type, defaultValue);
    }

    @Override
    public int getValueAsInt() {
        return propertiesHelper.getValueAsInt(key, type, defaultValue);
    }

    @Override
    public String getValueAsString() {
        return propertiesHelper.getValueAsString(key, defaultValue);
    }

    @Override
    public List<String> getValueAsList(String separator) {
        return propertiesHelper.getValueAsList(key, type, separator, defaultValue);
    }

    /**
     * Returns the value of this property as a string.
     * If the property is not defined, then null is returned
     *
     * @return the value of this property.
     */
    public String getValueAsStringOrNull() {
        return propertiesHelper.getValueAsStringOrNull(key);
    }

    /**
     * Returns the value of this property as a boolean.
     * If value is not a boolean, an exception will be thrown.<br>
     * The behavior of this method is the same as the {@link java.lang.Boolean#parseBoolean(String s)}. 
     * 
     * @return the value of this property or false if the value is not defined and does not specify a default.
     */
    public boolean getValueAsBoolean() {
        return propertiesHelper.getValueAsBoolean(key, type, defaultValue);
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
     * Get the absolute path of the given path.
     * <p>
     * It the path is absolute, then it is returned. If the path is relative, then {@code SCHEDULER_HOME} directory is
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

}
