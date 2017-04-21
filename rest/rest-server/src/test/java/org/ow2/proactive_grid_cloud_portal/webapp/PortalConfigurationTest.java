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

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.core.properties.PACommonPropertiesTestHelper;


public class PortalConfigurationTest extends PACommonPropertiesTestHelper {

    public static final String CACHE_LOGIN = "mylogin";

    @Before
    public void clear() {
        super.clear(PortalConfiguration.REST_HOME);
    }

    @Test
    public void testLoadProperties_NoFile_EmptyProperties() throws Exception {
        super.testLoadProperties_NoFile_EmptyProperties(PortalConfiguration.SCHEDULER_URL);
    }

    @Test
    public void testLoadProperties_NoFile_UseDefault() throws Exception {
        super.testLoadProperties_NoFile_UseDefault(PortalConfiguration.SCHEDULER_CACHE_LOGIN, "watcher");
    }

    @Test
    public void testLoadProperties_RelativeFileManuallySet() throws Exception {
        super.testLoadProperties_RelativeFileManuallySet(PortalConfiguration.SCHEDULER_CACHE_LOGIN,
                                                         CACHE_LOGIN,
                                                         PortalConfiguration.REST_HOME);
    }

    @Test
    public void testLoadProperties_PropertySet_NoFile() throws Exception {
        super.testLoadProperties_PropertySet_NoFile(PortalConfiguration.SCHEDULER_CACHE_LOGIN,
                                                    CACHE_LOGIN,
                                                    PortalConfiguration.REST_HOME);
    }

    @Test
    public void testLoadProperties_PropertySet_NoFile_AndReload() throws Exception {
        super.testLoadProperties_PropertySet_NoFile_AndReload(PortalConfiguration.SCHEDULER_CACHE_LOGIN,
                                                              CACHE_LOGIN,
                                                              PortalConfiguration.REST_HOME);
    }

    @Test
    public void testLoadProperties_FileManuallySet() throws Exception {
        super.testLoadProperties_FileManuallySet(PortalConfiguration.SCHEDULER_CACHE_LOGIN,
                                                 CACHE_LOGIN,
                                                 PortalConfiguration.REST_HOME);
    }

    @Test
    public void testLoadProperties_FileSetWithSystemProperty() throws Exception {
        super.testLoadProperties_FileSetWithSystemProperty(PortalConfiguration.SCHEDULER_CACHE_LOGIN,
                                                           CACHE_LOGIN,
                                                           PortalConfiguration.REST_HOME);
    }

    @Test(expected = RuntimeException.class)
    public void testLoadProperties_FileSetWithSystemProperty_NonExistingFile() throws Exception {
        super.testLoadProperties_FileSetWithSystemProperty_NonExistingFile(PortalConfiguration.SCHEDULER_URL,
                                                                           "an_url",
                                                                           PortalConfiguration.REST_HOME);
    }
}
