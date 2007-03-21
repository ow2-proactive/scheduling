/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest.security;

import nonregressiontest.security.applicationlifecycle.SecurityTestApplicationLifeCycle;
import nonregressiontest.security.dynamicsecuritypropagation.SecurityTestContextPropagation;
import nonregressiontest.security.keygeneration.SecurityTestKeyGen;
import nonregressiontest.security.policyserver.SecurityTestPolicyServer;
import nonregressiontest.security.securitymanager.SecurityTestSecurityManager;
import nonregressiontest.security.sessionkeyexchange.SecurityTestSessionKeyExchange;
import testsuite.test.FunctionalTest;


public class SecurityTests extends FunctionalTest {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5833323104163731463L;
	private boolean testSucceed = true;

    public SecurityTests() {
        super("Security Tests");
    }

    @Override
	public void action() throws Exception {
        FunctionalTest test = null;

        test = new SecurityTestKeyGen();
        test.runTest();
        if (!test.postConditions()) {
            testSucceed = false;
            return;
        }

        new SecurityTestPolicyServer().runTest();
        new SecurityTestSecurityManager().runTest();
        new SecurityTestApplicationLifeCycle().runTest();
        new SecurityTestContextPropagation().runTest();
        new SecurityTestSessionKeyExchange().runTest();
    }

    @Override
	public void initTest() throws Exception {
    }

    @Override
	public void endTest() throws Exception {
    }

    @Override
	public boolean postConditions() throws Exception {
        return testSucceed;
    }
}
