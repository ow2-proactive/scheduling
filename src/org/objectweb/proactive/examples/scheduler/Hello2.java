package org.objectweb.proactive.examples.scheduler;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;


/** A stripped-down Active Object example.
 * The object has only one public method, sayHello()
 * The object does nothing but reflect the host its on. */
public class Hello2 implements java.io.Serializable,
    org.objectweb.proactive.scheduler.JobConstants {
    static Logger logger = ProActiveLogger.getLogger(Loggers.JOB_TEMPLATE);
    private final String message = "Hello World!";
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

    /** ProActive compulsory no-args constructor */
    public Hello2() {
    }

    /** The Active Object creates and returns information on its location
     * @return a StringWrapper which is a Serialized version, for asynchrony */
    public StringMutableWrapper sayHello() {
        return new StringMutableWrapper(this.message + "\n from " +
            getHostName() + "\n at " + dateFormat.format(new java.util.Date()));
    }

    /** finds the name of the local machine */
    static String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /** The call that starts the Acive Objects, and displays results.
     * @param args must contain the name of an xml descriptor */
    public static void main(String[] args) throws Exception {
        // Access the nodes of the descriptor file
        String xmlFile = System.getProperty(XML_DESC);

        //    	System.out.println("XML Descriptor file is set to be: " + xmlFile);
        ProActiveDescriptor descriptorPad = ProActive.getProactiveDescriptor(xmlFile);
        descriptorPad.activateMappings();

        VirtualNode vnode = descriptorPad.getVirtualNode("schedulervn");
        Node[] nodes = vnode.getNodes();

        // Creates an active instance of class Hello2 on the local node
        Hello hello = (Hello) ProActive.newActive(Hello.class.getName(), // the class to deploy
                null, // the arguments to pass to the constructor, here none
                nodes[0]); // which jvm should be used to hold the Active Object

        // get and display a value 
        StringMutableWrapper received = hello.sayHello(); // possibly remote call
        logger.info("On " + getHostName() + ", a message was received: " +
            received); // potential wait-by-necessity 

        vnode = descriptorPad.getVirtualNode("schedulervn2");
        nodes = vnode.getNodes();

        // Creates an other active instance of class Hello2 on another node
        Hello hello2 = (Hello) ProActive.newActive(Hello.class.getName(), // the class to deploy
                null, // the arguments to pass to the constructor, here none
                nodes[0]); // which jvm should be used to hold the Active Object

        // get and display a value 
        StringMutableWrapper received2 = hello2.sayHello(); // possibly remote call
        logger.info("On " + getHostName() + ", a message was received: " +
            received2); // potential wait-by-necessity 

        //        System.exit(0);
    }
}
