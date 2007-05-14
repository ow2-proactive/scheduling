package functionalTests.component;

import java.util.Arrays;

import org.junit.Assert;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.type.Composite;

import functionalTests.ComponentTest;
import functionalTests.descriptor.defaultnodes.TestNodes;


public class TestComponentRemote extends ComponentTest {
    private static final String P1_NAME = "primitive-component-1";
    private static final String P2_NAME = "primitive-component-2";
    private static final String C1_NAME = "composite-component1";
    private static final String C2_NAME = "composite-component2";
    public static String MESSAGE = "-->Main";
    private static Component p1;
    private static Component p2;
    private static Component c1;
    private static Component c2;

    @org.junit.Test
    public void testCreationNewactiveComposite() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        ProActiveGenericFactory cf = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);
        ComponentType i1_i2_type = type_factory.createFcType(new InterfaceType[] {
                    type_factory.createFcItfType("i1", I1.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                    type_factory.createFcItfType("i2", I2.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
                });

        p1 = cf.newFcInstance(i1_i2_type,
                new ControllerDescription(P1_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentA.class.getName(),
                    new Object[] {  }));
        p2 = cf.newFcInstance(type_factory.createFcType(
                    new InterfaceType[] {
                        type_factory.createFcItfType("i2", I2.class.getName(),
                            TypeFactory.SERVER, TypeFactory.MANDATORY,
                            TypeFactory.SINGLE)
                    }),
                new ControllerDescription(P2_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(),
                    new Object[] {  }), TestNodes.getRemoteACVMNode());
        c1 = cf.newFcInstance(i1_i2_type,
                new ControllerDescription(C1_NAME, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }), TestNodes.getRemoteACVMNode());
        c2 = cf.newFcInstance(i1_i2_type,
                new ControllerDescription(C2_NAME, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }));

        Assert.assertEquals(Fractal.getNameController(p1).getFcName(), P1_NAME);
        Assert.assertEquals(Fractal.getNameController(p2).getFcName(), P2_NAME);
        Assert.assertEquals(Fractal.getNameController(c1).getFcName(), C1_NAME);
        Assert.assertEquals(Fractal.getNameController(c2).getFcName(), C2_NAME);
    }

    @org.junit.Test
    public void testAssemblyRemoteComposite() throws Exception {
        // ASSEMBLY
        Fractal.getContentController(c1).addFcSubComponent(p1);
        Fractal.getContentController(c2).addFcSubComponent(c1);

        Component[] c2SubComponents = Fractal.getContentController(c2)
                                             .getFcSubComponents();
        Component[] c1SubComponents = Fractal.getContentController(c1)
                                             .getFcSubComponents();
        Component[] c2_sub_components = { c1 };
        Component[] c1_sub_components = { p1 };

        Assert.assertTrue(Arrays.equals(c2SubComponents, c2_sub_components));
        Assert.assertTrue(Arrays.equals(c1SubComponents, c1_sub_components));
    }

    @org.junit.Test
    public void testBindingRemoteComposite() throws Exception {
        // BINDING
        Fractal.getBindingController(c2).bindFc("i1", c1.getFcInterface("i1"));
        Fractal.getBindingController(c1).bindFc("i1", p1.getFcInterface("i1"));
        Fractal.getBindingController(p1).bindFc("i2", c1.getFcInterface("i2"));
        Fractal.getBindingController(c1).bindFc("i2", c2.getFcInterface("i2"));
        Fractal.getBindingController(c2).bindFc("i2", p2.getFcInterface("i2"));

        // START LIFE CYCLE
        Fractal.getLifeCycleController(c2).startFc();
        Fractal.getLifeCycleController(p2).startFc();

        // INVOKE INTERFACE METHOD
        I1 i1 = (I1) c2.getFcInterface("i1");

        //I1 i1= (I1)p1.getFcInterface("i1");
        Message message = i1.processInputMessage(new Message(MESSAGE))
                            .append(MESSAGE);

        Assert.assertEquals(message.toString(),
            TestComponentRemote.MESSAGE + PrimitiveComponentA.MESSAGE +
            PrimitiveComponentB.MESSAGE + PrimitiveComponentA.MESSAGE +
            TestComponentRemote.MESSAGE);
    }
}
