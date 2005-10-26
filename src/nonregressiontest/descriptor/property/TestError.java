package nonregressiontest.descriptor.property;

import java.util.HashMap;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.XMLProperties;
import org.xml.sax.SAXException;

import testsuite.test.FunctionalTest;


public class TestError extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static String XML_LOCATION = TestError.class.getResource(
            "/nonregressiontest/descriptor/property/Test.xml").getPath();
    private static String XML_LOCATION_1 = TestError.class.getResource(
            "/nonregressiontest/descriptor/property/Test_1.xml").getPath();
    private static String XML_LOCATION_2 = TestError.class.getResource(
            "/nonregressiontest/descriptor/property/Test_2.xml").getPath();
    private ProActiveDescriptor pad = null;
    private boolean programSetNotDefined = true;
    private boolean programSetViolation = true;
    private boolean descriptorConstant = true;
    private boolean descriptorOverridable = true;

    public TestError() {
        super("Simple properties in deployment descriptors - type verifications",
            "Simple properties in deployment descriptors - type verifications");
    }

    public boolean postConditions() throws Exception {
        if (programSetNotDefined) {
            System.out.println("Error in programSetNotDefined");
            return false;
        }
        if (programSetViolation) {
            System.out.println("Error in programSetViolation");
            return false;
        }
        if (descriptorConstant) {
            System.out.println("Error in descriptorConstant");
            return false;
        }
        if (descriptorOverridable) {
            System.out.println("Error in descriptorOverridable");
            return false;
        }

        return true;
    }

    public void initTest() throws Exception {
    }

    public void endTest() throws Exception {
    }

    public void action() throws Exception {
        XMLProperties.clean();
        // Generate exception 
        // Using programset descriptor not define !
        try {
            pad = ProActive.getProactiveDescriptor(XML_LOCATION);
            if (pad != null) {
                pad.killall(true);
            }
        } catch (ProActiveException epa) {
            if (pad != null) {
                pad.killall(true);
            }
            programSetNotDefined = false;
        } catch (Exception e) {
            if (pad != null) {
                pad.killall(true);
            }
            programSetNotDefined = false;
        }

        // Generate exception 
        // Using definition of property both in program and descriptor !
        try {
            XMLProperties.clean();
            HashMap map = new HashMap();
            map.put("proac_home", "/user/ProActive");
            map.put("game", "/user/ProActive");
            XMLProperties.setVariableValue(map, "setInProgram");

            pad = ProActive.getProactiveDescriptor(XML_LOCATION);
            if (pad != null) {
                pad.killall(true);
            }
        } catch (ProActiveException epa) {
            if (pad != null) {
                pad.killall(true);
            }
            programSetViolation = false;
        } catch (SAXException ex) {
            System.out.println("org.xml.sax.SAXException ");
            if (pad != null) {
                pad.killall(true);
            }
            programSetViolation = false;
        } catch (Exception e) {
            if (pad != null) {
                pad.killall(true);
            }
            programSetViolation = false;
        }

        // Generate exception 
        // Redefine constant property  !
        try {
            pad = ProActive.getProactiveDescriptor(XML_LOCATION_1);
            if (pad != null) {
                pad.killall(true);
            }
        } catch (ProActiveException epa) {
            if (pad != null) {
                pad.killall(true);
            }
            descriptorConstant = false;
        } catch (Exception e) {
            if (pad != null) {
                pad.killall(true);
            }
            descriptorConstant = false;
        }

        // Generate exception 
        // Redefine overridable property to constant !
        try {
            pad = ProActive.getProactiveDescriptor(XML_LOCATION_2);
            if (pad != null) {
                pad.killall(true);
            }
        } catch (ProActiveException epa) {
            if (pad != null) {
                pad.killall(true);
            }
            descriptorOverridable = false;
        } catch (Exception e) {
            if (pad != null) {
                pad.killall(true);
            }
            descriptorOverridable = false;
        }
    }

    /**
     * @param args
     */

    /*        public static void main(String[] args) {

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
