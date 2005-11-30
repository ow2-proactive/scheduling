/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package nonregressiontest.activeobject.creation.parallel;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

import nonregressiontest.activeobject.creation.A;

import testsuite.test.FunctionalTest;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Nov 8, 2005
 */
public class TestArrayOfArray extends FunctionalTest {
    private static final String XML_PATH = TestVnNotActivated.class.getResource(
            "/nonregressiontest/activeobject/creation/parallel/4_local.xml")
                                                                   .getPath();
    private A[] aos;
    private VirtualNode vn;

    public TestArrayOfArray() {
        super("newActiveInParallel with an array of params",
            "Test newActiveInParallel method" +
            " with an array for constructor parameters");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        try {
            this.aos = (A[]) ProActive.newActiveInParallel(A.class.getName(),
                    new Object[][] {
                        { "toto" },
                        { "tata" }
                    }, vn.getNodes());
        } catch (Exception e) {
            this.aos = (A[]) ProActive.newActiveInParallel(A.class.getName(),
                    new Object[][] {
                        { "toto" },
                        { "tata" },
                        { "titi" },
                        { "tutu" }
                    }, vn.getNodes());
            return;
        }
        throw new Exception(
            "The total of constructors must be equal to the total of nodes" +
            " NOT VERIFIED");
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        ProActiveDescriptor padForActiving = ProActive.getProactiveDescriptor(XML_PATH);
        this.vn = padForActiving.getVirtualNode("Workers03");
        this.vn.activate();
    }

    public boolean postConditions() throws Exception {
        if ((this.aos == null) || (this.aos.length != 4)) {
            this.vn.killAll(false);
            return false;
        }
        if (aos[0].getName().compareTo("toto") != 0) {
            this.vn.killAll(false);
            return false;
        }
        if (aos[1].getName().compareTo("tata") != 0) {
            this.vn.killAll(false);
            return false;
        }
        if (aos[2].getName().compareTo("titi") != 0) {
            this.vn.killAll(false);
            return false;
        }
        if (aos[3].getName().compareTo("tutu") != 0) {
            this.vn.killAll(false);
            return false;
        }
        this.vn.killAll(false);
        return true;
    }

    public void endTest() throws Exception {
    }
}
