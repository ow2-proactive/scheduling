package nonregressiontest.descriptor.variablecontract.descriptorvariable;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorConstants;
import testsuite.test.FunctionalTest;

public class Test extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/variablecontract/descriptorvariable/Test.xml").getPath();
	ProActiveDescriptor pad;
	
	boolean bogusFromDescriptor, bogusFromProgram, bogusCheckContract;
	
	public Test() {
		super("Variable Contract: DescriptorVariable",
				"Tests conditions for variables of type DescriptorVariable");
	}

	public boolean postConditions() throws Exception {

		VariableContract variableContract=pad.getVariableContract();
		
		//System.out.println(variableContract);
		
		return !bogusCheckContract &&
				!bogusFromDescriptor &&
				!bogusFromProgram &&
				variableContract.getValue("test_var1").equals("value1") &&
				variableContract.getValue("test_var2").equals("value2") &&
				variableContract.getValue("test_var3").equals("value3") &&
				variableContract.getValue("test_var4").equals("value4") &&
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
		
		//Setting from Descriptor
		variableContract.setDescriptorVariable("test_var1", "value1", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_TAG));
				
		//Setting bogus from descriptor (this should fail)
		try{
			variableContract.setDescriptorVariable("test_empty", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_TAG));
		}catch (Exception e){
			bogusFromDescriptor=false;
		}
		
		//Setting from Program
		HashMap map = new HashMap();
		map.put("test_var2", "");
		variableContract.setVariableFromProgram(map, VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_TAG));
		
		bogusCheckContract=variableContract.checkContract(); //Contract should fail (return false)
		variableContract.setDescriptorVariable("test_var2", "value2", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_TAG));
		
		//Setting bogus variable from Program (this should fail)
		try{
			variableContract.setVariableFromProgram("bogus_from_program", "bogus_value", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_TAG));
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
