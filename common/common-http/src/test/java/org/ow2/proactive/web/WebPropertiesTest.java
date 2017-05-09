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

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.core.properties.PACommonPropertiesTestHelper;


public class WebPropertiesTest extends PACommonPropertiesTestHelper {
    public static final String HTTP_PORT = "9090";

    @Before
    public void clear() {
        super.clear(WebProperties.REST_HOME);
    }

    @Test
    public void testLoadProperties_NoFile_EmptyProperties() throws Exception {
        super.testLoadProperties_NoFile_EmptyProperties(WebProperties.RESOURCE_DOWNLOADER_PROXY);
    }

    @Test
    public void testLoadProperties_NoFile_UseDefault() throws Exception {
        super.testLoadProperties_NoFile_UseDefault(WebProperties.WEB_HTTP_PORT, "8080");
    }

    @Test
    public void testLoadProperties_RelativeFileManuallySet() throws Exception {
        super.testLoadProperties_RelativeFileManuallySet(WebProperties.WEB_HTTP_PORT,
                                                         HTTP_PORT,
                                                         WebProperties.REST_HOME);
    }

    @Test
    public void testLoadProperties_PropertySet_NoFile() throws Exception {
        super.testLoadProperties_PropertySet_NoFile(WebProperties.WEB_HTTP_PORT, HTTP_PORT, WebProperties.REST_HOME);
    }

    @Test
    public void testLoadProperties_PropertySet_NoFile_AndReload() throws Exception {
        super.testLoadProperties_PropertySet_NoFile_AndReload(WebProperties.WEB_HTTP_PORT,
                                                              HTTP_PORT,
                                                              WebProperties.REST_HOME);
    }

    @Test
    public void testLoadProperties_FileManuallySet() throws Exception {
        super.testLoadProperties_FileManuallySet(WebProperties.WEB_HTTP_PORT, HTTP_PORT, WebProperties.REST_HOME);
    }

    @Test
    public void testLoadProperties_FileSetWithSystemProperty() throws Exception {
        super.testLoadProperties_FileSetWithSystemProperty(WebProperties.WEB_HTTP_PORT,
                                                           HTTP_PORT,
                                                           WebProperties.REST_HOME);
    }

    @Test(expected = RuntimeException.class)
    public void testLoadProperties_FileSetWithSystemProperty_NonExistingFile() throws Exception {
        super.testLoadProperties_FileSetWithSystemProperty_NonExistingFile(WebProperties.RESOURCE_DOWNLOADER_PROXY,
                                                                           "myproxy",
                                                                           WebProperties.REST_HOME);
    }
}
