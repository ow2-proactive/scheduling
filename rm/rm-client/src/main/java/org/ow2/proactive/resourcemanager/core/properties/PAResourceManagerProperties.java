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
package org.ow2.proactive.resourcemanager.core.properties;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.core.properties.PACommonProperties;
import org.ow2.proactive.core.properties.PACommonPropertiesHelper;
import org.ow2.proactive.core.properties.PropertyType;
import org.ow2.proactive.utils.PAPropertiesLazyLoader;


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
public enum PAResourceManagerProperties implements PACommonProperties {

    /* ***************************************************************** */
    /* ********************** RMCORE PROPERTIES ********************* */
    /* ***************************************************************** */

    /** Name of the ProActive Node containing RM's active objects */
    RM_NODE_NAME("pa.rm.node.name", PropertyType.STRING, "RM_NODE"),

    /** Ping frequency in ms used by node source for keeping a watch on handled nodes */
    RM_NODE_SOURCE_PING_FREQUENCY("pa.rm.node.source.ping.frequency", PropertyType.INTEGER, "45000"),

    /** Ping frequency used by resource manager to ping connected clients (in ms) */
    RM_CLIENT_PING_FREQUENCY("pa.rm.client.ping.frequency", PropertyType.INTEGER, "45000"),

    /** Timeout in ms for selection script execution */
    RM_SELECT_SCRIPT_TIMEOUT("pa.rm.select.script.timeout", PropertyType.INTEGER, "60000"),

    /** The number of selection script digests stored in the cache to predict the execution results */
    RM_SELECT_SCRIPT_CACHE_SIZE("pa.rm.select.script.cache", PropertyType.INTEGER, "10000"),

    /**
     * The time period when a node has the same dynamic characteristics (in ms).
     * Default is 5 mins, which means that if any dynamic selection scripts returns
     * false on a node it won't be executed there at least for this time.
     */
    RM_SELECT_SCRIPT_NODE_DYNAMICITY("pa.rm.select.node.dynamicity", PropertyType.INTEGER, "300000"),

    /**
     * Timeout in ms for remote script execution using
     * {@code SelectionManager#executeScript(org.ow2.proactive.scripting.Script, java.util.HashMap)}
     */
    RM_EXECUTE_SCRIPT_TIMEOUT("pa.rm.execute.script.timeout", PropertyType.INTEGER, "180000"),

    /**
     * If set to non-null value the resource manager executes only selection scripts from
     * this directory. All other selection scripts will be rejected.
     */
    RM_EXECUTE_SCRIPT_AUTHORIZED_DIR("pa.rm.select.script.authorized.dir", PropertyType.STRING),

    /**
     * The pa.rm.select.script.authorized.dir is browsed every refreshperiod time to load authorized scripts.
     */
    RM_EXECUTE_SCRIPT_AUTHORIZED_DIR_REFRESHPERIOD("pa.rm.select.script.authorized.dir.refreshperiod", PropertyType.INTEGER, "60000"),

    /** Timeout in ms for node lookup */
    RM_NODELOOKUP_TIMEOUT("pa.rm.nodelookup.timeout", PropertyType.INTEGER, "60000"),

    /** GCM application template file path, used to perform GCM deployments */
    @Deprecated
    RM_GCM_TEMPLATE_APPLICATION_FILE("pa.rm.gcm.template.application.file", PropertyType.STRING, "config/rm/deployment/GCMNodeSourceApplication.xml"),

    /**
     * Name of a string contained in in the GCM Application (GCMA) XML file, that must mandatory appear
     * as a place of a GCM deployment file.
     */
    @Deprecated
    RM_GCMD_PATH_PROPERTY_NAME("pa.rm.gcmd.path.property.name", PropertyType.STRING, "gcmd.file"),

    /** Path to the Amazon EC2 account credentials properties file,
     *  mandatory when using the EC2 Infrastructure */
    RM_EC2_PATH_PROPERTY_NAME("pa.rm.ec2.properties", PropertyType.STRING, "config/rm/deployment/ec2.properties"),

    /** Resource Manager home directory */
    RM_HOME("pa.rm.home", PropertyType.STRING, "."),

    /** Path to the Jaas configuration file which defines what modules are available for
     * internal authentication */
    RM_AUTH_JAAS_PATH("pa.rm.auth.jaas.path", PropertyType.STRING, "config/authentication/jaas.config"),

    /** Path to the private key file which is used to decrypt credentials passed to the jaas module */
    RM_AUTH_PRIVKEY_PATH("pa.rm.auth.privkey.path", PropertyType.STRING, "config/authentication/keys/priv.key"),

