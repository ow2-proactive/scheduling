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
package org.ow2.proactive.web;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.core.properties.PACommonProperties;
import org.ow2.proactive.core.properties.PACommonPropertiesHelper;
import org.ow2.proactive.core.properties.PropertyType;
import org.ow2.proactive.utils.PAPropertiesLazyLoader;


/**
 * The class defines the name of the properties related
 * to the deployment of the Web server.
 *
 * @author ActiveEon Team
 */
public enum WebProperties implements PACommonProperties {

    /** Rest server home directory */
    REST_HOME("pa.rest.home", PropertyType.STRING),

    WEB_DEPLOY("web.deploy", PropertyType.BOOLEAN, "true"),

    WEB_HTTP_PORT("web.http.port", PropertyType.INTEGER, "8080"),

    WEB_HTTPS("web.https", PropertyType.BOOLEAN, "false"),

    WEB_HTTPS_ALLOW_ANY_CERTIFICATE("web.https.allow_any_certificate", PropertyType.BOOLEAN, "false"),

    WEB_HTTPS_ALLOW_ANY_HOSTNAME("web.https.allow_any_hostname", PropertyType.BOOLEAN, "false"),

    WEB_HTTPS_KEYSTORE("web.https.keystore", PropertyType.STRING, "config/web/keystore"),

    WEB_HTTPS_KEYSTORE_PASSWORD("web.https.keystore.password", PropertyType.STRING, "activeeon"),

    WEB_HTTPS_TRUSTSTORE("web.https.truststore", PropertyType.STRING),

    WEB_HTTPS_TRUSTSTORE_PASSWORD("web.https.truststore.password", PropertyType.STRING),

    WEB_HTTPS_PORT("web.https.port", PropertyType.INTEGER, "8443"),

    WEB_MAX_THREADS("web.max_threads", PropertyType.INTEGER, "100"),

    WEB_REDIRECT_HTTP_TO_HTTPS("web.redirect_http_to_https", PropertyType.BOOLEAN, "false"),

    METADATA_CONTENT_TYPE("content.type", PropertyType.STRING),

    METADATA_FILE_NAME("file.name", PropertyType.STRING),

    METADATA_FILE_EXTENSION("file.extension", PropertyType.STRING),

    RESOURCE_DOWNLOADER_PROXY("resource.downloader.proxy", PropertyType.STRING),

    RESOURCE_DOWNLOADER_PROXY_PORT("resource.downloader.proxy.port", PropertyType.INTEGER),

    RESOURCE_DOWNLOADER_PROXY_SCHEME("resource.downloader.proxy.scheme", PropertyType.STRING),

    WAR_WRAPPER_HTTP_PORT("war.wrapper.target.server.http.port", PropertyType.INTEGER, "9080"),

    WAR_WRAPPER_HTTPS_PORT("war.wrapper.target.server.https.port", PropertyType.INTEGER, "9443"),

    WAR_WRAPPER_HTTPS_ENABLED("war.wrapper.https.enabled", PropertyType.BOOLEAN, "false"),

    WAR_WRAPPER_CONTEXT_ROOT("war.wrapper.context.root", PropertyType.STRING, "/");

    public static final String PA_WEB_PROPERTIES_FILEPATH_PROPERTY_NAME = "pa.portal.configuration.filepath";

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

    WebProperties(String str, PropertyType type, String defaultValue) {
        this.key = str;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    WebProperties(String str, PropertyType type) {
        this(str, type, null);
    }

    /**
     * Load the properties from the given file.
     * This method will clean every loaded properties before.
     *
     * @param filename the file containing the properties to be loaded.
     */
    protected static void loadProperties(String filename) {
        propertiesLoader = new PAPropertiesLazyLoader(REST_HOME.key,
                                                      PA_WEB_PROPERTIES_FILEPATH_PROPERTY_NAME,
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
     * Return all properties as a HashMap.
     */
    public static Map<String, Object> getPropertiesAsHashMap() {
        return propertiesHelper.getPropertiesAsHashMap();
    }

    /**
     * Load or reload the configuration, using the default settings and the current value of the REST_HOME system property
     */
    public static synchronized void load() {
        propertiesLoader = new PAPropertiesLazyLoader(REST_HOME.key,
                                                      PA_WEB_PROPERTIES_FILEPATH_PROPERTY_NAME,
                                                      PA_WEB_PROPERTIES_RELATIVE_FILEPATH);
        propertiesHelper = new PACommonPropertiesHelper(propertiesLoader);
    }

    public static synchronized void storeInSystemProperties() {
        System.setProperties(propertiesLoader.getProperties());
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
            return WebProperties.REST_HOME.getValueAsString() + File.separator + userPath;
        }
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

    @Override
    public String getConfigurationFilePathPropertyName() {
        return PA_WEB_PROPERTIES_FILEPATH_PROPERTY_NAME;
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

}
