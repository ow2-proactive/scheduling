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
package functionaltests.authentication;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.*;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


public class ConnectionTest2 extends RMFunctionalTest {

    @Test
    public void action() throws Exception {
        rmHelper.killRM();

        log("Test 1");
        log("Connecting to non existing resource manager with join");
        try {
            RMConnection.join(RMTHelper.getLocalUrl());
            fail("Failed: exception should be thrown");
        } catch (RMException e) {
            log("Passed");
        }

        log("Test 2");
        log("Connecting to non existing resource manager with waitAndJoin and timeout");
        try {
            RMConnection.waitAndJoin(RMTHelper.getLocalUrl(), 1000);
            fail("Failed: exception should be thrown");
        } catch (RMException e) {
            log("Passed");
        }

        log("Test 3");
        log("Connecting to initializing resource manager with waitAndJoin and timeout");

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    log("Running resource manager");
                    rmHelper.getRMAuth();
                } catch (Exception e) {
                    log("Failed: unexpected error " + e.getMessage());
                }
            }
        };
        t.start();

        RMConnection.waitAndJoin(RMTHelper.getLocalUrl(), 60000);
        log("Passed");
    }

}
