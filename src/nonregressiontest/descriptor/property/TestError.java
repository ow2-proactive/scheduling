package nonregressiontest.descriptor.property;

import java.util.HashMap;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.XMLProperties;

import testsuite.test.FunctionalTest;

public class TestError extends FunctionalTest {
	static final long serialVersionUID = 1;
    private static String XML_LOCATION = TestError.class.getResource(
    "/nonregressiontest/descriptor/property/Test.xml").getPath();
    private static String XML_LOCATION_1 = TestError.class.getResource(
    "/nonregressiontest/descriptor/property/Test_1.xml").getPath();
    private static String XML_LOCATION_2 = TestError.class.getResource(
    "/nonregressiontest/descriptor/property/Test_2.xml").getPath();
//    private static String XML_LOCATION = "/user/phenri/home/ProActive/src/nonregressiontest/descriptor/property/Test.xml";

	private ProActiveDescriptor pad = null;
    private boolean	programSetNotDefine = true;
    private boolean	programSetViolation= true;
    private boolean	descriptorConstant= true;
    private boolean	descriptorOverridable= true;
	
    public TestError() { 
        super("Simple properties in deployment descriptors - type verifications","Simple properties in deployment descriptors - type verifications");
    }

    public boolean postConditions() throws Exception {
    	
		if ( programSetNotDefine) return false;
		if ( programSetViolation) return false;
		if ( descriptorConstant) return false;
		if ( descriptorOverridable) return false;
		
		return true;
    }
	
	public void initTest() throws Exception {
	}

	public void endTest() throws Exception {
		if ( pad != null) pad.killall( true);
	}

	public void action() throws Exception {

		// Generate exception 
		// Using programset descriptor not define !
		try {
			pad = ProActive.getProactiveDescriptor( XML_LOCATION);
	    } catch (ProActiveException epa) {
			if ( pad != null) pad.killall( true);
			programSetNotDefine = false;
	    } catch (Exception e) {
			if ( pad != null) pad.killall( true);
			programSetNotDefine = false;
	    }

		// Generate exception 
		// Using definition of property both in program and descriptor !
		try {
			XMLProperties.clean();
			HashMap map = new HashMap();
			map.put("proac_home","/user/ProActive");
			map.put("game","/user/ProActive");
			XMLProperties.setVariableValue( map);
			
			pad = ProActive.getProactiveDescriptor( XML_LOCATION);
	    } catch (ProActiveException epa) {
			if ( pad != null) pad.killall( true);
			programSetViolation = false;
		} catch ( SAXException ex) {
			System.out.println("org.xml.sax.SAXException ");
			if ( pad != null) pad.killall( true);
			programSetViolation = false; 
	    } catch (Exception e) {
			if ( pad != null) pad.killall( true);
			programSetViolation = false;
	    }
		
		// Generate exception 
		// Redefine constant property  !
		try {
			pad = ProActive.getProactiveDescriptor( XML_LOCATION_1);
	    } catch (ProActiveException epa) {
			if ( pad != null) pad.killall( true);
			descriptorConstant = false;
	    } catch (Exception e) {
			if ( pad != null) pad.killall( true);
			descriptorConstant = false;
	    }

		// Generate exception 
		// Redefine overridable property to constant !
		try {
			pad = ProActive.getProactiveDescriptor( XML_LOCATION_2);
	    } catch (ProActiveException epa) {
			if ( pad != null) pad.killall( true);
			descriptorOverridable = false;
	    } catch (Exception e) {
			if ( pad != null) pad.killall( true);
			descriptorOverridable = false;
	    }

	}

	/**
	 * @param args
	 */
/*	public static void main(String[] args) {

		TestError test = new TestError();
        try {
			test.initTest();
            test.action();
			test.postConditions();
            test.endTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
*/
}
