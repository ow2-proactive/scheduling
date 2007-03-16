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
package nonregressiontest.security.sessionkeyexchange;

import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;

import testsuite.test.FunctionalTest;


/**
 * @author arnaud
 *
 */
public class SecurityTestSessionKeyExchange extends FunctionalTest {
    private ProActiveSecurityManager psm = null;
    private boolean testSucceed = true;

    /**
     *
     */
    public SecurityTestSessionKeyExchange() {
        super("security setup - session key exchange  ",
            "Test if the session exchange works.");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        try {
            ProActiveSecurityManager psm1 = psm.generateSiblingCertificate(
                    "caller");
            TestSecurityEntity entity1 = new TestSecurityEntity(psm1);

            ProActiveSecurityManager psm2 = psm.generateSiblingCertificate(
                    "Callee");
            TestSecurityEntity entity2 = new TestSecurityEntity(psm2);

            entity1.initiateSession(1, entity2);
        } catch (Exception e) {
            e.printStackTrace();
            testSucceed = false;
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
        PolicyServer ps = ProActiveSecurityDescriptorHandler.createPolicyServer(
                "../src/nonregressiontest/security/applicationPolicy.xml");
        psm = new ProActiveSecurityManager(ps);
    }

    @Override
	public boolean postConditions() throws Exception {
        return testSucceed;
    }

    @Override
	public void endTest() throws Exception {
    }
}
