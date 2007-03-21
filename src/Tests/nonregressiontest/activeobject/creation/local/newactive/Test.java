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
package nonregressiontest.activeobject.creation.local.newactive;

import java.net.InetAddress;

import nonregressiontest.activeobject.creation.A;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.UrlBuilder;

import testsuite.test.FunctionalTest;


public class Test extends FunctionalTest {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8174997414009997078L;
	A a;
    String name;
    String nodeUrl;

    public Test() {
        super("local newActive",
            "Test newActive method on the local default node");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        a = (A) ProActive.newActive(A.class.getName(), new Object[] { "toto" });
        name = a.getName();
        nodeUrl = a.getNodeUrl();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
	public void endTest() throws Exception {
    }

    @Override
	public boolean postConditions() throws Exception {
        return (name.equals("toto") &&
        (nodeUrl.indexOf(UrlBuilder.getHostNameorIP(InetAddress.getLocalHost())) != -1));
    }
}
