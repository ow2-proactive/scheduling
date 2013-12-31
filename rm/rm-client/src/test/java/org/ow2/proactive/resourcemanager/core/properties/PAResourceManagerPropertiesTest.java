/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;


public class PAResourceManagerPropertiesTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void clear() {
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

        System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(),
          tempFolder.getRoot().getAbsolutePath());

        PAResourceManagerProperties.loadProperties(propertiesFile.getName());

        assertEquals("nodeName", PAResourceManagerProperties.RM_NODE_NAME.getValueAsString());
    }

    @Test
    public void testLoadProperties_RMHomeSet_NoFile() throws Exception {
        File configFolder = writePropertyToFileInConfigFolder(PAResourceManagerProperties.RM_NODE_NAME,
          "nodeName");

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

        System.setProperty(PAResourceManagerProperties.PA_RM_PROPERTIES_FILEPATH,
          propertiesFile.getAbsolutePath());

        PAResourceManagerProperties.loadProperties(null);

        assertEquals("nodeName", PAResourceManagerProperties.RM_NODE_NAME.getValueAsString());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadProperties_FileSetWithSystemProperty_NonExistingFile() throws Exception {
        System.setProperty(PAResourceManagerProperties.PA_RM_PROPERTIES_FILEPATH,
          "fakefilenotexisting");

        PAResourceManagerProperties.loadProperties(null);

        // trigger exception
        PAResourceManagerProperties.RM_NODE_NAME.isSet();
    }

    private File writePropertyToFile(PAResourceManagerProperties propertyKey,
      String propertyValue) throws IOException {
        Properties props = new Properties();
        props.setProperty(propertyKey.getKey(), propertyValue);
        File propertiesFile = tempFolder.newFile();
        props.store(new FileOutputStream(propertiesFile), "");
        return propertiesFile;
    }

    private File writePropertyToFileInConfigFolder(PAResourceManagerProperties propertyKey,
      String propertyValue) throws IOException {
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
