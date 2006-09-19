package nonregressiontest.component.collectiveitf.gathercast;

import java.util.HashMap;
import java.util.Map;

import nonregressiontest.component.ComponentTest;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import testsuite.test.Assertions;


public class Test extends ComponentTest {
    
    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;
    public static final String VALUE_1 = "10";
    public static final String VALUE_2 = "20";

    public Test() {
        super("Gather interfaces",
            "Gather interfaces");
    }


    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
    public void action() throws Exception {

        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        Component testcase = (Component) f.newComponent("nonregressiontest.component.collectiveitf.gathercast.testcase",context);
//        Component clientB = (Component) f.newComponent("nonregressiontest.component.collectiveitf.gather.GatherClient("+VALUE_2+")",context);
//        Component server = (Component) f.newComponent("nonregressiontest.component.collectiveitf.gather.GatherServer",context);
//        Fractal.getBindingController(clientA).bindFc("client", server.getFcInterface("serverGather"));
//        Fractal.getBindingController(clientB).bindFc("client", server.getFcInterface("serverGather"));
        
        
        Fractal.getLifeCycleController(testcase).startFc();
        	
        for (int i=0; i<100; i++) {
        	// several iterations for thoroughly testing concurrency issues
        	BooleanWrapper result1 = (BooleanWrapper)((TestItf)testcase.getFcInterface("testA")).test();
        	BooleanWrapper result2 = (BooleanWrapper)((TestItf)testcase.getFcInterface("testB")).test();
        	
        	Assertions.assertTrue(result1.booleanValue());
        	Assertions.assertTrue(result2.booleanValue());
        }
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

    /*
     * @see testsuite.test.FunctionalTest#postConditions()
     */
    @Override
    public boolean postConditions() throws Exception {
        return true;
    }

}
