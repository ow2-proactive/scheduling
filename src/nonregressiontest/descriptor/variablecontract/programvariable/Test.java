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
package nonregressiontest.descriptor.variablecontract.programvariable;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;

import testsuite.test.FunctionalTest;

public class Test extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/variablecontract/programvariable/Test.xml").getPath();
	ProActiveDescriptor pad;
	
	boolean bogusFromDescriptor, bogusFromProgram, bogusCheckContract;
	
	public Test() {
		super("Variable Contract: ProgramVariable",
				"Tests conditions for variables of type ProgramVariable");
	}

	public boolean postConditions() throws Exception {

		VariableContract variableContract=pad.getVariableContract();
		
		//System.out.println(variableContract);
		
		return !bogusCheckContract &&
				!bogusFromDescriptor &&
				!bogusFromProgram &&
				variableContract.getValue("test_var").equals("helloworld") &&
				variableContract.getValue("force_prog_set").equals("forcedhelloworld") &&
				variableContract.getValue("forcedFromDesc").equals("forcedhelloworldFromDesc") &&
				variableContract.isClosed() &&
				variableContract.checkContract();
	}

	public void initTest() throws Exception {
		bogusFromDescriptor=true;
		bogusCheckContract=true;
		bogusFromProgram=true;
	}
	
	public void endTest() throws Exception {
		
		if (pad != null) {
			pad.killall(false);
		}
	}

	public void action() throws Exception {
		
		VariableContract variableContract= new VariableContract();
		
		//Setting from Program
		HashMap map = new HashMap();
		map.put("test_var", "helloworld");
		variableContract.setVariableFromProgram(map, VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));
		
		//Setting bogus empty variable from Program (this should fail)
		try{
			variableContract.setVariableFromProgram("bogus_from_program", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));
		}catch (Exception e){
			bogusFromProgram=false;
		}
		
		//Setting from Descriptor
		variableContract.setDescriptorVariable("force_prog_set", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));
		bogusCheckContract=variableContract.checkContract(); //Contract should fail (return false)
		//Now it should be ok
		variableContract.setVariableFromProgram("force_prog_set", "forcedhelloworld", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));
		
		//Setting bogus from descriptor (this should fail)
		try{
			variableContract.setDescriptorVariable("nonempty", "non_empty", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));
		}catch (Exception e){
			bogusFromDescriptor=false;
		}
		
		variableContract.setVariableFromProgram("forcedFromDesc", "forcedhelloworldFromDesc", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG));	
		
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
