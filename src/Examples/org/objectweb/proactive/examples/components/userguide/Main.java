package org.objectweb.proactive.examples.components.userguide;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.components.userguide.primitive.ComputeItf;
import org.objectweb.proactive.examples.components.userguide.primitive.One;
import org.objectweb.proactive.examples.components.userguide.primitive.PrimitiveComputer;
import org.objectweb.proactive.examples.components.userguide.primitive.PrimitiveMaster;


//TODO replace false and true by TypeFactory.SERVER ...
public class Main {
    public static void main(String[] args) {
//System.out.println("Launch and deploy a simple AO:");
//        Main.launchAndDeployAO();

        //        System.out.println("Launch primitive component example");
        //        Main.launchFirstPrimitive();
        //System.out.println("Launch component assembly example");
        //        Main.launchWithoutADL();
        
                System.out.println("Launch and deploy component assembly example");
                Main.launchAndDeployWithoutADL();
        
        //        System.out.println("Launch component assembly example with ADL");
        //        Main.launchOneWithADL();
        
//                System.out.println(
//                    "Launch and deploy component assembly example with ADL");
//                        Main.launchAndDeployWithADL();

        //System.err.println("The END...");
        //System.exit(0);
    }

    private static void launchAndDeployAO() {
        ProActiveDescriptor pad = null;
        try {
            pad = PADeployment.getProactiveDescriptor(Main.class.getResource("deploymentDescriptor.xml")
                    .getPath());
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        VirtualNode vn = pad.getVirtualNode("primitive-node");
        System.out.println("Main.launchAndDeployAO() activate");
        vn.activate();
        A a = null;
        try {
            System.out.println("Main.launchAndDeployAO() new Active");
            a = (A) PAActiveObject.newActive("org.objectweb.proactive.examples.components.userguide.A",
                    new Object[0], vn.getNode());
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        System.out.println("Main.launchAndDeployAO() creation OK");
        System.err.println("Random : " + a.random());
        PAActiveObject.terminateActiveObject(a, false);
        try {
            pad.killall(false);
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    private static void launchFirstPrimitive() {
        try {
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory typeFact = Fractal.getTypeFactory(boot);
            GenericFactory genericFact = Fractal.getGenericFactory(boot);
            Component primitiveComputer = null;

            // type of PrimitiveComputer component
            ComponentType computerType = typeFact.createFcType(new InterfaceType[] {
                    typeFact.createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    typeFact.createFcItfType("on", One.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });

            // component creation
            primitiveComputer = genericFact.newFcInstance(computerType, new ControllerDescription("root",
                Constants.PRIMITIVE), new ContentDescription(PrimitiveComputer.class.getName()));

            // start PrimitiveComputer component
            Fractal.getLifeCycleController(primitiveComputer).startFc();
            ((LifeCycleController) primitiveComputer.getFcInterface("lifecycle-controller")).startFc();

            // get the compute-itf interface
            ComputeItf itf = ((ComputeItf) primitiveComputer.getFcInterface("compute-itf"));
            One itf2 = ((One) primitiveComputer.getFcInterface("on"));
            System.err.println("itf2 class: " + itf2.getClass());
            ;
            // call component
            itf.doNothing();
            int result = itf.compute(5);
            String result2 = itf2.helloWorld("Me");

            System.out.println("Result of computation whith 5 is: " + result); //display 10
            System.out.println("Result2 of hello is: " + result2); //display 10
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launchWithoutADL() {
        try {
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory typeFact = Fractal.getTypeFactory(boot);
            GenericFactory genericFact = Fractal.getGenericFactory(boot);

            // component types: PrimitiveComputer, PrimitiveMaster, CompositeWrapper
            ComponentType computerType = typeFact.createFcType(new InterfaceType[] { typeFact
                    .createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });
            ComponentType masterType = typeFact.createFcType(new InterfaceType[] {
                    typeFact.createFcItfType("run", Runnable.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    typeFact.createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.CLIENT,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });
            ComponentType wrapperType = typeFact.createFcType(new InterfaceType[] { typeFact.createFcItfType(
                    "run", Runnable.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE) });

            // components creation
            Component primitiveComputer = genericFact.newFcInstance(computerType, new ControllerDescription(
                "PrimitiveComputer", Constants.PRIMITIVE), new ContentDescription(PrimitiveComputer.class
                    .getName()));
            Component primitiveMaster = genericFact.newFcInstance(masterType, new ControllerDescription(
                "PrimitiveMaster", Constants.PRIMITIVE), new ContentDescription(PrimitiveMaster.class
                    .getName()));
            Component compositeWrapper = genericFact.newFcInstance(wrapperType, new ControllerDescription(
                "CompositeWrapper", Constants.COMPOSITE), null);

            // component assembling
            Fractal.getContentController(compositeWrapper).addFcSubComponent(primitiveComputer);
            Fractal.getContentController(compositeWrapper).addFcSubComponent(primitiveMaster);
            primitiveMaster.getFcInterface("run");
            Fractal.getBindingController(compositeWrapper).bindFc("run",
                    primitiveMaster.getFcInterface("run"));
            Fractal.getBindingController(primitiveMaster).bindFc("compute-itf",
                    primitiveComputer.getFcInterface("compute-itf"));

            // start CompositeWrapper component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the run interface
            Runnable itf = ((Runnable) compositeWrapper.getFcInterface("run"));

            // call component
            itf.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launchAndDeployWithoutADL() {
        try {
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory typeFact = Fractal.getTypeFactory(boot);
            ProActiveGenericFactory genericFact = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);

            ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(Main.class
                    .getResource("deploymentDescriptor.xml").getPath());
            //context.put("deployment-descriptor", deploymentDescriptor);
            deploymentDescriptor.activateMappings();
            VirtualNode vnode = deploymentDescriptor.getVirtualNode("primitive-node");
            vnode.activate();
            Node node1 = vnode.getNode();
//            Node[] nodes = vnode.getNodes();
            
            

            // component types: PrimitiveComputer, PrimitiveMaster, CompositeWrapper
            ComponentType computerType = typeFact.createFcType(new InterfaceType[] { typeFact
                    .createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });
            ComponentType masterType = typeFact.createFcType(new InterfaceType[] {
                    typeFact.createFcItfType("run", Runnable.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    typeFact.createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.CLIENT,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });
            ComponentType wrapperType = typeFact.createFcType(new InterfaceType[] { typeFact.createFcItfType(
                    "run", Runnable.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE) });

            // components creation
            Component primitiveComputer = genericFact.newFcInstance(computerType, new ControllerDescription(
                "PrimitiveComputer", Constants.PRIMITIVE), new ContentDescription(PrimitiveComputer.class
                    .getName()), node1);
            Component primitiveMaster = genericFact.newFcInstance(masterType, new ControllerDescription(
                "PrimitiveMaster", Constants.PRIMITIVE), new ContentDescription(PrimitiveMaster.class
                    .getName()));
            Component compositeWrapper = genericFact.newFcInstance(wrapperType, new ControllerDescription(
                "CompositeWrapper", Constants.COMPOSITE), null);

            // component assembling
            Fractal.getContentController(compositeWrapper).addFcSubComponent(primitiveComputer);
            Fractal.getContentController(compositeWrapper).addFcSubComponent(primitiveMaster);
            primitiveMaster.getFcInterface("run");
            Fractal.getBindingController(compositeWrapper).bindFc("run",
                    primitiveMaster.getFcInterface("run"));
            Fractal.getBindingController(primitiveMaster).bindFc("compute-itf",
                    primitiveComputer.getFcInterface("compute-itf"));

            // start CompositeWrapper component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the run interface
            Runnable itf = ((Runnable) compositeWrapper.getFcInterface("run"));

            // call component
            while (true) {
                Thread.sleep(1000);
                itf.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launchWithADL() {
        try {
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            // component creation
            Component compositeWrapper = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.components.userguide.adl.CompositeWrapper", context);

            // start PrimitiveComputer component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the run interface
            Runnable itf = ((Runnable) compositeWrapper.getFcInterface("run"));

            // call component
            itf.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launchOneWithADL() {
        try {
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            // component creation
            Component compositeWrapper = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.components.userguide.adl.PrimitiveComputer", context);

            // start PrimitiveComputer component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the run interface
            ComputeItf itf = ((ComputeItf) compositeWrapper.getFcInterface("compute-itf"));
            One itf2 = ((One) compositeWrapper.getFcInterface("on"));

            // call component
            System.out.println("Result compute: " + itf2.helloWorld("sss"));

            System.out.println("Result compute: " + itf.compute(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launchAndDeployWithADL() {
        try {
            // get the component Factory allowing component creation from ADL
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            // retrieve the deployment descriptor
            ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(Main.class
                    .getResource("deploymentDescriptor.xml").getPath());
            context.put("deployment-descriptor", deploymentDescriptor);
            deploymentDescriptor.activateMappings();
//            System.out.println("sleep");
//            Thread.sleep(10000);
//            System.out.println("wake up");

            // component creation
            Component compositeWrapper = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.components.userguide.adl.CompositeWrapper", context);
System.out.println("certion OK");
            // start PrimitiveComputer component
            Fractal.getLifeCycleController(compositeWrapper).startFc();
            System.out.println("start OK");

            // get the compute-itf interface
            Runnable itf = ((Runnable) compositeWrapper.getFcInterface("run"));

            // call component
            itf.run();
            System.out.println("call OK");

            Thread.sleep(1000);
            // wait for the end of execution 
            // and kill JVM created with the deployment descriptor
            deploymentDescriptor.killall(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
