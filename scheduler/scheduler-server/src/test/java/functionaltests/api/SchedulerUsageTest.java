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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.usage.JobUsage;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.TestUsers;


public class SchedulerUsageTest extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testSchedulerUsage() throws Exception {

        SchedulerAuthenticationInterface auth = schedulerHelper.getSchedulerAuth();
        PublicKey pubKey = auth.getPublicKey();

        Credentials cred = Credentials.createCredentials(new CredData(TestUsers.USER.username, TestUsers.USER.password),
                                                         pubKey);

        Scheduler scheduler = auth.login(cred);

        fallbackToMyAccountUsage(scheduler);

        scheduler.disconnect();
    }

    private void fallbackToMyAccountUsage(Scheduler scheduler) throws NotConnectedException, PermissionException {
        List<JobUsage> asAUser = scheduler.getMyAccountUsage(new Date(0), new Date(0));
        assertNotNull(asAUser);

        // fallback to my account usage
        List<JobUsage> forMyUser = scheduler.getAccountUsage(TestUsers.USER.username, new Date(0), new Date(0));
        assertNotNull(forMyUser);

        try {
            scheduler.getAccountUsage(TestUsers.DEMO.username, new Date(0), new Date(0));
            fail("Should throw permission exception");
        } catch (PermissionException e) {
        }
    }
}
