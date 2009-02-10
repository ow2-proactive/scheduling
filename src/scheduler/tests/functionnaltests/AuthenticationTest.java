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
package functionnaltests;

import static junit.framework.Assert.assertTrue;

import org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerConnection;
import org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface;


public class AuthenticationTest extends FunctionalTDefaultScheduler {

    private String adminName = "jl";
    private String adminPwd = "jl";

    private String userName = "user1";
    private String userPwd = "pwd1";

    @org.junit.Test
    public void action() throws Exception {

        schedUserInterface.disconnect();

        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin(schedulerDefaultURL);

        log("Test 1");
        log("Trying to authorized as an admin with correct user name and password");

        try {
            AdminSchedulerInterface admin = auth.logAsAdmin(adminName, adminPwd);
            admin.disconnect();
            log("Passed: successfull authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            log("Failed: unexpected error " + e.getMessage());
        }

        log("Test 2");
        log("Trying to authorized as a user with correct user name and password");

        try {
            UserSchedulerInterface user = auth.logAsUser(userName, userPwd);
            user.disconnect();
            log("Passed: successfull authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            log("Failed: unexpected error " + e.getMessage());
        }

        log("Test 3");
        log("Trying to authorized as a user with correct administrator name and password");

        try {
            UserSchedulerInterface user = auth.logAsUser(adminName, adminPwd);
            user.disconnect();
            log("Passed: successfull authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            log("Failed: unexpected error " + e.getMessage());
        }

        // negative
        log("Test 4");
        log("Trying to authorized as an admin with incorrect user name and password");

        try {
            auth.logAsAdmin(adminName, "b");
            log("Error: successfull authentication");
            assertTrue(false);
        } catch (Exception e) {
            log("Passed: expected error " + e.getMessage());
        }

        log("Test 5");
        log("Trying to authorized as a user with incorrect user name and password");

        try {
            auth.logAsUser(userName, "b");
            log("Error: successfull authentication");
            assertTrue(false);
        } catch (Exception e) {
            log("Passed: expected error " + e.getMessage());
        }

        log("Test 6");
        log("Trying to authorized as an admin with correct user name and password");

        try {
            auth.logAsAdmin(userName, userPwd);
            log("Error: successfull authentication");
            assertTrue(false);
        } catch (Exception e) {
            log("Passed: expected error " + e.getMessage());
        }
    }
}
