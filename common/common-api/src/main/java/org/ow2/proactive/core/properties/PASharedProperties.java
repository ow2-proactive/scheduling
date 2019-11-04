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

    /** Key used when decrypting properties */
    PROPERTIES_CRYPT_KEY("pa.shared.properties.crypt.key", PropertyType.STRING, "activeeon");

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
            return PASharedProperties.SHARED_HOME.getValueAsString() + File.separator + userPath;
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
