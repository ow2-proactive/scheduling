package nonregressiontest.descriptor.variablecontract.javaproperties;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;

import testsuite.test.FunctionalTest;

public class Test extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/variablecontract/javaproperties/Test.xml").getPath();
	ProActiveDescriptor pad;
	
	boolean bogusFromProgram, bogusFromDescriptor;
	
	public Test() {
		super("Variable Contract: JavaProperties",
				"Tests conditions for variables of type JavaProperties");
	}

	public boolean postConditions() throws Exception {

		VariableContract variableContract=pad.getVariableContract();
		//System.out.println(variableContract);
		
		return !bogusFromProgram &&
				!bogusFromDescriptor &&
				variableContract.getValue("user_home").equals(System.getProperty("user.home")) &&
				variableContract.getValue("user_dir").equals(System.getProperty("user.dir")) &&
				variableContract.getValue("user_name").equals(System.getProperty("user.name")) &&
				variableContract.isClosed();
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
		map.put("user_home", "user.home");
		variableContract.setVariableFromProgram(map, VariableContractType.getType("JavaPropertyVariable"));
		
		//Setting Bogus from program
		try{
			variableContract.setVariableFromProgram("bogus_property", "bogus.property", VariableContractType.getType("JavaPropertyVariable"));
		}catch (Exception e){
			bogusFromProgram=false;
		}
		
		//Setting from Descriptor
		variableContract.setDescriptorVariable("user_dir", "user.dir", VariableContractType.getType("JavaPropertyVariable"));
		//Setting bogus from program
		try{
			variableContract.setDescriptorVariable("bogus_property", "bogus.property", VariableContractType.getType("JavaPropertyVariable"));
		}catch (Exception e){
			bogusFromDescriptor=false;
		}

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
