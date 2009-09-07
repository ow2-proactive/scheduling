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
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMUser;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * test RM's authentication
 *
 * @author ProActive team
 *
 */
public class AuthenticationTest extends FunctionalTest {

    private String adminName = "demo";
    private String adminPwd = "demo";

    private String userName = "user1";
    private String userPwd = "pwd1";

    /**
     * test function
     *
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {

        RMAuthentication auth = RMTHelper.getRMAuth();

        RMTHelper.log("Test 1");
        RMTHelper.log("Trying to authorized as an admin with correct user name and password");

        try {
            RMAdmin admin = auth.logAsAdmin(adminName, adminPwd);
            admin.disconnect();
            RMTHelper.log("Passed: successful authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            RMTHelper.log("Failed: unexpected error " + e.getMessage());
        }

        RMTHelper.log("Test 2");
        RMTHelper.log("Trying to authorized as a user with correct user name and password");

        try {
            RMUser user = auth.logAsUser(userName, userPwd);
            user.disconnect();
            RMTHelper.log("Passed: successful authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            RMTHelper.log("Failed: unexpected error " + e.getMessage());
        }

        RMTHelper.log("Test 3");
        RMTHelper.log("Trying to authorized as a user with correct administrator name and password");

        try {
            RMUser user = auth.logAsUser(adminName, adminPwd);
            user.disconnect();
            RMTHelper.log("Passed: successful authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            RMTHelper.log("Failed: unexpected error " + e.getMessage());
        }

        // negative
        RMTHelper.log("Test 4");
        RMTHelper.log("Trying to authorized as an admin with incorrect user name and password");

        try {
            auth.logAsAdmin(adminName, "b");
            RMTHelper.log("Error: successful authentication");
            assertTrue(false);
        } catch (Exception e) {
            RMTHelper.log("Passed: expected error " + e.getMessage());
        }

        RMTHelper.log("Test 5");
        RMTHelper.log("Trying to authorized as a user with incorrect user name and password");

        try {
            auth.logAsUser(userName, "b");
            RMTHelper.log("Error: successful authentication");
            assertTrue(false);
        } catch (Exception e) {
            RMTHelper.log("Passed: expected error " + e.getMessage());
        }

        RMTHelper.log("Test 6");
        RMTHelper.log("Trying to authorized as an admin with correct user name and password");

        try {
            auth.logAsAdmin(userName, userPwd);
            RMTHelper.log("Error: successful authentication");
            assertTrue(false);
        } catch (Exception e) {
            RMTHelper.log("Passed: expected error " + e.getMessage());
        }

        RMTHelper.log("Test 6");
        RMTHelper.log("Trying to connect twice from one active object");

        try {
            auth.logAsAdmin(adminName, adminPwd);
            auth.logAsAdmin(adminName, adminPwd);
            RMTHelper.log("Error: second authentication was successful");
            assertTrue(false);
        } catch (Exception e) {
            RMTHelper.log("Passed: expected error " + e.getMessage());
        }

    }

}
