/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.authentication;

import static junit.framework.Assert.assertTrue;

import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * Test RM's connection helpers
 *
 * @author ProActive team
 *
 */
public class ConnectionTest extends FunctionalTest {

    /**
     * test function
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {

        RMAuthentication auth = RMTHelper.getRMAuth();

        RMTHelper.log("Test 1");
        RMTHelper.log("Connecting to existing resource manager");
        try {
            RMConnection.join(null);
            RMConnection.waitAndJoin(null);
            RMConnection.waitAndJoin(null, 10);

            RMTHelper.log("Passed");
        } catch (Exception e) {
            RMTHelper.log("Failed: unexpected error " + e.getMessage());
            assertTrue(false);
        }
    }

}
