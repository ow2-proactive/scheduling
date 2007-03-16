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
package nonregressiontest.security.dynamicsecuritypropagation;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;

import nonregressiontest.security.A;

import testsuite.test.FunctionalTest;


/**
 * @author arnaud
 *
 */
public class SecurityTestContextPropagation extends FunctionalTest {
    private ProActiveSecurityManager psm = null;
    private boolean isTestValide = false;

    /**
     *
     */
    public SecurityTestContextPropagation() {
        super("security setup - security context propagation ",
            "Test the dynamic propagation of an application context");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        A a = (A) ProActive.newActive("nonregressiontest.security.A",
                new Object[] {  });

        System.out.println(a.hello("arnaud"));
        isTestValide = true;
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
        PolicyServer ps = ProActiveSecurityDescriptorHandler.createPolicyServer(
                "../src/nonregressiontest/security/applicationPolicy.xml");
        psm = new ProActiveSecurityManager(ps);

        // set the default security manager
        ProActiveMetaObjectFactory.newInstance().setProActiveSecurityManager(psm);
    }

    @Override
	public boolean postConditions() throws Exception {
        return isTestValide;
    }

    @Override
	public void endTest() throws Exception {
    }
}
