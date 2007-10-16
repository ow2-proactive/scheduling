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
package functionalTests.descriptor.mistakes;

import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.VariableContract;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test service: variable support and mistakes in deployment descriptor
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = 8303982699999215955L;
    private static String TESTMISTAKES_XML_LOCATION_UNIX = Test.class.getResource(
            "/functionalTests/descriptor/mistakes/testMistakes.xml").getPath();
    private static String TESTVARIABLES_XML_LOCATION_UNIX = Test.class.getResource(
            "/functionalTests/descriptor/mistakes/testVariables.xml").getPath();
    ProActiveDescriptor pad;
    ProActiveDescriptor pad1;
    boolean testSuccess = true;

    @org.junit.Test
    public void action() throws Exception {
        // We try to parse an XML Deployment Descriptor with mistakes, an
        // exception must be thrown
        try {
            pad = ProDeployment.getProactiveDescriptor(TESTMISTAKES_XML_LOCATION_UNIX);
            testSuccess = false;
        } catch (Exception e) {
            // Mistake found as expected
            //            super.getLogger()
            //                 .debug("Message found as expected\n" + e.getMessage());
        }

        if (pad != null) {
            pad.killall(false);
        }

        assertTrue(testSuccess);

        // We now parse an XML Deployment Descriptor with variables
        // The preceding test resulted in an error during the parsing, if you
        // encounter an endless loop here,
        // it means that the lock of the Variable Contract was not properly
        // released.
        VariableContract variableContract = new VariableContract();
        try {
            pad1 = ProDeployment.getProactiveDescriptor(TESTVARIABLES_XML_LOCATION_UNIX,
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

    public static void main(String[] args) {
        Test test = new Test();

        try {
            test.action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
