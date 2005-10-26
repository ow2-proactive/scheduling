package nonregressiontest.descriptor.property;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.XMLProperties;

import testsuite.test.FunctionalTest;

public class Test extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/property/Test.xml").getPath();

	private ProActiveDescriptor pad = null;

	private TestProperty property = null;

	public Test() {
		super("Simple properties in deployment descriptors",
				"Simple properties in deployment descriptors");
	}

	public boolean postConditions() throws Exception {
		if (property == null) {
			return false;
		}

		String control = property.getProperty("user_dir");
		if (!control.equals("test/usr/phenri/home/ProActive")) {
			return false;
		}

		control = property.getProperty("test_proac");
		if (!control.equals("/user/ProActive")) {
			return false;
		}

		control = property.getProperty("lisa.game");
		if (!control.equals("/usr/phenri/home/lisa/sims")) {
			return false;
		}

		control = property.getProperty("lib.home");
		if (!control.equals("/usr/phenri/home/bin")) {
			return false;
		}

		control = property.getProperty("test_user_d");
		String result = System.getProperty("user.name") + "/toto";
		if (!control.equals(result)) {
			return false;
		}

		return true;
	}

	public void initTest() throws Exception {
	}

	private String oldUserDir;
	
	public void endTest() throws Exception {
		System.setProperty("user.dir", oldUserDir);
		if (pad != null) {
			pad.killall(true);
		}
	}

	public void action() throws Exception {
		XMLProperties.clean();

		oldUserDir = System.getProperty("user.dir");
		System.setProperty("user.dir", "test_user_dir");

		HashMap map = new HashMap();
		map.put("proac_home", "/user/ProActive");
		XMLProperties.setVariableValue(map, "setInProgram");
		
		XMLProperties.setVariableValue( "game", "", "overridableInXML");

		pad = ProActive.getProactiveDescriptor(XML_LOCATION);
		// Thread.sleep(1000);
		VirtualNode vnNode = pad.getVirtualNode("NodeTest");
		vnNode.activate();
		// Thread.sleep(1000);
		Node[] nodeTab = vnNode.getNodes();

		Object[] param = new Object[1];
		param[0] = new String("Test user properties.");

		property = (TestProperty) ProActive.newActive(TestProperty.class
				.getName(), param, nodeTab[0]);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test test = new Test();
		try {
			test.initTest();
			test.action();
			test.postConditions();
			test.endTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
