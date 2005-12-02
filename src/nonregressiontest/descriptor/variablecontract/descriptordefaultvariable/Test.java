package nonregressiontest.descriptor.variablecontract.descriptordefaultvariable;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;

import testsuite.test.FunctionalTest;

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

	public void initTest() throws Exception {
		bogusFromDescriptor=true;
		bogusFromProgram=true;
	}
	
	public void endTest() throws Exception {
		
		if (pad != null) {
			pad.killall(false);
		}
	}

	public void action() throws Exception {
		
		VariableContract variableContract= new VariableContract();
		
		//Setting from Descriptor
		variableContract.setDescriptorVariable("test_var1", "value1", VariableContractType.getType("DescriptorDefaultVariable"));
				
		//Setting bogus from descriptor (this should fail)
		try{
			variableContract.setDescriptorVariable("test_empty", "", VariableContractType.getType("DescriptorDefaultVariable"));
		}catch (Exception e){
			bogusFromDescriptor=false;
		}
		
		//Setting from Program
		HashMap map = new HashMap();
		map.put("test_var2", "value2a");
		variableContract.setVariableFromProgram(map, VariableContractType.getType("DescriptorDefaultVariable"));
		//The following value should not be set, because Descriptor is default and therefore has lower priority
		variableContract.setDescriptorVariable("test_var2", "value2b", VariableContractType.getType("DescriptorDefaultVariable"));
				
		//Setting bogus variable from Program (this should fail)
		try{
			variableContract.setVariableFromProgram("bogus_from_program", "", VariableContractType.getType("DescriptorDefaultVariable"));
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
