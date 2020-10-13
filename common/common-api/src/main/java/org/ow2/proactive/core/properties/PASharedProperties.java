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
package org.ow2.proactive.core.properties;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.utils.PAPropertiesLazyLoader;


/**
 * PASharedProperties contains all shared properties.
 *
 *
 * @author The ProActiveTeam
 * @since ProActive 10.1
 *
 * $Id$
 */
public enum PASharedProperties implements PACommonProperties {

    /** reusing existing rm home property **/
    SHARED_HOME("pa.rm.home", PropertyType.STRING),

    /** maximum number of failed attempts accepted in the given time window (to prevent brute force attacks). The mechanism can be disabled
     * by using a zero or negative value **/
    FAILED_LOGIN_MAX_ATTEMPTS("pa.shared.failed.max.attempts", PropertyType.INTEGER, "3"),

    /** time window in minutes after which a failed login attempt is forgotten **/
    FAILED_LOGIN_RENEW_MINUTES("pa.shared.failed.renew.minutes", PropertyType.INTEGER, "10"),

    /** Key used when decrypting properties */
    PROPERTIES_CRYPT_KEY("pa.shared.properties.crypt.key", PropertyType.STRING, "activeeon"),

    /* ***************************************************************** */
    /* ******************* SCHEDULER BACKUP PROPERTIES ******************* */
    /* ***************************************************************** */

    /** if backup is enabled */
    SERVER_BACKUP("pa.server.backup", PropertyType.BOOLEAN, "false"),

    /** cron expression which defines when backup should be performed (every day by default).
        A UNIX crontab-like expression in the following format:
        (minutes) (hours) (days of month) (month) (days of week)
     * */
    SERVER_BACKUP_PERIOD("pa.server.backup.period", PropertyType.STRING, "0 0 * * *"),

    /** determines how many backups should be kept */
    SERVER_BACKUP_WINDOWS("pa.server.backup.windows", PropertyType.INTEGER, "10"),

    /** a relative or absolute path to a folder (where the backup files should be stored) */
    SERVER_BACKUP_DESTINATION("pa.server.backup.destination", PropertyType.STRING, "backup"),

    /** comma-separated list of folder and/or files which need to be backup */
    SERVER_BACKUP_TARGETS("pa.server.backup.targets", PropertyType.STRING, "data,logs"),

    /** backup mechanism may wait possible.delay (in seconds) until all currently running tasks are finished.
     * if some tasks are still running after this delay, the backup will not be performed */
    SERVER_BACKUP_POSSIBLE_DELAY("pa.server.backup.possible.delay", PropertyType.INTEGER, "600"),

    /** Controls the fetch mode of scripts defined by URL.
    If true (default), it means that the script is fetched at task execution time
    If false, it means that the script is fetch when the job is submitted to the scheduler */
    LAZY_FETCH_SCRIPT("pa.lazy.fetch.script", PropertyType.BOOLEAN, "true");

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    public static final String PA_SHARED_PROPERTIES_FILEPATH = "pa.shared.properties.filepath";

    public static final String PA_SHARED_PROPERTIES_RELATIVE_FILEPATH = "config/shared/settings.ini";

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
    PASharedProperties(String str, PropertyType type, String defaultValue) {
        this.key = str;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    PASharedProperties(String str, PropertyType type) {
        this(str, type, null);
    }

    /**
     * Load the properties from the given file.
     * This method will clean every loaded properties before.
     *
     * @param filename the file containing the properties to be loaded.
     */
    protected static void loadProperties(String filename) {
        propertiesLoader = new PAPropertiesLazyLoader(SHARED_HOME.key,
                                                      PA_SHARED_PROPERTIES_FILEPATH,
                                                      PA_SHARED_PROPERTIES_RELATIVE_FILEPATH,
                                                      filename);
        propertiesHelper = new PACommonPropertiesHelper(propertiesLoader);
    }

    public static synchronized void reload() {
        propertiesLoader = new PAPropertiesLazyLoader(SHARED_HOME.key,
                                                      PA_SHARED_PROPERTIES_FILEPATH,
                                                      PA_SHARED_PROPERTIES_RELATIVE_FILEPATH);

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
            File pathName = new File(PASharedProperties.SHARED_HOME.getValueAsString(), userPath);
            try {
                return pathName.getCanonicalPath();
            } catch (IOException e) {
                return pathName.getAbsolutePath();
            }
        }
    }

    @Override
    public String getConfigurationFilePathPropertyName() {
        return PA_SHARED_PROPERTIES_FILEPATH;
    }

    @Override
    public String getConfigurationDefaultRelativeFilePath() {
        return PA_SHARED_PROPERTIES_RELATIVE_FILEPATH;
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
