/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.authentication;

import static junit.framework.Assert.assertTrue;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;


/**
 *
 * test RM's authentication
 *
 * @author ProActive team
 *
 */
public class AuthenticationTest extends RMConsecutive {

    private String adminName = "demo";
    private String adminPwd = "demo";

    private String userName = "user";
    private String userPwd = "pwd";

    /**
     * test function
     *
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {
        RMAuthentication auth = RMTHelper.getDefaultInstance().getRMAuth();
        RMTHelper.getDefaultInstance().getResourceManager().disconnect().getBooleanValue();

        RMTHelper.log("Test 1");
        RMTHelper.log("Trying to authorized with correct admin name and password");

        try {
            Credentials cred = Credentials.createCredentials(new CredData(adminName, adminPwd), auth
                    .getPublicKey());
            ResourceManager admin = auth.login(cred);
            admin.disconnect();
            RMTHelper.log("Passed: successful authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            RMTHelper.log("Failed: unexpected error " + e.getMessage());
        }

        RMTHelper.log("Test 2");
        RMTHelper.log("Trying to authorized with correct user name and password");

        try {
            Credentials cred = Credentials.createCredentials(new CredData(userName, userPwd), auth
                    .getPublicKey());
            ResourceManager user = auth.login(cred);
            user.disconnect();
            RMTHelper.log("Passed: successful authentication");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
            RMTHelper.log("Failed: unexpected error " + e.getMessage());
        }

        // negative
        RMTHelper.log("Test 3");
        RMTHelper.log("Trying to authorized with incorrect user name and password");

        try {
            Credentials cred = Credentials.createCredentials(new CredData(adminName, "b"), auth
                    .getPublicKey());
            auth.login(cred);
            RMTHelper.log("Error: successful authentication");
            assertTrue(false);
        } catch (Exception e) {
            RMTHelper.log("Passed: expected error " + e.getMessage());
        }

        RMTHelper.log("Test 4");
        RMTHelper.log("Trying to authorized with incorrect user name and password");

        try {
            Credentials cred = Credentials
                    .createCredentials(new CredData(userName, "b"), auth.getPublicKey());
            auth.login(cred);
            RMTHelper.log("Error: successful authentication");
            assertTrue(false);
        } catch (Exception e) {
            RMTHelper.log("Passed: expected error " + e.getMessage());
        }

        RMTHelper.log("Test 5");
        RMTHelper.log("Trying to connect twice from one active object");

        try {
            Credentials cred = Credentials.createCredentials(new CredData(adminName, adminPwd), auth
                    .getPublicKey());
            auth.login(cred);
            auth.login(cred);
            RMTHelper.log("Error: second authentication was successful");
            assertTrue(false);
        } catch (Exception e) {
            RMTHelper.log("Passed: expected error " + e.getMessage());
        }

    }
}
