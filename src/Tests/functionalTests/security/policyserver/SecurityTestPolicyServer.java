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
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.security.policyserver;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertNotNull;


/**
 * Test if the policy server is able to load a policy file, to be serialized and unserialized
 *
 * @author arnaud
 *
 */
public class SecurityTestPolicyServer extends FunctionalTest {
    private PolicyServer policyServer = null;
    private PolicyServer ps = null;

    @Test
    public void action() throws Exception {
        // retrieve policyserver
        ps = (PolicyServer) MakeDeepCopy.WithObjectStream.makeDeepCopy(policyServer);
        assertNotNull(ps);
    }

    @Before
    public void initTest() throws Exception {
        policyServer = ProActiveSecurityDescriptorHandler.createPolicyServer(SecurityTestPolicyServer.class
                .getResource("/functionalTests/security/applicationPolicy.xml").getPath());
    }
}
