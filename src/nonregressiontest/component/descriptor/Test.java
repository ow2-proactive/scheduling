/*
 * Created on Oct 23, 2003
 * author : Matthieu Morel
  */
package nonregressiontest.component.descriptor;

import nonregressiontest.component.I1;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;

import org.objectweb.fractal.api.Component;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractal;
import org.objectweb.proactive.core.component.xml.Loader;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;

import testsuite.test.FunctionalTest;


/**
 * @author Matthieu Morel
 */
public class Test extends FunctionalTest {
    private static String COMPONENTS_DESCRIPTOR_LOCATION = Test.class.getResource(
            "/nonregressiontest/component/descriptor/componentsDescriptor.xml")
                                                                     .getPath();
    private static String DEPLOYMENT_DESCRIPTOR_LOCATION = Test.class.getResource(
            "/nonregressiontest/component/descriptor/deploymentDescriptor.xml")
                                                                     .getPath();
    public static String MESSAGE = "-->Main";
    private Message message;

    //ComponentsCache componentsCache;
    ProActiveDescriptor deploymentDescriptor;

    public Test() {
        super("Components descriptor",
            "Test instantiation of a component system based on a components descriptor");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        System.setProperty("proactive.future.ac", "enable");
        // start a new thread so that automatic continuations are enabled for components
        ACThread acthread = new ACThread();
        acthread.start();
        acthread.join();
        System.setProperty("proactive.future.ac", "disable");
    }

    private class ACThread extends Thread {
        public void run() {
            try {
                // instantiate and deploy components
                deploymentDescriptor = ProActive.getProactiveDescriptor("file:" +
                        DEPLOYMENT_DESCRIPTOR_LOCATION);
                Loader component_loader = new Loader();
                component_loader.loadComponentsConfiguration("file:" +
                    COMPONENTS_DESCRIPTOR_LOCATION, deploymentDescriptor);
                // start components
                Component c = component_loader.getComponent("c");

                //System.out.println("name of c is : " + ((ComponentParametersController)c.getFcInterface(ComponentParametersController.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters().getName());
                Component p2 = component_loader.getComponent("p2");

                //System.out.println("name of p2 is : " + ((ComponentParametersController)p2.getFcInterface(ComponentParametersController.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters().getName());
                Fractal.getLifeCycleController(c).startFc();
                Fractal.getLifeCycleController(p2).startFc();

                // invoke method on composite
                I1 i1 = (I1) c.getFcInterface("i1");

                //I1 i1= (I1)p1.getFcInterface("i1");
                message = i1.processInputMessage(new Message(MESSAGE)).append(MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    deploymentDescriptor.killall();
                } catch (ProActiveException pae) {
                    pae.printStackTrace();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        deploymentDescriptor.killall();
    }

    public boolean postConditions() throws Exception {
        return (message.toString().equals(Test.MESSAGE +
            PrimitiveComponentA.MESSAGE + PrimitiveComponentB.MESSAGE +
            PrimitiveComponentA.MESSAGE + Test.MESSAGE));
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                test.endTest();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
