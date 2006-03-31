package nonregressiontest.descriptor.variablecontract.javapropertiesDescriptor;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;

public class Test extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/variablecontract/javapropertiesDescriptor/Test.xml").getPath();
	ProActiveDescriptor pad;
	
	boolean bogusFromProgram, bogusFromDescriptor;
	
	public Test() {
		super("Variable Contract: JavaPropertiesDescriptor",
				"Tests conditions for variables of type JavaPropertiesDescriptor");
	}

	public boolean postConditions() throws Exception {
	
		return true;
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
		map.put("user.home", "/home/userprogram");
		variableContract.setVariableFromProgram(map, VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
		variableContract.setVariableFromProgram("bogus.property", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
		
		Assertions.assertTrue( variableContract.getValue("user.home").equals("/home/userprogram")); 
		
		//Setting from Descriptor
		variableContract.setDescriptorVariable("user.home", "/home/userdesc", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
		Assertions.assertTrue( variableContract.getValue("user.home").equals("/home/userdesc"));
		
		//Setting bogus from program
		boolean bogus=false;
		try{
			variableContract.setDescriptorVariable("bogus.property", "", VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
			bogus=true;//shouldn't reach this line
		}catch (Exception e){
		}
		Assertions.assertTrue(!bogus);

		pad = ProActive.getProactiveDescriptor(XML_LOCATION, variableContract);
		variableContract = pad.getVariableContract();
		Assertions.assertTrue(variableContract.getValue("user.home").equals(System.getProperty("user.home")));
		
		Assertions.assertTrue(variableContract.isClosed());
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
