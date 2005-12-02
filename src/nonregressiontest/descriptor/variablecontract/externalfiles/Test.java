package nonregressiontest.descriptor.variablecontract.externalfiles;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.VariableContract;

import testsuite.test.FunctionalTest;

public class Test extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/variablecontract/externalfiles/Test.xml").getPath();
	ProActiveDescriptor pad;
	
	boolean bogusFromDescriptor, bogusFromProgram;
	
	public Test() {
		super("Variable Contract: External Files",
				"Tests conditions for external files");
	}

	public boolean postConditions() throws Exception {

		VariableContract variableContract=pad.getVariableContract();
		
		//System.out.println(variableContract);
		
		return 
				variableContract.getValue("test_var0").equals("value0") &&
				variableContract.getValue("test_var1").equals("value1") &&
				variableContract.getValue("test_var2").equals("value2") &&
				variableContract.getValue("test_var3").equals("value3") &&
				variableContract.getValue("test_var4").equals("value4") &&
				variableContract.getValue("test_var5").equals("value5") &&
				variableContract.getValue("test_var6").equals("value6") &&
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
		
		/*
		//Setting from Program
		HashMap map = new HashMap();
		map.put("test_var1", "value1");
		variableContract.setVariableFromProgram(map, XMLPropertiesType.getType("ProgramDefaultVariable"));
				
		//Setting bogus from Program (this should fail)
		try{
			variableContract.setVariableFromProgram("test_empty", "", XMLPropertiesType.getType("ProgramDefaultVariable"));
		}catch (Exception e){
			bogusFromProgram=false;
		}
		
		//Setting from Program
		variableContract.setDescriptorVariable("test_var2", "value2a", XMLPropertiesType.getType("ProgramDefaultVariable"));
		//The following value should not be set, because Program is default and therefore has lower priority
		variableContract.setVariableFromProgram("test_var2", "value2b", XMLPropertiesType.getType("ProgramDefaultVariable"));
				
		//Setting bogus variable from Descriptor (this should fail)
		try{
			variableContract.setDescriptorVariable("bogus_from_descriptor", "", XMLPropertiesType.getType("ProgramDefaultVariable"));
		}catch (Exception e){
			bogusFromDescriptor=false;
		}
		
		//test_var3=value3
		*/
		
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
