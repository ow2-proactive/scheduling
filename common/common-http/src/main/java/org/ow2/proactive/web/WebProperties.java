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

import org.ow2.proactive.core.properties.PACommonPropertiesHelper;
import org.ow2.proactive.core.properties.PropertyType;
import org.ow2.proactive.utils.PAPropertiesLazyLoader;


/**
 * The class defines the name of the properties related
 * to the deployment of the Web server.
 *
 * @author ActiveEon Team
 */
public enum WebProperties {

    /** Rest server home directory */
    REST_HOME("pa.rest.home", PropertyType.STRING),

    WEB_DEPLOY("web.deploy", PropertyType.BOOLEAN),

    WEB_HTTP_PORT("web.http.port", PropertyType.INTEGER),

    WEB_HTTPS("web.https", PropertyType.BOOLEAN),

    WEB_HTTPS_ALLOW_ANY_CERTIFICATE("web.https.allow_any_certificate", PropertyType.BOOLEAN),

    WEB_HTTPS_ALLOW_ANY_HOSTNAME("web.https.allow_any_hostname", PropertyType.BOOLEAN),

    WEB_HTTPS_KEYSTORE("web.https.keystore", PropertyType.STRING),

    WEB_HTTPS_KEYSTORE_PASSWORD("web.https.keystore.password", PropertyType.STRING),

    WEB_HTTPS_TRUSTSTORE("web.https.truststore", PropertyType.STRING),

    WEB_HTTPS_TRUSTSTORE_PASSWORD("web.https.truststore.password", PropertyType.STRING),

    WEB_HTTPS_PORT("web.https.port", PropertyType.INTEGER),

    WEB_MAX_THREADS("web.max_threads", PropertyType.INTEGER),

    @Deprecated
    WEB_PORT("web.port", PropertyType.INTEGER),

    WEB_REDIRECT_HTTP_TO_HTTPS("web.redirect_http_to_https", PropertyType.BOOLEAN),

    METADATA_CONTENT_TYPE("content.type", PropertyType.STRING),

    METADATA_FILE_NAME("file.name", PropertyType.STRING),

    METADATA_FILE_EXTENSION("file.extension", PropertyType.STRING),

    RESOURCE_DOWNLOADER_PROXY("resource.downloader.proxy", PropertyType.STRING),

    RESOURCE_DOWNLOADER_PROXY_PORT("resource.downloader.proxy.port", PropertyType.INTEGER),

    RESOURCE_DOWNLOADER_PROXY_SCHEME("resource.downloader.proxy.scheme", PropertyType.STRING);

    public static final String PA_WEB_PROPERTIES_FILEPATH = "pa.portal.configuration.filepath";

    public static final String PA_WEB_PROPERTIES_RELATIVE_FILEPATH = "config/web/settings.ini";

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

    WebProperties(String str, PropertyType type) {
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

    public static synchronized void reload() {
        propertiesLoader = new PAPropertiesLazyLoader(REST_HOME.key,
                                                      PA_WEB_PROPERTIES_FILEPATH,
                                                      PA_WEB_PROPERTIES_RELATIVE_FILEPATH);
        propertiesHelper = new PACommonPropertiesHelper(propertiesLoader);
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

}
