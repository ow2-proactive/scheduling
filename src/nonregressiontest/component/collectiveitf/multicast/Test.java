package nonregressiontest.component.collectiveitf.multicast;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;

import nonregressiontest.component.ComponentTest;


public class Test extends ComponentTest {
    
    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;


    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
    public void action() throws Exception {

        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        Component testcase = (Component) f.newComponent("nonregressiontest.component.collectiveitf.multicast.testcase",context);
        
        Fractal.getLifeCycleController(testcase).startFc();
        ((Tester)testcase.getFcInterface("runTestItf")).testConnectedServerMulticastItf();
        ((Tester)testcase.getFcInterface("runTestItf")).testOwnClientMulticastItf();
        
    }

    /*
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
    public void endTest() throws Exception {

        // TODO Auto-generated method stub
        
    }

    /*
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
    public void initTest() throws Exception {

        // TODO Auto-generated method stub
        
    }
    
    public static void main(String[] args) {
        System.setProperty("fractal.provider", "org.objectweb.proactive.core.component.Fractive");
        System.setProperty("java.security.policy", System.getProperty("user.dir")+"/proactive.java.policy");
        System.setProperty("log4j.configuration", System.getProperty("user.dir")+"/proactive-log4j");
        System.setProperty("log4j.configuration", "file:" + System.getProperty("user.dir")+"/proactive-log4j");
        System.setProperty("nonregressiontest.descriptor.defaultnodes.file", "/nonregressiontest/descriptor/defaultnodes/NodesLocal.xml");
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions()) {
                System.out.println("TEST SUCCEEDED");
            } else {
                System.out.println("TEST FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