    /** Path to the public key file which is used to encrypt credentials for authentication */
    RM_AUTH_PUBKEY_PATH("pa.rm.auth.pubkey.path", PropertyType.STRING, "config/authentication/keys/pub.key"),

    /** Resource Manager authentication method */
    RM_LOGIN_METHOD("pa.rm.authentication.loginMethod", PropertyType.STRING, "RMFileLoginMethod"),

    /** Resource Manager ldap configuration file */
    RM_LDAP_CONFIG("pa.rm.ldap.config.path", PropertyType.STRING, "config/authentication/ldap.cfg"),

    /** Resource Manager login file name */
    RM_LOGIN_FILE("pa.rm.defaultloginfilename", PropertyType.STRING, "config/authentication/login.cfg"),

    /** Resource Manager group file name */
    RM_GROUP_FILE("pa.rm.defaultgroupfilename", PropertyType.STRING, "config/authentication/group.cfg"),

    /** Name of the JMX MBean for the RM */
    RM_JMX_CONNECTOR_NAME("pa.rm.jmx.connectorname", PropertyType.STRING, "JMXRMAgent"),

    /** Port of the JMX service. Random if not set */
    RM_JMX_PORT("pa.rm.jmx.port", PropertyType.INTEGER, "5822"),

    /** RRD data base with statistic history */
    RM_RRD_DATABASE_NAME("pa.rm.jmx.rrd.name", PropertyType.STRING, "data/rm_statistics.rrd"),

    /** RRD data base step in seconds */
    RM_RRD_STEP("pa.rm.jmx.rrd.step", PropertyType.INTEGER, "4"),

    /** Resource Manager node source infrastructures file*/
    RM_NODESOURCE_INFRASTRUCTURE_FILE("pa.rm.nodesource.infrastructures", PropertyType.STRING, "config/rm/nodesource/infrastructures"),

    /** Resource Manager node source policies file*/
    RM_NODESOURCE_POLICY_FILE("pa.rm.nodesource.policies", PropertyType.STRING, "config/rm/nodesource/policies"),

    /** Timeout (ms) for the resource manager to recover a broken node source in scheduler aware policy*/
    RM_SCHEDULER_AWARE_POLICY_NODESOURCE_RECOVERY_TIMEOUT("pa.rm.scheduler.aware.policy.nodesource.recovery.timeout", PropertyType.INTEGER, "10000"),

    /** Number of trials for the resource manager to recover a broken node source in scheduler aware policy*/
    RM_SCHEDULER_AWARE_POLICY_NODESOURCE_RECOVERY_TRIAL_NUMBER("pa.rm.scheduler.aware.policy.nodesource.recovery.trial.number", PropertyType.INTEGER, "10"),

    /** Max number of threads in node source for parallel task execution */
    RM_NODESOURCE_MAX_THREAD_NUMBER("pa.rm.nodesource.maxthreadnumber", PropertyType.INTEGER, "50"),

    /** The full class name of the policy selected nodes */
    RM_SELECTION_POLICY("pa.rm.selection.policy", PropertyType.STRING, "org.ow2.proactive.resourcemanager.selection.policies.ShufflePolicy"),

    /** Max number of threads in node source for parallel task execution */
    RM_SELECTION_MAX_THREAD_NUMBER("pa.rm.selection.maxthreadnumber", PropertyType.INTEGER, "50"),

    /** Max number of threads in node source for parallel task execution */
    RM_MONITORING_MAX_THREAD_NUMBER("pa.rm.monitoring.maxthreadnumber", PropertyType.INTEGER, "5"),

    /** Max number of threads in the core for cleaning nodes after computations */
    RM_CLEANING_MAX_THREAD_NUMBER("pa.rm.cleaning.maxthreadnumber", PropertyType.INTEGER, "5"),

    /** Maximum node history period in seconds (Default: disabled) */
    RM_HISTORY_MAX_PERIOD("pa.rm.history.maxperiod", PropertyType.INTEGER),

    /** Frequency of node history removal (cron expression) */
    RM_HISTORY_REMOVAL_CRONPERIOD("pa.rm.history.removal.cronperiod", PropertyType.STRING, "*/10 * * * *"),

    /** Max number of lines stored from the infrastructure processes output */
    RM_INFRASTRUCTURE_PROCESS_OUTPUT_MAX_LINES("pa.rm.infrastructure.process.output.maxlines", PropertyType.INTEGER, "2000"),

    /** Path to the Resource Manager credentials for adding local nodes */
    RM_CREDS("pa.rm.credentials", PropertyType.STRING, "config/authentication/rm.cred"),

