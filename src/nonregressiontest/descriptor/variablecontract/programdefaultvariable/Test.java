package nonregressiontest.descriptor.variablecontract.programdefaultvariable;

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
			"/nonregressiontest/descriptor/variablecontract/programdefaultvariable/Test.xml").getPath();
	ProActiveDescriptor pad;
	
	boolean bogusFromDescriptor, bogusFromProgram;
	
	public Test() {
		super("Variable Contract: ProgramDefaultVariable",
				"Tests conditions for variables of type ProgramDefaultVariable");
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
		
		//Setting from Program
		HashMap map = new HashMap();
		map.put("test_var1", "value1");
		variableContract.setVariableFromProgram(map, VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_DEFAULT_TAG));
				
		//Setting bogus from Program (this should fail)
		try{
			variableContract.setVariableFromProgram("test_empty", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_DEFAULT_TAG));
		}catch (Exception e){
			bogusFromProgram=false;
		}
		
		//Setting from Program
		variableContract.setDescriptorVariable("test_var2", "value2a", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_DEFAULT_TAG));
		//The following value should not be set, because Program is default and therefore has lower priority
		variableContract.setVariableFromProgram("test_var2", "value2b", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_DEFAULT_TAG));
				
		//Setting bogus variable from Descriptor (this should fail)
		try{
			variableContract.setDescriptorVariable("bogus_from_descriptor", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_PROGRAM_DEFAULT_TAG));
		}catch (Exception e){
			bogusFromDescriptor=false;
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
