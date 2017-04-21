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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;


public class PACommonPropertiesTestHelper {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    public void clear(PACommonProperties homeProperty) {
        Logger.getRootLogger().setLevel(Level.OFF);
        System.clearProperty(homeProperty.getConfigurationFilePathPropertyName());
        System.clearProperty(homeProperty.getKey());
        homeProperty.loadPropertiesFromFile(null);
    }

    public void testLoadProperties_NoFile_EmptyProperties(PACommonProperties property) throws Exception {
        assertNull(property.getValueAsStringOrNull());
    }

    public void testLoadProperties_NoFile_UseDefault(PACommonProperties property, String expectedValue)
            throws Exception {
        assertEquals(expectedValue, property.getValueAsString());
    }

    public void testLoadProperties_RelativeFileManuallySet(PACommonProperties property, String valueToSet,
            PACommonProperties homeFolderProperty) throws Exception {
        File propertiesFile = writePropertyToFile(property, valueToSet);

        System.setProperty(homeFolderProperty.getKey(), tempFolder.getRoot().getAbsolutePath());

        homeFolderProperty.loadPropertiesFromFile(propertiesFile.getName());

        assertEquals(valueToSet, property.getValueAsString());
    }

    public void testLoadProperties_PropertySet_NoFile(PACommonProperties property, String valueToSet,
            PACommonProperties homeFolderProperty) throws Exception {
        File configFolder = writePropertyToFileInConfigFolder(property, valueToSet);

        System.setProperty(homeFolderProperty.getKey(), configFolder.getAbsolutePath());

        homeFolderProperty.loadPropertiesFromFile(null);

        assertEquals(valueToSet, property.getValueAsString());
    }

    public void testLoadProperties_FileManuallySet(PACommonProperties property, String valueToSet,
            PACommonProperties homeFolderProperty) throws Exception {
        File propertiesFile = writePropertyToFile(property, valueToSet);

        homeFolderProperty.loadPropertiesFromFile(propertiesFile.getAbsolutePath());

        assertEquals(valueToSet, property.getValueAsString());
    }

    public void testLoadProperties_FileSetWithSystemProperty(PACommonProperties property, String valueToSet,
            PACommonProperties homeFolderProperty) throws Exception {
        File propertiesFile = writePropertyToFile(property, valueToSet);

        System.setProperty(homeFolderProperty.getConfigurationFilePathPropertyName(), propertiesFile.getAbsolutePath());

        homeFolderProperty.loadPropertiesFromFile(null);

        assertEquals(valueToSet, property.getValueAsString());
    }

    public void testLoadProperties_PropertySet_NoFile_AndReload(PACommonProperties property, String valueToSet,
            PACommonProperties homeFolderProperty) throws Exception {
        File configFolder = writePropertyToFileInConfigFolder(property, valueToSet);

        // check that the value is not correctly set, as the configuration file could not be found
        assertNotEquals(valueToSet, property.getValueAsString());

        // set the home folder property
        System.setProperty(homeFolderProperty.getKey(), configFolder.getAbsolutePath());

        // reload the configuration
        homeFolderProperty.reloadConfiguration();

        // check that the value is correctly set
        assertEquals(valueToSet, property.getValueAsString());

        // check that the home folder is correctly set
        assertEquals(configFolder.getAbsolutePath(), homeFolderProperty.getValueAsString());
    }

    public void testLoadProperties_FileSetWithSystemProperty_NonExistingFile(PACommonProperties property,
            String valueToSet, PACommonProperties homeFolderProperty) throws Exception {
        try {
            System.setProperty(homeFolderProperty.getConfigurationFilePathPropertyName(), "fakefilenotexisting");

            homeFolderProperty.loadPropertiesFromFile(null);

            // trigger exception
            property.isSet();
        } finally {
            System.clearProperty(homeFolderProperty.getConfigurationFilePathPropertyName());
        }
    }

    private File writePropertyToFile(PACommonProperties propertyKey, String propertyValue) throws IOException {
        Properties props = new Properties();
        props.setProperty(propertyKey.getKey(), propertyValue);
        File propertiesFile = tempFolder.newFile();
        props.store(new FileOutputStream(propertiesFile), "");
        return propertiesFile;
    }

    private File writePropertyToFileInConfigFolder(PACommonProperties propertyKey, String propertyValue)
            throws IOException {
        Properties props = new Properties();
        props.setProperty(propertyKey.getKey(), propertyValue);

        File configFileRelativePath = new File(propertyKey.getConfigurationDefaultRelativeFilePath());
        File configFileRelativeFolder = configFileRelativePath.getParentFile();
        File configFolder = tempFolder.newFolder();
        File subFolder = new File(configFolder, configFileRelativeFolder.getPath());
        if (!subFolder.mkdirs()) {
            throw new IOException("Could not create subfolders");
        }
        File propertiesFile = new File(subFolder, configFileRelativePath.getName());
        props.store(new FileOutputStream(propertiesFile), "");
        return configFolder;
    }

}
