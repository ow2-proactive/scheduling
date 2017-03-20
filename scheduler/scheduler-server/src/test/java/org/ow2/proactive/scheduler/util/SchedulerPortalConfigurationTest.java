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
package org.ow2.proactive.scheduler.util;

import static org.junit.Assert.assertEquals;

import java.security.KeyException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.tests.ProActiveTest;


public class SchedulerPortalConfigurationTest extends ProActiveTest {

    private SchedulerPortalConfiguration configuration;

    @Before
    public void setUp() throws KeyException {
        String path = SchedulerPortalConfigurationTest.class.getResource("/config/scheduler-portal-display.conf")
                                                            .getPath();
        PASchedulerProperties.SCHEDULER_PORTAL_CONFIGURATION.updateProperty(path);
        configuration = SchedulerPortalConfiguration.getConfiguration();
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        Properties properties = configuration.getProperties();
        assertEquals("[properties]", properties.getProperty("execution-list-extra-columns"));
    }
}
