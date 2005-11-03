package nonregressiontest.component.nfpriority;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.ProActive;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.Message;
import nonregressiontest.component.Setup;


/**
 * @author Matthieu Morel
 */
public class Test extends ComponentTest {
    private static final int NB_WRAPPERS = 5;
    private Message result1;
    private Message result2;
    private Message result3;
    private Message result4;
    private Message result5;
    private Message result6;
    private final String expectedResult = "foo-->a-->b-->a";
    private Component systemWithWrappingWithShortcuts;
    private Component systemWithWrappingWithoutShortcuts;
    private Component systemWithoutWrapping;

    public Test() {
        super("Components : Prioritized non functional requests on components",
            "Components : Prioritized non functional requests on components");
    }

    public void action() throws Exception {
        Component a = Setup.createSlowPrimitiveA();
        Component c1 = Setup.createCompositeA();
        Component b = Setup.createRemoteSlowPrimitiveB();
        Component c2 = Setup.createCompositeB1();
        Fractal.getContentController(c2).addFcSubComponent(a);
        Fractal.getContentController(c2).addFcSubComponent(b);
        // try a
    }

    private void initializeComponentSystems() throws Exception {
        // system without wrapped components
        Component unwrappedA = Setup.createPrimitiveA();
        Component unwrappedB = Setup.createPrimitiveB1();
        Fractal.getBindingController(unwrappedA).bindFc("i2",
            unwrappedB.getFcInterface("i2"));
        Fractal.getLifeCycleController(unwrappedA).startFc();
        Fractal.getLifeCycleController(unwrappedB).startFc();
        systemWithoutWrapping = unwrappedA;

        // system with wrapping but without shortcuts
        Component wrappedAWithoutShortcuts = Setup.createPrimitiveA();
        for (int i = 0; i < NB_WRAPPERS; i++) {
            wrappedAWithoutShortcuts = wrapWithCompositeOfTypeA(NB_WRAPPERS -
                    i, wrappedAWithoutShortcuts);
        }

        Component wrappedBWithoutShortcuts = Setup.createPrimitiveB1();
        for (int i = 0; i < NB_WRAPPERS; i++) {
            wrappedBWithoutShortcuts = wrapWithCompositeOfTypeB(NB_WRAPPERS -
                    i, wrappedBWithoutShortcuts);
        }

        Fractal.getBindingController(wrappedAWithoutShortcuts).bindFc("i2",
            wrappedBWithoutShortcuts.getFcInterface("i2"));
        Fractal.getLifeCycleController(wrappedAWithoutShortcuts).startFc();
        Fractal.getLifeCycleController(wrappedBWithoutShortcuts).startFc();

        systemWithWrappingWithoutShortcuts = wrappedAWithoutShortcuts;

        // system with wrapping and with shortcuts
        Component wrappedAWithShortcuts = Setup.createPrimitiveA();
        for (int i = 0; i < NB_WRAPPERS; i++) {
            wrappedAWithShortcuts = wrapWithSynchronousCompositeOfTypeA(NB_WRAPPERS -
                    i, wrappedAWithShortcuts);
        }

        Component wrappedBWithShortcuts = Setup.createPrimitiveB1();
        for (int i = 0; i < NB_WRAPPERS; i++) {
            wrappedBWithShortcuts = wrapWithSynchronousCompositeOfTypeB(NB_WRAPPERS -
                    i, wrappedBWithShortcuts);
        }

        Fractal.getBindingController(wrappedAWithShortcuts).bindFc("i2",
            wrappedBWithShortcuts.getFcInterface("i2"));
        Fractal.getLifeCycleController(wrappedAWithShortcuts).startFc();
        Fractal.getLifeCycleController(wrappedBWithShortcuts).startFc();

        systemWithWrappingWithShortcuts = wrappedAWithShortcuts;
    }

    private void resetComponentSystem()
        throws IllegalContentException, IllegalLifeCycleException, 
            NoSuchInterfaceException, IllegalBindingException {
        // TODO_M change the inner wrapped components and check the shortcut is aware of the reconfiguration
    }

    private Component wrapWithSynchronousCompositeOfTypeB(int index,
        Component wrappee) throws Exception {
        Component wrapper = Setup.createSynchronousCompositeOfTypeB(
                "sync_composite_b" + index);
        Fractal.getContentController(wrapper).addFcSubComponent(wrappee);
        Fractal.getBindingController(wrapper).bindFc("i2",
            wrappee.getFcInterface("i2"));
        return wrapper;
    }

    private Component wrapWithCompositeOfTypeB(int index, Component wrappee)
        throws Exception {
        Component wrapper = Setup.createCompositeOfTypeB("composite_b" + index);
        Fractal.getContentController(wrapper).addFcSubComponent(wrappee);
        Fractal.getBindingController(wrapper).bindFc("i2",
            wrappee.getFcInterface("i2"));
        return wrapper;
    }

    private Component wrapWithSynchronousCompositeOfTypeA(int index,
        Component wrappee) throws Exception {
        Component wrapper = Setup.createSynchronousCompositeOfTypeA(
                "sync_composite_a" + index);
        Fractal.getContentController(wrapper).addFcSubComponent(wrappee);
        Fractal.getBindingController(wrapper).bindFc("i1",
            wrappee.getFcInterface("i1"));
        Fractal.getBindingController(wrappee).bindFc("i2",
            wrapper.getFcInterface("i2"));
        return wrapper;
    }

    private Component wrapWithCompositeOfTypeA(int index, Component wrappee)
        throws Exception {
        Component wrapper = Setup.createCompositeOfTypeA("composite_a" + index);
        Fractal.getContentController(wrapper).addFcSubComponent(wrappee);
        Fractal.getBindingController(wrapper).bindFc("i1",
            wrappee.getFcInterface("i1"));
        Fractal.getBindingController(wrappee).bindFc("i2",
            wrapper.getFcInterface("i2"));
        return wrapper;
    }

    public void initTest() throws Exception {
        System.setProperty("proactive.components.use_shortcuts", "true");
    }

    public void endTest() throws Exception {
        System.setProperty("proactive.components.use_shortcuts", "false");
    }

    public static void main(String[] args) {
        try {
            Test test = new Test();
            test.initTest();
            test.action();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * @see testsuite.test.AbstractTest#postConditions()
     */
    public boolean postConditions() throws Exception {
        return (expectedResult.equals(((Message) ProActive.getFutureValue(
                result4)).getMessage()) &&
        expectedResult.equals(((Message) ProActive.getFutureValue(result3)).getMessage()) &&
        expectedResult.equals(((Message) ProActive.getFutureValue(result2)).getMessage()) &&
        expectedResult.equals(ProActive.getFutureValue(
                ((Message) ProActive.getFutureValue(result1)).getMessage())));
    }
}
