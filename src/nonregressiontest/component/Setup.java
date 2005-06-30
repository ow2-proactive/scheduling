package nonregressiontest.component;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.type.Composite;

import java.util.HashMap;
import java.util.Map;


/**
 * This class provides primitives for the creation of systems of components
 * @author Matthieu Morel
 */
public class Setup {
    private static ComponentType D_TYPE = null;
    private static ComponentType A_TYPE = null;
    private static ComponentType B_TYPE = null;
    private static GenericFactory CF = null;
    private static TypeFactory TF = null;

    private static void createTypes() throws Exception {
        createTypeD();
        createTypeA();
        createTypeB();
    }

    private static void init()
        throws InstantiationException, NoSuchInterfaceException {
        if ((TF == null) || (CF == null)) {
            System.setProperty("fractal.provider",
                "org.objectweb.proactive.core.component.Fractive");
            Component boot = Fractal.getBootstrapComponent();
            if (TF == null) {
                TF = Fractal.getTypeFactory(boot);
            }

            if (CF == null) {
                CF = Fractal.getGenericFactory(boot);
            }
        }
    }

    private static void createTypeB() throws Exception {
        init();
        if (B_TYPE == null) {
            B_TYPE = TF.createFcType(new InterfaceType[] {
                        TF.createFcItfType("i2", I2.class.getName(),
                            TypeFactory.SERVER, TypeFactory.MANDATORY,
                            TypeFactory.SINGLE)
                    });
        }
    }

    private static void createTypeD() throws Exception {
        init();
        if (D_TYPE == null) {
            D_TYPE = TF.createFcType(new InterfaceType[] {
                        TF.createFcItfType("i1", I1.class.getName(),
                            TypeFactory.SERVER, TypeFactory.MANDATORY,
                            TypeFactory.SINGLE),
                        TF.createFcItfType("i2", I2.class.getName(),
                            TypeFactory.CLIENT, TypeFactory.MANDATORY,
                            TypeFactory.COLLECTION)
                    });
        }
    }

    public static void createTypeA() throws Exception {
        init();
        if (A_TYPE == null) {
            A_TYPE = TF.createFcType(new InterfaceType[] {
                        TF.createFcItfType("i1", I1.class.getName(),
                            TypeFactory.SERVER, TypeFactory.MANDATORY,
                            TypeFactory.SINGLE),
                        TF.createFcItfType("i2", I2.class.getName(),
                            TypeFactory.CLIENT, TypeFactory.MANDATORY,
                            TypeFactory.SINGLE)
                    });
        }
    }

    public static Map createPrimitiveComponents() throws Exception {
        createTypes();
        Map map = new HashMap();
        map.put("primitiveA", createPrimitiveA());
        map.put("primitiveB", createPrimitiveB1());
        map.put("primitiveB2", createPrimitiveB2());
        map.put("primitiveD", createPrimitiveD());
        map.put("primitiveDbis", createPrimitiveDbis());

        return map;
    }

    public static Component createPrimitiveDbis() throws Exception {
        createTypeD();
        return CF.newFcInstance(D_TYPE,
            new ControllerDescription("primitiveDbis", Constants.PRIMITIVE),
            new ContentDescription(PrimitiveComponentDbis.class.getName(),
                new Object[] {  }));
    }

    public static Component createPrimitiveD() throws Exception {
        createTypeD();
        return CF.newFcInstance(D_TYPE,
            new ControllerDescription("primitiveD", Constants.PRIMITIVE),
            new ContentDescription(PrimitiveComponentD.class.getName(),
                new Object[] {  }));
    }

    public static Component createPrimitiveB2() throws Exception {
        createTypeB();
        return CF.newFcInstance(B_TYPE,
            new ControllerDescription("primitiveB2", Constants.PRIMITIVE),
            new ContentDescription(PrimitiveComponentB.class.getName(),
                new Object[] {  }));
    }

    public static Component createPrimitiveB1() throws Exception {
        createTypeB();
        return CF.newFcInstance(B_TYPE,
            new ControllerDescription("primitiveB1", Constants.PRIMITIVE),
            new ContentDescription(PrimitiveComponentB.class.getName(),
                new Object[] {  }));
    }

    public static Component createRemotePrimitiveB1() throws Exception {
        createTypeB();
        return CF.newFcInstance(B_TYPE,
            new ControllerDescription("primitiveB1", Constants.PRIMITIVE),
            new ContentDescription(PrimitiveComponentB.class.getName(),
                new Object[] {  }, TestNodes.getLocalVMNode()));
    }
    
    public static Component createRemoteSlowPrimitiveB() throws Exception {
        createTypeB();
        return CF.newFcInstance(B_TYPE,
            new ControllerDescription("slowPrimitiveB1", Constants.PRIMITIVE),
            new ContentDescription(SlowPrimitiveComponentB.class.getName(),
                new Object[] {  }, TestNodes.getLocalVMNode()));
    }

    public static Component createPrimitiveA() throws Exception {
        createTypeA();
        return CF.newFcInstance(A_TYPE,
            new ControllerDescription("primitiveA", Constants.PRIMITIVE),
            new ContentDescription(PrimitiveComponentA.class.getName(),
                new Object[] {  }));
    }
    
    public static Component createSlowPrimitiveA() throws Exception {
        createTypeA();
        return CF.newFcInstance(A_TYPE,
            new ControllerDescription("slowPrimitiveA", Constants.PRIMITIVE),
            new ContentDescription(SlowPrimitiveComponentA.class.getName(),
                new Object[] {  }));
    }

    public static Map createCompositeComponents() throws Exception {
        Map map = Setup.createPrimitiveComponents();
        Component compositeA = createCompositeA();
        Component compositeB1 = createCompositeB1();
        map.put("compositeA", compositeA);
        map.put("compositeB1", compositeB1);

        return map;
    }

    public static Component createCompositeB1() throws Exception {
        return createCompositeOfTypeB("compositeB1");
    }

    public static Component createCompositeOfTypeA(String name) throws Exception {
        createTypeA();
        Component composite = CF.newFcInstance(A_TYPE,
                new ControllerDescription(name, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }));
        return composite;
    }

    public static Component createCompositeOfTypeB(String name) throws Exception {
        createTypeB();
        Component composite = CF.newFcInstance(B_TYPE,
                new ControllerDescription(name, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }));
        return composite;
    }
    


    public static Component createSynchronousCompositeOfTypeB(String name) throws Exception {
        createTypeB();
        Component composite = CF.newFcInstance(B_TYPE,
                new ControllerDescription(name, Constants.COMPOSITE, Constants.SYNCHRONOUS),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }));
        return composite;
    }

    public static Component createSynchronousCompositeOfTypeA(String name) throws Exception {
        createTypeA();
        Component composite = CF.newFcInstance(A_TYPE,
                new ControllerDescription(name, Constants.COMPOSITE, Constants.SYNCHRONOUS),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }));
        return composite;
    }

    public static Component createRemoteCompositeB1() throws Exception {
        createTypeB();
        Component compositeB1 = CF.newFcInstance(B_TYPE,
                new ControllerDescription("compositeB1", Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }, TestNodes.getLocalVMNode()));
        return compositeB1;
    }

    public static Component createCompositeA() throws Exception {
        createTypeA();
        Component compositeA = CF.newFcInstance(A_TYPE,
                new ControllerDescription("compositeA", Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(),
                    new Object[] {  }));
        return compositeA;
    }
}
