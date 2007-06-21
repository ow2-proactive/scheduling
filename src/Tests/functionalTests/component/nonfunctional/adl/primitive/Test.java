package functionalTests.component.nonfunctional.adl.primitive;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.representative.ProActiveNFComponentRepresentative;

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;


/**
 *
 *
 * @author Paul Naoumenko
 */
public class Test extends ComponentTest {
    Component root;

    public Test() {
        super("Basic creation of non-functional components",
            "Basic creation of non-functional components");
    }

    @org.junit.Test
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getNFFactory();
        Map context = new HashMap();

        root = (Component) f.newComponent("functionalTests.component.nonfunctional.adl.dummyPrimitive",
                context);

        Fractal.getLifeCycleController(root).startFc();

        DummyControllerItf ref = (DummyControllerItf) root.getFcInterface(
                "dummy-membrane");
        String name = ref.dummyMethodWithResult();
        ref.dummyVoidMethod("Message");
        Assert.assertTrue(root instanceof ProActiveNFComponentRepresentative);
    }
}
