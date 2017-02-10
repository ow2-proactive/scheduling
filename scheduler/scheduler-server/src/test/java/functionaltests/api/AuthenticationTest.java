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
package functionaltests.api;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.fail;

import java.security.KeyException;
import java.security.PublicKey;

import javax.security.auth.login.LoginException;

import org.junit.Test;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.TestUsers;


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
public class AuthenticationTest extends SchedulerFunctionalTestNoRestart {

    @Test
    public void action() throws Exception {
        SchedulerAuthenticationInterface auth = schedulerHelper.getSchedulerAuth();
        PublicKey pubKey = auth.getPublicKey();

        loginAsAdmin(auth, pubKey);
        loginAsUser(auth, pubKey);
        loginAsAdminIncorrectPassword(auth, pubKey);
        loginAsUserIncorrectPassword(auth, pubKey);
    }

    private void loginAsAdmin(SchedulerAuthenticationInterface auth, PublicKey pubKey)
            throws KeyException, LoginException, AlreadyConnectedException, NotConnectedException, PermissionException {
        log("Test 1");
        log("Trying to authorized as an admin with correct user name and password");

        Credentials cred = Credentials.createCredentials(new CredData(TestUsers.DEMO.username, TestUsers.DEMO.password),
                                                         pubKey);
        Scheduler admin = auth.login(cred);
        admin.disconnect();
        log("Passed: successful authentication");
    }

    private void loginAsUser(SchedulerAuthenticationInterface auth, PublicKey pubKey)
            throws KeyException, LoginException, AlreadyConnectedException, NotConnectedException, PermissionException {
        log("Test 2");
        log("Trying to authorized as a user with correct user name and password");

        Credentials cred = Credentials.createCredentials(new CredData(TestUsers.USER.username, TestUsers.USER.password),
                                                         pubKey);
        Scheduler user = auth.login(cred);
        user.disconnect();
        log("Passed: successful authentication");
    }

    private void loginAsAdminIncorrectPassword(SchedulerAuthenticationInterface auth, PublicKey pubKey) {
        // negative
        log("Test 3");
        log("Trying to authorized as an admin with incorrect user name and password");

        try {
            Credentials cred = Credentials.createCredentials(new CredData(TestUsers.DEMO.username, "b"), pubKey);
            auth.login(cred);
            fail("Error: successful authentication");
        } catch (Exception e) {
            log("Passed: expected error " + e.getMessage());
        }
    }

    private void loginAsUserIncorrectPassword(SchedulerAuthenticationInterface auth, PublicKey pubKey) {
        log("Test 4");
        log("Trying to authorized as a user with incorrect user name and password");

        try {
            Credentials cred = Credentials.createCredentials(new CredData(TestUsers.USER.username, "b"), pubKey);
            auth.login(cred);
            fail("Error: successful authentication");
        } catch (Exception e) {
            log("Passed: expected error " + e.getMessage());
        }
    }
}
