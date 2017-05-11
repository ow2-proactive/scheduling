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

import java.util.List;


public interface PACommonProperties {

    /**
     * Get the key.
     *
     * @return the key.
     */
    String getKey();

    /**
     * Set the value of this property to the given one.
     *
     * @param value the new value to set.
     */
    void updateProperty(String value);

    /**
     * Return true if this property is set or has a default value, false otherwise.
     *
     * @return true if this property is set, false otherwise.
     */
    boolean isSet();

    /**
     * Unset this property, if this property has a default value, calling unSet will revert to the default.
     */
    void unSet();

    /**
     * Returns the string to be passed on the command line
     *
     * The property surrounded by '-D' and '='
     *
     * @return the string to be passed on the command line
     */
    String getCmdLine();

    /**
     * Returns the value of this property as an integer.
     * If value is not an integer, an exception will be thrown.
     *
     * @return the value of this property.
     */
    int getValueAsInt();

    /**
     * Returns the value of this property as a long integer.
     * If value is not a long integer, an exception will be thrown.
     *
     * @return the value of this property.
     */
    long getValueAsLong();

    /**
     * Returns the value of this property as a string.
     *
     * @return the value of this property.
     */
    String getValueAsString();

    /**
     * Returns the value of this property as a string.
     * If the property is not defined or empty, then null is returned
     *
     * @return the value of this property.
     */
    String getValueAsStringOrNull();

    /**
     * Returns the value of this property as a boolean.
     * If value is not a boolean, an exception will be thrown.<br>
     * The behavior of this method is the same as the {@link java.lang.Boolean#parseBoolean(String s)}.
     *
     * @return the value of this property or false if it's not defined.
     */
    boolean getValueAsBoolean();

    /**
     * Returns the value of this property as a List of strings.
     *
     * @param separator the separator to use
     *
     * @return the list of values of this property.
     */
    List<String> getValueAsList(String separator);

    /**
     * Return the type of the given properties.
     *
     * @return the type of the given properties.
     */
    PropertyType getType();

    /**
     * Returns the System property name used to store the configuration file path
     * @return system property name
     */
    String getConfigurationFilePathPropertyName();

    /**
     * Returns the default relative path used to store the property file
     * @return default file path
     */
    String getConfigurationDefaultRelativeFilePath();

    /**
     * Load the properties from the given file.
     * This method will clean every loaded properties before.
     *
     * @param filename the file containing the properties to be loaded.
     */
    void loadPropertiesFromFile(String filename);

    /**
     * Reload the properties using the default property file configuration
     * This method will clean every loaded properties before.
     */
    void reloadConfiguration();

}
