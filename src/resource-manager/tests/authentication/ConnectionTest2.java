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
package authentication;

import static junit.framework.Assert.assertTrue;

import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;

import functionalTests.FunctionalTest;

public class ConnectionTest2 extends FunctionalTest {

    @org.junit.Test
    public void action() throws Exception {

        log("Test 1");
        log("Connecting to non existing resource manager with join");
        try {
        	RMConnection.join(null);
            log("Failed: exception should be thrown");
        	assertTrue(false);
        } catch (Exception e) {        	
            log("Passed");
        }        
        
        log("Test 2");
        log("Connecting to non existing resource manager with waitAndJoin and timeout");
        try {
        	RMConnection.waitAndJoin(null, 1000);
            log("Failed: exception should be thrown");
        	assertTrue(false);
        } catch (Exception e) {        	
            log("Passed");
        }

        log("Test 3");
        log("Connecting to initializing resource manager with waitAndJoin and timeout");
        try {
        	
        	Thread t = new Thread() {
        		public void run() {
        			try {
						Thread.sleep(1000);
						log("Running resource manager");
						RMFactory.startLocal();
					} catch (Exception e) {
						assertTrue(false);
						log("Failed: unexpected error " + e.getMessage());
					}
        		}
        	};
        	t.start();

        	RMConnection.waitAndJoin(null, 60000);
            log("Passed");
        } catch (Exception e) {        	
            log("Failed: unexpected error " + e.getMessage());
        	assertTrue(false);
        }
    }

    protected void log(String s) {
        System.out.println("------------------------------ " + s);
    }
}
