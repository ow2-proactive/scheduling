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
package nonregressiontest.descriptor.variablecontract.descriptordefaultvariable;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;

import testsuite.test.FunctionalTest;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorConstants;

public class Test extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/variablecontract/descriptordefaultvariable/Test.xml").getPath();
	ProActiveDescriptor pad;
	
	boolean bogusFromDescriptor, bogusFromProgram;
	
	public Test() {
		super("Variable Contract: DescriptorDefaultVariable",
				"Tests conditions for variables of type DescriptorDefaultVariable");
	}

	@Override
	public boolean postConditions() throws Exception {

		VariableContract variableContract=pad.getVariableContract();
		
		//System.out.println(variableContract);
		
		return 
				!bogusFromDescriptor &&
				!bogusFromProgram &&
				variableContract.getValue("test_var1").equals("value1") &&
				variableContract.getValue("test_var2").equals("value2a") &&
				variableContract.getValue("test_var3").equals("value3") &&
				variableContract.isClosed() &&
				variableContract.checkContract();
	}

	@Override
	public void initTest() throws Exception {
		bogusFromDescriptor=true;
		bogusFromProgram=true;
	}
	
	@Override
	public void endTest() throws Exception {
		
		if (pad != null) {
			pad.killall(false);
		}
	}

	@Override
	public void action() throws Exception {
		
		VariableContract variableContract= new VariableContract();
		
		//Setting from Descriptor
		variableContract.setDescriptorVariable("test_var1", "value1", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
				
		//Setting bogus from descriptor (this should fail)
		try{
			variableContract.setDescriptorVariable("test_empty", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
		}catch (Exception e){
			bogusFromDescriptor=false;
		}
		
		//Setting from Program
		HashMap map = new HashMap();
		map.put("test_var2", "value2a");
		variableContract.setVariableFromProgram(map, VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
		//The following value should not be set, because Descriptor is default and therefore has lower priority
		variableContract.setDescriptorVariable("test_var2", "value2b", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
				
		//Setting bogus variable from Program (this should fail)
		try{
			variableContract.setVariableFromProgram("bogus_from_program", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
		}catch (Exception e){
			bogusFromProgram=false;
		}
		
		//test_var3=value3
		pad = ProActive.getProactiveDescriptor(XML_LOCATION, variableContract);

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
			System.out.println("Result="+test.postConditions());
			System.out.println("endTest");
			test.endTest();
			System.out.println("The end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