    /** Resource Manager hibernate configuration file*/
    RM_DB_HIBERNATE_CONFIG("pa.rm.db.hibernate.configuration", PropertyType.STRING, "config/rm/database.properties"),

    /** Refresh time to reload the security policy file (security.java.policy-server) */
    POLICY_RELOAD_FREQUENCY_IN_SECONDS("pa.rm.auth.policy.refreshperiod.seconds", PropertyType.INTEGER, "30"),

    /**
     * Drop database before creating a new one
     * If this value is true, the database will be dropped and then re-created
     * If this value is false, database will be updated from the existing one.
     */
    RM_DB_HIBERNATE_DROPDB("pa.rm.db.hibernate.dropdb", PropertyType.BOOLEAN, "false"),

    /**
     * Drop only node sources from the database.
     */
    RM_DB_HIBERNATE_DROPDB_NODESOURCES("pa.rm.db.hibernate.dropdb.nodesources", PropertyType.BOOLEAN, "false"),

    /**
     * The period of sending "alive" event to resource manager's listeners.
     */
    RM_ALIVE_EVENT_FREQUENCY("pa.rm.aliveevent.frequency", PropertyType.INTEGER, "300000"),

    /** Accounting refresh rate from the database in seconds */
    RM_ACCOUNT_REFRESH_RATE("pa.rm.account.refreshrate", PropertyType.INTEGER, "180"),

    /** Topology feature enable option */
    RM_TOPOLOGY_ENABLED("pa.rm.topology.enabled", PropertyType.BOOLEAN, "true"),

    /**
     * If this is true, the topology mechanism will compute distances between hosts using ping
     **/
    RM_TOPOLOGY_DISTANCE_ENABLED("pa.rm.topology.distance.enabled", PropertyType.BOOLEAN, "false"),

    RM_TOPOLOGY_PINGER(
            "pa.rm.topology.pinger.class",
            PropertyType.STRING,
            "org.ow2.proactive.resourcemanager.frontend.topology.pinging.HostsPinger"),

    /** Resource Manager selection process logs*/
    RM_SELECTION_LOGS_LOCATION("pa.rm.logs.selection.location", PropertyType.STRING, "logs/jobs/"),

    RM_SELECTION_LOGS_MAX_SIZE("pa.rm.logs.selection.max.size", PropertyType.STRING, "10000"),

    RM_NB_LOCAL_NODES("pa.rm.local.nodes.number", PropertyType.INTEGER, "-1"),

    /**Kill Runtime when shutting down the Resource Manager.
     * Default value is true, the JVM Runtime is always killed when shutting down the Resource Manager.
     * Setting this parameter to false is useful when, e.g., deploying ProActive as a web or enterprise application (WAR/EAR)
     * */
    RM_SHUTDOWN_KILL_RUNTIME("pa.rm.shutdown.kill.rt", PropertyType.BOOLEAN, "true"),

    /**
     * Defines if the lock restoration feature is enabled on RM startup.
     * <p>
     * When set to {@code true}, the RM will try to lock per Node Source
     * as many Nodes as there were on the previous run.
     * <p>
     * The approach is best effort and Node hostname is not considered.
     * As a result, Nodes are not necessarily locked on the same host.
     */
    RM_NODES_LOCK_RESTORATION("pa.rm.nodes.lock.restoration", PropertyType.BOOLEAN, "true"),

    /**
     * Defines if the nodes should be kept when the resource manager exits.
     * This property is defaulted to false as shutting down the RM should
     * clean also its nodes.
     */
    RM_PRESERVE_NODES_ON_SHUTDOWN("pa.rm.preserve.nodes.on.shutdown", PropertyType.BOOLEAN, "false"),

    /**
     * Defines whether the node recovery mechanism is enabled on RM startup.
     * If set to {@code true}, it indicates that the node states and the node
     * source infrastructure states should be reloaded from database.
     * This flag is only taken into account if the database cleaner indicators
     * ({@link PAResourceManagerProperties#RM_DB_HIBERNATE_DROPDB} and
     * {@link PAResourceManagerProperties#RM_DB_HIBERNATE_DROPDB_NODESOURCES})
     * are not set to {@code true}.
     */
    RM_NODES_RECOVERY("pa.rm.nodes.recovery", PropertyType.BOOLEAN, "true"),

