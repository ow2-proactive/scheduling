package org.objectweb.proactive.examples.hello;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.extra.gcmdeployment.API;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;


public class HelloNew implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private final String message = "Hello World!";
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /** ProActive compulsory no-args constructor */
    public HelloNew() {
    }

    /** The Active Object creates and returns information on its location
     * @return a StringWrapper which is a Serialized version, for asynchrony */
    public StringMutableWrapper sayHello() {
        return new StringMutableWrapper(this.message + "\n from " + getHostName() + "\n at " +
            dateFormat.format(new java.util.Date()));
    }

    /** finds the name of the local machine */
    static String getHostName() {
        return ProActiveInet.getInstance().getInetAddress().getHostName();
    }

    
//    public static class Deployer {
//        public boolean gotNodeAttached = false;
//
//        public void nodeAttached(Node node, VirtualNode vNode) {
//            System.out.println("Recieved Node Attachment notif");
//            gotNodeAttached = true;
//        }
//        
//    }
    
    /** The call that starts the Acive Objects, and displays results.
     * @param args must contain the name of an xml descriptor */
    public static void main(String[] args) throws Exception {
        // Access the nodes of the descriptor file
        
        GCMApplicationDescriptor applicationDescriptor = API.getGCMApplicationDescriptor(new File("/home/glaurent/workspace/proactive_trunk/descriptors/helloApplicationLocal.xml"));
        
        VirtualNode vnode = applicationDescriptor.getVirtualNode("Hello");

        applicationDescriptor.startDeployment();

        while (vnode.getNbCurrentNodes() == 0) {
            Thread.sleep(1000);
        }
                
        Node firstNode = vnode.getCurrentNodes().iterator().next();
        
        // Creates an active instance of class Hello2 on the local node
        final HelloNew hello = (HelloNew) PAActiveObject.newActive(Hello.class.getName(), // the class to deploy
                null, // the arguments to pass to the constructor, here none
                firstNode); // which jvm should be used to hold the Active Object


        StringMutableWrapper received = hello.sayHello(); // possibly remote call
        logger.info("On " + getHostName() + ", a message was received: " + received); // potential wait-by-necessity 
        applicationDescriptor.kill();
        PALifeCycle.exitSuccess();
    }
}
