/*
 * Created on Apr 22, 2004
 * author : Matthieu Morel
 */
package nonregressiontest.component.descriptor.arguments;

import nonregressiontest.component.ComponentTest;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;

import testsuite.test.Assertions;

import java.util.HashMap;
import java.util.Map;


/**
 * This test instantiates a component from the "dummy.fractal" definition, which is parameterized
 * with the "message" argument.
 * The "message" argument is then used to set the "info" attribute in the dummy component.
 * 
 * @author Matthieu Morel
 */
public class Test extends ComponentTest {
    Component dummy;

    public Test() {
        super("Configuration with ADL arguments and AttributeController",
            "Configuration with ADL arguments and AttributeController");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        context.put("message", "hello world");
        dummy = (Component) f.newComponent("nonregressiontest.component.descriptor.arguments.dummy",
                context);
        Fractal.getLifeCycleController(dummy).startFc();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        Assertions.assertEquals("This component is storing the info : hello world",
            ((Action) dummy.getFcInterface("action")).doSomething());
    }

    public boolean postConditions() throws Exception {
        return true;
    }
    
    private Component getDummy() {
        return dummy;
    }

    public static void main(String[] args) {
        Test test = new Test();
        System.setProperty("fractal.provider",
            "org.objectweb.proactive.core.component.Fractive");
        try {
            test.action();
            Component dummy = test.getDummy();
            System.out.println(((Action) dummy.getFcInterface("action")).doSomething());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //test.endTest();
                System.exit(0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
