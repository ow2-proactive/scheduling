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

import org.ow2.proactive.core.properties.PACommonPropertiesHelper;
import org.ow2.proactive.core.properties.PropertyType;
import org.ow2.proactive.utils.PAPropertiesLazyLoader;


public enum PortalConfiguration {

    /** Rest server home directory */
    REST_HOME("pa.rest.home", PropertyType.STRING),

    SCHEDULER_URL("scheduler.url", PropertyType.STRING),

    SCHEDULER_CACHE_LOGIN("scheduler.cache.login", PropertyType.STRING),

    SCHEDULER_CACHE_PASSWORD("scheduler.cache.password", PropertyType.STRING),

    SCHEDULER_CACHE_CREDENTIALS("scheduler.cache.credential", PropertyType.STRING),

    SCHEDULER_LOGINFORWARDINGSERVICE_PROVIDER("scheduler.logforwardingservice.provider", PropertyType.STRING),

    RM_URL("rm.url", PropertyType.STRING),

    RM_CACHE_LOGIN("rm.cache.login", PropertyType.STRING),

    RM_CACHE_PASSWORD("rm.cache.password", PropertyType.STRING),

    RM_CACHE_CREDENTIALS("rm.cache.credential", PropertyType.STRING),

    RM_CACHE_REFRESHRATE("rm.cache.refreshrate", PropertyType.INTEGER),

    NOVNC_ENABLED("novnc.enabled", PropertyType.BOOLEAN),

    NOVNC_PORT("novnc.port", PropertyType.INTEGER),

    NOVNC_SECURED("novnc.secured", PropertyType.STRING),

    NOVNC_KEYSTORE("novnc.keystore", PropertyType.STRING),

    NOVNC_PASSWORD("novnc.password", PropertyType.STRING),

    NOVNC_KEYPASSWORD("novnc.keypassword", PropertyType.STRING);

    public static final String PA_WEB_PROPERTIES_FILEPATH = "pa.web.properties.filepath";

    public static final String PA_WEB_PROPERTIES_RELATIVE_FILEPATH = "config/web/settings.ini";

    /** memory entity of the properties file. */
    private static PAPropertiesLazyLoader propertiesLoader = new PAPropertiesLazyLoader(REST_HOME.key,
                                                                                        PA_WEB_PROPERTIES_FILEPATH,
                                                                                        PA_WEB_PROPERTIES_RELATIVE_FILEPATH);

    private static PACommonPropertiesHelper propertiesHelper = new PACommonPropertiesHelper(propertiesLoader);

    /** Key of the specific instance. */
    private String key;

    /** value of the specific instance. */
    private PropertyType type;

    PortalConfiguration(String str, PropertyType type) {
        this.key = str;
        this.type = type;
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
     * Return true if this property is set, false otherwise.
     *
     * @return true if this property is set, false otherwise.
     */
    public boolean isSet() {
        return propertiesHelper.isSet(key);
    }

    /**
     * Unset this property
     */
    public void unSet() {
        propertiesHelper.unSet(key);
    }

    /**
     * Returns the string to be passed on the command line
     *
     * The property surrounded by '-D' and '='
     *
     * @return the string to be passed on the command line
     */
    public String getCmdLine() {
        return propertiesHelper.getCmdLine(key);
    }

    /**
     * Returns the value of this property as an integer.
     * If value is not an integer, an exception will be thrown.
     *
     * @return the value of this property.
     */
    public int getValueAsInt() {
        return propertiesHelper.getValueAsInt(key);
    }

    /**
     * Returns the value of this property as a string.
     *
     * @return the value of this property.
     */
    public String getValueAsString() {
        return propertiesHelper.getValueAsString(key);
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
     * @return the value of this property.
     */
    public boolean getValueAsBoolean() {
        return propertiesHelper.getValueAsBoolean(key);
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
     * convert a job id to the location where the archive has been stored
     * @param jobId the job id
     * @return a string representing the path to the archive file
     */
    public static String jobIdToPath(String jobId) {
        return System.getProperty("java.io.tmpdir") + File.separator + "job_" + jobId + ".zip";
    }

}
