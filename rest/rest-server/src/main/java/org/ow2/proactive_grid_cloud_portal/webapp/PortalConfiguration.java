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
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.File;
import java.util.List;

import org.ow2.proactive.core.properties.PACommonProperties;
import org.ow2.proactive.core.properties.PACommonPropertiesHelper;
import org.ow2.proactive.core.properties.PropertyType;
import org.ow2.proactive.utils.PAPropertiesLazyLoader;


public enum PortalConfiguration implements PACommonProperties {

    /** Rest server home directory */
    REST_HOME("pa.rest.home", PropertyType.STRING),

    SCHEDULER_URL("scheduler.url", PropertyType.STRING),

    SCHEDULER_CACHE_LOGIN("scheduler.cache.login", PropertyType.STRING, "watcher"),

    SCHEDULER_CACHE_PASSWORD("scheduler.cache.password", PropertyType.STRING),

    SCHEDULER_CACHE_CREDENTIALS("scheduler.cache.credential", PropertyType.STRING),

    SCHEDULER_LOGINFORWARDINGSERVICE_PROVIDER(
            "scheduler.logforwardingservice.provider",
            PropertyType.STRING,
            "org.ow2.proactive.scheduler.common.util.logforwarder.providers.SocketBasedForwardingProvider"),

    RM_URL("rm.url", PropertyType.STRING),

    RM_CACHE_LOGIN("rm.cache.login", PropertyType.STRING, "watcher"),

    RM_CACHE_PASSWORD("rm.cache.password", PropertyType.STRING),

    RM_CACHE_CREDENTIALS("rm.cache.credential", PropertyType.STRING),

    RM_CACHE_REFRESHRATE("rm.cache.refreshrate", PropertyType.INTEGER, "3500"),

    NOVNC_ENABLED("novnc.enabled", PropertyType.BOOLEAN, "false"),

    NOVNC_PORT("novnc.port", PropertyType.INTEGER, "5900"),

    NOVNC_SECURED("novnc.secured", PropertyType.STRING, "ON"),

    NOVNC_KEYSTORE("novnc.keystore", PropertyType.STRING, "keystore.jks"),

    NOVNC_PASSWORD("novnc.password", PropertyType.STRING, "password"),

    NOVNC_KEYPASSWORD("novnc.keypassword", PropertyType.STRING, "password"),

    JOBPLANNER_URL("jp.url", PropertyType.STRING, "http://localhost:8080/job-planner/planned_jobs");

    public static final String PA_WEB_PROPERTIES_FILEPATH = "pa.web.properties.filepath";

    public static final String PA_WEB_PROPERTIES_RELATIVE_FILEPATH = "config/web/settings.ini";

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

    PortalConfiguration(String str, PropertyType type, String defaultValue) {
        this.key = str;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    PortalConfiguration(String str, PropertyType type) {
        this(str, type, null);
    }

    public static synchronized void storeInSystemProperties() {
        System.setProperties(propertiesLoader.getProperties());
    }

    /**
     * Load the properties from the given file.
     * This method will clean every loaded properties before.
     *
     * @param filename the file containing the properties to be loaded.
     */
    protected static void loadProperties(String filename) {
        propertiesLoader = new PAPropertiesLazyLoader(REST_HOME.key,
                                                      PA_WEB_PROPERTIES_FILEPATH,
                                                      PA_WEB_PROPERTIES_RELATIVE_FILEPATH,
                                                      filename);
        propertiesHelper = new PACommonPropertiesHelper(propertiesLoader);
    }

    public static synchronized void load() {
        propertiesLoader = new PAPropertiesLazyLoader(REST_HOME.key,
                                                      PA_WEB_PROPERTIES_FILEPATH,
                                                      PA_WEB_PROPERTIES_RELATIVE_FILEPATH);
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
            return PortalConfiguration.REST_HOME.getValueAsString() + File.separator + userPath;
        }
    }

    /**
     * convert a job id to the location where the archive has been stored
     * @param jobId the job id
     * @return a string representing the path to the archive file
     */
    public static String jobIdToPath(String jobId) {
        return System.getProperty("java.io.tmpdir") + File.separator + "job_" + jobId + ".zip";
    }

    @Override
    public String getConfigurationFilePathPropertyName() {
        return PA_WEB_PROPERTIES_FILEPATH;
    }

    @Override
    public String getConfigurationDefaultRelativeFilePath() {
        return PA_WEB_PROPERTIES_RELATIVE_FILEPATH;
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
    public long getValueAsLong() {
        return propertiesHelper.getValueAsLong(key, type, defaultValue);
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
