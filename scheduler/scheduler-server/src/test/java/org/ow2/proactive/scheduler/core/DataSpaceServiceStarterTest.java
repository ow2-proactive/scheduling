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
package org.ow2.proactive.scheduler.core;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.tests.ProActiveTest;


public class DataSpaceServiceStarterTest extends ProActiveTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    // For SCHEDULING-1902, minimal test
    public void testTerminateNamingService() throws Exception {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
        PASchedulerProperties.SCHEDULER_HOME.updateProperty(tmpFolder.newFolder().getAbsolutePath());
        DataSpaceServiceStarterTest activeObject = PAActiveObject.turnActive(new DataSpaceServiceStarterTest());
        activeObject.doTestTerminateNamingService();
    }

    public void doTestTerminateNamingService() throws Exception {
        DataSpaceServiceStarter dataSpaceServiceStarter = DataSpaceServiceStarter.getDataSpaceServiceStarter();

        dataSpaceServiceStarter.startNamingService();
        assertEquals(1, dataSpaceServiceStarter.getNamingService().getRegisteredApplications().size());

        dataSpaceServiceStarter.terminateNamingService();
        try {
            dataSpaceServiceStarter.getNamingService().getRegisteredApplications();
            fail("Naming service should not be accessible anymore");
        } catch (Exception expected) {
            assertTrue(expected instanceof IllegalStateException);
        }
    }

}
