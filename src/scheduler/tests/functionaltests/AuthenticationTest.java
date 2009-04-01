/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package functionaltests;

import static junit.framework.Assert.assertTrue;

import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;

import functionalTests.FunctionalTest;


/**
 * Test connection mechanisms :
 *
 * 1/ log as admin with a valid login/password
 * 2/ log as user  with a valid login/password
 * 3/ log as user with a valid login/password
 * 		(an admin can be logged as user too)
 * 4/ log as admin with an incorrect login/password
 * 5/ log as user with an incorrect login/password
 * 6/ log as admin with valid login/password for,
 * 		but without admin credentials
 *
 * @author ProActive team
 * @since ProActive Scheduling 1.0
 *
 */
public class AuthenticationTest extends FunctionalTest {

    private String adminName = "demo";
    private String adminPwd = "demo";

    private String userName = "user1";
    private String userPwd = "pwd1";

    /**
     * Tests start here.
     *
     * @throws Exception, any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void action() throws Exception {

        SchedulerAuthenticationInterface auth = SchedulerTHelper.getSchedulerAuth();

        SchedulerTHelper.log("Test 1");
        SchedulerTHelper.log("Trying to authorized as an admin with correct user name and password");

        try {
            AdminSchedulerInterface admin = auth.logAsAdmin(adminName, adminPwd);
            admin.disconnect();
            SchedulerTHelper.log("Passed: successfull authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            SchedulerTHelper.log("Failed: unexpected error " + e.getMessage());
        }

        SchedulerTHelper.log("Test 2");
        SchedulerTHelper.log("Trying to authorized as a user with correct user name and password");

        try {
            UserSchedulerInterface user = auth.logAsUser(userName, userPwd);
            user.disconnect();
            SchedulerTHelper.log("Passed: successfull authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            SchedulerTHelper.log("Failed: unexpected error " + e.getMessage());
        }

        SchedulerTHelper.log("Test 3");
        SchedulerTHelper.log("Trying to authorized as a user with correct administrator name and password");

        try {
            UserSchedulerInterface user = auth.logAsUser(adminName, adminPwd);
            user.disconnect();
            SchedulerTHelper.log("Passed: successfull authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            SchedulerTHelper.log("Failed: unexpected error " + e.getMessage());
        }

        // negative
        SchedulerTHelper.log("Test 4");
        SchedulerTHelper.log("Trying to authorized as an admin with incorrect user name and password");

        try {
            auth.logAsAdmin(adminName, "b");
            SchedulerTHelper.log("Error: successfull authentication");
            assertTrue(false);
        } catch (Exception e) {
            SchedulerTHelper.log("Passed: expected error " + e.getMessage());
        }

        SchedulerTHelper.log("Test 5");
        SchedulerTHelper.log("Trying to authorized as a user with incorrect user name and password");

        try {
            auth.logAsUser(userName, "b");
            SchedulerTHelper.log("Error: successfull authentication");
            assertTrue(false);
        } catch (Exception e) {
            SchedulerTHelper.log("Passed: expected error " + e.getMessage());
        }

        SchedulerTHelper.log("Test 6");
        SchedulerTHelper.log("Trying to authorized as an admin with correct user name and password");

        try {
            auth.logAsAdmin(userName, userPwd);
            SchedulerTHelper.log("Error: successfull authentication");
            assertTrue(false);
        } catch (Exception e) {
            SchedulerTHelper.log("Passed: expected error " + e.getMessage());
        }
    }
}
