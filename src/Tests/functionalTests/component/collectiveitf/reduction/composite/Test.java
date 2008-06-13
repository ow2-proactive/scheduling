package functionalTests.component.collectiveitf.reduction.composite;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Ignore;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;

import functionalTests.ComponentTest;


public class Test extends ComponentTest {
    public Test() {
        super("Multicast reduction mixing composite and primitive components",
                "Multicast reduction mixing composite and primitive components");
    }

    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @Ignore
    @org.junit.Test
    public void action() throws Exception {
        try {
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();

            Map<String, Object> context = new HashMap<String, Object>();

            Component root = (Component) f.newComponent(
                    "functionalTests.component.collectiveitf.reduction.composite.adl.testcase", context);
            Fractal.getLifeCycleController(root).startFc();
            Reduction reductionItf = ((Reduction) root.getFcInterface("mcast"));

            IntWrapper rval = reductionItf.doIt();
            Assert.assertEquals(new IntWrapper(123), rval);

            rval = reductionItf.doItInt(new IntWrapper(321));
            Assert.assertEquals(new IntWrapper(123), rval);

            reductionItf.voidDoIt();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
