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

import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.junit.Test;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.*;


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
