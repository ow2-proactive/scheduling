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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class PAResourceManagerPropertiesTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void clear() {
        Logger.getRootLogger().setLevel(Level.OFF);
        System.clearProperty(PAResourceManagerProperties.PA_RM_PROPERTIES_FILEPATH);
        System.clearProperty(PAResourceManagerProperties.RM_HOME.getKey());
        PAResourceManagerProperties.loadProperties(null);
    }

    @Test
    public void testLoadProperties_NoFile_EmptyProperties() throws Exception {
        assertEquals("", PAResourceManagerProperties.RM_NODE_NAME.getValueAsString());
    }

    @Test
    public void testLoadProperties_RelativeFileManuallySet() throws Exception {
        File propertiesFile = writePropertyToFile(PAResourceManagerProperties.RM_NODE_NAME, "nodeName");

        System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(), tempFolder.getRoot().getAbsolutePath());

        PAResourceManagerProperties.loadProperties(propertiesFile.getName());

        assertEquals("nodeName", PAResourceManagerProperties.RM_NODE_NAME.getValueAsString());
    }

    @Test
    public void testLoadProperties_RMHomeSet_NoFile() throws Exception {
        File configFolder = writePropertyToFileInConfigFolder(PAResourceManagerProperties.RM_NODE_NAME, "nodeName");

        System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(), configFolder.getAbsolutePath());

        PAResourceManagerProperties.loadProperties(null);

        assertEquals("nodeName", PAResourceManagerProperties.RM_NODE_NAME.getValueAsString());
    }

    @Test
    public void testLoadProperties_FileManuallySet() throws Exception {
        File propertiesFile = writePropertyToFile(PAResourceManagerProperties.RM_NODE_NAME, "nodeName");

        PAResourceManagerProperties.loadProperties(propertiesFile.getAbsolutePath());

        assertEquals("nodeName", PAResourceManagerProperties.RM_NODE_NAME.getValueAsString());
    }

    @Test
    public void testLoadProperties_FileSetWithSystemProperty() throws Exception {
        File propertiesFile = writePropertyToFile(PAResourceManagerProperties.RM_NODE_NAME, "nodeName");

        System.setProperty(PAResourceManagerProperties.PA_RM_PROPERTIES_FILEPATH, propertiesFile.getAbsolutePath());

        PAResourceManagerProperties.loadProperties(null);

        assertEquals("nodeName", PAResourceManagerProperties.RM_NODE_NAME.getValueAsString());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadProperties_FileSetWithSystemProperty_NonExistingFile() throws Exception {
        System.setProperty(PAResourceManagerProperties.PA_RM_PROPERTIES_FILEPATH, "fakefilenotexisting");

        PAResourceManagerProperties.loadProperties(null);

        // trigger exception
        PAResourceManagerProperties.RM_NODE_NAME.isSet();
    }

    private File writePropertyToFile(PAResourceManagerProperties propertyKey, String propertyValue) throws IOException {
        Properties props = new Properties();
        props.setProperty(propertyKey.getKey(), propertyValue);
        File propertiesFile = tempFolder.newFile();
        props.store(new FileOutputStream(propertiesFile), "");
        return propertiesFile;
    }

    private File writePropertyToFileInConfigFolder(PAResourceManagerProperties propertyKey, String propertyValue)
            throws IOException {
        Properties props = new Properties();
        props.setProperty(propertyKey.getKey(), propertyValue);
        File configFolder = tempFolder.newFolder();
        File subFolder = new File(configFolder, "/config/rm/");
        if (!subFolder.mkdirs()) {
            throw new IOException("Could not create subfolders");
        }
        File propertiesFile = new File(subFolder, "settings.ini");
        props.store(new FileOutputStream(propertiesFile), "");
        return configFolder;
    }
}
