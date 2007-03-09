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
package nonregressiontest.descriptor.mistakes;

import java.io.File;
import java.net.URL;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.VariableContract;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;


public class Test extends FunctionalTest {
    private static String TESTMISTAKES_XML_LOCATION_UNIX = Test.class.getResource(
            "/nonregressiontest/descriptor/mistakes/testMistakes.xml").getPath();
    private static String TESTVARIABLES_XML_LOCATION_UNIX = Test.class.getResource(
            "/nonregressiontest/descriptor/mistakes/testVariables.xml").getPath();
    ProActiveDescriptor pad;
    ProActiveDescriptor pad1;
    boolean testSuccess = true;

    /** ProActive compulsory no-args constructor */
    public Test() {
        super("Variable support and mistakes in deployment descriptor",
            "Test service: variable support and mistakes in deployment descriptor");
    }

    /*
     * (non-Javadoc)
     *
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        // We try to parse an XML Deployment Descriptor with mistakes, an
        // exception must be thrown
        try {
            pad = ProActive.getProactiveDescriptor(TESTMISTAKES_XML_LOCATION_UNIX);
            testSuccess = false;
        } catch (Exception e) {
            // Mistake found as expected
            super.getLogger()
                 .debug("Message found as expected\n" + e.getMessage());
        }

        if (pad != null) {
            pad.killall(false);
        }

        // We now parse an XML Deployment Descriptor with variables
        // The preceding test resulted in an error during the parsing, if you
        // encounter an endless loop here,
        // it means that the lock of the Variable Contract was not properly
        // released.
        VariableContract variableContract = new VariableContract();
        try {
            pad1 = ProActive.getProactiveDescriptor(TESTVARIABLES_XML_LOCATION_UNIX,
                    variableContract);
            // Descriptor parsed witout mistakes
        } catch (Exception e) {
            // Mistake found but not expected
            testSuccess = false;
            throw e;
        }

        if (pad1 != null) {
            pad1.killall(false);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
    }

    /*
     * (non-Javadoc)
     *
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
	public void endTest() throws Exception {
    }

    @Override
	public boolean postConditions() throws Exception {
        return testSuccess;
    }

    public static void main(String[] args) {
        Test test = new Test();

        try {
            test.action();
            test.endTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
