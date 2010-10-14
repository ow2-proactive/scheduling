package org.ow2.proactive.resourcemanager.examples.documentation;

import org.objectweb.proactive.core.node.StartNode;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.RMInitializer;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;


public class AdminGuide {

    public static String startRM() {

        // @snippet-start JavaAPIAdmin_RM_Start
        // Creates initializer for the resource manager.
        // The 6 following lines are mandatory for starting RM in a clean JVM
        // But each variable can be set as JVM argument (i.e. -Dvar=value) instead of as API.
        RMInitializer init = new RMInitializer();

        // PAResourceManagerProperties.RM_HOME is empty. You have to start your
        // application with -Dpa.rm.home=<YOUR_RM_HOME_DIR>
        System.out.println("RM home directory = " + PAResourceManagerProperties.RM_HOME);
        init.setRMHomePath(PAResourceManagerProperties.RM_HOME.toString());
        init.setLog4jConfiguration(PAResourceManagerProperties.RM_HOME + "config/log4j/rm-log4j-server");
        init
                .setJavaSecurityPolicy(PAResourceManagerProperties.RM_HOME +
                    "config/security.java.policy-server");
        init.setProActiveConfiguration(PAResourceManagerProperties.RM_HOME +
            "config/proactive/ProActiveConfiguration.xml");
        init.setResourceManagerPropertiesConfiguration(PAResourceManagerProperties.RM_HOME +
            "config/authentication/login.cfg");

        // Starts an empty RM...
        System.out.println("Starting RM, please wait...");

        RMAuthentication auth = null;
        try {
            auth = RMFactory.startLocal(init);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Retrieves connection URL for RM: auth.getHostURL()
        String rmUrl = auth.getHostURL();
        System.out.println("RM successfully started at : " + rmUrl);
        // @snippet-end JavaAPIAdmin_RM_Start

        return rmUrl;
    }

    public static void main(String[] args) throws Exception {
        String rmUrl = AdminGuide.startRM();
        ResourceManager resourceManager = UserGuide.connect(rmUrl, "admin", "admin");

        // @snippet-start JavaAPIAdmin_NodeSource
        // creating infrastructure manager parameters
        String javaPath = System.getProperty("java.home") + "/bin/java";
        String javaOptions = "";
        Object[] infrastructureParameters = new Object[] { javaPath,
                PAResourceManagerProperties.RM_HOME.toString(), "rmi", "1099", javaOptions,
                "kisscool.inria.fr".getBytes() };

        Object[] policyParameters = new Object[] { "MY_GROUPS", "ME" };

        resourceManager.createNodeSource("MySshNodeSource", SSHInfrastructure.class.getName(),
                infrastructureParameters, StaticPolicy.class.getName(), policyParameters);

        while (resourceManager.getState().getTotalNodesNumber() != 1) {
            System.out.println("waiting....");
            Thread.sleep(1000);
        }
        System.out.println("ok");
        // @snippet-end JavaAPIAdmin_NodeSource

        StartNode.main(new String[] { "rmi://kisscool.inria.fr:1099/MyNode" });

        // @snippet-start JavaAPIAdmin_AddNode
        String nodeUrl = "rmi://kisscool.inria.fr:1099/MyNode";
        BooleanWrapper isAdded = resourceManager.addNode(nodeUrl);
        // @snippet-end JavaAPIAdmin_AddNode

        if (isAdded.booleanValue()) {
            System.out.println("The node whose url is \"" + nodeUrl +
                "\" has been added to the resource manager");
        } else {
            System.out.println("The node whose url is \"" + nodeUrl +
                "\" has not been added to the resource manager");

        }

        // @snippet-start JavaAPIAdmin_RemoveNode
        BooleanWrapper isRemoved = resourceManager.removeNode(nodeUrl, true);
        // @snippet-end JavaAPIAdmin_RemoveNode

        if (isRemoved.booleanValue()) {
            System.out.println("The node whose url is \"" + nodeUrl +
                "\" has been removed from the resource manager");
        } else {
            System.out.println("The node whose url is \"" + nodeUrl +
                "\" has not been removed from the resource manager");
        }

        // @snippet-start JavaAPIAdmin_Shutdown
        resourceManager.shutdown(true);
        // @snippet-end JavaAPIAdmin_Shutdown
    }
}
