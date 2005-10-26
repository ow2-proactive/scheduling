package nonregressiontest.descriptor.property;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.XMLProperties;

import testsuite.test.FunctionalTest;

public class TestFile extends FunctionalTest {
	static final long serialVersionUID = 1;

	private static String XML_LOCATION = Test.class.getResource(
			"/nonregressiontest/descriptor/property/TestProFile.xml").getPath();

	private static String PROPERTIES_FILE = Test.class.getResource(
			"/nonregressiontest/descriptor/property/TestFile.properties")
			.getPath();

	private static String XML_SUBFILE = Test.class.getResource(
			"/nonregressiontest/descriptor/property/TestSubFile.xml").getPath();

	private ProActiveDescriptor pad = null;

	private TestProperty property = null;

	public TestFile() {
		super("Properties from property files in deployment descriptors",
				"Properties from property files in deployment descriptors");
	}

	public boolean postConditions() throws Exception {
		if (property == null) {
			return false;
		}

		String control = property.getProperty("user_dir");
		if (!control.equalsIgnoreCase("test/usr/phenri/home/ProActive")) {
			return false;
		}

		control = property.getProperty("test_proac");
		if (!control.equalsIgnoreCase("/user/ProActive")) {
			return false;
		}

		control = property.getProperty("lisa.game");
		if (!control.equalsIgnoreCase("/usr/phenri/home/lisa/sims")) {
			return false;
		}

		control = property.getProperty("lib.home");
		if (!control.equalsIgnoreCase("/usr/phenri/home/bin")) {
			return false;
		}

		return true;
	}

	public void initTest() throws Exception {
	}

	public void endTest() throws Exception {
		if (pad != null) {
			pad.killall(true);
		}
	}

	public void action() throws Exception {

		// Test properties defined in sun properties file
		XMLProperties.clean();
		HashMap map = new HashMap();
		map.put("proac_home", "/user/ProActive");
		map.put("testfile", PROPERTIES_FILE);
		map.put("testSubFile", XML_SUBFILE);
		XMLProperties.setVariableValue(map, "setInProgram");

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

		if (postConditions() == false)
			throw new Exception(
					"Properties from property files in deployment descriptors");

		// Test properties in disjoint XML file
		XMLProperties.clean();
		map = new HashMap();
		map.put("proac_home", "/user/ProActive");
		map.put("testSubFile", XML_SUBFILE);
		XMLProperties.setVariableValue(map, "setInProgram");

		pad = ProActive.getProactiveDescriptor(XML_LOCATION);
		// Thread.sleep(1000);
		vnNode = pad.getVirtualNode("NodeTest");
		vnNode.activate();
		// Thread.sleep(1000);
		nodeTab = vnNode.getNodes();

		param = new Object[1];
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
