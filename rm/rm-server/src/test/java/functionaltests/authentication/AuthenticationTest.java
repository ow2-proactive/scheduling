/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.TestUsers;
import org.junit.Test;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import javax.security.auth.login.LoginException;
import java.security.KeyException;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.fail;


public class AuthenticationTest extends RMFunctionalTest {

    @Test
    public void loginScenarios() throws Exception {
        RMAuthentication auth = rmHelper.getRMAuth();
        rmHelper.disconnect();

        loginAsAdmin(auth);
        loginAsUser(auth);
        loginIncorrectAdminPassword(auth);
        loginIncorrectUserPassword(auth);
    }

    private void loginAsAdmin(RMAuthentication auth) throws LoginException, KeyException {
        log("Test 1");
        log("Trying to authorized with correct admin name and password");

        Credentials cred = Credentials.createCredentials(new CredData(TestUsers.DEMO.username,
            TestUsers.DEMO.password), auth.getPublicKey());
        ResourceManager admin = auth.login(cred);
        admin.disconnect().getBooleanValue();
        log("Passed: successful authentication");
    }

    private void loginAsUser(RMAuthentication auth) throws LoginException, KeyException {
        log("Test 2");
        log("Trying to authorized with correct user name and password");

        Credentials cred = Credentials.createCredentials(new CredData(TestUsers.USER.username,
            TestUsers.USER.password), auth.getPublicKey());
        ResourceManager user = auth.login(cred);
        user.disconnect().getBooleanValue();
        log("Passed: successful authentication");
    }

    private void loginIncorrectAdminPassword(RMAuthentication auth) throws KeyException {
        // negative
        log("Test 3");
        log("Trying to authorized with incorrect user name and password");

        try {
            Credentials cred = Credentials.createCredentials(new CredData(TestUsers.DEMO.username, "b"),
                    auth.getPublicKey());
            auth.login(cred);
            fail("Error: successful authentication");
        } catch (LoginException e) {
            log("Passed: expected error " + e.getMessage());
        }
    }

    private void loginIncorrectUserPassword(RMAuthentication auth) throws KeyException {
        log("Test 4");
        log("Trying to authorized with incorrect user name and password");

        try {
            Credentials cred = Credentials.createCredentials(new CredData(TestUsers.USER.username, "b"),
                    auth.getPublicKey());
            auth.login(cred);
            fail("Error: successful authentication");
        } catch (LoginException e) {
            log("Passed: expected error " + e.getMessage());
        }
    }
}
