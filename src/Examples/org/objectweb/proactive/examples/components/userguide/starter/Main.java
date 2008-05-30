package org.objectweb.proactive.examples.components.userguide.starter;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Main {
    public static void main(String[] args) throws Exception {
        PAProperties.FRACTAL_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
        GCMApplication gcma = PAGCMDeployment
                .loadApplicationDescriptor(Main.class
                        .getResource("/org/objectweb/proactive/examples/components/userguide/starter/applicationDescriptor.xml"));
        gcma.startDeployment();

        Factory factory = FactoryFactory.getFactory();
        HashMap<String, GCMApplication> context = new HashMap<String, GCMApplication>(1);
        context.put("deployment-descriptor", gcma);

        // creates server component
        Component server = (Component) factory.newComponent(
                "org.objectweb.proactive.examples.components.userguide.starter.Server", context);

        // creates client component
        Component client = (Component) factory.newComponent(
                "org.objectweb.proactive.examples.components.userguide.starter.Client", context);

        // bind components
        BindingController bc = ((BindingController) client.getFcInterface("binding-controller"));
        bc.bindFc("s", server.getFcInterface("s"));

        // start components
        Fractal.getLifeCycleController(server).startFc();
        Fractal.getLifeCycleController(client).startFc();

        // launch the application
        ((Runnable) client.getFcInterface("m")).run();

        // stop components
        Fractal.getLifeCycleController(client).stopFc();
        Fractal.getLifeCycleController(server).stopFc();

        System.exit(0);
    }
}
