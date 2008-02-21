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
package functionalTests.descriptor.variablecontract.descriptordefaultvariable;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.legacyparser.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;


/**
 * Tests conditions for variables of type DescriptorDefaultVariable
 */
public class Test extends FunctionalTest {
    private static String XML_LOCATION = Test.class.getResource(
            "/functionalTests/descriptor/variablecontract/descriptordefaultvariable/Test.xml").getPath();
    ProActiveDescriptor pad;
    boolean bogusFromDescriptor;
    boolean bogusFromProgram;

    @Before
    public void initTest() throws Exception {
        bogusFromDescriptor = true;
        bogusFromProgram = true;
    }

    @After
    public void endTest() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }
    }

    @org.junit.Test
    public void action() throws Exception {
        VariableContractImpl variableContract = new VariableContractImpl();

        //Setting from Descriptor
        variableContract.setDescriptorVariable("test_var1", "value1", VariableContractType
                .getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));

        //Setting bogus from descriptor (this should fail)
        try {
            variableContract.setDescriptorVariable("test_empty", "", VariableContractType
                    .getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
        } catch (Exception e) {
            bogusFromDescriptor = false;
        }

        //Setting from Program
        HashMap map = new HashMap();
        map.put("test_var2", "value2a");
        variableContract.setVariableFromProgram(map, VariableContractType
                .getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
        //The following value should not be set, because Descriptor is default and therefore has lower priority
        variableContract.setDescriptorVariable("test_var2", "value2b", VariableContractType
                .getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));

        //Setting bogus variable from Program (this should fail)
        try {
            variableContract.setVariableFromProgram("bogus_from_program", "", VariableContractType
                    .getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
        } catch (Exception e) {
            bogusFromProgram = false;
        }

        //test_var3=value3
        pad = PADeployment.getProactiveDescriptor(XML_LOCATION, variableContract);

        variableContract = (VariableContractImpl) pad.getVariableContract();

        //System.out.println(variableContract);
        assertTrue(!bogusFromDescriptor);
        assertTrue(!bogusFromProgram);
        assertTrue(variableContract.getValue("test_var1").equals("value1"));
        assertTrue(variableContract.getValue("test_var2").equals("value2a"));
        assertTrue(variableContract.getValue("test_var3").equals("value3"));
        assertTrue(variableContract.isClosed());
        assertTrue(variableContract.checkContract());
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Test test = new Test();
        try {
            System.out.println("InitTest");
            test.initTest();
            System.out.println("Action");
            test.action();
            System.out.println("postConditions");
            System.out.println("endTest");
            test.endTest();
            System.out.println("The end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
