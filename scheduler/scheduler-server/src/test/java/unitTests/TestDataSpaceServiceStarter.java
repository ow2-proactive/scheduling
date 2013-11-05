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
package unitTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.core.DataSpaceServiceStarter;

public class TestDataSpaceServiceStarter {

    @Test // For SCHEDULING-1902, minimal test
    public void testTerminateNamingService() throws Exception {
        TestDataSpaceServiceStarter activeObject = PAActiveObject.turnActive(new TestDataSpaceServiceStarter());
        activeObject.doTestTerminateNamingService();
    }

    public void doTestTerminateNamingService() throws Exception {
        DataSpaceServiceStarter dataSpaceServiceStarter =  DataSpaceServiceStarter.getDataSpaceServiceStarter();

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
