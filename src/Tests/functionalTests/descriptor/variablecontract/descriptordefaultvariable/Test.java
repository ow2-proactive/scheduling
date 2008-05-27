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

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.objectweb.proactive.core.descriptor.legacyparser.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import functionalTests.FunctionalTest;


/**
 * Tests conditions for variables of type DescriptorDefaultVariable
 */
public class Test extends FunctionalTest {
    private static String XML_LOCATION = Test.class.getResource(
            "/functionalTests/descriptor/variablecontract/descriptordefaultvariable/Test.xml").getPath();
    GCMApplication gcma;
    boolean bogusFromDescriptor;
    boolean bogusFromProgram;

    @Before
    public void initTest() throws Exception {
        bogusFromDescriptor = true;
        bogusFromProgram = true;
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
        HashMap<String, String> map = new HashMap<String, String>();
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
        gcma = PAGCMDeployment.loadApplicationDescriptor(new File(XML_LOCATION), variableContract);

        variableContract = (VariableContractImpl) gcma.getVariableContract();

        //System.out.println(variableContract);
        Assert.assertFalse(bogusFromDescriptor);
        Assert.assertFalse(bogusFromProgram);
        Assert.assertEquals("value1", variableContract.getValue("test_var1"));
        Assert.assertEquals("value2a", variableContract.getValue("test_var2"));
        Assert.assertEquals("value3", variableContract.getValue("test_var3"));
        assertTrue(variableContract.isClosed());
        assertTrue(variableContract.checkContract());
    }
}
