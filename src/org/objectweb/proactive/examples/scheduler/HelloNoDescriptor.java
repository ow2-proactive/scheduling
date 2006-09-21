package org.objectweb.proactive.examples.scheduler;

import java.net.UnknownHostException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.scheduler.Scheduler;


/** A stripped-down Active Object example.
 * The object has only one public method, sayHello()
 * The object does nothing but reflect the host its on. */
public class HelloNoDescriptor implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.JOB_TEMPLATE);
    private final String message = "Hello World!";
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

    /** ProActive compulsory no-args constructor */
    public HelloNoDescriptor() {
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
        // Get the scheduler URL from the main argument and connect to the scheduler
        String schedulerURL = args[0];
        Scheduler scheduler = Scheduler.connectTo(schedulerURL);

        // demand from the scheduler 1 node and tell it that the estimated time
        // for the job to finish is about 3 seconds
        Vector nodes = scheduler.getNodes(1, 3);

        Node mainNode = (Node) nodes.get(0);

        // Creates an active instance of class Hello2 on the local node
        Hello hello = (Hello) ProActive.newActive(Hello.class.getName(), // the class to deploy
                null, // the arguments to pass to the constructor, here none
                mainNode); // which jvm should be used to hold the Active Object

        // get and display a value 
        StringMutableWrapper received = hello.sayHello(); // possibly remote call
        logger.info("On " + getHostName() + ", a message was received: " +
            received); // potential wait-by-necessity 

        // we should think of freeing the nodes here
        scheduler.del(mainNode.getNodeInformation().getJobID());
    }
}
