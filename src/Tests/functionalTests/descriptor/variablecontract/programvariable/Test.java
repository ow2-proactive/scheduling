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
package functionalTests.descriptor.variablecontract.programvariable;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.legacyparser.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Tests conditions for variables of type ProgramVariable
 */
public class Test extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static String XML_LOCATION = Test.class.getResource(
            "/functionalTests/descriptor/variablecontract/programvariable/Test.xml")
                                                   .getPath();
    ProActiveDescriptor pad;
    boolean bogusFromDescriptor;
    boolean bogusFromProgram;
    boolean bogusCheckContract;

    @Before
    public void initTest() throws Exception {
        bogusFromDescriptor = true;
        bogusCheckContract = true;
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
        VariableContract variableContract = new VariableContract();

        //Setting from Program
        HashMap map = new HashMap();
        map.put("test_var", "helloworld");
        variableContract.setVariableFromProgram(map,
            VariableContractType.getType(
                ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));

        //Setting bogus empty variable from Program (this should fail)
        try {
            variableContract.setVariableFromProgram("bogus_from_program", "",
                VariableContractType.getType(
                    ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));
        } catch (Exception e) {
            bogusFromProgram = false;
        }

        //Setting from Descriptor
        variableContract.setDescriptorVariable("force_prog_set", "",
            VariableContractType.getType(
                ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));
        bogusCheckContract = variableContract.checkContract(); //Contract should fail (return false)
                                                               //Now it should be ok

        variableContract.setVariableFromProgram("force_prog_set",
            "forcedhelloworld",
            VariableContractType.getType(
                ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));

        //Setting bogus from descriptor (this should fail)
        try {
            variableContract.setDescriptorVariable("nonempty", "non_empty",
                VariableContractType.getType(
                    ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));
        } catch (Exception e) {
            bogusFromDescriptor = false;
        }

        variableContract.setVariableFromProgram("forcedFromDesc",
            "forcedhelloworldFromDesc",
            VariableContractType.getType(
                ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));

        pad = ProDeployment.getProactiveDescriptor(XML_LOCATION,
                variableContract);

        variableContract = pad.getVariableContract();

        //System.out.println(variableContract);
        assertTrue(!bogusCheckContract);
        assertTrue(!bogusFromDescriptor);
        assertTrue(!bogusFromProgram);
        assertTrue(variableContract.getValue("test_var").equals("helloworld"));
        assertTrue(variableContract.getValue("force_prog_set")
                                   .equals("forcedhelloworld"));
        assertTrue(variableContract.getValue("forcedFromDesc")
                                   .equals("forcedhelloworldFromDesc"));
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
