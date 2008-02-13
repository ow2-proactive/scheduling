package org.objectweb.proactive.examples.components.userguide.multicast;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.component.adl.Launcher;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;


public class Main {
    private static String descriptor = "";

    public static void main(String[] args) {
        if (args.length == 1) {
            descriptor = args[0];
        } else {
            descriptor = Main.class.getResource("../deploymentDescriptorOld.xml").toString();
        }

        System.err.println("Launch multicast example");
        Main.manualLauncher();
        //Main.proactiveLauncher();
    }

    private static void proactiveLauncher() {
        System.err.println("Begin Launcher");
        String arg0 = "-fractal"; // using the fractal component model
        String arg1 = "org.objectweb.proactive.examples.components.userguide.multicast.adl.Launcher"; // which component definition to load
        String arg2 = "runnable";
        String arg3 = descriptor; // the deployment descriptor for proactive

        try {
            Launcher.main(new String[] { arg0, arg1, arg2, arg3 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void manualLauncher() {
        try {
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory type_factory = Fractal.getTypeFactory(boot);
            GenericFactory cf = Fractal.getGenericFactory(boot);

            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(descriptor);
            context.put("deployment-descriptor", deploymentDescriptor);
            deploymentDescriptor.activateMappings();

            Component launcher = null;
            launcher = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.components.userguide.multicast.adl.Launcher", context);
            if (launcher == null) {
                System.err.println("Component Launcher creation failed!");
                return;
            }

            Fractal.getLifeCycleController(launcher).startFc(); //root

            //     System.out.println("Components started!");
            ((java.lang.Runnable) launcher.getFcInterface("runnable")).run();
            Thread.sleep(10000);
            deploymentDescriptor.killall(false);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