    /**
     * Defines the minimum inter-time in MILLISECONDS between database
     * operations before a database transaction is triggered. In between
     * such a delay, several database operations will be batched together.
     *
     * This delayed is applied for two different batches of database
     * operations. It applies separately for node source updates and for node
     * adds. If node adds are pending while other node operations are
     * required, then the other node operations are also delayed to preserve
     * operation ordering. Then, if no node adds are pending, the subsequent
     * node operations are either executed synchronously or asynchronously,
     * depending on the {@link PAResourceManagerProperties#RM_NODES_DB_SYNCHRONOUS_UPDATES}
     * property.
     *
     * updates will be batched together In milliseconds. If this property is
     * set to 0, then database operations are not delayed and are synchronous.
     */
    RM_NODES_DB_OPERATIONS_DELAY("pa.rm.node.db.operations.delay", PropertyType.INTEGER, "100"),

    /**
     * Defines if, in the presence of a delay to execute node-related database
     * operations, the node update database operations must still be run
     * synchronously whenever possible.
     */
    RM_NODES_DB_SYNCHRONOUS_UPDATES("pa.rm.nodes.db.operations.update.synchronous", PropertyType.BOOLEAN, "true"),

    /**
     * Defines whether all the resources of the deployed cloud instances
     * should be destroyed along with the nodes termination when the scheduler 
     * is shut down.
     */
    RM_CLOUD_INFRASTRUCTURES_DESTROY_INSTANCES_ON_SHUTDOWN("pa.rm.cloud.infrastructures.destroy.instances.on.shutdown", PropertyType.BOOLEAN, "false");

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    public static final String PA_RM_PROPERTIES_FILEPATH = "pa.rm.properties.filepath";

    public static final String PA_RM_PROPERTIES_RELATIVE_FILEPATH = "config/rm/settings.ini";

    /** memory entity of the properties file. */
    private static PAPropertiesLazyLoader propertiesLoader;

    private static PACommonPropertiesHelper propertiesHelper;

    static {
        reload();
    }

    /** Key of the specific instance. */
    private String key;

    /** value of the specific instance. */
    private PropertyType type;

    /** default value to use if the property is not defined **/
    private String defaultValue;

    /**
     * Create a new instance of PAResourceManagerProperties
     *
     * @param str the key of the instance.
     * @param type the real java type of this instance.
     * @param defaultValue value to use if the property is not defined
     */
    PAResourceManagerProperties(String str, PropertyType type, String defaultValue) {
        this.key = str;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    PAResourceManagerProperties(String str, PropertyType type) {
        this(str, type, null);
    }

    /**
     * Load the properties from the given file.
     * This method will clean every loaded properties before.
     *
     * @param filename the file containing the properties to be loaded.
     */
    protected static void loadProperties(String filename) {
        propertiesLoader = new PAPropertiesLazyLoader(RM_HOME.key,
                                                      PA_RM_PROPERTIES_FILEPATH,
                                                      PA_RM_PROPERTIES_RELATIVE_FILEPATH,
                                                      filename);
        propertiesHelper = new PACommonPropertiesHelper(propertiesLoader);
    }

    public static synchronized void reload() {
        propertiesLoader = new PAPropertiesLazyLoader(RM_HOME.key,
                                                      PA_RM_PROPERTIES_FILEPATH,
                                                      PA_RM_PROPERTIES_RELATIVE_FILEPATH);

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

    /**
     * Return all properties as a HashMap.
     */
    public static Map<String, Object> getPropertiesAsHashMap() {
        return propertiesHelper.getPropertiesAsHashMap();
    }

    /**
     * Get the absolute path of the given path.<br>
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

    @Override
    public String getConfigurationFilePathPropertyName() {
        return PA_RM_PROPERTIES_FILEPATH;
    }

    @Override
    public String getConfigurationDefaultRelativeFilePath() {
        return PA_RM_PROPERTIES_RELATIVE_FILEPATH;
    }

    @Override
    public void loadPropertiesFromFile(String filename) {
        loadProperties(filename);
    }

    @Override
    public void reloadConfiguration() {
        reload();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void updateProperty(String value) {
        propertiesHelper.updateProperty(key, value);
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
    public int getValueAsInt() {
        return propertiesHelper.getValueAsInt(key, type, defaultValue);
    }

    @Override
    public long getValueAsLong() {
        return propertiesHelper.getValueAsLong(key, type, defaultValue);
    }

    @Override
    public String getValueAsString() {
        return propertiesHelper.getValueAsString(key, defaultValue);
    }

    @Override
    public String getValueAsStringOrNull() {
        return propertiesHelper.getValueAsStringOrNull(key);
    }

    @Override
    public boolean getValueAsBoolean() {
        return propertiesHelper.getValueAsBoolean(key, type, defaultValue);
    }

    @Override
    public List<String> getValueAsList(String separator) {
        return propertiesHelper.getValueAsList(key, type, separator, defaultValue);
    }

    @Override
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

}
