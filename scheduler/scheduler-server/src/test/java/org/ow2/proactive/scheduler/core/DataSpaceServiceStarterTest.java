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
package org.ow2.proactive.scheduler.core;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.tests.ProActiveTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;


public class DataSpaceServiceStarterTest extends ProActiveTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    // For SCHEDULING-1902, minimal test
    public void testTerminateNamingService() throws Exception {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
        PASchedulerProperties.SCHEDULER_HOME.updateProperty(tmpFolder.newFolder().getAbsolutePath());
        DataSpaceServiceStarterTest activeObject = PAActiveObject
                .turnActive(new DataSpaceServiceStarterTest());
        activeObject.doTestTerminateNamingService();
    }

    public void doTestTerminateNamingService() throws Exception {
        DataSpaceServiceStarter dataSpaceServiceStarter = DataSpaceServiceStarter
                .getDataSpaceServiceStarter();

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
